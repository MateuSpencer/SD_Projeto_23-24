package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerState {

  private List<String> tuples;

  public ServerState() {
    this.tuples = new ArrayList<String>();

  }

  public synchronized void put(String tuple) {
    tuples.add(tuple);
    notifyAll(); // Notify all threads that are waiting for a tuple to be put
  }

  private String getMatchingTuple(String pattern, boolean remove) {
    Iterator<String> iterator = this.tuples.iterator();
    while (iterator.hasNext()) {
      String tuple = iterator.next();
      if (tuple.matches(pattern)) {
        if (remove) {
          iterator.remove();
        }
        return tuple;
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

  public List<String> getTupleSpacesState() {
    return new ArrayList<>(this.tuples);
  }
}
