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
 * Note that unlike  other <code>Facet</code>s, <code>DateFacet</code> uses two
 * widgets, and stores two values. This necessitates the use of two <code>Terminus</code> 
 * objects to handle each widget and the relationships between them.
 * 
 * Note also that two modes of date calculation are supported.
 * (i) 'Strict' date selection, whereby the range of dates associated with each text (i.e.,
 * the terminus post quem and the terminus ante quem) must fall <em>within</em> the range 
 * specified by the user
 * (ii) 'Loose' date selection, whereby the range of dates associated with each text (the
 * t.p.q. and t.a.q) must <em>overlap</em> with the range specified by the user.
 * 
 * Mathematically, the relationship between Strict and Loose date selection is perhaps
 * counter-intuitive.
 * 
 * Strict date selection is relatively straightforward: counts are cumulative - so that, assuming
 * a total range between, say, 100 CE and 500 CE and a faceting interval of 100 years, 
 * the number of items with end dates prior to, 400 will consist of the total number with 
 * end dates falling in the facet range 100 - 200 plus the number with end dates in the range 200 - 300 
 * plus the number with end dates between 300 and 400. The number with start dates subsequent to 300 
 * will be the number with start dates between 400 and 500, plus those with start dates between 300 and 400.
 * 
 * Loose date selection involves, essentially, the complement of these values. When the
 * user in this mode uses the 'date before' control to select, say, all items with start 
 * <em>or</em> end dates before 400, (s)he's effectively asking for all items with start
 * dates before that period (because those with start dates afterwards will necessarily
 * have end dates after that point as well). The items to be returned, then, will consist
 * of the total body of texts less the number with start dates after the selected point.
 * 
 * The end result is that loose date queries can appear at first glance to be backwards,
 * both in terms of how items are selected and totals are calculated: selection involves
 * setting constraints on the complementary <code>Terminus</code> object, while count
 * calculation involves subtraction of the complementary <code>Terminus</code> objects
 * totals.
 * 
 * Also to be noted are several differences between backend and frontend date representation.
 * (i) On the backend, all dates are represented as integers, with negative values indicating
 * BCE dates and positive values CE dates. On the front end all dates are positive integers,
 * suffixed with 'BCE' or 'CE' string values
 * (ii) The backend uses 0 as a valid date value for faceting purposes; in display this 
 * is converted to 1 CE.
 * (iii) Items with unknown dates have this value represented as 'Unknown' in both the backend
 * and in the frontend drop-down date selector; in the text input it is 'n.a.'
 *
 * Note further the specific character of date-range specification: the start date is <em>
 * inclusive</em>, while the end date is <em>exclusive</em>
 * 
 * @author thill
 * @see info.papyri.dispatch.browse.facet.DateFacet.Terminus
 * @see info.papyri.dispatch.browse.facet.DateFacet.TerminusAfterWhich
 * @see info.papyri.dispatch.browse.facet.DateFacet.TerminusBeforeWhich
 */
public class DateFacet extends Facet {

    /** The year-interval to be used for display in the drop-down HTML controls and for faceting.
     *  (that is to say, the facet 'buckets' are 50-year spans
     */
   static int INTERVAL = 50;
   
   /** The earliest date to be used in faceting */
   static int RANGE_START = -2500;
   
   /** The latest date to be used in faceting */
   static int RANGE_END = 2500;
   
   /** A flag indicating whether the date has the value 'unknown'.
    * 
    * This value is special because
    * (a) it cannot be converted to a number; and 
    * (b) if *either* the terminus ante quem *or* the terminus post quem have the
    *     value 'unknown', then *both* must have the value 'unknown'. Note that this
    *     is not an arbitrary, but reflects the structure of data in the Solr
    *     index.
    * 
    */
   private static SolrField flagField = SolrField.unknown_date_flag;
   
   /** The names of the HTML form controls.
    * 
    *  DATE_MODE is used for the date mode (Strict/Loose) radio-button selector
    *  DATE_START_TEXT and DATE_END_TEXT are text inputs for designating the terminus post quem
    *  and terminus ante quem respectively
    *  DATE_START_ERA and DATE_END_ERA are the date era (BCE / CE) drop-down selectors associated
    *  with the DATE_START_TEXT and DATE_END_TEXT input controls
    * 
    *  Two other HTML controls - DATE_START and DATE_END - are used by the <code>DateFacet</code> -
    *  but for UI reasons simply ferry their values via JavaScript to the DATE_START_TEXT and 
    *  DATE_END_TEXT controls. They are thus essentially invisible to the servlet and do not
    *  have enum values of their own.
    * 
    *  @see TerminusAfterWhich#generateWidget() 
    *  @see TerminusBeforeWhich#generateWidget() 
    */
   
   enum DateParam{ DATE_MODE, DATE_START_TEXT, DATE_START_ERA, DATE_END_TEXT, DATE_END_ERA };
   /**
    * Comparator used in ordering dates in the <code>Facet</code> widget.
    */
   private static Comparator dateCountComparator;  
   
   /** Object representing a date <em>after which</em> sought items must have originated -
    *  that is to say, the <i>terminus post quem</i>.
    */
   Terminus terminusAfterWhich;
   /** Object representing a date <em>before which</em> sought items must have originated - 
    *  that is to say, the <i>terminus ante quem</i>.
    * 
    */
   Terminus terminusBeforeWhich;
   
   /**
    * Enum for the two possible modes of date calculation, strict and loose.
    * 
    */
   
   enum DateMode{ STRICT, LOOSE }
   
   /**
    *  The default date mode
    */
   DateMode dateModeDefault = DateMode.LOOSE;
   
   /** The date mode currently set */
   DateMode dateMode = dateModeDefault;
   
   public DateFacet(){
        
        super(SolrField.unknown_date_flag, FacetParam.DATE_START, "Date dummy");
        
        terminusBeforeWhich = new TerminusBeforeWhich("");
        terminusAfterWhich = new TerminusAfterWhich("");
        
        // dates need to sort with 'Unknown' at the top
        // followed by BCE dates (= negative date value)
        // followed by CE dates (= positive date value)
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
        // In 'loose' date mode it is possible in some cases to retrieve texts with the
        // end date set earlier than the start date. Although this is coherent in terms of backend
        // logic, it may not appear to be so to the end user. This conditional block accordingly
        // ensures that such queries return to results to the end user.
        if(endDate < startDate){
            
            solrQuery.addFilterQuery("-" + SolrField.earliest_date.name() + ":[* TO *]");
            solrQuery.addFilterQuery("-" + SolrField.unknown_date_flag.name() + ":true");
            
        }
        Integer startFacet = Integer.valueOf(terminusAfterWhich.getFacetBucket());
        Integer endFacet = Integer.valueOf(terminusBeforeWhich.getFacetBucket());
        solrQuery.addNumericRangeFacet(SolrField.earliest_date.name(), startFacet, endFacet, INTERVAL);
        solrQuery.addNumericRangeFacet(SolrField.latest_date.name(), startFacet, endFacet, INTERVAL);
        terminusBeforeWhich.buildQueryContribution(solrQuery);
        terminusAfterWhich.buildQueryContribution(solrQuery);
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
        // convert zero value to 1 CE
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
        // when calculating loose-date totals, we are interested in the total number
        // of items <em>associated with dates</em> - that is, the total number of 
        // items returned, less those with an unknown date value
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
    /**
     * Because date is specified as a range, each of the two widgets can have only one value,
     * and hidden fields are not generated.
     * 
     * This method accordingly returns an empty string.
     * 
     * @return 
     */
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
                
                if(selectedMode != null) dateMode = selectedMode;
                                
                
            }
            catch(IllegalArgumentException iae){ }
                 
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
    
    /**
     * <code>Terminus</code> objects handle all frontend and backend processing involved
     * with setting one end of a date range.
     * 
     * The <code>Terminus</code> class accordingly has two subclasses - <code>TerminusAfterWhich</code>,
     * for setting the start of the range, and <code>TerminusBeforeWhich</code> for setting its end.   
     * 
     * Note that many <code>Terminus</code> methods are simply delegated versions of <code>Facet</code>
     * methods of the same signature.
     * 
     */
    
    abstract class Terminus{
        
        /** A list of date ranges, specified by either the start or end of the range, and associated
         *  count of all items that fall into that range
         * 
         * @see Facet#valuesAndCounts
         */
        
        ArrayList<Count> valuesAndCounts;
        
        /** The current value of the <code>Terminus</code>
         * 
         * This will normally be a string representation of an integer, but may also be "Unknown",
         * or the empty string if not yet defined.
         * 
         */
        String currentValue;
        
        /**
         * The <code>SolrField</code> from which the <code>Terminus</code> draws its values.
         * 
         */
        private SolrField facetField;
        
        /**
         * The <code>Comparator</code> used to sort the faceting values returned from 
         * the server
         */
        Comparator facetCountComparator;
        
        /**
         * Constructor
         * 
         * @param value The value of the <code>Terminus</code>
         * @param f The Solr field the <code>Terminus</code> uses for faceting
         */
        
        public Terminus(String value, SolrField f){
            
            currentValue = value;
            valuesAndCounts = new ArrayList<Count>();
            facetField = f;
            /** Sorting should be from lowest to highest, but needs to take account
             *  of the possibility of string values being submitted.
             */
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
        
        /**
         * @see Facet#buildQueryContribution(org.apache.solr.client.solrj.SolrQuery) 
         */
        
        abstract SolrQuery buildQueryContribution(SolrQuery solrQuery);
        
        /**
         * @see Facet#getAsQueryString() 
         */
                      
        abstract String getAsQueryString();
        
        /**
         * Returns the label to be associated with the <code>Terminus</code>
         */
        abstract String getDisplayName();
        
        /**
         * @see Facet#getDisplayValue(java.lang.String) 
         */
        
        abstract String getDisplayValue(String dateCode);
        
        /**
         * @see Facet#generateWidget() 
         */
        
        abstract String generateWidget();
        
        /**
         * Sorts the facet values returned from the server using the facetCountComparator.
         * 
         * The point of this is to allow the <code>Terminus</code> subclasses to churn through
         * the returned values in order and add up the totals cumulatively. <code>TerminusAfterWhich</code>
         * accordingly reverses the normal order in order to proceed through them backwards (because
         * the number of items will increase the further back in time one sets the 'date after which'
         * 
         * @param fqs The relevant <code>List</code> of <code>RangeFacet.Count</code>s returned by the server
         */
        abstract void orderFacetQueries(List<RangeFacet.Count> fqs);
        
        
        /** Sorts the valuesAndCounts
         * 
         * As with the orderFacetQueries method, the two <code>Terminus</code> subclasses need to
         * do this in reverse order from each other.
         * 
         * @see Terminus#valuesAndCounts
         */
        abstract void orderValuesAndCounts();
        
        
        /** Returns the facet range into which an odd (that is, != any of the facet range intervals)
         * date falls
         */
        abstract Integer getFacetBucket();
        
        /**
         * Eliminates redundant facet ranges.
         * 
         * 'Redundant' facet ranges are those which:
         * (i)   do not contain any items
         * (ii)  contain the same number of items as the preceding facet range, in the case of the 
         * the <code>TerminusAfterWhich</code> (because, for example, any date that is after 200 CE
         * is necessarily also after 100 CE)
         * (iii)  contain the same number of items as the following facet range, in the case of the
         * <code>TerminusBeforeWhich</code> (because, for example, any date that falls before 100 CE
         * also necessarily falls before 200 CE).
         * 
         */
        void filterValuesAndCounts(){
            
            orderValuesAndCounts();
            ArrayList<Count> filteredCounts = new ArrayList<Count>();
            long previousCount = 0;
            
            for(Count vc : valuesAndCounts){
                
                long nowCount = vc.getCount();
                String nowName = vc.getName();
                // we want to include the currently selected value
                if((nowCount > 0 && nowCount != previousCount) || nowName.equals(currentValue)) filteredCounts.add(vc);
                previousCount = nowCount;
                
            }
            // the above loop fails to filter out the first value in the sortd valuesAndCounts list when required
            // so another conditional block is required
            if(filteredCounts.size() > 1){

                if(filteredCounts.get(0).getCount() == filteredCounts.get(1).getCount()) filteredCounts.remove(0);
                
            }
            Collections.sort(filteredCounts, DateFacet.dateCountComparator);
            valuesAndCounts = filteredCounts;         
            
        }       
        
        /**
         * Calculates the values to be displayed in the widget.
         * 
         * This functionality is typically handled in other <code>Facet</code>s by the setWidgetValues
         * method. Date values, however, are more complex,and the widget values thus require significantly
         * more post-processing. The chief function of this method, then, is chiefly to identify the relevant
         * part of the Solr query response and forward this on to the relevant post-processing methods.
         * 
         * @see Facet#setWidgetValues(org.apache.solr.client.solrj.response.QueryResponse) 
         * @see Terminus#calculateStrictWidgetValues(java.util.List) 
         * @see Terminus#calculateLooseWidgetValues(long) 
         * 
         */
        
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
        
        /**
         * Takes a list of <code>Count</code>s and converts them into a <code>HashMap</code>, 
         * with the name of the <code>Count</code> object (here, converted to an integer) 
         * as the key and the associated number of items as the value.
         */
              
        HashMap<Integer, Long> mapRangeFacets(List<RangeFacet.Count> counts){
            
            HashMap<Integer, Long> rangeMap = new HashMap<Integer, Long>();
            
            for(RangeFacet.Count count : counts){
                
                Integer name = Integer.valueOf(count.getValue());
                long number = count.getCount();
                rangeMap.put(name, number);  
                
            }
                       
            return rangeMap;
            
        }
        
        /**
         * Checks to see whether any items of unknown date are included in the <code>QueryResponse</code>,
         * and creates a <code>Count</code> object to represent these if so.
         * 
         * @param queryResponse 
         */
        
        public void addUnknownCount(QueryResponse queryResponse){
        
            long unknownCountNumber = getUnknownCount(queryResponse);
            if(unknownCountNumber > 0){
                
                Count unknownCount = new Count(new FacetField(flagField.name()), "Unknown", unknownCountNumber);
                valuesAndCounts.add(unknownCount);
            }
        
        }
        
        /**
         * Returns the number of items of unknown date returned in the <code>QueryResponse</code>.
         * 
         * @param queryResponse
         * @return The number of items of unknown date
         */
        
        private long getUnknownCount(QueryResponse queryResponse){

            List<Count> counts = queryResponse.getFacetField(flagField.name()).getValues();
            Iterator<Count> cit = counts.iterator();
            while(cit.hasNext()){

                Count count = cit.next();
                if(count.getName()!= null && count.getName().equals("true")) return count.getCount();

            }

            return 0;

        }        
        
        
        /**
         * Returns the <code>Terminus</code> other than <code>this</code> - that is, if the callee is 
         * the <code>terminusBeforeWhich</code>, then the <code>terminusAfterWhich</code> is returned,and
         * vice versa.
         * 
         * @return The other <code>Terminus</code> 
         */
        abstract Terminus getOtherTerminus();
        
        /** 
         * Adds the current value to the passed array
         * 
         * This is required because objects querying the <code>DateFacet<code> for its constraints will require
         * <em>both</em> the <code>TerminusBeforeWhich</code> and <code>TerminusAfterWhich</code> values.
         * 
         * @param constraints 
         * @see DateFacet#getFacetConstraints(java.lang.String) 
         */
        
        void addCurrentValue(ArrayList<String> constraints){ if(!currentValue.equals("")) constraints.add(currentValue); }
        
        void setCurrentValue(String newValue){ currentValue = newValue; }
                 
        String getCurrentValue(){ return currentValue; }
        
        SolrField getFacetField(){ return this.facetField;  }
        
        /**
         * Returns the end of the range for which the <code>Terminus</code> is responsible. This will
         * be either the current value of the <code>Terminus</code>, if set, or one of the DateFacet.RANGE_START
         * or DateFacet.RANGE_END values, depending on which one is relevant (RANGE_START for the <code>TerminusAfterWhich</code>,
         * RANGE_END for the <code>TerminusBeforeWhich</code>).
         * 
         * @return 
         */
        
        abstract Integer getMostExtremeValue();
        
        /**
         * Retrieves a <code>Count</code> object of the passed name from a <code>List</code> and returns it.
         * 
         * @param desiredValue
         * @param countList
         * @return 
         */
        
        Count pluckCountFromList(String desiredValue, List<Count> countList){
            
            for(Count count : countList){
                
                String value = count.getName();
                if(value.equals(desiredValue)) return count;
                
            }
            
            return null;
            
        }
        
        /**
         * Returns the aggregate total count of all the <code>Count</code> objects in
         * the passed <code>List</code> of <code>Count</code>s.
         * 
         * @param facetResponse
         * @return 
         */
        
        long getGrandTotal(List<Count> facetResponse){
            
            long grandTotal = 0;
            
            for(Count count : facetResponse){

                if(count.getName() != null && !count.getName().equals("")){
                
                      grandTotal += count.getCount();  
                    
                }
      
            }
            
            return grandTotal;
            
        }
        
        /**
         * Removes the <code>Count</code> object with the passed value from the
         * valuesAndCounts <code>ArrayList</code>
         *  
         * @param facetBucket
         * @return 
         */
        
        Long filterValueFromValuesAndCounts(Integer facetBucket){
            
            Count soughtCount = this.pluckCountFromList(String.valueOf(facetBucket), valuesAndCounts);
            long soughtNumber = soughtCount.getCount();
            this.getValuesAndCounts().remove(soughtCount);
            return soughtNumber;
        }
        
        /**
         * Calculates the values to be displayed in the relevant HTML control widget when in 'Strict' mode.
         * 
         * These values will consist essentially of a list of all the facet intervals
         * (i)  associated with a cumulative count of items with start/end dates coming after/before
         * that interval date respectively
         * (ii) filtered for duplicate and zero values
         * 
         * @param facetQueries 
         */
        
        abstract void calculateStrictWidgetValues(List<RangeFacet.Count> facetQueries);
        
        /**
         * Calculates the values to be displayed in the relevant HTML control widget when in 'Loose' mode.
         * 
         * These  values will consist essentially of a list of all the facet intervals
         * (i) associated with a cumulate count of items with either start or end dates coming after/before
         * the interval date
         * (ii) filterd for duplicate and zero values
         * 
         * @param grandTotal
         * @return 
         */
         
        abstract public ArrayList<Count> calculateLooseWidgetValues(long grandTotal);
          
        /**
         * Returns 'BCE' if the current value of the <code>Terminus</code> is negative, or 'CE' otherwise.
         * 
         * @return 
         */
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
                
                return "CE";
                
                
            }
            
            return "CE";
            
        }
        
        public ArrayList<Count> getValuesAndCounts(){ return this.valuesAndCounts; }
        
        public void setValuesAndCounts(ArrayList<Count> newCounts){ this.valuesAndCounts = newCounts; }
        
 
    }
    
    class TerminusAfterWhich extends Terminus{
                
        public TerminusAfterWhich(String value){
            
            super(value, SolrField.earliest_date);
    
        }

        @Override
        SolrQuery buildQueryContribution(SolrQuery solrQuery) {
            
            if(!currentValue.equals("")){
                 
                String fq = "";
                if(dateMode == DateMode.STRICT){
                    // end date is *exclusive*
                    Integer endDate = terminusBeforeWhich.getMostExtremeValue() - 1;
                    fq = this.getFacetField().name() + ":[" + String.valueOf(currentValue) + " TO " + String.valueOf(endDate) + "]";
                    
                }
                else{
                    
                    fq =  terminusBeforeWhich.getFacetField().name() + ":[" + String.valueOf(currentValue) + " TO " + DateFacet.RANGE_END + "]";
                      
                }
                solrQuery.addFilterQuery(fq);
            }
            
            return solrQuery;
            
        }

        @Override
        String getAsQueryString() {
            
            if(!currentValue.equals("")){
                
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
                        
            Boolean startIsUnknown = currentValue.equals("Unknown") || terminusBeforeWhich.getCurrentValue().equals("Unknown");
            String startDisplayValue = startIsUnknown ? "n.a." : currentValue.replaceAll("^-", "");  
            startDisplayValue = startDisplayValue.equals("0") ? "1" : startDisplayValue;
            
            StringBuilder html = new StringBuilder("<div class=\"facet-widget date-facet-widget\" title=\"");
            html.append(getAfterWhichToolTipText());
            html.append("\">");
            Boolean onlyOneValue = valuesAndCounts.size() == 1;
            String defaultSelected = onlyOneValue ? "" : "selected=\"true\"";
            String disabled = onlyOneValue ? " disabled=\"true\"" : "";
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
            // dealing with cases that do not fall exactly on a pre-defined faceting date interval
            if(!currentValue.equals("") && Integer.valueOf(currentValue) % DateFacet.INTERVAL != 0){
                
                int facetBucket = this.getFacetBucket();
                Long count = filterValueFromValuesAndCounts(facetBucket);
                Count newCount = new Count(new FacetField(getFacetField().name()), currentValue, count);
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
        Integer getFacetBucket(){
            
            try{
                
                Integer currVal = Integer.valueOf(currentValue);
                int remainder = Math.abs(currVal % DateFacet.INTERVAL);
                if(currVal < 0) remainder = DateFacet.INTERVAL - remainder;
                return currVal - remainder;
                
            }
            catch(NumberFormatException nfe){
                
                return DateFacet.RANGE_START;
                
            }
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
                
        public TerminusBeforeWhich(String value){
            
            super(value, SolrField.latest_date);
            
        }

        @Override
        SolrQuery buildQueryContribution(SolrQuery solrQuery) {
            
            if(!currentValue.equals("")){
                
                String fq = "";
                Integer endDate = Integer.valueOf(currentValue) - 1;
                
                if(dateMode == DateMode.STRICT){
                
                    Integer startDate = terminusAfterWhich.getMostExtremeValue();
                    fq = this.getFacetField().name() + ":[" + String.valueOf(startDate) + " TO " + String.valueOf(endDate) + "]";
                    solrQuery.addFilterQuery(fq);
                
                }
                else{
                    
                    fq = terminusAfterWhich.getFacetField().name() + ":[" + DateFacet.RANGE_START + " TO " + String.valueOf(endDate) + "]";
                    
                }
               
                solrQuery.addFilterQuery(fq);
            }
            
            return solrQuery;
            
        }
        
        @Override
        void calculateStrictWidgetValues(List<RangeFacet.Count> facetQueries) {
               
            long runningTotal = 0;
            HashMap<Integer, Long> responseAsMap = mapRangeFacets(facetQueries);
            
            for(int i = DateFacet.RANGE_START; i <= DateFacet.RANGE_END; i += DateFacet.INTERVAL){
                
                String name = String.valueOf(i);
                // note the decrement here - because facet ranges are defined *forward* (e.g.
                // '300' refers to the span '300 - 350', the TerminusBeforeWhich needs to shunt
                // everything one interval back to be accurate
                int retrievalNumber = i - DateFacet.INTERVAL;
                long number = responseAsMap.containsKey(retrievalNumber) ? responseAsMap.get(retrievalNumber) : 0;
                runningTotal += number;
                Count newCount = new Count(new FacetField(getFacetField().name()), name, runningTotal);
                valuesAndCounts.add(newCount);
                   
            }
            if(!currentValue.equals("") && Integer.valueOf(currentValue) % DateFacet.INTERVAL != 0){
                
                Integer facetBucket = this.getFacetBucket();
                Long count = filterValueFromValuesAndCounts(facetBucket);
                Count newCount = new Count(new FacetField(getFacetField().name()), currentValue, count);
                valuesAndCounts.add(newCount);
                
            }
            
            Collections.sort(valuesAndCounts, dateCountComparator);

        }        
        
        @Override
        String getAsQueryString() {
            
            if(!currentValue.equals("")){
                
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
            
            Boolean endIsUnknown = currentValue.equals("Unknown") || terminusAfterWhich.getCurrentValue().equals("Unknown");
            String endDisplayValue = endIsUnknown? "n.a." : currentValue.replaceAll("^-", "");           
            endDisplayValue = endDisplayValue.equals("0") ? "1" : endDisplayValue;
            
            StringBuilder html = new StringBuilder("<div class=\"facet-widget date-facet-widget\" title=\"");
            html.append(getBeforeWhichToolTipText());
            html.append("\">");
            Boolean onlyOneValue = valuesAndCounts.size() == 1;
            String defaultSelected = onlyOneValue  ? " selected=\"true\"" : "";
            String disabled = onlyOneValue ? " disabled=\"true\"" : "";
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
        Integer getFacetBucket(){
            
            try{
                
                Integer currVal = Integer.valueOf(currentValue);
                int remainder = Math.abs(currVal % DateFacet.INTERVAL);
                if(currVal > 0) remainder = DateFacet.INTERVAL - remainder;
                return currVal + remainder;
                
                
            } catch(NumberFormatException nfe){
                
                return DateFacet.RANGE_END;
                
            }
            
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
