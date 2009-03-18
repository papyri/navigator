package info.papyri.epiduke.lucene;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;

public interface SubstringDelegate {
    abstract boolean matches(String text) throws IOException;
    abstract boolean matches(char[]  text, int start, int len) throws IOException;
    abstract String [] matches(IndexReader reader) throws IOException;
    abstract Term substringTerm();
}
