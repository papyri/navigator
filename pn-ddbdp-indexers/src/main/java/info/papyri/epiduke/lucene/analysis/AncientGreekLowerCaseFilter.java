package info.papyri.epiduke.lucene.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class AncientGreekLowerCaseFilter extends TokenFilter {

    public AncientGreekLowerCaseFilter(TokenStream in){
        super(in);
    }
    @Override
    public Token next(Token t) throws IOException {
        if (t==null) t = new Token();
         t = input.next(t);

        if (t == null)
            return null;
        if(t.termLength()==0) return t;
        char[] chArray = t.termBuffer();
        if(chArray[0] == '&' && chArray[t.termLength() - 1] == ';') return t; // ignore entities

        for (int i = 0; i < chArray.length; i++)
        {
            chArray[i] = AncientGreekCharsets.toLowerCase(chArray[i]);
        }
        return t;
        }
    
    public Token next() throws IOException{
        return next(null);
    }

}
