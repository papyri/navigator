package info.papyri.ddbdp.servlet;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Query;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;

import org.z3950.zing.cql.*;

import info.papyri.epiduke.lucene.*;
import info.papyri.epiduke.lucene.analysis.*;
import info.papyri.epiduke.lucene.substring.*;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.SubstringPhraseQuery;
import org.apache.lucene.search.highlight.BracketEncoder;
import org.apache.lucene.search.highlight.FastHTMLFormatter;
import org.apache.lucene.search.highlight.HighlightUtil;
import org.apache.lucene.search.highlight.LineFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.QueryTermExtractor;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.PNSpanScorer;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.PNWeightedSpanTermExtractor;

public class Search extends DDBDPServlet implements IndexEventListener {
    private static final Logger LOG = Logger.getLogger(Search.class);
    private static final char[] sep = new char[]{'.','.','.'};
    private static final int sepLen =sep.length;
    public static final int PAGE_SIZE = 50;
    private IndexSearcher SEARCHER;
    private IndexSearcher [] bigrams = new IndexSearcher[4];
    private final CQLParser parser = new CQLParser();
    private final static Pattern LINE = Pattern.compile("(\\&LINE\\-)([A-Za-z0-9\\/]+)(;)");
    private IndexReader DOC_READER;

    @Override
    public void init() throws ServletException {
        super.init();
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
        SEARCHER = (IndexSearcher)event.getSource();
        this.bigrams[MODE_FILTER_DIACRITIC] = event.getDFBigrams();
        this.bigrams[MODE_FILTER_CAPITALS_AND_DIACRITICS] = event.getFLBigrams();
        this.bigrams[MODE_FILTER_CAPITALS] = event.getLCBigrams();
        this.bigrams[MODE_NONE] = event.getPlainBigrams();
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
        int mode = -1;
        qParm = req.getParameter("query");
            if (qParm == null || "".equals(qParm.trim())){
                req.setAttribute(DDBDPServlet.FILENAMES_ARRAY_ATTR, fnames);
                req.setAttribute(DDBDPServlet.FRAGMENTS_ARRAY_ATTR, fragments);
                RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/search.jsp");
                rd.forward(req,res);
                return;
            }
            else{
                qParm = ScriptSearch.getSafeUTF8(qParm);
            }
            Query query;
            PNSpanScorer scorer;
            CQLNode qNode = null;

            try{
                qNode = parser.parse(qParm);

                req.setAttribute(DDBDPServlet.XQL_ATTR, "<sru:xQuery>" + qNode.toXCQL(0).replace("<searchClause>", "<searchClause xmlns=\"http://www.loc.gov/zing/cql/xcql/\">") + "</sru:xQuery>");
                req.setAttribute(DDBDPServlet.CQL_ATTR,qNode.toCQL().replaceAll("<","&lt;").replaceAll(">", "&gt;"));
            }
            catch (CQLParseException cqlex){
                throw new ServletException(cqlex);
            }
            BitSet hits = null;

            if (qNode instanceof CQLTermNode){
                mode = Sru.getMode(((CQLTermNode)qNode).getRelation());
            }
            else{
                CQLBooleanNode bNode = (CQLBooleanNode)qNode;
                mode = Sru.getMode(Sru.getLeftmostRelation(bNode));
            }

            int resultMode = (mode >= Sru.MODE_BETA)?(mode -Sru.MODE_BETA):mode; // result text will not be in Betacode

            Object [] tuple = Sru.processNode(qNode,SEARCHER,bigrams);
            query = (Query)tuple[0];
            scorer = (PNSpanScorer)tuple[1];
            hits = Sru.getHits(query,SEARCHER);

            req.setAttribute(DDBDPServlet.NUM_RECS_ATTR, Integer.toString(hits.cardinality()));
            if (hits.cardinality() > 0){
                LineFragmenter fragmenter = new LineFragmenter();

               int startOffset = startRec - 1;
                fnames = new String[Math.min(hits.cardinality() - (startOffset),PAGE_SIZE)];
                if(doFrags) fragments = new String[fnames.length];
                int hitCtr = -1;
                int pos =  - 1;
                int limit = fnames.length + startOffset;
                FieldSelector selector = (doFrags)?DDBDPServlet.NAME_AND_TEXT:DDBDPServlet.NAME_ONLY;

                IndexReader reader =  DOC_READER;
                //long totalTokenizing = 0;
                //long totalHighlighting = 0;
                while((pos = hits.nextSetBit(pos+1)) != -1 && ++hitCtr < limit){
                    if(hitCtr < startOffset) continue;
                    int index = hitCtr - startOffset;
                    Document doc = reader.document(pos,selector);
                    fnames[index] = doc.get(DDBDPServlet.FNAME_FIELD);
                    if(!doFrags) continue;
                    String text = doc.getField(DDBDPServlet.TEXT_FIELD).stringValue();
                    //long start = System.currentTimeMillis();
                    TokenStream textTokens = DDBDPServlet.getTokenFilter(resultMode, text);
                    //long tokened = System.currentTimeMillis();
                    char [] chars = new char[0];
                    if (textTokens != null){

                        TextFragment [] frags = HighlightUtil.getBestTextFragmentsNoGroup(new CachingTokenFilter(textTokens),text,fragmenter,scorer, true, 5);

                        //totalHighlighting += (System.currentTimeMillis() - tokened);
                        //totalTokenizing += (tokened - start);
                        int fLen = 0;
                        for(TextFragment t:frags){
                            if(t != null) fLen += (t.toString().length());
                        }
                        
                        StringBuffer charBuff = new StringBuffer(fLen*2);
                        int fPos = 0;
                        char [] tChars;
                        for(TextFragment t:frags){
                            if(t != null && t.getScore()>0){
                                if(fPos > 0) {
                                    //System.arraycopy(new char[]{'<','b','r','/','>'}, 0, chars, fPos, 5);
                                    charBuff.append(new char[]{'<','b','r','/','>'});
                                    fPos += 5;
                                }
                                String tString = t.toString();
                                Matcher m = LINE.matcher(tString);
                                if(m.find()){
                                    tString = m.replaceAll("<b class=\"lineNumber\">(line $2)</b>");
                                }
                                tChars = tString.toCharArray();
                               //System.arraycopy(tChars,0,chars,fPos,tChars.length) ;
                                charBuff.append(tChars);
                               fPos += tChars.length;
                               //System.arraycopy(sep,0,chars,fPos,sepLen);
                               charBuff.append(sep);
                               fPos += sepLen;
                               char [] score = (" ( " + t.getScore() + " )").toCharArray();
                               if(chars.length < fPos + score.length){
                                   char [] buf = new char[fPos+score.length];
                                   
                                   //System.arraycopy(chars,0,buf,0,chars.length);
                                   chars = buf;
                               }
                               charBuff.append(score);
                               //System.arraycopy(score,0,chars,fPos,score.length);
                               fPos += score.length;
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
//                    cached.reset();
                }
//                System.out.println("Total tokenizing: " + totalTokenizing);
//                System.out.println("Total highlighting: " + totalHighlighting);
            }

            req.setAttribute(DDBDPServlet.FILENAMES_ARRAY_ATTR, fnames);
            req.setAttribute(DDBDPServlet.FRAGMENTS_ARRAY_ATTR, fragments);
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/search.jsp");
            rd.forward(req,res);
    }    

}
