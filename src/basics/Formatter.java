package basics;

import java.util.Map;
import java.util.Map.Entry;

import javatools.datatypes.FinalMap;
import javatools.parsers.Char;
import javatools.parsers.DateParser;
import javatools.parsers.NumberParser;

/**
 * Class Formatter - YAGO2S
 * 
 * Conventions:
 * (1) YAGO entities are always given as relative URIs "<Albert_Einstein>".
 * This is because qnames may not contain certain characters
 * (2) All entities from standard namespaces are given as qnames
 * This is to save space and keep readability.
 * (3) All other entities are given as full URIs "<http://...>"
 * @author Fabian M. Suchanek
 */
public class Formatter {
	
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

	public static String makeId() {
		return(makeQname("y:","id"+(ids++)));
	}
	public static String makeUri(String s) {
		if(s.startsWith(YAGONAMESPACE)) {
			return('<'+s.substring(YAGONAMESPACE.length())+'>');
		}
		if(s.startsWith("http://")) {
			for(Entry<String,String> entry :standardPrefixes.entrySet()) {
				if(s.startsWith(entry.getValue())) {
					s=entry.getKey()+s.substring(entry.getValue().length());
					break;
				}
			}
		}
		return ('<'+s+'>');
	}
	public static String makeNumber(String s) {
		return (makeString(s,null,makeQname("xsd:","decimal")));
	}

	public static String makeQname(String prefixWithColon,String name) {
		if(prefixWithColon.equals("y:")) return(makeUri(name));
		//if(standardPrefixes.containsKey(prefixWithColon)) return(makeUri(standardPrefixes.get(prefixWithColon)+name));
		return(prefixWithColon+name);
	}
	public static String makeString(String string,String language,String datatype) {
		if(datatype!=null) return ('"'+string+"\"^^"+datatype);
		return ('"'+string+'"');
	}

	
	/** Normalizes an entity */
	public static String entity(String s) {
		return (Char.encodeBackslashToAlphanumeric(s.trim().replace(' ', '_')));
	}

	/** Returns the readable string for an entity */
	public static String unEntity(String s) {
		return (Char.decodeBackslash(s).replace('_', ' '));
	}

	/** Normalizes a string */
	public static String string(String s) {
		return ('"' + Char.encodeBackslashToASCII(s.trim()).replace("\"", "\\u0022") + '"');
	}

	/** Normalizes a fact id */
	public static String factId(long myFactId) {
		return ("#" + myFactId);
	}
  
  /** Un-Normalizes a fact id  
   * @return numeric value of the fact id if the given string is a normalized fact id, null otherwise
   */public static Long unFactId(String normalizedFactId){
     if(normalizedFactId.matches("#\\d++"))
       return new Long(normalizedFactId.substring(1));
     else return null;
   }

	/** Normalizes a wordnet entity */
	public static String wordNetEntity(String word, String id) {
		return ("wordnet_" + entity(word) + "_" + id);
	}

	/** Normalizes a wordnet entity */
	public static String wikiCategory(String wikiCat) {
		return ("wikicategory_" + entity(wikiCat));
	}

	/** Returns the name of the wordnet Entity (or null) */
	public static String unWordNetEntity(String wn) {
		if (!wn.startsWith("wordnet_"))
			return (null);
		wn = wn.substring("wordnet_".length());
		wn = wn.substring(0, wn.lastIndexOf('_'));
		return (unEntity(wn));
	}

	/** Returns the name of the wiki category (or null) */
	public static String unWikiCategory(String wn) {
		if (!wn.startsWith("wikicategory_"))
			return (null);
		wn = wn.substring("wikicategory_".length());
		return (unEntity(wn));
	}

	/** Returns the pure number with unit (or null) */
	public static String unNumber(String s) {
		String[] nu = NumberParser.getNumberAndUnit(s, new int[2]);
		if (nu == null)
			return (null);
		if (nu[1] == null)
			return (nu[0]);
		return (nu[0] + ' ' + nu[1]);
	}

	
	/** Normalizes a geonames entitiy */
	public static String geonamesEntity(String word, String id) {
		return ("geoent_" + entity(word) + "_" + id);
	}

	/** Returns the name of a geonames entity (or null) */
	public static String unGeonamesEntity(String entity) {
		if (!entity.startsWith("geoent_"))
			return (null);
		entity = entity.substring("geoent_".length());
		entity = entity.substring(0, entity.lastIndexOf('_'));
		return (unEntity(entity));
	}

	/** Normalizes a geonames class */
	public static String geonamesClass(String name) {
		return ("geoclass_" + entity(name));
	}

	/** Returns the name of a geonames class (or null) */
	public static String unGeonamesClass(String entity) {
		if (!entity.startsWith("geoclass_"))
			return (null);
		entity = entity.substring("geoclass_".length());
		return (unEntity(entity));
	}

	/** Returns the wikipedia URL for an entity */
	public static String urlForEntity(String e) {
		if (e.startsWith("wiki_") || DateParser.isDate(e) || e.startsWith("wordnet_"))
			return (null);
		if (e.startsWith("wikicategory_")) {
			return ("http://en.wikipedia.org/wiki/Category:" + Char.encodeURIPathComponent(e.substring(13)));
		}
		return ("http://en.wikipedia.org/wiki/" + Char.encodeURIPathComponent(e));
	}
}
