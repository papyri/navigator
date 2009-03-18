package info.papyri.epiduke.lucene.bigrams;

import info.papyri.epiduke.lucene.spans.NoSpans;

import java.io.IOException;
import java.util.HashSet;
import java.util.Collection;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import info.papyri.epiduke.lucene.MultipleTermPositions;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.search.spans.TermSpans;
import org.apache.lucene.util.ToStringUtils;

public class BigramSuffixQuery extends SpanQuery {
	private final Term term;
	private final String field;
	private final char suffix;
    public BigramSuffixQuery(Term suffix){
    	this.term = suffix;
    	String text = suffix.text();
    	if(text.length() != 2) throw new IllegalArgumentException("\"" + text +"\" is not a bigram");
    	field = suffix.field();
    	this.suffix = text.charAt(1);
    }
    

	@Override
	public String getField() {
		return field;
	}

	@Override
	public Spans getSpans(IndexReader reader) throws IOException {
		Collection<String> terms = resolveTerms(reader);
		if(terms.size() == 0) return new NoSpans();
        return new TermSpans(new MultipleTermPositions(reader,terms,term.field()),term);
	}
	
	public Collection<String> resolveTerms(IndexReader reader) throws IOException {
		HashSet<String> terms = new HashSet<String>();
		TermEnum tEnum = reader.terms(term.createTerm(""));
        try{
		do{
			Term t = tEnum.term();
			if(t != null && t.text().charAt(t.text().length()- 1) == suffix)
				terms.add(t.text());
		}while(tEnum.next());
        }
        finally{
            tEnum.close();
        }
		return terms;
	}
	
    @Override
    public Query rewrite(IndexReader reader) throws IOException {
        Term [] matches = resolveTerms(reader).toArray(info.papyri.util.ArrayTypes.TERM);
        SpanTermQuery [] spans = new SpanTermQuery[matches.length];
        int i = 0;
        for(Term t:matches){
            spans[i++] = new SpanTermQuery(t);
        }
        return new SpanOrQuery(spans);
    }


	@Override
	public Collection getTerms() {
        HashSet terms = new HashSet(1);
        terms.add(term);
		return terms;
	}
	
	public void extractTerms(Set set){
		set.addAll(getTerms());
	}

	@Override
	   public String toString(String field){
        StringBuffer buffer = new StringBuffer();
        buffer.append("bigramSuffixSpan(");
        buffer.append(field);
        buffer.append(" ends with ");
        buffer.append(this.suffix);
        buffer.append(")");
        buffer.append(ToStringUtils.boost(getBoost()));
        return buffer.toString();
    }

}
