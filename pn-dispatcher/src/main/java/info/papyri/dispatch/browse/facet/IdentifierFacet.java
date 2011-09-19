package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.IdComparator;
import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
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

/**
 * <code>Facet</code> for selecting texts based on DDbDP/HGV series, APIS collection,
 * volume, and id number.
 * 
 * Note the following ruies for navigation in this fashion:
 * (1) Navigation may be performed EITHER through APIS collection OR DDbDP/HGV
 * series - the two are mutually exclusive
 * (2) DDbDP/HGV series <em>may</em> have volume numbers; APIS collections never do
 * (3) DDbDP identifiers for individual items are defined in the ddbdp_full_identifier field
 * (4) HGV identifiers for individual items are defined in the hgv_full_identifier field
 * (5) APIS identifiers for individual items are defined in apis_full_identifier, apis_
 * publication_id, and apis_inventory_number fields
 * 
 * Note also that most of the heavy lifting for this class is performed in the inner 
 * <code>SearchConfiguration</code> classes defined at the end of the file.
 * 
 * @author thill
 * @see Facet
 * @see SearchConfiguration
 */
public class IdentifierFacet extends Facet{

    /**
     * <code>Enum</code> giving the different elements in the classification hierarchy.
     * 
     * The <code>enum</code> serves a variety of functions. Most significantly, its values
     * serve as the keys to the <code>searchConfigurations</code> <code>EnumMap</code>. Its
     * name attributes are also used to supply the @name values of the relevant HTML form 
     * controls for this <code>Facet</code>, and its <code>label</code> property supplies
     * the control labels displayed to end users.
     * 
     */
    
    public enum IdParam{   
        
                    SERIES("Series"),
                    COLLECTION("Collection"), 
                    VOLUME("Vol."), 
                    IDNO("ID # ");
    
        private final String label;
        
        IdParam(String msg){
            
            label = msg;
            
        }
        
        public String getLabel(){
            
            return label;
            
        }
    
    }  
    
    /**
     * The basic data structure of the class.
     * 
     * Its keys are <code>IdParam</code> values; its values are <code>SearchConfiguration</code>
     * objects, defined in an inner class detailed below.
     * 
     */
    
    private EnumMap<IdParam, SearchConfiguration> searchConfigurations;
    
    ArrayList<IdParam> idnoLedOrder;
    ArrayList<IdParam> volumeLedOrder;
    ArrayList<IdParam> seriesLedOrder;
    EnumMap<SolrField, ArrayList<IdParam>> orders;
    
       
    /**
     * Constructor.
     * 
     * Note that the <code>searchConfigurations</code> <code>EnumMap</code> is initialized here.
     * 
     */
    
    public IdentifierFacet(){
        
        super(SolrField.ddbdp_series, FacetParam.IDENTIFIER, "Get documents by id");     
        searchConfigurations = new EnumMap<IdParam, SearchConfiguration>(IdParam.class);
        searchConfigurations.put(IdParam.SERIES, new SeriesSearchConfiguration());
        searchConfigurations.put(IdParam.COLLECTION, new CollectionSearchConfiguration());
        searchConfigurations.put(IdParam.VOLUME, new VolumeSearchConfiguration());
        searchConfigurations.put(IdParam.IDNO, new IdnoSearchConfiguration());
        
        idnoLedOrder = new ArrayList<IdParam>(Arrays.asList(IdParam.IDNO, IdParam.SERIES, IdParam.VOLUME));
        volumeLedOrder = new ArrayList<IdParam>(Arrays.asList(IdParam.VOLUME, IdParam.IDNO, IdParam.SERIES));
        seriesLedOrder = new ArrayList<IdParam>(Arrays.asList(IdParam.SERIES, IdParam.VOLUME, IdParam.IDNO)); 
        
        orders = new EnumMap<SolrField, ArrayList<IdParam>>(SolrField.class);
        
        orders.put(SolrField.idno_led_path, idnoLedOrder);
        orders.put(SolrField.volume_led_path, volumeLedOrder);
        orders.put(SolrField.series_led_path, seriesLedOrder);
        
    }    
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        Boolean noConstraints = true;
        
        for(SearchConfiguration sc : searchConfigurations.values()){
            
            if(sc.hasConstraint()) noConstraints = false;
            ArrayList<SolrField> fields = sc.getFacetFields();
            for(SolrField ipField : fields){ 
                
                solrQuery.addFacetField(ipField.name());
            
            }
            
        }
        
        if(noConstraints) return solrQuery;
        if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()){
            
            solrQuery.addFilterQuery(searchConfigurations.get(IdParam.COLLECTION).buildQueryClause(""));
            solrQuery.addFilterQuery(searchConfigurations.get(IdParam.IDNO).buildQueryClause(""));
            return solrQuery;
            
        }
        
        String specifierClause = getCompleteSpecifierClause();
            
        String filterQuery = this.getLeadingField().name() + ":" + specifierClause;
        solrQuery.addFilterQuery(filterQuery);
        return solrQuery; 
        
    }
    
    private SolrField getLeadingField(){
        
        if(searchConfigurations.get(IdParam.IDNO).hasConstraint()) return SolrField.idno_led_path;
        if(searchConfigurations.get(IdParam.SERIES).hasConstraint()) return SolrField.series_led_path;
        if(searchConfigurations.get(IdParam.VOLUME).hasConstraint()) return SolrField.volume_led_path;
        
        return SolrField.series_led_path;
        
    }
    
    private String getRawSpecifierClause(SolrField ledPath){
        
        String specifierClause = "";
        
        ArrayList<IdParam> order = orders.get(ledPath);
        for(IdParam ip : order){
            
            specifierClause += ip.name();
            specifierClause += ";";
            
        }
        specifierClause += "*";
        return specifierClause;
        
    }
    
    private String getCompleteSpecifierClause(){
        
        SolrField leadField = this.getLeadingField();

        String specifierClause = this.getRawSpecifierClause(leadField);

        specifierClause = searchConfigurations.get(IdParam.SERIES).buildQueryClause(specifierClause);
        specifierClause = searchConfigurations.get(IdParam.VOLUME).buildQueryClause(specifierClause);
        specifierClause = searchConfigurations.get(IdParam.IDNO).buildQueryClause(specifierClause);
        
        return specifierClause;
        
    }
    
    public String getSpecifierClauseAsJavaRegex(){
        
        String specifierClause = getCompleteSpecifierClause();
        System.out.println("Specifier clause is " + specifierClause);
        specifierClause = specifierClause.replace("*", ".+?");
        specifierClause = "^" + specifierClause + "$";
        System.out.println("Replaced clause is " + specifierClause);
        return specifierClause;
             
    }
    
    @Override
    public String generateWidget(){
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" id=\"id-selector-wrapper\" title=\"");
        html.append(getToolTipText());
        html.append("\">");
        html.append(generateHiddenFields());
        html.append("<div id=\"series-coll-wrapper\">");       
        html.append(searchConfigurations.get(IdParam.SERIES).buildHTMLControl());
        html.append("<p class=\"coll-or\">or</p>");
        html.append(searchConfigurations.get(IdParam.COLLECTION).buildHTMLControl());
        html.append("</div><!-- closing #series-coll-wrapper -->");
        html.append("<div id=\"vol-idno-wrapper\">");
        html.append(searchConfigurations.get(IdParam.VOLUME).buildHTMLControl());
        html.append(searchConfigurations.get(IdParam.IDNO).buildHTMLControl());
        html.append("</div><!-- closing #vol-idno-number -->");
        html.append("</div><!-- closing #id-selector-wrapper -->");     
        return html.toString();
        
    }
    
    @Override
    public void setWidgetValues(QueryResponse queryResponse){

        Boolean hasPathData = dispersePathData(queryResponse);
        
        if(!hasPathData){
            
            for(IdParam ip : IdParam.values()){
                
                searchConfigurations.get(ip).setIdValues(queryResponse, new ArrayList<String>());              
                
            }
            
            
        }
                
        
    }
    
    private Boolean dispersePathData(QueryResponse queryResponse){
        
        SolrField searchField = this.getLeadingField();
        
        ArrayList<IdParam> fieldOrder = orders.get(searchField);

        ArrayList<String> component0 = new ArrayList<String>();
        ArrayList<String> component1 = new ArrayList<String>();
        ArrayList<String> component2 = new ArrayList<String>();
        ArrayList<String> collections = new ArrayList<String>();
        ArrayList<ArrayList<String>> components = new ArrayList<ArrayList<String>>(Arrays.asList(component0, component1, component2, collections));
        
        FacetField facetField = queryResponse.getFacetField(searchField.name());
        
        if(facetField == null) return false; 

        Pattern pattern = Pattern.compile(this.getSpecifierClauseAsJavaRegex());

        List<Count> counts = facetField.getValues();

        for(Count count : counts){

            String name = count.getName();
            Long number = count.getCount();

             if(name != null && !"".equals(name) && !"0".equals(name) && !"null".equals(name)  && number != 0){


                Matcher matcher = pattern.matcher(name);

                if(matcher.matches()){

                    String[] pathComponents = name.split(";");
                    component0.add(pathComponents[0]);
                    component1.add(pathComponents[1]);
                    component2.add(pathComponents[2]);
                    collections.add(pathComponents[3]);                   

                }    

             }

        }
             
        int seriesIndex = fieldOrder.indexOf(IdParam.SERIES);

        ArrayList<String> series = components.get(seriesIndex);
        
        for(int i = 0; i < series.size(); i++){
            
            String seriesName = series.get(i);
            seriesName = collections.get(i) + ";" + seriesName;
            series.set(i, seriesName);
            
        }
        
        for(int j = 0; j < fieldOrder.size(); j++){
            
            SearchConfiguration sc = searchConfigurations.get(fieldOrder.get(j));
            sc.setIdValues(queryResponse, components.get(j));
            
        } 
        
        return true;
             
    }
    
    @Override
    public ArrayList<String> getFacetConstraints(String facetParam){
                
        for(IdParam ip : IdParam.values()){
        
            if(ip.name().equals(facetParam)){
                
                return new ArrayList<String>(Arrays.asList(searchConfigurations.get(ip).getConstraint()));
                     
            } 
            
        }
        
        return new ArrayList<String>();
    }
    
    @Override
    String generateHiddenFields(){ 
    
        StringBuilder html = new StringBuilder();
        
        for(IdParam ip : IdParam.values()){
            
            if(searchConfigurations.get(ip).hasConstraint()){
                
                String param = ip.name();
                String val = searchConfigurations.get(ip).getConstraint();
                html.append("<input type=\"hidden\" name=\"");
                html.append(param);
                html.append("\" value=\"");
                html.append(val);
                html.append("\"/>");
                
            }
            
        }
    
        return html.toString();
    
    }
    
    @Override
    public Boolean addConstraints(Map<String, String[]> params){
        
        Boolean hasConstraint = false;
        
        for(IdParam ip : IdParam.values()){
            
            searchConfigurations.get(ip).setConstraint("");
            
            if(params.containsKey(ip.name())){
                
               ArrayList<String> values = new ArrayList<String>(Arrays.asList(params.get(ip.name())));
               if(!values.isEmpty()){
                  
                   searchConfigurations.get(ip).setConstraint(values.get(0));
               
               }
               hasConstraint = true;
                
            }
            
            
        }
        
        return hasConstraint;
        
    }
    
    @Override
    public String getAsQueryString(){
        
        String queryString = "";
        
        for(int i = 0; i < IdParam.values().length; i++){
            
            IdParam ip = IdParam.values()[i];
            
            String paramName = ip.name();
            
            String constraint = searchConfigurations.get(ip).getConstraint();
            
            if(!"".equals(constraint)){
                
                queryString += "&";
                queryString += paramName;
                queryString += "=";
                queryString += constraint;
                
            }

            
        }
        
        if(!"".equals(queryString)) queryString = queryString.substring(1);
        return queryString;
        
    }
    
    @Override
    public String getAsFilteredQueryString(String filterParam, String filterValue){
        
        String queryString = "";
        
        for(IdParam ip : IdParam.values()){
        
            SearchConfiguration sc = searchConfigurations.get(ip);
                        
            if(!filterParam.equals(ip.name()) && sc.hasConstraint()){
                
                    queryString += "&";
                    queryString += ip.name() + "=";
                    queryString += sc.getConstraint();
                
            }
            
        }
        
        if(!"".equals(queryString)) queryString = queryString.substring(1);
        return queryString;
        
    }
    
    @Override
    public String getDisplayName(String facetParam, String facetValue){
        
        if(!facetParam.equals(IdParam.SERIES.name())) return facetParam.toUpperCase();
        
        String collection = "";
        SeriesSearchConfiguration ssc = (SeriesSearchConfiguration) searchConfigurations.get(IdParam.SERIES);
        HashMap<String, Long> csf = ssc.getIdValues();
        Collection<String> keys = csf.keySet();
        
        for(String key : keys){
        
            if(key.contains(facetValue)){
                
                String[] keyBits = key.split(";");
                collection = keyBits[0];
                break;
            }
        
        }

        collection = collection.toUpperCase() + " ";
        return collection + IdParam.SERIES.name().toUpperCase(); 
        
    }
    
    @Override
    public String getDisplayValue(String value){
        
        return value.replaceAll("_", " ");
        
    }
    
    @Override
    public String[] getFormNames(){
        
        String[] formNames = new String[IdParam.values().length];
        
        for(int i = 0; i < IdParam.values().length; i++){
            
            IdParam ip = IdParam.values()[i];
            formNames[i] = ip.name();     
            
        }
        
        return formNames;
        
    }
    
    @Override
    String getToolTipText(){
        
        return "Locate documents by publication or collection information";
        
    }
    
    
    
    
    /**************** PRIVATE CLASSES HERE ************************************/
    
    
    
    /**
     * Defines the logic and behavior of individual levels of the classification hierarchy
     * 
     * These classes are in themselves simple, and their members names and purpose should in 
     * most cases be self-evident. In most cases the methods are simply devolved versions of
     * methods normally more amply defined in the <code>Facet</code> class and its subclasses.
     * The difficult aspects relate chiefly to interactions among these 
     * various controls, and notes are made of this where appropriate.
     * 
     * 
     */
    
    private abstract class SearchConfiguration{
        
        
        String constraint;
        HashMap<String, Long> idValues;
        IdParam param;
        
        public SearchConfiguration(IdParam ip){
            
            constraint = "";
            idValues = new HashMap<String, Long>();
            param = ip;
                   
        }
        
        public void setConstraint(String newConstraint){
            
            constraint = newConstraint;
            
        }
        
        public String getConstraint(){
            
            return constraint;
            
        }
        
        public Boolean hasConstraint(){
            
            return !"".equals(constraint);
            
        }
        
        public HashMap<String, Long> getIdValues(){ 
        
               return idValues;
            
        }
        
        public Boolean anyConstraintSet(){
        
            for(SearchConfiguration sc : searchConfigurations.values()){
            
                if(sc.hasConstraint()) return true;
            
            
            }
        
            return false;
        
        }
        
        public void setIdValues(QueryResponse queryResponse, ArrayList<String> rawValues){
            
            ArrayList<SolrField> facetFields = this.getFacetFields();
            
            for(SolrField facetField : facetFields){
            
                FacetField ff = queryResponse.getFacetField(facetField.name());
                
                if(ff != null){
                    
                    List<Count> facetCounts = ff.getValues();
            
                    for(Count count : facetCounts){

                        String name = count.getName();
                        Long number = count.getCount();

                        if(name != null && !"".equals(name) && !"0".equals(name) && !"null".equals(name)  && number != 0){
                 
                            if(idValues.containsKey(name)){
                                
                                Long currentValue = idValues.get(name);
                                currentValue += number;
                                idValues.put(name, currentValue);
                                
                            }
                            else{
                                
                                idValues.put(name, number);
                                
                            }
                 
                        }   // closing extended null check
              
                    }       // closing loop through counts
                
                }           // closing ff null check          
            
            }               // closing loop through facet fiels
            
            
            for(String name : rawValues){
                
                if(idValues.containsKey(name)){
                    
                    Long currentValue = idValues.get(name);
                    currentValue++;
                    idValues.put(name, currentValue);   
                    
                }
                
            }
            
        }

        public String getCSSSelector(){
            
            return "id-" + param.name().toLowerCase();
            
            
        }
        public abstract ArrayList<SolrField> getFacetFields();
        public abstract ArrayList<String> getIdValuesAsHTML();
        public abstract String buildQueryClause(String rawClause);
        public abstract String buildHTMLControl();
        public abstract Boolean isDisabled();
    
    }
    
    private abstract class CollectionSeriesSearchConfiguration extends SearchConfiguration {
    
        public CollectionSeriesSearchConfiguration(IdParam ip){
            
            super(ip);
            
        }
      
        @Override
        public String buildHTMLControl(){
          
            String lbo = "<label for=\"";
            String lbe = "</label>";
            String sls = "<select name=\"";
            String slm = "\" id=\"";
            String cl = "\">";
            String sle = "</select>";
            String dis = " disabled";         
            
            StringBuilder html = new StringBuilder();
            
            // paragraph wrapper
            html.append("<p id=\"");
            html.append(this.param.name().toLowerCase());
            html.append("-wrapper\">");
            
            // label
            html.append(lbo);
            html.append(this.param.name());
            html.append(cl);
            html.append(this.param.getLabel());
            html.append(lbe);
            
            // select open
            html.append(sls);
            html.append(this.param.name());
            html.append(slm);
            html.append(this.getCSSSelector());
            html.append("\"");
            if(this.isDisabled()) html.append(" disabled");
            html.append(">");
            
            // add options
            ArrayList<String> options = this.getIdValuesAsHTML();
            
            for(String option : options) html.append(option);
            
            // select close
            html.append("</select>");
          
            html.append("</p>");
            return html.toString();
            
            
        }

    }
    
    
    /*********************** SERIESSEARCHCONFIGURATION ************************/
    
    
    private class SeriesSearchConfiguration extends CollectionSeriesSearchConfiguration{
        
        public SeriesSearchConfiguration(){
            
            super(IdParam.SERIES);
            
            
        }
        
        @Override
        public String getCSSSelector(){
            
            return "id-" + IdParam.SERIES.name().toLowerCase();

        }
        /**
         * 
         * We do not want faceted for this facet if:
         * (i) the COLLECTION facet has a constraint (in which case this facet is disabled
         * (ii) it itself has a constraint (in which case this is the only value it can have)
         * (iii) VOLUME or IDNO have a constraint set, in which case the *_led_path fields must be used
         * instead of the standard faceting mechanism
         * 
         * @return 
         */
        @Override
        public ArrayList<SolrField> getFacetFields(){
            
            ArrayList<SolrField> facetFields = new ArrayList<SolrField>();
            
            if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()) return facetFields;
            if(!anyConstraintSet()){
            
                facetFields.add(SolrField.hgv_series);
                facetFields.add(SolrField.ddbdp_series);
                return facetFields;
            }
            
            facetFields.add(getLeadingField());
            return facetFields;
            
        }
        
        @Override
        public String buildQueryClause(String rawClause){
            
            String queryString = "";
            if("".equals(rawClause)) return queryString;
            String specifier = this.hasConstraint() ? this.getConstraint() : "*";
            queryString = rawClause.replace(IdParam.SERIES.name(), specifier);
            return queryString;
            
        }
        
        @Override
        public void setIdValues(QueryResponse queryResponse, ArrayList<String> rawValues){
             
            ArrayList<SolrField> facetFields = this.getFacetFields();
            facetFields.remove(getLeadingField());            
            
            for(SolrField facetField : facetFields){
            
                FacetField ff = queryResponse.getFacetField(facetField.name());
                
                if(ff != null){
                    
                    String collectionPrefix = facetField.equals(SolrField.ddbdp_series) ? "ddbdp" : "hgv";
                    
                    List<Count> facetCounts = ff.getValues();
            
                    for(Count count : facetCounts){

                        String name = count.getName();
                        Long number = count.getCount();

                        if(name != null && !"".equals(name) && !"0".equals(name) && !"null".equals(name)  && number != 0){
                 
                            name = collectionPrefix + ";" + name;
                            
                            if(idValues.containsKey(name)){
                                
                                Long currentValue = idValues.get(name);
                                currentValue += number;
                                idValues.put(name, currentValue);
                                
                            }
                            else{
                                
                                idValues.put(name, number);
                                
                            }
                 
                        }   // closing extended null check
              
                    }       // closing loop through counts
                
                }           // closing ff null check          
            
            }               // closing loop through facet fiels
            
            
            for(String name : rawValues){
                
                if(idValues.containsKey(name)){
                    
                    Long currentValue = idValues.get(name);
                    currentValue++;
                    idValues.put(name, currentValue);   
                    
                }
                else{
                    
                    idValues.put(name, Long.valueOf(1));
                    
                }
                
            }
            
        }
        
        @Override
        public ArrayList<String> getIdValuesAsHTML(){ 
            
            ArrayList<String> stringifiedValues = new ArrayList<String>();
            
            for(Map.Entry<String, Long> entry : idValues.entrySet()){
                
                String extendedName = entry.getKey();
                String number = String.valueOf(entry.getValue());
                String[] nameBits = extendedName.split(";");
                String collection = nameBits[0].toUpperCase() + ": ";
                String name = nameBits[1];
                String displayName = name.replace("_", " ");
                displayName = collection + displayName + " (" + number + ")";
                String openTag = "<option value=\"";
                openTag += name;
                openTag += "\">";
                String stringValue = openTag + displayName + "</option>";
                
                if(!this.hasConstraint() || name.equals(this.getConstraint())){
                
                    stringifiedValues.add(stringValue); 
                    
                }
                
            }
            
            Collections.sort(stringifiedValues, new Comparator(){

                @Override
                public int compare(Object t, Object t1) {
                    
                    String rawFirst = (String) t;
                    String rawSecond = (String) t1;
                    return rawFirst.compareToIgnoreCase(rawSecond);
                    
                }
                
            }); 
            
            if(stringifiedValues.size() != 1){
            
                    String defaultValue = "<option value=\"default\">" + Facet.defaultValue + "</option>";
                    stringifiedValues.add(0, defaultValue);
                
            }
            
            return stringifiedValues;
            
        } 
        
        @Override
        public Boolean isDisabled(){
            
            if(this.hasConstraint()) return true;
            if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()) return true;
            if(idValues.size() < 2) return true;
            return false;
            
        }
   
    }
    
    /********************** COLLECTIONSEARCHCONFIGURATION *********************/
    
    private class CollectionSearchConfiguration extends CollectionSeriesSearchConfiguration{
        
        public CollectionSearchConfiguration(){
            
            super(IdParam.COLLECTION);
            
        }
        
        /**
         * 
         * Browsing by APIS collection is incompatible with browsing by HGV/DDbDP series,
         * and APIS collections do not use volumes. Accordingly, if either series or 
         * volume has been set, the APIS facet is disabled.
         *
         * @return 
         */
        
        @Override
        public ArrayList<SolrField> getFacetFields(){
            
            ArrayList<SolrField> fields = new ArrayList<SolrField>();
            
            if(searchConfigurations.get(IdParam.SERIES).hasConstraint()) return fields;
            if(searchConfigurations.get(IdParam.VOLUME).hasConstraint()) return fields;
            fields.add(SolrField.apis_series);
            return fields;
            
        }
        
        @Override
        public String getCSSSelector(){
            
            return "id-" + IdParam.COLLECTION.name().toLowerCase();
            
            
        }
        
        @Override
        public String buildQueryClause(String rawClause){
            
            String queryString = "";
            if(!this.hasConstraint()) return queryString;
            queryString = SolrField.apis_series + ":" + this.getConstraint();
            return queryString;
            
        }
        
        @Override
        public void setIdValues(QueryResponse queryResponse, ArrayList<String> rawValues){
                        
            for(SolrField facetField : this.getFacetFields()){
            
                FacetField ff = queryResponse.getFacetField(facetField.name());
                
                if(ff != null){
                    
                    List<Count> facetCounts = ff.getValues();
            
                    for(Count count : facetCounts){

                        String name = count.getName();
                        Long number = count.getCount();

                        if(name != null && !"".equals(name) && !"0".equals(name) && !"null".equals(name)  && number != 0){
                 
                            if(idValues.containsKey(name)){
                                
                                Long currentValue = idValues.get(name);
                                currentValue += number;
                                idValues.put(name, currentValue);
                                
                            }
                            else{
                                
                                idValues.put(name, number);
                                
                            }
                 
                        }   // closing extended null check
              
                    }       // closing loop through counts
                
                }           // closing ff null check          
            
            }               // closing loop through facet fields
            
        }
        
        @Override
        public ArrayList<String> getIdValuesAsHTML(){ 
            
            ArrayList<String> stringifiedValues = new ArrayList<String>();
            
            for(Map.Entry<String, Long> entry : idValues.entrySet()){
                
                String name = entry.getKey();
                String number = String.valueOf(entry.getValue());
                String displayName = name.replace("_", " ");
                displayName = displayName + " (" + number + ")";
                String openTag = "<option value=\"";
                openTag += name;
                openTag += "\">";
                String stringValue = openTag + displayName + "</option>";
                
                if(!this.hasConstraint() || entry.getKey().equals(this.getConstraint())){
                
                    stringifiedValues.add(stringValue); 
                    
                }
                
            }
            
            Collections.sort(stringifiedValues, new Comparator(){

                @Override
                public int compare(Object t, Object t1) {
                    
                    String rawFirst = (String) t;
                    String rawSecond = (String) t1;
                    return rawFirst.compareToIgnoreCase(rawSecond);
                    
                }
                
            }); 
            
            if(stringifiedValues.size() != 1){
            
                    String defaultValue = "<option value=\"default\">" + Facet.defaultValue + "</option>";
                    stringifiedValues.add(0, defaultValue);
                
            }
            
            return stringifiedValues;
            
        } 
        
        @Override
        public Boolean isDisabled(){
            
            if(searchConfigurations.get(IdParam.SERIES).hasConstraint()) return true;
            if(searchConfigurations.get(IdParam.VOLUME).hasConstraint()) return true;
            if(this.hasConstraint()) return true;
            if(idValues.size() < 2) return true;
            return false;
            
        }
        
    }
    
    /*********************** VOLUMEIDNOSEARCHCONFIGURATION ********************/
    
    private abstract class VolumeIdnoSearchConfiguration extends SearchConfiguration{
        
        public VolumeIdnoSearchConfiguration(IdParam ip){
            
           super(ip);
            
        }
        
        @Override
        public String buildHTMLControl(){
            
            StringBuilder html = new StringBuilder();
            
            html.append("<p id=\"");
            html.append(this.param.name().toLowerCase());
            html.append("-wrapper\">");
            
            html.append("<label for=\"");
            html.append(this.param.name());
            html.append("\">");
            html.append(this.param.getLabel());
            html.append("</label>");
            
            html.append("<input type=\"text\" name=\"");
            html.append(this.param.name());
            html.append("\" id=\"");
            html.append(this.getCSSSelector());
            html.append("\" size=\"5\" maxlength=\"10\"");

            if(this.isDisabled()){
                
                String msg = this.hasConstraint() ? this.getConstraint() : "n.a.";
                html.append(" value=\"");
                html.append(msg);
                html.append("\"");       
                html.append(" disabled");
                
            }

            html.append("/>");
            
            html.append("</p>");
            
                if(!this.isDisabled()){

                html.append("<div class=\"autocomplete-values\" id=\"");
                html.append(this.param.name().toLowerCase());
                html.append("-autocomplete\">");          
                ArrayList<String> autocompleteValues = this.getIdValuesAsHTML();
                Iterator<String> acit = autocompleteValues.iterator();
                while(acit.hasNext()){

                    html.append(acit.next());
                    if(acit.hasNext()) html.append(" ");


                }
                html.append("</div><!-- closing autocomplete values -->");
            
            }
            return html.toString();
            
        }

        @Override
        public ArrayList<String> getIdValuesAsHTML() {
            
             ArrayList<String> identifiers = new ArrayList<String>(idValues.keySet());
             Collections.sort(identifiers, new IdComparator());
             return identifiers;
            
        }
       
        
    } 
    
    /******************** VOLUMESEARCHCONFIGURATION **************************/
    
    private class VolumeSearchConfiguration extends VolumeIdnoSearchConfiguration{
        
        public VolumeSearchConfiguration(){
            
            super(IdParam.VOLUME);
            
        }
        
        /**
         * 
         * The VOLUME facet autopopulates only when IDNO or SERIES have been set,
         * and when either of these are set the *_led_path mechanism needs to be used,
         * rather than conventional faceting
         * 
         * @return 
         */
        @Override
        public ArrayList<SolrField> getFacetFields(){
            
            ArrayList<SolrField> ff = new ArrayList<SolrField>();
            if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()) return ff;
            if(!anyConstraintSet()) return ff;
            ff.add(getLeadingField());            
            return ff;
            
            
        }
        
        @Override
        public String buildQueryClause(String rawClause){
            
            String queryString = "";
            if("".equals(rawClause)) return queryString;
            String specifier = this.hasConstraint() ? this.getConstraint() : "*";
            queryString = rawClause.replace(IdParam.VOLUME.name(), specifier);      
            return queryString;
            
        }
        
        @Override
        public void setIdValues(QueryResponse queryResponse, ArrayList<String> rawValues){
                        
            for(String value : rawValues){
                
                if(!value.equals("0")) idValues.put(value, Long.valueOf(0) );

        
            }
            
        }
        
        @Override
        public Boolean isDisabled(){
            
            if(this.hasConstraint()) return true;
            if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()) return true;
            if(idValues.size() == 0 && anyConstraintSet()) return true;
            return false;
            
        }

    }
        

   
       /************************ IDNOSEARCHCONFIGURATION **********************/
       
    private class IdnoSearchConfiguration extends VolumeIdnoSearchConfiguration{
        
        public IdnoSearchConfiguration(){
            
            super(IdParam.IDNO);
            
        }
        
        /**
         * Facetting should only be performed in connection with IDNOs when:
         * (i) no constraint has been set on the facet itself
         * (ii) a COLLECTION constraint has been set
         * 
         * 
         * @return 
         */
        
        @Override
        public ArrayList<SolrField> getFacetFields(){
            
            ArrayList<SolrField> ff = new ArrayList<SolrField>(); 
            if(!anyConstraintSet()) return ff;
            if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()){
                
                ff.add(SolrField.apis_full_identifier);
                ff.add(SolrField.apis_publication_id);
                ff.add(SolrField.apis_inventory);
                ff.add(SolrField.ddbdp_full_identifier);
                ff.add(SolrField.hgv_full_identifier);
                return ff;
            }
            ff.add(getLeadingField());
            return ff;    
            
        }
        
        @Override
        public String buildQueryClause(String rawClause){
            
            String queryString = "";
            
            if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()){
                
                if(this.hasConstraint()){

                    queryString += "(";
                    queryString += SolrField.apis_full_identifier.name() + ":" + this.getConstraint();
                    queryString += " OR ";
                    queryString += SolrField.apis_inventory.name() + ":" + this.getConstraint();
                    queryString += " OR ";
                    queryString += SolrField.apis_publication_id.name() + ":" + this.getConstraint();
                    queryString += ")";
                
                }
                
            }
            else{
                
                if("".equals(rawClause)) return queryString;
                String specifier = this.hasConstraint() ? this.getConstraint() : "*";
                queryString = rawClause.replace(IdParam.IDNO.name(), specifier);
                
            }
            
            return queryString;
            
        }
        
        @Override
        public String getCSSSelector(){
            
            return "id-" + IdParam.IDNO.name().toLowerCase();
            
            
        }
        
        @Override
        public Boolean isDisabled(){
            
            if(this.hasConstraint()) return true;
            return false;
           
            
        }
        
        @Override
        public void setIdValues(QueryResponse queryResponse, ArrayList<String> rawValues){
             
            ArrayList<SolrField> facetFields = this.getFacetFields();
            facetFields.remove(getLeadingField());            
            
            for(SolrField facetField : facetFields){
            
                FacetField ff = queryResponse.getFacetField(facetField.name());
                
                if(ff != null){
                                        
                    List<Count> facetCounts = ff.getValues();
            
                    for(Count count : facetCounts){

                        String name = count.getName();
                        Long number = count.getCount();

                        if(name != null && !"".equals(name) && !"0".equals(name) && !"null".equals(name)  && number != 0){
                                             
                            if(idValues.containsKey(name)){
                                
                                Long currentValue = idValues.get(name);
                                currentValue += number;
                                idValues.put(name, currentValue);
                                
                            }
                            else{
                                
                                idValues.put(name, number);
                                
                            }
                 
                        }   // closing extended null check
              
                    }       // closing loop through counts
                
                }           // closing ff null check          
            
            }               // closing loop through facet fiels
            
            
            for(String name : rawValues){
                
                if(idValues.containsKey(name)){
                    
                    Long currentValue = idValues.get(name);
                    currentValue++;
                    idValues.put(name, currentValue);   
                    
                }
                else{
                    
                    idValues.put(name, Long.valueOf(1));
                    
                }
                
            }
            
        }
        
    }
    
  }
    

    
    
    
