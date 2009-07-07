package info.papyri.tests;

import info.papyri.epiduke.lucene.Indexer;
import info.papyri.epiduke.lucene.LemmaIndexer;
import info.papyri.epiduke.lucene.analysis.*;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
public class LemmaTest extends GreekTestsBase {
    private final static String ARGUROUS_HW = "ἀργ";
    public void testLemmaIndex() throws Exception {
        Connection db = LemmaIndexer.getSeedData(new File("/usr/local/pn/db/lemmas"));
        PreparedStatement stmt = LemmaFilter.getPreparedStatement(db);
        String morph = "ἀργυρίου";
        Iterator<String> lemmas = LemmaFilter.getLemmas(morph, stmt).iterator();
        boolean succeed = false;
        while(lemmas.hasNext()){
            String lemma = lemmas.next();
            System.err.println(lemma);
            succeed |= (lemma.equals(morph));
        }
        assertTrue(succeed);
    }
    public void testLemmas() throws IOException {
        Term term = new Term(Indexer.LEMMA_TERM,ARGUROUS_HW);
        PrefixQuery query = new PrefixQuery(term);
        Term blank = term.createTerm("");
        TermEnum terms = iSearch.getIndexReader().terms(blank);
        do{
            System.err.print(terms.term());
        }while(terms.next() && (blank=terms.term()).field()==term.field());
        
        Hits hits = iSearch.search(query);
        
        TreeSet<String> docs = new TreeSet<String>();
        Iterator<Hit> iter = hits.iterator();
        while(iter.hasNext()){
            String id =iter.next().get(Indexer.DDBDP_ID); 
            System.out.println(id);
            docs.add(id);
        }
        String expected = "oai:papyri.info:identifiers:ddbdp:0094:8:227";
        assertTrue(docs.contains(expected));
    }
    
    public void testLemmaPhrase(){
        
    }
}
