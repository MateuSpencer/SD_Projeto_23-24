package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

import io.grpc.stub.StreamObserver;
import io.grpc.Status;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.exceptions.TupleAlreadyExistsException;

public class ServerImpl extends TupleSpacesGrpc.TupleSpacesImplBase{

    private ServerState serverState = new ServerState();

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        
        try {
        serverState.put(request.getTuple());
        responseObserver.onNext(PutResponse.getDefaultInstance());
        responseObserver.onCompleted();
        } catch (TupleAlreadyExistsException e) {
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        }
        
        //System.out.println(request.getTuple()); // TODO: Remove DEBUG
    }

@Override
public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
    String result = serverState.read(request.getPattern());

    ReadResponse response;
    if (result != null) {
        response = ReadResponse.newBuilder().setResult(result).build();
    } else {
        response = ReadResponse.newBuilder().build();
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();

    //System.out.println(request.getPattern()); // TODO: Remove DEBUG
}

@Override
public void take(TakeRequest request, StreamObserver<TakeResponse> responseObserver) {
    String result = serverState.take(request.getPattern());

    TakeResponse response;
    if (result != null) {
        response = TakeResponse.newBuilder().setResult(result).build();
    } else {
        response = TakeResponse.newBuilder().build();
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();

    //System.out.println(request.getPattern()); // TODO: Remove DEBUG
}

    @Override
    public void getTupleSpacesState(GetTupleSpacesStateRequest request, StreamObserver<GetTupleSpacesStateResponse> responseObserver) {
        
        java.util.List<String> tuples = serverState.getTupleSpacesState();
        responseObserver.onNext(GetTupleSpacesStateResponse.newBuilder().addAllTuple(tuples).build());
        responseObserver.onCompleted();
    }
}
