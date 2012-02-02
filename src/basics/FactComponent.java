package basics;

import java.util.Map;
import java.util.Map.Entry;

import javatools.datatypes.FinalMap;
import javatools.parsers.Char;

/**
 * Class FactComponent - YAGO2S
 * 
 * Formats an RDF item to be used with Fact.java
 * 
 * Conventions:
 * (1) YAGO entities are always given as relative URIs "<Albert_Einstein>".
 * This is because qnames may not contain certain characters
 * (2) All entities from standard namespaces are given as qnames
 * This is to save space and keep readability.
 * (3) All other entities are given as full URIs "<http://...>"
 * 
 * @author Fabian M. Suchanek
 */
public class FactComponent {
	
	/** YAGO namespace*/
	public static final String YAGONAMESPACE="http://yago-knowledge/resource/";
	
	/** Standard namespace prefixes that this N4Reader will assume */
	public static final Map<String, String> standardPrefixes = new FinalMap<String, String>("rdf:",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdfs:", "http://www.w3.org/2000/01/rdf-schema#", "xsd:",
			"http://www.w3.org/2001/XMLSchema#", "owl:", "http://www.w3.org/2002/07/owl#", "dc:",
			"http://purl.org/dc/terms/", "foaf:", "http://xmlns.com/foaf/0.1/", "vcard:",
			"http://www.w3.org/2006/vcard/ns#");
	

	/** Some id counter for blank nodes*/
	public static int ids=0;

	/** Creates a new unique YAGO id*/
	public static String makeId() {
		return(forQname("y:","id"+(ids++)));
	}
	
	/** Creates a fact component for a URI*/
	public static String forUri(String s) {
		if(s.startsWith(YAGONAMESPACE)) {
			return('<'+s.substring(YAGONAMESPACE.length())+'>');
		}
		if(s.startsWith("http://")) {
			for(Entry<String,String> entry :standardPrefixes.entrySet()) {
				if(s.startsWith(entry.getValue())) {
					return(forQname(entry.getKey(),s.substring(entry.getValue().length())));
				}
			}
		}
		return ('<'+s+'>');
	}
	
	/** Creates a fact component for number*/
	public static String forNumber(String s) {
		if(s.indexOf('.')==-1 && s.indexOf("e")==-1 && s.indexOf("E")==-1) return (forString(s,null,forQname("xsd:","integer")));
		return (forString(s,null,forQname("xsd:","decimal")));
	}

	/** Creates a fact component for a Qname*/
	public static String forQname(String prefixWithColon,String name) {
		if(prefixWithColon.equals("y:")) return(forUri(name));
		return(prefixWithColon+name);
	}
	
	/** Creates a fact component for a String*/
	public static String forString(String string,String language,String datatype) {
		if(datatype!=null) return ('"'+Char.encodeBackslash(string,turtleString)+"\"^^"+datatype);
		return ('"'+Char.encodeBackslash(string,turtleString)+'"');
	}
	
	/** Turtle valid string characters */
	public static Char.Legal turtleString= new Char.Legal() {
		public boolean isLegal(char c) {
			if(c=='"') return(false);
			if(c=='\\') return(false);
			if(c<0x20) return(false);
			return(true);
		}
	};
}
