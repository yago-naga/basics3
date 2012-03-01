package basics;

import java.io.Closeable;
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
public interface FactReader extends Closeable{
  public Fact read() throws IOException;
  @Override
  public void close()throws IOException;
}
