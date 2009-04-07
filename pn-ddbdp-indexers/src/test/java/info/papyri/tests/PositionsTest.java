package info.papyri.tests;
import java.util.*;
import java.io.IOException;
import info.papyri.epiduke.lucene.Indexer;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import info.papyri.epiduke.lucene.*;

public class PositionsTest extends GreekTestsBase {
    public void testMergedDocs() throws IOException {
        String t2 = "^ὑπὸ";
        Term substring = new Term(Indexer.WORD_SPAN_TERM,t2);
        SubstringTermEnum  ste = new SubstringTermEnum(check.getIndexReader(),substring);
        ArrayList<Term> list = new ArrayList<Term>();
        while(ste.next()){
            list.add(ste.term());
        }
        Term [] terms = new Term[0];
        terms = list.toArray(terms);
        org.apache.lucene.index.MultipleTermPositions control = new org.apache.lucene.index.MultipleTermPositions(check.getIndexReader(),terms);
        info.papyri.epiduke.lucene.MultipleTermPositions test = new info.papyri.epiduke.lucene.MultipleTermPositions(check.getIndexReader(),terms);
        while(control.next()){
            boolean failed = false;
            while(test.doc() < control.doc() && test.next()){
                if (test.doc() != control.doc()){
                    System.err.println("merge >> " + test.doc());
                    failed = true;
                }
            }
            assertFalse(failed);
            int doc = test.doc();
            //assertTrue(merged.skipTo(doc));
            assertEquals(doc,test.doc());
            //merged.skipTo(multi.doc());
            if(test.doc() != control.doc()){
                System.err.println("multi << " + control.doc() + " merged.doc() = " + test.doc());
            }
            
        }
        assertFalse(test.next());
    }
    public void testMergedTerms() throws IOException {
        String t2 = "^ὑπὸ";
        Term substring = new Term(Indexer.WORD_SPAN_TERM,t2);
        SubstringTermEnum  ste = new SubstringTermEnum(check.getIndexReader(),substring);
        ArrayList<Term> list = new ArrayList<Term>();
        while(ste.next()){
            list.add(ste.term());
        }
        Term [] terms = new Term[0];
        terms = list.toArray(terms);
        org.apache.lucene.index.MultipleTermPositions multi = new org.apache.lucene.index.MultipleTermPositions(check.getIndexReader(),terms);
        info.papyri.epiduke.lucene.MultipleTermPositions merged = new info.papyri.epiduke.lucene.MultipleTermPositions(check.getIndexReader(),terms);
        while(multi.next()){
            boolean failed = false;
            while(merged.doc() < multi.doc() && merged.next()){
                if (merged.doc() != multi.doc()){
                    System.err.println("merge >> " + merged.doc());
                    failed = true;
                }
            }
            assertFalse(failed);
            int doc = merged.doc();
            if (merged.doc() < multi.doc())merged.skipTo(multi.doc());
            if(merged.doc() != multi.doc()){
                System.err.println("multi << " + multi.doc() + " merged.doc() = " + merged.doc());
            }
            String msg = check.doc(multi.doc()).get("fileName") + " differing freq: multi= " + multi.freq() + " merged= " + merged.freq();
            assertEquals(msg,multi.freq(),merged.freq());
            
        }
        assertFalse(merged.next());
    }}
