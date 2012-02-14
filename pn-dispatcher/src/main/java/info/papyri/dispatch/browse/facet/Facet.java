package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.facet.customexceptions.InternalQueryException;
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
 * Handles all the necessary backend (Solr) and frontend (HTML) interactions needed
 * for selection of constraint values within a single facet.
 * 
 * 
 * @author thill
 */
abstract public class Facet {
    
    
    /** A <code>List</code> of values to which the Solr responses must (possibly after processing
     *  by this class) conform
     */
    ArrayList<String> facetConstraints = new ArrayList<String>();
    
    /** A <code>List</code> of all values fond in the faceted field, along with the number
     * of each.
     * 
     * Note the peculiarity of Solr terminology here: a <code>Count</code> object is 
     * actually a member of a <code>FacetField</code>, and holds information both on
     * the <code>String</code> representation of the value, and the number associated 
     * with it (i.e., the "count" in the normal sense of the word)
     */
    List<Count> valuesAndCounts;
    /** The relevant Solr field */
    SolrField field;
    /** The value used for the <code>name</code> attribute in the <code>Facet</code>'s
     *  HTML control.
     * 
     *  @see FacetParam
     */
    FacetParam formName;
    
    /** The label displayed to the user */
    String displayName;
    
    /** Default value to be displayed if no value set
     * 
     *  Note that this value is only applicable for drop-down selectors; some
     *  subclasses may require a different default to be specified
     */
    static String defaultValue = "--- All values ---";
    
    /**
     * Constructor
     * 
     * @param sf
     * @param formName
     * @param displayName 
     */
    public Facet(SolrField sf, FacetParam formName, String displayName){
        
        this.field = sf; 
        this.formName = formName;
        this.displayName = displayName;
        valuesAndCounts = new ArrayList<Count>();
    }
    
    /**
     * Modifies the passed <code>SolrQuery</code> to reflect the constraints set upon, and 
     * faceting information required by, the <code>Facet</code>
     * 
     * @param solrQuery
     * @return The passed solrQuery, modified
     * @see FacetBrowser#buildFacetQuery(int, java.util.ArrayList) 
     */
    
    public SolrQuery buildQueryContribution(SolrQuery solrQuery) throws InternalQueryException{
        
        solrQuery.addFacetField(field.name());
        solrQuery.setFacetLimit(-1);                // = no limit
        
        Iterator<String> cit = facetConstraints.iterator();
        
        while(cit.hasNext()){
            
            String fq = cit.next();
            // slash-escape madness: java, solr, and java.regex all use backslash
            // as an escape character
            fq = fq.replaceAll("\\\\", "\\\\\\\\");
            if(fq.contains(" ")) fq = "\"" + fq + "\"";
            fq = field.name() + ":" + fq;
            solrQuery.addFilterQuery(fq);
            
            
        }
        
        return solrQuery;
        
    }
    
    /** 
     * Generates the HTML form element used for input.
     * 
     * @return A string representation of the requisite HTML
     * @see FacetBrowser#assembleWidgetHTML(java.util.ArrayList, java.lang.StringBuilder, java.util.Map) 
     */
    
     public String generateWidget() {
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" title=\"");
        html.append(getToolTipText());                                          
        html.append("\">");
        html.append(generateHiddenFields()); 
        html.append("<p>");
        // if only one value possible, then gray out control
        Boolean onlyOneValue = valuesAndCounts.size() == 1;
        Boolean allSelected = facetConstraints.size() == valuesAndCounts.size();
        String disabled = (onlyOneValue || allSelected) ? " disabled=\"true\"" : "";             
        String defaultSelected = (onlyOneValue || allSelected) ? "" : "selected=\"true\"";
        html.append("<span class=\"option-label\">");
        html.append(getDisplayName(null, null));
        html.append("</span>");
        html.append("<select");
        html.append(disabled);
        html.append(" name=\"");
        html.append(formName.name());
        html.append("\">");
        html.append("<option ");
        html.append(defaultSelected);
        html.append(" value=\"default\">");
        html.append(Facet.defaultValue);
        html.append("</option>");
        
        Boolean oneConstraintSet = facetConstraints.size() == 1;
                
        Iterator<Count> vcit = valuesAndCounts.iterator();
        
        while(vcit.hasNext()){
            
            Count valueAndCount = vcit.next();
            String value = valueAndCount.getName();
            String displayValue = getDisplayValue(value);
            // truncate if too long; otherwise control potentially takes up whole screen
            if(displayValue.length() > 35) displayValue = displayValue.substring(0, 35);    
            String count = String.valueOf(valueAndCount.getCount());
            String selected = onlyOneValue || (oneConstraintSet && value.equals(facetConstraints.get(0)))? " selected=\"true\"" : "";
            html.append("<option");
            html.append(selected);
            html.append(" value=\"");
            html.append(value);
            html.append("\">");
            html.append(displayValue);
            html.append(" (");
            html.append(count);
            html.append(")</option>");
            
        }
        
        html.append("</select>");
        html.append("</p>");
        html.append("</div><!-- closing .facet-widget -->");

        return html.toString();
        
    }
    
    /**
     * Returns the <code>Facet</code>'s constraints as a query string.
     * 
     * Required for pagination links to maintain state across pages.
     * 
     * @return A querystring representing a <code>Facet</code>s constraints.
     * @see FacetBrowser#doPagination(java.util.ArrayList, long) 
     * @see FacetBrowser#buildFullQueryString(java.util.ArrayList) 
     */
    
    public String getAsQueryString(){
        
        String queryString = "";
        
        Iterator<String> cit = facetConstraints.iterator();
        while(cit.hasNext()){
            
            String value = cit.next();
            queryString += formName.name() + "=" + value;
            if(cit.hasNext()) queryString += "&";
            
            
        }
        
        return queryString;
        
    }
    
    /**
     * Returns the <code>Facet</code>'s constraints as a query string, minus the 
     * value passed to the method,
     * 
     * Required for the anchor links that (from the user's perspective) 'remove' 
     * constraints from the faceted display.
     * 
     * @return A querystring representing the <code>Facet</code>'s constraints, excluding the
     * value passed as a <code>String</code>.
     * @see FacetBrowser#assemblePreviousValuesHTML(java.util.ArrayList, java.lang.StringBuilder, java.util.Map) 
     */
    
    public String getAsFilteredQueryString(String filterParam, String filterValue){
        
        String queryString = "";
        
        Iterator<String> cit = facetConstraints.iterator();
        while(cit.hasNext()){
            
            String value = cit.next();

            if(!value.equals(filterValue)){
                
                queryString += formName.name() + "=" + value;
                queryString += "&";
                
            }
            
        }
        
        if(!"".equals(queryString) && queryString.substring(queryString.length() - 1).equals("&")) queryString = queryString.substring(0, queryString.length() -1);
        return queryString;
        
    }
    
    /**
     * Sets the values to be displayed by the <code>Facet</code>'s HTML form control.
     * 
     * 
     * @param queryResponse
     * @see FacetBrowser#populateFacets(java.util.ArrayList, org.apache.solr.client.solrj.response.QueryResponse)  
     */

    public void setWidgetValues(QueryResponse queryResponse){
        
        FacetField facetField = queryResponse.getFacetField(field.name());
        valuesAndCounts = new ArrayList<Count>();
        List<Count> unfiltered = facetField.getValues();
        Iterator<Count> cit = unfiltered.iterator();
        while(cit.hasNext()){
            
            Count count = cit.next();            
            if(count.getName() != null && !count.getName().equals("") && count.getCount() > 0 && !count.getName().equals("null")) valuesAndCounts.add(count);
            
        }
          
    } 
    
   /**
     * Generates hidden fields for previously-selected constraints on the <code>Facet</code>.
     * 
     * 
     * @return The HTML for the hidden fields, as a <code>String</code>.
     */
    
    String generateHiddenFields(){
        
        String html = "";
        
        for(int i = 0; i < facetConstraints.size(); i++){
            
            String name = formName.name(); 
            String value = facetConstraints.get(i);
            html += "<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>";
            
        }
        
        return html;
        
    }
    
    /**
     * Parses the parameters submitted in the <code>HttpServletRequest</code>  and stores
     * those relevant to the <code>Facet</code> in question
     * 
     * @param params
     * @return A <code>Boolean</code> indicating whether or not a constraint exists on the current 
     * <code>Facet</code>.
     */
    
    public Boolean addConstraints(Map<String, String[]> params){
        
        Boolean hasConstraint = false;
        
        if(params.containsKey(this.formName.name())){
            
            String[] values = params.get(formName.name());
            
            for(int i = 0; i < values.length; i++){
                
                try{
                    
                    String param = java.net.URLDecoder.decode(values[i], "UTF-8");

                    if(param != null && !param.equals("default") && !param.equals("")){
                            
                         addConstraint(param);
                         hasConstraint = true;   
                    }
                    
                    
                }
                catch(UnsupportedEncodingException uee){
                    
                    System.out.println("UnsupportedEncodingException: " + uee.getMessage());
                    
                }
                
            }
            
        }
        
        return hasConstraint;
        
    }
    
    /**
     * Parses each individual value submitted to the <code>Facet</code>.
     * 
     * The chief purpose of the method as defined here (i.e., in the superclass) is to weed out 
     * default values and prevent them being used as constraints. Subclasses with 
     * idiosyncratic values may have considerably more complex behavior.
     * 
     * @param newValue 
     */
    
     void addConstraint(String newValue){
        
        if(newValue.equals(Facet.defaultValue)) return;
        if(!facetConstraints.contains(newValue)) facetConstraints.add(newValue.trim());
        
        
    }
     
     /**
      * Returns the value(s) to be used for the name attribute on HTML form controls.
      * 
      * In most cases only one value is required, and will be tat of the <code>formName</code>
      * member. However, some facets have more than one HTML control - hence the need for 
      * this method to return an array of Strings, rather than a String.
      * 
      * @return 
      */
     
    public String[] getFormNames(){
        
        String[] formNames = {formName.name()};
        
        return formNames;
          
    }
    
    /**
     * Returns the <code>formName</code> member in lower case, to be used as an id value for
     * the HTML form control
     * 
     * @return 
     */
    
    public String getCSSSelectorID(){
        
        return this.formName.name().toLowerCase();
        
    }

    /**
     * Takes a raw facet value and formats it appropriately for display in the facet's 
     * HTML form control.
     * 
     * Under most circumstances, the passed value itself will be appropriate for display;
     * some subclasses, however, may need to override this method to cope with particular
     * values requiring special treatment.
     * 
     * 
     * @param value
     * @return 
     */
    
    public String getDisplayValue(String value){
        
        return value;
        
    }
    
     /* getters and setters below */
    
    public SolrField getFacetField(){
        
        return field;
        
    }
    
    public ArrayList<String> getFacetConstraints(String facetParam){
        
        return facetConstraints;
        
    }
    
    public String getDisplayName(String facetParam, java.lang.String facetValue){
        
        return displayName;
        
    }
    
    abstract String getToolTipText();
 
    
}
