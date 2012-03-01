package basics;

import java.io.IOException;


public interface FactReader {
  public Fact read() throws IOException;
  public void close()throws IOException;
}
