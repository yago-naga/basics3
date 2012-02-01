package basics;


/**
 * Class Fact Represents a fact - YAGO2S
 * 
 * @author Fabian M. Suchanek
 * 
 * TODO: are two facts equal if their id is equal?
 * 
 */
public class Fact {
	/** ID */
	public String id;
	/** Argument 1 */
	public String arg1;
	/** Relation */
	public String relation;
	/** Argument 2 */
	public String arg2;
    /** Datatype of arg2 (or null)*/
	public String arg2datatype;
	
	public Fact(String id, String arg1, String relation, String arg2, String arg2datatype) {
		super();
		this.arg1 = arg1.intern();
		this.arg2 = arg2.intern();
		this.relation = relation.intern();
        this.id=id.intern();
        this.arg2datatype=arg2datatype.intern();
	}

	public Fact(Fact copy) {
		this.arg1 = copy.arg1;
		this.arg2 = copy.arg2;
		this.relation = copy.relation;
		this.id=copy.id;
		this.arg2datatype=copy.arg2datatype;
	}

	@Override
	public int hashCode() {
		return(id.hashCode());
		/*
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arg1 == null) ? 0 : arg1.hashCode());
		result = prime * result + ((arg2 == null) ? 0 : arg2.hashCode());
		result = prime * result
				+ ((relation == null) ? 0 : relation.hashCode());
		return result;
		*/
	}

	@Override
	public boolean equals(Object obj) {
		return(obj instanceof Fact && ((Fact)(obj)).id.equals(id));
		/*
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fact other = (Fact) obj;
		if (arg1 == null) {
			if (other.arg1 != null)
				return false;
		} else if (!arg1.equals(other.arg1))
			return false;
		if (arg2 == null) {
			if (other.arg2 != null)
				return false;
		} else if (!arg2.equals(other.arg2))
			return false;
		if (relation == null) {
			if (other.relation != null)
				return false;
		} else if (!relation.equals(other.relation))
			return false;
		return true;
		*/		
	}

	/** Sets argument 1 or 2 */
	public void setArg(int a, String s) {
		if (a == 1)
			arg1 = s;
		else
			arg2 = s;
	}

	/** Gets argument 1 or 2 */
	public String getArg(int a) {
		return (a == 1 ? arg1 : arg2);
	}
	
	@Override
	public String toString() {		
		return id+" "+arg1+" "+relation+" "+arg2;
	}

}
