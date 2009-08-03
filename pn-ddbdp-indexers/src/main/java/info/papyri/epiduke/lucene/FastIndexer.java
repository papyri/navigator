package info.papyri.epiduke.lucene;

import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer;
import info.papyri.epiduke.lucene.analysis.CopyingTokenFilter;
import info.papyri.epiduke.lucene.analysis.LinePositionTokenStream;
import info.papyri.epiduke.sax.TEILineHandler;
import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.NamespacePrefixes;
import info.papyri.util.NumberConverter;

import java.sql.SQLException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class FastIndexer {

    private static final Term XREF_TEMPLATE = new Term(CoreMetadataFields.XREFS, "");
    public static final String LINE_SPAN_TERM = "lines".intern();
    public static final String LINE_SPAN_TERM_LC = "linesLC".intern();
    public static final String LINE_SPAN_TERM_DF = "linesDF".intern();
    public static final String LINE_SPAN_TERM_FL = "linesFL".intern();
    public static final String COLLECTION = "collection".intern();
    public static final String VOLUME = "volume".intern();
    public static final String FILENAME = "fileName".intern();
    public static final String DDBDP_ID = "ddbdpId".intern();
    public static final String TEXT = "text".intern();
    public static final AncientGreekAnalyzer LINE_SPAN_ANALYZER = new AncientGreekAnalyzer();
    public static final AncientGreekAnalyzer WORD_SPAN_ANALYZER = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.NONE, false);
    public static final AncientGreekAnalyzer LINE_SPAN_ANALYZER_LC = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE);
    public static final AncientGreekAnalyzer WORD_SPAN_ANALYZER_LC = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE, false);
    public static final AncientGreekAnalyzer LINE_SPAN_ANALYZER_DF = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.DIACRITICS);
    public static final AncientGreekAnalyzer WORD_SPAN_ANALYZER_DF = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.DIACRITICS, false);
    public static final AncientGreekAnalyzer LINE_SPAN_ANALYZER_FL = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_DIACRITICS);
    public static final AncientGreekAnalyzer WORD_SPAN_ANALYZER_FL = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_DIACRITICS, false);

    //private final Directory dir;
    private final PerFieldAnalyzerWrapper pfa = new PerFieldAnalyzerWrapper(new AncientGreekAnalyzer());
    private final IndexWriter plain;
    private final IndexWriter lc;
    private final IndexWriter df;
    private final IndexWriter fl;
    private final IndexWriter docs;
    private final IndexWriter metadata;
    private final IndexSearcher coreMetadata;
    private final TEILineHandler main;
    private XMLReader digest;
    private int files = 0;
    private boolean indexLines = false;
    public static final int MAX_FIELD_LENGTH = 32 * 1024;

    private static final IndexWriter getWriter(File parent, String dirName, PerFieldAnalyzerWrapper pfa) throws IOException {
        IndexWriter result = new IndexWriter(FSDirectory.getDirectory(new File(parent, dirName)), pfa);
        result.setMaxFieldLength(MAX_FIELD_LENGTH);
        result.setTermIndexInterval(IndexWriter.DEFAULT_TERM_INDEX_INTERVAL / 4);
        return result;
    }

    private static final IndexSearcher getMetadataSearcher(String path) throws IOException {
        IndexReader rdr = IndexReader.open(path);
        return new IndexSearcher(rdr);
    }

    private static final IndexWriter getMetadataWriter(File parent, String dirName) throws IOException {
        IndexWriter result = new IndexWriter(FSDirectory.getDirectory(new File(parent, dirName)), new StandardAnalyzer());
        result.setMaxFieldLength(MAX_FIELD_LENGTH);
        result.setTermIndexInterval(IndexWriter.DEFAULT_TERM_INDEX_INTERVAL / 4);
        return result;
    }

    public FastIndexer(File out, String meta) throws IOException {
        main = new TEILineHandler(true);
        main.addLineBreakTag("lb");
        main.addTextPattern("TEI.2/text/body/div");
        digest = createXMLReader();
        digest.setContentHandler(main);
        digest.setEntityResolver(info.papyri.data.Indexer.getEpiDocResolver(digest.getEntityResolver()));
        pfa.addAnalyzer(Indexer.WORD_SPAN_TERM, WORD_SPAN_ANALYZER);
//        pfa.addAnalyzer(Indexer.LEMMA_TERM, WORD_SPAN_ANALYZER);
        pfa.addAnalyzer(Indexer.WORD_SPAN_TERM_DF, WORD_SPAN_ANALYZER_DF);
        pfa.addAnalyzer(Indexer.WORD_SPAN_TERM_FL, WORD_SPAN_ANALYZER_FL);
        pfa.addAnalyzer(Indexer.WORD_SPAN_TERM_LC, WORD_SPAN_ANALYZER_LC);
        this.plain = getWriter(out, "plain", pfa);
        this.lc = getWriter(out, "lc", pfa);
        this.df = getWriter(out, "df", pfa);
        this.fl = getWriter(out, "fl", pfa);
        this.docs = getWriter(out, "docs", pfa);
        if (meta != null) {
            this.coreMetadata = getMetadataSearcher(meta);
            this.metadata = getMetadataWriter(out, "metadata");
        // TODO get the metadata index;
        } else {
            this.coreMetadata = null;
            this.metadata = null;
        }
    }

    public void index(java.io.File file, boolean recur) throws IOException, SAXException, SQLException {
        index(file, recur, getTemplateDoc());
    }

    public void index(java.io.File file, boolean recur, Document template) throws IOException, SAXException, SQLException {
        if (file.isDirectory() && recur) {
            for (File child : file.listFiles(new FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory() || f.getName().endsWith(".xml")) {
                        return true;
                    }
                    return false;
                }
            })) {
                index(child, recur, template);
            }
        }
        String fname = file.getName();
        if (!fname.endsWith(".xml")) {  //nothing more to do if you're a directory
            return;
        }
        Document doc = new Document();
        Document plain = new Document();
        Document lc = new Document();
        Document df = new Document();
        Document fl = new Document();
        plain.add(new Field(Indexer.WORD_SPAN_TERM, WORD_SPAN_ANALYZER.tokenStream(null, new StringReader(""))));
//        plain.add(new Field(Indexer.LEMMA_TERM,WORD_SPAN_ANALYZER.tokenStream(null, new StringReader(""))));
        lc.add(new Field(Indexer.WORD_SPAN_TERM_LC, WORD_SPAN_ANALYZER_LC.tokenStream(null, new StringReader(""))));
        df.add(new Field(Indexer.WORD_SPAN_TERM_DF, WORD_SPAN_ANALYZER_DF.tokenStream(null, new StringReader(""))));
        fl.add(new Field(Indexer.WORD_SPAN_TERM_FL, WORD_SPAN_ANALYZER_FL.tokenStream(null, new StringReader(""))));
        doc.add(new Field(TEXT, "", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field(FILENAME, "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(DDBDP_ID, "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(COLLECTION, "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(VOLUME, "", Field.Store.YES, Field.Index.UN_TOKENIZED));

        System.out.println(file.getAbsolutePath());
        files++;
        System.out.print('.');
        if (files % 250 == 0) {
            System.out.println(files);
        }
        InputSource input = new InputSource(new FileInputStream(file));
        get(input, fname, template);
        try {
            plain.getField(Indexer.WORD_SPAN_TERM).setValue(template.getField(Indexer.WORD_SPAN_TERM).tokenStreamValue());
//                plain.getField(Indexer.LEMMA_TERM).setValue(template.getField(Indexer.LEMMA_TERM).tokenStreamValue());
            lc.getField(Indexer.WORD_SPAN_TERM_LC).setValue(template.getField(Indexer.WORD_SPAN_TERM_LC).tokenStreamValue());
            df.getField(Indexer.WORD_SPAN_TERM_DF).setValue(template.getField(Indexer.WORD_SPAN_TERM_DF).tokenStreamValue());
            fl.getField(Indexer.WORD_SPAN_TERM_FL).setValue(template.getField(Indexer.WORD_SPAN_TERM_FL).tokenStreamValue());

            doc.getField(TEXT).setValue(template.getField(TEXT).stringValue());
            doc.getField(FILENAME).setValue(template.getField(FILENAME).stringValue());
            doc.getField(DDBDP_ID).setValue(template.getField(DDBDP_ID).stringValue());
            doc.getField(COLLECTION).setValue(template.getField(COLLECTION).stringValue());
            doc.getField(VOLUME).setValue(template.getField(VOLUME).stringValue());

            this.plain.addDocument(plain);
            this.lc.addDocument(lc);
            this.df.addDocument(df);
            this.fl.addDocument(fl);
            this.docs.addDocument(doc);
            if (this.coreMetadata != null) {
                Document metadata = new Document();
                String coreId = template.getField(DDBDP_ID).stringValue();
                if (!coreId.startsWith(NamespacePrefixes.DDBDP)) {
                    throw new RuntimeException("Malformed id: " + coreId);
                }
                metadata.add(new Field(CoreMetadataFields.DOC_ID, coreId, Field.Store.YES, Field.Index.UN_TOKENIZED));
                Term idTerm = XREF_TEMPLATE.createTerm(coreId);
                Hits hits = this.coreMetadata.search(new TermQuery(idTerm));
                if (hits.length() == 0) {
                    // TODO deal with publications and indexed series when there is no associated metadata
                    String coll = doc.getField(COLLECTION).stringValue();
                    String vol = doc.getField(VOLUME).stringValue();
                    String roman = NumberConverter.getRoman(vol);
                    String docNum = fname.substring(0, fname.indexOf(".xml"));
                    int vi = docNum.indexOf(vol, coll.length());
                    int docStart = vi + vol.length() + 1;
                    docNum = (docStart < docNum.length()) ? docNum.substring(docStart) : "";
                    String pub = coll + " " + roman + " " + docNum;
                    metadata.add(new Field(CoreMetadataFields.BIBL_PUB, pub, Field.Store.YES, Field.Index.UN_TOKENIZED));
                    metadata.add(new Field(CoreMetadataFields.SORT_HAS_IMG, CoreMetadataFields.SORTABLE_NO_VALUE, Field.Store.YES, Field.Index.UN_TOKENIZED));
                    metadata.add(new Field(CoreMetadataFields.SORT_HAS_TRANS, CoreMetadataFields.SORTABLE_NO_VALUE, Field.Store.YES, Field.Index.UN_TOKENIZED));
                }
                metadata.add(new Field(FILENAME, template.getField(FILENAME).stringValue(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                metadata.add(new Field(COLLECTION, template.getField(COLLECTION).stringValue(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                metadata.add(new Field(VOLUME, template.getField(VOLUME).stringValue(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                Iterator<Hit> iterator = hits.iterator();
                while (iterator.hasNext()) {
                    Hit hit = iterator.next();
                    Document mDoc = hit.getDocument();
                    if (mDoc == null) {
                        continue;
                    }
                    String[] pubs = mDoc.getValues(CoreMetadataFields.BIBL_PUB);
                    if (pubs != null) {
                        for (String pub : pubs) {
                            metadata.add(new Field(CoreMetadataFields.BIBL_PUB, pub, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        }
                    }
                    String[] serials = mDoc.getValues(CoreMetadataFields.INDEXED_SERIES);
                    if (serials != null) {
                        for (String serial : serials) {
                            metadata.add(new Field(CoreMetadataFields.INDEXED_SERIES, serial, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        }
                    }
                    String[] date1s = mDoc.getValues(CoreMetadataFields.DATE1_I);
                    String[] date2s = mDoc.getValues(CoreMetadataFields.DATE2_I);
                    if (date1s != null) {
                        if (date2s == null) {
                            date2s = new String[0];
                        }
                        for (int i = 0; i < date1s.length; i++) {
                            metadata.add(new Field(CoreMetadataFields.DATE1_I, date1s[i], Field.Store.YES, Field.Index.UN_TOKENIZED));
                            if (i < date2s.length) {
                                metadata.add(new Field(CoreMetadataFields.DATE2_I, date2s[i], Field.Store.YES, Field.Index.UN_TOKENIZED));
                            } else {
                                metadata.add(new Field(CoreMetadataFields.DATE2_I, date1s[i], Field.Store.YES, Field.Index.UN_TOKENIZED));
                            }
                        }
                    }
                    String[] dates = mDoc.getValues(CoreMetadataFields.DATE1_D);
                    if (dates != null) {
                        for (String date : dates) {
                            metadata.add(new Field(CoreMetadataFields.DATE1_D, date, Field.Store.YES, Field.Index.NO));
                        }
                    }
                    String[] xrefs = mDoc.getValues(CoreMetadataFields.XREFS);
                    if (xrefs != null) {
                        for (String xref : xrefs) {
                            metadata.add(new Field(CoreMetadataFields.XREFS, xref, Field.Store.YES, Field.Index.UN_TOKENIZED));
                            if (xref.startsWith(NamespacePrefixes.APIS)) {
                                String apis = xref.substring(NamespacePrefixes.APIS.length());
                                int ix = apis.indexOf(':');
                                if (ix != -1) {
                                    apis = apis.substring(0, ix);
                                    metadata.add(new Field(CoreMetadataFields.APIS_COLLECTION, apis, Field.Store.YES, Field.Index.UN_TOKENIZED));
                                }
                            }
                        }
                    }
                    String[] places = mDoc.getValues(CoreMetadataFields.PROVENANCE_NOTE);
                    if (places != null) {
                        for (String place : places) {
                            metadata.add(new Field(CoreMetadataFields.PROVENANCE_NOTE, place, Field.Store.YES, Field.Index.TOKENIZED));
                        }
                    }
                    String title = mDoc.get(CoreMetadataFields.TITLE);
                    if (title != null) {
                        metadata.add(new Field(CoreMetadataFields.TITLE, title, Field.Store.YES, Field.Index.NO));
                    }
                    String hasImg = mDoc.get(CoreMetadataFields.SORT_HAS_IMG);
                    if (hasImg != null) {
                        metadata.add(new Field(CoreMetadataFields.SORT_HAS_IMG, hasImg, Field.Store.YES, Field.Index.UN_TOKENIZED));
                    } else {
                        metadata.add(new Field(CoreMetadataFields.SORT_HAS_IMG, CoreMetadataFields.SORTABLE_NO_VALUE, Field.Store.YES, Field.Index.UN_TOKENIZED));
                    }
                    String hasTrans = mDoc.get(CoreMetadataFields.SORT_HAS_TRANS);
                    if (hasTrans != null) {
                        metadata.add(new Field(CoreMetadataFields.SORT_HAS_TRANS, hasImg, Field.Store.YES, Field.Index.UN_TOKENIZED));
                    } else {
                        metadata.add(new Field(CoreMetadataFields.SORT_HAS_TRANS, CoreMetadataFields.SORTABLE_NO_VALUE, Field.Store.YES, Field.Index.UN_TOKENIZED));
                    }
                }
                this.metadata.addDocument(metadata);
            }
        } catch (IOException ioe) {
            System.err.println("Error parsing " + fname);
            ioe.printStackTrace(System.err);
            throw ioe;
        }


    }

    private Document getTemplateDoc() throws IOException {
        Document doc = new Document();
        doc.add(new Field(TEXT, "", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field(FILENAME, "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(DDBDP_ID, "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(COLLECTION, "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(VOLUME, "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        Iterator<String> nullIter = new ArrayList<String>().iterator();



        doc.add(new Field(Indexer.LINE_SPAN_TERM, new LinePositionTokenStream(nullIter, LINE_SPAN_ANALYZER)));
        doc.add(new Field(Indexer.LINE_SPAN_TERM_LC, new LinePositionTokenStream(nullIter, LINE_SPAN_ANALYZER_LC)));
        doc.add(new Field(Indexer.LINE_SPAN_TERM_DF, new LinePositionTokenStream(nullIter, LINE_SPAN_ANALYZER_DF)));
        doc.add(new Field(Indexer.LINE_SPAN_TERM_FL, new LinePositionTokenStream(nullIter, LINE_SPAN_ANALYZER_FL)));

        doc.add(new Field(Indexer.WORD_SPAN_TERM, WORD_SPAN_ANALYZER.tokenStream(null, new StringReader(""))));
//        doc.add(new Field(Indexer.LEMMA_TERM,WORD_SPAN_ANALYZER.tokenStream(null, new StringReader(""))));
        doc.add(new Field(Indexer.WORD_SPAN_TERM_LC, WORD_SPAN_ANALYZER_LC.tokenStream(null, new StringReader(""))));
        doc.add(new Field(Indexer.WORD_SPAN_TERM_DF, WORD_SPAN_ANALYZER_DF.tokenStream(null, new StringReader(""))));
        doc.add(new Field(Indexer.WORD_SPAN_TERM_FL, WORD_SPAN_ANALYZER_FL.tokenStream(null, new StringReader(""))));
        return doc;
    }

    public static final CachingTokenFilter cloneCache(CachingTokenFilter src) throws IOException {
        CopyingTokenFilter clone = new CopyingTokenFilter(src);
        clone.next();
        clone.reset();
        src.reset();
        return clone;
    }

    private void get(InputSource input, String fname, Document doc) throws IOException, SAXException, SQLException {
        digest.parse(input);

        doc.getField(TEXT).setValue(main.getText());
        doc.getField(FILENAME).setValue(fname);
        doc.getField(DDBDP_ID).setValue(main.getDdbdpId());
        if (indexLines) {
            doc.getField(Indexer.LINE_SPAN_TERM).setValue(new LinePositionTokenStream(main.getLines(), LINE_SPAN_ANALYZER));
            doc.getField(Indexer.LINE_SPAN_TERM_LC).setValue(new LinePositionTokenStream(main.getLines(), LINE_SPAN_ANALYZER_LC));
            doc.getField(Indexer.LINE_SPAN_TERM_DF).setValue(new LinePositionTokenStream(main.getLines(), LINE_SPAN_ANALYZER_DF));
            doc.getField(Indexer.LINE_SPAN_TERM_FL).setValue(new LinePositionTokenStream(main.getLines(), LINE_SPAN_ANALYZER_FL));
        }

        CachingTokenFilter cache = new CachingTokenFilter(WORD_SPAN_ANALYZER.tokenStream(null, new StringReader(main.getText())));

        doc.getField(Indexer.WORD_SPAN_TERM).setValue(cloneCache(cache));
//        doc.getField(Indexer.LEMMA_TERM).setValue(new LemmaFilter(cloneCache(cache),this.db));
        doc.getField(Indexer.WORD_SPAN_TERM_LC).setValue(WORD_SPAN_ANALYZER_LC.filter(cloneCache(cache)));

        doc.getField(Indexer.WORD_SPAN_TERM_DF).setValue(WORD_SPAN_ANALYZER_DF.filter(cloneCache(cache)));

        doc.getField(Indexer.WORD_SPAN_TERM_FL).setValue(WORD_SPAN_ANALYZER_FL.filter(cloneCache(cache)));
        cache.close();


        if (main.getCollection() != null) {
            doc.getField(COLLECTION).setValue(main.getCollection());
        } else {
            doc.getField(COLLECTION).setValue("");
        }
        if (main.getVolume() != null) {
            doc.getField(VOLUME).setValue(main.getVolume());
        } else {
            doc.getField(VOLUME).setValue("");
        }
    }

    public void optimize() throws IOException {
        System.out.println("Optimizing index of " + files + "files...");
        plain.optimize();
        lc.optimize();
        df.optimize();
        fl.optimize();
        docs.optimize();
        if (metadata != null) {
            metadata.optimize();
        }
    }

    public void close() throws IOException {
        System.out.println("Closing index of " + files + "files...");
        plain.close();
        lc.close();
        df.close();
        fl.close();
        docs.close();
        if (metadata != null) {
            metadata.close();
        }
    }

    private static XMLReader createXMLReader() {
        XMLReader xr = null;
        try {
            xr = XMLReaderFactory.createXMLReader();
        } catch (SAXException se) {
        }
        xr.setEntityResolver(new DefaultHandler() {

            public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
                if (systemId.endsWith("tei-epidoc.dtd")) {
                    return new InputSource(Indexer.class.getResourceAsStream("tei-epidoc.dtd"));
                }
                return super.resolveEntity(publicId, systemId);
            }
        });
        return xr;
    }

    private static void index(String in, String out, boolean recur, boolean lines, String meta) throws IOException, SQLException, SAXException {
        FastIndexer main = new FastIndexer(new File(out), meta);
        main.indexLines = lines;
        System.out.println("Start index: " + new java.util.Date(System.currentTimeMillis()));
        main.index(new File(in), recur);
        main.optimize();
        main.close();
        main.coreMetadata.close();
        System.out.println("End index: " + new java.util.Date(System.currentTimeMillis()));
    }

    public static void main(String[] args) throws IOException, SAXException, SQLException {
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))){
        printHelp();
        System.exit(0);
        }
        HashMap opts = getOpts(args,
        new String[]{"-i","-o","-r","-t","-l","-m"},
        new String[]{"--in","--out","--recur","--rotate","--lines","--meta"});
        String in = opts.get("-i").toString();
        String out = opts.get("-o").toString();
        String meta = opts.get("-m").toString();
        Object recur = opts.get("-r");
        Object lines = opts.get("-l");

        // this is just to load the driver class
        /*
         * Test code.  TODO: Refactor into real test
        String in = "/usr/local/pn/xml/idp.data/trunk/DDB_EpiDoc_XML/p.muench/p.muench.3.1";
        String out = "/usr/local/pn/indices/ddbdp_test";
        Object recur = new Object();
        String lemmas = "/usr/local/pn/db/lemmas_test";
        String meta = "/usr/local/pn/indices/merge";
        Object lines = null;
         */
        index(in, out, recur != null, lines != null, meta);
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("-i/--in\t<STRING>\t: top-level source directory for TEI xml");
        System.out.println("-o/--out\t<STRING>\t: directory for Lucene index");
        System.out.println("-r/--recur\t<BOOL>\t: index all xml files in subdirectories of source directory");
        System.out.println("-l/--lines\t<BOOL>\t:  - index tokens with line-based position increments");
        System.out.println("-m/--meta\t<STRING>\t:  - directory with Lucene core metadata index");
    }

    private static java.util.HashMap getOpts(String[] args, String[] sFlags, String[] lFlags) {
        HashMap result = new HashMap();
        if (args.length == 0) {
            return result;
        }
        args:
        for (int i = 0; i < args.length; i++) {
            flags:
            for (int j = 0; j < sFlags.length; j++) {
                if (args[i].equals(sFlags[j]) || args[i].equals(lFlags[j])) {
                    if (i + 1 >= args.length) {
                        Object in = result.get(sFlags[j]);
                        if (in == null) {
                            result.put(sFlags[j], Integer.valueOf(1));
                            break flags;
                        }
                        if (in instanceof Integer) {
                            result.put(sFlags[j], Integer.valueOf(((Integer) in).intValue() + 1));
                            break flags;
                        }
                        System.err.println("Missing value for flag " + sFlags[j]);
                        System.exit(1);
                    } else {
                        if (args[i + 1].charAt(0) == '-') {
                            Object in = result.get(sFlags[j]);
                            if (in == null) {
                                result.put(sFlags[j], Integer.valueOf(1));
                                break flags;
                            }
                            if (in instanceof Integer) {
                                result.put(sFlags[j], Integer.valueOf(((Integer) in).intValue() + 1));
                                break flags;
                            }
                            System.err.println("Missing value for flag " + sFlags[j]);
                            System.exit(1);
                        } else {
                            result.put(sFlags[j], args[i + 1]);
                            i++;
                        }
                    }
                }
            }
        }
        return result;
    }
}
