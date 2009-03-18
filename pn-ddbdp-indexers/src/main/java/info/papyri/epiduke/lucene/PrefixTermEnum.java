package info.papyri.epiduke.lucene;

import java.io.IOException;

import java.util.Arrays;
import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.TermQuery;

public class PrefixTermEnum extends FilteredTermEnum {
    private static final Pattern DELIMITER = Pattern.compile("[\\*\\?]");

    Term searchTerm;
    String field = "";
    String text = "";
    
    boolean endEnum = false;
    public PrefixTermEnum(IndexReader reader, Term searchTerm) throws IOException {
        super();
        this.searchTerm = searchTerm;
        this.field = searchTerm.field();
        this.text = searchTerm.text();

        setEnum(reader.terms(this.searchTerm));
    }
    @Override
    public float difference() {
        return 1.0f;
    }

    @Override
    protected boolean endEnum() {
        return endEnum;
    }

    @Override
    protected boolean termCompare(Term term) {
        if (term != null &&
                term.field() == this.field){
            if(term.text().startsWith(this.text)) return true;
        }
        endEnum = true;
        return false;
    }

}
