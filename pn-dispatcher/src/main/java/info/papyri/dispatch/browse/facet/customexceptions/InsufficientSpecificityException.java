package info.papyri.dispatch.browse.facet.customexceptions;

/**
 *
 * @author thill
 */
public class InsufficientSpecificityException extends StringSearchParsingException{
    
    public InsufficientSpecificityException(){
        
        super("You must specify at least three characters per term when using wildcards");
        
        
    }
    
    
}
