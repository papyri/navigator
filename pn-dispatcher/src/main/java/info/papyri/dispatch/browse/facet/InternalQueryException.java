package info.papyri.dispatch.browse.facet;

/**
 *
 * @author thill
 */
public class InternalQueryException extends Exception {
    
    private String err;
    
    public InternalQueryException(){
        
        super();
        err = "Unknown query exception";
        
    }
    
    public InternalQueryException(String msg){
        
        super();
        err = msg;
        
        
    }
    
    @Override
    public String getMessage(){
        
        return err;
        
    }
    
    
}
