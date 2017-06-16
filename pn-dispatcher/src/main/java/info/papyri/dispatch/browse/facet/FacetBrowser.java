package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.SolrUtils;
import info.papyri.dispatch.browse.DocumentBrowseRecord;
import info.papyri.dispatch.browse.IdComparator;
import info.papyri.dispatch.browse.SolrField;
import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchClause;
import info.papyri.dispatch.browse.facet.customexceptions.CustomApplicationException;
import info.papyri.dispatch.browse.facet.customexceptions.FacetNotFoundException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import info.papyri.dispatch.ServletUtils;
import org.apache.log4j.Logger;

@WebServlet(name = "FacetBrowser", urlPatterns = {"/search"})
/**
 * Enables faceted browsing of the pn collections
 *
 * @author thill
 */
public class FacetBrowser extends HttpServlet {

  /**
   * Solr server address. Supplied in config file
   */
  static String SOLR_URL;
  /**
   * Path to appropriate Solr core
   */
  static String PN_SEARCH;
  /**
   * path to home html directory
   */
  static private String home;
  /**
   * path to html file used in html injection
   */
  static private URL FACET_URL;
  /**
   * path to servlet
   */
  static private String FACET_PATH;
  /**
   * Default number of records to show per page
   */
  static private int defaultDocumentsPerPage = 15;
  /**
   * Utility class providing lemma expansion
   */
  static SolrUtils SOLR_UTIL;
  /**
   * Path to html instructions file
   */
  static String INSTRUCTIONS_PATH;
  static int SOCKET_TIMEOUT = 20000;
  
  private static Logger logger = Logger.getLogger("pn-dispatch");

  @Override
  public void init(ServletConfig config) throws ServletException {

    super.init(config);
    ServletUtils.setupLogging(config.getServletContext(), config.getInitParameter("log4j-properties-location"));
    SOLR_URL = config.getInitParameter("solrUrl");
    SOLR_UTIL = new SolrUtils(config);
    home = config.getInitParameter("home");
    PN_SEARCH = config.getInitParameter("pnSearchPath");
    FACET_PATH = config.getInitParameter("facetBrowserPath");
    INSTRUCTIONS_PATH = config.getInitParameter("instructionsPath");
    try {
      FACET_URL = new URL("file://" + home + "/" + "facetbrowse.html");
    } catch (MalformedURLException e) {
      throw new ServletException(e);
    }

  }

  /**
   * Processes requests for both HTTP
   * <code>GET</code> and
   * <code>POST</code> methods.
   *
   * Because this is the central coordinating method for the servlet, a
   * step-by-step explanation of the method calls is provided in the method
   * body.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

    /* Make sure both the request and the response are properly encoded */
    response.setContentType("text/html;charset=UTF-8");
    request.setCharacterEncoding("UTF-8");
    
    if (request.getQueryString() != null && request.getQueryString().toLowerCase().contains("%3cscript%3e")) {
        response.sendError(400, "Not today, Satan.");
        return;
    }

    /* Get the <code>List</code> of facets to be displayed */
    ArrayList<Facet> facets = getFacets();

    int docsPerPage = parseRequestForDocumentsPerPage(request);

    /* Parse request, allowing each facet to pull out and parse the part of the request
     * relevant to itself.
     */

    Boolean constraintsPresent = parseRequestToFacets(request, facets);

    /* determine what page of results has been requested ('0' if no page requested and
     * this is therefore the first page of results). 
     * 
     * Required for building the facet query.
     */
    int page = request.getParameter("page") != null ? Integer.valueOf(request.getParameter("page")) : 1;

    /* Build the SolrQuery object to be used in querying Solr out of query parts contributed 
     * by each of the facets in turn.
     */
    SolrQuery solrQuery = buildFacetQuery(page, facets, docsPerPage);
    solrQuery.add("project", "IDP");

    ArrayList<CustomApplicationException> exceptionLog = collectFacetExceptions(facets);

    /* Query the Solr server */
    QueryResponse queryResponse = runFacetQuery(solrQuery);

    /* Allow each facet to pull out the values relevant to it from the <code>QueryResponse</code>
     * returned by the Solr server.
     */

    if (queryResponse != null) {
      populateFacets(facets, queryResponse);
    } else {
      queryResponse = new QueryResponse();
    }

    /* Determine the number of results returned. 
     * 
     * Required for assembleHTML method 
     */
    long resultSize = queryResponse.getResults() == null ? 0 : queryResponse.getResults().getNumFound();

    // I'm feeling lucky
    if ("yes".equals(request.getParameter("lucky")) && resultSize == 1) {
      if ("ddb-text".equals(request.getParameter("source"))) {
        response.sendRedirect((String) queryResponse.getResults().get(0).getFieldValue(SolrField.id.name()) + "/source");
      } else {
        response.sendRedirect((String) queryResponse.getResults().get(0).getFieldValue(SolrField.id.name()));
      }
    } else {
      /* Convert the results returned as a whole to <code>DocumentBrowseRecord</code> objects, each
       of which represents one returned document. */
      ArrayList<DocumentBrowseRecord> returnedRecords = retrieveRecords(solrQuery, queryResponse, facets);

      /* Generate the HTML necessary to display the facet widgets, the facet constraints, 
       * the returned records, and pagination information */
      String html = this.assembleHTML(facets, constraintsPresent, resultSize, returnedRecords, request.getParameterMap(), docsPerPage, exceptionLog, page);

      /* Inject the generated HTML */
      displayBrowseResult(response, html);
    }
  }

  /**
   * Returns the
   * <code>List</code> of
   * <code>Facet</code>s to be used.
   *
   */
  ArrayList<Facet> getFacets() {

    ArrayList<Facet> facets = new ArrayList<Facet>();

    facets.add(new StringSearchFacet());
    facets.add(new IdentifierFacet());
    facets.add(new PlaceFacet());
    facets.add(new NomeFacet());
    facets.add(new DateFacet());
    facets.add(new LanguageFacet());
    facets.add(new TranslationFacet());
    facets.add(new HasImagesFacet());
    facets.add(new HasTranscriptionFacet());
    return facets;

  }

  Integer parseRequestForDocumentsPerPage(HttpServletRequest request) {

    int docsPerPage = defaultDocumentsPerPage;
    Map<String, String[]> requestParams = request.getParameterMap();

    try {

      docsPerPage = Integer.valueOf(requestParams.get(FacetParam.DOCS_PER_PAGE.name())[0]);

    } catch (Exception e) {
    }

    return docsPerPage;
  }

  /**
   * Passes the passed
   * <code>HttpServletRequest</code> object to each of the
   * <code>Facet</code>s in turn, allowing each of them to retrieve the parts of
   * the query string relevant to themselves.
   *
   * @param request
   * @param facets
   * @return A Boolean indicating whether or not any constraints have been
   * submitted in the query string which the results returned.
   * @see Facet#addConstraints(java.util.Map)
   *
   */
  Boolean parseRequestToFacets(HttpServletRequest request, ArrayList<Facet> facets) {

    Map<String, String[]> requestParams = request.getParameterMap();
    Boolean constraintsPresent = false;

    Iterator<Facet> fit = facets.iterator();

    while (fit.hasNext()) {

      Facet facet = fit.next();
      if (facet.addConstraints(requestParams)) {
        constraintsPresent = true;
      }

    }

    return constraintsPresent;

  }

  /**
   * Generates the
   * <code>SolrQuery</code> to be used to retrieve
   * <code>Record</code>s and to populate the
   * <code>Facet</code>s.
   *
   * @param pageNumber
   * @param facets
   * @return The <code>SolrQuery</code>
   * @see Facet#buildQueryContribution(org.apache.solr.client.solrj.SolrQuery)
   */
  SolrQuery buildFacetQuery(int pageNumber, ArrayList<Facet> facets, int docsPerPage) {

    SolrQuery sq = new SolrQuery();
    sq.setFacetMissing(true);
    sq.setFacetMinCount(1);         // we don't want to see zero-count values            
    sq.setRows(docsPerPage);
    sq.setStart((pageNumber - 1) * docsPerPage);

    // iterate through facets, adding their contributions to solr query
    // TODO: this is a cheap hack right now, to ensure that the StringSearchFacet
    // is passed a SolrQuery object with as many clauses as possible attached
    // i.e., with the search space narrowed as far as possible
    // this should probably be implemented in a more systematic way
    for (int i = facets.size() - 1; i >= 0; i--) {

      Facet facet = facets.get(i);
      sq = facet.buildQueryContribution(sq);

    }
    sq.addSortField(SolrField.series.name(), SolrQuery.ORDER.asc);
    sq.addSortField(SolrField.volume.name(), SolrQuery.ORDER.asc);
    sq.addSortField(SolrField.item.name(), SolrQuery.ORDER.asc);
    // each Facet, if constrained, will add a FilterQuery to the SolrQuery. For our results, we want
    // all documents that pass these filters - hence '*:*' as the actual query
    String queryString = sq.toString().contains("cache") ? "{!cache=false}*:*" : "*:*";
    sq.setQuery(queryString);
    return sq;


  }

  /**
   * Queries the Solr server.
   *
   * @param sq
   * @return The <code>QueryResponse</code> returned by the Solr server
   */
  private QueryResponse runFacetQuery(SolrQuery sq) {

    try {

      CommonsHttpSolrServer solrServer = new CommonsHttpSolrServer(SOLR_URL + PN_SEARCH);
      solrServer.setSoTimeout(SOCKET_TIMEOUT);
      QueryResponse qr = solrServer.query(sq, SolrRequest.METHOD.POST);
      return qr;
    } catch (MalformedURLException murle) {
      logger.error("MalformedURLException at info.papyri.dispatch.browse.facet.FacetBrowser: " + murle.getMessage(), murle);
      return null;
    } catch (SolrServerException sse) {
      logger.error("SolrServerException at info.papyri.dispatch.browse.facet.FacetBrowser: " + sse.getMessage(), sse);
      return null;
    }


  }

  ArrayList<CustomApplicationException> collectFacetExceptions(ArrayList<Facet> facets) {

    ArrayList<CustomApplicationException> exceptions = new ArrayList<CustomApplicationException>();

    Iterator<Facet> fit = facets.iterator();
    while (fit.hasNext()) {

      ArrayList<CustomApplicationException> facetExceptions = fit.next().getExceptionLog();

      Iterator<CustomApplicationException> xit = facetExceptions.iterator();
      while (xit.hasNext()) {

        exceptions.add(xit.next());

      }

    }

    return exceptions;

  }

  /**
   * Sends the
   * <code>QueryResponse</code> returned by the Solr server to each of the
   * <code>Facet</code>s in turn to populate its values list.
   *
   * @param facets
   * @param queryResponse
   * @see
   * Facet#setWidgetValues(org.apache.solr.client.solrj.response.QueryResponse)
   */
  private void populateFacets(ArrayList<Facet> facets, QueryResponse queryResponse) {

    Iterator<Facet> fit = facets.iterator();
    while (fit.hasNext()) {

      Facet facet = fit.next();
      facet.setWidgetValues(queryResponse);

    }

  }

  /**
   * Parses the
   * <code>QueryResponse</code> returned by the Solr server into a list of
   * <code>DocumentBrowseRecord</code>s.
   *
   *
   * @param queryResponse
   * @return An <code>ArrayList</code> of <code>DocumentBrowseRecord</code>s.
   * @see DocumentBrowseRecord
   */
  ArrayList<DocumentBrowseRecord> retrieveRecords(SolrQuery solrQuery, QueryResponse queryResponse, ArrayList<Facet> facets) {

    ArrayList<SearchClause> searchClauses = this.generateHighlightString(facets);

    ArrayList<DocumentBrowseRecord> records = new ArrayList<DocumentBrowseRecord>();

    if (queryResponse.getResults() == null) {
      return records;
    }

    int counter = 0;

    for (SolrDocument doc : queryResponse.getResults()) {

      try {

        URL url = new URL((String) doc.getFieldValue(SolrField.id.name()));                 // link to full record
        Boolean placeIsNull = doc.getFieldValue(SolrField.display_place.name()) == null;
        String place = placeIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.display_place.name());   // i.e., provenance
        Boolean dateIsNull = doc.getFieldValue(SolrField.display_date.name()) == null;
        String date = dateIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.display_date.name());      // original language
        Boolean titleIsNull = doc.getFieldValue(SolrField.title.name()) == null;
        ArrayList<String> documentTitles = titleIsNull ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.title.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
        Boolean languageIsNull = doc.getFieldValue(SolrField.facet_language.name()) == null;
        String language = languageIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.facet_language.name()).toString().replaceAll("\\[", "").replaceAll("\\]", "");
        Boolean noTranslationLanguages = doc.getFieldValue(SolrField.translation_language.name()) == null;
        String translationLanguages = noTranslationLanguages ? "None" : (String) doc.getFieldValue(SolrField.translation_language.name()).toString().replaceAll("[\\[\\]]", "");
        ArrayList<String> imagePaths = doc.getFieldValue(SolrField.image_path.name()) == null ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.image_path.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
        Boolean hasIllustration = doc.getFieldValue(SolrField.illustrations.name()) == null ? false : true;
        ArrayList<String> allIds = getAllSortedIds(doc);
        String preferredId = (allIds == null || allIds.isEmpty()) ? "No id supplied" : allIds.remove(0);
        DocumentBrowseRecord record = new DocumentBrowseRecord(preferredId, allIds, url, documentTitles, place, date, language, imagePaths, translationLanguages, hasIllustration, searchClauses);
        setLinearBrowseData(solrQuery, queryResponse, counter, record);
        records.add(record);
        counter++;

      } catch (MalformedURLException mue) {
        logger.error("Malformed URL in retrieveRecords: " + mue.getMessage(), mue);
      }
    }

    Collections.sort(records);
    return records;

  }

  /**
   * Retrieves the string to be highlighted from the
   * <code>StringSearchFacet</code>, to enable highlighting in the results
   * summary.
   *
   * @param facets
   * @return
   */
  private ArrayList<SearchClause> generateHighlightString(ArrayList<Facet> facets) {

    ArrayList<SearchClause> searchClauses = new ArrayList<SearchClause>();

    try {

      StringSearchFacet ssf = (StringSearchFacet) this.findFacet(facets, StringSearchFacet.class);
      searchClauses = ssf.getAllSearchClauses();


    } catch (FacetNotFoundException fnfe) {
    }
    return searchClauses;

  }

  /**
   * Generates the HTML for injection
   *
   *
   * @param facets
   * @param constraintsPresent
   * @param resultsSize
   * @param returnedRecords
   * @param solrQuery Used for cheap 'n' easy debugging only
   * @return The complete HTML for all interactive portions of the page, as
   * a <code>String</code>
   */
  private String assembleHTML(ArrayList<Facet> facets, Boolean constraintsPresent, long resultsSize, ArrayList<DocumentBrowseRecord> returnedRecords, Map<String, String[]> submittedParams, int docsPerPage, ArrayList<CustomApplicationException> exceptionLog, int page) {

    StringBuilder html = new StringBuilder();
    html.append("<form name=\"facets\" method=\"get\" action=\"");
    html.append(FACET_PATH);
    html.append("\"> ");
    html.append("<div id=\"facet-wrapper\">");
    assembleWidgetHTML(facets, constraintsPresent, html, submittedParams);
    html.append("<div id=\"vals-and-records-wrapper\" class=\"vals-and-records-min\">");
    if (constraintsPresent) {
      assemblePreviousValuesHTML(facets, html, submittedParams, docsPerPage);
    }
    assembleRecordsHTML(facets, returnedRecords, constraintsPresent, resultsSize, html, docsPerPage, exceptionLog, page);
    html.append("</div><!-- closing #vals-and-records-wrapper -->");
    html.append("</div><!-- closing #facet-wrapper -->");
    html.append("</form>");
    return html.toString();

  }

  /**
   * Assembles the HTML displaying the
   * <code>Facet</code> control widgets
   *
   *
   * @param facets
   * @param html
   * @return A <code>StringBuilder</code> holding the HTML for
   * the <code>Facet</code> control widgets
   * @see Facet#generateWidget()
   */
  private StringBuilder assembleWidgetHTML(ArrayList<Facet> facets, Boolean hasConstraints, StringBuilder html, Map<String, String[]> submittedParams) {

    html.append("<div id=\"facet-widgets-wrapper\" class=\"search search-open\">");
    html.append("<div id=\"search-toggle\" class=\"toggle-open\"><div id=\"search-toggle-pointer\">&lt;&lt;</div><!-- closing #pointer --></div><!-- closing #toggler -->");
    String heading = hasConstraints ? "Refine Search" : "Search";
    html.append("<h2>");
    html.append(heading);
    html.append("</h2>");
    html.append("<div id=\"search-reset-wrapper\">");
    html.append("<a href=\"");
    html.append(FacetBrowser.FACET_PATH);
    html.append("\" id=\"reset-all\" class=\"ui-button ui-widget ui-state-default ui-corner-all\" aria-disabled=\"false\">New Search</a>");
    html.append("<input type=\"submit\" value=\"Search\" id=\"search\" class=\"ui-button ui-widget ui-state-default ui-corner-all\" role=\"button\" aria-disabled=\"false\"/>");
    html.append("</div>");


    try {


      Facet stringFacet = findFacet(facets, StringSearchFacet.class);
      html.append(stringFacet.generateWidget());


    } catch (FacetNotFoundException fnfe) {

      logger.warn(fnfe.getMessage(), fnfe);
      html.append("<!-- Facet not found ");
      html.append(fnfe.getMessage());
      html.append(" -->");


    }

    try {

      Facet idFacet = findFacet(facets, IdentifierFacet.class);
      html.append(idFacet.generateWidget());
      Facet placeFacet = findFacet(facets, PlaceFacet.class);
      html.append(placeFacet.generateWidget());
      Facet nomeFacet = findFacet(facets, NomeFacet.class);
      html.append(nomeFacet.generateWidget());
      Facet dateFacet = findFacet(facets, DateFacet.class);
      html.append(dateFacet.generateWidget());
      Facet langFacet = findFacet(facets, LanguageFacet.class);
      html.append(langFacet.generateWidget());
      Facet translFacet = findFacet(facets, TranslationFacet.class);
      html.append(translFacet.generateWidget());
      Facet imgFacet = findFacet(facets, HasImagesFacet.class);
      html.append(imgFacet.generateWidget());
      Facet transcFacet = findFacet(facets, HasTranscriptionFacet.class);
      html.append(transcFacet.generateWidget());



    } catch (FacetNotFoundException fnfe) {

      html.append("<!-- Facet not found ");
      html.append(fnfe.getMessage());
      html.append(" -->");

    }

    html.append("</div><!-- closing #facet-widgets-wrapper -->");
    return html;

  }

  /**
   * Assembles the HTML displaying the records returned by the Solr server
   *
   * Note the importance of the table-cell order defined here corresponding to
   * the order defined at
   * <code>DocumentBrowseRecord.getHTML()</code>
   *
   *
   * @param facets
   * @param returnedRecords
   * @param constraintsPresent
   * @param resultSize
   * @param html
   * @param sq Used in debugging only
   * @return A <code>StringBuilder</code> holding the HTML for the records
   * returned by the Solr server
   * @see DocumentBrowseRecord#getHTML()
   */
  private StringBuilder assembleRecordsHTML(ArrayList<Facet> facets, ArrayList<DocumentBrowseRecord> returnedRecords, Boolean constraintsPresent, long resultSize, StringBuilder html, int docsPerPage, ArrayList<CustomApplicationException> exceptionLog, int page) {

    html.append("<div id=\"facet-records-wrapper\">");
    html.append("<div id=\"results-prefix-wrapper\">");
    int topMargin = resultSize == 0 || !constraintsPresent ? 30 : 10;
    html.append("<div id=\"results-prefix\" style=\"margin-top:");
    html.append(String.valueOf(topMargin));
    html.append("px\">");
    html.append("<div id=\"docs-per-page\">");
    html.append("<label for=\"");
    html.append(FacetParam.DOCS_PER_PAGE.name());
    html.append("\">Records per page</label>");
    html.append("<input type=\"text\" name=\"");
    html.append(FacetParam.DOCS_PER_PAGE.name());
    html.append("\" value=\"");
    html.append(String.valueOf(docsPerPage));
    html.append("\" id=\"");
    html.append(FacetParam.DOCS_PER_PAGE.name());
    html.append("\" maxlength=\"3\"></input>");
    html.append("<input type=\"submit\" value=\"Go\" id=\"img-go\" class=\"ui-button ui-widget ui-state-default ui-corner-all\" role=\"button\" aria-disabled=\"false\"/>");
    html.append("</div><!-- closing #docs-per-page -->");
    if (!constraintsPresent && exceptionLog.size() == 0) {

      html.append("<div id=\"opening-info\"><h2>Please select values from the left-hand column to return results</h2></div>");
      html.append("<noscript><div id=\"js-warning\"><p>You appear to have Javascript turned off in your browser.</p><p>Unfortunately, this site depends on Javascript in order to display correctly.</p><p>Please enable Javascript before searching.</p></div></noscript>");
      html.append(getInstructions());
    } else if (exceptionLog.size() > 0) {

      html.append(displayParsingErrorMessage(exceptionLog));
      returnedRecords.clear();
      resultSize = 0;

    } else if (resultSize == 0) {

      html.append("<h2>0 documents found matching criteria set.</h2>");
      html.append("<p>To determine why this is, try setting or removing criteria one at a time to see how this affects the results returned.</p>");

    } else {

      html.append("<p>");
      html.append(String.valueOf(resultSize));
      html.append(resultSize > 1 ? " hits." : " hit");
      html.append("</p>");

    }
    html.append("</div><!-- closing #results-prefix -->");
    html.append("</div><!-- closing #results-prefix-wrapper -->");

    if (constraintsPresent && resultSize > 0) {

      html.append("<table>");
      html.append("<tr class=\"tablehead\"><td>Identifier</td><td>Title</td><td>Location</td><td>Date</td><td>Languages</td><td>Translations</td><td>Images</td></tr>");
      Iterator<DocumentBrowseRecord> rit = returnedRecords.iterator();

      while (rit.hasNext()) {

        DocumentBrowseRecord dbr = rit.next();
        html.append(dbr.getHTML());

      }
      html.append("</table>");
      html.append(doPagination(facets, resultSize, docsPerPage, page));

    }


    html.append("</div><!-- closing #facet-records-wrapper -->");
    return html;

  }

  /**
   * Generates the HTML controls indicating the constraints currently set on
   * each
   * <code>Facet</code>, and that, when clicked, remove the constraint which
   * they designate.
   *
   * @param facets
   * @param html
   * @return A <code>StringBuilder</code> holding the HTML for all previous
   * constraints defined on the dataset
   * @see #buildFilteredQueryString(java.util.EnumMap,
   * info.papyri.dispatch.browse.facet.FacetParam, java.lang.String)
   */
  private StringBuilder assemblePreviousValuesHTML(ArrayList<Facet> facets, StringBuilder html, Map<String, String[]> submittedParams, int docsPerPage) {


    StringBuilder previousHTMLValues = new StringBuilder("<div id=\"previous-values\">");
    Iterator<Facet> fit = facets.iterator();

    while (fit.hasNext()) {

      Facet facet = fit.next();

      String[] params = facet.getFormNames();

      StringBuilder values = new StringBuilder();

      for (int i = 0; i < params.length; i++) {

        String param = params[i];

        if (submittedParams.containsKey(param)) {

          ArrayList<String> facetValues = facet.getFacetConstraints(param);

          Iterator<String> fvit = facetValues.iterator();

          while (fvit.hasNext()) {

            String facetValue = fvit.next();
            String displayName = facet.getDisplayName(param, facetValue);
            String displayFacetValue = facet.getDisplayValue(facetValue);
            String queryString = this.buildFilteredQueryString(facets, facet, param, facetValue, docsPerPage);
            values.append("<div class='facet-constraint constraint-");
            values.append(param.toLowerCase());
            values.append("'>");
            values.append("<div class='constraint-label'>");
            values.append(displayName);
            if (!"".equals(displayName)) {
              values.append("<span class='semicolon'>:</span> ");
            }
            values.append(displayFacetValue);
            values.append("</div><!-- closing .constraint-label -->");
            values.append("<div class='constraint-closer'>");
            values.append("<a href='");
            values.append(FACET_PATH);
            values.append("".equals(queryString) ? "" : "?");
            values.append(queryString);
            values.append("' title ='Remove facet value'>X</a>");
            values.append("</div><!-- closing .constraint-closer -->");
            values.append("<div class='spacer'></div>");
            values.append("</div><!-- closing .facet-constraint -->");
          }

        }

      }

      String valueString = values.toString();
      if (valueString.length() > 0) {

        previousHTMLValues.append("<div class='prev-constraint-wrapper' id='prev-constraint-");
        previousHTMLValues.append(facet.getCSSSelectorID());
        previousHTMLValues.append("'>");
        previousHTMLValues.append(values);
        previousHTMLValues.append("</div><!-- closing .prev-constraint-wrapper -->");


      }



    }

    previousHTMLValues.append("<div class='spacer'></div>");
    previousHTMLValues.append("</div><!-- closing #previous-values -->");
    html.append(previousHTMLValues.toString());
    return html;

  }

  String displayParsingErrorMessage(ArrayList<CustomApplicationException> exceptionLog) {

    StringBuilder html = new StringBuilder();
    html.append("<div id=\"parse-errors\">");
    html.append("<p>");
    html.append("Unfortunately, your string search could not be parsed, for the following ");
    String r = exceptionLog.size() == 1 ? "reason" : "reasons";
    html.append(r);
    html.append(":");
    html.append("</p>");
    html.append("<ul>");
    Iterator<CustomApplicationException> xit = exceptionLog.iterator();
    while (xit.hasNext()) {

      CustomApplicationException exception = xit.next();
      html.append("<li class=\"parse-error\">");
      html.append(exception.getMessage());
      html.append("</li>");

    }
    html.append("</ul>");
    html.append("</div><!-- closing #parsing-errors -->");
    return html.toString();
  }

  /**
   * Injects the HTML code previously generated by the
   * <code>assembleHTML</code> method
   *
   * @param response
   * @param html
   * @see #assembleHTML(java.util.EnumMap, java.lang.Boolean, long,
   * java.util.ArrayList, org.apache.solr.client.solrj.SolrQuery)
   */
  void displayBrowseResult(HttpServletResponse response, String html) {

    BufferedReader reader = null;
    try {

      PrintWriter out = response.getWriter();
      reader = new BufferedReader(new InputStreamReader(FACET_URL.openStream()));
      String line = "";
      while ((line = reader.readLine()) != null) {

        out.println(line);

        if (line.contains("<!-- Facet browse results -->")) {

          out.println(html);


        }

      }


    } catch (Exception e) {
    }

  }

  /**
   * Does what it says.
   *
   * The complicating factor is that the current query string needs to be
   * regenerated for each link in order to maintain state across the pages.
   *
   * @param paramsToFacets
   * @param resultSize
   * @return The HTML used for pagination display, as a <code>String</code>
   * @see #buildFullQueryString(java.util.EnumMap)
   */
  private String doPagination(ArrayList<Facet> facets, long resultSize, int docsPerPage, int page) {

    if (resultSize <= docsPerPage) {
      return "";
    }

    Float resultSizeAsFloat = Float.valueOf(resultSize);

    int numPages = (int) (Math.ceil(resultSizeAsFloat / docsPerPage));

    String fullQueryString = buildFullQueryString(facets, docsPerPage);

    double widthEach = 8;
    double totalWidth = widthEach * numPages;
    totalWidth = totalWidth > 100 ? 100 : totalWidth;

    StringBuilder html = new StringBuilder("<div id='pagination' style='width:");
    html.append(String.valueOf(totalWidth));
    html.append("%'>");

    for (long i = 1; i <= numPages; i++) {

      html.append("<div class=\"page");
      if (i == page) {
        html.append(" currentpage");
      }
      html.append("\">");
      html.append("<a href='");
      html.append(FACET_PATH);
      html.append("?");
      html.append(fullQueryString);
      html.append("page=");
      html.append(i);
      html.append("'>");
      html.append(String.valueOf(i));
      html.append("</a>");
      html.append("</div><!-- closing .page -->");

    }

    html.append("<div class='spacer'></div><!-- closing .spacer -->");
    html.append("</div><!-- closing #pagination -->");
    return html.toString();

  }

  /**
   * Retrieves querystrings from each of the
   * <code>Facet</code>s in turn and concatenates them into a complete
   * querystring.
   *
   * @param facets
   * @return The complete querystring
   * @see Facet#getAsQueryString()
   */
  private String buildFullQueryString(ArrayList<Facet> facets, int docsPerPage) {

    ArrayList<String> queryStrings = new ArrayList<String>();

    Iterator<Facet> fit = facets.iterator();
    while (fit.hasNext()) {

      Facet facet = fit.next();
      String queryString = facet.getAsQueryString();
      if (!"".equals(queryString)) {
        queryStrings.add(queryString);
      }

    }

    String fullQueryString = "";
    Iterator<String> qsit = queryStrings.iterator();
    while (qsit.hasNext()) {

      fullQueryString += qsit.next();
      fullQueryString += "&";

    }

    if (docsPerPage != defaultDocumentsPerPage) {

      String docsPerPageAsQString = FacetParam.DOCS_PER_PAGE.name() + "=" + String.valueOf(docsPerPage);
      fullQueryString += docsPerPageAsQString;
      fullQueryString += "&";

    }


    return fullQueryString;

  }

  /**
   * Concatenates the querystring portions retrieved from each
   * <code>Facet</code> into a complete querystring, with the exception of the
   * value given in the passed
   * <code>facetValue</code>, which is omitted.
   *
   * The resulting query string is then used in a link, which when clicked gives
   * the appearance that a facet constraint has been removed (as its value is
   * not included in the string.
   *
   *
   * @param facets The <code>List</code> of all <code>Facet</code>s
   * @param relFacet The <code>Facet</code> to which the value to be 'removed'
   * belongs
   * @param facetParam The name of the form control associated with
   * the <code>Facet</code> and submitted by the user.
   * @param facetValue The value to be 'removed'
   * @return A filtered querystring.
   * @see Facet#getAsFilteredQueryString(java.lang.String)
   */
  private String buildFilteredQueryString(ArrayList<Facet> facets, Facet relFacet, String facetParam, String facetValue, int docsPerPage) {

    ArrayList<String> queryStrings = new ArrayList<String>();

    Iterator<Facet> fit = facets.iterator();

    while (fit.hasNext()) {

      String queryString = "";

      Facet facet = fit.next();
      if (facet == relFacet) {

        queryString = facet.getAsFilteredQueryString(facetParam, facetValue);

      } else {

        queryString = facet.getAsQueryString();

      }


      if (!"".equals(queryString)) {

        queryStrings.add(queryString);

      }

    }

    String filteredQueryString = "";
    Iterator<String> qsit = queryStrings.iterator();
    while (qsit.hasNext()) {

      filteredQueryString += qsit.next();
      filteredQueryString += "&";

    }

    if (docsPerPage != defaultDocumentsPerPage) {

      String docsPerPageAsQString = FacetParam.DOCS_PER_PAGE.name() + "=" + String.valueOf(docsPerPage);
      filteredQueryString += docsPerPageAsQString;

    }
    return chopFinalAmpersand(filteredQueryString);

  }

  /**
   * Ensures that query-strings are not submitted with a trailing ampersand
   */
  private String chopFinalAmpersand(String queryString) {

    Pattern pattern = Pattern.compile("^(.+)&$");
    Matcher matcher = pattern.matcher(queryString);
    return matcher.matches() ? matcher.group(1) : queryString;

  }

  /**
   * Retrieves all IDs associated with a document and ranks them in order of
   * preference.
   *
   * This ranking proceeds in the order: (1) DDbDp identifier, based on the
   * document's URL (2) Other DDbDp identifiers specified in the document (3)
   * HGV identifiers specified in the document (4) APIS publication numbers (5)
   * APIS inventory numbers (6) APIS ID numbers.
   *
   * @param doc
   * @return
   */
  ArrayList<String> getAllSortedIds(SolrDocument doc) {

    ArrayList<String> ids = new ArrayList<String>();

    String id = (String) doc.getFieldValue(SolrField.id.name());

    if (id != null && id.matches("/ddbdp/")) {

      String canonicalId = id.substring("http://papyri.info".length());
      canonicalId = canonicalId.replaceAll("_", " ").trim();
      canonicalId = canonicalId.replaceAll(";;", ";");
      canonicalId = canonicalId.replaceAll(";", " ").trim();
      ids.add(canonicalId);

    }

    ArrayList<String> ddbdpIds = getCollectionIds("ddbdp", doc);
    if (ddbdpIds.size() > 0) {
      ids.addAll(ddbdpIds);
    }
    ArrayList<String> hgvIds = getCollectionIds("hgv", doc);
    if (hgvIds.size() > 0) {
      ids.addAll(hgvIds);
    }

    ArrayList<String> apisPublicationNumbers = doc.getFieldValue(SolrField.apis_publication_id.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.apis_publication_id.name()).toString().replaceAll("\\[\\]", "").split(",")));
    ArrayList<String> apisInventoryNumbers = doc.getFieldValue(SolrField.apis_inventory.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.apis_inventory.name()).toString().replaceAll("\\[\\]", "").split(",")));

    if (apisPublicationNumbers != null) {

      Iterator<String> pit = apisPublicationNumbers.iterator();
      while (pit.hasNext()) {

        pit.next().replaceAll("_", "").trim();


      }
      apisPublicationNumbers = filterIds(apisPublicationNumbers);
      Collections.sort(apisPublicationNumbers, new IdComparator());
      ids.addAll(apisPublicationNumbers);

    }

    if (apisInventoryNumbers != null) {

      Iterator<String> invit = apisInventoryNumbers.iterator();
      while (invit.hasNext()) {

        invit.next().replaceAll("_", "");
      }
      apisInventoryNumbers = filterIds(apisInventoryNumbers);
      Collections.sort(apisInventoryNumbers, new IdComparator());
      ids.addAll(apisInventoryNumbers);

    }

    ArrayList<String> apisIds = getCollectionIds("apis", doc);
    if (apisIds.size() > 0) {
      ids.addAll(apisIds);
    }
    return ids;

  }

  /**
   * Retrieves the list of all ids associated with a given document with a
   * particular collection (ddbdp | hgv | apis)
   *
   *
   * @param collection May be one of the values "ddbdp", "hgv", or "apis"
   * @param doc
   * @return
   */
  ArrayList<String> getCollectionIds(String collection, SolrDocument doc) {

    ArrayList<String> collectionMembers = new ArrayList<String>();

    String cpref = collection + "_";

    ArrayList<String> series = doc.getFieldValue(cpref + SolrField.series.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(cpref + SolrField.series.name()).toString().replaceAll("[\\[\\]]", ",").split(",")));
    ArrayList<String> volumes = doc.getFieldValue(cpref + SolrField.volume.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(cpref + SolrField.volume.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
    ArrayList<String> itemIds = doc.getFieldValue(cpref + SolrField.full_identifier.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(cpref + SolrField.full_identifier.name()).toString().replaceAll("[\\[\\]]", "").split(",")));

    if (series != null && volumes != null && itemIds != null) {

      for (int j = 0; j < series.size(); j++) {

        if (j > itemIds.size() - 1) {
          break;
        }
        if (series.get(j).equals("0") || series.get(j).equals("")) {
          break;
        }
        String nowId = series.get(j).replaceAll("_", " ").trim() + " " + ((j > (volumes.size() - 1)) ? "" : volumes.get(j)).replaceAll("_", " ").trim() + " " + itemIds.get(j).replaceAll("_", " ").trim();
        collectionMembers.add(nowId);

      } // closing for j loop

    }    // closing null check

    collectionMembers = filterIds(collectionMembers);
    Collections.sort(collectionMembers, new IdComparator());

    return collectionMembers;

  }

  /**
   * Filters duplicate and empty id values from the passed id list.
   *
   * @param rawIds
   * @return
   * @see FacetBrowser#getCollectionIds(java.lang.String,
   * org.apache.solr.common.SolrDocument)
   */
  ArrayList<String> filterIds(ArrayList<String> rawIds) {

    ArrayList<String> acceptedIds = new ArrayList<String>();

    // weeding out duplicates

    Iterator<String> rit = rawIds.iterator();

    while (rit.hasNext()) {

      String id = rit.next();

      if (!id.matches("^\\s*$") && !acceptedIds.contains(id)) {
        acceptedIds.add(id);
      }

    }

    return acceptedIds;

  }

  /**
   * Creates, starting from the
   * <code>SolrQuery</code> object used for the current result set, a new
   * <code>SolrQuery</code> capable of returning the immediate neighbour(s) of
   * the passed
   * <code>DocumentBrowseRecord</code> in that result set, in order to support
   * linear-browsing functionality.
   *
   * @param bigQuery
   * @param qr
   * @param recordPosition
   * @param record
   */
  private void setLinearBrowseData(SolrQuery bigQuery, QueryResponse qr, int recordPosition, DocumentBrowseRecord record) {

    SolrQuery newQuery = new SolrQuery();

    String[] filterQueries = bigQuery.getFilterQueries();
    String[] sortFields = bigQuery.getSortFields();

    try {

      Long currentPosition = Long.valueOf(bigQuery.getStart() + recordPosition);
      Long lastRecord = qr.getResults().getNumFound() - 1;
      newQuery.setRows((currentPosition == 0 || currentPosition == lastRecord) ? 2 : 3);
      int startValue = (int) (currentPosition == 0 ? 0 : currentPosition - 1);
      newQuery.setStart(startValue);
      newQuery.addField("id");
      newQuery.addField("title");

      for (int i = 0; i < filterQueries.length; i++) {

        String filterQuery = filterQueries[i];
        if (!"".equals(filterQuery)) {
          newQuery.addFilterQuery(filterQuery);
        }

      }

      for (int j = 0; j < sortFields.length; j++) {

        String[] sortBits = sortFields[j].split(" ");
        if (sortBits.length == 2) {
          newQuery.addSortField(sortBits[0], SolrQuery.ORDER.valueOf(sortBits[1]));
        }

      }

      record.setSolrData(newQuery, currentPosition, lastRecord);


    } catch (Exception e) {
    }

  }

  /**
   * Retrieves the required
   * <code>Facet</code> object from the facet list, given the
   * <code>Facet</code> subclass.
   *
   *
   * @param facets
   * @param facetSubClass
   * @return
   * @throws FacetNotFoundException
   */
  private Facet findFacet(ArrayList<Facet> facets, Class facetSubClass) throws FacetNotFoundException {

    Iterator<Facet> fit = facets.iterator();

    while (fit.hasNext()) {

      Facet facet = fit.next();

      if (facet.getClass().equals(facetSubClass)) {
        return facet;
      }


    }

    throw new FacetNotFoundException(facetSubClass.toString());

  }

  private String getInstructions() {

    String instructions = "<div id=\"info\"><p>Selecting a value using the controls in the left-hand column will return a list of all  documents that match it in the right-hand column. Once these results have been returned, the controls can be used to further refine the search with additional values. This process of adding new search constraints can be applied repeatedly until the results have been narrowed as far as desired.</p></div>";

    try {

      BufferedReader in = new BufferedReader(new FileReader(INSTRUCTIONS_PATH));
      StringBuilder instrBuilder = new StringBuilder();
      String startInst = "";
      while ((startInst = in.readLine()) != null) {
        instrBuilder.append(startInst);
      }
      return instrBuilder.toString();

    } catch (FileNotFoundException fnfe) {

      return instructions;

    } catch (IOException ioe) {

      return instructions;

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
