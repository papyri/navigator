package tests;

import info.papyri.ddbdp.portlet.DocumentPortlet;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.XMLReader;

import junit.framework.TestCase;

public class XSLTTests extends TestCase {
    public void testBadSurrogate()  throws TransformerException, SAXException, UnsupportedEncodingException {
        String xslt = "start-edition.xsl";
        XMLReader reader = DocumentPortlet.createReader();
        InputStream xsl = DocumentPortlet.class.getResourceAsStream("/info/papyri/ddbdp/xslt/" + xslt);
        System.out.println("version: " + org.apache.xalan.Version.getVersion());
        System.out.println("major: " + org.apache.xalan.Version.getMajorVersionNum());
        System.out.println("release: " + org.apache.xalan.Version.getReleaseVersionNum());
        System.out.println("maintenance: " + org.apache.xalan.Version.getMaintenanceVersionNum());
        System.out.println("dev: " + org.apache.xalan.Version.getDevelopmentVersionNum());
        StreamSource source = new StreamSource();
        source.setReader(new InputStreamReader(xsl,"UTF-8"));
        //DocumentPortlet.fact.setAttribute("http://xml.apache.org/xalan/properties/source-location", Boolean.TRUE);
        Transformer trans = DocumentPortlet.fact.newTransformer(source);
        trans.setErrorListener(new TestErrorListener());
        InputStream in = XSLTTests.class.getResourceAsStream("/tests/data/p.cair.goodsp.30.xml");
        InputSource inSrc = new InputSource();
        inSrc.setCharacterStream(new InputStreamReader(in,"UTF-8"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter outWriter = new OutputStreamWriter(out,"UTF-8");
        StreamResult result = new StreamResult(out);
        trans.transform(new SAXSource(reader,inSrc), result);
    }
}
