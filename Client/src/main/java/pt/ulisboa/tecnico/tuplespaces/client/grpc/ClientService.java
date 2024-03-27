package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.nameserver.contract.*;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientService {

  private NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;
  private List<TupleSpacesReplicaGrpc.TupleSpacesReplicaStub> tupleSpacesStubs;
  private List<TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub> tupleSpacesBlockingStubs;
  private List<ManagedChannel> channels;
  OrderedDelayer delayer;

  private boolean debug = false;
  private static final String TUPLE_SPACES = "TupleSpaces";
  private int numServers = 0;
  private final int clientId;

  public ClientService(int numServers, int clientId, boolean debug) {
    this.debug = debug;
    this.numServers = numServers;
    this.clientId = clientId;
    final String namingServer_host = "localhost";
    final String namingServer_port = "5001";
    delayer = new OrderedDelayer(numServers);

    final String namingServer_target = namingServer_host + ":" + namingServer_port;
    // Set up naming server gRPC stub
    final ManagedChannel namingServerChannel = ManagedChannelBuilder.forTarget(namingServer_target).usePlaintext()
        .build();
    this.namingServerStub = NamingServerServiceGrpc.newBlockingStub(namingServerChannel);

    tupleSpacesStubs = new ArrayList<>(numServers);
    tupleSpacesBlockingStubs = new ArrayList<>(numServers);
    channels = new ArrayList<>();

    lookup(TUPLE_SPACES, "");

    namingServerChannel.shutdown();
  }

  public List<ManagedChannel> getChannels() {
    return channels;
  }

  public void put(String tuple) {
    if (debug) {
      System.err.println("Putting tuple: " + tuple);
    }

    PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();

    // Create a list to hold the futures
    List<CompletableFuture<PutResponse>> futures = new ArrayList<>();

    for (Integer id : delayer) {
      CompletableFuture<PutResponse> future = new CompletableFuture<>();
      tupleSpacesStubs.get(id).put(request, new StreamObserver<PutResponse>() {
        @Override
        public void onNext(PutResponse response) {
          // The response is received
        }

        @Override
        public void onError(Throwable t) {
          future.completeExceptionally(t);
        }

        @Override
        public void onCompleted() {
          future.complete(null);
        }
      });
      futures.add(future);
    }

    // Wait for all replicas to acknowledge
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .exceptionally(t -> {
          System.out.println("Caught exception: " + t.getMessage());
          return null;
        })
        .join();

    System.out.println("OK");
  }

  public String read(String pattern) {
    if (debug) {
      System.err.println("Reading with pattern: " + pattern);
    }
    // lista de futuros
    List<CompletableFuture<String>> futures = new ArrayList<>();

    ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();

    // envia pedido de leitura a todos os replicas
    for (Integer id : delayer) {
      CompletableFuture<String> future = new CompletableFuture<>();

      StreamObserver<ReadResponse> responseObserver = new StreamObserver<ReadResponse>() {
        @Override
        public void onNext(ReadResponse response) {
          future.complete(response.getResult());
        }

        @Override
        public void onError(Throwable t) {
          System.out.println("Caught exception with description: " + t.getMessage());
          future.complete(null);
        }

        @Override
        public void onCompleted() {
          if (!future.isDone()) {
            future.complete(null);
          }
        }
      };

      tupleSpacesStubs.get(id).read(request, responseObserver);
      futures.add(future);
    }

    CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]));
    String result = null;

    try {
      result = (String) anyFuture.get(); // This will block until any future completes
      if (result != null) {
        System.out.println("OK");
      }
    } catch (InterruptedException | ExecutionException e) {
      System.out.println("Caught exception while waiting for futures to complete: " + e.getMessage());
      result = null;
    }
    return result;
  }

  public String take(String pattern) {
    if (debug) {
      System.err.println("Taking with pattern: " + pattern);
    }

    try {
      TakePhase1Request takePhase1Request = TakePhase1Request.newBuilder().setSearchPattern(pattern)
          .setClientId(clientId).build();
      String target_tuple = takePhase1(takePhase1Request, new ArrayList<>(), new AtomicInteger(0));

      if (debug) {
        System.out.println("Take Phase 1 Completed - Selected tuple: " + target_tuple);
      }

      TakePhase2Request takePhase2Request = TakePhase2Request.newBuilder().setClientId(clientId).setTuple(target_tuple)
          .build();
      takePhase2(takePhase2Request);
      
      if (debug) {
        System.out.println("Take Phase 2 Completed. Tuple " + target_tuple + " removed from tuple space.");
      }

      System.out.println("OK");
      return target_tuple;

    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " +
          e.getStatus().getDescription());
      return null;
    }
  }

  public String takePhase1(TakePhase1Request takePhase1Request, List<Integer> tupleSpacesStubsTakeIds, AtomicInteger acceptedRequests) {
    if (tupleSpacesStubsTakeIds.isEmpty()) {
      for (int i = 0; i < numServers; i++) {
        tupleSpacesStubsTakeIds.add(i);
      }
    }
    // Create a list to hold the futures
    List<CompletableFuture<TakePhase1Response>> futures = new ArrayList<>();
    // Create a list for the next tupleSpacesStubsTakeIds
    List<Integer> nextTupleSpacesStubsTakeIds = new ArrayList<>();
    List<Integer> acceptedTupleSpacesStubsTakeIds = new ArrayList<>();

    if (debug) {
      System.out.println("Take Phase 1 - Sending request to servers " + tupleSpacesStubsTakeIds + "\n");
    }
    for (Integer id : delayer) {
      if (tupleSpacesStubsTakeIds.contains(id)) {
        CompletableFuture<TakePhase1Response> future = new CompletableFuture<>();
        tupleSpacesStubs.get(id).takePhase1(takePhase1Request, new StreamObserver<TakePhase1Response>() {
          @Override
          public void onNext(TakePhase1Response response) {
            if (debug) {
              System.out.println("Take Phase 1 - Received response:\n" + response + "From replica " + id + "\n");
            }
            if (!response.getAccepted()) {
              nextTupleSpacesStubsTakeIds.add(id);
            }
            else {
              acceptedTupleSpacesStubsTakeIds.add(id);
            }
            future.complete(response);
          }

          @Override
          public void onError(Throwable t) {
            future.completeExceptionally(t);
          }

          @Override
          public void onCompleted() {
            // Do nothing
          }
        });
        futures.add(future);
      }
    }

    // Wait for all replicas to acknowledge
    try {
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
      List<String> intersection = futures.stream()
          .map(CompletableFuture::join)
          .flatMap(response -> {
            // If request was accepted, increment counter
            if (response.getAccepted()) {
              acceptedRequests.incrementAndGet();
            }
            return response.getReservedTuplesList().stream();
          })
          .collect(Collectors.toList());


      // If a strict majority of requests was accepted or all requests were accepted
      // but the intersection is empty, repeat phase 1
      if ((acceptedRequests.get() > (tupleSpacesStubs.size() / 2) && acceptedRequests.get() < tupleSpacesStubs.size())
          || (acceptedRequests.get() == tupleSpacesStubs.size() && intersection.isEmpty())) {
        if (debug) {
          System.out.println("Repeating Take Phase 1 for servers " + nextTupleSpacesStubsTakeIds + "\n");
        }
        return takePhase1(takePhase1Request, nextTupleSpacesStubsTakeIds, acceptedRequests); // Repeat phase 1 (Recursive call)
      }

      // If a minority of requests was accepted, release locks and repeat phase 1
      if (acceptedRequests.get() <= ((double)tupleSpacesStubs.size() / 2)) {
        if (debug) {
          System.out.println("Releasing locks for servers" + acceptedTupleSpacesStubsTakeIds + "and repeating Take Phase 1");
        }
        TakePhase1ReleaseRequest releaseRequest = TakePhase1ReleaseRequest.newBuilder()
            .setClientId(takePhase1Request.getClientId()).build();
        for (Integer id : delayer) {
          if (acceptedTupleSpacesStubsTakeIds.contains(id)) {
            tupleSpacesStubs.get(id).takePhase1Release(releaseRequest, new StreamObserver<TakePhase1ReleaseResponse>() {
              @Override
              public void onNext(TakePhase1ReleaseResponse response) {
                if (debug) {
                  System.out.println("Lock released for server " + id + " due to minority of requests accepted");
                }
              }

              @Override
              public void onError(Throwable t) {
                System.err.println("Error releasing lock for server " + id + ": " + t.getMessage());
              }

              @Override
              public void onCompleted() {
                // Do nothing
              }
            });
          }
        }

        int randomSleepTime = 0;

        try {
          Random rand = new Random();
          randomSleepTime = rand.nextInt(13) + 3; // Random sleep time between 3 and 15 seconds
          Thread.sleep(randomSleepTime * 1000); // Sleep for 3 to 15 seconds
        } catch (InterruptedException e) {
          System.out.println("Caught exception while sleeping: " + e.getMessage());
        }

        if (debug) {
          System.out.println("Slept for " + randomSleepTime + " seconds. Repeating phase 1.");
        }
        
        return takePhase1(takePhase1Request, new ArrayList<>(), new AtomicInteger(0)); // Repeat phase 1 (Recursive call)
      }

      // Select a tuple randomly from the intersection
      /*if (debug) {
        System.out.println("Intersection found: " + intersection);
      }*/
      return intersection.get(new Random().nextInt(intersection.size()));

    } catch (CompletionException e) {
      System.out.println("Caught exception: " + e.getCause().getMessage());
      return null;
    }
  }

  public void takePhase2(TakePhase2Request takePhase2Request) {
    // Create a list to hold the futures
    List<CompletableFuture<TakePhase2Response>> futures = new ArrayList<>();

    for (Integer id : delayer) {
      CompletableFuture<TakePhase2Response> future = new CompletableFuture<>();
      tupleSpacesStubs.get(id).takePhase2(takePhase2Request, new StreamObserver<TakePhase2Response>() {
        @Override
        public void onNext(TakePhase2Response response) {
          if (debug) {
            System.out.println("Take Phase 2 successful " + response + "from replica " + id);
          }
          future.complete(response);
        }

        @Override
        public void onError(Throwable t) {
          System.err.println("Error during takePhase2 from replica " + id + ": " + t.getMessage());
          future.completeExceptionally(t);
        }

        @Override
        public void onCompleted() {
          // Do nothing
        }
      });
      futures.add(future);
    }

    // Wait for all replicas to acknowledge
    try {
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    } catch (CompletionException e) {
      System.out.println("Error waiting for futures to complete: " + e.getCause().getMessage());
    }
  }

  public getTupleSpacesStateResponse getTupleSpacesState(String qualifier) {
    if (debug) {
      System.err.println("Getting tuple spaces state");
    }

    Integer qualifierPos = indexOfServerQualifier(qualifier);
    TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub stub = tupleSpacesBlockingStubs.get(qualifierPos);

    getTupleSpacesStateRequest request = getTupleSpacesStateRequest.getDefaultInstance();
    try {
      getTupleSpacesStateResponse response = stub.getTupleSpacesState(request);

      return response;
    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " +
          e.getStatus().getDescription());
      return null;
    }
  }

  public void setDelay(int id, int delay) {
    delayer.setDelay(id, delay);

    if (debug) {
      System.out.println("After setting the delay, I'll test it");
      for (Integer i : delayer) {
        System.out.println("Now I can send request to stub[" + i + "]");
      }
      System.out.println("Done.");
    }
  }

  public void lookup(String serviceName, String qualifier) {
    if (debug) {
      System.err.println("Looking up with service name and qualifier: " + serviceName + qualifier);
    }

    LookUpRequest request = LookUpRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build();
    try {
      LookUpResponse response = namingServerStub.lookup(request);

      if (response != null && response.getServerEntryCount() > 0) {
        // For each server address in the response, create a channels and two stubs
        for (ServerEntry serverEntry : response.getServerEntryList()) {
          ServerAddress address = serverEntry.getAddress();
          if (debug) {
            System.err.println("Received server entry: " + address.getHost() + ":" + address.getPort() + "-"
                + serverEntry.getQualifier());
          }

          int qualifierPos = indexOfServerQualifier(serverEntry.getQualifier());

          ManagedChannel channel = ManagedChannelBuilder.forAddress(address.getHost(), address.getPort()).usePlaintext()
              .build();
          channels.add(channel);

          TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stub = TupleSpacesReplicaGrpc.newStub(channel);
          tupleSpacesStubs.add(qualifierPos, stub); // Add stub to the tuplespacesstubs list in the position
                                                    // qualifierPos

          TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub blockingStub = TupleSpacesReplicaGrpc
              .newBlockingStub(channel);
          tupleSpacesBlockingStubs.add(qualifierPos, blockingStub);
        }
        return;
      } else {
        System.out.println("No server entry found with service name: " + serviceName + " and qualifier: " + qualifier);
        return;
      }
    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " + e.getStatus().getDescription());
      return;
    }
  }

  private int indexOfServerQualifier(String qualifier) {
    switch (qualifier) {
      case "A":
        return 0;
      case "B":
        return 1;
      case "C":
        return 2;
      default:
        return -1;
    }
  }
}
