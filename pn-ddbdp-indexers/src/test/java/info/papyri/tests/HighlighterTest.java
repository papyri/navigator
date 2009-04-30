package info.papyri.tests;

import org.apache.lucene.search.highlight.PNSpanScorer;
import info.papyri.epiduke.lucene.Indexer;
import info.papyri.epiduke.lucene.IndexOfQuery;
import info.papyri.epiduke.lucene.SubstringQuery;
import info.papyri.epiduke.lucene.spans.SubstringSpanTermQuery;
import info.papyri.epiduke.lucene.spans.SpanSequenceQuery;
import info.papyri.epiduke.lucene.WildcardSegmentDelegate;
import info.papyri.epiduke.lucene.WildcardSubstringQuery;
import info.papyri.epiduke.lucene.analysis.AnchoredTokenStream;
import info.papyri.epiduke.lucene.analysis.AncientGreekAccentFilter;
import info.papyri.epiduke.lucene.analysis.AncientGreekLowerCaseFilter;
import info.papyri.epiduke.lucene.analysis.LinePositionTokenStream;
import info.papyri.epiduke.lucene.analysis.SubstringRotationTokenStream;
import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer;
import info.papyri.epiduke.lucene.analysis.VectorTokenFilter;
import info.papyri.epiduke.sax.TEIHandler;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import java.util.BitSet;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.SubstringPhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryFilter;

import org.apache.lucene.search.highlight.*;

import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import junit.framework.TestCase;

public class HighlighterTest extends GreekTestsBase {
    private final static FieldSelector NAME_AND_TEXT = new FieldSelector(){
        public FieldSelectorResult accept(String field){
            if("filename".equals(field) ||"text".equals(field)) return FieldSelectorResult.LOAD;
            return FieldSelectorResult.NO_LOAD;
        }
    };

	private static final Term L2W2fullTerm = WORD_SPAN_TEMPLATE.createTerm("ἐξ");
    private static final Term L2W2iFullTerm = new Term(Indexer.WORD_SPAN_TERM_DF,"\u03B5ξ");
    private static final Term invL11W3fullTerm = WORD_SPAN_TEMPLATE.createTerm("τις");
    private static final Term invL11W4fullTerm = WORD_SPAN_TEMPLATE.createTerm("ἀγοράσηι");
    private static final Term invL11W4substring = WORD_SPAN_DF_TEMPLATE.createTerm("\u03B1\u03B3\u03BF\u03C1\u03B1");
    private static final Term invL11W9substring = WORD_SPAN_TEMPLATE.createTerm("π\u1F71ντα");
    private static final Term invL11W1substringFAIL = WORD_SPAN_TEMPLATE.createTerm("^\u03B3\u03BF\u03C1\u1F71^");
    private static final Term fail = WORD_SPAN_TEMPLATE.createTerm("ἀγἐξσηι");
    private static final Term pm5_295_4_1fullTerm = WORD_SPAN_TEMPLATE.createTerm("μοι");
    private static final String TEXT_FIELD = "text".intern();
    public void testSubstringPhraseHighlighter() throws IOException {
        //String fName = Indexer.WORD_SPAN_TERM_FL.intern();
        String l2w2i = "εξ";
        String l2w2 = "ἐξ";

        Term substring = new Term(Indexer.WORD_SPAN_TERM_DF,l2w2i);
        Term substring2 = new Term(Indexer.WORD_SPAN_TERM_DF,"Σιδ");
        SubstringPhraseQuery query = new SubstringPhraseQuery();
        query.add(substring);
        query.add(substring2);
        Hits hits = iSearch.search(query);
        assertTrue((hits.length() > 0));
        Iterator<Hit> iter = hits.iterator();

        Hit hit = iter.next();
        assertTrue((hit != null));
        Document doc = hit.getDocument();

        assertTrue(doc != null);
        Field field = doc.getField(TEXT_FIELD);
        assertTrue("Field not found - " + TEXT_FIELD, field != null);
        String text = field.stringValue();
        AncientGreekAnalyzer analyzer = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.DIACRITICS);
        org.apache.lucene.analysis.TokenStream textTokens = analyzer.tokenStream(null, new StringReader(text));

        assertTrue(textTokens != null);
        textTokens = new AnchoredTokenStream(textTokens);

        CachingTokenFilter cached = SubstringPhraseQuery.getCachedTokens(query, textTokens);
        PNSpanScorer scorer = new PNSpanScorer(query,Indexer.WORD_SPAN_TERM_DF,cached);
        cached.reset();
//        TextFragment [] frags = HighlightUtil.getBestTextFragments(cached, text, new LineFragmenter(), new QueryScorer(query), true, 3);
        TextFragment [] frags = HighlightUtil.getBestTextFragmentsNoGroup(cached, text, new LineFragmenter(), scorer, true, 3);
       assertTrue("no fragments retrieved",frags.length != 0);
        
        String frag = frags[0].toString();
        assertTrue("no fragments retrieved",frag != null);
        System.out.println(frag);
        boolean check = false;
        String val = substring.text();
        val = val.replaceAll( "[\\^]", "");
        val = "<B>" + l2w2;
        check = frag.contains(val);
        assertTrue("\"" + val + "\" not contained in \"" + frag + "\"",check);
        String val2 = substring2.text();
        val2 = val2.replaceAll( "[\\^]", "");
        val2 = "<B>" + val2;
        check = check && frag.contains(val2);
        assertTrue("\"" + val2 + "\" not contained in \"" + frag + "\"",check);
        assertTrue("Unexpected score of " + frags[0].getScore(), frags[0].getScore() > 0.5f);
    }


    public void testSubstringTermHighlighter() throws IOException {

        AncientGreekAnalyzer analyzer = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.NONE);
        Term substring = invL11W4fullTerm;
        SubstringSpanTermQuery query = new SubstringSpanTermQuery(substring,bigrams);

        Hits hits = iSearch.search(query);
        assertTrue((hits.length() > 0));
        Iterator<Hit> iter = hits.iterator();

        Hit hit = iter.next();
        assertTrue((hit != null));
        Document doc = hit.getDocument();

        assertTrue(doc != null);
        String text = doc.getField(TEXT_FIELD).stringValue();
        //Highlighter highlight = SubstringQuery.getHighlighter(query, iSearch.getIndexReader());
//        String frag = highlight.getBestFragment(tokens,text);
        LineFragmenter lf = new LineFragmenter();

         org.apache.lucene.analysis.TokenStream tokens = analyzer.tokenStream(TEXT_FIELD, new StringReader(text));
         CachingTokenFilter cache = new CachingTokenFilter(tokens);
         PNSpanScorer scorer = new PNSpanScorer(query,invL11W4fullTerm.field(),cache);
         cache.reset();
        TextFragment [] frags =  HighlightUtil.getBestTextFragmentsNoGroup(cache, text, lf, scorer, true,3);
        assertTrue(frags.length != 0);
        String actual = frags[0].toString();

        boolean check = false;
        String val = substring.text();
        val = val.replaceAll("[\\^]", "");
        val = "<B>" + val + "</B>";
//        System.out.println(frag);
        check = actual.contains(val); 
        assertTrue("\"" + val + "\" not contained in \"" + actual + "\"",check);
    }
    public void testWildcardSubstringHighlighter() throws IOException {
        Term substring = invL11W4fullTerm.createTerm("^Να*ρα");  //     ()
        AncientGreekAnalyzer analyzer = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.NONE);
        SpanQuery query = new SubstringSpanTermQuery(substring,bigrams);

        Hits hits = iSearch.search(query);
        assertTrue((hits.length() > 0));
        Iterator<Hit> iter = hits.iterator();

        Hit hit = iter.next();
        assertTrue((hit != null));
        Document doc = hit.getDocument();

        assertTrue(doc != null);

        Field field = doc.getField(TEXT_FIELD);
        assertTrue("Field not found - " + TEXT_FIELD, field != null);
        String text = hit.getDocument().getField(TEXT_FIELD).stringValue();
        org.apache.lucene.analysis.TokenStream tokens = analyzer.tokenStream(null, new StringReader(text));

        assertTrue(tokens != null);
        //tokens = new AnchoredTokenStream(tokens);
        CachingTokenFilter cache = new CachingTokenFilter(tokens);
        PNSpanScorer scorer = new PNSpanScorer(query,invL11W4fullTerm.field(),cache);
        cache.reset();
        TextFragment [] frags = HighlightUtil.getBestTextFragmentsNoGroup(cache, text, new SimpleFragmenter(), scorer, false, 10);
        //Highlighter highlight = SubstringQuery.getHighlighter(query, iSearch.getIndexReader());
        assertTrue("no fragments retrieved",frags.length  != 0);
        String frag = frags[0].toString(); // highlight.getBestFragment(tokens,text);

        boolean check = false;
        String val = "Να\u1F7Bκρατιν";
        val = val.replaceAll("[\\^]", "");
        val = "<B>" + val + "</B>";
//        System.out.println(frag);
        check = frag.contains(val); 
        assertTrue("\"" + val + "\" not contained in \"" + frag + "\"",check);
    }
    
    public void testBracketEncoder(){
        String src = ">foo<bar<";
        String expected = "&gt;foo&lt;bar&lt;";
        String actual = BracketEncoder.THREADSAFE_ENCODER.encodeText(src);
        assertEquals(actual,expected);
    }
    
    public void testMultipleDocumentHighlighting(){
        BooleanQuery.setMaxClauseCount(2*BooleanQuery.getMaxClauseCount());
        SubstringQuery q = new SubstringQuery(WORD_SPAN_DF_TEMPLATE.createTerm("και"));
        try{
            final BitSet hits = new BitSet(check.maxDoc());
            check.search(q,new HitCollector(){
                public void collect(int doc,float weight){
                    hits.set(doc);
                }
            });
            System.out.println(q.toString() + " matched " + hits.cardinality());
            SimpleFragmenter fragmenter = new SimpleFragmenter();
            int hitCtr = -1;
            int pos = -1;
            IndexReader reader =  this.check.getIndexReader();
            //long totalTokenizing = 0;
            //long totalHighlighting = 0;
            AncientGreekAnalyzer analyzer =  new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.DIACRITICS,false);
            long start = System.currentTimeMillis();
            while((pos = hits.nextSetBit(pos+1)) != -1 && hitCtr < 50){
                hitCtr++;
                Document doc = reader.document(pos,NAME_AND_TEXT);
                String text = doc.getField(TEXT_FIELD).stringValue();
                TokenStream textTokens = new AnchoredTokenStream(analyzer.tokenStream(null, new StringReader(text)));
                CachingTokenFilter cache = new CachingTokenFilter(textTokens);
                PNSpanScorer scorer =new PNSpanScorer(q,WORD_SPAN_DF_TEMPLATE.field(),cache);
                cache.reset();
                TextFragment [] frags = HighlightUtil.getBestTextFragmentsNoGroup(cache,text,fragmenter,scorer, true, 3);
//                TextFragment [] frags = HighlightUtil.getBestTextFragments(textTokens,text,fragmenter,scorer, true, 3);
                
            }
            long timing = System.currentTimeMillis() - start;
            assertTrue("Timing: " + timing + " ms",timing < 500L);
            System.out.println("Timing: " + timing + " ms");
        }
        catch(IOException ioe){
            fail(ioe.toString());
        }

    }
    
    public void testLineFragmenter() throws IOException {
        return;
        /*
        TermQuery nameFilter = new TermQuery(new Term("fileName","p.mich.1.28.xml"));
        TermQuery q = new TermQuery(invL11W4fullTerm);
        Hits hits = check.search(q, new QueryFilter(nameFilter));
        Document doc = hits.doc(0);
        System.out.println(doc.get("fileName"));
        String text = doc.getField(TEXT_FIELD).stringValue();
        AncientGreekAnalyzer analyzer = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.NONE,false);
        TokenStream tokenStream = (analyzer.tokenStream(null, new StringReader(text)));
        CachingTokenFilter cache = new CachingTokenFilter(tokenStream);
        LineFragmenter textFragmenter = new LineFragmenter();
        PNSpanScorer fragmentScorer = new PNSpanScorer(q,invL11W4fullTerm.field(),cache);
        cache.reset();
        TextFragment [] docFrags = HighlightUtil.getTextFragments(new CachingTokenFilter(tokenStream), text, textFragmenter, fragmentScorer);
        assertEquals(37,docFrags.length);
        tokenStream = analyzer.tokenStream(null, new StringReader(text));
        cache = new CachingTokenFilter(tokenStream);
        fragmentScorer = new PNSpanScorer(q,invL11W4fullTerm.field(),cache);
        cache.reset();
        TextFragment [] frags = HighlightUtil.getBestTextFragmentsNoGroup(new CachingTokenFilter(tokenStream),text,textFragmenter,fragmentScorer, false, 3);
        assertEquals(1, frags.length);
        System.out.println(frags[0]);
         */
    }
    
    public void testPhraseScorerAcrossLines() throws IOException {
        //String fName = Indexer.WORD_SPAN_TERM_FL.intern();
        String t1 = "^κα\u1f76";
        String t2 = "^ὑπὸ";

        Term substring = new Term(Indexer.WORD_SPAN_TERM,t1);
        Term substring2 = new Term(Indexer.WORD_SPAN_TERM,t2);
        SpanQuery[] spans = new SpanQuery[]{new SubstringSpanTermQuery(substring,bigrams),new SubstringSpanTermQuery(substring2,bigrams)};
        SpanNearQuery query = new SpanNearQuery(spans,1,true);

        TermQuery nameFilter = new TermQuery(new Term("fileName","bgu.8.1836.xml")); // lines 16-17
        Hits hits = check.search(query,new QueryFilter(nameFilter));
        assertTrue("No hits for expected match", hits.length() != 0);
        Document doc = hits.doc(0);
        Field field = doc.getField(TEXT_FIELD);
        assertTrue("Field not found - " + TEXT_FIELD, field != null);
        String text = field.stringValue();
        AncientGreekAnalyzer analyzer = new AncientGreekAnalyzer(AncientGreekAnalyzer.Normalize.NONE);
        org.apache.lucene.analysis.TokenStream textTokens = analyzer.tokenStream(null, new StringReader(text));

        assertTrue(textTokens != null);
        CachingTokenFilter cache = null;
//        cache = new CachingTokenFilter(textTokens);
        VectorTokenFilter vector = new VectorTokenFilter(textTokens);
        vector.buildCache();
        vector.reset();
        cache = vector.clone();
        PNSpanScorer fragmentScorer = new PNSpanScorer(query,Indexer.WORD_SPAN_TERM,cache);
        cache = vector.clone();
        TextFragment [] frags = HighlightUtil.getBestTextFragmentsNoGroup(cache, text, new LineFragmenter(), fragmentScorer, true, 3);
       assertTrue("no fragments retrieved",frags.length != 0);
        
        String frag = frags[0].toString();
        assertTrue("no fragments retrieved",frag != null);
        for(int i=0;i<frags.length;i++){
            System.out.println("[FRAG " +i  + "]: " + frags[i].toString() + " (" + frags[i].getScore() + ")");

            assertTrue("Unexpected score of " + frags[i].getScore(), frags[i].getScore() > 0.9f);
        }
    }
    

}