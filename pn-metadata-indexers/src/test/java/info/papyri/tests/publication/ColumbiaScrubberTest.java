package info.papyri.tests.publication;

import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.metadata.NamespacePrefixes;
import info.papyri.metadata.OutOfRangeException;
import info.papyri.metadata.apis.OAIRecord;
import info.papyri.metadata.hgv.EpiDocHandler;

import java.io.File;
import java.util.Collection;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import info.papyri.tests.PublicationTests;

import info.papyri.data.Indexer;
import info.papyri.data.publication.ColumbiaScrubber;
import info.papyri.data.publication.PublicationScrubber;
import info.papyri.digester.IndexerFactory;
import info.papyri.digester.offline.APISTuple;
import junit.framework.TestCase;

public class ColumbiaScrubberTest extends TestCase {

    public void testParentheticalSubdocument(){
        CoreMetadataRecord test = new OAIRecord();
        test.setControlName("oai:papyri.info:identifiers:apis:columbia:test");
        String data = "P.Col. IV 114 (l)";
        test.addPublication(data);
        PublicationScrubber cs = PublicationScrubber.get(data,test);
        Collection<String> result = cs.getPublications();
        assertTrue("Expected 1 results, got " + result.size(),result.size() == 1);
        String expected = "P.Col. IV 114 l";
        assertTrue("Did not contain expected " + expected,result.contains(expected));
    }
    
    public void testVolume5SidedSubdocumentRange() throws junit.framework.AssertionFailedError{
        String data = "P.Col. V 1 verso 1b + 1a";
        CoreMetadataRecord test = new OAIRecord();
        test.setControlName("oai:papyri.info:identifiers:apis:columbia:test");
        test.addPublication(data);
        PublicationScrubber cs = PublicationScrubber.get(data,test);
        Collection<String> result = cs.getPublications();
        try{
            assertTrue("Expected 2+ results, got " + result.size(),result.size() >= 2);
            String expected = "P.Col. V 1 Verso 1 ";
            assertTrue("Did not contain expected " + expected + "a",result.contains(expected + "a"));
            assertTrue("Did not contain expected " + expected + "b",result.contains(expected + "b"));
        }
        catch (junit.framework.AssertionFailedError ae){
            int i = 0;
            for (String r:result){
                System.err.println("\tresult " + i++ + ": " + r);
            }
            throw ae;
        }
        
    }
    public void testVolume2SidedSubdocumentRange() throws junit.framework.AssertionFailedError{
        String data = "P.Col. II 1 recto 1a-1b";
        CoreMetadataRecord test = new OAIRecord();
        test.setControlName("oai:papyri.info:identifiers:apis:columbia:test");
        test.addPublication(data);
        PublicationScrubber cs = PublicationScrubber.get(data,test);
        Collection<String> result = cs.getPublications();
        try{
            assertTrue("Expected 2+ results, got " + result.size(),result.size() >= 2);
            String expected = "P.Col. II 1 R 1 ";
            assertTrue("Did not contain expected " + expected + "a",result.contains(expected + "a"));
            assertTrue("Did not contain expected " + expected + "b",result.contains(expected + "b"));
        }
        catch (junit.framework.AssertionFailedError ae){
            int i = 0;
            for (String r:result){
                System.err.println("\tresult " + i++ + ": " + r);
            }
            throw ae;
        }
    }
    
    public void testColumbiaSB() throws java.io.IOException, java.sql.SQLException, OutOfRangeException {

        APISTuple test = new APISTuple(PublicationTests.HGV);
        String data = "SB XXII 15345";
        test.addPublication(data);
        test.addXref(NamespacePrefixes.DDBDP + "0239:22:15345");
        test.setControlName("oai:papyri.info:identifiers:apis:columbia:test");
        PublicationScrubber cs = PublicationScrubber.get(data,test);
        Collection<String> result = cs.getPublications();
        String expected = "SB XXII 15345";
        test.scrubPublications();
        if(!result.contains(expected)){
            System.err.println("Missing expected value: " + expected);
            for(String r:result){
                System.err.println("\t" + r);
            }
            fail();
        }

        test.matchStructuredPubs();
        expected = NamespacePrefixes.HGV + "SB:22:15345";
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
    public void testPOxyFromInv() throws java.io.IOException, java.sql.SQLException, OutOfRangeException {

        APISTuple test = new APISTuple(PublicationTests.HGV);
        String data = "Oxy. 486 recto";
        test.setInventory(data);
        test.setControlName("oai:papyri.info:identifiers:apis:columbia:test");
        PublicationScrubber cs = PublicationScrubber.get(data,test);
        Collection<String> result = cs.getPublications();
        String expected =  "P.Oxy. III 486 R";
        test.scrubPublications();
        assertTrue("No results found",result.size()>0);
        assertTrue("expected result not found: " + expected + " ( " + result.size() + " results)",result.contains(expected));
        test.matchStructuredPubs();
        expected = NamespacePrefixes.HGV + "P.Oxy.:3:486:R";
        assertTrue("no HGV matches!",test.getHGVNames().size()>0);
        for(String t:test.getHGVNames()) System.err.println(t);
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
    
    public void testVolume2SidedSubdocument(){
        String data = "P.Cair.Zen.  V 59804, the Columbia text + PSI VII 863 (g) | P.Col. II 1 recto 3";
        String[] expected = new String[]{"P.Cair.Zen. V 59804","PSI VII 863 g", "P.Col. II 1 R 3"};
        CoreMetadataRecord test = new OAIRecord();
        test.setControlName("oai:papyri.info:identifiers:apis:columbia:test");
        test.addPublication(data);
        PublicationScrubber cs = PublicationScrubber.get(data,test);
        Collection<String> result = cs.getPublications();
        try{
            assertTrue("Expected 1+ results, got " + result.size(),result.size() >= 1);
            for(String e:expected){
                assertTrue("Did not contain expected " + e, result.contains(e));
            }
        }
        catch (junit.framework.AssertionFailedError ae){
            int i = 0;
            for (String r:result){
                System.err.println("\tresult " + i++ + ": " + r);
            }
            throw ae;
        }
    }

}
