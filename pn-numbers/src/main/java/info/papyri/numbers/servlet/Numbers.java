package info.papyri.numbers.servlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;

import sun.rmi.runtime.GetThreadPoolAction;

import info.papyri.metadata.*;
public class Numbers extends HttpServlet {
    public static final String ATTR_BASE_URI = "papyri.info:baseURI";
    public static final String ATTR_BASE_URL = "papyri.info:baseURL";
    public static final String ATTR_OFFSET_URL = "papyri.info:offsetURL";
    public static final String ATTR_TITLE = "papyri.info:title";
    public static final String ATTR_PARTS = "papyri.info:numbers:parts";
    public static final String ATTR_AGGREGATES = "papyri.info:numbers:aggregates";
    private static final String COLON_PLUS = new String(new char[]{(':'+1)});
    
    public final static String DDBDP_DEFAULT = "/ddbdp/search";
    public final static String LDAB_DEFAULT = "http://ldab.arts.kuleuven.ac.be/ldab_text.php";
    public final static String APIS_DEFAULT = "http://www.columbia.edu/cgi-bin/cul/resolve?ATK2059";
    public final static String HGV_DEFAULT = "http://www.rzuser.uni-heidelberg.de/~gv0/gvz.html";
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

     
     final static String HGV =
        "http://aquila.papy.uni-heidelberg.de/Hauptregister/FMPro?-db=hauptregister_&amp;TM_Nr.=QQqueryQQ&amp;-format=DTableVw.htm&amp;-lay=Liste&amp;-find";
     public static String getHGVlinkFromTM(String f){
         if (f == null || (!f.startsWith(NamespacePrefixes.TM))) return "";
        String val = f.substring(NamespacePrefixes.TM.length());
        return HGV.replace("QQqueryQQ",val);
    }
    public static String getHGVlinkFromHGV(String f, IndexReader rdr){
        String result = HGV_DEFAULT;
        try{
            TermDocs td = rdr.termDocs(new Term(CoreMetadataFields.DOC_ID,f));
            if(!td.next()) return result;
            Document hgv = rdr.document(td.doc());
            String [] xrefs = hgv.getValues(CoreMetadataFields.XREFS);
            if(xrefs != null){
                for(String xref:xrefs){
                    if(xref.startsWith(NamespacePrefixes.TM)){
                        result = getHGVlinkFromTM(xref);
                        break;
                    }
                }
            }
        }
        catch(IOException e){
        }
        return result;
    }
    
    private IndexReader reader;
    private IndexSearcher searcher;
    private IndexReader hgv;
    @Override
    public void init() throws ServletException {
        super.init();
        String dirPath = this.getServletContext().getInitParameter("papyri.info:index:merge");
        String hgvPath = this.getServletContext().getInitParameter("papyri.info:index:hgv");
        try{
            reader = IndexReader.open(dirPath);
            hgv = IndexReader.open(hgvPath);
            searcher = new IndexSearcher(reader);
        }
        catch(IOException e){
            throw new ServletException(e.getMessage(),e);
        }
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doGet(request,response);
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String pathInfo = request.getPathInfo();
        String baseURI = "oai:papyri.info:identifiers";
        String baseURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + request.getServletPath();
        if(pathInfo!=null && !"/".equals(pathInfo)){
            baseURL += pathInfo;
            if(pathInfo.endsWith("/")) pathInfo = pathInfo.substring(0,pathInfo.length()-1);
            pathInfo = pathInfo.replace('/', ':');
            baseURI += pathInfo;
            baseURI = baseURI.replaceAll("\\s+","%20");
        }
        baseURL = baseURL.replaceAll("\\s+","%20");
        String prefix = baseURI + ":";
        request.setAttribute(ATTR_BASE_URI, baseURI);
        request.setAttribute(ATTR_BASE_URL, baseURL);
        request.setAttribute(ATTR_TITLE, "titlePlaceHolder");
        String urlBase = baseURL;
        urlBase = urlBase.substring("http://".length());
        Term next = new Term(CoreMetadataFields.XREFS,prefix);
        TermEnum terms = reader.terms(next);
        next = terms.term();
        RequestDispatcher head = this.getServletContext().getRequestDispatcher("/WEB-INF/numbers-head.jsp");
        head.include(request, response);
        Term lookup = next.createTerm(baseURI);
        TermQuery query = new TermQuery(lookup);
        Hits hits = searcher.search(query);
        if(hits.length()==0){
            int ix = baseURI.lastIndexOf(':');
            if(ix != -1){
                baseURI = baseURI.substring(0,ix) + ":" + baseURI.substring(ix);
                lookup = lookup.createTerm(baseURI);
                query = new TermQuery(lookup);
                hits = searcher.search(query);
            }
        }
        Iterator<Hit> iterator = hits.iterator();
        TreeMap<String,String> xrefs = new TreeMap<String,String>();
        
        while(iterator.hasNext()){
            String [] vals = iterator.next().getDocument().getValues(CoreMetadataFields.XREFS);

            for(String val:vals){
                if(val.startsWith(NamespacePrefixes.APIS)){
                    if(val.indexOf("apis:none") == -1){
                        xrefs.put(val, getAPISlink(val));
                    }
                }
                else if(val.startsWith(NamespacePrefixes.DDBDP)){
                    xrefs.put(val,getDDBDPlink(val));
                }
                else if(val.startsWith(NamespacePrefixes.HGV)){
                    xrefs.put(val, getHGVlinkFromHGV(val, hgv));
                }
                else if (val.startsWith(NamespacePrefixes.TM)){
                    xrefs.put(val,getLDABlink(val));
                }
                else xrefs.put(val,val);
            }
        }
        
        if(xrefs.size()>0){
            request.setAttribute(ATTR_AGGREGATES, xrefs);
            RequestDispatcher item = this.getServletContext().getRequestDispatcher("/WEB-INF/numbers-aggregates.jsp");
            item.include(request, response);
         }
        String offsetS = request.getParameter("offset");
        int offset = (offsetS != null)?Integer.parseInt(offsetS):0;
        TreeMap<String,String> parts = new TreeMap<String,String>();
        int ctr = 0;
        String offsetNext = null;
        do{
            if(next == null || next.field() != CoreMetadataFields.XREFS || !next.text().startsWith(prefix)) break;
            String text = next.text();
            int ix = text.indexOf(':',prefix.length());
            if(ix > prefix.length()+1){
                text = text.substring(0,ix);
                next = next.createTerm(text + COLON_PLUS);
            }
            else{
                next = next.createTerm(text + "0");
            }
            if(++ctr < offset) continue;
            if(ctr == offset + 500){
                offsetNext = Integer.toString(ctr);
                break;
            }
            String urlSuffix = text.substring(prefix.length());
            urlSuffix = urlSuffix.replace(':', '/');
            
            String url = urlBase + "/" + urlSuffix;
            url = url.replaceAll("\\/+", "/").replaceAll("\\s+", "%20");
            url = "http://" + url;
            parts.put(text, url);
        }while(terms.skipTo(next) && (next=terms.term()).field()==CoreMetadataFields.XREFS);
        if(parts.size()>0){
            request.setAttribute(ATTR_PARTS, parts);
            RequestDispatcher item = this.getServletContext().getRequestDispatcher("/WEB-INF/numbers-parts.jsp");
            item.include(request, response);
        }
        if(offsetNext != null){
            request.setAttribute(ATTR_OFFSET_URL, baseURL + "?offset=" + offsetNext);
        }
        RequestDispatcher foot = this.getServletContext().getRequestDispatcher("/WEB-INF/numbers-foot.jsp");
        foot.include(request, response);
        response.flushBuffer();
        return;
    }
}
