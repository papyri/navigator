package info.papyri.dispatch.browse;

import info.papyri.dispatch.FileUtils;
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
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Servlet enabling collection-browsing functionality
 * 
 * The design of this servlet is very simple: it parses the request for paths of the form
 * /[collection][/series]?[/volume]? and translates these into SPARQL queries. The only wrinkle
 * in this process is the irregularity of document collection hierarchies: while some series are
 * subdivided into volumes, others are not. In order to negotiate this irregularity, an optional
 * SPARQL clause checking whether the children of the current collection's contents are documents 
 * or collections of documents is added.
 * 
 * When the display reaches document level the user is rerouted to the faceted browser; i.e.,
 * when the display is at one level *above* document level, the links provided for drilling
 * deeper into the collections are to the faceted browser, with appropriate query-string
 * arguments.
 * 
 * 
 * @author thill
 * @see CollectionBrowser#parseUriToCollectionRecord(java.util.LinkedHashMap, java.lang.String, java.lang.String, java.lang.String)  
 * @see DocumentCollectionBrowseRecord
 */
@WebServlet(name = "CollectionBrowser", urlPatterns = {"/browse"})
public class CollectionBrowser extends HttpServlet {
    
    /** site home directory */
    static String home;
    /** HTML output is by injection; the browseURL member provides the html page for this */
    static URL browseURL;
    /** SPARQL-point URL */
    static String SPARQL_URL;
    /** Name of the SPARQL graph to be queried */
    static String SPARQL_GRAPH;
    /** URL of this servlet */
    static String BROWSE_SERVLET; 
    /** Faceted browser servlet URL*/
    static String FACET_SERVLET;
    /* an ordered list of the classification hierarchy: collection (ddbdp | hgv | apis), series, volume, and item identifer.
     * note that the ArrayList<String>(Arrays.asList ... construct is simply for ease of declaring literals
     */
    static ArrayList<SolrField> ORG_HIERARCHY = new ArrayList<SolrField>(Arrays.asList(SolrField.collection, SolrField.series, SolrField.volume));
    static ArrayList<String> COLLECTIONS = new ArrayList<String>(Arrays.asList("ddbdp", "hgv", "apis", "dclp"));
    
    @Override
    public void init(ServletConfig config) throws ServletException{
        
        super.init(config);

        SPARQL_URL = config.getInitParameter("sparqlUrl");
        SPARQL_GRAPH = "<" + config.getInitParameter("sparqlGraph") + ">";
        BROWSE_SERVLET = config.getInitParameter("browseServletPath");
        FACET_SERVLET = config.getInitParameter("facetBrowserPath");
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
            EnumMap<SolrField, String> pathParts = parseRequest(request);
            ArrayList<DocumentCollectionBrowseRecord> records = new ArrayList<DocumentCollectionBrowseRecord>();
            String sparqlQuery = buildSparqlQuery(pathParts);
            JsonNode resultNode = runSparqlQuery(sparqlQuery);
            records = buildCollectionList(pathParts, resultNode);    
            String html = this.buildHTML(pathParts, records);
            displayBrowseResult(response, html);
    }
    
    /**
     * Parses the request parameter into a data structure correlating the passed values to the relevant
     * levels of the collection hierarchy (collection -> series -> volume).
     * 
     * The request parameter will be of the form /[collection][/series]?[/volume]?
     * 
     * @param request the HttpServletRequest made to the servlet
     * @return A <code>LinkedHashMap</code> correlating the passed request params to the relevant levels
     * of the collection hierarchy
     *
     */
    
    private EnumMap<SolrField, String> parseRequest (HttpServletRequest request){
                
        String docPath = "documents";
        String queryParam = request.getParameter("q");
        int docIndex = queryParam.indexOf(docPath);
        String pathInfo = (docIndex == -1 ? queryParam : queryParam.substring(0, docIndex - 1));
        EnumMap<SolrField, String> pathComponents = new EnumMap<SolrField, String>(SolrField.class);
        
        String[] pathParts = pathInfo.split("/");
        
        for(int i = 0; i < pathParts.length && i < ORG_HIERARCHY.size(); i++){

            pathComponents.put(ORG_HIERARCHY.get(i), pathParts[i]);
            
        }
        
        return pathComponents;
           
    }
    
    /**
     * Builds a SPARQL query based on the information contained in the <code>pathParts</code> member.
     * 
     * @param pathParts A <code>LinkedHashMap</code> correlating the passed request params to the relevant levels
     * of the collection hierarchy
     * @return String A String that can act as a SPARQL query
     * 
     */ 
    
    String buildSparqlQuery(EnumMap<SolrField, String> pathParts){
        
        StringBuilder queryBuilder = new StringBuilder("PREFIX dc:<http://purl.org/dc/elements/1.1/> ");
        queryBuilder.append("PREFIX dcterms: <http://purl.org/dc/terms/> ");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
        queryBuilder.append("SELECT ?child ?label ?type ?parent ");
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
        
        queryBuilder.append("WHERE { <http://papyri.info/");
        queryBuilder.append(subj);
        queryBuilder.append("> dcterms:hasPart ?child . ");
        queryBuilder.append("OPTIONAL {<http://papyri.info/");
        queryBuilder.append(subj);
        queryBuilder.append("> dcterms:bibliographicCitation ?parent . } ");
        queryBuilder.append("OPTIONAL { ?child dcterms:bibliographicCitation ?label . } ");
        queryBuilder.append("OPTIONAL { ?child rdf:type ?type . } }");

        return queryBuilder.toString();
        
    }
    
    /**
     * Runs the SPARQL query and returns the result as JSON
     * 
     * @return The root JSON node returned by Fuseki
     * 
     */ 
    
    JsonNode runSparqlQuery(String sparqlQuery){
        
        try{
              
          URL sparq = new URL("http://localhost:8090/pi/query?query=" + URLEncoder.encode(sparqlQuery, "UTF-8") + "&output=json");
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
     * @param A <code>LinkedHashMap</code> correlating the passed request params to the relevant levels
     * of the collection hierarchy
     * @param resultNode The root JsonNode returned by Mulgara
     * @return An ArrayList of <code>BrowseRecord</code> objects
     * @see #processRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse) 
     * @see BrowseRecord
     * @see DocumentCollectionBrowseRecord
     * 
     */
    
    ArrayList<DocumentCollectionBrowseRecord> buildCollectionList(EnumMap<SolrField, String> pathParts, JsonNode resultNode){
        
        ArrayList<DocumentCollectionBrowseRecord> records = new ArrayList<DocumentCollectionBrowseRecord>();
        Iterator<JsonNode> rnit = resultNode.iterator();
        ArrayList<String> childLog = new ArrayList<String>();
        while(rnit.hasNext()){
            
            JsonNode result = rnit.next();
            String child = result.path("child").path("value").getValueAsText();
            String type = result.path("type").path("value").getValueAsText();
            String label = stringifyUnicodeLiterals(result.path("label").path("value").getValueAsText());
            String parent = stringifyUnicodeLiterals(result.path("parent").path("value").getValueAsText());
            if(!childLog.contains(child)){
                
                DocumentCollectionBrowseRecord dbr = parseUriToCollectionRecord(pathParts, child, type, label, parent);
                records.add(dbr);
                childLog.add(child);
                       
            }
            
        }
        
        Collections.sort(records);
        return records;
    }
    
    /**
     * Converts strings from the unicode-escaping used by mulgara to 
     * 
     * 
     * @param lbl
     * @return 
     */
    
    private String stringifyUnicodeLiterals(String lbl){
        
        if(lbl == null || lbl.equals("")) return "";
        
        String cleanLabel = "";
        
        char[] labelBits = lbl.toCharArray();
        
        for(int i = 0; i < labelBits.length; i++){
            
            cleanLabel += labelBits[i];
            
        }
        
        return cleanLabel;
        
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
    
    DocumentCollectionBrowseRecord parseUriToCollectionRecord(EnumMap<SolrField, String> pathParts, String child, String type, String label, String parentLabel){
        
        String[] uriBits = child.split("/");
        int sIndex = 2;
        String collection = uriBits[sIndex + 1];

        if("apis".equals(collection)){
            
            String series = uriBits[sIndex + 2];
            return new DocumentCollectionBrowseRecord(collection, series, true);
            
        }
        String otherInfo = uriBits[sIndex + 2];
        if("ddbdp".equals(collection)){
            
            String delimiter = ";";
            if(otherInfo.indexOf(delimiter) == -1) return new DocumentCollectionBrowseRecord(collection, otherInfo, "http://purl.org/ontology/bibo/Book".equals(type));
            String[] infoBits = otherInfo.split(delimiter);
            return new DocumentCollectionBrowseRecord(collection, infoBits[0], infoBits[1]);
            
        }
        // we're in HGV-land
        if ("dclp".equals(collection) || parentLabel == null || "".equals(parentLabel)) {
          return new DocumentCollectionBrowseRecord(collection, label, "http://purl.org/ontology/bibo/Book".equals(type));
        } else {
          return new DocumentCollectionBrowseRecord(collection, parentLabel, FileUtils.substringAfter(label, parentLabel).trim());
        }
        
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
        catch(Exception e){ }
        
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
     * @deprecated 
     */
    String buildHTML(LinkedHashMap<SolrField, String> pathParts, ArrayList<DocumentCollectionBrowseRecord> records){
        
        StringBuilder html = new StringBuilder("<h2>");
        html.append(pathParts.get(SolrField.collection));
        html.append("</h2>"); 
        html = buildCollectionsHTML(html, records);
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
    
    private StringBuilder buildCollectionsHTML(StringBuilder html, ArrayList<DocumentCollectionBrowseRecord> records){
       
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


  private String buildHTML(EnumMap<SolrField, String> pathParts, ArrayList<DocumentCollectionBrowseRecord> records) {
    StringBuilder html = new StringBuilder("<h2>");
    html.append(pathParts.get(SolrField.collection));
    html.append("</h2>"); 
    html = buildCollectionsHTML(html, records);
    return html.toString();
  }
    

    
    
}
