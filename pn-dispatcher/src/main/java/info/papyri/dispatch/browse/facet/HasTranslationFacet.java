package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.Iterator;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 * <code>BooleanFacet</code> regarding whether or not a translation is associated with a 
 * record.
 * 
 * @author thill
 */
public class HasTranslationFacet extends BooleanFacet {
    
    public HasTranslationFacet(String formName){
        
        super(SolrField.has_translation, formName, "Has Translation");
        
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
            if(value == null) value = "false";
            String count = String.valueOf(valueAndCount.getCount());
            String selected = onlyOneValue ? " selected=\"true\"" : "";
            html.append("<option" + selected + " value=\"" + getDisplayValue(value) + "\">" + value + " (" + count + ")</option>");            
        }
        
        html.append("</select>");
        html.append("</div><!-- closing .facet-widget -->");
        html.append(generateHiddenFields());
        return html.toString();
        
        
    }
    
}
