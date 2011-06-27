package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import org.apache.solr.client.solrj.SolrQuery;

/**
 *
 * @author thill
 */
public class LanguageFacet extends Facet{

    public LanguageFacet(){
        
        super(SolrField.language, FacetBrowser.FacetMapping.LANG);
        
    }
    
    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String generateHTML() {
        
        StringBuffer html = new StringBuffer("<h2>Language Facet here</h2>");
        
        return html.toString();
        
        
    }
    
}
