package basics;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import javatools.administrative.Announce;
import javatools.datatypes.PeekIterator;
import javatools.filehandlers.FileLines;

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

This class provides a reader for facts from an TSV document. Supports: 2
columns (relation will be "&lt;relation>"); 3 columns (ids will be null); 4
columns (id subject relation object); 5 columns (id subject relation object
value)
 */
public class TsvReader extends PeekIterator<Fact> {

  /** Reads the file */
  protected FileLines lines;

  private File file = null;

  /** Show warning for first line with wrong number of columns */
  private boolean showColumnWarning = true;

  /** Creates a N4 reader */
  public TsvReader(Reader r) throws IOException {
    lines = new FileLines(r);
  }

  @Override
  protected Fact internalNext() throws Exception {
    String entireLine;
    do {
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
          if (showColumnWarning) {
            Announce.warning("Unsupported number of columns (", line.length, ")", file == null ? "" : " from file " + file);
            showColumnWarning = false;
          }
      }
    } while (true);
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
