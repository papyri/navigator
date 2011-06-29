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
    
    public String getAsFilteredQueryString(String filterValue){
        
        String queryString = "";
        
        Iterator<String> cit = facetConstraints.iterator();
        while(cit.hasNext()){
            
            String value = cit.next();
            if(!value.equals(filterValue)){
                
                queryString += formName + "=" + value;
                queryString += "&";
                
            }
            
        }
        
        if(queryString.substring(queryString.length() - 1).equals("&")) queryString = queryString.substring(0, queryString.length() -1);
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
    
    public ArrayList<String> getFacetConstraints(){
        
        return facetConstraints;
        
    }
    
    String generateHiddenFields(){
        
        String html = "";
        
        for(int i = 1; i <= facetConstraints.size(); i++){
            
            String name = formName + String.valueOf(i);
            String value = facetConstraints.get(i - 1);
            html += "<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>";
            
        }
        
        return html;
        
    }
    
    String trimValue(String valueWithCount){
        
        valueWithCount = valueWithCount.trim();
        String valueWithoutCount = valueWithCount.replaceAll("\\([\\d]+\\)[\\s]*$", "");
        valueWithoutCount = valueWithoutCount.trim();
        return valueWithoutCount;
   
    }
    
}
