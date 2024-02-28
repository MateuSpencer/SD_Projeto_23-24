package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

import io.grpc.stub.StreamObserver;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;

public class ServerImpl extends TupleSpacesGrpc.TupleSpacesImplBase{

    private ServerState serverState = new ServerState();

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        serverState.put(request.getTuple());
        responseObserver.onNext(PutResponse.getDefaultInstance());
        responseObserver.onCompleted();
        System.out.println(request.getTuple());
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        
        String result = serverState.read(request.getPattern());
        responseObserver.onNext(ReadResponse.newBuilder().setResult(result).build());
        responseObserver.onCompleted();
    }

    @Override
    public void take(TakeRequest request, StreamObserver<TakeResponse> responseObserver) {
        
        String result = serverState.take(request.getPattern());
        responseObserver.onNext(TakeResponse.newBuilder().setResult(result).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getTupleSpacesState(GetTupleSpacesStateRequest request, StreamObserver<GetTupleSpacesStateResponse> responseObserver) {
        
        java.util.List<String> tuples = serverState.getTupleSpacesState();
        responseObserver.onNext(GetTupleSpacesStateResponse.newBuilder().addAllTuple(tuples).build());
        responseObserver.onCompleted();
    }
}
