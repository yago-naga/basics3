package basics;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import basics.Fact.ImplementationNote;
import javatools.administrative.D;
import javatools.datatypes.FinalMap;
import javatools.parsers.Char17;
import javatools.parsers.DateParser;

/**
Copyright 2016 Fabian M. Suchanek

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This class formats a component for a YAGO fact.
 */
public class FactComponent {

  /** YAGO namespace */
  public static final String YAGONAMESPACE = "http://yago-knowledge.org/resource/";

  /** Pattern for Wikipedia URLs*/
  public static Pattern wikipediaUrlPattern = Pattern.compile("https?://([a-z]{1,3}).wikipedia.org/wiki/(.*)");

  /** Standard namespace prefixes that this N4Reader will assume */
  public static final Map<String, String> standardPrefixes = new FinalMap<String, String>("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      "rdfs:", "http://www.w3.org/2000/01/rdf-schema#", "xsd:", "http://www.w3.org/2001/XMLSchema#", "owl:", "http://www.w3.org/2002/07/owl#",
      "skos:", "http://www.w3.org/2004/02/skos/core#", "dbp:", "http://dbpedia.org/ontology/");

  /** Creates a fact component for a URI */
  public static String forUri(String s) {
    if (s.startsWith("<") && s.endsWith(">")) return (s);
    if (s.startsWith(YAGONAMESPACE)) {
      return ('<' + Char17.encodeBackslash(s.substring(YAGONAMESPACE.length()), turtleUri) + '>');
    }
    if (s.startsWith("http://")) {
      for (Entry<String, String> entry : standardPrefixes.entrySet()) {
        if (s.startsWith(entry.getValue())) {
          return (forQname(entry.getKey(), s.substring(entry.getValue().length())));
        }
      }
    }
    return ('<' + Char17.encodeBackslash(s, turtleUri) + '>');
  }

  /** Creates a fact component for a URI, does not encode it */
  public static String forEncodedUri(String s) {
    if (s.startsWith("<") && s.endsWith(">")) return (s);
    if (s.startsWith(YAGONAMESPACE)) {
      return ('<' + s.substring(YAGONAMESPACE.length()) + '>');
    }
    if (s.startsWith("http://")) {
      for (Entry<String, String> entry : standardPrefixes.entrySet()) {
        if (s.startsWith(entry.getValue())) {
          return (entry.getKey() + s.substring(entry.getValue().length()));
        }
      }
    }
    return ('<' + s + '>');
  }

  /** Creates a fact component for number */
  public static String forNumber(int i) {
    return (forStringWithDatatype(i + "", forQname("xsd:", "integer")));
  }

  /** Creates a fact component for number */
  public static String forNumber(float i) {
    return (forStringWithDatatype(i + "", forQname("xsd:", "decimal")));
  }

  /** Creates a fact component for number. We don't do any syntax checks here. */
  public static String forNumber(String s) {
    if (javaStringIsDate(s)) return (forStringWithDatatype(s, forQname("xsd:", "date")));
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
    return (forStringWithDatatype(year + "-##-##", "xsd:date"));
  }

  /** Creates a fact component for a YAGO entity */
  public static String forYagoEntity(String name) {
    return (forUri(name.replace(' ', '_')));
  }

  /** Reverses the transformation from Wikipedia title to YAGO entity */
  public static String unYagoEntity(String entity) {
    return Char17.decodeBackslash(stripBracketsAndLanguage(entity).replace('_', ' '));
  }

  /** Creates a fact component for a YAGO entity from another language */
  public static String forForeignYagoEntity(String name, String lan) {
    if (isEnglish(lan)) return (forYagoEntity(name));
    return (forYagoEntity(lan + "/" + name));
  }

  /** Pattern for language*/
  public static final Pattern LAN = Pattern.compile("<([a-z]{2,3})/");

  /** Returns the language of a foreign entity (or null) */
  public static String getLanguageOfEntity(String name) {
    Matcher m = LAN.matcher(name);
    if (!m.lookingAt()) return (null);
    return (m.group(1));
  }

  /** Returns the pure entity name */
  public static String stripBracketsAndLanguage(String entity) {
    entity = stripBrackets(entity);
    int slash = entity.indexOf('/');
    // matches "[a-z]{2,3}/.*"
    if (slash == 2 || slash == 3) {
      for (int i = 0; i < slash; i++) {
        char c = entity.charAt(i);
        if (c < 'a' || c > 'z') {
          return entity;
        }
      }
      return entity.substring(slash + 1);
    }
    return (entity);
  }

  /** Returns the pure string of a Wikipedia category */
  public static String stripCat(String cat) {
    cat = stripBracketsAndLanguage(cat);
    if (cat.startsWith("wikicat_")) cat = cat.substring("wikicat_".length());
    cat = cat.replace('_', ' ');
    cat = Char17.decodeBackslash(cat);
    return (cat);
  }

  /** TRUE for wiki categories */
  public static boolean isCat(String cat) {
    return (cat.startsWith("<wikicat_"));
  }

  /** Creates a fact component for a Wikipedia title */
  public static String forWikipediaTitle(String name) {
    name = Char17.decodeAmpersand(name).trim();
    return (forYagoEntity(name));
  }

  /** Creates a fact component for a Wikipedia title */
  public static String forForeignWikipediaTitle(String name, String lan) {
    name = Char17.decodeAmpersand(name).trim();
    return (forForeignYagoEntity(name, lan));
  }

  /** Creates a fact component (containing the YAGO entity) for a
   Wikipedia URL */
  public static String forWikipediaURL(String url) {
    Matcher m = wikipediaUrlPattern.matcher(url);
    if (!m.matches()) return (null);
    String language = m.group(1);
    String title = Char17.decodePercentage(m.group(2));
    if ("en".equals(language)) return forUri(title);
    return forUri(language + "/" + title);
  }

  /** Creates a fact component for a String with language. We check the syntax */
  public static String forStringWithLanguage(String string, String language) {
    if (language != null && language.length() > 1) return ('"' + Char17.encodeBackslash(string, turtleString) + "\"@" + language);
    return ('"' + Char17.encodeBackslash(string, turtleString) + "\"");
  }

  /**
   * Creates a fact component for a String with language. The language code
   * can be given as 2 letter or 3 letter codes, and will be translated to 3
   * letter codes by help of the provided mapping. To get this mapping, use
   *
   * Map&lt;String, String&gt; languagemap =
   * PatternHardExtractor.LANGUAGECODEMAPPING
   * .factCollection().getStringMap("&lt;hasThreeLetterLanguageCode&gt;");
   *
   * Returns NULL in case of failure.
   */
  public static String forStringWithLanguage(String string, String language, Map<String, String> twoLetterCodes2threeLetterCodes) {
    if (language.length() == 2) language = twoLetterCodes2threeLetterCodes.get(language);
    if (language == null || language.length() != 3) return (null);
    return (forStringWithLanguage(string, language));
  }

  /** Creates a fact component for a String with datatype. We check the syntax */
  public static String forStringWithDatatype(String string, String datatype) {
    if (datatype != null && !datatype.equals(YAGO.string)) return ('"' + Char17.encodeBackslash(string, turtleString) + "\"^^" + datatype);
    return (forStringWithLanguage(string, null));
  }

  /** Creates a fact component for a String. We check the syntax */
  public static String forString(String string) {
    return (forStringWithLanguage(string, null));
  }

  /** Creates a fact component for a wordnet entity */
  public static String forWordnetEntity(String word, String id) {
    return (forYagoEntity("wordnet_" + word + "_" + id));
  }

  /** Creates a fact component for a Wikipedia category */
  public static String forWikiCategory(String word) {
    return (forForeignWikiCategory(word, "eng"));
  }

  /** Creates a fact component for a Wikipedia category */
  public static String forForeignWikiCategory(String word, String lan) {
    // We may have encodings in the string
    word = Char17.decodeAmpersand(word);
    // Capitalize the first letter for consistency
    // We still have inconsistencies then with "Living_people" and "Living_People"
    word = word.substring(0, 1).toUpperCase() + word.substring(1);
    // Remove subsequent spaces and weird spaces
    word = word.replaceAll("[_\\p{Zs}]+", "_");
    // Remove backslashes that appear from time to time
    word = word.replace("\\", "");
    if (isEnglish(lan)) return (forYagoEntity("wikicat_" + word));
    return (forYagoEntity(lan + "/wikicat_" + word));
  }

  /** Creates a fact component for a GeoNames entity */
  public static String forGeoNamesId(String geonamesId) {
    return (forYagoEntity(GeoNames.GEONAMES_NAMESPACE + geonamesId));
  }

  public static String geoNamesIdFromEntity(String genonamesYagoId) {
    return stripBrackets(genonamesYagoId.replace(GeoNames.GEONAMES_NAMESPACE, ""));
  }

  /** Creates a fact component for a GeoNames class */
  public static String forGeoNamesClass(String word) {
    return (forYagoEntity("geoclass_" + word));
  }

  /** Creates a fact component for a degree */
  public static String forDegree(double deg) {
    return (forStringWithDatatype(deg + "", "<degrees>"));
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
    if (javaStringIsDate(s)) {
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
  public static Char17.Legal turtleString = new Char17.Legal() {

    @Override
    public boolean isLegal(char c) {
      if (c == '"') return (false);
      if (c == '\\') return (false);
      if (c < 0x20) return (false);
      return (true);
    }
  };

  /**
   * Turtle valid URI characters. As per
   * http://www.w3.org/TR/turtle/#grammar-production-IRIREF
   */
  public static Char17.Legal turtleUri = new Char17.Legal() {

    @Override
    public boolean isLegal(char c) {
      if (Char17.in(c, "<>\"{}|^`\\%")) return (false);
      if (c <= 0x20) return (false);
      return (true);
    }
  };

  /** returns the string part of a literal (with quotes) */
  public static String getString(String stringLiteral) {
    String[] split = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    return (split[0]);
  }

  /** returns the datatype part of a literal */
  public static String getDatatype(String stringLiteral) {
    String[] split = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    return (split[1]);
  }

  /** returns the language part of a literal */
  public static String getLanguageOfString(String stringLiteral) {
    String[] split = literalAndDatatypeAndLanguage(stringLiteral);
    if (split == null) return (null);
    return (split[2]);
  }

  /** removes quotes before and after a string */
  public static String stripQuotes(String string) {
    if (string == null) return (null);
    if (string.startsWith("\"")) string = string.substring(1);
    if (string.endsWith("\"")) string = Char17.cutLast(string);
    return (string);
  }

  /** removes quotes before and after a string and the language*/
  public static String stripQuotesAndLanguage(String string) {
    if (string == null) return (null);
    if (string.startsWith("\"")) string = string.substring(1);
    int lanPos = string.lastIndexOf("\"@");
    if (lanPos != -1 && lanPos > string.length() - 6) {
      string = string.substring(0, lanPos);
    } else if (string.endsWith("\"")) {
      string = Char17.cutLast(string);
    }
    return (string);
  }

  /** removes the qualifier appended to the entity name
   * (e.g. Gerd_Müller_(politician) ==&gt; Gerd_Müller */
  public static String stripQualifier(String string) {
    if (!string.endsWith(")")) return string;
    int openingBracketIndex = string.lastIndexOf('(');
    if (openingBracketIndex < 1) return string;
    return string.substring(0, openingBracketIndex - 1);
  }

  /** removes brackets */
  public static String stripBrackets(String result) {
    if (result == null) return (null);
    if (result.startsWith("<")) result = result.substring(1);
    if (result.endsWith(">")) result = Char17.cutLast(result);
    return (result);
  }

  /** Strips the http://.../ from a URI. Warning: strips away until the last '/', which may belong to the entity title in Wikipedfia URLS! Use stripWikipediaPrefix*/
  public static String stripPrefix(String entityWithPrefix) {
    int pos = Math.max(entityWithPrefix.lastIndexOf('#'), entityWithPrefix.lastIndexOf('/'));
    return (stripBrackets(entityWithPrefix.substring(pos + 1)));
  }

  /** Strips http://en.wikipedia.org/wiki/ from a URI, or returns NULL*/
  public static String stripWikipediaPrefix(String entityWithPrefix) {
    entityWithPrefix = stripBrackets(entityWithPrefix);
    int pos = entityWithPrefix.indexOf("wikipedia.org/wiki/");
    if (pos == -1) return (null);
    return (entityWithPrefix.substring(pos + 19));
  }

  /** Returns a Java string for a YAGO string */
  public static String asJavaString(String stringLiteral) {
    return (Char17.decodeBackslash(FactComponent.stripQuotes(getString(stringLiteral))));
  }

  /** Sets data type of a literal */
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
    return entity != null && (entity.startsWith("\""));
  }

  /** TRUE for urls */
  public static boolean isUri(String entity) {
    return entity != null && (entity.startsWith("<"));
  }

  /**
   * Splits a literal into literal (with quotes) and datatype, followed by the
   * language. Non-existent components are NULL
   */
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

  /** TRUE if the first thing is more specific than the second */
  public static boolean isMoreSpecific(String first, String second) {
    if (first.equals(second)) return (false);
    if (isLiteral(first)) {
      if (!isLiteral(second)) return (false);
      String secondString = asJavaString(second);
      String firstString = asJavaString(first);
      if (javaStringIsDate(firstString) && javaStringIsDate(secondString) && DateParser.includes(secondString, firstString)) return (true);
      if (D.equal(getDatatype(first), getDatatype(second)) && javaStringIsFloat(firstString) && javaStringIsFloat(secondString)
          && first.indexOf('.') != -1 && first.startsWith(Char17.cutLast(secondString)) && first.length() > second.length())
        return (true);
      return (false);
    }
    return (false);
  }

  /** Pattern for a date */
  public static final Pattern DATEPATTERN = Pattern.compile("-?[#\\d]++-[#\\d]++-[#\\d]++");

  /** Tells whether this string is a date */
  public static boolean javaStringIsDate(CharSequence s) {
    return (DATEPATTERN.matcher(s).matches());
  }

  /** Pattern for a float */
  public static final Pattern FLOATPATTERN = Pattern.compile("([\\-\\+]?\\d++(?:\\.[0-9]++)?(?:[Ee]\\-?[0-9]++)?)");

  /** Tells whether this Java string is a float */
  public static boolean javaStringIsFloat(CharSequence s) {
    return (FLOATPATTERN.matcher(s).matches());
  }

  /** Characters which are used to represent a hash value as String */
  @ImplementationNote("Current implementation of hash needs a HASHBASE with 2^k characters")
  final static char[] HASHBASE = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!?".toCharArray();

  /** Returns a hash for a Java String */
  public static String hash(String string) {
    char[] str = new char[string.length()];
    string.getChars(0, str.length, str, 0);
    final int len = 10; // max useful len for a hash of type long: 65

    // FNV-1a hash, see https://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function
    long hash = 0xcbf29ce484222325L;
    for (int i = 0; i < str.length; i++) {
      hash ^= (str[i] ^ 0xff);
      hash *= 1099511628211L;
    }

    // convert as number in base 64, using characters of array base
    char[] out = new char[len];
    // optimize modulo for (1<<s)-1, see https://graphics.stanford.edu/~seander/bithacks.html#ModulusDivisionEasy
    long radix = 63;
    int pos = len;
    if (hash < 0) {
      hash = -hash;
    }
    while (hash > 0 && pos > 0) {
      out[--pos] = HASHBASE[(int) (hash & radix)];
      hash = hash >> 6;
    }

    return new String(out, pos, len - pos);
  }

  /** Returns a hash for an entity */
  public static String hashEntity(String entity) {
    return (hash(stripBrackets(entity)));
  }

  /** Returns a hash for a literal */
  public static String hashLiteral(String literal) {
    return (hash(asJavaString(literal)));
  }

  /** Returns a hash for a literal */
  public static String hashLiteralOrEntity(String s) {
    return (isLiteral(s) ? hashLiteral(s) : hashEntity(s));
  }

  /** Returns a hash for a literal */
  public static String hashRelation(String relation) {
    String hash = hashEntity(relation);
    return (hash.substring(0, Math.min(3, hash.length())));
  }

  /** TRUE if the argument is a fact id */
  public static boolean isFactId(String arg) {
    return arg.startsWith("<id_");
  }

  /** TRUE if the argument is an entity id starting with _ (empty node used for reification) */
  public static boolean isEmptyNode(String arg) {
    return arg.startsWith("<_");
  }

  /** Makes a Wikipedia URL for an entity coming from the English Wikipedia */
  public static String wikipediaURL(String entityName) {
    return wikipediaURL(entityName, "en");
  }

  /** Makes a Wikipedia URL for an entity coming from the LANGUAGE Wikipedia */
  public static String wikipediaURL(String entityName, String wikipediaLanguageCode) {
    entityName = unYagoEntity(entityName).replace(' ', '_');
    String url = null;
    try {
      url = "<http://" + wikipediaLanguageCode + ".wikipedia.org/wiki/" + URLEncoder.encode(entityName, "UTF-8").replace("+", "%20") + ">";
    } catch (UnsupportedEncodingException e) {
      // Should never happen, we are dealing with UTF-8.
      e.printStackTrace();
    }
    return url;
  }

  /** Makes a Wikipedia base URL such as http://de.wikipedia.org */
  public static String wikipediaBaseURL(String wikipediaLanguageCode) {
    return "<http://" + wikipediaLanguageCode + ".wikipedia.org>";
  }

  /**
   * Returns a full Wikipedia URL for English or otherwise just the base
   * Wikipedia URL
   */
  public static String wikipediaSourceURL(String entity, String wikipediaLanguageCode) {
    if (isEnglish(wikipediaLanguageCode)) return (wikipediaURL(entity));
    else return (wikipediaBaseURL(wikipediaLanguageCode));
  }

  /** Parses out the Wordnet name */
  public static String wordnetWord(String wordnetEntity) {
    if (!wordnetEntity.startsWith("<wordnet_") || wordnetEntity.length() < 8 + 9) return (null);
    wordnetEntity = wordnetEntity.substring("<wordnet_".length(), wordnetEntity.length() - 11);
    return (wordnetEntity);
  }

  /** DBpedia class prefix */
  public static final String dbpediaPrefix = "http://dbpedia.org/class/yago/";

  /** Returns the DBpedia class name for a YAGO class name */
  public static String dbpediaClassForYagoClass(String arg) {
    arg = FactComponent.stripBrackets(arg);
    if (arg.startsWith("wordnet_")) arg = arg.substring("wordnet_".length());
    if (arg.startsWith("wikicategory_")) arg = arg.substring("wikicategory_".length());
    StringBuilder result = new StringBuilder(dbpediaPrefix);
    boolean upcase = true;
    for (int i = 0; i < arg.length(); i++) {
      if (arg.charAt(i) == '_') {
        upcase = true;
        continue;
      }
      result.append(upcase ? Character.toUpperCase(arg.charAt(i)) : arg.charAt(i));
      upcase = false;
    }
    return FactComponent.forUri(result.toString());
  }

  public static String forInfoboxAttribute(String language, String attribute) {
    // return relation;
    return "<infobox/" + language + "/" + attribute.toLowerCase() + ">";
  }

  public static String forInfoboxTypeRelation(String language) {
    return (forInfoboxAttribute(language, "type"));
  }

  public static String forInfoboxTemplate(String cls, String lan) {
    cls = cls.toLowerCase().replace('_', ' ').trim();
    if (Character.isDigit(Char17.last(cls))) cls = Char17.cutLast(cls);
    return forStringWithLanguage(cls, lan);
  }

  /** TRUE if the entity is a wordnet class or a wikipedia category */
  public static boolean isClass(String entity) {
    return entity.startsWith("<wordnet_") || entity.startsWith("<wikicat_");
  }

  /** TRUE if the language is english */
  public static boolean isEnglish(String lan) {
    return (lan.equals("en") || lan.equals("eng"));
  }

  /** Testing */
  public static void main(String[] args) throws Exception {
    D.p(forWikiCategory("Al-Balqa`_ Applied_University_alumni"));
  }
}
