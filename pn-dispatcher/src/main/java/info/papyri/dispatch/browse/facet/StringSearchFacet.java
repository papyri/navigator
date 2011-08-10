package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.FileUtils;
import info.papyri.dispatch.browse.SolrField;
import java.util.Iterator;
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
    
    public StringSearchFacet(){
        
        super(SolrField.transcription_ngram_ia, FacetParam.SUBSTRING, "String search");
        
        
    }
    
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        String substrings = "";
        
        Iterator<String> cit = facetConstraints.iterator();
        
        while(cit.hasNext()){
            
            String query = cit.next();
            substrings += query;
            if(cit.hasNext()) substrings += " ";
                    
        }
        
        if(!"".equals(substrings)){
        
            substrings = substrings.replaceAll("ς", "σ");
            substrings = substrings.toLowerCase();
            substrings = FileUtils.stripDiacriticals(substrings);
            substrings = substrings.replace("^", "\\^");

            solrQuery.addFilterQuery(field.name() + ":" + substrings);
        
        }
        
        return solrQuery;
        
    }
    
    @Override
    public String generateWidget() {
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" id=\"text-search-widget\" title=\"");
        html.append(getToolTipText());
        html.append("\">");
        html.append(generateHiddenFields());
        html.append("<p class=\"ui-corner-all\" id=\"facet-stringsearch-wrapper\">");
        html.append("<input type=\"text\" name=\"");
        html.append(formName.name());
        html.append("\" size=\"40\" maxlength=\"250\" id=\"keyword\"></input>");
        html.append(" in <select name=\"target\" id=\"target\"><option value=\"all\">All</option>");
        html.append("<option value=\"text\" selected>Text</option>");              // this will need to be changed once full functionality in place
        html.append("<option value=\"metadata\">Metadata</option><option value=\"translation\">Translations</option>");
        html.append("</select>");
        html.append("<input type=\"submit\" value=\"Search\" id=\"search\" class=\"ui-button ui-widget ui-state-default ui-corner-all\" role=\"button\" aria-disabled=\"false\"/>");
        html.append("</p>");
        html.append("<div><h3>Type</h3>");
        html.append("<p>");
        html.append("<input class=\"type\" type=\"radio\" name=\"type\" value=\"phrase\" id=\"phrase\"/> ");
        html.append("<label for=\"phrase\" id=\"phrase-label\">phrase search</label>");
        html.append("<input class=\"type\" type=\"radio\" name=\"type\" value=\"substring\" id=\"substring\"/> ");
        html.append("<label for=\"substring\" id=\"substring-label\">substring search</label>");
        html.append("<input class=\"type\" type=\"radio\" name=\"type\" value=\"proximity\" id=\"proximity\"/> ");
        html.append("<label for=\"proximity\" id=\"proximity-label\">find within</label>");
        html.append(" <input type=\"text\" name=\"within\" value=\"10\" id=\"within\" size=\"2\" style=\"width:1.5em\"/> words");
        html.append("</p>");
        html.append("</div><!-- closing 'Type' anonymous div -->"); 
        html.append("<div id=\"search-options\">");
        html.append("<h3>Options</h3>");
        html.append("<p><input type=\"checkbox\" name=\"beta\" id=\"betaYes\" value=\"on\"/>");
        html.append("<label for=\"betaYes\" id=\"betaYes-label\">search text in Beta Code</label> <br/>");
        html.append("<input type=\"checkbox\" name=\"caps\" id=\"caps\" value=\"on\" checked></input>");    // will need to be changed once hooked in
        html.append("<label for=\"caps\" id=\"caps-label\">ignore capitalization</label><br/>");
        html.append("<input type=\"checkbox\" name=\"marks\" id=\"marks\" value=\"on\" checked></input>");  // will need to be changed once hooked in
        html.append("<label for=\"marks\" id=\"marks-label\">ignore diacritics/accents</label><br/>");
        html.append("<input type=\"checkbox\" name=\"lemmas\" id=\"lemmas\"/>");
        html.append("<label for=\"lemmas\" id=\"lemmas-label\">lemmatized search</label>");
        html.append("</p>");
        html.append("</div><!-- closing 'Options' anonymous div -->");
        html.append("</div><!-- closing .facet-widget -->");
        return html.toString();
        
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
    
}
