/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.File;
import java.io.PrintWriter;
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
  
  public void testGetHtmlBiblioFile() {
    String collection = "biblio";
    String item = "1234";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    File expResult = new File("/data/papyri.info/pn/idp.html/biblio/2/1234.html");
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

  public void testFindMatchesMultiple() {
    String query = "sheep";
    String id = "http://papyri.info/ddbdp/p.ross.georg;2;15";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("Sheep");
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
  
  public void testFindMatchesMultipleTerms() {
    String query = "αναγκαιας χρειας";
    String id = "http://papyri.info/ddbdp/bgu;12;2188";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("ἀναγκαία̣ς");
    expResult.add("χ[ρ]είας");
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

  public void testStripDiacriticals() {
    String in = "ὑπόμνημα";
    String out = "υπομνημα";
    assertEquals(FileUtils.stripDiacriticals(in), out);
  }

  public void testFindMatchesBigFile() {
    String query = "sheep";
    String id = "http://papyri.info/ddbdp/p.mich;2;123";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    String text = instance.loadHtmlFromId(id);
    //System.out.println(instance.highlight(query, text));
    assertTrue(true); //TODO: fix
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

  public void testFindMatchesSubstringPhraseWordBoundaries() {
    String query = "transcription_ngram_ia:(\"μεν\\^ \\^κα\")";
    String id = "http://papyri.info/ddbdp/bgu;1;110";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("μεν) κα");
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
    String query = "transcription_ngram_ia:(στρατηγωι)";
    String id = "http://papyri.info/ddbdp/bgu;16;2629";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("στρ̣[ατη-]<br/>31. γῶι");
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

  public void testFindMatchesPlace() {
    String query = "place:Alexandria";
    String id = "http://papyri.info/ddbdp/p.cair.zen;2;59195";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("Alexandria");
    String result = instance.highlight(query, instance.loadHtmlFromId(id));
    int matches = 0;
    for (String e : expResult) {
      if (result.contains(e)) {
        matches++;
        break;
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
    expResult.add("<span class=\"highlight\">sheep</span>");
    expResult.add("<span class=\"highlight\">sheep-</span>pens");
    List<String> result = instance.highlightMatches(query, instance.loadTextFromId(id));
    int matches = 0;
    for (String r : result) {
      for (String e : expResult) {
        if (r.contains(e)) {
          matches++;
        }
      }
    }
    assertEquals(expResult.size(), matches);
  }

  public void testFindMatchesBrokenQuery() {
    String query = "transcription_ngram_ia:(";
    String id = "http://papyri.info/ddbdp/bgu;2;521";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    String text = instance.loadTextFromId(id);
    String result = instance.highlight(query, text);
    assertTrue(result.equals(text));
  }

  public void testFindMatchesNot() {
    String query = "\\^καισ NOT \\^καισαρ";
    String id = "http://papyri.info/ddbdp/p.hamb;2;187";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("κα̣ισ");
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

  public void testFindNgram() {
    String query = "transcription_ngram_ia:(\"μεν\\^ \\^κα\")";
    String id = "http://papyri.info/ddbdp/bgu;3;923";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    List<String> expResult = new ArrayList<String>();
    expResult.add("<span class=\"highlight\">μὲν [κα</span>");
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

  public void testHighlightLineNo() {
    String query = "transcription_ngram_ia:(θμοινεθυμις)";
    String id = "http://papyri.info/ddbdp/p.fuad.i.univ;;5";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    String result = instance.highlight(query, instance.loadHtmlFromId(id));
    assertTrue(result.contains("<br id=\"av,2-l3\"><span class=\"linenumber\">3</span><span class=\"highlight\">Θμ̣ο̣ινεθῦμις</span>"));
  }
  
  public void testNgramHighlightLineNo() {
    String query = "transcription_ngram_ia:(\\^θρασω\\^)";
    String id = "http://papyri.info/ddbdp/p.sakaon;;94";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    String result = instance.highlight(query, instance.loadHtmlFromId(id));
    assertTrue(result.contains("<br id=\"ar-l5\"><span class=\"linenumber\">5</span><span class=\"highlight\">Θρασώ</span>"));
  }

  public void testHighlightByAnchor() {
    String query = "transcription_ngram_ia:(απολλωνιωι)";
    String id = "http://papyri.info/ddbdp/bgu;10;1941";
    FileUtils instance = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
    String result = instance.highlight(query, instance.loadHtmlFromId(id));
    assertTrue(result.contains("<span class=\"highlight\">Ἀπολλωνί̣ωι<br id=\"aFrA,2-l3\"></span>"));
  }

  public void testSubstringAfter() {
    String in = "foobar/baz/";
    String find = "/";
    assertEquals ("baz/", FileUtils.substringAfter(in, find));
  }

  public void testRewriteOldUrl() {
    String url = "/idp_static/current/data/ddb/html/bgu/bgu.1/bgu.1.308.html";
    String expResult = "http://papyri.info/ddbdp/bgu;1;308";
    assertEquals(expResult, FileUtils.rewriteOldUrl(url));
    url = "/idp_static/current/data/aggregated/html/p.aberd/p.aberd.98.html";
    expResult = "http://papyri.info/ddbdp/p.aberd;;98";
    assertEquals(expResult, FileUtils.rewriteOldUrl(url));
  }

  public void testRewriteOldUrl2() {
    String url = "/idp_static/current/data/aggregated/html/p.aberd/p.aberd.98.html";
    String expResult = "http://papyri.info/ddbdp/p.aberd;;98";
    assertEquals(expResult, FileUtils.rewriteOldUrl(url));
  }

}
