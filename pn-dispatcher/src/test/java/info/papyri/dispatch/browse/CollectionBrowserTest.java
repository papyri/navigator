/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.browse;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import junit.framework.TestCase;
import org.apache.solr.common.SolrDocument;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 *
 * @author thill
 */
public class CollectionBrowserTest extends TestCase {
    
    private CollectionBrowser testInstance;
    
    public CollectionBrowserTest(String testName) {
        super(testName);
        testInstance = new CollectionBrowser();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetAllIds(){
       
       // conditions that have to be met:
        
       //  (i) null value on volume field should not cause problems
        
        ArrayList<String> test1 = new ArrayList<String>(Arrays.asList("apis michigan 0 1234"));
        SolrDocument mockDoc1 = new SolrDocument();
        mockDoc1.setField(SolrField.collection.name(), "apis");
        mockDoc1.setField("apis_series", "michigan");
        mockDoc1.setField("apis_volume", null);
        mockDoc1.setField("apis_full_identifier", "1234");
        assertEquals(test1, testInstance.getAllIds(mockDoc1));
        
        // (ii) null value on itemIds should raise null pointer exception and warning to be included in ids
        
        SolrDocument mockDoc2 = new SolrDocument();
        mockDoc2.setField(SolrField.collection.name(), "apis");
        mockDoc2.setField("apis_series", "michigan");
        mockDoc2.setField("apis_volume", "0");
        mockDoc2.setField("apis_full_identifier", null);
        
        assertTrue(contentsMatchRegex(testInstance.getAllIds(mockDoc2), "Individual item id missing error:"));
        
        // (iii) all values null should cause general no id warning to be included in ids
        
        SolrDocument mockDoc3 = new SolrDocument();
        mockDoc3.setField(SolrField.collection.name(), "apis");
        
        assertTrue(contentsMatchRegex(testInstance.getAllIds(mockDoc3), "All item ids missing error"));
        
        // (iv) mismatched lengths of identifiers should raise a mismatched id exception and indicate warning
        
        String series1 = "berkeley, michigan";
        String volumes1 = "10, 20";
        String items1 = "5";
        
        SolrDocument mockDoc4 = new SolrDocument();
        mockDoc4.setField(SolrField.collection.name(), "apis");
        mockDoc4.setField("apis_series", series1);
        mockDoc4.setField("apis_volume", volumes1);
        mockDoc4.setField("apis_full_identifier", items1);
        
        assertTrue(contentsMatchRegex(testInstance.getAllIds(mockDoc4), "Individual id length error:"));
        
        // (v) basic functionality
        
        String dseries = "dtest1, dtest2, dtest3";
        String dvols = "1, 2, 3";
        String dids = "100, 200, 300";
        ArrayList<String> success1 = new ArrayList<String>(Arrays.asList("ddbdp dtest1 1 100", "ddbdp dtest2 2 200", "ddbdp dtest3 3 300"));
        
        SolrDocument mockDoc5 = new SolrDocument();
        mockDoc5.setField(SolrField.collection.name(), "ddbdp");
        mockDoc5.setField("ddbdp_series", dseries);
        mockDoc5.setField("ddbdp_volume", dvols);
        mockDoc5.setField("ddbdp_full_identifier", dids);
        
        assertEquals(success1, testInstance.getAllIds(mockDoc5));
        
        String hseries = "htest1, htest2, htest3, htest4";
        String hvols = "1, 2, 3, 4";
        String hids = "100, 200, 300, 400";
        String inv = "test inventory number";
        String pub = "test publication number";
        ArrayList<String> success2 = new ArrayList<String>(Arrays.asList("hgv htest1 1 100", "hgv htest2 2 200", "hgv htest3 3 300", "hgv htest4 4 400", "test inventory number", "test publication number"));
        
        SolrDocument mockDoc6 = new SolrDocument();
        mockDoc6.setField(SolrField.collection.name(), "hgv");
        mockDoc6.setField("hgv_series", hseries);
        mockDoc6.setField("hgv_volume", hvols);
        mockDoc6.setField("hgv_full_identifier", hids);
        mockDoc6.setField(SolrField.apis_inventory.name(), inv);
        mockDoc6.setField(SolrField.apis_publication_id.name(), pub);
        assertEquals(success2, testInstance.getAllIds(mockDoc6));
        
        
    }
    
    public void testGetPreferredId(){
        
        // requirements:
        
        // (i) returned id must match information submitted in path parts w/regard to collection, series, and volume
        
        LinkedHashMap<SolrField, String> test1 = new LinkedHashMap<SolrField, String>();
        test1.put(SolrField.collection, "hgv");
        test1.put(SolrField.series, "test-series");
        ArrayList<String> mockIds1 = new ArrayList<String>(Arrays.asList("ddbdp test-series 0 100", "hgv test-series 0 100", "apis test-series 0 100", "hgv test2-series 0 200"));
        SolrDocument mockDoc1 = new SolrDocument();
        assertEquals("hgv test-series 0 100", testInstance.getPreferredId(test1, mockIds1, mockDoc1));
        
        LinkedHashMap<SolrField, String> test2 = new LinkedHashMap<SolrField, String>();
        test2.put(SolrField.collection, "ddbdp");
        test2.put(SolrField.series, "test-series");
        test2.put(SolrField.volume, "1");
        SolrDocument mockDoc2 = new SolrDocument();
        ArrayList<String> mockIds2 = new ArrayList<String>(Arrays.asList("ddbdp test-series 1 100", "ddbdp test-series 2 200", "hgv test-series 1 100", "apis test-series 1 100"));
        assertEquals("ddbdp test-series 1 100", testInstance.getPreferredId(test2, mockIds2, mockDoc2));
        
        // (ii) unless apis, in which case should be (a) publication number (b) inventory number (c) standard identifier
        
        LinkedHashMap<SolrField, String> test3 = new LinkedHashMap<SolrField, String>();
        test3.put(SolrField.collection, "apis");
        test3.put(SolrField.series, "britmus");
        ArrayList<String> mockIds3 = new ArrayList<String>(Arrays.asList("apis britmus 0 100", "T. Test. Inv. 1", "T. Test. Pub. 1", "apis berkeley 0 100"));
        SolrDocument mockDoc3 = new SolrDocument();
        assertEquals("apis britmus 0 100", testInstance.getPreferredId(test3, mockIds3, mockDoc3));
        
        SolrDocument mockDoc4 = new SolrDocument();
        mockDoc4.setField(SolrField.apis_inventory.name(), "T. Test. Inv. 1");
        assertEquals("T. Test. Inv. 1", testInstance.getPreferredId(test3, mockIds3, mockDoc4));
        
        SolrDocument mockDoc5 = new SolrDocument();
        mockDoc5.setField(SolrField.apis_publication_id.name(), "T. Test. Pub. 1");
        mockDoc5.setField(SolrField.apis_inventory.name(), "T. Test. Inv. 1");
        assertEquals("T. Test. Pub. 1", testInstance.getPreferredId(test3, mockIds3, mockDoc5));
        
        // (iii) if no match is found, either because there is no suitable identifier or because no identifiers were
        // submitted, a warning should be sent
        
        ArrayList<String> mockIds4 = new ArrayList<String>(Arrays.asList("silly1", "silly2", "silly3", "silly4"));
        SolrDocument mockDoc6 = new SolrDocument();
        assertTrue(testInstance.getPreferredId(test1, mockIds4, mockDoc6).contains("ERROR"));
        
        ArrayList<String> mockIds5 = new ArrayList<String>();
        assertTrue(testInstance.getPreferredId(test1, mockIds5, mockDoc6).contains("ERROR"));
        
        // (iv) if any of the required pathparts are missing, display warning
        
        LinkedHashMap<SolrField, String> test5 = new LinkedHashMap<SolrField, String>();
        test5.put(SolrField.collection, "ddbdp");
        assertTrue(testInstance.getPreferredId(test5, mockIds4, mockDoc6).contains("SERIOUS ERROR"));
        
        
    }
    
    public void testFilterIds(){
        
        // requirements:
        
        // (i) eliminate duplicates
        
        ArrayList<String> redundancyTest = new ArrayList<String>(Arrays.asList("T. Test 1", "T. Test 2", "T. Test 3", "T. Test 1"));
        ArrayList<String> success1 = new ArrayList<String>(Arrays.asList("T. Test 1", "T. Test 2", "T. Test 3"));
        assertEquals(success1, testInstance.filterIds(redundancyTest));
        
        // (ii) weed out empty and whitespace-only strings
        
        ArrayList<String> whitespaceTest = new ArrayList<String>(Arrays.asList("T. Test 1", "", "T. Test 2", " ", "T. Test 3", "     "));
        ArrayList<String> success2 = new ArrayList<String>(Arrays.asList("T. Test 1", "T. Test 2", "T. Test 3"));
        assertEquals(success2, testInstance.filterIds(whitespaceTest));
        
        // (iii) null values shouldn't break it
        
        ArrayList<String> nullTest = new ArrayList<String>(Arrays.asList("T. Test 1", null, "T. Test 2", null, "T. Test 3"));
        assertEquals(success2, testInstance.filterIds(nullTest));
        
        
    }
    
    public void testParseSolrResponseIntoDocuments(){
        
        // requirements
        
        // (i) null on id should throw exception, no record created
        
        LinkedHashMap<SolrField, String> pathParts = new LinkedHashMap<SolrField, String>();
        pathParts.put(SolrField.collection, "ddbdp");
        pathParts.put(SolrField.series, "test-series-1");
        pathParts.put(SolrField.volume, "1");
        
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        
        SolrDocument blankDocument = new SolrDocument();
        
        solrDocumentList.add(blankDocument);
        
        QueryResponse mockResponse = createStrictMock(QueryResponse.class);
        
        expect(mockResponse.getResults()).andReturn(solrDocumentList).anyTimes();
        replay(mockResponse);
        
        assertEquals(0, testInstance.parseSolrResponseIntoDocuments(pathParts, mockResponse).size());
        verify(mockResponse);
        
        // (ii) null in other fields is tolerated
        
        SolrDocument nearlyBlankDocument = new SolrDocument();
        nearlyBlankDocument.setField(SolrField.id.name(), "http://papyri.info/ddbdp/test-series-1");
        solrDocumentList.add(nearlyBlankDocument);
        
        QueryResponse mockResponse2 = createStrictMock(QueryResponse.class);
        
        expect(mockResponse2.getResults()).andReturn(solrDocumentList).anyTimes();
        replay(mockResponse2);
        
        assertEquals(1, testInstance.parseSolrResponseIntoDocuments(pathParts, mockResponse2).size());
        verify(mockResponse);
        
        
    }
    
    private Boolean contentsMatchRegex(ArrayList<String> values, String soughtString){
        
        Iterator<String> vit = values.iterator();
        
        while(vit.hasNext()){
            
            String testString = vit.next();
            if(testString.contains(soughtString)) return true;
           
            
        }
        
        
     return false;
        
    }

}
