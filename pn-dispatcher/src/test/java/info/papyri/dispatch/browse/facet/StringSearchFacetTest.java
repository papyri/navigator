package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchClause;
import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchTerm;
import info.papyri.dispatch.browse.facet.customexceptions.MalformedProximitySearchException;
import info.papyri.dispatch.browse.facet.customexceptions.MismatchedBracketException;
import info.papyri.dispatch.browse.facet.customexceptions.StringSearchParsingException;
import java.util.ArrayList;
import java.util.HashMap;
import junit.framework.TestCase;



/**
 *
 * @author thill
 */
public class StringSearchFacetTest extends TestCase {
    
    private StringSearchFacet testInstance;
        
    public StringSearchFacetTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testInstance = new StringSearchFacet();    

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
        try{
            
            mockParams.put("STRING", new String[]{"orator"});
            mockParams.put("target", new String[]{ StringSearchFacet.SearchTarget.TEXT.name()});
            mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase(), new String[]{"on"});
            mockParams.put(StringSearchFacet.SearchOption.NO_MARKS.name().toLowerCase(), new String[]{"on"});

            HashMap<Integer, ArrayList<StringSearchFacet.SearchClause>> configs = testInstance.pullApartParams(mockParams);
            assertEquals(1, configs.size());
            StringSearchFacet.SearchClause config = configs.get(0).get(0);
            assertEquals("orator", config.getOriginalString());
            assertEquals(StringSearchFacet.SearchType.SUBSTRING, config.parseForSearchType());
            assertEquals(StringSearchFacet.SearchTarget.TEXT, config.getSearchTarget());
            assertTrue(config.getIgnoreCaps());
            assertTrue(config.getIgnoreMarks());

            mockParams.clear();

            mockParams.put("STRING2", new String[]{"Τοῦ Και"});
            mockParams.put("target2", new String[]{StringSearchFacet.SearchTarget.METADATA.name()});

            configs.clear();
            configs = testInstance.pullApartParams(mockParams);

            assertEquals(1, configs.size());
            StringSearchFacet.SearchClause config2 = configs.get(2).get(0);
            assertEquals("Τοῦ Και", config2.getOriginalString());
            assertEquals("τοῦ και", config2.buildTransformedString());
            assertEquals(StringSearchFacet.SearchTarget.METADATA, config2.getSearchTarget());


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
            mockParams.put("target4", new String[]{StringSearchFacet.SearchTarget.TEXT.name().toLowerCase(), StringSearchFacet.SearchTarget.TEXT.name().toLowerCase()});
            configs.clear();
            configs = testInstance.pullApartParams(mockParams);
            assertEquals(1, configs.size());
            StringSearchFacet.SearchClause config3 = configs.get(4).get(0);
            assertEquals("cupid", config3.getOriginalString());
            assertEquals(StringSearchFacet.SearchType.SUBSTRING, config3.parseForSearchType());
            assertEquals(StringSearchFacet.SearchTarget.TEXT, config3.getSearchTarget());

            // n submitted clauses results in n search configurations being created

            configs.clear();;
            mockParams.clear();

            mockParams.put("STRING6", new String[]{"(και AND ουκ)¤(\"ο στρατηγος\" THEN \"ο καισαροσ\")~5words¤(αι# OR #ου)¤(νερ NEAR τι)~10chars¤(LEX λυω)"});

            mockParams.put("target6", new String[]{"TEXT"});
            mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase() + "6", new String[]{"on"});
            configs = testInstance.pullApartParams(mockParams);
            ArrayList<StringSearchFacet.SearchClause> firstConfig = configs.get(6);
            assertEquals(1, configs.size());
            assertEquals(5, firstConfig.size());
            assertTrue(firstConfig.get(0).getIgnoreCaps()); 

            // Search type should default to substring
            assertEquals(StringSearchFacet.SearchType.SUBSTRING, firstConfig.get(0).parseForSearchType());
            assertEquals(StringSearchFacet.SearchType.PROXIMITY, firstConfig.get(1).parseForSearchType());
            assertEquals(StringSearchFacet.SearchType.SUBSTRING, firstConfig.get(2).parseForSearchType());
            assertEquals(StringSearchFacet.SearchType.REGEX, firstConfig.get(3).parseForSearchType());
            assertEquals(StringSearchFacet.SearchType.LEMMA, firstConfig.get(4).parseForSearchType());
            
            mockParams.clear();
            configs.clear();
            mockParams.put("STRING7", new String[]{"REGEX και.{1,5}\\b"});
            mockParams.put("target7", new String[]{"TEXT"});
            mockParams.put(StringSearchFacet.SearchOption.NO_CAPS.name().toLowerCase() + "7", new String[]{"on"});
            mockParams.put(StringSearchFacet.SearchOption.NO_MARKS.name().toLowerCase() + "7", new String[]{"on"});
            configs = testInstance.pullApartParams(mockParams);
            ArrayList<StringSearchFacet.SearchClause> sevenConfig = configs.get(7);
            assertEquals(1, sevenConfig.size());
            StringSearchFacet.SearchClause regex = sevenConfig.get(0);


        }
        catch(Exception e){
            
            fail("Exception erroneously thrown in pullApartParams test: " + e.getMessage());
            
        }

    }
    
    public void testGetFacetConstraints(){
        
        String testString1 = FacetParam.STRING.name();
        ArrayList<String> facetConstraints = testInstance.getFacetConstraints(testString1);
        assertEquals("0", facetConstraints.get(0));
        
        String testString2 = FacetParam.STRING.name() + "12";
        ArrayList<String> facetConstraints2 = testInstance.getFacetConstraints(testString2);
        assertEquals("12", facetConstraints2.get(0));
         
    } 
    
    public void testSwapInProxperators(){
        
       StringSearchFacet.SearchClauseFactory s = testInstance.CLAUSE_FACTORY;
        
        try{
            
            // sanity check - no search terms returns string unchanged
            String prox0 = "(kai AND ouk)";
            String test0 = "(kai AND ouk)";
            assertEquals(test0, s.swapInProxperators(prox0));
            
            // search type THEN swapped in as 'w' and unit type 'words' eliminated
            String prox1 = "(kai THEN ouk)~15words";
            String test1 = "(kai 15w ouk)"; 
            assertEquals(test1, s.swapInProxperators(prox1));


            // search type NEAR swapped in as 'n' and unit type 'chars' retained as 'c'
            String prox2 = "(kai NEAR ouk)~15chars";
            String test2 = "(kai 15nc ouk)";
            assertEquals(test2, s.swapInProxperators(prox2));

            // malformed unit defaults to 'words' (i.e. eliminated)
            String prox3 = "(kai THEN ouk)~15whars";
            assertEquals(test1, s.swapInProxperators(prox3));
            // absent unit defaults to 'words'
            String prox4 = "(kai THEN ouk)~15";
            assertEquals(test1, s.swapInProxperators(prox4));

            // absent brackets dealt with correctly
            String prox5 = "kai THEN ouk~15";
            String test5 = "kai 15w ouk";
            assertEquals(test5, s.swapInProxperators(prox5));

            // internal brackets dealt with correctly
            String prox6 = "(kai THEN (kai OR ouk))~17words";
            String test6 = "(kai 17w (kai OR ouk))";
            assertEquals(test6, s.swapInProxperators(prox6));
                 
            // absent metrics default to one word
            String prox8 = "(kai THEN ouk)";
            String test8 = "(kai 1w ouk)";
            assertEquals(test8, s.swapInProxperators(prox8));
        
        }
        catch(MalformedProximitySearchException mpse){
            
            fail("MalformedProximitySearchException wrongly thrown in proxperator test");
            
        }
        catch(MismatchedBracketException mbe){
            
            fail("MismatchedBracketException wrongly thrown in proxperator test");
            
        }
        // malformed or absent proximity operator throws MalformedProximitySearchException
        try{
            
            String prox8 = "(kai AND ouk)~15chars";
            s.swapInProxperators(prox8);
            fail("Proxperator swap should have thrown MalformedProximitySearchException");
            
            
        } catch(MalformedProximitySearchException mpse){}
        catch(MismatchedBracketException mbe){ 
            
            fail("MismatchedBracketException wrongly thrown in proxperator test");
        }
        
       
    }

    
    public void testBuildSearchClauses(){
        
        StringSearchFacet.SearchClauseFactory f = testInstance.CLAUSE_FACTORY;
        StringSearchFacet.SearchTarget t = StringSearchFacet.SearchTarget.TEXT;
        Boolean caps = true;
        Boolean marks = false;
        
        try{

            // sanity check - no terms yields null
            String search1 = "";
            ArrayList<StringSearchFacet.SearchClause> clauses = f.buildSearchClauses(search1, t, caps, marks);
            assertNull(clauses);

            // sanity check - single term 
            String search2 = "kai";
            ArrayList<StringSearchFacet.SearchClause> clauses1 = f.buildSearchClauses(search2, t, caps, marks);
            StringSearchFacet.SearchClause kaiClause = clauses1.get(0);
            assertEquals("kai", kaiClause.getOriginalString());
            assertTrue(kaiClause instanceof StringSearchFacet.SearchTerm);

            // sanity check - 4 terms 
            String search3 = "kai ouk eleutheria kaisaros";
            ArrayList<StringSearchFacet.SearchClause> clauses2 = f.buildSearchClauses(search3, t, caps, marks);
            assertEquals(4, clauses2.size());
            StringSearchFacet.SearchClause eleuClause = clauses2.get(2);
            assertEquals("eleutheria", eleuClause.getOriginalString());
            assertTrue(eleuClause instanceof StringSearchFacet.SearchTerm);

            // quote-delimited terms resolve as a single term
            String search4 = "\"kai ouk\" \"eleutheria kaisaros\"";
            ArrayList<StringSearchFacet.SearchClause> clauses3 = f.buildSearchClauses(search4, t, caps, marks);
            assertEquals(2, clauses3.size());
            StringSearchFacet.SearchClause oukClause = clauses3.get(0);
            assertEquals("\"kai ouk\"", oukClause.getOriginalString());
            assertTrue(oukClause instanceof StringSearchFacet.SearchTerm);

            // whitespace shouldn't make a difference
            String search6 = "kai     AND  ouk";
            ArrayList<StringSearchFacet.SearchClause> clauses5 = f.buildSearchClauses(search6, t, caps, marks);
            assertEquals(3, clauses5.size());
            StringSearchFacet.SearchClause andClause = clauses5.get(1);
            assertEquals("AND", andClause.getOriginalString());
            assertTrue(andClause instanceof StringSearchFacet.SearchTerm);
         
        
        } catch(StringSearchParsingException sspe){
            
            fail("StringSearchParsingException erroneously thrown in breakIntoComponents test: " + sspe.getMessage());
            
        }
        catch(Exception e){
            
            fail("Exception erroneously thrown in breakIntoComponents test:" + e.getMessage());
            
        }
        
        // non-matching brackets cause MismatchedBracketException to be thrown
        // TODO: will need to check for character-escaping?
        
        String search8 = "(kai and (ouk NOT ou)";
        try{
            
            ArrayList<StringSearchFacet.SearchClause> clauses8 = f.buildSearchClauses(search8, t, caps, marks);
            fail("MismatchedBracketException not thrown on test8");
            
        } catch(MismatchedBracketException mbe){}
         catch(StringSearchParsingException sspe){
            
            fail("StringSearchParsingException erroneously thrown in breakIntoComponents test: " + sspe.getMessage());
            
        }
        catch(Exception e){
            
            fail("Exception erroneously thrown in breakIntoComponents test:" + e.getMessage());
            
        }
        
          String search9 = "(kai and (ouk NOT ou)))";
        try{
            
            ArrayList<StringSearchFacet.SearchClause> clauses8 = f.buildSearchClauses(search9, t, caps, marks);
            fail("MismatchedBracketException not thrown on test9");
            
        } catch(MismatchedBracketException mbe){}     
         catch(StringSearchParsingException sspe){
            
            fail("StringSearchParsingException erroneously thrown in breakIntoComponents test: " + sspe.getMessage());
            
        }
        catch(Exception e){
            
            fail("Exception erroneously thrown in breakIntoComponents test:" + e.getMessage());
            
        }
    }
    
   public void testAssignClauseRoles(){
        
        StringSearchFacet.SearchTarget t = StringSearchFacet.SearchTarget.TEXT;
        Boolean caps = true;
        Boolean marks = true;
        
        try{
            
            StringSearchFacet.SubClause testTerm = testInstance.new SubClause("dummy values", t, caps, marks);
            ArrayList<SearchClause> testClauses = new ArrayList<SearchClause>();
            
            // single term without antecedent receives default role  
            StringSearchFacet.SearchTerm term1 = testInstance.new SearchTerm("kai", t, caps, marks);
            testClauses.add(term1);
            testClauses = testTerm.assignClauseRoles(testClauses);
            assertEquals(1, testClauses.get(0).getClauseRoles().size());
            assertEquals(StringSearchFacet.ClauseRole.DEFAULT, testClauses.get(0).getClauseRoles().get(0));

            testClauses.clear();
            
            // term with AND antecedent receives AND role
            
            term1 = testInstance.new SearchTerm("kai", t, caps, marks); 
            StringSearchFacet.SearchTerm term2 = testInstance.new SearchTerm("AND", t, caps, marks);
            StringSearchFacet.SearchTerm term3 = testInstance.new SearchTerm("ouk", t, caps, marks);
            testClauses.add(term1);
            testClauses.add(term2);
            testClauses.add(term3);
            testClauses = testTerm.assignClauseRoles(testClauses);
            assertEquals(StringSearchFacet.ClauseRole.DEFAULT, term1.getClauseRoles().get(0));
            assertEquals(StringSearchFacet.ClauseRole.AND, term3.getClauseRoles().get(0));
            
            testClauses.clear();
            
            // term with OR antecedent or postcedent receives OR role
            term1 = testInstance.new SearchTerm("kai", t, caps, marks); 
            term2 = testInstance.new SearchTerm("OR", t, caps, marks);
            term3 = testInstance.new SearchTerm("ouk", t, caps, marks);
            testClauses.add(term1);
            testClauses.add(term2);
            testClauses.add(term3);
            testClauses = testTerm.assignClauseRoles(testClauses);
            assertEquals(StringSearchFacet.ClauseRole.OR, term1.getClauseRoles().get(0));
            assertEquals(StringSearchFacet.ClauseRole.OR, term3.getClauseRoles().get(0));
           
            testClauses.clear();
            
            // term with NOT antecedent receives NOT role
            term1 = testInstance.new SearchTerm("NOT", t, caps, marks); 
            term2 = testInstance.new SearchTerm("ouk", t, caps, marks);
            testClauses.add(term1);
            testClauses.add(term2);
            testClauses = testTerm.assignClauseRoles(testClauses);
            assertEquals(StringSearchFacet.ClauseRole.NOT, term2.getClauseRoles().get(0));
            
            testClauses.clear();
            
            // term with LEX antecedent receives LEX role
            term1 = testInstance.new SearchTerm("LEX", t, caps, marks); 
            term2 = testInstance.new SearchTerm("luw", t, caps, marks);
            testClauses.add(term1);
            testClauses.add(term2);
            testClauses = testTerm.assignClauseRoles(testClauses);
            assertEquals(StringSearchFacet.ClauseRole.LEMMA, term2.getClauseRoles().get(0));

            testClauses.clear();
            
            // proximity searches assign pre- and post- roles correctly
            StringSearchFacet.SubClause term6 = testInstance.new SubClause("(luw THEN strategos)~15words", t, caps, marks);
            term1 = testInstance.new SearchTerm("luw", t, caps, marks); 
            term2 = testInstance.new SearchTerm("15w", t, caps, marks);
            term3 = testInstance.new SearchTerm("strategos", t, caps, marks);
            testClauses.add(term1);
            testClauses.add(term2);
            testClauses.add(term3);
            testClauses = testTerm.assignClauseRoles(testClauses);
            assertEquals(StringSearchFacet.ClauseRole.START_PROX, term1.getClauseRoles().get(0));
            assertEquals(StringSearchFacet.ClauseRole.OPERATOR, term2.getClauseRoles().get(0));
            assertEquals(StringSearchFacet.ClauseRole.END_PROX, term3.getClauseRoles().get(0));
            
            // mixed handlers add default roles appropriately
             // TODO: These actually work, but for code simplification may not be supported in future
            StringSearchFacet.SubClause term7 = testInstance.new SubClause("luw AND ouk OR kai", t, caps, marks);
            ArrayList<StringSearchFacet.SearchClause> subclauses7 = term7.getClauseComponents();
            StringSearchFacet.SearchClause oukAndOrClause = subclauses7.get(2);
            assertEquals(2, oukAndOrClause.getClauseRoles().size());
            assertTrue(oukAndOrClause.getClauseRoles().contains(StringSearchFacet.ClauseRole.AND));
            assertTrue(oukAndOrClause.getClauseRoles().contains(StringSearchFacet.ClauseRole.OR));
            
            StringSearchFacet.SubClause term8 = testInstance.new SubClause("kai THEN LEX luw", t, caps, marks);
            ArrayList<StringSearchFacet.SearchClause> subclauses8 = term8.getClauseComponents();
            StringSearchFacet.SearchClause lemLuwClause = subclauses8.get(3);
            assertEquals(2, lemLuwClause.getClauseRoles().size());
            assertTrue(lemLuwClause.getClauseRoles().contains(StringSearchFacet.ClauseRole.LEMMA));
            assertTrue(lemLuwClause.getClauseRoles().contains(StringSearchFacet.ClauseRole.END_PROX));            
        
        }
        catch(StringSearchParsingException sspe){
        
            fail("StringSearchParsingException erroneously thrown in test: " + sspe.getMessage());
        } 
        catch(Exception e){
            
            fail("Exception erroneously thrown in assignClauseRoles test:" + e.getMessage());
            
        }

    }
    
    public void testParseForSearchType(){
        
        StringSearchFacet.SearchTarget t = StringSearchFacet.SearchTarget.TEXT;
        
        try{

            // default to substring
            String rawString1 = "kai";
            StringSearchFacet.SearchTerm clause1 = testInstance.new SearchTerm(rawString1, t, true, true);
            assertEquals(StringSearchFacet.SearchType.SUBSTRING, clause1.parseForSearchType());

            // quotation marks w/o word-boundary marker indicate phrase search
            String rawString2 = "\"kai ouk\"";
            StringSearchFacet.SearchTerm clause2 = testInstance.new SearchTerm(rawString2, t, true, true);
            assertEquals(StringSearchFacet.SearchType.PHRASE, clause2.parseForSearchType());

            // target as metadata results in phrase search
            StringSearchFacet.SearchTerm clause3 = testInstance.new SearchTerm(rawString1, StringSearchFacet.SearchTarget.METADATA, true, true);
            assertEquals(StringSearchFacet.SearchType.PHRASE, clause3.parseForSearchType());
            
            // target as translation results in phrase search
            StringSearchFacet.SearchTerm clause4 = testInstance.new SearchTerm(rawString1, StringSearchFacet.SearchTarget.TRANSLATION, true, true);
            assertEquals(StringSearchFacet.SearchType.PHRASE, clause4.parseForSearchType());
            
            // quotation marks w/word-boundary marker indicate substring search
            String rawString5 = "\"#kai# #ouk\"";
            StringSearchFacet.SearchTerm clause5 = testInstance.new SearchTerm(rawString5, t, true, true);
            assertEquals(StringSearchFacet.SearchType.SUBSTRING, clause5.parseForSearchType());

            // lemma request anywhere in clause results in lemma
            String rawString6 = "kai LEX luw";
            StringSearchFacet.SubClause clause6 = testInstance.new SubClause(rawString6, t, true, true);
            assertEquals(StringSearchFacet.SearchType.LEMMA, clause6.parseForSearchType());

            // regex request anywhere in clause results in regex
            String rawString7 = "kai REGEX lu.?";
            StringSearchFacet.SubClause clause7 = testInstance.new SubClause(rawString7, t, true, true);
            assertEquals(StringSearchFacet.SearchType.REGEX, clause7.parseForSearchType());

            // proximity request with character unit results in regex
            String rawString8 = "(kai NEAR ouk)~4chars";
            StringSearchFacet.SubClause clause8 = testInstance.new SubClause(rawString8, t, true, true);
            assertEquals(StringSearchFacet.SearchType.REGEX, clause8.parseForSearchType());
            
            // proximity request with word unti results in proximity
            String rawString9 = "(kai THEN ouk)~5words";
            StringSearchFacet.SubClause clause9 = testInstance.new SubClause(rawString9, t, true, true);
            assertEquals(StringSearchFacet.SearchType.PROXIMITY, clause9.parseForSearchType());
            
            
        }
        catch(StringSearchParsingException sspe){          
            
            fail("StringSearchParsingException erroneously thrown in parseForSearchTypeTest: " + sspe.getMessage());         
            
        }
        catch(Exception e){
            
            fail("Exception erroneously thrown in parseForSearchType test:" + e.getMessage());
            
        }
        
    }
    
    public void testParseForField(){
                
        try{
        
            StringSearchFacet.SubClause testClause = testInstance.new SubClause("dummy value here", StringSearchFacet.SearchTarget.TEXT, true, true);
            
            // queries of user-defined type return null
            assertNull(testClause.parseForField(StringSearchFacet.SearchType.USER_DEFINED, StringSearchFacet.SearchTarget.TEXT, Boolean.TRUE, Boolean.TRUE));
            
            // substring searches
            // (i) with caps off returns transcription_ngram_ic
            assertEquals(SolrField.transcription_ngram_ic, testClause.parseForField(StringSearchFacet.SearchType.SUBSTRING, StringSearchFacet.SearchTarget.TEXT, Boolean.TRUE, Boolean.FALSE));            
            
            //(ii) with marks off returns transcription_ngram_id
            assertEquals(SolrField.transcription_ngram_id, testClause.parseForField(StringSearchFacet.SearchType.SUBSTRING, StringSearchFacet.SearchTarget.TEXT, Boolean.FALSE, Boolean.TRUE));
            
            //(iii) with both off returns transcription_ngram_ia
            assertEquals(SolrField.transcription_ngram_ia, testClause.parseForField(StringSearchFacet.SearchType.SUBSTRING, StringSearchFacet.SearchTarget.TEXT, Boolean.TRUE, Boolean.TRUE));
            
            // any search of lemma type must be transcription_ia
            assertEquals(SolrField.transcription_ia, testClause.parseForField(StringSearchFacet.SearchType.LEMMA, StringSearchFacet.SearchTarget.TEXT, Boolean.TRUE, Boolean.TRUE));
            
            // phrase searches
            // (i) with caps off only returns transcription_ic
            assertEquals(SolrField.transcription_ic, testClause.parseForField(StringSearchFacet.SearchType.PHRASE, StringSearchFacet.SearchTarget.TEXT, Boolean.TRUE, Boolean.FALSE));
            // (ii) with marks off only returns transcription_id
            assertEquals(SolrField.transcription_id, testClause.parseForField(StringSearchFacet.SearchType.PHRASE, StringSearchFacet.SearchTarget.TEXT, Boolean.FALSE, Boolean.TRUE));

            // (iii) with both off returns transcription_ia
            assertEquals(SolrField.transcription_ia, testClause.parseForField(StringSearchFacet.SearchType.PHRASE, StringSearchFacet.SearchTarget.TEXT, Boolean.TRUE, Boolean.TRUE));

            // regex searches 
            // (i) with caps off only returns untokenized_ic
            assertEquals(SolrField.untokenized_ic, testClause.parseForField(StringSearchFacet.SearchType.REGEX, StringSearchFacet.SearchTarget.TEXT, Boolean.TRUE, Boolean.FALSE));

            // (ii) with marks off only returns untokenized_id
            assertEquals(SolrField.untokenized_id, testClause.parseForField(StringSearchFacet.SearchType.REGEX, StringSearchFacet.SearchTarget.TEXT, Boolean.FALSE, Boolean.TRUE));

            // (iii) with both off returns untokenized_ia
            assertEquals(SolrField.untokenized_ia, testClause.parseForField(StringSearchFacet.SearchType.REGEX, StringSearchFacet.SearchTarget.TEXT, Boolean.TRUE, Boolean.TRUE));

            // proximity searches
            // (i) with caps off only returns transcription_ic
            assertEquals(SolrField.transcription_ic, testClause.parseForField(StringSearchFacet.SearchType.PROXIMITY, StringSearchFacet.SearchTarget.TEXT, Boolean.TRUE, Boolean.FALSE));
            
            // (ii) with caps off only returns transcription_id
            assertEquals(SolrField.transcription_id, testClause.parseForField(StringSearchFacet.SearchType.PROXIMITY, StringSearchFacet.SearchTarget.TEXT, Boolean.FALSE, Boolean.TRUE));
          
            // (iii) with both off returns untokenized_ia
            assertEquals(SolrField.transcription_ia, testClause.parseForField(StringSearchFacet.SearchType.PROXIMITY, StringSearchFacet.SearchTarget.TEXT, Boolean.TRUE, Boolean.TRUE));
            
            // metadata searches should return metadata field
            assertEquals(SolrField.metadata, testClause.parseForField(StringSearchFacet.SearchType.PHRASE, StringSearchFacet.SearchTarget.METADATA, Boolean.TRUE, Boolean.FALSE));

            // translation searches should return translation field
             assertEquals(SolrField.translation, testClause.parseForField(StringSearchFacet.SearchType.PHRASE, StringSearchFacet.SearchTarget.TRANSLATION, Boolean.TRUE, Boolean.FALSE));
               
        }
        catch(StringSearchParsingException sspe){
            
            fail("StringSearchParsingException erroneously thrown on parseForField test: " + sspe.getMessage());
            
        }
        catch(Exception e){
            
            fail("Exception erroneously thrown in parseForField test:" + e.getMessage());
            
        }
    }
    
    public void testParseForHandler(){
        
        
        try{
        
            StringSearchFacet.SubClause testClause = testInstance.new SubClause("dummy value here", StringSearchFacet.SearchTarget.TEXT, true, true);

            // empty list returns default
            ArrayList<StringSearchFacet.ClauseRole> roles = new ArrayList<StringSearchFacet.ClauseRole>();
            assertNull(testClause.parseForSearchHandler(roles));
            
            // any list containing regex returns regex
            roles.add(StringSearchFacet.ClauseRole.DEFAULT);
            roles.add(StringSearchFacet.ClauseRole.LEMMA);
            roles.add(StringSearchFacet.ClauseRole.REGEX);
            roles.add(StringSearchFacet.ClauseRole.END_PROX);
            roles.add(StringSearchFacet.ClauseRole.START_PROX);
            assertEquals(StringSearchFacet.SearchHandler.REGEXP, testClause.parseForSearchHandler(roles));

            // any list containing either before_prox or after_prox returns prox
            roles.remove(StringSearchFacet.ClauseRole.REGEX);
            assertEquals(StringSearchFacet.SearchHandler.SURROUND, testClause.parseForSearchHandler(roles));

            // any list containing none of (regex|before_prox|after_prox) returns default
            roles.remove(StringSearchFacet.ClauseRole.START_PROX);
            roles.remove(StringSearchFacet.ClauseRole.END_PROX);
            assertEquals(StringSearchFacet.SearchHandler.DEFAULT, testClause.parseForSearchHandler(roles));
        
        } catch(StringSearchParsingException sspe){
            
            fail("StringSearchParsingException erroneously thrown in parseForHandler test: " + sspe.getMessage());
            
        }
        catch(Exception e){
            
            fail("Exception erroneously thrown in parseForHandler test:" + e.getMessage());
            
        }
    }
    
    public void testGetQueryPrefix(){
        
        try{
            
            StringSearchFacet.SubClause testClause = testInstance.new SubClause("dummy value here", StringSearchFacet.SearchTarget.TEXT, true, true);
            
            // no field returns no prefix 
            
            assertEquals("", testClause.getQueryPrefix(StringSearchFacet.SearchHandler.DEFAULT, null));

            // REGEXP search handler returns appropriately formatted prefix
            assertEquals("{!regexp cache=false qf=\"untokenized_ia\"}", testClause.getQueryPrefix(StringSearchFacet.SearchHandler.REGEXP, SolrField.untokenized_ia));

            // SURROUND search handler returns appropriately formatted  prefix
            assertEquals("{!surround cache=false}transcription_ic:", testClause.getQueryPrefix(StringSearchFacet.SearchHandler.SURROUND, SolrField.transcription_ic));

            // DEFAULT search handler simply returns field name followed by colon
            assertEquals("transcription_ngram_ia:", testClause.getQueryPrefix(StringSearchFacet.SearchHandler.DEFAULT, SolrField.transcription_ngram_ia));
            
            
        } catch(StringSearchParsingException sspe){
            
            fail("StringSearchParsingException erroneously thrown on getQueryPrefix test: " + sspe.getMessage());
            
            
        }
        catch(Exception e){
            
            fail("Exception erroneously thrown in getQueryPrefix test:" + e.getMessage());
            
        }
    }
    
    public void testAnchorRegex(){
        
      
        try{
        
            StringSearchFacet.SearchTerm testTerm = testInstance.new SearchTerm("dummy", StringSearchFacet.SearchTarget.TEXT, true, false);
            
            // multi-character and anchors bracket unanchored expressions
            String test1 = "και";
            assertEquals("^.*και.*$", testTerm.anchorRegex(test1));
            
            // if already anchored, returned unchanged
            String test2 = "^και$";
            assertEquals(test2, testTerm.anchorRegex(test2));
            
            // if anchored on only one side, the other side is anchored with multi-character and anchor
            String test3 = "^και";
            assertEquals("^και.*$", testTerm.anchorRegex(test3));
            
            String test4 = "και.*$";
            assertEquals("^.*και.*$", testTerm.anchorRegex(test4));
            
            // if already multi-charactered, add anchors
            String test5 = ".*και.*";
            assertEquals("^.*και.*$", testTerm.anchorRegex(test5));
            
            // .+ is considered equivalent to .* for the purposes of anchoring
            String test6 = ".+και";
            assertEquals("^.+και.*$", testTerm.anchorRegex(test6));
        
        }
        catch(Exception e){
            
            fail("Exception erroneously thrown instantiating testTerm in anchorRegex test: " + e.getMessage());
            
        }
        
    }
    
    public void testTranscoder(){
        
        try{
            
            String test1 = "εῖ";
            StringSearchFacet.SearchTerm testTerm = testInstance.new SearchTerm("dummy", StringSearchFacet.SearchTarget.TEXT, true, true);
            assertEquals(test1, testTerm.transcodeToUnicodeC(test1));
            
            String test2 = "ει̃";
            StringSearchFacet.SearchTerm testTerm2 = testInstance.new SearchTerm("dummy", StringSearchFacet.SearchTarget.TEXT, true, true);
            assertEquals(test1, testTerm2.transcodeToUnicodeC(test2));
            
        }
        catch(Exception e){
            
            fail("Exception erroneously thrown instantiating testTerm in testTrancoder: " + e.getMessage());
            
            
        }
        
        
        
    }
    
    
}
   

        
    
    

