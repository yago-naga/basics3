package basics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class ExtendedFactCollection - YAGO2S
 * 
 * Represents a collection of facts, indexes them with extended functionality.
 * 
 * @author Farzaneh Mahdisoltani
 */
public class ExtendedFactCollection extends FactCollection {

  /** Maps objects to facts */
  protected Map<String, List<Fact>> obindex = Collections.synchronizedMap(new HashMap<String, List<Fact>>());

  /** Adds a fact, does not check for duplicates*/
  protected synchronized boolean justAdd(final Fact fact) {
    if (!super.justAdd(fact)) return false;

    if (!obindex.containsKey(fact.arg2)) obindex.put(fact.arg2, Collections.synchronizedList(new ArrayList<Fact>(1)));
    obindex.get(fact.arg2).add(fact);

    return (true);
  }

  /** Returns facts with matching second arg */
  public List<Fact> getFactsWithObject(String arg2) {
    List<Fact> result = new ArrayList<Fact>();
    if (obindex.containsKey(arg2)) result = obindex.get(arg2);
    return (result);
  }

  /** Removes a fact */
  public synchronized boolean remove(Object f) {
    if (!super.remove(f)) return false;
    obindex.get(((Fact) f).arg2).remove(((Fact) f));
    return true;
  }

  /** Removes all facts */
  public void clear() {
    super.clear();
    obindex.clear();
  }

  /** Loads from a FactSource */
  public static ExtendedFactCollection loadFacts(FactSource factSource) {
    ExtendedFactCollection result = new ExtendedFactCollection();
    for (Fact f : factSource) {
      result.add(f);
    }
    return result;
  }

}
