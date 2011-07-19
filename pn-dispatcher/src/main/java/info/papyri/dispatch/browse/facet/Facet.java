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
    String formName;
    
    /** The label displayed to the user */
    String displayName;
    static String defaultValue = "--- All values ---";
    
    /**
     * Constructor
     * 
     * @param sf
     * @param formName
     * @param displayName 
     */
    public Facet(SolrField sf, String formName, String displayName){
        
        this.field = sf; 
        this.formName = formName;
        this.displayName = displayName;
        
    }
    
    /**
     * Modifies the passed <code>SolrQuery</code> to reflect the constraints and 
     * faceting information required by the <code>Facet</code>
     * 
     * @param solrQuery
     * @return The passed solrQuery, modified
     * @see FacetBrowser#buildFacetQuery(int, java.util.EnumMap) 
     */
    
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        solrQuery.addFacetField(field.name());
        solrQuery.setFacetLimit(-1);
        
        Iterator<String> cit = facetConstraints.iterator();
        
        while(cit.hasNext()){
            
            String fq = cit.next();
            // slash-escape madness: java, solr, and java.regex all use backslash
            // as an escape character
            fq = fq.replaceAll("\\\\", "\\\\\\\\");
            if(fq.contains(" ")) fq = "\"" + fq + "\"";
            fq = field + ":" + fq;
            solrQuery.addFilterQuery(fq);
            
            
        }
        
        return solrQuery;
        
    }
    
    /** 
     * Generates the HTML form element used for input.
     * 
     * @return A string representation of the requisite HTML
     * @see FacetBrowser#assembleWidgetHTML(java.util.EnumMap, java.lang.StringBuffer) 
     */
    
     public String generateWidget() {
        
        StringBuffer html = new StringBuffer("<div class=\"facet-widget\" title=\"" + getToolTipText() + "\">");
        html.append(generateHiddenFields());
        Boolean onlyOneValue = valuesAndCounts.size() == 1;
        String disabled = onlyOneValue ? " disabled=\"true\"" : "";
        String defaultSelected = onlyOneValue ? "" : "selected=\"true\"";
        html.append("<span class=\"option-label\">" + getDisplayName(null) + "</span>");
        html.append("<select" + disabled + " name=\"" + formName + "\">");
        html.append("<option " + defaultSelected +  " value=\"default\">" + Facet.defaultValue + "</option>");
        
        Iterator<Count> vcit = valuesAndCounts.iterator();
        
        while(vcit.hasNext()){
            
            Count valueAndCount = vcit.next();
            String value = valueAndCount.getName();
            String displayValue = getDisplayValue(value);
            if(displayValue.length() > 60) displayValue = displayValue.substring(0, 60);
            String count = String.valueOf(valueAndCount.getCount());
            String selected = onlyOneValue ? " selected=\"true\"" : "";
            html.append("<option" + selected + " value=\"" + value + "\">" + displayValue + " (" + count + ")</option>");
            
        }
        
        html.append("</select>");
        html.append("</div><!-- closing .facet-widget -->");

        return html.toString();
        
    }
    
    /**
     * Returns the <code>Facet</code>'s constraints as a query string.
     * 
     * Required for pagination links to maintain state across pages.
     * 
     * @return 
     * @see FacetBrowser#doPagination(java.util.EnumMap, long) 
     * @see FacetBrowser#buildFullQueryString(java.util.EnumMap) 
     */
    
    public String getAsQueryString(){
        
        String queryString = "";
        
        Iterator<String> cit = facetConstraints.iterator();
        while(cit.hasNext()){
            
            String value = cit.next();
            queryString += formName + "=" + value;
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
     * @return 
     * @see FacetBrowser#assemblePreviousValuesHTML(java.util.EnumMap, java.lang.StringBuffer) 
     */
    
    public String getAsFilteredQueryString(String filterParam, String filterValue){
        
        String queryString = "";
        
        Iterator<String> cit = facetConstraints.iterator();
        while(cit.hasNext()){
            
            String value = cit.next();

            if(!value.equals(filterValue)){
                
                queryString += formName + "=" + value;
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
     * @see FacetBrowser#populateFacets(java.util.EnumMap, org.apache.solr.client.solrj.response.QueryResponse) 
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
     * Generates a hidden field for previously-selected constraints on the <code>Facet</code>.
     * 
     * 
     * @return 
     */
    
    String generateHiddenFields(){
        
        String html = "";
        
        for(int i = 0; i < facetConstraints.size(); i++){
            
            String name = formName; 
            String value = facetConstraints.get(i);
            html += "<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>";
            
        }
        
        return html;
        
    }
    
    public Boolean addConstraints(Map<String, String[]> params){
        
        Boolean hasConstraint = false;
        
        if(params.containsKey(this.formName)){
            
            String[] values = params.get(this.formName);
            
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
    
     void addConstraint(String newValue){
        
        if(newValue.equals(Facet.defaultValue)) return;
        if(!facetConstraints.contains(newValue)) facetConstraints.add(newValue.trim());
        
        
    }
    
    public SolrField getFacetField(){
        
        return field;
        
    }
    
    public ArrayList<String> getFacetConstraints(String facetParam){
        
        return facetConstraints;
        
    }
    
    public String getDisplayName(String facetParam){
        
        return displayName;
        
    }
    
    public String getDisplayValue(String value){
        
        return value;
        
    }
    
    public String[] getFormNames(){
        
        String[] formNames = {formName};
        
        return formNames;
        
        
    }
    
    abstract String getToolTipText();
 
    
}
