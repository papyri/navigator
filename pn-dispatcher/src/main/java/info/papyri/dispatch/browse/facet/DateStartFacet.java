package info.papyri.dispatch.browse.facet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * @author thill
 */
public class DateStartFacet extends DateFacet {
        
    public DateStartFacet(String formName){
        
        super(formName, "Date on or after");
        
    }
    
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        solrQuery = dateQueryCoordinator.addDateFacetQueryContribution(solrQuery);
               
        return solrQuery;
        
    }
    
    public void setWidgetValues(QueryResponse queryResponse){
        
        valuesAndCounts = new ArrayList<Count>();
        
        valuesAndCounts = dateQueryCoordinator.addUnknownCount((ArrayList<Count>)valuesAndCounts, queryResponse);
        
        Map<String, Integer> facetQueries = queryResponse.getFacetQuery();
        int bottomLimit = dateQueryCoordinator.getLowestCategoryWithMembers(queryResponse);
        for(Map.Entry<String, Integer> entry : facetQueries.entrySet()){
            
            String rawQueryName = entry.getKey();
            Pattern pattern = Pattern.compile("^" + field.name() + ":\\[\\s*(-?\\d+)\\s*TO\\s*-?\\d+\\s*\\]" + "$");
            Matcher matcher = pattern.matcher(rawQueryName);
            if(matcher.matches()){
            
                String queryName = String.valueOf(matcher.group(1));
                Integer queryCount = entry.getValue();
                
                if(queryName != null && !queryName.equals("") && !queryName.equals("null") && queryCount > 0 && Integer.valueOf(queryName) >= bottomLimit){
                    
                    Count count = new Count(new FacetField(field.name()), queryName, queryCount);
                    valuesAndCounts.add(count);                 
                    
                }
                
            }
                 
        }
        
        Collections.sort(valuesAndCounts, dateCountComparator);
           
        
    }
    
    @Override
    public void addConstraint(String newValue){
        
        String trimmedValue = newValue.trim();
        dateQueryCoordinator.setUnknownDateFlag(Boolean.FALSE);
        if(trimmedValue.equals("Unknown")){
            
            dateQueryCoordinator.setUnknownDateFlag(Boolean.TRUE);
            trimmedValue = "0";
            
        }
        dateQueryCoordinator.setTerminusAfterWhich(Integer.valueOf(trimmedValue));
        super.addConstraint(trimmedValue);
        
    }

    
}
