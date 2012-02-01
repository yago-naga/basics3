package basics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import javatools.administrative.Announce;
import javatools.administrative.D;
import javatools.datatypes.PeekIterator;
import javatools.filehandlers.FileLines;
import javatools.parsers.Char;

/**
 * N4Reader - YAGO2S
 * 
 * Provides a reader for facts from an N4 document. This follows the Turtle
 * Specification http://www.w3.org/TeamSubmission/turtle/#sec-grammar-grammar It
 * (1) understands a first (optional) component (2) and it does not support all
 * Turtle features
 * 
 * Passes all tests from
 * http://www.w3.org/TeamSubmission/turtle/#sec-conformance except 23
 * 
 * @author Fabian M. Suchanek
 * 
 */
public class N4Reader extends PeekIterator<Fact> {

	/** Reads the file */
	protected Reader reader;

	/** Maps custom prefixes */
	protected Map<String, String> prefixes = new TreeMap<String, String>();

	/** Custom base */
	protected String base = null;

	/** Creates a N4 reader */
	public N4Reader(File f) throws IOException {
		reader = new InputStreamReader(new FileInputStream(f), "UTF-8");
	}

	/** Value for "Ignore, read new */
	public static final int READNEW = -2;

	/** Current character */
	protected int c = READNEW;

	/** returns the next item */
	public String nextItem() throws IOException {
		if (c == READNEW)
			c = FileLines.firstCharAfterSpace(reader);
		switch (c) {
		case '@':
			c = READNEW;
			return ('@' + FileLines.readToSpace(reader).toString());
		case '#':
			c = READNEW;
			FileLines.scrollTo(reader, '\n', '\r');
			return (nextItem());
		case -1:
			return ("EOF");
		case '.':
			c = READNEW;
			return (".");
		case '<':
			c = READNEW;
			return (Formatter.makeUri(FileLines.readTo(reader, '>').toString()));
		case '"':
			String language = null;
			String datatype = null;
			String string = FileLines.readTo(reader, '"').toString();
			while (string.endsWith("\\"))
				string += '"' + FileLines.readTo(reader, '"').toString();
			c = reader.read();
			switch (c) {
			case '@':
				while (Character.isLetter(c = reader.read()))
					language += c;
				break;
			case '^':
				reader.read();
				c = READNEW;
				datatype = nextItem();
				break;
			case '"':
				string = FileLines.readTo(reader, "\"\"\"").toString();
				if (string.length() > 2)
					string = string.substring(0, string.length() - 3);
				c = READNEW;
				break;
			}
			if(Character.isWhitespace(c)) c=READNEW;
			return (Formatter.makeString(string, language, datatype));
		case '[':
			String blank = FileLines.readTo(reader, ']').toString().trim();
			if (blank.length() != 0) {
				Announce.warning("Properties of blank node ignored", blank);
			}
			c = READNEW;
			return (Formatter.makeId());
		case '(':
			c = READNEW;
			String list = FileLines.readTo(reader, ')').toString().trim();
			Announce.warning("Cannot handle list", list);
			return (Formatter.makeId());
		case ',':
			c = READNEW;
			Announce.warning("Commas are not supported");
			FileLines.scrollTo(reader, '.');
			return (".");
		case ';':
			c = READNEW;
			Announce.warning("Semicolons are not supported");
			FileLines.scrollTo(reader, '.');
			return (".");
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
			return (Formatter.makeNumber(number));
		default:
			String name = ((char) c) + FileLines.readToSpace(reader).toString();
			// Save some stuff that follows...
			if (".;,<".indexOf(Char.last(name)) != -1) {
				c = Char.last(name);
				name = Char.cutLast(name);
			} else {
				c = READNEW;
			}
			// Predefined Turtle entities
			if (name.equals("a"))
				return (Formatter.makeQname("rdf:", "type"));
			if (name.equals("true"))
				return (Formatter.makeString("true", Formatter.makeQname("xsd:", "boolean"), null));
			if (name.equals("false"))
				return (Formatter.makeString("false", Formatter.makeQname("xsd:", "boolean"), null));
			// Prefixes
			int colon = name.indexOf(':');
			if (colon == -1) {
				Announce.warning("Invalid entity", Char.encodeHex(name));
				FileLines.scrollTo(reader, '.');
				c = READNEW;
				return (".");
			}
			String prefix = name.substring(0, colon + 1);
			name = name.substring(colon + 1);
			if (prefixes.containsKey(prefix)) {
				return (Formatter.makeUri(prefixes.get(prefix) + name));
			}
			// Other
			return (Formatter.makeQname(prefix, name));
		}
	}

	@Override
	protected Fact internalNext() throws Exception {
		while (true) {
			String item = nextItem();
			if (item.equals("EOF"))
				return (null);
			// Prefix
			if (item.equalsIgnoreCase("@PREFIX")) {
				String prefix = FileLines.readTo(reader, ':').toString().trim() + ':';
				FileLines.scrollTo(reader, '<');
				String dest = FileLines.readTo(reader, '>').toString().trim();
				FileLines.scrollTo(reader, '.');
				if (Formatter.standardPrefixes.containsKey(prefix)) {
					if (dest.equals(Formatter.standardPrefixes.get(prefix)))
						continue;
					else
						Announce.warning("Redefining standard prefix", prefix, "from", Formatter.standardPrefixes
								.get(prefix), "to", dest);
				}
				prefixes.put(prefix, dest);
				continue;
			}

			// Base
			if (item.equalsIgnoreCase("@BASE")) {
				FileLines.scrollTo(reader, '<');
				base = FileLines.readTo(reader, '>').toString().trim();
				FileLines.scrollTo(reader, '.');
				continue;
			}

			// Unknown
			if (item.startsWith("@")) {
				Announce.warning("Unknown directive:", item);
				FileLines.scrollTo(reader, '.');
				continue;
			}

			// Subject Verb Object
			String item2 = nextItem();
			if (item2.equals(".")) {
				Announce.warning("Only one item on line", item, item2);
				continue;
			}
			String item3 = nextItem();
			if (item3.equals(".")) {
				Announce.warning("Only two items on line", item, item2);
				continue;
			}
			String item4 = nextItem();
			if (item4.equals(".")) {
				return (new Fact(Formatter.makeId(), item, item2, item3));
			}
			String item5 = nextItem();
			if (item5.equals(".")) {
				return (new Fact(item, item2, item3, item4));
			}

			// Line too long
			Announce.warning("More than four items on line", item, item2, item3, item4, item5);
			FileLines.scrollTo(reader, '.');
			continue;
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
	public static void main(String[] args) throws IOException {
		/*for (File in : new File("/Users/Fabian/Fabian/Temp/tests").listFiles()) {
			if (!in.getName().matches("test-\\d+\\.ttl.*"))
				continue;
			Announce.doing("Testing", in.getName());
			Writer w = new FileWriter(in.toString().replace("ttl", "myout"));
			for (Fact f : new N4Reader(in)) {
				w.write(f.toString() + " .\n");
			}
			w.close();
			Announce.done();
		}*/
		File in = new File("/Users/Fabian/Fabian/Temp/tests/test.nt.txt");
		Writer w = new FileWriter(in.toString().replace("nt", "myout"));
		for (Fact f : new N4Reader(in)) {
			w.write(f.toString() + " .\n");
			D.p(f.toString());
		}
		w.close();

	}
}
