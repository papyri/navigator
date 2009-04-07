package xml;

import java.io.StringReader;

import java.util.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import org.apache.lucene.store.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import info.papyri.epiduke.lucene.*;
import info.papyri.epiduke.lucene.analysis.LinePositionTokenStream;
import info.papyri.epiduke.sax.TEIHandler;

public class Indexer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			TEIHandler main = new TEIHandler();
			XMLReader digest = XMLReaderFactory.createXMLReader();
			digest.setContentHandler(main);
			digest.parse(new InputSource(Indexer.class.getResourceAsStream("heydiddle.xml")));
			System.out.println(main.getText());
			Directory inmem = new RAMDirectory();
            StandardAnalyzer sa = new StandardAnalyzer();
			IndexWriter iWrite = new IndexWriter(inmem,sa);
			Document doc = new Document();
			Iterator<String> lines = main.getLines();
			doc.add(new Field("lines",new LinePositionTokenStream(lines,sa),Field.TermVector.YES));
			doc.add(new Field("words",main.getText(),Field.Store.YES,Field.Index.TOKENIZED));

			iWrite.addDocument(doc);
			iWrite.optimize();
			IndexReader iRead = IndexReader.open(inmem);
			IndexSearcher iSearch = new IndexSearcher(iRead);
			SpanTermQuery first = new SpanTermQuery(new Term("lines","diddle"));
			SpanTermQuery second = new SpanTermQuery(new Term("lines","moon"));
			SpanTermQuery third = new SpanTermQuery(new Term("lines","spoon"));
			Hits hits = iSearch.search(new SpanNearQuery(new SpanQuery[]{first,second},2,true));
			System.out.println("Hits: " + hits.length());
			hits = iSearch.search(new SpanNearQuery(new SpanQuery[]{first,third},2,true));
			System.out.println("Hits: " + hits.length());
		}
		catch (Throwable t){
			System.err.println(t.toString());
		}
		

	}
	
	public Indexer(){
		
	}
	
	private StringBuffer tokens = new StringBuffer();
	public void pushLineBreakToken(){
		tokens.append("<<LINEBREAK>>");
	}
	public void pushText(String text){
		tokens.append(text);
	}

}
