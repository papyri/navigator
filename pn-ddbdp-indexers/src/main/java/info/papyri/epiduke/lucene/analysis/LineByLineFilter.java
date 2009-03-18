package info.papyri.epiduke.lucene.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class LineByLineFilter extends TokenFilter {

	private boolean newLine = true;
	private final TokenStream tokens;
	private final String lbToken;
	int offset = 1;
	public LineByLineFilter(String lbToken, TokenStream text){
	    super(text);
	    this.lbToken = lbToken;
	    this.tokens = text;
	}
	public Token next(Token next) throws IOException {
        if(next == null) next = new Token();
		next = tokens.next(next);
		if (next == null) return null;
		if (newLine){
			next.setPositionIncrement(offset);
			offset = 0;
			newLine = false;
		}
		else{
			next.setPositionIncrement(0);
		}
		return next;
	}
    public Token next() throws IOException {
        return next(null);
    }
}
