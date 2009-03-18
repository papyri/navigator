package info.papyri.epiduke.lucene.bigrams;

import info.papyri.epiduke.lucene.spans.PrefixSpanTermQuery;
import info.papyri.epiduke.lucene.spans.SpanNearExclusive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.BitVector;

public class WildcardSubstringQuery extends MultiTermQuery {
    final protected Term substring;
    final String [] bigrams;
    final Query bigramQuery;
    final Searcher bgSearcher;
	final String text;
    public WildcardSubstringQuery(Term substring, Searcher bgSearcher){
        super(substring);
        this.bgSearcher = bgSearcher;
        this.substring = substring;
        String text = substring.text();
        text = text.replaceAll("[?*]{2,}", "*");
        if (text.length() < 2) throw new IllegalArgumentException("Substring pattern must be at least 2 characters: \"" + substring.text() + "\"");
        if(text.charAt(0) == '*' || text.charAt(0) == '?') text = text.substring(1);
        if (text.length() < 2) throw new IllegalArgumentException("Substring pattern must be at least 2 characters: \"" + substring.text() + "\"");
        this.text = text;
        final int len = (text.length() > 1)?(text.length() - 1):0;
        this.bigrams = new String[len];
        for(int i =0;i<len;i++){
            this.bigrams[i] = this.text.substring(i,i+2);
        }
         switch(this.bigrams.length){
        case 0:
            this.bigramQuery = null;
            break;
        case 1:
        	if(this.bigrams[0].charAt(1) == '?' || this.bigrams[0].charAt(1) == '*'){
        		this.bigramQuery = new PrefixQuery(substring.createTerm(this.bigrams[0].substring(0,1)));
        	}
        	else if(this.bigrams[0].charAt(0) == '?' || this.bigrams[0].charAt(0) == '*'){
        		this.bigramQuery = new BigramSuffixQuery(substring.createTerm(this.bigrams[0]));
        	}else this.bigramQuery = new TermQuery(substring.createTerm(this.bigrams[0]));
            break;
        default:
           ArrayList<SpanQuery> spans = new ArrayList<SpanQuery>();

           for(int i=0;i<this.bigrams.length;i++){
            	SpanQuery q;
            	
            	boolean single = bigrams[i].charAt(0) == '?';
            	boolean multi = bigrams[i].charAt(0) == '*';
                
                if (single || multi){
                	int slop = (single)?1:10;
                	SpanQuery prev = (spans.size() > 1)?new SpanNearQuery(spans.toArray(new SpanQuery[0]),0,true):spans.get(0);
                	if(i < bigrams.length - 1){
                		i++;
                		q = getBigramQuery(bigrams[i],substring);
                    	q = new SpanNearExclusive(new SpanQuery[]{prev,q},slop,true); // in case of X?X patterns
                    	spans.clear();
                	}
                	else{
                		if(bigrams[i].charAt(1) == '^'){
                        	q = new BigramSuffixQuery(substring.createTerm(bigrams[i]));
                        	q = new SpanNearQuery(new SpanQuery[]{prev,q},slop,true);
                		}
                		else{
                			q = new PrefixSpanTermQuery(substring.createTerm(bigrams[i].substring(1)));
                        	q = new SpanNearExclusive(new SpanQuery[]{prev,q},slop,true);
                		}
                	spans.clear();
                	}
                }
                else{
                	q = getBigramQuery(bigrams[i], substring);
                }

                spans.add(q);

            	
            }
            this.bigramQuery = (spans.size()==1)?spans.get(0):new SpanNearQuery(spans.toArray(new SpanQuery[0]),0,true);
        }
    }
        
        private static SpanQuery getBigramQuery(String bigram,Term t){
        	if(bigram.charAt(0) == '?') return new BigramSuffixQuery(t.createTerm(bigram));
        	if(bigram.charAt(0) == '*') return new BigramSuffixQuery(t.createTerm(bigram));
        	if(bigram.charAt(1) == '?') return new PrefixSpanTermQuery(t.createTerm(bigram.substring(0,1)));
        	if(bigram.charAt(1) == '*') return new PrefixSpanTermQuery(t.createTerm(bigram.substring(0,1)));
        	return new SpanTermQuery(t.createTerm(bigram));
        }
    @Override
    protected FilteredTermEnum getEnum(IndexReader arg0) throws IOException {
        final BitVector hits = new BitVector(bgSearcher.maxDoc());
        HitCollector hc = new HitCollector(){
            public void collect(int doc, float score){
                hits.set(doc);
            }
        };
        bgSearcher.search(bigramQuery,hc);
        final TreeSet<String> terms = new TreeSet<String>();
        for(int i = 0; i < bgSearcher.maxDoc();i++){
            if(hits.get(i)){
                terms.add(bgSearcher.doc(i).get("term"));
            }
        }
        return new FilteredTermEnum(){
           boolean endEnum = terms.size() == 0;
           Term next = (next())?term():null;
           public boolean next(){
                if(!endEnum){
                    if(terms.size() > 0){
                        String first = terms.first();
                        next = substring.createTerm(first);
                        terms.remove(first);
                        return true;
                    }
                    else endEnum = true;
                }
                return false;
            }
            public Term term(){
                return next;
            }
            public boolean endEnum(){
                return endEnum;
            }
            public boolean termCompare(Term c){
                return terms.contains(c.text());
            }
            public final float difference(){
                return 1.0f;
            }
            public void close(){
                endEnum = true;
            }
        };
    }

}
