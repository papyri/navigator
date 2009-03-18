package info.papyri.epiduke.lucene.bigrams;

import java.io.IOException;
import java.util.TreeSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BitVector;

public class SubstringQuery extends MultiTermQuery {
    final protected Term substring;

    final info.papyri.epiduke.lucene.bigrams.SubstringTermDelegate delegate;
    final String [] knownTerms;
    public SubstringQuery(Term substring, IndexSearcher bgSearcher) throws IOException {
        super(substring);
        this.substring = substring;
        this.delegate = new info.papyri.epiduke.lucene.bigrams.SubstringTermDelegate(bgSearcher,substring);
        this.knownTerms = this.delegate.matches();

    }
    @Override
    protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
        return new info.papyri.epiduke.lucene.FilteredTermEnum(this.substring,this.knownTerms,reader);
    }

}
