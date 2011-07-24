package info.papyri.dispatch.browse;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.ArrayList;
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
    
    CollectionBrowser collectionBrowser; 
    
    
    public CollectionBrowserTest(String testName) {
        
        super(testName);
        collectionBrowser = new CollectionBrowser();
        
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetDisplayId(){
        
        LinkedHashMap<SolrField, String> pathParts = new LinkedHashMap<SolrField, String>();
        pathParts.put(SolrField.collection, "ddbdp");
        
        
        SolrDocument mockDoc = createStrictMock(SolrDocument.class);
        expect(mockDoc.getFieldValue("ddbdp_full_identifier")).andReturn("ddbdp.dummy");
        expect(mockDoc.getFieldValue("ddbdp_full_identifier")).andReturn("ddbdp.dummy");
        expect(mockDoc.getFieldValue("hgv_full_identifier")).andReturn("test1, sham1");
        expect(mockDoc.getFieldValue("hgv_volume")).andReturn("test, sham");
        expect(mockDoc.getFieldValue("hgv_series")).andReturn("o.hgv, o.hgv");
        replay(mockDoc);
        ArrayList<String> previousIds = new ArrayList<String>();

        assertEquals(collectionBrowser.getDisplayId(pathParts, mockDoc, previousIds), "ddbdp.dummy");
        previousIds.add("ddbdp.dummy");
        assertEquals(collectionBrowser.getDisplayId(pathParts, mockDoc, previousIds), "-1");
        pathParts = new LinkedHashMap<SolrField, String>();
        pathParts.put(SolrField.collection, "hgv");
        pathParts.put(SolrField.series, "o.hgv");
        pathParts.put(SolrField.volume, "test");
        assertEquals(collectionBrowser.getDisplayId(pathParts, mockDoc, previousIds), "test1");
        verify(mockDoc);
    }
    
        
 
    
}
