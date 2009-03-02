package info.papyri.digester.offline;
import org.apache.lucene.search.*;
import org.apache.commons.digester.*;
import org.xml.sax.Attributes;

public class HGVTupleFactory implements ObjectCreationFactory{
    public Object createObject(Attributes arg0) throws Exception {
        
        return new HGVTuple(s);
    }
    public Digester getDigester() {
        // TODO Auto-generated method stub
        return null;
    }
    public void setDigester(Digester arg0) {
        // TODO Auto-generated method stub
        
    }
    private final IndexSearcher s;
    public HGVTupleFactory(IndexSearcher searcher){
        s = searcher;
    }
}
