package info.papyri.data;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;


import info.papyri.lucene.ConstantBitsetFilter;

import java.util.regex.*;


public class LuceneIndex {
    public static IndexReader INDEX_HGV = null;
    public static IndexReader INDEX_COL = null;
    public static IndexReader INDEX_XREF = null;
    public static IndexSearcher SEARCH_HGV = null;
    public static IndexSearcher SEARCH_COL = null;
    public static IndexSearcher SEARCH_XREF = null;
    public static ConstantBitsetFilter LOOSE_APIS_FILTER = null;
    public static ConstantBitsetFilter LOOSE_HGV_FILTER = null;
    public static ConstantBitsetFilter XREF_MAPPED = null;
    static {
        BooleanQuery.setMaxClauseCount(24576);
    }
    
}
