/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.unc.epidoc.transcoder.TransCoder;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * 
 * @author hcayless
 */
public class Search extends HttpServlet {

  private SolrServer solr;
  private URL searchURL;
  private String xmlPath = "";
  private String htmlPath = "";
  private String home = "";
  private FileUtils util;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    xmlPath = config.getInitParameter("xmlPath");
    htmlPath = config.getInitParameter("htmlPath");
    home = config.getInitParameter("home");
    util = new FileUtils(xmlPath, htmlPath);
    try {
      solr = new CommonsHttpSolrServer(config.getInitParameter("solrUrl"));
      searchURL = new URL("file://" + home + "/" + "search.html");
    } catch (MalformedURLException e) {
      throw new ServletException(e);
    }

  }

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
   * methods.
   *
   * @param request
   *            servlet request
   * @param response
   *            servlet response
   * @throws ServletException
   *             if a servlet-specific error occurs
   * @throws IOException
   *             if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request,
          HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    try {
      if (request.getParameter("keyword") != null || request.getParameter("q") != null) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(searchURL.openStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
          if (line.contains("<!-- Search form -->")) {
            out.println("<div id=\"hidesearch\" style=\"display:none\">");
          }
          if (line.contains("<!-- Search form end -->")) {
            out.println("</div>");
            out.println("<p><a href=\"#\" class=\"button\" onclick=\"$('#hidesearch').css('display:block')\">New Search</a></p>");
            runQuery(out, request, response);
          }
          out.println(line);
        }
        reader.close();

      } else {
        BufferedReader reader = new BufferedReader(new InputStreamReader(searchURL.openStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
          out.println(line);
        }
        reader.close();
      }

      // q=transcription_ngram_ia:και+\^υπο&version=2.2&start=0&rows=10&indent=on&wt=json
      // mode: ngram | phrase

      /*
       * TODO output your page here out.println("<html>");
       * out.println("<head>");
       * out.println("<title>Servlet Search</title>");
       * out.println("</head>"); out.println("<body>");
       * out.println("<h1>Servlet Search at " + request.getContextPath ()
       * + "</h1>"); out.println("</body>"); out.println("</html>");
       */
    } catch (SolrServerException e) {
      // TODO Auto-generated catch block
      throw new IOException(e);
    } finally {
      out.close();
    }
  }

  private void runQuery(PrintWriter out, HttpServletRequest request, HttpServletResponse response) throws SolrServerException, ServletException {
    String q = request.getParameter("q");
    if (q == null) {
      String query = request.getParameter("keyword");
      if ("".equals(query)) query = null;

      if (query != null) {
        if ("on".equals(request.getParameter("beta"))) {
          try {
            TransCoder tc = new TransCoder("BetaCode", "UnicodeC");
            query = tc.getString(query);
            query = query.replace("ΑΝΔ", "AND").replace("ΟΡ", "OR").replace("ΝΟΤ", "NOT");
          } catch (Exception e) {
            throw new ServletException(e);
          }
        }
      }
      String field = null;
      if (query != null) {
        // assume that if the query string contains ":", the query specifies a field already,
        // so don't attempt to determine which one to target
        if (!query.contains(":")) {
          // substring queries can target only the transcription_ngram_ia field "^" is only
          // used as a word boundary marker and so is a clear indicator of a substring search
          if ("substring".equals(request.getParameter("type")) || query.contains("^")) {
              field = "transcription_ngram_ia";
              query = query.replace("^", "\\^");
          } else if ("text".equals(request.getParameter("target"))) {
            field = "transcription";
            if ("proximity".equals(request.getParameter("type"))) {
              query = "\"" + query + "\"~" + request.getParameter("within");
            }
            if ("on".equals(request.getParameter("caps")) && "on".equals(request.getParameter("marks"))) {
              field += "_ia";
            } else if ("on".equals(request.getParameter("caps"))) {
              field += "_ic";
            } else if ("on".equals(request.getParameter("marks"))) {
              field += "_id";
            }
            if ("on".equals(request.getParameter("lemmas"))) {
              field = "transcription_l";
            }
          } else if ("metadata".equals(request.getParameter("target"))) {
            field = "metadata";
          } else if ("translation".equals(request.getParameter("target"))) {
            field = "translation";
          }
        }
      }
      if (field != null) {
        q = field + ":(" + query + ")";
      } else {
        q = query;
      }
      String param;
      if ((param = request.getParameter("provenance")) != null && !"".equals(param)) {
        if (q == null) {
          q = "place:" + param;
        } else {
          q += " AND place:" + param;
        }
      }
      String ds = request.getParameter("date_start");
      if ("".equals(ds)) ds = null;
      String de = request.getParameter("date_end");
      if ("".equals(de)) de = null;
      String qds = ds == null ? "*" : ds;
      String qde = de == null ? "*" : de;
      if (ds != null || de != null) {
        if (q == null) {
          q = "date_start:[" + qds + " TO " + qde + "] AND date_end:[" + qds + " TO " + qde + "]";
        } else {
          q += " AND date_start:[" + qds + " TO " + qde + "] AND date_end:[" + qds + " TO " + qde + "]";
        }
      }
      String invnum = request.getParameter("invnum");
      if (invnum != null && !"".equals(invnum)) {
        if (q == null) {
          q = "invnum:(" + invnum + ")";
        } else {
          q += " AND invnum:(" + invnum + ")";
        }
      }

    }
    SolrQuery sq = new SolrQuery();
    int start = 0;
    try {
      start = Integer.parseInt(request.getParameter("start"));
    } catch (Exception e) {
    }
    sq.setStart(start);
    int rows = 20;
    try {
      rows = Integer.parseInt(request.getParameter("rows"));
    } catch (Exception e) {
    }
    String sort = request.getParameter("sort");
    if (sort == null || "".equals(sort)) {
      sq.addSortField("series", SolrQuery.ORDER.asc);
      sq.addSortField("volume", SolrQuery.ORDER.asc);
      sq.addSortField("item", SolrQuery.ORDER.asc);
    } else {
      sq.setSortField(sort, SolrQuery.ORDER.desc);
    }
    sq.setRows(rows);
    sq.setQuery(q);
    
    QueryResponse rs = solr.query(sq);
    SolrDocumentList docs = rs.getResults();
    out.println("<p>" + docs.getNumFound() + " hits.</p>");
    out.println("<ul class=\"results\">");
    for (SolrDocument doc : docs) {
      out.print("<li><a href=\"" + ((String)doc.getFieldValue("id")).substring(18) + "/\">"
              + doc.getFieldValue("id") + "</a><br>");
      for (String line : util.highlightMatches(q, util.loadTextFromId((String)doc.getFieldValue("id")))) {
        out.print(line + "<br>\n");
      }
      out.println("</li>");
    }
    out.println("</ul>");
    if (docs.getNumFound() > rows) {
      out.println("<p id=\"resultpages\">");
      int pages = (int) Math.ceil((double)docs.getNumFound() / (double)rows);
      int p = 0;
      try {
        q = URLEncoder.encode(q, "UTF-8");
      } catch (Exception e) {}
      while (p < pages) {
        if ((p * rows) == start) {
          out.print((p + 1) + " ");
        } else {
          out.print("<a href=\"/search?q=" + q + "&start=" + p * rows + "&rows=" + rows + "\">" + (p + 1) + "</a> ");
        }
        p++;
      }
      out.println("</p>");
    }
  }


  // <editor-fold defaultstate="collapsed"
  // desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request
   *            servlet request
   * @param response
   *            servlet response
   * @throws ServletException
   *             if a servlet-specific error occurs
   * @throws IOException
   *             if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request,
          HttpServletResponse response) throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request
   *            servlet request
   * @param response
   *            servlet response
   * @throws ServletException
   *             if a servlet-specific error occurs
   * @throws IOException
   *             if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request,
          HttpServletResponse response) throws ServletException, IOException {
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
