package info.papyri.dispatch.browse.facet;

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
        
        String firstChar = String.valueOf(value.charAt(0)).toUpperCase();
        String remainder = value.substring(1).toLowerCase();
        return firstChar + remainder;
        
        
    }
    
}
