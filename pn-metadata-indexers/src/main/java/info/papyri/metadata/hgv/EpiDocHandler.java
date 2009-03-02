package info.papyri.metadata.hgv;

import org.xml.sax.Attributes;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import info.papyri.data.publication.PublicationMatcher;
import info.papyri.metadata.*;
import info.papyri.util.DBUtils;
import info.papyri.util.NumberConverter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.net.URL;
public class EpiDocHandler implements ContentHandler {
    private HashMap<String,CoreMetadataRecord> records;
    private Stack<EpiDocField> fields;
    private String lang = null;
    private String ddbTitle = null;
    private String ddbdpId = null;
    private String ddbPlace = null;
    private String ddbDate1 = null;
    private String ddbDate2 = null;
    private Collection<String> ddbXrefs = null;
    private boolean cn = false;
    private boolean ddbdp = false;
    public EpiDocHandler(){

    }
    public Iterator<CoreMetadataRecord> getRecords(){
        return records.values().iterator();
    }
    public CoreMetadataRecord getRecord(String docScope){
        return records.get(docScope);
    }
    public void characters(char[] buffer, int start, int length) throws SAXException {
        if(fields.size()>0){
            fields.peek().appendValue(buffer, start, length);
        }
        else System.err.println("loose character data in epidoc: " + new String(buffer,start,length));
    }

    public void endDocument() throws SAXException {
        if(!cn){
            CoreMetadataRecord def = new EpiDocRecord();
            String uri = NamespacePrefixes.DDBDP + ddbdpId.replace(';', ':');
            def.setControlName(uri);
            def.addXref(uri);
            if(ddbXrefs != null){
                for(String s:ddbXrefs) def.addXref(s);
            }
            String [] idPub = ddbdpId.split(";");
            if(idPub.length > 0 && !"".equals(idPub[0])){
                try{
                    String series  = DBUtils.query(idPub[0], true);
                    if(!idPub[0].equals(series)){
                        if(idPub.length < 3){
                            def.addPublication(series + " " + idPub[1]);
                        }
                        else{
                            String vol = NumberConverter.getRoman(idPub[1]);
                            def.addPublication(series + " " + vol + " " + idPub[2]);
                        }
                    }
                }
                catch(SQLException se){
                    System.err.println(se.toString());
                    se.printStackTrace();
                }
            }
            def.setDate1(ddbDate1);
            if(ddbDate2 != null) def.setDate2(ddbDate2);
            if(ddbPlace != null) def.addProvenance(ddbPlace);
            if(ddbTitle != null) def.setTitle(ddbTitle);
            records.put(ddbdpId, def);
        }
        // debug
        for(String key:records.keySet()){
            CoreMetadataRecord rec = records.get(key);
            if(rec.getControlName() == null) System.err.println("no principal edition for " + key + "; removing");
        }
        // end debug

        if(lang != null){
            for(CoreMetadataRecord rec:records.values()){
                rec.addLanguage(lang);
            }
        }
    }

    public void endElement(String uri, String localName, String qName)
    throws SAXException {
        if(fields.size()==0) return;
        EpiDocField field = fields.pop();
        if(field.docScope == null) return;
        String [] scopes = field.docScope.split("\\s+");
        for(String docScope:scopes){
            if(docScope.startsWith("meta") || docScope.startsWith("tran"))  docScope = docScope.substring(docScope.indexOf('.')+1);
            CoreMetadataRecord record = records.get(docScope);
            if(record==null && field.value() != null ){
                boolean error = field.hasData(); // don't care about state fields
                boolean defaultData = (
                        field.type == EpiDocField.TYPE.ddbDate ||
                        field.type == EpiDocField.TYPE.ddbPlace ||
                        field.type == EpiDocField.TYPE.ddbXref ||
                        field.type == EpiDocField.TYPE.ddbTitle ||
                        field.type == EpiDocField.TYPE.placeName
                );
                if(docScope.equals(ddbdpId) && defaultData) error = false;
                if(error) {
                    System.err.println("missing metadata record for " + docScope + "; field type " + field.type+ "; field value " + field.value());
                    return;
                }
            }
            switch(field.type){
            case ddbXref:
                break;
            case ddbDate:
                String val = field.value();
                String val1,val2 = null;
                int i = val.indexOf('-',2);
                if(i > 2){ // midstring hyphen to indicate range
                    val1 = val.substring(0,i);
                    val2 = val.substring(i+1);
                }
                else val1 = val;
                if(val1.startsWith("BC")) val1 = "-" + val1.replace("BC", "").trim();
                else val1 = val1.replace("AD","").trim();
                if(val2 != null){
                    if(val2.startsWith("BC")) val2 = "-" + val2.replace("BC", "").trim();
                    else val2 = val2.replace("AD","").trim();
                }
                this.ddbDate1 = val1;
                this.ddbDate2 = val2;
                break;
            case ddbTitle:
                this.ddbTitle = field.value();
                break;
            case title:
                record.setTitle(field.value());
                break;
            case bl:
                record.addCorrectionNote(field.value());
                break;
            case placeName:
                if(ddbdpId.equals(field.docScope)){
                    this.ddbPlace = field.value();
                } else{
                    record.addProvenanceIndex(field.value());
                }
                break;
            case figure:
                String furl = field.uri();
                if(furl != null && furl.length() > 0){
                    try{
                        URL url = new URL(furl);
                        record.addWebImage(field.value(), url);
                        if(furl.startsWith(NamespacePrefixes.APIS_IMG_PREFIX)){
                            String apis = furl.substring(NamespacePrefixes.APIS_IMG_PREFIX.length());
                            int ix = apis.indexOf(".apis.");
                            if(ix != -1){
                                apis = (apis.substring(0,ix) + ":" + apis.substring(ix+6));
                                record.addXref(NamespacePrefixes.APIS + apis);
                            }
                        }
                    }
                    catch(java.io.IOException ioe){};
                }
                break;
            case figDesc:
                fields.peek().appendValue(field.value());
                break;
            case material:
                String data = field.value();
                if(data.startsWith("Papyr")) record.setMaterial("Papyri");
                else if(data.startsWith("Ostrac")) record.setMaterial("Ostraca");
                else record.setMaterial(data);
                break;
            case principalEdition:
                String id = field.value();
                record.addPublication(id.replace("Z.", "lines").replace("S.", "pages").replace("Kol.", "columns"));
                id = field.value().replaceAll("\\s+", "%20");
                id = "oai:papyri.info:identifiers:hgv:" +field.uri();
                record.setControlName(id);
                cn = true;
                break;
            case publication:
                record.addPublication(field.value().replace("Z.", "lines").replace("S.", "pages").replace("Kol.", "columns"));
                break;
            case ddbdp:
            case tm:
                record.addXref(field.value());
                break;
            case date_text:
                TextDateField tdf = (TextDateField)field;
                record.setDate1(tdf.value());
                try{
                    if(tdf.getEndIndex() != null) record.setDateIndexPair(tdf.getStartIndex(),tdf.getEndIndex(),true);
                    else record.setDateIndexPair(tdf.getStartIndex(),tdf.getStartIndex(),true);
                }
                catch(OutOfRangeException e){
                    System.err.println(e.toString());
                    record.addError(e.toString());
                }
                break;
            case date_mentioned:
                record.addMentionedDate(field.value());
                break;
            case notesIllustrations:
                String ill = field.value().trim().toLowerCase();
                if(!"keine".equals(ill)){ // sometimes instead of an empty field, the word "keine"
                    record.addIllustrationNote(field.value());
                }
                break;
            case notesTranslations:
                record.addTranslationNote(field.value());
                break;
            case keyword:
                record.addSubjectSearchField(field.value());
                break;
            case translationEN:
                record.setTranslation(field.value(),CoreMetadataRecord.ModernLanguage.ENGLISH);
                break;
            case translationDE:
                record.setTranslation(field.value(),CoreMetadataRecord.ModernLanguage.GERMAN);
                break;
            case translationFR:
                record.setTranslation(field.value(),CoreMetadataRecord.ModernLanguage.FRENCH);
                break;
            }
        }


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
        this.fields = new Stack<EpiDocField>();
        this.records = new HashMap<String,CoreMetadataRecord>();
        this.lang = null;
        this.ddbdpId = null;
        this.ddbXrefs = null;
        this.ddbDate1 = null;
        this.ddbDate2 = null;
        this.ddbTitle = null;
        this.ddbPlace = null;
        this.cn = false;
    }

    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        int start = fields.size();
        String docScope = atts.getValue("n");
        if("TEI.2".equals(localName)){
            this.ddbdp = true;
            this.ddbdpId = docScope; 
        }
        if(docScope != null){
            String [] scopes = docScope.split("\\s+");
            for(String scope:scopes){
                boolean meta = false;
                if( scope.matches("^meta\\.[0-9a-z]+$")){
                    scope = scope.substring("meta.".length());
                    meta = true;
                }
                if( scope.matches("^tran\\.[0-9a-z]+$")){
                    scope = docScope.substring("tran.".length());
                    meta = true;
                }
                if(meta){
                    if(!this.records.containsKey(scope)){
                        records.put(scope, new EpiDocRecord());
                    }
                }
            }
        }
        else{
            if(fields.size()>0){
                docScope = fields.peek().docScope;
            }
            else{
                System.err.println("empty stack for " + localName);
            }
        }
        String type = atts.getValue("type");
        String subtype = atts.getValue("subtype");
        if("text".equals(localName)){
            fields.push(EpiDocField.getStateField(EpiDocField.TYPE.container, ddbdpId));
        }
        else  if("head".equals(localName)){
            fields.push(EpiDocField.getStateField(EpiDocField.TYPE.head, docScope));
        }
        else if("div".equals(localName)){
            if("description".equals(type)){
                fields.push(EpiDocField.getStateField(EpiDocField.TYPE.container, docScope));
            }
            else if("commentary".equals(type)){
                if("general".equals(subtype)){
                    fields.push(EpiDocField.getDataField(EpiDocField.TYPE.notesGeneral, docScope));
                }
                else{
                    EpiDocField field = EpiDocField.getStateField(EpiDocField.TYPE.unknown, docScope);
                    fields.push(field);
                }
            }
            else if("bibliography".equals(type)){
                if("corrections".equals(subtype)){
                    fields.push(EpiDocField.getDataField(EpiDocField.TYPE.notesCorrections, docScope));
                }
                else if("illustrations".equals(subtype)){
                    fields.push(EpiDocField.getDataField(EpiDocField.TYPE.notesIllustrations, docScope));
                }
                else if("translations".equals(subtype)){
                    fields.push(EpiDocField.getDataField(EpiDocField.TYPE.notesTranslations, docScope));
                }
                else{
                    EpiDocField field = EpiDocField.getStateField(EpiDocField.TYPE.unknown, docScope);
                    fields.push(field);
                }
            }
            else if("translation".equals(type)){
                String lang = atts.getValue("lang");
                if("de".equals(lang)){
                    fields.push(EpiDocField.getDataField(EpiDocField.TYPE.translationDE, docScope));
                }
                else if("fr".equals(lang)){
                    fields.push(EpiDocField.getDataField(EpiDocField.TYPE.translationFR, docScope));
                }
                else{
                    fields.push(EpiDocField.getDataField(EpiDocField.TYPE.translationEN, docScope));
                }
            }
            else{
                if("edition".equals(type)){
                    String lang = atts.getValue("lang");
                    if(lang != null){
                        setLang(lang);
                    }
                }
                EpiDocField field = EpiDocField.getStateField(EpiDocField.TYPE.unknown, docScope);
                fields.push(field);
            }
        }
        else if("titleStmt".equals(localName)){
            EpiDocField field = EpiDocField.getStateField(EpiDocField.TYPE.titleContainer, docScope);
            fields.push(field);
        }
        else if("title".equals(localName)){
            if(fields.peek().type == EpiDocField.TYPE.titleContainer){
                EpiDocField field = (docScope.equals(ddbdpId))?EpiDocField.getDataField(EpiDocField.TYPE.ddbTitle,docScope):EpiDocField.getDataField(EpiDocField.TYPE.title,docScope);
                fields.push(field);
            }
            else if(fields.peek().type == EpiDocField.TYPE.principalEdition && "abbreviated".equals(type)){
                fields.push(PublicationField.getDataProxy(EpiDocField.TYPE.pub_series, (PublicationField)fields.peek()));
            }
            else{
                EpiDocField field = EpiDocField.getDataField(EpiDocField.TYPE.unknown, docScope);
                fields.push(field);
            }
        }
        else if("keywords".equals(localName)){
            EpiDocField field = EpiDocField.getStateField(EpiDocField.TYPE.keywordContainer, docScope);
            fields.push(field);
        }
        else if("term".equals(localName)){
            EpiDocField field = EpiDocField.getDataField(EpiDocField.TYPE.keywordContainer, docScope);
            fields.push(field);
        }
        else if("p".equals(localName)){
            EpiDocField field = EpiDocField.getDataProxy(EpiDocField.TYPE.unknown, fields.peek());
            fields.push(field);
        }
        else if("app".equals(localName)){
            EpiDocField field = EpiDocField.getDataProxy(EpiDocField.TYPE.unknown, fields.peek());
            fields.push(field);
        }
        else if("lem".equals(localName)){
            EpiDocField field = EpiDocField.getDataProxy(EpiDocField.TYPE.unknown, fields.peek());
            fields.push(field);
        }
        else if("figure".equals(localName)){
            EpiDocField field = (ddbdpId.equals(docScope))?EpiDocField.getStateField(EpiDocField.TYPE.unknown, docScope):EpiDocField.getDataField(EpiDocField.TYPE.figure, docScope);
            field.appendUri(atts.getValue("href"));
            fields.push(field);
        }
        else if("figDesc".equals(localName)){
            fields.push(EpiDocField.getDataField(EpiDocField.TYPE.figDesc, docScope));
        }
        else if("placeName".equals(localName)){
            fields.push(EpiDocField.getDataField(EpiDocField.TYPE.placeName, docScope));
        }
        else if("geogName".equals(localName)){
            fields.push(EpiDocField.getDataField(EpiDocField.TYPE.placeName, docScope));
        }
        else if("bibl".equals(localName)){
            if("Trismegistos".equals(type)){
                fields.push(PublicationField.getTMField(docScope));
            }
            else if("DDbDP".equals(type)){
                fields.push(PublicationField.getDDBField(docScope));
            }
            else if("BL".equals(type)){
                fields.push(PublicationField.getBLField(docScope));
            }
            else if("translations".equals(type)){
                fields.push(EpiDocField.getDataField(EpiDocField.TYPE.notesTranslations, docScope));
            }
            else if("illustration".equals(type)){
                fields.push(EpiDocField.getDataField(EpiDocField.TYPE.notesIllustrations, docScope));
            }
            else {
                if("principal".equals(atts.getValue("subtype"))){
                    fields.push(PublicationField.getPrincipalPublicationField(docScope));
                }
                else  fields.push(PublicationField.getPublicationField(docScope));
            }
        }
        else if("biblScope".equals(localName)){
            EpiDocField.TYPE fType;
            if("numbers".equals(type)){
                fields.push(PublicationField.getDataProxy(EpiDocField.TYPE.pub_number, (PublicationField)fields.peek()));
            }
            else if("volume".equals(type)){
                fields.push(PublicationField.getDataProxy(EpiDocField.TYPE.pub_volume, (PublicationField)fields.peek()));
            }
            else if("parts".equals(type)){
                fields.push(PublicationField.getDataProxy(EpiDocField.TYPE.pub_part, (PublicationField)fields.peek()));
            }
            else if("fascicle".equals(type)){
                EpiDocField field = EpiDocField.getStateField(EpiDocField.TYPE.unknown, docScope);
                fields.push(field);
            }
            else{
                fields.push(PublicationField.getDataProxy(EpiDocField.TYPE.pub_sub, (PublicationField)fields.peek()));
            }
        }
        else if("series".equals(localName)){
            fields.push(PublicationField.getDataProxy(EpiDocField.TYPE.pub_series, (PublicationField)fields.peek()));
        }
        else if("rs".equals(localName)){
            if("material".equals(type)){
                fields.push(EpiDocField.getDataField(EpiDocField.TYPE.material, docScope));
            }
            else if("textType".equals(type)){
                if(fields.peek().type == EpiDocField.TYPE.keywordContainer){
                    EpiDocField field = EpiDocField.getDataField(EpiDocField.TYPE.keyword, docScope);
                    fields.push(field);
                }
                else{
                    EpiDocField field = EpiDocField.getDataProxy(EpiDocField.TYPE.unknown, fields.peek());
                    fields.push(field);
                }
            }
            else{
                EpiDocField field = EpiDocField.getStateField(EpiDocField.TYPE.unknown, docScope);
                fields.push(field);
            }
        }
        else if("date".equals(localName)){
            if("textDate".equals(type)){
                TextDateField field = new TextDateField(EpiDocField.TYPE.date_text, docScope);
                fields.push(field);
                String notBefore = atts.getValue("value");
                if(notBefore == null) notBefore = atts.getValue("notBefore");
                if(notBefore !=  null){
                    try{
                        field.setIndexDate(notBefore, false);
                    }
                    catch(OutOfRangeException e){};
                }
                String notAfter = atts.getValue("notAfter");
                if(notAfter != null){
                    try{
                        field.setIndexDate(notAfter, true);
                    }
                    catch(OutOfRangeException e){};
                }
            }
            else if("mentioned".equals(type)){
                EpiDocField field = EpiDocField.getDataField(EpiDocField.TYPE.date_mentioned, docScope);
                fields.push(field);
            }
            else if(fields.peek().type == EpiDocField.TYPE.head){
                EpiDocField field = EpiDocField.getDataField(EpiDocField.TYPE.ddbDate, docScope);
                fields.push(field);
            }    
            else{
                EpiDocField field = EpiDocField.getStateField(EpiDocField.TYPE.unknown, docScope);
                fields.push(field);
            }
        }
        else if("xref".equals(localName)){
            String n = atts.getValue("n");
            if(n != null){
                n = n.replace(';', ':');
                String series = n.substring(0,n.indexOf(':'));

                try{
                    series = PublicationMatcher.matchSeries(series)[1];
                    String ddbSeries = DBUtils.query(series, true);
                    if(ddbSeries.startsWith(DBUtils.PERSEUS_PREFIX)) ddbSeries = ddbSeries.substring(DBUtils.PERSEUS_PREFIX.length());
                    if(this.ddbXrefs == null) this.ddbXrefs = new HashSet<String>();
                    this.ddbXrefs.add(NamespacePrefixes.DDBDP + ddbSeries + n.substring(n.indexOf(':')));
                }
                catch(SQLException s){}
            }
            EpiDocField field = EpiDocField.getStateField(EpiDocField.TYPE.unknown, docScope);
            fields.push(field);
        }
        else{
            EpiDocField field = EpiDocField.getStateField(EpiDocField.TYPE.unknown, docScope);
            fields.push(field);
        }
        if(fields.size() <= start) throw new RuntimeException("No push for " + localName);
    }

    public void startPrefixMapping(String arg0, String arg1)
    throws SAXException {
        // TODO Auto-generated method stub

    }

    private void setLang(String lang) {
        if("grc".equals(lang)){
            this.lang = RFC3066.GREEK;
        }
        else if ("la".equals(lang)){
            this.lang = RFC3066.LATIN;
        }
        else if ("grc-Latn".equals(lang)){
            this.lang = RFC3066.GREEK_IN_LATIN;
        }
        else if("cop".equals(lang)){
            this.lang = RFC3066.COPTIC;
        }
        else if("it".equals(lang)){
            this.lang = RFC3066.ITALIAN;
        }
        else if(lang != null && lang.length() == 2){
            this.lang = lang;
        }
    }

}
