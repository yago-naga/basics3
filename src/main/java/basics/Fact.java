package basics;

import java.util.regex.Pattern;

import javatools.administrative.D;
import javatools.parsers.DateParser;

/**
 * Class Fact
 * 
 * This code is part of the YAGO project at the Max Planck Institute for
 * Informatics and the Telecom ParisTech University. It is licensed under a
 * Creative Commons Attribution License by the YAGO team:
 * https://creativecommons.org/licenses/by/3.0/
 * 
 * @author Fabian M. Suchanek
 * 
 *         This class represents a fact for YAGO. Convention: all fact
 *         components must be the output of a method of the class FactComponent
 */
public class Fact {

	/** Use this to annotate hacks that are needed to make YAGO work*/
  public @interface ImplementationNote {
  
    String value();
  }

  /** ID (or NULL) */
	private String id;

	/** Argument 1 */
	protected final String subject;

	/** Relation */
	protected final String relation;

	/** Argument 2 */
	protected final String object;

	/** Hash code */
	protected final int hashCode;

	/**
	 * All fact components must be the output of a method of the class
	 * FactComponent!
	 */
	public Fact(String id, String arg1, String relation, String object) {
		this.subject = arg1;
		this.relation = relation;
		if (id != null) {
			this.id = id;
		}
		this.object = object;
		this.hashCode = arg1.hashCode() * relation.hashCode()
				* object.hashCode();
	}

	/**
	 * All fact components must be the output of a method of the class
	 * FactComponent!
	 */
	public Fact(String arg1, String relation, String arg2withDataType) {
		this(null, arg1, relation, arg2withDataType);
	}

	/** Creates a copy of the fact */
	public Fact(Fact copy) {
		this.subject = copy.subject;
		this.object = copy.object;
		this.relation = copy.relation;
		this.id = copy.getId();
		this.hashCode = copy.hashCode;
	}

	@Override
	public int hashCode() {
		return (hashCode);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Fact))
			return (false);
		Fact f = (Fact) obj;
		return (D.equal(id, f.id) && subject.equals(f.subject)
				&& relation.equals(f.relation) && object.equals(f.object));
	}

	/** Returns arg n as a Java string */
	public String getArgJavaString(int a) {
		return (FactComponent.asJavaString(a == 1 ? subject : object));
	}

	/** Returns object as a Java string */
	public String getObjectAsJavaString() {
		return (FactComponent.asJavaString(object));
	}

	/** Returns arg n, strips quotes, compiles a case-insensitive pattern */
	public Pattern getArgPattern(int a) {
		return (Pattern.compile(getArgJavaString(a), Pattern.CASE_INSENSITIVE));
	}

	/** Gets subject */
	public String getSubject() {
		return (subject);
	}

	/** Gets object with data type */
	public String getObject() {
		return (object);
	}

	/** Gets argument 1 or 2 */
	public String getArg(int a) {
		return (a == 1 ? getSubject() : getObject());
	}

	/** returns the relation */
	public String getRelation() {
		return (relation);
	}

	/** returns the datatype of the second argument */
	public String getDataType() {
		return (FactComponent.getDatatype(object));
	}

	@Override
	public String toString() {
		return (getId() == null ? "" : getId() + " ") + subject + " "
				+ relation + " " + object;
	}

	/**
	 * Makes (and sets) the id, which depends on the fact components, 1+6+3+6=16
	 * chars long, MIGHT HAVE DUPS, but the chances are # facts with id / 2
	 * billion
	 */
	public String makeId() {
		if (id != null)
			return (id);
		id = "id_";
		id += FactComponent.hashEntity(subject);
		id += "_" + FactComponent.hashRelation(relation);
		id += "_" + FactComponent.hashLiteralOrEntity(object);
		id = FactComponent.forYagoEntity(id);
		return (id);
	}

	/** returns the id */
	public String getId() {
		return id;
	}

	/** returns a TSV line */
	public String toTsvLine(boolean withValue) {
		if (withValue && FactComponent.isLiteral(object)) {
			String val = getValue();
			if (val == null)
				val = "";
			return ((id == null ? "" : id) + "\t" + getArg(1) + "\t"
					+ getRelation() + "\t" + getArg(2) + "\t" + val + "\n");
		} else {
			return ((id == null ? "" : id) + "\t" + getArg(1) + "\t"
					+ getRelation() + "\t" + getArg(2) + (withValue ? "\t\n"
						: "\n"));
		}
	}

	public String getValue() {
		String val = null;
		if (FactComponent.isLiteral(object)) {
			String datatype = getDataType();
			if (datatype != null && datatype.equals("xsd:date")) {
				String[] split = getObjectAsJavaString().split("-");
				if (split.length == 3) {
					for (int i = 0; i < 3; i++) {
						split[i] = split[i].replace('#', '0');
						while (split[i].length() < 2)
							split[i] = "0" + split[i];
					}
					val = split[0] + "." + split[1] + split[2];
				}
			} else if (datatype != null) {
				String object = getObjectAsJavaString();
				if (FactComponent.javaStringIsFloat(object))
					val = object;
			}
		}
		return val;
	}

	/** returns a TSV line */
	public String toTsvLine() {
		return toTsvLine(false);
	}

	/** Creates a meta fact for this fact (generates an id if necessary) */
	public Fact metaFact(String relation, String arg2withdatatype) {
		if (getId() == null)
			makeId();
		return (new Fact(getId(), relation, arg2withdatatype));
	}
}
