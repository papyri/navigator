package info.papyri.epiduke.lucene.bigrams;

import java.io.IOException;
import java.util.Collection;
import java.util.BitSet;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermTextSwap;
import org.apache.lucene.util.BitVector;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;

import info.papyri.epiduke.lucene.*;

public class SubstringSimilarity extends DefaultSimilarity {
   private final SubstringTermDelegate del;
    public SubstringSimilarity(){
        this.del = null;
    }
    public SubstringSimilarity(SubstringTermDelegate del){
        this.del = del;
    }
    @Override
    public float idf(Term substring, Searcher searcher) throws IOException {
        if(del == null) return 0f;
        del.setTerm(substring);
        String [] matches = del.matches();

        Term t = new Term(substring.field(),"");
        final BitVector or = new BitVector(searcher.maxDoc());
        HitCollector hc = new HitCollector(){
            @Override
            public void collect(int arg0, float arg1) {
               or.set(arg0);
            }
        };
        for(String text:matches){
            TermTextSwap.swapText(t, text);
            searcher.search(new TermQuery(t),hc);
        }

        return idf(or.count(), searcher.maxDoc());
      }
    
}
