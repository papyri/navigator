package info.papyri.dispatch.browse;

import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchClause;
import java.net.URL;
import java.util.ArrayList;
import junit.framework.TestCase;

/**
 *
 * @author thill
 */
public class DocumentBrowseRecordTest extends TestCase {
    
    DocumentBrowseRecord testInstance;
    
    public DocumentBrowseRecordTest(String testName) {
        super(testName);
        testInstance = new DocumentBrowseRecord("test", new ArrayList<String>(), null, new ArrayList<String>(), "", "", "", new ArrayList<String>(), "", false, new ArrayList<SearchClause>());
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAnchorAssertionAtStart(){
        
        String test1 = "kaisar";
        assertEquals("kaisar", testInstance.anchorAssertionAtStart(test1, 3));
        
        String test2 = "ai sar";
        assertEquals("\\bsar", testInstance.anchorAssertionAtStart(test2, 3));
        
        String test3 = " imsar";
        assertEquals("\\bimsar", testInstance.anchorAssertionAtStart(test3, 3));
        
        String test4 = "ai sar";
        assertEquals("ai sar", testInstance.anchorAssertionAtStart(test4, 0));
        
    }
    
    public void testAnchorAssertionAtEnd(){
        
         String test1 = "kaisar";
         assertEquals("kaisar", testInstance.anchorAssertionAtEnd(test1, 3));
         
         String test2 = "kai sa";
         assertEquals("kai\\b", testInstance.anchorAssertionAtEnd(test2, 3));
         
         String test3 = "kaisa ";
         assertEquals("kaisa\\b", testInstance.anchorAssertionAtEnd(test3, 3));
         
         String test4 = "kai sar";
         assertEquals("kai sar", testInstance.anchorAssertionAtEnd(test4, 0));
        
        
    }
}
