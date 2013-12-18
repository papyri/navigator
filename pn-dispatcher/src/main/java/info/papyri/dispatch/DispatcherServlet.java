package info.papyri.dispatch;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author hcayless
 */
@WebServlet(name="DispatcherServlet", urlPatterns={"/dispatch"})
public class DispatcherServlet extends HttpServlet {

  private static String graph = "http://papyri.info/graph";
  private static String path = "/pi/query";
  private String sparqlServer;
  private static Logger logger = Logger.getLogger("pn-dispatch");
  private enum Method {
    RDF ("rdfxml"),
    N3,
    TURTLE,
    JSON,
    SOURCE,
    ATOM;

    private final String name;

    Method() {
      this.name = this.name().toLowerCase();
    }
    Method(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return this.name;
    }

  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    sparqlServer = config.getInitParameter("sparqlUrl");
    ServletUtils.setupLogging(config.getServletContext(), config.getInitParameter("log4j-properties-location"));
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
      Map<String,String> params = new HashMap<String,String>();
      StringBuilder query = new StringBuilder();
      query.append(request.getParameter("query"));
      String format = query.substring(query.lastIndexOf("/") + 1);

      for (Method method : Method.values()) {
        if (method.name().toLowerCase().equals(format)) {
          format = method.toString();
        }
      }
      if ("rdfxml".equals(format) || "turtle".equals(format) || "n3".equals(format) || "json".equals(format)) {
        query.delete(query.lastIndexOf("/"), query.length());
        if (query.toString().contains("/annotation/")) {
          params.put("query", annotation(query.toString()));
        } else {
          String domain;
          if (query.indexOf("/") > 0) {
            domain = query.substring(0, query.indexOf("/"));
            query.delete(0, domain.length() + 1);
          } else {
            domain = query.toString();
            query.delete(0, query.length());
          }
          if ("ddbdp".equals(domain)) {
            params.put("query", ddbdp(query.toString()));
          }
          if ("apis".equals(domain)) {
            params.put("query", apis(query.toString()));
          }
          if ("hgv".equals(domain)) {
            params.put("query", hgv(query.toString()));
          }
          if ("hgvtrans".equals(domain) || "biblio".equals(domain) 
                  || "trismegistos".equals(domain)) {
            params.put("query", query(domain, query.toString()));
          } 
        }

        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        HttpURLConnection http = null;
        try {
          URL m = new URL(sparqlServer + path + "?" + readParams(params));
          http = (HttpURLConnection)m.openConnection();
          if ("rdfxml".equals(format)) {
            http.addRequestProperty("Accept", "application/rdf+xml");
          }
          if ("turtle".equals(format)) {
            http.addRequestProperty("Accept", "text/turtle");
          }
          if ("n3".equals(format)) {
            http.addRequestProperty("Accept", "text/plain");
          }
          if ("json".equals(format)) {
            http.addRequestProperty("Accept", "application/rdf+json");
          }
          http.setConnectTimeout(2000);
          if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
            response.setContentType(http.getContentType());
            byte[] b = new byte[8192];
            in = new BufferedInputStream(http.getInputStream());
            out = new BufferedOutputStream(response.getOutputStream());
            int s = in.read(b, 0, b.length);
            while (s > 0) {
              out.write(b, 0, s);
              s = in.read(b, 0, b.length);
            }
          } else {
            response.sendError(http.getResponseCode());
          }
        } catch (SocketTimeoutException e) {
          logger.error("Socket timeout during numbers server request.", e);
        } finally {
          if (out != null) {
            out.close();
          }
          if (in != null) {
            in.close();
          }
          if (http != null) {
            http.disconnect();
          }
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

    protected String readParams(Map params) throws ServletException {
      StringBuilder result = new StringBuilder();
      Object[] keys = params.keySet().toArray();
      for (int i = 0; i < keys.length; i++) {
        result.append((String)keys[i]);
        result.append("=");
        try {
          result.append(URLEncoder.encode((String)params.get(keys[i]), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          throw new ServletException(e);
        }
        if (i < keys.length - 1) {
          result.append("&");
        }
      }
      return result.toString();
    }
    
    protected void appendPrefixes(StringBuilder query) {
      query.append("prefix dc: <http://purl.org/dc/terms/> ");
      query.append("prefix foaf: <http://xmlns.com/foaf/0.1/> ");
      query.append("prefix cito: <http://purl.org/spar/cito/> ");
      query.append("prefix lawd: <http://lawd.info/ontology/> ");
      query.append("prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
      query.append("prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
      query.append("prefix oa: <http://www.openannotation.org/ns/>");
    }

    protected String ddbdp(String in) {
      StringBuilder out = new StringBuilder();
      appendPrefixes(out);
      if ("".equals(in) || in == null) {
        out.append("construct{<http://papyri.info/ddbdp> ?Predicate ?Object . ")
           .append("          ?s2 ?p2 <http://papyri.info/ddbdp> } ")
           .append("from <http://papyri.info/graph> ")
           .append("where {{ <http://papyri.info/ddbdp> ?Predicate ?Object } ")
           .append("union { ?s2 ?p2 <http://papyri.info/ddbdp> }} ")
           .append("order by ?Object");
        return out.toString();
      }
      String[] parts = in.split(";");
      if (parts.length == 1) {
        out.append("construct{<http://papyri.info/ddbdp/").append(parts[0]).append("> ?Predicate ?Object . ")
           .append("          ?s2 ?p2 <http://papyri.info/ddbdp/").append(parts[0]).append("> } ")
           .append("from <http://papyri.info/graph> ")
           .append("where {{ <http://papyri.info/ddbdp/").append(parts[0]).append("> ?Predicate ?Object } ")
           .append("union { ?s2 ?p2 <http://papyri.info/ddbdp/").append(parts[0]).append("> }} ")
           .append("order by ?Object");
        return out.toString();
      }
      if (parts.length == 2) {
        out.append("construct{<http://papyri.info/ddbdp/").append(parts[0])
           .append(";").append(parts[1]).append("> ?Predicate ?Object . ")
           .append("?s2 ?p2 <http://papyri.info/ddbdp/").append(parts[0])
           .append(";").append(parts[1]).append("> } ")   
           .append("from <http://papyri.info/graph> ")
           .append("where {{ <http://papyri.info/ddbdp/")
           .append(parts[0]).append(";").append(parts[1]).append("> ?Predicate ?Object } ")
           .append("union { ?s2 ?p2 <http://papyri.info/ddbdp/")
           .append(parts[0]).append(";").append(parts[1]).append("> }} ")
           .append("order by ?Object");
        return out.toString();
      }
      if (parts.length == 3) {
        if (parts[2].contains("/")) {
          // Handle /work, etc.
          String[] subparts = parts[2].split("/");
          subparts[0] = encode(subparts[0]);
          StringBuilder part = new StringBuilder();
          for (int i = 0; i < subparts.length; i++) {
            part.append(subparts[i]);
            if (i < subparts.length - 1) {
              part.append("/");
            }
          }
          out.append("construct{<http://papyri.info/ddbdp/").append(parts[0])
             .append(";").append(parts[1]).append(";").append(part).append("> ?Predicate ?Object . ")
             .append("          ?s2 ?p2 <http://papyri.info/ddbdp/").append(parts[0])
             .append(";").append(parts[1]).append(";").append(part).append("> } ")   
             .append("from <http://papyri.info/graph> ")
             .append("where {{ <http://papyri.info/ddbdp/")
             .append(parts[0]).append(";").append(parts[1]).append(";").append(part).append("> ?Predicate ?Object } ")
             .append("union { ?s2 ?p2 <http://papyri.info/ddbdp/")
             .append(parts[0]).append(";").append(parts[1]).append(";").append(part).append("> }} ")
             .append("order by ?Object");
          return out.toString();
        } else {
          parts[2] = encode(parts[2]);
          out.append("construct{<http://papyri.info/ddbdp/").append(parts[0])
             .append(";").append(parts[1]).append(";").append(parts[2]).append("> ?Predicate ?Object . ")
             .append("          ?s2 ?p2 <http://papyri.info/ddbdp/").append(parts[0])
             .append(";").append(parts[1]).append(";").append(parts[2]).append("> } ")   
             .append("from <http://papyri.info/graph> ")
             .append("where {{ <http://papyri.info/ddbdp/")
             .append(parts[0]).append(";").append(parts[1]).append(";").append(parts[2]).append("> ?Predicate ?Object } ")
             .append("union { ?s2 ?p2 <http://papyri.info/ddbdp/")
             .append(parts[0]).append(";").append(parts[1]).append(";").append(parts[2]).append("> }} ")
             .append("order by ?Object");
          return out.toString(); 
        }
         
      }
      return in;
    }
    
    protected String apis(String in) {
      StringBuilder out = new StringBuilder();
      appendPrefixes(out);
      if ("".equals(in) || in == null) {
        out.append("construct{<http://papyri.info/apis> ?Predicate ?Object . ")
           .append("          ?s2 ?p2 <http://papyri.info/apis> } ")
           .append("from <http://papyri.info/graph> ")
           .append("where {{ <http://papyri.info/apis> ?Predicate ?Object} ")
           .append("union { ?s2 ?p2 <http://papyri.info/apis> }} ")
           .append("order by ?Object");
        return out.toString();
      }
      if (!in.contains(".")) {
        out.append("construct{<http://papyri.info/apis/").append(in).append("> ?Predicate ?Object . ")
           .append("          ?s2 ?p2 <http://papyri.info/apis/").append(in).append("> } ")
           .append("from <http://papyri.info/graph> ")
           .append("where {{ <http://papyri.info/apis/").append(in).append("> ?Predicate ?Object} ")
           .append("union { ?s2 ?p2 <http://papyri.info/apis/").append(in).append("> }} ")
           .append("order by ?Object");
        return out.toString();
      }
      out.append("construct{<http://papyri.info/apis/").append(in).append("> ?Predicate ?Object . ")
         .append("          ?s2 ?p2 <http://papyri.info/apis/").append(in).append("> } ")
         .append("from <http://papyri.info/graph> ")
         .append("where {{ <http://papyri.info/apis/").append(in).append("> ?Predicate ?Object} ")
         .append("union { ?s2 ?p2 <http://papyri.info/apis/").append(in).append("> }} ")
         .append("order by ?Object");
      return out.toString();
    }
    
    protected String hgv(String in) {
      StringBuilder out = new StringBuilder();
      appendPrefixes(out);
      if ("".equals(in) || in == null) {
        out.append("construct{<http://papyri.info/hgv> ?Predicate ?Object . ")
           .append("          ?s2 ?p2 <http://papyri.info/hgv> } ")
           .append("from <http://papyri.info/graph> ")
           .append("where {{ <http://papyri.info/hgv> ?Predicate ?Object} ")
           .append("union { ?s2 ?p2 <http://papyri.info/hgv> }} ")
           .append("order by ?Object");
        return out.toString();
      }
      if (in.matches("\\d+[a-z]*")) {
        out.append("construct{<http://papyri.info/hgv/").append(in).append("> ?Predicate ?Object . ")
           .append("          ?s2 ?p2 <http://papyri.info/hgv/").append(in).append("> } ")
           .append("from <http://papyri.info/graph> ")
           .append("where {{ <http://papyri.info/hgv/").append(in).append("> ?Predicate ?Object} ")
           .append("union { ?s2 ?p2 <http://papyri.info/hgv/").append(in).append("> }} ")
           .append("order by ?Object");
        return out.toString();
      }
      out.append("construct{<http://papyri.info/hgv/").append(in).append("> ?Predicate ?Object . ")
         .append("          ?s2 ?p2 <http://papyri.info/hgv/").append(in).append("> } ")
         .append("from <http://papyri.info/graph> ")
         .append("where {{ <http://papyri.info/hgv/").append(in).append("> ?Predicate ?Object} ")
         .append("union { ?s2 ?p2 <http://papyri.info/hgv/").append(in).append("> }} ")
         .append("order by ?Object");
      return out.toString();
    }

    protected String query(String domain, String in) {
      StringBuilder out = new StringBuilder();
      appendPrefixes(out);
      out.append("construct{<http://papyri.info/").append(domain).append("/").append(in).append("> ?Predicate ?Object . ")
          .append("          ?s2 ?p2 <http://papyri.info/").append(domain).append("/").append(in).append("> } ")
          .append("from <http://papyri.info/graph> ")
          .append("where {{ <http://papyri.info/").append(domain).append("/").append(in).append("> ?Predicate ?Object} ")
          .append("union { ?s2 ?p2 <http://papyri.info/").append(domain).append("/").append(in).append("> }} ")
          .append("order by ?Object");
      return out.toString();
    }
    
    protected String annotation(String in) {
      StringBuilder out = new StringBuilder();
      appendPrefixes(out);
      out.append("construct{<http://papyri.info/").append(in).append("> ?Predicate ?Object . ")
          .append("          ?s2 ?p2 <http://papyri.info/").append(in).append("> } ")
          .append("from <http://papyri.info/graph> ")
          .append("where {{ <http://papyri.info/").append(in).append("> ?Predicate ?Object} ")
          .append("union { ?s2 ?p2 <http://papyri.info/").append(in).append("> }} ")
          .append("order by ?Object");
      return out.toString();
    }

    protected static String encode(String in) {
      try {
        String result = URLEncoder.encode(in, "UTF-8");
        return result.replaceAll("\\+", "%2B");
      } catch (Exception e) {
        logger.warn("Error encoding '" + in + "'", e);
        return in;
      }
    }

}
