package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.nameserver.contract.*;

import java.util.ArrayList;
import java.util.List;

public class ClientService {

  private final NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;
  private List<TupleSpacesReplicaGrpc.TupleSpacesReplicaStub> tupleSpacesStubs;
  private boolean debug = false;

  public ClientService(String host, String port, boolean debug) {
    this.debug = debug;

    final String namingServer_target = host + ":" + port;
    // Set up naming server gRPC stub
    final ManagedChannel namingServerChannel = ManagedChannelBuilder.forTarget(namingServer_target).usePlaintext().build();
    this.namingServerStub = NamingServerServiceGrpc.newBlockingStub(namingServerChannel);

    tupleSpacesStubs = new ArrayList<>();
    lookup("TupleSpaces", "");

  }

  public void put(String tuple) {
    if (debug) {
      System.err.println("Putting tuple: " + tuple);
    }

    PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
    try {
      tupleSpacesStubs.put(request);//TODO: 

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
      ReadResponse response = tupleSpacesStubs.read(request); //TODO: 

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
      TakePhase1Response response = tupleSpacesStubs.takePhase1(request);//TODO: 

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
      getTupleSpacesStateResponse response = tupleSpacesStubs.getTupleSpacesState(request);//TODO: 

      System.out.println("OK");
      return response;
    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " + e.getStatus().getDescription());
      return null;
    }
  }

  public void lookup(String serviceName, String qualifier) {
    if (debug) {
      System.err.println("Looking up with service name and qualifier: " + serviceName + qualifier);
    }

    // Clear the stubs list
    tupleSpacesStubs.clear();

    LookUpRequest request = LookUpRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build();
    try {
      LookUpResponse response = namingServerStub.lookup(request);

      if (response != null && response.getServerEntryCount() > 0) {
        // For each server address in the response, create a stub
        for (ServerEntry serverEntry : response.getServerEntryList()) {
          ServerAddress address = serverEntry.getAddress();
          ManagedChannel channel = ManagedChannelBuilder.forAddress(address.getHost(), address.getPort()).usePlaintext().build();
          TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stub = TupleSpacesReplicaGrpc.newStub(channel);
          tupleSpacesStubs.add(stub);
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
