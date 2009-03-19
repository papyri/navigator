package info.papyri.navigator.portlet;

import info.papyri.index.LuceneIndex;
import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.NamespacePrefixes;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public abstract class NavigatorPortlet extends GenericPortlet {
    private static final Logger LOG = Logger.getLogger(NavigatorPortlet.class);
    public static final String DOC_ATTR = "apis.portlet.apis.document";
    private static final Term ID_TEMPLATE = new Term(CoreMetadataFields.DOC_ID,"");
    private static final Term XREF_TEMPLATE = new Term(CoreMetadataFields.XREFS,"");
    public static final String SORT_BY = "sortBy";
    public static final String XREF_RESULTS = "xref:results";
    public static final String XREF_NUM_RESULTS = "xref:numResults";
    public static final String XREF_REQ_URL = "xref:requestUrl";
    public static final String XREF_PAGE = "xref:page";
    public static final String XREF_QUERY_STRING = "xref:queryString";
    
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

        String cn = (request.getParameter("controlName") == null)?null:request.getParameter("controlName").trim();
        if (LOG.isDebugEnabled()){
            LOG.debug("NavigatorPortlet: cn = \"" + cn + "\"");
        }

            Document doc = getDocumentByControlName(cn);
            request.setAttribute(NavigatorPortlet.DOC_ATTR, doc);
            renderView(request, response);
    }
    
    abstract void renderView(RenderRequest request, RenderResponse response) throws PortletException, IOException;
    
  
    public static Document getDocumentByControlName(String name) throws IOException {
        name = name.trim().replaceAll("\\s+", "%20");
        IndexSearcher primary = null;
        if (name.startsWith(NamespacePrefixes.APIS)){
            primary = LuceneIndex.SEARCH_COL;
        }
        else if (name.startsWith(NamespacePrefixes.HGV)){
            primary = LuceneIndex.SEARCH_HGV;
        }
        else if (name.startsWith(NamespacePrefixes.ID_NS)){
            primary = LuceneIndex.SEARCH_HGV;
            name = new String(name.getBytes("ISO-8859-1"),"UTF-8");
            Hashtable env = new Hashtable();
                env.put(Context.URL_PKG_PREFIXES, "edu.columbia");
                try{
            Context c = NamingManager.getURLContext("apis", env);
            int ix = name.indexOf(':',NamespacePrefixes.ID_NS.length());
            if(ix == -1) return null;

            Name xName = XREFPortlet.getName(name);
            c = (Context)c.lookup(xName);
            if (c == null) return null;
            name = (String)c.lookup("hgv");
                }
                catch(NamingException ne){
                    ne.printStackTrace();
                    return null;
                }
        }
        if (primary == null || name == null) return null;
        Query query = new TermQuery(new Term(CoreMetadataFields.DOC_ID,name));
        try{Hits hits = primary.search(query);
        int count = hits.length();
        if (count > 1){
            LOG.warn("ID Query produced more than one hit [" + count + "] for " + name);
         }
        if (count > 0){
            return hits.doc(0);
        }
        return getNoPrimaryDocument();
        }
        catch (IOException ioe){
            ioe.printStackTrace();
            return null;
        }
        
    }
    
    private static Document getNoPrimaryDocument(){
        return new Document();
    }
    
}