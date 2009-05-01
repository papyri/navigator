package info.papyri.ddbdp.servlet;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;

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

import org.z3950.zing.cql.*;

import info.papyri.epiduke.lucene.*;
import info.papyri.epiduke.lucene.analysis.*;
import info.papyri.epiduke.lucene.substring.*;
import info.papyri.ddbdp.parser.QueryFunctions;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.SubstringPhraseQuery;
import org.apache.lucene.search.highlight.TermScorer;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.QueryTermExtractor;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.PNSpanScorer;
import org.apache.lucene.search.highlight.PNWeightedSpanTermExtractor;

public class Sru extends DDBDPServlet implements IndexEventListener, QueryFunctions {
    
    public void replaceSearchers(SearcherEvent event) {
        this.check = event.getMultiSearcher();
        this.bigrams[MODE_FILTER_DIACRITIC] = event.getDFBigrams();
        this.bigrams[MODE_FILTER_CAPITALS_AND_DIACRITICS] = event.getFLBigrams();
        this.bigrams[MODE_FILTER_CAPITALS] = event.getLCBigrams();
        this.bigrams[MODE_NONE] = event.getPlainBigrams();
    }
    private static final List<String> SR_PARMS = srParms();
    private static final List<String> SCAN_PARMS = scanParms();
    private IndexSearcher [] bigrams = new IndexSearcher[4];
    private IndexSearcher check;
    private IndexReader reader;
     private final CQLParser parser = new CQLParser();
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
        this.reader = (IndexReader)event.getSource();
    }

    private static void debug(String text){
        for (char c:text.toCharArray()){
            System.out.print(Integer.toHexString(c));
            System.out.print(' ');
        }
        System.out.println();
    }

     static TokenStream getTokenFilter(int mode, List<String> text, boolean anchor){
        if (text == null) throw new IllegalArgumentException("Cannot tokenize null text!");
        StringBuilder buff = new StringBuilder();
        for (String term:text){
            buff.append(term);
            buff.append(' ');
        }
        if( buff.charAt(buff.length()-1) == ' '){
            buff.deleteCharAt(buff.length()-1);
        }
        return DDBDPServlet.getTokenFilter(mode,buff.toString());
    }

     static List<DDBDPServlet.ProxTuple> getPhraseTerms(CQLProxNode pNode, int leftmostOffset){
        ArrayList<DDBDPServlet.ProxTuple> result = new ArrayList<DDBDPServlet.ProxTuple>();
        int offset = getSlop(pNode);
        if (pNode.left instanceof CQLProxNode){
            result.addAll(getPhraseTerms((CQLProxNode)pNode.left,leftmostOffset));
        }
        else if (pNode.left instanceof CQLTermNode){
            DDBDPServlet.ProxTuple left = new DDBDPServlet.ProxTuple();
            left.term = getTerm( (CQLTermNode)pNode.left);
            left.offset = leftmostOffset;
            result.add(left);
        }
        else if (pNode.left instanceof CQLSortNode){
            result.addAll(getTerms ((CQLSortNode)pNode.left,leftmostOffset));
        }
        else {
            throw new UnsupportedOperationException("Unexpected left node: " +pNode. left.getClass().getName());
        }
        if (pNode.right instanceof CQLProxNode){
            result.addAll(getPhraseTerms((CQLProxNode)pNode.right,offset));
        }
        else if (pNode.right instanceof CQLTermNode){
            DDBDPServlet.ProxTuple right = new DDBDPServlet.ProxTuple();
            right.offset = offset;
            right.term = getTerm( (CQLTermNode)pNode.right);
            result.add(right);
        }
        else if (pNode.right instanceof CQLSortNode){
            result.addAll(getTerms ((CQLSortNode)pNode.right,offset));
        }
        else {
            throw new UnsupportedOperationException("Unexpected right node: " +pNode. right.getClass().getName());
        }
        return result;
    }
     static List<DDBDPServlet.ProxTuple> getTerms(CQLSortNode sNode, int leftmostOffset){
         ArrayList<DDBDPServlet.ProxTuple> result = new ArrayList<DDBDPServlet.ProxTuple>();
         CQLNode node  = sNode.subtree;
         if (node instanceof CQLTermNode){
             DDBDPServlet.ProxTuple tuple = new DDBDPServlet.ProxTuple();
             tuple.offset = leftmostOffset;
             tuple.term = getTerm((CQLTermNode)node); 
             result.add(tuple);
         }
         else  if(node instanceof CQLProxNode){
             result.addAll(getPhraseTerms((CQLProxNode)node,0));
         }
         else if (node instanceof CQLSortNode){
             result.addAll(getTerms((CQLSortNode)node,leftmostOffset));
         }
         else{
             throw new UnsupportedOperationException("Unsupported sort node subtree type: " + node.getClass().getName());
         }
         return result;
     }
     public static String getTerm(CQLTermNode tNode) {
        String index = tNode.getIndex();
        String relation =  tNode.getRelation().getBase();
        boolean exact = ("==".equals(relation) || "exact".equals(relation));
        String result;
        if ("cql.serverChoice".equals(index) || "cql.keywords".equals(index)){
            result =  tNode.getTerm();
        }
        else{
            throw new IllegalArgumentException("Unsupported CQL index: " + index);
        }
        if(exact){
            int s = 0;
            if(result.charAt(0) != AnchoredTokenStream.ANCHOR){
                s +=1; 
            }
            if(result.charAt(result.length()-1) != AnchoredTokenStream.ANCHOR){
                s+=2;
            }
            switch(s){
            case 1:
                return AnchoredTokenStream.ANCHOR_STR + result;
            case 2:
                return result +AnchoredTokenStream.ANCHOR_STR;
            case 3:
                return AnchoredTokenStream.ANCHOR_STR + result +AnchoredTokenStream.ANCHOR_STR;
            default:
                return result;
            }
        }
        return result;

    }

     public static int getMode(CQLRelation relation){
        boolean Case = true;
        boolean Accents = true;
        boolean Beta = false;
        for(Modifier mod:relation.getModifiers()){
            if("respectcapitals".equals(mod.getType())){
                Case = false;
            }
            else if("respectaccents".equals(mod.getType())){
                Accents = false;
            }
            else if("locale".equals(mod.getType()) && "grc.beta".equals(mod.getValue())){
                Beta = true;
            }
        }
        int mode = 0;
        if (Case) mode += MODE_FILTER_CAPITALS;
        if (Accents || Beta) mode += MODE_FILTER_DIACRITIC;
        if(Beta) mode += MODE_BETA;
        return mode;
    }

     public static CQLRelation getLeftmostRelation(CQLBooleanNode node){
        if (node.left instanceof CQLTermNode){
            return ((CQLTermNode)node.left).getRelation();
        }
        else{
            return(getLeftmostRelation((CQLBooleanNode)node.left));
        }
    }
     
     static List<String> srParms(){
         String [] names = new String[]{"operation","version","query","startRecord","maximumRecords","recordPacking","recordSchema","recordXPath","resultSetTTL","sortKeys","stylesheet","extraRequestData"};
         return java.util.Arrays.asList(names);
     }
     static List<String> scanParms(){
         String [] names = new String[]{"operation","version","scanClause","responsePosition","maximumTerms","stylesheet","extraRequestData"};
         return java.util.Arrays.asList(names);
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
        req.setAttribute(DDBDPServlet.XQL_ATTR,""); // just default it
        req.setAttribute(DDBDPServlet.CQL_ATTR,""); // just default it
        String operation = req.getParameter("operation");
        String version = req.getParameter("version");
        String qParm =  req.getParameter("query");
        String sParm = req.getParameter("scanClause");
        if( operation == null || "explain".equals(operation)){
            RequestDispatcher rd = getServletContext().getRequestDispatcher("1.1".equals(version)?"/WEB-INF/explain-1-1.jsp":"/WEB-INF/explain.jsp");
            rd.forward(req,res);
            return;
        }
        
        if(!"1.2".equals(version) && !"1.1".equals(version)){
            if(version == null){
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/7");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "version");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR, "This request is missing the  mandatory version parameter.");
                RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/sr-diagnostic.jsp");
                rd.forward(req,res);
                return;
            }
            else {
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/5");
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "1.2");
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR, "This server supports versions up to 1.2; the requested version was " + version);
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/sr-diagnostic.jsp");
            rd.forward(req,res);
            return;
            }
        }


        
        if (operation.equals("searchRetrieve")){
            if (qParm == null || "".equals(qParm.trim())){
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/7");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "query");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR, "This request is missing the  mandatory query parameter.");
                RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/sr-diagnostic.jsp");
                rd.forward(req,res);
                return;
            }
            else{
                qParm = ScriptSearch.getSafeUTF8(qParm);
            }
            CQLNode qNode = null;

            try{
                qNode = parser.parse(qParm);
                req.setAttribute(DDBDPServlet.XQL_ATTR, "<sru:xQuery>" + qNode.toXCQL(0).replace("<searchClause>", "<searchClause xmlns=\"http://www.loc.gov/zing/cql/xcql/\">") + "</sru:xQuery>");
                req.setAttribute(DDBDPServlet.CQL_ATTR,qNode.toCQL().replaceAll("<","&lt;").replaceAll(">", "&gt;"));
            }
            catch (CQLParseException cqlex){
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/10");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR,cqlex.getMessage());
                RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/sr-diagnostic.jsp");
                rd.forward(req,res);
                return;
            }
            int max = 50;
            String sMax = req.getParameter("maximumRecords");
            if(sMax != null){
                sMax = sMax.trim();
                try{
                    max = Integer.parseInt(sMax);
                }
                catch (NumberFormatException nfe){
                    req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/6");
                    req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "maximumRecords");
                    req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR, "Invalid value for maximumRecords: " + sMax);
                    RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/sr-diagnostic.jsp");
                    rd.forward(req,res);
                    return;
                }
            }
            int start = 1;
            String sStart = req.getParameter("startRecord");
            if(sStart != null){
                sStart = sStart.trim();
                try{
                    start = Integer.parseInt(sStart);
                }
                catch (NumberFormatException nfe){
                    req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/6");
                    req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "maximumRecords");
                    req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR, "Invalid value for startRecord: " + sStart);
                    RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/sr-diagnostic.jsp");
                    rd.forward(req,res);
                    return;
                }
            }
            searchRetrieve(req,res,qNode,version,max,start);
            return;
        }
        else if("scan".equals(operation)){
            if (sParm == null || "".equals(sParm.trim())){
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/7");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "scanClause");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR, "This request is missing the  mandatory scanClause parameter.");
                RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/sr-diagnostic.jsp");
                rd.forward(req,res);
                return;
            }
            else{
                qParm = ScriptSearch.getSafeUTF8(qParm);
            }
            CQLNode qNode = null;

            try{
                qNode = parser.parse(qParm);
                req.setAttribute(DDBDPServlet.XQL_ATTR, "<sru:xQuery>" + qNode.toXCQL(0).replace("<searchClause>", "<scanClause xmlns=\"http://www.loc.gov/zing/cql/xcql/\">").replace("</searchClause>", "</scanClause>") + "</sru:xQuery>");
                req.setAttribute(DDBDPServlet.CQL_ATTR,qNode.toCQL().replaceAll("<","&lt;").replaceAll(">", "&gt;"));
            }
            catch (CQLParseException cqlex){
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/10");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR,cqlex.getMessage());
                RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/scan-diagnostic.jsp");
                rd.forward(req,res);
                return;
            }
            if (!(qNode instanceof CQLTermNode)){
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/10");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR,"Bad scan CQL");
                RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/scan-diagnostic.jsp");
                rd.forward(req,res);
                return;
            }
            scan(req,res,qNode,version);
            return;
        }
        else{
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/4");
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "");
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR,"Unsupported operation: " + operation);
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/sr-diagnostic.jsp");
            rd.forward(req,res);
            return;
        }
    }
    
    private void searchRetrieve(HttpServletRequest req, HttpServletResponse res, CQLNode qNode, String version, int maxRecs, int startRec) throws ServletException, IOException {
        java.util.Enumeration parmNames = req.getParameterNames();
        while(parmNames.hasMoreElements()){
            String pn = (String)parmNames.nextElement();
            if(!SR_PARMS.contains(pn)){
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/8");
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, pn);
                req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR, "Unsupported parameter: " + pn);
                RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/sr-diagnostic.jsp");
                rd.forward(req,res);
                return;
            }
        }
        String maxRecString = req.getParameter("maximumRecords");
        String [] fnames = new String[0];
        String [] fragments = new String[0];
        int mode =-1;

        Query query;
        Scorer scorer;
        Sort sort;
        BitSet hits = null;
        if (qNode instanceof CQLTermNode){
            mode = getMode(((CQLTermNode)qNode).getRelation());
        }
        else{
            CQLBooleanNode bNode = (CQLBooleanNode)qNode;
            mode = getMode(getLeftmostRelation(bNode));
        }
        try{
            Object [] tuple = processNode(qNode,this.check,this.bigrams);
            query = (Query)tuple[0];
            scorer = (Scorer)tuple[1];
            sort = (Sort)tuple[2];
        }
        catch(IllegalArgumentException ie){
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/16");
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, ie.getMessage().substring("Unsupported CQL index: ".length()));
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR, ie.getMessage());
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/sr-diagnostic.jsp");
            rd.forward(req,res);
            return;
        }
        hits = getHits(query, this.check);
        if(startRec > hits.cardinality()){
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/61");
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "");
            req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR,"start position " + startRec + " exceeds number of matches " + hits.cardinality());
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/sr-diagnostic.jsp");
            rd.forward(req,res);
            return;
        }
        String fieldName =getField(mode);


        if (hits.cardinality() > 0){

            Highlighter highlight = null;
            if (query instanceof SubstringPhraseQuery){
                SubstringPhraseQuery pq = (SubstringPhraseQuery)query;
                highlight = SubstringPhraseQuery.getHighlighter(pq, fieldName, null);
            }
            else if (query instanceof SubstringQuery){
                highlight = SubstringQuery.getHighlighter((SubstringQuery)query,check.getIndexReader());
            }
            else{
                highlight = new Highlighter(scorer);
                highlight.setTextFragmenter(new SimpleFragmenter());
            }
            int startOffset = startRec - 1;
            fnames = new String[Math.min(hits.cardinality() - startOffset,maxRecs)];
            fragments = new String[fnames.length];
            int [] results = new int[fnames.length];
            loadHits(hits,results,startRec,maxRecs,sort != null);

            int next = hits.nextSetBit(results[results.length-1]);
            if (next != -1){
                System.out.println("found one at " +hits.nextSetBit(results[results.length -1]+1) );
                req.setAttribute(DDBDPServlet.NEXT_RECORD_POS_ATTR, Integer.toString(startRec + results.length));
            }
            
            int hitCtr = -1;
            for(int result:results){
                hitCtr++;
                Document doc = reader.document(result,DDBDPServlet.NAME_AND_TEXT);
                fnames[hitCtr] = doc.get("fileName");

                String text = getText(doc.getField(DDBDPServlet.TEXT_FIELD));
                int resultMode = (mode >= MODE_BETA)?(mode -MODE_BETA):mode; // result text will not be in Betacode
                TokenStream textTokens = DDBDPServlet.getTokenFilter(resultMode, text);
                CachingTokenFilter cached;

                if (query instanceof SubstringPhraseQuery){
                    SubstringPhraseQuery pq = (SubstringPhraseQuery)query;

                    cached = SubstringPhraseQuery.getCachedTokens(pq,textTokens);
                }
                else if (query instanceof SubstringQuery){
                    cached = SubstringQuery.getCachedTokens((SubstringQuery)query,textTokens);
                }
                else{
                    cached = new CachingTokenFilter(textTokens);
                }

                if (textTokens != null){
                    cached.reset();
                    String frag = highlight.getBestFragment(cached, text);
                   if(frag != null){
                       frag = frag.replaceAll("<B>","~B~");
                       frag = frag.replaceAll("</B>","~/B~");
                       frag = frag.replaceAll("<","&lt;").replaceAll(">", "&gt;");
                       frag = frag.replaceAll("~B~","<B>");
                       frag = frag.replaceAll("~/B~","</B>");
                       fragments[hitCtr] = frag;
                   }
                   else fragments[hitCtr] = "";
                }
                else {
                    System.err.println("tokens was null!");
                    fragments[hitCtr] = "";
                }
                cached.reset();
            }
            System.out.println("hitCtr: " + hitCtr + " ; results.length: " + results.length);

        }
        req.setAttribute(DDBDPServlet.FILENAMES_ARRAY_ATTR, fnames);
        req.setAttribute(DDBDPServlet.NUM_RECS_ATTR, Integer.toString(hits.cardinality()));
        req.setAttribute(DDBDPServlet.FRAGMENTS_ARRAY_ATTR, fragments);
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/srw-dc.jsp");
        rd.forward(req,res);
        return;
    }
    
    private int loadHits(final BitSet bits, int[] results,  int startRec, int maxRecs, boolean sort ) throws IOException {

        java.util.Arrays.fill(results,-1);
        int result = -1;
        if (sort){
            System.out.println("sorting...");
            IndexReader reader = this.check.getIndexReader();
            Term FNAME = new Term("fileName".intern(),"");
            TermEnum terms =reader.terms(FNAME);
            TermDocs td = null; //this.check.getIndexReader().termDocs();
            int ctr =-1;

            sortterms:
                while(terms.next()){
                    Term nTerm = terms.term();
                    System.out.println(nTerm.field() + " " + nTerm.text());
                    if(!FNAME.field().equals(nTerm.field())){
                        System.err.println(nTerm.field() + " " + nTerm.text());
                        break sortterms;
                    }
                    td = reader.termDocs(nTerm);
                    docs:
                        while(td.next()){
                            int curr = td.doc();
                            if(bits.get(curr)){
                                if (++ctr < results.length){
                                    results[ctr] = curr;
                                }
                                else{
                                    break sortterms;
                                }
                            }
                        }
                }

            result =  Math.min(ctr,results.length);
        }
        else {
            int pos = -1;
            int ctr = -1;
            int start = startRec - 1;
            advance:
            while ((pos = bits.nextSetBit(pos + 1)) != -1){
                ctr++;
                if(ctr < start) continue;
                pos--;
                break advance;
            }     
            ctr = -1;
            nosort:
            while ((pos = bits.nextSetBit(pos + 1)) != -1){
                ctr++;

                if(ctr < results.length){
                    results[ctr] = pos;
                }
                else {
                    break nosort;
                }
            }
            result = ctr;
        }
        return result;
    }
    
    private void scan(HttpServletRequest req, HttpServletResponse res, CQLNode qNode, String version) throws ServletException, IOException {
        req.setAttribute(DDBDPServlet.DIAGNOSTIC_URI_ATTR, "info:srw/diagnostic/1/4");
        req.setAttribute(DDBDPServlet.DIAGNOSTIC_DETAIL_ATTR, "");
        req.setAttribute(DDBDPServlet.DIAGNOSTIC_MESSAGE_ATTR,"Unsupported operation: scan");
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/WEB-INF/scan-diagnostic.jsp");
        rd.forward(req,res);
        return;
    }
    
     static Object[] processNode(CQLNode bNode, IndexSearcher reader, IndexSearcher [] bigrams) throws IOException {
        Object [] result = new Object[3]; // {query, scorer,sort}
        if (bNode instanceof CQLAndNode){
            CQLAndNode qNode = (CQLAndNode)bNode;
            BooleanQuery query = new BooleanQuery();
            Object [] left = processNode(qNode.left,reader, bigrams);
            Object [] right = processNode(qNode.right,reader, bigrams);
//            scorer.setLeft((Scorer)left[1]);
//            scorer.setRight((Scorer)right[1]);
            query.add((Query)left[0], BooleanClause.Occur.MUST);
            query.add((Query)right[0], BooleanClause.Occur.MUST);
            result[0] = query;
            result[1] = new QueryScorer(query);
            return result;
        }
        if (bNode instanceof CQLOrNode){
            CQLOrNode qNode = (CQLOrNode)bNode;
            BooleanQuery query = new BooleanQuery();
            Object [] left = processNode(qNode.left,reader, bigrams);
            Object [] right = processNode(qNode.right,reader,bigrams);
//            scorer.setLeft((Scorer)left[1]);
//            scorer.setRight((Scorer)right[1]);
            query.add((Query)left[0], BooleanClause.Occur.SHOULD);
            query.add((Query)right[0], BooleanClause.Occur.SHOULD);
            result[0] = query;
            result[1] = new QueryScorer(query);
            return result;
        }
        if (bNode instanceof CQLNotNode){
            CQLNotNode qNode = (CQLNotNode)bNode;
            BooleanQuery query = new BooleanQuery();
//            BooleanScorer scorer = new NotScorer();
            Object [] left = processNode(qNode.left,reader, bigrams);
            Object [] right = processNode(qNode.right,reader, bigrams);
//            scorer.setLeft((Scorer)left[1]);
//            scorer.setRight((Scorer)right[1]);
            query.add((Query)left[0], BooleanClause.Occur.MUST);
            query.add((Query)right[0], BooleanClause.Occur.MUST_NOT);
            result[0] = query;
            result[1] = new QueryScorer(query);
            return result;
        }
        if (bNode instanceof CQLProxNode){
            CQLProxNode qNode = (CQLProxNode)bNode;
            SubstringPhraseQuery query = getQuery(qNode);
            result[0] = query;
            result[1] = new org.apache.lucene.search.highlight.PhraseScorer(query);
            return result;
        }
        if (bNode instanceof CQLSortNode){
            CQLSortNode qNode = (CQLSortNode)bNode;
            Sort sort = getSort(qNode);
            result[2] = sort;
        }

            CQLTermNode qNode = (CQLTermNode)bNode;
            Query query = getQuery(qNode,bigrams);
            result[0] = query;
            result[1] = TermScorer.getTermScorer(query);
            return result;
    }

     static String getText(Field field){
        if(field.stringValue() != null || !field.isTokenized()) return field.stringValue();
        TokenStream tokens = field.tokenStreamValue();
        if (tokens == null) return field.stringValue();
        StringBuilder result = new StringBuilder();
        try{
            Token next = tokens.next();
            while(next != null){
                result.append(next.termBuffer(),0,next.termLength());
                next = tokens.next(next);
                if (next != null) result.append(' ');
            }
            return result.toString();
        }
        catch (IOException ioe){
            System.err.println(ioe.toString());
            return "";
        }
    }

     static Query getQuery(CQLTermNode tNode, IndexSearcher [] bigrams) throws IOException {
        String term =getTerm(tNode);

        CQLRelation relation = tNode.getRelation();
        int mode = getMode(relation); 
        String fieldName = getField(mode);
        term = DDBDPServlet.QUERY_ANALYZERS[mode].tokenStream(term, new StringReader(term)).next().termText();
        Term searchTerm = new Term(fieldName,term);
        if (term.indexOf('*') != -1 || term.indexOf('?') != -1){
            
            return new WildcardSubstringQuery(searchTerm,bigrams[(mode & MODE_FILTER_CAPITALS_AND_DIACRITICS)]);
        }
        else{
            if (term.charAt(0) == AnchoredTokenStream.ANCHOR){
                if(term.charAt(term.length()-1) ==  AnchoredTokenStream.ANCHOR){
                    return new TermQuery(searchTerm);
                }
                else{
                    return new PrefixQuery(searchTerm);
                }
            }
            else{
                return new SubstringQuery(searchTerm);
            }
        }
    }
     
     public static int getSlop(CQLProxNode pNode){
         int slop = 0;
         for (Modifier mod: (pNode).ms.getModifiers()){
             if ("distance".equals(mod.getType()) && mod.getValue() != null){
                 slop = (mod.getValue().length() > 0)?Integer.parseInt(mod.getValue()):1;
                 slop = Math.max(slop -1,0);
             }
         }
         return slop;
     }
     
     public static boolean getOrdered(CQLProxNode pNode){
         for (Modifier mod: (pNode).ms.getModifiers()){
             if ("ordered".equals(mod.getType())){
                 return true;
             }
         }
         return false;
     }

     public static boolean getNotNear(CQLProxNode pNode){
         for (Modifier mod: (pNode).ms.getModifiers()){
             if ("ddb.not".equals(mod.getType())){
                 return true;
             }
         }
         return false;
     }

     static SubstringPhraseQuery getQuery(CQLProxNode pNode) throws IOException {
        CQLRelation relation = getLeftmostRelation(pNode);
        int mode = getMode(relation);
        List<DDBDPServlet.ProxTuple> terms = getPhraseTerms(pNode,0);
        TokenStream base = DDBDPServlet.getQueryFilter(mode, terms);
        String fieldName = getField(mode);
        SubstringPhraseQuery pQuery = new SubstringPhraseQuery();
        pQuery.useMultiPos(true);
        pQuery.useExact(true);
        Token token = null;
        int offset = -1;
        while ((token = base.next()) != null){
            offset++;
            pQuery.add(new Term(fieldName,new String(token.termBuffer(),0,token.termLength())),offset,terms.get(offset).offset);
        }
        return pQuery;
    }

     static BitSet getHits(CQLNode node, IndexSearcher search, IndexSearcher [] bigrams) throws IOException {

        if (node instanceof CQLAndNode){
            CQLAndNode bNode = (CQLAndNode)node;

            BitSet left = getHits(bNode.left,search,bigrams);
            left = getHits(bNode.right,search,bigrams,left,SetOp.AND);
//            left.and(right);
            return left;
        }
        if (node instanceof CQLOrNode){
            CQLOrNode bNode = (CQLOrNode)node;

            BitSet left = getHits(bNode.left,search,bigrams);
            left = getHits(bNode.right,search,bigrams,left,SetOp.OR);
            return left;
        }
        if (node instanceof CQLNotNode){
            CQLNotNode bNode = (CQLNotNode)node;

            BitSet left = getHits(bNode.left,search,bigrams);
            left = getHits(bNode.right,search,bigrams,left,SetOp.NOT);
            return left;
        }
        Query q = null;
        if(node instanceof CQLProxNode){
            q = getQuery((CQLProxNode)node);
        }
        else{
            q = getQuery((CQLTermNode)node, bigrams);
        }
        return getHits(q,search);
    }
     
     static BitSet getHits(CQLNode node, IndexSearcher search, IndexSearcher [] bigrams, BitSet left, SetOp mode) throws IOException {
         BitSet result;
         if (node instanceof CQLAndNode){
             CQLAndNode bNode = (CQLAndNode)node;
             BitSet interm = getHits(bNode.left,search,bigrams);
             interm = getHits(bNode.right,search,bigrams,interm,SetOp.AND);
             result =  interm;
         }
         else if (node instanceof CQLOrNode){
             CQLOrNode bNode = (CQLOrNode)node;
             BitSet interm = getHits(bNode.left,search,bigrams);
             interm = getHits(bNode.right,search,bigrams,interm,SetOp.OR);
             result =  interm;
         }
         else if (node instanceof CQLNotNode){
             CQLNotNode bNode = (CQLNotNode)node;
             BitSet interm = getHits(bNode.left,search,bigrams);
             interm = getHits(bNode.right,search,bigrams,interm,SetOp.NOT);
             result =  interm;
         }
         else if(node instanceof CQLProxNode){
             Query q = getQuery((CQLProxNode)node);
             return getHits(q,search,left,mode);
         }
         else{
             Query q = getQuery((CQLTermNode)node,bigrams);
             return getHits(q,search,left,mode);
         }
         
         if(mode == SetOp.AND){
             left.and(result);
             return left;
         }
         else if (mode == SetOp.OR){
             left.or(result);
             return left;
         }
         else if(mode == SetOp.NOT){
             left.andNot(result);
             return left;
         }
         System.err.println("Unexpected setop: " + mode);

         
         return result;
     }
     static Query getHighlightableQuery(CQLNode node, IndexSearcher search, IndexSearcher[] bigrams) throws IOException {
        Query query = null;
        if (node instanceof CQLAndNode){
            BooleanQuery result = new BooleanQuery();
            CQLBooleanNode bNode = (CQLBooleanNode)node;
            Query left = getHighlightableQuery(bNode.left,search,bigrams);
            Query right = getHighlightableQuery(bNode.right,search,bigrams);
//            String rel = bNode.ms.getBase();
            result.add(left, BooleanClause.Occur.MUST);
            result.add(right, BooleanClause.Occur.MUST);
            query = result;
        }
        else if (node instanceof CQLNotNode){
            BooleanQuery result = new BooleanQuery();
            CQLBooleanNode bNode = (CQLBooleanNode)node;
            Query left = getHighlightableQuery(bNode.left,search,bigrams);
            Query right = getHighlightableQuery(bNode.right,search,bigrams);
//            String rel = bNode.ms.getBase();
            result.add(left,BooleanClause.Occur.MUST);
            result.add(right,BooleanClause.Occur.MUST_NOT);
            query = result;
        }
        else if (node instanceof CQLOrNode){
            BooleanQuery result = new BooleanQuery();
            CQLBooleanNode bNode = (CQLBooleanNode)node;
            Query left = getHighlightableQuery(bNode.left,search,bigrams);
            Query right = getHighlightableQuery(bNode.right,search,bigrams);
//            String rel = bNode.ms.getBase();
            result.add(left, BooleanClause.Occur.SHOULD);
            result.add(right, BooleanClause.Occur.SHOULD);
            query = result;
        }
        else if(node instanceof CQLProxNode) {
           query  = getQuery((CQLProxNode)node);
        }
        else if(node instanceof CQLTermNode) {
            query  = getQuery((CQLTermNode)node,bigrams);
         }
        return query;
    }

     static BitSet getHits(Query query, IndexSearcher search) throws IOException {
        final BitSet hits = new BitSet(search.maxDoc());
        search.search(query,new HitCollector(){
            public void collect(int doc,float weight){
                hits.set(doc);
            }
        });
        return hits;
    }
     static BitSet getHits(Query query, IndexSearcher search, final BitSet left, SetOp op) throws IOException {
         if(op == SetOp.OR){
             search.search(query,new HitCollector(){
                 public void collect(int doc,float weight){
                     left.set(doc);
                 }
             });
             return left;
         }
         else if(op == SetOp.NOT){
             search.search(query,new HitCollector(){
                 public void collect(int doc,float weight){
                     left.set(doc,false);
                 }
             });
             return left;
         }
         else if(op == SetOp.AND){
             final BitSet hits = new BitSet(search.maxDoc());
             
             search.search(query,new HitCollector(){
                 public void collect(int doc,float weight){
                     hits.set(doc,left.get(doc));
                 }
             });
             return hits;
         }
         System.err.println("Unexpected setop: " + op);
         final BitSet hits = new BitSet(search.maxDoc());
         
         search.search(query,new HitCollector(){
             public void collect(int doc,float weight){
                 hits.set(doc);
             }
         });
         return hits;
     }

     static String getField(int mode){
        switch (mode){
        case MODE_FILTER_CAPITALS:
            return Indexer.WORD_SPAN_TERM_LC.intern();
        case MODE_FILTER_DIACRITIC:
            return Indexer.WORD_SPAN_TERM_DF.intern();
        case MODE_FILTER_CAPITALS_AND_DIACRITICS:
            return Indexer.WORD_SPAN_TERM_FL.intern();
        case MODE_BETA:
            return Indexer.WORD_SPAN_TERM_DF.intern();
        case MODE_BETA_FILTER_CAPITALS:
            return Indexer.WORD_SPAN_TERM_FL.intern();
        case MODE_BETA_FILTER_DIACRITICS:
            return Indexer.WORD_SPAN_TERM_DF.intern();
        case MODE_BETA_FILTER_ALL:
            return Indexer.WORD_SPAN_TERM_FL.intern();
        default:
            return Indexer.WORD_SPAN_TERM.intern();
        }

    }
     
     public static Sort getSort(CQLSortNode node){
         Sort result = new Sort();
         try{ 
             java.lang.reflect.Field keysField = node.getClass().getDeclaredField("keys");
         boolean access = keysField.isAccessible();
         keysField.setAccessible(true);
         Object modVect = keysField.get(node);
         Vector<ModifierSet> keys = (Vector<ModifierSet>)modVect;
         ArrayList<SortField> sfa = new ArrayList<SortField>(keys.size());
         for (ModifierSet key:keys){
             String base = key.getBase();
             if ("dc.identifier".equals(base) || "identifier".equals(base)){
                 boolean reverse = key.modifier("sort.descending") != null;
                 SortField sf = new SortField("filename",reverse);
                 sfa.add(sf);
             }
         }
         result.setSort(sfa.toArray(new SortField[0]));
         keysField.setAccessible(access);
         }
         catch(Throwable  e){
         }
         return result;
     }
     public static String parseFileName(String xmlFileName){
         int xml = xmlFileName.lastIndexOf('.');
         int doc = xmlFileName.substring(0,xml).lastIndexOf('.');
         int vol = xmlFileName.substring(0,doc).lastIndexOf('.');
         StringBuffer result = new StringBuffer(96);
         result.append("series=");
         result.append(xmlFileName.substring(0,vol));
         result.append("&volume=");
         result.append(xmlFileName.substring(vol+1,doc));
         result.append("&document=");
         result.append(xmlFileName.substring(doc+1,xml));
         return result.toString();
     }
    static class SetOp {
        public static SetOp AND = new SetOp("AND");
        public static SetOp OR = new SetOp("OR");
        public static SetOp NOT = new SetOp("NOT");
        private final String label;
        private SetOp(){
            this.label = "NONE";
        };
        private SetOp(String label){
            this.label = label;
        }
        public String toString(){
            return this.label;
        }
    }

}
