package info.papyri.dispatch.browse.facet.customexceptions;

/**
 *
 * @author thill
 */
public class SubstringTooSmallException extends CustomApplicationException{
    
    public SubstringTooSmallException(){
        
    super("Each term in a substring search must be at least three characters long. If you are specifically searching for strings involving distinct substrings of less than three characters, try performing a proximity character search or a regular expression search. Alternatively, add word-boundaries ('#') to your search: these count as characters.");
        
        
    }
    
}
