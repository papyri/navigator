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
 *
 * @author thill
 */
public class HasTranscriptionFacet extends Facet {
    
    public HasTranscriptionFacet(String formName){
        
        super(SolrField.transcription, formName, "Has Transcription");
        
    }
    
    @Override
    public void setWidgetValues(QueryResponse queryResponse){
        
        FacetField facetField = queryResponse.getFacetField(field.name());
        valuesAndCounts = new ArrayList<Count>();
        List<Count> unfiltered = facetField.getValues();
        Iterator<Count> cit = unfiltered.iterator();
        long nullCount = 0;
        while(cit.hasNext()){
            
            Count count = cit.next();
            
            String value = count.getName();
            long number = count.getCount();
            
            if(value == null  && number > 0){
                
                nullCount = number;
                valuesAndCounts.add(count);
                
            }
   
        }
        
        long totalCount = queryResponse.getResults().getNumFound();
        totalCount = totalCount - nullCount;
        if(totalCount > 0){
            
            Count trueCount = new Count(new FacetField("true"), "true", totalCount);
            valuesAndCounts.add(trueCount);
            
            
        }

          
    } 
    
        public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        solrQuery.addFacetField(field.name());
        
        Iterator<String> cit = facetConstraints.iterator();
        
        while(cit.hasNext()){
            
            
            
            String pseudoBoolean = cit.next();
            String fq = field + ":[* TO *]";
            
            if("false".equals(pseudoBoolean)) fq = "-" + fq;
            
            solrQuery.addFilterQuery(fq);
            
            
        }
        
        return solrQuery;
        
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
