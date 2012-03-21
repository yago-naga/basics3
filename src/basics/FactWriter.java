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
public abstract class FactWriter implements Closeable{
  public abstract void write(Fact f) throws IOException; 
}
