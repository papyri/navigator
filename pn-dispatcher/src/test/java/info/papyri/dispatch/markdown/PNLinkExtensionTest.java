/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.markdown;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import junit.framework.TestCase;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;

/**
 *
 * @author Hugh A. Cayless
 */
public class PNLinkExtensionTest extends TestCase {
  private static final DataHolder OPTIONS = new MutableDataSet().set(Parser.EXTENSIONS, 
          Collections.singletonList(PNLinkExtension.create()));
  private static final Parser PARSER = Parser.builder(OPTIONS).build();
  private static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();
  
  /**
   * Test of DDbLinks in PN custom parser. These have the form:
   * <ddb:bgu;1;2>, which should produce: 
   * <a href="/ddbdp/bgu;1;2">bgu;1;2</a>
   */
  public void testDDbLink() {
    System.out.println("DDbLink");
    StringBuilder testMd = new StringBuilder();
    StringBuilder testHtml = new StringBuilder();
    char[] buffer = new char[1024];
    int l;
    try {
      System.out.println(this.getClass().getResource("/DDbLink.md"));
      Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("/DDbLink.md"),Charset.forName("UTF-8"));
      while ((l = reader.read(buffer)) >= 0) {
        testMd.append(buffer, 0, l);
      }
      reader = new InputStreamReader(this.getClass().getResourceAsStream("/DDbLink.html"));
      buffer = new char[1024];
      while ((l = reader.read(buffer)) >= 0) {
        testHtml.append(buffer, 0, l);
      }
    } catch (IOException e) {
      fail("File " + this.getClass().getResource("/DDbLink.md") + " not found.");
    }
    Node doc = PARSER.parse(testMd.toString());
    String result = RENDERER.render(doc);
    System.out.println("Result: " + result);
    assertEquals(result, testHtml.toString());
  }

  /**
   * Test of BibLinks in PN custom markdown.
   */
  public void testBibLink() {
    System.out.println("BibLink");
    
    StringBuilder testMd = new StringBuilder();
    StringBuilder testHtml = new StringBuilder();
    char[] buffer = new char[1024];
    int l;
    try {
      System.out.println(this.getClass().getResource("/BibLink.md"));
      Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("/BibLink.md"),Charset.forName("UTF-8"));
      while ((l = reader.read(buffer)) >= 0) {
        testMd.append(buffer, 0, l);
      }
      reader = new InputStreamReader(this.getClass().getResourceAsStream("/BibLink.html"));
      buffer = new char[1024];
      while ((l = reader.read(buffer)) >= 0) {
        testHtml.append(buffer, 0, l);
      }
    } catch (IOException e) {
      fail("File " + this.getClass().getResource("/BibLink.md") + " not found.");
    }
    Node doc = PARSER.parse(testMd.toString());
    String result = RENDERER.render(doc);
    assertEquals(result, testHtml.toString());
  }
}
