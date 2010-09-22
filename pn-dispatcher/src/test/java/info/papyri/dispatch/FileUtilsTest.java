/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author hcayless
 */
public class FileUtilsTest extends TestCase {

  public FileUtilsTest(String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test of getHtmlFile method, of class FileUtils.
   */
  public void testGetHtmlFile() {
    String collection = "ddbdp";
    String item = "bgu;1;2";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    File expResult = new File("/data/papyri.info/pn/idp.html/DDB_EpiDoc_XML/bgu/bgu.1/bgu.1.2.html");
    File result = instance.getHtmlFile(collection, item);
    assertEquals(expResult, result);
  }

  /**
   * Test of getTextFile method, of class FileUtils.
   */
  public void testGetTextFile() {
    String collection = "ddbdp";
    String item = "bgu;1;2";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    File expResult = new File("/data/papyri.info/pn/idp.html/DDB_EpiDoc_XML/bgu/bgu.1/bgu.1.2.txt");
    File result = instance.getTextFile(collection, item);
    assertEquals(expResult, result);
  }

  /**
   * Test of getXmlFile method, of class FileUtils.
   */
  public void testGetXmlFile() {
    String collection = "ddbdp";
    String item = "bgu;1;2";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    File expResult = new File("/data/papyri.info/idp.data/DDB_EpiDoc_XML/bgu/bgu.1/bgu.1.2.xml");
    File result = instance.getXmlFile(collection, item);
    assertEquals(expResult, result);
  }

  /**
   * Test of findMatches method, of class FileUtils.
   */
  public void testFindMatchesWildcard() {
    String query = "ostrak*";
    String id = "http://papyri.info/ddbdp/o.heid;;123";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("Ostrakon");
    List<String> result = instance.highlightMatches(query, instance.loadTextFromId(id));
    int matches = 0;
    for (String r : result) {
      for (String e : expResult) {
        if (r.contains(e)) {
          matches++;
          break;
        }
      }
    }
    assertEquals(expResult.size(), matches);
  }

  public void testFindMatchesSubstringPhrase() {
    String query = "\"\\^και\\^ \\^στρατηγ\"";
    String id = "http://papyri.info/ddbdp/bgu;14;2373";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("καὶ στρ]ατηγ");
    List<String> result = instance.highlightMatches(query, instance.loadTextFromId(id));
    int matches = 0;
    for (String r : result) {
      for (String e : expResult) {
        if (r.contains(e)) {
          matches++;
          break;
        }
      }
    }
    assertEquals(expResult.size(), matches);
  }

  public void testFindMatchesLinebreak() {
    String query = "στρατηγωι";
    String id = "http://papyri.info/ddbdp/bgu;16;2629";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("στρ̣[ατη]γῶι");
    List<String> result = instance.highlightMatches(query, instance.loadTextFromId(id));
    int matches = 0;
    for (String r : result) {
      for (String e : expResult) {
        if (r.contains(e)) {
          matches++;
          break;
        }
      }
    }
    assertEquals(expResult.size(), matches);
  }

  public void testFindMatchesElision() {
    String query = "τουτεστιν";
    String id = "http://papyri.info/ddbdp/p.neph;;31";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("τουτ’έστιν");
    List<String> result = instance.highlightMatches(query, instance.loadTextFromId(id));
    int matches = 0;
    for (String r : result) {
      for (String e : expResult) {
        if (r.contains(e)) {
          matches++;
          break;
        }
      }
    }
    assertEquals(expResult.size(), matches);
  }

  public void testFindMatchesAPIS() {
    String query = "sheep";
    String id = "http://papyri.info/apis/michigan.apis.4520";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("sheep");
    List<String> result = instance.highlightMatches(query, instance.loadTextFromId(id));
    int matches = 0;
    for (String r : result) {
      for (String e : expResult) {
        if (r.contains(e)) {
          matches++;
          break;
        }
      }
    }
    assertEquals(expResult.size(), matches);
  }
  
  public void testFindMatchesSheep() {
    String query = "sheep";
    String id = "http://papyri.info/ddbdp/p.cair.zen;1;59068";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("sheep");
    expResult.add("sheep");
    List<String> result = instance.highlightMatches(query, instance.loadTextFromId(id));
    int matches = 0;
    for (String r : result) {
      for (String e : expResult) {
        if (r.contains(e)) {
          matches++;
          break;
        }
      }
    }
    assertEquals(expResult.size(), matches);
  }

public void testFindMatchesBroken() {
    String query = "transcription_ngram_ia:(";
    String id = "http://papyri.info/ddbdp/bgu;2;521";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    String text = instance.loadTextFromId(id);
    String result = instance.highlight(query, text);
    assertTrue(result.equals(text));
  }

  public void testGetDivIndexes() {
    String id = "http://papyri.info/apis/toronto.apis.17";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    String html = instance.loadHtmlFromId(id);
    List<int[]> divs = instance.getDivIndexes(html);
    assertEquals(5, divs.size());
  }

  public void testDivHighlight() {
    String id = "http://papyri.info/apis/toronto.apis.17";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    String html = instance.loadHtmlFromId(id);
    List<int[]> divs = instance.getDivIndexes(html);
    boolean foundText = false;
    for (int[] div : divs) {
      if (instance.highlight("εσμεν", html.substring(div[0], div[1])).contains("<span class=\"highlight\">&lt;ἔ&gt;σμεν</span>")) {
        foundText = true;
      }
    }
    assertTrue(foundText);
  }
  
  public void testDivHighlight2() {
    String id = "http://papyri.info/ddbdp/bgu;1;74";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    String html = instance.loadHtmlFromId(id);
    List<int[]> divs = instance.getDivIndexes(html);
    boolean foundText = false;
    for (int[] div : divs) {
      if (instance.highlight("αρχιερ", html.substring(div[0], div[1])).contains("<span class=\"highlight\">ἀρχιερ</span>")) {
        foundText = true;
      }
    }
    assertTrue(foundText);
  }

}
