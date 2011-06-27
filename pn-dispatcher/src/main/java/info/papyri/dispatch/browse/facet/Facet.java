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
    
    private ArrayList<String> facetConstraints = new ArrayList<String>();
    List<Count> valuesAndCounts;
    SolrField field;
    String formName;
    
    public Facet(SolrField sf, String formName){
        
        this.field = sf; 
        this.formName = formName;
        
    }
    
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        solrQuery.addFacetField(field.name());
        
        Iterator<String> cit = facetConstraints.iterator();
        
        while(cit.hasNext()){
            
            String fq = cit.next();
            solrQuery.addFilterQuery(fq);
            
            
        }
        
        return solrQuery;
        
    }
    
    abstract public String generateWidget();

    public void setWidgetValues(QueryResponse queryResponse){
        
        FacetField facetField = queryResponse.getFacetField(field.name());
        valuesAndCounts = facetField.getValues();
          
    }  
    
    public void addConstraint(String newValue){
        
        newValue = newValue.trim();
        // getting rid of counts
        newValue = newValue.replaceAll("\\([\\d]+\\)$", "");
        newValue = newValue.trim();
        String filterQuery = field.name() + ":" + newValue;
        if(!facetConstraints.contains(filterQuery)) facetConstraints.add(filterQuery);
        
        
    }
    
    public SolrField getFacetField(){
        
        return field;
        
    }
    
}
