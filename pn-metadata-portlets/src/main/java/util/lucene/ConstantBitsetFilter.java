package util.lucene;

import java.io.IOException;
import java.util.BitSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;

public class ConstantBitsetFilter extends Filter {
    private final IndexReader reader;
    private final BitSet bits;
    public ConstantBitsetFilter(IndexReader reader, BitSet bits){
        this.reader = reader;
        this.bits = bits;
    }
    public BitSet bits(IndexReader arg0) throws IOException {
        if (this.reader != null && this.reader.equals(arg0)){
            return (BitSet)this.bits.clone();
        }
        return new BitSet(0);
    }

}
