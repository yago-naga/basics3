package basics;

import java.io.File;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;

import javatools.administrative.Announce;
import javatools.filehandlers.FileSet;
import javatools.util.FileUtils;

/**
 * FactSource - YAGO2S
 * 
 * A source of facts (given by an URL, or a file) in any format (N4, TSV, ...) that can be read multiple times
 * 
 * @author Steffen Metzger
 * 
 */
public abstract class FactSource implements Iterable<Fact> {
	
	/** returns a fact source from a file*/
	public static FactSource from(File f) {
		return(new FileFactSource(f));
	}
	
	/** returns a fact source from an url*/
	public static FactSource from(URL f) {
		return(new UrlFactSource(f));
	}
	
	/** returns a fact reader depending on the extension*/
	protected static Iterator<Fact> factReader(Reader reader, String fileExtension) throws Exception {
		switch (fileExtension) {
		case ".ttl":
			return (new N4Reader(reader));
		default:
			Announce.error("Unknown file format " + fileExtension);
			return (null);
		}
	}

	/** Fact source from file*/
	protected static class FileFactSource extends FactSource {
		File file;

		@Override
		public Iterator<Fact> iterator() {
			try {
				return factReader(FileUtils.getBufferedUTF8Reader(file), FileSet.extension(file));
			} catch (Exception e) {
				e.printStackTrace();
				return (null);
			}
		}

		public FileFactSource(File file) {
			super();
			this.file = file;
		}
		
		@Override
		public String toString() {		
			return file.toString();
		}
				
	}
	
	/** Fact source from url*/
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
		}

		@Override
		public String toString() {		
			return file.toString();
		}

	}

}
