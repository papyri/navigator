package info.papyri.tests.publication;

import junit.framework.TestCase;
import info.papyri.digester.offline.*;
import info.papyri.data.publication.StructuredPublication;

import java.util.*;

import org.apache.lucene.search.IndexSearcher;

import info.papyri.tests.PublicationTests;
public class PublicationScrubberTests extends TestCase {
    
    private static IndexSearcher getHGVSearcher()  {
        return PublicationTests.HGV;
    }
    @Override
    public void setUp(){
        org.apache.derby.jdbc.EmbeddedDriver.class.getClass();
    }
    public void testWisconsin(){

        String pubInfo = "Sijpesteijn PJ , PWisc II, 62 recto, 1977, Plate XVII -- BL VIII, 512 (lines 1 and 2)";
        APISTuple tuple = new APISTuple(null);
        tuple.addPublication(pubInfo);
        tuple.setControlName("wisconsin.apis.test");
        tuple.scrubPublications();
        Iterator<String> pubs = tuple.getPublications();
        boolean pass = false;
        String expected = "P.Wisc. II 62 R";
        while(pubs.hasNext()){
            if(expected.equals(pubs.next())) pass = true;
        }
        assertTrue(pass);
    }
    
    public void testYale(){
        String pubInfo = "P.Oxy. 115";
        APISTuple tuple = new APISTuple(null);
        tuple.addPublication(pubInfo);
        tuple.setControlName("yale.apis.test");
        tuple.scrubPublications();
        Iterator<String> pubs = tuple.getPublications();
        
        boolean pass = false;
        String expected = "P.Oxy. I 115";
        while(pubs.hasNext()){
            String pub = pubs.next();
            if(expected.equals(pub)) pass = true;
            System.out.println("pub: \"" + pub + "\"");
        }
        assertTrue(pass);
    }
    
    public void testYaleSB(){
        String pubInfo = "P.Yale inv. 1545aSB XII 10788";
        APISTuple tuple = new APISTuple(null);
        tuple.addPublication(pubInfo);
        tuple.setControlName("yale.apis.test");
        tuple.scrubPublications();
        Iterator<String> pubs = tuple.getPublications();
        
        boolean pass = false;
        String expected = "SB XII 10788 a";
        while(pubs.hasNext()){
            String pub = pubs.next();
            if(expected.equals(pub)) pass = true;
            System.out.println("pub: \"" + pub + "\"");
        }
        assertTrue(pass);
    }
    
    public void testBerkeley(){
        String pub = "P.Tebt., II.638 (desc. only)";
        String inv = "P.Tebt.0638 Recto";
        APISTuple tuple = new APISTuple(null);
        tuple.setControlName("berkeley.apis.test");
        tuple.setInventory(inv);
        tuple.addPublication(pub);
        tuple.scrubPublications();
        Iterator<String> pubs = tuple.getPublications();
        boolean pass = false;
        String expected = "P.Tebt. II 638 R";
        while(pubs.hasNext()){
            String next = pubs.next();
            if(expected.equals(next)) pass = true;
        }
        assertTrue(pass);
    }
    
    public void testBerkeleySubdoc(){
        String pub = "P.Tebt., III.701a";
        APISTuple tuple = new APISTuple(null);
        tuple.setControlName("berkeley.apis.test");
        String inv = "P.Tebt.0701a";
        tuple.setInventory(inv);
        tuple.addPublication(pub);
        tuple.scrubPublications();
        Iterator<String> pubs = tuple.getPublications();
        boolean pass = false;
        String expected = "P.Tebt. III.1 701 A";
        while(pubs.hasNext()){
            String next = pubs.next();
            if(expected.equals(next)) pass = true;
        }
        assertTrue(pass);
    }
    
    public void testColumbiaParens(){
        String pub = "P.Col. IV 114 (l)";
        APISTuple tuple = new APISTuple(null);
        tuple.setControlName("columbia.apis.test");
        tuple.addPublication(pub);
        tuple.scrubPublications();
        Iterator<String> pubs = tuple.getPublications();
        boolean pass = false;
        String expected = "P.Col. IV 114 l";
        while(pubs.hasNext()){
            String next = pubs.next();
            if(expected.equals(next)) pass = true;
        }
        assertTrue(pass);
    }

    public void testColumbiaPFay(){
        APISTuple tuple = new APISTuple(null);
        tuple.setControlName("columbia.apis.test");
        String inv = "Fay. 135 recto";
        tuple.setInventory(inv);
        String pub = "P.Fay. 135";
        tuple.addPublication(pub);
        tuple.scrubPublications();
        Iterator<String> pubs = tuple.getPublications();
        boolean pass = false;
        String expected = "P.Fay. 135 R";
        while(pubs.hasNext()){
            String next = pubs.next();
            if(expected.equals(next)) pass = true;
        }
        assertTrue(pass);
    }

    public void testColumbiaPOxyDescriptum(){
        APISTuple tuple = new APISTuple(null);
        tuple.setControlName("columbia.apis.test");
        String inv = "Oxy. 539 recto";
        tuple.setInventory(inv);
        String pub = "P.Oxy. II 317 Descriptum | P.Oxy. II 317";
        tuple.addPublication(pub);
        System.out.println("added pub");
        tuple.scrubPublications();
        System.out.println("scrubbed pub");
        Iterator<String> pubs = tuple.getPublications();
        boolean pass = false;
        String expected = "P.Oxy. II 317 R";
        while(pubs.hasNext()){
            String next = pubs.next();
            if(expected.equals(next)){
                pass = true;
            }
            else{
                System.err.println("test pubs.next: " + next);
            }
        }
        assertTrue(pass);
    }

    public void testMichiganSB(){
        String pub = "Hanson AE, BASP 19, 47-60, 1982, Photo -- SB XVI 12332";
        APISTuple tuple = new APISTuple(null);
        tuple.setControlName("michigan.apis.test");
        tuple.addNotes("Source of description: Recto + Verso");
        tuple.addPublication(pub);
        tuple.scrubPublications();
        Iterator<String> pubs = tuple.getPublications();
        boolean pass = false;
        String expected = "SB XVI 12332";
        while(pubs.hasNext()){
            String next = pubs.next();
            if(expected.equals(next)) pass = true;
        }
        assertTrue(pass);
    }

    public void testMichiganSBsubdoc(){
        String pub = "Pearl OM, Aegyptus 33, 24, 1953 -- SB VI 9436s -- BL VIII, 349 (l. 7); ";
        APISTuple tuple = new APISTuple(getHGVSearcher());
        tuple.setControlName("michigan.apis.test");
        tuple.addNotes("Source of description: Recto");
        tuple.setPublication(pub);
        //tuple.addPublication(pub);
        tuple.scrubPublications();
        tuple.matchStructuredDDB();
        Iterator<String> pubs = tuple.getPublications();
        boolean pass = false;
        String expected = "SB VI 9436 S Recto";
        while(pubs.hasNext()){
            String next = pubs.next();
            if(expected.equals(next)) pass = true;
            else System.err.println("Not expected: " + next);
        }
        assertTrue(pass);
        Set<String> struct = StructuredPublication.getStructuredPub(expected);
        expected = "series=0239;volume=6;document=9436;side=RECTO;subdoc=S";
        assertTrue(struct.contains(expected));
        struct = StructuredPublication.getStructuredPub("SB VI 9436 s");
        expected = "series=0239;volume=6;document=9436;side=*;subdoc=S";
        assertTrue(struct.contains(expected));
        try{
            tuple.matchStructuredPubs();
            Collection<String> hgvs = tuple.getHGVNames();
        }
        catch(Throwable t){
            assertTrue(t.toString(),false);
        }
        
    }
    
    public void testChicagoHaw(){
        APISTuple tuple = new APISTuple(null);
        tuple.setControlName("chicago.apis.test");
        tuple.setInventory("P. O.I. 25260 (P. Chic. Haw. 7C)");
        tuple.scrubPublications();
        Iterator<String> pubs = tuple.getPublications();
        boolean pass = false;
        String expected = "P.Chic.Haw. 7 C";
        while(pubs.hasNext()){
            String next = pubs.next();
            if(expected.equals(next)){
                pass = true;
            }
            else System.err.println(next + " != " + expected);
        }
        assertTrue(pass);
    }
}
