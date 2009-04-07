package info.papyri.ddbdp.portlet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.papyri.ddbdp.parser.QueryExecContext;
import info.papyri.ddbdp.servlet.IndexEvent;
import info.papyri.ddbdp.servlet.IndexEventListener;
import info.papyri.ddbdp.servlet.IndexEventPropagator;
import info.papyri.ddbdp.servlet.SearcherEvent;
import info.papyri.ddbdp.xslt.DelegatingResolver;
import info.papyri.ddbdp.xslt.Log4JTransformListener;
import info.papyri.epiduke.lucene.Indexer;

import info.papyri.metadata.*;

import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Source;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.EntityResolver;

public class DocumentPortlet extends GenericPortlet implements
IndexEventListener {
    private IndexSearcher SEARCHER;
    private IndexReader DOC_READER;
    private File docRoot;
    private static final Logger LOG = Logger.getLogger(DocumentPortlet.class);
    private static final Term TEMPLATE = new Term("ddbdpId","");
    public static final String DDB_ID = "info.papyri:identifiers:ddbdp:id";
    public static final String DDB_ERROR = "info.papyri:identifiers:ddbdp:error";
    public  static final TransformerFactory fact = getFactory();
    private static final TransformerFactory getFactory(){
        TransformerFactory fact = TransformerFactory.newInstance();
        fact.setURIResolver(new DelegatingResolver(fact.getURIResolver()));
        fact.setErrorListener(new Log4JTransformListener(LOG));
        return fact;
    }

    public void init() throws PortletException {
        super.init();
        String docroot = this.getPortletContext().getInitParameter("docroot");
        this.docRoot = new File(docroot);
        if (!this.docRoot.exists() || !this.docRoot.isDirectory()){
            throw new PortletException("No doc directory at " + docroot);
        }        IndexEventPropagator events = (IndexEventPropagator)this.getPortletContext().getAttribute("EVENTS");
        events.addListener(this);
    }

    @Override
    public void destroy()  {
        super.destroy();
        IndexEventPropagator events = (IndexEventPropagator)this.getPortletContext().getAttribute("EVENTS");
        events.removeListener(this);
    }


    public void replaceDocReader(IndexEvent event) {
        DOC_READER = (IndexReader)event.getSource();
        SEARCHER = new IndexSearcher(DOC_READER);
    }

    public void replaceSearchers(SearcherEvent event) {
    }

    public static XMLReader createReader() throws SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        final EntityResolver delegate = reader.getEntityResolver();
        EntityResolver prox = new EntityResolver(){
            public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
                if((publicId != null && publicId.endsWith("tei-epidoc.dtd")) || (systemId != null && systemId.endsWith("tei-epidoc.dtd"))){
                    return new InputSource(DocumentPortlet.class.getResourceAsStream("tei-epidoc.dtd"));
                }
                else return delegate.resolveEntity(publicId, systemId);
            }
        };
        reader.setEntityResolver(prox);
        return reader;
    }

    private static Term getTerm(String id) throws javax.naming.NamingException {
        Term term = null;
        if(id.startsWith(NamespacePrefixes.DDBDP)){
            return TEMPLATE.createTerm(id);
        }
        else{
            Hashtable env = new Hashtable();
            env.put(Context.URL_PKG_PREFIXES, "info.papyri");
            Context c = NamingManager.getURLContext("jndi", env);
            Name xName = getName(id);
            c = (Context)c.lookup(xName);
            if(c==null) return null;
            id = (String)c.lookup("ddbdp");
            if(id==null) {
                return null;
            }
            return TEMPLATE.createTerm(id);
        }
    }
    public static final Pattern LEADING_ZEROES = Pattern.compile("(^0+).*$");

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException {
        String xslt = request.getPreferences().getValue("xslt", "start-div-portlet.xsl");
        String cn = (request.getParameter("controlName") == null)?null:request.getParameter("controlName").trim();
        LOG.debug("request.getParameter(\"controlName\") == " + request.getParameter("controlName"));
        if (cn != null){
            try{
                cn = cn.trim();
                cn = new String(cn.getBytes("ISO-8859-1"),"UTF-8");
            }
            catch(UnsupportedEncodingException e){
                LOG.error("controlName decoding error: UnsupportedEncodingException: " + e.toString(),e);
            }
            try{
                Term term = getTerm(cn);
                if (term == null) {
                    request.setAttribute(DDB_ERROR, "No context for \"" + cn + "\"");
                    PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/ddb500.jsp");
                    rd.include(request, response);
                    response.flushBuffer();
                    return;
                }

                Hits hits = SEARCHER.search(new TermQuery(term));
                request.setAttribute(DDB_ID, term.text());
                LOG.debug("request.setAttribute(\"" + DDB_ID + ", " + term.text() + ")");
                if(hits.length()==0){
                    PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/ddb404.jsp");
                    rd.include(request, response);
                    response.flushBuffer();
                    return;
                }
                String name = hits.doc(0).get("fileName");
                String collection = hits.doc(0).get("collection");
                String volume = hits.doc(0).get("volume");
                if(volume != null){
                    Matcher m = LEADING_ZEROES.matcher(volume);
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
                    XMLReader reader = createReader();
                    InputStream xsl = DocumentPortlet.class.getResourceAsStream("/info/papyri/ddbdp/xslt/" + xslt);
                    StreamSource source = new StreamSource(xsl); 
                    Transformer trans = fact.newTransformer(source);
                    trans.setOutputProperty(OutputKeys.METHOD, "xml");
                    trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    LOG.debug("response.getCharacterEncoding()=="+response.getCharacterEncoding());
                    trans.setOutputProperty(OutputKeys.ENCODING, response.getCharacterEncoding());
                    LOG.debug("trans.getOutputProperty(OutputKeys.ENCODING) == " + trans.getOutputProperty(OutputKeys.ENCODING));
                    StreamResult result = new StreamResult(response.getWriter());
                    InputSource inSrc = new InputSource();
                    inSrc.setCharacterStream(isr);
                    trans.transform(new SAXSource(reader,inSrc), result);
                }
                catch(FileNotFoundException fnfe){
                    PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/ddb404.jsp");
                    rd.include(request, response);
                    response.flushBuffer();
                }
            }
            catch(NamingException e){
                LOG.error("NamingException: " + e.toString(),e);
            }
            catch(TransformerException e){
                LOG.error("TransformerException: " + e.toString(),e);
            }
            catch(SAXException e){
                LOG.error("SAXException: " + e.toString(),e);
            }
            catch(IOException e){
                LOG.error("IOException: " + e.toString(),e);
            }
            finally{
                try{
                    response.flushBuffer();
                }catch(Throwable t){}
            }
        }
    }
    private static final  Name getName(String cn) throws InvalidNameException{
        Properties syntax = new Properties();
        syntax.put("jndi.syntax.direction", "left_to_right");
        syntax.put("jndi.syntax.separator", ":");
        return new CompoundName(cn,syntax);
    }

}

