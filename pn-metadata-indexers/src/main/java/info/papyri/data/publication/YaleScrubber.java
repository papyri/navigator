package info.papyri.data.publication;

import info.papyri.metadata.CoreMetadataRecord;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import info.papyri.digester.offline.*;

public class YaleScrubber extends PublicationScrubber {
    static Pattern SB_MESS = Pattern.compile("(^P\\.Yale\\sinv\\.)(\\s+)(\\d+)([a-z])(SB\\s)([XIV]+)(\\s+)(\\d+)((\\s\\|.*)?$)");
    static Pattern POXY_NO_VOL = Pattern.compile("^P\\.Oxy\\.\\s+(\\d+)$");
    static Pattern YOUTIE_NO_VOL = Pattern.compile("^P\\.Coll\\.Youtie\\s\\d+$");
    public YaleScrubber(String pubInfo, CoreMetadataRecord apis){
        super(pubInfo,apis);
        init();
    }
    public YaleScrubber(String pubInfo, APISTuple apis){
        super(pubInfo,apis);
        init();
    }
    
    private void init(){
        System.out.println(this.controlName + ": " + this.basePubInfo);
        Matcher m = SB_MESS.matcher(this.basePubInfo.trim());
        if(m.matches()){
            String replace = m.group(5) + m.group(6) + " " + m.group(8) + " " + m.group(4);
            clearPublications();
            addPublication(replace);
            System.out.println("Added " + replace);
            replace = "P.Yale inv. " + m.group(3) + m.group(4);
            addPublication(replace);
            System.out.println("Added " + replace);
        }
        
        for(String orig:getPublications().toArray(new String[0])){
            System.out.println("Orig:\"" + orig + "\"");
            String pub = orig.replaceAll("P\\."," P.").replaceAll("SB"," SB").replaceAll("Rom\\."," Rom.").replaceAll("Ch\\."," Ch.").trim();
            System.out.println("Pub :\"" + pub + "\"");

            Collection<String> matches = PublicationMatcher.findMatches(pub);
            String [] matchArray = matches.toArray(new String[0]);
            for (String match:matchArray){
                match = match.replace('$', ' ').replaceAll("\\s+", " ");
                System.out.println("matched: \"" + match + "\"");

                    m = POXY_NO_VOL.matcher(match.trim());
                    String vol = null;
                    if(m.matches()){
                        int doc = Integer.parseInt(m.group(1));
                        if(doc < 208){
                            vol = "I";
                        }
                        else if (doc < 401){
                            vol = "II";
                        }
                        else if (doc < 654){
                            vol = "III";
                        }
                        else if (doc < 840){
                            vol = "IV";
                        }
                        else if (doc < 845){
                            vol = "V";
                        }
                        else if (doc < 1007){
                            vol = "VI";
                        }
                        else if (doc < 10073){
                            vol = "VII";
                        }
                        else if (doc < 1166){
                            vol = "VIII";
                        }
                        if(vol != null){
                            removePublication(orig);
                            match = match.replaceAll("\\s{2,}"," ");
                            String replace = match.replace("P.Oxy. ","P.Oxy. " + vol + " ");
                            addPublication(replace);
                        }
                    }
                    else {
                        m = YOUTIE_NO_VOL.matcher(match.trim());

                        if(m.matches()){
                            vol = null;
                            int doc = Integer.parseInt(match.substring(15));
                            if(doc < 66){
                                vol = "I";
                            }
                            else{
                                vol = "II";
                            }
                            removePublication(orig);
                            String replace = match.replace("Youtie ","Youtie " + vol + " ");
                            addPublication(replace);
                        }
                }
            }
        }

    }
}