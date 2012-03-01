package basics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import javatools.administrative.Announce;
import javatools.datatypes.PeekIterator;
import javatools.filehandlers.FileLines;
import javatools.filehandlers.UTF8Reader;
import javatools.parsers.Char;

import basics.N4Writer.N4FileWriter;

/**
 * N4Reader - YAGO2S
 * 
 * Provides a reader for facts from an N4 document. This follows the Turtle
 * Specification http://www.w3.org/TeamSubmission/turtle/#sec-grammar-grammar It
 * (1) understands a first (optional) component in the p[receding comment (2)
 * and it does not support all Turtle features
 * 
 * Passes all tests from
 * http://www.w3.org/TeamSubmission/turtle/#sec-conformance except 23
 * http://www.w3.org/2000/10/rdf-tests/rdfcore/ntriples/test.nt
 * 
 * @author Fabian M. Suchanek
 * 
 */
public class N4Reader extends PeekIterator<Fact> implements FactReader{

	/** Reads the file */
	protected Reader reader;

	/** Maps custom prefixes */
	protected Map<String, String> prefixes = new TreeMap<String, String>();

	/** Custom base */
	protected String base = null;

	/** Creates an N4 reader, 
	 * only for use by derived classes as no reader is defined by this constructor */
	protected N4Reader() {};
	
	/** Creates a N4 reader */
	public N4Reader(File f) throws IOException {
		this(new InputStreamReader(new FileInputStream(f), "UTF-8"));
	}

	/** Creates a N4 reader */
	public N4Reader(Reader r) throws IOException {
		reader = r;
	}

	/** Value for "Ignore, read new */
	public static final int READNEW = -2;

	/** Current character */
	protected int c = READNEW;

	/** returns element after element, null iff no element left */
	@Override
	public Fact read(){
	  if(hasNext())
	    return next();
	  else 
	    return null;
	}
	
	/** returns the next item */
	public String nextItem() throws IOException {
		if (c == READNEW)
			c = FileLines.firstCharAfterSpace(reader);
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
			String uri=FileLines.readTo(reader, '>').toString();
			if(base!=null && !uri.startsWith("http://")) uri=base+uri;
			return (FactComponent.forUri(uri));
		case '"':
			String language = null;
			String datatype = null;
			String string = FileLines.readTo(reader, '"').toString();
			while (string.endsWith("\\") && !string.endsWith("\\\\"))
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
			if (Character.isWhitespace(c))
				c = READNEW;
			return (FactComponent.forString(Char.decodeBackslash(string), language, datatype));
		case '[':
			String blank = FileLines.readTo(reader, ']').toString().trim();
			if (blank.length() != 0) {
				Announce.warning("Properties of blank node ignored", blank);
			}
			c = READNEW;
			return (FactComponent.makeId());
		case '(':
			c = READNEW;
			String list = FileLines.readTo(reader, ')').toString().trim();
			Announce.warning("Cannot handle list", list);
			return (FactComponent.forQname("rdf:", "nil"));
		case '.':
			c = READNEW;
			return (".");
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
			return (FactComponent.forNumber(number));
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
				return (FactComponent.forQname("rdf:", "type"));
			if (name.equals("true"))
				return (FactComponent.forString("true", null, FactComponent.forQname("xsd:", "boolean")));
			if (name.equals("false"))
				return (FactComponent.forString("false", null, FactComponent.forQname("xsd:", "boolean")));
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
				return (FactComponent.forUri(prefixes.get(prefix) + name));
			}
			// Other
			return (FactComponent.forQname(prefix, name));
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
				if(base!=null && !dest.startsWith("http://")) dest=base+dest;
				FileLines.scrollTo(reader, '.');
				if (FactComponent.standardPrefixes.containsKey(prefix)) {
					if (dest.equals(FactComponent.standardPrefixes.get(prefix)))
						continue;
					else
						Announce.warning("Redefining standard prefix", prefix, "from", FactComponent.standardPrefixes
								.get(prefix), "to", dest);
				}
				prefixes.put(prefix, dest);
				continue;
			}

			// Base
			if (item.equalsIgnoreCase("@BASE")) {
				FileLines.scrollTo(reader, '<');
				String uri = FileLines.readTo(reader, '>').toString().trim();
				if(uri.startsWith("http://")) base=uri;
				else base=base+uri;
				FileLines.scrollTo(reader, '.');
				continue;
			}

			// Unknown
			if (item.startsWith("@")) {
				Announce.warning("Unknown directive:", item);
				FileLines.scrollTo(reader, '.');
				continue;
			}

			// Fact identifier
			String factId = null;
			String subject;
			if (item.startsWith("&")) {
				factId = item.substring(1);
				subject = nextItem();
			} else {
				subject = item;
			}

			// Subject Verb Object
			if (subject.equals(".")) {
				Announce.warning("Dot on empty line");
				continue;
			}
			String predicate = nextItem();
			if (predicate.equals(".")) {
				Announce.warning("Only one item on line", subject);
				continue;
			}
			String object = nextItem();
			String dot = nextItem();
			if (!dot.equals(".")) {
				// Line too long
				Announce.warning("More than three items on line", factId, subject, predicate, object, dot);
				FileLines.scrollTo(reader, '.');
				continue;
			}
			return (new Fact(factId, subject, predicate, object));
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
	
	public static class N4WebReader extends N4Reader{
	  InputStream io=null;;
	  public N4WebReader(URL url) throws IOException{
	    super();
	    io=url.openStream();
	    reader=new UTF8Reader(io);	    
	  }
	  
	  @Override
	  public void close(){
	    super.close();
	    try {
	      io.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	  }
	}

	/** returns only facts with a specific relation*/
	public PeekIterator<Fact> factsWithRelation(final String relation) {
		return(new PeekIterator<Fact>() {

			@Override
			protected Fact internalNext() throws Exception {
				while(N4Reader.this.hasNext()) {
					Fact next=N4Reader.this.next();
					if(next.relation.equals(relation)) return(next);
				}
				return(null);
			}			
		});
	}
	/**
	 * Test
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {

		for (File in : new File("/Users/Fabian/Fabian/Temp/tests").listFiles()) {
			if (!in.getName().matches("test-\\d+\\.ttl.*"))
				continue;
			Announce.doing("Testing", in.getName());
			N4Writer w = new N4FileWriter(new File(in.toString().replace("ttl", "myout")),"Test run");
			for (Fact f : new N4Reader(in)) {
				w.write(f);
			}
			w.close();
			Announce.done();
		}

		File in = new File("/Users/Fabian/Fabian/Temp/tests/test.nt.txt");
		N4Writer w = new N4FileWriter(new File(in.toString().replace("nt", "myout")),"Test run");
		for (Fact f : new N4Reader(in)) {
			w.write(f);
		}
		w.close();

	}
}
