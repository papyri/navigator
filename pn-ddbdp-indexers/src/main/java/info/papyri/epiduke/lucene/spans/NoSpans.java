package info.papyri.epiduke.lucene.spans;

import java.io.IOException;

import java.util.Collection;
import org.apache.lucene.search.spans.PayloadSpans;
public class NoSpans implements PayloadSpans {
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

  public Collection getPayload() throws IOException {
    throw new UnsupportedOperationException("Not supported.");
  }

  public boolean isPayloadAvailable() {
    return false;
  }

}
