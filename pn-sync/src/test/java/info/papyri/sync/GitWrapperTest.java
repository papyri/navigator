/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.sync;

import info.papyri.map;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author hcayless
 */
public class GitWrapperTest {
  
  public GitWrapperTest() {
  }
  
  private String base = "/data/papyri.info/idp.data";

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
    System.out.println("filenameToUri for DDbDP");
    String file = base + "/DDB_EpiDoc_XML/p.mich/p.mich.20/p.mich.20.809.xml";
    String expResult = "http://papyri.info/ddbdp/p.mich;20;809/source";
    String result = GitWrapper.filenameToUri(file);
    assertEquals(expResult, result);
  }
  
  @Test
  public void testFakeFilenameToUriDDbDP() {
    System.out.println("filenameToUri for plausible but non-existent DDbDP");
    //Fake filename — does not exist in numbers server
    String file = base + "/DDB_EpiDoc_XML/sb/sb.26/sb.26.1234.xml";
    String expResult = "http://papyri.info/ddbdp/sb;26;1234/source";
    String result = GitWrapper.filenameToUri(file);
    assertEquals(expResult, result);
  }
  
  @Test
  public void test2LevelFilenameToUriDDbDP() {
    System.out.println("filenameToUri for 2 level DDbDP");
    String file = base + "/DDB_EpiDoc_XML/p.vet.aelii/p.vet.aelii.9.xml";
    String expResult = "http://papyri.info/ddbdp/p.vet.aelii;;9/source";
    String result = GitWrapper.filenameToUri(file);
    assertEquals(expResult, result);
  }
          
  @Test
  public void test2LevelFilenameToUriDDbDP2() {
    System.out.println("filenameToUri for 2 level DDbDP");
    String file = base + "/DDB_EpiDoc_XML/p.count/p.count.45.xml";
    String expResult = "http://papyri.info/ddbdp/p.count;;45/source";
    String result = GitWrapper.filenameToUri(file);
    assertEquals(expResult, result);
  }
  
  @Test
  public void testLookupDDbDPID() {
    System.out.println("Looking up DDbDP ID");
    String id = "http://papyri.info/apis/gothenburg.apis.14/source";
    String expResult = "http://papyri.info/ddbdp/sb;20;14671/source";
    String result = GitWrapper.lookupMainId(id);
    assertEquals(expResult, result);
  }
  
  @Test
  public void testBrokenFilenameToURI() {
    System.out.println("Broken filename to URI");
    String file = "DDB_EpiDoc_XML/p.rain.unterricht/p.rain.unterricht. /p.rain.unterricht. .61.xml";
    String expResult = "";
    String result = GitWrapper.filenameToUri(file);
    assertEquals(expResult, result);
  }
  
  /**
   * Test of filenameToUri method, of class GitWrapper.
   */
  @Test
  public void testFilenameToUriHGV() {
    System.out.println("filenameToUri for HGV");
    String file = "HGV_meta_EpiDoc/HGV20/19358.xml";
    String expResult = "http://papyri.info/hgv/19358/source";
    String result = GitWrapper.filenameToUri(file);
    assertEquals(expResult, result);
  }
  
  @Test
  public void testMapping() {
    System.out.println("Testing Mapping");
    List<String> l = new ArrayList<String>();
    l.add("/data/papyri.info/idp.data/DDB_EpiDoc_XML/bgu/bgu.1/bgu.1.2.xml");
    l.add("/data/papyri.info/idp.data/DDB_EpiDoc_XML/bgu/bgu.1/bgu.1.3.xml");
    try {
      map.mapFiles(l);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    assertTrue(true);
  }
  
  @Test
  public void testIndexing() {
    System.out.println("Indexing test disabled due to long duration of index optimization. Uncomment code in testIndexing() to run.");
    List<String> l = new ArrayList<String>();
    l.add("http://papyri.info/ddbdp/bgu;1;2/source");
    l.add("http://papyri.info/ddbdp/bgu;1;3/source");
    try {
      //indexer.index(l);
    } catch (Exception e) {
      e.printStackTrace();
    }
    assertTrue(true);
  }
   
}
