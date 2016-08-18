package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.ServletUtils;
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
 * Note further the idiosyncratic use of the terms <i>collection</i> and <i>series</i> here.
 * In other classes, <i>collection</i> is usually used to refer to the top level of the 
 * classification hierarchy (i.e., collection -> series -> idno). Here, however, a distinction
 * must be drawn between the top level of the apis hierarchy, and the top level of the 
 * ddbdp/hgv hierarchy. <i>Collection</i> in this class thus refers to the top of the apis
 * hierachy, while <i>series</i> is used for ddbdp/hgv hierarchies.
 * 
 * @author thill
 * @see info.papyri.dispatch.browse.facet.Facet
 * @see info.papyri.dispatch.browse.facet.SearchConfiguration
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
        
        IdParam(String msg){ label = msg; }
        
        public String getLabel(){ return label; }
    
    }  
    
    /**
     * The basic data structure of the class.
     * 
     * Its keys are <code>IdParam</code> values; its values are <code>SearchConfiguration</code>
     * objects, defined in an inner class detailed below.
     * 
     */
    
    private EnumMap<IdParam, SearchConfiguration> searchConfigurations;
    
    /**
     * The three *ledOrder members are used in relation to the <code>SolrParam.series_led_order</code>,
     * <code>SolrParam.volume_led_order</code>, and <code>SolrParam.idno_led_order</code> fields.
     * 
     * The reason these fields and associated members are required is that:
     * (a) without them there is no retrievable connection among the series, volume, and id 
     * numbers: if a text is identified both as bgu.1.1 and p.louvre.1.4, it is the job of these fields
     * to correlate the 'bgu', '1', and '1' values with each other (and similarly 'p.louvre', '1', and '4'.
     * (b) The exigencies of searching with wildcards mean that it is at best unwise to search using a
     * leading wildcard. Accordingly, all three fields must exist so that efficient searches can be
     * performed in cases where only one or two of the hierarchy values are given.
     * 
     */
    
    ArrayList<IdParam> idnoLedOrder;
    ArrayList<IdParam> volumeLedOrder;
    ArrayList<IdParam> seriesLedOrder;
    EnumMap<SolrField, ArrayList<IdParam>> orders;
    private String apisOnlyHTMLValue = "apisonly";
    private String apisOnlyHTMLLabel = "All APIS records";    
       
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
        
        idnoLedOrder = new ArrayList<IdParam>   (Arrays.asList(IdParam.IDNO, IdParam.SERIES, IdParam.VOLUME, IdParam.COLLECTION));
        volumeLedOrder = new ArrayList<IdParam> (Arrays.asList(IdParam.VOLUME, IdParam.IDNO, IdParam.SERIES, IdParam.COLLECTION));
        seriesLedOrder = new ArrayList<IdParam> (Arrays.asList(IdParam.SERIES, IdParam.VOLUME, IdParam.IDNO, IdParam.COLLECTION)); 
        
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
        
        Boolean seriesSet = searchConfigurations.get(IdParam.SERIES).hasConstraint();
        Boolean collectionSet = searchConfigurations.get(IdParam.COLLECTION).hasConstraint();
        Boolean volumeSet = searchConfigurations.get(IdParam.VOLUME).hasConstraint();
        Boolean idnoSet = searchConfigurations.get(IdParam.IDNO).hasConstraint();
        String qpref = getLeadingField().name() + ":";
        if(!seriesSet && !collectionSet){
            
            String specifierClause = this.getIdnoOrVolumeOnlySpecifierClause();
            solrQuery.addFilterQuery(qpref + specifierClause);
            return solrQuery;
            
        }
        if(seriesSet && collectionSet && (idnoSet || volumeSet)){
            
            solrQuery = buildStandardFieldQuery(solrQuery);
            return solrQuery;
            
            
        }
        if(seriesSet){
            
            String seriesSpecifierClause = getSeriesSpecifierClause();
            solrQuery.addFilterQuery(qpref + seriesSpecifierClause);
            
        }
        if(collectionSet){
            
            if(apisOnlyHTMLValue.equals(searchConfigurations.get(IdParam.COLLECTION).getConstraint())){
                
                solrQuery.addFilterQuery(SolrField.collection.name() + ":apis");
                
            }
            else{

                String collSpecifierClause = getCollectionSpecifierClause();
                solrQuery.addFilterQuery(qpref + collSpecifierClause);
            
            }
            
        }

        return solrQuery; 
        
    }
    
    /**
     * Returns the <code>SolrField</code> with the most specific id information available.
     * 
     * @return 
     */
    
    private SolrField getLeadingField(){
        
        if(searchConfigurations.get(IdParam.IDNO).hasConstraint()) return SolrField.idno_led_path;
        if(searchConfigurations.get(IdParam.SERIES).hasConstraint()) return SolrField.series_led_path;
        if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()) return SolrField.series_led_path;
        if(searchConfigurations.get(IdParam.VOLUME).hasConstraint()) return SolrField.volume_led_path;      
        return SolrField.series_led_path;
        
    }
    
    /**
     * Returns a string to be used as a search constraint applicable to the
     * <code>SolrField.series_led_order</code>, <code>SolrField.volume_led_order</code>,
     * or <code>SolrField.idno_led_order</code> fields.
     * 
     * @return 
     */
    
    private String getSpecifierClause(){
        
        SolrField leadField = this.getLeadingField();

        String specifierClause = this.getRawSpecifierClause(leadField);

        specifierClause = searchConfigurations.get(IdParam.VOLUME).buildQueryClause(specifierClause);
        specifierClause = searchConfigurations.get(IdParam.IDNO).buildQueryClause(specifierClause);
        
        return specifierClause;
        
    }
    
    private String getIdnoOrVolumeOnlySpecifierClause(){
        
        String specifierClause = getSpecifierClause();
        specifierClause = specifierClause.replace(IdParam.SERIES.name(), "*");
        specifierClause = specifierClause.replace(IdParam.COLLECTION.name(), "*");
        return specifierClause;
        
    }
    
    private String getSeriesSpecifierClause(){
        
        String specifierClause = getSpecifierClause();
        specifierClause = searchConfigurations.get(IdParam.SERIES).buildQueryClause(specifierClause);
        return specifierClause;
    }
    
    private String getCollectionSpecifierClause(){
        
        String specifierClause = getSpecifierClause();
        specifierClause = searchConfigurations.get(IdParam.COLLECTION).buildQueryClause(specifierClause); 
        return specifierClause;
        
    }
    
    // in cases where both volume and collection are specified, along with an id number and or a 
    // volume, than the coupling provided by the various SearchConfiguration classes is too tight -
    // the id and volume should be retrieved if they are associated with EITHER apis OR ddbdp, not
    // necessarily BOTH ... AND ...
    // this method accordingly cuts through the inner class methods
    // the existence of this method perhaps indicates that some refactoring should be done in future
    
    private SolrQuery buildStandardFieldQuery(SolrQuery solrQuery){
        
        SearchConfiguration collectionConfig = searchConfigurations.get(IdParam.COLLECTION);
        SearchConfiguration seriesConfig = searchConfigurations.get(IdParam.SERIES);
        SearchConfiguration volumeConfig = searchConfigurations.get(IdParam.VOLUME);
        SearchConfiguration idnoConfig = searchConfigurations.get(IdParam.IDNO);
        
        if(collectionConfig.getConstraint().equals(apisOnlyHTMLValue)){
            
            solrQuery.addFilterQuery(SolrField.collection.name() + ":apis");
            
        } else{
            
            solrQuery.addFilterQuery(SolrField.apis_series.name() + ":" + collectionConfig.getConstraint());
            
        }
        
        StringBuilder seriesConstraint = new StringBuilder("(");
        seriesConstraint.append(SolrField.ddbdp_series.name());
        seriesConstraint.append(":");
        seriesConstraint.append(seriesConfig.getConstraint());
        seriesConstraint.append(" OR ");
        seriesConstraint.append(SolrField.hgv_series.name());
        seriesConstraint.append(":");
        seriesConstraint.append(seriesConfig.getConstraint());
        seriesConstraint.append(" OR ");
        seriesConstraint.append(SolrField.dclp_series.name());
        seriesConstraint.append(":");
        seriesConstraint.append(seriesConfig.getConstraint());
        seriesConstraint.append(")");
        solrQuery.addFilterQuery(seriesConstraint.toString());
        
        if(volumeConfig.hasConstraint()){
            
            StringBuilder volumeConstraint = new StringBuilder("(");
            volumeConstraint.append(SolrField.ddbdp_volume.name());
            volumeConstraint.append(":");
            volumeConstraint.append(volumeConfig.getConstraint());
            volumeConstraint.append(" OR ");
            volumeConstraint.append(SolrField.hgv_volume.name());
            volumeConstraint.append(":");
            volumeConstraint.append(volumeConfig.getConstraint());
            volumeConstraint.append(" OR ");
            volumeConstraint.append(SolrField.dclp_volume.name());
            volumeConstraint.append(":");
            volumeConstraint.append(volumeConfig.getConstraint());
            volumeConstraint.append(")");
            solrQuery.addFilterQuery(volumeConstraint.toString());
        }
        
        if(idnoConfig.hasConstraint()){
            
            ArrayList<SolrField> idFields = new ArrayList<SolrField>(Arrays.asList(SolrField.apis_full_identifier, SolrField.apis_inventory, SolrField.apis_publication_id, SolrField.ddbdp_full_identifier, SolrField.hgv_full_identifier, SolrField.dclp_full_identifier));
            StringBuilder idnoConstraint = new StringBuilder("(");
            Iterator<SolrField> iit = idFields.iterator();
            while(iit.hasNext()){
                
                idnoConstraint.append(iit.next().name());
                idnoConstraint.append(":");
                idnoConstraint.append(idnoConfig.getConstraint());
                if(iit.hasNext()) idnoConstraint.append(" OR ");
                
            }
            idnoConstraint.append(")");
            solrQuery.addFilterQuery(idnoConstraint.toString());
            
            
        }
        return solrQuery;
        
    }
    
    /**
     * Returns an ordered template value string into which actual values may then 
     * be substituted
     * 
     * @param ledPath A <code>SolrField</code> indicating the template component 
     * (Collection, Series, Volume, or Idno) that is to come first in the ordering.
     * @see SearchConfiguration#buildQueryClause(java.lang.String) 
     */
    
    private String getRawSpecifierClause(SolrField ledPath){
        
        String specifierClause = "";
        
        ArrayList<IdParam> order = orders.get(ledPath);
        for(IdParam ip : order){
            
            specifierClause += ip.name();
            specifierClause += ";";
            
        }
        specifierClause = specifierClause.substring(0, specifierClause.length() - 1);
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
    
    /**
     * Takes the <code>QueryResponse</code> object, parses its values for the relevant series/volume/idno
     * information, and distributes this information to the relevant widgets.
     *  
     * @param queryResponse
     * @return 
     */
    
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
        
        Pattern pattern = Pattern.compile("^.+?;.+?;.+?;.+?$");

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
        int volumeIndex = fieldOrder.indexOf(IdParam.VOLUME);
        int idnoIndex = fieldOrder.indexOf(IdParam.IDNO);
        ArrayList<String> tempSeries = new ArrayList<String>();
        ArrayList<String> tempColls = new ArrayList<String>();
        ArrayList<String> tempVols = new ArrayList<String>();
        ArrayList<String> tempIdnos = new ArrayList<String>();
        ArrayList<String> relevantCollections = buildRelevantCollections();

        ArrayList<String> series = components.get(seriesIndex);
        ArrayList<String> volumes = components.get(volumeIndex);
        ArrayList<String> idnos = components.get(idnoIndex);
        
        for(int i = 0; i < series.size(); i++){
            
            String seriesName = series.get(i);
            String collection = collections.get(i);
            if(relevantCollections.contains(seriesName)){
                
                tempVols.add(volumes.get(i));
                tempIdnos.add(idnos.get(i));
                
                
            }
            if("apis".equals(collection)){
                
                tempColls.add(seriesName);
                
            }
            else{
                
                tempSeries.add(collection + ";" + seriesName);
            }
            
        }
        
        searchConfigurations.get(IdParam.SERIES).setIdValues(queryResponse, tempSeries);
        searchConfigurations.get(IdParam.COLLECTION).setIdValues(queryResponse, tempColls);
        searchConfigurations.get(IdParam.VOLUME).setIdValues(queryResponse, tempVols);
        searchConfigurations.get(IdParam.IDNO).setIdValues(queryResponse, tempIdnos);
        
        return true;
             
    }
    
    private ArrayList<String> buildRelevantCollections(){
        
        ArrayList<String> relevantCollections = new ArrayList<String>();
        if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()){
            
            relevantCollections.add(searchConfigurations.get(IdParam.COLLECTION).getConstraint());
            
        }
        if(searchConfigurations.get(IdParam.SERIES).hasConstraint()){
            
            relevantCollections.add(searchConfigurations.get(IdParam.SERIES).getConstraint());
            
        }
        return relevantCollections;
        
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
                    hasConstraint = true;
        
               }
                
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
        
            String[] keyBits = key.split(";");
            String seriesName = "";
            try{ seriesName = keyBits[1]; } 
            catch(Exception e){}
            if(seriesName.equals(facetValue)){
                
                collection = keyBits[0];
                break;
            }
        
        }

        collection = collection.toUpperCase() + " ";
        return collection + IdParam.SERIES.name().toUpperCase(); 
        
    }
    
    @Override
    public String getDisplayValue(String value){
        
        if(apisOnlyHTMLValue.equals(value)) return apisOnlyHTMLLabel;
        return ServletUtils.scrub(value).replaceAll("[_*]", " ");
        
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
    
    /**
     * Determines whether a constraint has been set on the <code>Facet</code> as a whole,
     * as opposed to simply on the particular <code>SearchConfiguration</code>
     * 
     * @return 
     */

    public Boolean anyConstraintSet(){

        for(SearchConfiguration sc : searchConfigurations.values()){

            if(sc.hasConstraint()) return true;

        }

        return false;

    }
       
    
    /**
     * Inner class defining the logic and behavior of individual levels of the classification hierarchy
     * 
     * These classes are in themselves simple, and their members names and purpose should in 
     * most cases be self-evident. In general the methods are simply devolved versions of
     * methods normally more amply defined in the <code>Facet</code> class and its subclasses.
     * More complex considerations arise chiefly in regard to interactions among these various 
     * controls, and notes are made of this where appropriate.
     * 
     */
    
    private abstract class SearchConfiguration{
        
        /** The constraint, if any, applied to the SearchConfiguration */
        String constraint;
       /**
         * The list of values, and corresponding counts, associated with the SearchConfiguration
         * 
         */
        HashMap<String, Long> idValues;
        /** @see info.papyri.dispatch.browse.facet.IdentifierFacet.IdParam */
        IdParam param;
        
        public SearchConfiguration(IdParam ip){
            
            constraint = "";
            idValues = new HashMap<String, Long>();
            param = ip;
                   
        }
        
        public void setConstraint(String newConstraint){ constraint = newConstraint; }
        
        public String getConstraint(){ return constraint; }
        
        public Boolean hasConstraint(){ return !"".equals(constraint); }
        
        public HashMap<String, Long> getIdValues(){  return idValues; }
        
        public String getCSSSelector(){ return "id-" + param.name().toLowerCase(); }
        
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

        
        public abstract ArrayList<SolrField> getFacetFields();
        public abstract ArrayList<String> getIdValuesAsHTML();
        public abstract String buildQueryClause(String rawClause);
        public abstract String buildHTMLControl();
        public abstract Boolean isDisabled();
    
    }
    
    /**
     * Superclass for the <code>CollectionSearchConfiguration</code> and <code>SeriesSearchConfiguration</code>
     * classes.
     * 
     * These classes differ from <code>VolumeIdnoSearchConfiguration</code> and its subclasses chiefly in:
     * (a) their HTML form controls - drop-down selectors in the case of the former, input boxes for the latter
     * (b) their population - the latter are populated only once a constraint has been set upon the former (because
     * there are simply too many individual id and volume numbers for this to be practical with a prior constraint
     * limiting the search space.
     * 
     * 
     */
    
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
    
    
    /**
     * <code>SearchConfiguration</code> for browsing by DDbDP / HGV series
     * 
     */
    
    
    private class SeriesSearchConfiguration extends CollectionSeriesSearchConfiguration{
                
        public SeriesSearchConfiguration(){
            
            super(IdParam.SERIES);
            
            
        }
        
        @Override
        public String getCSSSelector(){
            
            return "id-" + IdParam.SERIES.name().toLowerCase();

        }
        /*
         * 
         * Note that we do not want standard faceting for this facet if:
         * (i) the COLLECTION facet has a constraint (in which case this facet is disabled
         * (ii) it itself has a constraint (in which case this is the only value it can have)
         * (iii) VOLUME or IDNO have a constraint set, in which case the *_led_path fields must be used
         * instead of the standard faceting mechanism
         * 
         */
        @Override
        public ArrayList<SolrField> getFacetFields(){
            
            ArrayList<SolrField> facetFields = new ArrayList<SolrField>();
            
            if(!anyConstraintSet()){
            
                facetFields.add(SolrField.dclp_series);
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
            queryString = queryString.replace(IdParam.COLLECTION.name(), "*");
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
            //if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()) return true;
            if(idValues.size() < 2) return true;
            return false;
            
        }
   
    }
    
    /**
     * <code>SearchConfiguration</code> for browsing by APIS collection
     * 
     * Note that browsing by DDbDP/HGV series is incompatible with browsing by APIS collection - 
     * only one of <code>SeriesSearchConfiguration</code> and <code>CollectionSearchConfiguration</code>
     * may be active at any given time. In addition, APIS collections do not use volume numbers,
     * so only on of <code>CollectionSearchConfiguration</code> and <code>VolumeSearchConfiguration</code>
     * may be active at any one time.
     */
    
    private class CollectionSearchConfiguration extends CollectionSeriesSearchConfiguration{
        
        private Boolean apisOnly;
        
        public CollectionSearchConfiguration(){
            
            super(IdParam.COLLECTION);
            apisOnly = false;
            
        }
        
        @Override
        public ArrayList<SolrField> getFacetFields(){
            
            ArrayList<SolrField> fields = new ArrayList<SolrField>();
            
            if(searchConfigurations.get(IdParam.VOLUME).hasConstraint()) return fields;
            fields.add(SolrField.collection);
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
            if(getConstraint().equals(apisOnlyHTMLValue)){
                
                queryString = SolrField.collection + ":" + "apis";
                
            }
            else{
           
                String specifier = this.hasConstraint() ? this.getConstraint() : "*";
                queryString = rawClause.replace(IdParam.SERIES.name(), specifier); 
                queryString = queryString.replace(IdParam.COLLECTION.name(), "apis");
                
            }
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

                        if(name != null && !"".equals(name) && !"0".equals(name) && !"null".equals(name)  && number != 0 && !name.equals("hgv") && !name.equals("ddbdp")){
      
                               idValues.put(name, number);
                 
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
                if(displayName.equals("apis")){
                    
                    name = apisOnlyHTMLValue;
                    displayName = apisOnlyHTMLLabel;
                    
                }
                displayName = displayName + " (" + number + ")";
                String openTag = "<option value=\"";
                openTag += name;
                openTag += "\">";
                String stringValue = openTag + displayName + "</option>";
                
                if((!this.hasConstraint() || entry.getKey().equals(this.getConstraint())) || this.getConstraint().equals(apisOnlyHTMLValue)){
                
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
            
            if(stringifiedValues.size() != 1 && !apisOnly){
            
                    String defaultValue = "<option value=\"default\">" + Facet.defaultValue + "</option>";
                    stringifiedValues.add(0, defaultValue);
                
            }
            
            return stringifiedValues;
            
        } 
        
        @Override
        public Boolean isDisabled(){
            
           // if(searchConfigurations.get(IdParam.SERIES).hasConstraint()) return true;
            if(searchConfigurations.get(IdParam.VOLUME).hasConstraint()) return true;
            if(this.hasConstraint() && !apisOnly) return true;
            if(idValues.size() < 2) return true;
            return false;
            
        }
        
        @Override
        public void setConstraint(String newConstraint){
            
            constraint = newConstraint;
            if(constraint.equals(apisOnlyHTMLValue)){
                
                apisOnly = true;
            
            }
            else{
                
                apisOnly = false;
                
            }
            
        }
        
    }
    
    /**
     * Superclass for <code>VolumeSearchConfiguration</code> and <code>IdnoSearchConfiguration</code>.
     * 
     * The two subclasses are distinguished from <code>CollectionSeriesSearchConfiguration</code> and
     * its subclasses chiefly by their HTML controls - text inputs rather than drop-downs, and by the 
     * fact that they are populated only once other constraints have been set.
     */
    

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
    
   /**
     * <code>SearchConfiguration</code> for browsing by volume number.
     * 
     * Note that this <code>SearchConfiguration</code> is never applicable when browsing by
     * APIS collection.
     * 
     */
        
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
            if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint() && !searchConfigurations.get(IdParam.SERIES).hasConstraint()) return ff;
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
            if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint() && !searchConfigurations.get(IdParam.SERIES).hasConstraint()) return true;
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
         * (ii) a collection or series constraint has been set
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
            //    return ff;
            }
            if(searchConfigurations.get(IdParam.SERIES).hasConstraint()){
                
                ff.add(SolrField.ddbdp_full_identifier);
                ff.add(SolrField.hgv_full_identifier);
                ff.add(SolrField.dclp_full_identifier);
                
            }
            ff.add(getLeadingField());
            return ff;    
            
        }
        
        @Override
        public String buildQueryClause(String rawClause){
            
            String queryString = "";
            if("".equals(rawClause)) return queryString;
            String specifier = this.hasConstraint() ? this.getConstraint() : "*";
            queryString = rawClause.replace(IdParam.IDNO.name(), specifier); 
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
            
            Iterator<String> rvit = rawValues.iterator();
            while(rvit.hasNext()){
                
                String idno = rvit.next();
                if(idno != null && !"".equals(idno)) idValues.put(idno.replaceAll(" ", ":"), Long.valueOf(1));
                
                
            }
            
            
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
                msg = msg.replaceAll("[_*]", " ");
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
        
    }
    
  }
    

    
    
    
