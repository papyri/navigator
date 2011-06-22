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
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashMap;
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
 * 
 * @author thill
 */
@WebServlet(name = "CollectionBrowser", urlPatterns = {"/browse"})
public class CollectionBrowser extends HttpServlet {
    
    /* site home page */
    private String home;
    /* HTML output is by injection; the browseURL member provides the html page for this */
    private URL browseURL;
    /* some Solr fields are collection (ddbdp | hgv | apis) specific; this member, when set
     * provides the prefix that needs to be added to access these fields
     */
    private String collectionPrefix;

    /* for pagination: current page, or 0 if browsing at collection, series, or volume level */
    private int page = 0;
    private int docsPerPage = 50;
    private int totalResultSetSize;

    /* holds information given in request url*/
    private LinkedHashMap<SolrField, String> pathBits;
     
    enum SolrField{
        
        collection,
        series,
        identifier,
        volume,
        item,
        display_place,
        display_date,
        has_translation,
        language,
        hgv_identifier
        
        
    }
 
    private static String SPARQL_GRAPH = "<rmi://localhost/papyri.info#pi>";
    static String SOLR_URL;
    static String SPARQL_URL;
    static String BROWSE_SERVLET = "/browse";
    static String PN_SEARCH = "pn-search/";
    static ArrayList<SolrField> SOLR_FIELDS = new ArrayList<SolrField>(Arrays.asList(SolrField.collection, SolrField.series, SolrField.volume, SolrField.item));
    static ArrayList<String> COLLECTIONS = new ArrayList<String>(Arrays.asList("ddbdp", "hgv", "apis"));
    
    @Override
    public void init(ServletConfig config) throws ServletException{
        
        super.init(config);

        SOLR_URL = config.getInitParameter("solrUrl");
        SPARQL_URL = config.getInitParameter("sparqlUrl");
        home = config.getInitParameter("home");
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
            parseRequest(request); 
            ArrayList<BrowseRecord> records = new ArrayList<BrowseRecord>();
            if(page == 0){
                
                String sparqlQuery = buildSparqlQuery();
                JsonNode resultNode = processSparqlQuery(sparqlQuery);
                records = buildCollectionList(resultNode);
                
            }
            else{
                
                 setCollectionPrefix();
                 SolrQuery sq = buildSolrQuery();
                 QueryResponse qr = runSolrQuery(sq);
                 records = parseSolrResponseIntoDocuments(qr); 
                
            }
            String html = this.buildHTML(records);
            displayBrowseResult(response, html);

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

    
    private void parseRequest (HttpServletRequest request){
        
        String docPath = "documents";
        String queryParam = request.getParameter("q");
        int docIndex = queryParam.indexOf(docPath);
        String pathInfo = (docIndex == -1 ? queryParam : queryParam.substring(0, docIndex -1));
        pathBits = new LinkedHashMap<SolrField, String>();
        String[] pathParts = pathInfo.split("/");
        
        for(int i = 0; i < pathParts.length; i++){

            pathBits.put(SOLR_FIELDS.get(i), pathParts[i]);
            
        }
        
        if(docIndex != -1){
              
            String pageBit = queryParam.substring(docIndex + docPath.length() + 1, queryParam.length());
            page = Integer.valueOf(pageBit.replaceAll("[^\\d]", ""));
            
        }
        else{
            
            page = 0;
            
        }
           
    }
    
    void setCollectionPrefix(){
        
        String collection = pathBits.get(SolrField.collection);
        
        if(!COLLECTIONS.contains(collection)) collection = "ddbdp";
        
        collectionPrefix = collection + "_";
              
    }
    
    String buildSparqlQuery(){
        
        StringBuffer queryBuffer = new StringBuffer("PREFIX dc:<http://purl.org/dc/terms/> ");
        queryBuffer.append("PREFIX pyr: <http://papyri.info/> ");
        queryBuffer.append("SELECT ?child ?grandchild ");
        queryBuffer.append("FROM " + SPARQL_GRAPH + " ");
       
        // extract info from pathbits
        
        // delimiter rules: ddbdp - collection/series series;volume 
        //                  hgv -   collection/series series_volume 
        //                  apis -  collection/series [no volume] 
      
        String collection = pathBits.get(SolrField.collection);
        String subj = collection;
        if(pathBits.get(SolrField.series) != null){
            
            subj += "/" + pathBits.get(SolrField.series);
            
            
            
        }
        if(pathBits.get(SolrField.volume) != null){
            
            subj += ";" + pathBits.get(SolrField.volume);
            
        }
        
        queryBuffer.append("WHERE { <pyr:" + subj + "> dc:hasPart ?child . ");
        queryBuffer.append("OPTIONAL { ?child dc:hasPart ?grandchild . } }");
        
        return queryBuffer.toString();
        
    }
    
    JsonNode processSparqlQuery(String sparqlQuery){
        
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
    
    ArrayList<BrowseRecord> buildCollectionList(JsonNode resultNode){
        
        ArrayList<BrowseRecord> records = new ArrayList<BrowseRecord>();
        Iterator<JsonNode> rnit = resultNode.iterator();
        ArrayList<String> childLog = new ArrayList<String>();
        while(rnit.hasNext()){
            
            JsonNode result = rnit.next();
            String child = result.path("child").path("value").getValueAsText();
            String grandchild = result.path("grandchild").path("value").getValueAsText();
 
            if(!childLog.contains(child)){
                
                DocumentCollectionBrowseRecord dbr = parseUriToCollectionRecord(child, grandchild);
                records.add(dbr);
                childLog.add(child);
                       
            }
            
        }
        
        return records;
    }
    
    DocumentCollectionBrowseRecord parseUriToCollectionRecord(String child, String grandchild){

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
        
        ArrayList<String> pathInfo = this.getCollectionInfo(pathBits);
        Iterator<String> piits = pathInfo.iterator();
        String significantChildSubstring = child;
        while(piits.hasNext()){
            
            String pinf = piits.next();
            significantChildSubstring = significantChildSubstring.substring(significantChildSubstring.indexOf(pinf) + pinf.length());           
            
        }

        // remove leading slash (arises in the case of collections)
        
        if(significantChildSubstring.startsWith("/")) significantChildSubstring = significantChildSubstring.substring(1);
        
        if(pathBits.size() == SOLR_FIELDS.indexOf(SolrField.series)){
             
             return new DocumentCollectionBrowseRecord(collection, significantChildSubstring, grandchildIsDocument);

            
        }
        else{
            
            
             return new DocumentCollectionBrowseRecord(collection, pathBits.get(SolrField.series), significantChildSubstring);

            
        }

        
    }
        
    SolrQuery buildSolrQuery(){
        
        SolrQuery sq = new SolrQuery();
        sq.setStart((page - 1) * docsPerPage);
        sq.setRows(docsPerPage);
        String query = "";
        for(Map.Entry<SolrField, String> entry : pathBits.entrySet()){
        
            SolrField field = entry.getKey();
            String value = entry.getValue();
            
            if(field.equals(SolrField.collection)){
                
                sq.addFilterQuery(field.name() + ":" + value);
                
            }
            else{
                
                String collField = collectionPrefix + field.name();
                query += " +" + collField + ":" + value;
                
            }
        
        }
        if(!query.isEmpty()) sq.setQuery(query);
        sq.addSortField(SolrField.item.name(), SolrQuery.ORDER.asc);
        sq.addSortField(collectionPrefix + SolrField.series.name(), SolrQuery.ORDER.asc);
        sq.addSortField(collectionPrefix + SolrField.volume.name(), SolrQuery.ORDER.asc);
        return sq;
    }
    
      QueryResponse runSolrQuery(SolrQuery sq){
        
        try{
        
            SolrServer solrServer = new CommonsHttpSolrServer(SOLR_URL + PN_SEARCH);
            QueryResponse qr = solrServer.query(sq);
            totalResultSetSize = qr.getResults().size();
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
    
    ArrayList<BrowseRecord> parseSolrResponseIntoDocuments(QueryResponse qr){
        
        ArrayList<BrowseRecord> records = new ArrayList<BrowseRecord>();
                
        ArrayList<String> collectionInfo = getCollectionInfo(pathBits);
        DocumentCollectionBrowseRecord dcr = collectionInfo.size() > 2 ? new DocumentCollectionBrowseRecord(collectionInfo.get(0), collectionInfo.get(1), collectionInfo.get(2)) : new DocumentCollectionBrowseRecord(collectionInfo.get(0), collectionInfo.get(1), true);
   
        for(SolrDocument doc : qr.getResults()){
                   
            try{
                
                DocumentBrowseRecord record;
                ArrayList<String> itemIds = getDisplayIds(doc);
                String ddbdpDids = this.convertIdArraysToStrings(doc, "ddbdp");
                String hgvDids = this.convertIdArraysToStrings(doc, "hgv");
                String apisDids = this.convertIdArraysToStrings(doc, "apis");
                String itemId = (String) doc.getFieldValue(SolrField.item.name());
                Boolean placeIsNull = doc.getFieldValue(SolrField.display_place.name()) == null;
                String place = placeIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.display_place.name());
                Boolean dateIsNull = doc.getFieldValue(SolrField.display_date.name()) == null;
                String date = dateIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.display_date.name());
                Boolean languageIsNull = doc.getFieldValue(SolrField.language.name()).equals(null);
                String language = languageIsNull ? "Not recorded" : (String) doc.getFieldValue(SolrField.language.name()).toString().replaceAll("[\\[\\]]", "");
                Boolean hasTranslation = doc.getFieldValuesMap().containsKey(SolrField.has_translation.name()) && (Boolean)doc.getFieldValue(SolrField.has_translation.name()) ? true : false;
 
                if(pathBits.get(SolrField.collection).equals("hgv")){
                    
                   ArrayList<String> hgvIds = new ArrayList<String>(Arrays.asList(doc.getFieldValue(SolrField.hgv_identifier.name()).toString().replaceAll("[\\]\\[]", "").split(","))); 
                   
                   String hgvId = hgvIds.get(0);
                   record = new DocumentBrowseRecord(dcr, itemIds, itemId, ddbdpDids, hgvDids, apisDids, place, date, language, hasTranslation, hgvId);
                   records.add(record);
                }
                 else{
                    
                    record = new DocumentBrowseRecord(dcr, itemIds, itemId, ddbdpDids, hgvDids, apisDids, place, date, language, hasTranslation);
                    records.add(record);
                 }
                    
                
            
                } catch (NullPointerException npe){
                
                // exception catch needed for docs with missing fields. need a better way to deal with this?
                
                    System.out.println("Missing document " + doc.toString() + ": " + npe.getMessage());
                
                
                }
            
        }
        
        return records;          
        
    }
    
    private String convertIdArraysToStrings(SolrDocument doc, String collection){
        
        String fieldName = collection + "_" + SolrField.item.name();
        String fieldValue = doc.getFieldValue(fieldName).toString();
        if(fieldValue == null || fieldValue.equals("0")) return "None";
        fieldValue = fieldValue.replaceAll("[\\[\\]]", "");
        ArrayList<String> allIds = new ArrayList<String>(Arrays.asList(fieldValue.split(",")));
        ArrayList<String> trimmedIds = new ArrayList<String>();
        Iterator<String> ait = allIds.iterator();
        while(ait.hasNext()){
            
            String id = ait.next().replaceAll("[\\s]", "");
            if(!trimmedIds.contains(id)) trimmedIds.add(id);
            
        }
        
        return trimmedIds.toString().replaceAll("[\\[\\]]", "");
        
    }
    
    private ArrayList<String> getDisplayIds(SolrDocument doc){

        ArrayList<String> ids = new ArrayList<String>(Arrays.asList(doc.getFieldValue(collectionPrefix + SolrField.item.name()).toString().replaceAll("[\\[\\]]", "").split(",")));
        
        return ids;
        
    } 
    
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
     * in order
     * 
     * @param pathBitsMap
     * @return an ArrayList<String> of the pathBitsMap value set
     */
    
    private ArrayList<String> getCollectionInfo(LinkedHashMap<SolrField, String> pathBitsMap){
        
        
        ArrayList<String> collectionBits = new ArrayList<String>();
        
        for(Map.Entry<SolrField, String> entry : pathBitsMap.entrySet()){
            
            collectionBits.add(entry.getValue());
            
        }
        
        return collectionBits;
        
    }
    
    String buildHTML(ArrayList<BrowseRecord> records){
        
        StringBuffer html = new StringBuffer("<h2>" + pathBits.get(SolrField.collection) + "</h2>");
        html = page != 0 ?  buildDocumentsHTML(html, records) : buildCollectionsHTML(html, records);
        return html.toString();
        
    }
    
    private StringBuffer buildCollectionsHTML(StringBuffer html, ArrayList<BrowseRecord> records){
       
        String listHeader = "<ul style=\"margin-left:2em;float:left;\">";
        int columnLength = 25;
        
        html.append(listHeader);
        
        for(int i = 0; i < records.size(); i++){
            
            html.append(records.get(i).getHTML());
            
            if(i == (records.size() - 1)){
                
                html.append("</ul>");
                
            }
            else if((i + 1) % columnLength == 0){
                
                html.append("</ul>");
                html.append(listHeader);
                
                
            }
            
        }
        
        return html;
        
    }
    
    private StringBuffer buildDocumentsHTML(StringBuffer html, ArrayList<BrowseRecord> records){
        
        html.append("<table>");
        html.append("<tr class=\"tablehead\"><td>Identifier</td><td>Location</td><td>Date</td><td>All DDbDP IDs</td><td>All HGV IDs</td><td>All APIS IDs</td><td>Languages</td><td>Has translation</td></tr>");
        Iterator<BrowseRecord> rit = records.iterator();
        
        while(rit.hasNext()){
            
            BrowseRecord rec = rit.next();
            html.append(rec.getHTML());   
            
        }
        html.append("</table>");
       
        if(totalResultSetSize > docsPerPage){
            
            int numPages = (int) Math.ceil(totalResultSetSize / docsPerPage);

            html.append("<div id=\"pagination\">");
                    
            // pagination
            
            String pathBase = BROWSE_SERVLET + "/";
            for(Map.Entry<SolrField, String> entry : pathBits.entrySet()){
            
                pathBase += entry.getValue() + "/";
            
            }
            
            pathBase += "documents/page";
            
            for(int i = 1; i <= numPages; i++){
                
                html.append("<div class=\"page\">");
                html.append("<a href=\"" + pathBase + String.valueOf(i) + "\">");
                html.append(String.valueOf(i));
                html.append("</a></div>");   
                
            }
       
            
        }
        
        return html;
        
    }
    
    
    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        
        return "Servlet for browsing collections above  individual document level";
        
    }// </editor-fold>

    /**
     * Testing only
     * 
     * 
     */
    
    void setPathBits(LinkedHashMap<SolrField, String> bits){
        
        pathBits = bits;
    }
    

    
    
}
