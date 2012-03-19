package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.FileUtils;
import info.papyri.dispatch.browse.SolrField;
import info.papyri.dispatch.browse.facet.customexceptions.CustomApplicationException;
import info.papyri.dispatch.browse.facet.customexceptions.IncompleteClauseException;
import info.papyri.dispatch.browse.facet.customexceptions.InsufficientSpecificityException;
import info.papyri.dispatch.browse.facet.customexceptions.InternalQueryException;
import info.papyri.dispatch.browse.facet.customexceptions.MalformedProximitySearchException;
import info.papyri.dispatch.browse.facet.customexceptions.MismatchedBracketException;
import info.papyri.dispatch.browse.facet.customexceptions.RegexCompilationException;
import info.papyri.dispatch.browse.facet.customexceptions.StringSearchParsingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 * <code>Facet</code> complex string-search capability.
 * 
 * For the implementation of string-search used in the pre-faceted versions of the Navigator
 * see the <code>info.papyri.dispatch.Search</code> class, on which much of the functionality
 * of this <code>Facet</code> is originally modeled.
 * 
 * The code below can broadly be divided into the outer <code>StringSearchFacet</code>
 * class (broadly, responsible for display and user interaction) and its inner
 * <code>SearchClause</code> classes (responsible for handling the actual string-search
 * logic).
 * 
 * The outer class differs from other <code>Facet</code> subclasses chiefly in that
 * repeated complex (that is to say, involving more than one request parameter)
 * searches are possible. This entails that search parameters must be ordered, both to 
 * ensure that they remain correctly correlated with each other (so that for example the
 * IGNORE_CAPS setting used in one search is not mistakenly applied to another) and so
 * that they can be displayed correctly. Most of the methods overriding <code>Facet</code> 
 * methods do so in order to provide this ordering functionality.
 * 
 * Overview and documentation of string-search logic are given with the inner
 * <code>SearchClause</code> class and its subclasses.
 * 
 * @author thill
 * @version 2012.02.08
 * @see info.papyri.dispatch.Search
 * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchClause
 * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause
 * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchTerm
 */
public class StringSearchFacet extends Facet{
    
    final public String SUBFIELD_SEPARATOR = "¤";
    
    /**
     * Frontend descriptions of the type of search being performed.
     * <dl>
     * <dt>PHRASE       </dt>   <dd>searches performed for complete words or groups of complete words.</dd>
     * <dt>SUBSTRING    </dt>   <dd>seeks any instance of the string entered regardless of word divisions. Such
     * searches are the default search-setting</dd>
     * <dt>REGEX        </dt>   <dd>searches using Regular Expression syntax</dd>
     * <dt>LEMMA        </dt>   <dd>expands a word into all its possible forms, and then conducts a search
     * for these forms</dd>
     * <dt>USER_DEFINED </dt>   <dd>the user has chosen the fields to search manually, and thus wishes
     * to bypass the application's automatic determination of the search-field.</dd>
     * <dt>PROXIMITY    </dt>   <dd>the user is searching for two words or substrings within a 
     * given word or character range of each other (for example, the words 'Call' and 'Ishmael' separated
     * by a single word, or the strings 'Some y' and 'ulation' within 98 characters
     * of each other).</dd>
     * </dl>
     * 
     */
    public enum SearchType{ PHRASE, SUBSTRING, REGEX, LEMMA, USER_DEFINED, PROXIMITY };
    
    /**
     * Used in relation to PROXIMITY searches to indicate whether the relevant metric
     * of distance is characters (CHARS) or words (WORDS).     * 
     */
    
    enum SearchUnit{ WORDS, CHARS };
    
    /**
     * Frontend descriptions of the fields to search
     * 
     * Note that these values do not correspond directly to Solr fields; the Solr field
     * to search is determined by inspecting the submitted SearchTarget, along with 
     * the submitted SearchOption(s) and SearchType.
     * 
     */
    enum SearchTarget{ METADATA, TEXT, TRANSLATION, USER_DEFINED };
    /**
     * Values indicating whether or not capitalisation and diacritics should be considered
     * significant for the search
     * 
     */
    enum SearchOption{ NO_CAPS, NO_MARKS, PROXCOUNT, PROXUNIT };
    
    /**
     * Values indicating the function of a given <code>SearchTerm</code> within a search.
     * <dl>
     * <dt>LEMMA        </dt><dd>the term is to be lemmatised</dd>
     * <dt>REGEX        </dt><dd>the term is a regex</dd>
     * <dt>START_PROX   </dt><dd>the term is the first term in a proximity search</dd>
     * <dt>END_PROX     </dt><dd>the term is the second and last term of a proximity search</dd>
     * <dt>AND          </dt><dd>the term is mandatory for the search</dd>
     * <dt>OR           </dt><dd>the term is potentially optional in the search</dd>
     * <dt>NOT          </dt><dd>the term is excluded from the search</dd>
     * <dt>OPERATOR     </dt><dd>the term is a search operator</dd>
     * <dt>DEFAULT      </dt><dd>no role has yet been assigned.</dd>
     * </dl>
     * 
     */
    
    public enum ClauseRole{LEMMA, REGEX, START_PROX, END_PROX, AND, OR, NOT, OPERATOR, DEFAULT };
    
    /**
     * The list of possible search operators.
     * 
     * <strong>AND</strong>, <strong>OR</strong>, <strong>NOT</strong> all behave as standard Boolean operators
     * <strong>REGEX</strong> indicates that the term that follows should be parsed as a Regular Expression
     * <strong>THEN</strong> is used in proximity searching to indicate that the term following the operator should
     * follow the term preceding the operator by a given number of words or characters
     * <strong>NEAR</strong> is used in proximity searching to indicate that the term following the operator should
     * be found within a given number of words or characters in either direction from the term
     * preceding the operator.
     * <strong>LEX</strong> indicates that the term that follows it should be expanded to all of its possible forms
     * prior to search
     * 
     */
    
    enum SearchOperator{AND, OR, REGEX, THEN, NEAR, NOT, LEX };
    
    /**
     * List of button controls for the <code>Facet</code>
     * 
     * Most of these serve simply to display the relevant search operator in the search box.
     * CLEAR clears the search box; REMOVE removes the search box entirely.
     * 
     * @see #generateSearchButtons() 
     */
    
    enum SearchButton{
        
        AND("and"),
        OR("or"),
        NOT("not"),
        LEX("lex"),
        THEN("then"),
        NEAR("near"),
        REGEX("regex"),
        CLEAR("clear"),
        REMOVE("-");

        String label;
        
        SearchButton(String lbl){
            
            label = lbl;
            
        }
        
    }
    
    /**
     * List of usable Solr search-handlers.
     * <dl>
     * <dt>SURROUND </dt>    <dd>used for word-proximity searches</dd>
     * <dt>REGEXP   </dt>    <dd>used for Regular Expression searches</dd>
     * <dt>DEFAULT  </dt>    <dd>used for all other searches.</dd>
     * </dl>
     */
    
    enum SearchHandler{
        
        SURROUND,
        REGEXP,
        DEFAULT
        
        
    }
    
    // the following series of regex patterns is given here as static members 
    // because of the relative expense of regex compilation and matching
    
    /**
     * Regular Expression <code>Pattern</code> for detecting the presence of a 
     * proximity operator in a <code>SearchClause</code>.
     * 
     * Note the syntax here:
     * \d{1,2}: An integer between 1 and 99 indicating the number of proximity untis
     * within which a search term has to fall
     * (w|n): whether the proximity search is a THEN (w) or NEAR (n) search
     * c?: Optional; when appended, the 'c' indicates that a character-range search
     * is desired; when absent, a word-proximity search will be performed
     * 
     * @see SearchClause#isOperator() 
     */
    
    static Pattern PROX_OPERATOR_REGEX = Pattern.compile("\\d{1,2}(w|n|wc|nc)");
    
    /**
     * Regular Expression <code>Pattern</code> for detecting the presence of a 
     * character-proximity operator in a <code>SearchClause</code>.
     * 
     * @see SubClause#convertCharProxToRegexSyntax(java.lang.String, java.lang.String, java.lang.String) 
     */

    static Pattern CHAR_PROX_REGEX = Pattern.compile(".*?(\\d{1,2})(w|n)c.*");
    
    /**
     * Regular Expression <code>Pattern</code> for detecting the presence of a 
     * word-proximity operator in a <code>SearchClause</code>
     * 
     * @see SubClause#convertWordProxToRegexSyntax(java.lang.String, java.lang.String, java.lang.String) 
     */
    
    static Pattern WORD_PROX_REGEX = Pattern.compile(".*?(\\d{1,2})?(w|n).*");
    
    /**
     * Regular Expression <code>Pattern</code> for determining whether a given 
     * <code>SearchClause</code> contains a character-proximity operator.
     * 
     * @see SubClause#convertProxWildcards(java.lang.String, java.lang.String, int, int) 
     * @see SearchTerm#isCharactersProxTerm() 
     */
    
    static Pattern CHAR_PROX_TERM_REGEX = Pattern.compile("\\d{1,2}(w|n)c");
    
    
    /**
     * Regular Expression <code>Pattern</code> for determining whether a given
     * <code>SearchClause</code> contains a word-proximity operator.
     * 
     * @see SearchTerm#isWordsProxTerm() 
     */
    
    static Pattern WORD_PROX_TERM_REGEX = Pattern.compile("(\\d{1,2})?(w|n)");   
    
    /**
     * Regular Expression <code>Pattern</code> for determining whether a given
     * String contains a search phrase (that is to say, whether it contains a 
     * quotation-mark-delimited substring containing whitespace).
     * 
     * @see SearchClause#parseForSearchType() 
     * 
     */
    
    static String PHRASE_MARKER = ".*(\"|')[\\p{L}]+(\\s+[\\p{L}]+)*(\\1).*";
    
    /**
     * Regular Expression <code>Pattern</code> for determining whether a given String
     * contains any whitespace.
     * 
     * @see #isTerm(java.lang.String) 
     */
            
    static Pattern WHITESPACE_DETECTOR = Pattern.compile("^.*\\s+.*$");
       
    SearchClauseFactory CLAUSE_FACTORY = new SearchClauseFactory();
    

    
    /** A collection from which <code>SearchClause</code>s can be retrieved in an
     *  ordered manner.
     * 
     * Ordering is important because, unlike other facets, additional string-search
     * constraints can be added arbitrarily and repeatedly by the user. Some means is 
     * therefore required to both:
     * (a) correlate the parameters given in the query-string with each other(so that,
     * say, the 'ignore caps' setting used for one search is not mistakenly applied to a
     * subsequent search) and;
     * (b) retain the order of submission for display purposes.
     * 
     */
    private HashMap<Integer, ArrayList<SearchClause>> searchClauses = new HashMap<Integer, ArrayList<SearchClause>>();
    
    /**
     * Path to the Solr index for lemmatised searches 
     */
    private static String morphSearch = "morph-search/";
    
    /**
     * Stores exceptions thrown during search-clause parsing for feedback to the user.
     * 
     * @see info.papyri.dispatch.browse.facet.FacetBrowser#collectFacetExceptions(java.util.ArrayList) 
     * @see info.papyri.dispatch.browse.facet.customexceptions
     */
    
    ArrayList<CustomApplicationException> exceptionLog;

    public StringSearchFacet(){
        
        super(SolrField.transcription_ngram_ia, FacetParam.STRING, "String search");
        exceptionLog = new ArrayList<CustomApplicationException>();
             
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        Iterator<ArrayList<SearchClause>> scait = searchClauses.values().iterator();
        while(scait.hasNext()){
            
            ArrayList<SearchClause> nowConfigs = scait.next();
            
            Iterator<SearchClause> scit = nowConfigs.iterator();
            while(scit.hasNext()){
                
                   SearchClause nowConfig = scit.next();
                   
                   try{
                   
                        nowConfig.buildQuery(solrQuery);
                        
                   }
                   catch(CustomApplicationException cpe){
                       
                       exceptionLog.add(cpe);
                       
                   }
                   
            }
                     
        }
        
        return solrQuery;
        
    }
        
    @Override
    public String generateWidget() {
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" id=\"text-search-widget\" title=\"");
        html.append(getToolTipText());
        html.append("\">");
        
        html.append("<div class=\"stringsearch-top-controls\">");
        
        // textbox HTML
        html.append("<p class=\"ui-corner-all facet-stringsearch-wrapper\">");
        html.append("<input type=\"text\" name=\"");
        html.append(formName.name());
        html.append("\" size=\"37\" maxlength=\"250\" class=\"keyword\"></input>");
        html.append("<span class=\"prx\">");
        html.append("<span class=\"within\">within</span>");
        html.append("<input type=\"text\" name=\"prxcount\" class=\"prxcount\" size=\"2\" maxlength=\"2\" disabled=\"disabled\"/>");
        html.append(" <select name=\"prxunit\" class=\"prxunit\" disabled=\"disabled\">");
        html.append("<option selected=\"selected\" value=\"chars\">chars</option>");
        html.append("<option value=\"words\">words</option>");
        html.append("</select>");
        html.append("</span>");
        html.append("</p>");
        html.append(this.generateSearchButtons());
        html.append("</div><!-- closing .stringsearch-top-controls -->");

        // search options control
        html.append("<div class=\"stringsearch-section\">");
        html.append("<p>");
        html.append("<input type=\"checkbox\" name=\"beta-on\" id=\"beta-on\" value=\"on\"></input>");  
        html.append("<label for=\"beta-on\" id=\"marks-label\">Convert from betacode as you type</label><br/>");
        html.append("<input type=\"checkbox\" name=\"");
        html.append(SearchOption.NO_CAPS.name().toLowerCase());
        html.append("\" id=\"caps\" value=\"on\" checked></input>");    
        html.append("<label for=\"caps\" id=\"caps-label\">ignore capitalization</label><br/>");
        html.append("<input type=\"checkbox\" name=\"");
        html.append(SearchOption.NO_MARKS.name().toLowerCase());
        html.append("\" id=\"marks\" value=\"on\" checked></input>");  
        html.append("<label for=\"marks\" id=\"marks-label\">ignore diacritics/accents</label>");
        html.append("</p>");
        html.append("</div><!-- closing .stringsearch-section -->");
        
                
        // search target control
        html.append("<div class=\"stringsearch-section\">");
        html.append("<p>");
        html.append("<input type=\"radio\" name=\"target\" value=\"");
        html.append(SearchTarget.TEXT.name().toLowerCase());
        html.append("\" value=\"on\" id=\"target-text\" class=\"target\" checked/>");
        html.append("<label for=\"");
        html.append(SearchTarget.TEXT.name().toLowerCase());
        html.append("\" id=\"text-label\">Text</label>");
        html.append("<input type=\"radio\" name=\"target\" value=\"");
        html.append(SearchTarget.METADATA.name().toLowerCase());
        html.append("\" value=\"on\" id=\"target-metadata\" class=\"target\"/>");
        html.append("<label for=\"");
        html.append(SearchTarget.METADATA.name().toLowerCase());
        html.append("\" id=\"metadata-label\">Metadata</label>");
        html.append("<input type=\"radio\" name=\"target\" value=\"");        
        html.append(SearchTarget.TRANSLATION.name().toLowerCase());
        html.append("\" value=\"on\" id=\"target-translations\" class=\"target\"/>");
        html.append("<label for=\"");
        html.append(SearchTarget.TRANSLATION.name().toLowerCase());
        html.append("\" id=\"translation-label\">Translations</label>");
        html.append("</p>");
        html.append("</div><!-- closing .stringsearch-section -->");
        html.append(generateHiddenFields());
       
        html.append("</div><!-- closing .facet-widget -->");
        
        return html.toString();
        
    }
    
    /**
     * Generates search buttons for display
     * 
     * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchButton
     * @return 
     */
    
    private String generateSearchButtons(){
        
       StringBuilder html = new StringBuilder();
       html.append("<div id=\"str-search-controls\">");
       
       for(SearchButton sb : SearchButton.values()){
        
           String label = sb.label;
           String className = "syntax-" + sb.name().toLowerCase();
           html.append("<input type=\"button\" disabled=\"disabled\" class=\"ui-widget ui-button ui-corner-all ui-state-disabled syntax ");
           html.append(className);
           html.append("\" value=\"");
           html.append(label);
           html.append("\"></input>");
        
       } 
       html.append("</div><!-- closing #str-search-controls -->");
       return html.toString();
    }
    
    @Override
    public Boolean addConstraints(Map<String, String[]> params){
        
        searchClauses = pullApartParams(params);
        return !searchClauses.isEmpty();
             
    }
    
    @Override
    String generateHiddenFields(){
        
        StringBuilder html = new StringBuilder();
        
        int counter = 0;
        int index = 1;
        
        while(index <= searchClauses.size()){
            
            ArrayList<SearchClause> configs = searchClauses.get(counter);
            counter++;
            if(configs == null) continue;
            String concatenatedStringQuery = this.concatenateSearchClauses(configs);
            
            String inp = "<input type='hidden' name='";
            String v = "' value='";
            String c = "'/>";
            html.append(inp);
            html.append(formName.name());
            html.append(String.valueOf(index));
            html.append(v);
            html.append(concatenatedStringQuery);
            html.append(c);
            
            html.append(inp);
            html.append("target");
            html.append(String.valueOf(index));
            html.append(v);
            html.append(configs.get(0).getSearchTarget().name());
            html.append(c);
            
            if(configs.get(0).getIgnoreCaps()){
                
                html.append(inp);
                html.append(SearchOption.NO_CAPS.name().toLowerCase());
                html.append(String.valueOf(index));                
                html.append(v);
                html.append("on");
                html.append(c);
                
            }
            
            if(configs.get(0).getIgnoreMarks()){
                
                html.append(inp);
                html.append(SearchOption.NO_MARKS.name().toLowerCase());
                html.append(String.valueOf(index));
                html.append(v);
                html.append("on");
                html.append(c);
                
            }  
            index++;
            
        }
        
        return html.toString();
        
    }
    
    /**
     * Reconstitutes the various <code>SearchClause</code>s as passed in the params
     * to the servlet
     * 
     * Used in maintaining state between page transitions (for example, when paging through
     * result sets or removing another facet value)
     * 
     * @param configs
     * @return 
     */
    
    //TODO: get rid of old 'config' designation, replace with clause strings
    
    private String concatenateSearchClauses(ArrayList<SearchClause> clauses){
        
        StringBuilder query = new StringBuilder();
        Iterator<SearchClause> scit = clauses.iterator();
        while(scit.hasNext()){
        
            SearchClause nowClause = scit.next();
            query.append(nowClause.getOriginalString());
            if(scit.hasNext()) query.append(SUBFIELD_SEPARATOR);
        
            
        }
        return query.toString();
        
    }
    
    /**
     * Concatenates <code>SearchClause</code> search-strings in human-readable form.
     * 
     * @param configs
     * @return 
     */
    
    private String concatenateConfigDisplayValues(ArrayList<SearchClause> clauses){
        
        StringBuilder query = new StringBuilder();
        Iterator<SearchClause> scit = clauses.iterator();
        while(scit.hasNext()){
        
            SearchClause clause = scit.next();
            query.append(clause.getDisplayString());
            if(scit.hasNext()) query.append(" ");
        
            
        }
        return query.toString();
        
    }
    
    /**
     * Parses the request for string-search parameters, uses these to create appropriate
     * <code>SearchClause</code> objects, and populates a <code>HashMap</code> with them,
     * the keys of which are <code>Integer</code>s which allow the <code>SearchConfigurations</code>
     * to be retrieved in an ordered manner.
     * 
     * @param params
     * @return 
     */
    
    HashMap<Integer, ArrayList<SearchClause>> pullApartParams(Map<String, String[]> params){
        
        HashMap<Integer, ArrayList<SearchClause>> orderedClauses = new HashMap<Integer, ArrayList<SearchClause>>();
        
        Pattern pattern = Pattern.compile(formName.name() + "([\\d]*)");
        
        Iterator<String> kit = params.keySet().iterator();
        
        while(kit.hasNext()){
            
            String key = kit.next();
            
            Matcher matcher = pattern.matcher(key);
            
            if(matcher.matches()){
            
                String matchSuffix = matcher.group(1);                        

                String keywordGetter = formName.name() + matchSuffix;
                String targetGetter = "target" + matchSuffix;
                String capsGetter = SearchOption.NO_CAPS.name().toLowerCase() + matchSuffix;
                String marksGetter = SearchOption.NO_MARKS.name().toLowerCase() + matchSuffix;

                if(!params.containsKey(targetGetter)) continue;
                String keyword = params.get(keywordGetter)[0];
                if(keyword == null || "".equals(keyword)) continue;
                String rawSearchTarget = params.get(targetGetter)[0].toUpperCase();
                SearchTarget trgt = null;
                
                try{ trgt = SearchTarget.valueOf(rawSearchTarget); } 
                catch(IllegalArgumentException iae){ continue; }

                String[] rawCaps = params.get(capsGetter);
                String[] rawMarks = params.get(marksGetter);

                Boolean caps = rawCaps == null ? false : "on".equals(rawCaps[0]);
                Boolean marks = rawMarks == null ? false : "on".equals(rawMarks[0]);
                
                ArrayList<SearchClause> fieldConfigs = new ArrayList<SearchClause>();
                ArrayList<String> clauses = new ArrayList<String>(Arrays.asList(keyword.split(SUBFIELD_SEPARATOR)));

                Iterator<String> cit = clauses.iterator();
                while(cit.hasNext()){
                    
                    String clause = cit.next();
                    
                    try{
                    
                        SearchClause searchClause = isTerm(clause) ? new SearchTerm(clause, trgt, caps, marks) : new SubClause(clause, trgt, caps, marks);
                        fieldConfigs.add(searchClause);                   
                        
                    }
                    catch(CustomApplicationException cpe){ exceptionLog.add(cpe);}
                    
                }
                Integer matchNumber = matchSuffix.equals("") ? 0 : Integer.valueOf(matchSuffix);
                if(fieldConfigs.size() > 0) orderedClauses.put(matchNumber, fieldConfigs);
                
            }
            
        }
        
        return orderedClauses;
        
    }
    
    /**
     * Determines whether a given string constitutes a single search term or
     * whether it can be broken down further into individual search terms.
     * 
     * @param clause
     * @return 
     */
    
    Boolean isTerm(String clause){
        
        if(!WHITESPACE_DETECTOR.matcher(clause).matches()) return true;
        String firstChar = clause.substring(0, 1);
        if(!firstChar.equals("\"") && !firstChar.equals("'")) return false;
        String lastChar = clause.substring(clause.length() - 1);
        if(!lastChar.equals(firstChar)) return false;
        String test = clause.substring(1, clause.length() - 1);
        if(!test.contains(firstChar)) return true;
        return false;
        
    }
    
    @Override
    public ArrayList<String> getFacetConstraints(String facetParam){
        
        String paramNumber = "0";
        Pattern pattern = Pattern.compile("^.+?(\\d+)$");
        Matcher matcher = pattern.matcher(facetParam);
        if(matcher.matches()){

            paramNumber = matcher.group(1);
            
        }

        ArrayList<String> constraints = new ArrayList<String>();
        constraints.add(paramNumber);
        return constraints;
        
    }
    
    @Override
    public String getDisplayValue(String facetValue){
        
        Integer k = Integer.valueOf(facetValue);
        if(!searchClauses.containsKey(k)) return "Facet value not found";
        
        ArrayList<SearchClause> configs = searchClauses.get(k);
        SearchClause leadConfig = configs.get(0);
        String displaySearchString = this.concatenateConfigDisplayValues(configs);
        
        StringBuilder dv = new StringBuilder();
        dv.append(displaySearchString.replaceAll("\\^", "#"));   // shouldn't this be dealt with in the indexer?
        dv.append("<br/>");
        dv.append("Target: ");
        dv.append(leadConfig.getSearchTarget().name().toLowerCase().replace("_", "-"));
        dv.append("<br/>");
        
        if(leadConfig.getIgnoreCaps()) dv.append("No Caps: On<br/>");
        if(leadConfig.getIgnoreMarks()) dv.append("No Marks: On<br/>");
   /*     if(leadConfig.getSearchType().equals(SearchType.PROXIMITY)){
           // TODO: SHIFT THIS FUNCTIONALITY INTO CONCATENATECONFIGDISPLAYVALUE 
            dv.append("Within: ");
            dv.append(String.valueOf(config.getProximityDistance()));
            
        }*/

        return dv.toString();
        
    }
    
   /* String getTrismegistosDisplayValue(Matcher matcher){
       
     *  // TODO: shift this down to SearchConfiguration
        String tmNumber = matcher.group(1);
        StringBuilder tmsb = new StringBuilder();
        tmsb.append("Trismegistos Identifier: ");
        tmsb.append(tmNumber);
        return tmsb.toString();
        
        
    }*/
    
    @Override
    public String getDisplayName(String param, java.lang.String facetValue){
        
                
        String paramNumber = "0";
        Pattern pattern = Pattern.compile(this.formName.toString() + "(\\d+)$");
        Matcher matcher = pattern.matcher(param);
        if(matcher.matches()){
            
            paramNumber = matcher.group(1);
            
        }
        
        SearchClause config = searchClauses.get(Integer.valueOf(paramNumber)).get(0);
        
        String searchType = config.parseForSearchType().name().toLowerCase();
           
        String firstCap = searchType.substring(0, 1).toUpperCase();
        return firstCap + searchType.substring(1, searchType.length());
        
        
    }
    
    @Override
    public String getAsQueryString(){
        
        if(searchClauses.size() < 1) return "";
        
        StringBuilder qs = new StringBuilder();
        int counter = 0;
        int index = 1;
        while(index <= searchClauses.size()){
            
            ArrayList<SearchClause> configs = searchClauses.get(counter);
            counter++;
            if(configs == null) continue;
            qs.append(getConfigurationAsQueryString(index, configs));
            qs.append("&");
            index++;
            
        }
        
        String queryString = qs.toString();
        queryString = queryString.substring(0, queryString.length() - 1);
        return queryString;
    }
    
    @Override
    public String getAsFilteredQueryString(String filterParam, String filterValue){
                
        StringBuilder qs = new StringBuilder();
        int filteredLength = searchClauses.size() - 1;
        int counter = 0;
        int index = 1;
        
        while(index <= filteredLength){
            
            ArrayList<SearchClause> configs = searchClauses.get(counter);
            counter++;
            if(configs == null) continue;
            
            if(!String.valueOf(counter - 1).equals(filterValue)){
            
                qs.append(getConfigurationAsQueryString(index, configs));
                qs.append("&");
                index++;
                
            }
            
        }
        
        String queryString = qs.toString();
        if(queryString.endsWith("&")) queryString = queryString.substring(0, queryString.length() - 1);
        return queryString;
        
    }
    
    /**
     * Reconstitutes a query-string from the members of the passed <code>SearchClause</code> <code>ArrayList</code>.
     * 
     * 
     * @param pn
     * @param config
     * @return 
     */
    
    private String getConfigurationAsQueryString(Integer pn, ArrayList<SearchClause> clauses){
        
            String paramNumber = pn == 0 ? "" : String.valueOf(pn);
        
            StringBuilder qs = new StringBuilder();
            String kwParam = formName.name() + paramNumber;
            String targetParam = "target" + paramNumber;
            
            qs.append(kwParam);
            qs.append("=");
            qs.append(this.concatenateSearchClauses(clauses));
            
            SearchClause leadConfig = clauses.get(0);
            
            qs.append("&");
            qs.append(targetParam);
            qs.append("=");
            qs.append(leadConfig.getSearchTarget().name());
            
            if(leadConfig.getIgnoreCaps()){
                
                qs.append("&");
                qs.append(SearchOption.NO_CAPS.name().toLowerCase());
                qs.append(paramNumber);
                qs.append("=on");
                
            }
            if(leadConfig.getIgnoreMarks()){
                
                qs.append("&");
                qs.append(SearchOption.NO_MARKS.name().toLowerCase());
                qs.append(paramNumber);
                qs.append("=on");
                
            }
        
        return qs.toString();
        
    }
    
    @Override
    public String getCSSSelectorID(){
        
        return super.getCSSSelectorID() + String.valueOf(searchClauses.size());
        
        
    }
    
    @Override
    public void addConstraint(String newValue){
        
        if(newValue.equals(Facet.defaultValue) || "".equals(newValue)) return;
        super.addConstraint(newValue);
        
    }
    
    @Override
    public String[] getFormNames(){

        String[] formNames = new String[searchClauses.size() + 1];
        
        for(int i = 0; i <= searchClauses.size(); i++){
            
            String suffix = i == 0 ? "" : String.valueOf(i);
            String form = formName.name().toString() + suffix;
            formNames[i] = form;
            
            
        }
        
        return formNames;
        
    }
         
    @Override
    public void setWidgetValues(QueryResponse queryResponse){}       
         
    
    @Override
    public String getToolTipText(){
        
        return "Performs a string search.";
        
    }
    
    /**
     * Recursively eturns all the bottom-level <code>SearchTerm</code>s currently being 
     * used by the <code>Facet</code>.
     * 
     * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchTerm
     * @return 
     */
    
    public ArrayList<SearchTerm> getAllSearchTerms(){
        
        
        ArrayList<SearchTerm> allTerms = new ArrayList<SearchTerm>();
        for(Map.Entry<Integer, ArrayList<SearchClause>> entry : searchClauses.entrySet()){
            
            ArrayList<SearchClause> clauses = entry.getValue();
            Iterator<SearchClause> scit = clauses.iterator();
            while(scit.hasNext()){
                
                SearchClause clause = scit.next();
                ArrayList<SearchTerm> someTerms = clause.getConstituentTerms(new ArrayList<SearchTerm>());
                Iterator<SearchTerm> stit = someTerms.iterator();
                while(stit.hasNext()){
                    
                    allTerms.add(stit.next());
                    
                }
                
            }
            
        }
        
        return allTerms;
              
    }
    
    /**
     * Returns all the top-level <code>SearchClause</code>s being used by the <code>Facet</code>
     * 
     * @see info.papyri.dispatch.browse.facet.FacetBrowser#generateHighlightString(java.util.ArrayList) 
     * @return 
     */
    
    public ArrayList<SearchClause> getAllSearchClauses(){
        
        ArrayList<SearchClause> clauses = new ArrayList<SearchClause>();
        
        for(Map.Entry<Integer, ArrayList<SearchClause>> entry : searchClauses.entrySet()){
            
            ArrayList<SearchClause> linkedClauses = entry.getValue();
            clauses.addAll(linkedClauses);
            
        }
        
        return clauses;
        
    }
    
    /**
     * Returns the exceptionLog
     * 
     * @return 
     */
    
    @Override
    public ArrayList<CustomApplicationException> getExceptionLog(){
        
        return exceptionLog;
        
    }
    
    /**
     * Inner class responsible for parsing the search-strings as submitted to the servlet
     * into corresponding <code>SearchClause</code> objects.
     * 
     * As matters stand this class is probably slightly too complex, designed to deal
     * at laest partially with search strings more complicated than that strictly
     * allowed by the interface. However, it forms a useful base to build upon.
     * 
     * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchClause
     * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchTerm
     * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause
     * 
     */
    
     final class SearchClauseFactory{
        
        /**
         * Regular Expression <code>Pattern</code> for detecting the presence of 
         * a proximity search in the servlet search-string
         */
        
        Pattern proxClauseDetect = Pattern.compile(".*(\\bNEAR\\b|\\bTHEN\\b).*", Pattern.CASE_INSENSITIVE);
        
        /**
         * Regular Expression <code>Pattern</code> for detecting the presence of 
         * proximity search metrics in the servlet search-string 
         */
        Pattern proxMetricsDetect = Pattern.compile(".*(~(\\d{1,2})([\\w]+)?)\\s*$");
        
        /**
         * Regular Expression <code>Pattern</code> for pulling out proximity search
         * metrics from the servlet serach-string
         * 
         */
        Pattern justMetricsDetect = Pattern.compile("(~(\\d{1,2})([\\w]+)?)\\s*$");
        
        /**
         * Takes the passed search string and transforms it into corresponding
         * <code>SearchClause</code> objects
         * 
         * 
         * @param searchString
         * @param target
         * @param caps
         * @param marks
         * @return
         * @throws MismatchedBracketException
         * @throws InternalQueryException
         * @throws MalformedProximitySearchException
         * @throws IncompleteClauseException
         * @throws InsufficientSpecificityException
         * @throws RegexCompilationException 
         */
        
        ArrayList<SearchClause> buildSearchClauses(String searchString, SearchTarget target, Boolean caps, Boolean marks) throws MismatchedBracketException, InternalQueryException, MalformedProximitySearchException, IncompleteClauseException, InsufficientSpecificityException, RegexCompilationException{
            
            if(searchString == null) return null;
            ArrayList<SearchClause> clauses = new ArrayList<SearchClause>();
            
            // normalize whitespace
            searchString = searchString.replaceAll("\\s+", " ");
            searchString = searchString.trim();
            if(searchString.length() == 0) return null;
            
            searchString = swapInProxperators(searchString);
            // strip enclosing parens if present
            searchString = trimEnclosingBrackets(searchString);

            Pattern subClauseRegex = Pattern.compile("^\\(.*\\)$");
            
            while(searchString.length() > 0){

                String nowChar = Character.toString(searchString.charAt(0));
                int endIndex = getMatchingIndex(nowChar, searchString);
                if(endIndex == -1){
                    
                    if(nowChar.equals("(")){
                        
                        throw new MismatchedBracketException();
                        
                    }
                    else{
                        
                        endIndex = searchString.length() - 1;
                        
                    }
                    
                }
                endIndex = endIndex + 1;
                Matcher metricsMatch = justMetricsDetect.matcher(searchString.substring(endIndex).trim());
                if(metricsMatch.matches()){ endIndex += (metricsMatch.group(1).length()); }
                String nowWord = searchString.substring(0, endIndex).trim();
                SearchClause newClause = subClauseRegex.matcher(nowWord).matches() ? new SubClause(nowWord, target, caps, marks) : new SearchTerm(nowWord, target, caps, marks);
                clauses.add(newClause);
                searchString = searchString.substring(endIndex).trim();
                               
            }
            
            return clauses;
            
        }
        
        /**
         * Transforms a search-string metrics and operators from front-end syntax to an approximation of
         * Solr syntax.
         * 
         * For example, if a search-string is submitted in the form '(Call THEN Ishmael)~15words',
         * this method will return '(Call 15w Ishamel)'. If a search-string is submitted in the
         * form '(Som NEAR ation)~90chars', this method will return '(Som 90nc ation)'
         * 
         * This syntax does not conform precisely to the Solr <code>SurroundQParser</code> syntax. In
         * the case of word-proximity searches without leading wildcards the translation into Solr syntax 
         * will be exact and the resulting string can be submitted as-is to the Solr server. In the case of
         * proximity searches Solr does not directly support, however, the pseudo-Solr syntax generated 
         * will be further transformed into a regular expression.
         * 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause#doProxTransform(java.util.ArrayList) 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause#doCharsProxTransform(java.util.ArrayList, int) 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause#doWordsProxTransform(java.util.ArrayList, int) 
         * @param fullString
         * @return
         * @throws MalformedProximitySearchException
         * @throws MismatchedBracketException 
         */
        
        String swapInProxperators(String fullString) throws MalformedProximitySearchException, MismatchedBracketException{

            String searchString = fullString;
       /*     // first, we remove any subclauses, and deal with them later
            ArrayList<String> subclauses = suckOutSubClauses(searchString);
            Iterator<String> sit = subclauses.iterator();
            while(sit.hasNext()){
                
                   // found subclauses are replaced with an empty bracket-pair
                   // to mark their place
                   searchString = searchString.replace(sit.next(), "()");
                   
            } 
              
      */
            // check to see whether a proximity search is involved
            Matcher proxMatch = proxClauseDetect.matcher(searchString);
            Matcher metricsMatch = proxMetricsDetect.matcher(searchString);
            if(!proxMatch.matches()){
                
                if(metricsMatch.matches()){
                    // if we have metrics submitted with a non-proximity search,
                    // throw an exception
                    throw new MalformedProximitySearchException();
                    
                }
                // if we don't have any proximity info to worry about,
                // return the search-string unchanged
                return fullString;
                
            }
            String operator = proxMatch.group(1).toUpperCase().equals("NEAR") ? "n" : "w"; 
            String num = metricsMatch.matches() && metricsMatch.group(2) != null && metricsMatch.group(2).length() > 0? metricsMatch.group(2) : "1";
            String unit = metricsMatch.matches() && metricsMatch.group(3) != null && metricsMatch.group(3).length() > 0 && metricsMatch.group(3).equals("chars") ? "c" : "";
            
            // Solr-parseable string, or a reasonable facsimile thereof
            String proxOperator = num + operator + unit;

            searchString = searchString.replace(proxMatch.group(1), proxOperator);
            // trim off the original search metrics indication if required
            if(metricsMatch.matches() && metricsMatch.group(1) != null && metricsMatch.group(1).length() > 0){
                
                searchString = searchString.replace(metricsMatch.group(1), "");
            }

            searchString = transformPhraseSearch(searchString, unit);
            // reinsert the subclauses removed earlier in the method
           /* Iterator<String> sit2 = subclauses.iterator();
            while(sit2.hasNext()){

                searchString = searchString.replaceFirst("\\(\\)", sit2.next());
                
                
            }
*/
            return searchString;
            
        }
        
        /**
         * Transforms proximity searches for phrases into a (pseudo-)Solr syntax within 
         * each phrase.
         * 
         * For example, a user might wish to search for phrases such as "Call me Ishmael" and 
         * "how long precisely" separated by 12 words. At the servlet level, this would
         * be represented as ("Call me Ishmael" THEN "how long precisely")~12words.
         * For such a query to be parsed by Solr, not only does the ~12words indicator
         * needed to be transformed and swapped in for THEN (that is to say, ("Call me
         * Ishmael" 12w "how long precisely"), but the individual phrases need to be
         * transformed similarly into <code>SurroundQParser</code> syntax (that is to say '(Call w me w
         * Ishamel 12w how w long w precisely)')
         * 
         * @param fullSearch
         * @param unit
         * @return 
         */
        
        String transformPhraseSearch(String fullSearch, String unit){
            
            if(!fullSearch.contains("\"") && !fullSearch.contains("'")) return fullSearch;
            Pattern phrasePattern = Pattern.compile("(\"|')([^'\"]+)(\\1)");
            Matcher phraseMatcher = phrasePattern.matcher(fullSearch);
            ArrayList<String> quotedPhrases = new ArrayList<String>();
            ArrayList<String> transformedPhrases = new ArrayList<String>();
            while(phraseMatcher.find()){
                quotedPhrases.add(phraseMatcher.group());
                String phrase = phraseMatcher.group(2);
                String replacement = "c".equals(unit) ? " " : " w ";
                phrase = phrase.replaceAll("\\s+", replacement);
                transformedPhrases.add(phrase);
                
            }
            
            for(int i = 0; i < quotedPhrases.size(); i++){
                
                String quotedPhrase = quotedPhrases.get(i);
                String newPhrase = transformedPhrases.get(i);
                fullSearch = fullSearch.replaceAll(quotedPhrase, newPhrase);
                
            }
            
            return fullSearch;
            
            
        }
        
        /**
         * Removes bracket-delimited subclauses from a clause.
         * 
         * This is required for swapping of Solr proximity syntax into the correct clause in the
         * event of nexted proximity searches
         * 
         * @param searchString
         * @return
         * @throws MismatchedBracketException 
         */
        
        ArrayList<String> suckOutSubClauses(String searchString) throws MismatchedBracketException{
           
            String trimmedString = searchString.trim();//.replaceAll("~\\d{1,2}([^\\s\\d]+)?\\s*$", "");

            if(Character.toString(trimmedString.charAt(0)).equals("(")){
                
                int endIndex = getIndexOfMatchingCloseBracket(trimmedString);
                if(endIndex == -1) throw new MismatchedBracketException();
                trimmedString =  trimmedString.substring(1, endIndex);
            }
            ArrayList<String> subclauses = new ArrayList<String>();
            while(trimmedString.indexOf("(") != -1){
                
                Integer firstBracketIndex = trimmedString.indexOf("(");
                Integer endBracketIndex = firstBracketIndex + getIndexOfMatchingCloseBracket(trimmedString.substring(firstBracketIndex));
                endBracketIndex += 1;
                String clause = trimmedString.substring(firstBracketIndex, endBracketIndex);
                subclauses.add(clause);
                trimmedString = trimmedString.substring(endBracketIndex).trim();
                   
            }
            return subclauses;
        }
        
        /**
         * Removes insignificant brackets - that is to say, those which surround an entire clause.
         * 
         * 
         * @param searchString
         * @return
         * @throws MismatchedBracketException 
         */
        
        String trimEnclosingBrackets(String searchString) throws MismatchedBracketException{
            
            if(!Character.toString(searchString.charAt(0)).equals("(")) return searchString;
            int endBracketIndex = getIndexOfMatchingCloseBracket(searchString);
            if(endBracketIndex == -1){ throw new MismatchedBracketException(); }
            if(endBracketIndex == searchString.length() - 1){
                
                return searchString.substring(1, endBracketIndex);
                
            }
            
            return searchString;
                   
        }
        
        /**
         * Returns the index of a clause-delimiter's matching delimiter.
         * 
         * This will typically be a quotation mark, a bracket, or a space character.
         * 
         * @param startChar
         * @param remainder
         * @return 
         */
        Integer getMatchingIndex(String startChar, String remainder){
            
            if("(".equals(startChar)) return getIndexOfMatchingCloseBracket(remainder);
            if("\"".equals(startChar) || "'".equals(startChar)){
                
                int endIndex = remainder.indexOf(startChar, 1);
                if(endIndex != -1) return endIndex;
                
            }
            
            return remainder.indexOf(" ", 1);
            
        }
        
        /**
         * Returns the index of an open-bracket's matching close bracket.
         * 
         * This method needs to be more complex than getMatchingIndex, above, because
         * of the possibility of nested brackets.
         * 
         * 
         * @param remainder
         * @return 
         */
        
        Integer getIndexOfMatchingCloseBracket(String remainder){
        
            int pos = -1;
            int bracketCount = 1;
            for(int i = 1; i < remainder.length(); i++){
                
                String nowChar = Character.toString(remainder.charAt(i));
                if("(".equals(nowChar)){
                    bracketCount += 1;
                } else if(")".equals(nowChar)) {

                    bracketCount -= 1;
                }
                if(bracketCount == 0 && pos == -1){
                    
                    pos = i;
                }
                
                
            }
            if(bracketCount != 0) return -1;
            return pos;
        
        }
                
    }
    
    
    /**
     * Inner class responsible, with its subclasses, for handling Solr-level string-search logic.
     * 
     */
    
    public abstract class SearchClause{
        
        /**
         * The original string constituting the clause as submitted to the server
         */
        String originalString;
        /**
         * The original string, appropriately transformed for use in a query to the 
         * Solr server
         */
        String transformedString;
        /**
         * The list of sub-clauses of which the clause is composed
         */
        ArrayList<SearchClause> clauseComponents;
        /**
         * The list of roles played by the clause
         * 
         * For instance, a clause might be both a regular expression (ClauseRole.REGEX) and
         * for part of a proximity clause (ClauseRole.START_PROX or ClauseRole.END_PROX)
         * 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.ClauseRole
         */
        ArrayList<ClauseRole> clauseRoles;
        /**
         * Whether or not capitalisation is relevant
         */
        Boolean ignoreCaps;
        /**
         * Whether or not diacritics are relevant
         */
        Boolean ignoreMarks;
        /**
         * The field(s) to be searched.
         * 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchTarget
         */
        SearchTarget target;
        /**
         * Input string indicating that the clause that follows is to be lemmatised
         * 
         */
        private String LEX_MARKER = "LEX";
        /**
         * Input string indicating that the clause that follows is a regular expression
         */
        private String REGEX_MARKER = "REGEX";
        
        
        SearchClause(String rs, SearchTarget tg, Boolean caps, Boolean marks) throws InsufficientSpecificityException, InternalQueryException, MismatchedBracketException, MalformedProximitySearchException, IncompleteClauseException, RegexCompilationException{
            
            originalString = rs;
            target = tg;
            ignoreCaps = caps;
            ignoreMarks = marks;
            clauseComponents = hasSubComponents(rs) ? clauseComponents = CLAUSE_FACTORY.buildSearchClauses(rs, tg, caps, marks) : new ArrayList<SearchClause>();
            clauseRoles = new ArrayList<ClauseRole>();
            clauseComponents = assignClauseRoles(clauseComponents);
        }
        
        /**
         * Determines whether a clause consists of subclauses, or is itself a minimal clause.
         * 
         * A 'minimal clause' is the smallest unit that can be submitted to the Solr server with the semantics intact
         * 
         * For phrase queries, this will be a quote-delimited string
         * For all other queries it will be a whitespace-separated string of non-whitespace characters (though
         * further processing of this may be necessary before it can be submitted in query form).
         * 
         * 
         * @param rs
         * @return 
         */
        
       final Boolean hasSubComponents(String rs){
           
           rs = rs.trim();
           String startChar = Character.toString(rs.charAt(0));
           // quote-delimited means false
           if(startChar.equals("\"") || startChar.equals("'")){
               
               if(Character.toString(rs.charAt(rs.length() - 1)).equals(startChar)) return false;
               
           }
           // non-quote-delimited with internal whitespace means true
           if(rs.contains(" ")) return true;
           // internal brackets means true
           if((rs.contains("(")||rs.contains(")")) && !rs.contains("\\")) return true;
           return false;
           
       }
        
       
       /**
        * Returns the clause's clause roles
        * 
        * @return 
        */
       
       public ArrayList<ClauseRole> getClauseRoles(){
            
           return clauseRoles;
            
       }
       
       /**
        * Returns the clause roles of the clause, and all of its subclauses
        * 
        * @return 
        */
       
       public ArrayList<ClauseRole> getAllClauseRoles(){
           
           ArrayList<ClauseRole> allRoles = new ArrayList<ClauseRole>();
           Iterator<ClauseRole> scrit = clauseRoles.iterator();
           while(scrit.hasNext()){
               
               ClauseRole scr = scrit.next();
               if(!allRoles.contains(scr)) allRoles.add(scr);
              
           }
           ArrayList<ClauseRole> subRoles = getSubordinateClauseRoles();
           Iterator<ClauseRole> srit = subRoles.iterator();
           while(srit.hasNext()){
               
               ClauseRole sr = srit.next();
               if(!allRoles.contains(sr)) allRoles.add(sr);
               
           }
           
           return allRoles;
           
       }
       
       String getOriginalString(){
           
           return originalString;
           
       }
       
       /**
        * Returns the clause's subclauses. 
        * 
        * @return 
        */
       
       ArrayList<SearchClause> getClauseComponents(){
           
           return clauseComponents;
           
       }
       
       final void addClauseRole(ClauseRole role){
           
           if(!clauseRoles.contains(role)){
               
               clauseRoles.add(role);
               
               if(clauseRoles.contains(ClauseRole.DEFAULT) && role != ClauseRole.DEFAULT){
                   
                   clauseRoles.remove(ClauseRole.DEFAULT);
                   
               }
               
           }
           
       }
       
       /**
        * Determines the index in the clause list of the nearing following operator from a 
        * given starting point in the list.
        * 
        * @param start
        * @param clauses
        * @return 
        */
       
       Integer getIndexOfNextOperand(Integer start, ArrayList<SearchClause> clauses){
           
           for(int i = start; i < clauses.size(); i++){
               
               SearchClause clause = clauses.get(i);
               if(!clause.isOperator()) return i;            
               
           }
           
           return -1;
           
       }
       
       /**
        * Determines the index in the clause list of the nearest preceding operator from a 
        * given starting point in the list.
        * 
        * Required because in the search syntax some operators (the proximity operators, AND, and OR)
        * take two operands, one following and one preceding the operator
        * 
        * @param start
        * @param clauses
        * @return 
        */
       
       Integer getIndexOfPreviousOperand(Integer start, ArrayList<SearchClause> clauses){
                    
           for(int i = start; i >= 0; i--){
               
               SearchClause clause = clauses.get(i);
               if(!clause.isOperator()) return i;
               
           }
           
           return -1;
           
       }
       
       /**
        * Returns whether or not the clause consists solely of a search operator
        * 
        * @return 
        */
       
       public Boolean isOperator(){
           
           try{
               
               SearchOperator.valueOf(getOriginalString());
               return true;
               
           }
           catch(IllegalArgumentException iae){
               
               Matcher proxMatcher = PROX_OPERATOR_REGEX.matcher(getOriginalString());
               if(proxMatcher.matches()) return true;
               
               
           }
           
           return false;
           
       } 
       
       /**
        * Determines the <code>SearchType</code> of a clause, depending on the search target,
        * clause roles, and characteristics of the search-string itself.
        * 
        * The rules governing the determination of search-type are:
        * (i)   Searches on metadata and translation fields are SearchType.PHRASE searches
        * (ii)  Lemma, Regex, and Proximity searches are all distinct types of search
        * (iii) Quote-delimited strings are SearchType.PHRASE searches
        * (iv)  In the absence of the above, the default search-type is SearchType.SUBSTRING
        * 
        * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchType
        * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchTarget
        * @see info.papyri.dispatch.browse.facet.StringSearchFacet.ClauseRole
        * 
        * @return 
        */
       
       public SearchType parseForSearchType(){
           
            SearchType type = SearchType.SUBSTRING;
            if(SearchTarget.USER_DEFINED == target) return SearchType.USER_DEFINED;
            if(SearchTarget.METADATA == target || SearchTarget.TRANSLATION == target) return SearchType.PHRASE;
            ArrayList<ClauseRole> allRoles = getAllClauseRoles();
            if(allRoles.contains(ClauseRole.LEMMA)) return SearchType.LEMMA;
            if(allRoles.contains(ClauseRole.REGEX)) return SearchType.REGEX;
            if(allRoles.contains(ClauseRole.START_PROX)) return SearchType.PROXIMITY;
            if(originalString.matches(PHRASE_MARKER)) return SearchType.PHRASE;
            return type;
                  
       }
       
       /**
        * Determines the <code>SolrField</code> to search on, depending on the search target, the search type,
        * and whether capitalisation and diacritics are significant.
        * 
        * The rules governing the determination of search field are
        * (i)   In cases where the user has explicitly stated a field in the query string, this field will be used
        * (ii)  Searches for metadata will use the dedicated SolrField.metadata field
        * (iii) Searches on translations will use the dedicated SolrField.translation field
        * (iv)  Lemmatised searches function only on the SolrField.transcription_ia field
        * (v)   Substring searches will be performed on the transcription_ngram fields, which will
        * be transcription_ngram_ia, _ic, _id, or simply transcription_ngram, depending on whether 
        * both capitalisation and marks are ignored, only caps, only marks, or neither
        * (vi)  Regular expression searching will be performed on the untokenized fields, which follow the
        * same naming conventions as transcription_ngram fields.
        * (vii) The default search field is SolrField.transcription_ngram_ia
        * 
        * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchType
        * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchTarget
        * @see info.papyri.dispatch.browse.SolrField
        *  
        * @param searchType
        * @param searchTarget
        * @param caps
        * @param marks
        * @return 
        */
       
       SolrField parseForField(SearchType searchType, SearchTarget searchTarget, Boolean caps, Boolean marks){
           
           if(searchType == SearchType.USER_DEFINED) return null;
           if(searchTarget == SearchTarget.METADATA) return SolrField.metadata;
           if(searchTarget == SearchTarget.TRANSLATION) return SolrField.translation;
           if(searchType == SearchType.LEMMA) return SolrField.transcription_ia;
           String suffix = (caps && marks) ? "_ia" : (caps ? "_ic" : "_id" );
           String prefix = "transcription";
           if(searchType == SearchType.SUBSTRING){
               
               prefix = "transcription_ngram";
               
           }
           else if(searchType == SearchType.REGEX){
               
               prefix = "untokenized";
               
           }
           
           try{
               
               return SolrField.valueOf(prefix + suffix);
               
           }
           catch(IllegalArgumentException iae){
               
               return SolrField.transcription_ngram_ia;
               
           }
                   
       }
       
       /**
        * Determines the Solr search handler to use based on the clause's <code>ClauseRole</code>s
        * 
        * The rules for determining the search handler are:
        * (i)   Regular expression searches must use the SearchHandler.REGEXP handler
        * (ii)  Proximity searches must use the SearchHandler.SURROUND handler
        * (iii) All other search types use the default search handler.
        * 
        * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchHandler
        * @see info.papyri.dispatch.browse.facet.StringSearchFacet.ClauseRole
        * @param roles
        * @return 
        */
       
       SearchHandler parseForSearchHandler(ArrayList<ClauseRole> roles){
           
           if(roles.size() < 1) return null;
           if(roles.contains(ClauseRole.REGEX)) return SearchHandler.REGEXP;
           if(roles.contains(ClauseRole.START_PROX)) return SearchHandler.SURROUND;
           return SearchHandler.DEFAULT;
           
       }
       
       /**
        * Combines the search handler and seach field into a prefix to the search-string
        * parseable by Solr.
        * 
        * @param sh
        * @param field
        * @return 
        */
       
       String getQueryPrefix(SearchHandler sh, SolrField field){
           
           if(field == null) return "";
           StringBuilder prefix = new StringBuilder();
           if(sh == SearchHandler.REGEXP){
               
               prefix.append("{!");
               prefix.append(sh.name().toLowerCase());
               prefix.append(" cache=false qf=\"");
               prefix.append(field.name().toLowerCase());
               prefix.append("\"}");
               return prefix.toString();
               
           }
           else if(sh == SearchHandler.SURROUND){
               
               prefix.append("{!");
               prefix.append(sh.name().toLowerCase());
               prefix.append(" cache=false}");              
               
               
           }
           else if(this.getAllClauseRoles().contains(ClauseRole.NOT)){
               
               prefix.append("-");
               
           }
           prefix.append(field.name().toLowerCase());
           prefix.append(":"); 
           return prefix.toString();
           
       }
       
       /**
        * Converts the clause into a Solr-parseable filter query and adds this to the
        * passed <code>SolrQuery</code> object
        * 
        * @param sq
        * @return
        * @throws InternalQueryException
        * @throws IncompleteClauseException
        * @throws RegexCompilationException
        * @throws InsufficientSpecificityException 
        */

       SolrQuery buildQuery(SolrQuery sq) throws InternalQueryException, IncompleteClauseException, RegexCompilationException, InsufficientSpecificityException{
           
           SearchType type = parseForSearchType();
           SolrField field = parseForField(type, target, ignoreCaps, ignoreMarks);
           SearchHandler handler = parseForSearchHandler(getAllClauseRoles());
           String queryPrefix = getQueryPrefix(handler, field);
           String queryBody = buildTransformedString();
           queryBody = "(" + queryBody + ")";
           sq.addFilterQuery(queryPrefix + queryBody);
           return sq;
           
       }
       
       /**
        * Tidies the search string for display to the user
        * 
        * @return 
        */
       
       String getDisplayString(){
           
           String o = this.getOriginalString().trim();
           o = o.replaceAll(REGEX_MARKER, "");
           if(this.parseForSearchType() == SearchType.LEMMA) o = o.replaceAll(LEX_MARKER, "");
           if(Character.toString(o.charAt(0)).equals("(") && Character.toString(o.charAt(o.length() - 1)).equals(")")){
               
               String test = o.substring(1, o.length() - 1);
               if(!test.contains("(") && !test.contains(")")) return test.trim();
               
           }
           return o;
           
           
       }
       
       /**
        * Determines which operators are relevant to which operands and assigns operand 
        * clause-roles on this basis.
        * 
        * @param subclauses
        * @return
        * @throws IncompleteClauseException 
        */
       
       
       final ArrayList<SearchClause> assignClauseRoles(ArrayList<SearchClause> subclauses) throws IncompleteClauseException{
           
           if(this.getClauseRoles().isEmpty())addClauseRole(ClauseRole.DEFAULT);
           for(int i = 0; i < subclauses.size(); i++){
               
               SearchClause clause = subclauses.get(i);
               if(clause.isOperator()){
                   
                   clause.addClauseRole(ClauseRole.OPERATOR);
                   
                   try{
                       
                       SearchOperator op = SearchOperator.valueOf(clause.getOriginalString().trim());
                       if(op == SearchOperator.OR){
                           
                           int prevOperand = getIndexOfPreviousOperand(i, subclauses);
                           int nextOperand = getIndexOfNextOperand(i, subclauses);
                           if(prevOperand == -1 || nextOperand == -1) throw new IncompleteClauseException();
                           subclauses.get(prevOperand).addClauseRole(ClauseRole.OR);
                           subclauses.get(nextOperand).addClauseRole(ClauseRole.OR);
                           
                       }
                       else{

                           int nextOperand = getIndexOfNextOperand(i, subclauses);
                           if(nextOperand == -1) throw new IncompleteClauseException();
                           String opName = op.name();
                           if(opName.equals(SearchOperator.LEX.name())) opName = ClauseRole.LEMMA.name();
                           ClauseRole role = ClauseRole.valueOf(opName);
                           subclauses.get(nextOperand).addClauseRole(role);
                           
                       }
                       
                   }
                   catch(IllegalArgumentException iae){
                       
                       int prevOperand = getIndexOfPreviousOperand(i, subclauses);
                       int nextOperand = getIndexOfNextOperand(i, subclauses);
                       if(prevOperand == -1 || nextOperand == -1) throw new IncompleteClauseException();
                       subclauses.get(prevOperand).addClauseRole(ClauseRole.START_PROX);
                       subclauses.get(nextOperand).addClauseRole(ClauseRole.END_PROX);                   
                       
                   }
                    
               }
               else{
                   
                   clause.assignClauseRoles(clause.getClauseComponents());              
                   
               }           
               
           }
           
           return subclauses;
           
       }       
       /**
        * Returns the <code>ClauseRole</code>s of all subordinate clauses, but <em>not</em>
        * the roles of the clause itself.
        * 
        * 
        * @return 
        */
              
       abstract ArrayList<ClauseRole> getSubordinateClauseRoles();
       
       /**
        * Returns whether or not the clause consists solely of pseudo-Solr syntax
        * specifying a character proximity search.
        * 
        * Such clauses take the form \d{1,2}(n|w)c, where the digits indicate the number of
        * characters, n indicates a NEAR search, w indicates a THEN search, and c
        * is a flag indicating that characters rather than words are the search unit.
        * 
        * @return 
        */
       
       abstract Boolean isCharactersProxTerm();
       
       /**
        * Returns whether or not the clause consists solely of Solr syntax specifying
        * a word proximity search.
        * 
        * Such clauses take the form \d{1,2}(n|w), where the digits indicate the number of
        * words, n indicates a NEAR search, and w indicates a THEN search.
        * 
        * 
        * @return 
        */
       
       abstract Boolean isWordsProxTerm();
       
       /**
        * Returns whether or not the clause contains a term beginning with a wildcard character ('?' or '*')
        * 
        * @return 
        */
       
       abstract Boolean containsLeadingWildcard();
       
       /**
        * Transforms the clause's original search-string into a form directly usable in a
        * query to Solr.
        * 
        * 
        * @return
        * @throws InternalQueryException
        * @throws IncompleteClauseException
        * @throws RegexCompilationException
        * @throws InsufficientSpecificityException 
        */
             
       abstract public String buildTransformedString() throws InternalQueryException, IncompleteClauseException, RegexCompilationException, InsufficientSpecificityException;
        
       /**
        * Returns all the individual <code>SearchTerm</code>s of which a <code>SearchClause</code> is made up,
        * in order.
        * 
        * 
        * @param terms
        * @return 
        */
       
       abstract ArrayList<SearchTerm> getConstituentTerms(ArrayList<SearchTerm> terms);
       
       /* getters and setters */
       SearchTarget getSearchTarget(){ return target; }
       Boolean getIgnoreCaps(){ return ignoreCaps; }
       Boolean getIgnoreMarks(){ return ignoreMarks; }
       public String getTransformedString(){ return transformedString; }
        
    }
    
    /**
     * The <code>SubClause<code> class models any <code>SearchClause</code> that in the first instance consists
     * of more than one <code>SearchTerm</code>, 
     * 
     * <code>SubClause</code> objects stand in a HAS-A relationship with their constituent
     * <code>SearchClause</code> parts via the <code>clauseComponents ArrayList</code>. In principle,
     * these parts may be either <code>SearchTerm</code>s or other <code>SubClause</code>s themselves
     * in a fully recursive manner. However, because nested search-string parsing is not yet supported, the 
     * elements of the <code>clauseComponents</code> list will in fact always be <code>SearchTerm</code>s.
     * 
     * Note should be taken of the qualification 'in the first instance' found in the opening sentence, as processing
     * will often reduce what was submitted as a multi-term search clause to a single term. In particular,
     * some kinds of complex searches (namely character-proximity and word-proximity-with-leading-wildcards)
     * are converted to single-term regex representations for the purposes of querying Solr.
     * 
     */
    
    
    public class SubClause extends SearchClause{
            
        ArrayList<SearchClause> transformedClauses = new ArrayList<SearchClause>();
        
        SubClause(String rs, SearchTarget tg, Boolean caps, Boolean marks) throws MismatchedBracketException, InternalQueryException, InsufficientSpecificityException, MalformedProximitySearchException, IncompleteClauseException, RegexCompilationException{
            
            super(rs, tg, caps, marks);
            transformedClauses = doProxTransform(clauseComponents);
            
        }
        
        @Override
        public String buildTransformedString() throws InternalQueryException, IncompleteClauseException, RegexCompilationException, InsufficientSpecificityException{
            
            StringBuilder transformed = new StringBuilder();
            Iterator<SearchClause> scit = transformedClauses.iterator();
            while(scit.hasNext()){
                
                SearchClause clause = scit.next();
                String clauseContent = clause.buildTransformedString().trim();
                transformed.append(clauseContent);
                if(!clauseContent.equals("")) transformed.append(" ");
                
                
            }
            
            transformedString = transformed.toString().trim();
            return transformedString;
            
        }
        
        /**
         * Checks to see whether the clause is a proximity search requiring transformation into a regex search and, if so,
         * performs the transformation.
         * 
         * While Solr handles most word-proximity searches natively, character-proximity searches and word-proximity 
         * searches with leading wildcards need to be converted into regular expressions in order to work.
         * 
         * @param clauses
         * @return
         * @throws InternalQueryException
         * @throws InsufficientSpecificityException
         * @throws IncompleteClauseException
         * @throws RegexCompilationException 
         */
        
        final ArrayList<SearchClause> doProxTransform(ArrayList<SearchClause> clauses) throws InternalQueryException, InsufficientSpecificityException, IncompleteClauseException, RegexCompilationException{
            
            Integer chpIndex = getCharsProxIndex(clauses);
            if(chpIndex != -1) return doCharsProxTransform(clauses, chpIndex);
            Integer wdIndex = getWordsProxIndex(clauses);
            if(wdIndex != -1 && containsLeadingWildcard()) return doWordsProxTransform(clauses, wdIndex);
            return clauses;
    
        }
        
        /**
         * Converts character-proximity searches into regular expression syntax
         * 
         * 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause#doProxTransform(java.util.ArrayList) 
         * @param clauses
         * @param chpIndex
         * @return
         * @throws InternalQueryException
         * @throws InsufficientSpecificityException
         * @throws IncompleteClauseException
         * @throws RegexCompilationException 
         */
       
        final ArrayList<SearchClause> doCharsProxTransform(ArrayList<SearchClause> clauses, int chpIndex) throws InternalQueryException, InsufficientSpecificityException, IncompleteClauseException, RegexCompilationException{
            
            ArrayList<SearchClause> proxClauses = new ArrayList<SearchClause>();
            Integer startIndex = getProxStartTerm(chpIndex, clauses);
            Integer endIndex = getProxPostTerm(chpIndex, clauses);
            if(startIndex == -1 || endIndex == -1) throw new IncompleteClauseException();
            String startString = buildProxStartString(clauses, chpIndex);
            String endString = buildProxEndString(clauses, chpIndex);
            String regex = convertCharProxToRegexSyntax(startString, endString, clauses.get(chpIndex).originalString);

            try{
            
                SearchTerm regexSearchTerm = new SearchTerm(regex, target, ignoreCaps, ignoreMarks);
                regexSearchTerm.addClauseRole(ClauseRole.REGEX);
                proxClauses.add(regexSearchTerm);
                
            } catch(StringSearchParsingException sspe){
                
                throw new RegexCompilationException();
                
            }
            return proxClauses;
            
        }
        
        /**
         * Transforms leading-wildcards in word-proximity searches into regular expression syntax
         * 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause#doProxTransform(java.util.ArrayList) 
         * @param clauses
         * @param metricIndex
         * @return
         * @throws InternalQueryException
         * @throws InsufficientSpecificityException
         * @throws IncompleteClauseException
         * @throws RegexCompilationException 
         */
        final ArrayList<SearchClause> doWordsProxTransform(ArrayList<SearchClause> clauses, int metricIndex) throws InternalQueryException, InsufficientSpecificityException, IncompleteClauseException, RegexCompilationException{
            
            ArrayList<SearchClause> proxClauses = new ArrayList<SearchClause>();
            Integer startIndex = getProxStartTerm(metricIndex, clauses);
            Integer endIndex = getProxPostTerm(metricIndex,clauses);
            if(startIndex == -1 || endIndex == -1) throw new IncompleteClauseException();
            String startString = buildProxStartString(clauses, metricIndex);
            String endString = buildProxEndString(clauses, metricIndex);
            String regex = convertWordProxToRegexSyntax(startString, endString, clauses.get(metricIndex).originalString);

            try{

                SearchTerm regexSearchTerm = new SearchTerm(regex, target, ignoreCaps, ignoreMarks);
                regexSearchTerm.addClauseRole(ClauseRole.REGEX);
                proxClauses.add(regexSearchTerm);
                
            }
            catch(StringSearchParsingException sspe){

                throw new RegexCompilationException();
                
            }

            return proxClauses;
            
        }
        
        /**
         * Transforms the portion of a proximity-search before the proximity operator (NEAR | THEN) into Solr-parseable form
         * 
         * 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause#doCharsProxTransform(java.util.ArrayList, int) 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause#doWordsProxTransform(java.util.ArrayList, int)          * 
         * @param clauses
         * @param charProxIndex
         * @return
         * @throws InternalQueryException
         * @throws IncompleteClauseException
         * @throws RegexCompilationException
         * @throws InsufficientSpecificityException 
         */
        
        String buildProxStartString(ArrayList<SearchClause> clauses, int charProxIndex) throws InternalQueryException, IncompleteClauseException, RegexCompilationException, InsufficientSpecificityException{

            StringBuilder startBuilder = new StringBuilder();
            for(int i = 0; i < charProxIndex; i++){
                
                SearchClause clause = clauses.get(i);
                if(clause.getClauseRoles().contains(ClauseRole.OPERATOR)) continue;
                if(clause.getClauseRoles().contains(ClauseRole.LEMMA)){
                    
                    String lemmata = clause.buildTransformedString();
                    lemmata = lemmata.replaceAll("\\s?OR\\s?", "|");
                    startBuilder.append(lemmata);
                    
                }
                else{
                    
                    startBuilder.append(this.convertProxWildcards(clause.getOriginalString(), clauses.get(charProxIndex).getOriginalString(), i, clauses.size()));
                    
                }
                
                startBuilder.append(" ");
                
            }
            
            return startBuilder.toString().trim();
            
        }

        /**
         * Transforms the portion of a proximity-search after the proximity operator (NEAR | THEN) into Solr-parseable form 
         * 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause#doCharsProxTransform(java.util.ArrayList, int) 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause#doWordsProxTransform(java.util.ArrayList, int)          * 
         * @param clauses
         * @param charProxIndex
         * @return
         * @throws InternalQueryException
         * @throws IncompleteClauseException
         * @throws RegexCompilationException
         * @throws InsufficientSpecificityException 
         */
                
        String buildProxEndString(ArrayList<SearchClause> clauses, int charProxIndex) throws InternalQueryException, IncompleteClauseException, RegexCompilationException, InsufficientSpecificityException{
            
            StringBuilder endBuilder = new StringBuilder();
            for(int i = charProxIndex + 1; i < clauses.size(); i++){
                
                SearchClause clause = clauses.get(i);
                if(clause.getClauseRoles().contains(ClauseRole.OPERATOR)) continue;
                if(clause.getClauseRoles().contains(ClauseRole.LEMMA)){
                    
                    String lemmata = clause.buildTransformedString();
                    lemmata = lemmata.replaceAll("\\s?OR\\s?", "|");
                    endBuilder.append(lemmata);
                    
                }
                else{
                    
                    endBuilder.append(this.convertProxWildcards(clause.getOriginalString(), clauses.get(charProxIndex).getOriginalString(), i, clauses.size()));
                    
                }
                
                endBuilder.append(" ");
                
            }
                      
            return endBuilder.toString().trim();    
            
        }
        
        /**
         * Converts wildcard syntax into regular expression syntax for proximity searches
         * 
         * 
         * @param rawString
         * @param proxClause
         * @param iteration
         * @param totalLength
         * @return 
         */
        
        String convertProxWildcards(String rawString, String proxClause, int iteration, int totalLength){
            
            if(CHAR_PROX_TERM_REGEX.matcher(proxClause).matches()){
                
                return convertCharsProxWildcards(rawString);
                
            }
            
            return convertWordProxWildcards(rawString, iteration, totalLength);
            
        }
        
        /**
         * Converts wildcard syntax into regular expression syntax for character-proximity
         * searches.
         * 
         * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SubClause#convertProxWildcards(java.lang.String, java.lang.String, int, int) 
         * @param rawString
         * @return 
         */
        
        String convertCharsProxWildcards(String rawString){
            
            rawString = rawString.replaceAll("\\*", "");    // 
            rawString = rawString.replaceAll("\\?", ".");
            return rawString;
            
        }
        
        /**
         * Converts wildcard syntax into regular expression syntax for leading-wildcard 
         * word-proximity searches.
         * 
         * The complexity of ths method compared to the <code>convertCharsProxWildcards</code> method
         * arises because word-proximity searches, unlike character-proximity searches, involve the notions
         * both of words and word boundaries, both of which need to be reflected in the regex
         * expression produced. 
         * 
         * @param rawString
         * @param iteration
         * @param totalLength
         * @return 
         */
        
        String convertWordProxWildcards(String rawString, int iteration, int totalLength){

            StringBuilder asRegex = new StringBuilder();
            int end = rawString.length() - 1;
            
            for(int i = 0; i <= end; i++){
                
                String nowChar = Character.toString(rawString.charAt(i));
                
                if(i == 0){

                    if(!nowChar.equals("?") && !nowChar.equals("*")){
                        
                        if(iteration == 0) asRegex.append("\\b");
                        asRegex.append(nowChar);
                        continue;
                        
                    }
                    asRegex.append("\\b\\p{L}");
                    if(nowChar.equals("*")) asRegex.append("+");
                    continue;
                            
                }
                else if(i == end){
                    
                    if(!nowChar.equals("?") && !nowChar.equals("*")){
                        
                        asRegex.append(nowChar);
                        if(iteration == totalLength - 1) asRegex.append("\\b");
                        continue;
                        
                    }
                    asRegex.append("\\p{L}");
                    if(nowChar.equals("*")) asRegex.append("+");
                    asRegex.append("\\b");
                    
                }
                else{
                    
                    if(!nowChar.equals("?") && !nowChar.equals("*")){
                        
                        asRegex.append(nowChar);
                        continue;
                        
                    }
                    asRegex.append("\\p{L}");
                    if(nowChar.equals("*")) asRegex.append("+");
                    
                    
                }
    
                
            }
            
            return asRegex.toString();
            
            
        }
        
        /**
         * Converts an entire character-proximity search to regex syntax
         * 
         * Note the difference between this and the <code>buildProxStartStartString</code> and
         * <code>buildProxEndString</code> methods. Those methods are for the <strong>operands</strong>
         * while this method pulls together the entire clause into a single regex
         * 
         * 
         * @param prevTerm
         * @param nextTerm
         * @param charProx
         * @return
         * @throws RegexCompilationException 
         */
        
        String convertCharProxToRegexSyntax(String prevTerm, String nextTerm, String charProx) throws RegexCompilationException{
            
            Matcher charProxMatcher = CHAR_PROX_REGEX.matcher(charProx);
            if(!charProxMatcher.matches()) throw new RegexCompilationException();
            String numChars = charProxMatcher.group(1);
            String operator = charProxMatcher.group(2);
            String distRegex = ".{1," + numChars + "}";
            prevTerm = prevTerm.replaceAll("\\^", "\\\\b");
            nextTerm = nextTerm.replaceAll("\\^", "\\\\b");
            String regex = prevTerm.trim() + distRegex + nextTerm.trim();
            if(operator.equals("w")) return regex;
            String revRegex = nextTerm.trim() + distRegex + prevTerm.trim();
            String nearRegex = "(" + revRegex + "|" + regex + ")";
            return nearRegex;
            
        }
        
        /**
         * Converts word-proximity operators to regex syntax.
         * 
         * Note the difference between this and the <code>buildProxStartStartString</code> and
         * <code>buildProxEndString</code> methods. Those methods are for the <strong>operands</strong>
         * while this method pulls together the entire clause into a single regex
         * 
         * 
         * @param prevTerm
         * @param nextTerm
         * @param wordProx
         * @return
         * @throws RegexCompilationException 
         */
        
        String convertWordProxToRegexSyntax(String prevTerm, String nextTerm, String wordProx) throws RegexCompilationException{
            
            Matcher wordProxMatcher = WORD_PROX_REGEX.matcher(wordProx);
            if(!wordProxMatcher.matches()) throw new RegexCompilationException();          
            Integer numWords = Integer.valueOf(wordProx.substring(0, wordProx.length() - 1));
            String operator = wordProx.substring(wordProx.length() - 1).equals("n") ? "n" : "w";
            String distRegex = "(\\p{L}+\\s){0," + numWords + "}";
            prevTerm = prevTerm.replaceAll("\\^", "\\\\b");
            nextTerm = nextTerm.replaceAll("\\^", "\\\\b");
            String regex = prevTerm.trim() + "\\s" + distRegex + nextTerm.trim();
            if(operator.equals("w")) return regex;
            String revRegex = nextTerm.trim() + "\\s" + distRegex + prevTerm.trim();
            String nearRegex = "(" + regex + "|" + revRegex + ")";
            return nearRegex;
            
            
        }
        
        /**
         * Returns the index of the character-proximity operator within a list of subclause
         * <code>SearchTerm</code>s
         * 
         * @param clauses
         * @return 
         */
        
        Integer getCharsProxIndex(ArrayList<SearchClause> clauses){
            
            for(int i = 0; i < clauses.size(); i++){
                
                if(clauses.get(i).isCharactersProxTerm()) return i;
                
            }
            
            return -1;
            
        }
        
        /**
         * Returns the index of the word-proximity operator within a list of subclause
         * <code>SearchTerm</code>s
         * 
         * @param clauses
         * @return 
         */
        
        Integer getWordsProxIndex(ArrayList<SearchClause> clauses){
            
            for(int i = 0; i < clauses.size(); i++){
                             
                if(clauses.get(i).isWordsProxTerm()) return i;
                
            }
            
            return -1;
        }
        
        /**
         * Returns the index of the first operand of a proximity operator in a list 
         * containing the constituent <code>SearchClause</code>s of a proximity search.
         * 
         * Because nested proximity searches are currently not supported, this method
         * will always return either 0 (if the search is a valid proximity search) or 
         * -1 (if no first proximity operand can be found).
         * 
         * @param start
         * @param clauses
         * @return 
         */
        Integer getProxStartTerm(Integer start, ArrayList<SearchClause> clauses){
            
            for(int i = start; i >= 0; i--){
                
                 if(clauses.get(i).getClauseRoles().contains(ClauseRole.START_PROX)) return 0;
               
            }
            
            return -1;
            
        }
        
        /**
         * Returns the index of the second operand of a proximity operator in a list
         * containing the constituent <code>SearchClause</code>s of a proximity search.
         * 
         * Because nested proximity searches are currently not supported, this method will always
         * return either the index of the last element in the list (if the search is a 
         * valid proximity search) or -1 (if no second proximity operand can be found)
         * 
         * @param start
         * @param clauses
         * @return 
         */
        
        Integer getProxPostTerm(Integer start, ArrayList<SearchClause> clauses){
            
            for(int i = start; i < clauses.size(); i++){
                
                  if(clauses.get(i).getClauseRoles().contains(ClauseRole.END_PROX)) return clauses.size() - 1;
              
            }
            
            return -1;
        }
        
        @Override
        Boolean containsLeadingWildcard(){
            
            Iterator<SearchClause> scit = this.getClauseComponents().iterator();
            while(scit.hasNext()){
                
                SearchClause sc = scit.next();
                if(sc.containsLeadingWildcard()) return true;
                
            }
            
            return false;
            
        }
        
        @Override
        ArrayList<ClauseRole> getSubordinateClauseRoles(){
           
           ArrayList<ClauseRole> subroles = new ArrayList<ClauseRole>();
           
           Iterator<SearchClause> scit = transformedClauses.iterator();
           while(scit.hasNext()){
               
               SearchClause clause = scit.next();
               Iterator<ClauseRole> scrit = clause.getClauseRoles().iterator();
               while(scrit.hasNext()){
                   
                   ClauseRole scrole = scrit.next();
                   if(!subroles.contains(scrole)) subroles.add(scrole);
                                   
               }             
               
           }
           
           return subroles;
           
       }
        
       @Override
       ArrayList<SearchTerm> getConstituentTerms(ArrayList<SearchTerm> terms){
           
           Iterator<SearchClause> scit = clauseComponents.iterator();
           while(scit.hasNext()){
               
               SearchClause clause = scit.next();
               clause.getConstituentTerms(terms);
               
           }
           
           return terms;
           
       }
        
        @Override
        Boolean isCharactersProxTerm(){ return false; }
        
        @Override
        Boolean isWordsProxTerm(){ return false; };
        
    }
    
    /**
     * The <code>SearchTerm</code> class models atomic search elements, which may be either
     * search terms in the conventional sense or search operators.
     * 
     */
    
    public class SearchTerm extends SearchClause{
        

        SearchTerm(String rs, SearchTarget tg, Boolean caps, Boolean marks) throws MismatchedBracketException, MalformedProximitySearchException, InsufficientSpecificityException, InternalQueryException, IncompleteClauseException, RegexCompilationException{
            
            super(rs, tg, caps, marks);

        }
        
        @Override
        public String buildTransformedString() throws InternalQueryException, InsufficientSpecificityException{
            
            String transformed = originalString;
            if(wildcardsTooLoose(transformed)) throw new InsufficientSpecificityException();
            // operators except for 'OR' and proximity operators do not need to be represented
            // in the query
            if(transformed.equals(StringSearchFacet.SearchOperator.LEX.name())) return "";
            if(transformed.equals(StringSearchFacet.SearchOperator.REGEX.name())) return "";
            if(transformed.equals(StringSearchFacet.SearchOperator.NOT.name())) return "";
            if(transformed.equals(StringSearchFacet.SearchOperator.AND.name())) return ""; // because this is the default operator
            if(transformed.equals(StringSearchFacet.SearchOperator.OR.name())) return "OR";
            // deal with anomalous Solr syntax for representing single-word proximity
            if(clauseRoles.contains(ClauseRole.OPERATOR) && transformed.equals("1w")) return "w";
            // expand lemmas where required
            if(clauseRoles.contains(ClauseRole.LEMMA)){
                
                transformed = expandLemma(transformed);
                return transformed;
                
            }
            // metadata and translation fields are indexed in lowercase for searchability. transform accordingly.
            if(target != SearchTarget.TEXT){
                
                transformed = lowerCaseExcludingRegexes(transformed);               
                if(target == SearchTarget.USER_DEFINED){
                    
                    return translateUserDefinedField(transformed);
                    
                }
                return transformed;
                
            }
            if(ignoreCaps) transformed = lowerCaseExcludingRegexes(transformed);
            if(ignoreMarks) transformed = FileUtils.stripDiacriticals(transformed);
            transformed = transformed.replaceAll("ς", "σ");  
            if(clauseRoles.contains(ClauseRole.REGEX)){
                
                transformed = anchorRegex(transformed);
                return transformed;
            }
            transformed = transformed.replaceAll("#", "^");
            transformed = transformed.replaceAll("\\^", "\\\\^");     
            transformedString = transformed;
            return transformed;
            
        }
        
        /**
         * Determines whether a wildcard-containing string is of sufficient length (>=3 characters)
         * for Solr be able to search for it.
         *
         * Note that regexes (as opposed to wildcards) do not need to observe this limit, though
         * performance may of course slow if it is not observed.
         * 
         */
        
        Boolean wildcardsTooLoose(String test){
           
           if(clauseRoles.contains(ClauseRole.REGEX)) return false;
           if(!test.contains("?") && !test.contains("*")) return false;
           test = test.replaceAll("\\?", "");
           test = test.replaceAll("\\*", "");
           if(test.length() > 2) return false;
           return true;
           
           
        }
        
        /**
         * Transforms the literal parts of a regular expression to lower case.
         * 
         * 
         * @param ucString
         * @return 
         */
        
        String lowerCaseExcludingRegexes(String ucString){
            
            if(!clauseRoles.contains(ClauseRole.REGEX)) return ucString.toLowerCase();
            int curlyBracketCount = 0;
            String precedingCharacter = "";
            StringBuilder lced = new StringBuilder();
            for(int i = 0; i < ucString.length(); i++){
            
                String c = Character.toString(ucString.charAt(i));
                if("\\".equals(precedingCharacter) || curlyBracketCount > 0){
                    
                    lced.append(c);
                    
                }
                else{
                    
                    lced.append(c.toLowerCase());
                    
                }
                precedingCharacter = c;
                if("{".equals(c)) curlyBracketCount++;
                if("}".equals(c)) curlyBracketCount--;
                
            }
            
            return lced.toString();
        }
        
        /**
         * Expands a passed declined word into all of its possible forms by querying
         * the morph-search solr core.
         * 
         * 
         * @param declinedForm
         * @return
         * @throws InternalQueryException 
         */
        
        String expandLemma(String declinedForm) throws InternalQueryException{
            
           
           try{
               if(Character.toString(declinedForm.charAt(declinedForm.length() - 1)).equals("σ")){
                   
                   String startForm = declinedForm.substring(0, declinedForm.length() - 1);
                   declinedForm = startForm + "ς";
                   
               }
               SolrServer solr = new CommonsHttpSolrServer("http://localhost:8083/solr/" + morphSearch);
               // TODO: stop hard-coding string for prodo!
               String searchTerm = "lemma:" + declinedForm;
               SolrQuery sq = new SolrQuery();
               sq.setQuery(searchTerm);
               sq.setRows(1000);
               QueryResponse qr = solr.query(sq);
               SolrDocumentList forms = qr.getResults();
               Set<String> formSet = new HashSet<String>();
               if (forms.size() > 0) {
                  for (int i = 0; i < forms.size(); i++) {
                    formSet.add(FileUtils.stripDiacriticals((String)forms.get(i).getFieldValue("form")).replaceAll("[_^]", "").toLowerCase());
                  }
                 declinedForm = FileUtils.interpose(formSet, " OR ");

                } 
               declinedForm = "(" + declinedForm + ")";
               return declinedForm;
           
           }
           catch(Exception e){
               
               throw new InternalQueryException();
               
           }
           
       }
        
       /**
         * Translates the friendly aliases available on the front-end to specify search targets 
         * into the corresponding Solr fields.
         * 
         * @param rawString
         * @return 
         */
        
       String translateUserDefinedField(String rawString){
          
          Pattern pattern = Pattern.compile("\\W*(\\w+):.*");
          Matcher matcher = pattern.matcher(rawString);
          if(matcher.matches()){
              
              String userField = matcher.group(1);
           
              if("translation".equals(userField)){
               
                rawString = rawString.replace(userField, SolrField.translation.name());
               
              }
              else if("meta".equals(userField)){

                 rawString = rawString.replace(userField, SolrField.metadata.name());

              }
              else if("apis".equals(userField)){

                 rawString = rawString.replace(userField, SolrField.apis_metadata.name());

              }
              else if("hgv".equals(userField)){

                  rawString = rawString.replace(userField, SolrField.hgv_metadata.name()); 

              }

            } 
          
          return rawString;
              
       }   
       
       /**
        * Adds the multi-character match pattern ('.*') to the beginning and end of a regex as 
        * required.
        * 
        * This method is necessary because of Java's quirky requirement that a regex match an
        * <strong>entire</strong> string for the <code>mystring.matches()</code> and
        * <code>mymatcher.matches()</code> methods to return <code>true</code>
        * 
        * 
        * @param rawRegex
        * @return 
        */
        
        String anchorRegex(String rawRegex){
            
            StringBuilder anchoredRegex = new StringBuilder();
            int l = rawRegex.length();
            if(l < 1) return rawRegex;
            String firstChar = Character.toString(rawRegex.charAt(0));
            String lastChar  = Character.toString(rawRegex.charAt(l - 1));
            String startExp = l > 1 ? rawRegex.substring(0, 2) : firstChar;
            String lastExp = l > 1 ? rawRegex.substring(l - 2, l) : lastChar;
            if(!firstChar.equals("^")) anchoredRegex.append("^");
            if(!startExp.equals(".*") && !startExp.equals(".+") && !firstChar.equals("^")) anchoredRegex.append(".*");
            anchoredRegex.append(rawRegex);
            if(!lastExp.equals(".*") && !lastExp.equals(".+") && !lastChar.equals("$")) anchoredRegex.append(".*");
            if(!lastChar.equals("$")) anchoredRegex.append("$");            
            return anchoredRegex.toString();
            
        }
        
        @Override
        ArrayList<ClauseRole> getSubordinateClauseRoles(){
            
            return new ArrayList<ClauseRole>();
            
        }
        
        @Override
        Boolean isCharactersProxTerm(){
            
            return CHAR_PROX_TERM_REGEX.matcher(originalString).matches();
            
        }
        
        @Override
        Boolean isWordsProxTerm(){
            
            return WORD_PROX_TERM_REGEX.matcher(originalString).matches();
            
        }
        
        @Override
        Boolean containsLeadingWildcard(){
            
            if(originalString.length() < 1) return false;
            String firstChar = Character.toString(originalString.charAt(0));
            if("?".equals(firstChar) || "*".equals(firstChar)) return true;
            return false;
            
        }
        
        @Override
        ArrayList<SearchTerm> getConstituentTerms(ArrayList<SearchTerm> terms){
            
            terms.add(this);
            return terms;
            
        }
        
    }
    
}
