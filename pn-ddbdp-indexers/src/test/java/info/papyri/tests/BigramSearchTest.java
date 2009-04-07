package info.papyri.tests;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.lucene.analysis.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.document.*;
import org.apache.lucene.util.BitVector;
import java.io.*;
import java.util.*;

import info.papyri.epiduke.lucene.Indexer;
import info.papyri.epiduke.lucene.bigrams.SubstringTermDelegate;
import info.papyri.epiduke.lucene.bigrams.WildcardSubstringDelegate;

public class BigramSearchTest extends GreekTestsBase  {
    public void setUp() throws Exception {
        super.setUp();
    }
    
    
    public void testBigramsForSubstrings() throws IOException {
        String substring = "και";
        
        Term whole = WORD_SPAN_DF_TEMPLATE.createTerm(substring);
        Term b1 = WORD_SPAN_DF_TEMPLATE.createTerm("κα");
        Term b2 = WORD_SPAN_DF_TEMPLATE.createTerm("αι");
        long sStart = System.currentTimeMillis();
        SubstringTermDelegate del = new SubstringTermDelegate(bigramsDF, whole);
        String [] matches = del.matches(check.getIndexReader());
        long bStart = System.currentTimeMillis();
        PhraseQuery bQuery = new PhraseQuery();
        bQuery.add(b1);
        bQuery.add(b2);
        final BitVector hits = new BitVector(bigramsDF.maxDoc());
        HitCollector hc = new HitCollector(){
            public void collect(int doc, float score){
                hits.set(doc);
            }
        };
        bigramsDF.search(bQuery,hc);
        ArrayList<String> terms = new ArrayList<String>();
        for(int i = 0; i < bigramsDF.maxDoc();i++){
            if(hits.get(i)){
                terms.add(bigramsDF.doc(i).get("term"));
            }
        }
        long bEnd = System.currentTimeMillis();
        System.out.println("Time report: del" + (bStart - sStart) + " ; bigram: " + (bEnd - bStart) );
        if(matches.length != terms.size()){
            System.err.println("del term len: " + matches.length + "; bigram len: " + terms.size());
        }
        boolean fail = false;
        for(String t:matches){
            if(!terms.contains(t)){
                System.err.println("Del caught: " + t);
                fail = true;
            }
            terms.remove(t);
        }
        for(String s:terms){
            System.err.println("Bigrams caught: " + s);
            fail = true;
        }
        assertFalse(fail);
    }
    
    public void testBigramsForPrefixes() throws IOException {
        String substring = "και";
        Term sub = WORD_SPAN_DF_TEMPLATE.createTerm(substring);
        String prefix = "^και";
        Term pre = WORD_SPAN_DF_TEMPLATE.createTerm(prefix);
        SubstringTermDelegate subDel = new SubstringTermDelegate(bigramsDF, sub);
        SubstringTermDelegate preDel = new SubstringTermDelegate(bigramsDF, pre);
        String [] subMatches = subDel.matches();
        String [] preMatches = preDel.matches();
        assertTrue(subMatches.length > preMatches.length);
        String startsWith = prefix.substring(1);
        int ctr = 0;
        for(String t:preMatches){
        	if(!t.startsWith(startsWith)){
                System.out.println(ctr);
        		System.err.println(t + " does not match prefix " + startsWith);
        	}
            assertTrue(t.startsWith(startsWith));
            System.out.print('.');
            ctr++;
        }
        System.out.println(ctr);
        for(String t:subMatches){
            assertTrue(t.contains(substring));
        }
    }
    public void testWildcardNoWildcards() throws IOException {
        String prefix = "^και";
        Term pre = WORD_SPAN_DF_TEMPLATE.createTerm(prefix);
        SubstringTermDelegate wildDel = new WildcardSubstringDelegate(bigramsDF, pre);
        SubstringTermDelegate preDel = new SubstringTermDelegate(bigramsDF, pre);
        String [] wildMatches = wildDel.matches();
        String [] preMatches = preDel.matches();
        assertTrue(wildMatches.length ==  preMatches.length);
    }}
