package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.Iterator;
import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 * <code>Facet</code> for selection by provenance.
 * 
 * 
 * @author thill
 */
public class PlaceFacet extends Facet {
    
    public PlaceFacet(String formName){
        
        super(SolrField.display_place, formName, "Provenance");
        
    }
    
    @Override
    String getToolTipText() {
        
        return "Indicates the place where the text was produced, as far as can be determined. Often this will correspond to the findspot of the document.";
        
    }


    
}
