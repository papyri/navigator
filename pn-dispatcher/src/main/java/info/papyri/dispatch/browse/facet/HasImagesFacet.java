package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

public class HasImagesFacet extends Facet {
    
    enum ImageParam{
        
        INT ("Papyri.info", SolrField.images_int),
        EXT ("Other sites", SolrField.images_ext), 
        PRINT("Print publications", SolrField.illustrations);
    
        private final String label;
        private final SolrField searchField;
        
        ImageParam(String msg, SolrField sf){
            
            this.label = msg;
            this.searchField = sf;
            
        }
        
        public String getLabel(){ return this.label; }
        public String getSelector(){ return "img-" + this.name().toLowerCase(); }
        public String getSearchField(){ return this.searchField.name().replace("_", "-"); }
    
    };
    
    private EnumMap<ImageParam, SearchConfiguration> searchConfigurations;
    
    public HasImagesFacet(){
    
        super(SolrField.images_int, FacetParam.IMG, "Records with images only");
        searchConfigurations = new EnumMap<ImageParam, SearchConfiguration>(ImageParam.class);
        for(ImageParam ip : ImageParam.values()){
            
            searchConfigurations.put(ip, new SearchConfiguration(ip));
          
        }
    
    }

    
    @Override
    public String getToolTipText(){
        
        return "Filter records based on associated images.";       
        
    } 
    
    @Override
    String generateHiddenFields(){ return ""; }
    
    @Override
    public String generateWidget() {
        
        Boolean moreThanOneOn = getAllOnImageParams().size() > 1;
        
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
        html.append("<p id=\"img-selector-lbl\">Show only records with images from:</p>");
        html.append("<p>");
        
        SearchConfiguration intObj = searchConfigurations.get(ImageParam.INT);
        html.append(chbx);
        html.append(ImageParam.INT.name());
        html.append(val);
        html.append("on");
        html.append(clss);
        if(intObj.isOn()) html.append(" checked");
        if(intObj.isOn() && moreThanOneOn && !intObj.hasRecord()) html.append(" disabled");
        html.append(close);
        html.append(lblstrt);
        html.append(ImageParam.INT.name());
        html.append("\"");
        html.append(close);
        html.append(ImageParam.INT.getLabel());
        html.append(lblend);
        
        SearchConfiguration extObj = searchConfigurations.get(ImageParam.EXT);
        html.append(chbx);
        html.append(ImageParam.EXT.name());
        html.append(val);
        html.append("on");
        html.append(clss);
        if(extObj.isOn()) html.append(" checked");
        if(extObj.isOn() && moreThanOneOn && !extObj.hasRecord()) html.append(" disabled");
        html.append(close);
        html.append(lblstrt);
        html.append(ImageParam.EXT.name());
        html.append("\"");
        html.append(close);
        html.append(ImageParam.EXT.getLabel());
        html.append(lblend);        

        SearchConfiguration prObj = searchConfigurations.get(ImageParam.PRINT);
        html.append(chbx);
        html.append(ImageParam.PRINT.name());
        html.append(val);
        html.append("on");
        html.append(clss);
        if(prObj.isOn()) html.append(" checked");
        if(prObj.isOn() && moreThanOneOn && !prObj.hasRecord()) html.append(" disabled");
        html.append(close);
        html.append(lblstrt);
        html.append(ImageParam.PRINT.name());
        html.append("\"");
        html.append(close);
        html.append(ImageParam.PRINT.getLabel());
        html.append(lblend);
              
        html.append("</p>");
        html.append("</div><!-- closing .facet-widget -->");
        return html.toString();
        
        
    }
    
    private ArrayList<ImageParam> getAllOnImageParams(){
        
        ArrayList<ImageParam> ons = new ArrayList<ImageParam>();
        
        for(ImageParam ip : ImageParam.values()){
            
            if(searchConfigurations.get(ip).isOn()) ons.add(ip);
                   
        }
        
        return ons;
        
        
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
                
        for(int i = 0; i < ImageParam.values().length; i++){
            
            ImageParam ip = ImageParam.values()[i];
            solrQuery.addFacetField(ip.getSearchField());
            
        }
        
        ArrayList<ImageParam> onParams = getAllOnImageParams();
        
        if(onParams.size() < 1) return solrQuery;
        String fp = "(";
        Iterator<ImageParam> ipit = onParams.iterator();
        while(ipit.hasNext()){
            
            ImageParam onParam = ipit.next();
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
            
            if(params.containsKey(ip.name())){

                searchConfigurations.get(ip).setIsOn(true);
                constraintAdded = true;
                
            }         
            
        }
        
        return constraintAdded;
        
    }
    
    @Override
    public String getAsQueryString(){
        
        String qs = "";
        
        for(ImageParam ip : ImageParam.values()){
            
            if(searchConfigurations.get(ip).isOn()){
                
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
            List<Count> vsAndCs = facetField.getValues();
            Iterator<Count> cit = vsAndCs.iterator();
            while(cit.hasNext()){
                
                Count count = cit.next();
                if("true".equals(count.getName()) && count.getCount() > 0) searchConfigurations.get(ip).setHasRecord(Boolean.TRUE);
                
            }
     
        }
            
    }
    
    @Override
    public String[] getFormNames(){
        
        String[] formNames = new String[ImageParam.values().length];
        
        for(int i = 0; i < ImageParam.values().length; i++){
            
            formNames[i] = ImageParam.values()[i].name();
            
            
        }
        
        return formNames;
        
    }
    
    @Override
    public String getAsFilteredQueryString(String filterParam, String filterValue){
        
        String queryString = "";
        ArrayList<ImageParam> ons = getAllOnImageParams();
        
        for(ImageParam ip : ons){
                    
            if(!ip.name().equals(filterParam)) queryString += ip.name() + "=on&";
                        
        }
        
        if(queryString.endsWith("&")) queryString = queryString.substring(0, queryString.length() - 1);
        
        return queryString;
        
    }
    
   @Override
   public ArrayList<String> getFacetConstraints(String facetParam){
        
       ArrayList<ImageParam> ips = getAllOnImageParams();
       ArrayList<String> ipStringArray = new ArrayList<String>();
       for(ImageParam ip : ips){
           
           if(ip.name().equals(facetParam)) ipStringArray.add(ip.name());
           
       }
       
       return ipStringArray;
        
    }
   
   @Override
   public String getDisplayName(String facetParam, java.lang.String facetValue){
       
       ArrayList<ImageParam> onParams = getAllOnImageParams();
       String displayMsg = "";
       
       for(int i = 0; i < onParams.size(); i++){
           
           ImageParam ip = onParams.get(i);
           
           if(ip.name().equals(facetParam)){
               
               if(ip.equals(ImageParam.INT)){
                   
                   displayMsg = "Papyri.info image";
                   
               }
               else if(ip.equals(ImageParam.EXT)){
                   
                   displayMsg = "Image hosted externally";
                   
               }
               else if(ip.equals(ImageParam.PRINT)){
                   
                   displayMsg = "Print image exists";
                   
               }
               
              if(i < onParams.size() - 1) displayMsg += " OR ";
               
           }
           
           
       }
       
       return displayMsg;
       
   }
   
    @Override   
    public String getDisplayValue(String value){
        
        return "";
        
    }
    
    private class SearchConfiguration{
    
        private Boolean on;
        private Boolean hasRecords;
        
        SearchConfiguration(ImageParam ip){
            
            on = false;
            hasRecords = false;
            
        }
        
        public void setIsOn(Boolean state){ on = state; }
        public Boolean isOn(){ return on; }
        public void setHasRecord(Boolean hr){ hasRecords = hr; }
        public Boolean hasRecord(){ return hasRecords; }
    
    
    }
    

    
}
