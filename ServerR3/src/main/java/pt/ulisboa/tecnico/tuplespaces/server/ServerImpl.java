package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.stub.StreamObserver;
import java.util.List;
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
            System.err.println("Received TakePhase1Request with pattern: " + request.getSearchPattern());
        }

        if(!inputIsValid(request.getSearchPattern())){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid Input").asRuntimeException());
        } else {
            List<String> reservedTuples = serverState.reserveTuples(request.getSearchPattern(), request.getClientId());

            boolean requestAccepted = !reservedTuples.isEmpty();
            
            TakePhase1Response response = TakePhase1Response.newBuilder()
                .addAllReservedTuples(reservedTuples)
                .setAccepted(requestAccepted) //Set the accepted field
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            if (debug) {
                System.err.println("Sent TakePhase1Response with reserved tuples: " + reservedTuples);
            }
        }
    }

    @Override
    public void takePhase1Release(TakePhase1ReleaseRequest request, StreamObserver<TakePhase1ReleaseResponse> responseObserver) {
        if (debug) {
            System.err.println("Received TakePhase1ReleaseRequest from client: " + request.getClientId());
        }

        serverState.releaseTuples(request.getClientId());

        TakePhase1ReleaseResponse response = TakePhase1ReleaseResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        if (debug) {
            System.err.println("Sent TakePhase1ReleaseResponse to client: " + request.getClientId());
        }
    }

    @Override
    public void takePhase2(TakePhase2Request request, StreamObserver<TakePhase2Response> responseObserver) {
        if (debug) {
            System.err.println("Received TakePhase2Request with tuple: " + request.getTuple());
        }

        if(!inputIsValid(request.getTuple())){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid Input").asRuntimeException());
        } else {
            serverState.removeTuple(request.getTuple(), request.getClientId());

            TakePhase2Response response = TakePhase2Response.newBuilder().build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            if (debug) {
                System.err.println("Sent TakePhase2Response for tuple: " + request.getTuple());
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
            System.err.println("Sent getTupleSpacesStateResponse with tuples: " + tuples);
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
