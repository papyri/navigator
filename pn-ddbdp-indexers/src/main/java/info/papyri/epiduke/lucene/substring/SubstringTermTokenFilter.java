package info.papyri.epiduke.lucene.substring;

import java.util.ArrayList;
import java.util.Iterator;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.PhraseQuery;

import info.papyri.epiduke.lucene.*;

public class SubstringTermTokenFilter extends TokenFilter {
    final SubstringDelegate[] delegates;
    private Iterator<Token> tokens;
    private TokenStream src;
    public SubstringTermTokenFilter(TokenStream tokens, SubstringDelegate[] delegates){
        super(tokens);
        this.src = tokens;
        this.delegates = delegates;
    }
    
    public Token next() throws IOException {
        Token srcNext = null;
        while ((tokens == null || !tokens.hasNext()) && ((srcNext = src.next()) != null)){
            tokens = getRotation(srcNext);
        }
        
        if (tokens == null) return null;
        
        if (tokens.hasNext()){
            return tokens.next();
        }

        return null;
    }
    
    private Iterator<Token> getRotation(Token t) throws IOException{
        ArrayList<Token> tokens = new ArrayList<Token>();
        boolean matched = false;
        Token match;
        for (SubstringDelegate del:delegates){
            if (del.matches(t.termBuffer(),0,t.termLength())){
            match = new Token(del.substringTerm().text(),t.startOffset(),t.endOffset());
            if (tokens.size() == 0){
                match.setPositionIncrement(t.getPositionIncrement());
            }
            else {
                match.setPositionIncrement(0);
            }
            matched = true;
            tokens.add(match);
            }
        }
        if (!matched){
            char [] fc = new char[t.endOffset() - t.startOffset()];
            java.util.Arrays.fill(fc, 'X');
            Token fail = new Token(new String(fc),t.startOffset(),t.endOffset());
            fail.setPositionIncrement((t.getPositionIncrement()));
            tokens.add(t);
        }
        return tokens.iterator();
        
    }
}
