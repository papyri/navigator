package info.papyri.dispatch.browse.facet.customexceptions;

/**
 *
 * @author thill
 */
public class CustomApplicationException extends Exception {
    
    String message;
    
    public CustomApplicationException(String msg){
        
        super();
        message = msg;
        
    }
    
    @Override
    public String getMessage(){
        
        return "CustomApplicationException thrown: " + message;
        
    }
    
}
