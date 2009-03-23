package info.papyri.ddbdp.parser;

import org.apache.lucene.search.Query;
import info.papyri.epiduke.lucene.IntQueue;
import org.apache.lucene.util.BitVector;

public class SearchResult {
    private final Query query;
    private final IntQueue hits;
    private final String field;
    private final int offset;
    private final int total;
    public SearchResult(Query query, IntQueue hits, String field, int offset, int total){
        this.query = query;
        this.hits = hits;
        this.field = field;
        this.offset = offset;
        this.total = total;
    }
    public Query getQuery(){
        return this.query;
    }
    public String getField(){
        return this.field;
    }
    public IntQueue getHits(){
        return this.hits;
    }
    public int totalMatched(){
        return this.total;
    }
    public int offset(){
        return this.offset;
    }
}
