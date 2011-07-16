package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.Iterator;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 * <code>BooleanFacet</code> regarding whether or not images are associated with a 
 * record.
 * 
 * @author thill
 */
public class HasImagesFacet extends BooleanFacet {

    public HasImagesFacet(String formName){
    
        super(SolrField.images, formName, "Images Available");
    
    }

    
    @Override
    public String getToolTipText(){
        
        return "Indicates whether or not images of the artifact are accessible through IDP.";
        
        
    } 
    
}
