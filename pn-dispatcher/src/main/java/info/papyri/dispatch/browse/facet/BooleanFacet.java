package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Extends the <code>Facet</code> class to deal specifically with Boolean fields.
 * 
 * This class reworks its superclass only very slightly. Because only two values are
 * possible, some code can be simplified. In addition, however, the 'false' value may
 * correspond to a number of values in relation to Solr - in particular, to null- 
 * and additional checks have been added to cope appropriately with these.
 * 
 * 
 * @author thill
 */
abstract public class BooleanFacet extends Facet{
    
    public BooleanFacet(SolrField sf, String formName, String displayName){
        
        super(sf, formName, displayName);
        
    }
    
    @Override
    public void setWidgetValues(QueryResponse queryResponse){
        
        FacetField facetField = queryResponse.getFacetField(field.name());
        valuesAndCounts = new ArrayList<Count>();
        List<Count> unfiltered = facetField.getValues();
        Iterator<Count> cit = unfiltered.iterator();
        while(cit.hasNext()){
            
            Count count = cit.next();
            
            if(count.getCount() > 0) valuesAndCounts.add(count);
            
        }
          
    } 
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        solrQuery.addFacetField(field.name());
        
        Iterator<String> cit = facetConstraints.iterator();

        while(cit.hasNext()){
            
            String constraint = cit.next();
            
            String queryField = field.name();
            // note the Solr syntax here, negating the condition as a whole,
            // rather than specifying it to be false
            if(!"true".equals(constraint)) queryField = "-" + queryField;
            constraint = queryField + ":true";
            
            solrQuery.addFilterQuery(constraint);

        }
        return solrQuery;
        
    }
    
    String generateHiddenFields(){
        
        String html = "";
        
        for(int i = 0; i < facetConstraints.size(); i++){
            
            String name = formName; 
            String value = facetConstraints.get(i);
            if(value == null) value = "false";
            html += "<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>";
            
        }
        
        return html;
        
    }
    
    @Override
    public String generateWidget() {
        
        StringBuffer html = new StringBuffer("<div class=\"facet-widget\" title=\"" + getToolTipText() + "\">");
        Boolean onlyOneValue = valuesAndCounts.size() == 1;
        String disabled = onlyOneValue ? " disabled=\"true\"" : "";
        String defaultSelected = onlyOneValue ? "" : "selected=\"true\"";
        html.append("<span class=\"option-label\">" + getDisplayName() + "</span>");
        html.append("<select" + disabled + " name=\"" + formName + "\">");
        html.append("<option " + defaultSelected + " value=\"default\">" + Facet.defaultValue + "</option>");
        Iterator<Count> vcit = valuesAndCounts.iterator();
        
        while(vcit.hasNext()){
            
            Count valueAndCount = vcit.next();
            String value = valueAndCount.getName();
            if(value == null) value = "false";
            String displayValue = getDisplayValue(value);
            String count = String.valueOf(valueAndCount.getCount());
            String selected = onlyOneValue ? " selected=\"true\"" : "";
            html.append("<option" + selected + " value =\"" + value + "\">" + displayValue + " (" + count + ")</option>");            
        }
        
        html.append("</select>");
        html.append("</div><!-- closing .facet-widget -->");
        html.append(generateHiddenFields());
        return html.toString();
        
        
    }
    
}
