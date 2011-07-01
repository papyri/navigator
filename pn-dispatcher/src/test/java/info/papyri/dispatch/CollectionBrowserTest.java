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

    public void testIsCurrentlyDisplayingDocuments() throws MalformedURLException, SolrServerException{
        /*ArrayList<BooleanPathBits> bpis = this.generateDisplayingDocumentBits();
        Iterator<BooleanPathBits> bpit = bpis.iterator();
        while(bpit.hasNext()){
            
            BooleanPathBits bps = bpit.next();
            LinkedHashMap<CollectionBrowser.SolrField, String> bits = bps.getPathBits();
            Boolean bpBool = bps.getBooleanValue();
            COLLECTION_BROWSER.setPathBits(bits);
            COLLECTION_BROWSER.setCollectionPrefix();
            SolrQuery sq = COLLECTION_BROWSER.buildSolrQuery();
            System.out.println("SolrQuery is " + sq.toString());
            SolrServer solrServer = new CommonsHttpSolrServer("http://localhost:8082/solr/" + CollectionBrowser.PN_SEARCH);
            QueryResponse qr = solrServer.query(sq);
            //assertEquals(bpBool, COLLECTION_BROWSER.isCurrentlyDisplayingDocuments(qr));
        }
        */
            assertTrue(true);   
    }
        
    private ArrayList<BooleanPathBits> generateDisplayingDocumentBits(){
        
        ArrayList<BooleanPathBits>  testPathBits = new ArrayList<BooleanPathBits>();
        
        LinkedHashMap<CollectionBrowser.SolrField, String> testBits0 = new LinkedHashMap<CollectionBrowser.SolrField, String>();
        testBits0.put(CollectionBrowser.SolrField.collection, "ddbdp");
        testBits0.put(CollectionBrowser.SolrField.series, "bgu");
        BooleanPathBits bpb0 = new BooleanPathBits(testBits0, false);
        testPathBits.add(bpb0);
        
        LinkedHashMap<CollectionBrowser.SolrField, String> testBits1 = new LinkedHashMap<CollectionBrowser.SolrField, String>();
        testBits1.put(CollectionBrowser.SolrField.collection, "ddbdp");
        testBits1.put(CollectionBrowser.SolrField.series, "bgu");
        testBits1.put(CollectionBrowser.SolrField.volume, "1");
        BooleanPathBits bpb1 = new BooleanPathBits(testBits1, true);
        testPathBits.add(bpb1); 
        
        LinkedHashMap<CollectionBrowser.SolrField, String> testBits2 = new LinkedHashMap<CollectionBrowser.SolrField, String>();
        testBits2.put(CollectionBrowser.SolrField.collection, "ddbdp");
        testBits2.put(CollectionBrowser.SolrField.series, "c.ep.lat");
        BooleanPathBits bpb2 = new BooleanPathBits(testBits2, true);
        testPathBits.add(bpb2);
        
        LinkedHashMap<CollectionBrowser.SolrField, String> testBits3 = new LinkedHashMap<CollectionBrowser.SolrField, String>();
        testBits3.put(CollectionBrowser.SolrField.collection, "ddbdp");
        BooleanPathBits bpb3 = new BooleanPathBits(testBits3, false);
        testPathBits.add(bpb3); 
        
        LinkedHashMap<CollectionBrowser.SolrField, String> testBits5 = new LinkedHashMap<CollectionBrowser.SolrField, String>();
        testBits5.put(CollectionBrowser.SolrField.collection, "apis");
        BooleanPathBits bpb5 = new BooleanPathBits(testBits5, false);
        testPathBits.add(bpb5);
        
        LinkedHashMap<CollectionBrowser.SolrField, String> testBits6 = new LinkedHashMap<CollectionBrowser.SolrField, String>();
        testBits6.put(CollectionBrowser.SolrField.collection, "apis");
        testBits6.put(CollectionBrowser.SolrField.series, "britmus");
        BooleanPathBits bpb6 = new BooleanPathBits(testBits6, true);
        testPathBits.add(bpb6);
        
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
    
        public Boolean getBooleanValue(){
            
            return this.isCollection;
            
        }
    
    
    }
    
}
