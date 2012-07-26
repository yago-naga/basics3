package basics;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javatools.administrative.Announce;
import javatools.administrative.D;
import javatools.filehandlers.FileLines;
import javatools.parsers.Char;
import javatools.parsers.NumberFormatter;

/**
 * Class FactDatabase
 * 
 * This class implements an in-memory database for facts without identifiers, tuned for Luis' project
 * 
 * @author Fabian M. Suchanek
 * 
 */
public class FactDatabase {

  /** Index*/
  protected final Map<String, Map<String, Set<String>>> subject2predicate2object = new IdentityHashMap<>();

  /** Index*/
  protected final Map<String, Map<String, Set<String>>> predicate2object2subject = new IdentityHashMap<>();

  /** Index*/
  protected final Map<String, Map<String, Set<String>>> object2subject2predicate = new IdentityHashMap<>();

  /** Index*/
  protected final Map<String, Map<String, Integer>> predicate2subject2objectSize = new IdentityHashMap<>();

  /** Index*/
  protected final Map<String, Map<String, Integer>> object2predicate2subjectSize = new IdentityHashMap<>();

  /** Index*/
  protected final Map<String, Map<String, Integer>> subject2object2predicateSize = new IdentityHashMap<>();

  /** Number of facts per subject*/
  protected final Map<String, Integer> subjectSize = new IdentityHashMap<String, Integer>();

  /** Number of facts per object*/
  protected final Map<String, Integer> objectSize = new IdentityHashMap<String, Integer>();

  /** Number of facts per relation*/
  protected final Map<String, Integer> predicateSize = new IdentityHashMap<String, Integer>();

  /** Number of facts*/
  protected int size;

  /** Adds a fact*/
  protected boolean add(String subject, String relation, String object, Map<String, Map<String, Set<String>>> map) {
    synchronized (map) {
      Map<String, Set<String>> relation2object = map.get(subject);
      if (relation2object == null) map.put(subject, relation2object = new IdentityHashMap<String, Set<String>>());
      Set<String> objects = relation2object.get(relation);
      if (objects == null) relation2object.put(relation, objects = new HashSet<String>());
      return (objects.add(object));
    }
  }

  /** Increases the size*/
  protected void increaseSize(String subject, String relation, Map<String, Map<String, Integer>> map) {
    synchronized (map) {
      Map<String, Integer> relation2object = map.get(subject);
      if (relation2object == null) map.put(subject, relation2object = new IdentityHashMap<String, Integer>());
      Integer objects = relation2object.get(relation);
      if (objects == null) relation2object.put(relation, new Integer(1));
      else relation2object.put(relation, objects + 1);
    }
  }

  /** Adds a fact*/
  public boolean add(String subject, String predicate, String object) {
    subject = subject.trim().intern();
    predicate = predicate.trim().intern();
    object = object.trim().intern();
    if (!add(subject, predicate, object, subject2predicate2object)) return (false);
    add(predicate, object, subject, predicate2object2subject);
    add(object, subject, predicate, object2subject2predicate);
    D.addKeyValue(subjectSize, subject, 1);
    D.addKeyValue(predicateSize, predicate, 1);
    D.addKeyValue(objectSize, object, 1);
    increaseSize(predicate, subject, predicate2subject2objectSize);
    increaseSize(object, predicate, object2predicate2subjectSize);
    increaseSize(subject, object, subject2object2predicateSize);
    size++;
    return (true);
  }

  /** Returns the number of facts*/
  public int size() {
    return (size);
  }

  /** TRUE if the string is a SPARQL variable*/
  public static boolean isVariable(String s) {
    return (s.startsWith("?"));
  }

  /** Loads a file or all files in the folder*/
  public void load(File f) throws IOException {
    load(f, "Loading " + f.getName());
  }

  /** Loads a file or all files in the folder*/
  protected void load(File f, String message) throws IOException {
    int size = size();
    if (f.isDirectory()) {
      long time = System.currentTimeMillis();
      Announce.doing("Loading files in " + f.getName());
      for (File file : f.listFiles())
        load(file);
      Announce.done("Loaded " + (size() - size) + " facts in " + NumberFormatter.formatMS(System.currentTimeMillis() - time));
    }
    for (String line : new FileLines(f, "UTF-8", message)) {
      if (line.endsWith(".")) line = Char.cutLast(line);
      String[] split = line.split("\t");
      if (split.length == 3) add(split[0], split[1], split[2]);
      else if (split.length == 4) add(split[1], split[2], split[3]);
    }
    if(message!=null) Announce.message("     Loaded", (size() - size), "facts");
  }

  /** Loads a files in the folder that match the regex pattern*/
  public void load(File folder, Pattern namePattern) throws IOException {
    List<File> files = new ArrayList<>();
    for (File file : folder.listFiles())
      if (namePattern.matcher(file.getName()).matches()) files.add(file);
    load(files);
  }

  /** Loads the files*/
  public void load(File... files) throws IOException {
    load(Arrays.asList(files));
  }

  /** Loads the files*/
  public void load(List<File> files) throws IOException {
    int size = size();
    long time = System.currentTimeMillis();
    Announce.doing("Loading files");
    final Object lock=new Object();
    final int[] running = new int[1];
    for (final File file : files) {
      running[0]++;
      new Thread() {

        public void run() {
          try {
            synchronized(Announce.blanks) {
            Announce.message("Starting "+file.getName());}
            load(file,(String)null);
          } catch (IOException e) {
            e.printStackTrace();
          }
          synchronized (running) {
            if(--running[0]==0) lock.notify();
          }
          synchronized(Announce.blanks) {
          Announce.message("Finished "+file.getName()+ ", still running: "+running[0]);
          }
        }
      }.start();
    }
    lock.wait();
    Announce.done("Loaded " + (size() - size) + " facts in " + NumberFormatter.formatMS(System.currentTimeMillis() - time));
  }

  /** Returns the result of the map for key1 and key2*/
  protected Set<String> get(Map<String, Map<String, Set<String>>> map, String key1, String key2) {
    Map<String, Set<String>> m = map.get(key1.intern());
    if (m == null) return (Collections.emptySet());
    Set<String> r = m.get(key2.intern());
    if (r == null) return (Collections.emptySet());
    return (r);
  }

  /** Returns the results of the triple pattern query, if it contains exactly 1 variable*/
  public Set<String> resultsOneVariable(String... triple) {
    if (numVariables(triple) != 1) throw new IllegalArgumentException("Triple should contain exactly one variable: " + Arrays.toString(triple));
    if (isVariable(triple[0])) return (get(predicate2object2subject, triple[1], triple[2]));
    if (isVariable(triple[1])) return (get(object2subject2predicate, triple[2], triple[0]));
    return (get(subject2predicate2object, triple[0], triple[1]));
  }

  /** TRUE if the database contains this fact (no variables)*/
  public boolean contains(String... fact) {
    return (resultsOneVariable(fact[0], fact[1], "?x").contains(fact[2]));
  }

  /** Returns map results for key*/
  protected Map<String, Set<String>> get(Map<String, Map<String, Set<String>>> map, String key1) {
    Map<String, Set<String>> m = map.get(key1.intern());
    if (m == null) return (Collections.emptyMap());
    else return (m);
  }

  /** Returns the results of a triple query pattern with two variables as a map of first value to set of second values*/
  public Map<String, Set<String>> resultsTwoVariables(String... triple) {
    if (numVariables(triple) != 2) throw new InvalidParameterException("Triple must contain 2 variables: " + Arrays.toString(triple));
    if (!isVariable(triple[0])) return (get(subject2predicate2object, triple[0]));
    if (!isVariable(triple[1])) return (get(predicate2object2subject, triple[1]));
    return (get(object2subject2predicate, triple[2]));
  }

  /** Returns number of results of the triple pattern query with 1 variable*/
  protected int countOneVariable(String... triple) {
    return (resultsOneVariable(triple).size());
  }

  /** Returns number of results of the triple pattern query with 2 variables*/
  protected int countTwoVariables(String... triple) {
    // Safety check
    //if(numVariables(triple)!=2) throw new InvalidParameterException("Triple must contain exactly 2 variables: "+Arrays.toString(triple));
    if (!isVariable(triple[0])) return (D.getOr(subjectSize, triple[0], 0));
    if (!isVariable(triple[1])) {
      // To be correct, we should guard against the same variable...
      // if(triple[0].equals(triple[2])) throw new UnsupportedOperationException("Reflexive triple pattern");
      return (D.getOr(predicateSize, triple[1], 0));
    }
    return (D.getOr(objectSize, triple[2], 0));
  }

  /** Returns number of variable occurrences in a triple*/
  public static int numVariables(String... fact) {
    int counter = 0;
    for (int i = 0; i < fact.length; i++)
      if (isVariable(fact[i])) counter++;
    return (counter);
  }

  /** returns number of instances of this triple*/
  public int count(String... triple) {
    for (int i = 0; i < triple.length; i++)
      triple[i] = triple[i].intern();
    switch (numVariables(triple)) {
      case 0:
        return (contains(triple) ? 1 : 0);
      case 1:
        return (countOneVariable(triple));
      case 2:
        return (countTwoVariables(triple));
      case 3:
        return (size());
    }
    return (-1);
  }

  /** Returns the first variable of the pattern*/
  public static String firstVariable(String... fact) {
    for (int i = 0; i < fact.length; i++)
      if (isVariable(fact[i])) return (fact[i]);
    return (null);
  }

  /** TRUE if the query result exists*/
  public boolean exists(List<String[]> triples) {
    if (triples.isEmpty()) return (false);
    if (triples.size() == 1) return (count(triples.get(0)) != 0);
    String[] best = null;
    int count = Integer.MAX_VALUE;
    for (String[] triple : triples) {
      int myCount = count(triple);
      if (myCount > count) continue;
      best = triple;
      count = myCount;
    }
    switch (numVariables(best)) {
      case 0:
        if (!contains(best)) return (false);
        List<String[]> newList = new ArrayList<String[]>(triples);
        newList.remove(best);
        return (exists(newList));
      case 1:
        String variable = firstVariable(best);
        List<String[]> newList2 = new ArrayList<String[]>(triples);
        newList2.remove(best);
        for (String inst : resultsOneVariable(best)) {
          if (exists(instantiate(newList2, variable, inst))) return (true);
        }
        return (false);
      case 2:
        List<String[]> newList3 = new ArrayList<String[]>(triples);
        newList3.remove(best);
        String variable1 = firstVariable(best);
        Map<String, Set<String>> instantiations = resultsTwoVariables(best);
        for (String val1 : instantiations.keySet()) {
          String[] best1 = instantiate(best, variable1, val1);
          String variable2 = firstVariable(best1);
          List<String[]> newList4 = instantiate(newList3, variable1, val1);
          for (String val2 : instantiations.get(val1)) {
            if (exists(instantiate(newList4, variable2, val2))) return (true);
          }
        }
        return (false);
      case 3:
      default:
        return (true);
    }
  }

  /** Counts the number of instances of the projection triple that exist in joins with the other triples*/
  public int countProjection(String[] projectionTriple, List<String[]> otherTriples) {
    if (otherTriples.isEmpty()) return (count(projectionTriple));
    switch (numVariables(projectionTriple)) {
      case 0:
        return (count(projectionTriple));
      case 1:
        int counter = 0;
        String variable = firstVariable(projectionTriple);
        for (String inst : resultsOneVariable(projectionTriple)) {
          if (exists(instantiate(otherTriples, variable, inst))) counter++;
        }
        return (counter);
      case 2:
        counter = 0;
        String variable1 = firstVariable(projectionTriple);
        Map<String, Set<String>> instantiations = resultsTwoVariables(projectionTriple);
        for (String val1 : instantiations.keySet()) {
          String[] projectionTriple2 = instantiate(projectionTriple, variable1, val1);
          String variable2 = firstVariable(projectionTriple2);
          List<String[]> otherTriples2 = instantiate(otherTriples, variable1, val1);
          for (String val2 : instantiations.get(val1)) {
            if (exists(instantiate(otherTriples2, variable2, val2))) counter++;
          }
        }
        return (counter);
      case 3:
      default:
        throw new UnsupportedOperationException("3 variables in the projection triple are not yet supported");
    }
  }

  /** Instantiates a triple*/
  protected String[] instantiate(String[] triple, String variable, String inst) {
    String[] newT = triple;
    for (int i = 0; i < triple.length; i++) {
      if (triple[i].equals(variable)) {
        if (newT == triple) newT = Arrays.copyOf(triple, triple.length);
        newT[i] = inst;
      }
    }
    return (newT);
  }

  /** Instantiates a list of triples*/
  protected List<String[]> instantiate(List<String[]> triples, String variable, String inst) {
    List<String[]> result = new ArrayList<String[]>();
    for (String[] triple : triples) {
      result.add(instantiate(triple, variable, inst));
    }
    return (result);
  }

  /** Returns list of entities, each of which have map.get(entity)>minFrequency.*/
  protected List<String> mostFrequentValues(int minFrequency, final Map<String, Integer> map) {
    List<String> result = new ArrayList<>();
    for (String key : map.keySet()) {
      if (map.get(key) >= minFrequency) {
        result.add(key);
      }
    }
    Collections.sort(result, new Comparator<String>() {

      @Override
      public int compare(String o1, String o2) {
        int f1 = map.get(o1);
        int f2 = map.get(o2);
        return (f1 > f2 ? -1 : f2 > f1 ? 1 : 0);
      }
    });
    return (result);
  }

  /** Returns list of entities, each of which have map.get(entity)>minFrequency. */
  protected List<String> mostFrequentValuesSet(int minFrequency, final Map<String, Set<String>> map) {
    List<String> result = new ArrayList<>();
    for (String key : map.keySet()) {
      if (map.get(key).size() >= minFrequency) {
        result.add(key);
      }
    }
    Collections.sort(result, new Comparator<String>() {

      @Override
      public int compare(String o1, String o2) {
        int f1 = map.get(o1).size();
        int f2 = map.get(o2).size();
        return (f1 > f2 ? -1 : f2 > f1 ? 1 : 0);
      }
    });
    return (result);
  }

  /** Returns most frequent values for the given variable in the triple, sorted by descending frequency. Returns only values with a frequency higher than minFrequency. */
  public List<String> mostFrequentValues(int minFrequency, int pos, String... triple) {
    if (!isVariable(triple[pos])) throw new InvalidParameterException("Position " + pos + " should be a variable in " + Arrays.toString(triple));
    for (int i = 0; i < triple.length; i++)
      triple[i] = triple[i].intern();
    switch (numVariables(triple)) {
      case 2:
        switch (pos) {
          case 0: // We want the most frequent subjects
            // ?x loves ?y
            if (isVariable(triple[2])) return (mostFrequentValues(minFrequency, predicate2subject2objectSize.get(triple[1])));
            // ?x ?r Elvis
            else return (mostFrequentValuesSet(minFrequency, object2subject2predicate.get(triple[2])));
          case 1: // We want the most frequent predicates
            //Elvis ?r ?y
            if (isVariable(triple[2])) return (mostFrequentValuesSet(minFrequency, subject2predicate2object.get(triple[0])));
            // ?x ?r Elvis
            else return (mostFrequentValues(minFrequency, object2predicate2subjectSize.get(triple[2])));
          case 2: // we want the most frequent objects
            // Elvis ?r ?y
            if (isVariable(triple[1])) return (mostFrequentValues(minFrequency, subject2object2predicateSize.get(triple[0])));
            // ?x loves ?y
            return (mostFrequentValuesSet(minFrequency, predicate2object2subject.get(triple[1])));
        }
      case 3:
        final Map<String, Integer> sizes = pos == 0 ? subjectSize : pos == 1 ? predicateSize : objectSize;
        return (mostFrequentValues(minFrequency, sizes));
      default:
        throw new InvalidParameterException("Triple should contain at least 2 variables: " + Arrays.toString(triple));
    }
  }

  /** Makes a list of facts */
  public static List<String[]> triples(String[]... facts) {
    return (Arrays.asList(facts));
  }

  /** Makes a facts */
  public static String[] triple(String... fact) {
    return (fact);
  }

  /** test*/
  public static void main(String[] args) throws Exception {
    FactDatabase d = new FactDatabase();
    d.load(new File("/local/suchanek/yago2s/yagoTypes.ttl"), new File("/local/suchanek/yago2s/yagoFacts.ttl"));
    //d.load(new File("c:/fabian/data/yago2s/"), Pattern.compile("yago.*\\.ttl"));

    // Zero variables
    D.p("Contains Angela Merkel as person:", d.contains("<Angela_Merkel>", "rdf:type", "<wordnet_person_100007846>"));

    // Counting one variable
    D.p("Type facts about Angela Merkel:", d.resultsOneVariable("<Angela_Merkel>", "rdf:type", "?y"));
    D.p("Number of type facts about Angela Merkel:", d.count("<Angela_Merkel>", "rdf:type", "?y"));
    D.p("Type facts with person:", d.resultsOneVariable("?x", "rdf:type", "<wordnet_person_100007846>"));
    D.p("Number of type facts with person:", d.count("?x", "rdf:type", "<wordnet_person_100007846>"));
    D.p("Relationships between Angela and person:", d.resultsOneVariable("<Angela_Merkel>", "?r", "<wordnet_person_100007846>"));
    D.p("Number of relationships between Angela and person:", d.count("<Angela_Merkel>", "?r", "<wordnet_person_100007846>"));

    // Counting two variables
    D.p("Facts about Angela Merkel:", d.resultsTwoVariables("<Angela_Merkel>", "?r", "?y"));
    D.p("Number of facts about Angela Merkel:", d.count("<Angela_Merkel>", "?r", "?y"));
    D.p("Number of facts with relation rdf:type:", d.count("?x", "rdf:type", "?y"));
    D.p("Number of facts with object person:", d.count("?x", "?r", "<wordnet_person_100007846>"));

    // Most frequent values
    D.p("Most frequent relations of Angela Merkel:", d.mostFrequentValues(1, 1, "<Angela_Merkel>", "?r", "?y"));
    D.p("Most frequent objects of Angela Merkel:", d.mostFrequentValues(1, 2, "<Angela_Merkel>", "?r", "?y"));
    D.p("Most frequent subjects of rdf:type:", d.mostFrequentValues(1, 0, "?x", "rdf:type", "?y"));
    D.p("Most frequent objects of rdf:type:", d.mostFrequentValues(1, 2, "?x", "rdf:type", "?y"));
    D.p("Most frequent subjects of person:", d.mostFrequentValues(1, 0, "?x", "?r", "<wordnet_person_100007846>"));
    D.p("Most frequent relations of person:", d.mostFrequentValues(1, 1, "?x", "?r", "<wordnet_person_100007846>"));

    // Existence
    D.p("Angela type ?x subclass of ?y exists:",
        d.exists(triples(triple("<Angela_Merkel>", "rdf:type", "?x"), triple("?x", "rdfs:subClassOf", "?y"))));
    D.p("Angela type ?x subclass of person exists:",
        d.exists(triples(triple("<Angela_Merkel>", "rdf:type", "?x"), triple("?x", "rdfs:subClassOf", "<wordnet_person_100007846>"))));
    D.p("Angela type ?x subclass of ?y subclassof ?z exists:",
        d.exists(triples(triple("<Angela_Merkel>", "rdf:type", "?x"), triple("?x", "rdfs:subClassOf", "?y"), triple("?y", "rdfs:subClassOf", "?z"))));
    D.p("Angela type ?x subclass of ?y subclassof NONSENSE exists:",
        d.exists(triples(triple("<Angela_Merkel>", "rdf:type", "?x"), triple("?x", "rdfs:subClassOf", "?y"),
            triple("?y", "rdfs:subClassOf", "nonsense"))));

    // Count projection
    D.p("Number of ?x, such that Angela Merkel linksTo ?x:", d.countProjection(triple("<Angela_Merkel>", "<linksTo>", "?x"), triples()));
    D.p("Number of ?x, such that Angela Merkel linksTo ?x type person:",
        d.countProjection(triple("<Angela_Merkel>", "<linksTo>", "?x"), triples(triple("?x", "rdf:type", "<wordnet_person_100007846>"))));
    D.p("Number of ?x, such that Angela Merkel linksTo ?x type person ?x marriedTo ?y:",
        d.countProjection(triple("<Angela_Merkel>", "<linksTo>", "?x"),
            triples(triple("?x", "rdf:type", "<wordnet_person_100007846>"), triple("?x", "<isMarriedTo>", "?y"))));
    D.p("Number of ?x, such that Angela Merkel linksTo ?x type person ?x marriedTo Angela:",
        d.countProjection(triple("<Angela_Merkel>", "<linksTo>", "?x"),
            triples(triple("?x", "rdf:type", "<wordnet_person_100007846>"), triple("?x", "<isMarriedTo>", "<Angela_Merkel>"))));
    D.p("Number of ?x/?y, such that ?x marriedTo ?y type person:",
        d.countProjection(triple("?x", "<isMarriedTo>", "?y"), triples(triple("?y", "rdf:type", "<wordnet_person_100007846>"))));
  }
}
