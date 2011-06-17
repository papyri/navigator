/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
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
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 *
 * @author thill
 */
@WebServlet(name = "CollectionBrowser", urlPatterns = {"/browse"})
public class CollectionBrowser extends HttpServlet {
    
    private String home = "";
    private URL browseURL;
    private String collectionPrefix;

    private int page = 0;
    private int docsPerPage = 50;
    private Boolean currentlyDisplayingDocuments;
    private LinkedHashMap<SolrField, String> pathBits;
    private int totalResultSetSize;
    
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
 
    static String SOLR_URL;
    static String BROWSE_SERVLET = "/browse";
    static String PN_SEARCH = "pn-search/";
    static ArrayList<SolrField> SOLR_FIELDS = new ArrayList<SolrField>(Arrays.asList(SolrField.collection, SolrField.series, SolrField.volume, SolrField.item));
    static ArrayList<String> COLLECTIONS = new ArrayList<String>(Arrays.asList("ddbdp", "hgv", "apis"));
    
    @Override
    public void init(ServletConfig config) throws ServletException{
        
        super.init(config);

        SOLR_URL = config.getInitParameter("solrUrl");
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
        setCollectionPrefix();
        QueryResponse queryResponse = this.runQuery();
        ArrayList<Record> records = this.processSolrResponse(queryResponse);
        String html = this.buildHTML(records);
        displayBrowseResult(response, html);

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
        
        if(docIndex == -1){
            
            if((pathBits.size() - 1) >= SOLR_FIELDS.indexOf(SolrField.volume)){
                
                   page = 1;
                   
            }
            else{
                   page = 0;
            }
            
        }
        else{
            
            String pageBit = queryParam.substring(docIndex + docPath.length() + 1, queryParam.length());
            page = Integer.valueOf(pageBit.replaceAll("[^\\d]", ""));
            
        }
           
    }
    
    void setCollectionPrefix(){
        
        String collection = pathBits.get(SolrField.collection);
        
        if(!COLLECTIONS.contains(collection)) collection = "ddbdp";
        
        collectionPrefix = collection + "_";
        
        
    }

    /**
     * Builds a solr query based on the map of hierarchy levels / values associated with it
     * 
     * 
     * @param pathBitsMap The map of hierarchy levels to associated values
     * @return The SolrQuery object
     */
    
    SolrQuery buildSolrQuery(){
                
        SolrQuery sq = new SolrQuery();
        
        if(page != 0){
            
            sq.setStart((page - 1) * docsPerPage);
            sq.setRows(docsPerPage);
        
        }else{
            
            sq.setRows(1000);
            
        }    

        String query = "";
        
        for (Map.Entry<SolrField, String> entry : pathBits.entrySet()){
        
            String field = entry.getKey().name();
            String value = entry.getValue();
          
             if("collection".equals(field)){
                
                 if(pathBits.size() > 1){
                     
                       sq.addFilterQuery(field + ":" + value);   
                 
                 }
                 else{
                     
                       query += " +" + field + ":" + value;
                     
                 }
                 
                
            }
            else{
                
                field = collectionPrefix + field;
                query += " +" + field + ":" + value;
                
                
            }
            
        
        }

        if(!query.isEmpty())sq.setQuery(query);
        if((pathBits.size()) < SOLR_FIELDS.indexOf(SolrField.item)){
            
            sq.setFacet(true);
            sq.setFacetLimit(-1);
            sq.addFacetField(collectionPrefix + SOLR_FIELDS.get(pathBits.size())); 
            sq.setFacetMinCount(1);

        }
        
      sq.addSortField(SolrField.item.name(), SolrQuery.ORDER.asc);
      sq.addSortField(collectionPrefix + SolrField.series.name(), SolrQuery.ORDER.asc);
      sq.addSortField(collectionPrefix + SolrField.volume.name(), SolrQuery.ORDER.asc);
      return sq;
        
    }

    
    /**
     * Retrieves results from Solr as Record objects
     * 
     * @param pathBitsMap Map of levels in collections hierarchy to values for these
     * @return ArrayList of Record objects 
     * 
     */
    
    QueryResponse runQuery() throws MalformedURLException{
        
        
          SolrServer solrServer = new CommonsHttpSolrServer(SOLR_URL + PN_SEARCH);
          SolrQuery sq = buildSolrQuery();

          
          try{
          
              QueryResponse queryResponse = solrServer.query(sq);
              return queryResponse;
              
              
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
     * @param pathBitsMap Map of levels in collections hierarchy to values for these
     * @param queryResponse The response previously returned by the query to the solr server
     * @return  An ArrayList of Records
     */
    
    ArrayList<Record> processSolrResponse(QueryResponse queryResponse){

        totalResultSetSize = (int) queryResponse.getResults().getNumFound();
        currentlyDisplayingDocuments = isCurrentlyDisplayingDocuments(queryResponse); 
        ArrayList<Record> recordsReturned = currentlyDisplayingDocuments ? buildDocumentList(queryResponse) : buildCollectionsList(queryResponse) ;  
        if(currentlyDisplayingDocuments && recordsReturned.size() > docsPerPage) recordsReturned.subList(docsPerPage - 1, recordsReturned.size() - 1).clear();
        return recordsReturned;       
        
    }

    Boolean isCurrentlyDisplayingDocuments(QueryResponse queryResponse){

        if(page != 0) return true;

        String facetField = collectionPrefix + SOLR_FIELDS.get(pathBits.size());

        if(queryResponse.getFacetField(facetField) == null) return true;

        if(queryResponse.getFacetField(facetField).getValues().size() == 1) return true;

        return false;
        
    }

    
    /**
     * Assembles the items returned by the Solr query into an ArrayList of DocumentCollectionRecords.
     * 
     * 
     * @param pathBitsMap Map of levels in collections hierarchy to values for these
     * @param queryResponse The response previously returned by the query to the Solr server
     * @return an ArrayList of DocumentCollectionRecord objects
     */
    
    private ArrayList<Record> buildCollectionsList(QueryResponse queryResponse){

        ArrayList<String> collectionInfo = getCollectionInfo(pathBits);
 
       ArrayList<Record> collectionsList = new ArrayList<Record>();
              
       Iterator<Count> fit = queryResponse.getFacetField(collectionPrefix + SOLR_FIELDS.get(pathBits.size()).name()).getValues().iterator();
       
       while(fit.hasNext()){
           
           Count facetCount = fit.next();
           String facetValueName = facetCount.getName();
           Record coll = collectionInfo.size() > 1 ? new DocumentCollectionRecord(collectionInfo.get(0), collectionInfo.get(1), facetValueName) : new DocumentCollectionRecord(collectionInfo.get(0), facetValueName);
           collectionsList.add(coll);
       }
       Collections.sort(collectionsList);
       
       return collectionsList;
        
    }
    
   /**
     * Assembles the items returned by the Solr query into an ArrayList of DocumentRecords.
     * 
     * 
     * @param pathBitsMap Map of levels in collections hierarchy to values for these
     * @param queryResponse The response previously returned by the query to the Solr server
     * @return an ArrayList of DocumentRecord objects
     */
    
    ArrayList<Record> buildDocumentList(QueryResponse queryResponse){
        
        ArrayList<String> collectionInfo = getCollectionInfo(pathBits);
        DocumentCollectionRecord dcr = collectionInfo.size() > 2 ? new DocumentCollectionRecord(collectionInfo.get(0), collectionInfo.get(1), collectionInfo.get(2)) : new DocumentCollectionRecord(collectionInfo.get(0), collectionInfo.get(1));
        ArrayList<Record> documentList = new ArrayList<Record>();
        for(SolrDocument doc : queryResponse.getResults()){
                   
            try{
                Record record;
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
                   record = new DocumentRecord(dcr, itemIds, itemId, ddbdpDids, hgvDids, apisDids, place, date, language, hasTranslation, hgvId);
                   documentList.add(record);
                   
                }
                 else{
                    
                    record = new DocumentRecord(dcr, itemIds, itemId, ddbdpDids, hgvDids, apisDids, place, date, language, hasTranslation);
                    documentList.add(record);

                 }
                    
                
            
                } catch (NullPointerException npe){
                
                // exception catch needed for docs with missing fields. need a better way to deal with this?
                
                    System.out.println("Missing document " + doc.toString() + ": " + npe.getMessage());
                
                
                }
            
        }
        
        return documentList;      
        
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
    
    String buildHTML(ArrayList<Record> records){
        
        StringBuffer html = new StringBuffer("<h2>" + pathBits.get(SolrField.collection) + "</h2>");
        html = currentlyDisplayingDocuments ?  buildDocumentsHTML(html, records) : buildCollectionsHTML(html, records);
        return html.toString();
        
    }
    
    private StringBuffer buildCollectionsHTML(StringBuffer html, ArrayList<Record> records){
       
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
    
    private StringBuffer buildDocumentsHTML(StringBuffer html, ArrayList<Record> records){
        
        html.append("<table>");
        html.append("<tr class=\"tablehead\"><td>Identifier</td><td>Location</td><td>Date</td><td>All DDbDP IDs</td><td>All HGV IDs</td><td>All APIS IDs</td><td>Languages</td><td>Has translation</td></tr>");
        Iterator<Record> rit = records.iterator();
        
        while(rit.hasNext()){
            
            Record rec = rit.next();
            html.append(rec.getHTML());   
            
        }
        html.append("</table>");
       
        if(totalResultSetSize > docsPerPage){
            
            int numPages = (int) Math.ceil(totalResultSetSize / docsPerPage);

            html.append("<div id=\"pagination\" style=\"width:" + String.valueOf((numPages * 70) + 10) + "px;\">");
                    
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
        currentlyDisplayingDocuments = false;
    }
    
    
   /**
     * <code>Record</code>s store the salient characteristics of  documents retrieved from solr.
     * 
     * Which characteristics are salient will depend upon whether the user is viewing at the document
     * or document-collection level
     * 
     */
    
    private abstract class Record implements Comparable{
        
        /**
         * Returns an html representation of the Record 
         * 
         * @return a string of html
         */
        abstract public String getHTML();
        
        /**
         * Returns the relative path to be associated with the Record
         * 
         * @return the relative path as a String
         */
        abstract public String assembleLink();
        
        /**
         * Returns a tag or tags opening the appropriate html element(s) that wraps the list of <code>Record</code>s 
         * 
         * @return An html opening tag or tags
         */

        
    }
    
    /**
     * DocumentCollectionRecords are records of all information necessary to identify a <i>collection</i>
     * of documents.
     * 
     * Minimally, this will be collections and series. Maximally this will be collection, series, and volume.
     * 
     * 
     */
    
    private class DocumentCollectionRecord extends Record{
        
        private String collection;
        private String series;
        private String volume;
    
        public DocumentCollectionRecord(String collection, String series, String volume){
            
            this.collection = collection;
            this.series = series;
            this.volume = volume;
            
        }
        
        public DocumentCollectionRecord(String collection, String series){
            
            this(collection, series, null);
            
            
        }
        
        
        @Override
        public String getHTML(){
            
            String href = assembleLink();
            String displayString = series + (volume == null ? "" : " " + volume);
            if(displayString.equals("0")) return "";    // TODO: Work out why zero-records result and fix bodge if necessary
            String html = "<li><a href='" + href + "'>" + series + " " + (volume == null ? "" : volume) + "</li>";
            return html;
            
            
        }
        
        @Override
        public String assembleLink(){
            
            String href = BROWSE_SERVLET + "/" + collection;
            
            String seriesIdent = "/" + series;
            String volumeIdent = volume == null ? "" : "/" + volume;
            href += seriesIdent + volumeIdent;
            href = href.replaceAll("\\s", "");
            return href;
            
        }
        
        
        public String getCollection() { return collection; }
        public String getSeries(){ return series; }
        public String getVolume(){ return (volume == null) ? "" : volume; }

        @Override
        public int compareTo(Object o) {
            
            DocumentCollectionRecord comparandum = (DocumentCollectionRecord)o;
            if(this.getVolume().equals("0")) return 0;
            String thisVolume = this.getVolume() != null ? this.getVolume() : "";
            String thatVolume = comparandum.getVolume() != null ? comparandum.getVolume() : "";

            thisVolume = thisVolume.replaceAll("[^\\d]", "");
            thatVolume = thatVolume.replaceAll("[^\\d]", "");

            if(thisVolume.equals("")) thisVolume = "0";
            if(thatVolume.equals("")) thatVolume = "0";

            Integer thisNo = Integer.parseInt(thisVolume);
            Integer thatNo = Integer.parseInt(thatVolume); 


            return thisNo - thatNo;
            
        }
        
    }
    
    private class DocumentRecord extends Record implements Comparable{
        
        private DocumentCollectionRecord documentGroupRecord;
        private ArrayList<String> documentIds;
        private String displayId;
        private String place;
        private String date;
        private String language;        
        private String hasTranslation;
        private String hgv_identifier;
        private String ddbdpIds;
        private String hgvIds;
        private String apisIds;

        public DocumentRecord(DocumentCollectionRecord dgr, ArrayList<String> itemIds, String itemId, String ddb, String hgv0, String apis, String place, String date, String lang, Boolean hasTrans, String hgv){
            
            // this will have to be changed depending on what users want to see in the records
            
            this.documentGroupRecord = dgr;
            this.documentIds = itemIds;
            this.ddbdpIds = ddb;
            this.hgvIds = hgv0;
            this.apisIds = apis;
            this.place = place;
            this.date = date;
            this.language = lang;
            this.hasTranslation = hasTrans ? "Yes" : "No";
            this.hgv_identifier = hgv;
            determineDisplayId(itemId);
            
        }
        
        public DocumentRecord(DocumentCollectionRecord dgr, ArrayList<String> itemIds, String itemId, String ddb, String hgv, String apis, String place, String date, String lang, Boolean hasTrans){
            
            // this will have to be changed depending on what users want to see in the records
            
            this(dgr, itemIds, itemId, ddb, hgv, apis, place, date, lang, hasTrans, "");
            
            
        }
        
        private void determineDisplayId(String itemId){
            
            if(this.documentIds.size() == 1){
                
                this.displayId = documentIds.remove(0);
                return;
                
            }
            Collections.sort(this.documentIds);
            Iterator<String> dit = documentIds.iterator();
            int i;
            for(i = 0; i < documentIds.size(); i++){
                
               if(itemId.matches(".*" + documentIds.get(i) + ".*")){
                    
                    this.displayId = documentIds.get(i);
                    break;
                    
                }
                
                
            }
            
            this.documentIds.remove(i);
            
        }
        
        @Override
        public String getHTML(){
            
            if(this.displayId.equals("0")) return ""; //TODO: Work out why zero-records occur and fix this bodge if necessary
            String displayName = this.documentGroupRecord.getSeries() + " " + this.documentGroupRecord.getVolume() + " " + this.displayId;
            String anchor = "<a href='" + this.assembleLink() + "'>" + displayName + "</a>";
            String html = "<tr class=\"identifier\"><td>" + anchor + "</td>";
            html += "<td class=\"display-place\">" + place + "</td>";
            html += "<td class=\"display-date\">" + date + "</td>";
            html += "<td class=\"ddbdp-ids\">" + ((ddbdpIds.length() > 0) ? ddbdpIds : "None") + "</td>";
            html += "<td class=\"hgv-ids\">" + ((hgvIds.length() > 0) ? hgvIds : "None") + "</td>";
            html += "<td class=\"apis-ids\">" + ((apisIds.length() > 0) ? apisIds : "None") + "</td>";
            html += "<td class=\"language\">" + language + "</td>";
            html += "<td class=\"has-translation\">" + hasTranslation + "</td>";
            html += "</tr>";
            return html;
            
        }
        
        @Override
        public String assembleLink(){
            
           String coll = documentGroupRecord.getCollection();
           String url = "http://localhost/" + coll + "/";
           String item = "";
           
           if("ddbdp".equals(coll)){
               
               item += documentGroupRecord.getSeries() + ";";
               item += ("0".equals(documentGroupRecord.getVolume()) ? "" : documentGroupRecord.getVolume()) + ";";
               item += this.displayId;
               
               
           }
           else if("hgv".equals(coll)){
               
               item += this.hgv_identifier;
               
               
           }
           else{    // if APIS
               
               item += documentGroupRecord.getSeries() + ".";
               item += coll + ".";
               item += this.displayId;
               
               
           }
           url += item;
           url += "/";
           url = url.replaceAll("\\s", "");
           return url;
                      
        }
        
        public String getDisplayId(){ return this.displayId; }

        @Override
        public int compareTo(Object o) {
           
            DocumentRecord comparandum = (DocumentRecord)o;
            String thisId = this.displayId != null ? this.displayId : "";
            String thatId = comparandum.getDisplayId() != null ? comparandum.getDisplayId() : "";

            thisId = this.displayId.replace("[^\\d]", "").replaceFirst("^0+(?!$)", "").replaceAll("[\\s]", "");
            thatId = comparandum.getDisplayId().replace("[^\\d]", "").replaceFirst("^0+(?!$)", "").replaceAll("[\\s]", "");

            if(thisId.isEmpty()) thisId = "0";
            if(thatId.isEmpty()) thatId = "0";

            int thisIdNo = Integer.parseInt(thisId);
            int thatIdNo = Integer.parseInt(thatId);
            
            return thisIdNo - thatIdNo;
            
        }
        
        
    }
    
    
}
