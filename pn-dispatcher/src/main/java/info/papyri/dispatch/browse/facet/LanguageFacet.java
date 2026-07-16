package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.LanguageCode;
import info.papyri.dispatch.browse.SolrField;
import info.papyri.dispatch.ServletUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * <code>Facet</code> for selection by language
 *
 * @author thill
 */
public class LanguageFacet extends Facet{
    
    
    public LanguageFacet(){
        
        super(SolrField.facet_language, FacetParam.LANG, "Language");
        
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
        
        languageCode = LanguageCode.filterModernLanguageCodes(languageCode);
        String displayValue;
        
        try{
            
            String swappedLanguageCode = ServletUtils.scrub(languageCode).replaceAll("-", "_");
            LanguageCode lang = LanguageCode.valueOf(swappedLanguageCode);
            displayValue = lang.expanded();
            
        } 
        catch(IllegalArgumentException iae){
            
            displayValue = ServletUtils.scrub(languageCode);
            
        }
      
        return displayValue;
        
    }
    
    @Override
    public void setWidgetValues(QueryResponse queryResponse){
        
        FacetField facetField = queryResponse.getFacetField(field.name());
        valuesAndCounts = new ArrayList<Count>();
        List<Count> unfiltered = facetField.getValues();
        Iterator<Count> cit = unfiltered.iterator();
        while(cit.hasNext()){
            
            Count count = cit.next();            
            if(count.getName() != null && !count.getName().equals("") && count.getCount() > 0 && !count.getName().equals("null") && !LanguageCode.modernLanguageCodes.contains(count.getName())) valuesAndCounts.add(count);
            
        }
        
        Collections.sort(valuesAndCounts, new Comparator(){

            @Override
            public int compare(Object t, Object t1) {
                
                Count count1 = (Count) t;
                Count count2 = (Count) t1;
                
                if(count1.getCount() < count2.getCount()) return 1;
                if(count1.getCount() > count2.getCount()) return -1;
                return 0;
                 
                
            }
        
        
        });
        
        
          
    } 
    
    @Override
    String getToolTipText() {
        
        return "The language the text is in.";
        
    }
    
}
