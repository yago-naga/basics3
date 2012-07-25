package basics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javatools.administrative.D;
import javatools.filehandlers.FileLines;

/**
 * This is a database of facts without identifiers.
 * 
 * @author Fabian M. Suchanek
 *
 */
public class FactDatabase {

  protected Map<String, Map<String, Set<String>>> subject2relation2object = new HashMap<>();

  protected Map<String, Map<String, Set<String>>> subject2object2relation = new HashMap<>();

  protected Map<String, Map<String, Set<String>>> relation2subject2object = new HashMap<>();

  protected Map<String, Map<String, Set<String>>> relation2object2subject = new HashMap<>();

  protected Map<String, Map<String, Set<String>>> object2relation2subject = new HashMap<>();

  protected Map<String, Map<String, Set<String>>> object2subject2relation = new HashMap<>();

  protected Map<String, Integer> subjectSize = new HashMap<>();

  protected Map<String, Integer> objectSize = new HashMap<>();

  protected Map<String, Integer> relationSize = new HashMap<>();

  protected int size;

  protected boolean add(String subject, String relation, String object, Map<String, Map<String, Set<String>>> map, Map<String, Integer> sizeMap) {
    Map<String, Set<String>> relation2object = map.get(subject);
    if (relation2object == null) map.put(subject, relation2object = new HashMap<>());
    Set<String> objects = relation2object.get(relation);
    if (objects == null) relation2object.put(relation, objects = new HashSet<>());
    if (objects.add(object)) {
      if (sizeMap != null) D.addKeyValue(sizeMap, subject, 1);
      return (true);
    } else {
      return (false);
    }
  }

  public boolean add(String subject, String relation, String object) {
    subject = subject.intern();
    relation = relation.intern();
    object = object.intern();
    if (!add(subject, relation, object, subject2relation2object, subjectSize)) return (false);
    add(subject, object, relation, subject2object2relation, null);
    add(relation, subject, object, relation2subject2object, relationSize);
    add(relation, object, subject, relation2object2subject, null);
    add(object, relation, subject, object2relation2subject, objectSize);
    add(object, subject, relation, object2subject2relation, null);
    size++;
    return (true);
  }

  public int size() {
    return (size);
  }

  public static boolean isVariable(String s) {
    return (s.startsWith("?"));
  }

  public void load(File f) throws IOException {
    for (String line : new FileLines(f, "UTF-8", "Loading " + f.getName())) {
      String[] split = line.split("\t");
      if (split.length == 3) add(split[0], split[1], split[2]);
      else if (split.length == 4) add(split[1], split[2], split[3]);
    }
  }

  protected Set<String> get(Map<String, Map<String, Set<String>>> map, String key1, String key2) {
    Map<String, Set<String>> m = map.get(key1);
    if (m == null) return (Collections.emptySet());
    Set<String> r = m.get(key2);
    if (r == null) return (Collections.emptySet());
    return (r);
  }

  public Set<String> resultsOneVariable(String subject, String relation, String object) {
    if (!isVariable(subject) && !isVariable(relation)) return (get(subject2relation2object, subject, relation));
    if (!isVariable(subject) && !isVariable(object)) return (get(subject2object2relation, subject, object));

    if (!isVariable(relation) && !isVariable(subject)) return (get(relation2subject2object, relation, subject));
    if (!isVariable(relation) && !isVariable(object)) return (get(relation2object2subject, relation, object));

    if (!isVariable(object) && !isVariable(relation)) return (get(object2relation2subject, object, relation));
    if (!isVariable(object) && !isVariable(subject)) return (get(object2subject2relation, object, subject));

    throw new RuntimeException("Exactly one components should be unbound");
  }

  public boolean contains(String subject, String relation, String object) {
    return (resultsOneVariable(subject, relation, null).contains(object));
  }

  protected Map<String, Set<String>> get(Map<String, Map<String, Set<String>>> map, String key1) {
    Map<String, Set<String>> m = map.get(key1);
    if (m == null) return (Collections.emptyMap());
    else return (m);
  }

  public Map<String, Set<String>> resultsTwoVariables(String subject, String relation, String object) {
    if (!isVariable(subject)) return (get(subject2relation2object, subject));
    if (!isVariable(relation)) return (get(relation2subject2object, relation));
    if (!isVariable(object)) return (get(object2subject2relation, object));
    throw new RuntimeException("Exactly two components should be unbound");
  }

  public int countOneVariable(String subject, String relation, String object) {
    return (resultsOneVariable(subject, relation, object).size());
  }

  public int countTwoVariables(String subject, String relation, String object) {
    if (subject != null) return (D.getOr(subjectSize, subject, 0));
    if (relation != null) return (D.getOr(relationSize, subject, 0));
    if (object != null) return (D.getOr(objectSize, subject, 0));
    throw new RuntimeException("Exactly two components should be unbound");
  }

  public static int numVariables(String... fact) {
    int counter = 0;
    for (int i = 0; i < fact.length; i++)
      if (isVariable(fact[i])) counter++;
    return (counter);
  }

  public int count(String subject, String relation, String object) {
    switch (numVariables(subject, relation, object)) {
      case 0:
        return (contains(subject, relation, object) ? 1 : 0);
      case 1:
        return (countOneVariable(subject, relation, object));
      case 2:
        return (countTwoVariables(subject, relation, object));
      case 3:
        return (countThreeVariables(subject, relation, object));
    }
    return (-1);
  }

  public int countThreeVariables(String subject, String relation, String object) {
    return (size());
  }

  public static String variable(String... fact) {
    for (int i = 0; i < fact.length; i++)
      if (isVariable(fact[i])) return (fact[i]);
    return (null);
  }

  public int count(List<String[]> triples) {
    if (triples.isEmpty()) return (0);
    if (triples.size() == 1) return (count(triples.get(0)[0], triples.get(0)[1], triples.get(0)[2]));
    String[] best = null;
    int count = Integer.MAX_VALUE;
    for (String[] triple : triples) {
      int myCount = count(triple[0], triple[1], triple[2]);
      if (myCount > count) continue;
      best = triple;
      count = myCount;
    }
    List<String[]> newList = new ArrayList<>(triples);
    newList.remove(best);
    switch (numVariables(best)) {
      case 0:
        if (!contains(best[0], best[1], best[2])) return (0);
        return (count(newList));
      case 1:
        int counter = 0;
        String variable=variable(best);
        for (String inst : resultsOneVariable(best[0], best[1], best[2])) {
          counter += count(instantiate(newList, variable, inst));
        }
        return (counter);
      case 2:
        throw new RuntimeException("Not yet supported" + D.toString((Object[]) best));
    }
    throw new RuntimeException("Too many unbound variables in " + D.toString((Object[]) best));
  }

  protected List<String[]> instantiate(List<String[]> triples, String variable, String inst) {
    List<String[]> result=new ArrayList<>();
    for(String[] triple : triples) {
      String[] newT=new String[3];
      for(int i=0;i<triple.length;i++) {
        if(triple[i].equals(variable)) newT[i]=inst;
        else newT[i]=triple[i];
      }
      result.add(newT);
    }
    return(result);
  }
  
  public static void main(String[] args) throws Exception {
    FactDatabase d=new FactDatabase();
    d.load(new File("c:/fabian/data/yago2s/yagoFacts.ttl"));
    List<String[]>  q=new ArrayList<>();
    q.add(new String[]{"<Angela_Merkel>","?r","?d"});
    D.p(d.count(q));
  }
}
