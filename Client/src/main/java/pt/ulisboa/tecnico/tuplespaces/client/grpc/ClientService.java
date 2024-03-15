package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.nameserver.contract.*;

import java.util.ArrayList;
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
  private List<String> tupleSpacesQualifiers;
  private List<ManagedChannel> channels;

  private boolean debug = false;
  private static final String TUPLE_SPACES = "TupleSpaces";

  public ClientService(String namingServer_host, String namingServer_port, boolean debug) {
    this.debug = debug;

    final String namingServer_target = namingServer_host + ":" + namingServer_port;
    // Set up naming server gRPC stub
    final ManagedChannel namingServerChannel = ManagedChannelBuilder.forTarget(namingServer_target).usePlaintext()
        .build();
    this.namingServerStub = NamingServerServiceGrpc.newBlockingStub(namingServerChannel);

    tupleSpacesStubs = new ArrayList<>();
    tupleSpacesBlockingStubs = new ArrayList<>();
    tupleSpacesQualifiers = new ArrayList<>();
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

    // Send the put request to all replicas
    for (TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stub : tupleSpacesStubs) {
        CompletableFuture<PutResponse> future = new CompletableFuture<>();
        stub.put(request, new StreamObserver<PutResponse>() {
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
    for (TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stub : tupleSpacesStubs) {
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

      stub.read(request, responseObserver);
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
    /*
     * if (debug) {
     * System.err.println("Taking with pattern: " + pattern);
     * }
     * 
     * TakePhase1Request request =
     * TakePhase1Request.newBuilder().setSearchPattern(pattern).build();
     * try {
     * TakePhase1Response response = tupleSpacesStubs.takePhase1(request);//TODO:
     * 
     * System.out.println("OK");
     * return "TODO - here just to compile";//response.getResult();
     * } catch (StatusRuntimeException e) {
     * System.out.println("Caught exception with description: " +
     * e.getStatus().getDescription());
     * return null;
     * }
     */
    return null; // TODO: remove
  }

  public getTupleSpacesStateResponse getTupleSpacesState(String qualifier) {
    if (debug) {
      System.err.println("Getting tuple spaces state");
    }

    // find the stub with the given qualifier
    TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub stub = null;
    for (int i = 0; i < tupleSpacesQualifiers.size(); i++) {
      if (tupleSpacesQualifiers.get(i).equals(qualifier)) {
        stub = tupleSpacesBlockingStubs.get(i);
        break;
      }
    }

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

  public void lookup(String serviceName, String qualifier) {
    if (debug) {
      System.err.println("Looking up with service name and qualifier: " + serviceName + qualifier);
    }

    // Clear the lists (Redundant since lookup is only called once in the
    // constructor)
    tupleSpacesStubs.clear();
    tupleSpacesBlockingStubs.clear();
    tupleSpacesQualifiers.clear();
    channels.clear();

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

          tupleSpacesQualifiers.add(serverEntry.getQualifier());

          ManagedChannel channel = ManagedChannelBuilder.forAddress(address.getHost(), address.getPort()).usePlaintext()
              .build();
          channels.add(channel);

          TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stub = TupleSpacesReplicaGrpc.newStub(channel);
          tupleSpacesStubs.add(stub);

          TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub blockingStub = TupleSpacesReplicaGrpc
              .newBlockingStub(channel);
          tupleSpacesBlockingStubs.add(blockingStub);
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
}
