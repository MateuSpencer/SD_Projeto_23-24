package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.nameserver.contract.*;

public class ClientService {

  private final NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;
  private TupleSpacesReplicaGrpc.TupleSpacesReplicaBlockingStub tupleSpacesStub; //TODO: should be non blocking stub
  private boolean debug = false;

  public ClientService(String host, String port, boolean debug) {
    this.debug = debug;

    final String target = host + ":" + port;

    // Set up naming server gRPC stub
    final ManagedChannel namingServerChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.namingServerStub = NamingServerServiceGrpc.newBlockingStub(namingServerChannel);

    // Set up tuple spaces gRPC stub
    // TODO: this should probably happen on lookup and not with this target of course
    final ManagedChannel tupleSpacesChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.tupleSpacesStub = TupleSpacesReplicaGrpc.newBlockingStub(tupleSpacesChannel);//TODO: should be non blocking stub
  }

  public void put(String tuple) {
    if (debug) {
      System.err.println("Putting tuple: " + tuple);
    }

    PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
    try {
      tupleSpacesStub.put(request);

      System.out.println("OK");
    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " + e.getStatus().getDescription());
    }
  }

  public String read(String pattern) {
    if (debug) {
      System.err.println("Reading with pattern: " + pattern);
    }

    ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();
    try {
      ReadResponse response = tupleSpacesStub.read(request);

      System.out.println("OK");
      return response.getResult();
    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " + e.getStatus().getDescription());
      return null;
    }

  }

  public String take(String pattern) {
    if (debug) {
      System.err.println("Taking with pattern: " + pattern);
    }

    TakePhase1Request request = TakePhase1Request.newBuilder().setSearchPattern(pattern).build();
    try {
      TakePhase1Response response = tupleSpacesStub.takePhase1(request);

      System.out.println("OK");
      return "TODO - here just to compile";//response.getResult();
    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " + e.getStatus().getDescription());
      return null;
    }
  }

  public getTupleSpacesStateResponse getTupleSpacesState() {
    if (debug) {
      System.err.println("Getting tuple spaces state");
    }

    getTupleSpacesStateRequest request = getTupleSpacesStateRequest.getDefaultInstance();
    try {
      getTupleSpacesStateResponse response = tupleSpacesStub.getTupleSpacesState(request);

      System.out.println("OK");
      return response;
    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " + e.getStatus().getDescription());
      return null;
    }
  }

  public String lookup(String serviceName, String qualifier) {
    if (debug) {
      System.err.println("Looking up with service name and qualifier: " + serviceName + qualifier);
    }

    LookUpRequest request = LookUpRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build();
    try {
      LookUpResponse response = namingServerStub.lookup(request);

      if (response != null && response.getServerEntryCount() > 0) {
        ServerEntry serverEntry = response.getServerEntry(0); // Assuming only one ServerEntry is returned
        ServerAddress serverAddress = serverEntry.getAddress();
        String host = serverAddress.getHost();
        int port = serverAddress.getPort();
        System.out.println("Host: " + host + ", Port: " + port);
        return host + ":" + port;
      } else {
        System.out.println("No server entry found with service name: " + serviceName + " and qualifier: " + qualifier);
        return null;
      }
    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " + e.getStatus().getDescription());
      return null;
    }
  }
}
