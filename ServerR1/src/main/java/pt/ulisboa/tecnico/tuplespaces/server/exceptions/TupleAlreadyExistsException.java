package pt.ulisboa.tecnico.tuplespaces.server.exceptions;

import io.grpc.Status;

public class TupleAlreadyExistsException extends RuntimeException{

  public TupleAlreadyExistsException(String tuple) {
    super(String.format("Tuple" + tuple + "already exists"));
  }
}