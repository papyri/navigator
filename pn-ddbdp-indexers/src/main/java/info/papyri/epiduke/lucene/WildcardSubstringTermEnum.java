package info.papyri.epiduke.lucene;

import java.io.IOException;

import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.TermQuery;

public class WildcardSubstringTermEnum extends FilteredTermEnum {
    private static final Pattern DELIMITER = Pattern.compile("[\\*\\?]");

    Term searchTerm;
    String field = "";
    String text = "";

    private final WildcardSegmentDelegate[] delegates;
    private final String [] patterns;
    private final char [] delimiters;

    
    int preLen = 0;
    boolean endEnum = false;
    public WildcardSubstringTermEnum(IndexReader reader, Term searchTerm) throws IOException {
        super();
        this.searchTerm = searchTerm;
        this.field = searchTerm.field();
        this.text = searchTerm.text();
        
        text = text.replaceAll("[?*]{2,}", "*");

        patterns = text.split("[?*]");

        delimiters = new char[patterns.length - 1];
        delegates = new WildcardSegmentDelegate[patterns.length];
        int i;
        for (i=0;i<patterns.length;i++){
            delegates[i] = new WildcardSegmentDelegate(patterns[i]);
        }
        i = 0;
        Matcher matcher = DELIMITER.matcher(text);
        while (i<delimiters.length){
            matcher.find();
            delimiters[i] = matcher.group().charAt(0);
            i++;
        }

        setEnum(reader.terms(this.searchTerm.createTerm("")));
    }
    @Override
    public float difference() {
        // TODO Auto-generated method stub
        return 1.0f;
    }

    @Override
    protected boolean endEnum() {
        // TODO Auto-generated method stub
        return endEnum;
    }

    @Override
    protected boolean termCompare(Term term) {
        if (term != null &&
                term.field() == this.field){ 

                String text = term.text();
                try{
                BitSet[] matches = new BitSet[delegates.length];
                matches[0] = delegates[0].offsets(text);
                if (matches[0].cardinality() == 0){
                    return false;
                }

                termParts:
                for (int i=1;i<delegates.length;i++){
                    matches[i] = delegates[i].offsets(text);
                    if (matches[i].cardinality() == 0) {
                        return false;
                    }
                    int curr = -1;
                    boolean checked =false;
                    checkPrior:
                    while ((curr = matches[i - 1].nextSetBit(curr+1)) > -1 ){
                        if (matches[i].nextSetBit(curr + patterns[i-1].length()) > -1){
                            checked = true;
                            break;
                        }
                    }
                    if (!checked){
                        return false;
                    }

                    if (delimiters[i-1] == '?'){
                        curr = patterns[i -1].length() - 1;
                        int prev = 0;
                        checked = false;
                        checkZeroOrOne:
                        while ((curr = matches[i].nextSetBit(curr+1)) > -1 ){
                            prev = curr - patterns[i -1].length();
                            if (prev < 0) continue checkZeroOrOne;
                            if (matches[i-1].get(prev) || (prev > 0 && matches[i-1].get(prev - 1))){
                                checked = true;
                            }
                            else{
                                matches[i].set(curr,false); // to prevent false matches in subsequent patterns
                            }
                        }
                        if (!checked){
                            return false;
                        }
                    }
                    }
                }
                catch (IOException ioe){
                    ioe.printStackTrace();
                    return false;
                }
                return true;
        }
        endEnum = true;
        return false;
    }

}
