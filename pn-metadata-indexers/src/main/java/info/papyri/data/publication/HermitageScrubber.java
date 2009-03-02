package info.papyri.data.publication;

import info.papyri.metadata.CoreMetadataRecord;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import info.papyri.digester.offline.*;

public class HermitageScrubber extends PublicationScrubber {
    static Pattern prg = Pattern.compile("(^.*?)(P\\.Ross\\.Georg\\.)(\\s+)([XIV]+)(\\s+)(\\d+)(\\s)([XIV]+,[a-z])(.*?$)");
    public HermitageScrubber(String pubInfo, CoreMetadataRecord apis){
        super(pubInfo,apis); 
        init();
    }
    public HermitageScrubber(String pubInfo, APISTuple apis){
        super(pubInfo,apis);
        init();
        for (String pub:corrected){
            apis.removePublication(pub);
            removePublication(pub);
        }
        for (String pub:corrections){
            apis.addPublication(pub);
            addPublication(pub);
        }
    }
    
    private void init(){
        java.util.Set<String> pubSet = new java.util.HashSet<String>();
        pubSet.addAll(getPublications());
        String [] split = basePubInfo.split("\\|");
        //System.out.println("base pub: " + basePubInfo);
        for (String s:split){
            pubSet.add(s.trim());
        }
        String [] pubs = pubSet.toArray(new String[0]);
        for (int i=0;i<pubs.length;i++){
            String old = pubs[i];
                Matcher m = prg.matcher(old);
                if (m.matches()){
                  String newVal = m.group(2) + " " + m.group(4) + " " + m.group(6) + " " + m.group(8).replace(',', ' '); 
                  //System.err.println(newVal);
                  this.corrected.add(old);
                  
                  this.corrections.add(newVal);
                }
                else{
                    //@TODO Fix the regex to pick up the line range here, cf. hermitage.apis.15
                    if(old.equals("P.Lond. IV 1390.1")){
                        removeParentPubs(old);
                        addPublication("P.Lond. IV 1390");
                    }
                }
              
            }
            

    }
}