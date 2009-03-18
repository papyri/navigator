package info.papyri.epiduke.lucene;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer;
import info.papyri.epiduke.lucene.analysis.LinePositionTokenStream;
import info.papyri.epiduke.lucene.analysis.VectorTokenFilter;
import info.papyri.epiduke.sax.TEILineHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
public class Indexer {
    public static final String LINE_SPAN_TERM = "lines".intern();
    public static final String LEMMA_TERM = "lemmas".intern();
    public static final String WORD_SPAN_TERM = "words".intern();
    public static final String LINE_SPAN_TERM_LC = "linesLC";
    public static final String WORD_SPAN_TERM_LC = "wordsLC".intern();
    public static final String LINE_SPAN_TERM_DF = "linesDF".intern();
    public static final String WORD_SPAN_TERM_DF = "wordsDF".intern();
    public static final String LINE_SPAN_TERM_FL = "linesFL".intern();
    public static final String WORD_SPAN_TERM_FL = "wordsFL".intern();
    public static final String COLLECTION = "collection".intern();
    public static final String DDBDP_ID = "ddbdpId".intern();
    public static final String VOLUME = "volume".intern();

    public static final AncientGreekAnalyzer LINE_SPAN_ANALYZER = new AncientGreekAnalyzer();
    public static final AncientGreekAnalyzer WORD_SPAN_ANALYZER = new AncientGreekAnalyzer();
    public static final AncientGreekAnalyzer LINE_SPAN_ANALYZER_LC = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE);
    public static final AncientGreekAnalyzer WORD_SPAN_ANALYZER_LC = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE);
    public static final AncientGreekAnalyzer LINE_SPAN_ANALYZER_DF = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.DIACRITICS);
    public static final AncientGreekAnalyzer WORD_SPAN_ANALYZER_DF = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.DIACRITICS);
    public static final AncientGreekAnalyzer LINE_SPAN_ANALYZER_FL = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_DIACRITICS);
    public static final AncientGreekAnalyzer WORD_SPAN_ANALYZER_FL = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_DIACRITICS);

    private final Directory dir;
    private final PerFieldAnalyzerWrapper pfa = new PerFieldAnalyzerWrapper(new AncientGreekAnalyzer());
    private final IndexWriter writer;
    private final TEILineHandler main;
    private XMLReader digest;
    private int files = 0;
    public Indexer(org.apache.lucene.store.Directory dir) throws IOException {
        this.dir = dir;
        main = new TEILineHandler(true);
        main.addLineBreakTag("lb");
        main.addTextPattern("TEI.2/text/body/div");
        digest = createXMLReader();
        digest.setContentHandler(main);
        pfa.addAnalyzer(WORD_SPAN_TERM, WORD_SPAN_ANALYZER);
        pfa.addAnalyzer(WORD_SPAN_TERM_DF, WORD_SPAN_ANALYZER_DF);
        pfa.addAnalyzer(WORD_SPAN_TERM_FL, WORD_SPAN_ANALYZER_FL);
        pfa.addAnalyzer(WORD_SPAN_TERM_LC, WORD_SPAN_ANALYZER_LC);
        this.writer = new IndexWriter(this.dir,pfa);
        this.writer.setMaxFieldLength(32*1024);
        this.writer.setTermIndexInterval(IndexWriter.DEFAULT_TERM_INDEX_INTERVAL/4);
    }
    public void index(java.io.File file, boolean recur, boolean rotate) throws IOException, SAXException {
        String fname = file.getName();
        if(rotate) System.out.println("Rotation no longer supported");
        if (file.isFile() && fname.endsWith(".xml")){
            files++;
            InputSource input = new InputSource(new FileInputStream(file));
            Document doc = get(input,fname);
            try{
                writer.addDocument(doc);
            }
            catch (IOException ioe){
                System.err.println("Error parsing " + fname);
                ioe.printStackTrace(System.err);
                throw ioe;
            }

        }
        if (file.isDirectory() && recur){
            for (String child:file.list()){
                index(new File(file,child),recur,rotate);
            }
        }
    }
    
    private Document get(InputSource input, String fname) throws IOException, SAXException {
        Document doc = new Document();
        digest.parse(input);
        doc.add(new Field(FastIndexer.TEXT,main.getText(),Field.Store.YES,Field.Index.TOKENIZED,Field.TermVector.WITH_POSITIONS_OFFSETS));
        doc.add(new Field(FastIndexer.FILENAME,fname,Field.Store.YES,Field.Index.UN_TOKENIZED));

        doc.add(new Field(Indexer.LINE_SPAN_TERM,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER)));
        doc.add(new Field(Indexer.LINE_SPAN_TERM_LC,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER_LC)));
        doc.add(new Field(Indexer.LINE_SPAN_TERM_DF,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER_DF)));
        doc.add(new Field(Indexer.LINE_SPAN_TERM_FL,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER_FL)));
        VectorTokenFilter cache = new VectorTokenFilter(WORD_SPAN_ANALYZER.tokenStream(null, new StringReader(main.getText()))); 
        cache.buildCache();
        doc.add(new Field(Indexer.WORD_SPAN_TERM,cache.clone()));
        doc.add(new Field(Indexer.WORD_SPAN_TERM_LC,WORD_SPAN_ANALYZER_LC.filter(cache.clone())));
        doc.add(new Field(Indexer.WORD_SPAN_TERM_DF,WORD_SPAN_ANALYZER_DF.filter(cache.clone())));
        doc.add(new Field(Indexer.WORD_SPAN_TERM_FL,WORD_SPAN_ANALYZER_FL.filter(cache.clone())));
        doc.add(new Field(DDBDP_ID,main.getDdbdpId(),Field.Store.YES,Field.Index.UN_TOKENIZED));
        if(main.getCollection() != null){
            doc.add(new Field(COLLECTION,main.getCollection(),Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        if(main.getVolume() != null){
            String vol = main.getCollection();
            if (vol != null){
                vol += ":" + main.getVolume();
                doc.add(new Field(VOLUME,vol,Field.Store.YES,Field.Index.UN_TOKENIZED));
            }
        }
        return doc;
    }
    public void optimize() throws IOException {
        System.out.println("Optimizing index of " + files + "files...");
        writer.optimize();
    }
    private static XMLReader createXMLReader(){
        XMLReader xr = null;
        try{
            xr = XMLReaderFactory.createXMLReader();
        }
        catch (SAXException se){}
        xr.setEntityResolver(new DefaultHandler(){
            public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
                if (systemId.endsWith("tei-epidoc.dtd")){
                    return new InputSource(Indexer.class.getResourceAsStream("tei-epidoc.dtd"));
                }
                return super.resolveEntity(publicId, systemId);
            }
        });
        return xr;
    }

    private static void index(String in, String out, boolean recur) throws IOException, SAXException{
        String docRoot = in;
        Directory dir = FSDirectory.getDirectory(new File(out),true);
        Indexer main = new Indexer(dir);
        System.out.println("Start index: " + new java.util.Date(System.currentTimeMillis()));
        main.index(new File(in), recur, false);
        main.optimize();
        System.out.println("End index: " + new java.util.Date(System.currentTimeMillis()));
    }
    public static void main(String[]args) throws IOException, SAXException {
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))){
            printHelp();
            System.exit(0);
        }
        HashMap opts = getOpts(args,
                new String[]{"-i","-o","-r","-t"},
                new String[]{"--in","--out","--recur","--rotate"});
        String in = opts.get("-i").toString();
        String out = opts.get("-o").toString();
        Object recur = opts.get("-r");
        index(in,out,recur != null);
    }
    
    private static void printHelp(){
        System.out.println("Usage:");
        System.out.println("-i/--in\t<STRING>\t: top-level source directory for TEI xml");
        System.out.println("-o/--out\t<STRING>\t: directory for Lucene index");
        System.out.println("-r/--recur\t<BOOL>\t: index all xml files in subdirectories of source directory");
        System.out.println("-t/--rotate\t<BOOL>\t: OBSOLETE - rotate tokens");
    }
    
    private static java.util.HashMap getOpts(String [] args, String [] sFlags, String [] lFlags){
        HashMap result = new HashMap();
        if (args.length == 0) return result;
        argumentss:
        for (int i=0;i<args.length;i++){
            flags:
            for (int j=0;j<sFlags.length;j++){
                if(args[i].equals(sFlags[j]) || args[i].equals(lFlags[j])){
                    if(i+1 >= args.length){
                        Object in = result.get(sFlags[j]);
                        if(in == null){
                            result.put(sFlags[j], Integer.valueOf(1));
                            break flags;
                        }
                        if (in instanceof Integer){
                            result.put(sFlags[j], Integer.valueOf(((Integer)in).intValue() + 1));
                            break flags;
                        }
                        System.err.println("Missing value for flag " + sFlags[j]);
                        System.exit(1);
                    }
                    else {
                        if(args[i+1].charAt(0) == '-'){
                            Object in = result.get(sFlags[j]);
                            if(in == null){
                                result.put(sFlags[j], Integer.valueOf(1));
                                break flags;
                            }
                            if (in instanceof Integer){
                                result.put(sFlags[j], Integer.valueOf(((Integer)in).intValue() + 1));
                                break flags;
                            }
                            System.err.println("Missing value for flag " + sFlags[j]);
                            System.exit(1);
                        }
                        else{
                            result.put(sFlags[j], args[i+1]);
                            i++;
                        }
                    }
                }
            }
        }
        return result;
    }
}
