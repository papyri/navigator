/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.pegdown;

import junit.framework.TestCase;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.nio.charset.Charset;
import org.pegdown.PegDownProcessor;
import org.pegdown.plugins.PegDownPlugins;
import org.pegdown.Extensions;
import info.papyri.dispatch.pegdown.PNCustomLinkPlugin;

/**
 *
 * @author hcayless
 */
public class PNCustomLinkPluginTest extends TestCase {
  
  public PNCustomLinkPluginTest(String testName) {
    super(testName);
  }

  /**
   * Test of DDbLinks in PN custom parser. These have the form:
   * {ddb:bgu;1;2}, which should produce: 
   * <a href="/ddbdp/bgu;1;2" id="bgu;1;2">bgu;1;2</a>
   */
  public void testDDbLink() {
    System.out.println("DDbLink");
    PegDownPlugins.Builder plugins = PegDownPlugins.builder();
    plugins.withPlugin(PNCustomLinkPlugin.class);
    PNPegDownProcessor peg = new PNPegDownProcessor(
            Extensions.NONE, 
            PegDownProcessor.DEFAULT_MAX_PARSING_TIME,
            plugins.build(),
            new PNCustomLinkPlugin());
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
    String result = peg.markdownToHtml(testMd.toString());
    assertEquals(result, testHtml.toString());
  }

  /**
   * Test of BibLinks in PN custom markdown.
   */
  public void testBibLink() {
    System.out.println("BibLink");
    PegDownPlugins.Builder plugins = PegDownPlugins.builder();
    plugins.withPlugin(PNCustomLinkPlugin.class);
    PNPegDownProcessor peg = new PNPegDownProcessor(
            Extensions.NONE, 
            PegDownProcessor.DEFAULT_MAX_PARSING_TIME,
            plugins.build(),
            new PNCustomLinkPlugin());
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
    String result = peg.markdownToHtml(testMd.toString());
    assertEquals(result, testHtml.toString());
  }

}
