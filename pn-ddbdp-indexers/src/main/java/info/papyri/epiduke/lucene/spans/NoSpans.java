package info.papyri.epiduke.lucene.spans;

import java.io.IOException;

import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.index.Term;
public class NoSpans implements Spans {
    public NoSpans(){
    }
    public int doc() {
        return Integer.MAX_VALUE;
    }

    public int end() {
        return 0;
    }

    public boolean next() throws IOException {
        return false;
    }

    public boolean skipTo(int arg0) throws IOException {
        return false;
    }

    public int start() {
        return 0;
    }

}
