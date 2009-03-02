package info.papyri.metadata.apis;
import info.papyri.metadata.*;
public abstract class OAIField {
    public static enum TYPES {
        oai_pmh, responseDate, request, listRecords, record, header, metadata,
         headerId, id, institution, all, associatedName, author, xref, dcmiDate, lang, provenance,
        hasImage, imageUri, imageDesc, notes,  medium, format, title, references,
        description, source, resumptionToken, translation, subject, utc, setSpec, unknown,
        translationNote, transcriptionNote
    }
    OAIField.TYPES type;
    public abstract void appendData(char [] buffer, int start, int length);
    public abstract void appendUri(char [] buffer, int start, int length);
    public abstract void appendData(CharSequence data);
    public abstract void appendUri(CharSequence uri);
    public abstract boolean writeable();
    public abstract String data();
    public abstract String uri();
    public static OAIField getDataField(final OAIField.TYPES type){
        OAIField result =  new OAIField(){
            private StringBuffer data = new StringBuffer();
            private StringBuffer uri = new StringBuffer();
            public void appendData(char [] buffer, int start, int length){
                this.data.append(buffer,start,length);
            }
            public void appendUri(char [] buffer, int start, int length){
                this.uri.append(buffer,start,length);
            }
            public void appendData(CharSequence data){
                this.data.append(data);
            }
            public void appendUri(CharSequence uri){
                this.uri.append(uri);
            }
            public String data(){ return data.toString(); }
            public String uri(){ return uri.toString(); }
            public boolean writeable(){return true;}
        };
        result.type = type;
        return result;
    }
    
    public static OAIField getStateField(final OAIField.TYPES type){
        OAIField result =  new OAIField(){
            public void appendData(char [] buffer, int start, int length){
                throw new UnsupportedOperationException("State field has no data");
            }
            public void appendUri(char [] buffer, int start, int length){
                throw new UnsupportedOperationException("State field has no data");
            }
            public void appendData(CharSequence data){
                throw new UnsupportedOperationException("State field has no data");
            }
            public void appendUri(CharSequence uri){
                throw new UnsupportedOperationException("State field has no data");
            }
            public String data(){
                throw new UnsupportedOperationException("State field has no data");
            }
            public String uri(){
                throw new UnsupportedOperationException("State field has no data");
            }
            public boolean writeable(){return false;}
        };
        result.type = type;
        return result;
        }
}
