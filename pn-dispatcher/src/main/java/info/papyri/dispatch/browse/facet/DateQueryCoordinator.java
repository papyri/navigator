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
public class DateQueryCoordinator {
    
    private static SolrField field = SolrField.date_category;
    private static SolrField flagField = SolrField.unknown_date_flag;
    static int LOWER_BOUND = -50;
    static int UPPER_BOUND = 50;
    private int terminusAfterWhich = 0;
    private int terminusBeforeWhich = 0;
    private Boolean unknownDateFlag = false;
    
    public SolrQuery addDateFacetQueryContribution(SolrQuery solrQuery){
           
        // check to make sure query not already added
        if(solrQuery.toString().contains(field.name()) || solrQuery.toString().contains(flagField.name())) return solrQuery;
        solrQuery.addFacetField(flagField.name());
        
        if(unknownDateFlag){
            
            solrQuery.addFilterQuery(flagField + ":true");
            return solrQuery;
            
        }
        
        solrQuery.addFacetField(field.name());
        int startTerminus = terminusAfterWhich == 0 ? LOWER_BOUND : terminusAfterWhich;
        int endTerminus = terminusBeforeWhich == 0 ? UPPER_BOUND : terminusBeforeWhich;
        
        if(terminusAfterWhich != 0 || terminusBeforeWhich != 0){
        
            solrQuery.addFilterQuery(field.name() + ":[" + String.valueOf(startTerminus) + " TO " + String.valueOf(endTerminus) + "]");
        
        }
        for(int i = startTerminus; i <= endTerminus; i++){
                    
            if(i != 0){
                
                solrQuery.addFacetQuery(field.name() + ":[" + String.valueOf(i) + " TO " + String.valueOf(endTerminus) + "]");
            
            }
        }
        
        
        return solrQuery;
        
    }
    
    int getLowestCategoryWithMembers(QueryResponse queryResponse){
        
        if(unknownDateFlag) return 0;
        
        List<Count> counts = queryResponse.getFacetField(field.name()).getValues();
        
        for(int i = LOWER_BOUND; i <= UPPER_BOUND; i++){
            
            Iterator<Count> cit = counts.iterator();
            while(cit.hasNext()){
                
                Count count = cit.next();
                String name = count.getName();
                
                long number = count.getCount();
                
                if(name != null && Integer.valueOf(name) == i && number > 0) return i;
                
                
            }       
            
        }
        
        return 0;
    }
    
    
    
    public ArrayList<Count> addUnknownCount(ArrayList<Count> valuesAndCounts, QueryResponse queryResponse){
        
        long unknownCountNumber = getUnknownCount(queryResponse);
        Count unknownCount = new Count(new FacetField(flagField.name()), "Unknown", unknownCountNumber);
        if(unknownCountNumber > 0){
            
            valuesAndCounts.add(unknownCount);
        }
        
        return valuesAndCounts;
        
    }
    
    private long getUnknownCount(QueryResponse queryResponse){
        
        
        List<Count> counts = queryResponse.getFacetField(flagField.name()).getValues();
        Iterator<Count> cit = counts.iterator();
        while(cit.hasNext()){
            
            Count count = cit.next();
            if(count.getName()!= null && count.getName().equals("true")) return count.getCount();
            
        }
        
        return 0;
        
    }
    
    public void setTerminusAfterWhich(int terminus){
        
        terminusAfterWhich = terminus;
        
    }
    
    public void setTerminusBeforeWhich(int terminus){
        
        terminusBeforeWhich = terminus;
        
    }
    
    public void setUnknownDateFlag(Boolean flag){
        
        unknownDateFlag = flag;
        
    }
    
    
    public Boolean getUnknownDateFlag(){
        
        return unknownDateFlag;
        
    }
    
    
}
