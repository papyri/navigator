package info.papyri.dispatch.browse.facet.customexceptions;

/**
 *
 * @author thill
 */
public class MismatchedBracketException extends StringSearchParsingException {
    
    public MismatchedBracketException(){
        
        super("MismatchedBracketException: opening and closing brackets do not match");
        
    }
    
}
