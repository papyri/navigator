package info.papyri.data.publication;

import info.papyri.metadata.CoreMetadataRecord;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import info.papyri.digester.offline.*;

public class ColumbiaScrubber extends PublicationScrubber {
    static final Pattern sidedSubdocRangePatt = Pattern.compile("(^P\\.Col\\.\\s[XVI]+\\s\\d+\\s([Rr](ecto)?|[Vv](erso)?)\\s)(\\d)([ab])(\\s?[+-]\\s?)(\\d)([ab])(.*?$)");
    static final Pattern subdocPatt = Pattern.compile("(^P\\.Col\\.)(Zen\\.)?(\\s[XIV]+\\s[\\d]+)([a-z])$");
    static final Pattern parenPatt = Pattern.compile("(^P\\.Col\\.)(Zen\\.)?(\\s[XIV]+\\s[\\d]+)(\\s?[(][a-z][)])$");
    static final Pattern vol2 = Pattern.compile("^P\\.Col\\. II[^XIV]");
    static final Pattern vol5 = Pattern.compile("^P\\.Col\\. V[^XIV]");
    static final Pattern OXY = Pattern.compile("^(Oxy\\.\\s)(\\d+)(.*$)");
    static final Pattern FAY = Pattern.compile("^(Fay\\.\\s)(\\d+)(.*$)");
    public ColumbiaScrubber(String pubInfo, APISTuple apis) {
        super(pubInfo, apis);
        init();
    }
    public ColumbiaScrubber(String pubInfo, CoreMetadataRecord apis) {
        super(pubInfo, apis);
        init();
    }

    private void init(){
        java.util.Set<String> pubSet = new java.util.HashSet<String>();
        pubSet.addAll(getPublications());
        
        if(this.inventory != null){
            if( this.inventory.startsWith("Oxy.")){
                Matcher m = OXY.matcher(this.inventory);
                if(m.matches()){
                    int doc = Integer.parseInt(m.group(2));
                    String poxy = "P." + m.group(1) + info.papyri.util.VolumeUtil.romanForPOxyDoc(doc) + " " + m.group(2);
                    pubSet.add( poxy);
                }
            }
            if( this.inventory.startsWith("Fay.")){
                Matcher m = FAY.matcher(this.inventory);
                if(m.matches()){
                    String poxy = "P." + m.group(1)  + m.group(2);
                    pubSet.add( poxy);
                }
            }
        }
        String [] split = basePubInfo.split("\\|");
        //System.out.println("base pub: " + basePubInfo);
        for (String s:split){
            pubSet.add(s.trim());
        }
        String [] pubs = pubSet.toArray(new String[0]);
        for (int i=0;i<pubs.length;i++){
            String old = pubs[i];
            if(old.startsWith("P.Fay.")|| old.startsWith("P.Oxy.")){
                if (this.inventory.toLowerCase().indexOf("recto") != -1){
                    removePublication(old);
                    old += " R";
                }
                if (this.inventory.toLowerCase().indexOf("verso") != -1){
                    removePublication(old);
                    old += " V";
                }
                removeParentPubs(old);
                addPublication(old);
                continue;
            }
            if (vol2.matcher(old).find()){
                //System.out.println("old = \"" + old + "\";");
                String actual = old.replaceAll("[Rr]ecto", "R");
                Matcher m = sidedSubdocRangePatt.matcher(actual);
                if (m.find()){
                    String a1 = m.group(1) + m.group(5) + " " + m.group(6);
                    String a2 = m.group(1) + m.group(8) + " " + m.group(9);
                    removePublication(old);
                    addPublication(a1);
                    //System.out.println("adding " + a1);
                    addPublication(a2);
                    //System.out.println("adding " + a2);
                    continue;
                }
                removePublication(old);
                addPublication(actual);
                //System.out.println(" new = \"" + actual + "\"");
                continue;
            }
            if (vol5.matcher(old).find()){
                //System.out.println("vol 5 logic for " + old);
                Pattern side = Pattern.compile("\\d\\s[RrVv]\\b");
                String actual = old.replaceAll("verso", "Verso");
                actual = actual.replaceAll("recto", "Recto");
                Matcher sidedRangeM = side.matcher(actual);
                if (sidedRangeM.find()){
                    String match = sidedRangeM.group(0);
                    String replace = match.replaceAll("[rR]", "Recto");
                    replace = match.replaceAll("[vV]", "Verso");
                    //System.out.println("replacing " + match + " with " + replace);
                    actual = actual.replace(match,replace);
                }
                //System.out.println("checking for subdoc range on " + actual);
                Matcher m = sidedSubdocRangePatt.matcher(actual);
                if (m.find()){
                    String a1 = m.group(1) + m.group(5) + " " + m.group(6);
                    String a2 = m.group(1) + m.group(8) + " " + m.group(9);
                    removePublication(old);
                    removeParentPubs(a1);
                    addPublication(a1);
                    //System.out.println("adding " + a1);
                    removeParentPubs(a2);
                    addPublication(a2);
                    //System.out.println("adding " + a2);
                    continue;
                }
                

                removePublication(old);
                removeParentPubs(actual);
                addPublication(actual);
                //System.out.println("doc: new = \"" + actual + "\"");
                continue;
            }
            Matcher m = subdocPatt.matcher(old);
            if (m.matches()){
                String actual = m.group(1);
                if (m.group(2) != null) actual = actual + m.group(2);
                actual = actual + m.group(3);
                actual = actual + " " + m.group(4);
                removePublication(old);
                removeParentPubs(actual);
                addPublication(actual);
                if (actual.contains("P.Col.Zen. I ")){
                    actual = actual.replace("P.Col.Zen. I ", "P.Col. III ");
                    removeParentPubs(actual);
                    addPublication(actual);
                }
                if (actual.contains("P.Col.Zen. II ")){
                    actual = actual.replace("P.Col.Zen. II ", "P.Col. IV ");
                    removeParentPubs(actual);
                    addPublication(actual);
                }
                //System.out.println("subdoc: new = \"" + actual + "\"");
                continue;
            }
            m = parenPatt.matcher(old);
            if (m.matches()){
                int oParen = m.group(4).indexOf('(');

                String noParen = m.group(4).substring(oParen + 1,oParen + 2);
                String actual = m.group(1);
                if (m.group(2) != null) actual = actual + m.group(2);
                actual = actual + m.group(3);
                actual = actual + " " + noParen;
                removePublication(old);
                removeParentPubs(actual);
                addPublication(actual);
                if (actual.contains("P.Col.Zen. I ")){
                    actual = actual.replace("P.Col.Zen. I ", "P.Col. III ");
                    removeParentPubs(actual);
                    addPublication(actual);
                }
                if (actual.contains("P.Col.Zen. II ")){
                    actual = actual.replace("P.Col.Zen. II ", "P.Col. IV ");
                    removeParentPubs(actual);
                    addPublication(actual);
                }
                //System.out.println("paren: new = \"" + actual + "\"");
                continue;                
            }

        }
    }
}
