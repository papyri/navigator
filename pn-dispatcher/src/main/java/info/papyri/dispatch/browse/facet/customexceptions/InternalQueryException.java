package info.papyri.dispatch.browse.facet.customexceptions;

/**
 *
 * @author thill
 */
public class InternalQueryException extends CustomApplicationException {
    
    private String err;
    
    public InternalQueryException(){
        
        super("Internal Query Exception when expanding lemma");
        
    }
    
    public InternalQueryException(String msg){
        
        super(msg);
        err = msg;
        
        
    }
    
    @Override
    public String getMessage(){
        
        return err;
        
    }
    
    
}
