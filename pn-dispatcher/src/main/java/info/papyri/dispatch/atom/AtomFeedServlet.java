package info.papyri.dispatch.atom;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.apache.abdera.model.Person;
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

    /** The possible temporal parameters passed to the servlet */
    private enum TimeParam{
        
        AFTER,
        BEFORE,
        ON 
        
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
            EnumMap<TimeParam, String> dateParams = pullOutDateParams(request);
            SearchType typeFlag = pullOutTypeFlag(request);
            SolrDocumentList results = new SolrDocumentList();
            try{
                
                 SolrQuery sq = buildSolrQuery(dateParams, page, typeFlag);
                 results = queryServer(sq);

            } catch(ParseException pe){
                
                 results = buildErrorDocumentList("Parse error in submitted dates " + request.getQueryString());
                
            }
            if(results.size() == 0) results = buildNoResultsDocumentList(buildErrorMsg(dateParams));  
            Feed emptyFeed = initFeed(results);  
            paginateFeed(emptyFeed, dateParams, page, results);
            ArrayList<EmendationRecord> emendationRecords = buildEmendationRecords(results, typeFlag);
            addEntries(emptyFeed, emendationRecords);
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
    
    EnumMap<TimeParam, String> pullOutDateParams(HttpServletRequest req){
        
        EnumMap<TimeParam, String> dateParams = new EnumMap<TimeParam, String>(TimeParam.class);
        Map<String, String[]> params = req.getParameterMap();
        
        for(TimeParam value : TimeParam.values()){
            
            if(params.containsKey(value.name())) dateParams.put(value, params.get(value.name())[0]);
                
        }
        
        
        return dateParams;
        
    }
    
    SearchType pullOutTypeFlag(HttpServletRequest req){
        
        try{
            
            return SearchType.valueOf(req.getPathInfo().substring(1).toLowerCase());
            
            
        } catch(IllegalArgumentException iae){
            
            return SearchType.all;
            
        } catch(NullPointerException npe){
            
            return SearchType.all;
            
        }
         
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

    SolrQuery buildSolrQuery(EnumMap<TimeParam, String> dateParams, int page, SearchType searchType) throws ParseException{
        
        SolrQuery sq = new SolrQuery();
        String q = "";
        sq.addSortField(SolrField.edit_date.name(), SolrQuery.ORDER.desc);
        sq.setRows(entriesPerPage);
        sq.setStart((page - 1) * entriesPerPage);       
 
        for(Map.Entry<TimeParam, String> entry : dateParams.entrySet()){
            
            String rangeString = generateRangeString(entry.getKey(), entry.getValue());
            if(!"".equals(q)) q += " AND ";
            q += SolrField.edit_date.name() + ":" + rangeString;
            
        }
            
        if(q.equals(""))q = SolrField.edit_date.name() + ":[* TO *]";
        if(searchType != SearchType.all) sq.addFilterQuery(SolrField.edit_type.name() + ":" + searchType.name());
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
    
    String generateRangeString(TimeParam prm, String dateAsString) throws ParseException{
        
        String rs = "";
        SimpleDateFormat marshalFormat = new SimpleDateFormat("yyy-MM-dd");
        // Note that the full ISO 8601 representation is required for Solr date range queries
        SimpleDateFormat unmarshalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String earliestDate = "*";
        String latestDate = "*";
        Date dateAsDate = marshalFormat.parse(dateAsString);
        String iso8601 = unmarshalFormat.format(dateAsDate);
        
        if(prm == TimeParam.AFTER){
            
            earliestDate = iso8601;
            
        }
        else if(prm == TimeParam.BEFORE){
            
            
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
        feed.setUpdated((Date)results.get(0).getFieldValue(SolrField.edit_date.name()));
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

     void paginateFeed(Feed feed, EnumMap<TimeParam, String> dateParams, int page, SolrDocumentList results){
          
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
     
     ArrayList<EmendationRecord> buildEmendationRecords(SolrDocumentList ers, SearchType filterType){
         
         ArrayList<EmendationRecord> emendationRecords = new ArrayList<EmendationRecord>();
         for(SolrDocument doc : ers){
             
             EmendationRecord emendationRecord = new EmendationRecord(doc, filterType);
             emendationRecords.add(emendationRecord);
             
         }
         
         return emendationRecords;
            
     }
     
     
    /**
      * Parses the passed <code>SolrDocumentList</code> into atom:entry elements.
      * 
      * @param feed
      * @param entries 
      */ 
     
    void addEntries(Feed feed, ArrayList<EmendationRecord> emendationRecords){
        
        for(EmendationRecord rec : emendationRecords){
            
            Entry feedEntry = feed.addEntry();
            
            feedEntry.setId(rec.getID());
            feedEntry.setTitle(rec.getTitle());
            feedEntry.setUpdated(rec.getLastEmendationDate());
            feedEntry.setPublished(rec.getPublicationDate());
            
            Link contentLink = abdera.getFactory().newLink();
            contentLink.setHref(rec.getID());
            contentLink.setRel("alternate");
            contentLink.setMimeType("application/xhtml+xml");
            feedEntry.addLink(contentLink);
            
            Link rightsLink = abdera.getFactory().newLink();
            rightsLink.setRel("license");
            rightsLink.setHref(rec.getID().contains("/apis/") ? "http://creativecommons.org/licenses/by-nc/3.0/" : "http://creativecommons.org/licenses/by/3.0/");
            feedEntry.addLink(rightsLink);  
            
            Person contributor  = abdera.getFactory().newContributor();
            contributor.setUri(rec.getContributorURI());
            contributor.setName(rec.getContributorName());
            feedEntry.addContributor(contributor);
            
            if(rec.getSummary().length() > 0) feedEntry.setSummary(rec.getSummary());      

            
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
        doc.addField(SolrField.edit_date.name(), new Date());       
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
        doc.addField(SolrField.edit_date.name(), new Date());       
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
    
    String buildErrorMsg(EnumMap<TimeParam, String> dateParams){
        
        String none_msg = "No results returned for this query.";
        
        if(dateParams.size() < 2) return none_msg;

        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        String msg = "";
        
        Boolean hasAfter = dateParams.containsKey(TimeParam.AFTER);
        Boolean hasBefore = dateParams.containsKey(TimeParam.BEFORE);
        Boolean hasOn = dateParams.containsKey(TimeParam.ON);
        
        Date before = new Date();
        Date after = new Date();
        Date on = new Date();
        
        try{
        
            if(hasBefore) before = simpleFormat.parse(dateParams.get(TimeParam.BEFORE));
            if(hasAfter) after = simpleFormat.parse(dateParams.get(TimeParam.AFTER));
            if(hasOn) on = simpleFormat.parse(dateParams.get(TimeParam.ON));

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

    String buildDateQueryString(EnumMap<TimeParam, String> dateParams){
        
        String qs = "?";
        if(dateParams.size() == 0) return qs;
      
        for(Map.Entry<TimeParam, String> entry : dateParams.entrySet()){
            
            qs += entry.getKey().name() + "=" + entry.getValue();
            qs += "&";
            
        }
        
        return qs;
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
