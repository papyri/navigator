package info.papyri.index;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spell.LuceneDictionary;


import util.lucene.ConstantBitsetFilter;


import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.NamespacePrefixes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.*;


public class LuceneIndex {
    private static final Logger LOG = Logger.getLogger(LuceneIndex.class);
    public static IndexReader INDEX_HGV = null;
    public static IndexReader INDEX_COL = null;
    public static IndexReader INDEX_XREF = null;
    public static IndexSearcher SEARCH_HGV = null;
    public static IndexSearcher SEARCH_COL = null;
    public static IndexSearcher SEARCH_XREF = null;
    public static ConstantBitsetFilter LOOSE_APIS_FILTER = null;
    public static ConstantBitsetFilter LOOSE_HGV_FILTER = null;
    public static ConstantBitsetFilter XREF_MAPPED = null;
    static {
        BooleanQuery.setMaxClauseCount(24576);
    }
    public static boolean getIndexedValues(){
        ArrayList<String> pubSeries = new ArrayList<String>();
        ArrayList<String> provenance = new ArrayList<String>();
        ArrayList<String>lang = new ArrayList<String>();
        ArrayList<String>collection = new ArrayList<String>();
        try{
            Term template = new Term("controlName",NamespacePrefixes.APIS);
            TermEnum cns = INDEX_COL.terms(template);
            Term term = cns.term();
            Term next;
            do{
                if(term==null || !template.field().equals(term.field()) || !term.text().startsWith(NamespacePrefixes.APIS)) break;
                String text = term.text().substring(NamespacePrefixes.APIS.length());
                if(text.indexOf(':') != -1){
                    text = text.substring(0,text.indexOf(':'));
                    collection.add(text);
                    next = template.createTerm(NamespacePrefixes.APIS + text + LuceneIndex.COLON_PLUS_ONE);
                    if(LOG.isDebugEnabled())LOG.debug("skip to " + next);
                } else next = template.createTerm(NamespacePrefixes.APIS + text + "Z");
            }while(cns.skipTo(next) && (term = cns.term()) != null);
            
            TermEnum pubs = INDEX_COL.terms();
            while (pubs.next()) {
                next = pubs.term();
                String field = next.field();
                String val = next.text();
                if (CoreMetadataFields.INDEXED_SERIES.equals(field)){
                    if (!pubSeries.contains(val)){
                        pubSeries.add(val);    
                    }
                }
                else if (CoreMetadataFields.PROVENANCE.equals(field)){
                    if (!provenance.contains(val))provenance.add(val);
                }
                else if (CoreMetadataFields.LANG.equals(field)){
                    if (!lang.contains(val))lang.add(val);
                }
                else continue;
            }
            pubs = INDEX_HGV.terms();
            while (pubs.next()) {
                next = pubs.term();
                String field = next.field();
                String val = next.text();
                if (CoreMetadataFields.INDEXED_SERIES.equals(field)){
                    if (!pubSeries.contains(val)){
                        //System.out.println(val);
                        pubSeries.add(val);    
                    }
                }
                else if (CoreMetadataFields.PROVENANCE.equals(field)){
                    if (!provenance.contains(val))provenance.add(val);
                }
                else if (CoreMetadataFields.LANG.equals(field)){
                    if (!lang.contains(val))lang.add(val);
                }
                else continue;
            }
            
            String [] results = pubSeries.toArray(new String[0]);
            java.util.Arrays.sort(results);
            LuceneIndex.INDEXED_SERIES = new ArrayList<String>(results.length);
            LuceneIndex.INDEXED_SERIES.addAll(java.util.Arrays.asList(results));
            
            results = collection.toArray(new String[0]);
            java.util.Arrays.sort(results);
            LuceneIndex.COLLECTIONS = new TreeSet<String>();
            LuceneIndex.COLLECTIONS.addAll(java.util.Arrays.asList(results));
            
            results = provenance.toArray(new String[0]);
            java.util.Arrays.sort(results);
            LuceneIndex.PROVENANCE = results;
            
            results = lang.toArray(new String[0]);
            java.util.Arrays.sort(results);
            LuceneIndex.LANG = results;
            
            
        }
        catch (IOException ioe){
            ioe.printStackTrace();
            return false;
        }
        return true;
        
    }
    private static ArrayList<String> getSeries() {
        ArrayList<String> result = new ArrayList<String>();
        LuceneDictionary apisPub = new LuceneDictionary(INDEX_COL,
                CoreMetadataFields.INDEXED_SERIES);
        Term indexed = new Term(CoreMetadataFields.INDEXED_SERIES,"");
        String [] results = new String[0];
        try{
        org.apache.lucene.index.TermEnum pubs = INDEX_COL.terms();
        while (pubs.next()) {
            Term next = pubs.term();
            if (!next.field().equals(CoreMetadataFields.INDEXED_SERIES)) continue;
            String series = next.text();
            if (!result.contains(series)) {
                result.add(series);
            }
        }
    
        pubs = INDEX_HGV.terms();
        while (pubs.next()) {
            Term next = pubs.term();
            if (!next.field().equals(CoreMetadataFields.INDEXED_SERIES)) continue;
            String series = next.text();
            if (!result.contains(series)) {
                result.add(series);
            }
        }
        
        results =  result.toArray(new String[0]);
        java.util.Arrays.sort(results);
        }
        catch (IOException ioe){
            
        }
        
        ArrayList<String> r = new ArrayList<String>(results.length);
        for (String next: results){
            r.add(next);
        }
        return r;
    }
    private static List getCollections(){
    
        ArrayList<String> result = new ArrayList<String>();
        try{
            Term template = new Term("controlName",NamespacePrefixes.APIS);
            TermEnum cns = INDEX_COL.terms(template);
            Term term = cns.term();
            Term next;
            do{
                if(term==null || !template.field().equals(term.field()) || !term.text().startsWith(NamespacePrefixes.APIS)) break;
                String text = term.text().substring(NamespacePrefixes.APIS.length());
                if(text.indexOf(':') != -1){
                    text = text.substring(0,text.indexOf(':'));
                    result.add(text);
                    next = template.createTerm(NamespacePrefixes.APIS + text + LuceneIndex.COLON_PLUS_ONE);
                    //System.out.println("skip to " + next);
                } else next = template.createTerm(NamespacePrefixes.APIS + text + "Z");
            }while(cns.skipTo(next) && (term = cns.term()) != null);
        }
        finally{
            return result;
        }
    }
    private static String [] getProvenanceValues(){
        Iterator apis = LuceneIndex.getWordsIterator(INDEX_COL, CoreMetadataFields.PROVENANCE);
        Iterator hgv = LuceneIndex.getWordsIterator(INDEX_HGV, CoreMetadataFields.PROVENANCE);
        ArrayList<String> words = new ArrayList<String>();
        while (apis.hasNext()){
            String next = (String)apis.next();
            if (!words.contains(next)) words.add(next);
        }
        while (hgv.hasNext()){
            String next = (String)hgv.next();
            if (!words.contains(next)) words.add(next);
        }
        String [] wordArray = words.toArray(new String[0]);
        java.util.Arrays.sort(wordArray);
        return wordArray;
        
    }
    public static Iterator getWordsIterator(IndexReader index, String field){
        return new LuceneDictionary(index, field).getWordsIterator();    
    }

    public static final String COLON_PLUS_ONE = new String(new char[]{(':')+1});
    public static Iterator<String> getIndexedSeries(){
        return LuceneIndex.INDEXED_SERIES.iterator();    
    }
    public static ArrayList<String> INDEXED_SERIES = null;
    public static TreeSet<String> COLLECTIONS = null;
    public static String [] PROVENANCE = null;
    public static String [] LANG = null;
    public static Iterator<String> getIndexedApisCollections(){
        return COLLECTIONS.iterator();
        
    }
    public static final Iterator<String> getIndexedProvenanceValues(){
        return java.util.Arrays.asList(PROVENANCE).iterator();    
    }
    public static final Iterator<String> getIndexedLanguageValues(){
        return java.util.Arrays.asList(LANG).iterator();    
    }
    public static final String getLanguageLabel(String lc){
        if(lc == null) return lc;
        lc = lc.trim();
        if("ar-Arab".equals(lc)) return "Arabic";
        if("arc-Armi".equals(lc)) return "Aramaic";
        if("egy-Copt".equals(lc)) return "Coptic";
        if("egy-Egyd".equals(lc)) return "Demotic";
        if("en".equals(lc)) return "English"; // really?
        if("grc-Grek".equals(lc)) return "Greek";
        if("he-Hebr".equals(lc)) return "Hebrew";
        if("egy-Egyh".equals(lc)) return "Hieratic";
        if("egy-Egyp".equals(lc)) return "Hieroglyphic";
        if("it-Latn".equals(lc)) return "Italian";
        if("la-Latn".equals(lc)) return "Latin";
        if("pal-Phli".equals(lc)) return "Middle Persian";
        if("xpr-Prti".equals(lc)) return "Parthian";
        if("sem".equals(lc)) return "Semitic";
        if("syc-Syrc".equals(lc)) return "Syriac";
        LOG.error("Unknown lc code: \"" + lc + "\"");
        return lc;
    }
    
}
