package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * <code>BooleanFacet</code> regarding whether or not images are associated with a 
 * record.
 * 
 * @author thill
 */
public class HasImagesFacet extends Facet {
    
    enum ImageParam{
        
        INT ("Papyri.info", SolrField.images_int),
        EXT ("Other sites", SolrField.images_ext), 
        PRINT("Print publications", SolrField.illustrations);
    
        private final String message;
        private final SolrField searchField;
        private Boolean on;
        private Boolean hasRecords;
        
        ImageParam(String msg, SolrField sf){
            
            this.message = msg;
            this.searchField = sf;
            this.on = false;
            this.hasRecords = false;
            
        }
        
        public String getMessage(){ return this.message; }
        public String getSelector(){ return "img-" + this.name().toLowerCase(); }
        public void setIsOn(Boolean on){ this.on = on; }
        public Boolean isOn(){ return this.on; }
        public String getSearchField(){ return this.searchField.name().replace("_", "-"); }
        public void setHasRecord(Boolean hr){ this.hasRecords = hr; }
        public Boolean hasRecord(){ return this.hasRecords; }
    
    };
    
    public HasImagesFacet(){
    
        super(SolrField.images_int, FacetParam.IMG, "Records with images only");
    
    }

    
    @Override
    public String getToolTipText(){
        
        return "Filter records based on associated images.";       
        
    } 
    
    @Override
    String generateHiddenFields(){ return ""; }
    
    @Override
    public String generateWidget() {
        
        StringBuilder html = new StringBuilder();
        String chbx = "<input type=\"checkbox\" name=\"";
        String val = "\" value=\"";
        String clss = "\" class=\"img-select\"";
        String lblstrt = "<label for=\"";
        String close = ">";
        String lblend = "</label>";

        html.append("<div class=\"facet-widget\" id=\"img-select\" title=\"");
        html.append(getToolTipText());
        html.append("\">");
        html.append("<p>");
        html.append("<span id=\"img-selector-lbl\">Show only records with images from:</span>");
        
        html.append(chbx);
        html.append(ImageParam.INT.name());
        html.append(val);
        html.append("on");
        html.append(clss);
        if(ImageParam.INT.isOn()) html.append(" checked");
        if(ImageParam.INT.isOn() && !ImageParam.INT.hasRecord()) html.append(" disabled");
        html.append(close);
        html.append(lblstrt);
        html.append(ImageParam.INT.name());
        html.append("\"");
        html.append(close);
        html.append(ImageParam.INT.getMessage());
        html.append(lblend);
        
        html.append(chbx);
        html.append(ImageParam.EXT.name());
        html.append(val);
        html.append("on");
        html.append(clss);
        if(ImageParam.EXT.isOn()) html.append(" checked");
        if(ImageParam.EXT.isOn() && !ImageParam.EXT.hasRecord()) html.append(" disabled");
        html.append(close);
        html.append(lblstrt);
        html.append(ImageParam.EXT.name());
        html.append("\"");
        html.append(close);
        html.append(ImageParam.EXT.getMessage());
        html.append(lblend);        

        html.append(chbx);
        html.append(ImageParam.PRINT.name());
        html.append(val);
        html.append("on");
        html.append(clss);
        if(ImageParam.PRINT.isOn()) html.append(" checked");
        if(ImageParam.PRINT.isOn() && !ImageParam.PRINT.hasRecord()) html.append(" disabled");
        html.append(close);
        html.append(lblstrt);
        html.append(ImageParam.PRINT.name());
        html.append("\"");
        html.append(close);
        html.append(ImageParam.PRINT.getMessage());
        html.append(lblend);
              
        html.append("</p>");
        html.append("</div><!-- closing .facet-widget -->");
        return html.toString();
        
        
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        ArrayList<ImageParam> onParams = new ArrayList<ImageParam>();
        
        for(int i = 0; i < ImageParam.values().length; i++){
            
            ImageParam ip = ImageParam.values()[i];
            solrQuery.addFacetField(ip.getSearchField());
            if(ip.isOn()){
                System.out.println(ip.name() + " found to be on.");
                onParams.add(ip);
            }
            
        }
        
        if(onParams.size() < 1) return solrQuery;
        String fp = "(";
        Iterator<ImageParam> ipit = onParams.iterator();
        while(ipit.hasNext()){
            
            ImageParam onParam = ipit.next();
            System.out.println("OnParam is " + onParam);
            fp += onParam.getSearchField() + ":true";
            if(ipit.hasNext()) fp += " OR ";     
            
        }
        
        fp += ")";
           
        solrQuery.addFilterQuery(fp);
        return solrQuery;
        
    }
    
    
    @Override
    public Boolean addConstraints(Map<String, String[]> params){
       
        Boolean constraintAdded = false;
        
        for(ImageParam ip : ImageParam.values()){
            ip.setIsOn(false);
            System.out.println("Image param is " + ip.name());
            
            if(params.containsKey(ip.name())){
                System.out.println("Triggered on " + ip.name());
                ip.setIsOn(true);
                constraintAdded = true;
                
            }         
            
        }
        
        return constraintAdded;
        
    }
    
    @Override
    public String getAsQueryString(){
        
        String qs = "";
        
        for(ImageParam ip : ImageParam.values()){
            
            if(ip.isOn()){
                
                qs += ip.name() + "=on&";
                
            }
            
        }
        
        if(qs.endsWith("&")){
            
            qs = qs.substring(0, qs.length() -1);
            
        }
        
        return qs;
        
    }
    
    @Override
    public void setWidgetValues(QueryResponse qr){
    
        for(ImageParam ip : ImageParam.values()){
    
            FacetField facetField = qr.getFacetField(ip.getSearchField());
            List<Count> valuesAndCounts = facetField.getValues();
            Iterator<Count> cit = valuesAndCounts.iterator();
            while(cit.hasNext()){
                
                Count count = cit.next();
                if("true".equals(count.getName()) && count.getCount() > 0) ip.setHasRecord(Boolean.TRUE);
                
            }
    
    
        }
            
    }
    
    @Override
    public String getAsFilteredQueryString(String filterParam, String filterValue){
        
        return "";
        
    }
    
   @Override
   public ArrayList<String> getFacetConstraints(String facetParam){
        
       return new ArrayList<String>();
        
    }
   
    @Override   
    public String getDisplayValue(String value){
        
        return "";
        
    }
    

    
}
