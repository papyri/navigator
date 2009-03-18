package info.papyri.digester;

import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.metadata.NamespacePrefixes;
import info.papyri.metadata.OutOfRangeException;
import info.papyri.util.DBUtils;
import info.papyri.util.IntQueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jdom.input.DOMBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

import info.papyri.data.XREFIndices;
import info.papyri.digester.offline.APISTuple;
import info.papyri.data.Indexer;

public class APISCentricMergeIndexer  implements ObjectCreationFactory{
    private static final String HGV_ATT = "hgv";
    private static final String APIS_ATT = "apis";
    private static final String OUT_ATT = "out";
    public static PrintStream HGV_NO_APIS = System.out;
    public static PrintStream APIS_NO_HGV = System.out;
    public static PrintStream APIS_NO_PUB = System.out;
    public static PrintStream MULTIPLE_HGV_TO_APIS = System.out;
    public static PrintStream MULTIPLE_APIS_TO_HGV = System.out;
    private final static FieldSelector ID = new FieldSelector(){
        public FieldSelectorResult accept(String field){
            if(field.equals(CoreMetadataFields.DOC_ID)) return FieldSelectorResult.LOAD;
            return FieldSelectorResult.NO_LOAD;
        }
    };

    private int apisIdsCounted = 0;
    private int hgvNoApis = 0;
    private int apisNoHGV = 0;
    private int apisMapped = 0;
    private final int hgvDocs;
    private Indexer indexer;
    //private HashMap<String,String> mappedApis = new HashMap<String,String>();
    private BitSet mappedHGV;
    public static final String DATE_INDEX_ELEMENT = "dateIndex";
    public static final String DATE_START_ELEMENT = "start";
    public static final String DATE_END_ELEMENT = "end";
    public static final String HAS_IMAGE_ELEMENT = "hasImage";
    public static final String HAS_TRANSLATION_ELEMENT = "hasTranslation";
    public static final String LANG_ELEMENT = "language";
    public static final String PLACE_ELEMENT = "spatial";

    public Object createObject(Attributes arg0) throws Exception {

        return this;
    }
    public Digester getDigester() {
        // TODO Auto-generated method stub
        return null;
    }
    public void setDigester(Digester arg0) {
        // TODO Auto-generated method stub

    }

    public void merge(){
        try{


            int maxDoc = this.apisSearcher.maxDoc();
            IndexReader apisReader = this.apisSearcher.getIndexReader();
            for(int i = 0;i<maxDoc;i++){
                if(apisReader.isDeleted(i)) continue;
                org.apache.lucene.document.Document doc = apisReader.document(i);
                APISTuple tuple = new APISTuple(this.hgvSearcher,MULTIPLE_HGV_TO_APIS);
                tuple.setControlName(doc.get(CoreMetadataFields.DOC_ID));
                tuple.setPublication(doc.get(CoreMetadataFields.PUBLICATION_NOTE));
                tuple.setPublicationsAbout(doc.get(CoreMetadataFields.PUB_ABOUT));
                tuple.setInventory(doc.get(CoreMetadataFields.INV));
                tuple.setTitle(doc.get(CoreMetadataFields.TITLE));
                tuple.addNotes(doc.get(CoreMetadataFields.GEN_NOTES));
                tuple.addSummary(doc.get(CoreMetadataFields.SUMMARY));
                String [] subjects = doc.getValues(CoreMetadataFields.SUBJECT_I);
                if(subjects != null){
                    for(String subject:subjects)tuple.addSubject(subject);
                }
                tuple.addDateIndex(doc.get(CoreMetadataFields.DATE1_I),doc.get(CoreMetadataFields.DATE2_I),false);
                tuple.addDisplayDate(doc.get(CoreMetadataFields.DATE1_D),doc.get(CoreMetadataFields.DATE2_D));
                String [] xrefs = doc.getValues(CoreMetadataFields.XREFS);
                if(xrefs != null){
                    for(String xref:xrefs) tuple.addXref(xref);
                }
                String [] langs = doc.getValues(CoreMetadataFields.LANG);
                if(langs != null){
                    for(String lang:langs)tuple.addLanguage(lang);
                }
                String [] places = doc.getValues(CoreMetadataFields.PROVENANCE);
                if(places != null){
                    for(String place:places) tuple.addPlace(place);
                }
                if(doc.get(CoreMetadataFields.IMG_URL) != null) tuple.setHasImage();
                String translation = doc.get(CoreMetadataFields.TRANSLATION_EN);
                if(translation != null){
                    tuple.setTranslation(translation,CoreMetadataRecord.ModernLanguage.ENGLISH);
                }
                translation = doc.get(CoreMetadataFields.TRANSLATION_DE);
                if(translation != null){
                    tuple.setTranslation(translation,CoreMetadataRecord.ModernLanguage.GERMAN);
                }
                translation = doc.get(CoreMetadataFields.TRANSLATION_FR);
                if(translation != null){
                    tuple.setTranslation(translation,CoreMetadataRecord.ModernLanguage.FRENCH);
                }
                this.addTuple(tuple);
            }



            int dummyApisNum = 0;
            int next = -1;

            while ((next =  this.looseHGVIds.nextSetBit(next + 1)) != -1){

                MergeMetadataRecord result = new MergeMetadataRecord();
                org.apache.lucene.document.Document hgvDoc = this.hgvSearcher.doc(next);

                if (hgvDoc == null){
                    System.err.println(this.getClass().getSimpleName() + " : Unexpected no result: " + next);
                    continue;
                } else {
                    result.setControlName(hgvDoc.get(CoreMetadataFields.DOC_ID));
                    result.addXref(hgvDoc.get(CoreMetadataFields.DOC_ID));
                    String [] dateIndex = hgvDoc.getValues(CoreMetadataFields.DATE1_I);
                    if(dateIndex != null && dateIndex.length > 0){
                        String [] date2 = hgvDoc.getValues(CoreMetadataFields.DATE2_I);
                        if(date2==null) date2 = dateIndex;
                        for(int i=0;i<dateIndex.length; i++){
                            if(i<date2.length)result.setDateIndexPair(dateIndex[i], date2[i],false);
                            else result.setDateIndexPair(dateIndex[i],dateIndex[i],false);
                        }
                        result.setSortDate(dateIndex[0]);
                    }
                    String [] dateDisplay =  hgvDoc.getValues(CoreMetadataFields.DATE1_D);
                    if(dateDisplay !=  null){
                        for(String date:dateDisplay){
                            result.addDisplayDate(date);
                        }
                    }
                    String [] langs = hgvDoc.getValues(CoreMetadataFields.LANG);
                    if(langs != null){
                        for(String lang:langs){
                            result.addLanguage(lang);
                        }
                    }
                    String [] places = hgvDoc.getValues(CoreMetadataFields.PROVENANCE);
                    if(places != null){
                        for(String place:places){
                            result.addProvenance(place);
                        }
                    }
                    String [] subjects = hgvDoc.getValues(CoreMetadataFields.SUBJECT_I);
                    if(subjects != null){
                        for(String subject:subjects){
                            result.addSubjectSearchField(subject);
                        }
                    }
                    if(hgvDoc.get(CoreMetadataFields.IMG_URL) != null || CoreMetadataFields.SORTABLE_YES_VALUE.equals(hgvDoc.get(CoreMetadataFields.SORT_HAS_IMG))){
                        result.addIllustrationNote("illustration information available");
                    }
                    
                    if(hgvDoc.get(CoreMetadataFields.GEN_NOTES) != null){
                        result.addGeneralNotes(hgvDoc.get(CoreMetadataFields.GEN_NOTES));
                    }
                    
                    if(hgvDoc.get(CoreMetadataFields.BIBL_CORR) != null){
                        for(String note:hgvDoc.getValues(CoreMetadataFields.BIBL_CORR)) result.addCorrectionNote(note);
                    }

                    String translation = hgvDoc.get(CoreMetadataFields.TRANSLATION_EN);
                    if(translation != null) result.setTranslation(translation, CoreMetadataRecord.ModernLanguage.ENGLISH);
                    translation = hgvDoc.get(CoreMetadataFields.TRANSLATION_DE);
                    if(translation != null) result.setTranslation(translation, CoreMetadataRecord.ModernLanguage.GERMAN);
                    translation = hgvDoc.get(CoreMetadataFields.TRANSLATION_FR);
                    if(translation != null) result.setTranslation(translation, CoreMetadataRecord.ModernLanguage.FRENCH);
                    String [] biblTrans = hgvDoc.getValues(CoreMetadataFields.BIBL_TRANS);
                    if(biblTrans != null){
                        for(String note:biblTrans) result.addTranslationNote(note);
                    }

                    String [] xrefs = hgvDoc.getValues(CoreMetadataFields.XREFS);
                    
                    if (xrefs != null && xrefs.length > 0){
                        for (String xref: xrefs){
                            result.addXref(xref);
                        }
                    }
                    result.addXref(NamespacePrefixes.APIS + "none:" + ++dummyApisNum);
                    String [] pubs = hgvDoc.getValues(CoreMetadataFields.BIBL_PUB);
                    if (pubs != null && pubs.length != 0){
                        TreeSet<String> pubList = new TreeSet<String>();
                        pubList.addAll(Arrays.asList(pubs));
                        for (String publication: pubList){
                            result.addPublication(publication);
                        }
                    }
                }
                this.indexer.addCoreMetadataRecord(result);                   
            }

            this.indexer.writer.flush();
            this.indexer.writer.close();
        }
        catch (Throwable t){
            t.printStackTrace();
        }

        int next = -1;
        while ((next = this.looseHGVIds.nextSetBit(next+1)) != -1){
            try{
                if(next >= this.hgvSearcher.maxDoc()) {
                    break;
                }
                if(this.hgvSearcher.getIndexReader().isDeleted(next)) continue;
                this.hgvNoApis++;
                String lID = this.hgvSearcher.doc(next, ID).get(CoreMetadataFields.DOC_ID);
                HGV_NO_APIS.println("HGVNOAPIS\tunavailable\t" + lID);
            }catch(IOException ioe){
                ioe.printStackTrace();
                break;
            }
        }
    }
    
    public static void run(String apis, String hgv, String out){
        boolean test = false;
        boolean report = true;
        boolean reindex = true;
        BooleanQuery.setMaxClauseCount(64 * BooleanQuery.getMaxClauseCount());

        APISCentricMergeIndexer main = null;
        long start = System.currentTimeMillis();
        try {
            File outDir = new File(out); //"C:\\staging\\data\\index\\merge");
            File hgvDir = new File(hgv); //"C:\\staging\\data\\index\\hgv_epidoc");
            File apisDir = new File(apis); //"C:\\staging\\data\\index\\apis_oai");
            HashMap<String,Directory> directories = new HashMap<String,Directory>();
            directories.put(HGV_ATT, FSDirectory.getDirectory(hgvDir));
            directories.put(OUT_ATT, FSDirectory.getDirectory(outDir));
            directories.put(APIS_ATT, FSDirectory.getDirectory(apisDir));
            File syserr = new File("merge.system.err");
            PrintStream oldSysErr = System.err;
            File sysout = new File("merge.system.out");
            PrintStream oldSysOut = System.out;
            PrintStream newErr = new PrintStream(new FileOutputStream(syserr,false)); 
            PrintStream newOut = new PrintStream(new FileOutputStream(sysout,false)); 
            System.err.flush();
            System.setErr(newErr);
            System.out.flush();
            System.setOut(newOut);
            //indexDDBXREF(reindex);

            ThreadGroup tg = new ThreadGroup("local");
            java.net.URL derbydata = APISCentricMergeIndexer.class.getResource("/xml/ddbdp-perseus.xml");
            DBUtils.setupDerby(derbydata);

            main = new APISCentricMergeIndexer(reindex,report,directories);
            if (report){
                File apisnohgv = new File("apisnohgv.txt");
                File apisnopub = new File("apisnopub.txt");
                File hgvnoapis = new File("hgvnoapis.txt");
                File multihgvtoapis = new File("multihgvtoapis.txt");
                File multiapistohgv = new File("multiapistohgv.txt");
                APIS_NO_HGV = new PrintStream(new FileOutputStream(apisnohgv,false));
                APIS_NO_PUB = new PrintStream(new FileOutputStream(apisnopub,false));
                HGV_NO_APIS = new PrintStream(new FileOutputStream(hgvnoapis,false));
                MULTIPLE_APIS_TO_HGV = new PrintStream(new FileOutputStream(multiapistohgv,false));
                MULTIPLE_HGV_TO_APIS = new PrintStream(new FileOutputStream(multihgvtoapis,false));
            }
            main.merge();
            newErr.flush();
            System.setErr(oldSysErr);
            newOut.flush();
            System.setOut(oldSysOut);
            newErr.close();
            newOut.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }



        HGV_NO_APIS.flush();
        HGV_NO_APIS.close();
        APIS_NO_PUB.flush();
        APIS_NO_PUB.close();
        APIS_NO_HGV.flush();
        APIS_NO_HGV.close();
        MULTIPLE_APIS_TO_HGV.flush();
        MULTIPLE_APIS_TO_HGV.close();
        MULTIPLE_HGV_TO_APIS.flush();
        MULTIPLE_HGV_TO_APIS.close();
        System.out.println("Start: " + new java.util.Date(start));
        System.out.println("End:" + new java.util.Date());
        System.out.println("Total apis: " + main.apisIdsCounted);
        System.out.println("Total hgv: " + main.hgvDocs);
        System.out.println("Total apis mapped: " + main.apisMapped);
        System.out.println("Total hgv mapped: " + main.mappedHGV.cardinality());
        System.out.println("Total apis w/o hgv: " + main.apisNoHGV);
        System.out.println("Total hgv w/o apis: " + main.looseHGVIds.cardinality());
    }

    private static String [] getResource(NodeList nodes, String resourceVal) {
        if (resourceVal == null || nodes == null) return new String[0];
        ArrayList<String> vals = new ArrayList<String>();
        for (int i=0;i<nodes.getLength();i++){
            Node resource = nodes.item(i).getAttributes().getNamedItem("Resource");
            if (resource == null) continue;
            if (resourceVal.equals(resource.getNodeValue())){
                vals.add(nodes.item(i).getNodeValue());
            }
        }
        return vals.toArray(new String[vals.size()]);
    }


    private IndexSearcher hgvSearcher;
    private IndexSearcher apisSearcher;

    private BitSet looseHGVIds;

    private static final Term PUBLICATION = new Term("publication","");

    private static Directory getFSDir() throws IOException {
        String temp = System.getProperty("java.io.tmpdir");
        File tempF = new File(temp,"apisIndex");
        tempF.delete();
        FSDirectory fsd = FSDirectory.getDirectory(tempF,true);
        return fsd;
    }

    public APISCentricMergeIndexer(HashMap<String, Directory> directories) throws Exception{
        this(false,false,directories);
    }

    public APISCentricMergeIndexer(boolean reindex, boolean report, HashMap<String, Directory> directories) throws Exception{

        Directory outDir = directories.get(OUT_ATT); //FSDirectory.getDirectory(dir2, true);
        Directory hgvDir = directories.get(HGV_ATT); //FSDirectory.getDirectory( dir0 , false);
        Directory apisDir = directories.get(APIS_ATT); //FSDirectory.getDirectory( dir1 , false);

        this.hgvSearcher = new IndexSearcher(hgvDir);
        this.apisSearcher = new IndexSearcher(apisDir);
        this.indexer = new IndexerFactory(outDir,true).createObject(null);
        this.looseHGVIds = new BitSet(this.hgvSearcher.maxDoc());
        this.mappedHGV = new BitSet(this.hgvSearcher.maxDoc());
        if(this.hgvSearcher.maxDoc() == 0) throw new IllegalArgumentException("No docs in hgv index");
            IndexReader reader  = hgvSearcher.getIndexReader();
            org.apache.lucene.document.Document d = null;
            for (int i=0; i < this.hgvSearcher.maxDoc(); i++){
                if (reader.isDeleted(i)) continue;
                looseHGVIds.set(i);
            }
            this.hgvDocs = reader.numDocs();
    }

    public void addTuple(APISTuple tuple) throws IOException, OutOfRangeException {
        apisIdsCounted++;
        tuple.scrubPublications();
        tuple.matchStructuredPubs();
        if(tuple.getHGVNames().size()==0){
            tuple.matchXref();
        }
        MergeMetadataRecord rec = new MergeMetadataRecord();
        tuple.logDuplicates();
        HashMap<String,String> dateIndex = tuple.getDateIndexes();
        for(String key:dateIndex.keySet()){
            String val = dateIndex.get(key);
            rec.setDateIndexPair(key,val,false);
        }
        Collection<String> subjects = tuple.getSubjects();
        for(String subject:subjects){
            rec.addSubjectSearchField(subject);
        }
        Collection<String> langs = tuple.getLanguages();
        for(String lang:langs){
            rec.addLanguage(lang);
        }
        Collection<String> places = tuple.getPlaces();
        for(String place:places){
            rec.addProvenance(place);
        }
        String note = tuple.getNotes();
        if(note != null) rec.addGeneralNotes(note);
        note = tuple.getIllustrationNotes();
        if(note != null) rec.addIllustrationNote(note);
        note = tuple.getTranslationNotes();
        if(note != null) rec.addTranslationNote(note);
        note = tuple.getCorrectionNotes();
        if(note != null) rec.addCorrectionNote(note);
        if(tuple.getTranslation() != null){
            rec.setTranslation(tuple.getTranslation());
        }
        if(tuple.hasImage()){
            rec.addIllustrationNote("illustration information available");
        }
        if(tuple.hasTranslation()){
            rec.addTranslationNote("translation information available");
        }
        rec.setControlName(tuple.getControlName());
        rec.addXref(tuple.getControlName());
        if (tuple.getXrefs().size() > 0){
            Collection<String>tupleXrefs = tuple.getXrefs();
            for (String xref: tupleXrefs){
                rec.addXref(xref);
            }
        }
        rec.setInventoryNumber(tuple.getInventory());

        Iterator<String> iter = tuple.getPublications();
        while (iter.hasNext()){
            rec.addPublication(iter.next());
        }
        rec.setTitle(tuple.getTitle());
        rec.setSummary(tuple.getSummary());
        Collection<String> displayDates = tuple.getDisplayDates();
        for(String date:displayDates) rec.addDisplayDate(date);
        rec.setSortDate(tuple.getSortDate());
        boolean hgv = false;
        if (tuple.getHGVNames().size() > 0){
            hgv = true;
        }

        if (hgv){
            apisMapped++;
            IntQueue hgvIds = tuple.getHGVIds();
            while (hgvIds.size() > 0){
                int hgvId = hgvIds.next();
                if (this.looseHGVIds.get(hgvId)) {
                    this.looseHGVIds.set(hgvId,false);
                }

                if (mappedHGV.get(hgvId)){
                    String hgvName = hgvSearcher.doc(hgvId, ID).get(CoreMetadataFields.DOC_ID);
                    MULTIPLE_APIS_TO_HGV.println("MULTIAPISTOHGV\t\"" + hgvName + "\"\t" + tuple.getControlName());
                }
                else {
                    mappedHGV.set(hgvId);
                }
            }

        }
        else {
            apisNoHGV++;
            if(tuple.getPublications().hasNext()){
                APIS_NO_HGV.println("APISNOHGV\t\"" + tuple.getControlName() + "\"");
            }
            else{
                APIS_NO_PUB.println("APISNOPUB\t\"" + tuple.getControlName() + "\"");
            }
        }

       indexer.addCoreMetadataRecord(rec);
    }
}
