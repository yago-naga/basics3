package basics;

import java.io.File;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javatools.administrative.D;
import javatools.filehandlers.FileSet;
import javatools.util.FileUtils;

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

This class represents a source form which facts can be read (e.g., a file).
 */
public abstract class FactSource implements Iterable<Fact> {

  /** Name of this source */
  protected String name;

  /** returns a fact source from a file. assumes ttl by default. */
  public static FactSource from(File f) {
    if (!f.getName().contains(".")) f = FileSet.newExtension(f, ".ttl");
    return (new FileFactSource(f));
  }

  /**
   * returns a fact source from a file or URL
   * 
   * @throws MalformedURLException
   */
  public static FactSource from(String f) throws MalformedURLException {
    if (f.startsWith("http:")) return (new UrlFactSource(new URL(f)));
    return (new FileFactSource(new File(f)));
  }

  /** returns a fact source from an url */
  public static FactSource from(URL f) {
    return (new UrlFactSource(f));
  }

  /** returns a fact reader depending on the extension */
  protected static Iterator<Fact> factReader(Reader reader, String fileExtension) throws Exception {
    switch (fileExtension) {
      case ".ttl":
        return (new N4Reader(reader));
      case ".tsv":
        return (new TsvReader(reader));
      default:
        throw new RuntimeException("Unknown file format " + fileExtension);
    }
  }

  public String name() {
    return name;
  }

  /** Fact source from file */
  public static class FileFactSource extends FactSource {

    protected File file;

    @Override
    public Iterator<Fact> iterator() {
      try {
        return factReader(FileUtils.getBufferedUTF8Reader(file), FileSet.extension(file));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public FileFactSource(File file) {
      super();
      this.file = file;
      this.name = file == null ? "FactSource" : file.toString();
    }

    @Override
    public String toString() {
      return file.toString();
    }

  }

  /** Fact source from url */
  protected static class UrlFactSource extends FactSource {

    URL file;

    @Override
    public Iterator<Fact> iterator() {
      try {
        return factReader(FileUtils.getBufferedUTF8Reader(file.openStream()), FileSet.extension(file.toString()));
      } catch (Exception e) {
        e.printStackTrace();
        return (null);
      }
    }

    public UrlFactSource(URL file) {
      super();
      this.file = file;
      this.name = file.toString();
    }

    @Override
    public String toString() {
      return file.toString();
    }

  }

  public static void main(String[] args) throws Exception {
    for (Fact f : FactSource.from("c:/fabian/data/yago2s/yagoMetaFacts.tsv")) {
      D.p(f);
    }
  }

}
