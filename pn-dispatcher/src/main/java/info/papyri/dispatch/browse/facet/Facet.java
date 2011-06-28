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
abstract public class Facet {
    
    ArrayList<String> facetConstraints = new ArrayList<String>();
    List<Count> valuesAndCounts;
    SolrField field;
    String formName;
    static String defaultValue = "--- All values ---";
    
    public Facet(SolrField sf, String formName){
        
        this.field = sf; 
        this.formName = formName;
        
    }
    
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        solrQuery.addFacetField(field.name());
        
        Iterator<String> cit = facetConstraints.iterator();
        
        while(cit.hasNext()){
            
            String fq = cit.next();
            fq = field + ":" + fq;
            solrQuery.addFilterQuery(fq);
            
            
        }
        
        return solrQuery;
        
    }
    
    abstract public String generateWidget();
    
    public String getAsQueryString(){
        
        String queryString = "";
        
        Iterator<String> cit = facetConstraints.iterator();
        while(cit.hasNext()){
            
            String value = cit.next();
            queryString += formName + "=" + value;
            if(cit.hasNext()) queryString += "&";
            
            
        }
        
        return queryString;
        
    }

    public void setWidgetValues(QueryResponse queryResponse){
        
        FacetField facetField = queryResponse.getFacetField(field.name());
        valuesAndCounts = facetField.getValues();
          
    }  
    
    public void addConstraint(String newValue){
        
        if(newValue.equals(Facet.defaultValue)) return;
        newValue = trimValue(newValue);
        if(newValue.contains(" ")) newValue = "\"" + newValue + "\"";
        if(!facetConstraints.contains(newValue)) facetConstraints.add(newValue);
        
        
    }
    
    public SolrField getFacetField(){
        
        return field;
        
    }
    
    String trimValue(String valueWithCount){
        
        valueWithCount = valueWithCount.trim();
        String valueWithoutCount = valueWithCount.replaceAll("\\([\\d]+\\)[\\s]*$", "");
        valueWithoutCount = valueWithoutCount.trim();
        return valueWithoutCount;
   
    }
    
}
