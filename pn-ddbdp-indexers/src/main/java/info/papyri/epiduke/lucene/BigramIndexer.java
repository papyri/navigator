package info.papyri.epiduke.lucene;

import java.io.File;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class BigramIndexer {

	public static IndexReader indexBigrams(IndexReader src, Term template, File indexRoot, String indexName) throws Exception {
	    File index = new File(indexRoot, indexName);
	    Directory bigram = FSDirectory.getDirectory(index);
	    return indexBigrams(src, template,bigram);
	}
	
	public static IndexReader indexBigrams(IndexReader src, Term template, Directory store) throws Exception {

	    IndexWriter writer = new IndexWriter(store,null,true,IndexWriter.MaxFieldLength.LIMITED);
	    writer.setTermIndexInterval(IndexWriter.DEFAULT_TERM_INDEX_INTERVAL/16);
	    String field = template.field();
	    TermEnum terms = src.terms(template);
        int ctr = 0;
        try{
            do{
                Term term = terms.term();
                if(term == null || !field.equals(term.field())) break;
                String text = term.text();
                Document tDoc = new Document();
                tDoc.add(new Field("term",text,Field.Store.YES,Field.Index.NO));
                tDoc.add(new Field(field,getBigramTokenStream(text)));
                writer.addDocument(tDoc);
                ctr++;
            } while(terms.next());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            terms.close();
            writer.optimize();
            writer.close();
        }
	    System.out.println("indexed bigrams of " + ctr+ " terms of " + field);
	    return IndexReader.open(store);
	}

	public static TokenStream getBigramTokenStream(final String text){
	    final int len = text.length() + 1;
	    final String [ ]tokens = new String[len];
	    if(len > 1){
	    	char [] buf = new char[2];
	    	buf[0] = '^';
	    	buf[1] = text.charAt(0);
	    	tokens[0] = new String(buf);
	    for(int i =1;i<text.length();i++){
	    	buf[0] = text.charAt(i-1);
	    	buf[1] = text.charAt(i);
	        tokens[i] = new String(buf);
	    }
	    buf[0] = tokens[tokens.length - 2].charAt(1);
	    buf[1] = '^';
	    tokens[tokens.length - 1] = new String(buf);
	    }
	    else tokens[0] = "^^";
	    return new TokenStream(){
	        int ix = 0;
	        public Token next(){
	            if(ix >= tokens.length) return null;
	            Token t = new Token(tokens[ix],ix,ix+1);
	            ix++;
	            return t;
	        }
	    };
	    
	}
}
