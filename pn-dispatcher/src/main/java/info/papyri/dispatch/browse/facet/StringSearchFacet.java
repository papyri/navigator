package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.FileUtils;
import info.papyri.dispatch.browse.SolrField;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 * Replicates the functioning of <code>info.papyri.dispatch.Search</code> in a 
 * manner compatible with the faceted browse framework and codebase.
 * 
 * Note that although the algorithm used for the search process is intended to
 * replicate that defined in <code>info.papyri.dispatch.Search</code> it is implemented
 * very differently. The logic for string-handling is all encapsulated in the inner 
 * SearchConfiguration class, below, and more specific documentation is provided there.
 * 
 * One important difference between this subclass and other <code>Facet</code> subclasses
 * is that repeated complex (that is to say, involving more than one request parameter)
 * searches are possible. This means that search parameters must be ordered, both to 
 * ensure that they remain correctly correlated with each other (so that for example the
 * IGNORE_CAPS setting used in one search is not mistakenly applied to another) and so
 * that they can be displayed correctly. Most of the methods overriding <code>Facet</code> 
 * methods do so in order to provide this ordering functionality.
 * 
 * @author thill
 * @version 2011.08.19
 * @see info.papyri.dispatch.Search
 * @see info.papyri.dispatch.browse.facet.StringSearchFacet.SearchConfiguration
 */
public class StringSearchFacet extends Facet{
    
    final public String SUBFIELD_SEPARATOR = "¤";
    
    /**
     * The type of search being performed.
     * 
     * PROXIMITY and WITHIN values are both used for proximity searches- PROXIMITY indicating
     * that a 'slop search' is desired, and WITHIN to specify slop-distance
     * 
     * USER_DEFINED is used for searches in which the user bypasses the standard HTML
     * form controls to specify the search using string-input only.
     * 
     */
    enum SearchType{ PHRASE, SUBSTRING, REGEX, LEMMA, USER_DEFINED };
    
    enum SearchUnit{ WORDS, CHARS };
    
    /**
     * Values indicating the fields to search
     * 
     * Note that these values do not correspond directly to Solr fields; the Solr field
     * to search is determined by inspecting the submitted SearchTarget, along with 
     * the submitted SearchOption(s) and SearchType.
     * 
     */
    enum SearchTarget{ ALL, METADATA, TEXT, TRANSLATION, USER_DEFINED };
    /**
     * Values indicating whether or not capitalisation and diacritics should be considered
     * significant for the search
     * 
     */
    enum SearchOption{ NO_CAPS, NO_MARKS, PROXCOUNT, PROXUNIT };
    
    enum SearchButton{
        
        REMOVE("-"),
        ADD("+"),
        CLEAR("clear"),
        REGEX("regex"),
        NEAR("near"),
        THEN("then"),
        LEX("lex"),
        NOT("not"),
        OR("or"),
        AND("and");
        
        String label;
        
        SearchButton(String lbl){
            
            label = lbl;
            
        }
        
    }
    
    enum Handler{
        
        SURROUND,
        REGEXP,
        DEFAULT
        
        
    }
    
    /** A collection from which <code>SearchConfiguration</code>s can be retrieved in an
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
    private HashMap<Integer, ArrayList<SearchConfiguration>> searchConfigurations = new HashMap<Integer, ArrayList<SearchConfiguration>>();
    
    /** The path to the Solr index for lemmatisated searches */
    private static String morphSearch = "morph-search/";

    public StringSearchFacet(){
        
        super(SolrField.transcription_ngram_ia, FacetParam.STRING, "String search");
             
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        Iterator<ArrayList<SearchConfiguration>> scait = searchConfigurations.values().iterator();
        while(scait.hasNext()){
            
            ArrayList<SearchConfiguration> nowConfigs = scait.next();
            
            Iterator<SearchConfiguration> scit = nowConfigs.iterator();
            while(scit.hasNext()){
                
                   SearchConfiguration nowConfig = scit.next();
                   solrQuery.addFilterQuery(nowConfig.getSearchString()); 
                   
            }
                     
        }
        
        return solrQuery;
        
    }
        
    @Override
    public String generateWidget() {
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" id=\"text-search-widget\" title=\"");
        html.append(getToolTipText());
        html.append("\">");
        html.append(generateHiddenFields());
        
        html.append("<div class=\"stringsearch-top-controls\">");
        
        // textbox HTML
        html.append("<p class=\"ui-corner-all facet-stringsearch-wrapper\">");
        html.append("<span class=\"str-operator\"></span>");
        html.append("<input type=\"text\" name=\"");
        html.append(formName.name());
        html.append("\" size=\"37\" maxlength=\"250\" class=\"keyword\"></input>");
        html.append("<span class=\"prx\">");
        html.append("<input type=\"text\" name=\"prxcount\" class=\"prxcount\" size=\"2\" maxlength=\"2\" disabled=\"disabled\"/>");
        html.append(" <select name=\"prxunit\" class=\"prxunit\" disabled=\"disabled\">");
        html.append("<option selected=\"selected\" value=\"none\">---</option>");
        html.append("<option value=\"words\">words</option>");
        html.append("<option value=\"chars\">chars</option>");
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
               
        html.append("</div><!-- closing .facet-widget -->");
        
        return html.toString();
        
    }
    
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
        
        searchConfigurations = pullApartParams(params);
        return !searchConfigurations.isEmpty();
        
        
    }
    
    @Override
    String generateHiddenFields(){
        
        StringBuilder html = new StringBuilder();
        
        Integer numSearchConfigs = searchConfigurations.size();
        
        for(int counter = 0; counter < numSearchConfigs; counter++){
            
            ArrayList<SearchConfiguration> configs = searchConfigurations.get(counter);
            String concatenatedStringQuery = this.concatenateConfigs(configs);
            
            String inp = "<input type='hidden' name='";
            String v = "' value='";
            String c = "'/>";
            html.append(inp);
            html.append(formName.name());
            html.append(String.valueOf(counter));
            html.append(v);
            html.append(concatenatedStringQuery);
            html.append(c);
            
            html.append(inp);
            html.append("target");
            html.append(String.valueOf(counter));
            html.append(v);
            html.append(configs.get(0).getSearchTarget().name());
            html.append(c);
            
            if(configs.get(0).getIgnoreCaps()){
                
                html.append(inp);
                html.append(SearchOption.NO_CAPS.name().toLowerCase());
                html.append(String.valueOf(counter));                
                html.append(v);
                html.append("on");
                html.append(c);
                
            }
            
            if(configs.get(0).getIgnoreMarks()){
                
                html.append(inp);
                html.append(SearchOption.NO_MARKS.name().toLowerCase());
                html.append(String.valueOf(counter));
                html.append(v);
                html.append("on");
                html.append(c);
                
            }           
            
        }
        
        return html.toString();
        
    }
    
    private String concatenateConfigs(ArrayList<SearchConfiguration> configs){
        
        StringBuilder query = new StringBuilder();
        Iterator<SearchConfiguration> scit = configs.iterator();
        while(scit.hasNext()){
        
            SearchConfiguration nowConfig = scit.next();
            query.append(nowConfig.getRawString());
            if(scit.hasNext()) query.append(SUBFIELD_SEPARATOR);
        
            
        }
        return query.toString();
        
    }
    
    private String concatenateConfigDisplayValues(ArrayList<SearchConfiguration> configs){
        
        StringBuilder query = new StringBuilder();
        Iterator<SearchConfiguration> scit = configs.iterator();
        while(scit.hasNext()){
        
            SearchConfiguration nowConfig = scit.next();
            query.append(nowConfig.getDisplayString());
            if(scit.hasNext()) query.append(" AND ");
        
            
        }
        return query.toString();
        
    }
    
    /**
     * Parses the request for string-search parameters, uses these to create appropriate
     * <code>SearchConfiguration</code> objects, and populates a <code>HashMap</code> with them,
     * the keys of which are <code>Integer</code>s which allow the <code>SearchConfigurations</code>
     * to be retrieved in an ordered manner.
     * 
     * @param params
     * @return 
     */
    
    HashMap<Integer, ArrayList<SearchConfiguration>> pullApartParams(Map<String, String[]> params){
        
        HashMap<Integer, ArrayList<SearchConfiguration>> configs = new HashMap<Integer, ArrayList<SearchConfiguration>>();
        
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
                
                ArrayList<SearchConfiguration> fieldConfigs = new ArrayList<SearchConfiguration>();
                ArrayList<String> clauses = new ArrayList<String>(Arrays.asList(keyword.split(SUBFIELD_SEPARATOR)));

                Iterator<String> cit = clauses.iterator();
                while(cit.hasNext()){
                    
                    String clause = cit.next();
                    SearchConfiguration searchConfig = new SearchConfiguration(clause, trgt, caps, marks);
                    fieldConfigs.add(searchConfig);
                    
                }
                Integer matchNumber = matchSuffix.equals("") ? 0 : Integer.valueOf(matchSuffix);
                if(fieldConfigs.size() > 0) configs.put(matchNumber, fieldConfigs);
                
            }
            
        }
        
        return configs;
        
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
        if(!searchConfigurations.containsKey(k)) return "Facet value not found";
        
        ArrayList<SearchConfiguration> configs = searchConfigurations.get(k);
        SearchConfiguration leadConfig = configs.get(0);
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
        
       // can it be as easy as this? 
        
       return "STRING:"; 
        
       /* String paramNumber = "0";
        Pattern pattern = Pattern.compile(this.formName.toString() + "(\\d+)$");
        Matcher matcher = pattern.matcher(param);
        if(matcher.matches()){
            
            paramNumber = matcher.group(1);
            
        }
        
        SearchConfiguration config = searchConfigurations.get(Integer.valueOf(paramNumber));
        
        String searchType = config.getSearchType().name().toLowerCase().replaceAll("_", "-");
           
        String firstCap = searchType.substring(0, 1).toUpperCase();
        return firstCap + searchType.substring(1, searchType.length());
        */
        
    }
    
    @Override
    public String getAsQueryString(){
        
        if(searchConfigurations.size() < 1) return "";
        
        StringBuilder qs = new StringBuilder();
        
        for(Map.Entry<Integer, ArrayList<SearchConfiguration>> entry : searchConfigurations.entrySet()){
            
            Integer paramNumber = entry.getKey();
            ArrayList<SearchConfiguration> configs = entry.getValue();
            qs.append(getConfigurationAsQueryString(paramNumber, configs));
            qs.append("&");
            
        }
        
        String queryString = qs.toString();
        queryString = queryString.substring(0, queryString.length() - 1);
        return queryString;
    }
    
    @Override
    public String getAsFilteredQueryString(String filterParam, String filterValue){
        
        // filterValue is index to search configuration
        
        StringBuilder qs = new StringBuilder();
        
        for(Map.Entry<Integer, ArrayList<SearchConfiguration>> entry : searchConfigurations.entrySet()){
            
            Integer paramNumber = entry.getKey();
            
            if(!String.valueOf(paramNumber).equals(filterValue)){
            
                ArrayList<SearchConfiguration> configs = entry.getValue();
                qs.append(getConfigurationAsQueryString(paramNumber, configs));
                qs.append("&");
                
            }
            
        }
        
        String queryString = qs.toString();
        if(queryString.endsWith("&")) queryString = queryString.substring(0, queryString.length() - 1);
        return queryString;
        
    }
    
    /**
     * Reconstitutes a query-string from the members of the passed <code>SearchConfiguration</code> object.
     * 
     * 
     * @param pn
     * @param config
     * @return 
     */
    
    private String getConfigurationAsQueryString(Integer pn, ArrayList<SearchConfiguration> configs){
        
            String paramNumber = pn == 0 ? "" : String.valueOf(pn);
        
            StringBuilder qs = new StringBuilder();
            String kwParam = formName.name() + paramNumber;
            String targetParam = "target" + paramNumber;
            
            qs.append(kwParam);
            qs.append("=");
            qs.append(this.concatenateConfigs(configs));
            
            SearchConfiguration leadConfig = configs.get(0);
            
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
        
        return super.getCSSSelectorID() + String.valueOf(searchConfigurations.size());
        
        
    }
    
    @Override
    public void addConstraint(String newValue){
        
        if(newValue.equals(Facet.defaultValue) || "".equals(newValue)) return;
        super.addConstraint(newValue);
        
    }
    
    @Override
    public String[] getFormNames(){

        String[] formNames = new String[searchConfigurations.size()];
        
        Iterator<Integer> pnit = searchConfigurations.keySet().iterator();
        int counter = 0;
        
        while(pnit.hasNext()){
            
            Integer paramNumber = pnit.next();
            
            String paramSuffix = paramNumber.equals(0) ? "" : String.valueOf(paramNumber);
            String param = formName.name().toString() + paramSuffix;
            formNames[counter] = param;
            counter++;
            
        }

        return formNames;
        
    }
         
    @Override
    public void setWidgetValues(QueryResponse queryResponse){}       
         
    
    @Override
    public String getToolTipText(){
        
        return "Performs a substring search, as though using the standard Search page. Capitalisation and diacritcs are ignored.";
        
    }
    
    /**
     * Returns the text to be highlighted for each of the <code>SearchConfiguration</code> objects. 
     * 
     * @return 
     */
    
    public String getHighlightString(){
        // TODO: this isn't going to work as it stands
        String highlightString = "";
        
        for(Map.Entry<Integer, ArrayList<SearchConfiguration>> entry : searchConfigurations.entrySet()){
            
            ArrayList<SearchConfiguration> values = entry.getValue();
            Iterator<SearchConfiguration> vit = values.iterator();
            while(vit.hasNext()){
            
                SearchConfiguration config = vit.next();
                highlightString += config.getHighlightString();
                   
            }
        }
        
        return highlightString;
    }
    
    /**
     * This inner class handles the logic for string-searching previously found
     * in <code>info.papyri.dispatch.Search</code>.
     * 
     * Note that while the logic is intended to be the same, the implementation
     * is very different. In particular, the search query is understood to be made
     * up of three parts, which are dealt with separately, as far as this is possible.
     * (1) The search type (substring, phrase, lemmatised, or proximity)
     * (2) The search target (text, metadata, translations, or all three)
     * (3) Transformations to be made to the string itself (e.g., because it is in 
     * betacode format, caps should be ignored, etc.)
     * 
     * In practice these three are inter-related in a cascading fashion - the search type
     * affects the possible search targets and relevant transformations, while
     * the search target affects the possible transformations.
     * 
     * 
     */
    
    class SearchConfiguration{
        
        
        /** The search string as submitted by the user */
        private String rawString;
        /** The search string: i.e., the rawWord, after it has been
         * subjected to the relevant transformations
         */
        private String searchString;
        /**
         * The search target (text, metadata, translation, or all three) 
         */
        private SearchTarget target;
        /**
         * The search type (phrase, substring, lemmatized, proximity)
         */
        private SearchType searchType;
        /**
         * The search window used for proximity searches; defaults to 0 for
         * non-proximity searches
         */
        private int proximityDistance;

        /** <code>True</code> if capitalisation is to be ignored; <code>False</code>
         *  otherwise.
         */
        private Boolean ignoreCaps;
        /**
         * <code>True</code> if diacritics are to be ignored; <code>False</code>
         * otherwise.
         */
        private Boolean ignoreMarks;
        /** The SolrField that should be used in the search */
        private SolrField field;
        
        private Handler handler;
        
        private Integer proxCount;
        
        private SearchUnit proxUnit;
        
        /** Array of search operators potentially entered by  user */
        private String LEX_MARKER = "LEX ";
        private String REGEX_MARKER = "REGEX";
        private String SUBSTRING_MARKER = "#";
        private String PHRASE_MARKER_REGEX = ".*(\"|').*\\s.*(\\1).*";
        private Pattern PROXIMITY_REGEX = Pattern.compile("^.*~(\\d{1,2})((" + SearchUnit.WORDS.name().toLowerCase() + ")|(" + SearchUnit.CHARS.name().toLowerCase() + "))$");
        private String[] SEARCH_OPERATORS = {"AND", "OR", "NOT", "&&", "||", "+", "-", "THEN", "NEAR", LEX_MARKER.trim(), REGEX_MARKER.trim()};
        /** Map for correlating human-entered operators with Solr operators  */
        
        SearchConfiguration(String kw, SearchTarget tgt, Boolean caps, Boolean marks){
            
            target = tgt;
            ignoreCaps = caps;
            ignoreMarks = marks;
            rawString = kw;  
            proxCount = 0;
            proxUnit = null;
            parseForProximityMetrics();
            searchType = parseForSearchType();
            handler = parseForHandler();
            searchString = transformSearchString();

        }

        
        final ArrayList<String> breakIntoTerms(String search){
            
            ArrayList<String> termList = new ArrayList<String>();
            
            
            
            return termList;
            
        }
        
        final ArrayList<HashMap<String, ArrayList<SearchType>>> buildClauseStruct(ArrayList<String> termList){
            
            ArrayList<HashMap<String, ArrayList<SearchType>>> clauseStruct = new ArrayList<HashMap<String, ArrayList<SearchType>>>();
            
            return clauseStruct;
            
        }
        
        final void parseForProximityMetrics(){
            
            Matcher proxMatcher = PROXIMITY_REGEX.matcher(rawString);
            if(proxMatcher.matches()){
                
                try{ proxCount = Integer.valueOf(proxMatcher.group(1).toUpperCase()); } 
                catch(ClassCastException cce){ proxCount = 1; }
                try{ proxUnit = SearchUnit.valueOf(proxMatcher.group(2).toUpperCase());}
                catch(IllegalArgumentException iae){ proxUnit = SearchUnit.WORDS;}
                
            }           
            else{
                
                proxCount = 0;
                proxUnit = null;
                
            }
            
        }
        
        final SearchType parseForSearchType(){

            SearchType type = SearchType.SUBSTRING;
            if(SearchTarget.USER_DEFINED == target) return SearchType.USER_DEFINED;
            if(SearchTarget.METADATA == target || SearchTarget.TRANSLATION == target) return SearchType.PHRASE;
            if(rawString.contains(SUBSTRING_MARKER)) return SearchType.SUBSTRING;
            if(rawString.contains(REGEX_MARKER)) return SearchType.REGEX;
            if(rawString.contains(LEX_MARKER)) return SearchType.LEMMA;
            if(rawString.matches(PHRASE_MARKER_REGEX)) return SearchType.PHRASE;
            if(SearchUnit.CHARS == proxUnit) return SearchType.REGEX;
            return type;
            
        }
        
        final Handler parseForHandler(){
            
            if(SearchUnit.WORDS.equals(proxUnit)) return Handler.SURROUND;
            if(SearchUnit.CHARS.equals(proxUnit)) return Handler.REGEXP;
            if(SearchType.REGEX.equals(searchType)) return Handler.REGEXP;
            return Handler.DEFAULT;
            
        }

                
        /**
         * Applies whatever transformations are required to the search string before it can be 
         * used as a Solr query.
         * 
         * Note the order of processing here:
         * (1) The keywords submitted are identified
         * (2) These keywords are then transformed depending on the lemmatisation, caps, and marks settings
         * (3) The transformed keywords are substituted for the originals in the search string
         * (4) User-entered operators are replaced by Solr operators
         * (5) User-entered field names are replaced by Solr field names
         * (6) The '#' word-boundary delimiter character is replaced with '^'
         * (7) Backslash-escaping is performed
         * 
         * @return 
         * 
         */
        
        final String transformSearchString(){
            
            checkBetacodeSlip();
            ArrayList<String> keywords = harvestKeywords(rawString);
            ArrayList<String> transWords = transformKeywords(keywords);
            String swappedTerms = substituteTerms(keywords, transWords);
            String swappedProx = interpolateProximitySyntax(swappedTerms);
            String swappedFields = substituteFields(swappedProx);
            swappedFields = swappedFields.replaceAll("#", "^");
            swappedFields = swappedFields.replaceAll("\\^", "\\\\^"); 
            swappedFields = swappedFields.replaceAll(LEX_MARKER, "").replaceAll(REGEX_MARKER, "");
            swappedFields = addHandlerInformation(swappedFields);
            return swappedFields;
            
        }
        

        /**
         * Checks to ensure that latin-alphabet search operators haven't been 
         * accidentally transformed into greek-alphabet gibberish through betacode conversion 
         * 
         */
        
        private void checkBetacodeSlip(){
            
            rawString = rawString.replaceAll(" ΑΝΔ ", " AND ").replaceAll(" ΟΡ ", " OR ").replaceAll(" ΝΟΤ ", " NOT ").replaceAll("ΝΕΑΡ", "NEAR").replaceAll("ΤΗΕΝ", "THEN ");
            
        }
        
        /**
         * Extracts the keywords submitted by the user from the rest of the search-string.
         * 
         * That is to say, this method returns the terms remaining after all search fields, 
         * operators, and other search syntax has been removed.
         * 
         * 
         * @param rawInput
         * @return 
         */
        
        ArrayList<String> harvestKeywords(String rawInput){
            
            if(rawInput == null) return new ArrayList<String>();
            
            String cleanedInput = rawInput;
            
            // strip out word-boundary markers
            cleanedInput = cleanedInput.replaceAll("[()#^]", " ");
            // strip out proximity-search info
            cleanedInput = cleanedInput.replaceAll("~[\\s]*[\\d]+", " ");
            
            // get rid of all search operators
            for(String operator : this.SEARCH_OPERATORS){
                
                try{
                     String operatorPattern = operator;
                     if("||".equals(operator)){
                         
                         operatorPattern = "\\|\\|";
                         
                     }
                     else if(operator.matches("[A-Z-]+") && !operator.equals("-")){
                         
                         operatorPattern = "\\b" + operator + "\\b";
                         
                     }
                     cleanedInput = cleanedInput.replaceAll(operatorPattern, " ");
                    
                }
                catch(PatternSyntaxException pse){
                    
                    String operatorPattern = "\\" + operator;
                    cleanedInput = cleanedInput.replaceAll(operatorPattern, " ");
                    
                }
                
            }
            // strip out field names
            cleanedInput = cleanedInput.replaceAll("[^\\s]+?:", " ");
            // tidy excess whitespace
            cleanedInput = cleanedInput.trim();
            // tokenise on whitespace
            ArrayList<String> inputBits = new ArrayList<String>(Arrays.asList(cleanedInput.split("(\\s)+")));
            return inputBits;
            
        }
        
        /**
         * Transforms the passed keywords in accordance with caps, marks, and lemmatisation settings
         * 
         * @param keywords
         * @return 
         */
        ArrayList<String> transformKeywords(ArrayList<String> keywords){
            
            ArrayList<String> transformedKeywords = new ArrayList<String>();
            
            if(keywords == null) return transformedKeywords;
            if(SearchTarget.METADATA == target || SearchTarget.TRANSLATION == target){
                
                ignoreCaps = true;
                ignoreMarks = true;
                
                
            }
            int counter = 0;
            
            for(String keyword : keywords){

                if(ignoreCaps){
                    
                    if(searchType != SearchType.REGEX){
                        
                        keyword = keyword.toLowerCase();                        
                        
                    }
                    
                }

                if(lemmatizeWord(keywords, counter)){

                    try{ 
                        
                        String keywordExpanded = this.expandLemmas(keyword);
                        keyword = "(" + keywordExpanded + ")";
                    }
                    catch(Exception e){

                        transformedKeywords.add(keyword);
                        continue;
                        
                    }
                    
                }
                if(ignoreMarks) keyword = FileUtils.stripDiacriticals(keyword);
                // note: Solr index uses medial sigma only, even at the ends of words
                keyword = keyword.replaceAll("ς", "σ");  
                transformedKeywords.add(keyword);
                counter++;
            
                
            }
            
            return transformedKeywords;
            
        }
        
        /**
         * Substitutes transformed terms into the original search-string in order.
         * 
         * @param initialTerms
         * @param transformedTerms
         * @return 
         */
        
        String substituteTerms(ArrayList<String> initialTerms, ArrayList<String> transformedTerms){
            
            String remainingString = rawString;
            ArrayList<String> subBits = new ArrayList<String>();
            
            for(int i = 0; i < initialTerms.size(); i++){
                String iTerm = initialTerms.get(i);
                iTerm = Pattern.quote(iTerm);
                String sTerm = transformedTerms.get(i);  
                String[] remBits = remainingString.split(iTerm, 2);
                String newClause = remBits[0] + sTerm;
                remainingString = remBits[1];
                subBits.add(newClause);           
                
            }
            subBits.add(remainingString);
            String swapString = "";
            for(String bit : subBits){
                
                swapString += bit;
                
            }
            return swapString;
            
        }
   
 
        String interpolateProximitySyntax(String expandedString){

            if(proxCount == 0 || proxUnit == null) return expandedString;
            expandedString = expandedString.substring(0, expandedString.lastIndexOf("~"));
            // (1) Step 1 - split string into subgroups and find proximity operator within them
            String[] stringBits = expandedString.split("\\s+");
            Pattern proxPat = Pattern.compile("(near|then)");
            String lastBit = "w";
            int i;
            for(i = 0; i < stringBits.length; i++){
                
                lastBit = stringBits[i].toLowerCase().trim();
                Matcher proxmatcher = proxPat.matcher(lastBit);
                if(proxmatcher.matches()) break;
                
            }
            // (2) If the proximity search is by character rather than word then a regex
            // search should instead be performed
            String solrOperator = "then".equals(lastBit) ? "w" : "n";
            if(SearchUnit.CHARS.equals(proxUnit)){
                
                return convertProximitySearchToRegex(stringBits, i, solrOperator);
                
            }
            // (3) If it's by word, then all that's needed is to swap in the appropriate syntax
            String proxOperator = String.valueOf(proxCount) + solrOperator;
            stringBits[i] = proxOperator;
            
            StringBuilder html = new StringBuilder();
            for(int j = 0; j < stringBits.length; j++){
                
                html.append(stringBits[j]);
                if(j != stringBits.length - 1) html.append(" ");
                
            }
            
            return html.toString();
        }
        
        String convertProximitySearchToRegex(String[] searchBits, int operatorIndex, String operator){
            
            StringBuilder proximitySearch = new StringBuilder();
            proximitySearch.append("(^.*");
            String count = String.valueOf(proxCount);
            String centralRegex = ".{1," + count + "}";
            searchBits[operatorIndex] = centralRegex;
            for(int i = 0; i < searchBits.length; i++){
                
                String bit = searchBits[i].replaceAll("\\(", "").replaceAll("\\)", "");
                proximitySearch.append(bit);
                if(i != operatorIndex && i != operatorIndex - 1 && i < searchBits.length - 1){
                    
                    proximitySearch.append("\\s+");
                    
                }
                                
            }
            proximitySearch.append(".*$)");
            if(!operator.equals("n")) return proximitySearch.toString();
            int newOperatorIndex = searchBits.length - operatorIndex;
            proximitySearch.append("|(^.*");
            for(int i = searchBits.length - 1; i >= 0; i--){
                
                String bit = searchBits[i].replaceAll("\\(", "").replaceAll("\\)", "");
                proximitySearch.append(bit);
                if(i != newOperatorIndex && i != newOperatorIndex + 1 && i < 0){
                    
                    
                    proximitySearch.append("\\s+");
                    
                }
                
                
            }
            proximitySearch.append(".*$)");
            proximitySearch.append(")");
            return "(" + proximitySearch.toString();
            
        }
        
        /**
         * Substitutes user field names provided as a convenience with actual gory Solr fields.
         * 
         * 
         * @param fieldString
         * @return 
         */
        
        String substituteFields(String fieldString){
            
            if(searchType.equals(SearchType.USER_DEFINED)){
                
                fieldString = Pattern.compile("\\blem:", Pattern.CASE_INSENSITIVE).matcher(fieldString).replaceAll(SolrField.transcription_ia.name() + ":");
                fieldString = Pattern.compile("\\bstring:", Pattern.CASE_INSENSITIVE).matcher(fieldString).replaceAll(SolrField.transcription_ngram_ia.name() + ":");
                fieldString = Pattern.compile("\\bapis:", Pattern.CASE_INSENSITIVE).matcher(fieldString).replaceAll(SolrField.apis_metadata.name() + ":");
                fieldString = Pattern.compile("\\bhgv:", Pattern.CASE_INSENSITIVE).matcher(fieldString).replaceAll(SolrField.hgv_metadata.name() + ":");
                fieldString = Pattern.compile("\\bmeta:", Pattern.CASE_INSENSITIVE).matcher(fieldString).replaceAll(SolrField.metadata.name() + ":");
                return fieldString;
                
            }
            
            String fieldDesignator = "";
            
            if(searchType.equals(SearchType.SUBSTRING)){
                
                fieldDesignator = SolrField.transcription_ngram_ia.name();
                
            }
            else if(searchType.equals(SearchType.LEMMA)){
                      
                fieldDesignator = SolrField.transcription_ia.name();
            
            }
            else if(searchType.equals(SearchType.REGEX)){
                
                fieldDesignator = "";
                
            }
            else if(SearchUnit.CHARS.equals(proxUnit)){
                
                fieldDesignator = "";
                
            }
            else if(target.equals(SearchTarget.TEXT)){
                
                if(ignoreCaps && ignoreMarks){
                    
                    fieldDesignator = SolrField.transcription_ia.name();
                
                }
                
                else if(ignoreCaps){
                    
                    
                    fieldDesignator = SolrField.transcription_ic.name();
                
                }
                        
                else if(ignoreMarks){
                    
                    fieldDesignator = SolrField.transcription_id.name();
                
                }
                
                else{
                    
                    fieldDesignator = SolrField.transcription.name();

                    
                }
                
            }
                        
            else if(target.equals(SearchTarget.METADATA)){
                
                fieldDesignator = SolrField.metadata.name();
            
            }
            
            else if(target.equals(SearchTarget.TRANSLATION)){
                
                fieldDesignator = SolrField.translation.name();
            
            }
            
            else if(target.equals(SearchTarget.ALL)){
                
                fieldDesignator = SolrField.all.name();
            
            }
            fieldString = "(" + fieldString + ")";
            if(!fieldDesignator.equals("")) fieldString = fieldDesignator + ":" + fieldString;
            
            return fieldString;
            
        }
        
        private String addHandlerInformation(String search){
            
            if(SearchType.REGEX != searchType && proxCount == 0) return search;
            String prefix = "";
            if(SearchType.REGEX == searchType || proxUnit == SearchUnit.CHARS){
                
                prefix = "{!regexp cache=false qf=\"untokenized_ia\"}";
                
            }
            else{
                
                String queryField = "transcription_ia";
                if(search.contains(":")){
                    
                    try{
                        
                        queryField = SolrField.valueOf(search.substring(0, search.indexOf(":"))).name();
                        search = search.substring(search.indexOf(":") + 1, search.length() - 1 );
                        
                        
                    }
                    catch(IllegalArgumentException iae){
                        
                        
                        queryField = "transcription_ia";
                        
                    }
                    
                    
                }
                prefix = "{!surround cache=false qf=\"" + queryField + "\"}";
                
                
            }
            
            search = prefix + search;
            return search;
            
        }
        
        /**
         * Returns a Boolean regarding whether or not to lemmatise a token.
         * 
         * Internally, what's required here is that the method
         * 
         * (i) correctly identify the keyword in the string (with some extra
         * trickery required to take account of the possibility of repetition)
         * (ii) check the word immediately before it to determine whether or not
         * it signals the need for lemmification (i.e., matches 'LEX')
         * 
         * @param keywords
         * @param currentIteration
         * @return 
         */
                
        Boolean lemmatizeWord(ArrayList<String> keywords, int currentIteration){
                        
            String keyword = keywords.get(currentIteration);

            int previousOccurrences = 0;
            
            for(int i = 0; i < currentIteration; i++){
                
                if(keywords.get(i).equals(keyword)) previousOccurrences++;
                
            }

            int index = 0;
            String lemSub = rawString.substring(index);
            int counter = 0;
            
            while(lemSub.contains(keyword)){
                
                index = lemSub.indexOf(keyword);
                if(counter == previousOccurrences) break;
                lemSub = lemSub.substring(index + keyword.length());
                counter++;
                
            }
            
            if(index - LEX_MARKER.length() < 0) return false;
            String lemcheck = lemSub.substring(index - LEX_MARKER.length(), index);
            if(lemcheck.equals(LEX_MARKER)) return true;
            return false;
            
        }
        
        /**
         * Expands the given query string with its lemmas in a form usable for querying Solr
         * 
         * 
         * @param query
         * @return
         * @throws MalformedURLException
         * @throws SolrServerException 
         */
        
        public String expandLemmas(String query) throws MalformedURLException, SolrServerException {
            
            SolrServer solr = new CommonsHttpSolrServer("http://localhost:8083/solr/" + morphSearch);
            StringBuilder exp = new StringBuilder();
            SolrQuery sq = new SolrQuery();
            String[] lemmas = query.split("\\s+");
            for (String lemma : lemmas) {
              exp.append(" lemma:");
              exp.append(lemma);
            }
            sq.setQuery(exp.toString());
            sq.setRows(1000);
            QueryResponse rs = solr.query(sq);
            SolrDocumentList forms = rs.getResults();
            Set<String> formSet = new HashSet<String>();
            if (forms.size() > 0) {
              for (int i = 0; i < forms.size(); i++) {
                formSet.add(FileUtils.stripDiacriticals((String)forms.get(i).getFieldValue("form")).replaceAll("[_^]", "").toLowerCase());
              }
             return FileUtils.interpose(formSet, " OR ");
             
            }
            
            return query;
          }
       
        // TODO: Flesh this out. Used for the remove facet selectors
        public String getDisplayString(){ return ""; };
        
        /* getters and setters */
        
        public SearchTarget getSearchTarget(){ return target; }
        public String getSearchString(){ 
            
            
            return searchString; 
        
        
        }
        public String getHighlightString(){ 
            
            String highlightString = searchString;
            return highlightString;
        
        }
        public SearchType getSearchType(){ return searchType; }
        public String getRawString(){ return rawString; }
        public Boolean getIgnoreCaps(){ return ignoreCaps; }
        public Boolean getIgnoreMarks(){ return ignoreMarks; }
        public SolrField getField(){ return field; }
        public int getProxCount(){ return proxCount; }
        public SearchUnit getProxUnit(){ return proxUnit; }
    }
    
}
