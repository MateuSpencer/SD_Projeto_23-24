package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ServerState {

  private List<Tuple> tuples;


  public ServerState() {
    this.tuples = new ArrayList<>();
  }

  public synchronized void put(String tuple) {
    tuples.add(new Tuple(tuple));
    notifyAll(); // Notify all threads that are waiting for a tuple to be put
  }

  private String getMatchingTuple(String pattern, boolean remove) {
    Iterator<Tuple> iterator = this.tuples.iterator();
    while (iterator.hasNext()) {
        Tuple tuple = iterator.next();
        if (tuple.getData().matches(pattern)) {
            if (remove) {
                iterator.remove();
            }
            return tuple.getData();
        }
    }
    return null;
  }

  public synchronized String read(String pattern) {
    String result = null;
    while (result == null) {
        result = getMatchingTuple(pattern, false);
        if (result == null) {
            try {
                wait(); // Wait if no matching tuple is found
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
            }
        }
    }
    return result;
  }

  public synchronized List<String> reserveTuples(String pattern, int clientId) {
    if (getMatchingTuple(pattern, false) == null) {
      System.out.println("No matching tuple found");
      try {
        wait(); // Wait if no matching tuple is found
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Restore the interrupted status
    }
    }
    List<String> reservedTuples = tuples.stream()
        .filter(tuple -> !tuple.isLocked() && tuple.getData().matches(pattern))
        .peek(tuple -> {
            tuple.setLocked(true);
            tuple.setClientId(clientId);
        })
        .map(Tuple::getData)
        .collect(Collectors.toList());

    return reservedTuples;
  }

  public synchronized void releaseTuples(int clientId) {
    tuples.stream()
        .filter(tuple -> tuple.getClientId() == clientId)
        .forEach(tuple -> {
            tuple.setLocked(false);
            tuple.setClientId(0); // Reset client ID when the tuple is unlocked
        });
  }

  public synchronized void removeTuple(String tupleData, int clientId) {
    Iterator<Tuple> iterator = tuples.iterator();
    while (iterator.hasNext()) {
        Tuple tuple = iterator.next();
        if (tuple.getData().equals(tupleData) && tuple.getClientId() == clientId) {
            iterator.remove();
            break;
        }
    }
  }

  public List<String> getTupleSpacesState() {
    List<String> tupleDataList = new ArrayList<>();
    for (Tuple tuple : this.tuples) {
        tupleDataList.add(tuple.getData());
    }
    return tupleDataList;
  }
}


