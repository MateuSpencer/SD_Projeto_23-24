package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerState {

  private List<String> tuples;

  public ServerState() {
    this.tuples = new ArrayList<String>();

  }

  public void put(String tuple) {
    tuples.add(tuple);
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

  public String read(String pattern) {
    return getMatchingTuple(pattern, false);
  }

  public String take(String pattern) {
    return getMatchingTuple(pattern, true);
  }

  public List<String> getTupleSpacesState() {
    return new ArrayList<>(this.tuples);
  }
}
