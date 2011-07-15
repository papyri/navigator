/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.browse.facet;

import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;

/**
 *
 * @author thill
 */
public class DateQueryCoordinatorTest extends TestCase {
    
    public DateQueryCoordinatorTest(String testName) {
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
     * Test of addDateFacetQueryContribution method, of class DateQueryCoordinator.
     */
    public void testAddDateFacetQueryContribution() {
        System.out.println("addDateFacetQueryContribution");
        SolrQuery solrQuery = new SolrQuery();
        DateQueryCoordinator instance = new DateQueryCoordinator();
        solrQuery = instance.addDateFacetQueryContribution(solrQuery);
        System.out.println(solrQuery.toString());
        
    }

}
