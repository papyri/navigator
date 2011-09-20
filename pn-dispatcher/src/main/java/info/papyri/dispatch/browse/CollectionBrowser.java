package info.papyri.dispatch.browse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Servlet enabling collection-browsing functionality
 * 
 * Note that the nomenclature throughout this code is distinctive in the following ways:
 * (i) The entities to be displayed are thought of as consisting of either <i>documents</i>, 
 * or <i>collections</i> of documents - and note that the latter may have intermediate collections
 * between them and the documents that comprise them. The former are modelled using the
 * <code>DocumentBrowseRecord</code> and <code>DocumentCollectionBrowseRecord</code>s respectively.
 * (ii) To reflect this hierarchical relationship, the standard generational parent -> child 
 * metaphor is used. Collections, then, may be parents of other collections, or of documents; 
 * they may also be children of other collections; and all documents are children of collections.
 * (iii) Perhaps confusingly, the term <i>collection</i> is also used to refer to the top 
 * level of this hierarchy - i.e., to the ddbdp, hgv, or apis collections. The hierarchy
 * as a whole runs collection -> series -> volume -> item
 * 
 * @author thill
 * @see DocumentBrowseRecord
 * @see DocumentCollectionBrowseRecord
 */
@WebServlet(name = "CollectionBrowser", urlPatterns = {"/browse"})
public class CollectionBrowser extends HttpServlet {
    
    /** site home directory */
    static String home;
    /** HTML output is by injection; the browseURL member provides the html page for this */
    static URL browseURL;
    /** Pagination constant */
    static int docsPerPage = 50;
    static String SPARQL_GRAPH;
    static String SOLR_URL;
    static String SPARQL_URL;
    static String BROWSE_SERVLET;
    static String PN_SEARCH;
    static String BASE;
    static String FACET_SERVLET;
    /* an ordered list of the classification hierarchy: collection (ddbdp | hgv | apis), series, volume, and item identifer.
     * note that the ArrayList<String>(Arrays.asList ... construct is simply for ease of declaring literals
     */
    static ArrayList<SolrField> SOLR_FIELDS = new ArrayList<SolrField>(Arrays.asList(SolrField.collection, SolrField.series, SolrField.volume, SolrField.item));
    static ArrayList<String> COLLECTIONS = new ArrayList<String>(Arrays.asList("ddbdp", "hgv", "apis"));
    
    @Override
    public void init(ServletConfig config) throws ServletException{
        
        super.init(config);

        SOLR_URL = config.getInitParameter("solrUrl");
        SPARQL_URL = config.getInitParameter("sparqlUrl");
        SPARQL_GRAPH = "<" + config.getInitParameter("sparqlGraph") + ">";
        BROWSE_SERVLET = config.getInitParameter("browseServletPath");
        FACET_SERVLET = config.getInitParameter("facetBrowserPath");
        home = config.getInitParameter("home");
        BASE = config.getInitParameter("htmlPath");
        PN_SEARCH = config.getInitParameter("pnSearchPath");
        try {
            
            browseURL = new URL("file://" + home + "/" + "browse.html");
            
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
            request.setCharacterEncoding("UTF-8");        
            int page = getPageNumber(request);
            LinkedHashMap<SolrField, String> pathParts = parseRequest(request);
            ArrayList<BrowseRecord> records = new ArrayList<BrowseRecord>();
            long totalDocumentResults = 0;
            if(page == 0){
                
                String sparqlQuery = buildSparqlQuery(pathParts);
                JsonNode resultNode = runSparqlQuery(sparqlQuery);
                records = buildCollectionList(pathParts, resultNode);
                
            }
            else{
                
                 SolrQuery sq = buildSolrQuery(pathParts, page);
                 QueryResponse qr = runSolrQuery(sq);
                 records = parseSolrResponseIntoDocuments(pathParts, qr); 
                 totalDocumentResults = getTotalResultsReturned(qr);
                
            }
            String html = this.buildHTML(pathParts, records, page, totalDocumentResults);
            displayBrowseResult(response, html);

    }
    
    /**
     * Parses the request for the page number of the results requested
     * 
     * 
     * @param request The HttpServletRequest object submitted to the servet
     * @return The page number
     */
    
    private int getPageNumber (HttpServletRequest request){
        
        String queryParam = request.getParameter("q");
        Pattern pattern = Pattern.compile(".+page([\\d]+)$");
        Matcher matcher = pattern.matcher(queryParam);
        if(matcher.matches()) return Integer.valueOf(matcher.group(1));
        return 0;
        
    }
    
    /**
     * Parses the request parameter into a data structure correlating the passed values to the relevant
     * level of the collection hierarchy (collection -> series -> volume -> item).
     * 
     * The request parameter will be of the form /[collection]/[series]?/[volume]?/[documents/page[\d+]]?,
     * with documents/page[\d+] being present iff the page is a list of documents (rather than, e.g., 
     * series or volumes)
     * 
     * @param request the HttpServletRequest made to the servlet
     * @return A <code>LinkedHashMap</code> correlating the passed request params to the relevant levels
     * of the collection hierarchy
     *
     */
    
    private LinkedHashMap<SolrField, String> parseRequest (HttpServletRequest request){
                
        String docPath = "documents";
        String queryParam = request.getParameter("q");
        int docIndex = queryParam.indexOf(docPath);
        String pathInfo = (docIndex == -1 ? queryParam : queryParam.substring(0, docIndex - 1));
        LinkedHashMap<SolrField, String> pathComponents = new LinkedHashMap<SolrField, String>();
        
        String[] pathParts = pathInfo.split("/");
        
        for(int i = 0; i < pathParts.length; i++){

            pathComponents.put(SOLR_FIELDS.get(i), pathParts[i]);
            
        }
        
        return pathComponents;
           
    }
    
    
    /**
     * Retrieves the name of the collection (ddbdp | hgv | apis) currently being browsed and
     * modifies it so that it can be prepended to Solr field names for retrieval purposes.
     * 
     * This method is required because collection and document identifiers are collection-
     * specific, and there Solr fields thus have names like ddbdp_series, apis_series,
     * hgv_volume, etc.
     * 
     * @param pathParts A <code>LinkedHashMap</code> correlating the passed request params to the relevant levels
     * of the collection hierarchy
     * @return The value associated with the <code>collection</code> key of the <code>LinkedHashMap</code>,
     * appended with a slash.
     */
    
    String getCollectionPrefix(LinkedHashMap<SolrField, String> pathParts){
        
        String collection = pathParts.get(SolrField.collection);
        
        if(!COLLECTIONS.contains(collection)) collection = "ddbdp";
        
       return collection + "_";
              
    }
    
    /**
     * Builds a SPARQL query based on the information contained in the <code>pathParts</code> member.
     * 
     * @param pathParts A <code>LinkedHashMap</code> correlating the passed request params to the relevant levels
     * of the collection hierarchy
     * @return String A String that can act as a SPARQL query
     * 
     */ 
    
    String buildSparqlQuery(LinkedHashMap<SolrField, String> pathParts){
        
        StringBuilder queryBuilder = new StringBuilder("PREFIX dc:<http://purl.org/dc/terms/> ");
        queryBuilder.append("PREFIX pyr: <http://papyri.info/> ");
        queryBuilder.append("SELECT ?child ?grandchild ");
        queryBuilder.append("FROM ");
        queryBuilder.append(SPARQL_GRAPH);
        queryBuilder.append(" ");
       
        // extract info from pathbits
        
        // delimiter form: collection/series;volume 
    
        String collection = pathParts.get(SolrField.collection);
        String subj = collection;
        if(pathParts.get(SolrField.series) != null){
            
            subj += "/" + pathParts.get(SolrField.series);
            
            
            
        }
        if(pathParts.get(SolrField.volume) != null){
            
            subj += ";" + pathParts.get(SolrField.volume);
            
        }
        
        queryBuilder.append("WHERE { <pyr:");
        queryBuilder.append(subj);
        queryBuilder.append("> dc:hasPart ?child . ");
        queryBuilder.append("OPTIONAL { ?child dc:hasPart ?grandchild . } }");
        
        return queryBuilder.toString();
        
    }
    
    /**
     * Runs the SPARQL query and returns the result as JSON
     * 
     * @return The root JSON node returned by Mulgara
     * 
     */ 
    
    JsonNode runSparqlQuery(String sparqlQuery){
        
        try{
              
          URL sparq = new URL("http://localhost:8090/sparql/?query=" + URLEncoder.encode(sparqlQuery, "UTF-8") + "&format=json");
          HttpURLConnection http = (HttpURLConnection)sparq.openConnection();
          http.setConnectTimeout(2000);
          ObjectMapper o = new ObjectMapper();
          JsonNode root = o.readValue(http.getInputStream(), JsonNode.class);
          return root.path("results").path("bindings"); 
            
        } 
        catch(Exception e){
            
            e.printStackTrace();
            return null;  
            
        }
          
        
    }
    
    /**
     * Parses the root JsonNode into a list of <code>DocumentCollectionBrowseRecord</code>s.
     * 
     * Note that the returned type is a list of <code>BrowseRecord</code>s (the supertype), rather than
     * <code>DocumentCollectionBrowseRecord</code>s. This is for polymorphic functioning for HTML display 
     * in the <code>processRequest</code> method
     * 
     * @param A <code>LinkedHashMap</code> correlating the passed request params to the relevant levels
     * of the collection hierarchy
     * @param resultNode The root JsonNode returned by Mulgara
     * @return An ArrayList of <code>BrowseRecord</code> objects
     * @see #processRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse) 
     * @see BrowseRecord
     * @see DocumentCollectionBrowseRecord
     * 
     */
    
    ArrayList<BrowseRecord> buildCollectionList(LinkedHashMap<SolrField, String> pathParts, JsonNode resultNode){
        
        ArrayList<BrowseRecord> records = new ArrayList<BrowseRecord>();
        Iterator<JsonNode> rnit = resultNode.iterator();
        ArrayList<String> childLog = new ArrayList<String>();
        while(rnit.hasNext()){
            
            JsonNode result = rnit.next();
            String child = result.path("child").path("value").getValueAsText();
            String grandchild = result.path("grandchild").path("value").getValueAsText();
 
            if(!childLog.contains(child)){
                
                DocumentCollectionBrowseRecord dbr = parseUriToCollectionRecord(pathParts, child, grandchild);
                records.add(dbr);
                childLog.add(child);
                       
            }
            
        }
        
        Collections.sort(records);
        return records;
    }
    
    /**
     * Parses the URI identifiers returned by Mulgara into a <code>DocumentCollectionBrowseRecord</code> 
     * 
     * Note the conceptualisation at work here: the page currently in the process of being displayed is viewed as
     * being the 'parent'; the document collections being displayed <i>on</i> that page are the "children"; and any
     * children these might have are the "grandchildren". The currently displayed entity, in other words, has an
     * <http://purl.org/dc/terms/hasPart> relation to the currently displayed children, as do the children with their 
     * grandchildren.
     * 
     * @param A <code>LinkedHashMap</code> correlating the passed request params to the relevant levels
     * of the collection hierarchy
     * @param child A URI returned from Mulgara and representing a document collection
     * @param grandchild A URI returned from Mulgara, representing a document collection or document 
     * descended from <code>child</code>
     * @return DocumentCollectionBrowseRecord The DocumentCollectionBrowseRecord derived from <code>child</code>
     * and <code>grandchild</code>
     * 
     */
    
    DocumentCollectionBrowseRecord parseUriToCollectionRecord(LinkedHashMap<SolrField, String> pathParts, String child, String grandchild){

        Boolean grandchildIsDocument = grandchild.matches(".*/source$");
        
        String[] uriBits = child.split("/");
        int sIndex = 2;
        String collection = uriBits[sIndex + 1];

        if("apis".equals(collection)){
            
            String series = uriBits[sIndex + 2];
            return new DocumentCollectionBrowseRecord(collection, series, grandchildIsDocument);
            
        }
        String otherInfo = uriBits[sIndex + 2];
        if("ddbdp".equals(collection)){
            
            String delimiter = ";";
            if(otherInfo.indexOf(delimiter) == -1) return new DocumentCollectionBrowseRecord(collection, otherInfo, grandchildIsDocument);
            String[] infoBits = otherInfo.split(delimiter);
            return new DocumentCollectionBrowseRecord(collection, infoBits[0], infoBits[1]);
            
        }
        // hgv records only past this point
        // because there is no real regularity in hgv nomenclature
        // we rely essentially upon getting the 'difference' between the child and grandchild paths
        
        // first, we remove from the child string everything already specified in the url
        
        ArrayList<String> pathInfo = this.getCollectionInfo(pathParts);
        Iterator<String> piits = pathInfo.iterator();
        String significantChildSubstring = child;
        while(piits.hasNext()){
            
            String pinf = piits.next();
            significantChildSubstring = significantChildSubstring.substring(significantChildSubstring.indexOf(pinf) + pinf.length());           
            
        }

        // remove leading slash (arises in the case of collections)
        
        if(significantChildSubstring.startsWith("/")) significantChildSubstring = significantChildSubstring.substring(1);
        
        if(pathParts.size() == SOLR_FIELDS.indexOf(SolrField.series)){
             
             return new DocumentCollectionBrowseRecord(collection, significantChildSubstring, grandchildIsDocument);

            
        }
        else{
            
            
             return new DocumentCollectionBrowseRecord(collection, pathParts.get(SolrField.series), significantChildSubstring);

            
        }

        
    }
    
    /**
     * Creates a SolrQuery object based on the values stored in the passed LinkedHashMap object.
     * 
     * @param A <code>LinkedHashMap</code> correlating the passed request params to the relevant levels
     * of the collection hierarchy
     * @return SolrQuery The SolrQuery object
     */
    SolrQuery buildSolrQuery(LinkedHashMap<SolrField, String> pathParts, int page){
        
        SolrQuery sq = new SolrQuery();
        sq.setStart((page - 1) * docsPerPage);
        sq.setRows(docsPerPage);
        
        String query = "";
        for(Map.Entry<SolrField, String> entry : pathParts.entrySet()){
        
            SolrField field = entry.getKey();
            String value = entry.getValue();
            
            if(field.equals(SolrField.collection)){
                
                // optimization - filter queries are cached
                sq.addFilterQuery(field.name() + ":" + value);
                
            }
            else{
                
                String collField = getCollectionPrefix(pathParts) + field.name();
                query += " +" + collField + ":" + value;
                
            }
        
        }
        if(!query.isEmpty()) sq.setQuery(query);
        sq.addSortField(getCollectionPrefix(pathParts) + SolrField.item.name(), SolrQuery.ORDER.asc);
        sq.addSortField(getCollectionPrefix(pathParts) + SolrField.item_letter.name(), SolrQuery.ORDER.asc);
        return sq;
    }
    
    /**
     * Queries the Solr server and returns the response
     * 
     * @param sq A SolrQuery object
     * @return QueryResponse The response returned from the Solr server
     * 
     */
    
      QueryResponse runSolrQuery(SolrQuery sq){
        
        try{
        
            SolrServer solrServer = new CommonsHttpSolrServer(SOLR_URL + PN_SEARCH);
            QueryResponse qr = solrServer.query(sq);
            return qr;
            
        }
        catch(MalformedURLException mule){
            
            mule.printStackTrace();
            return null;
        }
        catch(SolrServerException sse){
            
            sse.printStackTrace();
            return null;
        }
       
        
    }
      
    /**
       * Determines the total number of results matching the query in the Solr store.
       * 
       * @param qr The <code>QueryRespone</code> returned from the server
       * @return The number of results contained in that <code>QueryResponse</code>.
       */  
      
    long getTotalResultsReturned(QueryResponse qr){
        
        return qr.getResults().getNumFound();      
        
    }
      
    /**
       * Parses the response returned by the Solr server into a list of <code>DocumentBrowseRecord</code>s.
       * 
       * Note that the returned type is a list of <code>BrowseRecord</code>s (the supertype), rather than
       * <code>DocumentBrowseRecord</code>s. This is for polymorphic functioning for HTML display 
       * in the <code>processRequest</code> method
       * 
       * @param qr The QueryResponse returned by the Solr server
       * @return ArrayList<BrowseRecord> A list of the <code>DocumentBrowseRecord</code>s
       * @see BrowseRecord
       * @see DocumentBrowseRecord
       */  
    
    ArrayList<BrowseRecord> parseSolrResponseIntoDocuments(LinkedHashMap<SolrField, String> pathParts, QueryResponse qr){
        
        ArrayList<BrowseRecord> records = new ArrayList<BrowseRecord>();
        ArrayList<String> previousIds = new ArrayList<String>();
                     
        for(SolrDocument doc : qr.getResults()){
            
            try{
                   
                DocumentBrowseRecord record;
                ArrayList<String> allIds = this.getAllIds(doc);
                String preferredId = this.getPreferredId(pathParts, allIds, doc);
                sortIds(allIds);
                URL url = new URL((String)doc.getFieldValue(SolrField.id.name()));
                Boolean titleIsNull = doc.getFieldValue(SolrField.apis_title.name()) == null;
                ArrayList<String> documentTitles = titleIsNull ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.apis_title.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
                Boolean placeIsNull = doc.getFieldValue(SolrField.display_place.name()) == null;
                String place = placeIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.display_place.name());
                Boolean dateIsNull = doc.getFieldValue(SolrField.display_date.name()) == null;
                String date = dateIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.display_date.name());
                Boolean languageIsNull = doc.getFieldValue(SolrField.facet_language.name()) == null;
                String language = languageIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.facet_language.name()).toString().replaceAll("[\\[\\]]", "");
                Boolean noTranslationLanguages = doc.getFieldValue(SolrField.translation_language.name()) == null;
                String translationLanguages = noTranslationLanguages ? "None" : doc.getFieldValue(SolrField.translation_language.name()).toString().replaceAll("[\\[\\]]", "");     
                ArrayList<String> imagePaths = doc.getFieldValue(SolrField.image_path.name()) == null ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.image_path.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
                Boolean hasIllustration = doc.getFieldValue(SolrField.illustrations.name()) == null ? false : true;
                if(!previousIds.contains(preferredId)){
                
                    previousIds.add(preferredId);
                    allIds.remove(preferredId);
                    record = new DocumentBrowseRecord(preferredId, allIds, url, documentTitles, place, date, language, imagePaths, translationLanguages, hasIllustration, "");
                    records.add(record);
                
                }
                
            } catch(MalformedURLException mui){
                
                System.out.println("URL malformed or missing " + mui.getMessage());
                
            }
            
                 
        }
        Collections.sort(records);
        return records;          
        
    }
     
    
    ArrayList<String> getAllIds(SolrDocument doc){
        
        ArrayList<String> allIds = new ArrayList<String>();
        ArrayList<String> collections = doc.getFieldValue(SolrField.collection.name()) == null ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.collection.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
        Iterator<String> cit = collections.iterator();
        
        while(cit.hasNext()){
            
            String collection = cit.next().toLowerCase().trim();
            String collectionPrefix = collection + "_";
            ArrayList<String> series = doc.getFieldValue(collectionPrefix + SolrField.series.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(collectionPrefix + SolrField.series.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
            ArrayList<String> volumes = doc.getFieldValue(collectionPrefix + SolrField.volume.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(collectionPrefix + SolrField.volume.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
            ArrayList<String> itemIds = doc.getFieldValue(collectionPrefix + SolrField.full_identifier.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(collectionPrefix + SolrField.full_identifier.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
            
            if(series != null){
                
                for(int i = 0; i < series.size(); i++){
                    
                    try{
                        
                        if(itemIds.size() != series.size() || (volumes != null && volumes.size() != series.size())) throw new MismatchedIdsException(series.size(), volumes == null ? 0 : volumes.size(), itemIds.size());
                        
                        String nowSeries = series.get(i).replaceAll("_", " ").trim();
                        String nowVolume = (volumes == null || i > volumes.size() - 1) ? "0" : volumes.get(i).replaceAll("_", " ").trim();

                        String nowItem = itemIds.get(i).replaceAll("_", " ").trim();
                        
                        String allInfo = collection + " " + nowSeries + " " + nowVolume + " " + nowItem;
                        allIds.add(allInfo);
                        
                    } catch(MismatchedIdsException mie){
                        
                        allIds.add("Individual id length error: " + mie.getError());

                        
                    } catch(NullPointerException npe){
                        
                        allIds.add("Individual item id missing error: " + npe.getMessage());
                        
                        
                    }
                        
                }
            }            
            
        }
        
        ArrayList<String> apisInventory = doc.getFieldValue(SolrField.apis_inventory.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.apis_inventory.name()).toString().replaceAll("\\[\\]", "").split(",")));
        ArrayList<String> apisPublicationId = doc.getFieldValue(SolrField.apis_publication_id.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.apis_publication_id.name()).toString().replaceAll("\\[\\]", "").split(",")));

        if(apisInventory != null) allIds.addAll(apisInventory);
        if(apisPublicationId != null) allIds.addAll(apisPublicationId);
        
        if(allIds.size() == 0){
            
            allIds.add("All item ids missing error");
            
        }
        
        return allIds;
        
    }
    
    String getPreferredId(LinkedHashMap<SolrField, String> pathParts, ArrayList<String> allIds, SolrDocument doc){
        
        if(pathParts.get(SolrField.collection) == null || pathParts.get(SolrField.series) == null) return "SERIOUS ERROR: Document hierarchy incorrectly defined.";
        
        ArrayList<String> candidates = new ArrayList<String>();
        
        if(pathParts.get(SolrField.collection).toLowerCase().equals("apis")){
            
            ArrayList<String> publicationIds = doc.getFieldValue(SolrField.apis_publication_id.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.apis_publication_id.name()).toString().replaceAll("\\[\\]", "").split(",")));
            
            if(publicationIds != null){
                
                candidates.addAll(publicationIds);
                
            }
            else{
                
                ArrayList<String> inventoryIds = doc.getFieldValue(SolrField.apis_inventory.name()) == null ? null : new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.apis_inventory.name()).toString().replaceAll("\\[\\]", "").split(",")));
                
                if(inventoryIds != null){
                    
                    candidates.addAll(inventoryIds);
                    
                }
                else{
                    
                   String searchPattern = "apis " + pathParts.get(SolrField.series) + " 0 ";
                   
                   Iterator<String> ait = allIds.iterator();
                   
                   while(ait.hasNext()){
                       
                       String id = ait.next();
                       
                       if(id.contains(searchPattern)) candidates.add(id);
                       
                       
                   }
                    
                    
                }
                
            }
            
        }
        else{
            
            String searchCollection = pathParts.get(SolrField.collection);
            String searchSeries = pathParts.get(SolrField.series).replaceAll("_", " ").trim();
            String searchVolume = pathParts.get(SolrField.volume) == null ? "0" : pathParts.get(SolrField.volume).replaceAll("_", " ").trim();
            String searchPattern = searchCollection + " " + searchSeries + " " + searchVolume;
            Iterator<String> ait = allIds.iterator();
            while(ait.hasNext()){
                
                String id = ait.next();
                
                if(id.contains(searchPattern)) candidates.add(id);
                
            }
                  
        }
        
        if(candidates.size() == 0){
            
            String errorMessages = "ERROR: ";
            
            if(allIds.size() > 0){
                
                Iterator<String> ait = allIds.iterator();
                while(ait.hasNext()){
                    
                    errorMessages += ait.next();
                    if(ait.hasNext()) errorMessages += ", ";
                    
                    
                }
                
            }
            else{
                
                errorMessages += " No ids supplied";
                
            }
            
            candidates.add(errorMessages);
            
        }
        
        candidates = filterIds(candidates);
        return candidates.get(0);
        
        
    }
    
    /**
     * Injects the <code>Record</code> HTML into the browse page.
     * 
     * 
     * @param response The <code>HttpResponse</code> object
     * @param html The HTML code to be displayed.
     * @see #browseURL;
     */
    
    void displayBrowseResult(HttpServletResponse response, String html){
        
        BufferedReader reader = null;
        try{
        
            PrintWriter out = response.getWriter();
            reader = new BufferedReader(new InputStreamReader(browseURL.openStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
              
                out.println(line);

                if (line.contains("<!-- Browse results -->")) {
            
                    out.println(html);
                
                
                }
          
            } 
        
        }
        catch(Exception e){
            
            
            
        }
        
    }
    
    /**
     * Converts the value set of the pathBitsMap map into an ArrayList.
     * 
     * Note that this is a workaround; although LinkedHashMaps are ordered, there is no .get(n) method
     * for them. This helper function is accordingly required so that the value set can be iterated through 
     * sequentially
     * 
     * @param pathParts A <code>LinkedHashMap</code> correlating the passed request params to the relevant levels
     * of the collection hierarchy
     * @return an ArrayList<String> of the pathBitsMap value set
     */
    
    private ArrayList<String> getCollectionInfo(LinkedHashMap<SolrField, String> pathParts){
        
        
        ArrayList<String> collectionBits = new ArrayList<String>();
        
        for(Map.Entry<SolrField, String> entry : pathParts.entrySet()){
            
            collectionBits.add(entry.getValue());
            
        }
        
        return collectionBits;
        
    }
    
    
    /**
     * Generates the HTML necessary for display of the browse page.
     * 
     * 
     * @param records The list of records to be displayed
     * @return String A String of html
     */
    String buildHTML(LinkedHashMap<SolrField, String> pathParts, ArrayList<BrowseRecord> records, int page, long totalResults){
        
        StringBuilder html = new StringBuilder("<h2>");
        html.append(pathParts.get(SolrField.collection));
        html.append("</h2>"); 
        html = page != 0 ?  buildDocumentsHTML(pathParts, html, records, totalResults) : buildCollectionsHTML(html, records);
        return html.toString();
        
    }
    
    /**
     * Generates the HTML for display of document collections
     * 
     * 
     * @param html A <code>StringBuffer</code> to store the HTML, and with opening tags already in place
     * @param records The <code>ArrayList</code> of <code>BrowseRecord</code>s to be displayed
     * @return A <code>StringBuffer</code> holding the generated HTML
     * @see DocumentCollectionBrowseRecord#getHTML() 
     */
    
    private StringBuilder buildCollectionsHTML(StringBuilder html, ArrayList<BrowseRecord> records){
       
        int numColumns = records.size() > 20 ? 5 : 1;
        int initTotalPerColumn = (int) Math.floor(records.size() / numColumns);
        int modulus = records.size() - initTotalPerColumn;
        
        for(int currentColumn = 0; currentColumn < numColumns; currentColumn++){
            
           html.append("<ul class=\"collections-column\">");

           int totalThisColumn = initTotalPerColumn;
            
           if(currentColumn < modulus) totalThisColumn++;
           
           if(totalThisColumn > records.size()) totalThisColumn = records.size();
            
           for(int i = 0; i < totalThisColumn; i++){
            
                html.append(records.remove(0).getHTML());
               
            
            }
           
           html.append("</ul>");
            
        }
        
        return html;
        
    }
    
    /**
     * Generates the HTML for the display of document summaries.
     * 
     * Note a dependency here - the html table code here must line up with the html <tr>
     * output of  <code>DocumentBrowseRecord</code>.
     * 
     * @param html A <code>StringBuffer</code> to store the HTML, and with opening tags already in place
     * @param records The <code>ArrayList</code> of <code>BrowseRecord</code>s to be displayed
     * @return A <code>StringBuffer</code> holding the generated HTML
     * @see DocumentBrowseRecord#getHTML() 
     */
    
    private StringBuilder buildDocumentsHTML(LinkedHashMap<SolrField, String> pathParts, StringBuilder html, ArrayList<BrowseRecord> records, long totalResultSetSize){
        
        html.append("<table>");
        html.append("<tr class=\"tablehead\"><td>Identifier</td><td>Location</td><td>Date</td><td>Languages</td><td>Translations</td><td>Images</td></tr>");
        Iterator<BrowseRecord> rit = records.iterator();
        
        while(rit.hasNext()){
            
            BrowseRecord rec = rit.next();
            html.append(rec.getHTML());   
            
        }
        html.append("</table>");
       
        if(totalResultSetSize > docsPerPage){
            
            double rawTotal = (double)totalResultSetSize;
            
            long numPages = (long) Math.ceil(rawTotal / docsPerPage);

            html.append("<div id=\"pagination\">");
                    
            // pagination
            
            String pathBase = BROWSE_SERVLET + "/";
            for(Map.Entry<SolrField, String> entry : pathParts.entrySet()){
            
                pathBase += entry.getValue() + "/";
            
            }
            
            pathBase += "documents/page";
            
            for(int i = 1; i <= numPages; i++){
                
                html.append("<div class=\"page\">");
                html.append("<a href=\"");
                html.append(pathBase);
                html.append(String.valueOf(i));
                html.append("\">");
                html.append(String.valueOf(i));
                html.append("</a></div>");   
                
            }
            
            html.append("</div>");
       
            
        }
        
        return html;
        
    }
    
    ArrayList<String> filterIds(ArrayList<String> rawIds){
        
        ArrayList<String> acceptedIds = new ArrayList<String>();
        
        // weed out duplicates and empties
        
        Iterator<String> rit = rawIds.iterator();
        
        while(rit.hasNext()){
        
            String id = rit.next();
            
            if(id != null && !id.matches("^\\s*$") && !acceptedIds.contains(id)) acceptedIds.add(id);
        
        }
        
        
       return acceptedIds;
       
       
    }
    
    
    void sortIds(ArrayList<String> rawIds){
        
        IdComparator idComparator = new IdComparator();
        Collections.sort(rawIds, idComparator);
        
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
        
        return "Servlet for browsing collections above  individual document level";
        
    }// </editor-fold>
    

    
    
}
