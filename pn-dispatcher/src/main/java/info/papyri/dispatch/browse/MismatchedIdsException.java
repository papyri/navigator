package info.papyri.dispatch.browse;

/**
 *
 * @author thill
 */
public class MismatchedIdsException extends Exception {
    
    private int seriesLength;
    private int volumeLength;
    private int idsLength;
    
    public MismatchedIdsException(int slength, int vlength, int idlength){
        
        super();
        seriesLength = slength;
        volumeLength = vlength;
        idsLength = idlength;
        
    }
    
    public String getError(){
        
        return "MismatchedIdsException: series with " + String.valueOf(seriesLength) + " elements, volume with " + String.valueOf(volumeLength) + " elements, and ids with " + String.valueOf(idsLength) + " elements.";
        
    }
    
    
}
