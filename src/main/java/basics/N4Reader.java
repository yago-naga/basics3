package basics;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javatools.administrative.Announce;
import javatools.administrative.D;
import javatools.datatypes.IterableForIterator;
import javatools.filehandlers.FileLines;
import javatools.filehandlers.FileUtils;
import javatools.parsers.Char17;

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

This class provides a reader for facts from an N4 document. This follows the
Turtle Specification
http://www.w3.org/TeamSubmission/turtle/#sec-grammar-grammar It (1)
understands a first (optional) component in the preceding comment (2) and it
does not support all Turtle features

Passes all tests from
http://www.w3.org/TeamSubmission/turtle/#sec-conformance except 23
http://www.w3.org/2000/10/rdf-tests/rdfcore/ntriples/test.nt
 */
public class N4Reader implements Iterator<Fact>, Closeable {

  /** Reads the file */
  protected Reader reader;

  /** Information to be displayed in error messages */
  protected String info = "<unknown source>";

  /** Maps custom prefixes */
  protected Map<String, String> prefixes = new TreeMap<String, String>();

  /** Custom base */
  protected String base = null;

  /** Creates a N4 reader */
  public N4Reader(Reader r) throws IOException {
    reader = r;
    next();
  }

  /** Creates a N4 reader */
  public N4Reader(Reader r, String info) throws IOException {
    this(r);
    this.info = info;
  }

  /** Creates a N4 reader */
  public N4Reader(File f) throws IOException {
    this(FileUtils.getBufferedUTF8Reader(f));
  }

  /** Counter for blank nodes */
  protected int blankCounter = 0;

  /** Value for "Ignore this character, read new one */
  public static final int READNEW = -2;

  /** Current character */
  protected int c = READNEW;

  /** returns the next item */
  protected String nextItem() throws IOException {
    if (c == READNEW) c = FileLines.firstCharAfterSpace(reader);
    switch (c) {
      case '@':
        c = READNEW;
        return ('@' + FileLines.readToSpace(reader).toString());
      case '#':
        c = reader.read();
        // Special YAGO fact identifier
        if (c == '@') {
          c = READNEW;
          return ('&' + nextItem());
        } else {
          // Normal comment
          c = READNEW;
          FileLines.scrollTo(reader, '\n', '\r');
          return (nextItem());
        }
      case -1:
        return ("EOF");
      case '<':
        c = READNEW;
        String uri = FileLines.readTo(reader, '>').toString();
        if (base != null && !uri.startsWith("http://")) uri = base + uri;
        return (FactComponent.forEncodedUri(uri));
      case '"':
        String language = null;
        String datatype = null;
        String string = "";
        do {
          String next = FileLines.readTo(reader, '"').toString();
          string += (string.length() > 0 ? "\"" : "") + next;
          // check whether the last character of the last segment is escaped
          int i = 0;
          for (; i < next.length(); i++) {
            if (next.charAt(i) == '\\') i++;
          }
          if (i == next.length()) break;
        } while (true);
        c = reader.read();
        switch (c) {
          case '@':
            language = "";
            while (Character.isLetter(c = reader.read()) || c == '-')
              language += (char) c;
            break;
          case '^':
            reader.read();
            c = READNEW;
            datatype = nextItem();
            break;
          case '"':
            string = FileLines.readTo(reader, "\"\"\"").toString();
            if (string.length() > 2) string = string.substring(0, string.length() - 3);
            c = READNEW;
            break;
        }
        if (Character.isWhitespace(c)) c = READNEW;
        if (language == null) return (FactComponent.forStringWithDatatype(Char17.decodeBackslash(string), datatype));
        else return (FactComponent.forStringWithLanguage(Char17.decodeBackslash(string), language));
      case '[':
        String blank = FileLines.readTo(reader, ']').toString().trim();
        if (blank.length() != 0) {
          Announce.warning("Properties of blank node ignored", blank, " (", info, ")");
        }
        c = READNEW;
        return (FactComponent.forYagoEntity("blank" + (blankCounter++)));
      case '(':
        c = READNEW;
        String list = FileLines.readTo(reader, ')').toString().trim();
        Announce.warning("Cannot handle list", list, "(", info, ")");
        return (FactComponent.forQname("rdf:", "nil"));
      case '.':
        c = READNEW;
        return (".");
      case ',':
        c = READNEW;
        //Announce.warning("Commas are not supported");
        //FileLines.scrollTo(reader, '.');
        return (",");
      case ';':
        c = READNEW;
        // Announce.warning("Semicolons are not supported");
        // FileLines.scrollTo(reader, '.');
        return (";");
      case '+':
      case '-':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        String number = ((char) c) + FileLines.readToSpace(reader).toString();
        c = READNEW;
        return (FactComponent.forNumber(number));
      default:
        String name = ((char) c) + FileLines.readToSpace(reader).toString();

        // Save some stuff that follows...
        if (".,<".indexOf(Char17.last(name)) != -1 || name.endsWith(";")) {
          c = Char17.last(name);
          name = Char17.cutLast(name);
        } else {
          c = READNEW;
        }
        // Predefined Turtle entities
        if (name.equals("a")) return (FactComponent.forQname("rdf:", "type"));
        if (name.equals("true")) return (FactComponent.forStringWithDatatype("true", FactComponent.forQname("xsd:", "boolean")));
        if (name.equals("false")) return (FactComponent.forStringWithDatatype("false", FactComponent.forQname("xsd:", "boolean")));
        // Prefixes
        int colon = name.indexOf(':');
        if (colon == -1) {
          return FactComponent.forEncodedUri(name);
          /*Announce.warning("Invalid entity", Char17.encodeHex(name, Char17.alphaNumericAndSpace));
          FileLines.scrollTo(reader, '.');
          c = READNEW;
          return (".");*/
        }
        String prefix = name.substring(0, colon + 1);
        name = name.substring(colon + 1);
        if (prefixes.containsKey(prefix)) {
          return (FactComponent.forEncodedUri(prefixes.get(prefix) + name));
        }
        // Other
        return (FactComponent.forQname(prefix, name));
    }
  }

  /** caches the next fact, initially a dummy fact; null for EOF */
  protected Fact nextFact = new Fact("Elvis", "rdf:type", "theBest");

  @Override
  public Fact next() {
    Fact toReturn = nextFact;
    if (toReturn != null) {
      try {
        nextFact = internalNext();
      } catch (Exception e) {
        e.printStackTrace();
        nextFact = null;
      }
      if (nextFact == null) close();
    }

    return (toReturn);
  }

  @Override
  public boolean hasNext() {
    return nextFact != null;
  }

  /** Holds the previous subject, predicate and object (in this order) */
  protected String[] state = new String[3];

  /** Save the next item at this index of state */
  protected int stateIdx = 0;

  protected int delimToIndex(char c) {
    switch (c) {
      case '.':
        return 0;
      case ';':
        return 1;
      case ',':
        return 2;
    }
    return -1;
  }

  /** returns the next fact */
  protected Fact internalNext() throws Exception {
    while (true) {
      String item = nextItem();
      if (item.equals("EOF")) return (null);
      // Prefix
      if (item.equalsIgnoreCase("@PREFIX")) {
        String prefix = FileLines.readTo(reader, ':').toString().trim() + ':';
        FileLines.scrollTo(reader, '<');
        String dest = FileLines.readTo(reader, '>').toString().trim();
        if (base != null && !dest.startsWith("http://")) dest = base + dest;
        FileLines.scrollTo(reader, '.');
        if (FactComponent.standardPrefixes.containsKey(prefix)) {
          if (dest.equals(FactComponent.standardPrefixes.get(prefix))) continue;
          else Announce.warning("Redefining standard prefix", prefix, "from", FactComponent.standardPrefixes.get(prefix), "to", dest, "(", info, ")");
        }
        prefixes.put(prefix, dest);
        continue;
      }

      // Base
      if (item.equalsIgnoreCase("@BASE")) {
        FileLines.scrollTo(reader, '<');
        String uri = FileLines.readTo(reader, '>').toString().trim();
        if (uri.startsWith("http://")) base = uri;
        else base = base + uri;
        FileLines.scrollTo(reader, '.');
        continue;
      }

      // Unknown
      if (item.startsWith("@")) {
        Announce.warning("Unknown directive:", item, "(", info, ")");
        FileLines.scrollTo(reader, '.');
        continue;
      }

      // Fact identifier
      // TODO: where is this used?
      String factId = null;
      if (item.startsWith("&")) {
        factId = item.substring(1);
        item = nextItem();
      }

      // check for . ; ,
      int idx = -1;
      if (item.length() == 1 && (idx = delimToIndex(item.charAt(0))) >= 0) {
        stateIdx = idx;
        continue;
      }
      // sanity check
      if (stateIdx > 2) {
        Announce.warning("More than three items on line", factId, " state ", state[0], state[1], state[2], item, " state index ", stateIdx, "(", info,
            ")");
        FileLines.scrollTo(reader, '.');
        continue;
      }

      // save item
      state[stateIdx++] = item;

      // we now have all three parts
      if (stateIdx == 3) {
        char lastChar = Char17.last(state[2]);
        idx = delimToIndex(lastChar);
        if (idx >= 0) {
          state[2] = Char17.cutLast(state[2]);
          stateIdx = idx;
        }

        return new Fact(factId, state[0], state[1], state[2]);
      }

    }
  }

  @Override
  public void close() {
    try {
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Test
   *
   * @throws IOException
   */
  public static void main(String[] args) throws Exception {

    for (Fact f : IterableForIterator.get(new N4Reader(new File(args[0])))) {
      D.p(f);
    }
    System.exit(0);
    for (Fact f : FactSource.from(new File("./data/wikidata-test.ttl"))) {
      D.p(f);
    }

    for (File in : new File("C://Users//Administrator//ILData").listFiles()) {
      if (!in.getName().matches("test-\\d+\\.ttl.*")) continue;
      Announce.doing("Testing", in.getName());
      N4Writer w = new N4Writer(new File(in.toString().replace("ttl", "myout")), "Test run");
      for (Fact f : FactSource.from(in)) {
        w.write(f);
      }
      w.close();
      Announce.done();
    }
    // File in2 = new File("C://Users//Administrator//Downloads//test.ttl");
    File in2 = new File("C://Users//Administrator//ILData//test.nt.ttl");

    Announce.doing("Testing", in2.getName());
    N4Writer w = new N4Writer(new File(in2.toString().replace("ttl", "myout")), "Test run");
    for (Fact f : FactSource.from(in2)) {
      w.write(f);
    }
    w.close();
    Announce.done();

  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove() on N4Reader");
  }
}
