package info.papyri.digester.offline;
import java.io.PrintStream;

import org.apache.lucene.search.*;
import org.apache.commons.digester.*;
import org.xml.sax.Attributes;

public class APISTupleFactory implements ObjectCreationFactory{
    public static PrintStream DUPLICATES = System.out;
    public Object createObject(Attributes arg0) throws Exception {
        
        return new APISTuple(s,DUPLICATES);
    }
    public Digester getDigester() {
        // TODO Auto-generated method stub
        return null;
    }
    public void setDigester(Digester arg0) {
        // TODO Auto-generated method stub
        
    }
    private final IndexSearcher s;
    public APISTupleFactory(IndexSearcher searcher){
        s = searcher;
    }
}
