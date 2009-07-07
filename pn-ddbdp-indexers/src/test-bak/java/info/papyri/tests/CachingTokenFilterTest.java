package info.papyri.tests;

import info.papyri.epiduke.lucene.analysis.AnchoredTokenStream;
import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer;
import info.papyri.epiduke.lucene.analysis.VectorTokenFilter;

import java.io.StringReader;
import java.io.IOException;

import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.memory.MemoryIndex;


public class CachingTokenFilterTest  extends GreekTestsBase {
    static boolean tokenEquals(Token t1, Token t2){
        if(t1 == null || t2 == null){
            System.out.println("cf: " + t1 + " ;  vf: " + t2);
            return false;
        }
        if(t1.startOffset() != t2.startOffset()){
            System.out.println("cf-st: " + t1.startOffset() + " ;  vf-st: " + t2.startOffset());
            return false;
        }
        if(t1.endOffset() != t2.endOffset()){
            System.out.println("cf-end: " + t1.endOffset() + " ;  vf-end: " + t2.endOffset());
            return false;
        }
        if(t1.getPositionIncrement() != t2.getPositionIncrement()){
            System.out.println("cf-pos: " + t1.endOffset() + " ;  vf-pos: " + t2.endOffset());
            return false;
        }
        if(t1.termLength() != t2.termLength()){
            System.out.println("cf-len: " + t1.endOffset() + " ;  vf-len: " + t2.endOffset());
            return false;
        }
        if(!java.util.Arrays.equals(t1.termBuffer(), t2.termBuffer())){
            System.out.println("cf: " + t1.termText() + " ;  vf: " + t2.termText());
            return false;
        }
        return true;
    }
    public void testVectorCaching() throws IOException {
        TermQuery nameFilter = new TermQuery(new Term("fileName","bgu.8.1836.xml"));
        Document doc = check.search(nameFilter).doc(0);
        System.out.println(doc.get("fileName"));
        String text = doc.getField("text").stringValue();

        AncientGreekAnalyzer analyzer = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.NONE);
        org.apache.lucene.analysis.TokenStream textTokens = analyzer.tokenStream(null, new StringReader(text));

        assertTrue(textTokens != null);
        textTokens = new AnchoredTokenStream(textTokens);

        VectorTokenFilter test = new VectorTokenFilter(textTokens);
        test.buildCache();
        test.reset();
        textTokens = analyzer.tokenStream(null, new StringReader(text));
        textTokens = new AnchoredTokenStream(textTokens);
        CachingTokenFilter control = new CachingTokenFilter(textTokens);
        Token t;
        System.out.println("Testing initial token correctness:");
        while((t=control.next()) != null){
            assertTrue(tokenEquals(t,test.next()));
            System.out.print('.');
        }
        System.out.println();
        assertTrue(test.next() == null);
        
        control.reset();
        test.reset();
        System.out.println("Testing reset token correctness:");
        while((t=control.next()) != null){
            assertTrue(tokenEquals(t,test.next()));
            System.out.print('.');
        }
        System.out.println();
        assertTrue(test.next() == null);

        System.out.println("Testing partial reset token correctness:");
        control.reset();
        test.reset();
        int i = 0;
        while((t=control.next()) != null){
            assertTrue(tokenEquals(t,test.next()));
            System.out.print('.');
            if(i++ == 5){
            	control.reset();
            	test.reset();
            }
        }
        System.out.println();
        assertTrue(test.next() == null);

        control.reset();
        test = test.clone();
        while((t=control.next()) != null){
            assertTrue(tokenEquals(t,test.next()));
        }
        assertTrue(test.next() == null);
        
        control.reset();
        test.reset();
        MemoryIndex controlIndex = new MemoryIndex();
        controlIndex.addField("words", control);
        MemoryIndex testIndex = new MemoryIndex();
        testIndex.addField("words",test);
        IndexReader crdr = controlIndex.createSearcher().getIndexReader();
        IndexReader trdr = testIndex.createSearcher().getIndexReader();
        TermEnum cEnum = crdr.terms();
        TermEnum tEnum = trdr.terms();
        assertEquals(cEnum.term(),tEnum.term());
        while(cEnum.next()){
        	assertTrue(tEnum.next());
            assertEquals(cEnum.term(),tEnum.term());
        }
        assertFalse(tEnum.next());
    }
}
