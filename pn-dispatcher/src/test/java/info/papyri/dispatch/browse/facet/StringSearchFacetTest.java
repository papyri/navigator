package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchConfiguration;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;


/**
 *
 * @author thill
 */
public class StringSearchFacetTest extends TestCase {
    
    private StringSearchFacet testInstance = new StringSearchFacet();    
        
    public StringSearchFacetTest(String testName) {
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

    


    public void testPullApartParams() {

        HashMap<String, String[]> mockParams = new HashMap<String, String[]>();
        
        // requirements
        
        // basic functionality - needs to pull out one or two
        // complete SearchConfiguration objects as appropriate
        
        mockParams.put("STRING", new String[]{"strateg"});
        mockParams.put("type", new String[]{ StringSearchFacet.SearchType.SUBSTRING.name()});
        mockParams.put("target", new String[]{ StringSearchFacet.SearchTarget.TEXT.name()});
        mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase(), new String[]{"on"});
        mockParams.put(StringSearchFacet.SearchOption.NO_MARKS.name().toLowerCase(), new String[]{"on"});
        
        HashMap<Integer, SearchConfiguration> configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        SearchConfiguration config = configs.get(0);
        assertEquals("strateg", config.getSearchString());
        assertEquals(StringSearchFacet.SearchType.SUBSTRING, config.getSearchType());
        assertEquals(StringSearchFacet.SearchTarget.TEXT, config.getSearchTarget());
        assertFalse(config.getBetaOn());
        assertTrue(config.getIgnoreCaps());
        assertTrue(config.getIgnoreMarks());
        
        mockParams.clear();
        
        mockParams.put("STRING2", new String[]{"tou="});
        mockParams.put("type2", new String[]{ StringSearchFacet.SearchType.PROXIMITY.name()}); 
        mockParams.put("target2", new String[]{StringSearchFacet.SearchTarget.METADATA.name()});
        mockParams.put(StringSearchFacet.SearchOption.BETA.toString().toLowerCase() + "2", new String[]{"on"});
        mockParams.put(StringSearchFacet.SearchType.WITHIN.name().toLowerCase() + "2", new String[]{"10"});

        
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        
        assertEquals(1, configs.size());
        SearchConfiguration config2 = configs.get(2);
        assertEquals("\"τοῦ\"~10", config2.getSearchString());
        assertEquals(StringSearchFacet.SearchTarget.METADATA, config2.getSearchTarget());
        assertEquals(StringSearchFacet.SearchType.PROXIMITY, config2.getSearchType());
        assertTrue(config2.getBetaOn());
        assertFalse(config2.getIgnoreCaps());
        assertFalse(config2.getIgnoreMarks());
        
        
        // missing keyword, type, or target parameters should cause the entire SC object not to be instantiated
        
        mockParams.put("STRING3", new String[]{"apuleius"});
        mockParams.put("type3", new String[]{StringSearchFacet.SearchType.PHRASE.name().toLowerCase()});
        mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase() + "3", new String[]{"on"});
        mockParams.put(StringSearchFacet.SearchOption.NO_MARKS.name().toLowerCase() + "3", new String[]{"on"});
        
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        
        // malformed search type or target parameters should cause the entire sc object not to be instantiated
        
        mockParams.put("STRING5", new String[]{"querunculus"});
        mockParams.put("type5", new String[]{"DUMMY_VALUE"});
        mockParams.put("target5", new String[]{"DUMMY_VALUE2"}); 
        
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        
        // multiple parameter values are ignored
        
        mockParams.clear();
        mockParams.put("STRING4",new String[]{"cupid", "psyche"});
        mockParams.put("type4", new String[]{StringSearchFacet.SearchType.PHRASE.name().toLowerCase(), StringSearchFacet.SearchType.SUBSTRING.name().toLowerCase()});
        mockParams.put("target4", new String[]{StringSearchFacet.SearchTarget.ALL.name().toLowerCase(), StringSearchFacet.SearchTarget.TEXT.name().toLowerCase()});
        mockParams.put(StringSearchFacet.SearchOption.BETA.name().toLowerCase() + "4", new String[]{"off", "on"});
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        SearchConfiguration config3 = configs.get(4);
        assertEquals("cupid", config3.getSearchString());
        assertEquals(StringSearchFacet.SearchType.PHRASE, config3.getSearchType());
        assertEquals(StringSearchFacet.SearchTarget.ALL, config3.getSearchTarget());
        assertFalse(config3.getBetaOn());
        
        // setting proximity search type without a within parameter causes within to set itself to zero
        
        mockParams.clear();
        mockParams.put("STRING6", new String[]{"test"});
        mockParams.put("type6", new String[]{ StringSearchFacet.SearchType.PROXIMITY.name().toLowerCase() });
        mockParams.put("target6", new String[]{ StringSearchFacet.SearchTarget.TEXT.name().toLowerCase() });
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        assertEquals(0, configs.get(6).getProximityDistance());
        
        mockParams.clear();
        
        mockParams.put("STRING7", new String[]{"γγελιας# γους#"});
        mockParams.put("type7", new String[]{ StringSearchFacet.SearchType.SUBSTRING.name()});
        mockParams.put("target7", new String[]{ StringSearchFacet.SearchTarget.TEXT.name()});
        mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase(), new String[]{"on"});
        mockParams.put(StringSearchFacet.SearchOption.NO_MARKS.name().toLowerCase(), new String[]{"on"});
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        System.out.println(configs.get(7).getSearchString());
        
    }
    
    public void testGetFacetConstraints(){
        
        String testString1 = FacetParam.STRING.name();
        ArrayList<String> facetConstraints = testInstance.getFacetConstraints(testString1);
        assertEquals("0", facetConstraints.get(0));
        
        String testString2 = FacetParam.STRING.name() + "12";
        ArrayList<String> facetConstraints2 = testInstance.getFacetConstraints(testString2);
        assertEquals("12", facetConstraints2.get(0));
        
        
    }
    


}
