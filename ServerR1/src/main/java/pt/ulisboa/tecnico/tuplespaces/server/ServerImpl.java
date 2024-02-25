package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.sequencer.contract.SequencerGrpc;
/*import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import pt.ulisboa.tecnico.sequencer.contract.SequencerGrpc;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;*/

import io.grpc.stub.StreamObserver;

public class ServerImpl extends SequencerGrpc.SequencerImplBase {

   /*private ServerState serverState;

    public ServerImpl(ServerState serverState) {
        this.serverState = serverState;
    }

    @Override
    public void put(pt.ulisboa.tecnico.sequencer.contract.PutRequest request,
                    io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.sequencer.contract.PutResponse> responseObserver) {
        
        serverState.put(request.getTuple());
        responseObserver.onNext(pt.ulisboa.tecnico.sequencer.contract.PutResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void read(pt.ulisboa.tecnico.sequencer.contract.ReadRequest request,
                     io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.sequencer.contract.ReadResponse> responseObserver) {
        
        String tuple = serverState.read(request.getPattern());
        responseObserver.onNext(pt.ulisboa.tecnico.sequencer.contract.ReadResponse.newBuilder().setTuple(tuple).build());
        responseObserver.onCompleted();
    }

    @Override
    public void take(pt.ulisboa.tecnico.sequencer.contract.TakeRequest request,
                     io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.sequencer.contract.TakeResponse> responseObserver) {
        
        String tuple = serverState.take(request.getPattern());
        responseObserver.onNext(pt.ulisboa.tecnico.sequencer.contract.TakeResponse.newBuilder().setTuple(tuple).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getTupleSpacesState(pt.ulisboa.tecnico.sequencer.contract.GetTupleSpacesStateRequest request,
                                    io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.sequencer.contract.GetTupleSpacesStateResponse> responseObserver) {
        
        java.util.List<String> tuples = serverState.getTupleSpacesState();
        responseObserver.onNext(pt.ulisboa.tecnico.sequencer.contract.GetTupleSpacesStateResponse.newBuilder().addAllTuples(tuples).build());
        responseObserver.onCompleted();
    }*/
}
