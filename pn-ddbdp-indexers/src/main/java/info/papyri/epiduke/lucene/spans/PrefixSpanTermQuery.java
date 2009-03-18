package info.papyri.epiduke.lucene.spans;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import info.papyri.epiduke.lucene.MultipleTermPositions;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.TermSpans;
import org.apache.lucene.util.ToStringUtils;
import org.apache.lucene.index.TermEnum;
import java.util.HashSet;
import java.util.Collection;
import java.util.Set;
public class PrefixSpanTermQuery extends SpanQuery {
	@Override
	public Collection getTerms() {
        HashSet result = new HashSet();
		result.add(this.term);
		return result;
	}
	public void extractTerms(Set set){
        if(rewritten){
            for(String t:rewrites) set.add(term.createTerm(t));
            return;
        }
		set.addAll(getTerms());
	}

	private final Term term;
    private final boolean rewritten;
    private final String [] rewrites;
    private String[] resolveTerms(IndexReader reader) throws IOException {
         String[] terms = new String[64];
         int ix = 0;
         TermEnum tEnum = reader.terms(this.term);
         String field = this.term.field();
         String text = this.term.text();
         do{
        	 Term t = tEnum.term();
        	 if(t != null &&
        			 t.field().equals(field) &&
        			 t.text().startsWith(text)){
        		 if(ix==terms.length){
                   String [] newTerms = new String[terms.length *2];
                   System.arraycopy(terms, 0, newTerms, 0, terms.length);
                   terms = newTerms;
                 }
                 terms[ix++] = t.text();
        	 }
        	 else break;
         }while(tEnum.next());
         String [] result = new String[ix];
         System.arraycopy(terms, 0, result, 0, ix);
         return result;
    }
    public PrefixSpanTermQuery(Term term){
        this.term = term;
        this.rewritten = false;
        this.rewrites = null;
     }
    
    public PrefixSpanTermQuery(Term term, String[] rewrites){
        this.term = term;
        this.rewritten = true;
        this.rewrites = rewrites;
     }
    
    public String getField(){
    	return this.term.field();
    }
    
    public String toString(String field){
        StringBuffer buffer = new StringBuffer();
        buffer.append("prefixSpan(");
        buffer.append(field);
        buffer.append(" starts with ");
        buffer.append(this.term.text());
        buffer.append(")");
        buffer.append(ToStringUtils.boost(getBoost()));
        return buffer.toString();
    }
    
    public Spans getSpans(final IndexReader reader) throws IOException {
        String [] matches = (rewritten)?rewrites:resolveTerms(reader);
        if(matches.length == 0){
            return new NoSpans(); 
        }
         return new TermSpans(new MultipleTermPositions(reader,matches,term.field()),term);
    }

    @Override
    public Query rewrite(IndexReader reader) throws IOException {
        if(rewritten) return this;
        String [] matches = resolveTerms(reader);
        return new PrefixSpanTermQuery(term,matches);
    }
}
