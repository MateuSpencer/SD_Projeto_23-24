package pt.ulisboa.tecnico.tuplespaces.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.nameserver.contract.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;

public class ServerService {
  /* private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;
  private boolean debug = false;

  public ServerService(boolean debug) {
    this.debug = debug;

    //final String target = host + ":" + port;
    String target = null; //TODO remover

    // Set up naming server gRPC stub
    final ManagedChannel namingServerChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.stub = NamingServerServiceGrpc.newBlockingStub(namingServerChannel);
  }
    
  public void register(String port, String qualifier){
    if (debug) {
        System.err.println("Registering server with port: " + port + " and qualifier: " + qualifier);
      }
  
      //RegisterRequest request = RegisterRequest.newBuilder().set
      try {
        
      } catch (StatusRuntimeException e) {
        System.out.println("Caught exception with description: " + e.getStatus().getDescription());
      }
  } */
}
