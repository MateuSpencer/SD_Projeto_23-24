package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.Arrays;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

public class ClientMain {
    public static void main(String[] args) {

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
        if ((debug && args.length != 3) || (!debug && args.length != 2)) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: mvn exec:java -Dexec.args=<host> <port> %s\n", debug ? "<-debug>" : "");
            return;
        }

        // get the host and the port
        final String host = args[0];
        final String port = args[1];

        CommandProcessor parser = new CommandProcessor(new ClientService(host, port, debug));
        parser.parseInput();

    }
}
