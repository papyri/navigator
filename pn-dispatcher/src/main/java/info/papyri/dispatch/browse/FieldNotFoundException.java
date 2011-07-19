package info.papyri.dispatch.browse;

/**
 *
 * @author thill
 */
public class FieldNotFoundException extends Exception{
    
    private String info;
    private String field;
    
    public FieldNotFoundException(){
        
        super();
        field = "Unknown";
        info = "Unknown";
        
    }
    
    public FieldNotFoundException(String field){
        
        super();
        this.field = field;
        info = "Unknown";
        
    }
    
    public FieldNotFoundException(String field, String info){
        
        super();
        this.field = field;
        this.info = info;
        
    }
    
    public String getError(){
        
        return "Field " + field + " missing with values " + info;
        
    }
    
}