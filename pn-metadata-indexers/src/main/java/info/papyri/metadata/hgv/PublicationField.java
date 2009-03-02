package info.papyri.metadata.hgv;

import java.util.regex.Pattern;


public abstract class PublicationField extends EpiDocField {
    private static final Pattern DIGITS = Pattern.compile("^\\d+$");
    protected StringBuffer series = new StringBuffer();
    protected StringBuffer volume = new StringBuffer();
    protected StringBuffer numbers = new StringBuffer();
    protected StringBuffer parts = new StringBuffer();
    protected StringBuffer subParts = new StringBuffer();
    private PublicationField(TYPE type, String docScope){
        super(type,docScope);
    }
    public void appendValue(char [] buffer, int start, int length){
    }
    public void appendValue(CharSequence buffer){
    }
    public void appendUri(char [] buffer, int start, int length){
    }
    public void appendUri(CharSequence buffer){
    }
    public String uri() {
        return null;
    }
    public abstract String value();
    public void appendVolume(char [] buffer, int start, int length){
        volume.append(buffer,start,length);
    }
    public void appendVolume(CharSequence buffer){
        volume.append(buffer);
    }
    public void appendSeries(char [] buffer, int start, int length){
        series.append(buffer,start,length);
    }
    public void appendSeries(CharSequence buffer){
        series.append(buffer);
    }
    public void appendNumber(char [] buffer, int start, int length){
        numbers.append(buffer,start,length);
    }
    public void appendNumber(CharSequence buffer){
        numbers.append(buffer);
    }
    public void appendParts(char [] buffer, int start, int length){
        parts.append(buffer,start,length);
    }
    public void appendParts(CharSequence buffer){
        parts.append(buffer);
    }
    public void appendSubParts(char [] buffer, int start, int length){
        subParts.append(buffer,start,length);
    }
    public void appendSubParts(CharSequence buffer){
        subParts.append(buffer);
    }
    public static PublicationField getPrincipalPublicationField(final String docScope){
        return new Publication(EpiDocField.TYPE.principalEdition,docScope);
    }
    public static PublicationField getPublicationField(final String docScope){
        return new Publication(EpiDocField.TYPE.publication,docScope);
    }
    public static PublicationField getDDBField(final String docScope){
        return new DDBPublication(EpiDocField.TYPE.ddbdp,docScope);
    }
    public static PublicationField getTMField(final String docScope){
        return new TMPublication(EpiDocField.TYPE.tm,docScope);
    }
    public static PublicationField getBLField(final String docScope){
        PublicationField result =  new BLPublication(EpiDocField.TYPE.bl,docScope);
        result.appendSeries("BL");
        return result;
    }
    public static PublicationField getDataProxy(TYPE type, PublicationField delegate){
        switch(type){
        case pub_volume:
            return new VolumeProxy(type,delegate);
        case pub_series:
            return new SeriesProxy(type,delegate);
        case pub_number:
            return new NumbersProxy(type,delegate);
        case pub_part:
            return new PartsProxy(type,delegate);
        case pub_sub:
            return new SubPartsProxy(type,delegate);
        }
        throw new IllegalArgumentException("bad type proxy type: " + type);
    }
    private static class Publication extends PublicationField{
        private StringBuffer value = new StringBuffer();
        Publication(TYPE type, String docScope){
            super(type,docScope);
        }
        public void appendValue(char [] buffer, int start, int length){
            this.value.append(buffer,start,length);
        }
        public void appendValue(CharSequence buffer){
            this.value.append(buffer);
        }
        public String uri(){
            if(series.length()==0){
                System.err.println("No series for uri, volume buffer= " + this.volume.toString());
                return "";
            }
            StringBuffer result = new StringBuffer();
            result.append(series);
            result.append(':');
            if(volume.length()!=0){
                result.append(volume);
            }
            result.append(':');
            result.append(numbers);
            if(parts.length() > 0){
                result.append(':');
                result.append(parts);
            }
            if(subParts.length() > 0){
                result.append(':');
                result.append(subParts);
            }
            return result.toString().replaceAll("\\s+", "%20");
        }
        public String value(){
            if(series.length()==0){
                return this.value.toString();
            }
            StringBuffer result = new StringBuffer();
            result.append(series);
            if(volume.length()==0){
                result.append(' ');
            }
            else{
                String vol = this.volume.toString();
                if(DIGITS.matcher(vol).matches()){
                    vol = info.papyri.util.NumberConverter.getRoman(vol);
                }
                result.append(' ');
                result.append(vol);
                result.append(' ');
            }
            result.append(numbers);
            if(parts.length() > 0){
                result.append(' ');
                result.append(parts);
            }
            if(subParts.length() > 0){
                result.append(' ');
                result.append(subParts);
            }
            return result.toString();
        }
    }
    private static class TMPublication extends PublicationField{
        TMPublication(TYPE type, String docScope){
            super(type,docScope);
        }
        public String value(){
            return "oai:papyri.info:identifiers:trismegistos:" + numbers.toString();
        }
    }
    private static class BLPublication extends PublicationField{
        BLPublication(TYPE type, String docScope){
            super(type,docScope);
        }
        public String value(){
            return "BL " + volume.toString() + ", pages " + subParts.toString();
        }
    }
    private static class DDBPublication extends PublicationField{
        DDBPublication(TYPE type, String docScope){
            super(type,docScope);
        }
        public String value(){
            return "oai:papyri.info:identifiers:ddbdp:" + series.toString() + ":" + volume.toString() + ":" + numbers.toString();
        }
    }
    private static class SeriesProxy extends PublicationField{
        final PublicationField delegate;
        SeriesProxy(TYPE type, PublicationField delegate){
            super(type,delegate.docScope);
            this.delegate = delegate;
        }
        public String value(){
            return null;
        }
        public void appendValue(char [] buffer, int start, int length){
            delegate.appendSeries(buffer,start,length);
        }
        public void appendValue(CharSequence buffer){
            delegate.appendSeries(buffer);
        }
    }
    private static class VolumeProxy extends PublicationField{
        final PublicationField delegate;
        VolumeProxy(TYPE type, PublicationField delegate){
            super(type,delegate.docScope);
            this.delegate = delegate;
        }
        public String value(){
            return null;
        }
        public void appendValue(char [] buffer, int start, int length){
            delegate.appendVolume(buffer,start,length);
        }
        public void appendValue(CharSequence buffer){
            delegate.appendVolume(buffer);
        }
    }
    
    private static class NumbersProxy extends PublicationField{
        final PublicationField delegate;
        NumbersProxy(TYPE type, PublicationField delegate){
            super(type,delegate.docScope);
            this.delegate = delegate;
        }
        public String value(){
            return null;
        }
        public void appendValue(char [] buffer, int start, int length){
            delegate.appendNumber(buffer,start,length);
        }
        public void appendValue(CharSequence buffer){
            delegate.appendNumber(buffer);
        }
    }
    
    private static class PartsProxy extends PublicationField{
        final PublicationField delegate;
        PartsProxy(TYPE type, PublicationField delegate){
            super(type,delegate.docScope);
            this.delegate = delegate;
        }
        public String value(){
            return null;
        }
        public void appendValue(char [] buffer, int start, int length){
            delegate.appendParts(buffer,start,length);
        }
        public void appendValue(CharSequence buffer){
            delegate.appendParts(buffer);
        }
    }    
    private static class SubPartsProxy extends PublicationField{
        final PublicationField delegate;
        SubPartsProxy(TYPE type, PublicationField delegate){
            super(type,delegate.docScope);
            this.delegate = delegate;
        }
        public String value(){
            return null;
        }
        public void appendValue(char [] buffer, int start, int length){
            delegate.appendSubParts(buffer,start,length);
        }
        public void appendValue(CharSequence buffer){
            delegate.appendSubParts(buffer);
        }
    }}
