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
 *
 * @author thill
 */
public class DateFacet extends Facet {

   static int INTERVAL = 50;        // years
   private static SolrField flagField = SolrField.unknown_date_flag;
   private static Comparator dateCountComparator;
   private static int LOWER_BOUND = -50;
   private static int UPPER_BOUND = 50;
   private String terminusAfterWhich = "0";
   private String terminusBeforeWhich = "0";
   List<Count> valuesAndCountsComplement;
   private SolrQuery completedQuery;
   
   
   public DateFacet(){
        
        super(SolrField.date_category, FacetParam.DATE_START, "Date dummy");
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
        solrQuery.addFacetField(field.name());

       if(terminusAfterWhich.equals("Unknown") || terminusBeforeWhich.equals("Unknown")){
            
            solrQuery.addFilterQuery(flagField.name() + ":true");
            
            if((!terminusAfterWhich.equals("Unknown") && !terminusAfterWhich.equals("0")) || (!terminusBeforeWhich.equals("Unknown") && !terminusBeforeWhich.equals("0"))){
                
                solrQuery.addFilterQuery(field.name() + ":0");    
                
            }
      }
      else{
            

            int startTerminus = terminusAfterWhich.equals("0") ? LOWER_BOUND : Integer.valueOf(terminusAfterWhich);
            int endTerminus = terminusBeforeWhich.equals("0") ? UPPER_BOUND : Integer.valueOf(terminusBeforeWhich);

            if(!terminusAfterWhich.equals("0") || !terminusBeforeWhich.equals("0")){

                solrQuery.addFilterQuery(field.name() + ":[" + String.valueOf(startTerminus) + " TO " + String.valueOf(endTerminus) + "]");

            }
            
        }
        for(int i = LOWER_BOUND; i <= UPPER_BOUND; i++){
                    
            if(i != 0){
                
                solrQuery.addFacetQuery(field.name() + ":[" + String.valueOf(i) + " TO " + String.valueOf(UPPER_BOUND) + "]");
            
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
                
        return filterField + "=" + terminus;
        
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
               
        removeAllDateRelatedFilterQueries(completedQuery);
        
        if(!terminusBeforeWhich.equals("0")){
            
            if(terminusBeforeWhich.equals("Unknown")){
                
                completedQuery.addFilterQuery(flagField.name() + ":true");
                
                
            }
            else{
                
                completedQuery.addFilterQuery(this.field.name() + ":[" + String.valueOf(LOWER_BOUND) + " TO " + String.valueOf(terminusBeforeWhich) + "]");             
                
            }
            
            
        }
        
        
        QueryResponse afterWhichQueryResponse = FacetBrowser.runFacetQuery(completedQuery);
        
        removeAllDateRelatedFilterQueries(completedQuery);
        
        if(!terminusAfterWhich.equals("0")){
            
            if(terminusAfterWhich.equals("Unknown")){
            
                completedQuery.addFilterQuery(flagField.name() + ":true");
            
            }
            else{
                
                completedQuery.addFilterQuery(this.field.name() + ":[" + String.valueOf(terminusAfterWhich) + " TO " + String.valueOf(UPPER_BOUND) + "]");
                
                
            }
            
        }
        
        QueryResponse beforeWhichQueryResponse = FacetBrowser.runFacetQuery(completedQuery);
   
        setAfterWhichWidgetValues(afterWhichQueryResponse);
        setBeforeWhichWidgetValues(beforeWhichQueryResponse);
        
         
    }
    
    private void removeAllDateRelatedFilterQueries(SolrQuery solrQuery){
                
        String[] filterQueries = solrQuery.getFilterQueries();
        
        
        if(filterQueries != null){
            
            for(int i = 0; i < filterQueries.length; i++){
                
                String filterQuery = filterQueries[i];
                
                if(filterQuery.contains(this.field.name()) || filterQuery.contains(flagField.name())) solrQuery.removeFilterQuery(filterQuery);
            
            }
        }
       
    }
    
    
    public void setAfterWhichWidgetValues(QueryResponse queryResponse){
        
        valuesAndCounts = new ArrayList<Count>();
        
        valuesAndCounts = addUnknownCount((ArrayList<Count>)valuesAndCounts, queryResponse);
        
        Map<String, Integer> facetQueries = queryResponse.getFacetQuery();
       // int bottomLimit = getLowestCategoryWithMembers(queryResponse);
        for(Map.Entry<String, Integer> entry : facetQueries.entrySet()){
            
            String rawQueryName = entry.getKey();
            Pattern pattern = Pattern.compile("^" + field.name() + ":\\[\\s*(-?\\d+)\\s*TO\\s*-?\\d+\\s*\\]" + "$");
            Matcher matcher = pattern.matcher(rawQueryName);
            
            if(matcher.matches()){
            
                String queryName = String.valueOf(matcher.group(1));
                Integer queryCount = entry.getValue();
                
                if(queryName != null && !queryName.equals("") && !queryName.equals("null")){
                    
                    if(!terminusBeforeWhich.equals("0") && (terminusBeforeWhich.equals("Unknown") || Integer.valueOf(queryName) > Integer.valueOf(terminusBeforeWhich))){
                        
                        queryCount = 0;
                        
                    }
                    
                    
                    Count count = new Count(new FacetField(field.name()), queryName, queryCount);
                    valuesAndCounts.add(count);                 
                    
                }
                
            }
            
            Pattern unknownPattern = Pattern.compile("^" + flagField.name() + ":true$");
            Matcher unknownMatcher = unknownPattern.matcher(rawQueryName);
            if(unknownMatcher.matches()){
                
                String name = "Unknown";
                Integer queryCount = entry.getValue();
                
                Count count = new Count(new FacetField(field.name()), name, queryCount);
                valuesAndCounts.add(count);
                
                
            }
                 
        }
        
        Collections.sort(valuesAndCounts, dateCountComparator);
           
        
    }
    
    public void setBeforeWhichWidgetValues(QueryResponse queryResponse){
        
        valuesAndCountsComplement = new ArrayList<Count>();
                
        valuesAndCountsComplement = addUnknownCount((ArrayList<Count>)valuesAndCountsComplement, queryResponse);
        
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
                                
                long rawValue = getNextSignificantCount(facetQueries, entry.getValue());
                
                long queryCount = totalCount - rawValue - missingCount;
                
                if(queryName != null && !queryName.equals("") && !queryName.equals("null")){
                    
                    if(!terminusAfterWhich.equals("0") && (terminusAfterWhich.equals("Unknown") || Integer.valueOf(queryName) < Integer.valueOf(terminusAfterWhich)) || Integer.valueOf(queryName) < bottomLimit){
                        
                        queryCount = 0;
                        
                    }
                    
                    Count count = new Count(new FacetField(field.name()), queryName, queryCount);
                    valuesAndCountsComplement.add(count);                 
                    
                }
                
            }
            
            Pattern unknownPattern = Pattern.compile("^" + flagField.name() + ":true$");
            Matcher unknownMatcher = unknownPattern.matcher(rawQueryName);
            if(unknownMatcher.matches()){
                
                String name = "Unknown";
                Integer queryCount = entry.getValue();
                
                Count count = new Count(new FacetField(field.name()), name, queryCount);
                valuesAndCounts.add(count);
                
                
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
        
        if(counts.isEmpty()) return 0;
        
        return Collections.max(counts);
        
    }
    
    @Override
    public String generateWidget() {
        
        String hiddenFields =  generateHiddenFields();
        String afterWhichWidget = generateAfterWhichWidget();
        String beforeWhichWidget = generateBeforeWhichWidget();

        return hiddenFields + afterWhichWidget + beforeWhichWidget;
        
    }
    
    private String generateAfterWhichWidget(){
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" title=\"");
        html.append(getAfterWhichToolTipText());
        html.append("\">");
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
            String selected = onlyOneValue ? " selected=\"true\"" : "";
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
        html.append("</div><!-- closing .facet-widget -->");

        return html.toString();  
        
    }
    
    private String generateBeforeWhichWidget(){
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" title=\"");
        html.append(getBeforeWhichToolTipText());
        html.append("\">");
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
            String selected = onlyOneValue ? " selected=\"true\"" : "";
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
        valuesAndCounts.add(unknownCount);
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
            
            ArrayList<String> filteredStartValues = chopDefaultValues(startValues);
            
            if(!filteredStartValues.isEmpty()){
            
                String lastSubmitted = filteredStartValues.get(filteredStartValues.size() - 1);

                terminusAfterWhich = lastSubmitted;

                hasConstraint = true;
           
            }
                
        }

        if(params.containsKey(FacetParam.DATE_END.name())){
            
           String[] endValues = params.get(FacetParam.DATE_END.name());
           
           ArrayList<String> filteredEndValues = chopDefaultValues(endValues);
           
           if(!filteredEndValues.isEmpty()){
           
               String lastSubmitted = filteredEndValues.get(filteredEndValues.size() - 1);

               terminusBeforeWhich = lastSubmitted;

               hasConstraint = true;

           }
        }
        
        return hasConstraint;
        
    }
    
    private ArrayList<String> chopDefaultValues(String[] unfiltered){
        
        ArrayList<String> filtered = new ArrayList<String>();
        
        for(int i = 0; i < unfiltered.length; i++){
            
            String testValue = unfiltered[i];
            if(!testValue.equals("default") && !testValue.equals(Facet.defaultValue)){
                
                filtered.add(testValue);
                
                
            }
            
            
        }
            
        return filtered;
        
    }
    
    @Override
    public String getDisplayName(String facetParam){
        
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
        
        return "TODO: fill in tool tips for date facet";
        
    }
    
    String getBeforeWhichToolTipText(){
        
        return "TODO: fill in tool tips for date facet";
        
    }
    
    void setCompletedQuery(SolrQuery completedQuery){
        
        this.completedQuery = completedQuery;
        
    }
    
}
