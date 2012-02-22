package basics;

import java.util.Map;
import java.util.Map.Entry;

import javatools.datatypes.FinalMap;
import javatools.parsers.Char;
import javatools.parsers.DateParser;
import javatools.parsers.NumberFormatter;

/**
 * Class FactComponent - YAGO2S
 * 
 * Formats an RDF item to be used with Fact.java
 * The format is Turtle
 * http://www.w3.org/TeamSubmission/turtle/#sec-grammar-grammar 
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
			"http://www.w3.org/2006/vcard/ns#","skos:","http://www.w3.org/2004/02/skos/core#");
	

	/** Some id counter for blank nodes*/
	public static int ids=0;

	/** Creates a new unique YAGO id*/
	public static String makeId() {
		return(forQname("y:","id_"+NumberFormatter.timeStamp()+"_"+(ids++)));
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
		return ('<'+Char.encodeBackslash(s,turtleUri)+'>');
	}
	
	/** Creates a fact component for number. We don't do any syntax checks here.*/
	public static String forNumber(String s) {
		if(DateParser.isDate(s)) return (forString(s,null,forQname("xsd:","date")));
		if(s.indexOf('.')==-1 && s.indexOf("e")==-1 && s.indexOf("E")==-1) return (forString(s,null,forQname("xsd:","integer")));
		return (forString(s,null,forQname("xsd:","decimal")));
	}

	/** Creates a fact component for a Qname. We don't do any syntax checks here.*/
	public static String forQname(String prefixWithColon,String name) {
		if(prefixWithColon.equals("y:")) return(forUri(name));
		return(prefixWithColon+name);
	}

	/** Creates a fact component for a date. No checks done.*/
	public static String forDate(String date) {
		return(forString(date, null, "xsd:date"));
	}

	/** Creates a fact component for a year. No checks done.*/
	public static String forYear(String year) {
		return(forString(year+"-00-00", null, "xsd:date"));
	}

	/** Creates a fact component for a YAGO entity*/
	public static String forYagoEntity(String name) {
		return(forUri(name));		
	}
	
	/** Creates a fact component for a String. We check the syntax*/
	public static String forString(String string,String language,String datatype) {
		if(datatype!=null) return ('"'+Char.encodeBackslash(string,turtleString)+"\"^^"+datatype);
		return ('"'+Char.encodeBackslash(string,turtleString)+'"');
	}

	/** Creates a fact component for a String. We check the syntax*/
	public static String forString(String string) {
		return (forString(string,null,null));
	}

	/** Creates a fact component for a wordnet entity*/
	public static String forWordnetEntity(String word, String id) {
		return(forYagoEntity("wordnet_"+word.replace(' ','_')+"_"+id));
	}
	
	/** Translates anything into a FactComponent*/
	public static String forAny(String s) {
		if(s==null || s.length()==0) return(null);
		if(s.startsWith("\"")) {
			return(forString(Char.cutLast(s.substring(1)),null,null));
		}
		if(DateParser.isDate(s)) {
			return(forDate(s));
		}
		if(Character.isDigit(s.charAt(0)) || s.charAt(0)=='-'|| s.charAt(0)=='+' || s.charAt(0)=='.') {
			return(forNumber(s));
		}
		if(s.startsWith("http://")) {
			return(forUri(s));
		}
		if(s.startsWith("<")) {
			return(s);
		}
		if(s.indexOf(':')!=-1) {
			return(forQname(s.substring(0,s.indexOf(':')+1), s.substring(s.indexOf(':')+1)));
		}		
		return(forUri(s));
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
	
	/** Turtle valid URI characters */
	public static Char.Legal turtleUri= new Char.Legal() {
		public boolean isLegal(char c) {
			if(c=='>') return(false);
			if(c=='\\') return(false);
			if(c<0x20) return(false);
			return(true);
		}
	};

	/** removes quotes*/
	public static String stripQuotes(String result) {
		if(result.startsWith("\"")) result=result.substring(1);
		if(result.endsWith("\"")) result=Char.cutLast(result);
		return(result);
	}
}
