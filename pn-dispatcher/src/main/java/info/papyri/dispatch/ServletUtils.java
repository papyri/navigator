/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

/**
 *
 * @author hcayless
 */
public class ServletUtils {
  
  /*
   * Cleans out any embedded markup in the input string.
   */
  public static String scrub(String in) {
    return in.replaceAll("<[^>]+>", "");
  }
  
}
