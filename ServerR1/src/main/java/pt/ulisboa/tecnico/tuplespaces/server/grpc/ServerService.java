package pt.ulisboa.tecnico.tuplespaces.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.nameserver.contract.*;

public class ServerService {
  private final NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;
  private final ManagedChannel namingServerChannel;
  private boolean debug = false;

  public ServerService(String host, String port, boolean debug) {
    this.debug = debug;
    final String target = host + ":" + port;

    // Set up naming server gRPC stub
    this.namingServerChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.namingServerStub = NamingServerServiceGrpc.newBlockingStub(namingServerChannel);
  }

  public ManagedChannel getNamingServerChannel() {
    return namingServerChannel;
  }

  public void register(String serviceName, String host, int port, String qualifier) throws StatusRuntimeException {
    if (debug) {
      System.err.println("Registering server with port: " + port + " and qualifier: " + qualifier);
    }
    ServerAddress address = ServerAddress.newBuilder().setHost(host).setPort(port).build();

    RegisterRequest request = RegisterRequest.newBuilder().setServiceName(serviceName).setAddress(address)
        .setQualifier(qualifier).build();

    this.namingServerStub.register(request);

    if (debug) {
      System.err.println("Server registered successfully");
    }
  }

  public void delete(String serviceName, String host, int port) throws StatusRuntimeException {
    if (debug) {
      System.err.println("Deleting server with serviceName: " + serviceName + ", host: " + host + ", port: " + port);
    }

    ServerAddress address = ServerAddress.newBuilder().setHost(host).setPort(port).build();

    DeleteRequest request = DeleteRequest.newBuilder().setServiceName(serviceName).setAddress(address).build();

    this.namingServerStub.delete(request);

    if (debug) {
      System.err.println("Server deleted successfully");
    }
  }
}
