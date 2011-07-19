package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.Iterator;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 * <code>BooleanFacet</code> regarding whether or not a translation is associated with a 
 * record.
 * 
 * @author thill
 */
public class HasTranslationFacet extends BooleanFacet {
    
    public HasTranslationFacet(){
        
        super(SolrField.has_translation, FacetParam.TRANSL.name(), "Has Translation");
        
    }
    
    @Override
    String getToolTipText() {
        
        return "Indicates whether or not a translation of the original text is available through the IDP interface.";
        
    }
    
}
