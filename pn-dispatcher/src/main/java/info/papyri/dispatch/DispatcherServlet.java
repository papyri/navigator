/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.papyri.dispatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author hcayless
 */
@WebServlet(name="SparqlWrapperServlet", urlPatterns={"/SparqlWrapper"})
public class DispatcherServlet extends HttpServlet {

  private static String graph = "rmi://localhost/papyri.info#pi";
  private static String path = "/sparql/";
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
      DispatcherRequestWrapper wRequest = new DispatcherRequestWrapper(request);
      StringBuilder query = new StringBuilder();
      query.append(request.getParameter("query"));
      String format = query.substring(query.lastIndexOf("/") + 1);
      if ("rdf".equals(format)) {
        format = "rdfxml";
      }
      query.delete(query.lastIndexOf("/"), query.length());
      String domain;
      if (query.indexOf("/") > 0) {
        domain = query.substring(0, query.indexOf("/"));
        query.delete(0, domain.length() + 1);
      } else {
        domain = query.toString();
        query.delete(0, query.length());
      }

      if ("ddbdp".equals(domain)) {
        wRequest.setParameter("query", ddbdp(query.toString()));
      }
      if ("apis".equals(domain)) {
        wRequest.setParameter("query", apis(query.toString()));
      }
      if ("hgv".equals(domain)) {
        wRequest.setParameter("query", hgv(query.toString()));
      }
      if ("hgvtrans".equals(domain)) {
        wRequest.setParameter("query", hgvtrans(query.toString()));
      }
      wRequest.setParameter("default-graph-uri", graph);
      wRequest.setParameter("format", format);
      ServletContext ctx = this.getServletContext();
      RequestDispatcher rd = ctx.getContext("/mulgara").getRequestDispatcher(path);
      rd.forward(wRequest, response);
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
    
    protected String ddbdp(String in) {
      if ("".equals(in) || in == null) {
        return "prefix dc: <http://purl"
             +  ".org/dc/terms/> construct{<http://papyri."
             +  "info/ddbdp> ?Predicate ?Object} from <rmi:/"
             +  "/localhost/papyri.info#pi> where { <http://papyri.info/ddbdp>" 
             +  " ?Predicate ?Object }";
      }
      String[] parts = in.split(";");
      if (parts.length == 1) {
        return "prefix dc: <http://"
             +  "purl.org/dc/terms/> construct{<http:/" 
             +  "/papyri.info/ddbdp/" + parts[0] + "> ?Predicate ?Object}"
             +  " from <rmi://localhost/papyri.info#pi>" 
             +  " where { <http://papyri.info/ddbdp/" + parts[0] + ">"
             +  " ?Predicate ?Object }";
      }
      if (parts.length == 2) {
        return "prefix dc: <http://"
             +  "purl.org/dc/terms/> construct{<http://"  
             +  "papyri.info/ddbdp/" + parts[0] + ";" + parts[1] + "> ?Predicate ?Object} "
             +  "from <rmi://localhost/papyri.info#pi> where "
             +  "{ <http://papyri.info/ddbdp/" + parts[0] + ";" + parts[1] + ">"
             +  " ?Predicate ?Object }";
      }
      if (parts.length == 3) {
          parts[2] = encode(parts[2]);
        return "construct{<http://"
             +  "papyri.info/ddbdp/" + parts[0] + ";" + parts[1] + ";" + parts[2]  
             +  "/source>  ?Predicate"  
             +  " ?Object} from <rmi://localhost/"  
             +  "papyri.info#pi> where {<http://"  
             +  "papyri.info/ddbdp/" + parts[0] + ";" + parts[1] + ";" + parts[2]  
             +  "/source> ?Predicate"  
             +  " ?Object}";
      }
      return in;
    }
    
    protected String apis(String in) {
      if ("".equals(in) || in == null) {
        return "prefix dc: <http://purl"
             +  ".org/dc/terms/> construct{<http://papyri."
             +  "info/apis> ?Predicate ?Object} from <rmi:/"
             +  "/localhost/papyri.info#pi> " 
             +  "where { <http://papyri.info/apis> ?Predicate ?Object}";
      }
      if (!in.contains(".")) {
        return "prefix dc: <http://purl.org/dc/terms/> " 
             + "construct{<http://papyri.info/apis/" + in + "> ?Predicate ?Object} "
             +  "from <rmi://localhost/papyri.info#pi> "
             +  "where { <http://papyri.info/apis/" + in + "> ?Predicate ?Object}";
      }
      return "construct{<http://papyri.info/apis/" + in + "/source> " 
           +  "?Predicate ?Object} from <rmi://localhost/papyri.info#pi> "
           +  "where {<http://papyri.info/apis/" + in + "/source> ?Predicate ?Object}";
    }
    
    protected String hgv(String in) {
      if ("".equals(in) || in == null) {
        return "prefix dc: <http://purl"
             +  ".org/dc/terms/> construct{<http://papyri."
             +  "info/hgv> ?Predicate ?Object} from <rmi:/"
             +  "/localhost/papyri.info#pi> " 
             +  "where { <http://papyri.info/hgv> ?Predicate ?Object}";
      }
      if (in.matches("\\d+[a-z]*")) {
        return "construct{<http://papyri.info/hgv/" + in + "/source> " +
                "?Predicate ?Object} from <rmi://localhost/papyri.info#pi> " +
                "where {<http://papyri.info/hgv/" + in + "/source> ?Predicate ?Object}";
      }
      return "prefix dc: <http://purl.org/dc/terms/> " +
              "construct{<http://papyri.info/hgv/" + in + "> ?Predicate ?Object} " +
              "from <rmi://localhost/papyri.info#pi> " +
              "where {<http://papyri.info/hgv/" + in + "> ?Predicate ?Object}";
    }

    protected String hgvtrans(String in) {
      return "construct{<http://papyri.info/hgvtrans/" + in + "/source> " +
                "?Predicate ?Object} from <rmi://localhost/papyri.info#pi> " +
                "where {<http://papyri.info/hgvtrans/" + in + "/source> ?Predicate ?Object}";
    }

    protected static String encode(String in) {
      try {
        String result = URLEncoder.encode(in, "UTF-8");
        return result.replaceAll("\\+", "%2B");
      } catch (Exception e) {
        //TODO: add warning;
        return in;
      }
    }

}
