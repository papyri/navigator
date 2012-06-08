package info.papyri.dispatch;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonNode;

/**
 *
 * @author hcayless
 */
@WebServlet(name = "Reader", urlPatterns = {"/reader"})
public class Reader extends HttpServlet {
  private static String graph = "http://papyri.info/graph";
  private static String path = "/pi/query";
  private String sparqlServer;
  private String xmlPath = "";
  private String htmlPath = "";
  private FileUtils util;
  private SolrUtils solrutil;
  private byte[] buffer = new byte[8192];

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    xmlPath = config.getInitParameter("xmlPath");
    htmlPath = config.getInitParameter("htmlPath");
    util = new FileUtils(xmlPath, htmlPath);
    solrutil = new SolrUtils(config);
    sparqlServer = config.getInitParameter("sparqlUrl");
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
    String page = request.getParameter("p");
    if (page != null) {
      // Redirection for old static URLs
      if (page.contains("current") && (page.contains("-citations-") || page.contains("index.html"))) {
        response.sendError(HttpServletResponse.SC_GONE);
      } else if (page.endsWith(".html")) {
        if (page.contains("ddb/html") || page.contains("aggrega     ted/html")) {
          response.setHeader("Location", FileUtils.rewriteOldUrl(page));
          response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        } else if (page.contains("hgvmeta")) {
          response.setHeader("Location", page.replaceAll("^[/a-z]+/HGV\\d+/([0-9]+[a-z]*).html$", "http://papyri.info/hgv/$1"));
          response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        }
      } else if (page.contains("/")) {
        String collection = FileUtils.substringBefore(page, "/");
        String item = FileUtils.substringAfter(page, "/").replaceAll("/$", "");
        File file = null;
        if (item.endsWith("/source")) {
          response.setContentType("application/xml;charset=UTF-8");
          file = util.getXmlFile(collection, item.replace("/source", ""));
          if (file != null && !file.exists()) { //use triple store to resolve to source file
            file = resolveFile("http://papyri.info/" + collection + "/" + item + "/source", "Xml");
          }
        } else if (page.endsWith("text")) {
          response.setContentType("text/plain;charset=UTF-8");
          file = util.getTextFile(collection, item.replace("/text", ""));
          if (file != null && !file.exists()) { //use triple store to resolve to source file
            file = resolveFile("http://papyri.info/" + collection + "/" + item + "/source", "Text");
          }
        } else {
          response.setContentType("text/html;charset=UTF-8");
          file = util.getHtmlFile(collection, item);
          if (file != null && !file.exists()) { //use triple store to resolve to source file
            file = resolveFile("http://papyri.info/" + collection + "/" + item + "/source", "Html");
          }
        }
        if (file == null) {
          response.sendError(response.SC_NOT_FOUND);
        } else {
          if (request.getParameter("q") != null) {
            sendWithHighlight(response, file, request.getParameter("q"));
          } else {
            send(response, file);
          }
        }
      }
    } else {
      response.sendError(response.SC_NOT_FOUND);
    }
  }

  private void send(HttpServletResponse response, File f)
          throws ServletException, IOException {
    FileInputStream reader = null;
    OutputStream out = response.getOutputStream();
    if (f != null && f.exists()) {
      try {
        reader = new FileInputStream(f);
        int size = reader.read(buffer);
        while (size > 0) { 
          out.write(buffer, 0, size);
          size = reader.read(buffer);
        }
      } catch (IOException e) {
        response.sendError(response.SC_NOT_FOUND);
        System.out.println("Failed to send " + f);
      } finally {
        reader.close();
        out.close();
      }
    } else {
      response.sendError(response.SC_NOT_FOUND);
    }
  }

  private void sendWithHighlight(HttpServletResponse response, File f, String q)
    throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    q = URLDecoder.decode(q,"UTF-8");
    if (q.contains("transcription_l")) {
      try {
        StringBuilder query = new StringBuilder();
        query.append(FileUtils.substringBefore(q, "transcription_l", false));
        query.append("transcription_ia:(");
        query.append(solrutil.expandLemmas(FileUtils.substringBefore(FileUtils.substringAfter(q, "transcription_l:(", false), ")", false)));
        query.append(")");
        query.append(FileUtils.substringAfter(FileUtils.substringAfter(q, "transcription_l:(", false), ")", false));
        q = query.toString();
        System.out.println(q);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (f != null && f.exists()) {
      try {
        Pattern[] patterns = util.buildPatterns(q);
        out.write(util.highlight(patterns, util.loadFile(f)));
      } catch (Exception e) {
        e.printStackTrace(System.out);
      } finally {
        out.close();
      }
    } else {
      response.sendError(response.SC_NOT_FOUND);
    }
  }

  private File resolveFile(String page, String type) {
    File result = null;
    StringBuilder sparql = new StringBuilder();
    sparql.append("prefix dc: <http://purl.org/dc/terms/> ");
    sparql.append("select ?related ");
    sparql.append("from <");
    sparql.append(graph);
    sparql.append("> where { <" + page +"> dc:relation ?related . ");
    sparql.append("optional { ?related dc:isReplacedBy ?orig } . ");
    sparql.append("filter (!bound(?orig)) . ");
    sparql.append("filter regex(str(?related), \"^http://papyri.info/(ddbdp|hgv)\") }");
    try {
      URL m = new URL(sparqlServer + path + "?query=" + URLEncoder.encode(sparql.toString(), "UTF-8") + "&format=json");
      HttpURLConnection http = (HttpURLConnection)m.openConnection();
      http.setConnectTimeout(2000);
      ObjectMapper o = new ObjectMapper();
      JsonNode root = o.readValue(http.getInputStream(), JsonNode.class);
      Iterator<JsonNode> i = root.path("results").path("bindings").iterator();
      String uri = "";
      while (i.hasNext()) {
        uri = FileUtils.substringBefore(i.next().path("related").path("value").getValueAsText(), "/source");
        if (uri.contains("ddbdp/")) {
          result = (File)util.getClass().getMethod("get"+type+"FileFromId", String.class).invoke(util, URLDecoder.decode(uri, "UTF-8"));
        }
        if (uri.contains("hgv/")) {
          result = (File)util.getClass().getMethod("get"+type+"FileFromId", String.class).invoke(util, URLDecoder.decode(uri, "UTF-8"));
        }
        if (result.exists()) break;
      }
      
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(sparql);
      return null;
    }
    return result;
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
