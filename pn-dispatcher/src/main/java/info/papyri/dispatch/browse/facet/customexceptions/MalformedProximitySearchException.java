/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.browse.facet.customexceptions;

/**
 *
 * @author thill
 */
public class MalformedProximitySearchException extends StringSearchParsingException {
    
    public MalformedProximitySearchException(){
    
        super("Malformed proximity search: proximity data provided but no proximity operator");
    
    
    }
    
    
}
