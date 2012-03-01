package basics;

import java.io.IOException;

/**
 * FactReader interface - YAGO2S
 * 
 * Provides an interface for fact deserialisation. 
 * Note that you will need to choose the interface implementation 
 * that fits to the FactWriter implementation used to generate the serialised fact representation.
 * 
 * 
 * 
 */
public interface FactReader {
  public Fact read() throws IOException;
  public void close()throws IOException;
}
