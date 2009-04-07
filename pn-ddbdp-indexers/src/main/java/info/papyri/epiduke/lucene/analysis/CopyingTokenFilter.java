package info.papyri.epiduke.lucene.analysis;

import org.apache.lucene.analysis.*;
import java.util.*;
import java.io.IOException;

/**
 * This is a CachingTokenFilter that simply prevents us from having to retokenize the input text
 * It provides an independent token stream
 * @author Benjamin Armintor
 *
 */
public class CopyingTokenFilter extends CachingTokenFilter {

    private List cache;
    private Iterator iterator;

    public CopyingTokenFilter(CachingTokenFilter src) {
        super(src);
    }

    @Override
    public Token next() throws IOException {
        if (cache == null) {
            // fill cache lazily
            cache = new LinkedList();
            fillCache();
            iterator = cache.iterator();
        }

        if (!iterator.hasNext()) {
            // the cache is exhausted, return null
            return null;
        }

        return (Token) iterator.next();
    }

    @Override
    public Token next(Token t) throws IOException {
        return next();
    }

    @Override
    public void reset() throws IOException {
        if (cache != null) {
            iterator = cache.iterator();
        }
    }

    private void fillCache() throws IOException {
        Token token;
        while ((token = input.next()) != null) {
            cache.add(clone(token));
        }
    }

    private static final Token clone(Token in) {
        Token out = new Token();
        out.setTermBuffer(in.termBuffer(), 0, in.termLength());
        out.setPositionIncrement(in.getPositionIncrement());
        out.setEndOffset(in.endOffset());
        out.setStartOffset(in.startOffset());
        return out;
    }
}
