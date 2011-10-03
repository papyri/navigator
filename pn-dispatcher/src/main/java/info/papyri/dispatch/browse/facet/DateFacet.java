package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * The <code>Facet</code> used for setting and displaying date constraints.
 * 
 * Note that unlike the other <code>Facet</code>s, <code>DateFacet</code> uses two
 * widgets, and stores two values. This complicates the code internal to each method considerably.
 * 
 * Note also that these values (stored in the terminusAfterWhich and terminusBeforeWhich members) are
 * triple-state: they may be '0' (effectively, 'undefined'), 'Unknown', or the <code>String</code>
 * representation of an <code>Integer</code> value.
 * 
 * 
 * @author thill
 */
public class DateFacet extends Facet {

    /** The interval the difference between each date_category represents, in years.
     * 
     * Note that this cannot be changed arbitrarily in the code alone; the value
     * reflects that used in the Solr index
     */
   static int INTERVAL = 50;                                                    
   
   /** A flag indicating whether the date has the value 'unknown'.
    * 
    * This value is special because
    * (a) it cannot be converted to a number; and 
    * (b) if *either* the terminus ante quem *or* the terminus post quem have the
    *     value 'unknown', then *both* must have the value 'unknown'. Note that this
    *     is not an arbitrary decision, but reflects the structure of data in the Solr
    *     index.
    * 
    */
   private static SolrField flagField = SolrField.unknown_date_flag;
   
   /**
    * Comparator used in ordering dates in the <code>Facet</code> widget.
    */
   private static Comparator dateCountComparator;  
   
   /** The lowest (=earliest) date used by the <code>Facet</code> */
   private static int LOWER_BOUND = -50;                                        // = 2500 BCE
  /** The highest (=latest) date used by the <code>Facet</code> */
   private static int UPPER_BOUND = 42;                                         // = 2100 CE 
   /**
    * Specifies the date *after which* documents will be returned.
    * 
    * Note that this is a <code>String</code>; it may hold either a string
    * value that evaluates to an integer (LOWER_BOUND <= x <= UPPER_BOUND) or the 
    * value 'Unknown'.
    * 
    * Note that the value '0' here serves as 'undefined' - there is no year zero
    * 
    */
   private String terminusAfterWhich = "0";
      /**
    * Specifies the date *before which* documents will be returned.
    * 
    * Note that this is a <code>String</code>; it may hold either a string
    * value that evaluates to an integer (LOWER_BOUND <= x <= UPPER_BOUND) or the 
    * value 'Unknown'.
    * 
    * Note that the value '0' here serves as 'undefined' - there is no year zero
    * 
    */
   private String terminusBeforeWhich = "0";
   /**
    * This is the complement of the <code>valuesAndCounts</code> member.
    * 
    * It is necessary because, unlike other <code>Facet</code>s, <code>this</code>
    * is associated with two widgets, hence two sets of values and counts. The 
    * standard <code>Facet.valuesAndCounts</code> <code>List</code> holds the 
    * values associated with the terminusAfterWhich widget; the complement holds
    * those associated with the terminusBeforeWhich widget, calculated by subtracting
    * the number of documents found for each category in <code>valuesAndCounts</code>
    * from the total number of documents found.
    * 
    * @see Facet#valuesAndCounts
    * @see DateFacet#setBeforeWhichWidgetValues(org.apache.solr.client.solrj.response.QueryResponse) 
    */
   List<Count> valuesAndCountsComplement;
   
   public DateFacet(){
        
        super(SolrField.date_category, FacetParam.DATE_START, "Date dummy");
        
        // dates need to sort with 'Unknown' at the top
        // followed by BCE dates (= negative date_category value)
        // followed by CE dates (= positive date_category value)
        valuesAndCountsComplement = new ArrayList<Count>();
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
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
           
        solrQuery.addFacetField(flagField.name());
        
        if(terminusAfterWhich.equals("Unknown") || terminusBeforeWhich.equals("Unknown")){
            
            solrQuery.addFilterQuery(flagField.name() + ":true");
            return solrQuery;
            
        }
        
        solrQuery.addFacetField(field.name());
        int startTerminus = terminusAfterWhich.equals("0") ? LOWER_BOUND : Integer.valueOf(terminusAfterWhich);
        int endTerminus = terminusBeforeWhich.equals("0") ? UPPER_BOUND : Integer.valueOf(terminusBeforeWhich);
        
        if(!terminusAfterWhich.equals("0") || !terminusBeforeWhich.equals("0")){
        
            solrQuery.addFilterQuery(field.name() + ":[" + String.valueOf(startTerminus) + " TO " + String.valueOf(endTerminus) + "]");
        
        }
        for(int i = startTerminus; i <= endTerminus; i++){
                    
            if(i != 0){
                
                solrQuery.addFacetQuery(field.name() + ":[" + String.valueOf(i) + " TO " + String.valueOf(endTerminus) + "]");
            
            }
        }
        
        
        return solrQuery;
        
    }   
   
   
    @Override
    public String getAsQueryString(){
        
        if(terminusAfterWhich.equals("Unknown")){
            
            return FacetParam.DATE_START.name() + "=Unknown";
            
        }
        else if(terminusBeforeWhich.equals("Unknown")){
            
            return FacetParam.DATE_END.name() + "=Unknown";
            
        }
        
        String tAfter = terminusAfterWhich.equals("0") ? "" : FacetParam.DATE_START.name() + "=" + terminusAfterWhich; 
        String tBefore = terminusBeforeWhich.equals("0") ? "" : FacetParam.DATE_END.name() + "=" + terminusBeforeWhich;
        
        String queryString = "";
        if(!tAfter.equals("") && !tBefore.equals("")){
            
            queryString = tAfter + "&" + tBefore;
            
        }
        else{
            
            queryString = tAfter + tBefore;
            
        }
        
        return queryString;
        
    }
    
    @Override
    public String getAsFilteredQueryString(String filterParam, String filterValue){
        
        String filterField = "";
        String terminus = "0";
        
        if(filterParam.equals(FacetParam.DATE_START.name())){
            
            if(!terminusBeforeWhich.equals("0")){
                
                filterField = FacetParam.DATE_END.name();
                terminus = terminusBeforeWhich;
                
                
            }
            
        }
        else{
            
            if(!terminusAfterWhich.equals("0")){
                
                filterField = FacetParam.DATE_START.name();
                terminus = terminusAfterWhich;
                
            }
            
            
        }
        
        if("".equals(filterField)) return "";
        
        String value = terminusBeforeWhich.equals("Unknown") || terminusAfterWhich.equals("Unknown") ? "Unknown" : String.valueOf(terminus);
        
        return filterField + "=" + value;
        
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
    
    @Override
    public void setWidgetValues(QueryResponse queryResponse){
        
        setAfterWhichWidgetValues(queryResponse);
        setBeforeWhichWidgetValues(queryResponse);
         
    }
    
    
    public void setAfterWhichWidgetValues(QueryResponse queryResponse){
        
        valuesAndCounts = new ArrayList<Count>();
        
        valuesAndCounts = addUnknownCount((ArrayList<Count>)valuesAndCounts, queryResponse);
        
        Map<String, Integer> facetQueries = queryResponse.getFacetQuery();
        int bottomLimit = getLowestCategoryWithMembers(queryResponse);
        for(Map.Entry<String, Integer> entry : facetQueries.entrySet()){
            
            String rawQueryName = entry.getKey();
            Pattern pattern = Pattern.compile("^" + field.name() + ":\\[\\s*(-?\\d+)\\s*TO\\s*-?\\d+\\s*\\]" + "$");
            Matcher matcher = pattern.matcher(rawQueryName);
            if(matcher.matches()){
            
                String queryName = String.valueOf(matcher.group(1));
                Integer queryCount = entry.getValue();
                
                if(queryName != null && !queryName.equals("") && !queryName.equals("null") && queryCount > 0 && Integer.valueOf(queryName) >= bottomLimit){
                    
                    Count count = new Count(new FacetField(field.name()), queryName, queryCount);
                    valuesAndCounts.add(count);                 
                    
                }
                
            }
                 
        }
        
        Collections.sort(valuesAndCounts, dateCountComparator);
           
        
    }
    
    public void setBeforeWhichWidgetValues(QueryResponse queryResponse){
        
        valuesAndCountsComplement = new ArrayList<Count>();
                
        valuesAndCountsComplement = addUnknownCount((ArrayList<Count>)valuesAndCountsComplement, queryResponse);
        
        if(terminusAfterWhich.equals("Unknown") || terminusBeforeWhich.equals("Unknown")) return;

        long totalCount = queryResponse.getResults().getNumFound();
        List<Count> counts = queryResponse.getFacetField(field.name()).getValues();
        Iterator<Count> cit = counts.iterator();
        long missingCount = 0;

        while(cit.hasNext()){
            
            Count count = cit.next();
            String name = count.getName();
            if(name == null || name.equals("")){
                
                missingCount = count.getCount();
                
            }
   
            
        }
        
        
        Map<String, Integer> facetQueries = queryResponse.getFacetQuery();
                
        int bottomLimit = getLowestCategoryWithMembers(queryResponse);
        
        for(Map.Entry<String, Integer> entry : facetQueries.entrySet()){
            
            String rawQueryName = entry.getKey();
            Pattern pattern = Pattern.compile("^" + field.name() + ":\\[\\s*(-?\\d+)\\s*TO\\s*-?\\d+\\s*\\]" + "$");
            Matcher matcher = pattern.matcher(rawQueryName);
            if(matcher.matches()){
            
                String queryName = matcher.group(1);
                
                if(Integer.valueOf(queryName) >= bottomLimit){
                
                    long rawValue = getNextSignificantCount(facetQueries, entry.getValue());

                    long queryCount = totalCount - rawValue - missingCount;

                    if(queryName != null && !queryName.equals("") && !queryName.equals("null") && queryCount > 0){

                        Count count = new Count(new FacetField(field.name()), queryName, queryCount);
                        valuesAndCountsComplement.add(count);                 

                    }
                
                }
                
            }
                 
        }
        
        Collections.sort(valuesAndCountsComplement, dateCountComparator);         
        
    } 
    
    private long getNextSignificantCount(Map<String, Integer> facetQueries, long currentValue){
        
        ArrayList<Long> counts = new ArrayList<Long>();
        
        for(Map.Entry<String, Integer> entry : facetQueries.entrySet()){
            
            long count = entry.getValue();
            if(count < currentValue) counts.add(count);
                  
        }
        
        if(counts.size() == 0) return 0;
        
        return Collections.max(counts);
        
    }
    
    public String generateWidget() {
        
        String afterWhichWidget = generateAfterWhichWidget();
        String beforeWhichWidget = generateBeforeWhichWidget();
        String hiddenFields =  generateHiddenFields();

        return hiddenFields + afterWhichWidget + beforeWhichWidget;
        
    }
    
    private String generateAfterWhichWidget(){
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" title=\"");
        html.append(getAfterWhichToolTipText());
        html.append("\">");
        html.append("<p>");
        Boolean onlyOneValue = valuesAndCounts.size() == 1;
        String disabled = onlyOneValue ? " disabled=\"true\"" : "";
        String defaultSelected = onlyOneValue ? "" : "selected=\"true\"";
        html.append("<span class=\"option-label\">Date on or after</span>");
        html.append("<select");
        html.append(disabled);
        html.append(" name=\"");
        html.append(FacetParam.DATE_START.name());
        html.append("\">");
        html.append("<option ");
        html.append(defaultSelected);
        html.append("  value=\"default\">");
        html.append(Facet.defaultValue);
        html.append("</option>");
        
        Iterator<Count> vcit = valuesAndCounts.iterator();
        
        while(vcit.hasNext()){
            
            Count valueAndCount = vcit.next();
            String value = valueAndCount.getName();
            String displayValue = getDisplayValue(value);
            String count = String.valueOf(valueAndCount.getCount());
            String selected = (onlyOneValue || value.equals(terminusAfterWhich)) ? " selected=\"true\"" : "";
            html.append("<option");
            html.append(selected);
            html.append(" value=\"");
            html.append(value);
            html.append("\">");
            html.append(displayValue);
            html.append(" (");
            html.append(count);
            html.append(")</option>");
            if(value.equals("Unknown")){
                
                html.append("<optgroup label=\"-------------------\"></optgroup>");
                
            }
            
        }
        
        html.append("</select>");
        html.append("</p>");
        html.append("</div><!-- closing .facet-widget -->");

        return html.toString();  
        
    }
    
    private String generateBeforeWhichWidget(){
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" title=\"");
        html.append(getBeforeWhichToolTipText());
        html.append("\">");
        html.append("<p>");
        Boolean onlyOneValue = valuesAndCountsComplement.size() == 1;
        String disabled = onlyOneValue ? " disabled=\"true\"" : "";
        String defaultSelected = onlyOneValue ? "" : "selected=\"true\"";
        html.append("<span class=\"option-label\">Date on or before</span>");
        html.append("<select");
        html.append(disabled);
        html.append(" name=\"");
        html.append(FacetParam.DATE_END.name());
        html.append("\">");
        html.append("<option ");
        html.append(defaultSelected);
        html.append("  value=\"default\">");
        html.append(Facet.defaultValue);
        html.append("</option>");
        
        Iterator<Count> vcit = valuesAndCountsComplement.iterator();
        
        while(vcit.hasNext()){
            
            Count valueAndCount = vcit.next();
            String value = valueAndCount.getName();
            String displayValue = getDisplayValue(value);
            String count = String.valueOf(valueAndCount.getCount());
            String selected = (onlyOneValue || value.equals(terminusBeforeWhich)) ? " selected=\"true\"" : "";
            html.append("<option");
            html.append(selected);
            html.append(" value=\"");
            html.append(value);
            html.append("\">");
            html.append(displayValue);
            html.append(" (");
            html.append(count);
            html.append(")</option>");
            if(value.equals("Unknown")){
                
                html.append("<optgroup label=\"-------------------\"></optgroup>");
                
            }
            
        }
        
        html.append("</select>");
        html.append("</p>");
        html.append("</div><!-- closing .facet-widget -->");

        return html.toString();         
        
        
    }
    
    int getLowestCategoryWithMembers(QueryResponse queryResponse){
        
        if(terminusAfterWhich.equals("Unknown") || terminusBeforeWhich.equals("Unknown")) return 0;
        
        List<Count> counts = queryResponse.getFacetField(field.name()).getValues();
        
        for(int i = LOWER_BOUND; i <= UPPER_BOUND; i++){
            
            Iterator<Count> cit = counts.iterator();
            while(cit.hasNext()){
                
                Count count = cit.next();
                String name = count.getName();
                
                long number = count.getCount();
                
                if(name != null && Integer.valueOf(name) == i && number > 0) return i;
                
                
            }       
            
        }
        
        return 0;
    }

    @Override
    public ArrayList<String> getFacetConstraints(String facetParam){
        
        ArrayList<String> constraints = new ArrayList<String>();
               
        if(facetParam.equals(FacetParam.DATE_START.name()) && !terminusAfterWhich.equals("0")) constraints.add(terminusAfterWhich);
        
        if(facetParam.equals(FacetParam.DATE_END.name()) && !terminusBeforeWhich.equals("0")) constraints.add(String.valueOf(terminusBeforeWhich));
        
        return constraints;
        
    }
    
    @Override
    String generateHiddenFields(){
        
        String html = "";
        
        if(!terminusAfterWhich.equals("0")){
            
            html += "<input type=\"hidden\" name=\"" + FacetParam.DATE_START.name() + "\" value=\"" + String.valueOf(terminusAfterWhich) + "\"/>";
            
        }
        if(!terminusBeforeWhich.equals("0")){
            
            html += "<input type=\"hidden\" name=\"" + FacetParam.DATE_END.name() + "\" value=\"" + String.valueOf(terminusBeforeWhich) + "\"/>";
            
        }

        
        return html;
        
    }
    
    public ArrayList<Count> addUnknownCount(ArrayList<Count> valuesAndCounts, QueryResponse queryResponse){
        
        long unknownCountNumber = getUnknownCount(queryResponse);
        Count unknownCount = new Count(new FacetField(flagField.name()), "Unknown", unknownCountNumber);
        if(unknownCountNumber > 0){
            
            valuesAndCounts.add(unknownCount);
        }
        
        return valuesAndCounts;
        
    }
    
    private long getUnknownCount(QueryResponse queryResponse){
             
        List<Count> counts = queryResponse.getFacetField(flagField.name()).getValues();
        Iterator<Count> cit = counts.iterator();
        while(cit.hasNext()){
            
            Count count = cit.next();
            if(count.getName()!= null && count.getName().equals("true")) return count.getCount();
            
        }
        
        return 0;
        
    }
    
    @Override
    public Boolean addConstraints(Map<String, String[]> params){
                
        Boolean hasConstraint = false;
        
        if(params.containsKey(FacetParam.DATE_START.name())){
            
            String[] startValues = params.get(FacetParam.DATE_START.name());
            
            for(int i = 0; i < startValues.length; i++){
                
                String dateStart = startValues[i];
                
                if(dateStart != null && !dateStart.equals("") && !dateStart.equals("default")){

                
                    if(terminusAfterWhich.equals("0")|| dateStart.equals("Unknown")){
                               
                            hasConstraint = true;               
                           terminusAfterWhich = dateStart;
                      
                
                    }
                    else{
                    
                        if(Integer.valueOf(dateStart) > Integer.valueOf(terminusAfterWhich)){
                               
                                hasConstraint = true;               
                                terminusAfterWhich = dateStart;
   
                       }
                    
                    }
                    
                }
                
            }
                
           }

        if(params.containsKey(FacetParam.DATE_END.name())){
            
           String[] endValues = params.get(FacetParam.DATE_END.name());
           
           for(int j = 0; j < endValues.length; j++){
               
               String dateEnd = endValues[j];
               
               if(dateEnd != null && !dateEnd.equals("") && !dateEnd.equals("default")){

               
                if(terminusBeforeWhich.equals("0") || dateEnd.equals("Unknown")){
                    
                
                            hasConstraint = true;               
                            terminusBeforeWhich = dateEnd;
                            
                     }
                
                else{
                    
                    if(Integer.valueOf(dateEnd) < Integer.valueOf(terminusBeforeWhich)){
                        
                
                            hasConstraint = true;               
                            terminusBeforeWhich = dateEnd;
                            
                        
                        
                    }
                    
                }              
               
               }
               
           }
      
        }
        
        return hasConstraint;
        
    }
    
    @Override
    public String getDisplayName(String facetParam, java.lang.String facetValue){
        
        if(facetParam.equals(FacetParam.DATE_START.name())) return "Date on or after";
        return "Date on or before";
        
    }
        
    @Override
    public String[] getFormNames(){
        
        String[] formNames = {FacetParam.DATE_START.name(), FacetParam.DATE_END.name()};
        return formNames;
        
        
    }


    @Override
    String getToolTipText() {
        
        return "TODO: fill in tool tips for date facet";
        
    }
    
    String getAfterWhichToolTipText(){
        
        return "The date after which the document is believed to have been in existence - i.e., the terminus post quem";
        
    }
    
    String getBeforeWhichToolTipText(){
        
        return "The date before which the document is believed to have been in existence - i.e., the terminus ante quem.";
        
    }
    
}
