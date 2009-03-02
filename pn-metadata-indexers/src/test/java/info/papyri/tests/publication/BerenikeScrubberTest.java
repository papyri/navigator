package info.papyri.tests.publication;

import info.papyri.metadata.hgv.EpiDocHandler;

import java.util.Iterator;
import java.util.Set;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import info.papyri.tests.PublicationTests;

import info.papyri.data.Indexer;
import info.papyri.data.XREFIndices;
import info.papyri.data.publication.StructuredPublication;
import info.papyri.digester.IndexerFactory;
import info.papyri.digester.offline.APISTuple;
import junit.framework.TestCase;

public class BerenikeScrubberTest extends TestCase {

    public void setUp(){
        org.apache.derby.jdbc.EmbeddedDriver.class.getClass();
    }
    public void testBerenikeDDb() throws Exception {
        Set<String> struct = StructuredPublication.getStructuredPub("O.Ber. II 254");
        //StructuredPublication.class.getClass();
         APISTuple tuple = new APISTuple(PublicationTests.HGV);
         tuple.setControlName("berenike.apis.test");
         String inv = "Inv. BE01-48-046+058";
         tuple.setInventory(inv);
         String pub = "O.Ber. II 254";
         tuple.setPublication(pub);
         //org.apache.derby.jdbc.

         tuple.addXref("oai:papyri.info:identifiers:ddbdp:0250:2:255");
         tuple.scrubPublications();
         Iterator<String> pubs = tuple.getPublications();
         int pass = 0;
         while(pubs.hasNext()){
             String next = pubs.next();
             if("O.Berenike II 254".equals(next)){
                 pass += 1;
             }
             if("O.Berenike II 255".equals(next)){
                 pass += 2;
             }
         }
         assertEquals("missing expected pub: O.Berenike II 254",1,pass&1);
//         assertEquals("missing expected pub: O.Berenike II 255",2,pass&1);
     }
    }
