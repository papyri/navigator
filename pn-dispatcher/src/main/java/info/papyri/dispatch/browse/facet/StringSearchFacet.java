package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.FileUtils;
import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * Note that this at the moment isn't strictly speaking a facet in terms of its
 * behaviour - there are no values to select from (though an autocomplete box
 * has the same sort of functionality ....)
 * 
 * @author thill
 */
public class StringSearchFacet extends Facet{
    
    enum SearchTarget{ ALL, METADATA, TEXT, TRANSLATIONS };
    enum SearchType{ PHRASE, SUBSTRING, PROXIMITY };
    enum SearchOption{ BETA, NO_CAPS, NO_MARKS, LEMMAS, WITHIN };

    private ArrayList<SearchConfiguration> searchConfigurations = new ArrayList<SearchConfiguration>();
    
    public StringSearchFacet(){
        
        super(SolrField.transcription_ngram_ia, FacetParam.STRING, "String search");
        
        
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        Iterator<SearchConfiguration> scit = searchConfigurations.iterator();
        
        while(scit.hasNext()){
            
            SearchConfiguration nowConfig = scit.next();
            
            String searchString = getTransformedText(nowConfig);
            
            
            
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
        html.append(formName.name().toLowerCase());
        html.append("\" size=\"40\" maxlength=\"250\" id=\"keyword\"></input>");
        
        // search target control
        html.append(" in <select name=\"target\" id=\"target\"><option value=\"");
        html.append(SearchTarget.ALL.name().toLowerCase());
        html.append("\">All</option>");
        html.append("<option value=\"");
        html.append(SearchTarget.TEXT.name().toLowerCase());
        html.append("\" selected>Text</option>");              // this will need to be changed once full functionality in place
        html.append("<option value=\"");
        html.append(SearchTarget.METADATA.name().toLowerCase());
        html.append("\">Metadata</option><option value=\"");
        html.append(SearchTarget.TRANSLATIONS.name().toLowerCase());
        html.append("\">Translations</option>");
        html.append("</select>");
        html.append("<input type=\"submit\" value=\"Search\" id=\"search\" class=\"ui-button ui-widget ui-state-default ui-corner-all\" role=\"button\" aria-disabled=\"false\"/>");
        html.append("</p>");
        
        // search type control
        html.append("<div><h3>Type</h3>");
        html.append("<p>");
        html.append("<input class=\"type\" type=\"radio\" name=\"type\" value=\"");
        html.append(SearchType.PHRASE.name().toLowerCase());
        html.append("phrase\" id=\"phrase\"/> ");
        html.append("<label for=\"phrase\" id=\"phrase-label\">phrase search</label>");
        html.append("<input class=\"type\" type=\"radio\" name=\"type\" value=\"");
        html.append(SearchType.SUBSTRING.name().toLowerCase());
        html.append("\" id=\"substring\" checked/> ");
        html.append("<label for=\"substring\" id=\"substring-label\">substring search</label>");
        html.append("<input class=\"type\" type=\"radio\" name=\"type\" value=\"");
        html.append(SearchType.PROXIMITY.name().toLowerCase());
        html.append("\" id=\"proximity\"/> ");
        html.append("<label for=\"proximity\" id=\"proximity-label\">find within</label>");
        html.append(" <input type=\"text\" name=\"within\" value=\"10\" id=\"within\" size=\"2\" style=\"width:1.5em\"/> words");
        html.append("</p>");
        html.append("</div><!-- closing 'Type' anonymous div -->"); 
        html.append("<div id=\"search-options\">");
        
        // search options control
        html.append("<h3>Options</h3>");
        html.append("<p><input type=\"checkbox\" name=\"");
        html.append(SearchOption.BETA.name().toLowerCase());
        html.append("\" id=\"betaYes\" value=\"on\"/>");
        html.append("<label for=\"betaYes\" id=\"betaYes-label\">search text in Beta Code</label> <br/>");
        html.append("<input type=\"checkbox\" name=\"");
        html.append(SearchOption.NO_CAPS.name().toLowerCase());
        html.append("\" id=\"caps\" value=\"on\" checked></input>");    // will need to be changed once hooked in
        html.append("<label for=\"caps\" id=\"caps-label\">ignore capitalization</label><br/>");
        html.append("<input type=\"checkbox\" name=\"");
        html.append(SearchOption.NO_MARKS.name().toLowerCase());
        html.append("\" id=\"marks\" value=\"on\" checked></input>");  // will need to be changed once hooked in
        html.append("<label for=\"marks\" id=\"marks-label\">ignore diacritics/accents</label><br/>");
        html.append("<input type=\"checkbox\" name=\"");
        html.append(SearchOption.LEMMAS.name().toLowerCase());
        html.append("\" id=\"lemmas\"/>");
        html.append("<label for=\"lemmas\" id=\"lemmas-label\">lemmatized search</label>");
        
        
        html.append("</p>");
        html.append("</div><!-- closing 'Options' anonymous div -->");
        html.append("</div><!-- closing .facet-widget -->");
        return html.toString();
        
    }
    
    private String getTransformedText(SearchConfiguration config){
        
        String searchString = config.getSearchString();
        
        
        
        
        return searchString;
        
        
        
    }
    
    @Override
    public Boolean addConstraints(Map<String, String[]> params){
        
        searchConfigurations = pullApartParams(params);
        
        return !searchConfigurations.isEmpty();
        
        
    }
    
    @Override
    String generateHiddenFields(){
        
        StringBuilder html = new StringBuilder();
        
        Iterator<SearchConfiguration> scit = searchConfigurations.iterator();
        
        int counter = 2;                // starting at 2 for ease of human parsing
        
        while(scit.hasNext()){
            
            String inp = "<input typ=\"hidden\" name=\"";
            String v = "\" value=\"";
            String c = "\"/>";
            SearchConfiguration config = scit.next();
            html.append(inp);
            html.append(formName.name());
            html.append(String.valueOf(counter));
            html.append(v);
            html.append(config.getSearchString());
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
            
            if(config.getBetaOn()){
                
                html.append(inp);
                html.append(SearchOption.BETA.name().toLowerCase());
                html.append(v);
                html.append("on");
                html.append(c);
                
            }
            
            if(config.getIgnoreCaps()){
                
                html.append(inp);
                html.append(SearchOption.NO_CAPS.name().toLowerCase());
                html.append(v);
                html.append("on");
                html.append(c);
                
            }
            
            if(config.getIgnoreMarks()){
                
                html.append(inp);
                html.append(SearchOption.NO_MARKS.name().toLowerCase());
                html.append(v);
                html.append("on");
                html.append(c);
                
            }
            if(config.getLemmatizedSearch()){
                
                html.append(inp);
                html.append(SearchOption.LEMMAS.name().toLowerCase());
                html.append(v);
                html.append("on");
                html.append(c);
                
            }
            
        }
        
        return html.toString();
        
    }
    
    ArrayList<SearchConfiguration> pullApartParams(Map<String, String[]> params){
        
        ArrayList<SearchConfiguration> configs = new ArrayList<SearchConfiguration>();
        
        Pattern pattern = Pattern.compile(formName.name().toLowerCase() + "([\\d]*)");
        
        Iterator<String> kit = params.keySet().iterator();
        
        while(kit.hasNext()){
            
            String key = kit.next();
            
            Matcher matcher = pattern.matcher(key);
            
            if(matcher.matches()){
            
                String matchSuffix = matcher.group(1);                        

                String keywordGetter = formName.name().toLowerCase() + matchSuffix;
                String typeGetter = "type" + matchSuffix;
                String withinGetter = SearchOption.WITHIN.name().toLowerCase() + matchSuffix;
                String targetGetter = "target" + matchSuffix;
                String betaGetter = SearchOption.BETA.name().toLowerCase() + matchSuffix;
                String capsGetter = SearchOption.NO_CAPS.name().toLowerCase() + matchSuffix;
                String marksGetter = SearchOption.NO_MARKS.name().toLowerCase() + matchSuffix;
                String lemmaGetter = SearchOption.LEMMAS.name().toLowerCase() + matchSuffix;

                if(!params.containsKey(typeGetter) || !params.containsKey(targetGetter)) continue;

                String keyword = params.get(keywordGetter)[0];
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
                
                String[] rawBeta = params.get(betaGetter);
                String[] rawCaps = params.get(capsGetter);
                String[] rawMarks = params.get(marksGetter);
                String[] rawLemmas = params.get(lemmaGetter);
                String[] rawProx = params.get(withinGetter);
                int prox = rawProx == null ? 0 : rawProx[0].matches("\\d+") ? Integer.valueOf(rawProx[0]) : 0;           
                Boolean beta = rawBeta == null ? false : "on".equals(rawBeta[0]);
                Boolean caps = rawCaps == null ? false : "on".equals(rawCaps[0]);
                Boolean marks = rawMarks == null ? false : "on".equals(rawMarks[0]);
                Boolean lemmas = rawLemmas == null ? false : "on".equals(rawLemmas[0]);

                SearchConfiguration searchConfig = new SearchConfiguration(keyword, trgt, ty, prox, beta, caps, marks, lemmas);
                configs.add(searchConfig);
            
            }
            
        }
        
        
        return configs;
        
    }
    
    @Override
    public void addConstraint(String newValue){
        
        if(newValue.equals(Facet.defaultValue) || "".equals(newValue)) return;
        super.addConstraint(newValue);
        
    }
         
         
    @Override
    public void setWidgetValues(QueryResponse queryResponse){}       
         
    
    @Override
    public String getToolTipText(){
        
        return "Performs a substring search, as though using the standard Search page. Capitalisation and diacritcs are ignored.";
        
    }
    
    class SearchConfiguration{
        
        private String searchString;
        private SearchTarget target;
        private SearchType type;
        private int within;
        private Boolean betaOn;
        private Boolean ignoreCaps;
        private Boolean ignoreMarks;
        private Boolean lemmatizedSearch;
        
        public SearchConfiguration(String kw,SearchTarget tgt, SearchType ty, int wi, Boolean beta, Boolean caps, Boolean marks, Boolean lemmas){
            
            target = tgt;
            type = ty;
            within = wi;
            searchString = kw;
            betaOn = beta;
            ignoreCaps = caps;
            ignoreMarks = marks;
            lemmatizedSearch = lemmas;
            
        }
        
        public SearchTarget getSearchTarget(){ return target; }
        public SearchType getSearchType(){ return type; }
        public int getProximity(){ return within; }
        public String getSearchString(){ return searchString; }
        public Boolean getBetaOn(){ return betaOn; }
        public Boolean getIgnoreCaps(){ return ignoreCaps; }
        public Boolean getIgnoreMarks(){ return ignoreMarks; }
        public Boolean getLemmatizedSearch(){ return lemmatizedSearch; }
    }
    
}
