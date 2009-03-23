package info.papyri.ddbdp.servlet;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

import info.papyri.epiduke.lucene.analysis.AnchoredTokenStream;
import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer;
import info.papyri.epiduke.lucene.analysis.AncientGreekQueryAnalyzer;
import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer.Normalize;
import info.papyri.epiduke.lucene.Indexer;
import info.papyri.ddbdp.parser.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
public abstract class DDBDPServlet extends HttpServlet implements QueryFunctions {
   private static final Logger LOG = Logger.getLogger(DDBDPServlet.class);
	static class ProxTuple{
	    int offset;
	    String term;
	}
	public static final String CQL_ATTR = "info.papyri.CQL";
    public static final String DDB_ID_ARRAY_ATTR = "info.papyri.DDBIDS";
	public static final String FILENAMES_ARRAY_ATTR = "info.papyri.FNAMES";
	public static final String FRAGMENTS_ARRAY_ATTR = "info.papyri.FRAGMENTS";
	public static final String NEXT_RECORD_POS_ATTR = "info.papyri.NEXT";
	public static final String NUM_RECS_ATTR = "info.papyri.NUMRECS";
	public static final String QUERY_TIMER_ATTR = "info.papyri.STARTTIME";
	public static final String XQL_ATTR = "info.papyri.XQL";
	public static final String DIAGNOSTIC_URI_ATTR = "info.papyri.DIAGNOSTIC.URI";
	public static final String DIAGNOSTIC_DETAIL_ATTR = "info.papyri.DIAGNOSTIC.DETAIL";
	public static final String TEXT_FIELD = "text".intern();
	public static final AncientGreekAnalyzer[] TEXT_ANALYZERS = DDBDPServlet.getAnalyzers();
	public static final AncientGreekAnalyzer[] QUERY_ANALYZERS = DDBDPServlet.getQueryAnalyzers();
	public final static FieldSelector NAME_AND_TEXT = new FieldSelector(){
	    public FieldSelectorResult accept(String field){
	        if(FNAME_FIELD.equals(field) || DDB_ID_FIELD.equals(field) ||TEXT_FIELD.equals(field) || field.startsWith("sort")) return FieldSelectorResult.LOAD;
	        return FieldSelectorResult.NO_LOAD;
	    }
	};
	public final static FieldSelector NAME_ONLY = new FieldSelector(){
	    public FieldSelectorResult accept(String field){
	        if(FNAME_FIELD.equals(field) || DDB_ID_FIELD.equals(field) || field.startsWith("sort")) return FieldSelectorResult.LOAD;
	        return FieldSelectorResult.NO_LOAD;
	    }
	};
	static AncientGreekAnalyzer[] getAnalyzers() {
	    AncientGreekAnalyzer[] a = new AncientGreekAnalyzer[MODE_BETA_FILTER_ALL + 1];
	a[ MODE_NONE] = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.NONE,false);
	a[ MODE_FILTER_CAPITALS] = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE,false);
	a[ MODE_FILTER_DIACRITIC] = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.DIACRITICS,false);
	a[ MODE_FILTER_CAPITALS_AND_DIACRITICS] = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_DIACRITICS,false);
	a[ MODE_BETA] = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.BETACODE);
	a[ MODE_BETA_FILTER_DIACRITICS]= new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.BETACODE);
	a[ MODE_BETA_FILTER_CAPITALS] = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_BETACODE);
	a[ MODE_BETA_FILTER_ALL] = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_BETACODE);
	return a;
	}
	static AncientGreekAnalyzer[] getQueryAnalyzers() {
	    AncientGreekAnalyzer[] a = new AncientGreekAnalyzer[MODE_BETA_FILTER_ALL + 1];
	a[ MODE_NONE] = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.NONE);
	a[ MODE_FILTER_CAPITALS] = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.CASE);
	a[ MODE_FILTER_DIACRITIC] = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.DIACRITICS);
	a[ MODE_FILTER_CAPITALS_AND_DIACRITICS] = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_DIACRITICS);
	a[ MODE_BETA] = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.BETACODE);
	a[ MODE_BETA_FILTER_DIACRITICS]= new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.BETACODE);
	a[ MODE_BETA_FILTER_CAPITALS] = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_BETACODE);
	a[ MODE_BETA_FILTER_ALL] = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_BETACODE);
	return a;
	}
	public static final String DIAGNOSTIC_MESSAGE_ATTR = "info.papyri.DIAGNOSTIC.MESSAGE";
    public static final String DDB_ID_FIELD = "ddbdpId".intern();
	public static final String FNAME_FIELD = "fileName".intern();
	public static final Pattern WS = Pattern.compile("\\s");
    static String getResultTokenField(int mode){
        switch (mode){
        case MODE_NONE:
            return Indexer.WORD_SPAN_TERM;
        case MODE_FILTER_CAPITALS:
            return Indexer.WORD_SPAN_TERM_LC;
        case MODE_FILTER_DIACRITIC:
            return Indexer.LINE_SPAN_TERM_DF;
        case MODE_FILTER_CAPITALS_AND_DIACRITICS:
            return Indexer.WORD_SPAN_TERM_FL;
        case MODE_BETA:
            return Indexer.WORD_SPAN_TERM;
        case MODE_BETA_FILTER_DIACRITICS:
            return Indexer.LINE_SPAN_TERM_DF;
        case MODE_BETA_FILTER_CAPITALS:
            return Indexer.WORD_SPAN_TERM_LC;
        case MODE_BETA_FILTER_ALL:
            return Indexer.WORD_SPAN_TERM_FL;
        default:
            return Indexer.WORD_SPAN_TERM;
        }
        }
	public static TokenStream getTokenFilter(int mode, String  text){
	    AncientGreekAnalyzer analyzer;
	    switch (mode){
	    case MODE_NONE:
	        analyzer = TEXT_ANALYZERS[MODE_NONE];
	        break;
	    case MODE_FILTER_CAPITALS:
	        analyzer = TEXT_ANALYZERS[MODE_FILTER_CAPITALS];
	        break;
	    case MODE_FILTER_DIACRITIC:
	        analyzer = TEXT_ANALYZERS[MODE_FILTER_DIACRITIC];
	        break;
	    case MODE_FILTER_CAPITALS_AND_DIACRITICS:
	        analyzer = TEXT_ANALYZERS[MODE_FILTER_CAPITALS_AND_DIACRITICS];
	        break;
	    case MODE_BETA:
	        analyzer = TEXT_ANALYZERS[MODE_BETA];
	        break;
	    case MODE_BETA_FILTER_DIACRITICS:
	        analyzer = TEXT_ANALYZERS[MODE_BETA_FILTER_DIACRITICS];
	        break;
	    case MODE_BETA_FILTER_CAPITALS:
	        analyzer = TEXT_ANALYZERS[MODE_BETA_FILTER_CAPITALS];
	        break;
	    case MODE_BETA_FILTER_ALL:
	        analyzer = TEXT_ANALYZERS[MODE_BETA_FILTER_ALL];
	        break;
	    default:
	        LOG.error("Unexpected mode value: " + mode);
	        analyzer = new AncientGreekAnalyzer();
	    }
	    TokenStream base = analyzer.tokenStream(null, new StringReader(text));
        return base;
	    //return anchor?new AnchoredTokenStream(base):base;
	}
	static TokenStream getQueryFilter(int mode, List<DDBDPServlet.ProxTuple> text){
	    if (text == null) throw new IllegalArgumentException("Cannot tokenize null text!");
	    StringBuilder buff = new StringBuilder();
	    for (DDBDPServlet.ProxTuple term:text){
	        buff.append(term.term);
	        buff.append(' ');
	    }
	    if( buff.charAt(buff.length()-1) == ' '){
	        buff.deleteCharAt(buff.length()-1);
	    }
	    return DDBDPServlet.getQueryFilter(mode,buff.toString());
	}
	static TokenStream getQueryFilter(int mode, String  text){
	    AncientGreekAnalyzer analyzer;
	    switch (mode){
	    case MODE_NONE:
	        analyzer = QUERY_ANALYZERS[MODE_NONE];
	        break;
	    case MODE_FILTER_CAPITALS:
	        analyzer = QUERY_ANALYZERS[MODE_FILTER_CAPITALS];
	        break;
	    case MODE_FILTER_DIACRITIC:
	        analyzer = QUERY_ANALYZERS[MODE_FILTER_DIACRITIC];
	        break;
	    case MODE_FILTER_CAPITALS_AND_DIACRITICS:
	        analyzer = QUERY_ANALYZERS[MODE_FILTER_CAPITALS_AND_DIACRITICS];
	        break;
	    case MODE_BETA:
	        analyzer = QUERY_ANALYZERS[MODE_BETA];
	        break;
	    case MODE_BETA_FILTER_DIACRITICS:
	        analyzer = QUERY_ANALYZERS[MODE_BETA_FILTER_DIACRITICS];
	        break;
	    case MODE_BETA_FILTER_CAPITALS:
	        analyzer = QUERY_ANALYZERS[MODE_BETA_FILTER_CAPITALS];
	        break;
	    case MODE_BETA_FILTER_ALL:
	        analyzer = QUERY_ANALYZERS[MODE_BETA_FILTER_ALL];
	        break;
	    default:
	        LOG.error("Unexpected mode value: " + mode);
	        analyzer = new AncientGreekAnalyzer();
	    }
	    return analyzer.tokenStream(null, new StringReader(text));
	}

}
