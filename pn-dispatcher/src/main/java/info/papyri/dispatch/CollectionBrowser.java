/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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

/**
 *
 * @author thill
 */
@WebServlet(name = "CollectionBrowser", urlPatterns = {"/browse"})
public class CollectionBrowser extends HttpServlet {
    
    PrintWriter out;    // debugging only 
    private String home = "";
    private String solrUrl;
    private URL browseURL;
    enum SolrField{
        
        collection,
        series,
        identifier,
        volume,
        item,
        display_place,
        display_date,
        has_translation,
        language
        
        
    }
 
    private static String BROWSE_SERVLET = "/browse";
    private static String PN_SEARCH = "pn-search/";
    private static SolrField[] SOLR_FIELDS = {SolrField.collection, SolrField.series, SolrField.volume, SolrField.item};

    @Override
    public void init(ServletConfig config) throws ServletException{
        
        super.init(config);

        solrUrl = config.getInitParameter("solrUrl");
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
        out = response.getWriter();
        try{
            
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet CollectionBrowser</title>");  
            out.println("</head>");
            out.println("<body>");     
            out.println("<h1> " + request.getParameter("coll") + " </h1>");

            
        }catch(Exception e){
            
            System.out.println(e.getStackTrace());
            
        }
 
        
        // create data structure associating hierarchy with submitted params
        LinkedHashMap<SolrField, String> pathBitsMap = this.getPathBitsMap(request); 
        ArrayList<Record> records = this.runQuery(pathBitsMap); 
        String html = records.get(0).getHtmlOpenTags();
        Iterator<Record> rit = records.iterator();
        while(rit.hasNext()){
            
            html += rit.next().getHTML();
            
            
        }
        
        html += records.get(0).getHtmlCloseTags(); 
        out.println(html);
        
        try{
            

            out.println("</body>");
            out.println("</html>");
            
        }
        finally{
            
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
     * Parses <code>Request</code> object's pathInfo String into a data-structure ordered in accordance with the  
     * document category hierarchy.
     * 
     * Specifically, this hierarchy runs collection (ddbdp, hgv, or apis) > series (e.g., p.ross.georg, 
     * o.buch, etc.) > volume (if present) > item (if present); this method ensures that the relevant value
     * submitted is aligned with the relevant level of the hierarchy.
     * 
     * 
     * @param request The <code>HttpServletRequest</code> object passed to the servlet
     * @return <code>LinkedHashMap<String, String></code>: an ordered <code>Map</code> in which the first 
     * <code>String</code> represents a level in the document collection hierarchy, and the second the value for
     * it passed in the Request object.
     * 
     * 
     */
    
    private LinkedHashMap<SolrField, String> getPathBitsMap (HttpServletRequest request){
        
        LinkedHashMap<SolrField, String> pathBitsMap = new LinkedHashMap<SolrField, String>();
        String pathInfo = request.getParameter("coll");
        String[] pathBits = pathInfo.split("/");
        for(int i = 0; i < pathBits.length; i++){
            
            pathBitsMap.put(SOLR_FIELDS[i], pathBits[i]);
            
            
        }
        
        return pathBitsMap;
           
    }
    

    /**
     * Builds a solr query based on the map of hierarchy levels / values associated with it
     * 
     * 
     * @param pathBitsMap The map of hierarchy levels to associated values
     * @return The SolrQuery object
     */
    
    private SolrQuery buildSolrQuery(LinkedHashMap<SolrField, String> pathBitsMap){
                
        SolrQuery sq = new SolrQuery();
        sq.setFacet(true);

        String query = "";
        
        for (Map.Entry<SolrField, String> entry : pathBitsMap.entrySet()){
        
            String field = entry.getKey().name();
            String value = entry.getValue();
          
             if("collection".equals(field) && pathBitsMap.size() > 1){
                
                out.println(field + " : " + value);
                sq.addFilterQuery(field + ":" + value);   
                
            }
            else{
                
                out.println(field + ":" + value);
                query += " +" + field + ":" + value;
                
                
            }
            
        
        }
        if(!query.isEmpty())sq.setQuery(query);
        String nextFieldDown = SOLR_FIELDS[pathBitsMap.size()].name();    // one test - make sure this never hits outOfIndex error
        sq.addFacetField(nextFieldDown);  
        out.println("<p>" + sq.toString()    + "</p>");
        return sq;
        
    }

    
    /**
     * Retrieves results from Solr as Record objects
     * 
     * @param pathBitsMap Map of levels in collections hierarchy to values for these
     * @return ArrayList of Record objects 
     * 
     */
    
    private ArrayList<Record> runQuery(LinkedHashMap<SolrField, String> pathBitsMap) throws MalformedURLException{
        
        
          SolrServer solrServer = new CommonsHttpSolrServer(solrUrl + PN_SEARCH);
          SolrQuery sq = buildSolrQuery(pathBitsMap);

          
          try{
          
              QueryResponse queryResponse = solrServer.query(sq);
              return processSolrResponse(pathBitsMap.size(), queryResponse);
              
              
          }
          catch(SolrServerException sse){
              
              System.out.println("Failure at CollectionBrowser.runQuery: " + sse.getMessage());
              return null;

              
          }
        
    }
    
    /**
     * Parses queryResponse into Record objects - either DocumentGroupRecord objects, or DocumentRecord objects
     * depending on their level in the hierarchy.
     * 
     * 
     * @param level The current level in the hierarchy
     * @param queryResponse The response returned by the query to the solr server
     * @return  An ArrayList of Records
     */
    
    private ArrayList<Record> processSolrResponse(int level, QueryResponse queryResponse){
        
        Boolean nextLevelIsCollection = queryResponse.getFacetFields().get(0).getValueCount() > 1;
        ArrayList<Record> recordsReturned = new ArrayList<Record>();
        
        for(SolrDocument doc : queryResponse.getResults()){
       
            Record record;
            
            String[] levels = new String[3];
                
            for(int i = 0; i <= level; i++){
                    
                  levels[i] = (String) doc.getFieldValue(SOLR_FIELDS[i].name());
                   
            }
            
            DocumentCollectionRecord documentGroupRecord = new DocumentCollectionRecord(levels[0], levels[1], levels[2]);
            
            if(!nextLevelIsCollection){
                

                record = documentGroupRecord; 
                
                
            }
            else{
                
                String item_id = (String) doc.getFieldValue(SolrField.item.name());
                String url = (String) doc.getFieldValue(SolrField.identifier.name());
                String place = (String) doc.getFieldValue(SolrField.display_place.name());
                String date = (String) doc.getFieldValue(SolrField.display_date.name());
                String language = (String) doc.getFieldValue(SolrField.language.name());
                Boolean hasTranslation = doc.getFieldValuesMap().containsKey(SolrField.has_translation.name()) && (Boolean)doc.getFieldValue(SolrField.has_translation.name()) ? true : false;
                
                record = new DocumentRecord(documentGroupRecord, item_id, url, place, date, language, hasTranslation);
                
                
            }
            
            recordsReturned.add(record);
            
            
        }
        
        return recordsReturned;
        
        
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
     * <code>Record</code>s store the salient characteristics of  documents retrieved from solr.
     * 
     * Which characteristics are salient will depend upon whether the user is viewing at the document
     * or document-collection level
     * 
     */
    
    private interface Record{
        
        /**
         * Returns an html representation of the Record 
         * 
         * @return a string of html
         */
        public String getHTML();
        
        /**
         * Returns the relative path to be associated with the Record
         * 
         * @return the relative path as a String
         */
        public String assembleLink();
        
        /**
         * Returns a tag or tags opening the appropriate html element(s) that wraps the list of <code>Record</code>s 
         * 
         * @return An html opening tag or tags
         */
        public String getHtmlOpenTags();
        
        /**
         * Returns closing tags for the <code>Record</code>-wrapping html structure
         * 
         * @return An html closing tag or tags
         */
        public String getHtmlCloseTags();
        
    }
    
    private class DocumentCollectionRecord implements Record {
        
        private String collection;
        private String series;
        private String volume;
    
        public DocumentCollectionRecord(String collection, String series, String volume){
            
            this.collection = collection;
            this.series = series;
            this.volume = volume;
            
        }
        
        
        @Override
        public String getHTML(){
            
            String href = assembleLink();
            String html = "<li><a href='" + href + "'>" + series + " " + volume + "</li>";
            return html;
            
            
        }
        
        @Override
        public String assembleLink(){
            
            String href = BROWSE_SERVLET + "/" + collection;
            
            String seriesIdent = series.isEmpty() ? "" : "/" + series;
            String volumeIdent = volume.isEmpty() ? "" : "/" + volume;
            
            href += seriesIdent + volumeIdent;
            return href;
            
        }
        
        @Override 
        public String getHtmlOpenTags(){ 
        
            
            return "<ul id='document-collection-list'>";
        
        }
        
        @Override
        public String getHtmlCloseTags(){
            
            return "</ul>";
            
        }
        
        public String getSeries(){ return series; }
        public String getVolume(){ return volume; }
        
    }
    
    private class DocumentRecord implements Record {
        
        private DocumentCollectionRecord documentGroupRecord;
        private String display_id;
        private String url;
        private String place;
        private String date;
        private String language;        
        private String hasTranslation;

        public DocumentRecord(DocumentCollectionRecord dgr, String item_id, String url, String place, String date, String lang, Boolean hasTrans){
            
            // this will have to be changed depending on what users want to see in the records
            
            this.documentGroupRecord = dgr;
            this.display_id = item_id;
            this.url = url;
            this.place = place;
            this.date = date;
            this.language = lang;
            this.hasTranslation = hasTrans ? "Yes" : "No";
            
            
        }
        
        @Override
        public String getHTML(){
            
            String displayName = this.documentGroupRecord.getSeries() + " " + this.documentGroupRecord.getVolume() + " " + this.display_id;
            String anchor = "<a href='" + this.assembleLink() + "'>" + displayName + "</a>";
            String html = "<tr><td>" + anchor + "</td>";
            html += "<td>" + place + "</td>";
            html += "<td>" + date + "</td>";
            html += "<td>" + language + "</td>";
            html += "<td>" + hasTranslation + "</td>";
            html += "</tr>";
            return html;
            
        }
        
        @Override
        public String assembleLink(){
            
            return this.url;
                      
        }
        
        @Override 
        public String getHtmlOpenTags(){ 
        
            return "<table id='document-table'>";
        
        }
        
        @Override
        public String getHtmlCloseTags(){
            
            
            return "</table>";
            
        }
        
        
        
    }
    
    
}
