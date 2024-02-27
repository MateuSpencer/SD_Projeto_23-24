package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.sequencer.contract.SequencerGrpc;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import pt.ulisboa.tecnico.sequencer.contract.SequencerGrpc;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;

public class ServerImpl extends TupleSpacesGrpc.TupleSpacesImplBase{

   /* private ServerState serverState;

    public ServerImpl(ServerState serverState) {
        this.serverState = serverState;
    }

    @Override

    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        serverState.put(request.getTuple());
        responseObserver.onNext(PutResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        
        String tuple = serverState.read(request.getPattern());
        responseObserver.onNext(ReadResponse.newBuilder().setTuple(tuple).build());
        responseObserver.onCompleted();
    }

    @Override
    public void take(TakeRequest request, StreamObserver<TakeResponse> responseObserver) {
        
        String tuple = serverState.take(request.getPattern());
        responseObserver.onNext(TakeResponse.newBuilder().setTuple(tuple).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getTupleSpacesState(GetTupleSpacesStateRequest request, StreamObserver<GetTupleSpacesStateResponse> responseObserver) {
        
        java.util.List<String> tuples = serverState.getTupleSpacesState();
        responseObserver.onNext(GetTupleSpacesStateResponse.newBuilder().addAllTuples(tuples).build());
        responseObserver.onCompleted();
    } */
}
