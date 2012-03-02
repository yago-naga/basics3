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
import javatools.filehandlers.FileSet;

/**
 * Class FactCollection - YAGO2S
 * 
 * Represents a collection of facts, indexes them
 * 
 * @author Fabian M. Suchanek
 */
public class FactCollection extends AbstractSet<Fact> {

	/** Holds the facts */
	protected Set<Fact> facts;
	/** Maps first arg to relation to facts */
	protected Map<String, Map<String, List<Fact>>> index = Collections
			.synchronizedMap(new TreeMap<String, Map<String, List<Fact>>>());
	/** Maps relation to facts */
	protected Map<String, List<Fact>> relindex = Collections.synchronizedMap(new TreeMap<String, List<Fact>>());

	public synchronized boolean add(Fact fact) {
		if (facts.contains(fact)) {
			Announce.warning("Duplicate fact not added:", fact);
			return (false);
		}
		if (fact.arg1.equals(fact.arg2)) {
			Announce.warning("Identical arguments not added", fact);
			return (false);
		}
		facts.add(fact);
		if (!index.containsKey(fact.arg1))
			index.put(fact.arg1, Collections.synchronizedMap(new TreeMap<String, List<Fact>>()));
		if (!index.get(fact.arg1).containsKey(fact.relation))
			index.get(fact.arg1).put(fact.relation, Collections.synchronizedList(new ArrayList<Fact>(1)));
		index.get(fact.arg1).get(fact.relation).add(fact);
		if (!relindex.containsKey(fact.relation))
			relindex.put(fact.relation, Collections.synchronizedList(new ArrayList<Fact>(1)));
		relindex.get(fact.relation).add(fact);
		return (true);
	}

	/** Empty list */
	protected static final List<Fact> EMPTY = new ArrayList<Fact>(0);

	/** Returns facts with matching first arg and relation */
	public List<Fact> get(String arg1, String relation) {
		if (!index.containsKey(arg1))
			return (EMPTY);
		if (!index.get(arg1).containsKey(relation))
			return (EMPTY);
		return (index.get(arg1).get(relation));
	}

	/** Returns the first arg2 (or null) */
	public String getArg2(String arg1, String relation) {
		List<Fact> res = get(arg1, relation);
		if (res == null || res.isEmpty())
			return (null);
		return (res.get(0).arg2);
	}

	/** Returns the arg2s */
	public Set<String> getArg2s(String arg1, String relation) {
		Set<String> result=new TreeSet<>();
		for(Fact f : get(arg1, relation)) {
			result.add(f.getArg(2));
		}
		return (result);
	}

	/** Returns facts with matching relation */
	public List<Fact> get(String relation) {
		if (!relindex.containsKey(relation))
			return (EMPTY);
		return (relindex.get(relation));
	}

	/**
	 * Returns facts with matching relation and second argument. This is very
	 * slow.
	 */
	public List<Fact> getBySecondArgSlow(String relation, String arg2) {
		if (!relindex.containsKey(relation))
			return (EMPTY);
		List<Fact> result = new ArrayList<Fact>();
		for (Fact f : relindex.get(relation)) {
			if (f.arg2.equals(arg2))
				result.add(f);
		}
		return (result);
	}

	/** Loads from N4 file */
	public FactCollection(File n4File) throws IOException {
		facts = Collections.synchronizedSet(new HashSet<Fact>());
		load(n4File);
	}

	/** Loads from N4 file */
	public FactCollection(FactSource n4File) throws IOException {
		facts = Collections.synchronizedSet(new HashSet<Fact>());
		load(n4File);
	}

	public FactCollection() {
		facts = Collections.synchronizedSet(new HashSet<Fact>());
	}

	public FactCollection(int capacity) {
		facts = Collections.synchronizedSet(new HashSet<Fact>(capacity));
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
		if (!facts.remove(f))
			return (false);
		Fact fact = (Fact) f;
		index.get(fact.arg1).get(fact.relation).remove(fact);
		relindex.get(fact.relation).remove(fact);
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
		if (!n4File.getName().contains("."))
			n4File = FileSet.newExtension(n4File, ".ttl");
		Announce.doing("Loading", n4File);
		load(FactSource.from(n4File));
		Announce.done();
	}
	
	/** Loads from N4 file */
	public void load(FactSource reader) throws IOException {
		Announce.doing("Loading",reader);
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
		Announce.doing("Checking containment");
		boolean matches = true;
		next: for (Fact fact : facts) {
			for (Fact other : goldStandard.get(fact.arg1, fact.relation)) {
				if (other.arg2.equals(fact.arg2))
					continue next;
			}
			Announce.message("Not found:", fact);
			matches = false;
		}
		Announce.done();
		return (matches);
	}

	/** Checks for differences, returns TRUE if equal, prints differences */
	public boolean checkEqual(FactCollection goldStandard) {
		Announce.doing("Comparing fact collections");
		boolean b = checkContainedIn(goldStandard) & goldStandard.checkContainedIn(this);
		Announce.done();
		return (b);
	}

	/** TRUE if this collection contains this fact with any id */
	public boolean contains(String arg1, String rel, String arg2) {
		Map<String, List<Fact>> map = index.get(arg1);
		if (map == null)
			return (false);
		List<Fact> facts = map.get(rel);
		if (facts == null)
			return (false);
		for (Fact f : facts) {
			if (f.getArg(2).equals(arg2))
				return (true);
		}
		return (false);
	}

	/** Returns a map for a relation */
	public Map<String, String> asStringMap(String relation) {
		Map<String, String> objects = new HashMap<String, String>();
		for (Fact fact : get(relation)) {
			objects.put(fact.getArgJavaString(1), fact.getArgJavaString(2));
		}
		if (objects.isEmpty())
			Announce.warning("No instances of", relation, "found");
		return (objects);
	}

	/** Returns a set of strings for a type */
	public Set<String> asStringSet(String type) {
		Set<String> result = new TreeSet<String>();
		for (Fact fact : getBySecondArgSlow("rdf:type", type)) {
			result.add(fact.getArgJavaString(1));
		}
		if(result.isEmpty()) Announce.warning("No instances of",type,"found");
		return (result);
	}

	
	/** TRUE if the object is an instance of the class */
	public boolean instanceOf(String instance, String clss) {	
		Collection<String> classes;
		if(FactComponent.isLiteral(instance)) {
			classes=Arrays.asList(FactComponent.getDataType(instance));
		} else {
			classes=getArg2s(instance,RDFS.type);
		}
		for(String c : classes) {
			if(isSubClassOf(c,clss)) return(true);
		}
		return(false);
	}

	/** TRUE if the first class is equal to or a subclass of the second*/
	public boolean isSubClassOf(String sub, String supr) {
		if(sub.equals(supr)) return(true);
		for(String s : getArg2s(sub,RDFS.subclassOf)) {
			if(isSubClassOf(s, supr)) return(true);
		}
		return(false);
	}

	/** Adds the superclasses of this class*/
	public void superClasses(String sub, Set<String> set) {
		set.add(sub);
		for(String s : getArg2s(sub,RDFS.subclassOf)) {
			superClasses(s, set);
		}
	}

}
