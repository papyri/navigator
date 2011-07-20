package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.LanguageCode;
import info.papyri.dispatch.browse.SolrField;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

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
    String getToolTipText() {
        
        return "The language the text is in.";
        
    }
    
}
