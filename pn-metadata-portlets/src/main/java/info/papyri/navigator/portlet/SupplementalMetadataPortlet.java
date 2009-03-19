package info.papyri.navigator.portlet;

import info.papyri.index.LuceneIndex;
import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.NamespacePrefixes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class SupplementalMetadataPortlet extends NavigatorPortlet {
    final static Logger LOG = Logger.getLogger(SupplementalMetadataPortlet.class);
    public static final String DOC_ATTR = "apis.portlet.hgv.document";

	public void init() throws PortletException {
		super.init();
	}
    
    

	@Override
    void renderView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        String docId = request.getParameter("controlName").trim();
        boolean supplemental = false;
        if (LOG.isDebugEnabled()) LOG.debug("supplemental: " + docId);
        if (docId.startsWith(NamespacePrefixes.HGV)){
            supplemental = true;
            LOG.debug("HGV controlName, supplements in APIS");
        }
        else if (docId.startsWith(NamespacePrefixes.APIS)){
            supplemental = true;
            LOG.debug("APIS controlName, supplements in HV");
        }
        else if(docId.startsWith(NamespacePrefixes.DDBDP)){
            supplemental = true;
            LOG.debug("DDB controlName, supplements in APIS");
        }
        else if(docId.startsWith(NamespacePrefixes.TM)){
            supplemental = true;
            LOG.debug("TM controlName, supplements in APIS");
        }
        PortletPreferences prefs = request.getPreferences();
        String jsp = "supp.jsp";
        if (prefs != null){
            jsp = request.getPreferences().getValue("apisDisplay", "supp.jsp");
        }
        jsp = "/WEB-INF/" + jsp;

        if (supplemental){
            if (docId != null) {
                Iterator<Document> hits = getSupplementalDocumentsByControlName(docId);
                if (!hits.hasNext()){
                    PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/supp-none.jsp");
                    rd.include(request, response);
                    return;
                }
                PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/supp-head.jsp");
                rd.include(request, response);
                while (hits.hasNext()){
                  Document next = hits.next();
                  request.setAttribute(SupplementalMetadataPortlet.DOC_ATTR, next);
                  rd = getPortletContext().getRequestDispatcher(jsp);
                  rd.include(request, response);
                }
                rd = getPortletContext().getRequestDispatcher("/WEB-INF/supp-foot.jsp");
                rd.include(request, response);
            }
            else {
                Exception e = new Exception("No document controlname specified for request");
                request.setAttribute("apis:exception",e);
                PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/exception.jsp");
                rd.include(request, response);
            }
        }
        else {
            PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/nohgv.jsp");
            rd.include(request, response);
        }
        
    }

	private static Iterator<Document> getSupplementalDocumentsByControlName(String name){
        name = name.trim().replaceAll("\\s+", "%20");
	    ArrayList<Document> result = new ArrayList<Document>();

	    IndexSearcher supplemental = null;
	    if (name.startsWith(NamespacePrefixes.HGV)){
	        supplemental = LuceneIndex.SEARCH_COL;
	    }
	    if (name.startsWith(NamespacePrefixes.APIS)){
	        supplemental = LuceneIndex.SEARCH_HGV;
	    }
        else if (name.startsWith(NamespacePrefixes.DDBDP)){
            supplemental = LuceneIndex.SEARCH_COL;
        }
        else if (name.startsWith(NamespacePrefixes.TM)){
            supplemental = LuceneIndex.SEARCH_COL;
        }
        try{
	        Hits xrefHits = null;
	        Query query = new TermQuery(new Term(CoreMetadataFields.XREFS,name));
            if (LOG.isDebugEnabled()){
                LOG.debug(SupplementalMetadataPortlet.class.getName() + " " + query);
            }
	        if (LuceneIndex.SEARCH_XREF != null){
	            try{
	                xrefHits = LuceneIndex.SEARCH_XREF.search(query);
	            }
	            catch (IOException ioe){
	                ioe.printStackTrace();
	            }
	        }
	        else{
	            throw new Exception("XREF index unavailable"); 
	        }
	        Iterator<Hit> hits = xrefHits.iterator();
	        while (xrefHits != null && hits.hasNext()){
	            Hit hit = hits.next();
	            Document xref = hit.getDocument();
	            if (xref != null){        
	                String [] suppIds = xref.getValues(CoreMetadataFields.XREFS);
	                if (suppIds == null) suppIds = new String[0];
	                for (String suppId :suppIds){
	                    query = new TermQuery(new Term(CoreMetadataFields.DOC_ID,suppId));
                        LOG.debug(query.toString());
                        if (supplemental != null){
	                        try{
	                            Hits supHits = supplemental.search(query);
	                            int count = supHits.length();
                                if (LOG.isDebugEnabled()) LOG.debug("getSupplementalDocumentsByControlName : " + count + " hits");
	                            if (count > 0){
	                                result.add(supHits.doc(0));
	                            }
	                        }
	                        catch (IOException ioe){
	                            ioe.printStackTrace();
	                        }
	                    }
	                }
	            }
	        }
	        return result.iterator();
	    }
	    catch (Exception e){
	        e.printStackTrace();
	        return result.iterator();
	    }
	}


	public void init(PortletConfig arg0) throws PortletException {
	    super.init(arg0);
	}
}
