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

public class SubstringTermEnum extends FilteredTermEnum {
    private static final Pattern DELIMITER = Pattern.compile("[\\*\\?]");

    Term searchTerm;
    String field = "";
    String text = "";
    private char [] textChars;
    private final short[] baseShifts;
    private final short[] extShifts;
    private final short[] latinShifts;
    private short[] gs;
    private short def;
    private short last;

    
    int preLen = 0;
    boolean endEnum = true;
    public SubstringTermEnum(){
        super();
        baseShifts = new short[256];
        extShifts = new short[256];
        latinShifts = new short[256];
    }
    public SubstringTermEnum(IndexReader reader, Term searchTerm) throws IOException {
        this();
        resetToTerm(reader, searchTerm);
    }
    @Override
    public float difference() {
        return 1.0f;
    }
    
    public void resetToTerm(IndexReader reader, Term searchTerm) throws IOException {
        this.searchTerm = searchTerm;
        this.field = searchTerm.field();
        this.text = searchTerm.text();
        textChars = this.text.toCharArray();
        gs = SubstringQuery.getGSShiftArray(textChars);
        def = (short)(textChars.length);
        last = (short)(textChars.length - 1);
        Arrays.fill(baseShifts,def);
        Arrays.fill(extShifts, def);
        Arrays.fill(latinShifts, def);

        SubstringQuery.initBCShiftMaps(textChars,baseShifts,extShifts,latinShifts);
        TermEnum base = reader.terms(this.searchTerm.createTerm(""));
        setEnum(base);
        endEnum = false;
    }

    @Override
    protected boolean endEnum() {
        return endEnum;
    }

    @Override
    protected boolean termCompare(Term term) {
        if (term != null &&
                term.field() == this.field){ 
                return SubstringQuery.matchShanghai(baseShifts, extShifts, latinShifts, gs, textChars,term.text().toCharArray(), last, def);
        }
        endEnum = true;
        return false;
    }

}
