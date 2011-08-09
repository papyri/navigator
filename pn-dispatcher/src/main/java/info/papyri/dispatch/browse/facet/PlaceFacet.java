package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;


/**
 * <code>Facet</code> for selection by provenance.
 * 
 * 
 * @author thill
 */
public class PlaceFacet extends Facet {
    
    public PlaceFacet(){
        
        super(SolrField.display_place, FacetParam.PLACE, "Provenance");
        
    }
    
    @Override
    String getToolTipText() {
        
        return "Indicates the place where the text was produced, as far as can be determined. Often this will correspond to the findspot of the document.";
        
    }


    
}
