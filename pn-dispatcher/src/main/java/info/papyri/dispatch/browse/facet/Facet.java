package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
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
    
    private ArrayList<String> facetConstraints;
    private List<Count> valuesAndCounts;
    SolrField field;
    FacetBrowser.FacetMapping formName;
    
    public Facet(SolrField sf, FacetBrowser.FacetMapping formName){
        
        this.field = sf; 
        this.formName = formName;
        
    }
    
    abstract public SolrQuery buildQueryContribution(SolrQuery solrQuery);
    
    abstract public String generateHTML();

    public void setWidgetValues(QueryResponse queryResponse){
        
        FacetField facetField = queryResponse.getFacetField(field.name());
        valuesAndCounts = facetField.getValues();
          
    }  
    
    public void addConstraint(String newValue){
        
        if(!facetConstraints.contains(newValue)) facetConstraints.add(newValue);
        
    }
    
    public SolrField getIdentifer(){
        
        return field;
        
    }
    
}
