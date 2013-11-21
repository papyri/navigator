package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.ServletUtils;
import info.papyri.dispatch.browse.SolrField;

/**
 * Facet allowing selection by nome
 * 
 * @author thill
 */
public class NomeFacet extends Facet {

    public NomeFacet(){
        
        super(SolrField.nome, FacetParam.NOME, "Nome");
        
    }
    
    @Override
    String getToolTipText() {
        
        return "Select documents by nome.";
        
    }
    
    @Override
    public String getDisplayValue(String value){
      
      StringBuilder result = new StringBuilder(ServletUtils.scrub(value));
      result.setCharAt(0, Character.toUpperCase(result.charAt(0)));
      return result.toString();
        
    }
    
}
