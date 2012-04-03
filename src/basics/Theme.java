package basics;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import javatools.filehandlers.FileSet;

/**
 * Represents a theme
 * 
 * @author Fabian M. Suchanek
 *
 */
public class Theme implements Comparable<Theme>{

	/** Name of the theme*/
	public final String name;

	/** Description of the theme*/
	public final String description;

	/** Counts the themes*/
	protected static int ids=0;
	
	/** (dynamic) Id of this theme*/
	protected final int id=ids++;
	
	/** maps the names to themes*/
	protected static Map<String,Theme> name2theme=new TreeMap<>();
	
	public Theme(String name, String description) {
		this.name=name;	
		this.description=description;
		name2theme.put(this.name,this);
	}
	
	/** Returns the theme for a file; works only if the theme has been created before!*/
	public static Theme forFile(File f) {
	  return(name2theme.get(FileSet.newExtension(f.getName(), "")));
	}
	
	/** Returns the file name of this theme in the given folder*/
	public File file(File folder) {
		return(new File(folder,name+".ttl"));
	}
	
	@Override
	public int compareTo(Theme o) {	
		return id>o.id?1:id<o.id?-1:0;
	}
	
	@Override
	public boolean equals(Object obj) {	
		return (obj instanceof Theme) && ((Theme)obj).id==id;
	}
	@Override
	public String toString() {	
		return name;
	}
	@Override
	public int hashCode() {	
		return id;
	}
}
