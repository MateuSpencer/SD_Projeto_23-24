package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class ServerMain {

  public static void main(String[] args) throws IOException, InterruptedException {

    System.out.println(ServerMain.class.getSimpleName());

    // receive and print arguments
    System.out.printf("Received %d arguments%n", args.length);
    for (int i = 0; i < args.length; i++) {
      System.out.printf("arg[%d] = %s%n", i, args[i]);
    }

    // check arguments
    if (args.length != 4) {
      System.err.println("Argument(s) missing!");
      System.err.println("Usage: mvn exec:java -Dexec.args=<host> <port> <qual>"); // qual?
      return;
    }

    final int port = Integer.parseInt(args[1]);
    final BindableService impl = new ServerImpl();

    // Create a new server to listen on port
    Server server = ServerBuilder.forPort(port).addService(impl).build();

    // Start the server
    server.start();

    // Server threads are running in the background.
    System.out.println("Server started");

    // Do not exit the main thread. Wait until server is terminated.
    server.awaitTermination();

  }
}
