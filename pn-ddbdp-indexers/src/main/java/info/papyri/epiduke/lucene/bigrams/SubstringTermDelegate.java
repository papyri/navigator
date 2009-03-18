package info.papyri.epiduke.lucene.bigrams;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import info.papyri.epiduke.lucene.IntQueue;
import info.papyri.epiduke.lucene.SubstringDelegate;

public class SubstringTermDelegate implements SubstringDelegate {
    protected Term substring;
    protected String text;
    protected String [] bigrams;
    protected Query bigramQuery;
    protected final IndexSearcher bgSearcher;

    public SubstringTermDelegate(IndexSearcher bgSearcher) {
        this.bgSearcher = bgSearcher;
    }
    public SubstringTermDelegate(IndexSearcher bgSearcher, Term substring) {
        this(bgSearcher);
        setTerm(substring);
    }
        
    public boolean matches(char[] text, int start, int len) throws IOException {
        String test = new String(text,start,len);
        return matches(test);
    }
    public String[] matches() throws IOException {
        if(bgSearcher == null) return info.papyri.util.ArrayTypes.STRING;
        Weight weight = bigramQuery.weight(bgSearcher);
        Scorer scorer = weight.scorer(bgSearcher.getIndexReader());
//        ArrayList<String> terms = new ArrayList<String>();
        String [] terms = new String[64];
        int ix = 0;
        while(scorer.next()){
            if(ix == terms.length){
                String [] newTerms = new String[terms.length*2];
                System.arraycopy(terms, 0, newTerms, 0, terms.length);
                terms = newTerms;
            }
            terms[ix++] = (bgSearcher.doc(scorer.doc()).get("term"));
        }
        String [] result = new String[ix];
        System.arraycopy(terms, 0, result, 0, ix);
        return result;
     }
    public String[] matches(IndexReader reader) throws IOException {
        return matches();
    }
    public boolean matches(String input) throws IOException {
    	return input.contains(text);
    }
    public Term substringTerm() {
        return substring;
    }
    
    public void setTerm(Term substring){
        this.substring = substring;
        this.text = substring.text();
        if (text.length() < 2) throw new IllegalArgumentException("Substring pattern must be at least 2 characters: \"" + substring.text() + "\"");
        final int len = (text.length() > 1)?(text.length() - 1):0;
        this.bigrams = new String[len];
        for(int i =0;i<len;i++){
            this.bigrams[i] = text.substring(i,i+2);
        }
        switch(this.bigrams.length){
        case 0:
            this.bigramQuery = null;
            break;
        case 1:
            this.bigramQuery = new TermQuery(substring.createTerm(this.bigrams[0]));
            break;
        default:
            PhraseQuery q = new PhraseQuery();
            for(String bigram:this.bigrams){
                q.add(substring.createTerm(bigram));
          }
            this.bigramQuery = q;
        }
    }

}
