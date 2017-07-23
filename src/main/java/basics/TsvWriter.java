package basics;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javatools.filehandlers.FileSet;
import javatools.util.FileUtils;

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

This class writes facts to TSV files
 */
public class TsvWriter extends FactWriter {

  protected Writer out;

  protected boolean writeDoubleValue = false;

  @Override
  public void close() throws IOException {
    out.close();
  }

  @Override
  public void flush() throws IOException {
    out.flush();
  }

  @Override
  public void write(Fact f) throws IOException {
    out.write(f.toTsvLine(writeDoubleValue));
  }

  public TsvWriter(File f) throws IOException {
    this(f, false);
  }

  public TsvWriter(File f, String header) throws IOException {
    this(f, true, header);
  }

  public TsvWriter(File f, boolean writeDoubleValue) throws IOException {
    this(f, writeDoubleValue, null);
  }

  public TsvWriter(File f, boolean writeDoubleValue, String header) throws IOException {
    super(f);
    this.writeDoubleValue = writeDoubleValue;
    out = FileUtils.getBufferedUTF8Writer(f);
    if (header != null) {
      header = header.replaceAll("\\s+", " ");
      Fact comment = new Fact(FactComponent.forYagoEntity("yagoTheme_" + FileSet.newExtension(f.getName(), null)), YAGO.hasGloss,
          FactComponent.forString(header));
      write(comment);
    }
  }

  /** Test*/
  public static void main(String[] args) throws Exception {
    try (FactWriter w = new TsvWriter(new File("c:/fabian/temp/t.tsv"), "Blah blah \n   \t blub \t blah")) {
      w.write(new Fact("<Elvis>", "rdf:type", "<livingPerson>"));
    }
  }

  @Override
  public void writeComment(String comment) throws IOException {
    out.write("# ");
    out.write(comment);
    out.write("\n");
  }

}
