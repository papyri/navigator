package info.papyri.tests;

import info.papyri.epiduke.lucene.BigramIndexer;
import info.papyri.epiduke.lucene.Indexer;
import info.papyri.epiduke.lucene.LemmaIndexer;
import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer;
import info.papyri.epiduke.lucene.analysis.CopyingTokenFilter;
import info.papyri.epiduke.lucene.analysis.LemmaFilter;
import info.papyri.epiduke.sax.TEIHandler;
import info.papyri.epiduke.sax.TEILineHandler;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public abstract class GreekTestsBase extends TestCase {
	protected IndexSearcher iSearch;
    protected IndexSearcher check;
    protected final Analyzer pfa = getPFA();
    protected java.util.Properties props;
    private static boolean INDEX_BIGRAMS = false;
    protected IndexSearcher bigrams;
    protected IndexSearcher bigramsDF;
    protected IndexSearcher bigramsLC;
    protected IndexSearcher bigramsFL;
    
    protected static final String BAR = "--------------------------------------------------------------------------------";
    protected static final Term LINE_SPAN_TEMPLATE = new Term(Indexer.LINE_SPAN_TERM,"");
    protected static final Term WORD_SPAN_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM,"");
    protected static final Term WORD_SPAN_DF_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM_DF,"");
    protected static final Term WORD_SPAN_FL_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM_FL,"");
    protected static final Term WORD_SPAN_LC_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM_LC,"");
    static final AncientGreekAnalyzer LINE_SPAN_ANALYZER = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.NONE);
    static final AncientGreekAnalyzer WORD_SPAN_ANALYZER = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.NONE,false);
    static final AncientGreekAnalyzer LINE_SPAN_ANALYZER_LC = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE);
    static final AncientGreekAnalyzer WORD_SPAN_ANALYZER_LC = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE,false);
    static final AncientGreekAnalyzer LINE_SPAN_ANALYZER_DF = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.DIACRITICS);
    static final AncientGreekAnalyzer WORD_SPAN_ANALYZER_DF = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.DIACRITICS,false);
    static final AncientGreekAnalyzer LINE_SPAN_ANALYZER_FL = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_DIACRITICS);
    static final AncientGreekAnalyzer WORD_SPAN_ANALYZER_FL = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.CASE_AND_DIACRITICS,false);
    protected static void printMatchedDocs(Hits hits, java.io.PrintStream out) throws IOException {
        Iterator<Hit> iter = hits.iterator();
        while(iter.hasNext()){
            out.println(iter.next().get("fileName"));
        }
    }
    protected static void printMatchedDocs(BitSet matched, IndexSearcher search, java.io.PrintStream out) throws IOException {
        int next = -1;
        while((next = matched.nextSetBit(next+1))!= -1){
            out.println(search.doc(next).get("fileName"));
        }
    }
    static Analyzer getPFA(){
        PerFieldAnalyzerWrapper pfa = new PerFieldAnalyzerWrapper(new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.NONE,false));
        pfa.addAnalyzer(Indexer.WORD_SPAN_TERM, WORD_SPAN_ANALYZER);
        pfa.addAnalyzer(Indexer.WORD_SPAN_TERM_LC, WORD_SPAN_ANALYZER_LC);
        pfa.addAnalyzer(Indexer.WORD_SPAN_TERM_DF, WORD_SPAN_ANALYZER_DF);
        pfa.addAnalyzer(Indexer.WORD_SPAN_TERM_FL, WORD_SPAN_ANALYZER_FL);
        return pfa;
    }
    
    protected static XMLReader createXMLReader(){
        XMLReader xr = null;
        try{
            xr = XMLReaderFactory.createXMLReader();
        }
        catch (SAXException se){}
        xr.setEntityResolver(new DefaultHandler(){
            public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
                if (systemId.endsWith("tei-epidoc.dtd")){
                    return new InputSource(GreekFullTermTest.class.getResourceAsStream("/xml/tei-epidoc.dtd"));
                }
                return super.resolveEntity(publicId, systemId);
            }
        });
        return xr;
    }

    static final Document loadDocument(XMLReader parser, String fileName) throws Exception {
        Document doc = new Document();
        TEIHandler main = (TEIHandler)parser.getContentHandler();
        parser.parse(new InputSource(GreekFullTermTest.class.getResourceAsStream("/xml/" + fileName)));
        //System.out.print(main.getText());
        doc.add(new Field("fileName",fileName,Field.Store.YES,Field.Index.UN_TOKENIZED));
        doc.add(new Field("text",main.getText(),Field.Store.YES,Field.Index.TOKENIZED));
//        TokenStream plainTokens = WORD_SPAN_ANALYZER.tokenStream(null, new StringReader(main.getText()));
//        CachingTokenFilter cache = new CachingTokenFilter(plainTokens);
//         doc.add(new Field(Indexer.LINE_SPAN_TERM,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER)));
//        doc.add(new Field(Indexer.LINE_SPAN_TERM_LC,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER_LC)));
//        doc.add(new Field(Indexer.LINE_SPAN_TERM_DF,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER_DF)));
//        doc.add(new Field(Indexer.LINE_SPAN_TERM_FL,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER_FL)));
        
        TokenStream tokens = WORD_SPAN_ANALYZER.tokenStream(Indexer.WORD_SPAN_TERM, new StringReader(main.getText()));
        doc.add(new Field(Indexer.WORD_SPAN_TERM,tokens));
//        cache.reset();
        tokens = WORD_SPAN_ANALYZER_LC.tokenStream(Indexer.WORD_SPAN_TERM_LC, new StringReader(main.getText()));
        doc.add(new Field(Indexer.WORD_SPAN_TERM_LC,tokens));
//        cache.reset();
        tokens = WORD_SPAN_ANALYZER_DF.tokenStream(Indexer.WORD_SPAN_TERM_DF, new StringReader(main.getText()));
        doc.add(new Field(Indexer.WORD_SPAN_TERM_DF,tokens));
//        cache.reset();
        tokens = WORD_SPAN_ANALYZER_FL.tokenStream(Indexer.WORD_SPAN_TERM_FL, new StringReader(main.getText()));
        doc.add(new Field(Indexer.WORD_SPAN_TERM_FL,tokens));
//        cache.close();
        Connection db = LemmaIndexer.getSeedData(new File("C:/DOCUME~1/User/MYDOCU~1/ddbdp/epidoc-lemmas"));
        tokens = new LemmaFilter(WORD_SPAN_ANALYZER.tokenStream(Indexer.LEMMA_TERM, new StringReader(main.getText())),db);
        doc.add(new Field(Indexer.LEMMA_TERM,tokens));
        return doc;
    }
    
    static final Document loadDocumentWithCache(XMLReader parser, String fileName) throws Exception {
        Document doc = new Document();
        TEIHandler main = (TEIHandler)parser.getContentHandler();
        parser.parse(new InputSource(GreekFullTermTest.class.getResourceAsStream("/xml/" + fileName)));
        //System.out.print(main.getText());
        doc.add(new Field("fileName",fileName,Field.Store.YES,Field.Index.UN_TOKENIZED));
        doc.add(new Field("text",main.getText(),Field.Store.YES,Field.Index.TOKENIZED));
        TokenStream plainTokens = WORD_SPAN_ANALYZER.tokenStream(null, new StringReader(main.getText()));
        CachingTokenFilter cache = new CachingTokenFilter(plainTokens);
//         doc.add(new Field(Indexer.LINE_SPAN_TERM,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER)));
//        doc.add(new Field(Indexer.LINE_SPAN_TERM_LC,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER_LC)));
//        doc.add(new Field(Indexer.LINE_SPAN_TERM_DF,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER_DF)));
//        doc.add(new Field(Indexer.LINE_SPAN_TERM_FL,new LinePositionTokenStream(main.getLines(),LINE_SPAN_ANALYZER_FL)));
        
        doc.add(new Field(Indexer.WORD_SPAN_TERM,WORD_SPAN_ANALYZER.filter(cloneCache(cache))));

        doc.add(new Field(Indexer.WORD_SPAN_TERM_LC,WORD_SPAN_ANALYZER_LC.filter(cloneCache(cache))));

        doc.add(new Field(Indexer.WORD_SPAN_TERM_DF,WORD_SPAN_ANALYZER_DF.filter(cloneCache(cache))));

        doc.add(new Field(Indexer.WORD_SPAN_TERM_FL,WORD_SPAN_ANALYZER_FL.filter(cloneCache(cache))));
        
        cache.close();
        return doc;
    }
    
    public static final CachingTokenFilter cloneCache(CachingTokenFilter src) throws IOException{
        CopyingTokenFilter clone = new CopyingTokenFilter(src);
        clone.next();
        clone.reset();
        src.reset();
        return clone;
    }
    
    protected void setUp() throws Exception {
		super.setUp();
        org.apache.derby.jdbc.EmbeddedDriver ed = new org.apache.derby.jdbc.EmbeddedDriver();
        TEIHandler main = new TEILineHandler(true);
        main.addLineBreakTag("lb");
        main.addTextPattern("TEI.2/text/body/div");
		XMLReader digest = createXMLReader();
        digest.setEntityResolver(info.papyri.data.Indexer.getEpiDocResolver(digest.getEntityResolver()));
		digest.setContentHandler(main);
		digest.parse(new InputSource(GreekTestsBase.class.getResourceAsStream("/xml/p.col.3.2.xml")));

		Directory dir = new RAMDirectory();
        //dir = FSDirectory.getDirectory("test",true);
		IndexWriter iWrite = new IndexWriter(dir,pfa);

        Document doc = loadDocumentWithCache(digest,"p.col.3.2.xml");
        iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"p.col.4.92.xml");
		iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"p.col.1.Inv480.xml");
		iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"p.mich.1.28.xml");
        iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"p.mich.1.42.xml");
        iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"p.mich.1.91.xml");
        iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"p.mich.1.100.xml");
        iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"p.mich.5.295.xml");
        iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"p.col.8.227.xml");
        iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"p.col.11.300.xml");
        iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"p.col.10.249.xml");
        iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"p.col.10.255.xml");
        iWrite.addDocument(doc);

        doc = loadDocumentWithCache(digest,"bgu.2.619.xml");
        iWrite.addDocument(doc);

        iWrite.optimize();
		IndexReader iRead = IndexReader.open(dir);
		iSearch = new IndexSearcher(iRead);
        //props = new java.util.Properties();
        //props.load(GreekFullTermTest.class.getResourceAsStream("/index.properties"));
        FSDirectory.setDisableLocks(true);
        //Directory check = FSDirectory.getDirectory(new File(props.getProperty("index.root")));
        Directory check = FSDirectory.getDirectory(new File("/usr/local/pn/indices/ddbdp/metadata"));
        this.check = new IndexSearcher(IndexReader.open(check));
        
        //File bigramRoot = new File(props.getProperty("bigram.root"));
        File bigramRoot = new File("/usr/local/pn/indices/ddbdp");
        if(!INDEX_BIGRAMS){
            IndexReader plain =IndexReader.open(FSDirectory.getDirectory(new File(bigramRoot,"plain")));
            IndexReader df =IndexReader.open(FSDirectory.getDirectory(new File(bigramRoot,"df")));
            IndexReader lc =IndexReader.open(FSDirectory.getDirectory(new File(bigramRoot,"lc")));
            IndexReader fl =IndexReader.open(FSDirectory.getDirectory(new File(bigramRoot,"fl")));
            bigrams = new IndexSearcher(plain);
            bigramsDF = new IndexSearcher(df);
            bigramsLC = new IndexSearcher(lc);
            bigramsFL = new IndexSearcher(fl);
            Collection fields = this.check.getIndexReader().getFieldNames(IndexReader.FieldOption.ALL);
            for(Object field:fields){
                System.out.print(field.toString() + ", ");
            }
            System.out.println();
            return;
        }
        bigramRoot.mkdirs();
        Term WORD_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM,"");
        Term WORD_FL_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM_FL,"");
        Term WORD_DF_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM_DF,"");
        Term WORD_LC_TEMPLATE = new Term(Indexer.WORD_SPAN_TERM_LC,"");
        IndexReader plain = BigramIndexer.indexBigrams(this.check.getIndexReader(), WORD_TEMPLATE, bigramRoot, "plain");
        IndexReader fl = BigramIndexer.indexBigrams(this.check.getIndexReader(), WORD_FL_TEMPLATE, bigramRoot, "fl");
        IndexReader df = BigramIndexer.indexBigrams(this.check.getIndexReader(), WORD_DF_TEMPLATE, bigramRoot, "df");
        IndexReader lc = BigramIndexer.indexBigrams(this.check.getIndexReader(), WORD_LC_TEMPLATE, bigramRoot, "lc");
        bigrams = new IndexSearcher(plain);
        bigramsDF = new IndexSearcher(df);
        bigramsLC = new IndexSearcher(lc);
        bigramsFL = new IndexSearcher(fl);
        INDEX_BIGRAMS = false;
        
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
        this.check.close();
	}
    
    public static class BitSetCollector extends HitCollector{
        BitSet bits;
        public BitSetCollector(BitSet bits){
            this.bits = bits;
        }
        public void collect(int doc, float score){
            this.bits.set(doc);
        }
    }
   
}