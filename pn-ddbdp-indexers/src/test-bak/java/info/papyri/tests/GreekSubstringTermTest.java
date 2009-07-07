package info.papyri.tests;

import info.papyri.epiduke.lucene.Indexer;
import info.papyri.epiduke.lucene.spans.*;
import info.papyri.epiduke.lucene.bigrams.SubstringQuery;
import info.papyri.epiduke.lucene.bigrams.SubstringTermDelegate;
import info.papyri.epiduke.lucene.WildcardSegmentDelegate;
import info.papyri.epiduke.lucene.bigrams.WildcardSubstringDelegate;
import info.papyri.epiduke.lucene.bigrams.WildcardSubstringQuery;
import info.papyri.epiduke.lucene.WildcardSubstringTermEnum;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.SubstringBigramPhraseQuery;
import org.apache.lucene.search.spans.*;

public class GreekSubstringTermTest extends GreekTestsBase {

	private static final Term L2W2 = WORD_SPAN_TEMPLATE.createTerm("^ἐξ^");
    private static final Term L2W2i = new Term(Indexer.WORD_SPAN_TERM_DF,"^\u03B5ξ^");
    private static final Term invL11W3 = WORD_SPAN_TEMPLATE.createTerm("^τισ^");
    // "γορά"
    private static final Term invL11W4fullTerm = WORD_SPAN_TEMPLATE.createTerm("^ἀγοράσηι^"); // "γορά"
    private static String pMich_28_42_sub = "γορά";
    private static String pcolInv_sub = "γορά";
    private static final Term invL11W4substring = WORD_SPAN_TEMPLATE.createTerm(pcolInv_sub); // createTerm("\u03B3\u03BF\u03C1\u1F71");
    private static final Term invL11W9substring = WORD_SPAN_TEMPLATE.createTerm("π\u1F71ντα");
    private static final Term invL11W1substringFAIL = WORD_SPAN_TEMPLATE.createTerm("^\u03B3\u03BF\u03C1\u1F71^");
    private static final Term fail = WORD_SPAN_TEMPLATE.createTerm("ἀγἐξσηι");
    private static final Term PantesI = new Term(Indexer.WORD_SPAN_TERM_DF,"^παντεσ"); // pcol 8 227 l. 26

    public void testDiacriticSensitiveSubstring() throws IOException {
        System.out.println(BAR);
        System.out.println("testDiacriticSensitiveSubstring");
        Term testIns = WORD_SPAN_DF_TEMPLATE.createTerm("γορα");
      SubstringQuery shouldMatch = new SubstringQuery(invL11W4substring,bigrams);
//        SubstringSpanTermQuery shouldMatch = new SubstringSpanTermQuery(testIns,bigramsDF); 
      
      SubstringTermDelegate del = new SubstringTermDelegate(bigrams,invL11W4substring);
      String [] terms = del.matches();
      for(String t:terms){
    	  System.out.println(t);
      }
      
        Hits hits = iSearch.search(shouldMatch);
        int shouldExpected = 3;
        int shouldActual = (hits == null)?0:hits.length();
        Iterator<Hit> iterator = hits.iterator();
        while(iterator.hasNext()){
        	System.out.println(iterator.next().get("fileName"));
        }
        String positiveErrorMsg = "Expected " + shouldExpected + " hits for " + shouldMatch + "; got " + shouldActual;
        assertTrue(positiveErrorMsg,(shouldExpected == shouldActual));
        SubstringQuery shouldNotMatch = new SubstringQuery(L2W2.createTerm(L2W2i.text()),bigrams);
        hits = iSearch.search(shouldNotMatch);
        int expected = 0;
        int actual = (hits == null)?0:hits.length();
        String negativeErrorMsg = "Expected " + expected + " hits for " + shouldMatch + "; got " + actual;
        assertTrue(negativeErrorMsg,(expected == actual));
    }
    

    public void testSubstringDelegate() throws IOException {
        System.out.println(BAR);
        System.out.println("testSubstringDelegate");

        String src = "boakpopkuyourbookbig";
        WildcardSegmentDelegate del = new WildcardSegmentDelegate("book");
        java.util.BitSet matches = del.offsets(src);
        int expected = 1;
        int actual = matches.cardinality();
        String errorMsg = "Expected " + expected + " matches for \"" + del.getSubstring() + "\" in \"" + src + "\"; got " + actual;
        assertTrue(errorMsg,(expected == actual));
        expected = 13;
        actual = matches.nextSetBit(0);
        errorMsg = "Expected " + expected + " match position; got " + actual;
        assertTrue(errorMsg,(expected == actual));
        
        src = "boakpopkuybookuorbig";
        expected = 1;
        matches = del.offsets(src);
        actual = matches.cardinality();
        errorMsg = "Expected " + expected + " matches for \"" + del.getSubstring() + "\" in \"" + src + "\"; got " + actual;
        assertTrue(errorMsg,(expected == actual));
        expected = 10;
        actual = matches.nextSetBit(0);
        errorMsg = "Expected " + expected + " match position; got " + actual;
        assertTrue(errorMsg,(expected == actual));
        
       src = "#τοσο\u1fe6το#";
       del = new WildcardSegmentDelegate("οσο");
       matches = del.offsets(src);
       expected = 1;
       actual = matches.cardinality();
       errorMsg = "Expected " + expected + " matches for \"" + del.getSubstring() + "\" in \"" + src + "\"; got " + actual;
       assertTrue(errorMsg,(expected == actual));
       expected = 2;
       actual = matches.nextSetBit(0);
       errorMsg = "Expected " + expected + " match position; got " + actual;
       assertTrue(errorMsg,(expected == actual));
       
       src = "#συναγορ\u1F71σηις,#";
       del = new WildcardSegmentDelegate("\u03B3\u03BF\u03C1\u1F71");
       matches = del.offsets(src);
       expected = 1;
       actual = matches.cardinality();
       errorMsg = "Expected " + expected + " matches for \"" + del.getSubstring() + "\" in \"" + src + "\"; got " + actual;
       assertTrue(errorMsg,(expected == actual));
       expected = 5;
       actual = matches.nextSetBit(0);
       errorMsg = "Expected " + expected + " match position; got " + actual;
       assertTrue(errorMsg,(expected == actual));
       
       src = "#ἀγαγόντες#";
       del = new WildcardSegmentDelegate("ντ");
       matches = del.offsets(src);
       expected = 1;
       actual = matches.cardinality();
       errorMsg = "Expected " + expected + " matches for \"" + del.getSubstring() + "\" in \"" + src + "\"; got " + actual;
       assertTrue(errorMsg,(expected == actual));
       expected = 6;
       actual = matches.nextSetBit(0);
       errorMsg = "Expected " + expected + " match position; got " + actual;
       assertTrue(errorMsg,(expected == actual));

       src = "#πορευομένου#";
       SubstringTermDelegate del2 = new SubstringTermDelegate(null,new Term("foo","νου#"));
       assertTrue(del2.matches(src));

       src = "#PANTWN#";
       SubstringTermDelegate del3 = new SubstringTermDelegate(null,new Term("foo","TWN#"));
       assertTrue(del3.matches(src));

       src = "#παντων#";
       SubstringTermDelegate del4 = new SubstringTermDelegate(null,new Term("foo","των#"));
       assertTrue(del4.matches(src));

       src = "#παντων#";
       SubstringTermDelegate del5 = new SubstringTermDelegate(null,new Term("foo","#παν"));
       assertTrue(del5.matches(src));

       src = "^πατρὸς^";
       SubstringTermDelegate del6 = new SubstringTermDelegate(null,new Term("foo","^πατρ"));
       assertTrue(del6.matches(src));

       //"#μου"
       src = "#μου#";
       del = new WildcardSegmentDelegate("#μου");
       matches = del.offsets(src);
       expected = 1;
       actual = matches.cardinality();
       errorMsg = "Expected " + expected + " matches for \"" + del.getSubstring() + "\" in \"" + src + "\"; got " + actual;
       assertTrue(errorMsg,(expected == actual));
       expected = 0;
       actual = matches.nextSetBit(0);
       errorMsg = "Expected " + expected + " match position; got " + actual;
       assertTrue(errorMsg,(expected == actual));
        
}
    
    public void testWildcardEnum() throws Exception{
        Term foo = new Term("foo","BB?CCC?DD");
        WildcardSubstringTermEnum del5 = new WildcardSubstringTermEnum(iSearch.getIndexReader(),foo);
        java.lang.reflect.Method termCompare = WildcardSubstringTermEnum.class.getDeclaredMethod("termCompare", new Class[]{Term.class});
        boolean access = termCompare.isAccessible();
        termCompare.setAccessible(true);
        Object result = termCompare.invoke(del5, foo.createTerm("BBxCCCxDD"));
        assertTrue(Boolean.valueOf(result.toString()));
        result = termCompare.invoke(del5, foo.createTerm("BBxCCCxCCCxDD"));
        assertFalse(Boolean.valueOf(result.toString()));
        WildcardSubstringTermEnum del6 = new WildcardSubstringTermEnum(iSearch.getIndexReader(),foo.createTerm("Να*ρα"));
        String text = "Νανκρατιν";
        result = termCompare.invoke(del6, foo.createTerm(text));
        assertTrue(Boolean.valueOf(result.toString()));
        termCompare.setAccessible(access);
        
    }
    
    public void testBigramImpl() throws IOException {
        String expected = "Σκην\u1FF6ν";
        String wcTerm = "Σ??ν?ν";
        Term term = WORD_SPAN_TEMPLATE.createTerm(wcTerm);
    	info.papyri.epiduke.lucene.WildcardSubstringDelegate shanghai = new info.papyri.epiduke.lucene.WildcardSubstringDelegate(term);
    	WildcardSubstringDelegate bg = new WildcardSubstringDelegate(bigrams,term);
    	String [] control = shanghai.matches(iSearch.getIndexReader());
        String [] test = bg.matches(iSearch.getIndexReader());
        System.out.println(BAR);
        System.out.println("testBigramImpl");
    	System.out.println("shanghai: " + control.length);
    	for(String t:control){
    		System.out.println("\t" + t.toString());
    	}
    	System.out.println("bigrams: " + test.length);
    	for(String t:test){
    		System.out.println("\t" + t.toString());
    	}
    	assertTrue(control.length <= test.length);
    }
        
    public void testSingleWildCard() throws IOException {
        String term = "Σκην\u1FF6ν";
        String wcTerm = "Σκην?ν"; //"Σκην?ν"; "ντ?ς"; "ντες";
        WildcardSubstringQuery wc = new WildcardSubstringQuery(WORD_SPAN_TEMPLATE.createTerm(wcTerm),bigrams);
        Hits hits = iSearch.search(wc);
        int expected = 1;
        int actual = (hits == null)?0:hits.length();
        String errorMsg = "Expected " + expected + " hits for " + wc + "; got " + actual;
        Iterator<Hit> hIter = hits.iterator();
        System.out.println(BAR);
        System.out.println("testSingleWildCard");
        while(hIter.hasNext()){
            Hit hit = hIter.next();
            System.out.println(hit.get("fileName"));
        }

        assertTrue(errorMsg,(expected == actual));
    }
    public void testMultipleWildCard() throws IOException {
        String term = "Σκην\u1FF6ν";
        String wcTerm = "Σ??ν?ν";
        WildcardSubstringQuery wc = new WildcardSubstringQuery(WORD_SPAN_TEMPLATE.createTerm(wcTerm),bigrams);
        Hits hits = iSearch.search(wc);
        int expected = 1;
        int actual = (hits == null)?0:hits.length();
        String errorMsg = "Expected " + expected + " hits for " + wc + "; got " + actual;
        Iterator<Hit> hIter = hits.iterator();
        System.out.println(BAR);
        System.out.println("testMultipleWildCard");
        while(hIter.hasNext()){
            Hit hit = hIter.next();
            System.out.println(hit.get("fileName"));
        }
        assertTrue(errorMsg,(expected == actual));
    }

    public void testSubstringSpanPhrase() throws IOException {
        System.out.println(BAR);
        System.out.println("testSubstringPhrase");
        // p.col.3.2 line 14
        String term1 = "θου";
        String term2 = "ντ?σ"; //"ντες";
        String term3 = "λαβο";
        SubstringSpanTermQuery span1 = new SubstringSpanTermQuery(WORD_SPAN_TEMPLATE.createTerm(term1),bigrams);
        SubstringSpanTermQuery span2 = new SubstringSpanTermQuery(WORD_SPAN_TEMPLATE.createTerm(term2),bigrams);
        SubstringSpanTermQuery span3 = new SubstringSpanTermQuery(WORD_SPAN_TEMPLATE.createTerm(term3),bigrams);
        SpanSequenceQuery query = new SpanSequenceQuery(new SpanQuery[]{span1,span2,span3});
        Hits hits = iSearch.search(query);
        int expected = 1;
        int actual = (hits == null)?0:hits.length();
        String errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        Iterator<Hit> hIter = hits.iterator();
        while(hIter.hasNext()){
            Hit hit = hIter.next();
            System.out.println(hit.get("fileName"));
        }
        assertTrue(errorMsg,(expected == actual));
    }
    public void testSubstringPhrase() throws IOException {
        System.out.println(BAR);
        System.out.println("testSubstringPhrase");
        // p.col.3.2 line 14
        String term1 = "θου";
        String term2 = "ντ?σ"; //"ντες";
        String term3 = "λαβο";
        SubstringBigramPhraseQuery query = new SubstringBigramPhraseQuery(bigrams);
        query.add(WORD_SPAN_TEMPLATE.createTerm(term1));
        query.add(WORD_SPAN_TEMPLATE.createTerm(term2));
        query.add(WORD_SPAN_TEMPLATE.createTerm(term3));
        Hits hits = iSearch.search(query);
        int expected = 1;
        int actual = (hits == null)?0:hits.length();
        String errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        Iterator<Hit> hIter = hits.iterator();
        while(hIter.hasNext()){
            Hit hit = hIter.next();
            System.out.println(hit.get("fileName"));
        }
        assertTrue(errorMsg,(expected == actual));
    }
    public void testSubstringPhraseWordBoundary() throws IOException {
        System.out.println(BAR);
        System.out.println("testSubstringPhraseWordBoundary");
        // p.col.4.92 line 3
        Term term1 = WORD_SPAN_TEMPLATE.createTerm("ου^"); // "μένου^" works
        Term term2 = WORD_SPAN_TEMPLATE.createTerm("^μου");
        SubstringSpanTermQuery span1 = new SubstringSpanTermQuery(term1,bigrams);
        SubstringSpanTermQuery span2 = new SubstringSpanTermQuery(term2,bigrams);
        SpanSequenceQuery query = new SpanSequenceQuery(new SpanQuery[]{span1,span2});

        Hits hits = iSearch.search(query);
        int expected = 1;
        int actual = (hits == null)?0:hits.length();
        String errorMsg = "Expected " + expected + " hits for " + query + "; got " + actual;
        Iterator<Hit> hIter = hits.iterator();
        while(hIter.hasNext()){
            Hit hit = hIter.next();
            System.out.println(hit.get("fileName"));
        }
        assertTrue(errorMsg,(expected == actual));
    }
    
    public void testWordBridgingSpanAndSupplied() throws IOException {
        System.out.println(BAR);
        System.out.println("testWordBridgingSpanAndSupplied");

        SubstringQuery shouldMatch = new SubstringQuery(PantesI,bigramsDF); 
        Hits hits = iSearch.search(shouldMatch);
        int shouldExpected = 1;
        int shouldActual = (hits == null)?0:hits.length();
        String positiveErrorMsg = "Expected " + shouldExpected + " hits for " + shouldMatch + "; got " + shouldActual;
        assertTrue(positiveErrorMsg,(shouldExpected == shouldActual));
    }
    public void testIgnoreHeaderText() throws IOException {
        System.out.println(BAR);
        System.out.println("testIgnoreHeaderText");

        SubstringQuery shouldMatch = new SubstringQuery(PantesI.createTerm("Automated"),bigramsDF); 
        Hits hits = iSearch.search(shouldMatch);
        int shouldExpected = 0;
        int shouldActual = (hits == null)?0:hits.length();
        String positiveErrorMsg = "Expected " + shouldExpected + " hits for " + shouldMatch + "; got " + shouldActual;
        assertTrue(positiveErrorMsg,(shouldExpected == shouldActual));
    }

}