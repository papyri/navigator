package info.papyri.ddbdp.parser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Vector;
import java.io.IOException;
import org.mozilla.javascript.*;
import info.papyri.ddbdp.servlet.*;
import info.papyri.epiduke.lucene.Indexer;
import info.papyri.epiduke.lucene.analysis.*;

import org.apache.log4j.Logger;
import org.apache.lucene.search.spans.*;
import info.papyri.epiduke.lucene.spans.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import info.papyri.epiduke.lucene.IntQueue;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.Scorer;

import sun.security.action.GetLongAction;

import edu.unc.epidoc.transcoder.BetaCodeParser;
import edu.unc.epidoc.transcoder.UnicodeCConverter;

public class QueryExecContext extends ScriptableObject implements QueryFunctions {

    public static final Object MODE_KEY = new Object();
    private static final Term RAW_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM, "");
    private static final Term IA_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM_FL, "");
    private static final Term IC_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM_LC, "");
    private static final Term IM_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM_DF, "");
    private static final Term LEMMA_TEMPLATE = new Term(Indexer.LEMMA_TERM, "");
    private static final int DEFAULT_RESULT_SIZE = 50;
    private static final Logger LOG = Logger.getLogger(QueryExecContext.class);
    private static final UnicodeCConverter converter = new UnicodeCConverter();
    private IndexSearcher searcher;
    private IndexSearcher bigrams;
    private IndexSearcher bigramsDF;
    private IndexSearcher bigramsLC;
    private IndexSearcher bigramsFL;
    private Connection db;

    public QueryExecContext() {
        Object[] myArray = new Object[0];
        Class[] signature = new Class[]{org.mozilla.javascript.Context.class, myArray.getClass(), Function.class, boolean.class};
        try {
            FunctionObject beta = new FunctionObject("beta", QueryExecContext.class.getDeclaredMethod("beta", signature), this);
            FunctionObject lemma = new FunctionObject("lemma", QueryExecContext.class.getDeclaredMethod("lemma", signature), this);
            FunctionObject term = new FunctionObject("term", QueryExecContext.class.getDeclaredMethod("term", signature), this);
            FunctionObject docs = new FunctionObject("docs", QueryExecContext.class.getDeclaredMethod("docs", signature), this);
            FunctionObject sub = new FunctionObject("sub", QueryExecContext.class.getDeclaredMethod("sub", signature), this);
            FunctionObject then = new FunctionObject("then", QueryExecContext.class.getDeclaredMethod("then", signature), this);
            FunctionObject near = new FunctionObject("near", QueryExecContext.class.getDeclaredMethod("near", signature), this);
            FunctionObject notnear = new FunctionObject("notnear", QueryExecContext.class.getDeclaredMethod("notnear", signature), this);
            FunctionObject and = new FunctionObject("and", QueryExecContext.class.getDeclaredMethod("and", signature), this);
            FunctionObject not = new FunctionObject("not", QueryExecContext.class.getDeclaredMethod("not", signature), this);
            FunctionObject or = new FunctionObject("or", QueryExecContext.class.getDeclaredMethod("or", signature), this);
            FunctionObject query = new FunctionObject("query", QueryExecContext.class.getDeclaredMethod("query", signature), this);
            this.put("beta", this, beta);
            this.put("lemma", this, lemma);
            this.put("term", this, term);
            this.put("docs", this, docs);
            this.put("sub", this, sub);
            this.put("then", this, then);
            this.put("near", this, near);
            this.put("notnear", this, notnear);
            this.put("and", this, and);
            this.put("or", this, or);
            this.put("not", this, not);
            this.put("query", this, query);
            defineConstProperty(this, CONST_IGNORE_CAPS);
            putConstProperty(this, CONST_IGNORE_CAPS, CONST_IGNORE_CAPS);
            defineConstProperty(this, CONST_IGNORE_MARKS);
            putConstProperty(this, CONST_IGNORE_MARKS, CONST_IGNORE_MARKS);
            defineConstProperty(this, CONST_IGNORE_ALL);
            putConstProperty(this, CONST_IGNORE_ALL, CONST_IGNORE_ALL);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public QueryExecContext(Connection db, IndexSearcher searcher, IndexSearcher bigrams, IndexSearcher bigramsDF, IndexSearcher bigramsLC, IndexSearcher bigramsFL) {
        this();
        this.db = db;
        setSearchers(searcher, bigrams, bigramsDF, bigramsLC, bigramsFL);
    }

    public synchronized void setDb(Connection db) {
        this.db = db;
    }

    public synchronized void setSearchers(IndexSearcher searcher, IndexSearcher bigrams, IndexSearcher bigramsDF, IndexSearcher bigramsFL, IndexSearcher bigramsLC) {
        this.searcher = searcher;
        this.bigrams = bigrams;
        this.bigramsDF = bigramsDF;
        this.bigramsLC = bigramsLC;
        this.bigramsFL = bigramsFL;
    }

    public synchronized void setBigramsPlain(IndexSearcher bigrams) {
    }

    @Override
    public String getClassName() {
        return "ParseContext";
    }
    private static final String TERM_HELP = "term() takes one String argument and an optional mode constant; it returns a positional query";
    private static final String SUB_HELP = "sub() takes one String argument and an optional mode constant; it returns a positional query";
    private static final String THEN_HELP = "then() takes two or more positional query arguments, and an optional positive integer slop value; it returns a positional query";
    private static final String NEAR_HELP = "near() takes two or more positional query arguments, and an optional positive integer slop value; it returns a positional query";
    private static final String NOTNEAR_HELP = "notnear() takes two positional query arguments, and an optional positive integer slop value; it returns a positional query";
    private static final String AND_HELP = "and() takes two query arguments; it returns a boolean query";
    private static final String OR_HELP = "or() takes two query arguments; it returns a boolean query";
    private static final String NOT_HELP = "not() takes two query arguments; it returns a boolean query";
    private static final String DOC_HELP = "doc() takes a query argument, an optional positive integer offset, and an optional positive integer limit to results returned";
    private static final String QUERY_HELP = "query() takes a query argument only";

    private static int getMode(Object mode, Scriptable cx) {
        Object IC = getProperty(cx, CONST_IGNORE_CAPS);
        Object IM = getProperty(cx, CONST_IGNORE_MARKS);
        Object IA = getProperty(cx, CONST_IGNORE_ALL);

        if (mode == null) {
            return MODE_NONE;
        }
        if (mode == IA) {
            return MODE_FILTER_CAPITALS_AND_DIACRITICS;
        } else if (mode == IM) {
            return MODE_FILTER_DIACRITIC;
        } else if (mode == IC) {
            return MODE_FILTER_CAPITALS;
        }
        return MODE_NONE;
    }

    private static Term getTermTemplate(int mode) {
        switch (mode) {
            case MODE_FILTER_CAPITALS:
                return IC_TEMPLATE;
            case MODE_FILTER_DIACRITIC:
                return IM_TEMPLATE;
            case MODE_FILTER_CAPITALS_AND_DIACRITICS:
                return IA_TEMPLATE;
            case MODE_LEMMAS:
                return RAW_TEMPLATE;
            default:
                return RAW_TEMPLATE;
        }
    }
    private static final String GET_MORPHS = "SELECT MORPH FROM APP.LEMMA WHERE LEMMA=?";

    private static String testDb(Connection db) throws IOException, SQLException {
        PreparedStatement ps = db.prepareStatement("SELECT * FROM APP.LEMMA WHERE LEMMA=?");
        ps.setString(1, "\u03c3\u03c4\u03c1\u03b1\u30c4\u03b7\u03b3\u03cc\u03c2");
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
            result.append(" | "+rs.getString(i+1) + " | ");
        }
        return "Test Results: " + result.toString();
        } else {
            return "no results.";
        }
    }

    private static SpanQuery getMorphs(String lemma, Connection db) throws IOException, SQLException {
        LOG.debug("Connection: " + db.getMetaData().getURL());
        LOG.debug(testDb(db));
        PreparedStatement ps = db.prepareStatement(GET_MORPHS);
        if (lemma.endsWith("\u03c3")) {
            lemma = lemma.substring(0, lemma.length() - 1);
            lemma += "\u03c2";
        }
        char[] lemmach = lemma.toCharArray();
        StringBuffer lemmaConv = new StringBuffer();
        for (char ch : lemmach) {
            lemmaConv.append("\\u"+Integer.toHexString((int)ch));
        }
        LOG.debug(lemmaConv.toString());
        ps.setString(1, lemma);
        ResultSet results = ps.executeQuery();
        int morphIx = results.findColumn("MORPH");
        ArrayList<String> morphs = new ArrayList<String>();
        while (results.next()) {
            String val = results.getString(morphIx);
            morphs.add(val);
        }
        if (morphs.size() == 0) {
            LOG.info("No morphs found, defaulting to lemma: " + lemma);
            morphs.add(lemma);
        }

        if (morphs.size() > 1) {
            SpanTermQuery[] spans = new SpanTermQuery[morphs.size()];
            int i = 0;
            for (String morph : morphs) {
                spans[i++] = new SpanTermQuery(RAW_TEMPLATE.createTerm(morph));
            }
            return new SpanOrQuery(spans);
        }
        return new SpanTermQuery(RAW_TEMPLATE.createTerm(morphs.get(0)));
    }

    public static Object lemma(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) throws IOException, SQLException {
        SpanQuery result = null;
        QueryExecContext scope = (QueryExecContext) funcObj.getParentScope();
        Connection db = scope.db;
        if (args.length > 2 || !(args[0] instanceof String)) {
            throw new IllegalArgumentException(TERM_HELP);
        }
        String term = (String) args[0];
        int mode = MODE_NONE;
        cx.putThreadLocal(MODE_KEY, Integer.valueOf(mode));
        AncientGreekAnalyzer a = DDBDPServlet.QUERY_ANALYZERS[mode];
        Term template = (args.length < 2) ? RAW_TEMPLATE : getTermTemplate(mode);
        TokenStream ts = a.tokenStream(template.field(), new java.io.StringReader(term));
        Token token = new Token();
        Vector<String> tList = new Vector<String>();

        while (ts.next(token) != null) {
            tList.add(new String(token.termBuffer(), 0, token.termLength()));
        }
        if (tList.size() > 1) {
            SpanQuery[] spans = new SpanQuery[tList.size()];
            int i = 0;
            for (String sTerm : tList) {
                spans[i++] = getMorphs(sTerm, db);
            }
            result = new SpanSequenceQuery(spans);
        } else {
            result = getMorphs(tList.firstElement(), db);
        }
        return result;
    }

    public static Object beta(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) {
        String beta = (String) args[0];
        try {
            String convertSrc = BetaCodeFilter.hideWildcards(beta, 1);
            BetaCodeParser bcp = new BetaCodeParser();
            bcp.setString(convertSrc);
            String converted = converter.convertToString(bcp);
            bcp.setString(convertSrc);
            LOG.debug("Converted: " + converter.convertToCharacterEntities(bcp));
            converted = BetaCodeFilter.showWildcards(converted);
            return converted;
        } catch (java.io.UnsupportedEncodingException e) {
            LOG.error(e);
            return "";
        }
    }

    public static Object term(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) throws IOException {
        SpanQuery result = null;
        if (args.length > 2 || !(args[0] instanceof String)) {
            throw new IllegalArgumentException(TERM_HELP);
        }
        String term = (String) args[0];
        if (args.length > 1) {
            LOG.debug("mode parm: " + args[1]);
        }
        int mode = (args.length > 1) ? getMode(args[1], funcObj.getParentScope()) : MODE_NONE;
        cx.putThreadLocal(MODE_KEY, Integer.valueOf(mode));
        AncientGreekAnalyzer a = DDBDPServlet.QUERY_ANALYZERS[mode];
        Term template = (args.length < 2) ? RAW_TEMPLATE : getTermTemplate(mode);
        TokenStream ts = a.tokenStream(template.field(), new java.io.StringReader(term));
        Token token = new Token();
        Vector<String> tList = new Vector<String>();
        while (ts.next(token) != null) {
            tList.add(new String(token.termBuffer(), 0, token.termLength()));
        }
        if (tList.size() > 1) {
            SpanTermQuery[] spans = new SpanTermQuery[tList.size()];
            int i = 0;
            for (String sTerm : tList) {
                spans[i++] = new SpanTermQuery(template.createTerm(sTerm));
            }
            result = new SpanSequenceQuery(spans);
//          result = new SpanNearQuery(spans,0,true);
        } else {
            result = new SpanTermQuery(template.createTerm(tList.firstElement()));
        }
        return result;
    }

    public static Object sub(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) throws IOException {
        SpanQuery result = null;
        if (args.length > 2 || !(args[0] instanceof String)) {
            throw new IllegalArgumentException(SUB_HELP);
        }
        QueryExecContext scope = (QueryExecContext) funcObj.getParentScope();
        String term = (String) args[0];
        if (args.length > 1) {
            LOG.debug("mode parm: " + args[1]);
        }
        int mode = (args.length > 1) ? getMode(args[1], funcObj.getParentScope()) : MODE_NONE;
        cx.putThreadLocal(MODE_KEY, Integer.valueOf(mode));
        AncientGreekAnalyzer a = DDBDPServlet.QUERY_ANALYZERS[mode];
        Term template = (args.length < 2) ? RAW_TEMPLATE : getTermTemplate(mode);
        IndexSearcher bigrams;
        switch (mode) {
            case DDBDPServlet.MODE_NONE:
                LOG.debug("search mode: MODE_NONE");
                bigrams = scope.bigrams;
                break;
            case DDBDPServlet.MODE_FILTER_CAPITALS:
                LOG.debug("search mode: MODE_FILTER_CAPITALS");
                bigrams = scope.bigramsLC;
                break;
            case DDBDPServlet.MODE_FILTER_DIACRITIC:
                LOG.debug("search mode: MODE_FILTER_DIACRITIC");
                bigrams = scope.bigramsDF;
                break;
            case DDBDPServlet.MODE_FILTER_CAPITALS_AND_DIACRITICS:
                LOG.debug("search mode: MODE_FILTER_CAPITALS_AND_DIACRITICS");
                bigrams = scope.bigramsFL;
                break;
            default:
                LOG.debug("search mode: MODE default");
                bigrams = scope.bigrams;
        }
        TokenStream ts = a.tokenStream(template.field(), new java.io.StringReader(term));
        Token token = new Token();
        Vector<String> tList = new Vector<String>();
        while (ts.next(token) != null) {
            String tString = new String(token.termBuffer(), 0, token.termLength());
            tList.add(tString);
        }
        if (tList.size() > 1) {
            SpanQuery[] spans = new SpanQuery[tList.size()];
            int i = 0;
            for (String sToken : tList) {
                spans[i++] = getSubTermQuery(template, sToken, bigrams);
            }
            result = new SpanSequenceQuery(spans);
        } else {
            result = getSubTermQuery(template, tList.firstElement(), bigrams);
        }
        return result;
    }

    private static final SpanQuery getSubTermQuery(Term template, String termText, IndexSearcher bigrams) throws IOException {
        if (termText.charAt(0) == '^') {
            if (termText.indexOf('*') == -1 && termText.indexOf('?') == -1) {
                if (termText.charAt(termText.length() - 1) == '^') {
                    termText = termText.substring(1, termText.length() - 1);
                    return new SpanTermQuery(template.createTerm(termText));
                } else {
                    return new PrefixSpanTermQuery(template.createTerm(termText.substring(1)));
                }
            } else {
                return new SubstringSpanTermQuery(template.createTerm(termText), bigrams);
            }
        } else {
            return new SubstringSpanTermQuery(template.createTerm(termText), bigrams);
        }
    }

    public static Object then(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) {
        SpanQuery result = null;
        final int last = args.length - 1;
        if ((args.length < 2) || (args.length < 3 && !(args[1] instanceof SpanQuery)) || !(args[last] instanceof SpanQuery || args[last] instanceof Number)) {
            throw new IllegalArgumentException(THEN_HELP);
        }
        int slop;
        int stop;
        if (args[last] instanceof Number) {
            stop = last;
            slop = ((Number) args[last]).intValue();
        } else {
            stop = args.length;
            slop = 0;
        }
        SpanQuery[] spans = new SpanQuery[stop];
        for (int i = 0; i < stop; i++) {
            spans[i] = (SpanQuery) args[i];
        }
        result = new SpanNearExclusive(spans, slop, true);
        return result;
    }

    public static Object near(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) {
        SpanQuery result = null;
        final int last = args.length - 1;
        if ((args.length < 2) || (args.length < 3 && !(args[1] instanceof SpanQuery)) || !(args[last] instanceof SpanQuery || args[last] instanceof Number)) {
            throw new IllegalArgumentException(NEAR_HELP);
        }
        int slop;
        int stop;
        if (args[last] instanceof Number) {
            stop = last;
            slop = ((Number) args[last]).intValue();
        } else {
            stop = args.length;
            slop = 0;
        }
        SpanQuery[] spans = new SpanQuery[stop];
        for (int i = 0; i < stop; i++) {
            spans[i] = (SpanQuery) args[i];
        }
        result = new SpanNearQuery(spans, slop, false);
        return result;
    }

    public static Object notnear(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) {
        SpanQuery result = null;
        int last = args.length - 1;
        if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException(NOTNEAR_HELP + "; (bad num args)");
        }
        if (!(args[0] instanceof SpanQuery) || !(args[1] instanceof SpanQuery)) {
            throw new IllegalArgumentException(NOTNEAR_HELP + "; (bad query args)");
        }
        if (args.length == 3 && !(args[2] instanceof Number)) {
            throw new IllegalArgumentException(NOTNEAR_HELP + "; (bad slop args)");
        }
        int slop;
        if (args[last] instanceof Number) {
            slop = ((Number) args[last]).intValue();
        } else {
            slop = 0;
        }
        SpanQuery yes = (SpanQuery) args[0];
        SpanQuery no = (SpanQuery) args[1];
        no = new SpanNearQuery(new SpanQuery[]{yes, no}, slop, false);
        result = new SpanNotQuery(yes, no);
        return result;
    }

    public static Object and(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) {

        if (args.length != 2) {
            throw new IllegalArgumentException(AND_HELP);
        }
        BooleanQuery result = new BooleanQuery();
        result.add((Query) args[0], BooleanClause.Occur.MUST);
        result.add((Query) args[1], BooleanClause.Occur.MUST);
        return result;
    }

    public static Object not(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) {

        if (args.length != 2) {
            throw new IllegalArgumentException(NOT_HELP);
        }
        BooleanQuery result = new BooleanQuery();
        result.add((Query) args[0], BooleanClause.Occur.MUST);
        result.add((Query) args[1], BooleanClause.Occur.MUST_NOT);
        return result;
    }

    public static Object or(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) {

        if (args.length != 2) {
            throw new IllegalArgumentException(OR_HELP);
        }
        BooleanQuery result = new BooleanQuery();
        result.setMinimumNumberShouldMatch(1);
        result.add((Query) args[0], BooleanClause.Occur.SHOULD);
        result.add((Query) args[1], BooleanClause.Occur.SHOULD);
        return result;
    }

    private static String getField(Query query) {
        if (query instanceof SpanQuery) {
            return ((SpanQuery) query).getField();
        }
        BooleanQuery b = (BooleanQuery) query;
        BooleanClause[] clauses = b.getClauses();
        if (clauses[0].getQuery() instanceof SpanQuery) {
            return ((SpanQuery) clauses[0].getQuery()).getField();
        } else {
            return getField((BooleanQuery) clauses[0].getQuery());
        }
    }

    public static Object query(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException(QUERY_HELP);
        }
        Query query = (Query) args[0];
        String field = getField(query);
        QueryExecContext scope = (QueryExecContext) funcObj.getParentScope();
        return new QueryResult(query, field);
    }

    public static Object docs(org.mozilla.javascript.Context cx, Object[] args, Function funcObj, boolean inNewExpr) throws IOException {

        if (args.length > 3) {
            throw new IllegalArgumentException(DOC_HELP);
        }
        Query query = (Query) args[0];
        String field = getField(query);
        QueryExecContext scope = (QueryExecContext) funcObj.getParentScope();
        final int limit; // = DEFAULT_RESULT_SIZE;
        final int offset;

        if (args.length > 1) {
            offset = ((Number) args[1]).intValue();
            if (args.length > 2) {
                limit = ((Number) args[2]).intValue();
            } else {
                limit = DEFAULT_RESULT_SIZE;
            }
        } else {
            offset = 0;
            limit = DEFAULT_RESULT_SIZE;
        }

        final IntQueue hits = new IntQueue(limit);

        Weight w = query.weight(scope.searcher);
        Scorer s = w.scorer(scope.searcher.getIndexReader());
        int ctr = 0;
        while (s.next()) {
            if (offset > ctr++) {
                continue;
            }
            if (hits.size() < limit) {
                hits.add(s.doc());
            }
        }
        SearchResult result = new SearchResult(query, hits, field, offset, ctr);
        return result;
    }
}
