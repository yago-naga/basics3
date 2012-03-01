package basics;

import java.io.IOException;


public interface FactWriter {
  public void write(Fact f) throws IOException;
  public void close()throws IOException;
}
