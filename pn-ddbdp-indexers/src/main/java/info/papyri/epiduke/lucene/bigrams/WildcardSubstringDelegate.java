package info.papyri.epiduke.lucene.bigrams;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.*;

import info.papyri.epiduke.lucene.spans.PrefixSpanTermQuery;
import info.papyri.epiduke.lucene.spans.SpanSequenceQuery;

import java.util.regex.Pattern;
import java.io.IOException;
import java.util.ArrayList;

public class WildcardSubstringDelegate extends SubstringTermDelegate {

    private Pattern pattern;

    public WildcardSubstringDelegate(IndexSearcher bgSearcher) {
        super(bgSearcher);
    }

    public WildcardSubstringDelegate(IndexSearcher bgSearcher, Term substring) {
        super(bgSearcher, substring);
    }

    @Override
    public void setTerm(Term substring) {
        this.substring = substring;
        this.text = substring.text();
        this.pattern = Pattern.compile(text.replaceAll("[\\^]", "[\\\\^]"));
        this.text = text.replaceAll("[?*]{2,}", "*");
        if (text.length() < 2) {
            throw new IllegalArgumentException("Substring pattern must be at least 2 characters: \"" + substring.text() + "\"");
        }
        if (text.charAt(0) == '*' || text.charAt(0) == '?') {
            text = text.substring(1);
        }
        if (text.length() < 2) {
            throw new IllegalArgumentException("Substring pattern must be at least 2 characters: \"" + substring.text() + "\"");
        }
        final int len = (text.length() > 1) ? (text.length() - 1) : 0;
        this.bigrams = new String[len];
        for (int i = 0; i < len; i++) {
            this.bigrams[i] = text.substring(i, i + 2);
        }
        switch (this.bigrams.length) {
            case 0:
                this.bigramQuery = null;
                break;
            case 1:
                if (this.bigrams[0].charAt(1) == '?' || this.bigrams[0].charAt(1) == '*') {
                    this.bigramQuery = new PrefixQuery(substring.createTerm(this.bigrams[0].substring(0, 1)));
                } else if (this.bigrams[0].charAt(0) == '?' || this.bigrams[0].charAt(0) == '*') {
                    this.bigramQuery = new BigramSuffixQuery(substring.createTerm(this.bigrams[0]));
                } else {
                    this.bigramQuery = new TermQuery(substring.createTerm(this.bigrams[0]));
                }
                break;
            default:
                ArrayList<SpanQuery> spans = new ArrayList<SpanQuery>();

                for (int i = 0; i < this.bigrams.length; i++) {
                    SpanQuery q;

                    boolean single = bigrams[i].charAt(0) == '?';
                    boolean multi = bigrams[i].charAt(0) == '*';

                    if (single || multi) {
                        int slop = (single) ? 1 : 10;
                        SpanQuery prev = (spans.size() > 1) ? new SpanNearQuery(spans.toArray(new SpanQuery[0]), slop, false) : spans.get(0);
                        if (i < bigrams.length - 1) {
                            i++;
                            q = getBigramQuery(bigrams[i], substring);
                            q = new SpanNearQuery(new SpanQuery[]{prev, q}, slop, true); // in case of X?X patterns
                            spans.clear();
                        } else {
                            if (bigrams[i].charAt(1) == '^') {
                                q = new BigramSuffixQuery(substring.createTerm(bigrams[i]));
                                q = new SpanNearQuery(new SpanQuery[]{prev, q}, slop, true);
                            } else {
                                q = new PrefixSpanTermQuery(substring.createTerm(bigrams[i].substring(1)));
                                q = new SpanNearQuery(new SpanQuery[]{prev, q}, slop, true);
                            }
                            spans.clear();
                        }
                    } else {
                        q = getBigramQuery(bigrams[i], substring);
                    }

                    spans.add(q);
                }
                this.bigramQuery = (spans.size() == 1) ? spans.get(0) : new SpanSequenceQuery(spans.toArray(new SpanQuery[0]));
        }
    }

    private static SpanQuery getBigramQuery(String bigram, Term t) {
        if (bigram.charAt(0) == '?') {
            return new BigramSuffixQuery(t.createTerm(bigram));
        }
        if (bigram.charAt(0) == '*') {
            return new BigramSuffixQuery(t.createTerm(bigram));
        }
        if (bigram.charAt(1) == '?') {
            return new PrefixSpanTermQuery(t.createTerm(bigram.substring(0, 1)));
        }
        if (bigram.charAt(1) == '*') {
            return new PrefixSpanTermQuery(t.createTerm(bigram.substring(0, 1)));
        }
        return new SpanTermQuery(t.createTerm(bigram));
    }

    @Override
    public boolean matches(String input) throws IOException {
        return pattern.matcher(input).find();
    }
}
