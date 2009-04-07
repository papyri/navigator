package info.papyri.tests;

import info.papyri.epiduke.lucene.BigramIndexer;
import info.papyri.epiduke.lucene.Indexer;
import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer;
import info.papyri.epiduke.lucene.spans.SubstringSpanTermQuery;
import info.papyri.epiduke.sax.TEIHandler;
import info.papyri.epiduke.sax.TEILineHandler;
import  info.papyri.epiduke.lucene.analysis.AncientGreekCharsets;
import java.io.StringReader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.xml.sax.XMLReader;

public class LongDocTest extends GreekTestsBase {

    static final String FL_TERM = "βωιδοσ";
    static final String LC_TERM = "β\u1f7dϊδοσ";
    static final String DF_TERM = "Βωιδοσ";
    static final String PLAIN_TERM ="Β\u1f7dϊδοσ";

    public void setUp() throws Exception {
        super.setUp();
    }
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testPlain() throws Exception {
        char [] plain = PLAIN_TERM.toCharArray();
        char [] filtered = FL_TERM.toCharArray();
        assertEquals(plain.length,filtered.length);
        for(int i=0;i<plain.length;i++){
            plain[i]=AncientGreekCharsets.toLowerCase(plain[i]);
            plain[i]=AncientGreekCharsets.toUnaccented(plain[i]);
            assertEquals(filtered[i],plain[i]);
        }
        testLongAssedDoc(WORD_SPAN_TEMPLATE,PLAIN_TERM,WORD_SPAN_ANALYZER);        
    }
    
    public void testDF() throws Exception {
        testLongAssedDoc(WORD_SPAN_DF_TEMPLATE,DF_TERM,WORD_SPAN_ANALYZER_DF);        
    }

    public void testFL() throws Exception {
        testLongAssedDoc(WORD_SPAN_FL_TEMPLATE,FL_TERM,WORD_SPAN_ANALYZER_FL);        
    }

    public void testLC() throws Exception {
        testLongAssedDoc(WORD_SPAN_LC_TEMPLATE,LC_TERM,WORD_SPAN_ANALYZER_LC);        
    }

    public void testLongAssedDoc(Term template, String testText, AncientGreekAnalyzer analyzer) throws Exception {
        
        TEIHandler main = new TEILineHandler(true);
        main.addLineBreakTag("lb");
        main.addTextPattern("TEI.2/text/body/div");
        XMLReader digest = createXMLReader();
        digest.setContentHandler(main);

        Directory dir = new RAMDirectory();
//        Directory    dir = FSDirectory.getDirectory("test",true);
        IndexWriter iWrite = new IndexWriter(dir,pfa);
        iWrite.setMaxFieldLength(32*1024);
        Document psorb = loadDocumentWithCache(digest, "p.sorb.2.69.xml");
        iWrite.addDocument(psorb);
        iWrite.optimize();
        iWrite.close();
        IndexReader reader = IndexReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
//        IndexSearcher searcher = check;
//        IndexReader bReader = BigramIndexer.indexBigrams(reader, WORD_SPAN_TEMPLATE, new RAMDirectory());
//        IndexSearcher bSearcher = new IndexSearcher(bReader);
        TermQuery query = new TermQuery(new Term("fileName","p.sorb.2.69.xml"));
        Hits hits = searcher.search(query);
        assertEquals(1,hits.length());
        Document doc = hits.doc(0);
        String text = doc.getField("text").stringValue();

        TokenStream tokens = analyzer.tokenStream(template.field(), new StringReader(text));
        Token t = null;
        boolean succeeded = false;
        int ctr = 0;
        Term test = template.createTerm("");
        java.util.HashSet<String> values = new java.util.HashSet<String>();
        while((t = tokens.next()) != null){
            String tt = t.termText();
            values.add(tt);
            ctr++;
            if(tt.equals(testText)){
                System.out.println("Found " + template.field() + " term " + tt + " at " + ctr + " in token stream");
                TokenStream bt = BigramIndexer.getBigramTokenStream(tt);
                Token b = null;
                System.out.print("\t");
                test = template.createTerm(tt);
                while((b=bt.next()) != null) System.out.print(b.termText() + " ,");
                System.out.println();
                succeeded = true;
            }
        }
        int unique = values.size();
        System.out.println("tokens counted: " + ctr);
        System.out.println("unique tokens: " + unique);
        assertTrue(succeeded);
        TermQuery tq = new TermQuery(test);
        hits = searcher.search(tq);
        assertTrue("TermQuery failed: " + test,hits.length() > 0);
        SpanTermQuery fullTermQuery = new SpanTermQuery(template.createTerm(testText)); // ,bigramsFL);
        //SubstringSpanTermQuery plainQuery = new SubstringSpanTermQuery(template.createTerm(testText),bigrams);
        hits = searcher.search(fullTermQuery);
        assertTrue(hits.length() > 0);
                TermEnum terms = searcher.getIndexReader().terms(template);
                int skipped = 0;
                int weird=0;
                boolean logged = false;
                do{
                    Term term = terms.term();
                    if(term==null) break;
                    if(!term.field().equals(template.field())){
//                    if(!term.field() != template.field()){
                        if(!logged){
                            logged = true;
                            System.err.println("gone to " + term.field());
                        }
                        break;
                    }
                    assertEquals(term.field(),template.field());
                    if (!values.remove(term.text())){
                        //System.err.println("that's weird: " + term.text() + " was not in values hash");
                        weird++;
                    }
                    if(test.compareTo(term)==0){
                        System.out.println("Found Term in enum");
                        break;
                    }
                    skipped++;
                }while(terms.next());

                if(skipped > 0) System.out.println("Examined and skipped " + skipped + " terms");
                if(weird > 0) System.out.println("weird: " + weird);
                if(values.size() + skipped + 1 != unique) System.out.println("Did not find " + values.size() + " terms from tokens");
                for(String missing:values){
                    //System.err.println("\t\"" + missing + "\"");
                }
                assertTrue(terms.term()!=null);
                assertEquals(test.toString(),terms.term().toString());
        assertTrue(succeeded);
        
        CachingTokenFilter cache = new CachingTokenFilter(analyzer.tokenStream(template.field(), new StringReader(text)));
        cache.next();
        cache.reset();
        CachingTokenFilter scoreCache = new CachingTokenFilter(analyzer.tokenStream(template.field(), new StringReader(text)));
        scoreCache.next();
        scoreCache.reset();
        SpanScorer scorer = new SpanScorer(fullTermQuery,template.field(),scoreCache);
        WeightedSpanTerm wst = scorer.getWeightedSpanTerm(testText);
        java.util.List positions = wst.getPositionSpans();

        System.out.println(wst.getTerm());
        cache.reset();
       scoreCache.reset();
        
        TextFragment[] frags =
//            HighlightUtil.getBestTextFragmentsNoGroup(cache, text, new LineFragmenter(), scorer, true, 5);
       HighlightUtil.getBestTextFragmentsNoGroup(cache, text, new LineFragmenter(), fullTermQuery, template.field(), true, 5);
        assertTrue(frags.length > 0);
        System.out.println(frags[0].toString());
    }
    
    public void testForRoamingKoronis() throws Exception {
        TEIHandler main = new TEILineHandler(true);
        main.addLineBreakTag("lb");
        main.addTextPattern("TEI.2/text/body/div");
        XMLReader digest = createXMLReader();
        digest.setContentHandler(main);
        Directory dir = new RAMDirectory();
//      Directory    dir = FSDirectory.getDirectory("test",true);
      IndexWriter iWrite = new IndexWriter(dir,pfa);
      iWrite.setMaxFieldLength(32*1024);
      Document owilb = loadDocumentWithCache(digest, "o.wilb.26.xml");
      iWrite.addDocument(owilb);
      Document pbingen = loadDocumentWithCache(digest, "p.bingen.119.xml");
      iWrite.addDocument(pbingen);
      Document plips = loadDocumentWithCache(digest, "p.lips.1.44.xml");
      iWrite.addDocument(plips);
      iWrite.optimize();
      iWrite.close();
      IndexReader reader = IndexReader.open(dir);
//
//        String iRoot = "C:/DOCUME~1/User/MYDOCU~1/ddbdp/epidoc-fast";
//        String docRoot = iRoot + "/docs";
//        iRoot += "/plain";
//        IndexReader docReader = IndexReader.open(FSDirectory.getDirectory(docRoot));
//        IndexReader plainReader = IndexReader.open(FSDirectory.getDirectory(iRoot));
        Term koronis = WORD_SPAN_TEMPLATE.createTerm("\u1fbd");
        TermDocs docs = reader.termDocs(koronis);
        while(docs.next()){
            Document d = reader.document(docs.doc());
            System.out.print(d.get("fileName") + " [");
            String text = d.getField("text").stringValue();
            int kIx = text.indexOf("\u1fbd");
            System.out.print(kIx);
            System.out.print(" ] : ");
            int start = Math.max(kIx - 25, 0);
            int end = Math.min(kIx+25, text.length());
            System.out.println(text.substring(start,end));
        }
    }
    
    

}
