package basics;

import java.io.Closeable;
import java.io.IOException;

/**
 * FactWriter interface - YAGO2S
 * 
 * Provides an interface for fact serialisation, 
 * allowing you to materialize a set of YAGO facts 
 * e.g. as N3/N4 file depending on the chosen implementation. 
 * 
 * @author Steffen Metzger
 * 
 */
public interface FactWriter extends Closeable{
  public void write(Fact f) throws IOException;
}
