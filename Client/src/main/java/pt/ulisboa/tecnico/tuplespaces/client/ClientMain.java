package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.Arrays;

import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

public class ClientMain {
    public static void main(String[] args) {

        final String namingServerHost = "localhost";
        final String namingServerPort = "5001";
        boolean debug = Arrays.asList(args).contains("-debug");

        if(debug){
            System.out.println("Debug mode enabled");

            System.out.println(ClientMain.class.getSimpleName());

            // receive and print arguments
            System.out.printf("Received %d arguments%n", args.length);
            for (int i = 0; i < args.length; i++) {
                System.out.printf("arg[%d] = %s%n", i, args[i]);
            }
        }

        // check arguments
        if ((debug && args.length != 1) || (!debug && args.length != 0)) {
            System.err.println("Invalid number of arguments");
            System.err.printf("Usage: mvn exec:java%s\n", debug ? " -Dexec.args= <-debug>" : "");
            return;
        }

        ClientService clientService = new ClientService(namingServerHost, namingServerPort, debug);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (ManagedChannel channel : clientService.getChannels()) {
                channel.shutdownNow();
                System.out.println("Channel shutdownNow() called");
            }
        }));

        CommandProcessor parser = new CommandProcessor(clientService);
        parser.parseInput();
        System.out.println("out");
        
    }
}
