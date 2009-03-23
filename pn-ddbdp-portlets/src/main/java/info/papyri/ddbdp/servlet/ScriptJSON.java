package info.papyri.ddbdp.servlet;

import info.papyri.ddbdp.parser.QueryExecContext;
import info.papyri.ddbdp.parser.SearchResult;
import info.papyri.epiduke.lucene.IntQueue;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.HighlightUtil;
import org.apache.lucene.search.highlight.LineFragmenter;
import org.apache.lucene.search.highlight.SpanScorer;
import org.apache.lucene.search.highlight.TextFragment;
import info.papyri.epiduke.lucene.analysis.CopyingTokenFilter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ErrorReporter;

public class ScriptJSON extends DDBDPServlet implements IndexEventListener {
    private static final char [] BR_TAG = new char[]{'<','b','r','/','>'};
    private static final char[] sep = new char[]{'.','.','.'};
    private static final int sepLen =sep.length;
    public static final int PAGE_SIZE = 50;
    private IndexSearcher SEARCHER;
    private static final ContextFactory CONTEXT = new ContextFactory();
    private final static Pattern LINE = Pattern.compile("(\\&LINE\\-)([A-Za-z0-9\\/]+)(;)");
    private IndexReader DOC_READER;
    private QueryExecContext jsExec;

    @Override
    public void init() throws ServletException {
        super.init();
        jsExec = new QueryExecContext();
        IndexEventPropagator events = (IndexEventPropagator)this.getServletContext().getAttribute("EVENTS");
        events.addListener(this);
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
        boolean doFrags = "on".equals(req.getParameter("frags"));
        String qParm = null;
        String startRecS = req.getParameter("startRecord");

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        qParm = req.getParameter("query");
        qParm = (qParm!=null)?ScriptSearch.getSafeUTF8(qParm):"";
            if ("".equals(qParm.trim())){
                PrintWriter pw = res.getWriter();
                pw.print("{error:'No query parameter',hits:0,timeMs:");
                pw.print(System.currentTimeMillis() - startTime);
                pw.print("}");
                pw.flush();
                return;
            }
            
            Query query;

            IntQueue hits = null;
            org.mozilla.javascript.Context jsContext = CONTEXT.enterContext();
            SearchResult tuple = null;
            try{
                tuple = (SearchResult)jsContext.evaluateString(jsExec, qParm, "docQuery", 1, null);
            }
            catch (IllegalArgumentException e){
                PrintWriter pw = res.getWriter();
                pw.print("{\"error\":'" +e.getMessage() + "',\"hits\":0,\"timeMs\":");
                pw.print(System.currentTimeMillis() - startTime);
                pw.print("}");
                pw.flush();
                return;
            }
            Integer mode = (Integer)jsContext.getThreadLocal(QueryExecContext.MODE_KEY);
            Context.exit();
            int resultMode = (mode == null)?(DDBDPServlet.MODE_NONE):mode.intValue(); // result text will not be in Betacode
            query = tuple.getQuery();
            hits = tuple.getHits();
            int hitCount = tuple.totalMatched();
            int hitSize =  hits.size();
            String field = tuple.getField();
            PrintWriter pw = res.getWriter();
            pw.println("{\"hits\":" + hitCount + ",\"offset\":" + tuple.offset() + ",\"returned\":" + hitSize);
            req.setAttribute(DDBDPServlet.NUM_RECS_ATTR, Integer.toString(hitCount));
            if (hitSize > 0){
                pw.println(",\"docs\":[");

                 int index = -1;

                FieldSelector selector = (doFrags)?DDBDPServlet.NAME_AND_TEXT:DDBDPServlet.NAME_ONLY;

                IndexReader reader =  DOC_READER;

                while( hits.size() > 0){
                    int pos = hits.next();
                    index++;
                    pw.print("{");
                    Document doc = reader.document(pos,selector);
                    String name= doc.get(DDBDPServlet.FNAME_FIELD);
                    String uri = req.getRequestURI();
                    uri = uri.substring(0,uri.indexOf(req.getContextPath())) +req.getContextPath()+  "/doc?name=" + name;
                    pw.print("\"name\":\"" + name + "\",\"uri\":\"" + uri + "\"");
                    if(!doFrags){
                        pw.print("}");
                        if(hits.size()>0) pw.print(",");
                        continue;
                    }

                    LineFragmenter fragmenter = new LineFragmenter();
                    String text = doc.getField(DDBDPServlet.TEXT_FIELD).stringValue();

                    TokenStream textTokens = DDBDPServlet.getTokenFilter(resultMode, text);

                    if (textTokens != null){
                        pw.println(",\"fragments\":[");
                        CachingTokenFilter cache = new CachingTokenFilter(textTokens);
                        SpanScorer scorer = new SpanScorer(query,field,cache);
                        cache.reset();
                        TextFragment [] frags = HighlightUtil.getBestTextFragmentsNoGroup(cache,text,fragmenter,scorer, true, 5);

                        boolean first = true;
                        for(TextFragment t:frags){
                            if(t != null && t.getScore()>0){
                                if(!first)pw.print(",");
                                else first = false;
                                pw.print("\"");
                                String tString = t.toString();
                                Matcher m = LINE.matcher(tString);
                                if(m.find()){
                                    tString = m.replaceAll("<b>(line $2)</b>").replaceAll("\\s+", " ");
                                }
                               pw.print(tString);
                               pw.print(sep);
                               pw.print(" ( ");
                               pw.print(Float.toString(t.getScore()));
                               pw.print(" )\"");
                               
                            }
                        }
                        pw.println("]"); // end frags
                        
                    }
                    pw.print("}"); // end a doc
                    if(hits.size()>0)pw.println(",");
                }
                pw.println("]"); // end docs
            }
            pw.print(",\"timeMs\":" + (System.currentTimeMillis() - startTime));
            pw.print("}");
            pw.flush();
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
}
