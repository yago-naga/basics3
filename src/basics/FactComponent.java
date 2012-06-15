package basics;

import java.util.Map;
import java.util.Map.Entry;

import javatools.administrative.D;
import javatools.datatypes.FinalMap;
import javatools.parsers.Char;
import javatools.parsers.DateParser;
import javatools.parsers.NumberParser;

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
  public static final Map<String, String> standardPrefixes = new FinalMap<String, String>("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      "rdfs:", "http://www.w3.org/2000/01/rdf-schema#", "xsd:", "http://www.w3.org/2001/XMLSchema#", "owl:", "http://www.w3.org/2002/07/owl#",
      "skos:", "http://www.w3.org/2004/02/skos/core#", "dbp:", "http://dbpedia.org/ontology/");

  /** Creates a fact component for a URI */
  public static String forUri(String s) {
    if (s.startsWith("<")) return (s);
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
    return (forStringWithDatatype(i + "", forQname("xsd:", "decimal")));
  }

  /** Creates a fact component for number*/
  public static String forNumber(float i) {
    return (forStringWithDatatype(i + "", forQname("xsd:", "decimal")));
  }

  /** Creates a fact component for number. We don't do any syntax checks here. */
  public static String forNumber(String s) {
    if (DateParser.isDate(s)) return (forStringWithDatatype(s, forQname("xsd:", "date")));
    if (s.indexOf('.') == -1 && s.indexOf("e") == -1 && s.indexOf("E") == -1) return (forStringWithDatatype(s, forQname("xsd:", "integer")));
    return (forStringWithDatatype(s, forQname("xsd:", "decimal")));
  }

  /**
   * Creates a fact component for a Qname. We don't do any syntax checks here.
   */
  public static String forQname(String prefixWithColon, String name) {
    if (prefixWithColon.equals("y:")) return (forUri(name));
    return (prefixWithColon + name);
  }

  /** Creates a fact component for a date. No checks done. */
  public static String forDate(String date) {
    return (forStringWithDatatype(date, "xsd:date"));
  }

  /** Creates a fact component for a year. No checks done. */
  public static String forYear(String year) {
    return (forStringWithDatatype(year + "-00-00", "xsd:date"));
  }

  /** Creates a fact component for a YAGO entity */
  public static String forYagoEntity(String name) {
    return (forUri(name));
  }

  /** Creates a fact component for a Wikipedia title */
  public static String forWikipediaTitle(String name) {
    name = name.trim().replace(' ', '_');
    return (forUri(name));
  }

  /** Creates a fact component for a String with language. We check the syntax */
  public static String forStringWithLanguage(String string, String language) {
    if (language != null) return ('"' + Char.encodeBackslash(string, turtleString) + "\"@" + language);
    return ('"' + Char.encodeBackslash(string, turtleString) + "\"");
  }

  /** Creates a fact component for a String with datatype. We check the syntax */
  public static String forStringWithDatatype(String string, String datatype) {
    if (datatype != null && !datatype.equals(YAGO.string)) return ('"' + Char.encodeBackslash(string, turtleString) + "\"^^" + datatype);
    return (forStringWithLanguage(string, null));
  }

  /** Creates a fact component for a String. We check the syntax */
  public static String forString(String string) {
    return (forStringWithLanguage(string, null));
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
    return (FactComponent.forYagoEntity("yagoTheme_" + t.name));
  }

  /** Translates anything into a FactComponent */
  public static String forAny(String s) {
    if (s == null || s.length() == 0) return (null);
    if (s.startsWith("\"")) {
      String split[] = literalAndDatatypeAndLanguage(s);
      if (split != null && (split[1] != null || split[2] != null)) return (s);
      return (forString(stripQuotes(s.substring(1))));
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
      if (c == '"') return (false);
      if (c == '\\') return (false);
      if (c < 0x20) return (false);
      return (true);
    }
  };

  /** Turtle valid URI characters */
  public static Char.Legal turtleUri = new Char.Legal() {

    public boolean isLegal(char c) {
      if (c == '>') return (false);
      if (c == '\\') return (false);
      if (c < 0x20) return (false);
      return (true);
    }
  };

  /** returns the string part of a literal (with quotes)*/
  public static String getString(String stringLiteral) {
    String[] split = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    return (split[0]);
  }

  /** returns the datatype part of a literal*/
  public static String getDatatype(String stringLiteral) {
    String[] split = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    return (split[1]);
  }

  /** returns the language part of a literal*/
  public static String getLanguage(String stringLiteral) {
    String[] split = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    return (split[2]);
  }

  /** removes quotes before and after a string*/
  public static String stripQuotes(String string) {
    if (string == null) return (null);
    if (string.startsWith("\"")) string = string.substring(1);
    if (string.endsWith("\"")) string = Char.cutLast(string);
    return (string);
  }

  /** removes brackets */
  public static String stripBrackets(String result) {
    if (result == null) return (null);
    if (result.startsWith("<")) result = result.substring(1);
    if (result.endsWith(">")) result = Char.cutLast(result);
    return (result);
  }

  /** Returns a Java string for a YAGO string */
  public static String asJavaString(String stringLiteral) {
    return (Char.decodeBackslash(FactComponent.stripQuotes(getString(stringLiteral))));
  }

  /** Sets data type of a literal*/
  public static String setDataType(String stringLiteral, String datatype) {
    String split[] = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    if (D.equal(split[1], datatype)) return (stringLiteral);
    return (FactComponent.forStringWithDatatype(stripQuotes(split[0]), datatype));
  }

  /** Sets language of a literal */
  public static String setLanguage(String stringLiteral, String language) {
    String split[] = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    if (D.equal(split[2], language)) return (stringLiteral);
    return (FactComponent.forStringWithLanguage(stripQuotes(split[0]), language));
  }

  /** TRUE for literals */
  public static boolean isLiteral(String entity) {
    return (entity.startsWith("\""));
  }

  /** TRUE for urls */
  public static boolean isUri(String entity) {
    return (entity.startsWith("<"));
  }

  /** Splits a literal into literal (with quotes) and datatype, followed by the language. Non-existent components are NULL*/
  public static String[] literalAndDatatypeAndLanguage(String s) {
    if (s == null || !s.startsWith("\"")) return (null);

    // Get the language tag
    int at = s.lastIndexOf('@');
    if (at > 0 && s.indexOf('\"', at) == -1) {
      String language = s.substring(at + 1);
      String string = s.substring(0, at);
      return (new String[] { string, null, language });
    }

    // Get the data type
    int dta = s.lastIndexOf("\"^^");
    if (dta > 0 && s.indexOf('\"', dta + 1) == -1) {
      String datatype = s.substring(dta + 3);
      String string = s.substring(0, dta + 1);
      return (new String[] { string, datatype, null });
    }

    // Otherwise, return just the string
    return (new String[] { s, null, null });
  }

  /** TRUE if the first thing is more specific than the second*/
  public static boolean isMoreSpecific(String first, String second) {
    if (first.equals(second)) return (false);
    if (isLiteral(first)) {
      if (!isLiteral(second)) return (false);
      String secondString = asJavaString(second);
      String firstString = asJavaString(first);
      if (DateParser.isDate(firstString) && DateParser.isDate(secondString) && DateParser.includes(secondString, firstString)) return (true);
      if (D.equal(getDatatype(first), getDatatype(second)) && NumberParser.isFloat(firstString) && NumberParser.isFloat(secondString)
          && first.indexOf('.') != -1 && first.startsWith(Char.cutLast(secondString)) && first.length() > second.length()) return (true);
      return (false);
    }
    return (false);
  }

  /** Returns a hash for a Java String*/
  public static String hash(String string) {
    int hash = string.hashCode();
    return (Long.toString((long) hash - (long) Integer.MIN_VALUE, Character.MAX_RADIX));
  }

  /** Returns a hash for an entity*/
  public static String hashEntity(String entity) {
    return (hash(stripBrackets(entity)));
  }

  /** Returns a hash for a literal*/
  public static String hashLiteral(String literal) {
    return (hash(asJavaString(literal)));
  }

  /** Returns a hash for a literal*/
  public static String hashLiteralOrEntity(String s) {
    return (isLiteral(s) ? hashLiteral(s) : hashEntity(s));
  }

  /** Returns a hash for a literal*/
  public static String hashRelation(String relation) {
    String hash = hashEntity(relation);
    return (hash.substring(0, Math.min(3, hash.length())));
  }

  /** TRUE if the argument is a fact id*/
  public static boolean isFactId(String arg) {
    return arg.startsWith("<id_");
  }

  /** Makes a Wikipedia URL for an entity*/
  public static String wikipediaURL(String entity) {
    entity=stripBrackets(entity);
    return("<http://en.wikipedia.org/wiki/"+entity+">");
  }
}
