package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchConfiguration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    
    public void testSubstituteTerms(){
        
        // sanity check - no transformation req'd
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, false, false, false, 0);
        ArrayList<String> initialInput = new ArrayList<String>(Arrays.asList("λόγιος"));
        ArrayList<String> transformedInput = new ArrayList<String>(Arrays.asList("λόγιος"));
        assertEquals("λόγιος", tinstance.substituteTerms(initialInput, transformedInput));
        
        // with field
        StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("transcription:lo/gios", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, true, false, false, 0);
        initialInput = new ArrayList<String>(Arrays.asList("lo/gios"));
        transformedInput = new ArrayList<String>(Arrays.asList("λόγιοσ"));
        assertEquals("transcription:λόγιοσ", tinstance2.substituteTerms(initialInput, transformedInput));
        
        // pseudo-lemmatised
        StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("lem:λύω AND του", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, true, false, false, 0);
        initialInput = new ArrayList<String>(Arrays.asList("λύω", "του"));
        transformedInput = new ArrayList<String>(Arrays.asList("(λυω OR λυεισ OR λυε)", "του"));
        assertEquals("lem:(λυω OR λυεισ OR λυε) AND του", tinstance3.substituteTerms(initialInput, transformedInput));
        
        // repeated
        StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("transcription:λύω AND lem:λύω AND transcription_ia:λυεις", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, true, false, false, 0);
        initialInput = new ArrayList<String>(Arrays.asList("λύω", "λύω", "λυεις"));
        transformedInput = new ArrayList<String>(Arrays.asList("λύω", "(λυω OR λυεισ OR λυε)", "λυεισ"));
        assertEquals("transcription:λύω AND lem:(λυω OR λυεισ OR λυε) AND transcription_ia:λυεισ", tinstance4.substituteTerms(initialInput, transformedInput));  
        
    }
    
    public void testSubstituteFields(){
        
        // sanity check - no transformation required
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("transcription:λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, false, 0);
        assertEquals("transcription:λόγιος", tinstance.substituteFields());
        
        // HTML control searches - need always to wrap in brackets
        // substring search -
        // field should be transcription_ngram_ia
         StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, false, false, 0);
         assertEquals("transcription_ngram_ia:(λόγιος)", tinstance2.substituteFields());
        
        // lemma search -
        // field should be transcription_ia
        StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, false, false, false, 0);
         assertEquals("transcription_ia:(λόγιος)", tinstance3.substituteFields());
        
        // phrase search
        // field should be transcription
         StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, false, false, false, 0);
         assertEquals("transcription:(λόγιος)", tinstance4.substituteFields());
         
        // metadata search - 
        // field should be metadata (for present)
         StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.METADATA, StringSearchFacet.SearchType.PHRASE, false, false, false, 0);
         assertEquals("metadata:(λόγιος)", tinstance5.substituteFields());
        
        // translation search -
        // field should be translation
         StringSearchFacet.SearchConfiguration tinstance6 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TRANSLATIONS, StringSearchFacet.SearchType.PHRASE, false, false, false, 0);
         assertEquals("translation:(λόγιος)", tinstance6.substituteFields());
         
        // all search -
        // field should be all
          StringSearchFacet.SearchConfiguration tinstance7 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.ALL, StringSearchFacet.SearchType.PHRASE, false, false, false, 0);
         assertEquals("all:(λόγιος)", tinstance7.substituteFields());
         
        // if no caps and no marks
        // field should be transcription_ia
         StringSearchFacet.SearchConfiguration tinstance8 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, false, true, true, 0);
         assertEquals("transcription_ia:(λόγιος)", tinstance8.substituteFields());
        
        // if no caps only
        // field should be transcription_ic
         StringSearchFacet.SearchConfiguration tinstance9 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, false, true, false, 0);
         assertEquals("transcription_ic:(λόγιος)", tinstance9.substituteFields());        
        
        // if no marks only
        // field should be transcription_id
         StringSearchFacet.SearchConfiguration tinstance10 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, false, false, true, 0);
         assertEquals("transcription_id:(λόγιος)", tinstance10.substituteFields());
         
        // USER-DEFINED SEARCHES
        
        // lem indicators need to be replaced with transcription_ia
        // pure
        StringSearchFacet.SearchConfiguration tinstance11 = testInstance.new SearchConfiguration("lem:λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, false, 0);
         assertEquals("transcription_ia:λόγιος", tinstance11.substituteFields());
         
         StringSearchFacet.SearchConfiguration tinstance12 = testInstance.new SearchConfiguration("lem:λόγιος AND lem:του", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, false, 0);
         assertEquals("transcription_ia:λόγιος AND transcription_ia:του", tinstance12.substituteFields());
        // mixed
         StringSearchFacet.SearchConfiguration tinstance13 = testInstance.new SearchConfiguration("lem:λόγιος AND transcription:του", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, false, 0);
         assertEquals("transcription_ia:λόγιος AND transcription:του", tinstance13.substituteFields());
        
        
        
        
        
        
    }
    
    public void testLemmatizeWord(){
        
        // sanity check - returns false if no lemma requested
        
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, true, true, 0);
        ArrayList<String> testInput = new ArrayList<String>(Arrays.asList("λόγιος"));
        assertFalse(tinstance.lemmatizeWord(testInput, 0));
        
        
         // basic test - single instance
         StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("lem:λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, true, true, 0);
         testInput = new ArrayList<String>(Arrays.asList("λόγιος"));
         assertTrue(tinstance2.lemmatizeWord(testInput, 0));
        
        // double instance
         StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("lem:λόγιος AND lem:προχειρισάμενοι", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, true, true, 0);
         testInput = new ArrayList<String>(Arrays.asList("λόγιος", "προχειρισάμενοι"));
         assertTrue(tinstance3.lemmatizeWord(testInput, 0));
         assertTrue(tinstance3.lemmatizeWord(testInput, 1));
         
        // mixed instance
        StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("lem:λόγιος AND προχειρισάμενοι", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, true, true, 0);
         testInput = new ArrayList<String>(Arrays.asList("λόγιος", "προχειρισάμενοι"));
         assertTrue(tinstance4.lemmatizeWord(testInput, 0));
         assertFalse(tinstance4.lemmatizeWord(testInput, 1));
        
        // repetitive instance
        StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("transcription_ngram_ia:λόγιος AND lem:λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, true, true, 0);
        testInput = new ArrayList<String>(Arrays.asList("λόγιος", "λόγιος"));
        assertFalse(tinstance5.lemmatizeWord(testInput, 0));
        assertTrue(tinstance5.lemmatizeWord(testInput, 1));
          
    }
    
    public void testTransformKeywords(){      
        
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, true, false, false, 0);

        // null check
        ArrayList<String> testOutput = tinstance.transformKeywords(null);
        assertEquals(0, testOutput.size());
        
        // return correct betacode
        // note also sigma transformation
        ArrayList<String> betaInput = new ArrayList<String>(Arrays.asList("lu/w", "strathgo\\s"));
        testOutput = tinstance.transformKeywords(betaInput);
        assertEquals(2, testOutput.size());
        assertEquals("λύω", testOutput.get(0));
        assertEquals("στρατηγὸσ", testOutput.get(1));
        
        // lowercase if required
        
        StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, true, false, 0);
        ArrayList<String> capsInput = new ArrayList<String>(Arrays.asList("Stratiou", "Στρατιοῦ", "ΣΤΡΑΤΙΟΥ"));
        testOutput.clear();
        testOutput = tinstance2.transformKeywords(capsInput);
        assertEquals(3, testOutput.size());
        assertEquals("stratiou", testOutput.get(0));
        assertEquals("στρατιοῦ", testOutput.get(1));
        assertEquals("στρατιου", testOutput.get(2));
        
        
        // remove diacritics if required
        StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, false, true, 0);
        ArrayList<String> diaInput = new ArrayList<String>(Arrays.asList("Στρατηγὸς", "παραγγελίας", "σίλλὖ"));
        testOutput.clear();
        testOutput = tinstance3.transformKeywords(diaInput);
        assertEquals(3, testOutput.size());
        assertEquals("Στρατηγοσ", testOutput.get(0));
        assertEquals("παραγγελιασ", testOutput.get(1));
        assertEquals("σιλλυ", testOutput.get(2));
        
        // combo - remove diacritics and lowecase betacode input
        StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, true, true, true, 0);
        ArrayList<String> comboInput = new ArrayList<String>(Arrays.asList("STRATHGO\\S"));
        testOutput.clear();
        testOutput = tinstance4.transformKeywords(comboInput);
        assertEquals(1, testOutput.size());
        assertEquals("στρατηγοσ", testOutput.get(0));
        
        // wrap phrase search in double-quotes if not so already
        StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, false, true, true, 0);
        ArrayList<String> phraseInput = new ArrayList<String>(Arrays.asList("των ανδρων", "'των ανδρων'", "\"των ανδρων\""));
        testOutput.clear();
        testOutput = tinstance5.transformKeywords(phraseInput);
        assertEquals(3, testOutput.size());
        assertEquals("\"των ανδρων\"", testOutput.get(0));
        assertEquals("\"των ανδρων\"", testOutput.get(1));
        assertEquals("\"των ανδρων\"", testOutput.get(2));
        
        // expand lemmas
        // both with html control input
        StringSearchFacet.SearchConfiguration tinstance6 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, false, true, true, 0);
        ArrayList<String> lemmaInput0 = new ArrayList<String>(Arrays.asList("λόγιος"));
        testOutput.clear();
        testOutput = tinstance6.transformKeywords(lemmaInput0);
        assertEquals(1, testOutput.size());
        assertTrue(testOutput.get(0).matches("\\((([^\\s]+?) OR )+[^\\s]+\\)"));
        // and in situ
        StringSearchFacet.SearchConfiguration tinstance7 = testInstance.new SearchConfiguration("lem:λύω", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, true, true, 0);
        ArrayList<String> lemmaInput1 = new ArrayList<String>(Arrays.asList("λύω"));
        testOutput.clear();
        testOutput = tinstance7.transformKeywords(lemmaInput1);
        assertEquals(1, testOutput.size());
        assertTrue(testOutput.get(0).matches("\\((([^\\s]+?) OR )+[^\\s]+\\)"));      
        // no lemma available should result in the string being left untransformed but bracketed
        StringSearchFacet.SearchConfiguration tinstance8 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, false, true, true, 0);
        ArrayList<String> lemmaInput2 = new ArrayList<String>(Arrays.asList("ψψζξ"));
        testOutput.clear();
        testOutput = tinstance6.transformKeywords(lemmaInput2);
        assertEquals(1, testOutput.size());
        assertEquals("(ψψζξ)", testOutput.get(0));
        
        // repeated terms in the query should not cause problems
        StringSearchFacet.SearchConfiguration tinstance9 = testInstance.new SearchConfiguration("lem:λόγος AND transcription_ngram_ia:λόγος AND transcription_ia:λόγος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, true, true, 0);
        ArrayList<String> repeatInput = new ArrayList<String>(Arrays.asList("λόγος", "λόγος", "λόγος"));
        testOutput.clear();
        testOutput = tinstance9.transformKeywords(repeatInput);
        assertEquals(3, testOutput.size());
        assertTrue(testOutput.get(0).matches("\\((([^\\s]+?) OR )+[^\\s]+\\)"));
        assertEquals("λογοσ", testOutput.get(1));
        assertEquals("λογοσ", testOutput.get(2));
        
        // word-boundary octothorpe characters should be converted into carets
        ArrayList<String> octoInput = new ArrayList<String>(Arrays.asList("#του", "#ανδρ#"));
        testOutput.clear();
        testOutput = tinstance9.transformKeywords(octoInput);
        assertEquals(2, testOutput.size());
        assertEquals("\\^του", testOutput.get(0));
        assertEquals("\\^ανδρ\\^", testOutput.get(1));
    }
    



}
