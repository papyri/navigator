package info.papyri.tests.publication;

import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.metadata.OutOfRangeException;
import info.papyri.metadata.apis.OAIRecord;
import info.papyri.metadata.hgv.EpiDocHandler;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import info.papyri.tests.PublicationTests;

import info.papyri.digester.IndexerFactory;
import info.papyri.digester.offline.APISTuple;
import info.papyri.data.Indexer;
import info.papyri.data.XREFIndices;
import info.papyri.data.publication.BerkeleyScrubber;
import info.papyri.data.publication.ColumbiaScrubber;
import junit.framework.TestCase;

public class BerkeleyScrubberTest extends TestCase {


    public void testVolume2Sided(){
        CoreMetadataRecord test = new OAIRecord();
        String data = "P.Tebt., II.554 (desc. only)";
        String inv = "P.Tebt.0554 Verso";
        test.setInventoryNumber(inv);
        test.addPublication(data);
        
        BerkeleyScrubber cs = new BerkeleyScrubber(data,test);
        Collection<String> result = cs.getPublications();
        assertTrue("Expected 1+ results, got " + result.size(),result.size() >= 1);
        String expected = "P.Tebt. II 554 V";
        assertTrue("Did not contain expected " + expected,result.contains(expected));
    }
    public void testPTebtVolumeCorrection(){
        CoreMetadataRecord test = new OAIRecord();
        String data = "P.Tebt., III.820; CPJ v. 1, no. 22";
        String inv = "P.Tebt.00";
        test.setInventoryNumber(inv);
        test.addPublication(data);
        test.addXref("oai:papyri.info:identifiers:ddbdp:0206:3:820");
        BerkeleyScrubber cs = new BerkeleyScrubber(data,test);
        Collection<String> result = cs.getPublications();
        assertTrue("Expected 1+ results, got " + result.size(),result.size() >= 1);
        String expected = "P.Tebt. III.1 820";
        assertTrue("Did not contain expected " + expected,result.contains(expected));
    }
    
    public void testSBVolumeCorrection() throws java.io.IOException, java.sql.SQLException, OutOfRangeException  {

        APISTuple test = new APISTuple(PublicationTests.HGV);
        test.setControlName("berkeley.apis.922");
        test.addXref(XREFIndices.DDB_ID +":0239:6:9899");
        test.setPublication("C.Ord.Ptol., no. 53ter.");
        test.setPublicationsAbout("See P. L. Bat. 29, p. 39.");
        test.setInventory("P.Tebt.1139 Verso");
        
        String expected = "oai:papyri.info:identifiers:hgv:SB:8:9899:b";
        test.scrubPublications();
        test.matchStructuredPubs();
        test.matchXref();
        System.out.println("hgvs:");
        for(String hgv:test.getHGVNames()){
            System.out.println(hgv);
        }
        assertTrue(test.getHGVNames().contains(expected));
        info.papyri.util.IntQueue ids = test.getHGVIds();
        boolean found = false;
        while(ids.size() > 0){
            int id = ids.next();
            org.apache.lucene.document.Document doc = PublicationTests.HGV.doc(id);
            if(doc.get("controlName").equals(expected)) found = true;
            System.out.println(doc.get("controlName"));
        }
        assertTrue(found);
        }
}
