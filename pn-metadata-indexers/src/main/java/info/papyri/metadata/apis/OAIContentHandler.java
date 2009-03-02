package info.papyri.metadata.apis;
import org.xml.sax.*;
import org.apache.lucene.document.*;

import info.papyri.data.Indexer;
import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.metadata.apis.OAIField.TYPES;

import java.util.Stack;
import java.io.IOException;
import java.net.URL;

public class OAIContentHandler implements ContentHandler {
    public static final String DC = "http://purl.org/dc/elements/1.1/";
    public static final String DCMI = "http://purl.org/dc/terms/";
    public static final String PI = "http://papyri.info/dc/";
    public static final String OAI = "http://www.openarchives.org/OAI/2.0/";
    private final Indexer indexer;

    private CoreMetadataRecord current;
    private Stack<OAIField> fields = new Stack<OAIField>();
    private String resumptionToken = null;
    public OAIContentHandler(Indexer indexer){
        this.indexer = indexer;
    }
    
    public String getResumptionToken(){
        return this.resumptionToken;
    }

    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        if(fields.size() > 0 && fields.peek().writeable()) fields.peek().appendData(arg0,arg1,arg2);
    }

    public void endDocument() throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        try{
            if(fields.size() == 0) return;
            OAIField field = fields.pop();
            switch(field.type){
            case record:
                this.indexer.addCoreMetadataRecord(this.current);
                this.current = null;
                break;
            case header:
                break;
            case source:
                fields.peek().appendUri(field.data());
                break;
            case imageDesc:
                fields.peek().appendData(field.data());
                break;
            case imageUri:
                fields.peek().appendUri(field.data());
                break;
            case description:
                this.current.setSummary(field.data());
                break;
            case headerId:
                this.current.setControlName(field.data());
                break;
            case id:
                this.current.addIdentifier(field.data());
                break;
            case xref:
                this.current.addXref(field.data());
                break;
            case associatedName:
                this.current.setAssociatedName(field.data());
                break;
            case author:
                this.current.setAuthor(field.data());
                break;
            case dcmiDate:
                this.current.setDCMIDate(field.data());
                break;
            case lang:
                this.current.addLanguage(field.data());
                break;
            case medium:
                this.current.setMaterial(field.data());
                break;
            case provenance:
                this.current.addProvenance(field.data());
                break;
            case hasImage:
                URL url = new URL(field.uri());
                this.current.addWebImage(field.data(),url);
                break;
            case format:
                this.current.setPhysicalDescription(field.data());
                break;
            case title:
                this.current.setTitle(field.data());
                break;
            case notes:
                this.current.addGeneralNotes(field.data());
                break;
            case references:
                this.current.addPublication(field.data());
                break;
            case translation:
                this.current.setTranslation(field.data());
                break;
            case utc:
                this.current.setUTC(field.data());
                break;
            case subject:
                this.current.addSubjectSearchField(field.data());
                break;
            case resumptionToken:
                this.resumptionToken = field.data();
                break;
            }
            /*
             *         headerId, id, institution, all, associatedName, author, xref, dcmiDate, lang, provenance,
             *         hasImage, imageUri, imageDesc, notes, medium, format, title, references,
             *         description, source
             */
        }
        catch(IOException ioe){
            throw new SAXException(ioe);
        }
    }

    public void endPrefixMapping(String arg0) throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public void processingInstruction(String arg0, String arg1) throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public void setDocumentLocator(Locator arg0) {
        // TODO Auto-generated method stub
        
    }

    public void skippedEntity(String arg0) throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public void startDocument() throws SAXException {
        this.resumptionToken = null;
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if("OAI-PMH".equals(localName)){
            fields.push(OAIField.getStateField(OAIField.TYPES.oai_pmh));
        }
        else if("responseDate".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.responseDate));
        }
        else if("request".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.request));
        }
        else if("ListRecords".equals(localName)){
            fields.push(OAIField.getStateField(OAIField.TYPES.listRecords));
        }
        else if("record".equals(localName)){
            fields.push(OAIField.getStateField(OAIField.TYPES.record));
            this.current = new OAIRecord();
        }
        else if("header".equals(localName)){
            fields.push(OAIField.getStateField(OAIField.TYPES.header));
        }
        else if("metadata".equals(localName)){
            fields.push(OAIField.getStateField(OAIField.TYPES.metadata));
        }
        else if("identifier".equals(localName)){
            OAIField.TYPES type = (fields.peek().type == OAIField.TYPES.header)?OAIField.TYPES.headerId:OAIField.TYPES.id;
            fields.push(OAIField.getDataField(type));
        }
        else if("setSpec".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.setSpec));
        }
        else if("hasImage".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.hasImage));
        }
        else if("description".equals(localName)){
            if(fields.peek().type == OAIField.TYPES.hasImage){
                fields.push(OAIField.getDataField(OAIField.TYPES.imageDesc));
            }
            else{
                if("pi_dc:Translations".equals(atts.getValue("xsi:type"))){
                    fields.push(OAIField.getDataField(OAIField.TYPES.translation));
                }
                else{
                    fields.push(OAIField.getDataField(OAIField.TYPES.description));
                }
            }
        }
        else if("source".equals(localName)){
            OAIField.TYPES type = (fields.peek().type == OAIField.TYPES.hasImage)?OAIField.TYPES.imageUri:OAIField.TYPES.source;
            fields.push(OAIField.getDataField(type));
        }
        else if("format".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.format));
        }
        else if("provenance".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.provenance));
        }
        else if("language".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.lang));
        }
        else if("notes".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.notes));
        }
        else if("title".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.title));
        }
        else if("date".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.dcmiDate));
        }
        else if("references".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.references));
        }
        else if("creator".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.author));
        }
        else if("subject".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.subject));
        }
        else if("datestamp".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.utc));
        }
        else if("hasFormat".equals(localName)){
            String type = atts.getValue("xsi:type");
            if("pi_dc:Translations".equals(type)){
                fields.push(OAIField.getDataField(OAIField.TYPES.translationNote));
            }
            else fields.push(OAIField.getStateField(OAIField.TYPES.unknown));
        }
        else if("resumptionToken".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.resumptionToken));
        }
        else if("personalName".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.associatedName));
        }
        else if("xref".equals(localName)){
            fields.push(OAIField.getDataField(OAIField.TYPES.xref));
        }
        else{
            fields.push(OAIField.getStateField(OAIField.TYPES.unknown));
        }
    }

    public void startPrefixMapping(String arg0, String arg1) throws SAXException {
        // TODO Auto-generated method stub
        
    }

}
