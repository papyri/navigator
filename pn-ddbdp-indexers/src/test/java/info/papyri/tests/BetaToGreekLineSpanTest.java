package info.papyri.tests;

import info.papyri.epiduke.lucene.Indexer;
import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer;
import info.papyri.epiduke.lucene.analysis.AncientGreekQueryAnalyzer;
import info.papyri.epiduke.lucene.analysis.LinePositionTokenStream;
import info.papyri.epiduke.sax.TEIHandler;

import java.io.IOException;

import java.util.Iterator;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import info.papyri.epiduke.lucene.WildcardSubstringQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import junit.framework.TestCase;

public class BetaToGreekLineSpanTest extends GreekTestsBase {
    private static final Term PantwnI = WORD_SPAN_DF_TEMPLATE.createTerm("παντων"); // pmich 5 295 l. 6/7
    private static final Term PantwnIBeta = WORD_SPAN_DF_TEMPLATE.createTerm("PANTWN"); // pmich 5 295 l. 6/7
    private static final Term FiladelfeiaIBeta = WORD_SPAN_DF_TEMPLATE.createTerm("*filadelfeia"); // pcol 249 line 4
    private static final Term FiladelfeiaI = WORD_SPAN_DF_TEMPLATE.createTerm("Φιλαδελφεια"); // pcol 249 line 4
    private static final Term FiladelfeiaIC = WORD_SPAN_FL_TEMPLATE.createTerm("φιλαδελφεια"); // pcol 249 line 4
    private static final String SUBSTRING_TEXT_IN = "^PAN?TWN^";
    private static final String SUBSTRING_TEXT_OUT = "^παν?των^";
    private static final Term TAIS = WORD_SPAN_DF_TEMPLATE.createTerm("ταισ"); // pmich;1;28
    private static final Term TAIS_BETA = WORD_SPAN_DF_TEMPLATE.createTerm("TAIS"); // pmich;1;28

    public void testBetaCodeInput() throws IOException {
        System.out.println(BAR);
        System.out.println("testBetaCodeInput");
        AncientGreekAnalyzer analyzer = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.BETACODE);
        TokenStream tokens = analyzer.tokenStream(null, new java.io.StringReader(PantwnIBeta.text()));
        String formC = tokens.next().termText();
        assertEquals(PantwnI.text(),formC);
        TermQuery query = new TermQuery(PantwnI.createTerm(formC));
        Hits hits = iSearch.search(query);
        int expected = 1;
        int actual = (hits == null)?0:hits.length();
        String errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        assertTrue(errorMsg,(expected == actual));
        tokens = analyzer.tokenStream(null, new java.io.StringReader(TAIS_BETA.text()));
        formC = tokens.next().termText();
        assertEquals(TAIS.text(),formC);
        query = new TermQuery(TAIS.createTerm(formC));
        hits = iSearch.search(query);
        expected = 1;
        actual = (hits == null)?0:hits.length();
        errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        assertTrue(errorMsg,(expected == actual));

        tokens = analyzer.tokenStream(null, new java.io.StringReader(FiladelfeiaIBeta.text()));
        formC = tokens.next().termText();
        assertEquals(FiladelfeiaI.text(),formC);
        query = new TermQuery(FiladelfeiaI.createTerm(formC));
        hits = iSearch.search(query);
        expected = 1;
        actual = (hits == null)?0:hits.length();
        errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        assertTrue(errorMsg,(expected == actual));

        analyzer = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_BETACODE);
        tokens = analyzer.tokenStream(null, new java.io.StringReader("*filadelfeia"));
        formC = tokens.next().termText();
        assertEquals(FiladelfeiaIC.text(),formC);
        query = new TermQuery(new Term(Indexer.WORD_SPAN_TERM_FL,formC));
        hits = iSearch.search(query);
        expected = 1;
        actual = (hits == null)?0:hits.length();
        errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        assertTrue(errorMsg,(expected == actual));
}
    public void testBetaCodeFilterWithWildcard() throws IOException {
        System.out.println(BAR);
        System.out.println("testBetaCodeFilterWithWildcard");
        AncientGreekAnalyzer analyzer = new AncientGreekQueryAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_BETACODE);
        TokenStream tokens = analyzer.tokenStream(null, new java.io.StringReader(SUBSTRING_TEXT_IN));
        String formC = tokens.next().termText();
        assertEquals(SUBSTRING_TEXT_OUT,formC);
        WildcardSubstringQuery query = new WildcardSubstringQuery(PantwnI.createTerm(formC),bigramsDF);
        Hits hits = iSearch.search(query);
        int expected = 1;
        int actual = (hits == null)?0:hits.length();
        String errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
//        System.out.println(((org.apache.lucene.search.Hit)hits.iterator().next()).get("fileName"));
        assertTrue(errorMsg,(expected == actual));
    }
}
