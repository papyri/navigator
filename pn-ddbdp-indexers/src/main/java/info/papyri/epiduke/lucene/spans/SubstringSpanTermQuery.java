package info.papyri.epiduke.lucene.spans;
import info.papyri.epiduke.lucene.bigrams.SubstringTermDelegate;
import info.papyri.epiduke.lucene.bigrams.WildcardSubstringDelegate;
import info.papyri.epiduke.lucene.MultipleTermPositions;
import java.io.IOException;
import java.util.HashSet;
import java.util.Collection;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermTextSwap;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.search.spans.TermSpans;
import org.apache.lucene.util.ToStringUtils;
public class SubstringSpanTermQuery extends SpanQuery {
    private final SubstringTermDelegate delegate;

    protected String[] rewrites = null;
    private final boolean rewritten;
    protected final Term term;

    public SubstringSpanTermQuery(Term term, IndexSearcher bigrams) throws IOException {
    	this.term = term;
        if (term.text().indexOf('?') != -1 || term.text().indexOf('*') != -1){
            delegate =  new WildcardSubstringDelegate(bigrams,term);
         }
        else{
            delegate = new SubstringTermDelegate(bigrams,term);
        }
        rewrites = delegate.matches();
        java.util.Arrays.sort(rewrites);
        this.rewritten = false;
    }
    private SubstringSpanTermQuery(Term term, String [] terms){
        this.term = term;
        this.rewrites = terms;
        this.delegate = null;
        this.rewritten = true;
    }
    
    public String getField(){
        return this.term.field();
    }
    
    public String toString(String field){
        StringBuffer buffer = new StringBuffer();
        buffer.append("substringSpan(");
        buffer.append(field);
        buffer.append(" includes ");
        buffer.append(this.term.text());
        if(rewritten) buffer.append(", rewritten");
        buffer.append(")");
        buffer.append(ToStringUtils.boost(getBoost()));
        return buffer.toString();
    }
    
    public Spans getSpans(final IndexReader reader) throws IOException {
        if(rewrites.length == 0){
            return new NoSpans(); 
        }
         return new TermSpans(new MultipleTermPositions(reader,rewrites,term.field()),term);
    }

    @Override
    public Query rewrite(IndexReader reader) throws IOException {
        if(rewritten) return this;
        if(rewrites.length == 0) return this;
        Term nextTerm = term.createTerm(rewrites[0]);
        TermEnum tEnum = reader.terms(nextTerm);

        int [] tPtrs = new int[64];
        int ix = 0;
        int nextMatch = 0;
        try{
        do{
            Term t = tEnum.term();
            if(t != null && t.field().equals(term.field())){
                int compare = 0;
                String tt = t.text();
                while((compare = tt.compareTo(rewrites[nextMatch])) > 0 ){
                    nextMatch++;
                    if(nextMatch >= rewrites.length) break;    
                }
                if(compare == 0){
                    if(ix == tPtrs.length){
                        int [] newTerms = new int[tPtrs.length*2];
                        System.arraycopy(tPtrs, 0, newTerms, 0, tPtrs.length);
                        tPtrs = newTerms;
                    }
                    tPtrs[ix++] = (nextMatch);
                    nextMatch++;
                }
            } else break;
            
        }while(nextMatch< rewrites.length && tEnum.skipTo(TermTextSwap.swapText(nextTerm,rewrites[nextMatch])));
        } finally {
        tEnum.close();
        }
        
        if(ix==0) return new SubstringSpanTermQuery(term,info.papyri.util.ArrayTypes.STRING);
        String [] result = new String[ix];
        ix = 0;
        for(int i:tPtrs){
            if(ix==result.length) break;
            result[ix++] = rewrites[i];
        }
        return new SubstringSpanTermQuery(term, result);
    }

    @Override
    public Collection getTerms() {
        java.util.HashSet terms = new java.util.HashSet();
        extractTerms(terms);
        return terms;
    }
    
    @Override
    public void extractTerms(Set terms) {
        if(!rewritten){
            terms.add(term);
        } else {
            for(String t:rewrites){
                terms.add(term.createTerm(t));
            }
        }
    }
}
