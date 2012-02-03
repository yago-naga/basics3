package basics;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javatools.datatypes.FinalMap;
import javatools.filehandlers.FileLines;

/**
 * Translates old hard facts to new hard facts
 * 
 * @author Fabian
 * 
 */
public class Translator {

	public static Map<String, String> identifierMap = new FinalMap<String, String>("means", "rdfs:label",
			"hasPreferredMeaning", "<isPreferredMenaingOf>", "hasDomain", "rdfs:domain", "hasRange", "rdfs:range",
			"type", "rdf:type", "yagoFact", "rdf:Statement", "yagoString", "xsd:string", "subpropertyOf",
			" rdfs:subPropertyOf", "subclassOf", "rdfs:subClassOf","hasPreferredName","skos:prefLabel",
			"yagoRationalNumber","xsd:decimal","yagoBoolean","xsd:boolean","yagoDuration","xsd:duration","yagoInteger","xsd:integer",
			"yagoNonNegativeInteger","xsd:nonNegativeInteger","yagoNumber","xsd:decimal","yagoDate","xsd:date","yagoYear","xsd:gYear");

	/** Loads from TSV file */
	public void loadFrom(File oldFile, File newFile) throws IOException {
		Fact previous = null;
		N4Writer writer=new N4Writer(newFile,"Manually coded facts of YAGO.\n DO NOT OVERWRITE!");
		for (String line : new FileLines(oldFile, "Translating from " + oldFile)) {
			String[] split = line.split("\t");
			if (split.length != 3)
				continue;
			if (split[0].length() == 0)
				add(previous = new Fact(previous, split[1].trim(), split[2].trim()), false);
			else
				add(previous = new Fact(split[0].trim(), split[1].trim(), split[2].trim(), technique), false);
		}
		writer.close();
	}

}
