package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.LanguageCode;
import info.papyri.dispatch.browse.SolrField;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 * <code>Facet</code> for selection by language
 *
 * @author thill
 */
public class LanguageFacet extends Facet{
    
    
    public LanguageFacet(String formName){
        
        super(SolrField.facet_language, formName, "Language");
        
    }
    

    @Override
    public String generateWidget() {

        StringBuffer html = new StringBuffer("<div class=\"facet-widget\">");
        Boolean onlyOneValue = valuesAndCounts.size() == 1;
        String disabled = onlyOneValue ? " disabled=\"true\"" : "";
        html.append("<span class=\"option-label\">" + getDisplayName() + "</span>");
        html.append("<select" + disabled + " name=\"" + formName + "\">");
        html.append("<option disabled=\"true\">" + Facet.defaultValue + "</option>");
        
        Iterator<Count> vcit = valuesAndCounts.iterator();
        
        while(vcit.hasNext()){
            
            Count valueAndCount = vcit.next();
            String value = valueAndCount.getName();
            String displayValue = getDisplayValue(value);
            String count = String.valueOf(valueAndCount.getCount());
            String selected = onlyOneValue ? " selected=\"true\"" : "";
            html.append("<option" + selected + " value=\"" + value + "\">" + displayValue + " (" + count + ")</option>");            
        }
        
        html.append("</select>");
        html.append("</div><!-- closing .facet-widget -->");
        html.append(generateHiddenFields());

        return html.toString();
        
    }
    
    /**
     * Transforms the BCP 47 language tagged used for internal representation into 
     * a readily human-readable form
     * 
     * @param languageCode
     * @return 
     * @see LanguageCode
     */
    
    @Override
    public String getDisplayValue(String languageCode){
        
        String displayValue = "";
        
        try{
            
            String swappedLanguageCode = languageCode.replaceAll("-", "_");
            LanguageCode lang = LanguageCode.valueOf(swappedLanguageCode);
            displayValue = lang.expanded();
            
        } 
        catch(IllegalArgumentException iae){
            
            displayValue = languageCode;
            
        }
      
        return displayValue;
        
    }
    
}
