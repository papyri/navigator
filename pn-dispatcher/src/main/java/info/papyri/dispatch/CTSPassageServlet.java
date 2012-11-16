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
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author hcayless
 */
public class CTSPassageServlet extends HttpServlet {
  
  private String xmlPath = "";
  private String htmlPath = "";
  private FileUtils util;
  private byte[] buffer = new byte[8192];

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    xmlPath = config.getInitParameter("xmlPath");
    htmlPath = config.getInitParameter("htmlPath");
    System.out.println("XML Path: " + xmlPath);
    util = new FileUtils("/data/papyri.info/idp.data", htmlPath);
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
    String cts = request.getParameter("urn");
    String id = FileUtils.substringAfter(cts, "urn:cts:papyri.info:ddbdp.", false);
    String location = FileUtils.substringAfter(id, ":", false);
    File f = util.getXmlFile("ddbdp", FileUtils.substringBefore(id, ":"));
    if (location.length() > 0) {
      PrintWriter out = response.getWriter();
      CTSContentHandler handler = new CTSContentHandler();
      handler.setup(out);
      handler.parseReference(location);
      try {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(handler);
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        reader.setFeature("http://xml.org/sax/features/validation", false);
        InputSource is = new InputSource(new java.io.FileInputStream(f));
        is.setSystemId(f.getAbsoluteFile().getParentFile().getAbsolutePath() + "/");
        reader.parse(is);
      } catch (Exception e) {
        e.printStackTrace();
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } finally {      
        out.close();
      }
    } else {
      send(response, f);
    }
    
    

    
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
  
  class CTSContentHandler implements ContentHandler {
    
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
      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
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
      if (currentRef.isPartOf(refStart) || write) {
        if (inElt) {
          out.write(">");
          inElt = false;
        }
        out.write("<");
        out.write(qName);
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
      if ("div".equals(localName)) {
        currentRef.removePart("line");
      }
      if (currentRef.isPartOf(refStart) || write) {
        if (inElt) {
          out.write("/>");
          inElt = false;
        } else {
          out.write("</");
          out.write(qName);
          out.write(">");
        }
      }
      if ("div".equals(localName)) {
        currentRef.pop();
      }
    }

    @Override
    public void characters(char[] chars, int start, int len) throws SAXException {
      if (currentRef.isPartOf(refStart) || write) {
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
      for (String p : ref) {
        result.append(p);
        if (!p.equals(last())) {
          result.append(".");
        }
      }
      return result.toString();
    }
    
    public boolean matches(Ref r) {
      if (r.ref.size() < ref.size()) {
        return false;
      }
      for (int i = 0; i < ref.size(); i++) {
        if (!ref.get(i).equals(r.ref.get(i))) {
          return false;
        }
      }
      return true;
    }
    
    public boolean isPartOf(Ref r) {
      return ref.size() == 0 || r.toString().contains(toString());
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
