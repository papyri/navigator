/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import javax.servlet.ServletConfig;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.client.solrj.SolrRequest.METHOD;

/**
 *
 * @author hcayless
 */
public class BiblioSearch extends HttpServlet {

  private String solrUrl;
  private URL searchURL;
  private String xmlPath = "";
  private String htmlPath = "";
  private String home = "";
  private FileUtils util;
  private SolrUtils solrutil;
  private static String BiblioSearch = "biblio_search/";

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    solrUrl = config.getInitParameter("solrUrl");
    xmlPath = config.getInitParameter("xmlPath");
    htmlPath = config.getInitParameter("htmlPath");
    home = config.getInitParameter("home");
    util = new FileUtils(xmlPath, htmlPath);
    solrutil = new SolrUtils(config);
    try {
      searchURL = new URL("file://" + home + "/" + "bibliosearch.html");
    } catch (MalformedURLException e) {
      throw new ServletException(e);
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
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    BufferedReader reader = null;
    try {
      String q = request.getParameter("q");
      reader = new BufferedReader(new InputStreamReader(searchURL.openStream()));
      String line = "";
      while ((line = reader.readLine()) != null) {
        if (line.contains("<!-- Results -->") && !("".equals(q) || q == null)) {
          SolrClient solr = new HttpSolrClient.Builder(solrUrl + BiblioSearch).build();
          int rows = 30;
          try {
            rows = Integer.parseInt(request.getParameter("rows"));
          } catch (Exception e) {
          }
          int start = 0;
          try {
            start = Integer.parseInt(request.getParameter("start"));
          } catch (Exception e) {}
          SolrQuery sq = new SolrQuery();
          try {
            sq.setQuery(q.toLowerCase());
            sq.setStart(start);
            sq.setRows(rows);
            sq.addSort("date", SolrQuery.ORDER.asc);
            sq.addSort("sort", SolrQuery.ORDER.asc);
            QueryRequest req = new QueryRequest(sq);
            req.setMethod(METHOD.POST);
            QueryResponse rs = req.process(solr);
            SolrDocumentList docs = rs.getResults();
            out.println("<p>" + docs.getNumFound() + " hits on \"" + q.toString() + "\".</p>");
            out.println("<table>");
            String uq = q;
            try {
              uq = URLEncoder.encode(q, "UTF-8");
            } catch (Exception e) {
            }
            for (SolrDocument doc : docs) {
              StringBuilder row = new StringBuilder("<tr class=\"result-record\"><td>");
              row.append("<a href=\"");
              row.append("/biblio/");
              row.append(((String) doc.getFieldValue("id")));
              row.append("/?q=");
              row.append(uq);
              row.append("\">");
              row.append(doc.getFieldValue("display"));
              row.append("</a>");
              row.append("</td>");
              row.append("</tr>");
              out.print(row);
            }
            out.println("</table>");
            if (docs.getNumFound() > rows) {
              out.println("<div id=\"pagination\">");
              int pages = (int) Math.ceil((double) docs.getNumFound() / (double) rows);
              int p = 0;
              while (p < pages) {
                if ((p * rows) == start) {
                  out.print("<div class=\"page current\">");
                  out.print((p + 1) + " ");
                  out.print("</div>");
                } else {
                  StringBuilder plink = new StringBuilder(uq + "&start=" + p * rows + "&rows=" + rows);
                  out.print("<div class=\"page\"><a href=\"/bibliosearch?q=" + plink + "\">" + (p + 1) + "</a></div>");
                }
                p++;
              }
              out.println("</div>");
            }
          } catch (SolrServerException e) {
            out.println("<p>Unable to execute query.  Please try again.</p>");
            throw new ServletException(e);
          }
        } else {
          out.println(line);
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
