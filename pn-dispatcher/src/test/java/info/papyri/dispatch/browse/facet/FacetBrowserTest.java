package info.papyri.dispatch.browse.facet;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.client.solrj.response.QueryResponse;
import java.util.Arrays;
import java.util.ArrayList;
import info.papyri.dispatch.browse.SolrField;
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
public class FacetBrowserTest extends TestCase {
    
    private FacetBrowser testInstance;
    
    public FacetBrowserTest(String testName) {
        super(testName);
        testInstance = new FacetBrowser();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetAllSortedIds(){
        
        // requirements:
        
        // (i) basic functionality
        
        SolrDocument mockDoc1 = new SolrDocument();
        mockDoc1.setField("id", "http://papyri.info/apis/duke.apis.31254916");
        mockDoc1.setField("apis_series", "duke");
        mockDoc1.setField("apis_volume", "0");
        mockDoc1.setField("apis_full_identifier", "31254916");
        
        ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("duke 0 31254916"));
        
        assertEquals(expectedResult, testInstance.getAllSortedIds(mockDoc1));
        
        SolrDocument mockDoc2 = new SolrDocument();
        mockDoc2.setField("id", "http://papyri.info/ddbdp/ddbdp-series-1;1;100");
        mockDoc2.setField("ddbdp_series", "ddbdp-series-1");
        mockDoc2.setField("ddbdp_volume", "1");
        mockDoc2.setField("ddbdp_full_identifier", "100");
        mockDoc2.setField("apis_series", "apis-series-1");
        mockDoc2.setField("apis_volume", "0");
        mockDoc2.setField("apis_full_identifier", "100");
        
        ArrayList<String> expectedResult2 = new ArrayList<String>(Arrays.asList("ddbdp-series-1 1 100", "apis-series-1 0 100"));
        assertEquals(expectedResult2, testInstance.getAllSortedIds(mockDoc2));
        
        // (ii) ordering should go:
        //      (a) canonical ddbdp identifier as reconsituted from uri
        //      (b) APIS publication number
        //      (c) APIS inventory number
        //      (d) other ddbdp identifiers
        //      (e) hgv identifiers
        //      (f) other apis identifiers
        // nb: ordering *within* these divisions is guaranteed by the IdComparator
        // see info.papyri.dispatch.browse.IdComparator and info.papyri.dispatch.browse.IdComparatorTest
        
        
        SolrDocument mockDoc3 = new SolrDocument();
        mockDoc3.setField("id", "http://papyri.info/hgv/103000");
        mockDoc3.setField("ddbdp_series", "ddbdp-series-1");
        mockDoc3.setField("ddbdp_volume", "1");
        mockDoc3.setField("ddbdp_full_identifier", "100");
        mockDoc3.setField("hgv_series", "hgv-series-1");
        mockDoc3.setField("hgv_volume", "1");
        mockDoc3.setField("hgv_full_identifier", "103000");
        mockDoc3.setField("apis_series", "britmus");
        mockDoc3.setField("apis_volume", "0");
        mockDoc3.setField("apis_full_identifier", "5");
        mockDoc3.setField(SolrField.apis_publication_id.name(), "apis-test-1-publication");
        mockDoc3.setField(SolrField.apis_inventory.name(), "apis-test-1-inventory");
        
        ArrayList<String> expectedResult3 = new ArrayList<String>(Arrays.asList("apis-test-1-publication", "apis-test-1-inventory", "ddbdp-series-1 1 100", "hgv-series-1 1 103000", "britmus 0 5"));
        assertEquals(expectedResult3, testInstance.getAllSortedIds(mockDoc3));
        
        // (iii) nulls shouldn't make it blow a gasket
        
        SolrDocument mockDoc4 = new SolrDocument();
        assertEquals(new ArrayList<String>(), testInstance.getAllSortedIds(mockDoc4));
        
        // (iv) empty strings shouldn't make it blow a gasket
        
        mockDoc4.setField("id", "");
        mockDoc4.setField("ddbdp_series", "");
        mockDoc4.setField("ddbdp_volume", "     ");
        mockDoc4.setField("ddbdp_full_identifier", " ");
        assertEquals(new ArrayList<String>(), testInstance.getAllSortedIds(mockDoc4));
        
        
    }
    
    public void testRetrieveRecords(){
        
        QueryResponse mockQueryResponse = createStrictMock(QueryResponse.class);
                
        // requirements:
        
        // (i) null id should throw exception, no record created
        
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        
        SolrDocument blankDocument = new SolrDocument();
        solrDocumentList.add(blankDocument);
        
        expect(mockQueryResponse.getResults()).andReturn(solrDocumentList).anyTimes();
        replay(mockQueryResponse);

        assertEquals(0, testInstance.retrieveRecords(mockQueryResponse).size());
        
        verify(mockQueryResponse);
        
        // (ii) if id specified, nulls in other fields are tolerated
        
        SolrDocument nearlyBlankDocument = new SolrDocument();
        nearlyBlankDocument.setField("id", "http://papyri.info/ddbdp/dummy_papyrus");
        solrDocumentList.add(nearlyBlankDocument);
        
        QueryResponse mockQueryResponse2 = createStrictMock(QueryResponse.class);
        
        expect(mockQueryResponse2.getResults()).andReturn(solrDocumentList).anyTimes();
        replay(mockQueryResponse2);
        assertEquals(1, testInstance.retrieveRecords(mockQueryResponse2).size());
        verify(mockQueryResponse2);
        
        
    }


}
