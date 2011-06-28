package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.Iterator;
import org.apache.solr.client.solrj.SolrQuery;

/**
 *
 * @author thill
 */
abstract public class BooleanFacet extends Facet{
    
    public BooleanFacet(SolrField sf, String formName){
        
        super(sf, formName);
        
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        solrQuery.addFacetField(field.name());
        
        Iterator<String> cit = facetConstraints.iterator();

        while(cit.hasNext()){
            
            String constraint = cit.next();
            
            String queryField = field.name();
            if(!"true".equals(constraint)) queryField = "-" + queryField;
            constraint = queryField + ":true";
            
            solrQuery.addFilterQuery(constraint);

        }
        return solrQuery;
        
    }
    
}
