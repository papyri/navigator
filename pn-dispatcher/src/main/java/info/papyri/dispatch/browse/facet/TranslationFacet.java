package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.LanguageCode;
import info.papyri.dispatch.browse.SolrField;

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
    String getToolTipText() {
        
        return "Languages into which a text has been translated.";
        
    }
    
    
}
