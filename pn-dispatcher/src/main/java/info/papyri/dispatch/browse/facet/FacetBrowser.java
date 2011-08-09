package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.DocumentBrowseRecord;
import info.papyri.dispatch.browse.IdComparator;
import info.papyri.dispatch.browse.SolrField;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

@WebServlet(name = "FacetBrowser", urlPatterns = {"/FacetBrowser"})

/**
 * Enables faceted browsing of the pn collections
 * 
 * @author thill
 */

public class FacetBrowser extends HttpServlet {
    
    /** Solr server address. Supplied in config file */
    static String SOLR_URL;
    /** Path to appropriate Solr core */
    static String PN_SEARCH = "pn-search/";  
    /** path to home html directory */
    static private String home;
    /** path to html file used in html injection */
    static private URL FACET_URL;
    /** path to servlet */
    /* TODO: Get this squared up with urlPatterns, above */
    static private String FACET_PATH = "/dispatch/faceted/";
    /** Number of records to show per page. Used in pagination */
    static private int documentsPerPage = 50;
        
    @Override
    public void init(ServletConfig config) throws ServletException{
        
        super.init(config);

        SOLR_URL = config.getInitParameter("solrUrl");
        home = config.getInitParameter("home");
        try {
            
            FACET_URL = new URL("file://" + home + "/" + "facetbrowse.html");
            
        } catch (MalformedURLException e) {
      
            throw new ServletException(e);
   
        }
        
    } 
    
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * 
     * Because this is the central coordinating method for the servlet, a step-by-step
     * explanation of the method calls is provided in the method body.
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
        
        /* Get the <code>List</code> of facets to be displayed */
        ArrayList<Facet> facets = getFacets();
        
        /* Parse request, allowing each facet to pull out and parse the part of the request
         * relevant to itself.
         */
        Boolean constraintsPresent = parseRequestToFacets(request, facets);
        
        /* determine what page of results has been requested ('0' if no page requested and
         * this is therefore the first page of results). 
         * 
         * Required for building the facet query.
         */
        int page = request.getParameter("page") != null ? Integer.valueOf(request.getParameter("page")) : 0;
        
        /* Build the SolrQuery object to be used in querying Solr out of query parts contributed 
         * by each of the facets in turn.
         */
        SolrQuery solrQuery = this.buildFacetQuery(page, facets);
        
        /* Query the Solr server */
        QueryResponse queryResponse = this.runFacetQuery(solrQuery);
        
        /* Allow each facet to pull out the values relevant to it from the <code>QueryResponse</code>
         * returned by the Solr server.
         */
        populateFacets(facets, queryResponse);
        
        /* Convert the results returned as a whole to <code>DocumentBrowseRecord</code> objects, each
           of which represents one returned document. */
        ArrayList<DocumentBrowseRecord> returnedRecords = retrieveRecords(queryResponse);
        
                
        /* Determine the number of results returned. 
         * 
         * Required for assembleHTML method 
         */
        long resultSize = queryResponse.getResults().getNumFound();
        
        /* Generate the HTML necessary to display the facet widgets, the facet constraints, 
         * the returned records, and pagination information */
        String html = this.assembleHTML(facets, constraintsPresent, resultSize, returnedRecords, request.getParameterMap());
        
        /* Inject the generated HTML */
        displayBrowseResult(response, html);  
     
    }
    

    /** Returns the <code>List</code> of <code>Facet</code>s to be used. 
     * 
     *  Note that the order of <code>Facet</code> declaraion in the <code>List</code> determines
     *  the order in which they are displayed.
     */
    private ArrayList<Facet> getFacets(){
          
        ArrayList<Facet> facets = new ArrayList<Facet>();
        
        facets.add(new StringSearchFacet());
        facets.add(new LanguageFacet());
        facets.add(new PlaceFacet());
        facets.add(new DateFacet());
        facets.add(new HasTranscriptionFacet());
        facets.add(new TranslationFacet());
        facets.add(new HasImagesFacet());
        return facets;
        
    }
    
    /**
     * Passes the passed <code>HttpServletRequest</code> object to each of the <code>Facet</code>s
     * in turn, allowing each of them to retrieve the parts of the query string relevant to themselves.
     * 
     * @param request
     * @param facets
     * @return A Boolean indicating whether or not any constraints have been submitted in the query
     * string which the results returned.
     * @see Facet#addConstraints(java.util.Map) 
     * 
     */
  
    Boolean parseRequestToFacets(HttpServletRequest request, ArrayList<Facet> facets){
                
        Map<String, String[]> requestParams = request.getParameterMap();
        Boolean constraintsPresent = false;
                    
        Iterator<Facet> fit = facets.iterator();
        
        while(fit.hasNext()){
            
            Facet facet = fit.next();
            if(facet.addConstraints(requestParams)) constraintsPresent = true;
            
        }
        
        return constraintsPresent;
               
    }
    
    /**
     * Generates the <code>SolrQuery</code> to be used to retrieve <code>Record</code>s and to 
     * populate the <code>Facet</code>s.
     * 
     * @param pageNumber
     * @param facets
     * @return The <code>SolrQuery</code>
     * @see Facet#buildQueryContribution(org.apache.solr.client.solrj.SolrQuery) 
     */
    
    SolrQuery buildFacetQuery(int pageNumber, ArrayList<Facet> facets){
        
        SolrQuery sq = new SolrQuery();
        sq.setFacetMissing(true);
        sq.setFacetMinCount(1);         // we don't want to see zero-count values            
        sq.setRows(documentsPerPage); 
        sq.setStart(pageNumber * documentsPerPage); 
        
        // iterate through facets, adding their contributions to solr query
        Iterator<Facet> fit = facets.iterator();
        while(fit.hasNext()){
            
            Facet facet = fit.next();
            sq = facet.buildQueryContribution(sq);
            
            
        }
        // each Facet, if constrained, will add a FilterQuery to the SolrQuery. For our results, we want
        // all documents that pass these filters - hence '*:*' as the actual query
        sq.setQuery("*:*");
        
        return sq;
        
        
    }
    
    /**
     * Queries the Solr server.
     * 
     * @param sq
     * @return The <code>QueryResponse</code> returned by the Solr server
     */
    
    private QueryResponse runFacetQuery(SolrQuery sq){
        
        try{
            
          SolrServer solrServer = new CommonsHttpSolrServer(SOLR_URL + PN_SEARCH);
          QueryResponse qr = solrServer.query(sq);
          return qr;
            
            
        }
        catch(MalformedURLException murle){
            
            System.out.println("MalformedURLException at info.papyri.dispatch.browse.facet.FacetBrowser: " + murle.getMessage());
            return null;
            
            
        }
        catch(SolrServerException sse){
            
           System.out.println("SolrServerException at info.papyri.dispatch.browse.facet.FacetBrowser: " + sse.getMessage());
           return null;
            
            
        }
        
        
    }
    
    /**
     * Sends the <code>QueryResponse</code> returned by the Solr server to each of the
     * <code>Facet</code>s in turn to populate its values list.
     * 
     * 
     * @param facets
     * @param queryResponse 
     * @see Facet#setWidgetValues(org.apache.solr.client.solrj.response.QueryResponse) 
     */
    
    private void populateFacets(ArrayList<Facet> facets, QueryResponse queryResponse){
        
        Iterator<Facet> fit = facets.iterator();
        while(fit.hasNext()){
            
            Facet facet = fit.next();
            facet.setWidgetValues(queryResponse);
                      
        }
        
    }
    
    /**
     * Parses the <code>QueryResponse</code> returned by the Solr server into a list of
     * <code>DocumentBrowseRecord</code>s.
     * 
     * 
     * @param queryResponse
     * @return An <code>ArrayList</code> of <code>DocumentBrowseRecord</code>s.
     * @see DocumentBrowseRecord
     */
    
    ArrayList<DocumentBrowseRecord> retrieveRecords(QueryResponse queryResponse){
               
        ArrayList<DocumentBrowseRecord> records = new ArrayList<DocumentBrowseRecord>();
        
        for(SolrDocument doc : queryResponse.getResults()){
            
           try{ 
            
                URL url = new URL((String) doc.getFieldValue(SolrField.id.name()));                 // link to full record
                Boolean placeIsNull = doc.getFieldValue(SolrField.display_place.name()) == null;    
                String place = placeIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.display_place.name());   // i.e., provenance
                Boolean dateIsNull = doc.getFieldValue(SolrField.display_date.name()) == null;
                String date = dateIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.display_date.name());      // original language
                Boolean languageIsNull = doc.getFieldValue(SolrField.facet_language.name()) == null;
                String language = languageIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.facet_language.name()).toString().replaceAll("\\[", "").replaceAll("\\]", "");
                Boolean noTranslationLanguages = doc.getFieldValue(SolrField.translation_language.name()) == null;
                String translationLanguages = noTranslationLanguages ? "No translation" : (String)doc.getFieldValue(SolrField.translation_language.name()).toString().replaceAll("[\\[\\]]", "");
                ArrayList<String> imagePaths = doc.getFieldValue(SolrField.image_path.name()) == null ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.image_path.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
                ArrayList<String> allIds = getAllSortedIds(doc);
                String preferredId = (allIds == null || allIds.isEmpty()) ? "No id supplied" : allIds.remove(0);
                DocumentBrowseRecord record = new DocumentBrowseRecord(preferredId, allIds, url, place, date, language, imagePaths, translationLanguages);
                records.add(record);
                
           }
           catch (MalformedURLException mue){
               
               System.out.print("Malformed URL: " + mue.getMessage());
               
           }
        }
          
        Collections.sort(records);
        return records;
        
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
     * @return The complete HTML for all interactive portions of the page, as a <code>String</code>
     */
    
    private String assembleHTML(ArrayList<Facet> facets, Boolean constraintsPresent, long resultsSize, ArrayList<DocumentBrowseRecord> returnedRecords, Map<String, String[]> submittedParams){
        
        StringBuilder html = new StringBuilder("<div id=\"facet-wrapper\">");
        assembleWidgetHTML(facets, html, submittedParams);
        html.append("<div id=\"vals-and-records-wrapper\">");
        if(constraintsPresent) assemblePreviousValuesHTML(facets,html, submittedParams);
        assembleRecordsHTML(facets, returnedRecords, constraintsPresent, resultsSize, html);
        html.append("</div><!-- closing #vals-and-records-wrapper -->");
        html.append("</div><!-- closing #facet-wrapper -->");
        return html.toString();
        
    }
    
    /**
     * Assembles the HTML displaying the <code>Facet</code> control widgets
     * 
     * 
     * @param facets
     * @param html
     * @return A <code>StringBuilder</code> holding the HTML for the <code>Facet</code> control widgets
     * @see Facet#generateWidget() 
     */
  
    private StringBuilder assembleWidgetHTML(ArrayList<Facet> facets, StringBuilder html, Map<String, String[]> submittedParams){
        
        html.append("<div id=\"facet-widgets-wrapper\">");
        html.append("<form name=\"facets\" method=\"get\" action=\"");
        html.append(FACET_PATH);
        html.append("\"> ");
        try{
            
        
            Facet stringFacet = findFacet(facets, StringSearchFacet.class);
            html.append(stringFacet.generateWidget());
            
        
        } catch (FacetNotFoundException fnfe){
            
            System.out.println(fnfe.getMessage());
            
            
        }
        html.append("<h3>Filters</h3>");
        try{
            
            Facet imgFacet = findFacet(facets, HasImagesFacet.class);
            html.append(imgFacet.generateWidget());
            Facet transcFacet = findFacet(facets, HasTranscriptionFacet.class);
            html.append(transcFacet.generateWidget());
            Facet translFacet = findFacet(facets, TranslationFacet.class);
            html.append(translFacet.generateWidget());
            Facet placeFacet = findFacet(facets, PlaceFacet.class);
            html.append(placeFacet.generateWidget());
            Facet langFacet = findFacet(facets, LanguageFacet.class);
            html.append(langFacet.generateWidget());
            Facet dateFacet = findFacet(facets, DateFacet.class);
            html.append(dateFacet.generateWidget());
            
        } catch (FacetNotFoundException fnfe){
            
            System.out.println(fnfe.getMessage());
            
        }
        // nb: submit button found in string-search facet html
        html.append("</form>");
        html.append("</div><!-- closing #facet-widgets-wrapper -->");
        return html;
        
    }
    
    /**
     * Assembles the HTML displaying the records returned by the Solr server
     * 
     * 
     * @param facets
     * @param returnedRecords
     * @param constraintsPresent
     * @param resultSize
     * @param html
     * @param sq Used in debugging only
     * @return A <code>StringBuilder</code> holding the HTML for the records returned by the Solr server
     */
    
    private StringBuilder assembleRecordsHTML(ArrayList<Facet> facets, ArrayList<DocumentBrowseRecord> returnedRecords, Boolean constraintsPresent, long resultSize, StringBuilder html){
        
        html.append("<div id=\"facet-records-wrapper\">");
        if(!constraintsPresent){
            
            html.append("<h2>Please select values from the left-hand column to return results</h2>");
            
        }
        else if(resultSize == 0){
            
            html.append("<h2>0 documents found matching criteria set.</h2>");
            html.append("<p>To determine why this is, try setting your criteria one at a time to see how this affects the results returned.</p>");
            
        }
        else{
            
            html.append("<p>");
            html.append(String.valueOf(resultSize));
            html.append(" hits.");
            html.append("</p>");
            html.append("<table>");
            html.append("<tr class=\"tablehead\"><td>Identifier</td><td>Location</td><td>Date</td><td>Languages</td><td>Translations</td><td>Images</td></tr>");
            Iterator<DocumentBrowseRecord> rit = returnedRecords.iterator();
            
            while(rit.hasNext()){
            
                DocumentBrowseRecord dbr = rit.next();
                html.append(dbr.getHTML());
                  
            }
            html.append("</table>");
            html.append(doPagination(facets, resultSize));
        }
        
        html.append("</div><!-- closing #facet-records-wrapper -->");
        return html;
          
    }
    
    /**
     * Generates the HTML controls indicating the constraints currently set on each <code>Facet</code>, and that,
     * when clicked, remove the constraint which they designate. 
     * 
     * @param facets
     * @param html
     * @return A <code>StringBuilder</code> holding the HTML for all previous constraints defined on the dataset
     * @see #buildFilteredQueryString(java.util.EnumMap, info.papyri.dispatch.browse.facet.FacetParam, java.lang.String) 
     */
    
    private StringBuilder assemblePreviousValuesHTML(ArrayList<Facet> facets, StringBuilder html, Map<String, String[]> submittedParams){
                
        html.append("<div id=\"previous-values\">");
        
        Iterator<Facet> fit = facets.iterator();
        
        while(fit.hasNext()){
            
            Facet facet = fit.next();
            
            String[] params = facet.getFormNames();
            
            for(int i = 0; i < params.length; i++){
            
                String param = params[i];
                
                if(submittedParams.containsKey(param)){

                    String displayName = facet.getDisplayName(param);

                    ArrayList<String> facetValues = facet.getFacetConstraints(param);

                    Iterator<String> fvit = facetValues.iterator();

                    while(fvit.hasNext()){

                        String facetValue = fvit.next();
                        String displayFacetValue = facet.getDisplayValue(facetValue);
                        String queryString = this.buildFilteredQueryString(facets, facet, param, facetValue);
                        html.append("<div class=\"facet-constraint\">");
                        html.append("<div class=\"constraint-label\">");
                        html.append(displayName);
                        html.append(": ");
                        html.append(displayFacetValue);
                        html.append("</div><!-- closing .constraint-label -->");
                        html.append("<div class=\"constraint-closer\">");
                        html.append("<a href=\"");
                        html.append(FACET_PATH);
                        html.append("".equals(queryString) ? "" : "?");
                        html.append(queryString);
                        html.append("\" title =\"Remove facet value\">X</a>");
                        html.append("</div><!-- closing .constraint-closer -->");
                        html.append("<div class=\"spacer\"></div>");
                        html.append("</div><!-- closing .facet-constraint -->");
                    }

                }
            
            }
                     
        }
                
        html.append("<div class=\"spacer\"></div>");
        html.append("</div><!-- closing #previous-values -->");
        return html;
        
    }
    
    /**
     * Injects the HTML code previously generated by the <code>assembleHTML</code> method
     * 
     * @param response
     * @param html 
     * @see #assembleHTML(java.util.EnumMap, java.lang.Boolean, long, java.util.ArrayList, org.apache.solr.client.solrj.SolrQuery) 
     */
    
    void displayBrowseResult(HttpServletResponse response, String html){
        
        BufferedReader reader = null;
        try{
        
            PrintWriter out = response.getWriter();
            reader = new BufferedReader(new InputStreamReader(FACET_URL.openStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
              
                out.println(line);

                if (line.contains("<!-- Facet browse results -->")) {
            
                    out.println(html);
                
                
                }
          
            } 
        
        }
        catch(Exception e){
            
            
            
        }
        
    }
    
    /**
     * Does what it says.
     * 
     * The complicating factor is that the current query string needs to be regenerated for each link in order
     * to maintain state across the pages.
     * 
     * @param paramsToFacets
     * @param resultSize
     * @return The HTML used for pagination display, as a <code>String</code>
     * @see #buildFullQueryString(java.util.EnumMap) 
     */
    
    private String doPagination(ArrayList<Facet> facets, long resultSize){
        
        int numPages = (int)(Math.ceil(resultSize / documentsPerPage));
        
        if(numPages <= 1) return "";
        
        String fullQueryString = buildFullQueryString(facets);
        
        double widthEach = 8;
        double totalWidth = widthEach * numPages;
        totalWidth = totalWidth > 100 ? 100 : totalWidth;
        
        StringBuilder html = new StringBuilder("<div id=\"pagination\" style=\"width:");
        html.append(String.valueOf(totalWidth));
        html.append("%\">");
        
        for(long i = 1; i <= numPages; i++){
            
            html.append("<div class=\"page\">");
            html.append("<a href=\"");
            html.append(FACET_PATH);
            html.append("?");
            html.append(fullQueryString);
            html.append("page=");
            html.append(i);
            html.append("\">");
            html.append(String.valueOf(i));
            html.append("</a>");
            html.append("</div><!-- closing .page -->");
            
        }
        html.append("<div class=\"spacer\"></div><!-- closing .spacer -->");
        html.append("</div><!-- closing #pagination -->");
        return html.toString();
        
    }
    
    /**
     * Retrieves querystrings from each of the <code>Facet</code>s in turn and concatenates them
     * into a complete querystring.
     * 
     * @param facets
     * @return The complete querystring
     * @see Facet#getAsQueryString() 
     */
    
    private String buildFullQueryString(ArrayList<Facet> facets){
             
        ArrayList<String> queryStrings = new ArrayList<String>();
        
        Iterator<Facet> fit = facets.iterator();
        while(fit.hasNext()){
            
            Facet facet = fit.next();
            String queryString = facet.getAsQueryString();
            if(!"".equals(queryString)) queryStrings.add(queryString);
            
        }
        
        String fullQueryString = "";
        Iterator<String> qsit = queryStrings.iterator();
        while(qsit.hasNext()){
            
            fullQueryString += qsit.next();
            fullQueryString += "&";
            
        }

        return fullQueryString;
        
    }
    
    /**
     * Concatenates the querystring portions retrieved from each <code>Facet</code> into a
     * complete querystring, with the exception of the value given in the passed <code>facetValue</code>, 
     * which is omitted.
     * 
     * The resulting query string is then used in a link, which when clicked gives the appearance
     * that a facet constraint has been removed (as its value is not included in the string.
     * 
     * 
     * @param facets The <code>List</code> of all <code>Facet</code>s
     * @param relFacet The <code>Facet</code> to which the value to be 'removed' belongs
     * @param facetParam The name of the form control associated with the <code>Facet</code> and submitted by the user.
     * @param facetValue The value to be 'removed'
     * @return A filtered querystring.
     * @see Facet#getAsFilteredQueryString(java.lang.String) 
     */
    
    private String buildFilteredQueryString(ArrayList<Facet> facets, Facet relFacet, String facetParam, String facetValue){
              
        ArrayList<String> queryStrings = new ArrayList<String>();
        
        Iterator<Facet> fit = facets.iterator();
        
        while(fit.hasNext()){
            
            String queryString = "";
            
            Facet facet = fit.next();
            if(facet == relFacet){
                
                queryString = facet.getAsFilteredQueryString(facetParam, facetValue);
                
            }
            else{
                
                queryString = facet.getAsQueryString();
                
            }
            
            
            if(!"".equals(queryString)){
                
                queryStrings.add(queryString);
                
            }
            
        }
        
        String filteredQueryString = "";
        Iterator<String> qsit = queryStrings.iterator();
        while(qsit.hasNext()){
            
            filteredQueryString += qsit.next();
            if(qsit.hasNext()) filteredQueryString += "&";
            
        }
        return filteredQueryString;
        
    }
   
    
    ArrayList<String> getAllSortedIds(SolrDocument doc){
        
        ArrayList<String> ids = new ArrayList<String>();
        
        String id = (String) doc.getFieldValue(SolrField.id.name());
        
        // preferred identifier will always be ddbdp identifier
        // based on url
                
        if(id != null && id.matches("/ddbdp/")){
            
            String canonicalId = id.substring("http://papyri.info".length());
            canonicalId = canonicalId.replaceAll("_", " ").trim();
            canonicalId = canonicalId.replaceAll(";;", ";");
            canonicalId = canonicalId.replaceAll(";", " ").trim();
            ids.add(canonicalId);
            
        }
        
        ArrayList<String> ddbdpIds = getCollectionIds("ddbdp", doc);
        if(ddbdpIds.size() > 0) ids.addAll(ddbdpIds);
        ArrayList<String> hgvIds = getCollectionIds("hgv", doc);
        if(hgvIds.size() > 0) ids.addAll(hgvIds);
        
        ArrayList<String> apisPublicationNumbers = doc.getFieldValue(SolrField.apis_publication_id.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.apis_publication_id.name()).toString().replaceAll("\\[\\]", "").split(",")));
        ArrayList<String> apisInventoryNumbers = doc.getFieldValue(SolrField.apis_inventory.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.apis_inventory.name()).toString().replaceAll("\\[\\]", "").split(",")));
        
        // following applies to apis only
        // ideally order should go first publication numbers, then inventory numbers
               
        if(apisPublicationNumbers != null) {

            Iterator<String> pit = apisPublicationNumbers.iterator();
            while(pit.hasNext()){

                pit.next().replaceAll("_", "").trim();


            }
            apisPublicationNumbers = filterIds(apisPublicationNumbers);
            sortIds(apisPublicationNumbers);
            ids.addAll(apisPublicationNumbers);
            
        }      
        
        if(apisInventoryNumbers != null){

            Iterator<String> invit = apisInventoryNumbers.iterator();
            while(invit.hasNext()){

                invit.next().replaceAll("_", "");
            }
            apisInventoryNumbers = filterIds(apisInventoryNumbers);
            sortIds(apisInventoryNumbers);
            ids.addAll(apisInventoryNumbers);

        }
        
        ArrayList<String> apisIds = getCollectionIds("apis", doc);
        if(apisIds.size() > 0) ids.addAll(apisIds);
        return ids;
        
    }
    
    ArrayList<String> getCollectionIds(String collection, SolrDocument doc){
            
        ArrayList<String> collectionMembers = new ArrayList<String>();

        String cpref = collection + "_";

        ArrayList<String> series = doc.getFieldValue(cpref + SolrField.series.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(cpref + SolrField.series.name()).toString().replaceAll("[\\[\\]]",",").split(",")));
        ArrayList<String> volumes = doc.getFieldValue(cpref + SolrField.volume.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(cpref + SolrField.volume.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
        ArrayList<String> itemIds = doc.getFieldValue(cpref + SolrField.full_identifier.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(cpref + SolrField.full_identifier.name()).toString().replaceAll("[\\[\\]]", "").split(",")));

        if(series != null && volumes != null && itemIds != null){

            for(int j = 0; j < series.size(); j++){

                if(j > itemIds.size() - 1) break;
                if(series.get(j).equals("0") || series.get(j).equals("")) break;
                String nowId = series.get(j).replaceAll("_", " ").trim() + " " + ((j > (volumes.size() - 1)) ? "" : volumes.get(j)).replaceAll("_", " ").trim() + " " + itemIds.get(j).replaceAll("_", " ").trim();
                collectionMembers.add(nowId);

            } // closing for j loop

        }    // closing null check

        collectionMembers = filterIds(collectionMembers);
        sortIds(collectionMembers);

        return collectionMembers;
        
    }
    
    ArrayList<String> filterIds(ArrayList<String> rawIds){
        
        ArrayList<String> acceptedIds = new ArrayList<String>();
        
        // weeding out duplicates
        
        Iterator<String> rit = rawIds.iterator();
        
        while(rit.hasNext()){
        
            String id = rit.next();
            
            if(!id.matches("^\\s*$") && !acceptedIds.contains(id)) acceptedIds.add(id);
        
        }        
        
       return acceptedIds;
       
       
    }
    
    void sortIds(ArrayList<String> rawIds){
        
        IdComparator idComparator = new IdComparator();
        Collections.sort(rawIds, idComparator);
        
    }
    
    private Facet findFacet(ArrayList<Facet> facets, Class facetSubClass) throws FacetNotFoundException{
        
        Iterator<Facet> fit = facets.iterator();
        
        while(fit.hasNext()){
            
            Facet facet = fit.next();
            
            if(facet.getClass().equals(facetSubClass)) return facet;
            
            
        }
        
        throw new FacetNotFoundException(facetSubClass.toString());
        
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
