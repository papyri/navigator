package info.papyri.tests;

import info.papyri.metadata.hgv.EpiDocHandler;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import info.papyri.tests.metadata.APISDataTest;
import info.papyri.tests.publication.BerenikeScrubberTest;
import info.papyri.tests.publication.BerkeleyScrubberTest;
import info.papyri.tests.publication.ColumbiaScrubberTest;
import info.papyri.tests.publication.PubTests;
import info.papyri.tests.publication.PublicationMappingTests;
import info.papyri.tests.publication.PublicationPatternTests;
import info.papyri.tests.publication.PublicationScrubberTests;
import info.papyri.util.DBUtils;

import info.papyri.data.Indexer;
import info.papyri.digester.IndexerFactory;
import junit.framework.Test;
import junit.framework.TestSuite;

public class PublicationTests {
    public static IndexSearcher APIS = null;
    public static IndexSearcher HGV = null;
    private static void oneTimeSetUp() {
        try{
            java.net.URL derbydata = DBUtils.class.getResource("ddbdp.xml");
            DBUtils.setupDerby(derbydata);
            RAMDirectory indexDir = new RAMDirectory();
            IndexerFactory.indexAPIS_OAI(APISDataTest.class.getResource("/xml/apis-oai-data.xml"), indexDir, true);
            APIS = new IndexSearcher(indexDir);
            indexDir = new RAMDirectory();
            IndexerFactory iFact = new IndexerFactory(indexDir,true);
            iFact.createObject(null);
            Indexer hgvIndex = iFact.createObject(null);
            XMLReader reader = XMLReaderFactory.createXMLReader();
            EpiDocHandler handler = new EpiDocHandler();
            reader.setContentHandler(handler);
            reader.setEntityResolver(Indexer.getEpiDocResolver(reader.getEntityResolver()));
            IndexerFactory.indexHGVEpidDoc(BerkeleyScrubberTest.class.getResource("/xml/p.col.7.188.xml"), hgvIndex, reader);
            IndexerFactory.indexHGVEpidDoc(BerkeleyScrubberTest.class.getResource("/xml/bgu.1.23.xml"), hgvIndex, reader);
            IndexerFactory.indexHGVEpidDoc(BerkeleyScrubberTest.class.getResource("/xml/bgu.3.790.xml"), hgvIndex, reader);
            IndexerFactory.indexHGVEpidDoc(BerkeleyScrubberTest.class.getResource("/xml/p.lond.7.2057.xml"), hgvIndex, reader);
            IndexerFactory.indexHGVEpidDoc(BerkeleyScrubberTest.class.getResource("/xml/sb.6.9436.xml"), hgvIndex, reader);
            IndexerFactory.indexHGVEpidDoc(BerkeleyScrubberTest.class.getResource("/xml/sb.8.9899.xml"), hgvIndex, reader);
            IndexerFactory.indexHGVEpidDoc(BerkeleyScrubberTest.class.getResource("/xml/sb.22.15345.xml"), hgvIndex, reader);
            IndexerFactory.indexHGVEpidDoc(BerkeleyScrubberTest.class.getResource("/xml/p.oxy.3.486.xml"), hgvIndex, reader);
            IndexerFactory.indexHGVEpidDoc(BerkeleyScrubberTest.class.getResource("/xml/p.alex.giss.62.xml"), hgvIndex, reader);
            hgvIndex.writer.flush();
            hgvIndex.writer.optimize();
            hgvIndex.writer.close();
            HGV = new IndexSearcher(indexDir);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    static {
        oneTimeSetUp();
    }    
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for tests");
        //$JUnit-BEGIN$
        suite.addTestSuite(BerenikeScrubberTest.class);
        suite.addTestSuite(BerkeleyScrubberTest.class);
        suite.addTestSuite(ColumbiaScrubberTest.class);
        suite.addTestSuite(PublicationMappingTests.class);
        suite.addTestSuite(PublicationPatternTests.class);
        suite.addTestSuite(PublicationScrubberTests.class);
        suite.addTestSuite(PubTests.class);
        //$JUnit-END$
        return suite;
    }

}
