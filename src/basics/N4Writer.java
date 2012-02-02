package basics;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * N4Writer - YAGO2s
 * 
 * Writes YAGO facts into a Turtle-compatible outout format
 * 
 * @author Fabian
 * 
 */
public class N4Writer implements Closeable {
	/** Writes the file */
	protected Writer writer;

	/** Starts a writer to this file*/
	public N4Writer(File f, String header) throws Exception {
		writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
		for(String line : header.split("\n")) {
			writer.write("# "+line+"\n");			
		}
		writer.write("\n@base <http://yago-knowledge.org> .\n");
		for(String prefix : FactComponent.standardPrefixes.keySet()) {
			writer.write("@prefix "+prefix+" <"+FactComponent.standardPrefixes.get(prefix)+"> .\n");
		}
		writer.write("\n");
	}
	
	/** Writes a fact*/
	public synchronized void write(Fact f) throws IOException {
		if(f.id!=null) writer.write("#@ "+f.id+"\n");
		writer.write(f.arg1);
		writer.write("\t");
		writer.write(f.relation);
		writer.write("\t");
		writer.write(f.arg2);
		if(f.arg2datatype!=null) writer.write("^^"+f.arg2datatype);
		writer.write(" .\n");
	}
	
	@Override
	public void close() throws IOException {
		writer.close();
	}

}
