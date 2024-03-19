package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

  public synchronized String take(String pattern) {
    return getMatchingTuple(pattern, true);
  }

  public List<String> getTupleSpacesState() {
    List<String> tupleDataList = new ArrayList<>();
    for (Tuple tuple : this.tuples) {
        tupleDataList.add(tuple.getData());
    }
    return tupleDataList;
  }
}


