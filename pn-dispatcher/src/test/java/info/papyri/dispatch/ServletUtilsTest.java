/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import junit.framework.TestCase;

/**
 *
 * @author hcayless
 */
public class ServletUtilsTest extends TestCase {
  
  public ServletUtilsTest(String testName) {
    super(testName);
  }

  /**
   * Test of scrub method, of class ServletUtils.
   */
  public void testScrub() {
    System.out.println("scrub");
    String in = "<\"<img%20src=https://pbs.twimg.com/profile_images/3274461853/52263042d7ca94ca26b0685d89132ba2.jpeg%20/>%20>";
    String expResult = "%20>";
    String result = ServletUtils.scrub(in);
    assertEquals(expResult, result);
  }
}
