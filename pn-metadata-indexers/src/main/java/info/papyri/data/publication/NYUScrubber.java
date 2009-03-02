package info.papyri.data.publication;

import info.papyri.metadata.CoreMetadataRecord;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import info.papyri.digester.offline.*;

public class NYUScrubber extends PublicationScrubber {
    static Pattern pNYURANGE = Pattern.compile("(^.*?)(P\\.\\sNYU\\s)([XIV]+)(\\s?,\\s)(\\d+)(-)(\\d+)(,.*?$)");
    static Pattern pNYU = Pattern.compile("(^.*?)(P\\.\\sNYU\\s)([XIV]+)(\\s?,\\s)(\\d+)(,.*?$)");
    static String DESC_ONLY = "(desc. only)";
    public NYUScrubber(String pubInfo, CoreMetadataRecord apis){
        super(pubInfo,apis);    
        init();    
    }
    public NYUScrubber(String pubInfo, APISTuple apis){
        super(pubInfo,apis);    
        init();    
    }
    
    private void init(){
        String [] pubs = getPublications().toArray(new String[0]);
        for (int i=0;i<pubs.length;i++){
            String old = pubs[i];
            Matcher m = pNYURANGE.matcher(old);
            Matcher m2 = pNYU.matcher(old);
            if (m.matches()){
                int start = Integer.parseInt(m.group(5));
                int end = Integer.parseInt(m.group(7));
                if (end < start){
                    end += (start / 10) * 10;
                }
                for (int j=start;j<=end;j++){
                    if (m.group(3) != null)
                    addPublication("P.NYU " + m.group(3) + " "  + j);    
                }
            }
            else if (m2.matches()){
                if (m2.group(5) != null)
                addPublication("P.NYU " + m2.group(3) + " " + m2.group(5));
            }
                
            }
    }
}