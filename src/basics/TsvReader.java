package basics;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import paris.storage.YagoFact;

import javatools.administrative.Announce;
import javatools.datatypes.PeekIterator;
import javatools.filehandlers.TSVFile;

/**
 * TsvReader - YAGO2S
 * 
 * Provides a reader for facts from an TSV document. Supports
 * - 2 columns (relation will be "&lt;relation>")
 * - 3 columns (ids will be null)
 * - 4 columns
 * 
 * @author Fabian M. Suchanek
 * 
 */
public class TsvReader extends PeekIterator<Fact> {

  /** Reads the file */
  protected TSVFile lines;

  /** Creates a N4 reader */
  public TsvReader(Reader r) throws IOException {
    lines = new TSVFile(r);
  }

  @Override
  protected Fact internalNext() throws Exception {
    if (!lines.hasNext()) return (null);
    List<String> line = lines.next();
    switch (line.size()) {
      case 2:
        return (new Fact(line.get(0), "<relation>", line.get(1)));
      case 3:
        if(line.get(0).startsWith("#")) return (new Fact(line.get(0),line.get(1), "<relation>", line.get(2)));
        return (new Fact(line.get(0), line.get(1), line.get(2)));
      case 4:
      case 5:
        return (new Fact(line.get(0),line.get(1), line.get(2), line.get(3)));
      default:
        Announce.warning("Unsupported number of columns (", line.size(), ")");
        return (null);
    }
  }
}
