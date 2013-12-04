/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

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

  public static void send(HttpServletResponse response, File f)
          throws ServletException, IOException {
    send(response, f, new byte[8094]);
  }
  
  public static void send(HttpServletResponse response, File f, byte[] buffer)
          throws ServletException, IOException {
    FileInputStream reader;
    OutputStream out = response.getOutputStream();
    if (f != null && f.exists()) {
      reader = new FileInputStream(f);
      try {
        int size = reader.read(buffer);
        while (size > 0) {
          out.write(buffer, 0, size);
          size = reader.read(buffer);
        }
      } catch (IOException e) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        System.out.println("Failed to send " + f);
      } finally {
        reader.close();
        out.close();
      }
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
