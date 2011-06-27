package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import org.apache.solr.client.solrj.SolrQuery;

/**
 *
 * @author thill
 */
public class HasImagesFacet extends Facet {

    public HasImagesFacet(){
    
        super(SolrField.images, FacetBrowser.FacetMapping.IMG);
    
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String generateHTML() {
        
        StringBuffer html = new StringBuffer("<h2>Images Facet here</h2>");
        
        return html.toString();
        
        
    }
    
}
