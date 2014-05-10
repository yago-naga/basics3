package basics;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javatools.administrative.Announce;
import javatools.filehandlers.FileSet;

/**
 * Represents a theme
 * 
 * @author Fabian M. Suchanek
 * 
 */
public class Theme implements Comparable<Theme> {

	/** Types of Theme */
	public enum ThemeGroup {
		TAXONOMY, SIMPLETAX, CORE, GEONAMES, META, MULTILINGUAL, LINK, OTHER, INTERNAL, WIKIPEDIA
	}

	/** Types of my theme */
	public final ThemeGroup themeGroup;

	/** Name of the theme */
	public final String name;

	/** Description of the theme */
	public final String description;

	/** maps the names to themes */
	protected static Map<String, Theme> name2theme = new TreeMap<>();

	public Theme(String name, String description) {
		this(name, description, name.startsWith("yago") ? ThemeGroup.OTHER
				: ThemeGroup.INTERNAL);
	}

	public Theme(String name, String description, ThemeGroup group) {
		this.name = name;
		this.description = description;
		if (name2theme.containsKey(name))
			Announce.error("Duplicate Theme: ", this);
		name2theme.put(this.name, this);
		themeGroup = group;
	}

	public Theme(String name, String language, String description,
			ThemeGroup group) {
		this(name + "_" + language, description, group);
	}

	public Theme(String name, String language, String description) {
		this(name + "_" + language, description);
	}

	/** Returns the language of a theme (or NULL) */
	public String language() {
		if (name.length() < 3 || name.charAt(name.length() - 3) != '_')
			return (null);
		return (name.substring(name.length() - 2));
	}

	/**
	 * Returns the theme for a file; works only if the theme has been created
	 * before!
	 */
	public static Theme forFile(File f) {
		return (name2theme.get(FileSet.newExtension(f.getName(), "")));
	}

	/** Returns the file name of this theme in the given folder */
	public File file(File folder) {
		return (new File(folder, name + ".ttl"));
	}

	/** TRUE for export-ready themes */
	public boolean isFinal() {
		return (name.startsWith("yago"));
	}

	@Override
	public int compareTo(Theme o) {
		return name.compareTo(o.name);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Theme) && ((Theme) obj).name.equals(name);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/** returns the theme group with that name */
	public static ThemeGroup themeGroupFor(String name) {
		try {
			return (ThemeGroup.valueOf(name));
		} catch (Exception whocares) {
			return (null);
		}
	}

	/** Fact writer for writing the theme */
	protected FactWriter factWriter;
	/** Caching the theme */
	protected FactCollection cache = null;
	/** File of the theme */
	protected File file;

	/** Opens the theme for writing */
	public synchronized void open(FactWriter w) {
		if (factWriter != null)
			Announce.error("Already writing into Theme " + this);
		if (file != null)
			Announce.error("Theme " + this + " already written");
		factWriter = w;
		file = w.getFile();
	}

	/** Closes the theme for writing */
	public void close() throws IOException {
		if (factWriter == null)
			Announce.error("Theme " + this
					+ " cannot be closed because it was not open");
		factWriter.close();
		factWriter = null;
	}

	/**
	 * Returns the fact source. Unlike the fact cache, the fact source provides
	 * the facts in the order in which they were written
	 */
	public FactSource factSource() {
		if (file == null)
			Announce.error("Theme " + this + " has not yet been written");
		if (factWriter != null)
			Announce.error("Theme " + this + " is currently being written");
		return (FactSource.from(file));
	}

	/** Returns the file (or NULL) */
	public File file() {
		return (file);
	}

	/** Sets the fact source (to use a theme that is already there) */
	public synchronized void setFile(File f) {
		if (file != null) {
			if (file.equals(f))
				return;
			else
				Announce.error("Theme " + this
						+ " is already assigned to a file");
		}
		if (!f.exists())
			Announce.error("File " + f + " for theme " + this
					+ " does not exist");
		file = f;
	}

	/** returns the cache */
	public FactCollection factCollection() throws IOException {
		if (factWriter != null)
			Announce.error("Theme " + this + " is currently being written");
		if (file == null)
			Announce.error("Theme " + this + " has not yet been written");
		if (cache == null)
			cache = new FactCollection(file, true);
		return (cache);
	}

	/** Removes the cache */
	public void killCache() {
		cache = null;
	}

	/** Writes a fact */
	public void write(Fact f) throws IOException {
		factWriter.write(f);
	}

	public boolean isAvailable() {
		return file != null && factWriter == null;
	}

}
