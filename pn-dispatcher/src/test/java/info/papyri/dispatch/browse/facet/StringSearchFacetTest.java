package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchConfiguration;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
        mockParams.put("target", new String[]{ StringSearchFacet.SearchTarget.TEXT.name()});
        mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase(), new String[]{"on"});
        mockParams.put(StringSearchFacet.SearchOption.NO_MARKS.name().toLowerCase(), new String[]{"on"});
        
        HashMap<Integer, ArrayList<StringSearchFacet.SearchConfiguration>> configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        StringSearchFacet.SearchConfiguration config = configs.get(0).get(0);
        assertEquals("orator", config.getRawString());
        assertEquals(StringSearchFacet.SearchType.SUBSTRING, config.getSearchType());
        assertEquals(StringSearchFacet.SearchTarget.TEXT, config.getSearchTarget());
        assertTrue(config.getIgnoreCaps());
        assertTrue(config.getIgnoreMarks());
        
        mockParams.clear();
        
        mockParams.put("STRING2", new String[]{"τοῦ και"});
        mockParams.put("target2", new String[]{StringSearchFacet.SearchTarget.METADATA.name()});
        
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        
        assertEquals(1, configs.size());
        StringSearchFacet.SearchConfiguration config2 = configs.get(2).get(0);
        assertEquals("τοῦ και", config2.getRawString());
        assertEquals(StringSearchFacet.SearchTarget.METADATA, config2.getSearchTarget());
        assertTrue(config2.getIgnoreCaps());
        assertTrue(config2.getIgnoreMarks());
        
        
        // missing keyword, target parameters should cause the entire SC object not to be instantiated
        mockParams.clear();
        mockParams.put("STRING3", new String[]{"apuleius"});
        mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase() + "3", new String[]{"on"});
        mockParams.put(StringSearchFacet.SearchOption.NO_MARKS.name().toLowerCase() + "3", new String[]{"on"});
        
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(0, configs.size());
        
        // malformed search type or target parameters should cause the entire sc object not to be instantiated
        mockParams.clear();
        mockParams.put("STRING5", new String[]{"querunculus"});
        mockParams.put("target5", new String[]{"DUMMY_VALUE2"}); 
        
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(0, configs.size());
        
        // multiple parameter values are ignored
        
        mockParams.clear();
        mockParams.put("STRING4",new String[]{"cupid", "psyche"});
        mockParams.put("target4", new String[]{StringSearchFacet.SearchTarget.ALL.name().toLowerCase(), StringSearchFacet.SearchTarget.TEXT.name().toLowerCase()});
        configs.clear();
        configs = testInstance.pullApartParams(mockParams);
        assertEquals(1, configs.size());
        StringSearchFacet.SearchConfiguration config3 = configs.get(4).get(0);
        assertEquals("cupid", config3.getRawString());
        assertEquals(StringSearchFacet.SearchType.SUBSTRING, config3.getSearchType());
        assertEquals(StringSearchFacet.SearchTarget.ALL, config3.getSearchTarget());
        
        // n submitted clauses results in n search configurations being created
        
        configs.clear();;
        mockParams.clear();
        
        mockParams.put("STRING6", new String[]{"((και AND ουκ)¤(\"ο στρατηγος\" THEN \"ο καισαροσ\")~5words¤(αι# OR #ου)¤(νερ NEAR τι)~10chars¤(LEX λυω)"});
        mockParams.put("target6", new String[]{"TEXT"});
        mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase() + "6", new String[]{"on"});
        configs = testInstance.pullApartParams(mockParams);
        ArrayList<SearchConfiguration> firstConfig = configs.get(6);
        assertEquals(1, configs.size());
        assertEquals(5, firstConfig.size());
        assertTrue(firstConfig.get(0).getIgnoreCaps());
        
        // Search type should default to substring
        assertEquals(StringSearchFacet.SearchType.SUBSTRING, firstConfig.get(0).getSearchType());
        assertEquals(StringSearchFacet.SearchType.PHRASE, firstConfig.get(1).getSearchType());
        assertEquals(StringSearchFacet.SearchType.SUBSTRING, firstConfig.get(2).getSearchType());
        assertEquals(StringSearchFacet.SearchType.REGEX, firstConfig.get(3).getSearchType());
        assertEquals(StringSearchFacet.SearchType.LEMMA, firstConfig.get(4).getSearchType());

       
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
        
        StringSearchFacet.SearchConfiguration testInnerInstance = testInstance.new SearchConfiguration("λυω", StringSearchFacet.SearchTarget.TEXT, false, false);
        
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
 
    public void testInterpolateProximitySyntax(){
                
        // words-as-unit: postfix expanded syntax swapped for infix terse syntax
        
        // THEN
        
        String postThen = "(και THEN ουκ)~15words";
        String inThen = "(και 15w ουκ)";
        StringSearchFacet.SearchConfiguration sc = testInstance.new SearchConfiguration(postThen, StringSearchFacet.SearchTarget.TEXT, true, true);
        assertEquals(inThen, sc.interpolateProximitySyntax(postThen));
        
        // NEAR
        
        String postNear = "(και NEAR ουκ)~5words";
        String inNear = "(και 5n ουκ)";
        StringSearchFacet.SearchConfiguration sc2 = testInstance.new SearchConfiguration(postNear, StringSearchFacet.SearchTarget.TEXT, true, true);
        assertEquals(inNear, sc2.interpolateProximitySyntax(postNear));
        
        // chars-as-unit: postfix expanded syntax swapped for regex syntax
        
        // THEN
        
        String postThenChar = "(και THEN ουκ)~7chars";
        String regxThenChar = "(^.*και.{1,7}ουκ.*$)";
        StringSearchFacet.SearchConfiguration sc3 = testInstance.new SearchConfiguration(postThenChar, StringSearchFacet.SearchTarget.TEXT, true, true);
        assertEquals(regxThenChar, sc3.interpolateProximitySyntax(postThenChar));
        
        // NEAR
        
        String postNearChar = "(και NEAR ουκ)~12chars";
        String regxNearChar = "((^.*και.{1,12}ουκ.*$)|(^.*ουκ.{1,12}και.*$))";
        StringSearchFacet.SearchConfiguration sc4 = testInstance.new SearchConfiguration(postNearChar, StringSearchFacet.SearchTarget.TEXT, true, true);
        assertEquals(regxNearChar, sc4.interpolateProximitySyntax(postNearChar));
        
        
        
    }
    
    public void testSubstituteTerms(){
        
        // sanity check - no transformation req'd
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("λόγιος", StringSearchFacet.SearchTarget.TEXT, false, false);
        ArrayList<String> initialInput = new ArrayList<String>(Arrays.asList("λόγιος"));
        ArrayList<String> transformedInput = new ArrayList<String>(Arrays.asList("λόγιος"));
        assertEquals("λόγιος", tinstance.substituteTerms(initialInput, transformedInput));
                
        // with field
        StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("transcription:λόγιος", StringSearchFacet.SearchTarget.TEXT, false, false);
        initialInput = new ArrayList<String>(Arrays.asList("λόγιος"));
        transformedInput = new ArrayList<String>(Arrays.asList("λόγιοσ"));
        assertEquals("transcription:λόγιοσ", tinstance2.substituteTerms(initialInput, transformedInput));
        
        // pseudo-lemmatised
        StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("LEX λύω AND του", StringSearchFacet.SearchTarget.TEXT, false, false);
        initialInput = new ArrayList<String>(Arrays.asList("λύω", "του"));
        transformedInput = new ArrayList<String>(Arrays.asList("(λυω OR λυεισ OR λυε)", "του"));
        assertEquals("LEX (λυω OR λυεισ OR λυε) AND του", tinstance3.substituteTerms(initialInput, transformedInput));
        
        // repeated
        StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("transcription:λύω AND LEX λύω AND transcription_ia:λυεις", StringSearchFacet.SearchTarget.TEXT, false, false);
        initialInput = new ArrayList<String>(Arrays.asList("λύω", "λύω", "λυεις"));
        transformedInput = new ArrayList<String>(Arrays.asList("λύω", "(λυω OR λυεισ OR λυε)", "λυεισ"));
        assertEquals("transcription:λύω AND LEX (λυω OR λυεισ OR λυε) AND transcription_ia:λυεισ", tinstance4.substituteTerms(initialInput, transformedInput));  
        
        // with wildcards
        StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("η??υμενον", StringSearchFacet.SearchTarget.TEXT, true, true);
        initialInput = new ArrayList<String>(Arrays.asList("η??υμενον"));
        transformedInput = new ArrayList<String>(Arrays.asList("η??υμενον"));
        assertEquals("η??υμενον", tinstance5.substituteTerms(initialInput, transformedInput));
        
        // with regex
        StringSearchFacet.SearchConfiguration tinstance6 = testInstance.new SearchConfiguration("REGEX κα.{1,5}\\sουκ", StringSearchFacet.SearchTarget.TEXT, true, true);
        initialInput = new ArrayList<String>(Arrays.asList("REGEX κα.{1,5}\\sουκ"));
        transformedInput = new ArrayList<String>(Arrays.asList("REGEX κα.{1,5}\\sουκ"));
        assertEquals("REGEX κα.{1,5}\\sουκ", tinstance6.substituteTerms(initialInput, transformedInput));
 
        
    }
    
    public void testParseForSearchType(){
        
        // searches on TEXT
        
        // default should be substring
        
        String substringSearch = "(και)";
        StringSearchFacet.SearchConfiguration tinstance1 = testInstance.new SearchConfiguration(substringSearch, StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(StringSearchFacet.SearchType.SUBSTRING, tinstance1.parseForSearchType());
        
        // hash-marks indicate substring
        
        String substringSearch2 = "(και# AND #ουκ)";
        StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration(substringSearch2, StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(StringSearchFacet.SearchType.SUBSTRING, tinstance2.parseForSearchType());
        // quotes containing whitespace w/o hash-marks indicate phrase
        
        String phraseSearch = "(\"και AND ουκ\")";
        StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration(phraseSearch, StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(StringSearchFacet.SearchType.PHRASE, tinstance3.parseForSearchType());
        // LEX keyword results in lemma search
        
        String lexSearch = "(LEX στρατιοῦ)";
        StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration(lexSearch, StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(StringSearchFacet.SearchType.LEMMA, tinstance4.parseForSearchType());
        // REGEX keyword results in regex search
        
        String regexSearch = "(REGEX και\\so.{1,3}κ)";
        StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration(regexSearch, StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(StringSearchFacet.SearchType.REGEX, tinstance5.parseForSearchType());
        // proximity search with word units results in appropriate kind of search
        // (i) default to substring
        
        String subsProxSearch = "(και THEN ουκ)~10words";
        StringSearchFacet.SearchConfiguration tinstance6 = testInstance.new SearchConfiguration(subsProxSearch, StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(StringSearchFacet.SearchType.SUBSTRING, tinstance6.parseForSearchType());
        
        // (ii) with whitespace in quotes, phrase
        
        String phraseProxSearch = "(\"και ουκ\" THEN \"ουκ και\")~10words";
        StringSearchFacet.SearchConfiguration tinstance7 = testInstance.new SearchConfiguration(phraseProxSearch, StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(StringSearchFacet.SearchType.PHRASE, tinstance7.parseForSearchType());
        
        // proximity search with char units results in regex search
        
        String charProxSearch = "(και NEAR ουκ)~10chars";
        StringSearchFacet.SearchConfiguration tinstance8 = testInstance.new SearchConfiguration(charProxSearch, StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(StringSearchFacet.SearchType.REGEX, tinstance8.parseForSearchType());
        
        // searches on METADATA
        
        // these should always be phrase searches with caps and diacritcs ignored
        
        String metaSearch = "sheep";
        StringSearchFacet.SearchConfiguration tinstance9 = testInstance.new SearchConfiguration(metaSearch, StringSearchFacet.SearchTarget.METADATA, true, true);
        assertEquals(StringSearchFacet.SearchType.PHRASE, tinstance9.getSearchType());
        assertTrue(tinstance9.getIgnoreCaps());
        assertTrue(tinstance9.getIgnoreMarks());
        
        // searches on TRANSLATION
        // these should always be phrase searches with caps and diacritics ignored
        String translation = "sheep";
        StringSearchFacet.SearchConfiguration tinstance10 = testInstance.new SearchConfiguration(translation, StringSearchFacet.SearchTarget.TRANSLATION, true, false);
        assertEquals(StringSearchFacet.SearchType.PHRASE, tinstance10.getSearchType());
        assertTrue(tinstance10.getIgnoreCaps());
        assertTrue(tinstance10.getIgnoreMarks());
    }
    
    public void testParseForProximityMetrics(){
        
        // correct distinctions between near/then, words/chars drawn
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("(και THEN ουκ)~10words", StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(tinstance.getProxCount(), 10);
        assertEquals(tinstance.getProxUnit(), StringSearchFacet.SearchUnit.WORDS);
        
        StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("(και THEN ουκ)~5chars", StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(tinstance2.getProxCount(), 5);
        assertEquals(tinstance2.getProxUnit(), StringSearchFacet.SearchUnit.CHARS);
        
        // malformed metrics result in discarded proximity values
        StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("(και THEN ουκ)~999words", StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(tinstance3.getProxCount(), 0);
        assertNull(tinstance3.getProxUnit());
        
        StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("(και THEN ουκ)~10chras", StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals(tinstance4.getProxCount(), 0);
        assertNull(tinstance4.getProxUnit());
    }
    
    public void testSubstituteFields(){
        
        // sanity check - no transformation required
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("transcription:λόγιος", StringSearchFacet.SearchTarget.USER_DEFINED, false, false);
        assertEquals("transcription:λόγιοσ", tinstance.substituteFields("transcription:λόγιοσ"));
        
        // Default to substring search (ie, to transcription_ngram_ia)
         StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("λόγιος", StringSearchFacet.SearchTarget.TEXT, false, false);
         assertEquals("transcription_ngram_ia:(λόγιοσ)", tinstance2.substituteFields("λόγιοσ"));
        
        // phrase search
        // field should be transcription
         StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("\"λόγιος τουσ\"", StringSearchFacet.SearchTarget.TEXT, false, false);
         assertEquals("transcription:(\"λόγιος τουσ\")", tinstance4.substituteFields("\"λόγιος τουσ\""));
         
        // metadata search - 
        // field should be metadata (for present)
         StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("λόγιος", StringSearchFacet.SearchTarget.METADATA, false, false);
         assertEquals("metadata:(λόγιος)", tinstance5.substituteFields("λόγιος"));
        
        // translation search -
        // field should be translation
         StringSearchFacet.SearchConfiguration tinstance6 = testInstance.new SearchConfiguration("λόγιος", StringSearchFacet.SearchTarget.TRANSLATION, false, false);
         assertEquals("translation:(λόγιος)", tinstance6.substituteFields("λόγιος"));
         
        // if no caps and no marks and phrase search
        // field should be transcription_ia
         StringSearchFacet.SearchConfiguration tinstance8 = testInstance.new SearchConfiguration("\"λόγιος τουσ\"", StringSearchFacet.SearchTarget.TEXT, true, true);
         assertEquals("transcription_ia:(\"λόγιος τουσ\")", tinstance8.substituteFields("\"λόγιος τουσ\""));
        
        // if no caps only
        // field should be transcription_ic
         StringSearchFacet.SearchConfiguration tinstance9 = testInstance.new SearchConfiguration("\"λόγιος τουσ\"", StringSearchFacet.SearchTarget.TEXT, true, false);
         assertEquals("transcription_ic:(\"λόγιος τουσ\")", tinstance9.substituteFields("\"λόγιος τουσ\""));        
        
        // if no marks only
        // field should be transcription_id
         StringSearchFacet.SearchConfiguration tinstance10 = testInstance.new SearchConfiguration("\"λόγιος τουσ\"", StringSearchFacet.SearchTarget.TEXT, false, true);
         assertEquals("transcription_id:(\"λόγιος τουσ\")", tinstance10.substituteFields("\"λόγιος τουσ\""));
         
        // USER-DEFINED SEARCHES
        
        // lem indicators need to be replaced with transcription_ia
        // pure
      /*  StringSearchFacet.SearchConfiguration tinstance11 = testInstance.new SearchConfiguration("LEM λόγιος", StringSearchFacet.SearchTarget.TEXT, false, false);
         assertEquals("transcription_ia:λόγιος", tinstance11.substituteFields("LEM λόγιος"));
         
         StringSearchFacet.SearchConfiguration tinstance12 = testInstance.new SearchConfiguration("LEM λόγιος AND LEM του", StringSearchFacet.SearchTarget.TEXT, false, false);
         assertEquals("transcription_ia:λόγιος AND transcription_ia:του", tinstance12.substituteFields("LEM λόγιος AND LEM του"));
        // mixed
         StringSearchFacet.SearchConfiguration tinstance13 = testInstance.new SearchConfiguration("LEM λόγιος AND transcription:του", StringSearchFacet.SearchTarget.TEXT, false, false);
         assertEquals("transcription_ia:λόγιος AND transcription:του", tinstance13.substituteFields("LEM λόγιος AND transcription:του"));
    */
    }
    
   /* public void testLemmatizeWord(){
        
        // sanity check - returns false if no lemma requested
        
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("λόγιος", StringSearchFacet.SearchTarget.TEXT, true, true);
        ArrayList<String> testInput = new ArrayList<String>(Arrays.asList("λόγιος"));
        assertFalse(tinstance.lemmatizeWord(testInput, 0));
        
        
         // basic test - single instance
         StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("lem:λόγιος", StringSearchFacet.SearchTarget.TEXT, true, true);
         testInput = new ArrayList<String>(Arrays.asList("λόγιος"));
         assertTrue(tinstance2.lemmatizeWord(testInput, 0));         
        
        // double instance
         StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("lem:λόγιος AND lem:προχειρισάμενοι", StringSearchFacet.SearchTarget.TEXT, true, true);
         testInput = new ArrayList<String>(Arrays.asList("λόγιος", "προχειρισάμενοι"));
         assertTrue(tinstance3.lemmatizeWord(testInput, 0));
         assertTrue(tinstance3.lemmatizeWord(testInput, 1));
         
        // mixed instance
        StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("lem:λόγιος AND προχειρισάμενοι", StringSearchFacet.SearchTarget.TEXT, true, true);
         testInput = new ArrayList<String>(Arrays.asList("λόγιος", "προχειρισάμενοι"));
         assertTrue(tinstance4.lemmatizeWord(testInput, 0));
         assertFalse(tinstance4.lemmatizeWord(testInput, 1));
        
        // repetitive instance
        StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("transcription_ngram_ia:λόγιος AND lem:λόγιος", StringSearchFacet.SearchTarget.TEXT, true, true);
        testInput = new ArrayList<String>(Arrays.asList("λόγιος", "λόγιος"));
        assertFalse(tinstance5.lemmatizeWord(testInput, 0));
        assertTrue(tinstance5.lemmatizeWord(testInput, 1));
        
  
    }*/
    
    public void testTransformKeywords(){      
        
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("test", StringSearchFacet.SearchTarget.TEXT, false, false);

        // null check
        ArrayList<String> testOutput = tinstance.transformKeywords(null);
        assertEquals(0, testOutput.size());
                
        // lowercase if required
        
        StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("test", StringSearchFacet.SearchTarget.TEXT, true, false);
        ArrayList<String> capsInput = new ArrayList<String>(Arrays.asList("Stratiou", "Στρατιοῦ", "ΣΤΡΑΤΙΟΥ"));
        testOutput.clear();
        testOutput = tinstance2.transformKeywords(capsInput);
        assertEquals(3, testOutput.size());
        assertEquals("stratiou", testOutput.get(0));
        assertEquals("στρατιοῦ", testOutput.get(1));
        assertEquals("στρατιου", testOutput.get(2));
        
        
        // remove diacritics if required
        StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("test", StringSearchFacet.SearchTarget.TEXT, false, true);
        ArrayList<String> diaInput = new ArrayList<String>(Arrays.asList("Στρατηγὸς", "παραγγελίας", "σίλλὖ"));
        testOutput.clear();
        testOutput = tinstance3.transformKeywords(diaInput);
        assertEquals(3, testOutput.size());
        assertEquals("Στρατηγοσ", testOutput.get(0));
        assertEquals("παραγγελιασ", testOutput.get(1));
        assertEquals("σιλλυ", testOutput.get(2));
               
        // expand lemmas

        StringSearchFacet.SearchConfiguration tinstance7 = testInstance.new SearchConfiguration("LEX λύω", StringSearchFacet.SearchTarget.TEXT, true, true);
        ArrayList<String> lemmaInput1 = new ArrayList<String>(Arrays.asList("λύω"));
        testOutput.clear();
        testOutput = tinstance7.transformKeywords(lemmaInput1);
        assertEquals(1, testOutput.size());
        assertTrue(testOutput.get(0).matches("\\((([^\\s]+?) OR )+[^\\s]+\\)"));      
                
        // no lemma available should result in the string being left untransformed but bracketed
        StringSearchFacet.SearchConfiguration tinstance8 = testInstance.new SearchConfiguration("LEX ψψζξ", StringSearchFacet.SearchTarget.TEXT, true, true);
        ArrayList<String> lemmaInput2 = new ArrayList<String>(Arrays.asList("ψψζξ"));
        testOutput.clear();
        testOutput = tinstance8.transformKeywords(lemmaInput2);
        assertEquals(1, testOutput.size());
        assertEquals("(ψψζξ)", testOutput.get(0));
        
        // repeated terms in the query should not cause problems
        StringSearchFacet.SearchConfiguration tinstance9 = testInstance.new SearchConfiguration("LEX λόγος AND transcription_ngram_ia:λόγος AND transcription_ia:λόγος", StringSearchFacet.SearchTarget.TEXT, true, true);
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
        StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("λόγος", StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals("transcription_ngram_ia:(λόγοσ)", tinstance.transformSearchString());
        
        // search with operator processed correctly 
        StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("λόγος AND στρατηγὸς", StringSearchFacet.SearchTarget.TEXT, false, false);
        assertEquals("transcription_ngram_ia:(λόγοσ AND στρατηγὸσ)", tinstance2.transformSearchString());        
              
        // lemma expansions should be wrapped in brackets
         StringSearchFacet.SearchConfiguration tinstance3 = testInstance.new SearchConfiguration("LEX λόγος", StringSearchFacet.SearchTarget.TEXT, false, false);
         assertTrue(tinstance3.transformSearchString().matches("transcription_ia:\\((([^\\s]+?) OR )+[^\\s]+\\)"));
               
        // complex searches as detailed in searchPatterns document
         StringSearchFacet.SearchConfiguration tinstance4 = testInstance.new SearchConfiguration("LEX ἀγαθός OR μητρικ", StringSearchFacet.SearchTarget.TEXT, false, false);
                          System.out.println(tinstance4.transformSearchString());

         assertTrue(tinstance4.transformSearchString().matches("transcription_ia:\\((([^\\s]+?) OR )+[^\\s]+\\) OR μητρικ"));
        
         StringSearchFacet.SearchConfiguration tinstance5 = testInstance.new SearchConfiguration("LEX ἀγαθός AND LEX στρατηγός", StringSearchFacet.SearchTarget.TEXT, false, false);

         assertTrue(tinstance5.transformSearchString().matches("transcription_ia:\\(\\((([^\\s]+?) OR )+[^\\s]+\\) AND transcription_ia:\\((([^\\s]+?) OR )+[^\\s]+\\)\\)"));
        
          // word-boundary escaping
         StringSearchFacet.SearchConfiguration tinstance9 = testInstance.new SearchConfiguration("των^ ^ανδρων", StringSearchFacet.SearchTarget.TEXT, false, false);
          assertEquals("transcription_ngram_ia:(των\\^ \\^ανδρων)", tinstance9.transformSearchString());
          
         // wildcards shouldn't cause a problem
         StringSearchFacet.SearchConfiguration tinstance10 = testInstance.new SearchConfiguration("η??υμενον", StringSearchFacet.SearchTarget.TEXT, true, true);
         assertEquals("transcription_ngram_ia:(η??υμενον)", tinstance10.transformSearchString());
 
         // regex searches
         StringSearchFacet.SearchConfiguration tinstance11 = testInstance.new SearchConfiguration("REGEX και.{1,5}\\sουκ", StringSearchFacet.SearchTarget.TEXT, true, true);
         assertEquals("{!regexp qf=\"untokenized_ia\" cache=false}και.{1,5}\\sουκ", tinstance11.transformSearchString());
    }
    
    /*public void testSubstituteOperators(){
        
        // sanity-check: non-proximity searches returned untransformed
          StringSearchFacet.SearchConfiguration tinstance = testInstance.new SearchConfiguration("test", StringSearchFacet.SearchTarget.TEXT, false, false);
          String testString = "transcription:λογος";
          assertEquals(testString, tinstance.interpolateProximitySyntax(testString));
                  
        // manual proximity searches are corrected 
          testString = "των ανδρων ~ 10";
          assertEquals("\"των ανδρων\"~10", tinstance.interpolateProximitySyntax(testString));
          
        // html control proximity searches are added to
           StringSearchFacet.SearchConfiguration tinstance2 = testInstance.new SearchConfiguration("test", StringSearchFacet.SearchTarget.TEXT, false, false);
           testString = "των ανδρων";
           assertEquals("\"των ανδρων\"~10", tinstance2.interpolateProximitySyntax(testString));
        
        
    }*/
   
    


}
        
    
    

