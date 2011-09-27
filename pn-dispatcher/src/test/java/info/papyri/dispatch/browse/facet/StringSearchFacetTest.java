package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchConfiguration;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.xerces.util.URI.MalformedURIException;


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

        mockParams.clear();
        
        /*mockParams.put("STRING8", new String[]{"λυω"});
        mockParams.put("type8", new String[]{ StringSearchFacet.SearchType.LEMMAS.name().toLowerCase() });
        mockParams.put("target8", new String[] { StringSearchFacet.SearchTarget.TEXT.name().toLowerCase()});
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        System.out.println("Search is: \n" + configs.get(8).getSearchString());*/
    }
    
    public void testGetFacetConstraints(){
        
        String testString1 = FacetParam.STRING.name();
        ArrayList<String> facetConstraints = testInstance.getFacetConstraints(testString1);
        assertEquals("0", facetConstraints.get(0));
        
        String testString2 = FacetParam.STRING.name() + "12";
        ArrayList<String> facetConstraints2 = testInstance.getFacetConstraints(testString2);
        assertEquals("12", facetConstraints2.get(0));
        
        
    }   
    
    public void testHarvestKeywords(){
        
        StringSearchFacet.SearchConfiguration testInnerInstance = testInstance.new SearchConfiguration("λυω", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, false, false, false, 0);
        
        //null check
        ArrayList<String> keywords = testInnerInstance.harvestKeywords(null);
        assertEquals(0, keywords.size());
        
        // sanity check
        keywords = testInnerInstance.harvestKeywords("lu/w");
        assertEquals(1, keywords.size());
        assertEquals("lu/w", keywords.get(0));
        
        // needs to split based on whitespace
        keywords = testInnerInstance.harvestKeywords("λυω στρατηγος");
        assertEquals(2, keywords.size());
        assertEquals("λυω", keywords.get(0));
        assertEquals("στρατηγος", keywords.get(1));
        
        // needs to filter out # delimiters
        keywords = testInnerInstance.harvestKeywords("#κοι #υψι");
        assertEquals(2, keywords.size());
        assertEquals("κοι", keywords.get(0));
        assertEquals("υψι", keywords.get(1));
        
        // needs to filter out operators, whether AND OR NOT + - && || format
        
        keywords = testInnerInstance.harvestKeywords("λυω AND στρατηγος -του +λογιας && παραγελλιας || κοινον");
        assertEquals(6, keywords.size());
        assertEquals("λυω", keywords.get(0));
        assertEquals("στρατηγος", keywords.get(1));
        assertEquals("του", keywords.get(2));
        assertEquals("λογιας", keywords.get(3));
        assertEquals("παραγελλιας", keywords.get(4));
        assertEquals("κοινον", keywords.get(5));
        
        // needs to separate from field indicators, if any
        
        keywords = testInnerInstance.harvestKeywords("lem:lu/w");
        assertEquals(1, keywords.size());
        assertEquals("lu/w", keywords.get(0));
        
        keywords = testInnerInstance.harvestKeywords("lem:λυω AND transcription_ia:παραγελλιας");
        assertEquals(2, keywords.size());
        assertEquals("λυω", keywords.get(0));
        assertEquals("παραγελλιας", keywords.get(1));
             
        // needs to be able to cope with brackets
        keywords = testInnerInstance.harvestKeywords("στρατηγος AND (lem:λυω OR lem:κοινον)");
        assertEquals(3, keywords.size());
        assertEquals("στρατηγος", keywords.get(0));
        assertEquals("λυω", keywords.get(1));
        assertEquals("κοινον", keywords.get(2));
        
        keywords = testInnerInstance.harvestKeywords("(στρατηγος AND (lem:λυω OR lem:κοινον)) OR φαλαγξ");
        assertEquals(4, keywords.size());
        assertEquals("στρατηγος", keywords.get(0));
        assertEquals("λυω", keywords.get(1));
        assertEquals("κοινον", keywords.get(2));
        assertEquals("φαλαγξ", keywords.get(3));
        
        // tolerant of whitespace faults
        keywords = testInnerInstance.harvestKeywords("(στρατηγος AND(lem:λυω||lem:κοινον))OR φαλαγξ");
        assertEquals(4, keywords.size());
        assertEquals("στρατηγος", keywords.get(0));
        assertEquals("λυω", keywords.get(1));
        assertEquals("κοινον", keywords.get(2));
        assertEquals("φαλαγξ", keywords.get(3));

    }
    
    private void testTransformKeywords(){      
        
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, true, false, false, 0);
        
        // null check
        HashMap<String, String> testOutput = tinstance.transformKeywords(null);
        assertEquals(0, testOutput.size());
        
        
        // return correct betacode
        // note also sigma transformation
        ArrayList<String> betaInput = new ArrayList<String>(Arrays.asList("lu/w", "strathgo\\s"));
        testOutput = tinstance.transformKeywords(betaInput);
        assertEquals(2, testOutput.size());
        assertEquals("λύω", testOutput.get("lu/w"));
        assertEquals("στρατηγὸσ", testOutput.get("strathgo\\s"));
        
        
        // lowercase if required
        
        StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, true, false, 0);
        ArrayList<String> capsInput = new ArrayList<String>(Arrays.asList("Stratiou", "Στρατιοῦ", "ΣΤΡΑΤΙΟΥ"));
        testOutput.clear();
        testOutput = tinstance2.transformKeywords(capsInput);
        assertEquals(3, testOutput.size());
        assertEquals("stratiou", testOutput.get("Stratiou"));
        assertEquals("στρατιοῦ", testOutput.get("Στρατιοῦ"));
        assertEquals("στρατιου", testOutput.get("ΣΤΡΑΤΙΟΥ"));
        
        
        // remove diacritics if required
        StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, false, true, 0);
        ArrayList<String> diaInput = new ArrayList<String>(Arrays.asList("Στρατηγὸς", "παραγγελίας", "σίλλὖ"));
        testOutput.clear();
        testOutput = tinstance3.transformKeywords(diaInput);
        assertEquals(3, testOutput.size());
        assertEquals("Στρατηγοσ", testOutput.get("Στρατηγὸς"));
        assertEquals("παραγγελιασ", testOutput.get("παραγγελίας"));
        assertEquals("σιλλυ", "σίλλὖ");
        
        // combo - remove diacritics and lowecase betacode input
        StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, true, true, true, 0);
        ArrayList<String> comboInput = new ArrayList<String>(Arrays.asList("STRATHGO\\S"));
        testOutput.clear();
        testOutput = tinstance4.transformKeywords(comboInput);
        assertEquals(1, testOutput.size());
        assertEquals("στρατηγοσ", testOutput.get("STRATHGO\\S"));
        
        // wrap phrase search in double-quotes if not so already
        StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, false, true, true, 0);
        ArrayList<String> phraseInput = new ArrayList<String>(Arrays.asList("των ανδρων", "'των ανδρων'", "\"των ανδρων\""));
        testOutput.clear();
        testOutput = tinstance5.transformKeywords(phraseInput);
        assertEquals(3, testOutput.size());
        assertEquals("\"των ανδρων\"", testOutput.get("των ανδρων"));
        assertEquals("\"των ανδρων\"", testOutput.get("'των ανδρων'"));
        assertEquals("\"των ανδρων\"", testOutput.get("\"των ανδρων\""));
        
        // expand lemmas
        // both with html control input
        StringSearchFacet.SearchConfiguration tinstance6 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, false, true, true, 0);
        ArrayList<String> lemmaInput0 = new ArrayList<String>(Arrays.asList("λόγιας"));
        testOutput.clear();
        testOutput = tinstance6.transformKeywords(lemmaInput0);
        assertEquals(1, testOutput.size());
        assertTrue(testOutput.get("λόγιας").matches("\\((([^\\s]+?) OR )+[^\\s]+\\)"));
        // and in situ
        StringSearchFacet.SearchConfiguration tinstance7 = testInstance.new SearchConfiguration("lem:προχειρισάμενοι", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, true, true, 0);
        ArrayList<String> lemmaInput1 = new ArrayList<String>(Arrays.asList("προχειρισάμενοι"));
        testOutput.clear();
        testOutput = tinstance7.transformKeywords(lemmaInput1);
        assertEquals(1, testOutput.size());
        assertTrue(testOutput.get("προχειρισάμενοι").matches("\\((([^\\s]+?) OR )+[^\\s]+\\)"));
        
        
    }
    


}
