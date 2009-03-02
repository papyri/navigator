package info.papyri.data.publication;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import java.util.Stack;
import java.util.HashSet;
import java.util.HashMap;

public class ContentHandler implements org.xml.sax.ContentHandler {
    private static final boolean DEBUG = "true".equals(System.getProperty("pn.matcher.debug"));
    private final static int TITLE = 1;
    private final static int IDENT = -1;
    private final static int TITLE_INFO = -1;
    private final HashMap<String,String> forms;
    private boolean abbr_mode = false;
    private boolean canonical = false;
    private HashSet<String> abbreviations = new HashSet<String>();
    private String canonical_form = null;
    private String first_form = null;
    private Stack<StringBuffer> data = new Stack<StringBuffer>();
    
    public ContentHandler(HashMap<String,String> forms) {
        this.forms = forms;
    }
    
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        data.peek().append(arg0,arg1,arg2);
    }

    public void endDocument() throws SAXException {
        // do something with mapping
    }

    public void endPrefixMapping(String arg0) throws SAXException {
    }

    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {

    }

    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
    }

    public void setDocumentLocator(Locator arg0) {
    }

    public void skippedEntity(String arg0) throws SAXException {
    }

    public void startDocument() throws SAXException {
    }

    public void startElement(String uri, String localName, String qname,
            Attributes atts) throws SAXException {
        data.push(new StringBuffer());
        if(qname.equals("titleInfo")){
            if("abbreviated".equals(atts.getValue("type"))){
                this.abbr_mode = true;
            }
            if("checklist".equals(atts.getValue("role"))){
                this.canonical = true;
            }
            else {
                this.canonical = false;
            }
        }
        if (qname.equals("mods")){
            this.abbreviations = new HashSet<String>();
            this.canonical_form = null;
            this.first_form = null;
            this.canonical = false;
        }
    }

    public void endElement(String uri, String localName, String qname){
        String data = this.data.pop().toString().trim();
        if (qname.equals("mods")){
            if (canonical_form == null){
                this.canonical_form = this.first_form;
            }
            for (String abbr:this.abbreviations){
                this.forms.put(abbr, this.canonical_form);
                if(DEBUG) System.out.println("Mapping \"" + abbr + "\" to canonical \"" + this.canonical_form + "\"");
            }
        }
        if (qname.equals("titleInfo")){
            this.canonical = false;
            this.abbr_mode = false;
        }
        if (qname.equals("title") && abbr_mode){
            this.abbreviations.add(data);
            if (this.first_form == null) this.first_form = data;
            if (this.canonical){
                this.canonical_form = data;
                this.canonical = false;
            }
        }
        //System.out.println("uri: " + uri + " lname: " + localName + " qname: " + qname);
    }

    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
    }

}
