package basics;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import javatools.filehandlers.FileSet;

/**
 * FactWriter interface - YAGO2S
 * 
 * Provides an interface for fact serialisation, allowing you to materialize a
 * set of YAGO facts e.g. as N3/N4 file depending on the chosen implementation.
 * 
 * @author Steffen Metzger
 * 
 */
public abstract class FactWriter implements Closeable {
	/** Contains the file */
	protected final File file;

	/** Returns the file that we are writing to */
	public File getFile() {
		return (file);
	};

	public abstract void write(Fact f) throws IOException;

	public FactWriter(File f) {
		file = f;
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
		throw new RuntimeException(
				"Unsupported output file format for writing to: " + f);
	}
}
