package basics;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * FactReader interface - YAGO2S
 * 
 * Provides an interface for fact deserialisation. 
 * Note that you will need to choose the interface implementation 
 * that fits to the FactWriter implementation used to generate the serialised fact representation.
 * 
 * @author Steffen Metzger
 * 
 */
public interface FactReader extends Iterable<Fact> {
}
