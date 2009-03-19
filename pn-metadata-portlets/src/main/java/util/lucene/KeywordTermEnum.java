package util.lucene;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;

public class KeywordTermEnum extends FilteredTermEnum {

    @Override
    public float difference() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected boolean endEnum() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean termCompare(Term arg0) {
        // TODO Auto-generated method stub
        return false;
    }

}
