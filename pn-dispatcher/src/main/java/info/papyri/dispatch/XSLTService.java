/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcayless
 */
@WebServlet(name = "XSLTService", urlPatterns = {"/epidocinator"})
public class XSLTService extends HttpServlet {

  private HashMap<String, XsltExecutable> xslts;
  private HashMap<String, String> resultTypes;
  private FileUtils util;
  private Processor processor = new Processor(false);
  private static final Logger LOGGER = Logger.getLogger(XSLTService.class.getName());

  @Override
  public void init(ServletConfig config) {
    util = new FileUtils(config.getInitParameter("xmlPath"));
    Enumeration<String> names = config.getInitParameterNames();
    xslts = new HashMap<String, XsltExecutable>();
    resultTypes = new HashMap<String, String>();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      if (name.contains("Path")) continue;
      if (name.contains("-type")) {
        resultTypes.put(name, config.getInitParameter(name));
        LOGGER.log(Level.INFO, "Adding " + name + ": " + config.getInitParameter(name));
      } else {
        try {
          XsltCompiler compiler = processor.newXsltCompiler();
          XsltExecutable xslt = compiler.compile(new StreamSource(new File(config.getInitParameter(name))));
          xslts.put(name, xslt);
        } catch (SaxonApiException e) {
          LOGGER.log(Level.SEVERE, "Failed to compile "+name+".", e);
        }
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
    PrintWriter out;
    if ("GET".equals(request.getMethod())) {
      if (request.getParameter("doc") != null) {
        response.setContentType(resultTypes.get(request.getParameter("xsl") + "-type") + ";charset=UTF-8");
        out = response.getWriter();
        try {
          XsltTransformer xslt = xslts.get(request.getParameter("xsl")).load();
          if (request.getParameter("coll") != null) {
            xslt.setParameter(new QName("collection"), new XdmAtomicValue(request.getParameter("coll")));
          }
          xslt.setSource(new StreamSource(util.getXmlFileFromId(request.getParameter("doc"))));
          Processor processor = new Processor(false);
          xslt.setDestination(processor.newSerializer(out));
          xslt.transform();
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Transformation "+request.getParameter("xsl")+" failed.", e);
        } finally {
          out.close();
        }
      } else {
        response.setContentType("application/json");
        out = response.getWriter();
        if(request.getParameter("jsonp") != null) {
          out.print(request.getParameter("jsonp"));
          out.print("(");
        }
        out.print("{\"xslts\":[");
        Iterator<String> itr = xslts.keySet().iterator();
        while (itr.hasNext()) {
          String key = itr.next();
          out.print("{\""+key+"\": \""+resultTypes.get(key+"-type")+"\"}");
          if (itr.hasNext()) {
            out.print(",");
          }
        }
        out.print("]}");
        if(request.getParameter("jsonp") != null) {
          out.print(")");
        }
        out.close();
      }
    } else {
      response.setContentType(resultTypes.get(request.getParameter("xsl") + "-type") + ";charset=UTF-8");
      out = response.getWriter();
      try {
        XsltTransformer xslt = xslts.get(request.getParameter("xsl")).load();
        if (request.getParameter("coll") != null) {
          xslt.setParameter(new QName("collection"), new XdmAtomicValue(request.getParameter("coll")));
        }
        xslt.setSource(new StreamSource(request.getReader()));
        Processor processor = new Processor(false);
        xslt.setDestination(processor.newSerializer(out));
        xslt.transform();
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Transformation "+request.getParameter("xsl")+" failed.", e);
      } finally {
        out.close();
      }
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
