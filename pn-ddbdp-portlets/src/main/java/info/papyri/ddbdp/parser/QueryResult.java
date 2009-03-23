package info.papyri.ddbdp.parser;

import org.apache.lucene.search.Query;
import info.papyri.epiduke.lucene.IntQueue;
import org.apache.lucene.util.BitVector;

public class QueryResult {
    private final Query query;
    private final String field;
    public QueryResult(Query query, String field){
        this.query = query;
        this.field = field;
    }
    public Query getQuery(){
        return this.query;
    }
    public String getField(){
        return this.field;
    }
}
