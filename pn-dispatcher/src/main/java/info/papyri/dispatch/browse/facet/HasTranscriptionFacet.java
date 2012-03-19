package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * <code>Facet</code> regarding whether or not a transcription is associated with a 
 * record.
 * 
 * Note that, although this <code>Facet</code> is logically and in appearance a 
 * <code>BooleanFacet</code>, the transcription field is not in fact a Boolean one.
 * This <code>Facet</code> accordingly tests whether or not the field has a value 
 * associated with it at all, treating absence of a value as 'false' and presence as
 * 'true'. It might thus be termed a 'pseudo-Boolean'.
 * 
 * @author thill
 */
public class HasTranscriptionFacet extends BooleanFacet {
    
    public HasTranscriptionFacet(){
        
        super(SolrField.has_transcription, FacetParam.TRANSC, "Has Transcription");
        
    }

    
    @Override
    String getToolTipText() {
        
        return "Indicates whether or not a transcription of the original text is available through the IDP interface.";
        
    }
    
}
