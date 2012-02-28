package info.papyri.dispatch.browse.facet.customexceptions;

/**
 *
 * @author thill
 */
public class FacetNotFoundException extends Exception {
    
    String facetName;
    
    public FacetNotFoundException(String soughtFacet){
        
        super();
        facetName = soughtFacet;
        
    }
    
    @Override
    public String getMessage(){
        
        return "FacetNotFoundException: Facet " + facetName + " not found in collection";
        
    }
    
}
