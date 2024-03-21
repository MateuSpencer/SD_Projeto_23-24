package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

public class ClientMain {
    public static void main(String[] args) {
        boolean debug = Arrays.asList(args).contains("-debug");
        final int numServers = 3;

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
        if ((debug && args.length != 2) || (!debug && args.length != 1)) {
            System.err.println("Invalid number of arguments");
            System.err.printf("Usage: mvn exec:java%s\n", debug ? " -Dexec.args= <clientId> <-debug>" : " -Dexec.args= <clientId>");
            return;
        }

        int clientId = Integer.parseInt(args[0]);

        ClientService clientService = new ClientService(numServers, clientId, debug);

        AtomicBoolean cleanupDone = new AtomicBoolean(false);

        // Add a shutdown hook to close all channels when the program exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!cleanupDone.get()) {
                for (ManagedChannel channel : clientService.getChannels()) {
                    channel.shutdownNow();
                }
                cleanupDone.set(true);
            }
        }));

        CommandProcessor parser = new CommandProcessor(clientService);
        parser.parseInput();
        System.out.println("out");
        
        if (!cleanupDone.get()) {
            for (ManagedChannel channel : clientService.getChannels()) {
                channel.shutdownNow();
            }
            cleanupDone.set(true);
        }
    }
}