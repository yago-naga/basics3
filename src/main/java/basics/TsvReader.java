package basics;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import javatools.administrative.Announce;
import javatools.datatypes.PeekIterator;
import javatools.filehandlers.FileLines;

/**
 * Class TsvReader
 *
 * This code is part of the YAGO project at the Max Planck Institute for
 * Informatics and the Telecom ParisTech University. It is licensed under a
 * Creative Commons Attribution License by the YAGO team:
 * https://creativecommons.org/licenses/by/3.0/
 *
 * This class provides a reader for facts from an TSV document. Supports: 2
 * columns (relation will be "&lt;relation>"); 3 columns (ids will be null); 4
 * columns (id subject relation object); 5 columns (id subject relation object
 * value)
 *
 * @author Fabian M. Suchanek
 *
 */
public class TsvReader extends PeekIterator<Fact> {

  /** Reads the file */
  protected FileLines lines;

  private File file = null;

  /** Creates a N4 reader */
  public TsvReader(Reader r) throws IOException {
    lines = new FileLines(r);
  }

  @Override
  protected Fact internalNext() throws Exception {
    String entireLine;
    do {
      if (!lines.hasNext()) return (null);
      entireLine = lines.next();
    } while (entireLine.isEmpty() || entireLine.startsWith("// "));
    String[] line = entireLine.split("\t");
    switch (line.length) {
      case 2:
        return (new Fact(line[0], "<relation>", line[1]));
      case 3:
        if (line[0].startsWith("#")) return (new Fact(line[0], line[1], "<relation>", line[2]));
        return (new Fact(line[0], line[1], line[2]));
      case 4:
      case 5:
        String id = line[0];
        if (id.isEmpty() || id.equals("null")) id = null;
        return (new Fact(id, line[1], line[2], line[3]));
      default:
        Announce.warning("Unsupported number of columns (", line.length, "), aborting read", file == null ? "" : " from file " + file);
        return (null);
    }
  }

  @Override
  public void close() {
    super.close();
  }

  public TsvReader(File f) throws IOException {
    file = f;
    lines = new FileLines(f, "UTF8");
  }

  /** Parses out the gloss and the theme group from the object of a hasGloss-fact about a theme*/
  public static String[] glossAndGroup(String themeGloss) {
    int pos = themeGloss.lastIndexOf('.');
    int endPos = themeGloss.lastIndexOf(' ');
    if (endPos == -1 || endPos < pos) {
      return (new String[] { themeGloss.substring(pos + 1), null });
    }
    return (new String[] { themeGloss.substring(pos + 1, endPos), themeGloss.substring(endPos + 1) });
  }

  /** Test program shows that reading TSV is twice as fact as reading TTL */
  public static void main(String[] args) throws Exception {
    Announce.startTimer();
    for (@SuppressWarnings("unused")
    Fact f : FactSource.from(new File("c:/fabian/data/yago3test/yagoTaxonomy.tsv"))) {

    }
    Announce.message("TSV: ", Announce.getTime());
    Announce.startTimer();
    for (@SuppressWarnings("unused")
    Fact f : FactSource.from(new File("c:/fabian/data/yago3test/yagoTaxonomy.ttl"))) {

    }
    Announce.message("TTL: ", Announce.getTime());

  }
}
