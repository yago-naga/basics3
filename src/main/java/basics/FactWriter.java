package basics;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import javatools.filehandlers.FileSet;

/**
Copyright 2016 Fabian M. Suchanek, with contributions by Steffen Metzger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 This class provides an interface for fact serialisation, allowing you to
 materialize a set of YAGO facts e.g. as N3/N4 file depending on the chosen
 implementation.

 */
public abstract class FactWriter implements Closeable {

  /** Contains the file */
  protected final File file;

  /** Returns the file that we are writing to */
  public File getFile() {
    return (file);
  };

  /** Appends a fact to the output */
  public abstract void write(Fact f) throws IOException;

  /** Appends a comment to the output */
  public abstract void writeComment(String comment) throws IOException;

  /** Flushes buffers to disk */
  public abstract void flush() throws IOException;

  public FactWriter(File f) {
    file = f;

    // Make sure the target directory has been created.
    f.getAbsoluteFile().getParentFile().mkdirs();
  }

  /** Returns a fact writer for a file */
  public static FactWriter from(File f) throws Exception {
    return (FactWriter.from(f, null));
  }

  /** Returns a fact writer for a file */
  public static FactWriter from(File f, String header) throws Exception {
    switch (FileSet.extension(f).toLowerCase()) {
      case ".ttl":
        return (new N4Writer(f, header));
      case ".tsv":
        return (new TsvWriter(f, header));
    }
    throw new RuntimeException("Unsupported output file format for writing to: " + f);
  }

}
