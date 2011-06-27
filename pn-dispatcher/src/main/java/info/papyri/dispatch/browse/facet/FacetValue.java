package info.papyri.dispatch.browse.facet;

/**
 *
 * @author thill
 */
public class FacetValue {
    
    private String value;
    private int count;
    
    public FacetValue(String val, int count){
        
        this.value = val;
        this.count = count;
        
    }
    
    public String getValue(){
        
        return value;
        
    }
    
    public int getCount(){
        
        return count;
        
    }
    
    
}
