package info.papyri.ddbdp.servlet;

import java.util.EventObject;
import java.util.HashMap;
import org.apache.lucene.search.IndexSearcher;
public class SearcherEvent extends EventObject {
	private final IndexSearcher multiSearcher;
    private final IndexSearcher plainBigrams;
    private final IndexSearcher dfBigrams;
    private final IndexSearcher lcBigrams;
    private final IndexSearcher flBigrams;
    public SearcherEvent(IndexSearcher multiSearcher, IndexSearcher plainBigrams, IndexSearcher dfBigrams, IndexSearcher flBigrams, IndexSearcher lcBigrams){
    	super("SEARCHER_EVENT");
    	this.multiSearcher = multiSearcher;
        this.plainBigrams = plainBigrams;
        this.lcBigrams = lcBigrams;
        this.dfBigrams = dfBigrams;
        this.flBigrams = flBigrams;
    }
    
    public IndexSearcher getPlainBigrams(){
    	return this.plainBigrams;
    }
    public IndexSearcher getLCBigrams(){
        return this.lcBigrams;
    }
    public IndexSearcher getDFBigrams(){
        return this.dfBigrams;
    }
    public IndexSearcher getFLBigrams(){
        return this.flBigrams;
    }
    public IndexSearcher getMultiSearcher(){
    	return this.multiSearcher;
    }
}
