/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hcayless
 */
@WebServlet(name = "XSLTService", urlPatterns = {"/epidocinator"})
public class XSLTService extends HttpServlet {

  private HashMap<String, XsltExecutable> xslts;
  private Processor processor = new Processor(false);
  private Logger log;

  @Override
  public void init(ServletConfig config) {
    log = LoggerFactory.getLogger(this.getClass());
    Enumeration<String> names = config.getInitParameterNames();
    xslts = new HashMap<String, XsltExecutable>();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      try {
        XsltCompiler compiler = processor.newXsltCompiler();
        XsltExecutable xslt = compiler.compile(new StreamSource(new File(config.getInitParameter(name))));
        xslts.put(name, xslt);
      } catch (SaxonApiException e) {
        log.error("Failed to compile "+name+".", e);
      }
    }

  }

  /** 
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    try {
      if ("GET".equals(request.getMethod())) {
        response.setContentType("application/json");
        out.print("{\"xslts\":[");
        String[] keys = (String[])xslts.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
          out.print("\""+keys[i]+"\"");
          if (i < keys.length - 2) out.print(",");
        }
        out.print("]}");
      } else {
        try {
          response.setContentType("text/html;charset=UTF-8");
          XsltTransformer xslt = xslts.get(request.getParameter("xsl")).load();
          xslt.setSource(new StreamSource(request.getReader()));
          xslt.setDestination(new Serializer(response.getWriter()));
          xslt.transform();
        } catch (Exception e) {
          log.error("Transformation "+request.getParameter("xsl")+" failed.", e);
        }
      }
    } finally {
      out.close();
    }
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /** 
   * Handles the HTTP <code>GET</code> method.
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
   * Handles the HTTP <code>POST</code> method.
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
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }// </editor-fold>
}
