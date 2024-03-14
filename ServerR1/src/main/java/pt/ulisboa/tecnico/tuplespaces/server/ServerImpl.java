package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.stub.StreamObserver;
import static io.grpc.Status.INVALID_ARGUMENT;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import pt.ulisboa.tecnico.tuplespaces.server.exceptions.TupleAlreadyExistsException;

public class ServerImpl extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase{

    private ServerState serverState = new ServerState();
    private boolean debug = false;

    public ServerImpl(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        if (debug) {
            System.err.println("Received PutRequest with tuple: " + request.getNewTuple());
        }
        
        if(!inputIsValid(request.getNewTuple())){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid Input").asRuntimeException());
        }

        else {   
            serverState.put(request.getNewTuple());
            responseObserver.onNext(PutResponse.getDefaultInstance());
            responseObserver.onCompleted();
        }   
        
        if (debug) {
            System.err.println("Sent PutResponse");
        }
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        if (debug) {
            System.err.println("Received ReadRequest with pattern: " + request.getSearchPattern());
        }
        
        if(!inputIsValid(request.getSearchPattern())){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid Input").asRuntimeException());
        }

        else {   
            String result = serverState.read(request.getSearchPattern());

            ReadResponse response;
            if (result != null) {
                response = ReadResponse.newBuilder().setResult(result).build();
            } else {
                response = ReadResponse.newBuilder().build();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            if (debug) {
                System.err.println("Sent ReadResponse with result: " + response.getResult());
            }
        }
    }

    @Override
    public void takePhase1(TakePhase1Request request, StreamObserver<TakePhase1Response> responseObserver) {
        if (debug) {
            System.err.println("Received TakeRequest with pattern: " + request.getSearchPattern());
        }
        
        if(!inputIsValid(request.getSearchPattern())){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid Input").asRuntimeException());
        }

        else {
            String result = serverState.take(request.getSearchPattern());

            TakePhase1Response response;
            if (result != null) {
                // TODOresponse = TakePhase1Response.newBuilder().setResult(result).build();
            } else {
                response = TakePhase1Response.newBuilder().build();
            }

            // TODO responseObserver.onNext(response);
            responseObserver.onCompleted();

            if (debug) {
                //TODO System.err.println("Sent TakeResponse with result: " + response.getResult());
            }
        }
    }

    @Override
    public void getTupleSpacesState(getTupleSpacesStateRequest request, StreamObserver<getTupleSpacesStateResponse> responseObserver) {
        if (debug) {
            System.err.println("Received GetTupleSpacesStateRequest");
        }

        java.util.List<String> tuples = serverState.getTupleSpacesState();
        responseObserver.onNext(getTupleSpacesStateResponse.newBuilder().addAllTuple(tuples).build());
        responseObserver.onCompleted();

        if (debug) {
            System.err.println("Sent GetTupleSpacesStateResponse with tuples: " + tuples);
        }
    }

    private boolean inputIsValid(String input){
        
        if ( !input.substring(0,1).equals("<") || !input.endsWith(">")) {
            return false;
        }
        else {
            return true;
        }
    }
}
