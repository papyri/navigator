package info.papyri.epiduke.lucene.analysis;

import java.io.IOException;
import java.util.*;
import java.io.StringReader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;

public class SubstringRotationTokenStream extends TokenStream {
    private TokenStream src;
    private Iterator<Token> tokens;

	public SubstringRotationTokenStream(TokenStream src){
		this.src = src;
	}

    private Token[] getRotation(Token token){
        if (token.termText().length() == 0) return new Token[0];
        String src = "$" + token.termText() + "$";
        
        String [] rotations = new String[src.length()];
        rotations[0] = src;
        rotations[1] = "$$" + token.termText() ;
        for (int i = 2; i < rotations.length; i++){
            String prev = rotations[i - 1];
            int pLen = prev.length();
            rotations[i] = prev.substring(pLen - 1) + prev.substring(0,pLen - 1);
        }
        Token [] result = new Token[rotations.length];
        for (int i = 0; i< result.length; i++){
            result[i] = new Token(rotations[i],token.startOffset(),token.endOffset());
            if (i == 0){
                result[i].setPositionIncrement(token.getPositionIncrement());
            }
            else {
                result[i].setPositionIncrement(0);
            }
        }
        return result;
        
    }
    
	public Token next() throws IOException {

        Token srcNext = null;
        while ((tokens == null || !tokens.hasNext()) && ((srcNext = src.next()) != null)){
            Token [] rotation = getRotation(srcNext);
            tokens = Arrays.asList(rotation).iterator();
        }
        
        if (tokens == null) return null;
        
        if (tokens.hasNext()){
            return tokens.next();
        }

        return null;
	}

}
