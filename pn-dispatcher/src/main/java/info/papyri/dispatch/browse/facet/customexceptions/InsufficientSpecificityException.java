/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
