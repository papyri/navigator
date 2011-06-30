package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.Iterator;
import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 *
 * @author thill
 */
public class PlaceFacet extends Facet {
    
    public PlaceFacet(String formName){
        
        super(SolrField.display_place, formName);
        
    }

    @Override
    public String generateWidget() {
        
        StringBuffer html = new StringBuffer("<div class=\"facet-widget\">");

        html.append("<span class=\"option-label\">Place</span>");
        html.append("<select name=\"" + formName + "\">");
        html.append("<option disabled=\"true\">" + Facet.defaultValue + "</option>");
        
        Iterator<Count> vcit = valuesAndCounts.iterator();
        
        while(vcit.hasNext()){
            
            Count valueAndCount = vcit.next();
            String value = valueAndCount.getName();
            String count = String.valueOf(valueAndCount.getCount());
            html.append("<option>" + value + " (" + count + ")</option>");
            
        }
        
        html.append("</select>");
        html.append("</div><!-- closing .facet-widget -->");
        html.append(generateHiddenFields());

        return html.toString();
        
    }
    
}
