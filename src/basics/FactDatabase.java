package basics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javatools.administrative.Announce;
import javatools.administrative.D;
import javatools.filehandlers.FileLines;
import javatools.parsers.Char;

/**
 * Class FactDatabase - YAGO2S
 * 
 * This class implements an efficient database of facts without identifiers.
 * 
 * @author Fabian M. Suchanek
 * 
 */
public class FactDatabase {

	/** Index*/
	protected final Map<String, Map<String, Set<String>>> subject2relation2object;

	/** Index*/
	protected final Map<String, Map<String, Set<String>>> subject2object2relation;

	/** Index*/
	protected final Map<String, Map<String, Set<String>>> relation2subject2object;

	/** Index*/
	protected final Map<String, Map<String, Set<String>>> relation2object2subject;

	/** Index*/
	protected final Map<String, Map<String, Set<String>>> object2relation2subject;

	/** Index*/
	protected final Map<String, Map<String, Set<String>>> object2subject2relation;

	/** Number of facts per subject*/
	protected final Map<String, Integer> subjectSize = new HashMap<String, Integer>();

	/** Number of facts per object*/
	protected final Map<String, Integer> objectSize = new HashMap<String, Integer>();

	/** Number of facts per relation*/
	protected final Map<String, Integer> relationSize = new HashMap<String, Integer>();

	/** Number of facts*/
	protected int size;

	/** Constructor*/
	public FactDatabase() {
		this(1000, 1000);
	}

	/** Constructor. Allocates enough space for numSubjects and numObjects*/
	public FactDatabase(int numSubjects, int numObjects) {
		subject2relation2object = new IdentityHashMap<String, Map<String, Set<String>>>(numSubjects);
		subject2object2relation = new IdentityHashMap<String, Map<String, Set<String>>>(numSubjects);
		relation2subject2object = new IdentityHashMap<String, Map<String, Set<String>>>();
		relation2object2subject = new IdentityHashMap<String, Map<String, Set<String>>>();
		object2relation2subject = new IdentityHashMap<String, Map<String, Set<String>>>(numObjects);
		object2subject2relation = new IdentityHashMap<String, Map<String, Set<String>>>(numObjects);
	}

	/** Adds a fact*/
	protected boolean add(String subject, String relation, String object, Map<String, Map<String, Set<String>>> map,
			Map<String, Integer> sizeMap) {
		Map<String, Set<String>> relation2object = map.get(subject);
		if (relation2object == null)
			map.put(subject, relation2object = new HashMap<String, Set<String>>());
		Set<String> objects = relation2object.get(relation);
		if (objects == null)
			relation2object.put(relation, objects = new HashSet<String>());
		if (objects.add(object)) {
			if (sizeMap != null)
				D.addKeyValue(sizeMap, subject, 1);
			return (true);
		} else {
			return (false);
		}
	}

	/** Adds a fact*/
	public boolean add(String subject, String relation, String object) {
		subject = subject.trim().intern();
		relation = relation.trim().intern();
		object = object.trim().intern();
		if (!add(subject, relation, object, subject2relation2object, subjectSize))
			return (false);
		add(subject, object, relation, subject2object2relation, null);
		add(relation, subject, object, relation2subject2object, relationSize);
		add(relation, object, subject, relation2object2subject, null);
		add(object, relation, subject, object2relation2subject, objectSize);
		add(object, subject, relation, object2subject2relation, null);
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
		int size = size();
		if (f.isDirectory()) {
			Announce.doing("Loading files in " + f.getName());
			for (File file : f.listFiles())
				load(file);
			Announce.done("Loaded " + (size() - size) + " facts");
		}
		for (String line : new FileLines(f, "UTF-8", "Loading " + f.getName())) {
			if (line.endsWith("."))
				line = Char.cutLast(line);
			String[] split = line.split("\t");
			if (split.length == 3)
				add(split[0], split[1], split[2]);
			else if (split.length == 4)
				add(split[1], split[2], split[3]);
		}
		Announce.message("  Loaded", (size() - size), "facts");
	}

	/** Returns the result of the map for key1 and key2*/
	protected Set<String> get(Map<String, Map<String, Set<String>>> map, String key1, String key2) {
		Map<String, Set<String>> m = map.get(key1.intern());
		if (m == null)
			return (Collections.emptySet());
		Set<String> r = m.get(key2.intern());
		if (r == null)
			return (Collections.emptySet());
		return (r);
	}

	/** Returns the results of the triple pattern query, if it contains exactly 1 variable*/
	public Set<String> resultsOneVariable(String subject, String relation, String object) {
		if (!isVariable(subject) && !isVariable(relation))
			return (get(subject2relation2object, subject.intern(), relation));
		if (!isVariable(subject) && !isVariable(object))
			return (get(subject2object2relation, subject, object));

		if (!isVariable(relation) && !isVariable(subject))
			return (get(relation2subject2object, relation, subject));
		if (!isVariable(relation) && !isVariable(object))
			return (get(relation2object2subject, relation, object));

		if (!isVariable(object) && !isVariable(relation))
			return (get(object2relation2subject, object, relation));
		if (!isVariable(object) && !isVariable(subject))
			return (get(object2subject2relation, object, subject));

		throw new RuntimeException("Exactly one components should be unbound");
	}

	/** TRUE if the database contains this fact (no variables)*/
	public boolean contains(String subject, String relation, String object) {
		return (resultsOneVariable(subject, relation, null).contains(object));
	}

	/** Returns map results for key*/
	protected Map<String, Set<String>> get(Map<String, Map<String, Set<String>>> map, String key1) {
		Map<String, Set<String>> m = map.get(key1.intern());
		if (m == null)
			return (Collections.emptyMap());
		else
			return (m);
	}

	/** Returns the results of a triple query pattern with two variables as a map of first value to set of second values*/ 
	public Map<String, Set<String>> resultsTwoVariables(String subject, String relation, String object) {
		if (!isVariable(subject))
			return (get(subject2relation2object, subject));
		if (!isVariable(relation))
			return (get(relation2subject2object, relation));
		if (!isVariable(object))
			return (get(object2subject2relation, object));
		throw new RuntimeException("Exactly two components should be unbound");
	}

	/** Returns number of results of the triple pattern query with 1 variable*/
	public int countOneVariable(String subject, String relation, String object) {
		return (resultsOneVariable(subject, relation, object).size());
	}

	/** Returns number of results of the triple pattern query with 2 variables*/
	public int countTwoVariables(String subject, String relation, String object) {
		if (!isVariable(subject))
			return (D.getOr(subjectSize, subject, 0));
		if (!isVariable(relation)) {
			return (D.getOr(relationSize, relation, 0));
			//throw new UnsupportedOperationException("Reflexive triple pattern");
		}
		if (!isVariable(object))
			return (D.getOr(objectSize, object, 0));
		throw new RuntimeException("Exactly two components should be unbound");
	}

	/** Returns number of variable occurrences in a triple*/
	public static int numVariables(String... fact) {
		int counter = 0;
		for (int i = 0; i < fact.length; i++)
			if (isVariable(fact[i]))
				counter++;
		return (counter);
	}

	/** returns number of instances of this triple*/
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

	/** Returns the number of instantiations for a triple pattern with 3 variables*/  
	public int countThreeVariables(String subject, String relation, String object) {
		return (size());
	}

	/** Returns the first variable of the pattern*/
	public static String firstVariable(String... fact) {
		for (int i = 0; i < fact.length; i++)
			if (isVariable(fact[i]))
				return (fact[i]);
		return (null);
	}

	/** TRUE if the query result exists*/
	public boolean exists(List<String[]> triples) {
		if (triples.isEmpty())
			return (false);
		if (triples.size() == 1)
			return (count(triples.get(0)[0], triples.get(0)[1], triples.get(0)[2]) != 0);
		String[] best = null;
		int count = Integer.MAX_VALUE;
		for (String[] triple : triples) {
			int myCount = count(triple[0], triple[1], triple[2]);
			if (myCount > count)
				continue;
			best = triple;
			count = myCount;
		}
		switch (numVariables(best)) {
		case 0:
			if (!contains(best[0], best[1], best[2]))
				return (false);
			List<String[]> newList = new ArrayList<String[]>(triples);
			newList.remove(best);
			return (exists(newList));
		case 1:
			String variable = firstVariable(best);
			List<String[]> newList2 = new ArrayList<String[]>(triples);
			newList2.remove(best);
			for (String inst : resultsOneVariable(best[0], best[1], best[2])) {
				if (exists(instantiate(newList2, variable, inst)))
					return (true);
			}
			return (false);
		case 2:
			List<String[]> newList3 = new ArrayList<String[]>(triples);
			newList3.remove(best);
			String variable1 = firstVariable(best);
			Map<String, Set<String>> instantiations = resultsTwoVariables(best[0], best[1], best[2]);
			for (String val1 : instantiations.keySet()) {
				String[] best1 = instantiate(best, variable1, val1);
				String variable2 = firstVariable(best1);
				List<String[]> newList4 = instantiate(newList3, variable1, val1);
				for (String val2 : instantiations.get(val1)) {
					if (exists(instantiate(newList4, variable2, val2)))
						return (true);
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
		if (otherTriples.isEmpty())
			return (count(projectionTriple[0], projectionTriple[1], projectionTriple[2]));
		switch (numVariables(projectionTriple)) {
		case 0:
			return (count(projectionTriple[0], projectionTriple[1], projectionTriple[2]));
		case 1:
			int counter = 0;
			String variable = firstVariable(projectionTriple);
			for (String inst : resultsOneVariable(projectionTriple[0], projectionTriple[1], projectionTriple[2])) {
				if (exists(instantiate(otherTriples, variable, inst)))
					counter++;
			}
			return (counter);
		case 2:
			counter = 0;
			String variable1 = firstVariable(projectionTriple);
			Map<String, Set<String>> instantiations = resultsTwoVariables(projectionTriple[0], projectionTriple[1],
					projectionTriple[2]);
			for (String val1 : instantiations.keySet()) {
				String[] projectionTriple2 = instantiate(projectionTriple, variable1, val1);
				String variable2 = firstVariable(projectionTriple2);
				List<String[]> otherTriples2 = instantiate(otherTriples, variable1, val1);
				for (String val2 : instantiations.get(val1)) {
					if (exists(instantiate(otherTriples2, variable2, val2)))
						counter++;
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
				if (newT == triple)
					newT = Arrays.copyOf(triple, triple.length);
				newT[i] = inst;
			}
		}
		return (newT);
	}

	/** Instatiates a list of triples*/
	protected List<String[]> instantiate(List<String[]> triples, String variable, String inst) {
		List<String[]> result = new ArrayList<String[]>();
		for (String[] triple : triples) {
			result.add(instantiate(triple, variable, inst));
		}
		return (result);
	}

	/** test*/
	public static void main(String[] args) throws Exception {
		FactDatabase d = new FactDatabase(200000, 200000);
		d.load(new File("/Users/Fabian/Fabian/Work/yago2/newfacts/wordnetClasses.ttl"));
		List<String[]> q = new ArrayList<String[]>();
		q.add(new String[] { "<wordnet_animal_100015388>", "?r", "?d" });
		D.p(true, d.exists(q));
		q.clear();
		q.add(new String[] { "<wordnet_animal_100015388>", "blah", "?d" });
		D.p(false, d.exists(q));
		q.clear();
		q.add(new String[] { "<wordnet_animal_100015388>", "rdfs:subClassOf", "?d" });
		D.p(true, d.exists(q));
		q.clear();
		q.add(new String[] { "<wordnet_animal_100015388>", "?r", "blah" });
		D.p(false, d.exists(q));
		q.clear();
		q.add(new String[] { "<wordnet_animal_100015388>", "?x", "<wordnet_organism_100004475>" });
		D.p(true, d.exists(q));
		q.clear();
		q.add(new String[] { "<wordnet_animal_100015388>", "?r", "?d" });
		q.add(new String[] { "?d", "?r", "?e" });
		D.p(true, d.exists(q));
		q.clear();
		q.add(new String[] { "<wordnet_animal_100015388>", "rdfs:subClassOf", "?d" });
		q.add(new String[] { "?d", "rdfs:subClassOf", "?e" });
		D.p(true, d.exists(q));
		q.clear();
		q.add(new String[] { "<wordnet_animal_100015388>", "rdfs:subClassOf", "?d" });
		q.add(new String[] { "?d", "rdfs:subClassOf", "?d" });
		D.p(false, d.exists(q));

		String[] projectionTriple=new String[] { "<wordnet_person_100007846>", "rdfs:subClassOf", "?d" };
		q.clear();
		D.p(2, d.countProjection(projectionTriple, q));
		q.clear();
		q.add(new String[] { "?d", "rdfs:subClassOf", "?e" });
		D.p(2, d.countProjection(projectionTriple, q));
		
	}
}
