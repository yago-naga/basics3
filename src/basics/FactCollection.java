package basics;

import java.io.File;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javatools.administrative.Announce;

/**
 * Class FactCollection - YAGO2S
 * 
 * Represents a collection of facts, indexes them
 * 
 * @author Fabian M. Suchanek
 */
public class FactCollection extends AbstractSet<Fact> {

	private static final long serialVersionUID = -1L;

	/** Holds the facts */
	protected Set<Fact> facts;
	/** Maps first arg to relation to facts */
	protected Map<String, Map<String, List<Fact>>> index = Collections
			.synchronizedMap(new TreeMap<String, Map<String, List<Fact>>>());
	/** Maps relation to facts */
	protected Map<String, List<Fact>> relindex = Collections.synchronizedMap(new TreeMap<String, List<Fact>>());

	public synchronized boolean add(Fact fact) {
		if (facts.contains(fact)) {
			Announce.debug("Duplicate fact not added:", fact);
			return (false);
		}
		if (fact.arg1.equals(fact.arg2)) {
			Announce.debug("Identical arguments not added", fact);
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

	/** Returns facts with matching relation */
	public List<Fact> get(String relation) {
		if (!relindex.containsKey(relation))
			return (EMPTY);
		return (relindex.get(relation));
	}

	/** Loads from N4 file */
	public FactCollection(File n4File) throws IOException {
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
		Announce.doing("Loading", n4File);
		for (Fact f : new N4Reader(n4File)) {
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
}
