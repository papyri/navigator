package info.papyri.dispatch.atom;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.writer.Writer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * Servlet that accepts date parameters BEFORE, AFTER, and ON, returning an atom feed
 * of all papyri.info documents edited within the span specified.
 * 
 * 
 * @author thill
 */

// TODO: Validate feed output

public class AtomFeedServlet extends HttpServlet{
    
    static String SOLR_URL;
    static String PN_SEARCH;
    static Abdera abdera = null;
    static int entriesPerPage = 50;
    /** URL assigned to rel="self" feed header link, and root URL for all other ids */
    final static String SELF = "http://papyri.info/atom/";
    final static String PAGE = "page";
    /** Identifier assigned to requests that return an error */
    final static String ERROR_ID = SELF + "error";
    /** Identifier assigned for requests that return no results */
    final static String NONEFOUND_ID = SELF + "none";

    /** The possible parameters passed to the servlet */
    private enum Param{
        
        AFTER,
        BEFORE,
        ON 
        
    }
    
    /** Solr fields used in the population of Atom elements */
    private enum SolrField{
    
        first_revised,
        last_revised, 
        title,
        id,
        metadata,
        display_place,
        display_date
    
    
    }
    
    /** Solr fields used in determining the value of the atom:title element
     *  on individual atom:entry elements
     */
    private enum TitleField{
        
        ddbdp_series,
        hgv_series,
        apis_series,
        ddbdp_volume,
        hgv_volume,
        ddbdp_full_identifier,
        hgv_full_identifier,
        apis_full_identifier
       
        
    }
    
    /** Values used for pagination specification in the feed header */
    private enum Pagination{
        
        first,
        next,
        previous,
        last
        
        
        
    }
    
    /**
     * Because of the expense of creating the Abdera instance, it is instantiated as a singleton.
     * 
     * 
     * @see https://cwiki.apache.org/confluence/display/ABDERA/Creating+and+Consuming+Atom+Documents#CreatingandConsumingAtomDocuments-InstantiatingAbdera
     * @return 
     */
    
    public static synchronized Abdera getAbderaInstance() {
        
      if (abdera == null) abdera = new Abdera();
      return abdera;
      
    }
        
    @Override
    public void init(ServletConfig config) throws ServletException{
        
        super.init(config);
        SOLR_URL = config.getInitParameter("solrUrl");
        PN_SEARCH = config.getInitParameter("pnSearchPath");
        abdera = getAbderaInstance();
 
        
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

            request.setCharacterEncoding("UTF-8");        
            response.setContentType("xml");
            ServletOutputStream out = response.getOutputStream();
            int page = pullOutPageNumber(request);
            EnumMap<Param, String> dateParams = pullOutDateParams(request);
            SolrDocumentList results = new SolrDocumentList();
            try{
                
                 SolrQuery sq = buildSolrQuery(dateParams, page);
                 results = queryServer(sq);

            } catch(ParseException pe){
                
                 results = buildErrorDocumentList("Parse error in submitted dates " + request.getQueryString());
                
            }
            if(results.size() == 0) results = buildNoResultsDocumentList(buildErrorMsg(dateParams));  
            Feed emptyFeed = initFeed(results);  
            paginateFeed(emptyFeed, dateParams, page, results);
            addEntries(emptyFeed, results);
            Writer writer = abdera.getWriterFactory().getWriterByMediaType("application/atom+xml");
            emptyFeed.writeTo(writer, out);

    }
    
    /**
     * Parses request for page information, defaulting to first page if none found
     * 
     * @param req
     * @return 
     */

    Integer pullOutPageNumber(HttpServletRequest req){
        
        Map<String, String[]> params = req.getParameterMap();
        
        try{ return Integer.valueOf(params.get(PAGE)[0]); } 
        catch(Exception e){ return 1; }
               
    }
    
    /**
     * Parses request for date parameters.
     * 
     * Any combination of the three possible date parameters will be accepted; all 
     * values after the first  assigned to any one parameter, however, will be 
     * discarded.
     * 
     * @param req
     * @return An <code>EnumMap</code> of parameters mapped to their values
     */
    
    EnumMap<Param, String> pullOutDateParams(HttpServletRequest req){
        
        EnumMap<Param, String> dateParams = new EnumMap<Param, String>(Param.class);
        Map<String, String[]> params = req.getParameterMap();
        
        for(Param value : Param.values()){
            
            if(params.containsKey(value.name())) dateParams.put(value, params.get(value.name())[0]);
                
        }
        
        
        return dateParams;
        
    }

    /** 
     * Assigns the passed parameter values into a SolrQuery object.
     * 
     * Note that if any single passed date cannot be parsed, a parse exception
     * will be thrown and the query abandoned.
     * 
     * @param dateParams
     * @param page
     * @return
     * @throws ParseException 
     */

    SolrQuery buildSolrQuery(EnumMap<Param, String> dateParams, int page) throws ParseException{
        
        SolrQuery sq = new SolrQuery();
        String q = "";
        sq.addSortField(SolrField.last_revised.name(), SolrQuery.ORDER.desc);
        sq.addSortField(SolrField.first_revised.name(), SolrQuery.ORDER.desc);
        sq.setRows(entriesPerPage);
        sq.setStart((page - 1) * entriesPerPage);       
 
        for(Map.Entry<Param, String> entry : dateParams.entrySet()){
            
            String rangeString = generateRangeString(entry.getKey(), entry.getValue());
            if(!"".equals(q)) q += " AND ";
            q += SolrField.last_revised.name() + ":" + rangeString;
            
        }
            
        if(q.equals("")){
            
            q = SolrField.last_revised.name() + ":[* TO *]";
                     
        }       
        sq.setQuery(q);
        return sq;
        
    }
    
    /**
     * Converts the date parameter and value passed to it into a string usable as part
     * of a Solr range query.
     * 
     * @param prm
     * @param dateAsString
     * @return
     * @throws ParseException 
     */
    
    String generateRangeString(Param prm, String dateAsString) throws ParseException{
        
        String rs = "";
        SimpleDateFormat marshalFormat = new SimpleDateFormat("yyy-MM-dd");
        // Note that the full ISO 8601 representation is required for Solr date range queries
        SimpleDateFormat unmarshalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String earliestDate = "*";
        String latestDate = "*";
        Date dateAsDate = marshalFormat.parse(dateAsString);
        String iso8601 = unmarshalFormat.format(dateAsDate);
        
        if(prm == Param.AFTER){
            
            earliestDate = iso8601;
            
        }
        else if(prm == Param.BEFORE){
            
            
            latestDate = iso8601;
            
        }
        else{       // i.e., if Param.ON
            
            earliestDate = iso8601;
            // note that the single passed date value needs to be converted into 
            // a date range with a span of one day
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateAsDate);     
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            latestDate = unmarshalFormat.format(calendar.getTime());
            
        }
        
        rs = "[" + earliestDate + " TO " + latestDate + "]";   
        return rs;
        
    }
    
    /**
     * Uses the passed <code>SolrQuery</code> object to query the server.
     * 
     * @param sq
     * @return 
     */

  
    SolrDocumentList queryServer(SolrQuery sq){
        
        try{
            
            SolrServer solrServer = new CommonsHttpSolrServer(SOLR_URL + PN_SEARCH);
            QueryResponse qr = solrServer.query(sq);
            SolrDocumentList sdl = qr.getResults();
            return sdl;
        }
        catch(MalformedURLException mue){
            
            return buildErrorDocumentList("MalformedURLException: " + mue.getMessage());
                
        }
        catch(SolrServerException sse){
            
            return buildErrorDocumentList("SolrServerException: " + sse.getMessage());
            
        }
          
    }
    
    /**
     * Builds the feed object and its header.
     * 
     * @param results
     * @return 
     */
    
    Feed initFeed(SolrDocumentList results){
                
        Feed feed = abdera.newFeed(); 
        feed.setId("http://papyri.info/atom/");
        feed.setTitle("Papyri.info Data");
        Link selfLink = abdera.getFactory().newLink();
        selfLink.setHref(SELF);
        selfLink.setRel("self");
        selfLink.setMimeType("application/atom+xml");
        feed.addLink(selfLink);
        // TODO: Right now the 'updated' date is equal to the 'updated' field
        // of the most recently updated document in the feed. Is this useful/correct?
        feed.setUpdated((Date)results.get(0).getFieldValue(SolrField.last_revised.name()));
        // TODO: Is this a sensible value for the author field?
        // TODO: Should author fields be attached instead to individual records?
        // TODO: If so, these will need to be indexed.
        // TODO: Should the additional available members be assigned to the author element
        feed.addAuthor("http://papyri.info");
        return feed;
        
    }
    
    /**
     * Deals with feed pagination.
     * 
     * @param feed
     * @param dateParams
     * @param page
     * @param results 
     */

     void paginateFeed(Feed feed, EnumMap<Param, String> dateParams, int page, SolrDocumentList results){
          
         long numResults = results.getNumFound();
         if(numResults <= entriesPerPage) return;
            
         Link startLink = abdera.getFactory().newLink();
         startLink.setRel("first");
         startLink.setHref(SELF);
         feed.addLink(startLink);
         
         String pageQS = SELF + buildDateQueryString(dateParams) + PAGE + "=";
         long lastPage = numResults / entriesPerPage + (numResults % entriesPerPage == 0 ? 0 : 1);
               
         Link lastLink = abdera.getFactory().newLink();
         lastLink.setRel("last");
         lastLink.setHref(pageQS + String.valueOf(lastPage));
         feed.addLink(lastLink);
            
         if(page > 1){
                
            Link prevLink = abdera.getFactory().newLink();
            prevLink.setRel("previous");
            prevLink.setHref(pageQS + String.valueOf(page - 1));
            feed.addLink(prevLink);
                
          }
            
          if(page < lastPage){
                
            Link nextLink = abdera.getFactory().newLink();
            nextLink.setRel("next");
            nextLink.setHref(pageQS + String.valueOf(page + 1));
            feed.addLink(nextLink);            

          }                 
        
    }
     
    /**
      * Parses the passed <code>SolrDocumentList</code> into atom:entry elements.
      * 
      * @param feed
      * @param entries 
      */ 
     
    void addEntries(Feed feed, SolrDocumentList entries){
        
        for(SolrDocument doc : entries){
            
            String id = (String) doc.getFieldValue(SolrField.id.name());
            String title = getTitle(id, doc);
            // TODO: Right now these values are simply pulled from the revisionDesc element
            // TODO: Is this the right approach?
            Date modified = (Date) doc.getFieldValue(SolrField.last_revised.name());
            Date published = (Date) doc.getFieldValue(SolrField.first_revised.name());
            String summary = getSummary(doc);
            Entry newEntry = feed.addEntry();
            Link contentLink = abdera.getFactory().newLink();
            contentLink.setHref(id);
            contentLink.setRel("alternate");
            contentLink.setMimeType("application/xhtml+xml");
            newEntry.addLink(contentLink);
            newEntry.setId(id);
            newEntry.setTitle(title);
            newEntry.setUpdated(modified);
            newEntry.setPublished(published);
            Link rightsLink = abdera.getFactory().newLink();
            rightsLink.setRel("license");
            rightsLink.setHref(id.contains("/apis/") ? "http://creativecommons.org/licenses/by-nc/3.0/" : "http://creativecommons.org/licenses/by/3.0/");
            newEntry.addLink(rightsLink);
            if(summary.length() > 0) newEntry.setSummary(summary);      
            
        }
        
    }

    /**
     * Builds an atom:entry element to display information to the user in the event that
     * an exception is thrown.
     * 
     * @param msg
     * @return 
     */
   
    
    SolrDocumentList buildErrorDocumentList(String msg){
        
        SolrDocumentList sdl = new SolrDocumentList();
        
        SolrDocument doc = new SolrDocument();
        doc.addField(SolrField.id.name(), SELF + "error");
        doc.addField(SolrField.title.name(), "There has been an error in processing your request");
        doc.addField(SolrField.metadata.name(), msg);
        doc.addField(SolrField.last_revised.name(), new Date());       
        sdl.add(doc);
        
        return sdl;
    }
    
    /**
     * Builds an atom:entry element to display information to the user in the event that
     * no results and no error condition are returned from the server.
     * 
     * 
     * @param msg
     * @return 
     */
    
    SolrDocumentList buildNoResultsDocumentList(String msg){
        
        SolrDocumentList sdl = new SolrDocumentList();
        
        SolrDocument doc = new SolrDocument();
        doc.addField(SolrField.id.name(), SELF + "none");
        doc.addField(SolrField.title.name(), "No results returned");
        doc.addField(SolrField.metadata.name(), msg);
        doc.addField(SolrField.last_revised.name(), new Date());       
        sdl.add(doc);
        
        return sdl;
    }
    
    /**
     * Attempts to validate the logic of the passed date parameters, in the event that no
     * results and no error condition are returned from the server, and returns the result
     * of the validation attempt as a string.
     * 
     * 
     * @param dateParams
     * @return 
     * @see info.papyri.dispatch.atom.AtomFeedServlet#buildNoResultsDocumentList(java.lang.String) 
     */
    
    String buildErrorMsg(EnumMap<Param, String> dateParams){
        
        String none_msg = "No results returned for this query.";
        
        if(dateParams.size() < 2) return none_msg;

        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        String msg = "";
        
        Boolean hasAfter = dateParams.containsKey(Param.AFTER);
        Boolean hasBefore = dateParams.containsKey(Param.BEFORE);
        Boolean hasOn = dateParams.containsKey(Param.ON);
        
        Date before = new Date();
        Date after = new Date();
        Date on = new Date();
        
        try{
        
            if(hasBefore) before = simpleFormat.parse(dateParams.get(Param.BEFORE));
            if(hasAfter) after = simpleFormat.parse(dateParams.get(Param.AFTER));
            if(hasOn) on = simpleFormat.parse(dateParams.get(Param.ON));

            if(hasAfter && hasBefore){

                if(before.before(after)) msg += "'BEFORE' and 'AFTER' date settings are incompatible.";

            }
            if(hasAfter && hasOn){

                String spacer = msg.length() == 0 ? "" : " ";

                if(on.before(after)) msg += spacer + "'AFTER' and 'ON' date settings are incompatible.";

            }
            if(hasBefore && hasOn){

                String spacer = msg.length() == 0 ? "" : " ";

                if(on.after(before)) msg += spacer + "'BEFORE' and 'ON' date settings are incompatible.";

            }
        
        }
        catch(ParseException pe){
            
            msg = "Malformed date string error.";
            
        }
        
        if("".equals(msg)) msg = none_msg;
        return msg;
        
    }
    
    /**
     * Converts the parameters used in querying the server into a query string to be used in 
     * building pagination links
     * 
     * 
     * @param dateParams
     * @return 
     * @see info.papyri.dispatch.atom.AtomFeedServlet#paginateFeed(org.apache.abdera.model.Feed, java.util.EnumMap, int, org.apache.solr.common.SolrDocumentList) 
     */

    String buildDateQueryString(EnumMap<Param, String> dateParams){
        
        String qs = "?";
        if(dateParams.size() == 0) return qs;
      
        for(Map.Entry<Param, String> entry : dateParams.entrySet()){
            
            qs += entry.getKey().name() + "=" + entry.getValue();
            qs += "&";
            
        }
        
        return qs;
    }
    
    /**
     * Assembles the relevant <code>SolrField</code> values into a <code>String</code> 
     * to be used as the value of an atom:title element.
     * 
     * If the passed <code>SolrDocument</code> represents an error condition, the title will
     * be 'Error'. If it represents no results having been returned, the title will be 
     * 'No results found'.
     * 
     * @param id
     * @param doc
     * @return 
     */
    
    String getTitle(String id, SolrDocument doc){
        
        String title = "";
        String series = "";
        String volume = "";
        String item = "";
        
        if(id.contains("/ddbdp/")){
            
            series = doc.getFieldValue(TitleField.ddbdp_series.name()) == null ? "" : (String) doc.getFieldValue(TitleField.ddbdp_series.name());
            volume = doc.getFieldValue(TitleField.ddbdp_volume.name()) == null ? "" : (String) doc.getFieldValue(TitleField.ddbdp_volume.name());
            item = doc.getFieldValue(TitleField.ddbdp_full_identifier.name()) == null ? "" : (String) doc.getFieldValue(TitleField.ddbdp_full_identifier.name());
            
        }
        else if(id.contains("/hgv/")){
            
            series = doc.getFieldValue(TitleField.hgv_series.name()) == null ? "" : (String) doc.getFieldValue(TitleField.hgv_series.name());
            volume = doc.getFieldValue(TitleField.hgv_volume.name()) == null ? "" : (String) doc.getFieldValue(TitleField.hgv_volume.name());
            item = doc.getFieldValue(TitleField.hgv_full_identifier.name()) == null ? "" : (String) doc.getFieldValue(TitleField.hgv_full_identifier.name());            
            
        }
        else{
            
            series = doc.getFieldValue(TitleField.apis_series.name()) == null ? "" : (String) doc.getFieldValue(TitleField.hgv_series.name());
            item = doc.getFieldValue(TitleField.apis_full_identifier.name()) == null ? "" : (String) doc.getFieldValue(TitleField.hgv_full_identifier.name());               
            
        }
        
        if(volume.equals("0")) volume = "";
        if(!series.equals("") && (!volume.equals("") || !item.equals(""))) series += " ";
        if(!volume.equals("") && (!item.equals("") || !item.equals(""))) volume += " ";
        title = series + volume + item;
        if(id.equals(ERROR_ID)) title = "Error";
        if(id.equals(NONEFOUND_ID)) title = "No reults found";
        if(title.equals("")) title = id;
        return title;
        
    }
    
    /**
     * Assembles <code>SolrField</code> values to populate an atom:summary element
     * 
     * @param doc
     * @return 
     */
    
    // TODO: Are these actually the values desired?
    
    String getSummary(SolrDocument doc){
        
       if((ERROR_ID).equals(doc.getFieldValue(SolrField.id.name())) || (NONEFOUND_ID).equals(doc.getFieldValue(SolrField.id.name()))){
                
           String msg = doc.getFieldValue(SolrField.metadata.name()) == null ? "Unspecified error" : (String) doc.getFieldValue(SolrField.metadata.name());
           return msg;
                
       }
               
        String summary = "";
        
        if(doc.getFieldValue(SolrField.title.name()) != null){
            
            summary += "Title: " + doc.getFieldValue(SolrField.title.name());
            
        }
        
        if(doc.getFieldValue(SolrField.display_place.name()) != null){
            
            if(summary.length() != 0) summary += ", ";
            summary += "Provenance: " + (String) doc.getFieldValue(SolrField.display_place.name());
            
        }
        if(doc.getFieldValue(SolrField.display_date.name()) != null){
            
            if(summary.length() != 0) summary += ", ";
            summary += "Date: " + (String) doc.getFieldValue(SolrField.display_date.name());
        }
       
        return summary;
        
    }
        
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
        
        return "Returns atom feed for papyri.info records modified before, after, or on a passed date";
        
    }
    
    
}
