package info.papyri.data.publication;

import info.papyri.metadata.CoreMetadataRecord;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import info.papyri.digester.offline.*;

public class PrincetonScrubber extends PublicationScrubber {
    static Pattern withside = Pattern.compile("(^.*?)(P\\.Princ\\.)(\\s+)([XIV]+)(\\s+)(\\d+)([rv])(.*?$)");
    public PrincetonScrubber(String pubInfo, CoreMetadataRecord apis){
        super(pubInfo,apis); 
        init();
    }
    public PrincetonScrubber(String pubInfo, APISTuple apis){
        super(pubInfo,apis);
        init();
    }
    
    private void init(){
        String [] pubs = getPublications().toArray(new String[0]);
        for (int i=0;i<pubs.length;i++){
            String old = pubs[i];
                Matcher m = withside.matcher(old);
                if (m.matches()){
                  String newVal = m.group(2) + " " + m.group(4) + " " + m.group(6) + " " + m.group(7).toUpperCase(); 
                  removePublication(old);
                  addPublication(newVal);
                }
                else{
                String inv = this.inventory.toLowerCase();
                if (inv.indexOf("verso") != -1){
                  addPublication(old + " V");    
                  removePublication(old);
                }
                else if (inv.indexOf("recto") != -1){
                    addPublication(old + " R");    
                    removePublication(old);
                }
                }
               
            }
            

    }
}