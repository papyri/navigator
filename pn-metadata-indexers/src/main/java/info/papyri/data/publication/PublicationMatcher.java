package info.papyri.data.publication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public abstract class PublicationMatcher {

    private static final String sep = "([\\.\\s,]{1,3})?";
    private static final String pStart = "(^.*?)(";
    private static final String pEnd = ")(.*?$)";
    private static final Match DECOY = new Match("QQQQQQQQQQ", Pattern.compile("\\d").matcher("!"));
    private static final boolean DEBUG = "true".equals(System.getProperty("pn.matcher.debug"));
    private static final HashMap<String,String> VAR_TO_CANON = getVarToCanonMap();
    private static final String [] VARIANTS = VAR_TO_CANON.keySet().toArray(new String[0]);
    private static final String [] CANONICAL_FORMS = VAR_TO_CANON.values().toArray(new String[0]);
    static final Pattern top = Pattern.compile("\\([^\\(^\\)]*(\\([^\\(^\\)]*\\))?[^\\(^\\)]*\\)");
    
    private static final Pattern [] VARIANT_PATTERNS = parsePatterns(VARIANTS,false);
    private static final Pattern [] VARIANT_SERIES_PATTERNS = parsePatterns(VARIANTS,true);
    public static Pattern getPattern(String canonical, boolean seriesOnly){
        Iterator<String> parts = Arrays.asList(canonical.split("[\\.\\s]+")).iterator();
        StringBuffer patternBuf = new StringBuffer();
        patternBuf.append('(');
        while (parts.hasNext()){
            String next = parts.next();
            if ("".equals(next)) continue;
            patternBuf.append(next.toUpperCase());
            if (parts.hasNext()){
                patternBuf.append(sep);
            }
            else {
                patternBuf.append("\\.?");
            }
        }
        patternBuf.append(')');
        if (seriesOnly) return Pattern.compile(patternBuf.toString());
        patternBuf.append(sep + "([LXVI]+[:]?)?(\\.\\d+)?" + sep + "(\\d+)?+(\\.\\d+)?" + sep + "(([XIV]+[,\\s][A-Za-z])|([A-Z0-9]\\b)|(\\([A-Z0-9]\\)))?" + sep);
        return Pattern.compile(patternBuf.toString());
    }
    
    public static String[] matchSeries(String data){
        String result = "";
        String matched = "";
        int matchLen = -1;
        for (int i = 0; i < VARIANT_SERIES_PATTERNS.length;i++){
            String upcase = data.toUpperCase();
            Matcher curr = VARIANT_SERIES_PATTERNS[i].matcher( upcase );
            if (!curr.find()){
                continue;
            }
            if(VARIANTS[i].length() < matchLen) continue;
            result = CANONICAL_FORMS[i];
            matched =  curr.group(1);
            int pos = upcase.indexOf(matched);
            matched = data.substring(0,pos + matched.length());

            matchLen = VARIANTS[i].length();
        }
        if (!"".equals(result)) return new String[]{matched,result};
        return new String[]{null,data};
    }

    public static Collection<String> findMatches(String data){
        
        HashSet<String> matches = new HashSet<String>();
        if (data == null) return matches;
        if (DEBUG && false){
            Pattern p = Pattern.compile("\\b([Rr](ecto)?|[Vv]erso)\\b");
            if (p.matcher(data).find()) System.out.println("Sided pub number :" + data);
        }
        data = data.toUpperCase();
        Match lastMatch = null;
        for (int i = 0; i < VARIANT_PATTERNS.length;i++){
            Matcher curr = VARIANT_PATTERNS[i].matcher(data);
            if (!curr.find()){
                continue;
            }
            //String fullP = curr.pattern().pattern();
            //int groupLength = countTopLevelGroups(fullP);
            int doc = curr.groupCount() - 7;
            int volRoman = curr.groupCount() - 10;
            int altVol = curr.groupCount() - 9;
            int altDoc = curr.groupCount() - 6;
            int subDoc = curr.groupCount() - 4;
            boolean hasDoc = (curr.group(doc) != null);
            boolean hasRomanVol = (curr.group(volRoman) != null);
            boolean hasAltVol = (curr.group(altVol) != null);
            boolean hasAltDoc = (curr.group(altDoc) != null);
            boolean hasSubDoc = (curr.group(subDoc) != null);
            if (DEBUG && false){
                if (hasDoc) System.out.println(data + " matches doc with " +  curr.group(doc));
                if (hasRomanVol) System.out.println(data + " matches vol with " + curr.group(volRoman));
                if (hasAltVol) System.out.println(data + " matches altVol with " + curr.group(altVol));
                if (hasAltDoc) System.out.println(data + " matches altDoc with " + curr.group(altDoc));
                if (hasSubDoc) System.out.println(data + " matches subDoc with " + curr.group(subDoc));
            }
            String series = CANONICAL_FORMS[i] + "$";
            StandardPublication matched = new StandardPublication();
            matched.setSeries(series);
            if (hasRomanVol){
                matched.setVolume(curr.group(volRoman).replace(":", ""));
                if (hasAltVol){
                    if (hasDoc){
                        matched.setVolume(curr.group(volRoman) + curr.group(altVol));
                    }
                    else {
                        matched.setDocument(curr.group(altVol).replaceAll("\\.",""));
                    }
                }
                else {
                    if (!hasDoc){
                        //System.err.println("No doc group; leaving " + data);
                        continue;
                    }
                }
            }
            if (!hasRomanVol && !hasAltDoc && !hasDoc){
                    //System.err.println("No doc or altDoc group; leaving " + data);
                    continue;
                
            }
            if (hasDoc){
                if (hasAltDoc){
                    if (!hasRomanVol){
                        String altDocVol = curr.group(doc);
                        String altDocDoc = curr.group(altDoc).replaceAll("\\.","");
                        matched.setVolume(info.papyri.util.NumberConverter.getRoman(altDocVol));
                        matched.setDocument(altDocDoc);
                    }
                    else{
                        matched.setDocument(curr.group(doc) + curr.group(altDoc));
                    }
                }else{
                    matched.setDocument(curr.group(doc).replaceAll("[\\.\\s,]+", " ").trim());
                }
            }
            if (hasSubDoc){
                String orig = curr.group(subDoc);
                String replace = (orig.indexOf('(') != -1)?orig.toLowerCase().substring(1,2):orig.toUpperCase();
                String ptt = orig.replace("(", "\\(").replace(")","\\)");
                
                matched.setSubDocument(replace);
            }

            matches.add(matched.toString());
            // CANONICAL_FORMS[i].startsWith(lastMatch.pattern) redundant now
            if (lastMatch != null && curr.start() == lastMatch.pos.start()){
                matches.remove(lastMatch.pattern);
                //System.out.println("Removed " + lastMatch.pattern);
                while(lastMatch.pos.find(lastMatch.pos.end())){
                    //System.out.println("Found again after discard...");
                    if(!curr.find(lastMatch.pos.start())){
                        matches.add(lastMatch.pattern);
                    }
                }
            }
            curr.find(0);
            lastMatch = new Match(CANONICAL_FORMS[i],curr);
        }
        matches.addAll(requiredAlternates(matches));
        return matches;
    }
    
    public  static Collection<String> requiredAlternates(Collection<String> in){
        // quick and dirty, this is just to inject the alternate forms by which number-preserved names in other series might be found
        HashSet<String> out = new HashSet<String>();
        for(String next:in){
            if(next.startsWith("P.Lugd.Bat.")){
                if(next.startsWith("P.Lugd.Bat. I ")){
                    out.add(next.replace("P.Lugd.Bat. I", "P.Warr."));
                }
                else if(next.startsWith("P.Lugd.Bat. II ")){
                    out.add(next.replace("P.Lugd.Bat. II", "P.Vind.Bosw."));
                }
                else if(next.startsWith("P.Lugd.Bat. III ")){
                    out.add(next.replace("P.Lugd.Bat. III", "P.Oxf."));
                }
                else if(next.startsWith("P.Lugd.Bat. VI ")){
                    out.add(next.replace("P.Lugd.Bat. VI", "P.Fam.Tebt."));
                }
                else if(next.startsWith("P.Lugd.Bat. XI ")){
                    out.add(next.replace("P.Lugd.Bat. XI", "P.Vind.Sijp."));
                }
                else if(next.startsWith("P.Lugd.Bat. XIII ")){
                    out.add(next.replace("P.Lugd.Bat. XIII", "P.Select."));
                }
                else if(next.startsWith("P.Lugd.Bat. XVI ")){
                    out.add(next.replace("P.Lugd.Bat. XVI", "P.Wisc. I"));
                }
                else if(next.startsWith("P.Lugd.Bat. XVII ")){
                    out.add(next.replace("P.Lugd.Bat. XVII", "P.David"));
                }
                else if(next.startsWith("P.Lugd.Bat. XX ")){
                    out.add(next.replace("P.Lugd.Bat. XX", "P.Zen.Pestm."));
                }
                else if(next.startsWith("P.Lugd.Bat. XXII ")){
                    out.add(next.replace("P.Lugd.Bat. XXII", "P.Dion."));
                }
                else if(next.startsWith("P.Lugd.Bat. XXVI ")){
                    out.add(next.replace("P.Lugd.Bat. XXVI", "O.Varia"));
                }
            }
        }
        return out;
    }
        
    private static Pattern [] parsePatterns(String [] forms, boolean seriesOnly){
        if (forms == null){
            System.err.println("parsePatterns(String [] forms): forms was null!");
            return new Pattern[0];
        }
        Pattern [] result = new Pattern[forms.length];
        for (int i=0;i<forms.length;i++){
            result[i] = getPattern(forms[i],seriesOnly);
        }
        return result;
    }
    
    
    public static int countTopLevelGroups(String pattern){
        int result = 0;
        Matcher m = top.matcher(pattern);
        while (m.find()){
            result++;
        }
        return result;
    }
    
    
    private static class Match {
        String pattern;
        Matcher pos;
        Match(String p, Matcher i){
            pattern = p;
            pos = i;
        }
    }
    private static final Pattern allRoman = Pattern.compile("^[LXVI]+$");
    private static final Pattern hasRoman = Pattern.compile("(^.*?)([LXVI]{2,5})(.*?$)");
    private static final Pattern indexablePub = Pattern.compile("^[A-Z][A-Za-z0-9\\s\\.]+$");
    private static final Pattern vol = Pattern.compile("(^.*?)(\\s(([LXVI]+\\s)|\\d+))(.*$)");
    public static ArrayList<String> indexableSeries(String rawPub){
        if (rawPub == null) return null;
        //System.out.println(rawPub);
        ArrayList<String> result = new ArrayList<String>();
        String [] raw = rawPub.split("\\|");
        for (String rp: raw){
        Collection<String> matches = findMatches(rp);
        for (String match: matches){
            //System.out.println("\tmatched " + match);
            
            //Matcher v = vol.matcher(match);
            if (match.indexOf('$') != -1){
                String series = match.substring(0,match.indexOf('$'));
                if(!result.contains(series)) result.add(series);
            }
        }
        }
        return result;
    }
    private static HashMap<String,String> getVarToCanonMap(){
        HashMap<String,String> map = new HashMap<String,String>();
        try{
            XMLReader rdr = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
            rdr.setContentHandler(new ContentHandler(map));
            if(DEBUG) System.out.println("parsing checklist variants...");
            rdr.parse(new InputSource(PublicationMatcher.class.getResourceAsStream("/info/papyri/data/publication/checklist/checklist.xml")));
        }
        catch(Exception e){}
        if(DEBUG) System.out.println("Returning map of " + map.keySet().size() + " known series variants");
        return map;
    }
}
