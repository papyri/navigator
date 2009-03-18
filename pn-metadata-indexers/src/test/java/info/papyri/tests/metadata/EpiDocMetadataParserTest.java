package info.papyri.tests.metadata;

import junit.framework.TestCase;
import java.io.IOException;

import org.apache.lucene.search.IndexSearcher;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import info.papyri.tests.PublicationTests;

import info.papyri.data.Indexer;

import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;

import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.metadata.NamespacePrefixes;
import info.papyri.metadata.hgv.*;
public class EpiDocMetadataParserTest extends TestCase {
    static IndexSearcher s = PublicationTests.APIS;
    public void testWebImages() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.1.23.xml");
        assertTrue(record  != null);
        String knownImg = "http://www.papyrology.uw.edu.pl/papyrology/papyri/Berlin06831.html";
        String expected = "Abbildung im Internet";
        String actual = record.getWebImages().get(new URL(knownImg));
        assertEquals(expected,actual);
    }
    public void testUri() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.3.790.xml","9353a");
        assertTrue("record missing: 9353a", record  != null);
        String expected = NamespacePrefixes.HGV + "BGU:3:790:Z.%201%20-%208";
        String actual = record.getControlName();
        if(!expected.equals(actual)){
            System.err.println("expected: " + expected);
            System.err.println("actual: " + actual);
        }
        assertEquals(expected, actual);
    }
    public void testTranslation() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.3.790.xml","9353a");
        assertTrue("record missing: 9353a", record  != null);
        Vector<String> transDE = record.getTranslation(CoreMetadataRecord.ModernLanguage.GERMAN);
        String expected = "Im 7. Jahr. Eingezahlt haben";
        String actual = transDE.elementAt(0);
        assertTrue(actual.startsWith(expected));
        Vector<String> translation = record.getTranslation();
        actual = translation.elementAt(0).replaceAll("\\s+"," ");
        expected = "1 Year 7. Paid to S[otas] and his associates";
        if(!actual.startsWith(expected)){
            System.err.println("expected: " + expected);
            System.err.println("actual: " + actual);
        }
        assertTrue(actual.startsWith(expected));
    }
    public void testSubjects() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.1.23.xml");
        assertTrue(record  != null);
        String expected = "Eingabe";
        assertTrue(record.getSubjectSearchField().contains(expected));
    }
    public void testTitle() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.1.23.xml");
        assertTrue(record  != null);
        String expected = "keiner";
        assertEquals(expected,record.getTitle());
    }
    public void testPlaceNames() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.1.23.xml");
        assertTrue(record  != null);
        assertTrue(record.getProvenance().contains("soknopaiu nesos"));
        assertTrue(record.getProvenance().contains("arsinoites"));
        assertTrue(record.getProvenance().contains("egypt"));
    }
    public void testHGVId() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.1.23.xml");
        assertTrue(record  != null);
        String expected = "oai:papyri.info:identifiers:hgv:BGU:1:23";
        String actual = record.getControlName();
        assertEquals("actual: " + actual + " expected: " + expected,expected,actual);
    }
    public void testTM() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.1.23.xml");
        assertTrue(record  != null);
        String expected = "oai:papyri.info:identifiers:trismegistos:8992";
        assertTrue(record.getXrefs().contains(expected));
    }
    public void testDDb() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.1.23.xml");
        assertTrue(record  != null);
        String expected = "oai:papyri.info:identifiers:ddbdp:0001:1:23";
        assertTrue(record.getXrefs().contains(expected));
    }
    public void testPublications() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/p.lond.7.2057.xml");
        assertTrue(record  != null);
        String expected = "P.Col. IV 114 D";
        try{
            assertTrue(record.getPublication().contains(expected));
        }
        catch(junit.framework.AssertionFailedError e){
            for(String s:record.getPublication()){
                System.err.println(s + " did not match " + expected);
            }
            throw e;
        }
    }
    public void testLanguage() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.1.23.xml");
        assertTrue(record  != null);
        java.util.Collection<String> langs = record.getLanguages();
        assertTrue(langs != null);
        assertTrue(langs.contains(info.papyri.metadata.RFC3066.GREEK));
    }
    public void testMaterial() throws IOException, SAXException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.1.23.xml");
        assertTrue(record  != null);
        java.util.Collection<String> material = record.getMaterial();
        assertTrue(material != null);
        String expected = "Papyri";
        assertTrue(material.contains(expected));
    }
    public void testDateSingle() throws IOException, SAXException, info.papyri.metadata.OutOfRangeException {
        CoreMetadataRecord record = getTestRecord("/xml/bgu.1.23.xml");
        assertTrue(record  != null);
        String expected = info.papyri.util.NumberConverter.encodeDate(207,1,1);
        assertTrue(record.getDateIndexes().containsKey(expected));
        expected = "ca. 207";
        assertEquals(expected,record.getDate1());
    }
    public void testDateRange() throws IOException, SAXException, info.papyri.metadata.OutOfRangeException {
        CoreMetadataRecord record = getTestRecord("/xml/p.lond.7.2057.xml");
        assertTrue(record  != null);
        String expected = info.papyri.util.NumberConverter.encodeDate(-275,1,1);
        String actual = record.getDateIndexes().get(expected);
        assertTrue(actual != null);
        expected = info.papyri.util.NumberConverter.encodeDate(-226,1,1);
        assertEquals(expected,actual);
        expected = "Mitte III v.Chr.";
        actual = record.getDate1();
        assertEquals(expected,actual);
    }
    public void testDDbOnlyMetadata() throws IOException, SAXException, info.papyri.metadata.OutOfRangeException {
        CoreMetadataRecord record = getTestRecord("/xml/p.alex.giss.62.xml","0055;;62");
        assertTrue(record  != null);
        String date1 = record.getDate1();
        String date2 = record.getDate2();
        String id = record.getControlName();
        String eId = NamespacePrefixes.DDBDP + "0055::62";
        assertEquals(eId,id);
        assertEquals("216 CE",date1);
        assertEquals("219 CE",date2);
        String prov = record.getProvenance().firstElement();
        assertEquals("Oxyrhynchite",prov);
    }
    public void testHGVOnlyMetadata() throws IOException, SAXException, info.papyri.metadata.OutOfRangeException {
        CoreMetadataRecord record = getTestRecord("/xml/chla.44.1299.xml","70086");
        assertTrue(record  != null);
        String eId = NamespacePrefixes.HGV + "ChLA:44:1299";
        assertEquals(eId, record.getControlName());
        String ddb = NamespacePrefixes.DDBDP + "0279:44:1299";
        assertTrue(record.getXrefs().contains(ddb));
    }
    static CoreMetadataRecord getTestRecord(String path) throws IOException, SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        EpiDocHandler handler = new EpiDocHandler();
        reader.setContentHandler(handler);
        reader.setEntityResolver(Indexer.getEpiDocResolver(reader.getEntityResolver()));
        reader.parse(new InputSource(EpiDocMetadataParserTest.class.getResourceAsStream(path)));
        return handler.getRecords().next();
    }
    static CoreMetadataRecord getTestRecord(String path, String docScope) throws IOException, SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        EpiDocHandler handler = new EpiDocHandler();
        reader.setContentHandler(handler);
        reader.setEntityResolver(Indexer.getEpiDocResolver(reader.getEntityResolver()));
        reader.parse(new InputSource(EpiDocMetadataParserTest.class.getResourceAsStream(path)));
        return handler.getRecord(docScope);
    }
}
