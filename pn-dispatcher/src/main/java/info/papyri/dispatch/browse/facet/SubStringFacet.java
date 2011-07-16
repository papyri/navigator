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
public class SubStringFacet extends Facet{
    
    public SubStringFacet(String formName){
        
        super(SolrField.transcription_ngram_ia, formName, "Substring Search<br>(ignores caps,<br/>ignores accents)");
        
        
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
    
    public String generateWidget() {
        
        StringBuffer html = new StringBuffer("<div class=\"facet-widget\" title=\"" + getToolTipText() + "\">");
        html.append(generateHiddenFields());
        html.append("<span class=\"widget-label\">" + displayName + "</span>" );
        html.append("<input type=\"text\" name=\"" + formName + "\" size=\"40\" maxlength=\"250\"></input>");
        html.append("</div><!-- closing .facet-widget -->");
        return html.toString();
        
    }
    
    public void addConstraint(String newValue){
        
        if(newValue.equals(Facet.defaultValue) || "".equals(newValue)) return;
        super.addConstraint(newValue);
        
    }
         
         
    public void setWidgetValues(QueryResponse queryResponse){}       
         
    
    public String getToolTipText(){
        
        return "Performs a substring search, as though using the standard Search page. Capitalisation and diacritcs are ignored.";
        
    }
    
}
