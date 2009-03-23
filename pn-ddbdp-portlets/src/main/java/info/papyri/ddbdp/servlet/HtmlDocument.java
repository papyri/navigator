package info.papyri.ddbdp.servlet;

import info.papyri.ddbdp.portlet.DocumentPortlet;
import info.papyri.ddbdp.xslt.DelegatingResolver;
import info.papyri.ddbdp.xslt.Log4JTransformListener;
import info.papyri.metadata.NamespacePrefixes;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.regex.Matcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.Log4jEntityResolver;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Hits;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class HtmlDocument extends HttpServlet implements IndexEventListener {
    private static final Logger LOG = Logger.getLogger(HtmlDocument.class);
    private static final String ID_PREFIX = "oai:papyri.info:identifiers:ddbdp:";
    private static final int ID_PREFIX_LEN = ID_PREFIX.length();
    private static final Term DDB_TEMPLATE = new Term("ddbdpId","");
    private static final Term FILENAME_TEMPLATE = new Term("fileName","");
    private static final TransformerFactory fact = getFactory();
    private static final TransformerFactory getFactory(){
        TransformerFactory fact = TransformerFactory.newInstance();
        fact.setURIResolver(new DelegatingResolver(fact.getURIResolver()));
        fact.setErrorListener(new Log4JTransformListener(LOG));
        return fact;
    }
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String collection;
        String volume;
        if(name == null){
            String id = request.getParameter("identifier");
            if(id != null && id.startsWith(NamespacePrefixes.DDBDP)){
                TermQuery query = new TermQuery(DDB_TEMPLATE.createTerm(id));
                Hits hits = DOC_SEARCHER.search(query);
                if(hits.length()==0){
                    response.sendError(404, "Could not locate transcription for " + id);
                    response.flushBuffer();
                    return;
                }
                name = hits.doc(0).get("fileName");
                collection = hits.doc(0).get("collection");
                volume = hits.doc(0).get("volume");
            }
            else {
                response.sendError(400, "Missing or inappropriate transcription name or id requested (name: null, id: " + id + ")");
                response.flushBuffer();
                return;
            }
        }
        else {
            TermQuery query = new TermQuery(FILENAME_TEMPLATE.createTerm(name));
            Hits hits = DOC_SEARCHER.search(query);
            if(hits.length()==0){
                response.sendError(404, "Could not locate transcription for " + name);
                response.flushBuffer();
                return;
            }
            name = hits.doc(0).get("fileName");
            collection = hits.doc(0).get("collection");
            volume = hits.doc(0).get("volume");
        }
        if(volume != null){
            Matcher m = DocumentPortlet.LEADING_ZEROES.matcher(volume);
            if(m.matches()) volume = volume.substring(m.group(1).length());
        }
        if(volume == null || "".equals(volume)) name = collection + "/" + name;
        else name = (collection + "/" +  collection + "." + volume.replace(':', '.') + "/" + name);
        File xml = new File(this.docRoot,name);
        try{
            InputStream in = new FileInputStream(xml);
            in = new BufferedInputStream(in);
            
            BufferedReader isr = null;
                isr = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            XMLReader reader = DocumentPortlet.createReader();
            InputStream xsl = DocumentPortlet.class.getResourceAsStream("/info/papyri/ddbdp/xslt/start-edition.xsl");
            StreamSource source = new StreamSource(xsl);
            Transformer trans = fact.newTransformer(source);
            trans.setOutputProperty(OutputKeys.ENCODING, response.getCharacterEncoding());
            LOG.debug("response.getCharacterEncoding()=="+response.getCharacterEncoding());
            //ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(response.getOutputStream());
            InputSource inSrc = new InputSource();
            inSrc.setCharacterStream(isr);
            trans.transform(new SAXSource(reader,inSrc), result);
            //LOG.debug(arg0)
        }
        catch(FileNotFoundException fnfe){
            response.sendError(404, "Could not locate transcription for " + name);
            response.flushBuffer();
            return;
        }
        catch (SAXException se){
            throw new ServletException(se);
        }
        catch (TransformerException te){
            throw new ServletException(te);
        }
    }
    @Override
    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
         doGet(arg0, arg1);
    }
    
    public static String parseName(String fName){
        int xmlDot = fName.indexOf(".xml");
        int docDot = fName.substring(0,xmlDot).lastIndexOf('.');
        int volDot = fName.substring(0,docDot).lastIndexOf('.');
        int colDot = fName.substring(0,volDot).indexOf('.',3);
        String volName = null;
        if(volDot > 2){
             volName = fName.substring(0,docDot);
        }
        String result = (colDot != -1)?fName.substring(0,colDot):fName.substring(0,docDot);
        if(volName != null) result = fName.substring(0,volDot) + "/" + volName;
        result += "/" + fName;
        return result;
    }
    
    
}
