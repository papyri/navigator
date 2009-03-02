package info.papyri.data.publication;

import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.util.NumberConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.papyri.digester.offline.*;

public class BerkeleyScrubber extends PublicationScrubber {
    static Pattern vNo = Pattern.compile("(^.*?)(v\\.\\s(\\d+),\\sno\\.\\s(\\d+))(.*$)");
    static Pattern pTebt = Pattern.compile("(^.*?)(P\\.Tebt\\.[,\\$]\\s)([XIV]+)([\\.\\s])(\\d{2,})(\\s)?([a-zA-Z]\\b)?(.*$)");
    static Pattern SB = Pattern.compile("(^.*?)(SB)(,\\sv\\.\\s)(\\d+)(,\\sno\\.\\s)(\\d+)(.*$)");
    static Pattern pTebtLineSpan = Pattern.compile("(^.*?)(P\\.Tebt\\.[,\\$]\\s)([XIV]+)(\\.)(\\d+)(\\b)(,\\slines\\s)(\\d+)(-)(\\d+)(.*$)");
    static Pattern digits = Pattern.compile("^\\d+$");
    static String DESC_ONLY = "(desc. only)";
    static Pattern INV_LOWER = Pattern.compile("^(p\\.tebt\\.\\d+)(\\s\\([a-z]\\))?(\\s[rv]e[rc][ts]o)?(\\(\\s\\d\\))?");
    private static final int INV_SIDE = 3;
    private static final int INV_SUBDOC_1 = 4;
    private static final int INV_SUBDOC_2 = 2;
    private String ddb = null;
    public BerkeleyScrubber(String pubInfo, APISTuple apis){
        super(pubInfo,apis);    
        init();
        
    }
    public BerkeleyScrubber(String pubInfo, CoreMetadataRecord apis){
        super(pubInfo,apis);    
        init();
    }
    private void init(){
        java.util.Set<String> pubSet = new java.util.HashSet<String>();
        //clearPublications();
        String [] split = basePubInfo.split("[\\|;]");
        for(int i=0;i<split.length;i++){
            Matcher volNo = vNo.matcher(split[i]);
            if(volNo.matches()){
                String replace = NumberConverter.getRoman(volNo.group(3)) + " " + volNo.group(4);
                replace = split[i].replace(volNo.group(2), replace);
                removePublication(split[i]);
                split[i] =replace;
            }
        }
        //System.out.println("base pub: " + basePubInfo);
        for (String s:split){
            Matcher m = SB.matcher(s);
            if(m.matches()){
                String doc = m.group(6);
                int docI = Integer.parseInt(doc);
                String vol = info.papyri.util.VolumeUtil.romanForSBDoc(docI);
                String s1 = "SB " +vol + " " + m.group(6);
                pubSet.add(s1);
            }
            else {
                java.util.Collection<String> matches = scrub(s.trim());
                for(String s1:matches){
                    pubSet.add(s1.trim());
                }
            }
        }
        String [] pubs = pubSet.toArray(new String[0]);
        for (int i=0;i<pubs.length;i++){
            String old = pubs[i];
            Matcher pTebtMatcher = pTebt.matcher(old);
            Matcher lineSpanMatcher = pTebtLineSpan.matcher(old);
            if (lineSpanMatcher.matches()){
                //System.out.println("matched pTebtLineSpan for " + old);
                String volume = lineSpanMatcher.group(3);
                String doc = lineSpanMatcher.group(5);

                if ("III".equals(volume) && digits.matcher(doc).matches()){
                    int docInt = Integer.parseInt(doc);
                    if (docInt > 825){
                        volume += ".2";
                    }
                    else{
                        volume += ".1";
                    }
                }
                StringBuffer base = new StringBuffer();
                base.append("P.Tebt. ");
                base.append(volume);
                base.append(' ');
                base.append(doc);
                String plain = base.toString();
                
                if (old.indexOf(DESC_ONLY) != -1){
                    plain += " desc.";
                }
                String inv = this.inventory.toLowerCase();
                Matcher invMatch = INV_LOWER.matcher(inv);
                if(invMatch.matches()){
                    if(invMatch.group(INV_SIDE) != null){
                        if((invMatch.group(INV_SIDE).charAt(1) == 'v')){
                            base.append( " V");
                        }
                        else {
                            base.append( " R");
                        }
                        if(invMatch.group(INV_SUBDOC_1) != null) {
                            base.append(' ');
                            base.append(invMatch.group(INV_SUBDOC_1).charAt(1));
                        }
                        if(invMatch.group(INV_SUBDOC_2) != null) {
                            base.append(' ');
                            base.append(invMatch.group(INV_SUBDOC_2).charAt(1));
                        }
                    }
                }
                //System.out.println("berkeley inv: " + inv);

                if (lineSpanMatcher.group(8) != null){
                    addPublication(base.toString() + " Z. " + lineSpanMatcher.group(8) + " - " + lineSpanMatcher.group(10));
                }
                else{
                    removeParentPubs(base.toString());
                    addPublication(base.toString());
                }

                removePublication(old);
                removeParentPubs(plain);
                addPublication(plain);
            }
            if (pTebtMatcher.matches()){
                String volume = pTebtMatcher.group(3);
                String doc = pTebtMatcher.group(5);
                String subdoc = null;
                if (pTebtMatcher.group(7) != null){
                    subdoc = pTebtMatcher.group(7);
                }
                if ("III".equals(volume) && digits.matcher(doc).matches()){
                    int docInt = Integer.parseInt(doc);
                    if (docInt > 825){
                        volume += ".2";
                    }
                    else{
                        volume += ".1";
                    }
                }
                StringBuffer base = new StringBuffer();
                base.append("P.Tebt. ");
                base.append(volume);
                base.append(' ');
                base.append(doc);
                if(subdoc != null) {
                    base.append(' ');
                    base.append(subdoc);
                }
                String plain = base.toString();
                String desc = plain;
                if (old.indexOf(DESC_ONLY) != -1){
                    desc = plain + " desc.";
                }
                String inv = this.inventory.toLowerCase();
                Matcher invMatch = INV_LOWER.matcher(inv);
                if(invMatch.matches()){
                    if(invMatch.group(INV_SIDE) != null){
                        if((invMatch.group(INV_SIDE).charAt(1) == 'v')){
                            base.append( " V");
                        }
                        else {
                            base.append( " R");
                        }
                        if(invMatch.group(INV_SUBDOC_1) != null) {
                            base.append(' ');
                            base.append(invMatch.group(INV_SUBDOC_1).charAt(1));
                        }
                        if(invMatch.group(INV_SUBDOC_2) != null) {
                            base.append(' ');
                            base.append(invMatch.group(INV_SUBDOC_2).charAt(1));
                        }
                    }
                }
                //System.out.println("old= " + old + "; sided= " + sided + "; plain= "+ plain);
                removePublication(old);
                removeParentPubs(base.toString());
                removeParentPubs(plain);
                addPublication(base.toString());    
                addPublication(plain);
                addPublication(desc);
            }

        }
    }
}