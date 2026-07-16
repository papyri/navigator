/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author hcayless
 */
public class CTSServlet extends HttpServlet {
  
  private String xmlPath = "";
  private String htmlPath = "";
  private FileUtils util;
  private byte[] buffer = new byte[8192];
  File inventory = new File("/data/papyri.info/cts/fakeTextInventory.xml");


  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    xmlPath = config.getInitParameter("xmlPath");
    htmlPath = config.getInitParameter("htmlPath");
    System.out.println("XML Path: " + xmlPath);
    util = new FileUtils("/data/papyri.info/idp.data/", htmlPath);
  }

  /**
   * Processes requests for both HTTP
   * <code>GET</code> and
   * <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    response.setContentType("application/xml;charset=UTF-8");
    String req = request.getParameter("request");
    String inv = request.getParameter("inv");
    if ("GetCapabilities".equals(req)) {
      send(response,inventory);
    }
    if ("GetValidReff".equals(req)) {
      CTSUrn cts = new CTSUrn(request.getParameter("urn"));
      String id = FileUtils.substringAfter(cts.toString(), "urn:cts:papyri.info:ddbdp.", false);
      PrintWriter out = response.getWriter();
      File f = util.getXmlFile("ddbdp", FileUtils.substringBefore(id, ":"));
      try {
        writeStart(out, req, inv, cts);
        CTSReffContentHandler handler = new CTSReffContentHandler();
        handler.setup(out, cts);
        XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        reader.setContentHandler(handler);
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        reader.setFeature("http://xml.org/sax/features/validation", false);
        InputSource is = new InputSource(new java.io.FileInputStream(f));
        is.setSystemId(f.getAbsoluteFile().getParentFile().getAbsolutePath() + "/");
        reader.parse(is);
        writeEnd(out,req);
      } catch (Exception e) {
          e.printStackTrace();
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } finally {      
        out.close();
      }
    }
    if ("GetPassage".equals(req)) {
      String cts = request.getParameter("urn");
      String id = FileUtils.substringAfter(cts, "urn:cts:papyri.info:ddbdp.", false);
      String location = FileUtils.substringAfter(id, ":", false);
      File f = util.getXmlFile("ddbdp", FileUtils.substringBefore(id, ":"));
      if (location.length() > 0) {
        PrintWriter out = response.getWriter();
        CTSPassageContentHandler handler = new CTSPassageContentHandler();
        handler.setup(out);
        handler.parseReference(location);
        try {
          writeStart(out, req, inv, new CTSUrn(cts));
          XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
          reader.setContentHandler(handler);
          reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
          reader.setFeature("http://xml.org/sax/features/validation", false);
          InputSource is = new InputSource(new java.io.FileInputStream(f));
          is.setSystemId(f.getAbsoluteFile().getParentFile().getAbsolutePath() + "/");
          reader.parse(is);
          writeEnd(out, req);
        } catch (Exception e) {
          e.printStackTrace();
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {      
          out.close();
        }
      } else {
        PrintWriter out = response.getWriter();
        writeStart(out, req, inv, new CTSUrn(cts));
        send(response, f);
        writeEnd(out, req);
        out.close();
      }
    }
  }
  
  private void writeStart(PrintWriter out, String name, String inv, CTSUrn urn) {
    out.write("<cts:" + name + "\n" +
"            xmlns:cts=\"http://chs.harvard.edu/xmlns/cts3\"\n" +
"            xmlns=\"http://chs.harvard.edu/xmlns/cts3\">");
    out.write("  <request>\n" +
              "	 <requestName>" + name +"</requestName>");
    out.write("  <requestUrn>" + urn +"</requestUrn>\n" +
              "    <psg>" + urn.ref() + "</psg>\n" +
              "    <workUrn>" + urn.work() +"</workUrn>\n" +
              "    <inv>" + inv + "</inv>\n" +
              "  </request>");
    out.write("  <cts:reply xmlns:tei=\"http://www.tei-c.org/ns/1.0\" xml:space=\"preserve\">\n");
  }
  
  private void writeEnd(PrintWriter out, String action) {
    out.write("  </cts:reply>\n</cts:" + action + ">");
  }
    
  private void send(HttpServletResponse response, File f)
          throws ServletException, IOException {
    FileInputStream reader = null;
    OutputStream out = response.getOutputStream();
    if (f != null && f.exists()) {
      try {
        reader = new FileInputStream(f);
        int size = reader.read(buffer);
        while (size > 0) { 
          out.write(buffer, 0, size);
          size = reader.read(buffer);
        }
      } catch (IOException e) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      } finally {
        reader.close();
        out.close();
      }
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    
  }
  
  void setCurrentRef(Ref currentRef, String localName, Attributes atts) {
      if ("div".equals(localName) && "textpart".equals(atts.getValue("type"))) {
        String n = atts.getValue("n");
        if (n != null) {
          if (atts.getIndex("subtype") < 0) {
            currentRef.addPart(n, "side");
          } else {
            currentRef.addPart(n, atts.getValue("subtype"));
          }
        }
      }
      if ("lb".equals(localName)) {
        currentRef.removePart("line");
        String n = atts.getValue("n");
        if (n != null) {
          currentRef.addPart(n, "line");
        }
      }
    }
  
  class CTSUrn {
    private Map<String,String> parts = new HashMap<String,String>(); 
    private String urn;
    public CTSUrn(String urn) {
      this.urn = urn;
      parts.put("namespace", "papyri.info");
      parts.put("collection", "ddbdp");
      String id = FileUtils.substringAfter(urn, "urn:cts:papyri.info:ddbdp.", false);
      String location = FileUtils.substringAfter(id, ":", false);
      parts.put("id", FileUtils.substringBefore(id, ":"));
      parts.put("ref", location);
      parts.put("ref-start", FileUtils.substringBefore(location, "-"));
      parts.put("ref-end", FileUtils.substringAfter(location, "-"));
    }
    
    public String namespace() {
      return parts.get("namespace");
    }
    
    public String collection() {
      return parts.get("collection");
    }
    
    public String work() {
      StringBuilder result = new StringBuilder();
      result.append("urn:cts:");
      result.append(namespace());
      result.append(":");
      result.append(collection());
      result.append(".");
      result.append(id());
      return result.toString();
    }
    
    public String id() {
      return parts.get("id");
    }
    
    public String ref() {
      return parts.get("ref");
    }
    
    public String refStart() {
      return parts.get("ref-start");
    }
    
    public String refEnd() {
      return parts.get("ref-end");
    }
    
    @Override
    public String toString() {
      return urn;
    }
  }
  
  class CTSReffContentHandler implements ContentHandler {
    
    Ref currentRef = new Ref();
    List<String> refs = new ArrayList<String>();
    PrintWriter out;
    CTSUrn base;
    
    public void setup(PrintWriter out, CTSUrn base) {
      this.out = out;
      this.base = base;
    } 

    @Override
    public void setDocumentLocator(Locator lctr) {
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
      out.write("<reff>");
      for (String ref : refs) {
        out.write("<urn>");
        out.write(base.toString());
        out.write(":");
        out.write(ref);
        out.write("</urn>");
      }
      out.write("</reff>");
    }

    @Override
    public void startPrefixMapping(String string, String string1) throws SAXException {
    }

    @Override
    public void endPrefixMapping(String string) throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
      setCurrentRef(currentRef, localName, atts);
      if (!currentRef.empty() && !refs.contains(currentRef.toString())) {
        refs.add(currentRef.toString());
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if ("ab".equals(localName)) {
        currentRef.removePart("line");
      }
      if ("div".equals(localName)) {
        currentRef.pop();
      }
    }

    @Override
    public void characters(char[] chars, int i, int i1) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
    }

    @Override
    public void processingInstruction(String string, String string1) throws SAXException {
    }

    @Override
    public void skippedEntity(String string) throws SAXException {
    }
    
  }
  
  class CTSPassageContentHandler implements ContentHandler {
    
    private Map<String,String> uris = new HashMap<String,String>();
    private boolean write = false;
    private boolean xmlns = false;
    private boolean stopNext = false;
    private boolean inElt = false;
    private PrintWriter out;
    private Ref refStart = new Ref();
    private Ref refEnd = new Ref();
    private Ref currentRef = new Ref();
    
    public void setup(PrintWriter out) {
      this.out = out;
    }   
 
    public void parseReference(String location) {
      if (location.contains("-")) {
        String[] loc = location.split("-");
        String[] start = loc[0].split("\\.");
        for (int i = 0; i < start.length; i++) {
          refStart.addPart(start[i]);
        }
        String[] end = loc[1].split("\\.");
        for (int i = 0; i < end.length; i++) {
          refEnd.addPart(end[i]);
        }
      } else {
        String[] start = location.split("\\.");
        for (int i = 0; i < start.length; i++) {
          refStart.addPart(start[i]);
          refEnd.addPart(start[i]);
        }
      }
    }
    

    @Override
    public void setDocumentLocator(Locator lctr) {
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
      uris.put(uri, prefix);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
      for (String key : uris.keySet()) {
        if (prefix.equals(uris.get(key))) {
          uris.remove(key);
        }
      }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
      matchRef(localName, atts);
      if (currentRef.isPartOf(refStart) || currentRef.isPartOf(refEnd) || write) {
        if (inElt) {
          out.write(">");
          inElt = false;
        }
        out.write("<tei:");
        out.write(localName);
        for (int i = 0; i < atts.getLength(); i++) {
          out.write(" ");
          out.write(atts.getQName(i));
          out.write("=\"");
          out.write(atts.getValue(i));
          out.write("\"");
        }
        inElt = true;
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if ("ab".equals(localName)) {
        currentRef.removePart("line");
      }
      if (currentRef.isPartOf(refStart) || currentRef.isPartOf(refEnd) || write) {
        if (inElt) {
          out.write("/>");
          inElt = false;
        } else {
          out.write("</tei:");
          out.write(localName);
          out.write(">");
        }
      }
      if ("div".equals(localName)) {
        currentRef.pop();
      }
    }

    @Override
    public void characters(char[] chars, int start, int len) throws SAXException {
      if (currentRef.isPartOf(refStart) || currentRef.isPartOf(refEnd) || write) {
        if (inElt) {
          out.write(">");
          inElt = false;
        }
        out.write(chars, start, len);
      }
    }

    @Override
    public void ignorableWhitespace(char[] chars, int start, int len) throws SAXException {
      characters(chars, start, len);
    }

    @Override
    public void processingInstruction(String string, String string1) throws SAXException {
    }

    @Override
    public void skippedEntity(String string) throws SAXException {
    }
        
    private void matchRef(String localName, Attributes atts) {
      xmlns = false;
      setCurrentRef(currentRef, localName, atts); 
      if (stopNext && !refEnd.matches(currentRef)) {
        write = false;
        return;
      }
      if (refStart.matches(currentRef)) {
        write = true;
        xmlns = true;
      }
      if (refEnd.matches(currentRef)) {
        stopNext = true;
      }
    }
  }
  
  class Ref {
    
    private List<String> ref = new ArrayList<String>();
    private List<String> parts = new ArrayList<String>();
    
    public boolean empty() {
      return ref.isEmpty();
    }
    
    public void addPart(String part) {
      ref.add(part);
      parts.add(Integer.toString(ref.size()));
    }
    
    public void addPart(String part, String label) {
      ref.add(part);
      parts.add(label);
    }
    
    public void removePart(String label) {
      int remove = parts.indexOf(label);
      if (remove >= 0) {
        for (int i = parts.size() - 1; i >= remove; i--) {
          parts.remove(i);
          ref.remove(i);
        }
      }
    }
    
    public void pop() {
      int remove = ref.size() - 1;
      if (remove >= 0) {
        parts.remove(remove);
        ref.remove(remove);
      }
    }
 
    public String last() {
      return ref.get(ref.size() - 1);
    }
    
    @Override
    public String toString() {
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < ref.size(); i++) {
        result.append(ref.get(i));
        if (i < ref.size() - 1) {
          result.append(".");
        }
      }
      return result.toString();
    }
    
    public boolean matches(Ref r) {
      if (r.ref.size() < ref.size()) {
        return false;
      }
      for (int i = 0; i < ref.size() && i < r.ref.size(); i++) {
        if (!ref.get(i).equals(r.ref.get(i))) {
          return false;
        }
      }
      return true;
    }
    
    public boolean contains(Ref r) {
      for (int i = 0; i < r.ref.size() && i < ref.size(); i++) {
        if (!r.ref.get(i).equals(ref.get(i))) {
          return false;
        }
      }
      return true;
    }
    
    public boolean isPartOf(Ref r) {
      return !ref.isEmpty() && r.contains(this);
    }
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP
   * <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP
   * <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }// </editor-fold>
}
