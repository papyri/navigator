package info.papyri.metadata.hgv;

public abstract class EpiDocField {
    public static enum TYPE{
        container, ddbDate, ddbXref, ddbPlace, ddbTitle, head,
        sourceDesc, keywordContainer, keyword, rs, placeName,
        notesGeneral, notesIllustrations, notesCorrections, notesTranslations,
        material, textDate, figure, figDesc, dataProxy, date_text, date_mentioned,
        ddbdp, tm, publication, principalEdition, bl, title, titleContainer,
        pub_series, pub_volume, pub_number, pub_part, pub_sub,
        unknown, translationDE, translationEN, translationFR
    }
    public final TYPE type;
    public final String docScope;
    protected EpiDocField(TYPE type, String docScope){
        this.type = type;
        this.docScope = docScope;
    }
    public abstract void appendValue(char [] buffer, int start, int length);
    public abstract void appendValue(CharSequence buffer);
    public abstract String value();
    public abstract void appendUri(char [] buffer, int start, int length);
    public abstract void appendUri(CharSequence buffer);
    public abstract String uri();
    public boolean hasData(){
        return true;
    }
    public static EpiDocField getStateField(final TYPE type, final String docScope){
        EpiDocField result =  new  EpiDocField(type,docScope){
            public void appendValue(char [] buffer, int start, int length){
            }
            public void appendValue(CharSequence buffer){
            }
            public String value() {
                return null;
            }
            public void appendUri(char [] buffer, int start, int length){
            }
            public void appendUri(CharSequence buffer){
            }
            public String uri() {
                return null;
            }
            //@override
            public boolean hasData(){return false;};
            };
            return result;
    }
    public static EpiDocField getDataField(final TYPE type, final String docScope){
        EpiDocField result =  new  EpiDocField(type,docScope){
            private StringBuffer value = new StringBuffer();
            private StringBuffer uri = new StringBuffer();
            public void appendValue(char [] buffer, int start, int length){
                this.value.append(buffer,start,length);
            }
            public void appendValue(CharSequence buffer){
                this.value.append(buffer);
            }
            public String value() {
                return this.value.toString();
            }
            public void appendUri(char [] buffer, int start, int length){
                uri.append(buffer,start,length);
            }
            public void appendUri(CharSequence buffer){
                uri.append(buffer);
            }
            public String uri() {
                return this.uri.toString();
            }
            };
            return result;
   }
    public static EpiDocField getDataProxy(final TYPE type,final EpiDocField delegate){
        EpiDocField result =  new  EpiDocField(type,delegate.docScope){
            public void appendValue(char [] buffer, int start, int length){
                delegate.appendValue(buffer,start,length);
            }
            public void appendValue(CharSequence buffer){
                delegate.appendValue(buffer);
            }
            public String value() {
                return delegate.value();
            }
            public void appendUri(char [] buffer, int start, int length){
                delegate.appendUri(buffer,start,length);
            }
            public void appendUri(CharSequence buffer){
                delegate.appendUri(buffer);
            }
            public String uri() {
                return delegate.uri();
            }
            };
            return result;
   }    
}
