package info.papyri.tests;

import info.papyri.epiduke.lucene.Indexer;
import info.papyri.epiduke.lucene.IndexOfQuery;
import info.papyri.epiduke.lucene.SubstringQuery;
import info.papyri.epiduke.lucene.SubstringDelegate;
import info.papyri.epiduke.lucene.SubstringTermDelegate;
import info.papyri.epiduke.lucene.WildcardSegmentDelegate;
import info.papyri.epiduke.lucene.WildcardSubstringQuery;
import info.papyri.epiduke.lucene.analysis.AnchoredTokenStream;
import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer;
import info.papyri.epiduke.lucene.analysis.AncientGreekAccentFilter;
import info.papyri.epiduke.lucene.analysis.AncientGreekLowerCaseFilter;
import info.papyri.epiduke.lucene.analysis.LinePositionTokenStream;
import info.papyri.epiduke.lucene.analysis.SubstringRotationTokenStream;
import info.papyri.epiduke.sax.TEIHandler;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import java.util.Iterator;
import java.util.BitSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import info.papyri.epiduke.lucene.analysis.AncientGreekTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.SubstringPhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Query;

import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import info.papyri.epiduke.lucene.spans.SubstringSpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import junit.framework.TestCase;

public class GreekFullTermTest extends GreekTestsBase {

	private static final Term EmoiI = WORD_SPAN_DF_TEMPLATE.createTerm("εμοι"); // pcol 11 300 l. 13
    private static final Term TonI = WORD_SPAN_DF_TEMPLATE.createTerm("των"); // pmich 5 100 l. 6
    private static final Term PantwnI = WORD_SPAN_DF_TEMPLATE.createTerm("παντων"); // pmich 5 295 l. 6/7
    private static final Term L2W2 = WORD_SPAN_TEMPLATE.createTerm("ἐξ");
    private static final Term L2W2i = new Term(Indexer.WORD_SPAN_TERM_DF,"εξ");
    private static final Term invL11W3 = WORD_SPAN_TEMPLATE.createTerm("τισ");
    private static final Term invL11W4FormD = WORD_SPAN_TEMPLATE.createTerm("\u03B1\u0313γορ\u03B1\u0301σηι");
    private static final Term invL11W4 = WORD_SPAN_TEMPLATE.createTerm("ἀγοράσηι"); // pcol;1;inv480;11 pmich;1;28;16
    private static final Term invL11W4substring = WORD_SPAN_TEMPLATE.createTerm("\u03B3\u03BF\u03C1\u1F71");
    private static final Term invL11W9substring = WORD_SPAN_TEMPLATE.createTerm("π\u1F71ντα");
    private static final Term invL11W1substringFAIL = WORD_SPAN_TEMPLATE.createTerm("\u03B3\u03BF\u03C1\u1F71");
    private static final Term fail = WORD_SPAN_TEMPLATE.createTerm("ἀγἐξσηι");
    private static final Term Filadelfeia = WORD_SPAN_TEMPLATE.createTerm("Φιλαδελφε\u1F77ᾳ"); //pcol 10 249 line 4
    private static final Term Prokeitai = WORD_SPAN_DF_TEMPLATE.createTerm("προκειται");
    private static final Term ean = WORD_SPAN_DF_TEMPLATE.createTerm("εαν");
    
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
    public void testDiacriticInsensitive() throws IOException {
        System.out.println(BAR);
        System.out.println("testDiacriticInsensitive");

        TermQuery shouldMatch = new TermQuery(L2W2i); 
        BitSet controlBits = new BitSet(iSearch.maxDoc());
        iSearch.search(shouldMatch,new BitSetCollector(controlBits));
        int shouldExpected = 4;
        int shouldActual = controlBits.cardinality();
        int next = -1;
        while((next = controlBits.nextSetBit(next + 1)) != -1){
            System.out.println(iSearch.doc(next).get("fileName"));
        }
        String errorMsg = "Expected " + shouldExpected + " hits for " + shouldMatch + "; got " + shouldActual;
        assertTrue(errorMsg,(shouldExpected == shouldActual));

        shouldMatch = new TermQuery(fail); 
        Hits hits = iSearch.search(shouldMatch);
        shouldExpected = 0;
        shouldActual = (hits == null)?0:hits.length();
        errorMsg = "Expected " + shouldExpected + " hits for " + shouldMatch + "; got " + shouldActual;
        assertTrue(errorMsg,(shouldExpected == shouldActual));
        }
    
    public void testDiacriticSensitive() throws IOException {
        System.out.println(BAR);
        System.out.println("testDiacriticSensitive");

        TermQuery shouldMatch = new TermQuery(L2W2); 
        Hits hits = iSearch.search(shouldMatch);
        int shouldExpected = 3;
        int shouldActual = (hits == null)?0:hits.length();
        Iterator<org.apache.lucene.search.Hit> hIter = (hits != null)?hits.iterator():null;
        while(hIter != null && hIter.hasNext()){
            org.apache.lucene.search.Hit hit = hIter.next();
            System.out.println(hit.get("fileName"));
        }
        
        String positiveErrorMsg = "Expected " + shouldExpected + " hits for " + shouldMatch + "; got " + shouldActual;
        assertTrue(positiveErrorMsg,(shouldExpected == shouldActual));
        TermQuery shouldNotMatch = new TermQuery(L2W2.createTerm(L2W2i.text()));
        hits = iSearch.search(shouldNotMatch);
        int expected = 0;
        int actual = (hits == null)?0:hits.length();
        String negativeErrorMsg = "Expected " + expected + " hits for " + shouldMatch + "; got " + actual;
        assertTrue(negativeErrorMsg,(expected == actual));
    }
    
    public void testUnclearFreetextSupplied() throws IOException {
        System.out.println(BAR);
        System.out.println("testUnclearFreetextSupplied");

        TermQuery shouldMatch = new TermQuery(EmoiI); 
        Hits hits = iSearch.search(shouldMatch);
        int expected = 1;
        int actual = (hits == null)?0:hits.length();
        Iterator<org.apache.lucene.search.Hit> hIter = (hits != null)?hits.iterator():null;
        while(hIter != null && hIter.hasNext()){
            org.apache.lucene.search.Hit hit = hIter.next();
            System.out.println(hit.get("fileName"));
        }
      
    }
    
    
    public void testLemSplitOverLine() throws IOException {
        System.out.println(BAR);
        System.out.println("testLemSplitOverLine");
        TermQuery query = new TermQuery(PantwnI);
        Hits hits = iSearch.search(query);
        int expected = 1;
        int actual = (hits == null)?0:hits.length();
        String errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        assertTrue(errorMsg,(expected == actual));
        PhraseQuery shouldMatch = new PhraseQuery();
        shouldMatch.add(TonI);
        shouldMatch.add(PantwnI);
        shouldMatch.setSlop(1);
        hits = iSearch.search(shouldMatch);
        expected = 1;
        actual = (hits == null)?0:hits.length();
        errorMsg = "Expected " + expected + " hits for " + shouldMatch + "; got " + actual;
        if(expected != actual){
            System.err.println(errorMsg);
            printMatchedDocs(hits, System.err);
        }
        assertTrue(errorMsg,(expected == actual));
        // TODO get the line phrase test here to ensure that pantwn counts on both lines
    }
    public void testTextSplitAcrossTags() throws IOException {
        System.out.println(BAR);
        System.out.println("testTextSplitAcrossTags");
        TermQuery query = new TermQuery(Filadelfeia);
        Hits hits = iSearch.search(query);
        int expected = 1;
        int actual = (hits == null)?0:hits.length();
        String errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        if(expected != actual){
            System.err.println(errorMsg);
            printMatchedDocs(hits, System.err);
        }
        assertTrue(errorMsg,(expected == actual));
        
        query = new TermQuery(Prokeitai);
        hits = iSearch.search(query);
        actual = (hits == null)?0:hits.length();
        errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        if(expected != actual){
            System.err.println(errorMsg);
            printMatchedDocs(hits, System.err);
        }
        assertTrue(errorMsg,(expected == actual));
        
        query = new TermQuery(ean);
        hits = iSearch.search(query);
        actual = (hits == null)?0:hits.length();
        expected = 3;
        errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        if(expected != actual){
            System.err.println(errorMsg);
            printMatchedDocs(hits, System.err);
        }
        assertTrue(errorMsg,(expected == actual));
    }
    
    public void testFormDInput() throws IOException {
        System.out.println(BAR);
        System.out.println("testFormDInput");
        AncientGreekAnalyzer analyzer = new AncientGreekAnalyzer();
        TokenStream tokens = analyzer.tokenStream(null, new java.io.StringReader(invL11W4FormD.text()));
        String formC = tokens.next().termText();
        assertEquals(invL11W4.text(),formC);
        TermQuery query = new TermQuery(invL11W4FormD.createTerm(formC));
        Hits hits = iSearch.search(query);
        int expected = 1;
        int actual = (hits == null)?0:hits.length();
        String errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        if(expected != actual){
            System.err.println(errorMsg);
            printMatchedDocs(hits, System.err);
        }
        assertTrue(errorMsg,(expected == actual));
    }

}