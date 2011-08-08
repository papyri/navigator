package info.papyri.dispatch.browse;

/**
 * Custom exception class handling cases in which a <code>SolrField</code> is expected 
 * but absent.
 * 
 * @author thill
 * @see info.papyri.dispatch.browse.facet.FacetBrowser#retrieveRecords(org.apache.solr.client.solrj.response.QueryResponse) 
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