package info.papyri.data.publication;

import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.util.NumberConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.papyri.digester.offline.*;

public class TorontoScrubber extends PublicationScrubber {
    static Pattern withside = Pattern.compile("(^.*?)(P\\.[A-Z][a-z]+\\.?)(\\s+)([XIV]+)(\\s+)(\\d+)([rv])(.*?$)");
    static Pattern tpub = Pattern.compile("(^.*?)(P\\.[A-Z][a-z]+\\.)(\\d+)(\\s+)(\\d+)(.*?$)");
    public TorontoScrubber(String pubInfo, CoreMetadataRecord apis){
        super(pubInfo,apis);  
        init();
    }
    public TorontoScrubber(String pubInfo, APISTuple apis){
        super(pubInfo,apis);  
        init();
    }
    
    private void init(){
        Matcher m = withside.matcher(this.inventory);
        if (m.matches()){         
            String [] pubs = getPublications().toArray(new String[0]);
            for (int i=0;i<pubs.length;i++){
                String old = pubs[i];
                String newS = old + " " + m.group(7).toUpperCase();
                addPublication(newS);
            }
        }else {
        
            String [] pubs = getPublications().toArray(new String[0]);
            for (int i=0;i<pubs.length;i++){
            Matcher m2 = tpub.matcher(pubs[i]);
            if (m2.matches()){
               String newPub = m2.group(2) + " " + NumberConverter.getRoman(m2.group(3)) + " " + m2.group(5);
               addPublication(newPub);
            }
            
        }
    }
        }
}