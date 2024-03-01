package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

import io.grpc.stub.StreamObserver;
import io.grpc.Status;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;
import pt.ulisboa.tecnico.tuplespaces.server.exceptions.TupleAlreadyExistsException;

public class ServerImpl extends TupleSpacesGrpc.TupleSpacesImplBase{

    private ServerState serverState = new ServerState();
    private boolean debug = false;

    public ServerImpl(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        if (debug) {
            System.err.println("Received PutRequest with tuple: " + request.getTuple());
        }
        
        try {
        serverState.put(request.getTuple());
        responseObserver.onNext(PutResponse.getDefaultInstance());
        responseObserver.onCompleted();
        } catch (TupleAlreadyExistsException e) {
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        }
        
        if (debug) {
            System.err.println("Sent PutResponse");
        }
    }

@Override
public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
    if (debug) {
        System.err.println("Received ReadRequest with pattern: " + request.getPattern());
    }
    
    String result = serverState.read(request.getPattern());

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

@Override
public void take(TakeRequest request, StreamObserver<TakeResponse> responseObserver) {
    if (debug) {
        System.err.println("Received TakeRequest with pattern: " + request.getPattern());
    }
    
    String result = serverState.take(request.getPattern());

    TakeResponse response;
    if (result != null) {
        response = TakeResponse.newBuilder().setResult(result).build();
    } else {
        response = TakeResponse.newBuilder().build();
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();

    if (debug) {
        System.err.println("Sent TakeResponse with result: " + response.getResult());
    }
}

    @Override
    public void getTupleSpacesState(GetTupleSpacesStateRequest request, StreamObserver<GetTupleSpacesStateResponse> responseObserver) {
        if (debug) {
            System.err.println("Received GetTupleSpacesStateRequest");
        }

        java.util.List<String> tuples = serverState.getTupleSpacesState();
        responseObserver.onNext(GetTupleSpacesStateResponse.newBuilder().addAllTuple(tuples).build());
        responseObserver.onCompleted();

        if (debug) {
            System.err.println("Sent GetTupleSpacesStateResponse with tuples: " + tuples);
        }
    }
}
