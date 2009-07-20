package info.papyri.ddbdp.servlet;

import info.papyri.ddbdp.portlet.DocumentPortlet;
import info.papyri.metadata.NamespacePrefixes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Hits;

public class Document extends HttpServlet implements IndexEventListener {
    private static final String ID_PREFIX = "oai:papyri.info:identifiers:ddbdp:";
    private static final int ID_PREFIX_LEN = ID_PREFIX.length();
    private static final Term DDB_TEMPLATE = new Term("ddbdpId","");
    private static final Term FILENAME_TEMPLATE = new Term("fileName","");
    private File docRoot;
    private IndexSearcher SEARCHER;
    private IndexReader DOC_READER;
    private IndexSearcher DOC_SEARCHER;
    @Override
    public void init() throws ServletException {
        super.init();
            String docroot = this.getServletContext().getInitParameter("docroot");
            this.docRoot = new File(docroot);
            if (!this.docRoot.exists() || !this.docRoot.isDirectory()){
                throw new ServletException("No doc directory at " + docroot);
            }
            IndexEventPropagator events = (IndexEventPropagator)this.getServletContext().getAttribute("EVENTS");
            events.addListener(this);
    }
    @Override
    public void destroy()  {
        super.destroy();
        IndexEventPropagator events = (IndexEventPropagator)this.getServletContext().getAttribute("EVENTS");
        events.removeListener(this);
        SEARCHER = null;
        DOC_READER = null;
        DOC_SEARCHER = null;
    }

    
    public void replaceDocReader(IndexEvent event) {
        DOC_READER = (IndexReader)event.getSource();
        DOC_SEARCHER = new IndexSearcher(DOC_READER);
    }

    public void replaceSearchers(SearcherEvent event) {
        SEARCHER = event.getMultiSearcher();
        //setSearchers(IndexSearcher searcher, IndexSearcher bigrams, IndexSearcher bigramsDF, IndexSearcher bigramsFL, IndexSearcher bigramsLC)
    }
    
    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        String name = arg0.getParameter("name");
        String collection;
        String volume;
        if(name == null){
            String id = arg0.getParameter("identifier");
            if(id != null && id.startsWith(NamespacePrefixes.DDBDP)){
                TermQuery query = new TermQuery(DDB_TEMPLATE.createTerm(id));
                Hits hits = DOC_SEARCHER.search(query);
                if(hits.length()==0){
                    arg1.sendError(404, "Could not locate transcription for " + id);
                    arg1.flushBuffer();
                    return;
                }
                name = hits.doc(0).get("fileName");
                collection = hits.doc(0).get("collection");
                volume = hits.doc(0).get("volume");
                if(volume != null){
                    Matcher m = DocumentPortlet.LEADING_ZEROES.matcher(volume);
                    if(m.matches()) volume = volume.substring(m.group(1).length());
                }
            }
            else {
                arg1.sendError(400, "Missing or inappropriate transcription name or id requested (name: null, id: " + id + ")");
                arg1.flushBuffer();
                return;
            }
        }
        else {
            TermQuery query = new TermQuery(FILENAME_TEMPLATE.createTerm(name));
            Hits hits = DOC_SEARCHER.search(query);
            if(hits.length()==0){
                arg1.sendError(404, "Could not locate transcription for " + name);
                arg1.flushBuffer();
                return;
            }
            name = hits.doc(0).get("fileName");
            collection = hits.doc(0).get("collection");
            volume = hits.doc(0).get("volume");
        }
        if(volume == null || "".equals(volume)) name = collection + "/" + name;
        else name = (collection + "/" +  collection + "." + volume.replace(':', '.') + "/" + name);
        File xml = new File(this.docRoot,name);
        try{
            FileInputStream in = new FileInputStream(xml);
        byte [] buf = new byte[1024];
        int read = 0;
        arg1.setContentType("text/xml");
        OutputStream out = arg1.getOutputStream();
        while((read = in.read(buf)) != -1){
            out.write(buf, 0, read);
        }
        out.flush();
        return;
        }
        catch(FileNotFoundException fnfe){
            arg1.sendError(404, "Could not locate transcription for " + name);
            arg1.flushBuffer();
            return;
        }
    }
    @Override
    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
         doGet(arg0, arg1);
    }
    
}
