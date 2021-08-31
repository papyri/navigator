/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;


import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 *
 * @author hcayless
 */
@WebServlet(name = "MDReader", urlPatterns = {"/docs"})
public class AuthorBrowse extends HttpServlet {

  private static String TEMPLATE;
  private static String solrUrl;
  private static Logger logger = Logger.getLogger("pn-dispatch");

  @Override
  public void init(ServletConfig config) {
    TEMPLATE = config.getInitParameter("template");
    solrUrl = config.getInitParameter("solrUrl");
    logger.info("Template: " + TEMPLATE);
    logger.info("Solr URL: " + solrUrl);
  }

  /**

   * 
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
      
      //Top-level browse to query everything, get back author_str facets
      //selecting one is a search on author_str:"<selected author>" gets back author_work facets
      //filter those for the current author and display, selecting one gives you
    response.setContentType("text/html;charset=UTF-8");
    logger.info("Author Browsing!");
    SolrClient solr = new HttpSolrClient.Builder(solrUrl).build();
    SolrQuery sq = new SolrQuery();
    sq.add("q", "*:*");
    sq.addFacetField("author_work");
    sq.setFacetLimit(-1);
    List<Count> authors;
    try {
        QueryResponse qr = solr.query(sq, SolrRequest.METHOD.GET);
        authors = qr.getFacetField("author_work").getValues();
    } catch (SolrServerException sse) {
        logger.log(Level.SEVERE, "Unable to execute query.", sse);
        authors = new ArrayList<>();
    }

    PrintWriter out = response.getWriter();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(new File(TEMPLATE)));
      String line;
      while ((line = reader.readLine()) != null) {
        out.println(line);
        if (line.contains("<div class=\"browse\">")) {
          out.println("<ul>");
          String initial = "";
          String auth = "";
          boolean closeInitial = false;
          boolean closeAuthor = false;
          boolean worksOpen = false;
          Collections.sort(authors, new Comparator() {
              @Override
              public int compare(Object o1, Object o2) {
                  return ((Count)o1).getName().trim().compareTo(((Count)o2).getName().trim());
              }
              
          });
          for (Count author : authors) {
              String name = author.getName();
              
              //Initial Link
              if (!initial.equals(name.substring(0,1))) {
                initial = name.substring(0,1);
                if (worksOpen) {
                    out.print("</ul>");
                    worksOpen = false;
                }
                if (closeAuthor) {
                    out.print("</li></ul>");
                    closeAuthor = false;
                }
                if (closeInitial) {
                    out.println("</li>");
                    closeInitial = false;
                }
                out.print("<li><a name=\"");
                out.print(initial);
                out.print("\" class=\"initial\">");
                out.print(initial);
                out.println("</a> <a class=\"top\" href=\"#\">^</a><ul class=\"authors\">");
                closeInitial = true;
              }
              //Author link
              if (!auth.equals(FileUtils.substringBefore(name, " // "))) {
                if (worksOpen) {
                    out.println("</ul>");
                    worksOpen = false;
                }
                if (closeAuthor) {
                    out.println("</li>");
                    closeAuthor = false;
                }
                auth = FileUtils.substringBefore(name, " // ");
                out.print("<li><a href=\"/author/");
                out.print(URLEncoder.encode(FileUtils.substringBefore(name, " // "), "UTF-8"));
                out.print("\">");
                out.print(FileUtils.substringBefore(name, " // "));
                out.print("</a>");
                if (name.contains(" // ")) {
                    out.println("<ul class=\"works\">");
                    worksOpen = true;
                }
                closeAuthor = true;
              }
              //Work Link
              if (name.contains(" // ")) {
                if (!worksOpen) {
                    out.print("<ul class=\"works\">");
                    worksOpen = true;
                }
                out.print("<li><a href=\"/author/");
                out.print(URLEncoder.encode(FileUtils.substringBefore(name, " // "), "UTF-8"));
                out.print("/");
                out.print(URLEncoder.encode(FileUtils.substringAfter(name, " // "), "UTF-8"));
                out.print("\">");
                out.print(FileUtils.substringAfter(name, " // "));
                out.print("</a></li>");
              }
              
          }
          if (worksOpen) {
              out.println("</ul>");
          }
          if (closeAuthor) {
             out.println("</li></ul>");
          }
          out.println("</li></ul>");
          reader.readLine(); // assume template has a throwaway line inside the content div
        }
      }
    } catch (IOException e) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } finally {
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
