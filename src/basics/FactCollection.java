package basics;

import java.io.File;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javatools.administrative.Announce;
import javatools.administrative.D;
import javatools.filehandlers.FileSet;

/**
 * Class FactCollection - YAGO2S
 * 
 * Represents a collection of facts, indexes them
 * 
 * @author Fabian M. Suchanek
 */
public class FactCollection extends AbstractSet<Fact> {

  /** Holds a name*/
  protected final String name;

  /** Holds the facts */
  protected Set<Fact> facts;

  /** Maps first arg to relation to facts */
  protected Map<String, Map<String, List<Fact>>> index = Collections.synchronizedMap(new HashMap<String, Map<String, List<Fact>>>());

  /** Maps relation to facts */
  protected Map<String, List<Fact>> relindex = Collections.synchronizedMap(new HashMap<String, List<Fact>>());

  /** Adds a fact, adds a source fact and a technique fact*/
  public synchronized boolean add(Fact fact, String source, String technique) {
    Fact sourceFact = fact.metaFact(YAGO.extractionSource, FactComponent.forUri(source));
    Fact techniqueFact = sourceFact.metaFact(YAGO.extractionTechnique, FactComponent.forString(technique));
    return (add(fact) && add(sourceFact) && add(techniqueFact));
  }

  /** Adds a fact, checks for duplicates*/
  public boolean add(final Fact fact) {
    return (add(fact, null));
  }

  /** Adds a fact, checks for functional duplicates*/
  public synchronized boolean add(final Fact fact, Set<String> functions) {
    if (fact.getArg(1) == null || fact.getArg(2) == null) {
      Announce.debug("Null fact not added:", fact);
      return (false);
    }
    if (facts.contains(fact)) {
      Announce.debug("Duplicate fact not added:", fact);
      return (false);
    }
    if (fact.arg1.equals(fact.arg2)) {
      Announce.debug("Identical arguments not added", fact);
      return (false);
    }
    Map<String, List<Fact>> map = index.get(fact.arg1);
    if (map == null) index.put(fact.arg1, map = Collections.synchronizedMap(new HashMap<String, List<Fact>>()));
    if (!map.containsKey(fact.relation)) map.put(fact.relation, Collections.synchronizedList(new ArrayList<Fact>(1)));
    for (Fact other : map.get(fact.relation)) {
      if (FactComponent.isMoreSpecific(fact.getArg(2), other.getArg(2))) {
        Announce.debug("Removed", other, "because of newly added", fact);
        remove(other);
        break;
      }
      if (FactComponent.isMoreSpecific(other.getArg(2), fact.getArg(2))) {
        Announce.debug("More general fact not added:", fact, "because of", other);
        return (false);
      }
      if (!other.getArg(2).equals(fact.getArg(2))) continue;
      if (other.getId() != null && fact.getId() == null) {
        Announce.debug("Fact without id not added:", fact, "because of", other);
        return (false);
      }
      if (other.getId() == null && fact.getId() != null) {
        Announce.debug("Removed", other, "because of newly added", fact);
        remove(other);
        break;
      }
    }
    if (functions != null && functions.contains(fact.getRelation()) && !map.get(fact.relation).isEmpty()) {
      Announce.debug("Functional fact not added because another fact is already there:", fact, ". Already there:", map.get(fact.relation));
      return (false);
    }
    return (justAdd(fact));
  }

  /** Adds a fact, does not check for duplicates*/
  protected synchronized boolean justAdd(final Fact fact) {
    if (facts.contains(fact)) {
      Announce.debug("Duplicate fact not added:", fact);
      return (false);
    }
    Map<String, List<Fact>> map = index.get(fact.arg1);
    if (map == null) index.put(fact.arg1, map = Collections.synchronizedMap(new HashMap<String, List<Fact>>()));
    if (!map.containsKey(fact.relation)) map.put(fact.relation, Collections.synchronizedList(new ArrayList<Fact>(1)));
    map.get(fact.relation).add(fact);
    if (!relindex.containsKey(fact.relation)) relindex.put(fact.relation, Collections.synchronizedList(new ArrayList<Fact>(1)));
    relindex.get(fact.relation).add(fact);
    facts.add(fact);
    return (true);
  }

  /** Empty list */
  protected static final List<Fact> EMPTY = new ArrayList<Fact>(0);

  /** Returns facts with matching first arg and relation */
  public List<Fact> get(String arg1, String relation) {
    if (!index.containsKey(arg1)) return (EMPTY);
    if (!index.get(arg1).containsKey(relation)) return (EMPTY);
    return (index.get(arg1).get(relation));
  }

  /** Returns facts with matching first arg */
  public List<Fact> getFactsWithSubject(String arg1) {
    List<Fact> result = new ArrayList<>();
    if (!index.containsKey(arg1)) return (result);
    for (Collection<Fact> facts : index.get(arg1).values()) {
      result.addAll(facts);
    }
    return (result);
  }

  /** Returns the first arg2 (or null) */
  public String getArg2(String arg1, String relation) {
    List<Fact> res = get(arg1, relation);
    if (res == null || res.isEmpty()) return (null);
    return (res.get(0).arg2);
  }

  /** Returns the arg2s */
  public Set<String> getArg2s(String arg1, String relation) {
    Set<String> result = new TreeSet<>();
    for (Fact f : get(arg1, relation)) {
      result.add(f.getArg(2));
    }
    return (result);
  }

  /** Returns facts with matching relation */
  public List<Fact> get(String relation) {
    if (!relindex.containsKey(relation)) return (EMPTY);
    return (relindex.get(relation));
  }

  /**
   * Returns facts with matching relation and second argument. This is very
   * slow.
   */
  public List<Fact> getBySecondArgSlow(String relation, String arg2) {
    if (!relindex.containsKey(relation)) return (EMPTY);
    List<Fact> result = new ArrayList<Fact>();
    for (Fact f : relindex.get(relation)) {
      if (f.arg2.equals(arg2)) result.add(f);
    }
    return (result);
  }

  /**
   * Returns subjects with matching relation and second argument. This is very
   * slow.
   */
  public Set<String> getArg1sSlow(String relation, String arg2) {
    Set<String> result = new HashSet<>();
    for (Fact f : getBySecondArgSlow(relation, arg2)) {
      result.add(f.getArg(1));
    }
    return (result);
  }

  /** Loads from N4 file */
  public FactCollection(File n4File) throws IOException {
    this(n4File, false);
  }

  /** Loads from N4 file. FAST does not check duplicates */
  public FactCollection(File n4File, boolean fast) throws IOException {
    this(FactSource.from(n4File), fast);
  }

  /** Loads from N4 file. FAST does not check duplicates */
  public FactCollection(FactSource n4File) throws IOException {
    this(n4File, false);
  }

  /** Loads from N4 file. FAST does not check duplicates */
  public FactCollection(FactSource n4File, boolean fast) throws IOException {
    facts = Collections.synchronizedSet(new HashSet<Fact>());
    name = n4File.name();
    if (fast) loadFast(n4File);
    else load(n4File);
  }

  public FactCollection() {
    this("anonymous FactCollection");
  }

  public FactCollection(String name) {
    facts = Collections.synchronizedSet(new HashSet<Fact>());
    this.name = name;
  }

  public FactCollection(int capacity) {
    facts = Collections.synchronizedSet(new HashSet<Fact>(capacity));
    name = "anonymous FactCollection";
  }

  /** Add facts */
  public synchronized boolean add(Iterable<Fact> facts) {
    boolean change = false;
    for (Fact f : facts)
      change |= add(f);
    return (change);
  }

  /** Removes a fact */
  public synchronized boolean remove(Object f) {
    if (!facts.remove(f)) return (false);
    Fact fact = (Fact) f;
    index.get(fact.arg1).get(fact.relation).remove(fact);
    relindex.get(fact.relation).remove(fact);
    if (fact.getId() != null) {
      List<Fact> metaFacts = getFactsWithSubject(fact.getId());
      for (Fact m : metaFacts) {
        remove(m);
      }
    }
    return (true);
  }

  /** Removes all facts */
  public void clear() {
    facts.clear();
    index.clear();
    relindex.clear();
  }

  /** Loads from N4 file */
  public void load(File n4File) throws IOException {
    load(FactSource.from(n4File));
  }

  /** Loads from N4 file, does not check duplicates */
  public void loadFast(File n4File) throws IOException {
    loadFast(FactSource.from(n4File));
  }

  /** Loads from N4 file, does not check duplicates */
  public void loadFast(FactSource reader) throws IOException {
    Announce.doing("Fast loading", reader);
    for (Fact f : reader) {
      justAdd(f);
    }
    Announce.done();
  }

  /** Loads from N4 file */
  public void load(FactSource reader) throws IOException {
    Announce.doing("Loading", reader);
    for (Fact f : reader) {
      add(f);
    }
    Announce.done();
  }

  @Override
  public Iterator<Fact> iterator() {
    return facts.iterator();
  }

  public int size() {
    return facts.size();
  }

  public String toString() {
    return facts.toString();
  }

  /** Checks if all of my facts are in the other set, prints differences */
  public boolean checkContainedIn(FactCollection goldStandard) {
    boolean matches = true;
    next: for (Fact fact : facts) {
      for (Fact other : goldStandard.get(fact.arg1, fact.relation)) {
        if (other.arg2.equals(fact.arg2)) {
          //if (!D.equal(fact.getId(), other.getId())) Announce.message("Different ids:", fact, other);
          continue next;
        }
      }
      Announce.message("Not found in", goldStandard.name(), ":", fact);
      matches = false;
    }
    return (matches);
  }

  /** returns the name*/
  public String name() {
    return name;
  }

  /** Checks for differences, returns TRUE if equal, prints differences */
  public boolean checkEqual(FactCollection goldStandard) {
    boolean b = checkContainedIn(goldStandard) & goldStandard.checkContainedIn(this);
    return (b);
  }

  /** TRUE if this collection contains this fact with any id */
  public boolean contains(String arg1, String rel, String arg2) {
    Map<String, List<Fact>> map = index.get(arg1);
    if (map == null) return (false);
    List<Fact> facts = map.get(rel);
    if (facts == null) return (false);
    for (Fact f : facts) {
      if (f.getArg(2).equals(arg2)) return (true);
    }
    return (false);
  }

  /** Returns a map for a relation */
  public Map<String, String> asStringMap(String relation) {
    Map<String, String> objects = new HashMap<String, String>();
    for (Fact fact : get(relation)) {
      objects.put(fact.getArgJavaString(1), fact.getArgJavaString(2));
    }
    if (objects.isEmpty()) Announce.warning("No instances of", relation, "found");
    return (objects);
  }

  /** Returns a set of strings for a type */
  public Set<String> asStringSet(String type) {
    Set<String> result = new TreeSet<String>();
    for (Fact fact : getBySecondArgSlow("rdf:type", type)) {
      result.add(fact.getArgJavaString(1));
    }
    if (result.isEmpty()) Announce.warning("No instances of", type, "found");
    return (result);
  }

  /** TRUE if the object is an instance of the class */
  public boolean instanceOf(String instance, String clss) {
    Collection<String> classes;
    if (FactComponent.isLiteral(instance)) {
      classes = Arrays.asList(FactComponent.getDatatype(instance));
    } else {
      classes = getArg2s(instance, RDFS.type);
    }
    for (String c : classes) {
      if (isSubClassOf(c, clss)) return (true);
    }
    return (false);
  }

  /** TRUE if the first class is equal to or a subclass of the second*/
  public boolean isSubClassOf(String sub, String supr) {
    if (sub.equals(supr)) return (true);
    for (String s : getArg2s(sub, RDFS.subclassOf)) {
      if (isSubClassOf(s, supr)) return (true);
    }
    return (false);
  }

  /** Adds the superclasses of this class*/
  public void superClasses(String sub, Set<String> set) {
    set.add(sub);
    for (String s : getArg2s(sub, RDFS.subclassOf)) {
      superClasses(s, set);
    }
  }

  /** Adds the superclasses of this class*/
  public Set<String> superClasses(String sub) {
    Set<String> set = new TreeSet<>();
    superClasses(sub, set);
    return (set);
  }

  /**
   * Creates a map for quickly getting arg1 for a given arg2.
   * Notice that this might overwrite arg1s that occur multiple
   * times, make sure you know that they are unique.
   * 
   * This is useful for all the YAGO -> Other_KB_ID mappings
   * 
   * @param relation relation for which to generate the reverse map
   * @return reverse map
   */
  public Map<String, String> getReverseMap(String relation) {
    Map<String, String> reverseMap = new HashMap<>();

    for (Fact f : get(relation)) {
      reverseMap.put(f.getArg(2), f.getArg(1));
    }

    return reverseMap;
  }
  
  public Set<String> getSubjects(){
    Set<String> subjects= new HashSet<String>();
    for(Fact f2:facts){
      subjects.add(f2.getArg(1));
    }
    return subjects;
  }

}
