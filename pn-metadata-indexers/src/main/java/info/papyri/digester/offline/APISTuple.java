package info.papyri.digester.offline;

import info.papyri.data.*;
import info.papyri.data.publication.*;

import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.metadata.NamespacePrefixes;
import info.papyri.metadata.OutOfRangeException;

import java.util.regex.*;
import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.document.*;

import info.papyri.util.DBUtils;


import info.papyri.metadata.CoreMetadataFields;
import info.papyri.util.IntQueue;
import info.papyri.util.NumberConverter;
import info.papyri.util.VolumeUtil;
public class APISTuple {
    static final String startP = "^(.*?)";
    static final String endP = "(.*?$)";
    static final String seriesP = "(([PO]\\.([A-Z][a-zA-Z]*\\.?))(\\s[A-Z][a-zA-Z]*\\.?)?|(SB))";
    static final String volP = "(\\s[MDCLXVI]+)";
    static final String docP = "(\\s\\d+((\\.\\d)?|(\\s?[a-z]))?)";
    static final String sideP = "((\\s((V(erso)?)|(R(ecto)?)))?(\\s[a-z0-9])?)?";
    static final String col = "(\\sKol\\.\\s[XIV]+(\\s[0-9a-z])?)";
    static final Pattern DOC_FRAG = Pattern.compile("^(\\d+)([a-zA-Z])$");
    static final Pattern FIND_NUM = Pattern.compile("\\d+");
    static Pattern colPub = Pattern.compile(startP + "(" + seriesP + volP + docP + col + ")" + "(.+$)");
    static Pattern plainPub = Pattern.compile(startP + "(" + seriesP + volP + docP + sideP + ")" + endP);
    //Pattern.compile("\\b([([PO]\\.([A-Z][a-zA-Z]*\\.))(SB)]+\\s[MDCLXVI]+\\s\\d+(\\s[a-z])?(\\s[RV])?\\b)(descr\\.)?.*");
    static Term PUB_TERM = new Term(CoreMetadataFields.BIBL_PUB,"");
    static Term XREF_TERM = new Term(CoreMetadataFields.XREFS,"");
    static Term CN_TERM = new Term(CoreMetadataFields.DOC_ID,"");
    static Term STRUCT_PUB_TERM = new Term(CoreMetadataFields.PUBLICATION_STRUCTURED,"");
    private static final String SB_STRUCT = "0239";
    private static final String POXY_STRUCT = "0181";
    static Pattern number = java.util.regex.Pattern.compile("\\d+");
    static Pattern parens = Pattern.compile("\\(.+\\)");
    final PrintStream duplicates;
    String title;
    String controlName;
    String inventoryNum;
    StringBuffer translation = new StringBuffer();
    StringBuffer translationDE = new StringBuffer();
    StringBuffer translationFR = new StringBuffer();
    StringBuffer notes = new StringBuffer();
    StringBuffer illustrations = new StringBuffer();
    StringBuffer corrections = new StringBuffer();
    StringBuffer translationNotes = new StringBuffer();
    StringBuffer summary = new StringBuffer();
    HashSet<String> subjects = new HashSet<String>();
    String pubInfo = null;
    String sortDate = null;
    HashMap<String,String> dateIndex = new HashMap<String,String>();
    boolean hasImage = false;
    boolean hasTranslation = false;
    boolean log = false;
    static int totalBand;
    static int totalRomanBand;
    HashSet<String> pubs = new HashSet<String>();
    HashSet<String> struct_pubs = new HashSet<String>();
    HashSet<String>hgvCNs = new HashSet<String>();
    IntQueue hgvIDs = new IntQueue();
   // HashSet<String>DDb = new HashSet<String>();
    HashSet<String>xrefs = new HashSet<String>();
    HashSet<String>langs = new HashSet<String>();
    HashSet<String>places = new HashSet<String>();
    HashSet<String> displayDates = new HashSet<String>();
    HashSet<String>struct_DDb = new HashSet<String>();
    IndexSearcher hgvSearcher;
    static int PUB = 0;
    static int DDB = 1;
    static String [] FIELDS = {"publication","ddb"};


    public APISTuple(IndexSearcher s, PrintStream duplicates){
        this.hgvSearcher = s;
        this.duplicates = duplicates;
    }
    public APISTuple(IndexSearcher s){
        this(s,System.out);
    }
    public void setControlName(String apisCN){
        this.controlName = apisCN;
    }

    public String getControlName(){
        return this.controlName;
    }

    public void addNotes(String notes){
        if(notes==null)return;
        if((notes = notes.trim()).equals("")) return;
        if(this.notes.length() > 0) this.notes.append(" ; ");
        this.notes.append(notes);
    }

    public String getNotes(){
        if(this.notes.length()==0) return null;
        return this.notes.toString();
    }
    
    public void addSubject(String subject){
        if(subject==null)return;
        if((subject = subject.trim()).equals("")) return;
        this.subjects.add(subject);
    }
    
    public Collection<String> getSubjects(){
        return this.subjects;
    }
    
    public void addSummary(String summary){
        if(summary==null)return;
        if((summary = summary.trim()).equals("")) return;
        if(this.summary.length() > 0) this.summary.append(" ; ");
        this.summary.append(summary);
    }
    
    public String getSummary(){
        if(this.summary.length()==0) return null;
        return this.summary.toString();
    }

    public void addCorrectionNotes(String notes){
        if(notes==null)return;
        if((notes = notes.trim()).equals("")) return;
        if(this.notes.length() > 0) this.notes.append(" ; ");
        this.notes.append(notes);
        if(this.corrections.length() > 0) this.corrections.append(" ; ");
        this.corrections.append(notes);
    }

    public String getCorrectionNotes(){
        if(this.corrections.length()==0) return null;
        return this.corrections.toString();
    }

    public void addIllustrationNotes(String notes){
        if(notes==null)return;
        if((notes = notes.trim()).equals("")) return;
        if(this.notes.length() > 0) this.notes.append(" ; ");
        this.notes.append(notes);
        if(this.illustrations.length() > 0) this.illustrations.append(" ; ");
        this.illustrations.append(notes);
        this.hasImage = true;
    }

    public String getTranslationNotes(){
        if(this.translationNotes.length()==0) return null;
        return this.translationNotes.toString();
    }
    public void addTranslationNotes(String notes){
        if(notes==null)return;
        if((notes = notes.trim()).equals("")) return;
        if(this.notes.length() > 0) this.notes.append(" ; ");
        this.notes.append(notes);
        if(this.translationNotes.length() > 0) this.translationNotes.append(" ; ");
        this.translationNotes.append(notes);
        this.hasTranslation = true;
    }

    public String getIllustrationNotes(){
        if(this.illustrations.length()==0) return null;
        return this.illustrations.toString();
    }
    public HashMap<String,String> getDateIndexes(){
        return dateIndex;
    }

    public void setDateIndex(String date1, String date2,boolean encode) throws OutOfRangeException{
        addDateIndex(date1, date2,encode);
    }
    public void addDateIndex(String date1, String date2, boolean encode) throws OutOfRangeException{
        if(encode){
            int di = Integer.parseInt(date1);
            int d2 = (date2==null)?di:Integer.parseInt(date2);
            date1 = NumberConverter.encode(di);
            date2 = NumberConverter.encode(d2);
            this.dateIndex.put(date1 , date2);
        }else{
            this.dateIndex.put(date1,date2);
        }
        if(this.sortDate==null)this.sortDate = date1;

    }
    
    public void addPlace(String place){
        this.places.add(place);
    }
    
    public Collection<String> getPlaces(){
        return this.places;
    }

    public void setHasImage(){
        this.hasImage = true;
    }

    public boolean hasImage(){
        return this.hasImage;
    }

    public void setHasTranslation(){
        this.hasTranslation = true;
    }

    public boolean hasTranslation(){
        return this.hasTranslation;
    }
    
    public void setTitle(String title){
        this.title = title;
    }
    
    public String getTitle(){
        return this.title;
    }
    
    public void setTranslation(String translation){
        setTranslation(translation,CoreMetadataRecord.ModernLanguage.ENGLISH);
    }
    
    public void setTranslation(String translation, CoreMetadataRecord.ModernLanguage lang){
        if(translation == null || (translation = translation.trim()).equals("")) return;
        switch(lang){
        case ENGLISH:
            this.translation.append(translation);
            this.hasTranslation = true;
            break;
        case FRENCH:
            this.translationFR.append(translation);
            this.hasTranslation = true;
            break;
        case GERMAN:
            this.translationDE.append(translation);
            this.hasTranslation = true;
            break;
        }
    }
    
    public String getTranslation(){
        return getTranslation(CoreMetadataRecord.ModernLanguage.ENGLISH);
    }

    public String getTranslation(CoreMetadataRecord.ModernLanguage lang){
        switch(lang){
        case ENGLISH:
            return (this.translation.length()>0)?this.translation.toString():null;
        case FRENCH:
            return (this.translationFR.length()>0)?this.translationFR.toString():null;
        case GERMAN:
            return (this.translationDE.length()>0)?this.translationDE.toString():null;
        default: return null;
        }
    }
    
    public void setPublication(String publication){
        if (publication == null || publication.startsWith("The text is not published")) return;
        publication = publication.trim();
        if("".equals(publication)) return;
        if (pubInfo == null){
            this.pubInfo = publication;
        }
        else {
            pubInfo += (" ; " + publication);
        }

        Collection<String> matches = PublicationMatcher.findMatches(publication);
        handleMatches(matches);
        }
    
    private void handleMatches(Collection<String> matches){
        Iterator<String> mIter = matches.iterator();
        while (mIter.hasNext()){
            String next = mIter.next();
            if (next.startsWith("P. ") || next.startsWith("O. ")) next = (next.substring(0,2) + next.substring(3));
            //System.out.println("setPublication(): next= " + next);
            if (next.matches("(^.*?)[\\d]+(.*?$)")){
                String p = "";
                int flag = next.indexOf('$');
                if(flag != -1){
                    p = next.replaceAll("\\$", "");
                    String s = next.substring(0,flag);
                    try{
                        String series = DBUtils.query(s,true);
                        if (series.startsWith(DBUtils.PERSEUS_PREFIX)){
                            series = series.substring(DBUtils.PERSEUS_PREFIX.length());
                        }
                        else{
                            System.err.println("Unmapped pub series: " +s + " from " + next);
                        }
                    }
                    catch(SQLException sqe){
                        sqe.printStackTrace(System.err);
                    }
                }
                else{
                    continue;
                }

                //if (!this.pubs.contains(p))this.pubs.add(p);
                //below to try and guarantee no cheap pub matches
                boolean add = true;
                for (String pub:this.pubs.toArray(new String[0])){
                    if (pub.startsWith(p)) add = false;
                    if(p.startsWith(pub)) this.pubs.remove(pub);
                }
                if (add){
                    this.pubs.add(p);
                }
                Set<String> struct = StructuredPublication.getStructuredPub(p);
                if(struct.size() > 0) {
                    for(String sp:struct){
                    this.struct_pubs.add(sp);
                    }
                }
                else {
                    if(!"".equals(p) && !p.matches("^[\\s\\r\\n]+$"))  System.err.println("Could not structure: " + p);
                }
            }
        }
    }

    private void setDDB(String ddbVal){
        throw new UnsupportedOperationException("setDDB no longer supported; use addXref");
//      if (ddb == null || ddb.startsWith("The text is not published")) return;
//      if(ddb.indexOf('|') != -1){
//          String [] ddbs = ddb.split("\\|");
//          for (String val: ddbs){
//              setDDB(val,c);
//          }
//      }
//        if (ddbVal == null || "".equals(ddbVal.trim())) return;
//        if(!ddbVal.startsWith(NamespacePrefixes.DDBDP)) throw new IllegalArgumentException(ddbVal);
//        ddbVal = ddbVal.trim();
//        if(ddbVal.startsWith("http:")){
//            String docPrefix = "doc=Perseus:abo:pap,";
//            int doc = ddbVal.indexOf(docPrefix);
//            if (doc != -1){
//                ddbVal = ddbVal.substring(doc + docPrefix.length());
//            }
//        }
//        String [] ddbs = ddbVal.split("\\|");
//        for (String ddb: ddbs){
//            if (ddb.startsWith("P. ") || ddb.startsWith("O. ")) ddb = (ddb.substring(0,2) + ddb.substring(3)); 
//            if (ddb.startsWith("P.Mich.4:1")) ddb = ddb.replaceAll("P.Mich.4:1", "P.Mich.:4");
//            if (ddb.startsWith("P.Mich:4:1")) ddb = ddb.replaceAll("P.Mich:4:1", "P.Mich.:4");
//            if (ddb.startsWith("P.Mich.:4:1:")) ddb = ddb.replaceAll("P.Mich.:4:1", "P.Mich.:4");
//            if (ddb.startsWith("P:NYU:")) ddb = ddb.replaceAll("P:NYU:", "P.NYU:");
//            if (ddb.startsWith("O:Berenike:")) ddb = ddb.replaceAll("O:Berenike:", "O.Berenike:");
//            if(ddb.startsWith("P.:")) ddb = ddb.replace("P.:","P.");
//            if(ddb.startsWith("O.:")) ddb = ddb.replace("O.:","O.");
//            if(ddb.startsWith("P.Congr. :")) ddb = ddb.replace("P.Congr. :","P.Congr.:");
//            if(ddb.startsWith("P.Congr.:15:")) ddb = ddb.replace("P.Congr.:15:","P.Congr.XV::");
//            String [] parts = ddb.split("[:]");
//            String [] pub = new String[parts.length];
//            if (parts.length < 2) continue;
//            try{
//                if ("O.Ber.".equals(parts[0])) parts[0] = "O.Berenike";
//                else if ("SB.".equals(parts[0])) parts[0] = "SB";
//                else if (parts[0].startsWith("SB.") && parts.length == 2){
//                    parts = new String[]{"SB",parts[0].substring(3),parts[1]};
//                    pub = new String[parts.length];
//                }
//                else if ("CPR.".equals(parts[0])) parts[0] = "CPR";
//                else if ("P.Mich.Michl.".equals(parts[0])) parts[0] = "P.Mich.Mchl";
//                else if ("P.Mich.inv.".equals(parts[0].trim())) parts[0] = "P.Mich.";
//                else if ("P.Mich.4".equals(parts[0])) parts[0] = "P.Mich.";
//                else if ("P.NYU.".equals(parts[0])) parts[0] = "P.NYU";
//                else if (".PNYU".equals(parts[0])) parts[0] = "P.NYU";
//                else if ("P.Cair.Isidor.".equals(parts[0])) parts[0] = "P.Cair.Isid.";
//                else if ("P.Col".equals(parts[0])) parts[0] = "P.Col.";
//                else if ("P.CollYoutie".equals(parts[0])) parts[0] = "P.Coll.Youtie";
//                else if ("PColl Youtie.".equals(parts[0])) parts[0] = "P.Coll.Youtie";
//                else if ("PColl Youtie .".equals(parts[0])) parts[0] = "P.Coll.Youtie";
//                else if ("P.XV.Congr.".equals(parts[0])) parts[0] = "P.Congr.XV";
//                else if ("P.Congr.".equals(parts[0])){
//                    if ("XV".equals(parts[1])) {
//                        parts = new String[]{"P.Congr.XV",parts[2]}; 
//                        pub = new String[parts.length];
//                    }
//                }
//                else if ("P.Corn".equals(parts[0])) parts[0] = "P.Corn.";
//                else if ("P.Customs.".equals(parts[0])) parts[0] = "P.Customs";
//                else if ("P. Oxy.".equals(parts[0])) parts[0] = "P.Oxy.";
//                else if ("P. Oxy.".equals(parts[0])) parts[0] = "P.Oxy.";
//                System.arraycopy(parts,0,pub,0,parts.length);
//                String series = DBUtils.query(parts[0],true);
//                if (series.startsWith(DBUtils.PERSEUS_PREFIX)){
//                    series = series.substring(DBUtils.PERSEUS_PREFIX.length());
//                    parts[0] = series;
//                }
//                else{
//                    System.err.println("Unmapped DDb series: " + parts[0] + " from " + ddb);
//                }
//            }
//            catch (Throwable t){
//                //System.out.println(t.toString());
//            };
//            Matcher match;
//            String struct = null;
//            switch (parts.length){
//            case 0:
//            case 1:
//                break;
//            case 2:
//
//                match = DOC_FRAG.matcher(parts[1]);
//                String vol = "*";
//                if(match.matches()){
//                    if(SB_STRUCT.equals(parts[0])){
//                        vol = info.papyri.util.VolumeUtil.romanForSBDoc(Integer.parseInt(match.group(1)));
//                    }
//                    else if (POXY_STRUCT.equals(parts[0])){
//                        vol = info.papyri.util.VolumeUtil.romanForPOxyDoc(Integer.parseInt(match.group(1)));
//                    }
//                    if (match.group(2) != null){
//                        struct = ("series=" + parts[0] + ";volume=" + vol + ";document=" + match.group(1) + ";side=*;subdoc=" + match.group(2).toUpperCase());
//                    }
//                    else{
//                        struct = ("series=" + parts[0] + ";volume=" + vol + ";document=" + match.group(1) + ";side=*");
//                    }
//                }
//                else{
//                    struct = ("series=" + parts[0] + ";volume=" + vol + ";document=" + parts[1] + ";side=*");
//                }
//                break;
//            case 3:
//                match = DOC_FRAG.matcher(parts[2]);
//                if(match.matches()){
//                    if(SB_STRUCT.equals(parts[0])){
//                        vol = info.papyri.util.VolumeUtil.romanForSBDoc(Integer.parseInt(match.group(1)));
//                    }
//                    else if (POXY_STRUCT.equals(parts[0])){
//                        vol = info.papyri.util.VolumeUtil.romanForPOxyDoc(Integer.parseInt(match.group(1)));
//                    }
//                    else{
//                        vol =  StructuredPublication.getStructVolValue(parts[1]);
//                    }
//                    if (match.group(2) != null){
//                        struct = ("series=" + parts[0] + ";volume=" +vol + ";document=" + match.group(1) + ";side=*;subdoc=" + match.group(2).toUpperCase());
//                    }
//                    else{
//                        struct = ("series=" + parts[0] + ";volume=" + vol  + ";document=" + match.group(1) + ";side=*");
//                    }
//                }
//                else{
//                    String [] docParts = parts[2].trim().split("\\s");
//                    String doc = docParts[0].replaceAll("dupl\\.?","");
//                    int dot = doc.indexOf('.');
//                    if(dot != -1){
//                        String [] newParts = new String[docParts.length + 1];
//                        newParts[1] = doc.substring(dot + 1);
//                        doc = doc.substring(0,dot);
//                        newParts[0] = doc;
//                        for(int i=1;i<docParts.length;i++){
//                            newParts[i+1] = docParts[i];
//                        }
//                        docParts = newParts;
//                    }
//                    if(SB_STRUCT.equals(parts[0])){
//                        vol = info.papyri.util.VolumeUtil.romanForSBDoc(Integer.parseInt(doc));
//                    }
//                    else if (POXY_STRUCT.equals(parts[0])){
//                        vol = info.papyri.util.VolumeUtil.romanForPOxyDoc(Integer.parseInt(doc));
//                    }
//                    else{
//                        vol =  StructuredPublication.getStructVolValue(parts[1]);
//                    }
//                    if(docParts.length > 1){
//                        struct =("series=" + parts[0] + ";volume=" + vol + ";document=" + doc + ";side=*;subdoc=" + docParts[1].toUpperCase());
//                    }
//                    else {
//                        struct =("series=" + parts[0] + ";volume=" + vol + ";document=" + doc + ";side=*");
//                    }
//                }
//                break;
//            default:
//                System.err.println("Unexpected parts length of " + parts.length + " for " + ddb);
//            }
//            if(struct != null){
//                //System.out.println(struct);
//                this.struct_DDb.add(struct);
//            }
//            java.util.regex.Matcher m = number.matcher(pub[1]);
//            if (m.find() && pub.length > 2){
//                int dot = pub[1].indexOf('.');
//                pub[1] = (dot == -1)?NumberConverter.getRoman(pub[1].trim())
//                        :(NumberConverter.getRoman(pub[1].substring(0,dot)).trim() + pub[1].substring(dot));
//            }
//            pub[0] = PublicationMatcher.matchSeries(pub[0])[1];
//            if(pub.length > 1){
//                int docIx = (pub.length > 2)?2:1;
//                String doc = pub[docIx];
//                String vol = null;
//                Matcher docNum = FIND_NUM.matcher(pub[docIx]);
//                if(docNum.matches()){
//                    if("SB".equals(pub[0])){
//                        vol = info.papyri.util.VolumeUtil.romanForSBDoc(Integer.parseInt(docNum.group()));
//                    }
//                    else if("P.Oxy.".equals(pub[0])){
//                        vol = info.papyri.util.VolumeUtil.romanForPOxyDoc(Integer.parseInt(docNum.group()));
//                    }
//                    else{
//                        if(docIx == 2) vol = pub[1];
//                    }
//                }
//                if(vol != null){
//                    if(docIx == 2){
//                        pub[1] = vol;
//                    }
//                    else{
//                        String[] newpub = new String[pub.length + 1];
//                        newpub[0] = pub[0];
//                        newpub[1] = vol;
//                        for(int i = 1; i<pub.length;i++){
//                            newpub[i+1] = pub[i];
//                        }
//                        pub = newpub;
//                    }
//                }
//            }
//            Iterator pubIter = Arrays.asList(pub).iterator();
//            StringBuffer buf = new StringBuffer();
//            while(pubIter.hasNext()){
//                String next = pubIter.next().toString();
//                if ("".equals(next)) continue;
//                buf.append(next);
//                if (pubIter.hasNext()) buf.append(' ');
//            }
//            String bufS = buf.toString();
//            if (!this.pubs.contains(bufS)) this.pubs.add(bufS);
//            Iterator<String> partIter = Arrays.asList(parts).iterator();
//            StringBuffer ddbBuf = new StringBuffer();
//            while(partIter.hasNext()){
//                String next = partIter.next();
//                ddbBuf.append(next.trim());
//                if (partIter.hasNext()) ddbBuf.append(':');
//            }
//            String ddbS = ddbBuf.toString();
//            if (parts.length == 2) ddbS = ddbS.replaceFirst(":", "::");
//            addDDB(ddbS);
//        }
    }

    private boolean addDDB(String ddb){
        throw new UnsupportedOperationException("addDDB no longer supported; use addXref");
//        if (ddb == null) return false;
//        ddb = ddb.trim();
//        if (ddb.length() == 0) return false;
//        if (this.DDb.contains(ddb)) return false;
//        this.DDb.add(ddb);
//        return true;
    }

    public Collection<String> getDDB(){
        throw new UnsupportedOperationException("getDDB no longer supported; use addXref");
//        return this.DDb;
    }

    public void setInventory(String inv){
        this.inventoryNum = inv;
    }

    public String getInventory(){
        return this.inventoryNum;
    }

    public void addLanguage(String lang){
        this.langs.add(lang);
    }
    
    public Collection<String> getLanguages(){
        return this.langs;
    }
    
    public void matchXref(){
        if (getHGVIdsSize() != 0) return;
        String [] xrefs = getXrefs().toArray(new String[0]); // avoid Concurrent Mod if we find something
        try{for (String ddb:xrefs){
            Hits hgvHits = hgvSearcher.search(new TermQuery(XREF_TERM.createTerm(ddb)));
            int numHits = hgvHits.length();
            for(int i = 0; i < numHits; i++){
                addHGV(hgvHits.id(i));
            }
        }
        }
        catch(Exception ioe){

        }
        if (getHGVIdsSize() != 0) return;
        try{
            Hits hgvHits = hgvSearcher.search(new TermQuery(XREF_TERM.createTerm(this.controlName)));
            int numHits = hgvHits.length();
            for(int i = 0; i < numHits; i++){
                addHGV(hgvHits.id(i));
            }
        }
        catch(Exception ioe){

        }
        
    }

    public void matchStructuredDDB(){
        if (getHGVIdsSize() != 0) return;
        try{for (String ddb:struct_DDb){
            Hits hgvHits = hgvSearcher.search(new TermQuery(STRUCT_PUB_TERM.createTerm(ddb)));
            int numHits = hgvHits.length();
            for(int i = 0; i < numHits; i++){
                addHGV(hgvHits.id(i));
            }
        }
        }
        catch(Exception ioe){

        }
    }    

    private boolean hasHGV(String hgv){
        return this.hgvCNs.contains(hgv);
    }

    private int getHGVIdsSize(){
        return this.hgvCNs.size();
    }

    private boolean addHGV(int id) throws IOException, OutOfRangeException {
        if(!this.hgvIDs.contains(id)){
            Document doc = hgvSearcher.doc(id);
            this.hgvIDs.add(id);
            String hgvId = doc.get(CoreMetadataFields.DOC_ID);
            this.hgvCNs.add(hgvId);
            addXref(hgvId);
            String [] pubs = doc.getValues(CoreMetadataFields.BIBL_PUB);
            if(pubs != null){
                for(String pub:pubs)addPublication(pub);
            }
            String [] xrefs = doc.getValues(CoreMetadataFields.XREFS);
            if(xrefs != null){
                for(String xref:xrefs)addXref(xref.trim());
            }
            String [] langs = doc.getValues(CoreMetadataFields.LANG);
            if(langs != null){
                for(String lang:langs)addLanguage(lang);
            }
            String [] notes = doc.getValues(CoreMetadataFields.ALL_NOTES_ONLY);
            if(notes != null){
                for(String note:notes) addNotes(note);
            }
            String [] places = doc.getValues(CoreMetadataFields.PROVENANCE);
            if(places != null){
                for(String place:places) addPlace(place);
            }
            String [] displayDates = doc.getValues(CoreMetadataFields.DATE1_D);
            if(displayDates != null){
                for(String date:displayDates) this.addDisplayDate(date);
            }
            String date1 = doc.get(CoreMetadataFields.DATE1_I);
            String date2 = doc.get(CoreMetadataFields.DATE2_I);
            if(date2==null)date2=date1;
            if(date1 != null){
                setDateIndex(date1, date2,false);
                setSortDate(date1);
            }
            notes = doc.getValues(CoreMetadataFields.BIBL_CORR);
            if(notes != null){
                for(String note:notes) addCorrectionNotes(note);
            }
            if(this.title==null){
                setTitle(doc.get(CoreMetadataFields.TITLE));
            }
            String translation = doc.get(CoreMetadataFields.TRANSLATION_EN);
            if(translation != null) setTranslation(translation, CoreMetadataRecord.ModernLanguage.ENGLISH);
            translation = doc.get(CoreMetadataFields.TRANSLATION_DE);
            if(translation != null) setTranslation(translation, CoreMetadataRecord.ModernLanguage.GERMAN);
            translation = doc.get(CoreMetadataFields.TRANSLATION_FR);
            if(translation != null) setTranslation(translation, CoreMetadataRecord.ModernLanguage.FRENCH);
            return true;
        }
        return false;
    }
    
    public void setSortDate(String date){
        this.sortDate = date;
    }
    
    public String getSortDate(){
        return this.sortDate;
    }
    
    public void addDisplayDate(String date){
        if(date==null) return;
        this.displayDates.add(date);
    }
    public void addDisplayDate(String start, String end){
        if(start == null) return;
        if(end == null || end.equals(start)){
            this.displayDates.add(start);
        }
        else{
            this.displayDates.add(start + " - " + end);
        }
    }
    
    public Collection<String> getDisplayDates(){
        return this.displayDates;
    }

    public Collection<String> getHGVNames(){
        ArrayList<String> result = new ArrayList<String>(this.hgvCNs.size());
        result.addAll(hgvCNs);
        return result;
    }
    public IntQueue getHGVIds(){
        return IntQueue.copy(this.hgvIDs);
    }
    private static int partLength(char [] input){
       int sc = 0;
        for(char c:input){
            if(c==';') sc++;
            else if(c=='*') sc--;
        }
        return sc;
    }
    public int matchStructuredPubs() throws IOException, OutOfRangeException {
        int result = 0;
        Comparator<String> sorter = new Comparator<String>(){
            public int compare(String a, String b){
                char [] aC = a.toCharArray();
                char [] bC = b.toCharArray();
                int aL = partLength(aC);
                int bL= partLength(bC);
                if (aL > bL) return -1;
                if (aL < bL) return 1;
                return 0;
            }
        };
        String [] sorted = struct_pubs.toArray(new String[0]);
        Arrays.sort(sorted,sorter);
        Set<String> matched = new HashSet<String>(0);
        
        matching:
        for(String struct:sorted){
            boolean sided = (struct.indexOf("RECTO") != -1 || struct.indexOf("VERSO") != -1);
            checking:
            for(String match:matched){
                if(match.startsWith(struct) && result > 0) continue matching;
            }
            int numMatch = matchStructuredPub(struct);
            if(numMatch == 0 && struct.indexOf("side=*") != -1){
                numMatch += matchStructuredPub(struct.replace("side=*","side=RECTO"));
                numMatch += matchStructuredPub(struct.replace("side=*","side=VERSO"));
            }
            if (numMatch == 0 && sided ){
                String wc = struct.replaceAll("(RECTO|VERSO)", "*");
                numMatch += matchStructuredPub(wc);
            }
            if(numMatch == 0 && struct.indexOf(";subdoc") != -1){
                String ud = struct.substring(0,struct.indexOf(";subdoc"));
              System.out.println("Trying " + struct + " as " + ud);
                numMatch += matchStructuredPub(ud);
                if(numMatch == 0 && ud.indexOf("side=*") != -1){
                    numMatch += matchStructuredPub(ud.replace("side=*","side=RECTO"));
                    numMatch += matchStructuredPub(ud.replace("side=*","side=VERSO"));
                }
            }
            if(numMatch == 0 && struct.indexOf(";span") != -1){
                String ud = struct.substring(0,struct.indexOf(";span"));
                numMatch += matchStructuredPub(ud);
                if(numMatch == 0 && ud.indexOf("side=*") != -1){
                    numMatch += matchStructuredPub(ud.replace("side=*","side=RECTO"));
                    numMatch += matchStructuredPub(ud.replace("side=*","side=VERSO"));
                }
            }
            if(numMatch == 0){
                if (sided){
                    String wc = struct.replaceAll("(RECTO|VERSO)", "*");
                    numMatch += matchStructuredPub(wc);
                    if(numMatch == 0 && wc.indexOf(";subdoc") != -1){
                        String ud = wc.substring(0,wc.indexOf(";subdoc"));
                        numMatch += matchStructuredPub(ud);
                    }
                    if(numMatch == 0 && wc.indexOf(";span") != -1){
                        String ud = wc.substring(0,wc.indexOf(";span"));
                        numMatch += matchStructuredPub(ud);
                        if(numMatch == 0 && ud.indexOf("side=*") != -1){
                            numMatch += matchStructuredPub(ud.replace("side=*","side=RECTO"));
                            numMatch += matchStructuredPub(ud.replace("side=*","side=VERSO"));
                        }
                    }
                }
            }
            if(numMatch > 0){
                matched.add(struct);
                if(sided) matched.add(struct.replaceAll("(RECTO|VERSO)", "*"));
            }
            result += numMatch;
        }    
        return result;
    }
    
    public void setPublicationsAbout(String pubAbout){
        if(this.pubInfo == null) this.pubInfo = pubAbout;
        else this.pubInfo += " ; " + pubAbout;
        Collection<String> matches = PublicationMatcher.findMatches(pubAbout);
        handleMatches(matches); 
    }

    private int matchStructuredPub(String struct) throws IOException, OutOfRangeException {
        int result = 0;
        Term term = STRUCT_PUB_TERM.createTerm(struct);
        Query query = new TermQuery(term);
        Hits hits = hgvSearcher.search(query);
        for(int i = 0; i < hits.length(); i++){
            addHGV(hits.id(i));
            result++;
        }
        if(result == 0){
            query = new PrefixQuery(term);
            hits = hgvSearcher.search(query);
            for(int i = 0; i < hits.length(); i++){
                addHGV(hits.id(i));
                result++;
            }
        }
        return result;
    }

    public void scrubPublications(){
        System.out.print(true);
        StringBuffer pubBuffer = new StringBuffer();

        Iterator pubIter = this.pubs.iterator();
        if (pubInfo != null){
            pubBuffer.append(pubInfo);
            if(pubIter.hasNext()) pubBuffer.append(" ; ");
        }
        while (pubIter.hasNext()){
            String next = pubIter.next().toString();
            pubBuffer.append(next);
            if(pubIter.hasNext()) pubBuffer.append(" ; ");
        }
        PublicationScrubber scrubber = PublicationScrubber.get(pubBuffer.toString(), this);
        Collection<String> scrubbed = scrubber.getPublications();
        if (scrubbed != null && scrubbed.size() > 0){
            this.pubs.clear();
            this.struct_pubs.clear();
            for(String scrub:scrubbed) this.pubs.add(scrub.replaceAll("\\s+"," "));
        }
        for(String p:this.pubs){
            Set<String> struct = StructuredPublication.getStructuredPub(p);
            if(struct.size() > 0) {
                for(String sp:struct){
                    System.err.println(sp);
                    this.struct_pubs.add(sp);
                }
            }
            else {
                if(!"".equals(p) && !p.matches("^[\\s\\r\\n]+$"))  System.err.println("Could not structure: " + p);
            }
        }

    }

    public Iterator<String> getPublications(){
        ArrayList<String>result = new ArrayList<String>(pubs.size());
        result.addAll(pubs);
        return result.iterator();
    }

    public boolean addPublication(String pub){
        String [] split = pub.split("[\\|;]");
        boolean result = false;
        for (String s:split){
            if((s=s.trim()).equals("")) continue;
            result = (result || pubs.add(s));
        }
        return result;
    }

    public boolean removePublication(String pub){
        String [] split = pub.split("[\\|;]");
        boolean result = false;
        for (String s:split){
            result = (result || pubs.remove(s));
        }
        return result;
    }

    private LookupMatch lookup(String pub,int field) throws IOException, OutOfRangeException {
        pub = normalize(pub);
        LookupMatch result = new LookupMatch(new ArrayList<String>(),new IntQueue(), FIELDS[field]);
        LookupMatch match = search(pub,field);
        if (match.matches.size() > 0 || match.docIds.size() > 0){
            result = match;
        }
        String [] parts = pub.split("\\s");
        if (result.matches.size() == 0 && result.docIds.size() == 0){

            if (parts.length ==4 && parens.matcher(parts[3]).find()){
                String key = pub.replaceAll("\\(", "").replaceAll("\\)",""); 
                match = search(key,field);
                if ((match.matches != null && match.matches.size() > 0) || (match.docIds != null && match.docIds.size() > 0)) return match;
            }
        }
        if (result.matches.size() == 0 && result.docIds.size() == 0){
            if (parts[parts.length - 1].matches("[RV]") && pub.indexOf(' ') != -1){
                String key = pub.substring(0,pub.lastIndexOf(' '));
                match =search(key,field);
                if ((match.matches != null && match.matches.size() > 0) || (match.docIds != null && match.docIds.size() > 0)) return match;
            }
        }
        //System.out.println("lookup returning " + match.matches.size() + " matches");
        return result;
    }

    private String getHGVKey(String key){
        if (key.startsWith("O.Berenike")){
            return key.replaceFirst("enike",".");
        }
        if (key.startsWith("P.Stras.")){
            return key.replaceFirst("tras.", "trasb.");
        }
        if (key.startsWith("P.Ross.Georg.")){
            return key.replaceFirst("Georg", " Georg");
        }
        return key;
    }

    public Iterator<String> getStructs(){
        return Arrays.asList(struct_pubs.toArray(new String[0])).iterator();
    }

    private LookupMatch search(String stdKey, int field) throws IOException, OutOfRangeException {
        String hgvKey = getHGVKey(stdKey);
        Term cnTerm = PUB_TERM.createTerm(hgvKey);
        Hits hits = hgvSearcher.search(new TermQuery(cnTerm));
        Iterator<Hit> hitIter = hits.iterator();
        IntQueue hgvIds = new IntQueue(4);
        ArrayList<String> hgvNames = new ArrayList<String>();
        while(hitIter.hasNext()){
            Hit hit = hitIter.next();
            String cn = hit.get(CoreMetadataFields.DOC_ID);
            if (cn != null && !hgvIds.contains(hit.getId())){
                hgvIds.add(hit.getId());
                hgvNames.add(cn);
                if (hit.get(CoreMetadataFields.DATE1_I) != null){
                    String d1 = hit.get(CoreMetadataFields.DATE1_I);
                    String d2 = hit.get(CoreMetadataFields.DATE2_I);
                    if(d2 == null) d2 = d1;
                    this.addDateIndex(d1,d2,false);
                }
                if (hit.get(CoreMetadataFields.IMG_URL) != null || hit.get(CoreMetadataFields.BIBL_ILLUSTR) != null){
                    this.setHasImage();
                }
                boolean hasTranslation = hit.get(CoreMetadataFields.TRANSLATION_EN) != null ;
                hasTranslation |= hit.get(CoreMetadataFields.TRANSLATION_DE) != null ;
                hasTranslation |= hit.get(CoreMetadataFields.TRANSLATION_FR) != null ;
                hasTranslation |= hit.get(CoreMetadataFields.BIBL_TRANS) != null ;
                if(hasTranslation) this.setHasTranslation();
            }
        }
        if (hgvIds.size() == 0){
            hits = hgvSearcher.search(new TermQuery(CN_TERM.createTerm(hgvKey)));
            hitIter = hits.iterator();
            while(hitIter.hasNext()){
                Hit hit = hitIter.next();
                String cn = hit.get(CoreMetadataFields.DOC_ID);
                if (cn != null && !hgvIds.contains(hit.getId())){
                    hgvIds.add(hit.getId());
                    hgvNames.add(cn);
                    if (hit.get(CoreMetadataFields.DATE1_I) != null){
                        String d1 = hit.get(CoreMetadataFields.DATE1_I);
                        String d2 = hit.get(CoreMetadataFields.DATE2_I);
                        if(d2 == null) d2 = d1;
                        this.addDateIndex(d1,d2,false);
                    }
                    if (hit.get(CoreMetadataFields.IMG_URL) != null || hit.get(CoreMetadataFields.BIBL_ILLUSTR) != null){
                        this.setHasImage();
                    }
                    boolean hasTranslation = hit.get(CoreMetadataFields.TRANSLATION_EN) != null ;
                    hasTranslation |= hit.get(CoreMetadataFields.TRANSLATION_DE) != null ;
                    hasTranslation |= hit.get(CoreMetadataFields.TRANSLATION_FR) != null ;
                    hasTranslation |= hit.get(CoreMetadataFields.BIBL_TRANS) != null ;
                    if(hasTranslation) this.setHasTranslation();
                }
            }
        }
        Set<String> success = new HashSet<String>();
        if (hgvIds.size() == 0){
            hits = hgvSearcher.search(new WildcardQuery(PUB_TERM.createTerm(hgvKey + ",*")));
            hitIter = hits.iterator();
            hits:
                while(hitIter.hasNext()){
                    Hit hit = hitIter.next();
                    String cn = hit.get(APISIndices.CONTROL_NAME);

                    for(String s:success){
                        if (!hgvKey.equals(s) && s.startsWith(hgvKey)){
                            break hits;
                        }
                    }
                    success.add(hgvKey);
                    if (cn != null && !hgvIds.contains(hit.getId())){
                        hgvIds.add(hit.getId());
                        hgvNames.add(cn);
                        if (hit.get(CoreMetadataFields.DATE1_I) != null){
                            String d1 = hit.get(CoreMetadataFields.DATE1_I);
                            String d2 = hit.get(CoreMetadataFields.DATE2_I);
                            if(d2 == null) d2 = d1;
                            this.addDateIndex(d1,d2,false);
                        }
                        if (hit.get(CoreMetadataFields.IMG_URL) != null || hit.get(CoreMetadataFields.BIBL_ILLUSTR) != null){
                            this.setHasImage();
                        }
                        boolean hasTranslation = hit.get(CoreMetadataFields.TRANSLATION_EN) != null ;
                        hasTranslation |= hit.get(CoreMetadataFields.TRANSLATION_DE) != null ;
                        hasTranslation |= hit.get(CoreMetadataFields.TRANSLATION_FR) != null ;
                        hasTranslation |= hit.get(CoreMetadataFields.BIBL_TRANS) != null ;
                        if(hasTranslation) this.setHasTranslation();
                    }
                }
        }
        if (hgvIds.size() == 0){
            hits = hgvSearcher.search(new WildcardQuery(PUB_TERM.createTerm(hgvKey + " *")));
            hitIter = hits.iterator();
            hits:
                while(hitIter.hasNext()){
                    Hit hit = hitIter.next();
                    String cn = hit.get(APISIndices.CONTROL_NAME);

                    for(String s:success){
                        if (!hgvKey.equals(s) && s.startsWith(hgvKey)){
                            break hits;
                        }
                    }
                    success.add(hgvKey);
                    if (cn != null && !hgvIds.contains(hit.getId())){
                        hgvIds.add(hit.getId());
                        hgvNames.add(cn);
                        if (hit.get(CoreMetadataFields.DATE1_I) != null){
                            String d1 = hit.get(CoreMetadataFields.DATE1_I);
                            String d2 = hit.get(CoreMetadataFields.DATE2_I);
                            if(d2 == null) d2 = d1;
                            this.addDateIndex(d1,d2,false);
                        }
                        if (hit.get(CoreMetadataFields.IMG_URL) != null || hit.get(CoreMetadataFields.BIBL_ILLUSTR) != null){
                            this.setHasImage();
                        }
                        boolean hasTranslation = hit.get(CoreMetadataFields.TRANSLATION_EN) != null ;
                        hasTranslation |= hit.get(CoreMetadataFields.TRANSLATION_DE) != null ;
                        hasTranslation |= hit.get(CoreMetadataFields.TRANSLATION_FR) != null ;
                        hasTranslation |= hit.get(CoreMetadataFields.BIBL_TRANS) != null ;
                        if(hasTranslation) this.setHasTranslation();
                    }
                }
        } 
        return new LookupMatch(hgvNames, hgvIds,FIELDS[field]);
    }
    public void addXref(String xref){
        if(!xref.startsWith(NamespacePrefixes.ID_NS)) throw new IllegalArgumentException("Not a papyri.info xref: " + xref);
        xref=xref.trim();
        if(xref.startsWith(NamespacePrefixes.DDBDP)){
            String ddb = xref.substring(NamespacePrefixes.DDBDP.length());
            
            if(ddb.startsWith(POXY_STRUCT)){ // P.Oxy
                int lastSep = ddb.lastIndexOf(':');
                if(lastSep != -1){
                    String doc = ddb.substring(lastSep + 1);
                    if(doc.matches("^\\d+$")){
                        xref = NamespacePrefixes.DDBDP + "0181:" + VolumeUtil.arabicForPOxyDoc(Integer.parseInt(doc)) + ":" + doc;
                    }
                }
            }
            else if(ddb.startsWith(SB_STRUCT)){
                int lastSep = ddb.lastIndexOf(':');
                if(lastSep != -1){
                    String doc = ddb.substring(lastSep + 1);
                    if(doc.matches("^\\d+$")){
                        xref = NamespacePrefixes.DDBDP + "0239:" + VolumeUtil.arabicForSBDoc(Integer.parseInt(doc)) + ":" + doc;
                    }
                }
             }
        }
        xrefs.add(xref);
    }
    
    public Set<String> getXrefs(){
        return this.xrefs;
    }

    private String normalize(String val){
        val = val.trim();
        val = val.replaceAll("\\s+", " ");
        return val;
    }

    public void logDuplicates(){
        Collection<String> hgvIds = getHGVNames();
        if (hgvIds  == null) return;
        if (hgvIds.size() > 1){
//          System.out.println("Multiple APIS: pubL [" + normal + "]");
            for (String hgvId:hgvIds){
                duplicates.println("MULTIHGVTOAPIS\t\"" + controlName + "\"\t" + hgvId);
            }
        }
    }

    static class LookupMatch {
        LookupMatch(ArrayList<String> pubMatches, IntQueue ids, String pattern){
            matches = pubMatches;
            docIds = ids;
            pubmatch = pattern;
            if(matches.size() != docIds.size()) throw new IllegalArgumentException("match queues are unequal");
        }

        ArrayList<String> matches;
        IntQueue docIds;
        String pubmatch;
    }    
}