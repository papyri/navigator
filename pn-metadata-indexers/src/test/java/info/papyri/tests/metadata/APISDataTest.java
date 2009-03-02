package info.papyri.tests.metadata;

import info.papyri.data.Indexer;
import info.papyri.data.XREFIndices;
import info.papyri.digester.IndexerFactory;
import info.papyri.metadata.CoreMetadataFields;
import junit.framework.TestCase;
import java.io.*;

import org.apache.lucene.store.*;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;

import info.papyri.tests.PublicationTests;

public class APISDataTest extends TestCase {
    
    public void testIndex(){
        try{
            String APISval =XREFIndices.APIS_ID+":columbia:p223";
            TermQuery q = new TermQuery(new Term(CoreMetadataFields.DOC_ID,APISval));

            Hits h = PublicationTests.APIS.search(q);
            int hits = h.length();
            assertTrue("No document hits for " + APISval,hits > 0);
            if (hits > 1){
                System.out.println("WARNING: " + hits + " docs associated with ["+ APISval + "]");
            }
            for (int i=0;i<hits;i++){
                Document d = h.doc(i);
                Field f = d.getField(CoreMetadataFields.TRANSLATION_EN);
                assertTrue("No translation field found in " + APISval,f != null);
                System.out.println(f.stringValue());
            }
            
            
        }
        catch (Exception e){
            fail(e.toString());
        }
    }
}
