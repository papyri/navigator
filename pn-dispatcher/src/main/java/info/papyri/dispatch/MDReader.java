/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.pegdown.PegDownProcessor;
/**
 *
 * @author hcayless
 */
@WebServlet(name = "MDReader", urlPatterns = {"/docs"})
public class MDReader extends HttpServlet {
  
  private static String DOCSHOME;
  private static String TEMPLATE;
  private PegDownProcessor peg;
  
  @Override
  public void init(ServletConfig config) {
    DOCSHOME = config.getInitParameter("docs");
    TEMPLATE = config.getInitParameter("template");
    peg = new PegDownProcessor();
  }

  /**
   * Processes requests for both HTTP
   * <code>GET</code> and
   * <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    StringBuilder requestPath = new StringBuilder(DOCSHOME);
    requestPath.append("/").append(request.getParameter("f")).append(".md");
    File f = new File(requestPath.toString());
    if (f.exists()) {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8")));
        StringBuilder mdf = new StringBuilder();
        char[] ch = new char[1024];
        while (reader.read(ch) > 0) {
          mdf.append(ch);
        }
        reader = new BufferedReader(new FileReader(new File(TEMPLATE)));
        String line;
        while ((line = reader.readLine()) != null) {
          out.println(line);
          if (line.contains("<div class=\"content\">")) {
            out.write(peg.markdownToHtml(mdf.toString()));
            reader.readLine(); // assume template has a throwaway line inside the content div
          }
        }
      } catch (IOException e) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } finally {      
        out.close();
      }
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      out.close();
    }
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP
   * <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP
   * <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }// </editor-fold>
}
