package info.papyri.data.publication;

import info.papyri.metadata.CoreMetadataRecord;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import info.papyri.digester.offline.APISTuple;

public class MichiganScrubber extends PublicationScrubber {

    public final static Pattern pmichSB = Pattern.compile("(^.*)(SB\\s[XIV]{1,4})(\\s\\d{1,5})(\\s?[a-zA-Z])?(((\\.\\d)?\\w)?(,\\s\\d)?)(.*$)");
    public final static  Pattern pmich = Pattern.compile("(^.*?)(PMich\\s)([XVI]+)(,\\s)(\\d+)([,\\s])(.*?$)");
    public final static  Pattern pmichTile = Pattern.compile("(^.*?)(PMich\\s)([XVI]+)(,\\s)(\\d+)(,\\stext\\s)([a-z0-9+])(,.*?$)");
    public final static  Pattern pmichKol = Pattern.compile("(^.*?)(PMich\\s)([XVI]+)(,\\s)(\\d+)(,\\scol\\.\\s)([XVI]+)((\\s\\()([a-z])(\\)))?(.*?$)");
    public final static  Pattern pmichCorn = Pattern.compile("(^.*)(PCorn\\s?)(,\\s)(\\d{1,5})(((\\.)?\\w)?(,\\s\\d)?)(.*$)");
    public final static  Pattern pmichNYU = Pattern.compile("(^.*)(P\\.NYU)(\\s)(\\d+\\s?)([a-z]$)");
    public final static  Pattern pmichCustoms = Pattern.compile("(^.*)(PCustoms)(\\,\\s)(\\d+\\-\\d+)(\\,.*$)");
    public final static  Pattern cpapjud = Pattern.compile("(^.*)(C\\.Pap\\.Jud\\.)(\\s)([XVI]+)(;\\s\\d\\d\\d\\d;\\sNo\\.\\s)(\\d+)(\\,.*$)");
    
    public MichiganScrubber(String pubInfo, CoreMetadataRecord apis) {
        super(pubInfo, apis);
        init();
    }
    public MichiganScrubber(String pubInfo, APISTuple apis) {
        super(pubInfo, apis);
        init();
    }
    
    
    private void init(){
        String [] pubs = getPublications().toArray(new String[0]);
        for (int i=0;i<pubs.length;i++){
            String old = pubs[i];
            System.out.println("old: " + old);
            Matcher sbmatcher = pmichSB.matcher(old);
            Matcher kolmatcher = pmichKol.matcher(old);
            Matcher plainMatcher = pmich.matcher(old);
            Matcher nyumatcher = pmichNYU.matcher(old);
            String actual = null;
            if (plainMatcher.matches()){
                actual = "P.Mich. " + plainMatcher.group(3) + " " + plainMatcher.group(5);
            }
            if (sbmatcher.matches()){
                System.out.println("SBMatcher...");
                actual = sbmatcher.group(2);
                String doc = sbmatcher.group(3);
                doc = doc.trim();
                String vol = info.papyri.util.VolumeUtil.romanForSBDoc(Integer.parseInt(doc));
                actual = "SB " + vol +  " " + doc;

                if(sbmatcher.group(5)!=null){ // numbered subdoc
                    actual += sbmatcher.group(5).replace('.', ' ');
                }
                if(sbmatcher.group(6) != null){
                    actual += sbmatcher.group(6).replaceAll(", "," (") + ")";
                }
                if (sbmatcher.group(4)!= null){ // lettered subdoc
                    actual += " " + sbmatcher.group(4);
                }
                if(notes.indexOf("Source of description: Recto + Verso") == -1){
                    if (notes.indexOf("Source of description: Recto") != -1){
                        actual += " Recto";
                    }
                    else if (notes.indexOf("Source of description: Verso") != -1){
                        actual += " Verso";
                    }
                }
                actual.replaceAll("\\s+"," ");
            }
            // PMich II, 128, col. I (a)
            if (kolmatcher.matches()){
                actual = "P.Mich. " + kolmatcher.group(3) + " " + kolmatcher.group(5) + " Kol. " + kolmatcher.group(7);
                if (kolmatcher.groupCount() > 9 && kolmatcher.group(10) != null) {
                    actual += " " + kolmatcher.group(10);
                }
                //System.out.println("Adding Mich: " + actual);
            }
            Matcher tileMatcher = pmichTile.matcher(old);
            if (tileMatcher.matches()){
                actual = "P.Mich. " + tileMatcher.group(3) + " " + tileMatcher.group(5) + " " + tileMatcher.group(7);
            }

            Matcher matcher = pmichCorn.matcher(old);
            if (matcher.matches()){
                actual = "P.Corn. " + matcher.group(4);
            }
            if (nyumatcher.matches()){
                String impliedVol = old.replaceAll("P.NYU ", "P.NYU I ");
                addPublication(impliedVol);
            }

            if (actual != null){
                actual = actual.replaceAll("\\s+", " ");
                removeParentPubs(actual);
                addPublication(actual);
                removePublication(old);
            }
            
        }
        Matcher customsMatcher = pmichCustoms.matcher(this.basePubInfo);
        if(customsMatcher.matches()){
            String newPub = "P.Customs " + customsMatcher.group(4).replaceAll("\\-"," - ");
            removePublication(this.basePubInfo);
            addPublication(newPub);
        }
        Matcher cpap = cpapjud.matcher(this.basePubInfo);
        if(cpap.matches()){
            String newPub = "C.Pap.Jud. " + cpap.group(4) + " " + cpap.group(6);
            removePublication(this.basePubInfo);
            addPublication(newPub);
        }
    }

}
