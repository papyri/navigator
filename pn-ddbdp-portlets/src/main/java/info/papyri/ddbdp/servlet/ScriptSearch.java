package info.papyri.ddbdp.servlet;

import info.papyri.ddbdp.parser.QueryExecContext;
import info.papyri.ddbdp.parser.SearchResult;
import info.papyri.epiduke.lucene.IntQueue;
import info.papyri.epiduke.lucene.analysis.VectorTokenFilter;

import java.io.IOException;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.HighlightUtil;
import org.apache.lucene.search.highlight.LineFragmenter;
import org.apache.lucene.search.highlight.PNSpanScorer;
import org.apache.lucene.search.highlight.TextFragment;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

public class ScriptSearch extends DDBDPServlet implements IndexEventListener {
    private static final Logger LOG = Logger.getLogger(ScriptSearch.class);
    private static final char [] BR_TAG = new char[]{'<','b','r','/','>'};
    private static final char[] sep = new char[]{'.','.','.'};
    private static final int sepLen =sep.length;
    public static final int PAGE_SIZE = 50;
    private IndexSearcher SEARCHER;
    private IndexReader DOC_READER;
    private static final ContextFactory CONTEXT = new ContextFactory();
    private final static Pattern LINE = Pattern.compile("(\\&LINE\\-)([A-Za-z0-9\\/]+)(;)");
    private QueryExecContext jsExec;
    private Connection db;

    @Override
    public void init() throws ServletException {
        super.init();
        IndexEventPropagator events = (IndexEventPropagator)this.getServletContext().getAttribute("EVENTS");
        events.addListener(this);
        db = events.connection();
        jsExec = new QueryExecContext();
        jsExec.setDb(db);
    }
    
    @Override
    public void destroy()  {
        super.destroy();
        IndexEventPropagator events = (IndexEventPropagator)this.getServletContext().getAttribute("EVENTS");
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
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req,res);
        return;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        Long startTime = System.currentTimeMillis();
        req.setAttribute(DDBDPServlet.QUERY_TIMER_ATTR, startTime);
        String [] fnames = new String[0];
        String [] fragments = new String[0];
        boolean doFrags = "on".equals(req.getParameter("frags"));
        String qParm = null;
        String startRecS = req.getParameter("startRecord");
        int startRec;
        if(startRecS == null || "".equals(startRecS)){
            startRec = 1;
        }
        else {
            startRec = Math.max(1,Integer.parseInt(startRecS));
        }
        qParm = req.getParameter("query");
            if (qParm == null || "".equals(qParm.trim())){
                req.setAttribute(DDBDPServlet.FILENAMES_ARRAY_ATTR, fnames);
                req.setAttribute(DDBDPServlet.FRAGMENTS_ARRAY_ATTR, fragments);
                RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/script-search.jsp");
                rd.forward(req,res);
                return;
            }
            else{
                qParm = ScriptSearch.getSafeUTF8(qParm);
            }
            Query query;

            IntQueue hits = null;
            org.mozilla.javascript.Context jsContext = CONTEXT.enterContext();
            SearchResult tuple = (SearchResult)jsContext.evaluateString(jsExec, qParm, "docQuery", 1, null);
            Integer mode = (Integer)jsContext.getThreadLocal(QueryExecContext.MODE_KEY);
            Context.exit();
            int resultMode = (mode == null)?(DDBDPServlet.MODE_NONE):mode.intValue(); // result text will not be in Betacode
            query = tuple.getQuery();
            //scorer = tuple.getScorer(SEARCHER.getIndexReader());
            hits = tuple.getHits();
            int hitCount = tuple.totalMatched();
            int hitSize =  hits.size();
            String field = tuple.getField();

            req.setAttribute(DDBDPServlet.NUM_RECS_ATTR, Integer.toString(hitCount));
            if (hitSize > 0){

                fnames = new String[Math.min(hitSize,PAGE_SIZE)];
                if(doFrags) fragments = new String[fnames.length];
                int index = -1;

                FieldSelector selector = (doFrags)?DDBDPServlet.NAME_AND_TEXT:DDBDPServlet.NAME_ONLY;

                IndexReader reader =  DOC_READER;
                //long totalTokenizing = 0;
                //long totalHighlighting = 0;
                while( hits.size() > 0){
                    int pos = hits.next();
                    index++;

                    Document doc = reader.document(pos,selector);
                    fnames[index] = doc.get(DDBDPServlet.FNAME_FIELD);
                    if(!doFrags) continue;
                    String text = doc.getField(DDBDPServlet.TEXT_FIELD).stringValue();
                    //long start = System.currentTimeMillis();
                    TokenStream textTokens = DDBDPServlet.getTokenFilter(resultMode, text);
                    
                    //long tokened = System.currentTimeMillis();

                    if (textTokens != null){
                        CachingTokenFilter cache = new CachingTokenFilter(textTokens);
                        cache.next(); // fill cache
                        cache.reset();
                        //CachingTokenFilter scoreCache = new CachingTokenFilter(cache);
                        //scoreCache.next(); // fill cache
                        //scoreCache.reset();
                        //PNSpanScorer scorer = new PNSpanScorer(query,field,scoreCache);
                        cache.reset();
                        TextFragment [] frags = HighlightUtil.getBestTextFragmentsNoGroup(cache,text, new LineFragmenter(),query,field, true, 5);

                        //totalHighlighting += (System.currentTimeMillis() - tokened);
                        //totalTokenizing += (tokened - start);
                        StringBuffer charBuff = new StringBuffer();

                        for(TextFragment t:frags){
                            if(t != null && t.getScore()>0){
                                if(charBuff.length() > 0) {
                                    charBuff.append(BR_TAG);
                                }
                                String tString = t.toString();
                                Matcher m = LINE.matcher(tString);
                                if(m.find()){
                                    tString = m.replaceAll("<b class=\"lineNumber\">(line $2)</b>");
                                }
                               charBuff.append(tString);
                               charBuff.append(sep);

                               charBuff.append(" ( ");
                               charBuff.append(Float.toString(t.getScore()));
                               charBuff.append(" ) ");
                               //System.arraycopy(score,0,chars,fPos,score.length);
                            }
                        }
                        
                       String frag = charBuff.toString();
                       if(frag != null){
                           fragments[index] = frag ;
                       }
                       else fragments[index] = "";
                    }
                    else {
                        LOG.error("tokens was null!");
                        fragments[index] = "";
                    }
                }
            }

            req.setAttribute(DDBDPServlet.FILENAMES_ARRAY_ATTR, fnames);
            req.setAttribute(DDBDPServlet.FRAGMENTS_ARRAY_ATTR, fragments);
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/script-search.jsp");
            rd.forward(req,res);
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
}
