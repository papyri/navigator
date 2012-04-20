/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
    
    
    public DocumentBrowseRecordTest(String testName) {
        super(testName);


    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimplifyFoundString(){
        
         try{
             
             DocumentBrowseRecord testInstance = new DocumentBrowseRecord("1", new ArrayList<String>(), new URL("http://papyri.info/"), new ArrayList<String>(), "", "", "", new ArrayList<String>(), "", false, new ArrayList<SearchClause>());
             String test1 = "Ka[ra-]3.             nidi.";
             testInstance.simplifyFoundString(test1);
             
             
        } catch(Exception e){ System.out.println("Could not instantiate test instance on simplifyFoundString test"); }       
        
        
        
        
    }

}
