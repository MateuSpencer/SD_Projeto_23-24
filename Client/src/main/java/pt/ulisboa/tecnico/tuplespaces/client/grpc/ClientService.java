package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;
import pt.ulisboa.tecnico.nameserver.contract.*;

public class ClientService {

  private final TupleSpacesGrpc.TupleSpacesBlockingStub tupleSpacesStub;
  private final NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;
  private boolean debug = false;

  public ClientService(String host, String port, boolean debug) {
    this.debug = debug;

    final String target = host + ":" + port;

    // Set up tuple spaces gRPC stub
    final ManagedChannel tupleSpacesChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.tupleSpacesStub = TupleSpacesGrpc.newBlockingStub(tupleSpacesChannel);

    // Set up naming server gRPC stub
    final ManagedChannel namingServerChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.namingServerStub = NamingServerServiceGrpc.newBlockingStub(namingServerChannel);
  }

  public void put(String tuple) {
    if (debug) {
      System.err.println("Putting tuple: " + tuple);
    }

    PutRequest request = PutRequest.newBuilder().setTuple(tuple).build();
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

    ReadRequest request = ReadRequest.newBuilder().setPattern(pattern).build();
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

    TakeRequest request = TakeRequest.newBuilder().setPattern(pattern).build();
    try {
      TakeResponse response = tupleSpacesStub.take(request);

      System.out.println("OK");
      return response.getResult();
    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " + e.getStatus().getDescription());
      return null;
    }
  }

  public GetTupleSpacesStateResponse getTupleSpacesState() {
    if (debug) {
      System.err.println("Getting tuple spaces state");
    }

    GetTupleSpacesStateRequest request = GetTupleSpacesStateRequest.getDefaultInstance();
    try {
      GetTupleSpacesStateResponse response = tupleSpacesStub.getTupleSpacesState(request);

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

      //TODO: handle response
      System.out.println("OK");

      return ""; //TODO: return 
    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " + e.getStatus().getDescription());
      return null;
    }
  }
}
