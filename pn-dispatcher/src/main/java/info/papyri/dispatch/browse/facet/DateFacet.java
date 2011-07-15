/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.Comparator;
import org.apache.solr.client.solrj.response.FacetField.Count;

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
                
                if(Integer.valueOf(count1.getName()) < Integer.valueOf(count2.getName())) return -1;
                if(Integer.valueOf(count1.getName()) > Integer.valueOf(count2.getName())) return 1;
                return 0;
                
            }
        };
    }
   
    @Override
    public String getAsQueryString(){
        
        String queryString = terminus == 0 ? "" : formName + "=" + String.valueOf(terminus); 
        return queryString;
        
    }
    
    @Override
    public String getAsFilteredQueryString(String filterValue){
        
        return "";
        
        
    }
    
    @Override
    public String getDisplayValue(String dateCode){
        
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
    
    
    @Override
    public ArrayList<String> getFacetConstraints(){
        
        ArrayList<String> constraints = new ArrayList<String>();
        
        if(terminus != 0) constraints.add(String.valueOf(terminus));
        
        return constraints;
        
    }
    
    @Override
    String generateHiddenFields(){
        
        String html = "";
        
        if(terminus != 0){
            
            html += "<input type=\"hidden\" name=\"" + formName + "\" value=\"" + String.valueOf(terminus) + "\"/>";
            
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
