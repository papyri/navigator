package info.papyri.ddbdp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.*;

import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class Text extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(Text.class);
    private static final String XML_HDR = "<?xml version=\"1.0\"?>";
    private static final Transformer TRANSFORMER = getTransformer();
    private static final XMLReader READER = getReader();
    private File docRoot = null;
    @Override
    public void init() throws ServletException {
        super.init();
        String docRootPath = getServletContext().getInitParameter("docroot");
        if (docRootPath!= null){
                this.docRoot = new File(docRootPath);
                
                if (!this.docRoot.canRead() || !this.docRoot.isDirectory()){
                    this.docRoot = null;
                    LOG.error("bad docroot path at " + docRootPath);
                }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String series = getParm("series",request,true);
        String volume = getParm("volume",request,false);
        String document = getParm("document",request,false);
        if(!document.endsWith(".xml")){
            document = ("-".equals(volume))?series + "."+ document + ".xml":series + "." + volume + "." + document + ".xml";
        }
        series = series.trim();
        series = series.replaceAll("\\s", "");
        if (series.charAt(series.length() - 1) == '.') series = series.substring(0, series.length() - 1);
        File r = ("-".equals(volume))?new File(docRoot,series):new File(new File(docRoot,series),volume);
//        try{
//            System.out.println("Trying to get documents in series/vol root " + r.getCanonicalPath());
//        }
//        catch (IOException ioe){
//            System.err.println(ioe.toString());
//        }
        if(!r.exists() && !"-".equals(volume)) r = new File(new File(docRoot,series),series + "." + volume);
        File doc = new File(r,document);
        FileInputStream fis = new FileInputStream(doc);
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        OutputStream os = response.getOutputStream();
        transform(fis,os);
        //fis = new FileInputStream(doc);
        //transform(fis,System.out);
        //System.out.flush();
        os.flush();
        fis.close();
    }

    private static String getParm(String name, HttpServletRequest request, boolean normal){
        String parm = request.getParameter(name);
        if (parm == null) parm = "error";
        if (normal) parm = parm.toLowerCase();
        return parm;
    }
    private static Transformer getTransformer(){
        InputStream xsl = Text.class.getResourceAsStream("xslt/start-edition-div.xsl");
        StreamSource source = new StreamSource(xsl);
        try{
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setURIResolver(new URIResolver(){
                public Source resolve(String href, String base) throws TransformerException {
                    String path = (href.endsWith(".xsl")||href.endsWith(".xml"))?"xslt/":"";
                    path += href;
                    return new StreamSource(Text.class.getResourceAsStream(path));
                }
            });
            Transformer result = factory.newTransformer(source);
            return result;
        }
        catch (Throwable t){
            t.printStackTrace();
            return null;
        }
    }
    
    private static XMLReader getReader(){
        XMLReader xr = null;
        try{
            xr = XMLReaderFactory.createXMLReader();
        }
        catch (SAXException se){}
        xr.setEntityResolver(new DefaultHandler(){
            public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
                if (systemId.endsWith("tei-epidoc.dtd")){
                    return new InputSource(Text.class.getResourceAsStream("tei-epidoc.dtd"));
                }
                return super.resolveEntity(publicId, systemId);
            }
        });
        return xr;
    }
    
    private static void transform(InputStream xml, OutputStream out){
        StreamResult result = new StreamResult(out);
        try{
            SAXSource ss = new SAXSource(READER,new InputSource(xml));
            TRANSFORMER.transform(ss, result);
            out.flush();
        }
        catch (Throwable t){
            t.printStackTrace();
        }
    }

}
