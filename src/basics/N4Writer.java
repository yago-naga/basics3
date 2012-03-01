package basics;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

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
public class N4Writer implements Closeable, FactWriter{
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
	public N4Writer(File file, String string) throws Exception {
		this(new FileWriter(file),string);
	}

	/** Writes a fact*/
	@Override
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
	
	public static class N4FileWriter extends N4Writer implements Closeable{
	  
	      /** Name of the file*/
	    public final File file;

	    
	     /** Starts a writer to this file*/
	    public N4FileWriter(File f, String header, String base, Map<String,String> prefixes ) throws Exception {
	      super(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8")),
	            header, base, prefixes);
	      file=f;
	    }
	    
	    /** Starts a writer to this file*/
	    public N4FileWriter(File f, String header) throws Exception {
	      this(f,header,FactComponent.YAGONAMESPACE,null);
	    }
	    
	    @Override
	    public void close() throws IOException {
	      super.close();
	    }	  
	}
	
	public static class N4StringWriter extends N4Writer implements Closeable{


	  /** Starts a writer to this file*/
	  public N4StringWriter(String header, String base, Map<String,String> prefixes ) throws Exception {
	    super(new StringWriter(), header, base, prefixes);     
	  }

	  /** Starts a writer to this file*/
	  public N4StringWriter(String header) throws Exception {
	    this(header,FactComponent.YAGONAMESPACE,null);
	  }

	  /** returns the ontology as N4 formatted string */
	  @Override
	  public String toString(){     
	    return writer.toString();
	  }

	  @Override
	  public void close() throws IOException {
	    super.close();
	  }
	}


}
