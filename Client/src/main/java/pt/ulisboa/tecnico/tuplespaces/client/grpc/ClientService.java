package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.nameserver.contract.*;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class ClientService {

  private NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;
  private List<TupleSpacesReplicaGrpc.TupleSpacesReplicaStub> tupleSpacesStubs;
  private List<TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub> tupleSpacesBlockingStubs;
  private List<ManagedChannel> channels;
  OrderedDelayer delayer;

  private boolean debug = false;
  private static final String TUPLE_SPACES = "TupleSpaces";

  public ClientService(String namingServer_host, String namingServer_port, boolean debug) {
    this.debug = debug;
    delayer = new OrderedDelayer(3); // TODO: alterar

    final String namingServer_target = namingServer_host + ":" + namingServer_port;
    // Set up naming server gRPC stub
    final ManagedChannel namingServerChannel = ManagedChannelBuilder.forTarget(namingServer_target).usePlaintext()
        .build();
    this.namingServerStub = NamingServerServiceGrpc.newBlockingStub(namingServerChannel);

    tupleSpacesStubs = new ArrayList<>(3);
    tupleSpacesBlockingStubs = new ArrayList<>(3);
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

    // Clear the lists (Redundant since lookup is only called once in the
    // constructor)
    tupleSpacesStubs.clear();
    tupleSpacesBlockingStubs.clear();
    channels.clear();

    for (int i = 0; i < 3; i++) {
      tupleSpacesStubs.add(null);
      tupleSpacesBlockingStubs.add(null);
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
