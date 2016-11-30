package basics;

import java.io.File;

import javatools.administrative.Announce;
import javatools.filehandlers.FileSet;

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

This class translates a TSV file to a TTL file and vice versa.
*/
public class Tsv2Ttl {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) Announce.help("Tsv2Ttl file1.(tsv|ttl)", "", "Translates a tsv file to a ttl file or vice versa");
    File input = new File(args[0]);
    File output = null;
    String extension = FileSet.extension(input).toLowerCase();
    switch (extension) {
      case ".ttl":
        output = FileSet.newExtension(input, "tsv");
        break;
      case ".tsv":
        output = FileSet.newExtension(input, "ttl");
        break;
      default:
        Announce.error("Argument must be a TSV or TTL file");
    }
    if (output.exists() && output.length() > 200) Announce.error("Output file already exists:", output);
    Announce.doing("Translating", input, "to", output);
    try (FactWriter out = FactWriter.from(output)) {
      for (Fact f : FactSource.from(input)) {
        out.write(f);
      }
    }
    Announce.done();
  }
}
