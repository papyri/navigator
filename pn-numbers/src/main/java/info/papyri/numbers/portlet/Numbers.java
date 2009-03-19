package info.papyri.numbers.portlet;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.lucene.index.*;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardTermEnum;
import org.apache.lucene.document.*;
import info.papyri.metadata.*;
import java.util.*;
public class Numbers extends GenericPortlet {
   private static final String COLON_PLUS = new String(new char[]{(':'+1)});
   private static final Term XREF_TEMPLATE = new Term(CoreMetadataFields.XREFS,"");
   public static final int PAGE_SIZE = 100;
   public static final String ATTR_PORTLET = "papyri.info:numbers:portlet";
   public static final String ATTR_FROM = "papyri.info:numbers:from";
   public static final String ATTR_PREFIX = "papyri.info:numbers:prefix";
   public static final String ATTR_ITEM = "papyri.info:numbers:item";
   public static final String ATTR_PUBS = "papyri.info:numbers:pubs";
   public static final String ATTR_APIS = "papyri.info:numbers:apis";
   public static final String ATTR_DDB = "papyri.info:numbers:ddbdp";
   public static final String ATTR_TM = "papyri.info:numbers:trismegistos";
   public static final String ATTR_HGV = "papyri.info:numbers:hgv";
   private IndexSearcher searcher;
   private String [] apisCollections;
   private String [] ddbdpCollections;
   private String [] hgvCollections;
    @Override
    protected void doView(RenderRequest request, RenderResponse arg1) throws PortletException, IOException {
        request.setAttribute(ATTR_PORTLET, this);
        String prefix = request.getParameter("prefix");
        if(prefix != null && !(prefix = prefix.trim()).equals("")){
            prefix = prefix.replaceAll("\\s+", "%20");
            prefix = prefix.replaceAll("\\*:\\*","*");
            request.setAttribute(ATTR_PREFIX, prefix);
        }
        else {
            prefix = null;
            request.setAttribute(ATTR_PREFIX, "");
        }
        PortletRequestDispatcher prd = this.getPortletContext().getRequestDispatcher("/WEB-INF/portlet-ui.jsp");
        prd.include(request, arg1);
        String from = request.getParameter("from");
        if(prefix != null){
            int count = 0;
            IndexReader rdr  = this.searcher.getIndexReader();
            Term term = XREF_TEMPLATE.createTerm(prefix);
            boolean wild = prefix.indexOf('*')!=-1;
            if(wild){
            TermEnum terms = new WildcardTermEnum(rdr,term);
            if(from != null && !(from = from.trim()).equals("")){
                if(!terms.skipTo(XREF_TEMPLATE.createTerm(from))){
                    prd = this.getPortletContext().getRequestDispatcher("/WEB-INF/portlet-footer.jsp");
                    prd.include(request, arg1);
                    terms.close();
                    return;
                }
            }
            prd = this.getPortletContext().getRequestDispatcher("/WEB-INF/portlet-match.jsp");
            do{
                term = terms.term();
                if(term == null) continue;
                String item = term.text();
                request.setAttribute(ATTR_ITEM, item);
                prd.include(request, arg1);
            }while(++count<PAGE_SIZE && terms.next());
            if(count == PAGE_SIZE && terms.next()){
                Term fromTerm = terms.term();
                request.setAttribute(ATTR_FROM, fromTerm.text());
            }
        }
            else{
                TermQuery query = new TermQuery(term);
                Set<String> apis = new TreeSet<String>();
                Set<String> hgv = new TreeSet<String>();
                Set<String> tm = new TreeSet<String>();
                Set<String> ddbdp = new TreeSet<String>();
                Set<String> pubs = new TreeSet<String>();
                Hits hits = this.searcher.search(query);
                if(hits.length()>0){
                    Iterator<Hit> iter = hits.iterator();
                    while(iter.hasNext()){
                        Document doc = iter.next().getDocument();
                        String [] values = doc.getValues(CoreMetadataFields.BIBL_PUB);
                        if(values != null){
                            for(String pub:values)pubs.add(pub);
                        }
                        values = doc.getValues(CoreMetadataFields.XREFS);
                        if(values != null){
                            for(String xref:values){
                                if(xref.startsWith(NamespacePrefixes.APIS)) apis.add(xref);
                                if(xref.startsWith(NamespacePrefixes.DDBDP)) ddbdp.add(xref);
                                if(xref.startsWith(NamespacePrefixes.HGV)) hgv.add(xref);
                                if(xref.startsWith(NamespacePrefixes.TM)) tm.add(xref);
                            }
                        }
                    }
                    request.setAttribute(ATTR_APIS, apis);
                    request.setAttribute(ATTR_HGV, hgv);
                    request.setAttribute(ATTR_DDB, ddbdp);
                    request.setAttribute(ATTR_TM, tm);
                    request.setAttribute(ATTR_PUBS, pubs);
                    prd = this.getPortletContext().getRequestDispatcher("/WEB-INF/portlet-item.jsp");
                    prd.include(request, arg1);
                }
                else{
                    prd = this.getPortletContext().getRequestDispatcher("/WEB-INF/portlet-nomatch.jsp");
                    prd.include(request, arg1);
                }
            }
        }
        else{
            
        }
        prd = this.getPortletContext().getRequestDispatcher("/WEB-INF/portlet-footer.jsp");
        prd.include(request, arg1);
    }

    @Override
    public void init() throws PortletException {
        super.init();
        String dirPath = this.getPortletContext().getInitParameter("papyri.info:index:merge");
        try{
            IndexReader reader = IndexReader.open(dirPath);
            this.searcher = new IndexSearcher(reader);
            this.apisCollections = getPartValues(reader, CoreMetadataFields.XREFS, NamespacePrefixes.APIS);
            this.ddbdpCollections = getPartValues(reader, CoreMetadataFields.XREFS, NamespacePrefixes.DDBDP);
            this.hgvCollections =  getPartValues(reader, CoreMetadataFields.XREFS, NamespacePrefixes.HGV);
        }
        catch(Throwable t){
            throw new PortletException(t);
        }
    }
    private static String [] getPartValues(IndexReader reader, String field, String prefix) throws IOException {
        ArrayList<String> values = new ArrayList<String>();
        Term term = new Term(field,prefix);
        TermEnum terms = reader.terms(term);
        term = terms.term();
        do{
            if(term != null && term.text().startsWith(prefix)){
                String val = term.text();
                val = val.substring(prefix.length());
                int ix = val.indexOf(':');
                if(ix != -1){
                    val = val.substring(0,ix);
                    values.add(val);
                    term = term.createTerm(prefix + val + COLON_PLUS);
                }else break;
            }else break;
        }while(terms.skipTo(term) && (term = terms.term()).field()==field);
        return values.toArray(new String[0]);
    }
    public Iterator<String> getApisCollections(){
        return java.util.Arrays.asList(apisCollections).iterator();
    }
    public Iterator<String> getDDbCollections(){
        return java.util.Arrays.asList(ddbdpCollections).iterator();
    }
    public Iterator<String> getHGVCollections(){
        return java.util.Arrays.asList(hgvCollections).iterator();
    }
}
