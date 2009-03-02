package info.papyri.data.publication;

import info.papyri.digester.offline.APISTuple;
import info.papyri.metadata.CoreMetadataRecord;

import java.util.*;
public class ChicagoScrubber extends PublicationScrubber {
    public ChicagoScrubber(String pubInfo, APISTuple apis){
        super(pubInfo,apis);    
        init();
        
    }
    public ChicagoScrubber(String pubInfo, CoreMetadataRecord apis){
        super(pubInfo,apis);    
        init();
    }
    private void init(){
        String invPubs = this.inventory.replace("P. Chic. Haw.", "P.Chic.Haw.").replace("P. O.I.","POI");
            Collection<String> pubs = PublicationMatcher.findMatches(invPubs);
            for(String pub:pubs){
                addPublication(pub.replaceAll("\\$", "").replaceAll("\\s+"," "));
            }
    }
}
