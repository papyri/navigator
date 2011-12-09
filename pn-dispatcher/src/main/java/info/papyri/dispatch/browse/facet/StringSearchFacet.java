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
    enum SearchType{ PHRASE, SUBSTRING, LEMMAS, PROXIMITY, WITHIN, USER_DEFINED  };
    
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
    enum SearchOption{ NO_CAPS, NO_MARKS };
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
    private HashMap<Integer, SearchConfiguration> searchConfigurations = new HashMap<Integer, SearchConfiguration>();
    
    /** The path to the Solr index for lemmatisated searches */
    private static String morphSearch = "morph-search/";

    public StringSearchFacet(){
        
        super(SolrField.transcription_ngram_ia, FacetParam.STRING, "String search");
             
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        Iterator<SearchConfiguration> scit = searchConfigurations.values().iterator();
        while(scit.hasNext()){
            
            SearchConfiguration nowConfig = scit.next();
            solrQuery.addFilterQuery(nowConfig.getSearchString()); 
                     
        }
        
        return solrQuery;
        
    }
        
    @Override
    public String generateWidget() {
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" id=\"text-search-widget\" title=\"");
        html.append(getToolTipText());
        html.append("\">");
        html.append(generateHiddenFields());
        
        // textbox HTML
        html.append("<p class=\"ui-corner-all\" id=\"facet-stringsearch-wrapper\">");
        html.append("<input type=\"text\" name=\"");
        html.append(formName.name());
        html.append("\" size=\"50\" maxlength=\"250\" id=\"keyword\"></input>");

        html.append("</p>");

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
        html.append("<input type=\"radio\" name=\"target\" value=\"");
        html.append(SearchTarget.ALL.name().toLowerCase());
        html.append("\" value=\"on\" id=\"target-all\" class=\"target\"/>");
        html.append("<label for=\"");
        html.append(SearchTarget.ALL.name().toLowerCase());
        html.append("\" id=\"all-label\">All</label>");
        html.append("</p>");
        html.append("</div><!-- closing .stringsearch-section -->");
               
        // search type control
        html.append("<div class=\"stringsearch-section\">");
        html.append("<p>");
        html.append("<input class=\"type\" type=\"radio\" name=\"type\" value=\"");
        html.append(SearchType.PHRASE.name().toLowerCase());
        html.append("\" id=\"phrase\"/> ");
        html.append("<label for=\"phrase\" id=\"phrase-label\">Word/Phrase search</label><br/>");
        html.append("<input class=\"type\" type=\"radio\" name=\"type\" value=\"");
        html.append(SearchType.SUBSTRING.name().toLowerCase());
        html.append("\" id=\"substring\" checked/> ");
        html.append("<label for=\"substring\" id=\"substring-label\">Substring search</label><br/>");
        html.append("<input class=\"type\" type=\"radio\" name=\"type\" value=\"");
        html.append(SearchType.LEMMAS.name().toLowerCase());
        html.append("\" id=\"lemmas\"/> ");
        html.append("<label for=\"lemmas\" id=\"lemmas-label\">Lemmatized search</label><br/>");
        html.append("<input class=\"type\" type=\"radio\" name=\"type\" value=\"");
        html.append(SearchType.PROXIMITY.name().toLowerCase());
        html.append("\" id=\"proximity\"/>");
        html.append("<label for=\"proximity\" id=\"proximity-label\"> Proximity: find within</label>");
        html.append(" <input type=\"text\" name=\"");
        html.append(SearchType.WITHIN.name().toLowerCase());
        html.append("\" value=\"10\" id=\"within\" size=\"2\" style=\"width:1.5em\"/> words");    
        html.append("</p>");    
        html.append("</div><!-- closing .stringsearch section -->"); 
        html.append("</div><!-- closing .facet-widget -->");
        
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
        
        Iterator<SearchConfiguration> scit = searchConfigurations.values().iterator();
        
        int counter = 1;                
        
        while(scit.hasNext()){
            
            String inp = "<input type='hidden' name='";
            String v = "' value='";
            String c = "'/>";
            SearchConfiguration config = scit.next();
            html.append(inp);
            html.append(formName.name());
            html.append(String.valueOf(counter));
            html.append(v);
            html.append(config.getRawString());
            html.append(c);
            
            html.append(inp);
            html.append("target");
            html.append(String.valueOf(counter));
            html.append(v);
            html.append(config.getSearchTarget());
            html.append(c);
            
            html.append(inp);
            html.append("type");
            html.append(String.valueOf(counter));
            html.append(v);
            html.append(config.getSearchType());
            html.append(c);
            
            if(config.getSearchType().equals(SearchType.PROXIMITY)){
                
                html.append(inp);
                html.append(SearchType.WITHIN.name().toLowerCase());
                html.append(String.valueOf(counter));               
                html.append(v);
                html.append(String.valueOf(config.getProximityDistance()));
                html.append(c);
                
            }
            
            if(config.getIgnoreCaps()){
                
                html.append(inp);
                html.append(SearchOption.NO_CAPS.name().toLowerCase());
                html.append(String.valueOf(counter));                
                html.append(v);
                html.append("on");
                html.append(c);
                
            }
            
            if(config.getIgnoreMarks()){
                
                html.append(inp);
                html.append(SearchOption.NO_MARKS.name().toLowerCase());
                html.append(String.valueOf(counter));
                html.append(v);
                html.append("on");
                html.append(c);
                
            }
            
            counter++;
            
        }
        
        return html.toString();
        
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
    
    HashMap<Integer, SearchConfiguration> pullApartParams(Map<String, String[]> params){
        
        HashMap<Integer, SearchConfiguration> configs = new HashMap<Integer, SearchConfiguration>();
        
        Pattern pattern = Pattern.compile(formName.name() + "([\\d]*)");
        
        Iterator<String> kit = params.keySet().iterator();
        
        while(kit.hasNext()){
            
            String key = kit.next();
            
            Matcher matcher = pattern.matcher(key);
            
            if(matcher.matches()){
            
                String matchSuffix = matcher.group(1);                        

                String keywordGetter = formName.name() + matchSuffix;
                String typeGetter = "type" + matchSuffix;
                String withinGetter = SearchType.WITHIN.name().toLowerCase() + matchSuffix;
                String targetGetter = "target" + matchSuffix;
                String capsGetter = SearchOption.NO_CAPS.name().toLowerCase() + matchSuffix;
                String marksGetter = SearchOption.NO_MARKS.name().toLowerCase() + matchSuffix;

                if(!params.containsKey(typeGetter) || !params.containsKey(targetGetter)) continue;

                String keyword = params.get(keywordGetter)[0];
                if(keyword == null || "".equals(keyword)) continue;
                String rawSearchType = params.get(typeGetter)[0].toUpperCase();
                String rawSearchTarget = params.get(targetGetter)[0].toUpperCase();
                
                SearchType ty = null;
                SearchTarget trgt = null;
                
                try{
                    
                    ty = SearchType.valueOf(rawSearchType);
                    trgt = SearchTarget.valueOf(rawSearchTarget);
                    
                } catch(IllegalArgumentException iae){
                    
                    continue;
                    
                }
                
                String[] rawCaps = params.get(capsGetter);
                String[] rawMarks = params.get(marksGetter);
                String[] rawDistance = params.get(withinGetter);

                int distance = rawDistance == null ? 0 : rawDistance[0].matches("\\d+") ? Integer.valueOf(rawDistance[0]) : 0;    

                Boolean caps = rawCaps == null ? false : "on".equals(rawCaps[0]);
                Boolean marks = rawMarks == null ? false : "on".equals(rawMarks[0]);

                
                SearchConfiguration searchConfig = new SearchConfiguration(keyword, matchSuffix.equals("") ? 0 : Integer.valueOf(matchSuffix), trgt, ty, caps, marks, distance);
                Integer matchNumber = matchSuffix.equals("") ? 0 : Integer.valueOf(matchSuffix);
                configs.put(matchNumber, searchConfig);
                
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
        
        SearchConfiguration config = searchConfigurations.get(k);
        String rs = config.getRawString();
        
        Pattern pattern = Pattern.compile(".*identifier:([\\d]+).*");
        Matcher matcher = pattern.matcher(rs);
        
        if(matcher.matches()) return getTrismegistosDisplayValue(matcher);
        
        StringBuilder dv = new StringBuilder();
        dv.append(rs.replaceAll("\\^", "#"));
        dv.append("<br/>");
        dv.append("Target: ");
        dv.append(config.getSearchTarget().name().toLowerCase().replace("_", "-"));
        dv.append("<br/>");
        
        if(config.getIgnoreCaps()) dv.append("No Caps: On<br/>");
        if(config.getIgnoreMarks()) dv.append("No Marks: On<br/>");
        if(config.getSearchType().equals(SearchType.PROXIMITY)){
            
            dv.append("Within: ");
            dv.append(String.valueOf(config.getProximityDistance()));
            
        }
        
        return dv.toString();
        
    }
    
    String getTrismegistosDisplayValue(Matcher matcher){
        
        String tmNumber = matcher.group(1);
        StringBuilder tmsb = new StringBuilder();
        tmsb.append("Trismegistos Identifier: ");
        tmsb.append(tmNumber);
        return tmsb.toString();
        
        
    }
    
    @Override
    public String getDisplayName(String param, java.lang.String facetValue){
        
        String paramNumber = "0";
        Pattern pattern = Pattern.compile(this.formName.toString() + "(\\d+)$");
        Matcher matcher = pattern.matcher(param);
        if(matcher.matches()){
            
            paramNumber = matcher.group(1);
            
        }
        
        SearchConfiguration config = searchConfigurations.get(Integer.valueOf(paramNumber));
        
        String searchType = config.getSearchType().name().toLowerCase().replaceAll("_", "-");
           
        String firstCap = searchType.substring(0, 1).toUpperCase();
        return firstCap + searchType.substring(1, searchType.length());
        
        
    }
    
    @Override
    public String getAsQueryString(){
        
        if(searchConfigurations.size() < 1) return "";
        
        StringBuilder qs = new StringBuilder();
        
        for(Map.Entry<Integer, SearchConfiguration> entry : searchConfigurations.entrySet()){
            
            Integer paramNumber = entry.getKey();
            SearchConfiguration config = entry.getValue();
         
            qs.append(getConfigurationAsQueryString(paramNumber, config));
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
        
        for(Map.Entry<Integer, SearchConfiguration> entry : searchConfigurations.entrySet()){
            
            Integer paramNumber = entry.getKey();
            
            if(!String.valueOf(paramNumber).equals(filterValue)){
            
                SearchConfiguration config = entry.getValue();
                qs.append(getConfigurationAsQueryString(paramNumber, config));
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
    
    private String getConfigurationAsQueryString(Integer pn, SearchConfiguration config){
        
            String paramNumber = pn == 0 ? "" : String.valueOf(pn);
        
            StringBuilder qs = new StringBuilder();
            String kwParam = formName.name() + paramNumber;
            String typeParam = "type" + paramNumber;
            String targetParam = "target" + paramNumber;
            
            qs.append(kwParam);
            qs.append("=");
            qs.append(config.getRawString());
            qs.append("&");
            qs.append(typeParam);
            qs.append("=");
            qs.append(config.getSearchType().name());
            qs.append("&");
            qs.append(targetParam);
            qs.append("=");
            qs.append(config.getSearchTarget().name());
            
            if(config.getIgnoreCaps()){
                
                qs.append("&");
                qs.append(SearchOption.NO_CAPS.name().toLowerCase());
                qs.append(paramNumber);
                qs.append("=on");
                
            }
            if(config.getIgnoreMarks()){
                
                qs.append("&");
                qs.append(SearchOption.NO_MARKS.name().toLowerCase());
                qs.append(paramNumber);
                qs.append("=on");
                
            }
            if(config.getSearchType().equals(SearchType.PROXIMITY)){
                
                qs.append("&");
                qs.append(SearchType.PROXIMITY.name().toLowerCase());
                qs.append(paramNumber);
                qs.append("=on");            
                qs.append("&");
                qs.append(SearchType.WITHIN.name().toLowerCase());
                qs.append(paramNumber);
                qs.append("=");
                qs.append(String.valueOf(config.getProximityDistance()));
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
        
        String highlightString = "";

        for(SearchConfiguration searchConfiguration : searchConfigurations.values()){
            
            highlightString += searchConfiguration.getHighlightString();
            
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
        private SearchType type;
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
        /** Array of search operators potentially entered by  user */
        private String[] SEARCH_OPERATORS = {"AND", "OR", "NOT", "&&", "||", "+", "-", "WITHIN", "BEFORE", "AFTER", "IMMEDIATELY-BEFORE", "IMMEDIATELY-AFTER"};
        /** Map for correlating human-entered operators with Solr operators  */
        private HashMap<String, String> STRINGOPS_TO_SOLROPS = new HashMap<String, String>();
        
        SearchConfiguration(String kw, Integer no, SearchTarget tgt, SearchType ty, Boolean caps, Boolean marks, int wi){
            
            target = tgt;
            type = ty;
            ignoreCaps = caps;
            ignoreMarks = marks;
            proximityDistance = wi;
            rawString = kw;  
            searchString = transformSearchString();
            STRINGOPS_TO_SOLROPS.put("NOT", "-");
            STRINGOPS_TO_SOLROPS.put("WITHIN", "~");
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
         */
        
        String transformSearchString(){
            
            checkBetacodeSlip();
            ArrayList<String> keywords = harvestKeywords(this.rawString);
            ArrayList<String> transWords = transformKeywords(keywords);
            String swappedTerms = substituteTerms(keywords, transWords);
            String swappedOps = substituteOperators(swappedTerms);
            String swappedFields = substituteFields(swappedOps);
            swappedFields = swappedFields.replaceAll("#", "^");
            swappedFields = swappedFields.replaceAll("\\^", "\\\\^"); 
            return swappedFields;
            
        }
        
        /**
         * Checks to ensure that latin-alphabet search operators haven't been 
         * accidentally transformed into greek-alphabet gibberish through betacode conversion 
         * 
         */
        
        private void checkBetacodeSlip(){
            
            rawString = rawString.replaceAll(" ΑΝΔ ", " AND ").replaceAll(" ΟΡ ", " OR ").replaceAll(" ΝΟΤ ", " NOT ");
            
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
            // strip out fiels names
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
            int counter = 0;
            
            for(String keyword : keywords){
            
                if(ignoreCaps){
                    
                    keyword = keyword.toLowerCase();
                    
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
                
                String iTerm = initialTerms.get(i).replace("?", "\\?").replace("*", "\\*");
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
        
        /**
         * In theory, this method should swap human-entered search operators for those
         * which make sense to Solr.
         * 
         * At the moment, however, it is only handlng proximity searches. 
         * 
         * @param expandedString
         * @return 
         */     
        //TODO: revisit the question of operator substitution once string-search work is
        // more advanced
        // TODO: this won't work with user-defined settings
        String substituteOperators(String expandedString){
            
            String smallString = expandedString;
            if(type.equals(SearchType.PROXIMITY)){
                
                smallString = smallString + "~" + String.valueOf(proximityDistance);
                
            }
            
            smallString = transformProximitySearch(smallString);
            return smallString;
            
            
        }
        
        /**
         * Ensures correct syntax for proximity searches
         * 
         * @param searchString
         * @return 
         */
        
        // TODO: this is a bit fubarred. Revisit once the larger quesion of user-defined
        // searches has been addressed
        String transformProximitySearch(String searchString){
            
            if(!searchString.contains("~")) return searchString;
            // tidy proximity syntax
            searchString = searchString.replaceAll("\\s*~\\s*(\\d+)", "~$1");
            String[] searchBits = searchString.split("~");
            if(searchBits.length < 2) return searchString;
            // retrieve everything prior to proximity setting
            String prelimSearchTerms = searchBits[0];
            // tokenise on whitespace
            String[] medSearchTerms = prelimSearchTerms.split(" ");
            // if only one term is involved, return unchanged, as proximity search 
            // only makes sense with two-and-only-two terms
            if(medSearchTerms.length < 2) return searchString;
            // reconstitute search terms
            String searchTerms = medSearchTerms[medSearchTerms.length - 2] + " " + medSearchTerms[medSearchTerms.length - 1];
            // wrap in quotation marks
            if(searchTerms.indexOf("\"") != 0) searchTerms = "\"" + searchTerms;
            if(searchTerms.lastIndexOf("\"") != (searchTerms.length() - 1)) searchTerms = searchTerms + "\"";
            // restore proximity setting to end of string
            String newSearchString = searchTerms + "~" + searchBits[1];
            return newSearchString;
                     
        }
        
        /**
         * Substitutes user field names provided as a convenience with actual gory Solr fields.
         * 
         * 
         * @param fieldString
         * @return 
         */
        
        String substituteFields(String fieldString){
            
            if(type.equals(SearchType.USER_DEFINED)){
                
                fieldString = Pattern.compile("\\blem:", Pattern.CASE_INSENSITIVE).matcher(fieldString).replaceAll(SolrField.transcription_ia.name() + ":");
                fieldString = Pattern.compile("\\bstring:", Pattern.CASE_INSENSITIVE).matcher(fieldString).replaceAll(SolrField.transcription_ngram_ia.name() + ":");
                fieldString = Pattern.compile("\\bapis:", Pattern.CASE_INSENSITIVE).matcher(fieldString).replaceAll(SolrField.apis_metadata.name() + ":");
                fieldString = Pattern.compile("\\bhgv:", Pattern.CASE_INSENSITIVE).matcher(fieldString).replaceAll(SolrField.hgv_metadata.name() + ":");
                fieldString = Pattern.compile("\\bmeta:", Pattern.CASE_INSENSITIVE).matcher(fieldString).replaceAll(SolrField.metadata.name() + ":");
                return fieldString;
                
            }
            
            String fieldDesignator = "";
            
            if(type.equals(SearchType.SUBSTRING)){
                
                fieldDesignator = SolrField.transcription_ngram_ia.name();
                
            }
            
            else if(type.equals(SearchType.LEMMAS)){
                
                
                fieldDesignator = SolrField.transcription_ia.name();
            
            }
            
            // henceforth only proximity and phrase searches possible
            
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
            
            if(!fieldDesignator.equals("")) fieldString = fieldDesignator + ":(" + fieldString + ")";
            
            return fieldString;
            
        }
        
        /**
         * Returns a Boolean regarding whether or not to lemmatise a token.
         * 
         * @param keywords
         * @param currentIteration
         * @return 
         */
                
        Boolean lemmatizeWord(ArrayList<String> keywords, int currentIteration){
            
            if(type.equals(SearchType.LEMMAS)) return true;
            
            if(!type.equals(SearchType.USER_DEFINED)) return false;
            
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
            
            if(index - "lem:".length() < 0) return false;
            String lemcheck = lemSub.substring(index - "lem:".length(), index);
            if(lemcheck.equals("lem:")) return true;
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
       
        
        
        /* getters and setters */
        
        public SearchTarget getSearchTarget(){ return target; }
        public String getSearchString(){ return searchString; }
        public String getHighlightString(){ 
            
            String highlightString = searchString;
            return highlightString;
        
        }
        public SearchType getSearchType(){ return type; }
        public int getProximityDistance(){ return proximityDistance; }
        public String getRawString(){ return rawString; }
        public Boolean getIgnoreCaps(){ return ignoreCaps; }
        public Boolean getIgnoreMarks(){ return ignoreMarks; }
        public SolrField getField(){ return field; }
    }
    
}
