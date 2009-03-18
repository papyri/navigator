package info.papyri.tests.metadata;

import info.papyri.metadata.CoreMetadataFields;
import junit.framework.TestCase;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spell.*;

import info.papyri.data.APISIndices;
import info.papyri.data.LuceneIndex;

import java.io.File;
import java.io.IOException;
public class LuceneIndexTest extends TestCase {
    public void testTermVectors(){
    File dir = new File("C:\\PROGRA~1\\APACHE~1.0\\temp\\apis\\IndexerStartupThread0hgv0");
    try{
        IndexReader reader = IndexReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        Term indexed = new Term(CoreMetadataFields.INDEXED_SERIES,"");
        TermEnum pubs = reader.terms(indexed);
        int skipped = 0;
        boolean bgu = false;
        boolean back = false;
        System.out.println("Starting loop");
        do {
            Term next = pubs.term();
            back = (bgu && next.field().equals(CoreMetadataFields.INDEXED_SERIES));
            skipped++;
            if (bgu && !back){
                continue;
            }

            String series = next.text();
            if (series.equals("BGU")) bgu = true;
            //if (bgu) System.out.println(next);
            
        }while(pubs.next() && pubs.term().field().equals(indexed.field()));
        System.out.println("Ending loop");
        assertTrue(bgu);
    }
    catch(IOException t){
        t.printStackTrace();
        fail(t.toString());
    }
    
        
    }

}
