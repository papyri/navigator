package info.papyri.dispatch.browse.facet.customexceptions;

/**
 *
 * @author thill
 */
public class StringSearchParsingException extends CustomApplicationException {
    
    String message;
    
    public StringSearchParsingException(String msg){
        
        super(msg);
        message = msg;
        
    }
    
    @Override
    public String getMessage(){
        
        return "StringSearchParsingException - "+ message;
        
    }
    
}
