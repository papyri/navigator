package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import org.apache.solr.client.solrj.SolrQuery;

/**
 *
 * @author thill
 */
public class HasTranslationFacet extends Facet {
    
    public HasTranslationFacet(){
        
        super(SolrField.has_translation, FacetBrowser.FacetMapping.TRANS);
        
    }

    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String generateHTML() {
        
        StringBuffer html = new StringBuffer("<h2>Translation Facet here</h2>");
        
        return html.toString();
        
        
    }
    
}
