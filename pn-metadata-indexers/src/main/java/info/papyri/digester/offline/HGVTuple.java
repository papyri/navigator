package info.papyri.digester.offline;

import info.papyri.data.APISIndices;
import info.papyri.data.publication.StructuredPublication;
import info.papyri.util.NumberConverter;

import java.util.regex.*;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.document.*;

import java.util.*;
import java.io.IOException;
import java.io.PrintStream;

public class HGVTuple {
    static final String startP = "^(.*?)";
    static final String endP = "(.*?$)";
    static final String seriesP = "(([PO]\\.([A-Z][a-zA-Z]*\\.?))(\\s[A-Z][a-zA-Z]*\\.?)?|(SB))";
    static final String volP = "(\\s[MDCLXVI]+)";
    static final String docP = "(\\s\\d+((\\.\\d)?|(\\s?[a-z]))?)";
    static final String sideP = "((\\s((V(erso)?)|(R(ecto)?)))?(\\s[a-z0-9])?)?";
    static final String col = "(\\sKol\\.\\s[XIV]+(\\s[0-9a-z])?)";
    static Pattern columnPub = Pattern.compile(startP + "(" + seriesP + volP + docP + col + ")" + "(.+$)");
    static Pattern plainPub = Pattern.compile(startP + "(" + seriesP + volP + docP + sideP + ")" + endP);
    static Pattern DIGITS = Pattern.compile("\\d+");
    static Pattern ROMAN = Pattern.compile("[MDCLXVI]+");
        //Pattern.compile("\\b([([PO]\\.([A-Z][a-zA-Z]*\\.))(SB)]+\\s[MDCLXVI]+\\s\\d+(\\s[a-z])?(\\s[RV])?\\b)(descr\\.)?.*");
    static Term PUB_TERM = new Term(APISIndices.PUBLICATION,"");
    static Term DDB_TERM = new Term(APISIndices.DDBDP_FIRST,"");
    static Term CN_TERM = new Term(APISIndices.CONTROL_NAME,"");
    private final PrintStream duplicates;
    ArrayList<String>controlNameList = new ArrayList<String>();
    String pl;
    String zusätzlich;
    String texID;
    String publikation;
    String nummer;
    String band;
    boolean log = false;
    static int totalBand;
    static int totalRomanBand;
    ArrayList<String> pubs = new ArrayList<String>();
    ArrayList<String> struct_pubs = new ArrayList<String>();
    IndexSearcher s;
    static int PL = 0;
    static int ANDERE = 1;
    static int DDB = 2;
    static String [] FIELDS = {"pubL","andere","ddb"};
    static int unmod_pub_match  = 0;
    static int plain_pub_match  = 0;
    static int nrml_space_pub_match = 0;
    static int nrml_space_pub_match3 = 0;
    static int nrml_space_pub_match2 = 0;
    static int ddb_match = 0;
    
    HGVTuple(IndexSearcher s, PrintStream duplicates){
        this.s = s;
        this.duplicates = duplicates;
    }
    HGVTuple(IndexSearcher s){
        this(s,System.out);
    }
    
    public static void report(){
        System.out.println("unmod_pub_match " + unmod_pub_match);
        System.out.println("plain_pub_match " + plain_pub_match);
        System.out.println("nrml_space_pub_match " + nrml_space_pub_match);
        System.out.println("nrml_space_pub_match3 " + nrml_space_pub_match3);
        System.out.println("nrml_space_pub_match2 " + nrml_space_pub_match2);
        System.out.println("ddb_match " + ddb_match);
        
    }
    
    public ArrayList<String> getControlNames(){
        return this.controlNameList;
        //return (this.controlNameList.size() > 0)?this.controlNameList.get(0):null;
    }
    
    void addControlNames(Collection<String> names){
        if (names == null) return;
        Iterator<String> iter = names.iterator();
        while(iter.hasNext()){
            String next = iter.next();
            if (!this.controlNameList.contains(next)){
                this.controlNameList.add(next);
                try{
                    Hits hits = s.search(new TermQuery(CN_TERM.createTerm(next)));
                    if (hits.length() > 0){
                        Document apis = ((Hit)hits.iterator().next()).getDocument();
                        String [] publications = apis.getValues(APISIndices.PUBLICATION);
                        if (publications == null) continue;
                        for (String pub: publications){
                            if (!this.pubs.contains(pub))this.pubs.add(pub);    
                        }
                    }
                }
                catch (IOException ioe){
                    
                }
            }
        }
    }
    
    public void setPublikationL(String pl){
        if (this.pl != null){
            System.err.println("Multiple PubL: " + this.pl + " " + pl);
            return;
        }
        String normal = normalize(pl);
//        if (pl.startsWith("P.Zen. Pestm.")) this.log = true;
        this.pl = normal;
        normal = normal.replaceAll("O.Ber.", "O.Berenike");
        if (pubs.contains(pl) || pubs.contains(normal))return;
        LookupMatch match = lookup(normal,PL);
        ArrayList<String> controlNames =  match.apismatches;
        if (controlNames != null){
            addControlNames(controlNames);
        }
        pubs.add(pl);
        pubs.add(normal);
        Set<String> struct = StructuredPublication.getStructuredPub(pl);
        if(struct.size() > 0){
            for(String sp:struct) struct_pubs.add(sp);
        }
    }
    
    public String getPublikationL(){
        return this.pl;
    }
    

    
    public void setPublikation(String val){
        if (val == null) return;
        this.publikation = val;
        Set<String> struct = StructuredPublication.getStructuredPub(pl);
        if(struct.size() > 0){
            for(String sp:struct) struct_pubs.add(sp);
        }
        String normal = normalize(val);
        if (pubs.contains(val) || pubs.contains(normal))return;
        LookupMatch match = lookup(normal,PL);
        ArrayList<String> controlNames =  match.apismatches;
        if (controlNames != null){
            addControlNames(controlNames);
        }
    }
    
    public void setBand(String val){
        if (val == null) return;
        val = val.trim();
        totalBand++;
        if (val.indexOf("~\\d") != -1){
            totalRomanBand++;
            this.band = val;
        }
        else{
            this.band = NumberConverter.getRoman(val);
        }
        this.band = NumberConverter.getRoman(val);
    }
    
    public void setNummer(String val){
        if (val == null) return;
        this.nummer = val.trim();
    }
    
    public Iterator<String> getPublications(){
        ArrayList<String>result = new ArrayList<String>(pubs.size() + 1);
        result.addAll(pubs);
        if (!result.contains(pl)) result.add(pl);
        return result.iterator();
    }
    
    public void setZusätzlich(String val){
        if (val == null) return;
        this.zusätzlich = val.trim();
    }
    
    public void setTexID(String val){
        this.texID = val.trim();
    }
    
    public String getTexID(){
        return this.texID;
    }
    
    LookupMatch lookup(String pl,int field){
        boolean log = false;
        //String controlName = null;
        ArrayList<String> controlNames = null;
            String publication = pl;

        try{
            Matcher match = columnPub.matcher(publication);
            if (match.matches()){
                publication = match.group(2);
                controlNames = searchIndex(PUB_TERM.createTerm(publication),field);
                if (controlNames != null) return new LookupMatch(controlNames,publication);
            }
            
            controlNames = searchIndex(PUB_TERM.createTerm(publication),field);
                if (controlNames != null){
                    unmod_pub_match++;
                    return new LookupMatch(controlNames,publication);
                }

            String [] parts = publication.split(" ");
            if (parts.length > 4){
                String key = parts[0];
                for (int i=1;i<parts.length;i++){
                    key += " " + parts[i];
                }
                controlNames = searchIndex(PUB_TERM.createTerm(key),field);
                if (log)System.out.println(key);
                if (controlNames != null){
                    nrml_space_pub_match++;
                    return new LookupMatch(controlNames,key);
                }
            }
            if (parts.length > 3){
                String key = parts[0] + " " + parts[1] + " " + parts[2];

                   String tileKey = key + parts[3];
                   controlNames = searchIndex(PUB_TERM.createTerm(tileKey),field);
                   if (log)System.out.println(tileKey);
                  if (controlNames != null){
                      System.out.println(tileKey);
                      return new LookupMatch(controlNames,tileKey);
                  }

                    tileKey = key + " (" +  parts[3] + ")";
                    controlNames = searchIndex(PUB_TERM.createTerm(tileKey),field);
                    if (log)System.out.println(tileKey);
                    if (controlNames != null){
                        System.out.println(tileKey);
                        return new LookupMatch(controlNames,tileKey);
                    }

            }
                if (parts.length > 2){
                    String key = parts[0] + " " + parts[1] + " " + parts[2];
                    controlNames = searchIndex(PUB_TERM.createTerm(key),field);
                    if (log)System.out.println(key);
                    if (controlNames != null){
                        nrml_space_pub_match3++;
                        return new LookupMatch(controlNames,key);
                    }

                    String part2 = parts[2].toUpperCase();
                    key = (!(part2.equals("R") || part2.equals("V")))?parts[0] + ":" + NumberConverter.getInt(parts[1]) + ":" + parts[2]:parts[0] + ":" + parts[1];
                    if (parts.length > 3){
                        key += parts[3];
                    }
                    controlNames = searchIndex(DDB_TERM.createTerm(key),DDB);
                    if (controlNames != null){
                        if (key.startsWith("P.Corn"))System.out.println(key + " ddb match: " + (controlNames.get(0)));
                        ddb_match++;
                        return new LookupMatch(controlNames,key);
                    }
                }

            if (parts.length == 2){
                String key = parts[0] + "  " + parts[1];
                controlNames = searchIndex(PUB_TERM.createTerm(key),field);
                if (controlNames != null){
                    nrml_space_pub_match2++;
                    return new LookupMatch(controlNames,key);
                }
            }
            
            Matcher m = plainPub.matcher(publication);
            if (m.matches()){
                publication = m.group(2);
                controlNames = searchIndex(PUB_TERM.createTerm(publication),field);
                if (controlNames != null){
                    plain_pub_match++;
                    return new LookupMatch(controlNames,publication);
                }
            }

            
            
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
        return new LookupMatch(controlNames,null);
    }

    
    private ArrayList<String> searchIndex(Term term, int field) throws IOException{
        TermQuery q = new TermQuery(term);
        Hits h = s.search(q);
        int len = h.length();
        ArrayList<String> cns = new ArrayList<String>(len);

        if (len > 0){
            for (int i=0;i<len;i++){
                String cn = h.doc(i).get("controlName");
              if (!cns.contains(cn)) cns.add(cn);    
            }
            return cns;
        }
        return null;
    }
    
    public void setAndere(String pl){
        
        String [] pubs = pl.split(";");
        for (int i=0;i<pubs.length;i++){
            String key = normalize(pubs[i]);
            
            if ("".equals(key) || " ".equals(key) || this.pubs.contains(key)) continue;
            this.pubs.add(key);
            Set<String> struct = StructuredPublication.getStructuredPub(key);
            if(struct.size() > 0){
                for(String sp:struct) struct_pubs.add(sp);
            }
            String normal = normalize(pubs[i]);

            LookupMatch match = lookup(normal,ANDERE);
            ArrayList<String> controlNames =  match.apismatches;
            if (controlNames != null){
                addControlNames(controlNames);
            }
            
        }
        
    }
    
    private String normalize(String val){
        val = val.trim();
        val = val.replaceAll("\\s+", " ");
        return val;
    }
    
    public void logDuplicates(){
        if (this.controlNameList  == null) return;
        if (this.controlNameList.size() > 1){
//          System.out.println("Multiple APIS: pubL [" + normal + "]");
          for (int j=0;j<controlNameList.size();j++){
              this.duplicates.println("MULTIAPISTOHGV\t\"" + pl + "\"\t" + controlNameList.get(j));
          }
      }
    }
    
    static class LookupMatch {
        LookupMatch(ArrayList<String> cnMatches, String pattern){
            apismatches = cnMatches;
            pubmatch = pattern;
        }
        ArrayList<String> apismatches;
        String pubmatch;
    }
}