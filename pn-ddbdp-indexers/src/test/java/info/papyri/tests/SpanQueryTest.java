package info.papyri.tests;

import info.papyri.epiduke.lucene.Indexer;

import java.util.BitSet;
import java.io.*;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.SubstringPhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;

import info.papyri.epiduke.lucene.spans.SpanSequenceQuery;
import info.papyri.epiduke.lucene.spans.SubstringSpanTermQuery;

public class SpanQueryTest extends GreekTestsBase {
    private static final Term invL11W3 = WORD_SPAN_TEMPLATE.createTerm("^τις^");
    private static final Term L2W2i = new Term(Indexer.WORD_SPAN_TERM_DF,"^εξ^");
    public void testSingleSpan() throws IOException {
        System.out.println(BAR);
        System.out.println("testSingleSpan");
        Term t = L2W2i.createTerm("^και^");
        TermQuery shouldMatch = new TermQuery(t); 
        BitSet controlBits = new BitSet(iSearch.maxDoc());
        iSearch.search(shouldMatch,new BitSetCollector(controlBits));
        int shouldExpected = 11;
        int shouldActual = controlBits.cardinality();
        int next = -1;
        while((next = controlBits.nextSetBit(next + 1)) != -1){
            System.out.println(iSearch.doc(next).get("fileName"));
        }
        String errorMsg = "Expected " + shouldExpected + " hits for " + shouldMatch + "; got " + shouldActual;
        assertTrue(errorMsg,(shouldExpected == shouldActual));
        BitSet testBits = new BitSet(iSearch.maxDoc());
        SubstringSpanTermQuery test = new SubstringSpanTermQuery(t,bigramsDF);
        iSearch.search(test,new BitSetCollector(testBits));
        next = -1;
        int last = next;
        while((next = controlBits.nextSetBit(next + 1)) != -1){
            assertTrue(testBits.get(next));
            last = next;
        }
        assertTrue(testBits.nextSetBit(last + 1) == -1);
    }
    
    public void testStaggeredPhrase() throws IOException {
        System.out.println(BAR);
        System.out.println("testStaggeredPhrase");
        
        SubstringPhraseQuery shouldMatch = new SubstringPhraseQuery();
        shouldMatch.add(invL11W3.createTerm("ναπ")); // inv line 30
        shouldMatch.add(invL11W3.createTerm("^ἧι")); // inv line 30 "ἂν"
        shouldMatch.add(invL11W3.createTerm( "ἂν")); // inv line 30 "ἂν"
        shouldMatch.add(invL11W3.createTerm("^ἡμέραι")); // inv line 30
        shouldMatch.add(invL11W3.createTerm("^ἀπο")); // line 30
        BitSet controlBits = new BitSet(iSearch.maxDoc());
        BitSet testBits = new BitSet(iSearch.maxDoc());
        iSearch.search(shouldMatch,new BitSetCollector(controlBits));
        int expected = 1;
        int actual = controlBits.cardinality();
        String errorMsg = "Expected " + expected + " hits for " + shouldMatch + "; got " + actual;
        if(expected != actual){
            System.err.println(errorMsg);
            printMatchedDocs(controlBits,iSearch, System.err);
        }
        assertTrue(errorMsg,(expected == actual));
        SubstringSpanTermQuery t1 = new SubstringSpanTermQuery(invL11W3.createTerm("ναπ"),bigrams);
        SubstringSpanTermQuery t2 = new SubstringSpanTermQuery(invL11W3.createTerm("^ἧι"),bigrams);
        SubstringSpanTermQuery t3 = new SubstringSpanTermQuery(invL11W3.createTerm("^ἀπο"),bigrams);
        SubstringSpanTermQuery t4 = new SubstringSpanTermQuery(invL11W3.createTerm("ήμερον^"),bigrams);
        SpanNearQuery p1 = new SpanNearQuery(new SpanQuery[]{t1,t2},0,true);
        SpanNearQuery p2 = new SpanNearQuery(new SpanQuery[]{t3,t4},0,true);
        SpanNearQuery p12 = new SpanNearQuery(new SpanQuery[]{p1,p2},3,true);
        iSearch.search(p12,new BitSetCollector(testBits));
        expected  = controlBits.cardinality();
        actual = testBits.cardinality();
        errorMsg = "Expected " + expected + " hits for " + p12 + "; got " + actual;
        if(actual != expected){
            System.err.println(t1 + " " + iSearch.search(t1).length() + " hits");
            System.err.println(t2 + " " + iSearch.search(t2).length() + " hits");
            System.err.println(t3 + " " + iSearch.search(t3).length() + " hits");
            System.err.println(t4 + " " + iSearch.search(t4).length() + " hits");
            System.err.println(p1 + " " + iSearch.search(p1).length() + " hits");
            System.err.println(p2 + " " + iSearch.search(p2).length() + " hits");
        }
        assertEquals(errorMsg,expected,actual);
        int next = -1;
        int last = next;
        while((next = controlBits.nextSetBit(next + 1)) != -1){
            assertTrue(testBits.get(next));
            last = next;
        }
        assertTrue(testBits.nextSetBit(last + 1) == -1);    }
    
    public void testNot() throws IOException {
        System.out.println(BAR);
        System.out.println("testNot");
        String kai = "^κα\u1f76^";
        String thn = "^τ\u1f74ν^";
        String allo = "^ἄλλο^";
        SpanTermQuery kaiQ = new SpanTermQuery(invL11W3.createTerm(kai));
        SpanTermQuery thnQ = new SpanTermQuery(invL11W3.createTerm(thn));
        SpanTermQuery alloQ = new SpanTermQuery(invL11W3.createTerm(allo));
        SpanNearQuery p1 = new SpanNearQuery(new SpanQuery[]{kaiQ,thnQ},0,true);
        SpanNearQuery p2 = new SpanNearQuery(new SpanQuery[]{kaiQ,alloQ},0,true);
        SpanNotQuery not = new SpanNotQuery(p1,p2);
        BitSet testBits = new BitSet(iSearch.maxDoc());
        iSearch.search(not,new BitSetCollector(testBits));
        assertTrue(testBits.cardinality() > 0);
        String bgu = "bgu.2.619.xml";
        String pmich = "p.mich.1.42.xml";
        int found = 0;
        int next = -1;
        while((next = testBits.nextSetBit(next+1))!= -1){
            String doc = iSearch.doc(next).get("fileName");
            if(bgu.equals(doc)){
                found += 1;
            }
            else if(pmich.equals(doc)){
                found += 2;
            }
            else if(doc != null){
                found += 4;
            }
        }
        assertTrue("Did not find " + bgu + " among matches", ((found & 1) == 1));
        assertTrue("Did not find " + pmich + " among matches", ((found & 2) == 2));
        int expected = 2;
        if(testBits.cardinality() != expected){
            printMatchedDocs(testBits, iSearch, System.err);
        }
        assertTrue("More than " + expected + " documents matched: " + testBits.cardinality() + " out of " + iSearch.maxDoc(),testBits.cardinality()== expected);
    }
    public void testSequenceSpans() throws IOException {
        String t1 = "^κα\u1f76^";
        String t2 = "^ὑπὸ";

        Term substring = new Term(Indexer.WORD_SPAN_TERM,t1);
        Term substring2 = new Term(Indexer.WORD_SPAN_TERM,t2);
        SpanQuery[] spans = new SpanQuery[]{new SubstringSpanTermQuery(substring,bigrams),new SubstringSpanTermQuery(substring2,bigrams)};
        SpanNearQuery control = new SpanNearQuery(spans,0,true);
        SpanSequenceQuery test = new SpanSequenceQuery(spans);
        Spans cSpans = control.getSpans(iSearch.getIndexReader());
        Spans tSpans = test.getSpans(iSearch.getIndexReader());
        int ctr = 0;
       
        while(cSpans.next()){
            ctr++;
            assertTrue(tSpans.next());
            String cFile = check.doc(cSpans.doc()).get("fileName");
            String tFile = check.doc(tSpans.doc()).get("fileName");
            assertEquals("doc failed on match: " + ctr + " e: " + cFile + " a:" + tFile,cSpans.doc(),tSpans.doc());
            assertEquals("start failed on match: " + ctr,cSpans.start(),tSpans.start());
            assertEquals("end failed on match: " + ctr,cSpans.end(),tSpans.end());
        }
        boolean fail = false;
        int s = tSpans.start();
        int e = tSpans.end();
        int d = tSpans.doc();
        while(tSpans.next()){
            fail = true;
            String tFile = check.doc(tSpans.doc()).get("fileName");
            System.err.println("Extra match: " + tFile);
            if(s == tSpans.start() && e == tSpans.end() && d == tSpans.doc()){
                System.err.println("tSpan not advancing match: " + tFile);
                break;
            }
            s = tSpans.start();
            e = tSpans.end();
            d = tSpans.doc();
        }
        assertTrue(!fail);
    }
    public void testSequenceSpansPerformance() throws IOException {
        // "πωλουσαν" "^πωλο"
        String t0 = "^πωλ";
        String t1 = "και";
        String t2 = "υπο";
        org.apache.lucene.search.IndexSearcher searcher = check;
        Term substring0 = new Term(Indexer.WORD_SPAN_TERM_DF,t0);
        Term substring1 = new Term(Indexer.WORD_SPAN_TERM_DF,t1);
        Term substring2 = new Term(Indexer.WORD_SPAN_TERM_DF,t2);
        SpanQuery[] spans = new SpanQuery[]{new SubstringSpanTermQuery(substring0,bigramsDF),new SubstringSpanTermQuery(substring1,bigramsDF),new SubstringSpanTermQuery(substring2,bigramsDF)};
        SpanNearQuery control = new SpanNearQuery(spans,0,true);
        SpanSequenceQuery test = new SpanSequenceQuery(spans);
        Spans cSpans = control.getSpans(searcher.getIndexReader());
        Spans tSpans = test.getSpans(searcher.getIndexReader());
        int ctr = 0;
       
        while(cSpans.next()){
            ctr++;
            assertTrue(tSpans.next());
            String cFile = searcher.doc(cSpans.doc()).get("fileName");
            String tFile = searcher.doc(tSpans.doc()).get("fileName");
            assertEquals("doc failed on match: " + ctr + " e: " + cFile + " a:" + tFile,cSpans.doc(),tSpans.doc());
            assertEquals("start failed on match: " + ctr,cSpans.start(),tSpans.start());
            assertEquals("end failed on match: " + ctr,cSpans.end(),tSpans.end());
        }
        boolean fail = false;
        int s = tSpans.start();
        int e = tSpans.end();
        int d = tSpans.doc();
        int repeats = 0;
        while(tSpans.next()){
            String tFile = searcher.doc(tSpans.doc()).get("fileName");
            System.err.println("Extra match: " + tFile);
            if(s == tSpans.start() && e == tSpans.end() && d == tSpans.doc()){
                System.err.println("tSpan not advancing match: " + tFile);
                repeats++;
            }
            else repeats = 0;
            if(repeats > 5){
                System.err.println("Breaking on repeats: " + repeats);
                break;
            }
            s = tSpans.start();
            e = tSpans.end();
            d = tSpans.doc();
        }
        assertTrue(!fail);
        cSpans = control.getSpans(searcher.getIndexReader());
        tSpans = test.getSpans(searcher.getIndexReader());
        assertTrue(cSpans.next());
        assertTrue(tSpans.next());
        long cStart = System.currentTimeMillis();
        while(cSpans.next()){}
        long tStart = System.currentTimeMillis();
        while(tSpans.next()){}
        long tEnd = System.currentTimeMillis();
        long tTime = tEnd - tStart;
        long cTime = tStart - cStart;
        assertTrue("tTime (" + tTime + ") > cTime (" + cTime +") !", tTime < cTime);
        System.out.println("tTime (" + tTime + ") ; cTime (" + cTime +")");
    }
}
