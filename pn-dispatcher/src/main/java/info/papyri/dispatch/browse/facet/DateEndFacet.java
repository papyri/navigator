package info.papyri.dispatch.browse.facet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
public class DateEndFacet extends DateFacet{
    
    public DateEndFacet(String formName){
        
        super(formName, "Date on or before");
        
    }
    
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        solrQuery = dateQueryCoordinator.addDateFacetQueryContribution(solrQuery);
            
        return solrQuery;
        
    }
    
   public void setWidgetValues(QueryResponse queryResponse){
        
        valuesAndCounts = new ArrayList<Count>();
        long totalCount = queryResponse.getResults().getNumFound();
        List<Count> counts = queryResponse.getFacetField(field.name()).getValues();
        Iterator<Count> cit = counts.iterator();
        long missingCount = 0;

        while(cit.hasNext()){
            
            Count count = cit.next();
            String name = count.getName();
            if(name == null || name.equals("")){
                
                missingCount = count.getCount();
                
            }
   
            
        }
        
        
        Map<String, Integer> facetQueries = queryResponse.getFacetQuery();
                
        int bottomLimit = dateQueryCoordinator.getLowestCategoryWithMembers(queryResponse);
        
        for(Map.Entry<String, Integer> entry : facetQueries.entrySet()){
            
            String rawQueryName = entry.getKey();
            Pattern pattern = Pattern.compile("^" + field.name() + ":\\[\\s*(-?\\d+)\\s*TO\\s*-?\\d+\\s*\\]" + "$");
            Matcher matcher = pattern.matcher(rawQueryName);
            if(matcher.matches()){
            
                String queryName = matcher.group(1);
                
                if(Integer.valueOf(queryName) >= bottomLimit){
                
                long rawValue = getNextSignificantCount(facetQueries, entry.getValue());
                
                long queryCount = totalCount - rawValue - missingCount;
                
                if(queryName != null && !queryName.equals("") && !queryName.equals("null") && queryCount > 0){
                    
                    Count count = new Count(new FacetField(field.name()), queryName, queryCount);
                    valuesAndCounts.add(count);                 
                    
                }
                
                }
                
            }
                 
        }
        
        Collections.sort(valuesAndCounts, dateCountComparator);         
        
    } 
   
    private long getNextSignificantCount(Map<String, Integer> facetQueries, long currentValue){
        
        ArrayList<Long> counts = new ArrayList<Long>();
        
        for(Map.Entry<String, Integer> entry : facetQueries.entrySet()){
            
            long count = entry.getValue();
            if(count < currentValue) counts.add(count);
                  
        }
        
        if(counts.size() == 0) return 0;
        
        return Collections.max(counts);
        
    }

    @Override
    public void addConstraint(String newValue){
        
        dateQueryCoordinator.setTerminusBeforeWhich(Integer.valueOf(newValue.trim()));
        super.addConstraint(newValue);
        
    }

    
}
