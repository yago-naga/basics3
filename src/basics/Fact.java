package basics;

import javatools.administrative.D;


/**
 * Class Fact Represents a fact - YAGO2S
 * 
 * @author Fabian M. Suchanek
 * 
 * Convention: all fact components must be the output of a method of the class FactComponent
 */
public class Fact {
	/** ID (or NULL)*/
	public String id;
	/** Argument 1 */
	public String arg1;
	/** Relation */
	public String relation;
	/** Argument 2 */
	public String arg2;
    /** Datatype of arg2 (or null)*/
	public String arg2datatype;
	
	/** All fact components must be the output of a method of the class FactComponent!*/
	public Fact(String id, String arg1, String relation, String arg2, String arg2datatype) {
		super();
		this.arg1 = arg1.intern();
		this.arg2 = arg2.intern();
		this.relation = relation.intern();
		this.id=id==null?null:id.intern();
        this.arg2datatype=arg2datatype==null?null:arg2datatype.intern();
	}

	/** All fact components must be the output of a method of the class FactComponent!*/
	public Fact(String id, String arg1, String relation, String arg2withDataType) {
		super();
		String[] a2=arg2withDataType.split("^^");
		this.arg1 = arg1.intern();
		this.arg2 = a2[0].intern();
		this.relation = relation.intern();
        this.id=id==null?null:id.intern();
        this.arg2datatype=a2.length>1?a2[1].intern():null;
	}

	/** Creates a copy of the fact*/
	public Fact(Fact copy) {
		this.arg1 = copy.arg1;
		this.arg2 = copy.arg2;
		this.relation = copy.relation;
		this.id=copy.id;
		this.arg2datatype=copy.arg2datatype;
	}

	@Override
	public int hashCode() {
		if(id!=null) return(id.hashCode());
		return(arg1.hashCode()^relation.hashCode()^arg2.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Fact)) return(false);
		Fact f=(Fact)obj;
		// I have an id
		if(id!=null) {
			if(f.id==null) return(false);
			return(id.equals(f.id));
		}
		// I don't have an id
		if(f.id!=null) return(false);
 		return(D.equalPairs(arg1,f.arg1,relation,f.relation,arg2,f.arg2));
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
		return id+" "+arg1+" "+relation+" "+arg2+(arg2datatype==null?"":"^^"+arg2datatype);
	}

}
