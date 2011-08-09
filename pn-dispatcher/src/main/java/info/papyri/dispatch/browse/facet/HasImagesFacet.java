package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * <code>BooleanFacet</code> regarding whether or not images are associated with a 
 * record.
 * 
 * @author thill
 */
public class HasImagesFacet extends BooleanFacet {

    private Boolean isSet = false;
    
    public HasImagesFacet(){
    
        super(SolrField.images, FacetParam.IMG, "Records with images only");
    
    }

    
    @Override
    public String getToolTipText(){
        
        return "Filter out all records that do not have images associated with them.";       
        
    } 
    
    @Override
    public String generateWidget() {
        

        StringBuilder html = new StringBuilder();

        html.append("<div class=\"facet-widget\" title=\"");
        html.append(getToolTipText());
        html.append("\">");
        
        html.append("<input type=\"checkbox\" name=\"");
        html.append(formName.name());
        html.append("\"");
        
        if(isSet){
            
            html.append(" checked=\"true\"");
            
        }
        html.append("/><label for=\"");
        html.append(formName.name());
        html.append("\">");
        html.append(getDisplayName(null));
        html.append("</label>");
        html.append("</div><!-- closing .facet-widget -->");
        return html.toString();
        
        
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        

        if(isSet){
            
           solrQuery.addFilterQuery(SolrField.images.name() + ":true"); 
                   
        }
        
        return solrQuery;
        
    }
    
    
    @Override
    public Boolean addConstraints(Map<String, String[]> params){
       
        if(params.containsKey(this.formName.name())){
            
            String[] values = params.get(formName.name());
            
            for(int i = 0; i < values.length; i++){
                
                try{
                    
                    String param = java.net.URLDecoder.decode(values[i], "UTF-8");

                    if(param.equals("on")){
                            
                         isSet = true;
                         
                    } else {
                        
                         isSet = false;
                        
                    }
                    
                    
                }
                catch(UnsupportedEncodingException uee){
                    
                    System.out.println("UnsupportedEncodingException: " + uee.getMessage());
                    
                }
                
            }
            
        }
        
        return isSet;
        
    }
    
    @Override
    public String getAsQueryString(){
        
        if(isSet){
            
            return this.formName.name() + "=on";
            
        }
        else{
            
            return "";
            
        }
        
    }
    
    @Override
    public String getAsFilteredQueryString(String filterParam, String filterValue){
        
        return "";
        
    }
    
   @Override
   public ArrayList<String> getFacetConstraints(String facetParam){
        
        ArrayList<String> facetConstraints = new ArrayList<String>();
        
        if(isSet) facetConstraints.add("true");
        
        return facetConstraints;
        
    }
   
    @Override   
    public String getDisplayValue(String value){
        
        if(isSet) return "true";
        return "";
        
    }
    
    @Override
    public void setWidgetValues(QueryResponse qr){}
    
}
