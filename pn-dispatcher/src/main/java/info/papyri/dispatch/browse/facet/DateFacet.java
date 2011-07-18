/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * @author thill
 */
abstract public class DateFacet extends Facet {

   static int INTERVAL = 50;        // years
   DateQueryCoordinator dateQueryCoordinator;
   Comparator dateCountComparator;
   /** Will be terminus post or terminus ante depending upon whether is
    *  DateStart or DateEnd
    */
   int terminus = 0;                    
    
   public DateFacet(String formName, String displayName){
        
        super(SolrField.date_category, formName, displayName);
        dateCountComparator = new Comparator() {

            @Override
            public int compare(Object t, Object t1) {
                
                Count count1 = (Count)t;
                Count count2 = (Count)t1;
                
                // value 'Unknown' should always sort first
                if(count1.getName().equals("Unknown") && count2.getName().equals("Unknown")) return 0;
                if(count1.getName().equals("Unknown")) return -1;
                if(count2.getName().equals("Unknown")) return 1;
                
                if(Integer.valueOf(count1.getName()) < Integer.valueOf(count2.getName())) return -1;
                if(Integer.valueOf(count1.getName()) > Integer.valueOf(count2.getName())) return 1;
                return 0;
                
            }
        };
    }
   
    @Override
    public String getAsQueryString(ArrayList<String> previousQueryStrings){
        
        if(dateQueryCoordinator.getUnknownDateFlag()){
         
            String[] dateFields = {FacetParam.DATE_START.name(), FacetParam.DATE_END.name()};
            String unknownString = "=Unknown";
            
            for(int i = 0; i < dateFields.length; i++){
                
                String testString = dateFields[i] + unknownString;
                if(previousQueryStrings.contains(testString)) return "";
                
            }
            
            return formName + unknownString;
            
        }
        String queryString = terminus == 0 ? "" : formName + "=" + String.valueOf(terminus); 
        return queryString;
        
    }
    
    @Override
    public String getAsFilteredQueryString(String filterValue){
        
        return "";
        
        
    }
    
    @Override
    public String getDisplayValue(String dateCode){
        
        if(dateCode.equals("Unknown")) return dateCode;
        int rawDate = Integer.valueOf(dateCode);
        
        String era;
        String startRange;
        String endRange;
        
        if(rawDate < 0){
            
            era = "BCE";
            int startNum = rawDate * INTERVAL;
            startRange = String.valueOf(startNum).substring(1);
            endRange = String.valueOf(startNum + (INTERVAL - 1)).substring(1);
            
        }
        else{
            
            era = "CE";
            int endNum = rawDate * INTERVAL;
            startRange = String.valueOf(endNum - (INTERVAL - 1));
            endRange = String.valueOf(endNum);
            
        }
        
        return startRange + "-" + endRange + " " + era; 
        
    }
    
    public String generateWidget() {
        
        StringBuffer html = new StringBuffer("<div class=\"facet-widget\" title=\"" + getToolTipText() + "\">");
        html.append(generateHiddenFields());
        Boolean onlyOneValue = valuesAndCounts.size() == 1;
        String disabled = onlyOneValue ? " disabled=\"true\"" : "";
        String defaultSelected = onlyOneValue ? "" : "selected=\"true\"";
        html.append("<span class=\"option-label\">" + getDisplayName() + "</span>");
        html.append("<select" + disabled + " name=\"" + formName + "\">");
        html.append("<option " + defaultSelected + "  value=\"default\">" + Facet.defaultValue + "</option>");
        
        Iterator<Count> vcit = valuesAndCounts.iterator();
        
        while(vcit.hasNext()){
            
            Count valueAndCount = vcit.next();
            String value = valueAndCount.getName();
            String displayValue = getDisplayValue(value);
            String count = String.valueOf(valueAndCount.getCount());
            String selected = onlyOneValue ? " selected=\"true\"" : "";
            html.append("<option" + selected + " value=\"" + value + "\">" + displayValue + " (" + count + ")</option>");
            if(value.equals("Unknown")){
                
                html.append("<optgroup label=\"-------------------\"></optgroup>");
                
            }
            
        }
        
        html.append("</select>");
        html.append("</div><!-- closing .facet-widget -->");

        return html.toString();
        
    }
    
    
    @Override
    public ArrayList<String> getFacetConstraints(){
        
        ArrayList<String> constraints = new ArrayList<String>();
        
        if(dateQueryCoordinator.getUnknownDateFlag()) constraints.add("Unknown");
        
        if(terminus != 0) constraints.add(String.valueOf(terminus));
        
        return constraints;
        
    }
    
    @Override
    String generateHiddenFields(){
        
        String html = "";
        
        if(terminus != 0){
            
            html += "<input type=\"hidden\" name=\"" + formName + "\" value=\"" + String.valueOf(terminus) + "\"/>";
            
        }
        else if(dateQueryCoordinator.getUnknownDateFlag()){
            
            html += "<input type=\"hidden\" name=\"" + formName + "\" value=\"Unknown\"/>";
            
        }
        
        return html;
        
    }
    
    @Override
    public void addConstraint(String newValue){
        
        terminus = Integer.valueOf(newValue.trim());
         
    }
    
    public void setDateQueryCoordinator(DateQueryCoordinator dqc){
        
        dateQueryCoordinator = dqc;
        
    }
    
    
}
