package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;

public class ClientService {

  private final TupleSpacesGrpc.TupleSpacesBlockingStub stub;

  public ClientService(String host, String port) {
    final String target = host + ":" + port;
    final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.stub = TupleSpacesGrpc.newBlockingStub(channel);
  }

  public void put(String tuple) {
    PutRequest request = PutRequest.newBuilder().setTuple(tuple).build();
    stub.put(request);

    System.out.println("OK");
  }

  public String read(String pattern) {
    ReadRequest request = ReadRequest.newBuilder().setPattern(pattern).build();
    ReadResponse response = stub.read(request);

    System.out.println("OK");
    return response.getResult();
  }

  public String take(String pattern) {
    TakeRequest request = TakeRequest.newBuilder().setPattern(pattern).build();
    TakeResponse response = stub.take(request);

    System.out.println("OK");
    return response.getResult();
  }

  public GetTupleSpacesStateResponse getTupleSpacesState() {
    GetTupleSpacesStateRequest request = GetTupleSpacesStateRequest.getDefaultInstance();
    GetTupleSpacesStateResponse response = stub.getTupleSpacesState(request);

    System.out.println("OK");
    return response;
  }
}
