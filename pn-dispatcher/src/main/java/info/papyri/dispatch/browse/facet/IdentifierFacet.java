package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * @author thill
 */
public class IdentifierFacet extends Facet{

    enum IdParam{
        
        SERIES("Series", new ArrayList<SolrField>(Arrays.asList(SolrField.ddbdp_series, SolrField.hgv_series))),
        COLLECTION("Collection", new ArrayList<SolrField>(Arrays.asList(SolrField.apis_series))),
        VOLUME("Volume", new ArrayList<SolrField>(Arrays.asList(SolrField.ddbdp_volume, SolrField.hgv_volume))),
        IDNO("ID number", new ArrayList<SolrField>(Arrays.asList(SolrField.ddbdp_full_identifier, SolrField.hgv_full_identifier, SolrField.apis_full_identifier, SolrField.apis_inventory, SolrField.apis_publication_id)));
        
        private final String label;
        private final ArrayList<SolrField> fields;
        
        IdParam(String msg, ArrayList<SolrField> f){
        
            label = msg;
            fields = f;
        
        }
        
        public String getLabel(){ return label; }
        public String getSelector(){ return "id-" + this.name().toLowerCase(); }
        
    }
    
    private EnumMap<IdParam, SearchConfiguration> searchConfigurations;
       
    
    public IdentifierFacet(){
        
        super(SolrField.collection, FacetParam.IDENTIFIER, "Get documents by id");     
        searchConfigurations = new EnumMap<IdParam, SearchConfiguration>(IdParam.class);
        for(IdParam ip : IdParam.values()){
            
            searchConfigurations.put(ip, new SearchConfiguration(ip));
            
        }
    }    
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        for(IdParam ip : IdParam.values()){
            
            ArrayList<SolrField> fields = searchConfigurations.get(ip).getFields();
            for(SolrField ipField : fields){ 
                
                if(ip.equals(IdParam.COLLECTION) || ip.equals(IdParam.SERIES)){
                
                    solrQuery.addFacetField(ipField.name()); 
                    
                }
                else{ 
                    
                   if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint() || searchConfigurations.get(IdParam.SERIES).hasConstraint()){
                       
                       solrQuery.addFacetField(ipField.name());
                       
                   }
                    
                }
            
            } 
            String luceneQueryString = searchConfigurations.get(ip).concatenateToLQL();
            if(!"".equals(luceneQueryString)) solrQuery.addFilterQuery(luceneQueryString);
        }
                
        return solrQuery;
        
    }
    
    @Override
    public void setWidgetValues(QueryResponse queryResponse){

        for(IdParam ip : IdParam.values()){
        
            ArrayList<Count> allCounts = new ArrayList<Count>();
            
            ArrayList<SolrField> fields = searchConfigurations.get(ip).getFields();
            
            for(SolrField ipField : fields){
                
                FacetField ff = queryResponse.getFacetField(ipField.name());
                
                if(ff != null){
                    
                    allCounts.addAll(ff.getValues());
                    
                }
                
            }
         
            searchConfigurations.get(ip).setIdValues(allCounts);
            
        }
        
    }
    
    @Override 
    public String generateWidget(){
        
        // define some snippets to save typing
        
        String lbo = "<label for=\"";
        String lbe = "</label>";
        String sls = "<select name=\"";
        String slm = "\" id=\"";
        String cl = "\">";
        String sle = "</select>";
        String dis = " disabled";
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" id=\"id-selector-wrapper\" title=\"");
        html.append(getToolTipText());
        html.append("\">");
        html.append(generateHiddenFields());
        html.append("<div id=\"series-coll-wrapper\">");
        html.append("<p id=\"series-wrapper\">");
        
        // series control
        
        SearchConfiguration seriesObj = searchConfigurations.get(IdParam.SERIES);
        SearchConfiguration collObj = searchConfigurations.get(IdParam.COLLECTION);
        SearchConfiguration volObj = searchConfigurations.get(IdParam.VOLUME);
        SearchConfiguration idObj = searchConfigurations.get(IdParam.IDNO);
        
        ArrayList<String> seriesOptionList = this.getOptions(IdParam.SERIES, seriesObj.getIdValues());
        
        html.append(lbo);
        html.append(IdParam.SERIES.name());
        html.append(cl);
        html.append(IdParam.SERIES.getLabel());
        html.append(lbe);
        html.append(sls);
        html.append(IdParam.SERIES.name());
        html.append(slm);
        html.append(IdParam.SERIES.getSelector());
        html.append("\"");
        if(collObj.hasConstraint() || seriesOptionList.size() == 1) html.append(dis);
        html.append(">");
        
        html.append(this.stringifyOptionList(seriesOptionList));
        
        html.append(sle);
        html.append("</p><!-- closing #series-wrapper -->");
        html.append("<p class=\"coll-or\">or</p>");

        // collection control
        
        ArrayList<String> collectionOptionList = this.getOptions(IdParam.COLLECTION, collObj.getIdValues());
        
        html.append("<p id=\"collection-wrapper\">");
        html.append(lbo);
        html.append(IdParam.COLLECTION.name());
        html.append(cl);
        html.append(IdParam.COLLECTION.getLabel());
        html.append(lbe);
        html.append(sls);
        html.append(IdParam.COLLECTION.name());
        html.append(slm);
        html.append(IdParam.COLLECTION.getSelector());
        html.append("\"");
        if(seriesObj.hasConstraint() || collectionOptionList.size() == 1) html.append(dis);
        html.append(">");
        
        html.append(this.stringifyOptionList(collectionOptionList));
        
        html.append(sle);
        html.append("</p><!-- closing #collection-wrapper -->");
        html.append("</div><!-- closing #series-coll-wrapper -->");
        
        html.append("<div id=\"vol-idno-wrapper\">");
        
        // volume control
        
        ArrayList<String> volumeOptionList = this.getOptions(IdParam.VOLUME, volObj.getIdValues());
        
        html.append("<p id=\"vol-wrapper\">");
        html.append(lbo);
        html.append(IdParam.VOLUME.name());
        html.append(cl);
        html.append(IdParam.VOLUME.getLabel());
        html.append(lbe);
        html.append(sls);
        html.append(IdParam.VOLUME.name());
        html.append(slm);
        html.append(IdParam.VOLUME.getSelector());
        html.append("\"");
        if((!collObj.hasConstraint() && !seriesObj.hasConstraint()) || volumeOptionList.size() == 1) html.append(dis);
        html.append(">");
        html.append(volumeOptionList);
        html.append(sle);
        html.append("</p><!-- closing #vol-wrapper -->");
        
        html.append("<p id=\"idno-wrapper\">");
        html.append(lbo);
        html.append(IdParam.IDNO.name());
        html.append(cl);
        html.append(IdParam.IDNO.getLabel());
        html.append(lbe);

        // implement as selector right now, but move to autocomplete later?
        
        ArrayList<String> idnoOptionList = this.getOptions(IdParam.IDNO, idObj.getIdValues());
        html.append(sls);
        html.append(IdParam.IDNO.name());
        html.append(slm);
        html.append(IdParam.IDNO.getSelector());
        html.append("\"");
        if((!seriesObj.hasConstraint() && !idObj.hasConstraint()) || idnoOptionList.size() == 1) html.append(dis);
        html.append(">");       
        html.append(this.stringifyOptionList(idnoOptionList));
        html.append(sle);
        html.append("</p><!-- closing #idno-wrapper -->");
        html.append("</div><!-- closing #vol-idno-wrapper -->");
        html.append("</div><!-- closing identifier facet widget -->");
        return html.toString();
        
    }
    
    private ArrayList<String> getOptions(IdParam ip, ArrayList<Count> counts){
        
        String cl = "\">";
        String opo = "<option value=\"";
        String ope = "</option>";
        
        ArrayList<String> optionList = new ArrayList<String>();
        
        Boolean disabled = false;
        
        SearchConfiguration seriesObj = searchConfigurations.get(IdParam.SERIES);
        SearchConfiguration collObj = searchConfigurations.get(IdParam.COLLECTION);
        if(ip.equals(IdParam.VOLUME) && (!seriesObj.hasConstraint() && !collObj.hasConstraint())) disabled = true;
        if(ip.equals(IdParam.IDNO) && (!seriesObj.hasConstraint() && !collObj.hasConstraint())) disabled = true;
        if(ip.equals(IdParam.COLLECTION) && seriesObj.hasConstraint()) disabled = true;
        if(ip.equals(IdParam.SERIES) && collObj.hasConstraint()) disabled = true;
        
        if(!disabled){
            
            for(Count count : counts){

                String name = count.getName();
                String number = String.valueOf(count.getCount());
                String option = "";

                if(name != null && !"".equals(name) && !"0".equals(number) && !"0".equals(name)){

                    option += opo;
                    option += count.getName();
                    option += cl;
                    option += count.getName();
                    option += " (";
                    option += String.valueOf(count.getCount());
                    option += ")";
                    option += ope;
                    optionList.add(option);

                }

            }
                        
        }
        
        if(optionList.size() != 1){
            
            String def = "<option value=\"default\">" + Facet.defaultValue + "</option>";
            optionList.add(0, def);
            
        }
        
        return optionList;
        
    }
    
    private String stringifyOptionList(ArrayList<String> options){
       
        StringBuilder b = new StringBuilder();
        
        for(String option : options){
            
            b.append(option);
            
        }
        
        return b.toString();
       
        
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
    
    private class SearchConfiguration{
    
        private final IdParam param;
        private ArrayList<String> constraints;
        private ArrayList<Count> idValues;
        private final ArrayList<SolrField> apisVolumeFields = new ArrayList<SolrField>();
        private final ArrayList<SolrField> apisIdnoFields = new ArrayList<SolrField>(Arrays.asList(SolrField.apis_inventory, SolrField.apis_publication_id, SolrField.apis_full_identifier));
        private final ArrayList<SolrField> ddbHgvIdnoFields = new ArrayList<SolrField>(Arrays.asList(SolrField.ddbdp_full_identifier, SolrField.hgv_full_identifier));
        private final ArrayList<SolrField> collectionFields = new ArrayList<SolrField>(Arrays.asList(SolrField.apis_series));
        private final ArrayList<SolrField> seriesFields = new ArrayList<SolrField>(Arrays.asList(SolrField.hgv_series, SolrField.ddbdp_series));
        private final ArrayList<SolrField> volumeFields = new ArrayList<SolrField>(Arrays.asList(SolrField.hgv_volume, SolrField.ddbdp_volume));
            
        public SearchConfiguration(IdParam idp){
            
            param = idp;
            constraints = new ArrayList<String>();
            idValues = new ArrayList<Count>();
            
        }
    
        public void addConstraints(ArrayList<String> newValues){ constraints = newValues; }
        public ArrayList<String> getConstraints(){ return constraints; }
        public ArrayList<Count> getIdValues(){ return idValues; }       
        public void setIdValues(ArrayList<Count> counts){ idValues = counts; }
        public Boolean hasConstraint(){ return constraints.size() > 0; }    
        public ArrayList<SolrField> getFields(){
            
            if(param.equals(IdParam.VOLUME) && searchConfigurations.get(IdParam.COLLECTION).hasConstraint()) return apisVolumeFields;
            
            if(param.equals(IdParam.IDNO)){
                
                if(searchConfigurations.get(IdParam.COLLECTION).hasConstraint()){
                    
                    return apisIdnoFields;
                    
                }
                else{
                    
                    return ddbHgvIdnoFields;
                    
                }            
                
            }
            
            if(param.equals(IdParam.COLLECTION)) return collectionFields;
            if(param.equals(IdParam.SERIES)) return seriesFields;
            if(param.equals(IdParam.VOLUME)) return volumeFields;
            
            // TODO: throw error here
            
            return null; 
            
        }
        public String concatenateToLQL(){

            String lql = "";

            Iterator<String> cit = constraints.iterator();

            while(cit.hasNext()){

                String constraint = cit.next();
                lql += "(";

                Iterator<SolrField> fit = this.getFields().iterator();

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

    }
    
    
}
