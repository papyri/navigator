package info.papyri.epiduke.lucene.analysis;

import java.io.IOException;
import java.util.*;
import java.io.StringReader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;

public class InPlaceAltTokenStream extends TokenStream {
    private TokenStream src;

    public InPlaceAltTokenStream(TokenStream src){
		this.src = src;
	}
    public Token next(Token t) throws IOException {
        if(t == null) t = new Token();
        Token result = src.next(t);
        if (result != null){
            if( result.termBuffer()[0] == '~'){
                result.setTermBuffer(result.termBuffer(), 1, result.termLength() - 1);
                result.setPositionIncrement(0);
            }
            if(result.termBuffer()[0]=='&' && result.termBuffer()[result.termLength()-1] == ';'){
                result.setPositionIncrement(0);
            }
        }
        return result;
    }    
	public Token next() throws IOException {
        return next(null);
	}
    
}
