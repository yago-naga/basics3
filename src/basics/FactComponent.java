package basics;

import java.util.Map;
import java.util.Map.Entry;

import javatools.datatypes.FinalMap;
import javatools.parsers.Char;
import javatools.parsers.DateParser;

/**
 * Class FactComponent - YAGO2S
 * 
 * Formats an RDF item to be used with Fact.java See yago2s/policies/dataFormat
 * for details
 * 
 * @author Fabian M. Suchanek
 */
public class FactComponent {

	/** YAGO namespace */
	public static final String YAGONAMESPACE = "http://yago-knowledge/resource/";

	/** Standard namespace prefixes that this N4Reader will assume */
	public static final Map<String, String> standardPrefixes = new FinalMap<String, String>("rdf:",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdfs:", "http://www.w3.org/2000/01/rdf-schema#", "xsd:",
			"http://www.w3.org/2001/XMLSchema#", "owl:", "http://www.w3.org/2002/07/owl#", "skos:",
			"http://www.w3.org/2004/02/skos/core#", "dbp:", "http://dbpedia.org/ontology/");

	/** Creates a fact component for a URI */
	public static String forUri(String s) {
		if(s.startsWith("<")) return(s);
		if (s.startsWith(YAGONAMESPACE)) {
			return ('<' + s.substring(YAGONAMESPACE.length()) + '>');
		}
		if (s.startsWith("http://")) {
			for (Entry<String, String> entry : standardPrefixes.entrySet()) {
				if (s.startsWith(entry.getValue())) {
					return (forQname(entry.getKey(), s.substring(entry.getValue().length())));
				}
			}
		}
		return ('<' + Char.encodeBackslash(s, turtleUri) + '>');
	}


	/** Creates a fact component for number*/
	public static String forNumber(int i) {
		return(forString(i+"",null,forQname("xsd:", "decimal")));
	}
	
	 /** Creates a fact component for number*/
  public static String forNumber(float i) {
    return(forString(i+"",null,forQname("xsd:", "decimal")));
  }
	
	/** Creates a fact component for number. We don't do any syntax checks here. */
	public static String forNumber(String s) {
		if (DateParser.isDate(s))
			return (forString(s, null, forQname("xsd:", "date")));
		if (s.indexOf('.') == -1 && s.indexOf("e") == -1 && s.indexOf("E") == -1)
			return (forString(s, null, forQname("xsd:", "integer")));
		return (forString(s, null, forQname("xsd:", "decimal")));
	}

	/**
	 * Creates a fact component for a Qname. We don't do any syntax checks here.
	 */
	public static String forQname(String prefixWithColon, String name) {
		if (prefixWithColon.equals("y:"))
			return (forUri(name));
		return (prefixWithColon + name);
	}

	/** Creates a fact component for a date. No checks done. */
	public static String forDate(String date) {
		return (forString(date, null, "xsd:date"));
	}

	/** Creates a fact component for a year. No checks done. */
	public static String forYear(String year) {
		return (forString(year + "-00-00", null, "xsd:date"));
	}

	/** Creates a fact component for a YAGO entity */
	public static String forYagoEntity(String name) {
		return (forUri(name));
	}

	/** Creates a fact component for a Wikipedia title */
	public static String forWikipediaTitle(String name) {
		name=name.trim().replace(' ','_');
		return (forUri(name));
	}

	/** Creates a fact component for a String. We check the syntax */
	public static String forString(String string, String language, String datatype) {
	  if (language != null) 
	     return ('"' + Char.encodeBackslash(string, turtleString) + "\"@" + language);
		if (datatype != null)
			return ('"' + Char.encodeBackslash(string, turtleString) + "\"^^" + datatype);
		//return ('"' + Char.encodeBackslash(string, turtleString) + "\"@en");
		return ('"' + Char.encodeBackslash(string, turtleString) + "\"");
	}

	/** Creates a fact component for a String. We check the syntax */
	public static String forString(String string) {
		return (forString(string, null, null));
	}

	/** Creates a fact component for a wordnet entity */
	public static String forWordnetEntity(String word, String id) {
		return (forYagoEntity("wordnet_" + word.replace(' ', '_') + "_" + id));
	}

	/** Creates a fact component for a Wikipedia category */
	public static String forWikiCategory(String word) {
		return (forYagoEntity("wikicategory_" + word.replace(' ', '_')));
	}
	
	 /** Creates a fact component for a GeoNames class */
  public static String forGeoNamesClass(String word) {
    return (forYagoEntity("geoclass_" + word.replace(' ', '_')));
  }

  /** returns a YAGO entity name for this theme*/
  public static String forTheme(Theme t) {
    return(FactComponent.forYagoEntity("yagoTheme_"+t.name));
  }

	/** Translates anything into a FactComponent */
	public static String forAny(String s) {
		if (s == null || s.length() == 0)
			return (null);
		if (s.startsWith("\"")) {
			if(s.contains("\"^^")) return(s);
			return (forString(stripQuotes(s.substring(1)), null, null));
		}
		if (s.startsWith("http://")) {
			return (forUri(s));
		}
		if (s.startsWith("<")) {
			return (s);
		}
		if (DateParser.isDate(s)) {
			return (forDate(s));
		}
		if (Character.isDigit(s.charAt(0)) || s.charAt(0) == '-' || s.charAt(0) == '+' || s.charAt(0) == '.') {
			return (forNumber(s));
		}
		if (s.indexOf(':') != -1) {
			return (forQname(s.substring(0, s.indexOf(':') + 1), s.substring(s.indexOf(':') + 1)));
		}
		return (forUri(s));
	}

	/** Turtle valid string characters */
	public static Char.Legal turtleString = new Char.Legal() {
		public boolean isLegal(char c) {
			if (c == '"')
				return (false);
			if (c == '\\')
				return (false);
			if (c < 0x20)
				return (false);
			return (true);
		}
	};

	/** Turtle valid URI characters */
	public static Char.Legal turtleUri = new Char.Legal() {
		public boolean isLegal(char c) {
			if (c == '>')
				return (false);
			if (c == '\\')
				return (false);
			if (c < 0x20)
				return (false);
			return (true);
		}
	};

	/** removes quotes */
	public static String stripQuotes(String result) {
		if (result.startsWith("\"")) {
		  int at=result.lastIndexOf('@');
		  if(at>0 && result.indexOf('\"',at)==-1) result=result.substring(0,at);
			result = result.substring(1);
		}
		if (result.endsWith("\"")) {
			result = Char.cutLast(result);
		}
		return (result);
	}

	/** removes brackets */
	public static String stripBrackets(String result) {
		if (result.startsWith("<"))
			result = result.substring(1);
		if (result.endsWith(">"))
			result = Char.cutLast(result);
		return (result);
	}

	/** Returns a Java string for a YAGO string */
	public static String asJavaString(String string) {
		if(string==null) return(null);
		int pos = string.lastIndexOf("\"^^");
		if (pos != -1)
			string = string.substring(0, pos + 1);
		return (FactComponent.stripQuotes(Char.decodeBackslash(string)));
	}

	/** Sets data type */
	public static String setDataType(String string, String datatype) {
		if (!isLiteral(string))
			return (string);
		int pos = string.indexOf("\"^^");
		if (pos != -1)
			string = string.substring(0, pos + 1);
		return (FactComponent.forString(string, null, datatype));
	}

	/** Sets data type */
	public static String getDataType(String string) {
		if (!isLiteral(string))
			return (null);
		int pos = string.indexOf("\"^^");
		if (pos != -1) return(YAGO.string);
		return (string.substring(pos+3));
	}

	/** TRUE for literals */
	public static boolean isLiteral(String entity) {
		return (entity.startsWith("\""));
	}

	/** TRUE for urls */
	public static boolean isUri(String entity) {
		return (entity.startsWith("<"));
	}

	/** Splits a literal into literal (with quotes) and datatype*/
	public static String[] literalAndDataType(String arg2withDataType) {		
		return arg2withDataType.split("\\^\\^");
	}

	/** TRUE if the first thing is more specific than the second*/
	public static boolean isMoreSpecific(String first, String second) {
		if(first.equals(second)) return(false);
		if(isLiteral(first)) {
			if(!isLiteral(second)) return(false);
			second=asJavaString(second);
			first=asJavaString(first);
			if(DateParser.isDate(first) && DateParser.isDate(second) && DateParser.includes(second, first)) return(true);
			return(false);
		}
		return(false);
	}

	/** Returns a hash for a Java String*/
	public static String hash(String string) {
		int hash=string.hashCode();
		return(Long.toString((long)hash-(long)Integer.MIN_VALUE, Character.MAX_RADIX));
	}
	
	/** Returns a hash for an entity*/
	public static String hashEntity(String entity) {
		return(hash(stripBrackets(entity)));
	}
	
	/** Returns a hash for a literal*/
	public static String hashLiteral(String literal) {
		//return(hash(literal.replaceAll("[@\"\\^]", "")));
	  return(hash(asJavaString(literal)));
	}

	/** Returns a hash for a literal*/
	public static String hashLiteralOrEntity(String s) {
		return(isLiteral(s)?hashLiteral(s):hashEntity(s));
	}

	/** Returns a hash for a literal*/
	public static String hashRelation(String relation) {
		String hash=hashEntity(relation);
		return(hash.substring(0,Math.min(3,hash.length())));
	}

    /** TRUE if the argument is a fact id*/
	public static boolean isFactId(String arg) {
		return arg.startsWith("<id_");
	}

}
