package basics;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import basics.Theme.ThemeGroup;

/**
 * BaseTheme
 * 
 * Represents a theme that can be instantiated in several languages.
 * 
 * @author Fabian M. Suchanek
 * 
 */

public class MultilingualTheme {

	/** Types of my theme */
	public final ThemeGroup themeGroup;

	/** Name of the theme */
	public final String name;

	/** Description of the theme */
	public final String description;

	public MultilingualTheme(String name, String description) {
		this(name, description, name.startsWith("yago") ? ThemeGroup.OTHER
				: ThemeGroup.INTERNAL);
	}

	public MultilingualTheme(String name, String description, ThemeGroup group) {
		this.name = name;
		this.description = description;
		themeGroup = group;
	}

	/** Maps languages to themes */
	protected Map<String, Theme> language2theme = new HashMap<String, Theme>();

	/** Returns the theme in the given languages */
	public Collection<Theme> inLanguages(Collection<String> languages) {
		Set<Theme> result = new HashSet<>();
		for (String language : languages)
			result.add(inLanguage(language));
		return (result);
	}

	/** Returns this theme in a language */
	public Theme inLanguage(String lang) {
		Theme result = language2theme.get(lang);
		if (result == null) {
			result = new Theme(name, lang, description, themeGroup);
			language2theme.put(lang, result);
		}
		return (result);
	}

}
