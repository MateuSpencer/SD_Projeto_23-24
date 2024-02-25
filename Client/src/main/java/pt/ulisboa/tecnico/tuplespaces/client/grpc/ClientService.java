package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.grpc.ClientTupleSpaces;

public class ClientService {

  /*TODO: The gRPC client-side logic should be here.
        This should include a method that builds a channel and stub,
        as well as individual methods for each remote operation of this service. */

  private final ClientServiceGrpc.ClientServiceBlockingStub stub;

  public ClientService(String host, String port) {
    final String target = host + ":" + port;
    final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.stub = ClientServiceGrpc.newBlockingStub(channel);
  }

  public void put() {
    ClientTupleSpaces.PutRequest request = ClientTupleSpaces.PutRequest.getDefaultInstance();
    stub.put(request);

    System.out.println("OK");
  }

  public void read() {
    ClientTupleSpaces.ReadRequest request = ClientTupleSpaces.ReadRequest.getDefaultInstance();
    stub.read(request);

    System.out.println("OK");
  }

  public void take() {
    ClientTupleSpaces.TakeRequest request = ClientTupleSpaces.TakeRequest.getDefaultInstance();
    stub.take(request);

    System.out.println("OK");
  }

  public void getTupleSpacesState() {
    ClientTupleSpaces.GetTupleSpacesStateRequest request = ClientTupleSpaces.GetTupleSpacesStateRequest.getDefaultInstance();
    stub.getTupleSpacesState(request);

    System.out.println("OK");
  }
}
