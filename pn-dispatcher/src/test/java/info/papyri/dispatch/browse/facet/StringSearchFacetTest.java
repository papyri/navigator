package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;


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
        
        System.out.println("pullApartParams");
       
        HashMap<String, String[]> mockParams = new HashMap<String, String[]>();
        
        // requirements
        
        // basic functionality - needs to pull out one or two
        // complete SearchConfiguration objects as appropriate
        
        mockParams.put("string", new String[]{"strateg"});
        mockParams.put("type", new String[]{ StringSearchFacet.SearchType.SUBSTRING.name()});
        mockParams.put("target", new String[]{ StringSearchFacet.SearchTarget.TEXT.name()});
        mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase(), new String[]{"on"});
        mockParams.put(StringSearchFacet.SearchOption.NO_MARKS.name().toLowerCase(), new String[]{"on"});
        
        ArrayList<SearchConfiguration> configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        SearchConfiguration config = configs.get(0);
        assertEquals("strateg", config.getSearchString());
        assertEquals(StringSearchFacet.SearchType.SUBSTRING, config.getSearchType());
        assertEquals(StringSearchFacet.SearchTarget.TEXT, config.getSearchTarget());
        assertFalse(config.getBetaOn());
        assertTrue(config.getIgnoreCaps());
        assertTrue(config.getIgnoreMarks());
        assertFalse(config.getLemmatizedSearch());
        
        mockParams.clear();
        
        mockParams.put("string2", new String[]{"τοῦ"});
        mockParams.put("type2", new String[]{ StringSearchFacet.SearchType.PROXIMITY.name()}); 
        mockParams.put("target2", new String[]{StringSearchFacet.SearchTarget.METADATA.name()});
        mockParams.put(StringSearchFacet.SearchOption.BETA.toString().toLowerCase() + "2", new String[]{"on"});
        mockParams.put(StringSearchFacet.SearchOption.LEMMAS.toString().toLowerCase() + "2", new String[]{"on"}); 
        
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        
        assertEquals(1, configs.size());
        SearchConfiguration config2 = configs.get(0);
        
        assertEquals("τοῦ", config2.getSearchString());
        assertEquals(StringSearchFacet.SearchTarget.METADATA, config2.getSearchTarget());
        assertEquals(StringSearchFacet.SearchType.PROXIMITY, config2.getSearchType());
        assertTrue(config2.getBetaOn());
        assertFalse(config2.getIgnoreCaps());
        assertFalse(config2.getIgnoreMarks());
        assertTrue(config2.getLemmatizedSearch());
        
        
        // missing keyword, type, or target parameters should cause the entire SC object not to be instantiated
        
        mockParams.put("string3", new String[]{"apuleius"});
        mockParams.put("type3", new String[]{StringSearchFacet.SearchType.PHRASE.name().toLowerCase()});
        mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase() + "3", new String[]{"on"});
        mockParams.put(StringSearchFacet.SearchOption.NO_MARKS.name().toLowerCase() + "3", new String[]{"on"});
        
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        
        // malformed search type or target parameters should cause the entire sc object not to be instantiated
        
        mockParams.put("string5", new String[]{"querunculus"});
        mockParams.put("type5", new String[]{"DUMMY_VALUE"});
        mockParams.put("target5", new String[]{"DUMMY_VALUE2"}); 
        
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        
        // multiple parameter values are ignored
        
        mockParams.clear();
        mockParams.put("string4",new String[]{"cupid", "psyche"});
        mockParams.put("type4", new String[]{StringSearchFacet.SearchType.PROXIMITY.name().toLowerCase(), StringSearchFacet.SearchType.SUBSTRING.name().toLowerCase()});
        mockParams.put("target4", new String[]{StringSearchFacet.SearchTarget.ALL.name().toLowerCase(), StringSearchFacet.SearchTarget.TEXT.name().toLowerCase()});
        mockParams.put(StringSearchFacet.SearchOption.BETA.name().toLowerCase() + "4", new String[]{"on", "off"});
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        SearchConfiguration config3 = configs.get(0);
        assertEquals("cupid", config3.getSearchString());
        assertEquals(StringSearchFacet.SearchType.PROXIMITY, config3.getSearchType());
        assertEquals(StringSearchFacet.SearchTarget.ALL, config3.getSearchTarget());
        assertTrue(config3.getBetaOn());
        
    }


}
