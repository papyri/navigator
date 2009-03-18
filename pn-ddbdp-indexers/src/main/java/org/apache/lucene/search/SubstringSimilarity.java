package org.apache.lucene.search;

import java.io.IOException;
import java.util.Collection;
import java.util.BitSet;

import org.apache.lucene.index.Term;
import org.apache.lucene.util.BitVector;

import info.papyri.epiduke.lucene.*;

public class SubstringSimilarity extends DefaultSimilarity {
   private final SubstringTermEnum del;
    public SubstringSimilarity(){
        this.del = new SubstringTermEnum();
    }
    public SubstringSimilarity(SubstringTermEnum del){
        this.del = (del != null)?del:new SubstringTermEnum();
    }
    @Override
    public float idf(Term substring, Searcher searcher) throws IOException {
        del.resetToTerm(((IndexSearcher)searcher).getIndexReader(),substring);

        Term t = null;
        final BitVector or = new BitVector(searcher.maxDoc());
        do{
            if(( t = del.term()) != null){
                searcher.search(new TermQuery(t),new HitCollector(){
                    @Override
                    public void collect(int arg0, float arg1) {
                       or.set(arg0);
                    }
                });
            }
        }while(del.next());

        return idf(or.count(), searcher.maxDoc());
      }
    
}
