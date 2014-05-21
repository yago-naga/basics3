package basics;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javatools.util.FileUtils;

/**
 * Class TsvWriter
 * 
 * This code is part of the YAGO project at the Max Planck Institute for
 * Informatics and the Telecom ParisTech University. It is licensed under a
 * Creative Commons Attribution License by the YAGO team:
 * https://creativecommons.org/licenses/by/3.0/
 * 
 * This class writes facts to TSV files
 * 
 * @author Fabian M. Suchanek
 * 
 */
public class TsvWriter extends FactWriter {

	protected Writer out;

	protected boolean writeDoubleValue = false;

	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public void write(Fact f) throws IOException {
		out.write(f.toTsvLine(writeDoubleValue));
	}

	public TsvWriter(File f) throws IOException {
		this(f, false);
	}

	public TsvWriter(File f, String header) throws IOException {
		this(f, false, header);
	}

	public TsvWriter(File f, boolean writeDoubleValue) throws IOException {
		this(f, writeDoubleValue, null);
	}

	public TsvWriter(File f, boolean writeDoubleValue, String header)
			throws IOException {
		super(f);
		this.writeDoubleValue = writeDoubleValue;
		out = FileUtils.getBufferedUTF8Writer(f);
		if (header != null) {
			for (String line : header.split("\n")) {
				out.write("// " + line + "\n");
			}
			out.write("\n");
		}
	}
}
