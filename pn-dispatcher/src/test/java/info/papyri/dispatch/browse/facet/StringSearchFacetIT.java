package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.facet.customexceptions.CustomApplicationException;
import info.papyri.dispatch.browse.facet.customexceptions.StringSearchParsingException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;

/**
 *
 * @author thill
 */
public class StringSearchFacetIT extends TestCase {
    
    private StringSearchFacet testInstance;    
    StringSearchFacet.SearchTarget t = StringSearchFacet.SearchTarget.TEXT;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testInstance = new StringSearchFacet();    

    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testSubClauseParseClauseRoleAssignmentIntegration(){

        StringSearchFacet.SearchTarget t = StringSearchFacet.SearchTarget.TEXT;
        Boolean caps = true;
        Boolean marks = true;

        try{

            // single term without antecedent receives default role  
            StringSearchFacet.SearchTerm term1 = testInstance.new SearchTerm("kai", t, caps, marks, true);
            assertEquals(1, term1.getClauseRoles().size());
            assertEquals(StringSearchFacet.ClauseRole.DEFAULT, term1.getClauseRoles().get(0));

            // term with AND antecedent receives AND role

            StringSearchFacet.SubClause term2 = testInstance.new SubClause("kai AND ouk", t, caps, marks);
            ArrayList<StringSearchFacet.ClauseRole> roles2 = term2.getSubordinateClauseRoles();
            assertEquals(3, roles2.size());
            ArrayList<StringSearchFacet.SearchClause> subclauses2 = term2.getClauseComponents();
            StringSearchFacet.SearchClause kaiClause = subclauses2.get(0);
            StringSearchFacet.SearchClause oukClause = subclauses2.get(2);
            assertEquals(StringSearchFacet.ClauseRole.DEFAULT, kaiClause.getClauseRoles().get(0));
            assertEquals(StringSearchFacet.ClauseRole.AND, oukClause.getClauseRoles().get(0));

            // term with OR antecedent or postcedent receives OR role
            StringSearchFacet.SubClause term3 = testInstance.new SubClause("kai OR ouk", t, caps, marks);
            ArrayList<StringSearchFacet.ClauseRole> roles3 = term3.getSubordinateClauseRoles();            
            assertEquals(2, roles3.size()); 
            assertTrue(roles3.contains(StringSearchFacet.ClauseRole.OR));
            StringSearchFacet.SearchClause kaiOrClause = term3.getClauseComponents().get(0);
            StringSearchFacet.SearchClause oukOrClause = term3.getClauseComponents().get(2);
            assertEquals(StringSearchFacet.ClauseRole.OR, kaiOrClause.getClauseRoles().get(0));
            assertEquals(StringSearchFacet.ClauseRole.OR, oukOrClause.getClauseRoles().get(0));

            // term with NOT antecedent receives NOT role
            StringSearchFacet.SubClause term4 = testInstance.new SubClause("NOT ouk", t, caps, marks);
            ArrayList<StringSearchFacet.ClauseRole> roles4 = term4.getSubordinateClauseRoles();
            assertEquals(2, roles4.size()); 
            assertTrue(roles4.contains(StringSearchFacet.ClauseRole.NOT));
            StringSearchFacet.SearchClause oukNotClause = term4.getClauseComponents().get(1);
            assertEquals(StringSearchFacet.ClauseRole.NOT, oukNotClause.getClauseRoles().get(0));

            // term with LEX antecedent receives LEX role
            StringSearchFacet.SubClause term5 = testInstance.new SubClause("LEX luw", t, caps, marks);
            ArrayList<StringSearchFacet.ClauseRole> roles5 = term5.getSubordinateClauseRoles();
            assertEquals(2, roles5.size());
            assertTrue(roles5.contains(StringSearchFacet.ClauseRole.LEMMA));
            StringSearchFacet.SearchClause lexNotClause = term5.getClauseComponents().get(1);
            assertEquals(StringSearchFacet.ClauseRole.LEMMA, lexNotClause.getClauseRoles().get(0));

            // proximity searches assign pre- and post- roles correctly
            StringSearchFacet.SubClause term6 = testInstance.new SubClause("(luw THEN strategos)~15words", t, caps, marks);
            ArrayList<StringSearchFacet.ClauseRole> roles6 = term6.getSubordinateClauseRoles();
            assertEquals(3, roles6.size());
            StringSearchFacet.SearchClause luwClause = term6.getClauseComponents().get(0);
            StringSearchFacet.SearchClause opClause = term6.getClauseComponents().get(1);
            StringSearchFacet.SearchClause stratClause = term6.getClauseComponents().get(2);
            assertEquals(StringSearchFacet.ClauseRole.START_PROX, luwClause.getClauseRoles().get(0));
            assertEquals(StringSearchFacet.ClauseRole.OPERATOR, opClause.getClauseRoles().get(0));
            assertEquals(StringSearchFacet.ClauseRole.END_PROX, stratClause.getClauseRoles().get(0));

            // mixed handlers add default roles appropriately

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
        
   public void testBuildQuery(){
                
        try{
            
            // single-word substring search, no caps
            String test1 = "ΚάΙ";
            StringSearchFacet.SearchTerm clause1 = testInstance.new SearchTerm(test1, t, true, false, true);
            assertEquals("fq=transcription_ngram_ic:(κάι)", URLDecoder.decode(clause1.buildQuery(new SolrQuery()).toString(), "UTF-8"));
                        
            // user-defined, caps and marks set
            String test2 = "HGV:και";
            StringSearchFacet.SearchTerm clause2 = testInstance.new SearchTerm(test2, StringSearchFacet.SearchTarget.USER_DEFINED, true, true, true);
            assertEquals("fq=(hgv_metadata:και)", URLDecoder.decode(clause2.buildQuery(new SolrQuery()).toString(), "UTF-8"));
            
            // substring search, caps and marks
            String test3 = "ΚΆΙ";
            StringSearchFacet.SearchTerm clause3 = testInstance.new SearchTerm(test3, t, true, true, true);
            assertEquals("fq=transcription_ngram_ia:(και)", URLDecoder.decode(clause3.buildQuery(new SolrQuery()).toString(), "UTF-8"));
            
            // phrase search on text field, no marks
            String test4 = "\"κάι οὐκ\"";
            StringSearchFacet.SearchTerm clause4 = testInstance.new SearchTerm(test4, t, false, true, true);
            assertEquals("fq=transcription_id:(\"και ουκ\")", URLDecoder.decode(clause4.buildQuery(new SolrQuery()).toString(), "UTF-8"));
            
            // quote-delimited with substring markers, no caps or marks
            String test5 = "\"#κάι# #οὐ\"";
            StringSearchFacet.SearchTerm clause5 = testInstance.new SearchTerm(test5, t, true, true, true);
            assertEquals("fq=transcription_ngram_ia:(\"#και# #ου\")", URLDecoder.decode(clause5.buildQuery(new SolrQuery()).toString(), "UTF-8"));
            
            // regex, no marks
            String test6 = "REGEX κ.ι\\s";
            StringSearchFacet.SubClause clause6 = testInstance.new SubClause(test6, t, false, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_id\"}^.*κ.ι\\s.*$", URLDecoder.decode(clause6.buildQuery(new SolrQuery()).toString(), "UTF-8"));
            
            // proximity, words unit, no caps
            String test7 = "(ΚἈΙ? THEN ΟΎΚ)~8words";
            StringSearchFacet.SubClause clause7 = testInstance.new SubClause(test7, t, true, false);
            assertEquals("fq={!surround cache=false}transcription_ic:(κἀι? 8w ούκ)", URLDecoder.decode(clause7.buildQuery(new SolrQuery()).toString(), "UTF-8"));  
            
            // proximity, chars unit, no caps or marks
            String test8 = "(ΚἈ? NEAR ΟΎΚ)~8chars";
            StringSearchFacet.SubClause clause8 = testInstance.new SubClause(test8, t, true, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_ia\"}^.*(ουκ.{1,8}κα.|κα..{1,8}ουκ).*$", URLDecoder.decode(clause8.buildQuery(new SolrQuery()).toString() ,"UTF-8"));
  
            // lexical 
            String test9 = "LEX λύω";
            StringSearchFacet.SubClause clause9 = testInstance.new SubClause(test9, StringSearchFacet.SearchTarget.TEXT, false, false);
            Pattern lemmaOutput = Pattern.compile("fq=transcription_ia:(\\((\\s*\\S+\\s+OR\\s+)+\\S+\\s*\\))");
            assertTrue(lemmaOutput.matcher(URLDecoder.decode(clause9.buildQuery(new SolrQuery()).toString(), "UTF-8")).matches());
              
            // proximity with leading wildcards
            String test10 = "(*ιοσ* THEN λογ*)~2words";
            StringSearchFacet.SubClause clause10 = testInstance.new SubClause(test10, t, true, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_ia\"}^.*\\b\\p{L}+ιοσ\\p{L}+\\b\\s(\\p{L}+\\s){0,2}λογ\\p{L}+\\b.*$", URLDecoder.decode(clause10.buildQuery(new SolrQuery()).toString(), "UTF-8"));
            
            String test11 = "(?ιοσ* THEN *λογ?)~5words";
            StringSearchFacet.SubClause clause11 = testInstance.new SubClause(test11, t, true, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_ia\"}^.*\\b\\p{L}ιοσ\\p{L}+\\b\\s(\\p{L}+\\s){0,5}\\b\\p{L}+λογ\\p{L}\\b.*$", URLDecoder.decode(clause11.buildQuery(new SolrQuery()).toString(), "UTF-8"));
           
            String test12 = "(?ιοσ* NEAR *λογ?)~5words";
            StringSearchFacet.SubClause clause12 = testInstance.new SubClause(test12, t, true, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_ia\"}^.*(\\b\\p{L}ιοσ\\p{L}+\\b\\s(\\p{L}+\\s){0,5}\\b\\p{L}+λογ\\p{L}\\b|\\b\\p{L}+λογ\\p{L}\\b\\s(\\p{L}+\\s){0,5}\\b\\p{L}ιοσ\\p{L}+\\b).*$", URLDecoder.decode(clause12.buildQuery(new SolrQuery()).toString(), "UTF-8"));
                
            // metadata search
            StringSearchFacet.SearchTerm clause13 = testInstance.new SearchTerm(test3, StringSearchFacet.SearchTarget.METADATA, false, false, true);
            assertEquals("fq=metadata:(κάι)", URLDecoder.decode(clause13.buildQuery(new SolrQuery()).toString(), "UTF-8"));
  
            // simplified negative lookahead/behind
            String test14 = "[-kai]sar"; 
            StringSearchFacet.SearchTerm clause14 = testInstance.new SearchTerm(test14, StringSearchFacet.SearchTarget.TEXT, true, true, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_ia\"}^.*(?<!kai)sar.*$", URLDecoder.decode(clause14.buildQuery(new SolrQuery()).toString(), "UTF-8"));
            
            String test15 = "[-kai]sar#";
            StringSearchFacet.SearchTerm clause15 = testInstance.new SearchTerm(test15, StringSearchFacet.SearchTarget.TEXT, true, true, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_ia\"}^.*(?<!kai)sar\\b.*$", URLDecoder.decode(clause15.buildQuery(new SolrQuery()).toString(), "UTF-8"));
            
            String test16 = "kai[-sar]";
            StringSearchFacet.SearchTerm clause16 = testInstance.new SearchTerm(test16, t, true, true, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_ia\"}^.*kai(?!sar).*$", URLDecoder.decode(clause16.buildQuery(new SolrQuery()).toString(), "UTF-8"));
           
            String test17 = "( ou[-k] NEAR [-kai]sar )~5chars";
            StringSearchFacet.SubClause clause17 = testInstance.new SubClause(test17, t, true, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_ia\"}^.*((?<!kai)sar.{1,5}ou(?!k)|ou(?!k).{1,5}(?!kai)sar).*$", URLDecoder.decode(clause17.buildQuery(new SolrQuery()).toString(), "UTF-8"));
              
            String test18 = "(*sar THEN ou[-k])~15words";
            StringSearchFacet.SubClause clause18 = testInstance.new SubClause(test18, t, true, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_ia\"}^.*\\b\\p{L}+sar\\s(\\p{L}+\\s){0,15}ou(?!k)\\b.*$", URLDecoder.decode(clause18.buildQuery(new SolrQuery()).toString(), "UTF-8"));
           
            String test19 = "[-#]tou[-#]";
            StringSearchFacet.SearchTerm clause19 = testInstance.new SearchTerm(test19, t, true, true, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_ia\"}^.*(?<!\\b)tou(?!\\b).*$", URLDecoder.decode(clause19.buildQuery(new SolrQuery()).toString(), "UTF-8"));
          
            
            // simplified syntax when part of regex needs to pass through unchanged
            String test25 = "REGEX hyphen[-_]test";
            StringSearchFacet.SubClause clause25 = testInstance.new SubClause(test25, t, true, true);
            assertEquals("fq={!regexp cache=false qf=\"untokenized_ia\"}^.*hyphen[-_]test.*$", URLDecoder.decode(clause25.buildQuery(new SolrQuery()).toString(), "UTF-8"));
             
            
            
            // translation search
          /*  StringSearchFacet.SearchTerm clause14 = testInstance.new SearchTerm(test3, StringSearchFacet.SearchTarget.TRANSLATION, false, false);
            assertEquals("fq=translation:(κάι)", URLDecoder.decode(clause14.buildQuery(new SolrQuery()).toString(), "UTF-8"));          
            System.out.println("****************");
            StringSearchFacet.SearchTerm clause15 = testInstance.new SearchTerm("k", StringSearchFacet.SearchTarget.TEXT, false, false);
            clause15.buildQuery(new SolrQuery());
            System.out.println("=================="); */
        }
        catch(CustomApplicationException cae){
            
              fail("Integration test on StringSearchFacet.buildQuery erroneously threw custom exception " + cae.getMessage());
            
        }
        catch(UnsupportedEncodingException uee){
        
            fail("Integration test on StringSearchFacet.buildQuery threw encoding exception  " + uee.getMessage());
     
        }
        catch(Exception e){
            
            fail("Exception erroneously thrown on buildQuery test: " + e.getMessage());
            
        }
          
    }
   
    public void testDoProxTransform(){
                
        try{
            
            // sanity check - if no proximity clause present, then return unchanged
            String andClause = "kai AND ouk";
            StringSearchFacet.SubClause andSubClause = testInstance.new SubClause(andClause, t, true, true);
            ArrayList<StringSearchFacet.SearchClause> andClauses = andSubClause.getClauseComponents();
            ArrayList<StringSearchFacet.SearchClause> proxAndClauses = andSubClause.doRegexTransform(andClauses);
            assertEquals(andClauses, proxAndClauses);

            // if proximity search but with words as unit, return unchanged
            String wordProxClause = "kai 5w ouk";
            StringSearchFacet.SubClause wordSubClause = testInstance.new SubClause(wordProxClause, t, true, true);
            ArrayList<StringSearchFacet.SearchClause> wordClauses = wordSubClause.getClauseComponents();
            ArrayList<StringSearchFacet.SearchClause> proxWordClauses = wordSubClause.doRegexTransform(wordClauses);
            assertEquals(wordClauses, proxWordClauses);

            // if THEN search with chars as unit, return appropriate regex
            String thenClause = "kai 5wc ouk";
            StringSearchFacet.SubClause thenSubClause = testInstance.new SubClause(thenClause, t, true, true);
            ArrayList<StringSearchFacet.SearchClause> thenClauses = thenSubClause.getClauseComponents();
            ArrayList<StringSearchFacet.SearchClause> proxThenClauses = thenSubClause.doRegexTransform(thenClauses);
            assertEquals(1, proxThenClauses.size());
            assertEquals(1, proxThenClauses.get(0).getClauseRoles().size());
            assertTrue(proxThenClauses.get(0).getClauseRoles().contains(StringSearchFacet.ClauseRole.REGEX));
            assertEquals("^.*kai.{1,5}ouk.*$", proxThenClauses.get(0).buildTransformedString());

            // if NEAR search with chars as unit, return appropriate regex
            String nearClause = "kai 5nc ouk";
            StringSearchFacet.SubClause nearSubClause = testInstance.new SubClause(nearClause, t, true, true);
            ArrayList<StringSearchFacet.SearchClause> nearClauses = nearSubClause.getClauseComponents();
            ArrayList<StringSearchFacet.SearchClause> proxNearClauses = nearSubClause.doRegexTransform(nearClauses);
            assertEquals(1, proxNearClauses.size());
            assertEquals(1, proxNearClauses.get(0).getClauseRoles().size());
            assertTrue(proxNearClauses.get(0).getClauseRoles().contains(StringSearchFacet.ClauseRole.REGEX));
            assertEquals("^.*(ouk.{1,5}kai|kai.{1,5}ouk).*$", proxNearClauses.get(0).buildTransformedString());
            
            // wildcards converted to regex syntax
            String wildClause = "k?i 5wc ou*";
            StringSearchFacet.SubClause wildSubClause = testInstance.new SubClause(wildClause, t, true, true);
            ArrayList<StringSearchFacet.SearchClause> wildClauses = wildSubClause.getClauseComponents();
            ArrayList<StringSearchFacet.SearchClause> proxWildClauses = wildSubClause.doRegexTransform(wildClauses);
            assertEquals(1, proxWildClauses.size());
            assertEquals(1, proxWildClauses.get(0).getClauseRoles().size());
            assertTrue(proxWildClauses.get(0).getClauseRoles().contains(StringSearchFacet.ClauseRole.REGEX));
            assertEquals("^.*k.i.{1,5}ou.*$", proxWildClauses.get(0).buildTransformedString());  
            


        } catch(Exception e){
            
            fail("Exception erroneosly thrown on doProxTransform test: " + e.getMessage());
            
            
        }
            
    }

     public void testWordWildcardDoProxTransform(){
        
        String test1 = "*ιοσ* 2w λογ*";
        String expected1 = "^.*\\b\\p{L}+ιοσ\\p{L}+\\b\\s(\\p{L}+\\s){0,2}λογ\\p{L}+\\b.*$";

        String test2 = "?ιοσ* 5w *λογ?";
        String expected2 = "^.*\\b\\p{L}ιοσ\\p{L}+\\b\\s(\\p{L}+\\s){0,5}\\b\\p{L}+λογ\\p{L}\\b.*$";

        String test3 = "*saros 10n tyrann*";
        String expected3 = "^.*(\\b\\p{L}+saros\\s(\\p{L}+\\s){0,10}tyrann\\p{L}+\\b|tyrann\\p{L}+\\b\\s(\\p{L}+\\s){0,10}\\b\\p{L}+saros).*$";
       
        try{
        
            StringSearchFacet.SubClause wildClause1 = testInstance.new SubClause(test1, t, true, true);
            ArrayList<StringSearchFacet.SearchClause> wildClauses1 = wildClause1.getClauseComponents();
            ArrayList<StringSearchFacet.SearchClause> proxWildClauses1 = wildClause1.doRegexTransform(wildClauses1);
            assertEquals(1, proxWildClauses1.size());
            assertEquals(1, proxWildClauses1.get(0).getClauseRoles().size());
            assertEquals(expected1, proxWildClauses1.get(0).buildTransformedString());
            
            StringSearchFacet.SubClause wildClause2 = testInstance.new SubClause(test2, t, true, true);
            ArrayList<StringSearchFacet.SearchClause> wildClauses2 = wildClause2.getClauseComponents();
            ArrayList<StringSearchFacet.SearchClause> proxWildClauses2 = wildClause2.doRegexTransform(wildClauses2);
            assertEquals(1, proxWildClauses2.size());
            assertEquals(1, proxWildClauses2.get(0).getClauseRoles().size());
            assertEquals(expected2, proxWildClauses2.get(0).buildTransformedString());    

            StringSearchFacet.SubClause wildClause3 = testInstance.new SubClause(test3, t, true, true);
            ArrayList<StringSearchFacet.SearchClause> wildClauses3 = wildClause3.getClauseComponents();
            ArrayList<StringSearchFacet.SearchClause> proxWildClauses3 = wildClause3.doRegexTransform(wildClauses3);
            assertEquals(1, proxWildClauses3.size());
            assertEquals(1, proxWildClauses3.get(0).getClauseRoles().size());
            assertEquals(expected3, proxWildClauses3.get(0).buildTransformedString());
        
        }
        catch(Exception e){
            
            fail("Exception erroneously thrown in integration test for word-wildcards-prox-transform: " + e.getMessage());
            
        }


    }
        
    public void testBuildTransformedString(){
                
        try{

            // sanity-check: no transformation req'd returns as same
            String origString1 = "κάι";
            StringSearchFacet.SearchTerm term1 = testInstance.new SearchTerm(origString1, t, false, false, true);
            assertEquals(origString1, term1.buildTransformedString());

            // regex means no transformation
            String origString2 = "REGEX κ.+ι";
            StringSearchFacet.SubClause term2 = testInstance.new SubClause(origString2, t, false, false);
            assertEquals("^.*κ.+ι.*$", term2.buildTransformedString());

            // ignore_caps results in lower-casing
            String origString3 = "ΚάΙ";
            StringSearchFacet.SearchTerm term3 = testInstance.new SearchTerm(origString3, t, true, false, true);
            assertEquals(origString1, term3.buildTransformedString());

            // ignore_marks results in diacritic-stripping
            StringSearchFacet.SearchTerm term4 = testInstance.new SearchTerm(origString1, t, false, true, true);
            assertEquals("και", term4.buildTransformedString());

            // both results in lower-casing and diacritic-stripping
            StringSearchFacet.SearchTerm term5 = testInstance.new SearchTerm(origString3, t, true, true, true);
            assertEquals("και", term5.buildTransformedString());
            
            Pattern lemmaOutput = Pattern.compile("\\((\\s*\\S+\\s+OR\\s+)+\\S+\\s*\\)");
            // lemmatised causes expansion
            String origString6 = "LEX amo";
            StringSearchFacet.SubClause term6 = testInstance.new SubClause(origString6, StringSearchFacet.SearchTarget.TEXT, false, false);
            String lemmad1 = term6.buildTransformedString();
            assertTrue(lemmaOutput.matcher(lemmad1).matches());

            // lemmatised disregards lower-casing and diacritic-stripping 
            // (diacritics automatically included, capitalisation automatically disregarded)
            StringSearchFacet.SubClause term7 = testInstance.new SubClause(origString6, StringSearchFacet.SearchTarget.TEXT, true, true);
            String lemmad2 = term7.buildTransformedString();
            assertTrue(lemmaOutput.matcher(lemmad2).matches()); 
            
            // metadata as target causes automatic lower-casing
            String origString8 = "Caesar Augustus";
            StringSearchFacet.SubClause term8 = testInstance.new SubClause(origString8, StringSearchFacet.SearchTarget.METADATA, false, false);
            assertEquals(origString8.toLowerCase(), term8.buildTransformedString());
            
            // translation as target causes automatic lower-casing
            StringSearchFacet.SubClause term9 = testInstance.new SubClause(origString8, StringSearchFacet.SearchTarget.TRANSLATION, false, false);
            assertEquals(origString8.toLowerCase(), term9.buildTransformedString());
            
            // final sigma replaced with medial sigma
            String origString10 = "στρατηγος";
            StringSearchFacet.SearchTerm term10 = testInstance.new SearchTerm(origString10, t, true, true, true);
            assertEquals("στρατηγοσ", term10.buildTransformedString());
            
            // replace hash-mark \b markers with caret \b markers
            String origString11 = "#kai# #tou";
            StringSearchFacet.SubClause term11 = testInstance.new SubClause(origString11, t, true, true);
            assertEquals("#kai# #tou", term11.buildTransformedString());
            
            // character-proximity searches transformed into appropriate regex
            String origString13 = "(kai 10wc ouk)";
            StringSearchFacet.SubClause term13 = testInstance.new SubClause(origString13, t, true, true);
            assertEquals("^.*(kai.{1,10}ouk).*$", term13.buildTransformedString());  
            
            String origString14 = "kai 12nc ouk";
            StringSearchFacet.SubClause term14 = testInstance.new SubClause(origString14, t, true, true);
            assertEquals("^.*(ouk.{1,12}kai|kai.{1,12}ouk).*$", term14.buildTransformedString());
            
            String origString15 = "REGEX kai(?<!i)";
            StringSearchFacet.SubClause term15 = testInstance.new SubClause(origString15, t, true, true);
            assertEquals("^.*kai(?<!i).*$", term15.buildTransformedString());
            
            // regex special chars should pass through unchanged but for anchoring
            String origString16 = "REGEX kai(?!sar)";
            StringSearchFacet.SubClause term16 = testInstance.new SubClause(origString16, StringSearchFacet.SearchTarget.TEXT, true, true);
            assertEquals("^.*kai(?!sar).*$", term16.buildTransformedString());
            
            String origString17 = "REGEX (?<!kai)sar";
            StringSearchFacet.SubClause term17 = testInstance.new SubClause(origString17, StringSearchFacet.SearchTarget.TEXT, true, true);
            assertEquals("^.*(?<!kai)sar.*$", term17.buildTransformedString());
               
            String origString18 = "REGEX kai[sa]r";
            StringSearchFacet.SubClause term18 = testInstance.new SubClause(origString18, StringSearchFacet.SearchTarget.TEXT, true, true);
            assertEquals("^.*kai[sa]r.*$", term18.buildTransformedString());
            
            
        }
        catch(CustomApplicationException e){
           
           fail("Integration test on StringSearchFacet.buildTransformedString erroneously threw exception: " + e.getMessage());
           
       }
        catch(Exception e){
            
            fail("Exception erroneously thrown on string transform test: " + e.getMessage());           
            
        }

        
    }
    

    

    
    
}
