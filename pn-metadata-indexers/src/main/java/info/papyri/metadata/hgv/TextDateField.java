package info.papyri.metadata.hgv;
import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.metadata.OutOfRangeException;
import java.util.regex.*;
public class TextDateField extends EpiDocField {
    private static Pattern ATTEMPT = Pattern.compile("[-]?\\d+");
    private String start = null;
    private String end = null;
    private boolean uncertain = false;
    private StringBuffer value = new StringBuffer();
    public TextDateField(TYPE type, String docScope){
        super(type,docScope);
    }
    @Override
    public void appendUri(char[] buffer, int start, int length) {
    }

    @Override
    public void appendUri(CharSequence buffer) {
    }

    @Override
    public void appendValue(char[] buffer, int start, int length) {
        value.append(buffer,start,length);
    }

    @Override
    public void appendValue(CharSequence buffer) {
        value.append(buffer);
    }
    
    public void setIndexDate(String index, boolean end) throws OutOfRangeException {
        if("".equals(index)) return;
        if(end)this.end = index;
        else this.start = index;
    }
    
    public String getStartIndex() throws OutOfRangeException {
        if(this.start == null){
            if(this.end != null){
                int ix = this.end.indexOf('-',1);
                int e = Integer.MAX_VALUE;
                if(ix==-1){
                    e = Integer.parseInt(this.end);
                }
                else{
                    e = Integer.parseInt(this.end.substring(0,ix));
                }
                int s = (this.uncertain)?(e-CoreMetadataRecord.DATE_ARBITRATION_UNCERTAIN):(e-CoreMetadataRecord.DATE_ARBITRATION_CERTAIN);
                this.start = Integer.toString(s);
                return this.start;
            }
            Matcher m = ATTEMPT.matcher(this.value());
            if(m.find()){
                String val = m.group();
                if(val.charAt(0)!='-'){
                    if(this.value.indexOf("BCE") != -1 || this.value.indexOf("v.Chr.") != -1) val = ("-" + val);
                }
                this.start = val;
            }
        }
        return this.start;
    }
    public String getEndIndex()  {
        if(this.end==null){
            if(this.start != null){
                int ix = this.start.indexOf('-',1);
                int s = Integer.MIN_VALUE;
                if(ix==-1){
                    s = Integer.parseInt(this.start);
                }
                else{
                    s = Integer.parseInt(this.start.substring(0,ix));
                }
                int e = this.uncertain?(s+CoreMetadataRecord.DATE_ARBITRATION_UNCERTAIN):(s+CoreMetadataRecord.DATE_ARBITRATION_CERTAIN);
                this.end = Integer.toString(e);
                return this.end;
            }
        }
        return this.end;
    }
    
    public void setCertAtt(String value){
        if("low".equals(value)) this.uncertain = true;
    }

    @Override
    public String uri() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String value() {
        return value.toString();
    }

}
