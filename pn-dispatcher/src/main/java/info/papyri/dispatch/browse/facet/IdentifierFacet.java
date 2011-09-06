package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.FieldNotFoundException;
import info.papyri.dispatch.browse.IdComparator;
import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
                    VOLUME("Volume"), 
                    IDNO("ID Number");
    
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
       
    /**
     * Constructor.
     * 
     * Note that the <code>searchConfigurations</code> <code>EnumMap</code> is initialized here.
     * 
     */
    
    public IdentifierFacet(){
        
        super(SolrField.collection, FacetParam.IDENTIFIER, "Get documents by id");     
        searchConfigurations = new EnumMap<IdParam, SearchConfiguration>(IdParam.class);
        searchConfigurations.put(IdParam.SERIES, new SeriesSearchConfiguration());
        searchConfigurations.put(IdParam.COLLECTION, new CollectionSearchConfiguration());
        searchConfigurations.put(IdParam.VOLUME, new VolumeSearchConfiguration());
        searchConfigurations.put(IdParam.IDNO, new IdnoSearchConfiguration());
        
    }    
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        for(IdParam ip : IdParam.values()){
            
            ArrayList<SolrField> fields = searchConfigurations.get(ip).getFacetFields();
            for(SolrField ipField : fields){ 
                
                solrQuery.addFacetField(ipField.name());
            
            } 

            if(searchConfigurations.get(ip).hasConstraint()){
                
                String luceneQueryString = searchConfigurations.get(ip).buildQueryClause();
                if(!"".equals(luceneQueryString)) solrQuery.addFilterQuery(luceneQueryString);
                
            }

        }
                
        return solrQuery; 
        
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

        for(IdParam ip : IdParam.values()){

            ArrayList<SolrField> fields = searchConfigurations.get(ip).getFacetFields();
            
            for(SolrField ipField : fields){

                FacetField ff = queryResponse.getFacetField(ipField.name());
                
                if(ff != null){
                    
                    searchConfigurations.get(ip).addIdValues(ipField, new ArrayList<Count>(ff.getValues()));
                    
                }
                
            }   
                     
        }
        
    }
    
    @Override
    public ArrayList<String> getFacetConstraints(String facetParam){
                
        for(IdParam ip : IdParam.values()){
        
            if(ip.name().equals(facetParam)){
                
                return searchConfigurations.get(ip).getConstraints();
                     
            } 
            
        }
        
        return new ArrayList<String>();
    }
    
    @Override
    String generateHiddenFields(){
        
        StringBuilder html = new StringBuilder();
        
        for(IdParam ip : IdParam.values()){
            
            String name = ip.name();
            
            for(String constraint : searchConfigurations.get(ip).getConstraints()){
                
                html.append("<input type=\"hidden\" name=\"");
                html.append(name);
                html.append("\" value=\"");
                html.append(constraint);
                html.append("\"/>");
                            
            }
            
        }
        
        return html.toString();
        
    }
    
    @Override
    public Boolean addConstraints(Map<String, String[]> params){
        
        Boolean hasConstraint = false;
        
        for(IdParam ip : IdParam.values()){
            
            if(params.containsKey(ip.name())){
                
               ArrayList<String> values = new ArrayList<String>(Arrays.asList(params.get(ip.name())));
               searchConfigurations.get(ip).addConstraints(values);
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
            
            ArrayList<String> constraints = searchConfigurations.get(ip).getConstraints();
            
            Iterator<String> cit = constraints.iterator();
            
            while(cit.hasNext()){
                
                queryString += "&";
                String constraint = cit.next();
                queryString += paramName + "=" + constraint;
                
            }
            
        }
        
        if(!"".equals(queryString)) queryString = queryString.substring(1);
        return queryString;
        
    }
    
    @Override
    public String getAsFilteredQueryString(String filterParam, String filterValue){
        
        String queryString = "";
        
        for(IdParam ip : IdParam.values()){
        
            ArrayList<String> constraints = searchConfigurations.get(ip).getConstraints();
            
            for(String constraint : constraints){
            
                if(!(filterParam.equals(ip.name()) && filterValue.equals(constraint))){
                
                    queryString += "&";
                    queryString += ip.name() + "=";
                    queryString += constraint;
                
                }
            }
        }
        
        if(!"".equals(queryString)) queryString = queryString.substring(1);
        return queryString;
        
    }
    
    @Override
    public String getDisplayName(String facetParam, String facetValue){
        
        if(!facetParam.equals(IdParam.SERIES.name())) return facetParam.toUpperCase();
             
        SeriesSearchConfiguration ssc = (SeriesSearchConfiguration) searchConfigurations.get(IdParam.SERIES);
        HashMap<Count, SolrField> csf = ssc.getIdValues();

        String collection = "";
        
        try{
            
            SolrField collectionField = ssc.getFieldFromCount(facetValue);
            
            if(collectionField.equals(SolrField.ddbdp_series)) collection =  "DDbDP ";
            if(collectionField.equals(SolrField.hgv_series)) collection = "HGV ";
            
        }
        catch(FieldNotFoundException fnfe){

            System.out.println(fnfe.getMessage());
            
        }

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
    
    private interface SearchConfiguration{
    
        public void addConstraints(ArrayList<String> newValues);
        public ArrayList<String> getConstraints();
        public ArrayList<SolrField> getFacetFields();
        public void addIdValues(SolrField solrField, ArrayList<Count> results);
        public ArrayList<String> getIdValuesAsHTML();
        public String buildQueryClause();
        public Boolean hasConstraint();
        public String getCSSSelector();
        public String buildHTMLControl();
        public Boolean isDisabled();
    
    }
    
    private abstract class CollectionSeriesSearchConfiguration implements SearchConfiguration {
    
        /** The constraints to be applied, as selected by the user. */
        ArrayList<String> constraints;
        /** Maps between the faceted values returned and the SolrFields within which these
         * values are defined. Required because DDbDp and HGV values are lumped together in a single
         * control, but need to be disambiguated for query definition and display purposes.
         */
        HashMap<Count, SolrField> idValues;
        IdParam param;
 
        public CollectionSeriesSearchConfiguration(IdParam ip){
            
            constraints = new ArrayList<String>();
            idValues = new HashMap<Count, SolrField>();
            param = ip;
            
        }
    
        @Override
        public void addConstraints(ArrayList<String> newValues){ 
            
            for(String newValue : newValues){
                
                if(!constraints.contains(newValue)) constraints.add(newValue);
                
            }
            
        
        }
        
        @Override
        public ArrayList<String> getConstraints(){ return constraints; }
        
        @Override
        public ArrayList<String> getIdValuesAsHTML(){ 
            
            ArrayList<String> stringifiedValues = new ArrayList<String>();
            
            for(Map.Entry<Count, SolrField> entry : idValues.entrySet()){
                
                Count count = entry.getKey();
                SolrField sf = entry.getValue();
                String collection = "";
                if(sf.equals(SolrField.hgv_series)) collection = "HGV: ";
                if(sf.equals(SolrField.ddbdp_series)) collection = "DDbDP: ";
                String name = count.getName();
                String displayName = name.replace("_", " ");
                String number = String.valueOf(count.getCount());         
                String stringValue = collection + displayName + " (" + number + ")";
                String openTag = "<option value=\"";
                openTag += name;
                openTag += "\">";
                stringValue = openTag + stringValue + "</option>";
                stringifiedValues.add(stringValue);             
                
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
        
        public HashMap<Count, SolrField> getIdValues(){ 
        
               return idValues;
            
        }
        
        /**
         * Given a string representing the name of a DDbDP or HGV series, returns a SolrField
         * (<code>SolrField.ddbdp_series | SolrField.hgv_series</code>) indicating which of the two
         * it is.
         * 
         * @param soughtName The name of a DDbDP or HGV collection
         * @return 
         * @throws FieldNotFoundException 
         */
        
        public SolrField getFieldFromCount(String soughtName) throws FieldNotFoundException{
                        
            for(Map.Entry<Count, SolrField> entry : idValues.entrySet()){
                
                String name = entry.getKey().getName();

                if(name.equals(soughtName)){
                    return entry.getValue();
                    
                }
                
                
            }
            
            throw new FieldNotFoundException("Field not found: no Count found with name " + soughtName);     
            
        }
        
        
        @Override
        public Boolean hasConstraint(){ return constraints.size() > 0; }    
        
        @Override
        public abstract ArrayList<SolrField> getFacetFields();
        
        @Override
        public String buildQueryClause(){

            String lql = "";

            Iterator<String> cit = constraints.iterator();

            while(cit.hasNext()){

                String constraint = cit.next();
                lql += "(";

                Iterator<SolrField> fit = this.getFacetFields().iterator();

                while(fit.hasNext()){

                    SolrField field = fit.next();

                    lql += field.name() + ":" + constraint;

                    if(fit.hasNext()) lql += " OR ";

                }

                lql += ")";

                if(cit.hasNext()) lql += " AND ";

            }    

            return lql;

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
        
        @Override
        public void addIdValues(SolrField solrField, ArrayList<Count> counts){ 
        
          for(Count count : counts){
              
             String name = count.getName();
             long number = count.getCount();
             
             // TODO: check why null or otherwise meaningless values are so often encountered;
             // this has *got* to be a data curation problem
             if(name != null && !"".equals(name) && !"0".equals(name) && !"null".equals(name)  && number != 0){
                 
                 idValues.put(count, solrField);
                 
             }
              
          }  
        
        }

    }
    
    private class SeriesSearchConfiguration extends CollectionSeriesSearchConfiguration{
        
        public SeriesSearchConfiguration(){
            
            super(IdParam.SERIES);
            
            
        }
        
        @Override
        public ArrayList<SolrField> getFacetFields(){
            
            ArrayList<SolrField> fields = new ArrayList<SolrField>();
            
            // it is not possible to choose an HGV/DDbDP series 
            //if an APIS collection has already been set
            
            if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()) return fields;
            
            fields.add(SolrField.ddbdp_series);
            fields.add(SolrField.hgv_series);
            
            return fields;
            
        }
        
        @Override
        public String getCSSSelector(){
            
            return "id-" + IdParam.SERIES.name().toLowerCase();

        }
        
        @Override
        public Boolean isDisabled(){
            
            if(this.getFacetFields().isEmpty()) return true;
            if(this.getIdValues().size() < 2) return true;
            return false;
            
            
        }
        
    }
    
    private class CollectionSearchConfiguration extends CollectionSeriesSearchConfiguration{
        
        public CollectionSearchConfiguration(){
            
            super(IdParam.COLLECTION);
            
        }
        
        @Override
        public ArrayList<SolrField> getFacetFields(){
            
            ArrayList<SolrField> fields = new ArrayList<SolrField>();
            
            // it is not possible to navigate by APIS collection
            // if a DDbDP/HGV series has already been set
            
            if(searchConfigurations.get(IdParam.SERIES).hasConstraint()) return fields;
            
            fields.add(SolrField.apis_series);
            
            return fields;
            
        }
        
        @Override
        public String getCSSSelector(){
            
            return "id-" + IdParam.COLLECTION.name().toLowerCase();
            
            
        }
        
        @Override
        public Boolean isDisabled(){
            
            if(this.getFacetFields().isEmpty()) return true;
            if(this.getIdValues().size() < 2) return true;
            VolumeSearchConfiguration vsc = (VolumeSearchConfiguration)searchConfigurations.get(IdParam.VOLUME);
            if(vsc.hasConstraint()) return true;
            return false;
            
            
        }
 
        
    }
    
    private abstract class VolumeIdnoSearchConfiguration implements SearchConfiguration{
       
        ArrayList<String> constraints;
        ArrayList<String> idValues;
        IdParam param;
        
        public VolumeIdnoSearchConfiguration(IdParam ip){
            
           constraints = new ArrayList<String>();
           idValues = new ArrayList<String>();
           param = ip;
            
        }
        
        @Override
        public void addConstraints(ArrayList<String> newValues){ 
            
            for(String newValue : newValues){
                
                if(!constraints.contains(newValue)) constraints.add(newValue);
                
            }
            
        
        }

        @Override
        public ArrayList<String> getConstraints() {
            
            return constraints;
            
            
        }

        @Override
        public abstract ArrayList<SolrField> getFacetFields();
        
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
            
            Collections.sort(idValues, new IdComparator());
            return idValues;
            
            
        }

        @Override
        abstract public String buildQueryClause();

        @Override
        public Boolean hasConstraint(){
            
            return constraints.size() > 0;
            
        }
        
        @Override
        public Boolean isDisabled(){
            
            return this.getFacetFields().size() < 1;
            
        }
        
    } 
    
    private class VolumeSearchConfiguration extends VolumeIdnoSearchConfiguration{
        
        public VolumeSearchConfiguration(){
            
            super(IdParam.VOLUME);
            
        }
        
        @Override
        public ArrayList<SolrField> getFacetFields(){
            
            ArrayList<SolrField> ff = new ArrayList<SolrField>();
            if(!searchConfigurations.get(IdParam.SERIES).hasConstraint()) return ff;
            ff.add(SolrField.ddbdp_volume);
            ff.add(SolrField.hgv_volume);
            return ff;
            
            
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
            if(this.isDisabled()) html.append(" class=\"vol-disabled\" value=\"n.a.\" disabled");
            html.append("/>");
            
            html.append("</p>");
            
            if(this.getFacetFields().size() > 0){

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
        public void addIdValues(SolrField solrField, ArrayList<Count> results) {
            
            Boolean seriesRestriction = false;
            
            SeriesSearchConfiguration ssc = (SeriesSearchConfiguration)searchConfigurations.get(IdParam.SERIES);
            if(ssc.hasConstraint()){
                
                ArrayList<String> seriesConstraints = ssc.getConstraints();
                
                ArrayList<SolrField> relFields = new ArrayList<SolrField>();
                
                Iterator<String> scit = seriesConstraints.iterator();
                while(scit.hasNext()){
                    try{                   

                        SolrField seriesField = ssc.getFieldFromCount(scit.next());
                        if(seriesField.equals(SolrField.ddbdp_series)) relFields.add(SolrField.ddbdp_volume);
                        if(seriesField.equals(SolrField.hgv_series)) relFields.add(SolrField.hgv_volume);
                    
                    } catch (FieldNotFoundException fnfe){ 
                        
                        System.out.println(fnfe.getMessage()); 
                    
                    }
                }
                
               seriesRestriction = true;
               if(relFields.contains(solrField)) seriesRestriction = false;
                
            }
            
            if(!seriesRestriction){
                for(Count result : results){

                    String name = result.getName();
                    long number = result.getCount();

                    if(name != null && !idValues.contains(name) && !name.equals("null") && !name.equals("0") && number != 0){

                        idValues.add(name);

                    }

                }
                
            }
            
        }
        
        @Override
        public String buildQueryClause(){
            
            String queryString = "";

            ArrayList<String> volumeConstraints = this.getConstraints();
                            
            Iterator<String> vcit = volumeConstraints.iterator();

            while(vcit.hasNext()){

                String vConstr = vcit.next();

                queryString += "(";

                queryString += SolrField.ddbdp_volume.name() + ":" + vConstr;
                queryString += " OR ";
                queryString += SolrField.hgv_volume.name() + ":" + vConstr;

                queryString += ")";
                if(vcit.hasNext()) queryString += " AND ";

            }

            return queryString;
    

        }
        
        @Override
        public String getCSSSelector(){
            
            return "id-" + IdParam.VOLUME.name().toLowerCase();
            
        }
        
        @Override
        public Boolean isDisabled(){
            
            CollectionSearchConfiguration csc = (CollectionSearchConfiguration)searchConfigurations.get(IdParam.COLLECTION);
            SeriesSearchConfiguration ssc = (SeriesSearchConfiguration)searchConfigurations.get(IdParam.SERIES);
            if(csc.hasConstraint()) return true;
            if(ssc.hasConstraint() && this.getIdValuesAsHTML().size() < 1) return true;
            return false;
            
        }
        
    }
    
    private class IdnoSearchConfiguration extends VolumeIdnoSearchConfiguration{
        
        public IdnoSearchConfiguration(){
            
            super(IdParam.IDNO);
            
        }
        
        @Override
        public ArrayList<SolrField> getFacetFields(){
            
            ArrayList<SolrField> ff = new ArrayList<SolrField>();
            
            SearchConfiguration collSc = searchConfigurations.get(IdParam.COLLECTION);
            SearchConfiguration serSc = searchConfigurations.get(IdParam.SERIES);
            SearchConfiguration volSc = searchConfigurations.get(IdParam.VOLUME);
            
            if(!collSc.hasConstraint() && !serSc.hasConstraint() && !volSc.hasConstraint()) return ff;
            
           if(collSc.hasConstraint()){
               
               ff.add(SolrField.apis_full_identifier);
               ff.add(SolrField.apis_inventory);
               ff.add(SolrField.apis_publication_id);
               
           }
           else{
               
               ff.add(SolrField.ddbdp_full_identifier);
               ff.add(SolrField.hgv_full_identifier);
               
           }
            return ff;    
            
        }
        
        @Override
        public void addIdValues(SolrField solrField, ArrayList<Count> results) {
            
            Boolean seriesRestriction = false;
            
            SeriesSearchConfiguration ssc = (SeriesSearchConfiguration)searchConfigurations.get(IdParam.SERIES);
            
            if(ssc.hasConstraint()){
                
                ArrayList<String> seriesConstraints = ssc.getConstraints();
                
                ArrayList<SolrField> relFields = new ArrayList<SolrField>();
                
                Iterator<String> scit = seriesConstraints.iterator();
                while(scit.hasNext()){
                    try{                   

                        SolrField seriesField = ssc.getFieldFromCount(scit.next());
                        if(seriesField.equals(SolrField.ddbdp_series)) relFields.add(SolrField.ddbdp_full_identifier);
                        if(seriesField.equals(SolrField.hgv_series)) relFields.add(SolrField.hgv_full_identifier);
                    
                    } catch (FieldNotFoundException fnfe){ 
                        
                        System.out.println(fnfe.getMessage()); 
                    
                    }
                }
                
               seriesRestriction = true;
               if(relFields.contains(solrField)) seriesRestriction = false;
                
            }
            
            if(!seriesRestriction){
                for(Count result : results){

                    String name = result.getName();
                    long number = result.getCount();

                    if(name != null && !idValues.contains(name) && !name.equals("null") && !name.equals("0") && number != 0){

                        idValues.add(name);

                    }

                }
                
            }
            
        }
        
        @Override
        public String buildQueryClause(){
            
            String queryString = "";
            
            SearchConfiguration collSc = searchConfigurations.get(IdParam.COLLECTION);
            SearchConfiguration serSc = searchConfigurations.get(IdParam.SERIES);
            SearchConfiguration volSc = searchConfigurations.get(IdParam.VOLUME);
                 
            Iterator<String> iit = this.getConstraints().iterator();
          
            if(collSc.hasConstraint()){  
                
                while(iit.hasNext()){
                    
                    String idConst = iit.next();
                    
                    queryString += "(";
                    queryString += SolrField.apis_full_identifier.name() + ":" + idConst;
                    queryString += " OR ";
                    queryString += SolrField.apis_inventory.name() + ":" + idConst;
                    queryString += " OR ";
                    queryString += SolrField.apis_publication_id.name() + ":" + idConst;
                    queryString += ")";
                    if(iit.hasNext()) queryString += " AND ";
                    
                }
                                      
            }
            else if(serSc.hasConstraint() || volSc.hasConstraint()){
                                
                while(iit.hasNext()){
                    
                    String constr = iit.next();
                    
                    queryString += "(";
                    queryString += SolrField.ddbdp_full_identifier.name() + ":" + constr;
                    queryString += " OR ";
                    queryString += SolrField.hgv_full_identifier.name() + ":" + constr;
                    queryString += ")";
                    if(iit.hasNext()) queryString += " AND ";
                    
                }
                
            }
            else{       // no constraints
                
                
                while(iit.hasNext()){
                    
                    String constr = iit.next();

                    queryString += "(";
                    queryString += SolrField.apis_full_identifier.name() + ":" + constr;
                    queryString += " OR ";
                    queryString += SolrField.apis_inventory.name() + ":" + constr;
                    queryString += " OR ";
                    queryString += SolrField.apis_publication_id.name() + ":" + constr;
                    queryString += " OR ";
                    queryString += SolrField.ddbdp_full_identifier.name() + ":" + constr;
                    queryString += " OR ";
                    queryString += SolrField.hgv_full_identifier.name() + ":" + constr;
                    queryString += ")";
                    if(iit.hasNext()) queryString += ")";
                    
                    
                }
                
                
            }

            return queryString;
            
        }
        
        @Override
        public String getCSSSelector(){
            
            return "id-" + IdParam.IDNO.name().toLowerCase();
            
            
        }
        
    }
    
    
}
