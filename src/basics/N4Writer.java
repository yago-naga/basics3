package basics;


import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javatools.util.FileUtils;

/**
 * N4Writer - YAGO2s
 * 
 * Writes YAGO facts into a Turtle-compatible output format
 * 
 * Output validates with
 * http://www.rdfabout.com/demo/validator
 * 
 * @author Fabian Suchanek
 * 
 */
public class N4Writer extends FactWriter{
	/** Writes the file */
	protected Writer writer;

	
	 /** Starts a writer to this file*/
  public N4Writer(Writer w, String header,String base, Map<String,String> prefixes ) throws Exception {
    writer=w;
    for(String line : header.split("\n")) {
      writer.write("# "+line+"\n");     
    }
    writer.write("\n@base <"+base+"> .\n");
    for(String prefix : FactComponent.standardPrefixes.keySet()) {
      writer.write("@prefix "+prefix+" <"+FactComponent.standardPrefixes.get(prefix)+"> .\n");
    }
    if(prefixes!=null)
      for(Map.Entry<String, String> prefix : prefixes.entrySet()) {
        writer.write("@prefix "+prefix.getKey()+" <"+FactComponent.standardPrefixes.get(prefix.getValue())+"> .\n");
      }
    writer.write("\n");
    
  }
	
	/** Starts a writer to this file*/
	public N4Writer(Writer w, String header) throws Exception {
		this(w,header,FactComponent.YAGONAMESPACE,null);
	}
	
  /** Starts a writer to this file*/
  public N4Writer(File f, String header, String base, Map<String,String> prefixes ) throws Exception {
    this(FileUtils.getBufferedUTF8Writer(f), header, base, prefixes);    
  }
  
  /** Starts a writer to this file*/
  public N4Writer(File f, String header) throws Exception {
    this(FileUtils.getBufferedUTF8Writer(f),header);
  }
  
  
	
	/** Writes a fact*/
	@Override
	public synchronized void write(Fact f) throws IOException {
		if(f.getId()!=null) writer.write("#@ "+f.getId()+"\n");
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
