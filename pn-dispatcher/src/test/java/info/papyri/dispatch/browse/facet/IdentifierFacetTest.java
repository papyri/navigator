package info.papyri.dispatch.browse.facet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;

/**
 *
 * @author thill
 */
public class IdentifierFacetTest extends TestCase {
    
    private IdentifierFacet testInstance;
    
    public IdentifierFacetTest(String testName) {
        super(testName);
        testInstance = new IdentifierFacet();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBuildQueryContribution() {
        
        HashMap<String, String[]> mockParams = new HashMap<String, String[]>();
        mockParams.put(IdentifierFacet.IdParam.SERIES.name(), new String[]{"BGU"});
        mockParams.put(IdentifierFacet.IdParam.VOLUME.name(), new String[]{"12"});
        mockParams.put(IdentifierFacet.IdParam.IDNO.name(), new String[]{"2133"});
        
        assertTrue(testInstance.addConstraints(mockParams));
        SolrQuery sq = new SolrQuery();
        sq = testInstance.buildQueryContribution(sq);
        System.out.println(sq.toString());
        
    }
    
    public void testGetSpecifierClauseAsJavaRegex(){
        
        HashMap<String, String[]> mockParams = new HashMap<String, String[]>();
        mockParams.put(IdentifierFacet.IdParam.SERIES.name(), new String[]{"BGU"});
        mockParams.put(IdentifierFacet.IdParam.VOLUME.name(), new String[]{"12"});
        mockParams.put(IdentifierFacet.IdParam.IDNO.name(), new String[]{"2133"});
        testInstance.addConstraints(mockParams);
        
        String test1 = testInstance.getSpecifierClauseAsJavaRegex();
        assertEquals("^2133;BGU;12;.+?$", test1);
        Pattern pattern = Pattern.compile(test1);
        ArrayList<String> patterns = new ArrayList<String>();
        patterns.add("testing1");
        patterns.add("testing2");
        for(String patt : patterns){
            
            Matcher matcher = pattern.matcher(patt);
            if(!matcher.matches()) System.out.println("didn't match");
            
        }
        
        mockParams.clear();
        mockParams.put(IdentifierFacet.IdParam.SERIES.name(), new String[]{"bgu"});
        testInstance.addConstraints(mockParams);
        
        String test2 = testInstance.getSpecifierClauseAsJavaRegex();
        assertEquals("^bgu;.+?;.+?;.+?$", test2);
        
    }

 
}
