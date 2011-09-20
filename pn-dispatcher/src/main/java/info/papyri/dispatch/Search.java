package info.papyri.dispatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import info.papyri.dispatch.browse.SolrField;
import org.apache.solr.client.solrj.SolrRequest.METHOD;

/**
 * 
 * @author hcayless
 */
public class Search extends HttpServlet {

  private String solrUrl;
  private URL searchURL;
  private String xmlPath = "";
  private String htmlPath = "";
  private String home = "";
  private FileUtils util;
  private SolrUtils solrutil;
  private static String PNSearch = "pn-search/";
  private static String morphSearch = "morph-search/";

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
    BufferedReader reader = null;
    try {
      if (request.getParameter("keyword") != null || request.getParameter("q") != null) {
        reader = new BufferedReader(new InputStreamReader(searchURL.openStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
          if (line.contains("<!-- Search form -->")) {
            out.println("<div id=\"hidesearch\" style=\"display:none\">");
          }
          if (line.contains("<!-- Search form end -->")) {
            out.println("</div>");
            out.println("<p><a href=\"#\" id=\"newsearch\">New Search</a></p>");
            runQuery(out, request, response);
          }
          out.println(line);
        }
      } else {
        reader = new BufferedReader(new InputStreamReader(searchURL.openStream()));
        char[] cbuf = new char[8192];
        int l = -1;
        while ((l = reader.read(cbuf)) > 0) {
          out.write(cbuf, 0, l);
        }
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
    } catch (MalformedURLException mue) {
      throw new ServletException(mue);
    } finally {
      out.close();
      if (reader != null) reader.close();
    }
  }

  private void runQuery(PrintWriter out, HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, SolrServerException, ServletException {
    SolrServer solr = new CommonsHttpSolrServer(solrUrl + PNSearch);
    String q = request.getParameter("q");
    if (q == null) {
      String query = request.getParameter("keyword");
      if ("".equals(query)) query = null;

      if (query != null) {
        if ("on".equals(request.getParameter("beta"))) {
          try {
            TransCoder tc = new TransCoder("BetaCodeCaps", "UnicodeC");
            query = tc.getString(query);
            query = query.replace("ΑΝΔ", "AND").replace("ΟΡ", "OR").replace("ΝΟΤ", "NOT");
          } catch (Exception e) {
            throw new ServletException(e);
          }
        }
      }
      boolean lemmaSearch = false;
      String field = null;
      if (query != null) {
        // assume that if the query string contains ":", the query specifies a field already,
        // so don't attempt to determine which one to target
        if (!query.contains(":")) {
          // substring queries can target only the transcription_ngram_ia field "^" is only
          // used as a word boundary marker and so is a clear indicator of a substring search
          if ("substring".equals(request.getParameter("type")) || query.contains("^")) {
              field = "transcription_ngram_ia";
              if ("on".equals(request.getParameter("caps"))) {
                if (query.contains("NOT") || query.contains("AND") || query.contains("OR")) {
                  query = query.toLowerCase();
                  query = query.replaceAll("\\bnot\\b", "NOT").replaceAll("\\band\\b", "AND").replaceAll("\\bor\\b", "OR");
                } else {
                  query = query.toLowerCase();
                }
              }
              if ("on".equals(request.getParameter("marks"))) {
                query = FileUtils.stripDiacriticals(query);
              }
              query = query.replace("^", "\\^");
          } else if ("text".equals(request.getParameter("target"))) {
            field = "transcription";
            if ("proximity".equals(request.getParameter("type"))) {
              query = "\"" + query + "\"~" + request.getParameter("within");
            }
            if ("on".equals(request.getParameter("caps")) && "on".equals(request.getParameter("marks"))) {
              field += "_ia";
              query = FileUtils.stripDiacriticals(query).toLowerCase();
            } else if ("on".equals(request.getParameter("caps"))) {
              field += "_ic";
              query = query.toLowerCase();
            } else if ("on".equals(request.getParameter("marks"))) {
              field += "_id";
              query = FileUtils.stripDiacriticals(query);
            }
          } else if ("on".equals(request.getParameter("lemmas"))) {
            field = "transcription_l";
          } else if ("metadata".equals(request.getParameter("target"))) {
            field = "metadata";
          } else if ("translation".equals(request.getParameter("target"))) {
            field = "translation";
          }
        }
      }

      if (field != null) {
        q = field + ":(" + query + ")";
      } 
      String param;
      if ((param = request.getParameter("provenance")) != null && !"".equals(param)) {
        param = param.toLowerCase();
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
      if (!"*".equals(qds) && "bce".equals(request.getParameter("start_era"))) {
        qds = "-" + qds;
      }
      if (!"*".equals(qde) && "bce".equals(request.getParameter("end_era"))) {
        qde = "-" + qde;
      }
      if (ds != null || de != null) {
        if (q == null) {
          q = "(date_start:[" + qds + " TO " + qde + "] OR date_end:[" + qds + " TO " + qde + "])";
        } else {
          q += " AND (date_start:[" + qds + " TO " + qde + "] OR date_end:[" + qds + " TO " + qde + "])";
        }
      }
      if ((param = request.getParameter("ddbseries")) != null && !"".equals(param)) {
        if (request.getParameter("volume") != null && !"".equals(request.getParameter("volume"))) {
          param += ";" + request.getParameter("volume");
        }
        if (q == null) {
          q = "identifier:http\\://papyri.info/ddbdp/" + param + "*";
        } else {
          q += " AND identifier:http\\://papyri.info/ddbdp/" + param + "*";
        }
      }
      if ((param = request.getParameter("hgvseries")) != null && !"".equals(param)) {
        if (q == null) {
          q = "identifier:http\\://papyri.info/hgv/" + param + "*";
        } else {
          q += " AND identifier:http\\://papyri.info/hgv/" + param + "*";
        }
      }
      if ((param = request.getParameter("volume")) != null && !"".equals(param)) {
        if (q == null) {
          q = "volume:" + param;
        } else {
          q += " AND volume:" + param;
        }
      }
      if ((param = request.getParameter("apiscol")) != null && !"".equals(param)) {
        if (q == null) {
          q = "identifier:http\\://papyri.info/apis/" + param + "*";
        } else {
          q += " AND identifier:http\\://papyri.info/apis/" + param + "*";
        }
      }
      if ((param = request.getParameter("invnum")) != null && !"".equals(param)) {
        if (q == null) {
          q = "invnum:(" + param + ")";
        } else {
          q += " AND invnum:(" + param + ")";
        }
      }
      if (q == null && query == null) {
        out.println("<p>Empty search.</p>");
        return;
      }
      if (q == null) q= query;

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
      if ("yes".equals(request.getParameter("imagesfirst"))) {
        sq.addSortField("images", SolrQuery.ORDER.desc);
      }
      if ("yes".equals(request.getParameter("translationssfirst"))) {
        sq.addSortField("has_translation", SolrQuery.ORDER.desc);
      }
      sq.addSortField("series", SolrQuery.ORDER.asc);
      sq.addSortField("volume", SolrQuery.ORDER.asc);
      sq.addSortField("item", SolrQuery.ORDER.asc);
    } else {
      sq.setSortField(sort, SolrQuery.ORDER.desc);
    }
    sq.setRows(rows);
    if (q != null && q.contains("transcription_l")) {
      StringBuilder query = new StringBuilder();
      query.append(FileUtils.substringBefore(q, "transcription_l", false));
      query.append("transcription_ia:(");
      query.append(solrutil.expandLemmas(FileUtils.substringBefore(FileUtils.substringAfter(q, "transcription_l:(", false), ")", false)));
      query.append(")");
      query.append(FileUtils.substringAfter(FileUtils.substringAfter(q, "transcription_l:(", false), ")", false));
      sq.setQuery(query.toString().replace("ς", "σ"));
    } else {
      sq.setQuery(q.replace("ς", "σ"));
    }
    try {
      QueryRequest req = new QueryRequest(sq);
      req.setMethod(METHOD.POST);
      QueryResponse rs = req.process(solr);
      SolrDocumentList docs = rs.getResults();
      out.println(q.toString());
      out.println("<p>" + docs.getNumFound() + " hits.</p>");
      out.println("<table>");
      out.println("<tr class=\"tablehead\"><td>Identifier</td><td>Location</td><td>Date</td><td>Languages</td><td>Translation</td><td>Images</td></tr>");
      String uq = q;
      try {
        uq = URLEncoder.encode(q, "UTF-8");
      } catch (Exception e) {}
      for (SolrDocument doc : docs) {
        Object[] translations = (doc.getFieldValues(SolrField.translations.name()) == null ? new Object[0] : doc.getFieldValues(SolrField.has_translation.name()).toArray());
        boolean hasImages = doc.getFieldValuesMap().containsKey(SolrField.images.name()) && (Boolean)doc.getFieldValue(SolrField.images.name()) ? true : false;
        boolean languageIsNull = doc.getFieldValue(SolrField.language.name()) == null;
        String language = languageIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.language.name()).toString().replaceAll("[\\[\\]]", "");
        StringBuilder row = new StringBuilder("<tr class=\"result-record\"><td class=\"identifier\">");
        row.append("<a href=\"");
        row.append(((String)doc.getFieldValue("id")).substring(18));
        row.append("/?q=");
        row.append(uq);
        row.append("\">");
        row.append(util.substringAfter(((String)doc.getFieldValue("id")), "http://papyri.info/").replace("/", " - "));
        row.append("</a>");
        row.append("</td>");
        row.append("<td class=\"display-place\">");
        row.append(doc.getFieldValue("display_place"));
        row.append("</td>");
        row.append("<td class=\"display-date\">");
        row.append(doc.getFieldValue("display_date"));
        row.append("</td>");
        row.append("<td class=\"language\">");
        row.append(language);
        row.append("</td>");
        row.append("<td class=\"has-translation\">");
        if (translations.length == 0) {
          row.append("none");
        } else {
          for (int i = 0; i < translations.length; i++) {
            row.append(translations[i]);
            if (i < (translations.length - 1)) row.append(", ");
          } 
        }
        row.append("</td>");
        row.append("<td class=\"has-images\">");
        row.append(hasImages);
        row.append("</td>");
        row.append("</tr>");
        row.append("<tr class=\"result-text\"><td class=\"kwic\" colspan=\"6\">");
        for (String line : util.highlightMatches(sq.getQuery(), util.loadTextFromId((String)doc.getFieldValue("id")))) {
          row.append(line + "<br>\n");
        }
        row.append("</td></tr>");
        out.print(row);
      }
      out.println("</table>");
      if (docs.getNumFound() > rows) {
        out.println("<div id=\"pagination\">");
        int pages = (int) Math.ceil((double)docs.getNumFound() / (double)rows);
        int p = 0;
        while (p < pages) {
          if ((p * rows) == start) {
            out.print("<div class=\"page current\">");
            out.print((p + 1) + " ");
            out.print("</div>");
          } else {
            StringBuilder plink = new StringBuilder(uq + "&start=" + p * rows + "&rows=" + rows);
            if ("yes".equals(request.getParameter("imagesfirst"))) plink.append("&imagesfirst=yes");
            if ("yes".equals(request.getParameter("translationsfirst"))) plink.append("&translationssfirst=yes");
            out.print("<div class=\"page\"><a href=\"/search?q=" + plink + "\">" + (p + 1) + "</a></div>");
          }
          p++;
        }
        out.println("</div>");
      }
    } catch (SolrServerException e) {
      out.println("<p>Unable to execute query.  Please try again.</p>");
      throw e;
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
