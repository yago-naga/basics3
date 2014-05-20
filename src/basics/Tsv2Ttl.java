package basics;

import java.io.File;

import javatools.administrative.Announce;
import javatools.filehandlers.FileSet;

/** Translates a TSV file to a TTL file and vice versa */
public class Tsv2Ttl {
	public static void main(String[] args) throws Exception {
		if (args.length != 1)
			Announce.help("Tsv2Ttl file1.(tsv|ttl)", "",
					"Translates a tsv file to a ttl file or vice versa");
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
		if (output.exists())
			Announce.error("Output file already exists:", output);
		try (FactWriter out = FactWriter.from(output)) {
			for (Fact f : FactSource.from(input)) {
				out.write(f);
			}
		}

	}
}
