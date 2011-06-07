/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.sync;

import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hcayless
 */
public class GitWrapperTest {
  
  public GitWrapperTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  /**
   * Test of filenameToUri method, of class GitWrapper.
   */
  @Test
  public void testFilenameToUriDDbDP() {
    System.out.println("filenameToUri");
    String file = "DDB_EpiDoc_XML/bgu/bgu.1/bgu.1.2.xml";
    String expResult = "http://papyri.info/ddbdp/bgu;1;2";
    String result = GitWrapper.filenameToUri(file);
    assertEquals(expResult, result);
  }
  
  /**
   * Test of filenameToUri method, of class GitWrapper.
   */
  @Test
  public void testFilenameToUriHGV() {
    System.out.println("filenameToUri");
    String file = "HGV_meta_EpiDoc/HGV20/19358.xml";
    String expResult = "http://papyri.info/hgv/19358";
    String result = GitWrapper.filenameToUri(file);
    assertEquals(expResult, result);
  }
}
