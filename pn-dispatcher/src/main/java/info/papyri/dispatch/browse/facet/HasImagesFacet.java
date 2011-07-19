package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;

/**
 * <code>BooleanFacet</code> regarding whether or not images are associated with a 
 * record.
 * 
 * @author thill
 */
public class HasImagesFacet extends BooleanFacet {

    public HasImagesFacet(){
    
        super(SolrField.images, FacetParam.IMG.name(), "Images Available");
    
    }

    
    @Override
    public String getToolTipText(){
        
        return "Indicates whether or not images of the artifact are accessible through IDP.";
        
        
    } 
    
}
