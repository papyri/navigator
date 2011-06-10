/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;



/**
 *
 * @author thill
 */


public class CollectionBrowserTest extends TestCase {
    
    CollectionBrowser COLLECTION_BROWSER = new CollectionBrowser();
    
    
    public CollectionBrowserTest(String testName) {
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

    /**
     * Test of init method, of class CollectionBrowser.
     */
    public void testInit() throws Exception {
        /*System.out.println("init");
        ServletConfig config = null;
        CollectionBrowser instance = new CollectionBrowser();
        instance.init(config);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");*/
    }

    /**
     * Test of processRequest method, of class CollectionBrowser.
     */
    public void testProcessRequest() throws Exception {
       /* System.out.println("processRequest");
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        CollectionBrowser instance = new CollectionBrowser();
        instance.processRequest(request, response);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");*/
    }

    /**
     * Test of doGet method, of class CollectionBrowser.
     */
    public void testDoGet() throws Exception {
        /*System.out.println("doGet");
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        CollectionBrowser instance = new CollectionBrowser();
        instance.doGet(request, response);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");*/
    }

    /**
     * Test of doPost method, of class CollectionBrowser.
     */
    public void testDoPost() throws Exception {
        /*System.out.println("doPost");
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        CollectionBrowser instance = new CollectionBrowser();
        instance.doPost(request, response);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");*/
    }

    /**
     * Test of getServletInfo method, of class CollectionBrowser.
     */
    public void testGetServletInfo() {
       /* System.out.println("getServletInfo");
        CollectionBrowser instance = new CollectionBrowser();
        String expResult = "";
        String result = instance.getServletInfo();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");*/
    }
    
    // tests to create
    
    public void testGetPathBitsMap(){
        
        
        
        
    }
    
    public void testBuildSolrQuery(){
        
        /*ArrayList<LinkedHashMap<CollectionBrowser.SolrField, String>> testPathBits = generateTestPathBits();
        Iterator<LinkedHashMap<CollectionBrowser.SolrField, String>> tpit = testPathBits.iterator();
        try {
            
            SolrServer solrServer = new CommonsHttpSolrServer("http://localhost:8082/solr/" + CollectionBrowser.PN_SEARCH);
                
            while(tpit.hasNext()){
            
                LinkedHashMap<CollectionBrowser.SolrField, String> tpb = tpit.next();
                SolrQuery sq = COLLECTION_BROWSER.buildSolrQuery(tpb);
                QueryResponse qr = solrServer.query(sq);
            
            }
        
        
        } catch (MalformedURLException ex) {
            Logger.getLogger(CollectionBrowserTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SolrServerException sse){}*/
       
        
    }
    
    public void testIsNextLevelCollection() throws SolrServerException, MalformedURLException{
        
        SolrServer solrServer = new CommonsHttpSolrServer("http://localhost:8082/solr/" + CollectionBrowser.PN_SEARCH);
        ArrayList<BooleanPathBits> testBits = generateTestPathBits();
        Iterator<BooleanPathBits> bpbit = testBits.iterator();
        while(bpbit.hasNext()){
        
            BooleanPathBits bpb = bpbit.next();
            LinkedHashMap<CollectionBrowser.SolrField, String> pathBits = bpb.getPathBits();
            Boolean isCollection = bpb.getIsCollection();
            
            SolrQuery sq = COLLECTION_BROWSER.buildSolrQuery(pathBits);
            System.out.println("!!!! " + sq.toString() + " !!!!");
            QueryResponse queryResponse = solrServer.query(sq);
            assertEquals(COLLECTION_BROWSER.isNextLevelCollection(pathBits, queryResponse), isCollection);
            
        
        }
    }
    
    
    private ArrayList<BooleanPathBits> generateTestPathBits(){
        
        ArrayList<BooleanPathBits>  testPathBits = new ArrayList<BooleanPathBits>();
        
        LinkedHashMap<CollectionBrowser.SolrField, String> testBits0 = new LinkedHashMap<CollectionBrowser.SolrField, String>();
        testBits0.put(CollectionBrowser.SolrField.collection, "ddbdp");
        testBits0.put(CollectionBrowser.SolrField.series, "bgu");
        BooleanPathBits bpb0 = new BooleanPathBits(testBits0, true);
        testPathBits.add(bpb0);
        
        LinkedHashMap<CollectionBrowser.SolrField, String> testBits1 = new LinkedHashMap<CollectionBrowser.SolrField, String>();
        testBits1.put(CollectionBrowser.SolrField.collection, "ddbdp");
        testBits1.put(CollectionBrowser.SolrField.series, "bgu");
        testBits1.put(CollectionBrowser.SolrField.volume, "1");
        BooleanPathBits bpb1 = new BooleanPathBits(testBits1, false);
        testPathBits.add(bpb1);
        
        LinkedHashMap<CollectionBrowser.SolrField, String> testBits2 = new LinkedHashMap<CollectionBrowser.SolrField, String>();
        testBits2.put(CollectionBrowser.SolrField.collection, "ddbdp");
        testBits2.put(CollectionBrowser.SolrField.series, "c.ep.lat");
        BooleanPathBits bpb2 = new BooleanPathBits(testBits2, false);
        testPathBits.add(bpb2);
        
        LinkedHashMap<CollectionBrowser.SolrField, String> testBits3 = new LinkedHashMap<CollectionBrowser.SolrField, String>();
        testBits3.put(CollectionBrowser.SolrField.collection, "ddbdp");
        BooleanPathBits bpb3 = new BooleanPathBits(testBits3, true);
        testPathBits.add(bpb3);
        
        LinkedHashMap<CollectionBrowser.SolrField, String> testBits4 = new LinkedHashMap<CollectionBrowser.SolrField, String>();
        testBits4.put(CollectionBrowser.SolrField.collection, "hgv");
        BooleanPathBits bpb4 = new BooleanPathBits(testBits4, true);
        testPathBits.add(bpb4);
        return testPathBits;
        
    }
    
    private class BooleanPathBits{
    
        private LinkedHashMap<CollectionBrowser.SolrField, String> pathBits;
        private Boolean isCollection;
    
        public BooleanPathBits(LinkedHashMap<CollectionBrowser.SolrField, String> pathBits, Boolean isCollection){
            
            this.pathBits = pathBits;
            this.isCollection = isCollection;
              
        }
        
        public LinkedHashMap<CollectionBrowser.SolrField, String> getPathBits(){
            
            return this.pathBits;
            
        }
    
        public Boolean getIsCollection(){
            
            return this.isCollection;
            
        }
    
    
    }
    
}
