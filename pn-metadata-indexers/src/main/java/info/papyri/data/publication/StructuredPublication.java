package info.papyri.data.publication;

import info.papyri.util.NumberConverter;

import java.sql.SQLException;
import java.util.regex.*;
import java.util.*;

import info.papyri.util.DBUtils;


public abstract class StructuredPublication {
    private static final Pattern VOLUME = Pattern.compile("^(([LXVI]+)(A|B|\\.\\d)?,?)\\b.*");
    private static final Pattern TWO_NUM  = Pattern.compile("(\\d+)\\s(\\d+)");
    private static final Pattern NUM_RANGE = Pattern.compile("(\\d+)\\s?[-]\\s?(\\d+)");
    private static final Pattern ALPHA_RANGE = Pattern.compile("([A-Z])\\s?[-]\\s?([A-Z])");
    private static final Pattern HGV_SPAN_UNIT = Pattern.compile("^((Kol\\.)|(Z\\.)|(S\\.)|(lines)|(pages)|(columns))$");
    private static final boolean DEBUG = true;//"true".equals(System.getProperty("pn.matcher.debug"));
    private static void oneTimeSetUp() {
        try{
            if(!DBUtils.checkDDBDPXrefTable()){
                java.net.URL derbydata = DBUtils.class.getResource("ddbdp.xml");
                info.papyri.util.DBUtils.setupDerby(derbydata);
            }
        }
        catch(java.io.IOException e){
            e.printStackTrace();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    static {
        oneTimeSetUp();
    }
    
    public static String getStructVolValue(String in){
        in = in.trim();
        Matcher volMatch = VOLUME.matcher(in);
        String result = "";
        if (volMatch.matches()){
            String roman = volMatch.group(2);
            result = Integer.toString(NumberConverter.getInt(roman));
            if(volMatch.group(3) != null){
                result += volMatch.group(3).toUpperCase();
            }
        }
        else{
            return in;
        }
        return result;
    }
    
    public  static Set<String> getStructuredPub(String pub)  {
        Set<String> result = new HashSet<String>();
        
        if(pub == null) return result;
        pub = pub.trim();
        if ("".equals(pub)) return result;
        String series = null;
        String volume = "*";
        String document = null;
        String side = "*";
        Set<String> subdoc = new HashSet<String>();
        String span = null;
        if(DEBUG){
            System.out.flush();
            System.out.println("structuring: " + pub);
        }
        pub = pub.replaceAll("[()]", "");
        String [] seriesMatch = info.papyri.data.publication.PublicationMatcher.matchSeries(pub);
//       System.out.println("seriesMatch[0]=" + seriesMatch[0] + " seriesMatch[1]=" + seriesMatch[1]);
        String match = (seriesMatch[0] == null)?(seriesMatch[1] + "$" +  pub.substring(seriesMatch[1].length())):(seriesMatch[1] + "$" + pub.substring(seriesMatch[0].length()));

            int sep = match.indexOf('$');
            if (sep == -1) return result;
            series = match.substring(0,sep);
            String remainder = null;
            if(sep+1 < match.length()){
                if (match.charAt(sep+1) != ' '){
                    remainder = match.substring(sep + 1);
                }
                else{
                    if(sep + 2 < match.length()){
                        remainder = match.substring(sep + 2);
                    }
                    else return result;
                }
            }
            else {
                return result;
            }
            
            try{
            String mseries = DBUtils.query(series,true);
            if (mseries.startsWith(DBUtils.PERSEUS_PREFIX)){
                series = mseries.substring(DBUtils.PERSEUS_PREFIX.length());
            }
            else{
                System.err.println("Unmapped hgv-pub series: " + series + " from " + match);
                return result;
            }
            }
            catch (SQLException e){
                return result;
            }
            remainder = remainder.replace(" - ", "-").trim();
            Matcher volMatch = VOLUME.matcher(remainder);
            if (volMatch.matches()){
                String roman = volMatch.group(2);
                String matchedVol = volMatch.group(1);
                volume = Integer.toString(NumberConverter.getInt(roman));
                if(volMatch.group(3) != null){
                    volume += volMatch.group(3).toUpperCase();
                }
                remainder = remainder.substring(matchedVol.length());
                if(remainder.matches("^\\W\\s.+")) remainder = remainder.substring(2);
                remainder = remainder.trim();
            }
            else{
                if (!remainder.matches("^\\d+$")) System.err.println("Structuring problem: non-numeric remainder " + remainder + " ; did not match ( " + match + " )" );
            }

            String [] parts = remainder.split("([,]?\\s+)");
            int spanExtent = -1;
            int spanUnit = -1;
            int numSubdocs = 0;
            for(int i=1;i<parts.length;i++){
                if(HGV_SPAN_UNIT.matcher(parts[i]).matches()){
                    spanUnit = i;
                    spanExtent = i + 1;
                    i++;
                    continue;
                }
                if(side.equals("*") && parts[i].matches("^([Rr](ecto)?)$")){
                    side = "RECTO";
                    continue;
                }
                else if(side.equals("*") && parts[i].matches("^([Vv](erso)?)$")){
                    side =  "VERSO";
                    continue;
                }
                else{
                    numSubdocs++;
                    if(numSubdocs <= 2){
                        Matcher m = ALPHA_RANGE.matcher(parts[i].toUpperCase());
                        if(m.matches()){
                            char start = m.group(1).charAt(0);
                            char end = m.group(2).charAt(0);
                            for(char alpha = start;alpha<=end;alpha++){
                                subdoc.add(";subdoc=" + alpha);
                            }
                        }
                        else subdoc.add(";subdoc=" + parts[i].toUpperCase());
                    }
                }
            }
            if(spanUnit != -1 && spanExtent < parts.length){
                if("Kol.".equals(parts[spanUnit]) || "columns".equals(parts[spanUnit]) ){
                    if(parts[spanExtent].matches("^[MDCLXVI]+$")){
                        span = "column:" + NumberConverter.getInt(parts[spanExtent]);
                    }
                    else {
                        span = "column:" + parts[spanExtent];
                    }
                }
                else if ("Z.".equals(parts[spanUnit]) || "lines".equals(parts[spanUnit]) ){
                    span = "line:" + parts[spanExtent];
                }
                else if ("S.".equals(parts[spanUnit]) || "pages".equals(parts[spanUnit]) ){
                    span = "page:" + parts[spanExtent];
                }
            }
            if(parts.length > 0) document = parts[0];
            
            
//            if(false){
//            switch (parts.length){
//            case 7:
//            case 6:
//            case 5:
//                if("Kol.".equals(parts[3])){
//                    if(parts[4].matches("^[MDCLXVI]+$")){
//                        span = "column:" + NumberConverter.getInt(parts[4]);
//                    }
//                    else {
//                        span = "column:" + parts[4];
//                    }
//                }
//                else if ("Z.".equals(parts[3])){
//                    span = "line:" + parts[4];
//                }
//                else if ("S.".equals(parts[3])){
//                    span = "page:" + parts[4];
//                }
//            case 4:
//                if("Kol.".equals(parts[2])){
//                    if(parts[3].matches("^[MDCLXVI]+$")){
//                        span = "column:" + NumberConverter.getInt(parts[3]);
//                    }
//                    else {
//                        span = "column:" + parts[3];
//                    }
//                }
//                else if ("Z.".equals(parts[2])){
//                    span = "line:" + parts[3];
//                }
//                else if ("S.".equals(parts[2])){
//                    span = "page:" + parts[3];
//                }
//            case 3:
//                if(parts[2].matches("^([Rr](ecto)?)$")){
//                    side = "RECTO";
//                }
//                else if(parts[2].matches("^([Vv](erso)?)$")){
//                    side =  "VERSO";
//                }
//                else {
//                    if(!"Kol.".equals(parts[2]) && !"Z.".equals(parts[2])&& !"S.".equals(parts[2])){
//                        if( !(HGV_SPAN_UNIT.matcher(parts[1]).matches())){
//                            subdoc.add(";subdoc=" + parts[2].toUpperCase());
//                        }
//                    }
//                }
//                if("Kol.".equals(parts[1])){
//                    if(parts[2].matches("^[MDCLXVI]+$")){
//                        span = "column:" + NumberConverter.getInt(parts[2]);
//                    }
//                    else {
//                        span = "column:" + parts[2];
//                    }
//                }
//                else if ("Z.".equals(parts[1])){
//                    span = "line:" + parts[2];
//                }
//                else if ("S.".equals(parts[1])){
//                    span = "page:" + parts[2];
//                }
//            case 2:
//                if(parts[1].matches("^([Rr](ecto)?)$")){
//                    side = "RECTO";
//                }
//                else if(parts[1].matches("^([Vv](erso)?)$")){
//                    side =  "VERSO";
//                }
//                else {
//                    if( !(HGV_SPAN_UNIT.matcher(parts[0]).matches())){
//                        subdoc.add(";subdoc=" + parts[1].toUpperCase());
//                    }
//                }
//            case 1:
//                if (document == null) document = parts[0];
//                break;
//            default:
//                System.err.println("Unexpected parts length of " + parts.length + " for " + match);
//            }
//            }
            if (document != null){
                String temp =  ("series=" + series + ";volume=" + volume + ";document=" + document + ";side=" + side);
                for(String suffix:subdoc){
                    String add = temp + suffix;
                    if(span != null) add += ";span=" + span;
                    result.add(add);
                }
                if(span != null) {
                     result.add(temp + ";span=" + span);
                }
                else{
                    result.add(temp);
                }
                if(DEBUG)  System.out.println(result);
            }
            return result;
    }
}
