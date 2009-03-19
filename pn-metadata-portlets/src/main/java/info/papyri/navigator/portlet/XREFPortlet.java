package info.papyri.navigator.portlet;

import info.papyri.index.LuceneIndex;
import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.NamespacePrefixes;

import java.io.IOException;
import java.util.Properties;

import javax.naming.CompoundName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;


public class XREFPortlet extends GenericPortlet {
    private static final Logger LOG = Logger.getLogger(XREFPortlet.class);
    public static final String DOC_ATTR = "apis.portlet.apis.document";
    private static final Term ID_TEMPLATE = new Term(CoreMetadataFields.DOC_ID,"");
//    public final static String DDBDP_DEFAULT = "http://www.perseus.tufts.edu/hopper/collection.jsp?collection=Perseus:collection:DDBDP";
    public final static String DDBDP_DEFAULT = "/ddbdp/search";
    public final static String LDAB_DEFAULT = "http://ldab.arts.kuleuven.ac.be/ldab_text.php";
    public final static String APIS_DEFAULT = "http://www.columbia.edu/cgi-bin/cul/resolve?ATK2059";
    public final static String HGV_DEFAULT = "http://www.rzuser.uni-heidelberg.de/~gv0/gvz.html";
    
//    final static String DDBDP =
//        "http://www.perseus.tufts.edu/hopper/text.jsp?doc=Perseus:text:1999.05.QQserQQ:volume=QQvolQQ:document=QQdocQQ";
    final static String DDBDP =
        "/ddbdp/doc?identifier=";
    final static String DDBDP_NO_PREFIX =
        "/ddbdp/doc?identifier=oai:papyri.info:identifiers:ddbdp:";
    public static String getDDBDPlink(String f){
        if (f == null) return DDBDP_DEFAULT;
        if(f.startsWith(NamespacePrefixes.DDBDP)){
            return DDBDP + f;
        }
        String [] vals = f.split(";");
        if (vals.length < 2) return DDBDP_DEFAULT;
        if(vals.length<3){
            f = vals[0] + "::" + vals[1];
        }
        else{
            f = vals[0] + ":" + vals[1] + ":" + vals[2];
        }
        return DDBDP_NO_PREFIX + f;
    }
     final static String LDAB =
        "http://ldab.arts.kuleuven.ac.be/ldab_text_detail.php?tm=QQqueryQQ";
     public static String getLDABlink(String f){
         if (f == null || !f.startsWith(NamespacePrefixes.TM)) return "";
          String val = f.substring(NamespacePrefixes.TM.length());
          return LDAB.replace("QQqueryQQ",val);
    }
     final static String APIS =
        "http://wwwapp.cc.columbia.edu/ldpd/app/apis/search?mode=search&amp;apisnum_inst=QQinstQQ&amp;apisnum_num=QQnumQQ&amp;sort=date&amp;resPerPage=25&amp;action=search&amp;p=1";
     public static String getAPISlink(String f){
        if (f == null || !f.startsWith(NamespacePrefixes.APIS)) return "";
        String collection = getAPISCollection(f);
        String number = f.substring(f.indexOf(':',NamespacePrefixes.APIS.length())+1);
        return APIS.replace("QQinstQQ",collection).replace("QQnumQQ",number);
    }
     final static String HGV =
        "http://aquila.papy.uni-heidelberg.de/Hauptregister/FMPro?-db=hauptregister_&amp;TM_Nr.=QQqueryQQ&amp;-format=DTableVw.htm&amp;-lay=Liste&amp;-find";
     public static String getHGVlink(String f){
         if (f == null || (!f.startsWith(NamespacePrefixes.TM) && !f.startsWith(NamespacePrefixes.HGV))) return "";
         if(f.startsWith(NamespacePrefixes.HGV)){
            if(LuceneIndex.SEARCH_HGV == null) return "";
            try{
                Hits hits = LuceneIndex.SEARCH_HGV.search(new TermQuery(ID_TEMPLATE.createTerm(f)));
                if(hits.length()==0) return "";
                String [] xrefs = hits.doc(0).getValues(CoreMetadataFields.XREFS);
                if(xrefs == null) return "";
                boolean found = false;
                for(String xref:xrefs){
                    if(xref.startsWith(NamespacePrefixes.TM)){
                        found = true;
                        f = xref;
                        break;
                    }
                }
                if(!found) return "";
            }
            catch(Throwable t){
                return "";
            }
            
        }
        String val = f.substring(NamespacePrefixes.TM.length());
        return HGV.replace("QQqueryQQ",val);
    }
     
    protected String jsp;
    
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

        String apisId = request.getParameter("controlName").trim();

        if (LuceneIndex.SEARCH_XREF != null && apisId != null){
                request.setAttribute(NavigatorPortlet.DOC_ATTR, getDocumentByControlName(apisId));
            //INDEX.
            PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(jsp);
            rd.include(request, response);
        }
        else {
            PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/indexing.jsp");
            rd.include(request, response);
        }
    
    }
    
    public void init(PortletConfig config) throws PortletException {
        try{
            super.init(config);
        }
        catch (Throwable t){
            t.printStackTrace();
        }
        this.jsp = "/WEB-INF/xref.jsp"; 
    }
    
    public static Document getDocumentByControlName(String name, String collection){
        return getDocumentByControlName(name);
    }
    public static Document getDocumentByControlName(String name){
        if(LOG.isDebugEnabled())LOG.debug("getDocumentByControlName( \"" + name + "\" )");
        name = name.trim().replaceAll("\\s+", "%20");
        if (LuceneIndex.SEARCH_XREF != null){
            try{
                Query query = new TermQuery(new Term(CoreMetadataFields.DOC_ID,name));
                Hits hits = LuceneIndex.SEARCH_XREF.search(query);
                int count = hits.length();
                if (count > 0){
                    return ((org.apache.lucene.search.Hit)hits.iterator().next()).getDocument();
                }
                query = new TermQuery(new Term(CoreMetadataFields.XREFS,name));
                hits = LuceneIndex.SEARCH_XREF.search(query);
                count = hits.length();
                if (count > 0){
                    return ((org.apache.lucene.search.Hit)hits.iterator().next()).getDocument();
                }
                LOG.error("DOC_ID and XREFS queries had no matches (name: " + name + ")");
            }
            catch (IOException ioe){
                ioe.printStackTrace();
                return null;
            }
        }
        return new Document();
    }
    
    public static String getAPISCollection(String apisId){
        boolean apis = (apisId.startsWith(NamespacePrefixes.APIS));
        if(apis){
            int ix = apisId.indexOf(':',NamespacePrefixes.APIS.length());
            if(ix==-1) return null;
            else{
                return apisId.substring(NamespacePrefixes.APIS.length(),ix);
            }
        } else return null;
    }
    
    public static final Name getName(String cn) throws InvalidNameException{
        Properties syntax = new Properties();
        syntax.put("jndi.syntax.direction", "left_to_right");
        syntax.put("jndi.syntax.separator", ":");
        return new CompoundName(cn,syntax);
    }
    
    public static final String getDisplay(String id){
        if(id==null) return null;
        if(id.startsWith(NamespacePrefixes.APIS)){
            String display = id.substring(NamespacePrefixes.APIS.length()).replace(":", ".apis.");
            return display;
        }
        if(id.startsWith(NamespacePrefixes.INV)){
            String display = id.substring(NamespacePrefixes.INV.length()).replace(':',' ').replace("%20", " ");
            return display;
        }
        if(id.startsWith(NamespacePrefixes.HGV)){
            String display = id.substring(NamespacePrefixes.HGV.length()).replace("%20", " ").replace(':', ' ');
            return display;
        }
        if(id.startsWith(NamespacePrefixes.DDBDP)){
            String display = id.substring(NamespacePrefixes.DDBDP.length());
            return display.replace(':',';');
        }
        if(id.startsWith(NamespacePrefixes.TM)){
            String display = id.substring(NamespacePrefixes.TM.length());
            return display;
        }
        return id;
    }
    
}