package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchConfiguration;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import junit.framework.TestCase;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.TermsResponse.Term;
import org.apache.solr.common.params.CommonParams;



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
        
        mockParams.put("STRING", new String[]{"orator"});
        mockParams.put("type", new String[]{ StringSearchFacet.SearchType.SUBSTRING.name()});
        mockParams.put("target", new String[]{ StringSearchFacet.SearchTarget.TEXT.name()});
        mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase(), new String[]{"on"});
        mockParams.put(StringSearchFacet.SearchOption.NO_MARKS.name().toLowerCase(), new String[]{"on"});
        
        HashMap<Integer, StringSearchFacet.ISearchStringRetrievable> configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        StringSearchFacet.ISearchStringRetrievable config = configs.get(0);
        assertEquals("orator", config.getRawString());
        assertEquals(StringSearchFacet.SearchType.SUBSTRING, config.getSearchType());
        assertEquals(StringSearchFacet.SearchTarget.TEXT, config.getSearchTarget());
        assertTrue(config.getIgnoreCaps());
        assertTrue(config.getIgnoreMarks());
        
        mockParams.clear();
        
        mockParams.put("STRING2", new String[]{"τοῦ"});
        mockParams.put("type2", new String[]{ StringSearchFacet.SearchType.PROXIMITY.name()}); 
        mockParams.put("target2", new String[]{StringSearchFacet.SearchTarget.METADATA.name()});
        mockParams.put(StringSearchFacet.SearchType.WITHIN.name().toLowerCase() + "2", new String[]{"10"});

        
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        
        assertEquals(1, configs.size());
        StringSearchFacet.ISearchStringRetrievable config2 = configs.get(2);
        assertEquals("τοῦ", config2.getRawString());
        assertEquals(StringSearchFacet.SearchTarget.METADATA, config2.getSearchTarget());
        assertEquals(StringSearchFacet.SearchType.PROXIMITY, config2.getSearchType());
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
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        StringSearchFacet.ISearchStringRetrievable config3 = configs.get(4);
        assertEquals("cupid", config3.getRawString());
        assertEquals(StringSearchFacet.SearchType.PHRASE, config3.getSearchType());
        assertEquals(StringSearchFacet.SearchTarget.ALL, config3.getSearchTarget());
        
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
        
        StringSearchFacet.SearchConfiguration testInnerInstance = testInstance.new SearchConfiguration("λυω", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, false, false, 0);
        
        //null check
        ArrayList<String> keywords = testInnerInstance.harvestKeywords(null);
        assertEquals(0, keywords.size());
        
        // sanity check
        keywords = testInnerInstance.harvestKeywords("dux");
        assertEquals(1, keywords.size());
        assertEquals("dux", keywords.get(0));
        
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
        
        // needs to filter out literal proximity searches in a space-insensitive manner
        keywords = testInnerInstance.harvestKeywords("\"των ανδρων\"~10");
        assertEquals(2, keywords.size());
        assertEquals("\"των", keywords.get(0));
        assertEquals("ανδρων\"", keywords.get(1));
        
        keywords = testInnerInstance.harvestKeywords("\"των ανδρων\" ~ 10");
        assertEquals(2, keywords.size());
        assertEquals("\"των", keywords.get(0));
        assertEquals("ανδρων\"", keywords.get(1));
        
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
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, false, false, 0);
        ArrayList<String> initialInput = new ArrayList<String>(Arrays.asList("λόγιος"));
        ArrayList<String> transformedInput = new ArrayList<String>(Arrays.asList("λόγιος"));
        assertEquals("λόγιος", tinstance.substituteTerms(initialInput, transformedInput));
                
        // with field
        StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("transcription:λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, 0);
        initialInput = new ArrayList<String>(Arrays.asList("λόγιος"));
        transformedInput = new ArrayList<String>(Arrays.asList("λόγιοσ"));
        assertEquals("transcription:λόγιοσ", tinstance2.substituteTerms(initialInput, transformedInput));
        
        // pseudo-lemmatised
        StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("lem:λύω AND του", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, false, false, 0);
        initialInput = new ArrayList<String>(Arrays.asList("λύω", "του"));
        transformedInput = new ArrayList<String>(Arrays.asList("(λυω OR λυεισ OR λυε)", "του"));
        assertEquals("lem:(λυω OR λυεισ OR λυε) AND του", tinstance3.substituteTerms(initialInput, transformedInput));
        
        // repeated
        StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("transcription:λύω AND lem:λύω AND transcription_ia:λυεις", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, false, false, 0);
        initialInput = new ArrayList<String>(Arrays.asList("λύω", "λύω", "λυεις"));
        transformedInput = new ArrayList<String>(Arrays.asList("λύω", "(λυω OR λυεισ OR λυε)", "λυεισ"));
        assertEquals("transcription:λύω AND lem:(λυω OR λυεισ OR λυε) AND transcription_ia:λυεισ", tinstance4.substituteTerms(initialInput, transformedInput));  
        
        // with wildcards
        StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("η??υμενον", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, true, true, 0);
        initialInput = new ArrayList<String>(Arrays.asList("η??υμενον"));
        transformedInput = new ArrayList<String>(Arrays.asList("η??υμενον"));
        assertEquals("η??υμενον", tinstance5.substituteTerms(initialInput, transformedInput));
        
    }
    
    public void testSubstituteFields(){
        
        // sanity check - no transformation required
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("transcription:λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, 0);
        assertEquals("transcription:λόγιος", tinstance.substituteFields("transcription:λόγιος"));
        
        // HTML control searches - need always to wrap in brackets
        // substring search -
        // field should be transcription_ngram_ia
         StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, false, 0);
         assertEquals("transcription_ngram_ia:(λόγιος)", tinstance2.substituteFields("λόγιος"));
        
        // lemma search -
        // field should be transcription_ia
        StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, false, false, 0);
         assertEquals("transcription_ia:(λόγιος)", tinstance3.substituteFields("λόγιος"));
        
        // phrase search
        // field should be transcription
         StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, false, false, 0);
         assertEquals("transcription:(λόγιος)", tinstance4.substituteFields("λόγιος"));
         
        // metadata search - 
        // field should be metadata (for present)
         StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.METADATA, StringSearchFacet.SearchType.PHRASE, false, false, 0);
         assertEquals("metadata:(λόγιος)", tinstance5.substituteFields("λόγιος"));
        
        // translation search -
        // field should be translation
         StringSearchFacet.SearchConfiguration tinstance6 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TRANSLATION, StringSearchFacet.SearchType.PHRASE, false, false, 0);
         assertEquals("translation:(λόγιος)", tinstance6.substituteFields("λόγιος"));
         
        // all search -
        // field should be all
          StringSearchFacet.SearchConfiguration tinstance7 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.ALL, StringSearchFacet.SearchType.PHRASE, false, false, 0);
         assertEquals("all:(λόγιος)", tinstance7.substituteFields("λόγιος"));
         
        // if no caps and no marks
        // field should be transcription_ia
         StringSearchFacet.SearchConfiguration tinstance8 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, true, true, 0);
         assertEquals("transcription_ia:(λόγιος)", tinstance8.substituteFields("λόγιος"));
        
        // if no caps only
        // field should be transcription_ic
         StringSearchFacet.SearchConfiguration tinstance9 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, true, false, 0);
         assertEquals("transcription_ic:(λόγιος)", tinstance9.substituteFields("λόγιος"));        
        
        // if no marks only
        // field should be transcription_id
         StringSearchFacet.SearchConfiguration tinstance10 = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PHRASE, false, true, 0);
         assertEquals("transcription_id:(λόγιος)", tinstance10.substituteFields("λόγιος"));
         
        // USER-DEFINED SEARCHES
        
        // lem indicators need to be replaced with transcription_ia
        // pure
        StringSearchFacet.SearchConfiguration tinstance11 = testInstance.new SearchConfiguration("lem:λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, 0);
         assertEquals("transcription_ia:λόγιος", tinstance11.substituteFields("lem:λόγιος"));
         
         StringSearchFacet.SearchConfiguration tinstance12 = testInstance.new SearchConfiguration("lem:λόγιος AND lem:του", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, 0);
         assertEquals("transcription_ia:λόγιος AND transcription_ia:του", tinstance12.substituteFields("lem:λόγιος AND lem:του"));
        // mixed
         StringSearchFacet.SearchConfiguration tinstance13 = testInstance.new SearchConfiguration("lem:λόγιος AND transcription:του", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, 0);
         assertEquals("transcription_ia:λόγιος AND transcription:του", tinstance13.substituteFields("lem:λόγιος AND transcription:του"));
    
    }
    
    public void testLemmatizeWord(){
        
        // sanity check - returns false if no lemma requested
        
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, true, true, 0);
        ArrayList<String> testInput = new ArrayList<String>(Arrays.asList("λόγιος"));
        assertFalse(tinstance.lemmatizeWord(testInput, 0));
        
        
         // basic test - single instance
         StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("lem:λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, true, true, 0);
         testInput = new ArrayList<String>(Arrays.asList("λόγιος"));
         assertTrue(tinstance2.lemmatizeWord(testInput, 0));         
        
        // double instance
         StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("lem:λόγιος AND lem:προχειρισάμενοι", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, true, true, 0);
         testInput = new ArrayList<String>(Arrays.asList("λόγιος", "προχειρισάμενοι"));
         assertTrue(tinstance3.lemmatizeWord(testInput, 0));
         assertTrue(tinstance3.lemmatizeWord(testInput, 1));
         
        // mixed instance
        StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("lem:λόγιος AND προχειρισάμενοι", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, true, true, 0);
         testInput = new ArrayList<String>(Arrays.asList("λόγιος", "προχειρισάμενοι"));
         assertTrue(tinstance4.lemmatizeWord(testInput, 0));
         assertFalse(tinstance4.lemmatizeWord(testInput, 1));
        
        // repetitive instance
        StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("transcription_ngram_ia:λόγιος AND lem:λόγιος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, true, true, 0);
        testInput = new ArrayList<String>(Arrays.asList("λόγιος", "λόγιος"));
        assertFalse(tinstance5.lemmatizeWord(testInput, 0));
        assertTrue(tinstance5.lemmatizeWord(testInput, 1));
        
  
    }
    
    public void testTransformKeywords(){      
        
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, false, 0);

        // null check
        ArrayList<String> testOutput = tinstance.transformKeywords(null);
        assertEquals(0, testOutput.size());
                
        // lowercase if required
        
        StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, true, false, 0);
        ArrayList<String> capsInput = new ArrayList<String>(Arrays.asList("Stratiou", "Στρατιοῦ", "ΣΤΡΑΤΙΟΥ"));
        testOutput.clear();
        testOutput = tinstance2.transformKeywords(capsInput);
        assertEquals(3, testOutput.size());
        assertEquals("stratiou", testOutput.get(0));
        assertEquals("στρατιοῦ", testOutput.get(1));
        assertEquals("στρατιου", testOutput.get(2));
        
        
        // remove diacritics if required
        StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, true, 0);
        ArrayList<String> diaInput = new ArrayList<String>(Arrays.asList("Στρατηγὸς", "παραγγελίας", "σίλλὖ"));
        testOutput.clear();
        testOutput = tinstance3.transformKeywords(diaInput);
        assertEquals(3, testOutput.size());
        assertEquals("Στρατηγοσ", testOutput.get(0));
        assertEquals("παραγγελιασ", testOutput.get(1));
        assertEquals("σιλλυ", testOutput.get(2));
               
        // expand lemmas
        // both with html control input
        StringSearchFacet.SearchConfiguration tinstance6 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, true, true, 0);
        ArrayList<String> lemmaInput0 = new ArrayList<String>(Arrays.asList("λόγιος"));
        testOutput.clear();
        testOutput = tinstance6.transformKeywords(lemmaInput0);
        assertEquals(1, testOutput.size());
        assertTrue(testOutput.get(0).matches("\\((([^\\s]+?) OR )+[^\\s]+\\)"));
        // and in situ
        StringSearchFacet.SearchConfiguration tinstance7 = testInstance.new SearchConfiguration("lem:λύω", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, true, true, 0);
        ArrayList<String> lemmaInput1 = new ArrayList<String>(Arrays.asList("λύω"));
        testOutput.clear();
        testOutput = tinstance7.transformKeywords(lemmaInput1);
        assertEquals(1, testOutput.size());
        assertTrue(testOutput.get(0).matches("\\((([^\\s]+?) OR )+[^\\s]+\\)"));      
                
        // no lemma available should result in the string being left untransformed but bracketed
        StringSearchFacet.SearchConfiguration tinstance8 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, true, true, 0);
        ArrayList<String> lemmaInput2 = new ArrayList<String>(Arrays.asList("ψψζξ"));
        testOutput.clear();
        testOutput = tinstance6.transformKeywords(lemmaInput2);
        assertEquals(1, testOutput.size());
        assertEquals("(ψψζξ)", testOutput.get(0));
        
        // repeated terms in the query should not cause problems
        StringSearchFacet.SearchConfiguration tinstance9 = testInstance.new SearchConfiguration("lem:λόγος AND transcription_ngram_ia:λόγος AND transcription_ia:λόγος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, true, true, 0);
        ArrayList<String> repeatInput = new ArrayList<String>(Arrays.asList("λόγος", "λόγος", "λόγος"));
        testOutput.clear();
        testOutput = tinstance9.transformKeywords(repeatInput);
        assertEquals(3, testOutput.size());
        assertTrue(testOutput.get(0).matches("\\((([^\\s]+?) OR )+[^\\s]+\\)"));
        assertEquals("λογοσ", testOutput.get(1));
        assertEquals("λογοσ", testOutput.get(2));        
        
    }
    
    public void testTransformSearchString(){
        
        // sanity check - single substring passed in
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("λόγος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, false, 0);
        assertEquals("transcription_ngram_ia:(λόγοσ)", tinstance.transformSearchString());
        
        // search with operator processed correctly 
        StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("λόγος AND στρατηγὸς", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, false, 0);
        assertEquals("transcription_ngram_ia:(λόγοσ AND στρατηγὸσ)", tinstance2.transformSearchString());        
              
        // lemma expansions should be wrapped in brackets
         StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("λόγος", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, false, false, 0);
         assertTrue(tinstance3.transformSearchString().matches("transcription_ia:\\((([^\\s]+?) OR )+[^\\s]+\\)"));
               
        // complex searches as detailed in searchPatterns document
         StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("lem:ἀγαθός OR μητρικ", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, 0);
         assertTrue(tinstance4.transformSearchString().matches("transcription_ia:\\((([^\\s]+?) OR )+[^\\s]+\\) OR μητρικ"));
        
         StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("lem:ἀγαθός AND lem:στρατηγός", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, 0);
         assertTrue(tinstance5.transformSearchString().matches("transcription_ia:\\((([^\\s]+?) OR )+[^\\s]+\\) AND transcription_ia:\\((([^\\s]+?) OR )+[^\\s]+\\)"));
        
         StringSearchFacet.SearchConfiguration tinstance6 = testInstance.new SearchConfiguration("ἀγαθός AND στρατηγός", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.LEMMAS, false, false, 0);
         assertTrue(tinstance6.transformSearchString().matches("transcription_ia:\\(\\((([^\\s]+?) OR )+[^\\s]+\\) AND \\((([^\\s]+?) OR )+[^\\s]+\\)\\)"));
         
         // Proximity searches
         StringSearchFacet.SearchConfiguration tinstance7 = testInstance.new SearchConfiguration("\"των ανδρων\"", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PROXIMITY, false, false, 10);
         assertEquals("transcription:(\"των ανδρων\"~10)", tinstance7.transformSearchString());
         
         StringSearchFacet.SearchConfiguration tinstance8 = testInstance.new SearchConfiguration("\"των ανδρων\"~10", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, 0);
          assertEquals("\"των ανδρων\"~10", tinstance8.transformSearchString());
        
          // word-boundary escaping
         StringSearchFacet.SearchConfiguration tinstance9 = testInstance.new SearchConfiguration("των^ ^ανδρων", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, false, 0);
          assertEquals("transcription_ngram_ia:(των\\^ \\^ανδρων)", tinstance9.transformSearchString());
          
         // wildcards shouldn't cause a problem
         StringSearchFacet.SearchConfiguration tinstance10 = testInstance.new SearchConfiguration("η??υμενον", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, true, true, 0);
         assertEquals("transcription_ngram_ia:(η??υμενον)", tinstance10.transformSearchString());
 
          
    }
    
    public void testTransformProximitySearch(){
        
          StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.USER_DEFINED, false, false, 0);
          
          // sanity-check - pass correctly formatted qstrings untransformed
          String proxString = "\"των ανδρων\"~10";
          assertEquals(proxString, tinstance.transformProximitySearch(proxString));
          
          // ensure that space-insensitive
          proxString = "\"των ανδρων\"  ~   10";
          assertEquals("\"των ανδρων\"~10", tinstance.transformProximitySearch(proxString));
          
          // wrap in double-quotes if left unquoted or is single-quoted
          proxString = "των ανδρων~10";
          assertEquals("\"των ανδρων\"~10", tinstance.transformProximitySearch(proxString));
        
    }
    
    public void testSubstituteOperators(){
        
        // sanity-check: non-proximity searches returned untransformed
          StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.SUBSTRING, false, false, 0);
          String testString = "transcription:λογος";
          assertEquals(testString, tinstance.substituteOperators(testString));
                  
        // manual proximity searches are corrected 
          testString = "των ανδρων ~ 10";
          assertEquals("\"των ανδρων\"~10", tinstance.substituteOperators(testString));
          
        // html control proximity searches are added to
           StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("test", 0,  StringSearchFacet.SearchTarget.TEXT, StringSearchFacet.SearchType.PROXIMITY, false, false, 10);
           testString = "των ανδρων";
           assertEquals("\"των ανδρων\"~10", tinstance2.substituteOperators(testString));
        
        
    }
   
    
    public void testRegexSearch(){
        
        StringSearchFacet.RegexSearchConfiguration tinstance = testInstance.new RegexSearchConfiguration("regex:ψινα\\p{L}+");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        try{
            String regexString = tinstance.getSearchString();
            System.out.println(regexString);
        }
        catch(InternalQueryException iqe){
            
            System.out.println(iqe.getMessage());
            
        }
        System.out.print("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        
        
    }


}
