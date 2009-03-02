package info.papyri.data.publication;

import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.util.NumberConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import info.papyri.digester.offline.APISTuple;

public class WisconsinScrubber extends PublicationScrubber {
    static Pattern withside = Pattern.compile("(^.*?)(PWisc)(\\s+)([XIV]+)(\\,\\s+)(\\d+)\\s([rvRV])(.*?$)");

    public WisconsinScrubber(String pubInfo, CoreMetadataRecord apis){
        super(pubInfo,apis);  
        init();
    }
    public WisconsinScrubber(String pubInfo, APISTuple apis){
        super(pubInfo,apis);  
        init();
    }
    
    private void init(){
        Matcher m = withside.matcher(this.basePubInfo);
        if (m.matches()){         
            String newPub = "P.Wisc. " + m.group(4) + " " + m.group(6) + " " + m.group(7).toUpperCase();
            String [] pubs = getPublications().toArray(new String[0]);
            for (int i=0;i<pubs.length;i++){
                String old = pubs[i];
                if(newPub.startsWith(old)){
                    removePublication(old);
                    addPublication(newPub);
                }
            }
        }
    }

}