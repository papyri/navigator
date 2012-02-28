package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Facet class for selecting records based on the availability of images within
 * papyri.info, online more generally, or in print publications.
 * 
 * This facet is unusual only that the controls operate in a somewhat inverse way
 * to those of other facets: while the controls of other facets are generally 
 * <em>subtractive</em>, the controls for this facet are <em>additive</em>: while
 * with other facets adding values narrows the set of documents that meet the 
 * specified criteria, with this facet adding values expands it.
 * 
 * 
 * @author thill
 */

public class HasImagesFacet extends Facet {
    
    /**
     * Enum listing the possible values for the controls.
     * 
     * Note the uses of each field of the Enum. The Enum name serves both to
     * identify the enum and supplies the name of the relevant HTML form control; the
     * label string provides the label for this control; and the searchField gives
     * the relevant Solr field.
     * 
     */
    
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
    
    /**
     * Each possible value has a corresponding <code>searchConfiguration</code> to
     * manage it.
     * 
     * @see info.papyri.dispatch.browse.facet.HasImagesFacet.SearchConfiguration
     * 
     */
    
    private EnumMap<ImageParam, SearchConfiguration> searchConfigurations;
    
    public HasImagesFacet(){
    
        super(SolrField.images_int, FacetParam.IMG, "Records with images only");
        searchConfigurations = new EnumMap<ImageParam, SearchConfiguration>(ImageParam.class);
        for(ImageParam ip : ImageParam.values()){  searchConfigurations.put(ip, new SearchConfiguration(ip)); }
    
    }

    
    @Override
    public String getToolTipText(){
        
        return "Filter records based on associated images.";       
        
    } 
    
    
    @Override
    // Note that state is maintained purely through checkbox controls; there are
    // no hidden fields for this facet
    String generateHiddenFields(){ return ""; }
    
    @Override
    // Output is a troika of checkboxes, with their own submit button 
    // Note, however, that this button submits the entire form, as is 
    // necessary
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
        html.append("/");
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
        html.append("/");
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
        html.append("/");
        html.append(close);
        html.append(lblstrt);
        html.append(ImageParam.PRINT.name());
        html.append("\"");
        html.append(close);
        html.append(ImageParam.PRINT.getLabel());
        html.append(lblend);
        html.append("<input type=\"submit\" value=\"Go\" id=\"img-go\" class=\"ui-button ui-widget ui-state-default ui-corner-all\" role=\"button\" aria-disabled=\"false\"/>");              
        html.append("</p>");
        html.append("</div><!-- closing .facet-widget -->");
        return html.toString();
        
        
    }
    
    /**
     * Returns the set of <code>ImageParam</code>s which are currently set to "on"
     * 
     * @return 
     */
    
    private ArrayList<ImageParam> getAllOnImageParams(){
        
        ArrayList<ImageParam> ons = new ArrayList<ImageParam>();
        
        for(ImageParam ip : ImageParam.values()){  if(searchConfigurations.get(ip).isOn()) ons.add(ip); }
        
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
    // the display values returned by HasImagesFacet#getDisplayValue are self-explanatory:
    // accordingly, no separate display value is required
    public String getDisplayValue(String value){
        
        return "";
        
    }
    
    /**
     * Internal management class instantiated for each <code>ImageParam</code>.
     * 
     * @see info.papyri.dispatch.browse.facet.HasImagesFacet.ImageParam
     * 
     */
    
    private class SearchConfiguration{
    
        /** Records whether or not the HTML control (checkbox) is activated */
        private Boolean on;
        /** Records whether or not any records are associated with the 'on' state
         *  of the HTML control
         */
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
