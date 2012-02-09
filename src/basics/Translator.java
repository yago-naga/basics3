package basics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javatools.administrative.D;
import javatools.datatypes.FinalMap;
import javatools.filehandlers.FileLines;
import javatools.parsers.Char;

/**
 * Translates old hard facts to new hard facts
 * 
 * THIS IS TO BE RUN ONLY ONCE!
 * 
 * @author Fabian
 * 
 */
public class Translator {

	/** Mapping to new identifiers */
	public static Map<String, String> identifierMap = new FinalMap<String, String>("means", "rdfs:label",
			"hasDomain", "rdfs:domain", "hasRange", "rdfs:range",
			"type", "rdf:type", "yagoFact", "rdf:Statement", "yagoString", "xsd:string", "subpropertyOf",
			" rdfs:subPropertyOf", "subclassOf", "rdfs:subClassOf", "hasPreferredName", "skos:prefLabel",
			"yagoRationalNumber", "xsd:decimal", "yagoBoolean", "xsd:boolean", "yagoDuration", "xsd:duration",
			"yagoInteger", "xsd:integer", "yagoNonNegativeInteger", "xsd:nonNegativeInteger", "yagoNumber",
			"xsd:decimal", "yagoDate", "xsd:date", "yagoYear", "xsd:gYear", "wordnet_entity_100001740", "rdf:Resource",
			"yagoRelation", "rdf:Property", "yagoClass", "rdf:Class");

	/** Translates an array to a Fact */
	public static Fact makeFact(String id, String arg1, String relation, String arg2) {
		arg1 = Char.decodeBackslash(D.getOr(identifierMap, arg1, arg1));
		relation = Char.decodeBackslash(D.getOr(identifierMap, relation, relation));
		arg2 = Char.decodeBackslash(D.getOr(identifierMap, arg2, arg2));
		if (arg1.startsWith("\"") && !arg2.startsWith("\"") && relation.indexOf(':')==-1) {
			String temp = arg1;
			arg1 = arg2;
			arg2 = temp;
			relation=relation.replace("has", "is")+"Of";
		}
		return (new Fact(FactComponent.forAny(id), FactComponent.forAny(arg1), FactComponent.forAny(relation),
				FactComponent.forAny(arg2)));
	}

	/** Writes a fact */
	public static void writeFact(N4Writer writer, Fact f, List<String[]> attached) throws IOException {
		if (f == null)
			return;
		if (attached.isEmpty()) {
			writer.write(f);
		} else {
			f.id = FactComponent.makeId();
			writer.write(f);
			for (String[] a : attached) {
				writer.write(makeFact(null, f.id, a[1], a[2]));
			}
		}
	}

	/** Translates a TSV file */
	public static void translate(File oldFile, File newFile) throws Exception {
		Fact previous = null;
		List<String[]> attached = new ArrayList<String[]>();
		N4Writer writer = new N4Writer(newFile, "Manually coded facts of YAGO.\n DO NOT OVERWRITE!");
		for (String line : new FileLines(oldFile, "Translating from " + oldFile.getName())) {
			String[] split = line.split("\t");
			if (split.length != 3)
				continue;
			if (split[0].length() == 0) {
				attached.add(split);
			} else {
				writeFact(writer, previous, attached);
				previous = makeFact(null, split[0], split[1], split[2]);
				attached.clear();
			}
		}
		writeFact(writer, previous, attached);
		writer.close();
	}

	/** Translates all files */
	public static void main(String[] args) throws Exception {
		if (D.readBoolean("Are you sure you want to overwrite the new manual hard facts?")) {
			for (File f : new File("/Users/Fabian/Fabian/Work/EclipseProjects/yago2/data").listFiles()) {
				if (!f.getName().endsWith(".tsv"))
					continue;
				translate(f, new File("/Users/Fabian/Fabian/Work/EclipseProjects/yago2s/data/"
						+ f.getName().replace("tsv", "ttl")));
			}
		}
	}
}
