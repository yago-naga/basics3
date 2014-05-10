package basics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import javatools.util.FileUtils;

/**
 * YAGO2s - TsvWriter
 * 
 * Writes facts to TSV files
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

	public TsvWriter(File f) throws FileNotFoundException {
		this(f, false);
	}

	public TsvWriter(File f, boolean writeDoubleValue)
			throws FileNotFoundException {
		super(f);
		this.writeDoubleValue = writeDoubleValue;
		out = FileUtils.getBufferedUTF8Writer(f);
	}
}
