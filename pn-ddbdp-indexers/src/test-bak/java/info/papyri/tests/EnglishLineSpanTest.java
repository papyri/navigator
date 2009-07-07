package info.papyri.tests;

import info.papyri.epiduke.lucene.analysis.LinePositionTokenStream;
import info.papyri.epiduke.sax.TEIHandler;

import java.io.IOException;

import java.util.Iterator;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import junit.framework.TestCase;

public class EnglishLineSpanTest extends TestCase {

	private IndexSearcher iSearch;
	private static final String LINE_SPAN_TERM = "lines";
	private static final String WORD_SPAN_TERM = "words";
	private static final Term LINE_SPAN_TEMPLATE = new Term(LINE_SPAN_TERM,"");
	private static final Term WORD_SPAN_TEMPLATE = new Term(WORD_SPAN_TERM,"");

	private SpanTermQuery L1W2W3 = new SpanTermQuery(LINE_SPAN_TEMPLATE.createTerm("diddle"));
	private SpanTermQuery L3W6 = new SpanTermQuery(LINE_SPAN_TEMPLATE.createTerm("moon"));
	private SpanTermQuery L5W8 = new SpanTermQuery(LINE_SPAN_TEMPLATE.createTerm("spoon"));


	private SpanTermQuery W2W3 = new SpanTermQuery(WORD_SPAN_TEMPLATE.createTerm("diddle"));
	private SpanTermQuery W14 = new SpanTermQuery(WORD_SPAN_TEMPLATE.createTerm("moon"));
	private SpanTermQuery W30 = new SpanTermQuery(WORD_SPAN_TEMPLATE.createTerm("spoon"));
	
	
	protected void setUp() throws Exception {
		super.setUp();
		TEIHandler main = new TEIHandler();
		main.addLineBreakTag("lb");
		main.addLineBreakTag("div0");
		main.addTextPattern("TEI.2/div0");
		XMLReader digest = XMLReaderFactory.createXMLReader();
		digest.setContentHandler(main);
		digest.parse(new InputSource(EnglishLineSpanTest.class.getResourceAsStream("/xml/heydiddle.xml")));

		Directory inmem = new RAMDirectory();
		StandardAnalyzer sa = new StandardAnalyzer();
		IndexWriter iWrite = new IndexWriter(inmem,sa);
		Document doc = new Document();
		Iterator<String> lines = main.getLines();
		doc.add(new Field(LINE_SPAN_TERM,new LinePositionTokenStream(lines,sa),Field.TermVector.YES));
		doc.add(new Field(WORD_SPAN_TERM,main.getText(),Field.Store.YES,Field.Index.TOKENIZED));

		iWrite.addDocument(doc);
		iWrite.optimize();
		IndexReader iRead = IndexReader.open(inmem);
		iSearch = new IndexSearcher(iRead);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testLineSpanPositiveOrderAccept() throws IOException {

		SpanNearQuery shouldMatch = new SpanNearQuery(new SpanQuery[]{L1W2W3,L3W6},2,true); 
		Hits hits = iSearch.search(shouldMatch);
		int shouldExpected = 1;
		int shouldActual = (hits == null)?0:hits.length();
		String positiveErrorMsg = "Expected " + shouldExpected + " hits for " + shouldMatch + "; got " + shouldActual;
		assertTrue(positiveErrorMsg,(shouldExpected == shouldActual));
	}
	
	public void testLineSpanPositiveOrderReject() throws IOException {

		SpanNearQuery shouldNotMatch = new SpanNearQuery(new SpanQuery[]{L1W2W3,L5W8},2,true); 
		Hits hits = iSearch.search(shouldNotMatch);
		int expected = 0;
		int actual = (hits == null)?0:hits.length();
		String positiveErrorMsg = "Expected " + expected + " hits for " + shouldNotMatch + "; got " + actual;
		assertTrue(positiveErrorMsg,(expected == actual));
	}
	
	public void testWordSpanPositiveOrderAccept() throws IOException {

		SpanNearQuery shouldMatch = new SpanNearQuery(new SpanQuery[]{W2W3,W14},12,true);
		Hits hits = iSearch.search(shouldMatch);
		int shouldExpected = 1;
		int shouldActual = (hits == null)?0:hits.length();
		String positiveErrorMsg = "Expected " + shouldExpected + " hits for " + shouldMatch + "; got " + shouldActual;
		assertTrue(positiveErrorMsg,(shouldExpected == shouldActual));
	}
	
	public void testWordSpanPositiveOrderReject() throws IOException {
		SpanNearQuery shouldNotMatch = new SpanNearQuery(new SpanQuery[]{W2W3,W30},12,true); 
		Hits hits = iSearch.search(shouldNotMatch);
		int expected = 0;
		int actual = (hits == null)?0:hits.length();
		String positiveErrorMsg = "Expected " + expected + " hits for " + shouldNotMatch + "; got " + actual;
		assertTrue(positiveErrorMsg,(expected == actual));
	}
	

}
