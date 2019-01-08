package basics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Running all the test cases for N4Reader from this source:
 * http://www.w3.org/TeamSubmission/turtle/#sec-conformance
 * 
 * @author Ghazaleh Haratinezhad Torbati
 *
 */
public class N4ReaderTest {
  
  private static final String RESOURCESPATH = "src/test/resources/" + N4Reader.class.getName();
  
  //Uncomment to not ignore the cases with @Ignore.
  //public @interface Ignore {};
  
  @Test
  @Ignore
  public void test00() throws IOException {
    String testFileNumber = "00";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }

  
  @Test
  public void test01() throws IOException {
    String testFileNumber = "01";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test02() throws IOException {
    String testFileNumber = "02";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test03() throws IOException {
    String testFileNumber = "03";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  @Ignore
  public void test04() throws IOException {
    String testFileNumber = "04";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  @Ignore
  public void test05() throws IOException {
    String testFileNumber = "05";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test06() throws IOException {
    String testFileNumber = "06";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  @Ignore
  public void test07() throws IOException {
    String testFileNumber = "07";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test08() throws IOException {
    String testFileNumber = "08";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  @Ignore
  public void test09() throws IOException {
    String testFileNumber = "09";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test10() throws IOException {
    String testFileNumber = "10";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test11() throws IOException {
    String testFileNumber = "11";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test12() throws IOException {
    String testFileNumber = "12";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  @Ignore
  public void test13() throws IOException {
    String testFileNumber = "13";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test14() throws IOException {
    String testFileNumber = "14";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test15() throws IOException {
    String testFileNumber = "15";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test16() throws IOException {
    String testFileNumber = "16";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test17() throws IOException {
    String testFileNumber = "17";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test18() throws IOException {
    String testFileNumber = "18";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test19() throws IOException {
    String testFileNumber = "19";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test20() throws IOException {
    String testFileNumber = "20";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  @Ignore
  public void test21() throws IOException {
    String testFileNumber = "21";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  @Ignore
  public void test22() throws IOException {
    String testFileNumber = "22";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  @Ignore
  public void test23() throws IOException {
    String testFileNumber = "23";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test24() throws IOException {
    String testFileNumber = "24";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test25() throws IOException {
    String testFileNumber = "25";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test26() throws IOException {
    String testFileNumber = "26";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test27() throws IOException {
    String testFileNumber = "27";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  @Ignore
  public void test28() throws IOException {
    String testFileNumber = "28";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void test29() throws IOException {
    String testFileNumber = "29";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  @Ignore
  public void test30() throws IOException {
    String testFileNumber = "30";
    String testFilePath = RESOURCESPATH + "/input/test-" + testFileNumber + ".ttl";
    String expectedOutputFilePath = RESOURCESPATH + "/output/test-" + testFileNumber + ".out";
    
    runAndCompare(testFilePath, expectedOutputFilePath);
  }
  
  @Test
  public void testN_Triples() throws IOException {
    String testFilePath = RESOURCESPATH + "/input/test.nt";
    N4Reader nr = new N4Reader(new File(testFilePath));
    List<Fact> actual = new ArrayList<>();
    while(nr.hasNext())
      actual.add(nr.next());
    nr.close();
    
    // I have nothing to compare it to. But It reads all facts with no error.
  }

  private void runAndCompare(String testFilePath, String expectedOutputFilePath) throws IOException {
    List<Fact> expected = new ArrayList<>();
    List<Fact> actual = new ArrayList<>();
    
    N4Reader nr = new N4Reader(new File(testFilePath));
    while(nr.hasNext())
      actual.add(nr.next());
    nr.close();
    
    nr = new N4Reader(new File(expectedOutputFilePath));
    while(nr.hasNext())
      expected.add(nr.next());
    nr.close();
    
    assertArrayEquals(expected.toArray(), actual.toArray());
  }

}
