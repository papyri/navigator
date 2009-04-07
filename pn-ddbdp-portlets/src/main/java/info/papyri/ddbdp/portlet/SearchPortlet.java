package info.papyri.ddbdp.portlet;

import info.papyri.ddbdp.parser.QueryExecContext;
import info.papyri.ddbdp.parser.QueryResult;
import info.papyri.ddbdp.servlet.*;
import info.papyri.epiduke.lucene.IntQueue;
import info.papyri.epiduke.lucene.analysis.VectorTokenFilter;
import info.papyri.metadata.*;
import info.papyri.util.NumberConverter;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.HighlightUtil;
import org.apache.lucene.search.highlight.LineFragmenter;
import org.apache.lucene.search.highlight.TextFragment;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;


import java.util.regex.*;
public class SearchPortlet extends GenericPortlet implements IndexEventListener {
    public static final String RESULT_ITEM_ATTR = "info.papyri:ddbdp:search:item";
    public static final String DDB_ID_ATTR = "info.papyri:ddbdp:search:ddbdpid";
    public static final String FNAME_ATTR = "info.papyri:ddbdp:search:filename";
    public static final String FRAGMENT_ATTR = "info.papyri:ddbdp:search:fragment";
    public static final String ERROR_ATTR = "info.papyri:ddbdp:search:error";
    private static final char [] BR_TAG = new char[]{'<','b','r','/','>'};
    private static final Pattern DIGITS = Pattern.compile("^\\d+$");
    private static final SortField SF_COLL = new SortField("collection",SortField.STRING);
    private static final SortField SF_VOL = new SortField("volume",SortField.STRING);
    private static final SortField SF_FNAME = new SortField("fileName",SortField.STRING);
    private static final SortField SF_IMG = new SortField(CoreMetadataFields.SORT_HAS_IMG,SortField.STRING);
    private static final SortField SF_TRANS = new SortField(CoreMetadataFields.SORT_HAS_TRANS,SortField.STRING);
    private static final Sort SORT_NAME = new Sort(new SortField[]{SF_COLL,SF_VOL,SF_FNAME});
    private static final Sort SORT_IMG = new Sort(new SortField[]{SF_IMG,SF_COLL,SF_VOL,SF_FNAME});
    private static final Sort SORT_TRANS = new Sort(new SortField[]{SF_TRANS,SF_COLL,SF_VOL,SF_FNAME});
    private static final Sort SORT_ALL = new Sort(new SortField[]{SF_IMG,SF_TRANS,SF_COLL,SF_VOL,SF_FNAME});
    private static final Term DATE1_TEMPLATE = new Term(CoreMetadataFields.DATE1_I,"");
    private static final Term DATE2_TEMPLATE = new Term(CoreMetadataFields.DATE2_I,"");
    private static final Logger LOG = Logger.getLogger(SearchPortlet.class);
    private static final String getSafeDate(int y, int m, int d){
        try{
            return NumberConverter.encodeDate(y,m,d);
        }
        catch (OutOfRangeException e){
            LOG.error(e.toString(),e);
            return "";
        }
    }
    
    private static Query getDateQuery(String onOrBeforeDate, int onOrBeforeDateEra, String onOrAfterDate, int onOrAfterEra) throws OutOfRangeException {
        
        int beforeInt = Integer.MAX_VALUE;
        String beforeQuery = MAXIMUM_DATE;
        int afterInt = Integer.MIN_VALUE;
        String afterQuery = MINIMUM_DATE;
        int state = 0;
        if (onOrBeforeDate != null && (onOrBeforeDate=onOrBeforeDate.trim()).length() > 0){
            try{
                 beforeInt = Integer.parseInt(onOrBeforeDate) * onOrBeforeDateEra;
                 beforeQuery = NumberConverter.encodeDate(beforeInt,12,31);
                 state += 1;
            }
            catch (NumberFormatException nfe){
                throw new OutOfRangeException("Bad year value: " + onOrBeforeDate + " - " + nfe.getMessage());
            }
            }
        
        if (onOrAfterDate != null && (onOrAfterDate=onOrAfterDate.trim()).length() > 0){
            try{
                afterInt = Integer.parseInt(onOrAfterDate) * onOrAfterEra;
                if (
                        (beforeInt < 0 && afterInt > -1) ||
                        (afterInt > beforeInt && beforeInt > -1) ||
                        (beforeInt < afterInt && afterInt < 0 )){
                    throw new OutOfRangeException( "The end date of date range cannot be after the beginning date.");
                }
               afterQuery = NumberConverter.encodeDate(afterInt,1,1);
                state +=2;
            }
            catch (NumberFormatException nfe){
                throw new OutOfRangeException("Bad year value: " + onOrAfterDate + " - " + nfe.getMessage());
            }
        }
        Query before,afterRangeStart,afterRangeEnd;
        BooleanQuery after;
        BooleanQuery dates;
        switch(state){
        case 0: return null; //no date info
        case 1: // before only
          return new ConstantScoreRangeQuery(CoreMetadataFields.DATE1_I,MINIMUM_DATE,beforeQuery,false,true);
        case 2: // after only
          afterRangeStart = 
              new ConstantScoreRangeQuery(CoreMetadataFields.DATE1_I,afterQuery,MAXIMUM_DATE,true,false);
          afterRangeEnd = 
              new ConstantScoreRangeQuery(CoreMetadataFields.DATE2_I,afterQuery, MAXIMUM_DATE,true,false);
          after = new BooleanQuery();
          after.add(afterRangeStart, BooleanClause.Occur.SHOULD);
          after.add(afterRangeEnd, BooleanClause.Occur.SHOULD);
          return after;
        case 3:
            before = new ConstantScoreRangeQuery(CoreMetadataFields.DATE1_I,MINIMUM_DATE,beforeQuery,false,true);
            afterRangeStart = 
                new ConstantScoreRangeQuery(CoreMetadataFields.DATE1_I,afterQuery,MAXIMUM_DATE,true,false);
            afterRangeEnd = 
                new ConstantScoreRangeQuery(CoreMetadataFields.DATE2_I,afterQuery, MAXIMUM_DATE,true,false);
            after = new BooleanQuery();
            after.add(afterRangeStart, BooleanClause.Occur.SHOULD);
            after.add(afterRangeEnd, BooleanClause.Occur.SHOULD);
            dates = new BooleanQuery();
            dates.add(before,BooleanClause.Occur.MUST);
            dates.add(after,BooleanClause.Occur.MUST);
            return dates;
        default:
            return null;
        }
    }
    private static final String MINIMUM_DATE = getSafeDate(NumberConverter.MIN_INT,1,1);
    private static final String MAXIMUM_DATE = getSafeDate(NumberConverter.MAX_INT,12,31);
    private static final char[] sep = new char[]{'.','.','.'};
    private static final int sepLen =sep.length;
    public static final int PAGE_SIZE = 50;
    private static final Term APIS_TEMPLATE = new Term(CoreMetadataFields.APIS_COLLECTION,"");
    private IndexSearcher SEARCHER;
    private IndexReader DOC_READER;
    private static final ContextFactory CONTEXT = new ContextFactory();
    private final static Pattern LINE = Pattern.compile("(\\&LINE\\-)([A-Za-z0-9\\/]+)(;)");
    private QueryExecContext jsExec = new QueryExecContext();
    private Connection db;

    @Override
    public void init() throws PortletException {
        super.init();
        BooleanQuery.setMaxClauseCount(32*1024);
        IndexEventPropagator events = (IndexEventPropagator)this.getPortletContext().getAttribute("EVENTS");
        if(events == null) throw new PortletException("No IndexEventPropagator found");
        events.addListener(this);
        db = events.connection();
        jsExec.setDb(db);
    }

    @Override
    public void destroy()  {
        super.destroy();
        IndexEventPropagator events = (IndexEventPropagator)this.getPortletContext().getAttribute("EVENTS");
        events.removeListener(this);
    }


    public void replaceDocReader(IndexEvent event) {
        DOC_READER = (IndexReader)event.getSource();
    }

    public void replaceSearchers(SearcherEvent event) {
        SEARCHER = event.getMultiSearcher();
        //setSearchers(IndexSearcher searcher, IndexSearcher bigrams, IndexSearcher bigramsDF, IndexSearcher bigramsFL, IndexSearcher bigramsLC)
        jsExec.setSearchers(SEARCHER,event.getPlainBigrams(),event.getDFBigrams(),event.getFLBigrams(),event.getLCBigrams());
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response)
    throws PortletException, IOException  {
        if (WindowState.MINIMIZED.equals(request.getWindowState())){
            return;    
        }

        Long startTime = System.currentTimeMillis();
        request.setAttribute(DDBDPServlet.QUERY_TIMER_ATTR, startTime);
        String [] fnames = new String[0];
        String [] fragments = new String[0];
        String [] ids = new String[0];
        String offsetParm = request.getParameter("offset");
        if(offsetParm == null || offsetParm.matches("^\\d*[^\\d]\\d*$")) offsetParm = "0";
        int offset = Integer.parseInt(offsetParm);
        boolean noFrags = "on".equals(request.getParameter("nofrags"));
        boolean beta = "on".equals(request.getParameter("beta"));
        String qParm = null;
        qParm = request.getParameter("query");
        if (qParm == null || "".equals(qParm.trim())){
            request.setAttribute(DDBDPServlet.DDB_ID_ARRAY_ATTR, ids);
            request.setAttribute(DDBDPServlet.FILENAMES_ARRAY_ATTR, fnames);
            request.setAttribute(DDBDPServlet.FRAGMENTS_ARRAY_ATTR, fragments);
            PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/portlet-ui.jsp");
            rd.include(request,response);
            rd = getPortletContext().getRequestDispatcher("/WEB-INF/portlet-foot.jsp"); 
            rd.include(request,response);
            return;
        }
//        else{
//            qParm = SearchPortlet.getSafeUTF8(qParm);
//        }
        Query query;

        org.mozilla.javascript.Context jsContext = CONTEXT.enterContext();
        QueryResult tuple = (QueryResult)jsContext.evaluateString(jsExec, qParm, "docQuery", 1, null);
        Integer mode = (Integer)jsContext.getThreadLocal(QueryExecContext.MODE_KEY);
        Context.exit();
        int resultMode = (mode == null)?(DDBDPServlet.MODE_NONE):mode.intValue(); // result text will not be in Betacode
        query = tuple.getQuery();
        //scorer = tuple.getScorer(SEARCHER.getIndexReader());
        String apis = request.getParameter("apis");
        String pub = request.getParameter("pubSeries");
        String vol = request.getParameter("pubVol");
        String place = request.getParameter("place");
        ArrayList<Query> queries = new ArrayList<Query>();
        queries.add(query);
        if(apis != null && !(apis=apis.trim()).equals("")){
            Term apisTerm = APIS_TEMPLATE.createTerm(apis);
            queries.add(new TermQuery(apisTerm));
        }
        if(pub != null && !(pub=pub.trim()).equals("")){
            if(vol != null && !(vol=vol.trim()).equals("")){
                if(DIGITS.matcher(vol).matches()){
                    vol = NumberConverter.getRoman(vol);
                }
                Term pubTerm = new Term(CoreMetadataFields.BIBL_PUB,pub + " " + vol);
                queries.add(new PrefixQuery(pubTerm));
            }
            else{
                Term pubTerm = new Term(CoreMetadataFields.INDEXED_SERIES,pub);
                queries.add(new TermQuery(pubTerm));
            }
        }
        if(place != null && !(place=place.trim()).equals("")){
            Term placeTerm = new Term(CoreMetadataFields.PROVENANCE_NOTE,place.toLowerCase());
            queries.add(new FuzzyQuery(placeTerm,0.75f,3));
        }
        String onOrAfterDate = request.getParameter("after");
        if (onOrAfterDate != null) onOrAfterDate = onOrAfterDate.trim();
        else onOrAfterDate = "";
        int onOrAfterEra = ("CE".equals(request.getParameter("afterEra"))?1:-1);
        String onOrBeforeDate = request.getParameter("before");
        if (onOrBeforeDate != null) onOrBeforeDate = onOrBeforeDate.trim();
        else onOrBeforeDate = "";
        int onOrBeforeEra = ("CE".equals(request.getParameter("beforeEra"))?1:-1);
        try{
            Query dateQuery = getDateQuery(onOrBeforeDate, onOrBeforeEra, onOrAfterDate, onOrAfterEra); 
            if(dateQuery != null){
                queries.add(dateQuery);
            }
        }
        catch(OutOfRangeException e){
            request.setAttribute(ERROR_ATTR, e);
        }
        if(queries.size()>1){
            BooleanQuery allQueries = new BooleanQuery();
            for(Query q:queries){
                allQueries.add(q, BooleanClause.Occur.MUST);
            }
            query = allQueries;
        }
        int sortSwitch = 0;
        Sort sort;
        if("on".equals(request.getParameter("imgFirst"))) sortSwitch += 1;
        if("on".equals(request.getParameter("transFirst"))) sortSwitch += 2;
        switch(sortSwitch){
        case 1:
            sort = SORT_IMG;
            break;
        case 2:
            sort = SORT_TRANS;
            break;
        case 3:
            sort = SORT_ALL;
            break;
        default:
            sort = SORT_NAME;
        }
        LOG.debug("Query: " + query.toString());
        Hits  hits = SEARCHER.search(query ,sort);
        LOG.debug("Hit count: " + hits.length());
        int hitCount = hits.length();
        int hitSize =  hitCount - offset;
        String field = tuple.getField();

        request.setAttribute(DDBDPServlet.NUM_RECS_ATTR, Integer.toString(hitCount));

        if (hitSize > 0){
            PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/portlet-head.jsp");
            rd.include(request,response);

            int index = -1;

            FieldSelector selector = (noFrags)?DDBDPServlet.NAME_ONLY:DDBDPServlet.NAME_AND_TEXT;

            IndexReader reader =  DOC_READER;
            java.util.Iterator<Hit> iter = hits.iterator();
            for(int i=0;i<offset&&iter.hasNext();i++)iter.next();
            while( ++index < ScriptSearch.PAGE_SIZE && iter.hasNext()){
                int id = iter.next().getId();
                Document doc = DOC_READER.document(id,selector);
                request.setAttribute(RESULT_ITEM_ATTR, SEARCHER.doc(id));
                String fname = doc.get(DDBDPServlet.FNAME_FIELD);
                if(fname==null)fname="";
                request.setAttribute(FNAME_ATTR, fname);
                String ddbdpid = doc.get(DDBDPServlet.DDB_ID_FIELD);
                if(ddbdpid==null) ddbdpid="";
                request.setAttribute(DDB_ID_ATTR, ddbdpid);
                String frag = "";
                if(!noFrags){
                    String text = doc.getField(DDBDPServlet.TEXT_FIELD).stringValue();

                    TokenStream textTokens = DDBDPServlet.getTokenFilter(resultMode, text);
                    if (textTokens != null){
                        CachingTokenFilter cache = new CachingTokenFilter(textTokens);
                        cache.next(); // fill cache
                        cache.reset();
                        TextFragment [] frags = HighlightUtil.getBestTextFragmentsNoGroup(cache,text, new LineFragmenter(),query,field, true, 5);

                        StringBuffer charBuff = new StringBuffer();

                        for(TextFragment t:frags){
                            if(t != null && t.getScore()>0){
                                if(charBuff.length() > 0) {
                                    charBuff.append(BR_TAG);
                                }
                                String tString = t.toString();
                                Matcher m = LINE.matcher(tString);
                                if(m.find()){
                                    tString = m.replaceAll("<b>(line $2)</b>");
                                }
                                charBuff.append(tString);
                                charBuff.append(sep);

                                charBuff.append(" ( ");
                                charBuff.append(Float.toString(t.getScore()));
                                charBuff.append(" ) ");
                            }
                        }

                        frag = charBuff.toString();
                    }
                }
                request.setAttribute(FRAGMENT_ATTR, frag);
                rd = getPortletContext().getRequestDispatcher("/WEB-INF/portlet-item.jsp"); 
                rd.include(request,response);
            }
            rd = getPortletContext().getRequestDispatcher("/WEB-INF/portlet-foot.jsp"); 
            rd.include(request,response);
        }
        else{
            PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/portlet-ui.jsp");
            rd.include(request,response);
        }

        response.flushBuffer();
        return;
    }

    public static String getSafeUTF8(String iso){
        try{
            byte [] bytes = iso.getBytes("ISO-8859-1");
            String result =  new String(bytes,"UTF-8");
            return result.trim();
        }
        catch (Throwable t){
            return "";
        }
    }    
    public static String getSafeUTF16(String iso){
        try{
            byte [] bytes = iso.getBytes("ISO-8859-1");
            String result =  new String(bytes,"UTF-16");
            return result.trim();
        }
        catch (Throwable t){
            return "";
        }
    }    
    }
