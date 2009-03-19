package util.lucene;

import org.apache.lucene.search.*;
import org.apache.lucene.document.*;

import java.util.*;
import java.io.IOException;

public class SimpleCollector extends org.apache.lucene.search.HitCollector {

    
    protected final IndexSearcher searcher;
    private final BitSet docs;
    
    public SimpleCollector(IndexSearcher s) throws IOException {
        searcher  = s;
        docs = new BitSet(searcher.maxDoc());
        docs.set(0,docs.length(),false);
    }
    public SimpleCollector(IndexSearcher s, BitSet docs) throws IOException {
        searcher  = s;
        this.docs = new BitSet(searcher.maxDoc());
        this.docs.set(0,docs.length(),false);
        this.docs.or(docs);
    }
    public void collect(int arg0, float arg1) {
        docs.set(arg0);
    }
    
    public BitSet get(){
        return docs;
    }
    
    public int length(){
        return docs.length();
    }
    
    public void reset(){
        docs.set(0,docs.size(),false);
    }
    
    public Iterator<Document> iterator(){
        return new Iterator<Document>(){
            int next = docs.nextSetBit(0);
            public boolean hasNext(){
                try{
                    return (next > -1 && docs.get(next) && next <= (searcher.maxDoc() - 1));    
                }
                catch (IOException ioe){
                    ioe.printStackTrace();
                    return false;
                }
            }
            
            public void remove(){
                throw new UnsupportedOperationException("Iterator.remove() not implemented");    
            }
            
            public Document next(){
                Document nextObj = null;
                try{
                    nextObj = searcher.doc(next);
                    if (next + 1 > searcher.maxDoc()){
                        next = -1;
                    }
                    else {
                        next = docs.nextSetBit(next + 1);
                    }
                    return nextObj;

                }
                catch (IOException ioe){
                    ioe.printStackTrace();
                    next = -1;
                    return null;    
                }
            }
        };
        
    }
    

}
