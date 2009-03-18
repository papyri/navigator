package info.papyri.epiduke.lucene.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class LineBreakFilter extends TokenFilter {

	private boolean newLine = true;
	private final TokenStream tokens;
	private final String lbToken;
	int offset = 1;
	public LineBreakFilter(String lbToken, TokenStream text){
	    super(text);
	    this.lbToken = lbToken;
	    this.tokens = text;
	}
    public Token next(Token t) throws IOException {
        if(t==null) t=new Token();
        t=tokens.next(t);
        if (t == null) return null;
        if (t.termText().equals(lbToken)){
            offset += 1;
            newLine = true;
            return next();
        }
        if (newLine){
            t.setPositionIncrement(offset);
            offset = 0;
            newLine = false;
        }
        else{
            t.setPositionIncrement(0);
        }
        return t;    }
    
	public Token next() throws IOException {
		return next(null);

	}

}
