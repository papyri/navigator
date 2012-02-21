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
    public enum SearchType{ PHRASE, SUBSTRING, REGEX, LEMMA, USER_DEFINED, PROXIMITY };
    
    enum SearchUnit{ WORDS, CHARS };
    
    /**
     * Values indicating the fields to search
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
    
    public enum ClauseRole{LEMMA, REGEX, START_PROX, END_PROX, AND, OR, NOT, OPERATOR, DEFAULT };
    
    enum SearchOperator{AND, OR, REGEX, THEN, NEAR, NOT, LEX };
    
    enum SearchButton{
        
        AND("and"),
        OR("or"),
        NOT("not"),
        LEX("lex"),
        THEN("then"),
        NEAR("near"),
        REGEX("regex"),
        CLEAR("clear"),
        ADD("+"),
        REMOVE("-");

        String label;
        
        SearchButton(String lbl){
            
            label = lbl;
            
        }
        
    }
    
    enum SearchHandler{
        
        SURROUND,
        REGEXP,
        DEFAULT
        
        
    }
    
    static Pattern PROX_OPERATOR_REGEX = Pattern.compile("\\d{1,2}(w|n|wc|nc)");
    
    static String PHRASE_MARKER = ".*(\"|')[\\p{L}]*\\s[\\p{L}]*(\\1).*";
            
    static Pattern WHITESPACE_DETECTOR = Pattern.compile("^.*\\s+.*$");
   
    SearchClauseFactory CLAUSE_FACTORY = new SearchClauseFactory();
    
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
    private HashMap<Integer, ArrayList<SearchClause>> searchConfigurations = new HashMap<Integer, ArrayList<SearchClause>>();
    
    /** The path to the Solr index for lemmatisated searches */
    private static String morphSearch = "morph-search/";
    
    ArrayList<CustomApplicationException> exceptionLog;

    public StringSearchFacet(){
        
        super(SolrField.transcription_ngram_ia, FacetParam.STRING, "String search");
        exceptionLog = new ArrayList<CustomApplicationException>();
             
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        Iterator<ArrayList<SearchClause>> scait = searchConfigurations.values().iterator();
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
        html.append("<span class=\"str-operator\"></span>");
        html.append("<input type=\"text\" name=\"");
        html.append(formName.name());
        html.append("\" size=\"37\" maxlength=\"250\" class=\"keyword\"></input>");
        html.append("<span class=\"prx\">");
        html.append("<span id=\"within\">within</span>");
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
        
        int counter = 0;
        int index = 1;
        
        while(index <= searchConfigurations.size()){
            
            ArrayList<SearchClause> configs = searchConfigurations.get(counter);
            counter++;
            if(configs == null) continue;
            String concatenatedStringQuery = this.concatenateConfigs(configs);
            
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
    
    private String concatenateConfigs(ArrayList<SearchClause> configs){
        
        StringBuilder query = new StringBuilder();
        Iterator<SearchClause> scit = configs.iterator();
        while(scit.hasNext()){
        
            SearchClause nowConfig = scit.next();
            query.append(nowConfig.getOriginalString());
            if(scit.hasNext()) query.append(SUBFIELD_SEPARATOR);
        
            
        }
        return query.toString();
        
    }
    
    private String concatenateConfigDisplayValues(ArrayList<SearchClause> configs){
        
        StringBuilder query = new StringBuilder();
        Iterator<SearchClause> scit = configs.iterator();
        while(scit.hasNext()){
        
            SearchClause nowConfig = scit.next();
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
    
    HashMap<Integer, ArrayList<SearchClause>> pullApartParams(Map<String, String[]> params){
        
        HashMap<Integer, ArrayList<SearchClause>> configs = new HashMap<Integer, ArrayList<SearchClause>>();
        
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
                if(fieldConfigs.size() > 0) configs.put(matchNumber, fieldConfigs);
                
            }
            
        }
        
        return configs;
        
    }
    
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
        if(!searchConfigurations.containsKey(k)) return "Facet value not found";
        
        ArrayList<SearchClause> configs = searchConfigurations.get(k);
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
        
        SearchClause config = searchConfigurations.get(Integer.valueOf(paramNumber)).get(0);
        
        String searchType = config.parseForSearchType().name().toLowerCase();
           
        String firstCap = searchType.substring(0, 1).toUpperCase();
        return firstCap + searchType.substring(1, searchType.length());
        
        
    }
    
    @Override
    public String getAsQueryString(){
        
        if(searchConfigurations.size() < 1) return "";
        
        StringBuilder qs = new StringBuilder();
        int counter = 0;
        int index = 1;
        while(index <= searchConfigurations.size()){
            
            ArrayList<SearchClause> configs = searchConfigurations.get(counter);
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
        
        // filterValue is index to search configuration
        
        StringBuilder qs = new StringBuilder();
        int filteredLength = searchConfigurations.size() - 1;
        int counter = 0;
        int index = 1;
        
        while(index <= filteredLength){
            
            ArrayList<SearchClause> configs = searchConfigurations.get(counter);
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
     * Reconstitutes a query-string from the members of the passed <code>SearchConfiguration</code> object.
     * 
     * 
     * @param pn
     * @param config
     * @return 
     */
    
    private String getConfigurationAsQueryString(Integer pn, ArrayList<SearchClause> configs){
        
            String paramNumber = pn == 0 ? "" : String.valueOf(pn);
        
            StringBuilder qs = new StringBuilder();
            String kwParam = formName.name() + paramNumber;
            String targetParam = "target" + paramNumber;
            
            qs.append(kwParam);
            qs.append("=");
            qs.append(this.concatenateConfigs(configs));
            
            SearchClause leadConfig = configs.get(0);
            
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
        
        return super.getCSSSelectorID() + String.valueOf(searchConfigurations.size());
        
        
    }
    
    @Override
    public void addConstraint(String newValue){
        
        if(newValue.equals(Facet.defaultValue) || "".equals(newValue)) return;
        super.addConstraint(newValue);
        
    }
    
    @Override
    public String[] getFormNames(){

        String[] formNames = new String[searchConfigurations.size() + 1];
        
        for(int i = 0; i <= searchConfigurations.size(); i++){
            
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
        
        for(Map.Entry<Integer, ArrayList<SearchClause>> entry : searchConfigurations.entrySet()){
            
            ArrayList<SearchClause> values = entry.getValue();
            Iterator<SearchClause> vit = values.iterator();
            while(vit.hasNext()){
            
                SearchClause config = vit.next();
                highlightString += config.getOriginalString();  // might need to replace this with bespoke highlight string
                   
            }
        }
        
        return highlightString;
    }
    
    public ArrayList<SearchTerm> getAllSearchTerms(){
        
        
        ArrayList<SearchTerm> allTerms = new ArrayList<SearchTerm>();
        for(Map.Entry<Integer, ArrayList<SearchClause>> entry : searchConfigurations.entrySet()){
            
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
    
    
    
    @Override
    public ArrayList<CustomApplicationException> getExceptionLog(){
        
        return exceptionLog;
        
    }
    
    final class SearchClauseFactory{
        
        Pattern proxClauseDetect = Pattern.compile(".*(\\bNEAR\\b|\\bTHEN\\b).*", Pattern.CASE_INSENSITIVE);
        Pattern proxMetricsDetect = Pattern.compile(".*(~(\\d{1,2})([\\w]+)?)\\s*$");
        Pattern justMetricsDetect = Pattern.compile("(~(\\d{1,2})([\\w]+)?)\\s*$");
                
        ArrayList<SearchClause> buildSearchClauses(String searchString, SearchTarget target, Boolean caps, Boolean marks) throws MismatchedBracketException, MalformedProximitySearchException, IncompleteClauseException, RegexCompilationException{
            
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
        
        String swapInProxperators(String fullString) throws MalformedProximitySearchException, MismatchedBracketException{

            String searchString = fullString;
            ArrayList<String> subclauses = suckOutSubClauses(searchString);
            Iterator<String> sit = subclauses.iterator();
            while(sit.hasNext()){
                
                   searchString = searchString.replace(sit.next(), "()");
                   
            }

            Matcher proxMatch = proxClauseDetect.matcher(searchString);
            Matcher metricsMatch = proxMetricsDetect.matcher(searchString);
            if(!proxMatch.matches()){
                
                if(metricsMatch.matches()){
    
                    throw new MalformedProximitySearchException();
                    
                }
                return fullString;
                
            }
            String operator = proxMatch.group(1).toUpperCase().equals("NEAR") ? "n" : "w"; 
            String num = metricsMatch.matches() && metricsMatch.group(2) != null && metricsMatch.group(2).length() > 0? metricsMatch.group(2) : "1";
            String unit = metricsMatch.matches() && metricsMatch.group(3) != null && metricsMatch.group(3).length() > 0 && metricsMatch.group(3).equals("chars") ? "c" : "";
            
            String proxOperator = num + operator + unit;

            searchString = searchString.replace(proxMatch.group(1), proxOperator);

            if(metricsMatch.matches() && metricsMatch.group(1) != null && metricsMatch.group(1).length() > 0){
                
                searchString = searchString.replace(metricsMatch.group(1), "");
            }

            Iterator<String> sit2 = subclauses.iterator();
            while(sit2.hasNext()){

                searchString = searchString.replaceFirst("\\(\\)", sit2.next());
                
                
            }

            return searchString;
            
        }
        
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
        
        String trimEnclosingBrackets(String searchString) throws MismatchedBracketException{
            
            if(!Character.toString(searchString.charAt(0)).equals("(")) return searchString;
            int endBracketIndex = getIndexOfMatchingCloseBracket(searchString);
            if(endBracketIndex == -1){ throw new MismatchedBracketException(); }
            if(endBracketIndex == searchString.length() - 1){
                
                return searchString.substring(1, endBracketIndex);
                
            }
            
            return searchString;
                   
        }
        
        Integer getMatchingIndex(String startChar, String remainder){
            
            if("(".equals(startChar)) return getIndexOfMatchingCloseBracket(remainder);
            if("\"".equals(startChar) || "'".equals(startChar)){
                
                int endIndex = remainder.indexOf(startChar, 1);
                if(endIndex != -1) return endIndex;
                
            }
            
            return remainder.indexOf(" ", 1);
            
        }
        
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
    
    
    public abstract class SearchClause{
        
        String originalString;
        String transformedString;
        ArrayList<SearchClause> clauseComponents;
        ArrayList<ClauseRole> clauseRoles;
        Boolean ignoreCaps;
        Boolean ignoreMarks;
        SearchTarget target;
        Pattern charProxRegex = Pattern.compile(".*?(\\d{1,2})(w|n)c.*");
        private String LEX_MARKER = "LEX";
        private String REGEX_MARKER = "REGEX";
        
        SearchClause(String rs, SearchTarget tg, Boolean caps, Boolean marks) throws MismatchedBracketException, MalformedProximitySearchException, IncompleteClauseException, RegexCompilationException{
            
            originalString = rs;
            target = tg;
            ignoreCaps = caps;
            ignoreMarks = marks;
            clauseComponents = hasSubComponents(rs) ? clauseComponents = CLAUSE_FACTORY.buildSearchClauses(rs, tg, caps, marks) : new ArrayList<SearchClause>();
            clauseRoles = new ArrayList<ClauseRole>();
            addClauseRole(ClauseRole.DEFAULT);
        }
        
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
           if(rs.contains("(")||rs.contains(")")) return true;
           return false;
           
       }
       
        
       public ArrayList<ClauseRole> getClauseRoles(){
            
           return clauseRoles;
            
       }
       

       
       ArrayList<ClauseRole> getAllClauseRoles(){
           
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
       
       Integer getIndexOfNextOperand(Integer start, ArrayList<SearchClause> clauses){
           
           for(int i = start; i < clauses.size(); i++){
               
               SearchClause clause = clauses.get(i);
               if(!clause.isOperator()) return i;            
               
           }
           
           return -1;
           
       }
       
       Integer getIndexOfPreviousOperand(Integer start, ArrayList<SearchClause> clauses){
                    
           for(int i = start; i >= 0; i--){
               
               SearchClause clause = clauses.get(i);
               if(!clause.isOperator()) return i;
               
           }
           
           return -1;
           
       }
       
       Boolean isOperator(){
           
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
       
       SearchHandler parseForSearchHandler(ArrayList<ClauseRole> roles){
           
           if(roles.size() < 1) return null;
           if(roles.contains(ClauseRole.REGEX)) return SearchHandler.REGEXP;
           if(roles.contains(ClauseRole.START_PROX)) return SearchHandler.SURROUND;
           return SearchHandler.DEFAULT;
           
       }
       
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
       
       abstract void assignClauseRoles() throws IncompleteClauseException;
              
       abstract ArrayList<ClauseRole> getSubordinateClauseRoles();
       
       abstract Boolean isCharactersProxTerm();
             
       abstract public String buildTransformedString() throws InternalQueryException, IncompleteClauseException, RegexCompilationException, InsufficientSpecificityException;
        
       abstract ArrayList<SearchTerm> getConstituentTerms(ArrayList<SearchTerm> terms);
       
       /* getters and setters */
       SearchTarget getSearchTarget(){ return target; }
       Boolean getIgnoreCaps(){ return ignoreCaps; }
       Boolean getIgnoreMarks(){ return ignoreMarks; }
       public String getTransformedString(){ return transformedString; }
        
    }
    
    public class SubClause extends SearchClause{
            
        ArrayList<SearchClause> transformedClauses = new ArrayList<SearchClause>();
        
        SubClause(String rs, SearchTarget tg, Boolean caps, Boolean marks) throws MismatchedBracketException, MalformedProximitySearchException, IncompleteClauseException, RegexCompilationException{
            
            super(rs, tg, caps, marks);
            assignClauseRoles();
            transformedClauses = doCharsProxTransform(clauseComponents);

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
        
       
        final ArrayList<SearchClause> doCharsProxTransform(ArrayList<SearchClause> clauses) throws IncompleteClauseException, RegexCompilationException{
            
            Integer chpIndex = getCharsProxIndex(clauses);
            if(chpIndex == -1) return clauses;
            ArrayList<SearchClause> proxClauses = new ArrayList<SearchClause>();
            Integer startIndex = getProxStartTerm(chpIndex, clauses);
            Integer endIndex = getProxPostTerm(chpIndex, clauses);
            if(startIndex == -1 || endIndex == -1) throw new IncompleteClauseException();
            for(int i = 0; i < startIndex; i++){
                
                proxClauses.add(clauses.get(i));
                
            }
            String regex = convertCharProxToRegexSyntax(clauses.get(startIndex).originalString, clauses.get(endIndex).originalString, clauses.get(chpIndex).originalString);

            try{
            
                SearchTerm regexSearchTerm = new SearchTerm(regex, target, ignoreCaps, ignoreMarks);
                regexSearchTerm.addClauseRole(ClauseRole.REGEX);
                proxClauses.add(regexSearchTerm);
                
            } catch(StringSearchParsingException sspe){
                
                throw new RegexCompilationException();
                
            }
            for(int k = endIndex + 1; k < clauses.size(); k++){
                
                proxClauses.add(clauses.get(k));
            
            }
            return proxClauses;
            
        }
        
        @Override
        final void assignClauseRoles() throws IncompleteClauseException{
                      
           for(int i = 0; i < clauseComponents.size(); i++){
               
               SearchClause clause = clauseComponents.get(i);
               if(clause.isOperator()){
                   
                   clause.addClauseRole(ClauseRole.OPERATOR);
                   
                   try{
                       
                       SearchOperator op = SearchOperator.valueOf(clause.getOriginalString().trim());
                       if(op == SearchOperator.OR){
                           
                           int prevOperand = getIndexOfPreviousOperand(i, clauseComponents);
                           int nextOperand = getIndexOfNextOperand(i, clauseComponents);
                           if(prevOperand == -1 || nextOperand == -1) throw new IncompleteClauseException();
                           clauseComponents.get(prevOperand).addClauseRole(ClauseRole.OR);
                           clauseComponents.get(nextOperand).addClauseRole(ClauseRole.OR);
                           
                       }
                       else{

                           int nextOperand = getIndexOfNextOperand(i, clauseComponents);
                           if(nextOperand == -1) throw new IncompleteClauseException();
                           String opName = op.name();
                           if(opName.equals(SearchOperator.LEX.name())) opName = ClauseRole.LEMMA.name();
                           ClauseRole role = ClauseRole.valueOf(opName);
                           clauseComponents.get(nextOperand).addClauseRole(role);
                           
                       }
                       
                   }
                   catch(IllegalArgumentException iae){
                       
                       int prevOperand = getIndexOfPreviousOperand(i, clauseComponents);
                       int nextOperand = getIndexOfNextOperand(i, clauseComponents);
                       if(prevOperand == -1 || nextOperand == -1) throw new IncompleteClauseException();
                       clauseComponents.get(prevOperand).addClauseRole(ClauseRole.START_PROX);
                       clauseComponents.get(nextOperand).addClauseRole(ClauseRole.END_PROX);                   
                       
                   }
                    
               }
               else{
                   
                   clause.assignClauseRoles();              
                   
               }
               
               
           }
           
       }
        
        String convertCharProxToRegexSyntax(String prevTerm, String nextTerm, String charProx) throws RegexCompilationException{
            
            Matcher charProxMatcher = charProxRegex.matcher(charProx);
            if(!charProxMatcher.matches()) throw new RegexCompilationException();
            String numChars = charProxMatcher.group(1);
            String unit = charProxMatcher.group(2);
            String distRegex = ".{1," + numChars + "}";
            prevTerm = convertWildcardToRegexSyntax(prevTerm);
            nextTerm = convertWildcardToRegexSyntax(nextTerm);
            String regex = prevTerm.trim() + distRegex + nextTerm.trim();
            if(unit.equals("w")) return regex;
            String revRegex = nextTerm.trim() + distRegex + prevTerm.trim();
            String nearRegex = "(" + revRegex + "|" + regex + ")";
            return nearRegex;
            
        }
        
        String convertWildcardToRegexSyntax(String wildcard){
            
            String regex = wildcard.replaceAll("\\*", ".*");
            regex = regex.replaceAll("\\?", ".");
            return regex;
            
            
        }
        
        Integer getCharsProxIndex(ArrayList<SearchClause> clauses){
            
            for(int i = 0; i < clauses.size(); i++){
                
                if(clauses.get(i).isCharactersProxTerm()) return i;
                
            }
            
            return -1;
            
        }
        
        Integer getProxStartTerm(Integer start, ArrayList<SearchClause> clauses){
            
            for(int i = start; i >= 0; i--){
                
                if(clauses.get(i).getClauseRoles().contains(ClauseRole.START_PROX)) return i;
                
            }
            
            return -1;
            
        }
        
        Integer getProxPostTerm(Integer start, ArrayList<SearchClause> clauses){
            
            for(int i = start; i < clauses.size(); i++){
                
                if(clauses.get(i).getClauseRoles().contains(ClauseRole.END_PROX)) return i;
                
            }
            
            return -1;
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
        
        
        
    }
    
    public class SearchTerm extends SearchClause{
        
        Pattern charProxTermRegex = Pattern.compile("\\d{1,2}(w|n)c");
        
        SearchTerm(String rs, SearchTarget tg, Boolean caps, Boolean marks) throws MismatchedBracketException, MalformedProximitySearchException, IncompleteClauseException, RegexCompilationException{
            
            super(rs, tg, caps, marks);

        }
        
        @Override
        void assignClauseRoles(){}
        
        @Override
        public String buildTransformedString() throws InternalQueryException, InsufficientSpecificityException{
            
            String transformed = originalString;
            if(wildcardsTooLoose(transformed)) throw new InsufficientSpecificityException();
            if(transformed.equals(StringSearchFacet.SearchOperator.LEX.name())) return "";
            if(transformed.equals(StringSearchFacet.SearchOperator.REGEX.name())) return "";
            if(transformed.equals(StringSearchFacet.SearchOperator.NOT.name())) return "";
            if(transformed.equals(StringSearchFacet.SearchOperator.AND.name())) return ""; // because this is the default operator
            if(transformed.equals(StringSearchFacet.SearchOperator.OR.name())) return "OR";
            if(clauseRoles.contains(ClauseRole.OPERATOR) && transformed.equals("1w")) return "w";
            if(clauseRoles.contains(ClauseRole.LEMMA)){
                
                transformed = expandLemma(transformed);
                return transformed;
                
            }
            if(target != SearchTarget.TEXT){
                
                transformed = transformed.toLowerCase();
                return transformed;
                
            }
            if(ignoreCaps) transformed = transformed.toLowerCase();
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
        
        Boolean wildcardsTooLoose(String test){
           
           if(clauseRoles.contains(ClauseRole.REGEX)) return false;
           if(!test.contains("?") && !test.contains("*")) return false;
           test = test.replaceAll("\\?", "");
           test = test.replaceAll("\\*", "");
           if(test.length() > 2) return false;
           return true;
           
           
        }
        
        String expandLemma(String declinedForm) throws InternalQueryException{
            
           
           try{
            
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
            
            return charProxTermRegex.matcher(originalString).matches();
            
        }
        
        @Override
        ArrayList<SearchTerm> getConstituentTerms(ArrayList<SearchTerm> terms){
            
            terms.add(this);
            return terms;
            
        }
        
    }
    
}
