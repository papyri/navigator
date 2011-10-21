package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;

/**
 * The <code>Facet</code> used for setting and displaying date constraints.
 * 
 * Note that unlike the other <code>Facet</code>s, <code>DateFacet</code> uses two
 * widgets, and stores two values. This complicates the code internal to each method considerably.
 *
 * 
 * @author thill
 */
public class DateFacet extends Facet {

    /** The interval the difference between each date_category represents, in years.
     * 
     * 
     */
   static int INTERVAL = 50;
   static int RANGE_START = -2500;
   static int RANGE_END = 2500;
   
   /** A flag indicating whether the date has the value 'unknown'.
    * 
    * This value is special because
    * (a) it cannot be converted to a number; and 
    * (b) if *either* the terminus ante quem *or* the terminus post quem have the
    *     value 'unknown', then *both* must have the value 'unknown'. Note that this
    *     is not an arbitrary o, but reflects the structure of data in the Solr
    *     index.
    * 
    */
   private static SolrField flagField = SolrField.unknown_date_flag;
   
   enum DateParam{ DATE_MODE, DATE_START_TEXT, DATE_START_ERA, DATE_END_TEXT, DATE_END_ERA };
   /**
    * Comparator used in ordering dates in the <code>Facet</code> widget.
    */
   private static Comparator dateCountComparator;  
   
   Terminus terminusAfterWhich;
   Terminus terminusBeforeWhich;
   
   enum DateMode{ STRICT, LOOSE }
   
   DateMode dateModeDefault = DateMode.LOOSE;
   DateMode dateMode = dateModeDefault;
   
   public DateFacet(){
        
        super(SolrField.unknown_date_flag, FacetParam.DATE_START, "Date dummy");
        
        terminusBeforeWhich = new TerminusBeforeWhich("");
        terminusAfterWhich = new TerminusAfterWhich("");
        
        // dates need to sort with 'Unknown' at the top
        // followed by BCE dates (= negative date_category value)
        // followed by CE dates (= positive date_category value)
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
        
        if(terminusAfterWhich.getCurrentValue().equals("Unknown") || terminusBeforeWhich.getCurrentValue().equals("Unknown")){
            
            solrQuery.addFilterQuery(flagField.name() + ":" + Boolean.TRUE.toString());
            return solrQuery;
            
        }
       
        Integer startDate = terminusAfterWhich.getMostExtremeValue();
        Integer endDate = terminusBeforeWhich.getMostExtremeValue(); 
        if(endDate < startDate){
            
            solrQuery.addFilterQuery("-" + SolrField.earliest_date.name() + ":[* TO *]");
            solrQuery.addFilterQuery("-" + SolrField.unknown_date_flag.name() + ":true");
            
        }
        solrQuery.addNumericRangeFacet(SolrField.earliest_date.name(), startDate, endDate, INTERVAL);
        solrQuery.addNumericRangeFacet(SolrField.latest_date.name(), startDate, endDate, INTERVAL);
        terminusBeforeWhich.getQueryContribution(solrQuery);
        terminusAfterWhich.getQueryContribution(solrQuery);

        return solrQuery;
        
    }   
   
   
    @Override
    public String getAsQueryString(){
        
        String tBefore = terminusBeforeWhich.getAsQueryString();
        String tAfter = terminusAfterWhich.getAsQueryString();
        
        String queryString = "";
        if(!tAfter.equals("") && !tBefore.equals("")){
            
            queryString = tAfter + "&" + tBefore;
            
        }
        else{
            
            queryString = tAfter + tBefore;
            
        }
        
        if(!queryString.equals("")) queryString += "&" + DateParam.DATE_MODE.name() + "=" + dateMode.name();
        
        return queryString;
        
    }
    
    @Override
    public String getAsFilteredQueryString(String filterParam, String filterValue){
        
        if(filterValue.equals("Unknown")) return "";
        String queryString = "";
                
        if(filterParam.equals(DateParam.DATE_START_TEXT.name())){
            
            queryString = terminusBeforeWhich.getAsQueryString();
            
        }
        else{
            
            queryString = terminusAfterWhich.getAsQueryString();
                     
        }
        
        if(!queryString.equals("")) queryString += "&" + DateParam.DATE_MODE.name() + "=" + dateMode.name();
        return queryString;
                
    }
    
    @Override
    public String getDisplayValue(String date){
        
        if(date.equals("Unknown")) return date;
        int rawDate = Integer.valueOf(date);
        rawDate = rawDate == 0 ? 1 : rawDate;
        
        String era;
        
        if(rawDate < 0){
            
            era = "BCE";
            date = String.valueOf(rawDate).substring(1);
            
        }
        else{
            
            era = "CE";
            date = String.valueOf(rawDate);
        }
        
        return date + " " + era; 
        
    }
    
    @Override
    public void setWidgetValues(QueryResponse queryResponse){

        terminusBeforeWhich.calculateWidgetValues(queryResponse);
        terminusAfterWhich.calculateWidgetValues(queryResponse);
        
        if(dateMode == DateMode.LOOSE){

            long grandTotal = queryResponse.getResults().getNumFound();
            if(queryResponse.getFacetField(SolrField.unknown_date_flag.name()) != null){
                
                List<Count> counts = queryResponse.getFacetField(SolrField.unknown_date_flag.name()).getValues();
                for(Count count : counts){
                    
                    if(count.getName() != null && count.getName().equals("true")) grandTotal -= count.getCount();
                    
                }
                
            }
            ArrayList<Count> beforeWhichLooseValues = terminusBeforeWhich.calculateLooseWidgetValues(grandTotal);
            ArrayList<Count> afterWhichLooseValues = terminusAfterWhich.calculateLooseWidgetValues(grandTotal);
            terminusBeforeWhich.setValuesAndCounts(beforeWhichLooseValues);
            terminusAfterWhich.setValuesAndCounts(afterWhichLooseValues);
            
        }
        
        terminusBeforeWhich.filterValuesAndCounts();
        terminusAfterWhich.filterValuesAndCounts();
         
    }
       
    @Override
    public String generateWidget() {
        
        String afterWhichWidget = terminusAfterWhich.generateWidget();
        String beforeWhichWidget = terminusBeforeWhich.generateWidget();
        String modeSelector = generateModeSelector();
        return afterWhichWidget + beforeWhichWidget + modeSelector;
        
    }
    
    private String generateModeSelector(){
        
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"facet-widget date-facet-widget\" id=\"date-mode-facet-widget\">");
        String l = DateMode.LOOSE.name();
        String s = DateMode.STRICT.name();
        String checked = "\" checked=\"checked\" ";
        String unchecked = "\"";    
        String looseChecked = dateMode == DateMode.LOOSE ? checked : unchecked;
        String strictChecked = dateMode == DateMode.STRICT ? checked : unchecked;
        html.append("<input type=\"radio\" title=\"Items fall within defined timespan: i.e., their date-range overlaps with the range specified \" name=\"");
        html.append(DateParam.DATE_MODE.name());
        html.append("\" value=\"");
        html.append(l);
        html.append("\" id=\"");
        html.append(l);
        html.append(looseChecked);
        html.append("/>");
        html.append("<label for=\"");
        html.append(l);
        html.append("\">");
        html.append(l.substring(0, 1));
        html.append(l.toLowerCase().substring(1));
        html.append("</label>");
        html.append("<input type=\"radio\" title=\"Both ends of item's timespan fall within the timespan defined: i.e., the 'after' and 'before' values represent termini post and ante quem\" name=\"");
        html.append(DateParam.DATE_MODE.name());
        html.append("\" value=\"");
        html.append(s);
        html.append("\" id=\"");
        html.append(s);
        html.append(strictChecked);
        html.append("/>");
        html.append("<label for=\"");
        html.append(s);
        html.append("\">");
        html.append(s.substring(0, 1));
        html.append(s.toLowerCase().substring(1));
        html.append("</label>");  
        html.append("</div><!-- closing .facet-widget -->");
        return html.toString();
        
    }
    
    private String getEraOptions(String terminusEra){
        
        String allOptions = "";
        
        ArrayList<String> eras = new ArrayList<String>(Arrays.asList("BCE", "CE"));
        
        for(String era : eras){
            
            String tag = era.equals(terminusEra) ? "<option selected>" + era + "</option>" : "<option>" + era + "</option>";
            allOptions += tag;
            
        }
        
        return allOptions;
        
    }

    @Override
    public ArrayList<String> getFacetConstraints(String facetParam){
        
        ArrayList<String> constraints = new ArrayList<String>();
               
        if(facetParam.equals(DateParam.DATE_START_TEXT.name())) terminusAfterWhich.addCurrentValue(constraints);
        
        if(facetParam.equals(DateParam.DATE_END_TEXT.name())) terminusBeforeWhich.addCurrentValue(constraints);
        
        return constraints;
        
    }
    
    @Override
    String generateHiddenFields(){ return ""; }
    
    @Override
    public Boolean addConstraints(Map<String, String[]> params){
        
        if(params.containsKey(DateParam.DATE_MODE.name())){
            
            try{
                
                String[] dateModes = params.get(DateParam.DATE_MODE.name());
                DateMode selectedMode = null;
                for(int i = 0; i < dateModes.length; i++){
                    
                    String nowMode = dateModes[i];
                    if(nowMode != null && !nowMode.equals("")){
                        
                       selectedMode = DateMode.valueOf(nowMode);   
                       break;
                        
                    }
                    
                }
                
                dateMode = selectedMode == null ? dateModeDefault : selectedMode;
                
                
                
            }
            catch(IllegalArgumentException iae){

                dateMode = dateModeDefault;
                
            }
                 
        }
        
        Boolean hasStartDate = parseParamsForStartDate(params);
        Boolean hasEndDate = parseParamsForEndDate(params);
                
        return (hasStartDate || hasEndDate);
        
        
    }
    
    public Boolean parseParamsForStartDate(Map<String, String[]> params){
                
        if(params.containsKey(DateParam.DATE_START_TEXT.name())){
           
            String[] startValues = params.get(DateParam.DATE_START_TEXT.name());
            
            String startValue = null;
            
            for(int i = 0; i < startValues.length; i++){
                
                startValue = startValues[i];
                if(startValue != null && !startValue.equals("")) break;
                
                
            }
            if(startValue != null){
            
                if(startValue.equals("n.a.")){
                    
                    terminusAfterWhich.setCurrentValue("Unknown");
                    return true;
                }
                Pattern pattern = Pattern.compile("(\\d{1,4})");
                Matcher matcher = pattern.matcher(startValue);
            
                if(matcher.matches()){
                
                    String year = matcher.group();
                    if(year.equals("1")) year = "0";
                    
                    if(params.containsKey(DateParam.DATE_START_ERA.name()) && params.get(DateParam.DATE_START_ERA.name())[0].equals("BCE")){
                        
                        year = "-" + year;
                        
                    }
                    
                    terminusAfterWhich.setCurrentValue(year);
                    return true;
                }
                
            }
                      
        }
        
        
        return false;        
    }
    
    public Boolean parseParamsForEndDate(Map<String, String[]> params){
        
        if(params.containsKey(DateParam.DATE_END_TEXT.name())){
            
            String endValue = params.get(DateParam.DATE_END_TEXT.name())[0];
            
            if(endValue != null){
            
                if(endValue.equals("n.a.")){
                    
                    terminusBeforeWhich.setCurrentValue("Unknown");
                    return true;
                    
                }
                Pattern pattern = Pattern.compile("(\\d{1,4})");
                Matcher matcher = pattern.matcher(endValue);  
                
                if(matcher.matches()){
                    
                    String year = matcher.group();
                    if(year.equals("1")) year = "0";
                   
                    if(params.containsKey(DateParam.DATE_END_ERA.name()) && params.get(DateParam.DATE_END_ERA.name())[0].equals("BCE")){
                        
                        year = "-" + year;
                        
                    }
                    
                   terminusBeforeWhich.setCurrentValue(year);
                   return true; 
                    
                }
                
            }
            
        }
        
        return false;
        
    }
    
    @Override
    public String getDisplayName(String facetParam, String facetValue){
        
        if(facetParam.equals(DateParam.DATE_START_TEXT.name())) return terminusAfterWhich.getDisplayName();
        return terminusBeforeWhich.getDisplayName();
        
    }
        
    @Override
    public String[] getFormNames(){
        
        String[] formNames = {DateParam.DATE_START_TEXT.name(), DateParam.DATE_END_TEXT.name()};
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
    
    abstract class Terminus{
        
        ArrayList<Count> valuesAndCounts;
        String currentValue;
        private SolrField facetField;
        Comparator facetCountComparator;
        
        public Terminus(String value, SolrField f){
            
            currentValue = value;
            valuesAndCounts = new ArrayList<Count>();
            facetField = f;
            facetCountComparator = new Comparator()  {

                @Override
                public int compare(Object t, Object t1) {
                    
                    String c1 = ((RangeFacet.Count)t).getValue();
                    String c2 = ((RangeFacet.Count)t1).getValue();
                    
                    try{
                        
                        Integer i1 = Integer.valueOf(c1);
                        Integer i2 = Integer.valueOf(c2);                        
                        return  i1 - i2;
                        
                    } 
                    catch(NumberFormatException nfe){
                    
                        return 0;
                    
                    }
                    
                    
                }
            };
        }
        
        abstract SolrQuery getQueryContribution(SolrQuery solrQuery);
                      
        abstract String getAsQueryString();
        
        abstract String getDisplayName();
        
        abstract String getDisplayValue(String dateCode);
        
        abstract String generateWidget();
                                                
        abstract void orderFacetQueries(List<RangeFacet.Count> fqs);
        
        abstract void orderValuesAndCounts();
        
        
        void filterValuesAndCounts(){
            
            orderValuesAndCounts();
            ArrayList<Count> filteredCounts = new ArrayList<Count>();
            long previousCount = 0;
            
            for(Count vc : valuesAndCounts){
                
                long nowCount = vc.getCount();
                String nowName = vc.getName();
                if((nowCount > 0 && nowCount != previousCount) || nowName.equals(currentValue)) filteredCounts.add(vc);
                previousCount = nowCount;
                
            }
            
            if(filteredCounts.size() > 1){

                if(filteredCounts.get(0).getCount() == filteredCounts.get(1).getCount()) filteredCounts.remove(0);
                
            }
            Collections.sort(filteredCounts, DateFacet.dateCountComparator);
            valuesAndCounts = filteredCounts;         
            
        }       
        
        void calculateWidgetValues(QueryResponse qr) {
            
            addUnknownCount(qr);
            if(currentValue.equals("Unknown") || this.getOtherTerminus().getCurrentValue().equals("Unknown")) return;
            List<RangeFacet> facetQueries = qr.getFacetRanges();
            List<RangeFacet.Count> dateList = new ArrayList<RangeFacet.Count>();
                        
            for(RangeFacet rf : facetQueries){
            
                String rangeName = rf.getName();
                if(rangeName.equals(this.getFacetField().name())) dateList = rf.getCounts();
                      
            }
            this.calculateStrictWidgetValues(dateList);            
             
        }  
              
        HashMap<Integer, Long> mapRangeFacets(List<RangeFacet.Count> counts){
            
            HashMap<Integer, Long> rangeMap = new HashMap<Integer, Long>();
            
            for(RangeFacet.Count count : counts){
                
                Integer name = Integer.valueOf(count.getValue());
                long number = count.getCount();
                rangeMap.put(name, number);  
                
            }
                       
            return rangeMap;
            
        }
        
        public void addUnknownCount(QueryResponse queryResponse){
        
            long unknownCountNumber = getUnknownCount(queryResponse);
            Count unknownCount = new Count(new FacetField(flagField.name()), "Unknown", unknownCountNumber);
            if(unknownCountNumber > 0){

                valuesAndCounts.add(unknownCount);
            }
        
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
        
        
        Boolean active(){
            
            if(currentValue.equals("")) return false;
            return true;
                
        }
        
        Boolean otherIsUnknown(){
            
            return getOtherTerminus().getCurrentValue().equals("Unknown");

            
        }
        
        abstract Terminus getOtherTerminus();
        
        void addCurrentValue(ArrayList<String> constraints){ if(!currentValue.equals("")) constraints.add(currentValue); }
        
        void setCurrentValue(String newValue){  
           
            currentValue = newValue;
           
            
        }
          
        
        String getCurrentValue(){
            
            return currentValue;
            
        }
        

        
        SolrField getFacetField(){ 
            
            return this.facetField; 
        
        }
        
        abstract Integer getMostExtremeValue();
        
        Count pluckCountFromList(String desiredValue, List<Count> countList){
            
            for(Count count : countList){
                
                String value = count.getName();
                if(value.equals(desiredValue)) return count;
                
            }
            
            return null;
            
        }
        
        long getGrandTotal(List<Count> facetResponse){
            
            long grandTotal = 0;
            
            for(Count count : facetResponse){

                if(count.getName() != null && !count.getName().equals("")){
                
                      grandTotal += count.getCount();  
                    
                }
      
            }
            
            return grandTotal;
            
        }
        
        abstract void calculateStrictWidgetValues(List<RangeFacet.Count> facetQueries);
         
        abstract public ArrayList<Count> calculateLooseWidgetValues(long grandTotal);
          
        String getEra(){
            
            try{
                
                String era = "";
                if(Integer.valueOf(currentValue) <= -1){
                    
                    return "BCE";
                    
                }
                else if(Integer.valueOf(currentValue) >= 0){
                    
                    return "CE";
                    
                }
                
            }
            catch(NumberFormatException nfe){
                
                return "";
                
                
            }
            
            return "";
            
        }
        
        public ArrayList<Count> getValuesAndCounts(){ return this.valuesAndCounts; }
        
        public void setValuesAndCounts(ArrayList<Count> newCounts){ this.valuesAndCounts = newCounts; }
        
 
    }
    
    class TerminusAfterWhich extends Terminus{
        
        HashMap<Integer, Integer> cumulativeTotals;
        
        public TerminusAfterWhich(String value){
            
            super(value, SolrField.earliest_date);
    
        }

        @Override
        SolrQuery getQueryContribution(SolrQuery solrQuery) {
            
            if(active()){
                 
                String fq = "";
                if(dateMode == DateMode.STRICT){
                
                    Integer endDate = getOtherTerminus().getMostExtremeValue() - 1;
                    fq = this.getFacetField().name() + ":[" + String.valueOf(currentValue) + " TO " + String.valueOf(endDate) + "]";
                    
                }
                else{
                    
                    fq =  this.getOtherTerminus().getFacetField().name() + ":[" + String.valueOf(currentValue) + " TO " + DateFacet.RANGE_END + "]";
                      
                }
                solrQuery.addFilterQuery(fq);
            }
            
            return solrQuery;
            
        }

        @Override
        String getAsQueryString() {
            
            if(active()){
                
                String paramValue = currentValue;
                if(!paramValue.equals("Unknown")){
                    
                    paramValue = String.valueOf(Math.abs(Integer.valueOf(paramValue)));
                    
                    
                    
                }
                else{
                    
                    paramValue = "n.a.";
                    
                }
                
                String qs = DateParam.DATE_START_TEXT.name() + "=" + paramValue;
                
                if(!paramValue.equals("n.a.")) qs += "&" + DateParam.DATE_START_ERA.name() + "=" + getEra(); 
                
                return  qs;  
                
            }
            
            return "";
            
        }

        @Override
        String getDisplayValue(String dateCode) {
            
            
            if(dateCode.equals("Unknown")) return dateCode;
            Integer rawDate = Integer.valueOf(dateCode);
            String era = rawDate < 0 ? "BCE" : "CE";
            if(rawDate == 0) rawDate = 1;
            rawDate = Math.abs(rawDate);
            return String.valueOf(rawDate) + " " + era;
            
        }
        
        @Override
        public void orderFacetQueries(List<RangeFacet.Count> fqs){
            
            Collections.sort(fqs, facetCountComparator);
            Collections.reverse(fqs);
            
        }

        @Override
        String generateWidget() {  
            
            Boolean thisIsOdd = (!currentValue.equals("") && !currentValue.equals("Unknown") && Integer.valueOf(currentValue) % DateFacet.INTERVAL != 0) ? true : false;
            String otherValue = terminusBeforeWhich.getCurrentValue();
            Boolean thatIsOdd = (!otherValue.equals("") && !otherValue.equals("Unknown") && Integer.valueOf(otherValue) % DateFacet.INTERVAL != 0) ? true : false;
            Boolean oddValues = (thisIsOdd || thatIsOdd);
            
            Boolean startIsUnknown = currentValue.equals("Unknown") || terminusBeforeWhich.getCurrentValue().equals("Unknown");
            String startDisplayValue = startIsUnknown ? "n.a." : currentValue.replaceAll("^-", "");  
            startDisplayValue = startDisplayValue.equals("0") ? "1" : startDisplayValue;
            
            StringBuilder html = new StringBuilder("<div class=\"facet-widget date-facet-widget\" title=\"");
            html.append(getAfterWhichToolTipText());
            html.append("\">");
            Boolean onlyOneValue = valuesAndCounts.size() == 1;
            String defaultSelected = (onlyOneValue || oddValues) ? "" : "selected=\"true\"";
            String disabled = (onlyOneValue || oddValues) ? " disabled=\"true\"" : "";
            html.append("<span class=\"option-label\">Date on or after</span>");
            html.append("<select name=\"");
            html.append(FacetParam.DATE_START.name());
            html.append("\"");
            html.append(disabled);
            html.append(">");
            html.append("<option ");
            html.append(defaultSelected);
            html.append("  value=\"default\">");
            html.append(Facet.defaultValue);
            html.append("</option>");

            if(!oddValues){
            
                Iterator<Count> vcit = valuesAndCounts.iterator();

                while(vcit.hasNext()){

                    Count valueAndCount = vcit.next();
                    String value = valueAndCount.getName();
                    String displayValue = getDisplayValue(value);
                    String count = String.valueOf(valueAndCount.getCount());
                    String selected = (onlyOneValue || value.equals(currentValue)) ? " selected=\"true\"" : "";
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
                
            }

            html.append("</select>");
            
            html.append("<div class=\"date-input-box");
            if(startIsUnknown) html.append(" unknown-date");
            html.append("\">");
            html.append("<input type=\"text\" size=\"4\" maxlength=\"4\" name=\"");
            html.append(DateParam.DATE_START_TEXT.name());
            html.append("\" id=\"");
            html.append(DateParam.DATE_START_TEXT.name());
            html.append("\" value=\"");
            html.append(startDisplayValue);    
            html.append("\"");
            html.append("/>");
            html.append("</input>");

            html.append("<select name=\"");
            html.append(DateParam.DATE_START_ERA.name());
            html.append("\" id=\"");
            html.append(DateParam.DATE_START_ERA.name());
            html.append("\"");
            html.append(">");
            html.append(getEraOptions(this.getEra()));
            html.append("</select>");
            html.append("</div><!-- closing .date-input-box -->");
            html.append("</div><!-- closing .facet-widget -->");

            return html.toString();                    
            
        }
        
        @Override
        void calculateStrictWidgetValues(List<RangeFacet.Count> facetQueries) {
               
            long runningTotal = 0;
      
            HashMap<Integer, Long> responseAsMap = mapRangeFacets(facetQueries);
            
            for(int i = DateFacet.RANGE_END; i >= DateFacet.RANGE_START; i -= DateFacet.INTERVAL){
                
                String name = String.valueOf(i);
                long number = responseAsMap.containsKey(i) ? responseAsMap.get(i) : 0;
                runningTotal += number;
                Count newCount = new Count(new FacetField(getFacetField().name()), name, runningTotal);
                valuesAndCounts.add(newCount);
                   
            }
                        
            Collections.sort(valuesAndCounts, dateCountComparator);

        }

        @Override
        Integer getMostExtremeValue(){
            
            if(!currentValue.equals("Unknown") && !currentValue.equals("")) return Integer.valueOf(currentValue);
            return DateFacet.RANGE_START;
            
            
        }

        @Override
        String getDisplayName() {
            
            return "Date on or after";
            
        }

        @Override
        public ArrayList<Count> calculateLooseWidgetValues(long grandTotal) {
            
            ArrayList<Count> looseWidgetValues = new ArrayList<Count>();
            ArrayList<Count> complement = terminusBeforeWhich.getValuesAndCounts();
                    
            for(int i = 0; i < this.valuesAndCounts.size(); i++){
                
                try{
                    
                    Count count = this.valuesAndCounts.get(i); 
                    String name = count.getName();
                    if(name.equals("Unknown")){
                        
                        looseWidgetValues.add(count);
                        
                    }
                    else{
                        
                        long looseCount = i > 0 ? grandTotal - (complement.get(i).getCount()) : 0;
                        Count newCount = new Count(new FacetField(SolrField.earliest_date.name()), name, looseCount);
                        looseWidgetValues.add(newCount);
                    
                    }
                      
                }
                catch(NumberFormatException nfe){}
                catch(NullPointerException npe){}
           
            }
            Collections.sort(looseWidgetValues, dateCountComparator);
            return looseWidgetValues;
                     
        }

        @Override
        Terminus getOtherTerminus() {
            
            return terminusBeforeWhich;
            
        }

        @Override
        void orderValuesAndCounts() {
            
            Collections.sort(valuesAndCounts, DateFacet.dateCountComparator);
            
        }
        
        
    }
    
    class TerminusBeforeWhich extends Terminus{
        
       HashMap<Integer, Integer> cumulativeTotals;
        
        public TerminusBeforeWhich(String value){
            
            super(value, SolrField.latest_date);
            
        }

        @Override
        SolrQuery getQueryContribution(SolrQuery solrQuery) {
            
            if(active()){
                
                String fq = "";
                Integer endDate = Integer.valueOf(currentValue) - 1;
                
                if(dateMode == DateMode.STRICT){
                
                    Integer startDate = getOtherTerminus().getMostExtremeValue();
                    fq = this.getFacetField().name() + ":[" + String.valueOf(startDate) + " TO " + String.valueOf(endDate) + "]";
                    solrQuery.addFilterQuery(fq);
                
                }
                else{
                    
                    fq = this.getOtherTerminus().getFacetField().name() + ":[" + DateFacet.RANGE_START + " TO " + String.valueOf(endDate) + "]";
                    
                }
               
                solrQuery.addFilterQuery(fq);
            }
            
            return solrQuery;
            
        }
        
        @Override
        void calculateStrictWidgetValues(List<RangeFacet.Count> facetQueries) {
               
            long runningTotal = 0;
            HashMap<Integer, Long> responseAsMap = mapRangeFacets(facetQueries);
            int start = terminusAfterWhich.getMostExtremeValue();
            int end = this.getMostExtremeValue();
            
            for(int i = DateFacet.RANGE_START; i <= DateFacet.RANGE_END; i += DateFacet.INTERVAL){
                
                String name = String.valueOf(i);
                int retrievalNumber = i - DateFacet.INTERVAL;
                long number = responseAsMap.containsKey(retrievalNumber) ? responseAsMap.get(retrievalNumber) : 0;
                runningTotal += number;
                Count newCount = new Count(new FacetField(getFacetField().name()), name, runningTotal);
                valuesAndCounts.add(newCount);
                   
            }
            
            Collections.sort(valuesAndCounts, dateCountComparator);

        }        
        
        @Override
        String getAsQueryString() {
            
            if(active()){
                
                String paramValue = currentValue;
                if(!paramValue.equals("Unknown")){
                    
                    paramValue = String.valueOf(Math.abs(Integer.valueOf(currentValue)));
                    
                }
                else{
                    
                    paramValue = "n.a.";                    
                }
                
                String qs = DateParam.DATE_END_TEXT.name() + "=" + paramValue;
                if(!paramValue.equals("n.a.")) qs += "&" + DateParam.DATE_END_ERA.name() + "=" +  getEra();  
                return  qs; 
                
            }
            
            return "";
            
        }

        @Override
        String getDisplayValue(String date) {
            
            if(date.equals("Unknown")) return date;
            Integer rawDate = Integer.valueOf(date);
            if(rawDate == 0) rawDate = 1;
            String era = rawDate < 0 ? "BCE" : "CE";
            rawDate = Math.abs(rawDate);
            return String.valueOf(rawDate) + " " + era;
            
        }

        @Override
        String generateWidget() {
            
            Boolean thisIsOdd = (!currentValue.equals("") && !currentValue.equals("Unknown") && Integer.valueOf(currentValue) % DateFacet.INTERVAL != 0) ? true : false;
            String otherValue = terminusAfterWhich.getCurrentValue();
            Boolean thatIsOdd = (!otherValue.equals("") && !otherValue.equals("Unknown") && Integer.valueOf(otherValue) % DateFacet.INTERVAL != 0) ? true : false;
            Boolean oddValues = (thisIsOdd || thatIsOdd);

            Boolean endIsUnknown = currentValue.equals("Unknown") || terminusAfterWhich.getCurrentValue().equals("Unknown");
            String endDisplayValue = endIsUnknown? "n.a." : currentValue.replaceAll("^-", "");           
            endDisplayValue = endDisplayValue.equals("0") ? "1" : endDisplayValue;
            
            StringBuilder html = new StringBuilder("<div class=\"facet-widget date-facet-widget\" title=\"");
            html.append(getBeforeWhichToolTipText());
            html.append("\">");
            Boolean onlyOneValue = valuesAndCounts.size() == 1;
            String defaultSelected = (onlyOneValue || oddValues) ? " selected=\"true\"" : "";
            String disabled = (onlyOneValue || oddValues) ? " disabled=\"true\"" : "";
            html.append("<span class=\"option-label\">Date before</span>");
            html.append("<select name=\"");
            html.append(FacetParam.DATE_END.name());
            html.append("\"");
            html.append(disabled);
            html.append(">");
            html.append("<option ");
            html.append(defaultSelected);
            html.append("  value=\"default\">");
            html.append(Facet.defaultValue);
            html.append("</option>");

            if(!oddValues){
                     
                Iterator<Count> vcit = valuesAndCounts.iterator();

                while(vcit.hasNext()){

                    Count valueAndCount = vcit.next();
                    String value = valueAndCount.getName();
                    String displayValue = getDisplayValue(value);
                    String count = String.valueOf(valueAndCount.getCount());
                    String selected = (onlyOneValue || value.equals(currentValue)) ? " selected=\"true\"" : "";
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

            }

            html.append("</select>");
            html.append("<div class=\"date-input-box");
            if(endIsUnknown) html.append(" unknown-date");
            html.append("\">");
            html.append("<input type=\"text\" size=\"4\" maxlength=\"4\" name=\"");
            html.append(DateParam.DATE_END_TEXT.name());
            html.append("\" id=\"");
            html.append(DateParam.DATE_END_TEXT.name());
            html.append("\" value=\"");
            html.append(endDisplayValue);        
            html.append("\"");
            html.append(">");
            html.append("</input>");
            html.append("<select name=\"");
            html.append(DateParam.DATE_END_ERA.name());
            html.append("\" id=\"");
            html.append(DateParam.DATE_END_ERA.name());
            html.append("\">");
            html.append(getEraOptions(this.getEra()));
            html.append("</select>");    
            html.append("</div><!-- closing .date-input-box -->");
            html.append("</div><!-- closing .facet-widget -->");

            return html.toString();         
                       
        }
        
        @Override
        public void orderFacetQueries(List<RangeFacet.Count> fqs){
            
            Collections.sort(fqs, facetCountComparator);
            
        }

        
        @Override
        Integer getMostExtremeValue(){
            
            if(!currentValue.equals("Unknown") && !currentValue.equals("")) return Integer.valueOf(currentValue);
            return DateFacet.RANGE_END;
            
        }
        
        @Override
        public ArrayList<Count> calculateLooseWidgetValues(long grandTotal) {
            
            ArrayList<Count> looseWidgetValues = new ArrayList<Count>();
            ArrayList<Count> complement = terminusAfterWhich.getValuesAndCounts();
            
            for(int i = 0; i < this.getValuesAndCounts().size(); i++){
                
                try{
                    
                    Count count = this.getValuesAndCounts().get(i);
                    String name = count.getName();
                    if(name.equals("Unknown")){
                        
                        looseWidgetValues.add(count);
                        
                    }
                    else{
                        
                        long looseCount = 0;
                        if(i > 0){
                            
                            looseCount = grandTotal - complement.get(i).getCount();
                        }
                        Count newCount = new Count(new FacetField(SolrField.latest_date.name()), name, looseCount);
                        looseWidgetValues.add(newCount);
                    
                    }
                }
                catch(NumberFormatException nfe){}
                catch(NullPointerException npe){}
           
            }
           Collections.sort(looseWidgetValues, dateCountComparator);
           return looseWidgetValues; 
                     
        }


        @Override
        String getDisplayName() {
            
            return "Date before";
            
        }

        @Override
        Terminus getOtherTerminus() {
            
            return terminusAfterWhich;
            
        }

        @Override
        void orderValuesAndCounts() {
            
            Collections.sort(valuesAndCounts, DateFacet.dateCountComparator);
            Collections.reverse(valuesAndCounts);
            
        }
        
        @Override
        void filterValuesAndCounts(){
            
            orderValuesAndCounts();
            ArrayList<Count> filteredCounts = new ArrayList<Count>();
            long previousCount = 0;
            int otherLimit = terminusAfterWhich.getMostExtremeValue();
            
            for(Count vc : valuesAndCounts){
                
                long nowCount = vc.getCount();
                String nowName = vc.getName();
                Boolean tooSmall = !nowName.equals("Unknown") && !nowName.equals("") && Integer.valueOf(nowName) <= otherLimit;
                if(((nowCount > 0 && nowCount != previousCount) && !tooSmall) || nowName.equals(currentValue)) filteredCounts.add(vc);
                previousCount = nowCount;
                
            }
            
            if(filteredCounts.size() > 1){

                if(filteredCounts.get(0).getCount() == filteredCounts.get(1).getCount()) filteredCounts.remove(0);
                
            }
            Collections.sort(filteredCounts, DateFacet.dateCountComparator);
            valuesAndCounts = filteredCounts;         
            
        } 
                
        
    }
    
}
