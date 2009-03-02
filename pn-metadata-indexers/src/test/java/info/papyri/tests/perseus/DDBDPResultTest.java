package info.papyri.tests.perseus;

import info.papyri.antlr.DDBDPResult;
import junit.framework.TestCase;
import info.papyri.*;
import info.papyri.digester.*;
import info.papyri.data.*;

import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
import org.xml.sax.helpers.AttributesImpl;



public class DDBDPResultTest extends TestCase {
    public void testDDBDPQueryResolution(){
        //DDBDPResult result = new DDBDPResult("ABOUTHIOS");
        //result.setQuery("");
        Field ddbdpall = new Field("ddbdp_all","P.Col.:7:188", Field.Store.YES, Field.Index.TOKENIZED);
        Document doc = new Document();
        doc.add(ddbdpall);
        IndexerFactory ixF = new IndexerFactory();
        try{
            Indexer ix = (Indexer)ixF.createObject(new AttributesImpl());
        
            IndexWriter writer = ix.writer;
            writer.addDocument(doc);
            writer.optimize();
            writer.close();
            IndexReader ir = IndexReader.open(ix.indexDir);
            Searcher ixS = new IndexSearcher(ir);
            Term t = new Term("ddbdp_all","P.Col.:7:188");
            int freq = ixS.docFreq(t);
            if (freq != 1){
                fail("Unexpected frequency of " + t.field() + " = " + t.text() + " ... " + freq);
            }
            Query q = new WildcardQuery(new Term("ddbdp_all","P.Col.:*:188"));
            Hits hits = ixS.search(q);
            if (hits.length() != 1){
                fail("Unexpected hits length for " + q.toString() + " : " + hits.length());
            }
        }
        catch (Throwable t){
            fail(t.toString());
        }
        
        
    }
    
}
