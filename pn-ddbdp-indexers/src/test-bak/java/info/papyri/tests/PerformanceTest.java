package info.papyri.tests;

import info.papyri.epiduke.lucene.IndexOfQuery;
import info.papyri.epiduke.lucene.Indexer;
import info.papyri.epiduke.lucene.bigrams.SubstringQuery;
import info.papyri.epiduke.lucene.WildcardSubstringQuery;
import info.papyri.epiduke.lucene.spans.*;
import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SubstringPhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.*;
import java.util.*;
public class PerformanceTest extends GreekTestsBase {

	private static final Term WORD_SPAN_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM,"");
    private static final Term invL11W4 = WORD_SPAN_TEMPLATE.createTerm("#ἀγοράσηι#");
    private static final Term invL11W4substring = WORD_SPAN_TEMPLATE.createTerm("\u03B3\u03BF\u03C1\u1F71");

    
    public void testSubstringQueryTime() throws IOException {
        System.out.println(BAR);
        System.out.println("testSubstringQueryTime");
        //Directory rotate = FSDirectory.getDirectory(new File("pcolRotate"));
        //IndexSearcher searchRotate = new IndexSearcher(IndexReader.open(rotate));
        long itime = 0;
        long btime = 0;
        long wtime = 0;
        int isize = 0;
        int bsize = 0;
        int wsize = 0;
        long start = System.currentTimeMillis();
        for(int i=0;i<50;i++){
        Query shouldMatch = new IndexOfQuery(invL11W4substring); 
        Hits hits = check.search(shouldMatch);
        isize = hits.length();
        }
        long end = System.currentTimeMillis();
        itime += (end - start);
        //        Iterator<org.apache.lucene.search.Hit> hIter = hits.iterator();
//        ArrayList<String> indexOf = new ArrayList<String>();
//        while(hIter.hasNext()){
//            org.apache.lucene.search.Hit hit = hIter.next();
//            indexOf.add((hit.get("fileName")));
//        }

        Term wildcard = invL11W4substring.createTerm("*" + invL11W4substring.text() + "*");
        start = System.currentTimeMillis();
        for(int i=0;i<50;i++){
            Query shouldMatch = new WildcardQuery(wildcard);
        Hits hits = check.search(shouldMatch);
        wsize = hits.length();
        assertEquals(isize,wsize);
        }
        end = System.currentTimeMillis();
        wtime += (end - start);
        
        
        start = System.currentTimeMillis();
        for(int i=0;i<50;i++){
            Query shouldMatch = new SubstringQuery(invL11W4substring,bigrams); 
        Hits hits = check.search(shouldMatch);
        bsize = hits.length();
        assertEquals(isize,bsize);
//        ArrayList<String>grams = new ArrayList<String>();
//        hIter = hits.iterator();
//        while(hIter.hasNext()){
//            org.apache.lucene.search.Hit hit = hIter.next();
//            grams.add((hit.get("fileName")));
//        }
        }
        end = System.currentTimeMillis();
        btime += (end - start);

        System.out.println("indexof check: " + isize + " hits found in " + Long.toString(itime) + " ms");
        System.out.println("wildcard check: " + wsize + " hits found in " + Long.toString(wtime) + " ms");
        System.out.println("bigrams check: " + bsize + " hits found in " + Long.toString(btime) + " ms");
        
        
        int checkCtr = 0;
        Term empty = invL11W4substring.createTerm("");
        TermEnum checkTerm = check.getIndexReader().terms(empty);
        //TermEnum rotateTerm = searchRotate.getIndexReader().terms(empty);
        while(checkTerm.next() ){
            if(checkTerm == null || checkTerm.term().field() != empty.field()) break;
            checkCtr++;
        }while(checkTerm.next());
//        while(rotateTerm.next() && rotateTerm.term().field() == empty.field()){
//            rotateCtr++;
//        }
        //rotate.close();
        //System.out.print("rotate terms: " + rotateCtr + "; ");
        System.out.println("check terms: " + checkCtr);
        
    }
    

    public void testMultipleWildCardQueryTime() throws IOException {
        System.out.println(BAR);
        System.out.println("testMultipleWildCardQueryTime");
        String term = "Σκην\u1FF6ν";
        String wcTerm = "Σ??ν?ν";

        
        WildcardSubstringQuery wc = new WildcardSubstringQuery(WORD_SPAN_TEMPLATE.createTerm(wcTerm),bigrams);
        long start = System.currentTimeMillis();
        Hits hits = check.search(wc);
        long end = System.currentTimeMillis();
        
        System.out.println("check: " + hits.length() + " hits found in " + Long.toString(end - start) + " ms");
        Iterator<org.apache.lucene.search.Hit> hIter = hits.iterator();
        while(hIter.hasNext()){
            org.apache.lucene.search.Hit hit = hIter.next();
            System.out.println(hit.get("fileName"));
        }
    }


    public void testSubstringPhraseQueryTime() throws IOException {
        System.out.println(BAR);
        System.out.println("testSubstringPhraseQueryTime");
        // p.col.3.2 line 14
        Term term1 = WORD_SPAN_TEMPLATE.createTerm("θου");
        Term term2 = WORD_SPAN_TEMPLATE.createTerm("ντεσ"); //"ντες";
        Term term3 = WORD_SPAN_TEMPLATE.createTerm("λαβο");
        SubstringPhraseQuery query = new SubstringPhraseQuery();
        query.add(term1);
        query.add(term2);
        query.add(term3);
        long start = System.currentTimeMillis();
        Hits hits = check.search(query);
        long end = System.currentTimeMillis();
        SpanQuery [] spans = new SpanQuery[]{
        		new SubstringSpanTermQuery(term1,bigrams),
        		new SubstringSpanTermQuery(term2,bigrams),
        		new SubstringSpanTermQuery(term3,bigrams)
        };
        SpanSequenceQuery query2 = new SpanSequenceQuery(spans);
        check.search(query2);
        long end2 = System.currentTimeMillis();
        System.out.println("shanghai check: " + hits.length() + " hits found in " + Long.toString(end - start) + " ms");
        System.out.println("bigrams  check: " + hits.length() + " hits found in " + Long.toString(end2 - end) + " ms");
        Iterator<Hit> hIter = hits.iterator();
        while(hIter.hasNext()){
            Hit hit = hIter.next();
            System.out.println(hit.get("fileName"));
        }
    }
    
    public void testSorting() throws IOException{
        System.out.println(BAR);
        System.out.println("testSorting");
        final BitSet bits = new BitSet(this.check.maxDoc());
        TermQuery query = new TermQuery(WORD_SPAN_TEMPLATE.createTerm("^καὶ^"));
        this.check.search(query,new HitCollector(){
            public void collect(int doc, float score){
                if(score > 0) bits.set(doc);
            }
        });
        IndexReader reader = this.check.getIndexReader();
        Term FNAME = new Term("fileName".intern(),"");
        TermEnum terms =reader.terms(FNAME);
        TermDocs td = null; //this.check.getIndexReader().termDocs();
        int [] results = new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        int ctr =-1;
        int next = -1;
        terms:
        do{
            Term nTerm = terms.term();
            if(!FNAME.field().equals(nTerm.field())){
                System.err.println(nTerm.field() + " " + nTerm.text());
                break terms;
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
                        next = curr;
                        break terms;
                    }
                }
            }
        }while(terms.next());
        if(td != null && td.next() && next == -1) next = td.doc();
        for(int i:results){
            if(i == -1) break;
           Document doc = check.doc(i);
           System.out.println("" + i + ". " + doc.get("fileName"));
        }
        if(next != -1) System.out.println("next doc: " + next + " " + check.doc(next).get("fileName"));
    }
    
}