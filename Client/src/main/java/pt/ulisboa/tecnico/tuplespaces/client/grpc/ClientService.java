package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;

public class ClientService {

  /*TODO: The gRPC client-side logic should be here.
        This should include a method that builds a channel and stub,
        as well as individual methods for each remote operation of this service. */

  private final TupleSpacesGrpc.TupleSpacesBlockingStub stub;

  public ClientService(String host, String port) {
    final String target = host + ":" + port;
    final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.stub = TupleSpacesGrpc.newBlockingStub(channel);
  }

  public void put(String tuple) {
    PutRequest request = PutRequest.getDefaultInstance();
    stub.put(request);

    System.out.println("OK");
  }

  public void read(String tuple) {
    ReadRequest request = ReadRequest.getDefaultInstance();
    stub.read(request);

    System.out.println("OK");
  }

  public void take(String tuple) {
    TakeRequest request = TakeRequest.getDefaultInstance();
    stub.take(request);

    System.out.println("OK");
  }

  public void getTupleSpacesState(String qualifier) {
    GetTupleSpacesStateRequest request = GetTupleSpacesStateRequest.getDefaultInstance();
    stub.getTupleSpacesState(request);

    System.out.println("OK");
  }
}
