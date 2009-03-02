package info.papyri.data;
import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.CoreMetadataRecord;

import java.io.*;
import java.util.*;
import java.net.URL;

import info.papyri.digester.*;
import info.papyri.data.publication.PublicationScrubber;

import org.apache.lucene.analysis.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.jdom.input.DOMBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Indexer implements info.papyri.metadata.CoreMetadataFields {
    
    private static Term CN_TEMPLATE = new Term(DOC_ID,"");
    private static Term HGV_ID_TEMPLATE = new Term(XREFIndices.HGV_ID,"");
    private static Term APIS_ID_TEMPLATE = new Term(XREFIndices.APIS_ID,"");
    
    public IndexWriter writer;
    public Directory indexDir;

    public int numIndexedRecords = 0;

    public int numFailedRecords = 0;

    private static XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
    private static DOMBuilder db = new DOMBuilder();

    public Indexer(Directory dir, Analyzer analyzer, boolean create) throws IOException{
        this.indexDir = dir;
        this.writer = new IndexWriter(this.indexDir, analyzer, create);
        
        this.writer.setMergeFactor(25);
        this.writer.setRAMBufferSizeMB(256);
    }

    public Indexer(Directory dir, Analyzer analyzer) throws IOException{
        this(dir,analyzer,true);
    }

    private static Field getField(String name, String val,
            boolean store, Field.Index index) {
        Field.Store fstore = store ? Field.Store.YES : Field.Store.NO;
        //Field.Index findex = index ? Field.Index.TOKENIZED : Field.Index.NO;
        return new Field(name, val, fstore, index);
    }

    private static Vector<Field> getFields(String name, Vector<String> vals, boolean tokenized){
        Vector<Field> result = new Vector<Field>(vals.size());

        for (String val: vals){
            Field field = tokenized?(new Field(name,val,Field.Store.YES,Field.Index.TOKENIZED))
                    :new Field(name,val,Field.Store.YES,Field.Index.UN_TOKENIZED);
            result.add(field);
        }
        return result;
    }
    private static final String NO_IDS_TEMPLATE = "ERROR: $CLASS.getControlName() == null; $CLASS.getXrefs().size()== 0.  Skipping record.";
    public void addCoreMetadataRecord(CoreMetadataRecord coreData) throws IOException
    {
        boolean addDocumentFlag = true;
        boolean hasImages = false;
        boolean hasTranslation = false;
        boolean hasPublications = false;


        if (coreData.getControlName() == null) {
            if(coreData.getXrefs().size() > 0){
                System.out.println("ERROR: no control_name.  Skipping record.");
                for(String xref:coreData.getXrefs()){
                    System.err.println("\txref: " + xref);
                }
            }
            else System.err.println(NO_IDS_TEMPLATE.replaceAll("\\$CLASS", coreData.getClass().getSimpleName()));
            numFailedRecords++;
            return;
        }
        
        
        Document coreDocument  = null;
//        String cn = coreData.getControlName().intern();
        
//        int hash = cn.hashCode();
//        if (update){
//            writer.flush();
//            coreDocument = new IndexSearcher(this.indexDir).search(new TermQuery(CN_TEMPLATE.createTerm(cn))).doc(0);
//            if (coreDocument == null){
//                throw new RuntimeException("Cannnot find previously indexed document! [" + cn + " : " + hash + "]");
//            }
//            else {
//                addDocumentFlag = false;
//            }
//        }
//        else {
            coreDocument  = new Document();
            if (addDocumentFlag){
                coreDocument.add(new Field(DOC_ID,
                        coreData.getControlName(),
                        Field.Store.YES,
                        Field.Index.UN_TOKENIZED));
            }
//        }
        
        scrubPublications(coreData);

//        if (!addDocumentFlag){
//            updateCoreMetadataRecord(coreData, coreDocument);
//            return;
//        }

        if (coreData.getInstitution() != null){
            coreDocument.add(new Field(APIS_COLLECTION,
                    coreData.getInstitution(),
                    Field.Store.YES,
                    Field.Index.UN_TOKENIZED)
            );
        }
        Collection<String> all = coreData.getAll();
        if (all != null && all.size() > 0){
            coreDocument.add(new Field(ALL, 
                    CoreMetadataRecord.getMultipleValuesAsString(
                            all),
                            Field.Store.YES,
                            Field.Index.TOKENIZED)
            );
        }
        Collection<String> allNoTrans = coreData.getAllNoTrans();
        if (allNoTrans != null && allNoTrans.size() > 0){
            coreDocument.add(new Field(ALL_NO_TRANS, 
                    CoreMetadataRecord.getMultipleValuesAsString(
                            allNoTrans),
                            Field.Store.YES,
                            Field.Index.TOKENIZED)
            );
        }
        
        if(coreData.getSortDate()!=null){
            coreDocument.add(new Field(SORT_DATE,coreData.getSortDate(),Field.Store.YES,Field.Index.UN_TOKENIZED));
        }

        if (coreData.getAssociatedNames() != null && coreData.getAssociatedNames().size() > 0){
            Collection<String>names = coreData.getAssociatedNames();
            for(String name:names){
                coreDocument.add(new Field(NAME_ASSOC, 
                        name,
                        Field.Store.YES,
                        Field.Index.TOKENIZED));
            }
        }
        String correction = coreData.getCorrectionNote();
        if(correction != null && !"".equals(correction)){
            coreDocument.add(new Field(BIBL_CORR,correction,Field.Store.YES,Field.Index.NO));
        }
        
        String illustration = coreData.getIllustrationNote();
        if(illustration != null && !"".equals(illustration)){
            hasImages = true;
            coreDocument.add(new Field(BIBL_ILLUSTR,illustration,Field.Store.YES,Field.Index.NO));
        }
        
        String translationNote = coreData.getTranslationNote();
        if(translationNote != null && !"".equals(translationNote)){
            hasTranslation = true;
            coreDocument.add(new Field(BIBL_TRANS,translationNote,Field.Store.YES,Field.Index.NO));
        }

        if (coreData.getAuthor() != null && coreData.getAuthor().size() > 0){
            Collection<String> authors = coreData.getAuthor();
            for(String author:authors){
                coreDocument.add(new Field(NAME_AUTHOR, 
                        author,
                                Field.Store.YES,
                                Field.Index.NO));
            }
        }

        if (coreData.getXrefs().size() > 0){
            for(String xref:coreData.getXrefs()){
            coreDocument.add(new Field(XREFS,
                    xref,
                    Field.Store.YES,
                    Field.Index.UN_TOKENIZED));
        }
        }
        
        if(coreData instanceof MergeMetadataRecord){
            MergeMetadataRecord merge = (MergeMetadataRecord)coreData;
            for(String display:merge.getDisplayDates()){
                coreDocument.add(getField(DATE1_D,
                        display,
                        true,
                        Field.Index.NO));
            }
        }
        if (coreData.getDate1() != null) {
            coreDocument.add(getField(DATE1_D,
                    coreData.getDate1(),
                    true,
                    Field.Index.NO));
        }
        else {
            coreDocument.add(getField(DATE1_D,
                    CoreMetadataRecord.UNDEFINED_DATE,
                    true,
                    Field.Index.NO));
        }
        
        Map<String,String> dateIndices = coreData.getDateIndexes();
        if (dateIndices.size()>0){
            for(String date1:dateIndices.keySet()){
                String date2 = dateIndices.get(date1);
                coreDocument.add(getField(DATE1_I,
                        date1,
                        true,
                        Field.Index.UN_TOKENIZED));
                coreDocument.add(getField(DATE2_I,
                        date2,
                        true,
                        Field.Index.UN_TOKENIZED));
            }
        }

        if (coreData.getDate2() != null) {
            coreDocument.add(getField(DATE2_D,
                    coreData.getDate2(),
                    true,
                    Field.Index.NO));
        }
        else {
            if (coreData.getDate1() != null){
                coreDocument.add(getField(DATE2_D,
                        coreData.getDate1(),
                        true,
                        Field.Index.NO));
            }
        }

        if (coreData.getGeneralNotes() != null && coreData.getGeneralNotes().length() > 0){
            coreDocument.add(getField(GEN_NOTES, coreData.getGeneralNotes(),
                            true,
                            Field.Index.NO));
        }

        if (coreData.getHistoricalData() != null && coreData.getHistoricalData().size() > 0){
            coreDocument.add(getField(HIST_DATA, 
                    CoreMetadataRecord.getMultipleValuesAsString(
                            coreData.getHistoricalData()),
                            true,
                            Field.Index.NO)
            );
        }
        if (coreData.getWebImages() != null && coreData.getWebImages().size() > 0){
            hasImages = true;
            Map<URL,String>imageUris = coreData.getWebImages();
            for(URL url:imageUris.keySet()){
                String caption = imageUris.get(url);
                coreDocument.add(new Field(IMG_URL,url.toString(),Field.Store.YES,Field.Index.NO));
                coreDocument.add(new Field(IMG_CAPTION,caption,Field.Store.YES,Field.Index.NO));
            }
        }

        if (coreData.getInventoryNumber() != null){
            coreDocument.add(new Field(INV,
                    coreData.getInventoryNumber(),
                    Field.Store.YES,
                    Field.Index.TOKENIZED));
        }

        if (coreData.getLanguages() != null){
            Collection<String> langs = coreData.getLanguages();
            for (String lang: langs){
                coreDocument.add(getField(LANG,
                        lang,
                        true,
                        Field.Index.UN_TOKENIZED));                
            }

        }

        if (coreData.getMaterial() != null && coreData.getMaterial().size() > 0){
            coreDocument.add(new Field(MATERIAL, 
                    CoreMetadataRecord.getMultipleValuesAsString(
                            coreData.getMaterial()),
                            Field.Store.YES,
                            Field.Index.UN_TOKENIZED
            ));
        }

        if (coreData.getPhysicalDescription() != null) 
            coreDocument.add(getField(PHYS_DESC,
                    coreData.getPhysicalDescription(),
                    true,
                    Field.Index.NO));

        if (coreData.getProvenance() != null && coreData.getProvenance().size() > 0) 
            coreDocument.add(getField(PROVENANCE_NOTE,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            coreData.getProvenance()),
                            true,
                            Field.Index.TOKENIZED
            ));

        if (coreData.getProvenanceIndices() != null && coreData.getProvenanceIndices().size() > 0){
            Vector<Field> fields = getFields(PROVENANCE,coreData.getProvenanceIndices(),false);
            for (Field field: fields){
                coreDocument.add(field);
            }
        }

        if (coreData.getPublication() != null){
            hasPublications = true;
            Iterator<String> iter = coreData.getPublication().iterator();
            ArrayList<String> pubs = new ArrayList<String>();
            if(coreDocument.getValues(BIBL_PUB)!=null){
                pubs.addAll(Arrays.asList(coreDocument.getValues(BIBL_PUB)));
            }
            while(iter.hasNext()){
                String val = iter.next();
                if (!pubs.contains(val)){
                    coreDocument.add(new Field(BIBL_PUB,val,Field.Store.YES,Field.Index.UN_TOKENIZED));
                    pubs.add(val);
                }
            }
            iter = coreData.getIndexedSeries().iterator();
            ArrayList<String> series = new ArrayList<String>();
            if(coreDocument.getValues(INDEXED_SERIES) != null){
                series.addAll(Arrays.asList(coreDocument.getValues(INDEXED_SERIES)));
            }
            while(iter.hasNext()){
                String val = iter.next();
                if (!series.contains(val)){
                    coreDocument.add(new Field(INDEXED_SERIES,val,Field.Store.YES,Field.Index.UN_TOKENIZED,Field.TermVector.YES));
                    series.add(val);
                }
            }
        }
        
        if(coreData.getStructuredPublication() != null){
            hasPublications = true;
            Iterator<String> iter = coreData.getStructuredPublication().iterator();
            ArrayList<String> pubs = new ArrayList<String>();
            if(coreDocument.getValues(PUBLICATION_STRUCTURED)!=null){
                pubs.addAll(Arrays.asList(coreDocument.getValues(PUBLICATION_STRUCTURED)));
            }
            while(iter.hasNext()){
                String val = iter.next();
                if (!pubs.contains(val)){
                    coreDocument.add(new Field(PUBLICATION_STRUCTURED,val,Field.Store.YES,Field.Index.UN_TOKENIZED));
                    pubs.add(val);
                }
            }
        }

        if (coreData.getFreeformPublication() != null && !"".equals(coreData.getFreeformPublication())){
            hasPublications = true;
            coreDocument.add(new Field(PUBLICATION_NOTE,coreData.getFreeformPublication(),Field.Store.YES,Field.Index.NO));
        }

        if (coreData.getPublicationsAbout() != null && coreData.getPublicationsAbout().size() > 0) 
            coreDocument.add(getField(PUB_ABOUT,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            coreData.getPublicationsAbout()),
                            true,
                            Field.Index.NO
            ));

        if (coreData.getPublicationsAboutMoreInfo() != null && coreData.getPublicationsAboutMoreInfo().size() > 0) 
            coreDocument.add(getField(PUB_ABOUT_INFO,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            coreData.getPublicationsAboutMoreInfo()),
                            true,
                            Field.Index.NO
            ));

        if (coreData.getSubjectSearchField() != null && coreData.getSubjectSearchField().size() > 0) 
            coreDocument.add(getField(SUBJECT_I,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            coreData.getSubjectSearchField()),
                            true,
                            Field.Index.TOKENIZED
            ));

        if (coreData.getSubjectDisplayInItemView() != null && coreData.getSubjectDisplayInItemView().size() > 0) 
            coreDocument.add(getField(SUBJECT_D,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            coreData.getSubjectDisplayInItemView()),
                            true,
                            Field.Index.NO
            ));
        if (coreData.getSummary() != null && coreData.getSummary().size() > 0) 
            coreDocument.add(getField(SUMMARY,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            coreData.getSummary()),
                            true,
                            Field.Index.NO
            ));

        if (coreData.getTitle() != null) 
            coreDocument.add(getField(TITLE,
                    coreData.getTitle(),
                    true,
                    Field.Index.NO));
        Collection<String> translation = coreData.getTranslation(CoreMetadataRecord.ModernLanguage.ENGLISH);
        if (translation != null && translation.size() > 0){ 
            hasTranslation = true;
            coreDocument.add(new Field(TRANSLATION_EN,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            translation),
                            Field.Store.YES,
                            Field.Index.NO
            ));
        }
        translation = coreData.getTranslation(CoreMetadataRecord.ModernLanguage.FRENCH);
        if (translation != null && translation.size() > 0){ 
            hasTranslation = true;
            coreDocument.add(new Field(TRANSLATION_FR,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            translation),
                            Field.Store.YES,
                            Field.Index.NO
            ));
        }
        translation = coreData.getTranslation(CoreMetadataRecord.ModernLanguage.GERMAN);
        if (translation != null && translation.size() > 0){ 
            hasTranslation = true;
            coreDocument.add(new Field(TRANSLATION_DE,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            translation),
                            Field.Store.YES,
                            Field.Index.NO
            ));
        }

        if (coreData.getExternalResource() != null){
            for (String ext:coreData.getExternalResource()){
                coreDocument.add(new Field(EXTERNAL_RESOURCE,ext,Field.Store.YES,Field.Index.NO));
            }
        }
        if(coreData.getUTC() != null){
        coreDocument.add(getField(UTC,
                coreData.getUTC(),
                true,
                Field.Index.UN_TOKENIZED));
        }
        if (coreData.hadError()){
            coreDocument.add(new Field(CoreMetadataFields.HAS_ERROR,CoreMetadataFields.SORTABLE_YES_VALUE,Field.Store.NO,Field.Index.UN_TOKENIZED));
            String [] errors = coreData.getErrors();
            for (String error: errors){
                coreDocument.add(new Field(CoreMetadataFields.ERROR,error,Field.Store.YES,Field.Index.NO));
            }
        }
        if(hasImages){
            coreDocument.add(new Field(SORT_HAS_IMG,SORTABLE_YES_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        else{
            coreDocument.add(new Field(SORT_HAS_IMG,SORTABLE_NO_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        if(hasPublications){
            coreDocument.add(new Field(SORT_HAS_PUB,SORTABLE_YES_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        else{
            coreDocument.add(new Field(SORT_HAS_PUB,SORTABLE_NO_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        if(hasTranslation){
            coreDocument.add(new Field(SORT_HAS_TRANS,SORTABLE_YES_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        else{
            coreDocument.add(new Field(SORT_HAS_TRANS,SORTABLE_NO_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }

        Term delTerm = new Term(DOC_ID,coreDocument.get(DOC_ID));
        if (addDocumentFlag) writer.addDocument(coreDocument);
        else writer.updateDocument(delTerm,coreDocument);
//        this.indexed.set(hash);
        numIndexedRecords++;
    }
    
//    private int getDocId(Document doc, Term delTerm, boolean add){
//        int result = -1;
//        try{
//            java.lang.reflect.Field dw =  writer.getClass().getDeclaredField("docWriter");
//            boolean dwAccess = dw.isAccessible();
//            
//            dw.setAccessible(true);
//            Object inst = dw.get(writer);
//            java.lang.reflect.Method getThreadState = inst.getClass().getDeclaredMethod("getThreadState", new Class[]{doc.getClass(),delTerm.getClass()});
//            getThreadState.setAccessible(true);
//            Object threadState = (add)?getThreadState.invoke(inst, new Object[]{doc,null}):getThreadState.invoke(inst, new Object[]{doc,delTerm});
//            java.lang.reflect.Field nextDocId = threadState.getClass().getDeclaredField("docID");
//            boolean nextAccess = nextDocId.isAccessible();
//            nextDocId.setAccessible(true);
//            
//            result =  nextDocId.getInt(threadState);
//            dw.setAccessible(dwAccess);
//            nextDocId.setAccessible(nextAccess);
//            return result;
//        }
//        catch(Throwable t){
//           t.printStackTrace();
//        }
//        return result;
//    }
    
    private void updateCoreMetadataRecord(CoreMetadataRecord coreData, Document coreDocument) throws IOException
    {
        boolean hasImages = false;
        boolean hasTranslation = false;
        boolean hasPublications = false;

        if (coreData.getInstitution() != null){
            String [] vals = getNewValues(coreDocument, APIS_COLLECTION, coreData.getInstitution());
            for (String newVal:vals){
                coreDocument.add(new Field(APIS_COLLECTION,
                        newVal,
                        Field.Store.YES,
                        Field.Index.UN_TOKENIZED)
                );
            }
        }

        if (coreData.getAll() != null && coreData.getAll().size() > 0){
            String newVal = CoreMetadataRecord.getMultipleValuesAsString(coreData.getAll());
            String [] vals = getNewValues(coreDocument,ALL,newVal);
            for (String val:vals){
                coreDocument.add(new Field(ALL, 
                        val, Field.Store.YES, Field.Index.TOKENIZED)
                );
            }
        }

        if (coreData.getAllNoTrans() != null && coreData.getAllNoTrans().size() > 0){
            String mval = CoreMetadataRecord.getMultipleValuesAsString(coreData.getAllNoTrans());
            String [] vals = getNewValues(coreDocument,ALL_NO_TRANS,mval);
            for (String val:vals){
                coreDocument.add(new Field(ALL_NO_TRANS, 
                        val, Field.Store.YES, Field.Index.TOKENIZED)
                );
            }
        }

        if (coreData.getAssociatedNames() != null && coreData.getAssociatedNames().size() > 0){
            String mval = CoreMetadataRecord.getMultipleValuesAsString(coreData.getAssociatedNames());
            String [] vals = getNewValues(coreDocument,NAME_ASSOC,mval);
            for (String val:vals){
                coreDocument.add(new Field(NAME_ASSOC, 
                        val,
                        Field.Store.YES,
                        Field.Index.TOKENIZED));
            }
        }

        if (coreData.getAuthor() != null && coreData.getAuthor().size() > 0){
            String mval = CoreMetadataRecord.getMultipleValuesAsString(coreData.getAuthor());
            String [] vals = getNewValues(coreDocument,NAME_AUTHOR,mval);
            for (String val:vals){
                coreDocument.add(new Field(NAME_AUTHOR, 
                        val,
                        Field.Store.YES,
                        Field.Index.NO));
            }
        }

        if (coreData.getXrefs().size() > 0){
            String [] vals = getNewValues(coreDocument,XREFS,coreData.getXrefs().toArray(new String[0]));
            for (String val:vals){
                coreDocument.add(new Field(XREFS,
                        val,
                        Field.Store.YES,
                        Field.Index.TOKENIZED));
            }
        }

        if (coreData.getDate1() != null) {

            coreDocument.add(getField(DATE1_D,
                    coreData.getDate1(),
                    true,
                    Field.Index.NO));

        }
        else {
            coreDocument.add(getField(DATE1_D,
                    CoreMetadataRecord.UNDEFINED_DATE,
                    true,
                    Field.Index.NO));
        }

        Map<String,String> dateIndices = coreData.getDateIndexes();
        if (dateIndices.size()>0){
            for(String date1:dateIndices.keySet()){
                String date2 = dateIndices.get(date1);
                coreDocument.add(getField(DATE1_I,
                        date1,
                        true,
                        Field.Index.UN_TOKENIZED));
                coreDocument.add(getField(DATE2_I,
                        date2,
                        true,
                        Field.Index.UN_TOKENIZED));
            }
        }

        if (coreData.getDate2() != null) {
            coreDocument.add(getField(DATE2_D,
                    coreData.getDate2(),
                    true,
                    Field.Index.NO));
        }
        else {
            if (coreData.getDate1() != null){
                coreDocument.add(getField(DATE2_D,
                        coreData.getDate1(),
                        true,
                        Field.Index.NO));
            }
            else {
                coreDocument.add(getField(DATE2_D,
                        CoreMetadataRecord.UNDEFINED_DATE,
                        true,
                        Field.Index.NO));
            }
        }

        if (coreData.getGeneralNotes() != null && coreData.getGeneralNotes().length() > 0){
            String mval = coreData.getGeneralNotes();
            String [] vals = getNewValues(coreDocument,GEN_NOTES,mval);
            for (String val:vals){
                coreDocument.add(getField(GEN_NOTES, 
                        val,
                                true,
                                Field.Index.NO));
            }
        }

        if (coreData.getHistoricalData() != null && coreData.getHistoricalData().size() > 0){
            String mval = CoreMetadataRecord.getMultipleValuesAsString(coreData.getHistoricalData());
            String [] vals = getNewValues(coreDocument,HIST_DATA,mval);
            for (String val:vals){
                coreDocument.add(getField(HIST_DATA, 
                        val,true,Field.Index.NO)
                );
            }
        }

        if (coreData.getWebImages() != null){
            hasImages = true;
            Map<URL,String> images = coreData.getWebImages();
            for(URL url:images.keySet()){
                String caption = images.get(url);
                coreDocument.add(new Field(IMG_URL,url.toString(),Field.Store.YES,Field.Index.NO));
                coreDocument.add(new Field(IMG_CAPTION,caption,Field.Store.YES,Field.Index.NO));
            }
        }

        if (coreData.getInventoryNumber() != null){
            String [] vals = getNewValues(coreDocument,INV,coreData.getInventoryNumber());
            for (String val:vals){
                coreDocument.add(new Field(INV,
                        val,
                        Field.Store.YES,
                        Field.Index.TOKENIZED));
            }
        }

        if (coreData.getLanguages() != null){
            Collection<String> langs = coreData.getLanguages();
            String [] vals = getNewValues(coreDocument, LANG, langs.toArray(new String[0]));
            for (String lang: vals){
                coreDocument.add(getField(LANG,
                        lang,
                        true,
                        Field.Index.UN_TOKENIZED));                
            }
        }

        if (coreData.getMaterial() != null && coreData.getMaterial().size() > 0){
            String mval = CoreMetadataRecord.getMultipleValuesAsString(coreData.getMaterial());
            String [] vals = getNewValues(coreDocument,MATERIAL,mval);
            for (String val:vals){
                coreDocument.add(new Field(MATERIAL,val,Field.Store.YES,
                                Field.Index.UN_TOKENIZED));
            }
        }

        if (coreData.getPhysicalDescription() != null) 
            coreDocument.add(getField(PHYS_DESC,
                    coreData.getPhysicalDescription(),
                    true,
                    Field.Index.NO));

        if (coreData.getProvenance() != null && coreData.getProvenance().size() > 0) {
            String mval = CoreMetadataRecord.getMultipleValuesAsString(
                    coreData.getProvenance());
            String [] vals = getNewValues(coreDocument,PROVENANCE_NOTE,mval);
            for (String val:vals){
                coreDocument.add(new Field(PROVENANCE_NOTE,val,Field.Store.YES,Field.Index.TOKENIZED
                ));
            }
        }    

        if (coreData.getProvenanceIndices() != null && coreData.getProvenanceIndices().size() > 0){
            String [] vals = getNewValues(coreDocument,PROVENANCE,coreData.getProvenanceIndices().toArray(new String[0]));
            for (String val:vals){
                coreDocument.add(new Field(PROVENANCE,val,Field.Store.YES,Field.Index.UN_TOKENIZED));
            }
        }

        if (coreData.getPublication() != null){
            hasPublications = true;
            Iterator<String> iter = coreData.getPublication().iterator();
            ArrayList<String> pubs = new ArrayList<String>();
            if(coreDocument.getValues(BIBL_PUB)!=null){
                pubs.addAll(Arrays.asList(coreDocument.getValues(BIBL_PUB)));
            }
            while(iter.hasNext()){
                String val = iter.next();
                if (!pubs.contains(val)){
                    coreDocument.add(new Field(BIBL_PUB,val,Field.Store.YES,Field.Index.UN_TOKENIZED));
                    pubs.add(val);
                }
            }
            iter = coreData.getIndexedSeries().iterator();
            ArrayList<String> series = new ArrayList<String>();
            if(coreDocument.getValues(INDEXED_SERIES) != null){
                series.addAll(Arrays.asList(coreDocument.getValues(INDEXED_SERIES)));
            }
            while(iter.hasNext()){
                String val = iter.next();
                if (!series.contains(val)){
                    coreDocument.add(new Field(INDEXED_SERIES,val,Field.Store.YES,Field.Index.UN_TOKENIZED,Field.TermVector.YES));
                    series.add(val);
                }
            }
        }

        if (coreData.getFreeformPublication() != null && !"".equals(coreData.getFreeformPublication())){
            hasPublications = true;
            String [] vals = getNewValues(coreDocument,PUBLICATION_NOTE,new String[]{coreData.getFreeformPublication()});
            for (String val:vals){
                coreDocument.add(new Field(PUBLICATION_NOTE,val,Field.Store.YES,Field.Index.NO));
            }
        }

        if (coreData.getPublicationsAbout() != null && coreData.getPublicationsAbout().size() > 0) {
            String mval = CoreMetadataRecord.getMultipleValuesAsString(coreData.getPublicationsAbout());
            String [] vals = getNewValues(coreDocument,PUB_ABOUT,new String[]{mval});
            for (String val:vals){
                coreDocument.add(getField(PUB_ABOUT,val,true,Field.Index.NO
                ));
            }
        }
        if (coreData.getPublicationsAboutMoreInfo() != null && coreData.getPublicationsAboutMoreInfo().size() > 0){ 
            String mval = CoreMetadataRecord.getMultipleValuesAsString(coreData.getPublicationsAboutMoreInfo());
            String [] vals = getNewValues(coreDocument,PUB_ABOUT_INFO,new String[]{mval});
            for (String val:vals){
                coreDocument.add(getField(PUB_ABOUT_INFO,val,true,Field.Index.NO
                ));
            }
        }
        // get reference for searching
        if ( (coreData.getReference() != null) || (coreData.getXrefs().size() > 0) ) {

            Vector<String> referenceVector = new Vector<String>();
            if (coreData.getReference() != null) {
                referenceVector.addAll(coreData.getReference());
            }
            if (coreData.getXrefs().size() > 0) {
                referenceVector.addAll(coreData.getXrefs());
            }
        }

        if (coreData.getSubjectSearchField() != null && coreData.getSubjectSearchField().size() > 0){ 
            String mval = CoreMetadataRecord.getMultipleValuesAsString(coreData.getSubjectSearchField());
            String [] vals = getNewValues(coreDocument,SUBJECT_I,mval);
            for (String val:vals){
                coreDocument.add(getField(SUBJECT_I,val,
                        true,Field.Index.TOKENIZED
                ));
            }
        }
        if (coreData.getSubjectDisplayInItemView() != null && coreData.getSubjectDisplayInItemView().size() > 0){ 
            String mval = CoreMetadataRecord.getMultipleValuesAsString(
                    coreData.getSubjectDisplayInItemView());
            String [] vals = getNewValues(coreDocument,SUBJECT_D,mval);
            for (String val:vals){
            coreDocument.add(getField(SUBJECT_D,val,
                            true,
                            Field.Index.NO
            ));
            }
        }
        if (coreData.getSummary() != null && coreData.getSummary().size() > 0){ 
            String mval = CoreMetadataRecord.getMultipleValuesAsString(coreData.getSummary());
            String [] vals = getNewValues(coreDocument,SUMMARY,mval);
            for (String val:vals){
            coreDocument.add(getField(SUMMARY,val,
                            true,
                            Field.Index.NO
            ));
            }
        }
        
        if (coreData.getTitle() != null) {
            String [] vals = getNewValues(coreDocument,TITLE,coreData.getTitle());
            for (String val:vals){
                coreDocument.add(getField(TITLE,
                        val,
                        true,
                        Field.Index.NO));
            }
        }
        
        Collection<String> translation = coreData.getTranslation(CoreMetadataRecord.ModernLanguage.ENGLISH);
        if (translation != null && translation.size() > 0){ 
            hasTranslation = true;
            coreDocument.add(new Field(TRANSLATION_EN,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            translation),
                            Field.Store.YES,
                            Field.Index.NO
            ));
        }
        translation = coreData.getTranslation(CoreMetadataRecord.ModernLanguage.FRENCH);
        if (translation != null && translation.size() > 0){ 
            hasTranslation = true;
            coreDocument.add(new Field(TRANSLATION_FR,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            translation),
                            Field.Store.YES,
                            Field.Index.NO
            ));
        }translation = coreData.getTranslation(CoreMetadataRecord.ModernLanguage.GERMAN);
        if (translation != null && translation.size() > 0){ 
            hasTranslation = true;
            coreDocument.add(new Field(TRANSLATION_DE,
                    CoreMetadataRecord.getMultipleValuesAsString(
                            translation),
                            Field.Store.YES,
                            Field.Index.NO
            ));
        }

        if (coreData.getExternalResource() != null){
            String [] vals = getNewValues(coreDocument,EXTERNAL_RESOURCE,coreData.getExternalResource().toArray(new String[0]));
            for (String ext:vals){
                coreDocument.add(new Field(EXTERNAL_RESOURCE,ext,Field.Store.YES,Field.Index.NO));
            }
        }


       if(coreData.getUTC() != null){
        coreDocument.add(getField(UTC,
                coreData.getUTC(),
                true,
                Field.Index.UN_TOKENIZED));
       }

        if (coreData.hadError()){
            coreDocument.add(new Field(APISIndices.HAS_ERROR,APISIndices.SORTABLE_YES_VALUE,Field.Store.NO,Field.Index.UN_TOKENIZED));
            String [] errors = coreData.getErrors();
            for (String error: errors){
                coreDocument.add(new Field(APISIndices.ERROR,error,Field.Store.YES,Field.Index.NO));
            }
        }
        if(hasImages){
            coreDocument.add(new Field(SORT_HAS_IMG,SORTABLE_YES_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        else{
            coreDocument.add(new Field(SORT_HAS_IMG,SORTABLE_NO_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        if(hasPublications){
            coreDocument.add(new Field(SORT_HAS_PUB,SORTABLE_YES_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        else{
            coreDocument.add(new Field(SORT_HAS_PUB,SORTABLE_NO_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        if(hasTranslation){
            coreDocument.add(new Field(SORT_HAS_TRANS,SORTABLE_YES_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        else{
            coreDocument.add(new Field(SORT_HAS_TRANS,SORTABLE_NO_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        writer.updateDocument(new Term(DOC_ID,coreDocument.get(DOC_ID)),coreDocument);
        numIndexedRecords++;

    }

    public static final EntityResolver getEpiDocResolver(final EntityResolver delegate) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte [] buf = new byte[4096];
        InputStream in = Indexer.class.getResourceAsStream("/xml/tei-epidoc.dtd");
        int len = -1;
        while((len = in.read(buf)) != -1){
            bos.write(buf,0,len);
        }
        final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        final InputSource epidoc = new InputSource(bis);
        EntityResolver result = new EntityResolver(){
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException  {
                if(systemId.endsWith("tei-epidoc.dtd")){
                    bis.reset();
                    return epidoc;
                }
                else return delegate.resolveEntity(publicId, systemId);
            }
        };
        return result;
    }

    private static String [] getNewValues(Document doc, String field, String [] candidates) {
        String [] oldVals = doc.getValues(field);
        if (candidates == null) candidates = new String[0];
        if (oldVals == null) return candidates;
        List<String> candidateList = new ArrayList<String>();
        candidateList.addAll(Arrays.asList(candidates));
        for (String val:oldVals){
            candidateList.remove(val);
        }
        return candidateList.toArray(new String[0]);
    }
    private static String [] getNewValues(Document doc, String field, String candidate) {
        return getNewValues(doc,field,new String[]{candidate});
    }

    private static void scrubPublications(CoreMetadataRecord apis){
        String publicationString = apis.getFreeformPublication();
        if ( publicationString == null){
            return;    
        }

        PublicationScrubber scrubber = PublicationScrubber.get(publicationString, apis);


        Collection<String> publication = apis.getPublication();

        for (String pub: scrubber.getPublications()){
            if (!publication.contains(pub))apis.addPublication(pub);
        }

    }

}
