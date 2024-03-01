package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

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
    stub.put(request);

    System.out.println("OK");
  }

  public String read(String pattern) {
    if (debug) {
      System.err.println("Reading with pattern: " + pattern);
    }

    ReadRequest request = ReadRequest.newBuilder().setPattern(pattern).build();
    ReadResponse response = stub.read(request);

    System.out.println("OK");
    return response.getResult();
  }

  public String take(String pattern) {
    if (debug) {
      System.err.println("Taking with pattern: " + pattern);
    }

    TakeRequest request = TakeRequest.newBuilder().setPattern(pattern).build();
    TakeResponse response = stub.take(request);

    System.out.println("OK");
    return response.getResult();
  }

  public GetTupleSpacesStateResponse getTupleSpacesState() {
    if (debug) {
      System.err.println("Getting tuple spaces state");
    }
    
    GetTupleSpacesStateRequest request = GetTupleSpacesStateRequest.getDefaultInstance();
    GetTupleSpacesStateResponse response = stub.getTupleSpacesState(request);

    System.out.println("OK");
    return response;
  }
}
