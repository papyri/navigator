package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.LanguageCode;
import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * @author thill
 */
public class TranslationFacet extends Facet {
    
    public TranslationFacet(){
        
        super(SolrField.translation_language, FacetParam.TRANSL, "Translation language");
        
    }
    
       
    /**
     * Transforms the BCP 47 language tagged used for internal representation into 
     * a readily human-readable form
     * 
     * @param languageCode
     * @return 
     * @see LanguageCode
     */
    
    @Override
    public String getDisplayValue(String languageCode){
        
        String displayValue = "";
        
        try{
            
            String swappedLanguageCode = languageCode.replaceAll("-", "_");
            LanguageCode lang = LanguageCode.valueOf(swappedLanguageCode);
            displayValue = lang.expanded();
            
        } 
        catch(IllegalArgumentException iae){
            
            displayValue = languageCode;
            
        }
      
        return displayValue;
        
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){
        
        solrQuery.addFacetField(field.name());
        solrQuery.setFacetLimit(-1);
        
        Iterator<String> cit = facetConstraints.iterator();
        
        while(cit.hasNext()){
            
            String fq = cit.next();
            if(fq.equals("None")){
                
                fq = "-" + field.name() + ":[* TO *]";
                
            }
            else{
                
                // slash-escape madness: java, solr, and java.regex all use backslash
                // as an escape character
                fq = fq.replaceAll("\\\\", "\\\\\\\\");
                if(fq.contains(" ")) fq = "\"" + fq + "\"";
                fq = field.name() + ":" + fq;
            
            }
            
            solrQuery.addFilterQuery(fq);

            
        }
        
        return solrQuery;
        
    }
    

    public void setWidgetValues(QueryResponse queryResponse){
        
        FacetField facetField = queryResponse.getFacetField(field.name());
        valuesAndCounts = new ArrayList<Count>();
        List<Count> unfiltered = facetField.getValues();
        Iterator<Count> cit = unfiltered.iterator();
        long total = 0;
        while(cit.hasNext()){
            
            Count count = cit.next();            
            if(count.getName() != null && !count.getName().equals("") && count.getCount() > 0 && !count.getName().equals("null")){
                
                valuesAndCounts.add(count);
                total += count.getCount();
            }
            
        }
        
        long noTranslationTotal = queryResponse.getResults().getNumFound() - total;
        
        if(noTranslationTotal > 0){
            
            valuesAndCounts.add(new Count(new FacetField(SolrField.translation_language.name()), "None", noTranslationTotal));
         
        }
    }
    
    @Override
    String getToolTipText() {
        
        return "Languages into which a text has been translated.";
        
    }
    
    
}
