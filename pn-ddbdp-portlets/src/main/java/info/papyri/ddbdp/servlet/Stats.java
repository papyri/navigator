package info.papyri.ddbdp.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import info.papyri.epiduke.lucene.Indexer;
import info.papyri.ddbdp.util.Collection;

public class Stats extends HttpServlet implements IndexEventListener {
    private static final Logger LOG = Logger.getLogger(Stats.class);
    private File docRoot;
    private HashMap<String,Collection> collections;
    private IndexSearcher SEARCHER;
    @Override
    public void init() throws ServletException {
        super.init();
        this.docRoot = new File(getServletContext().getInitParameter("docroot"));
        collections = new HashMap<String,Collection>();
        
        File [] colls = this.docRoot.listFiles();
        for(File c:colls){
            if(c.isDirectory() && !".svn".equals(c.getName())){
                collections.put(c.getName(),new Collection(this.docRoot,c.getName()));
            }
        }
        IndexEventPropagator events = (IndexEventPropagator)this.getServletContext().getAttribute("EVENTS");
        events.addListener(this);
    }
    @Override
    public void destroy()  {
        super.destroy();
        IndexEventPropagator events = (IndexEventPropagator)this.getServletContext().getAttribute("EVENTS");
        events.removeListener(this);
    }
    
    public void replaceDocReader(IndexEvent event) {
    }

    public void replaceSearchers(SearcherEvent event) {
        SEARCHER = event.getMultiSearcher();
        
    }


    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        PrintWriter out = arg1.getWriter();
        printHeader(out);
        int wordlike = countTerms(Indexer.WORD_SPAN_TERM);
        int ignoreCaps = countTerms(Indexer.WORD_SPAN_TERM_LC);
        int ignoreMarks = countTerms(Indexer.WORD_SPAN_TERM_DF);
        int ignoreBoth = countTerms(Indexer.WORD_SPAN_TERM_FL);
        
        out.println("<table>");
        out.println("<caption>Indexed Term Statistics</caption>");
        out.println("<tr><th scope=\"row\">Word-like Forms</th><td>" + wordlike + "</td></tr>");
        out.println("<tr><th scope=\"row\">Word-like Forms (Ignoring case)</th><td>" + ignoreCaps + "</td></tr>");
        out.println("<tr><th scope=\"row\">Word-like Forms (Ignoring marks)</th><td>" + ignoreMarks + "</td></tr>");
        out.println("<tr><th scope=\"row\">Word-like Forms (Ignoring caps and marks)</th><td>" + ignoreBoth + "</td></tr>");
        out.println("</table>");
        
        out.println("<table>");
        out.println("<caption>Indexed Document Statistics</caption>");
        out.println("<tr><th scope=\"row\">TOTAL</th><td>" + countAllIndexedDocuments() + "</td></tr>");
        Iterator<String> iColls = getIndexedCollections();
        while(iColls.hasNext()){
            String iColl = iColls.next();
            out.println("<tr><th colspan=\"2\">"  + iColl + "</th></tr>");
            out.println("<tr><th>volume</th><th>num docs</th></tr>");
                       Iterator<String> volKeys = getIndexedVolumeKeys(iColl);
            while(volKeys.hasNext()){
                String volKey = volKeys.next();
                out.println("<th scope=\"row\">" + volKey.substring(volKey.indexOf(':')+1) + "</th><td>" + countIndexedDocuments(volKey) + "</td></tr>");
            }
            out.println("<tr><th scope=\"row\">TOTAL</th><td>" + countIndexedDocuments(iColl) + "</td></tr>");
        }
        out.print("</table>");
        out.println("<table>");
        out.println("<caption>Document-on-Disk Statistics</caption>");
        out.println("<tr><th scope=\"row\">TOTAL</th><td>" + countAllDocumentsOnDisk() + "</td></tr>");
        Iterator<String> dColls = getCollectionsOnDisk();
        while(dColls.hasNext()){
            String dColl =dColls.next();
            out.println("<tr><th colspan=\"2\">"  + dColl + "</th></tr>");
            out.println("<tr><th>volume</th><th>num docs</th></tr>");
             Iterator<String> volKeys = getVolumesOnDisk(dColl);
            while(volKeys.hasNext()){
                String volKey = volKeys.next();
                out.println("<th scope=\"row\">" + volKey + "</th><td>" + countDocumentsOnDisk(dColl,volKey) + "</td></tr>");
            }
            out.println("<tr><th scope=\"row\">TOTAL</th><td>" + countDocumentsOnDisk(dColl, null) + "</td></tr>");
        }
        out.print("</table>");
        printFooter(out);
        out.flush();
        return;
    }

    @Override
    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        doGet(arg0,arg1);
    }
    
    private void printHeader(PrintWriter out){
        out.println("<html>");
        out.println("<head><title>DDbDP Stats</title></head>");
        out.println("<title>DDbDP Stats</title>");
        out.println("<style type=\"text/css\">");
        out.println("TABLE{");
        out.println("margin:20px;");
       out.println("}");
        out.println("CAPTION{");
        out.println("color:gray;");
        out.println("font-weight:bold;");
        out.println("}");
        out.println("TH{");
         out.println("background-color:gray;");
        out.println("}");
        out.println("TH[scope=\"row\"] {");
        out.println("text-align:right;");
        out.println("}");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
    }
    private void printFooter(PrintWriter out){
        out.println("</body>");
        out.println("</html>");
    }
        
    public Iterator<String> getCollectionsOnDisk(){
        return this.collections.keySet().iterator();
    }
    
    public  Iterator<String> getVolumesOnDisk(String collection){
        Collection c = this.collections.get(collection);
        if(c == null) return new ArrayList<String>(0).iterator();
        return c.volumes();
    }
    
    public  int countDocumentsOnDisk(String collection, String volume){
        Collection c = this.collections.get(collection);
        if(c == null) return 0;
        return c.numDocs(volume);
    }
    
    public int countAllDocumentsOnDisk(){
        int result = 0;
        for(String s:this.collections.keySet()){
            result += this.collections.get(s).numDocs();
        }
        return result;
    }
    
    public int countAllIndexedDocuments(){
       try{
           return this.SEARCHER.maxDoc();
       }
       catch(IOException ioe){
           LOG.error(ioe.toString());
           return 0;
       }
    }
    
    public  int countIndexedDocuments(String collection, String volume){
        String val = collection + ":" + volume;
        TermQuery q = new TermQuery(new Term(Indexer.VOLUME,val));
        try{
            return this.SEARCHER.search(q).length();
        }
        catch(IOException ioe){
            LOG.error(ioe.toString());
            return 0;
        }
    }

    public int countIndexedDocuments(String volKey){
        TermQuery q = (volKey.indexOf(':') != -1)?new TermQuery(new Term(Indexer.VOLUME,volKey)):new TermQuery(new Term(Indexer.COLLECTION,volKey));
        try{
            return this.SEARCHER.search(q).length();
        }
        catch(IOException ioe){
            LOG.error(ioe.toString());
            return 0;
        }
    }
    
    public Iterator<String> getIndexedCollections(){
        ArrayList<String> collections = new ArrayList<String>();
        Term coll = new Term(Indexer.COLLECTION, "");
        try{
            TermEnum terms = this.SEARCHER.getIndexReader().terms();
            while (terms.next()){
                Term t = terms.term();
                if(!t.field().equals(coll.field())) continue;
                collections.add(t.text());
            }
            return collections.iterator();
        }
        catch(IOException ioe){
            LOG.error(ioe.toString());
            return new ArrayList<String>(0).iterator();
        }
    }

    public int countTerms(String field){
        Term coll = new Term(field, "");
        int result = 0;
        try{
            TermEnum terms = this.SEARCHER.getIndexReader().terms();
            while (terms.next()){
                Term t = terms.term();
                if(!t.field().equals(coll.field())) continue;
                result++;
            }
            return result;
        }
        catch(IOException ioe){
            LOG.error(ioe.toString());
            return 0;
        }
    }

    
    public Iterator<String> getIndexedVolumeKeys(String collection){
        ArrayList<String> volKeys = new ArrayList<String>();
        Term volKey = new Term(Indexer.VOLUME,collection + ":");
        try{
            TermEnum terms = this.SEARCHER.getIndexReader().terms(volKey);
            while (terms.next()){
                Term t = terms.term();
                if(!t.field().equals(volKey.field())) break;
                if(!t.text().startsWith(volKey.text())) break;
                volKeys.add(t.text());
            }
            return volKeys.iterator();
        }
        catch(IOException ioe){
            LOG.error(ioe.toString());
            return new ArrayList<String>(0).iterator();
        }
    }
    
    public  int countIndexedTerms(String fieldName){
        throw new UnsupportedOperationException("Stats.countTerms");
    }

}
