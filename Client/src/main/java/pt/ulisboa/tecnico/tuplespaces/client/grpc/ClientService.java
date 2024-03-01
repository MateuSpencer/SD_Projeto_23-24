package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;

public class ClientService {

  private final TupleSpacesGrpc.TupleSpacesBlockingStub stub;
  private boolean debug = false;

  public ClientService(String host, String port, boolean debug) {
    this.debug = debug;
    final String target = host + ":" + port;
    final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.stub = TupleSpacesGrpc.newBlockingStub(channel);
  }

  public void put(String tuple) {
    if (debug) {
      System.err.println("Putting tuple: " + tuple);
    }

    PutRequest request = PutRequest.newBuilder().setTuple(tuple).build();
    try {
      stub.put(request);

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
      ReadResponse response = stub.read(request);

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
      TakeResponse response = stub.take(request);

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
      GetTupleSpacesStateResponse response = stub.getTupleSpacesState(request);

      System.out.println("OK");
      return response;
    } catch (StatusRuntimeException e) {
      System.out.println("Caught exception with description: " + e.getStatus().getDescription());
      return null;
    }
  }
}
