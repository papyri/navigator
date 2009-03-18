package info.papyri.epiduke.lucene.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class AncientGreekAccentFilter extends TokenFilter {

    public AncientGreekAccentFilter(TokenStream in){
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
        int len = t.termLength();
        int ignore = 0;
        int include = 0;
        char [] newArray = new char[len];
        for (int i = 0; i < len; i++)
        {
            char c = AncientGreekCharsets.toUnaccented(chArray[i]);
            if (c > '\u0000'){
                newArray[include++] = c;
            }
            else newArray[newArray.length - (++ignore)] = c;
            //chArray[i] = AncientGreekCharsets.toUnaccented(chArray[i]);
        }
        if(include > len){
            t.resizeTermBuffer(include);
            System.arraycopy(newArray,0,t.termBuffer(),0,include);
        }
        else{
            if(include==0){
                System.err.print("stripped all" + len + " characters:\n\t");
                for(int i=0;i<len;i++)  System.err.print(Integer.toHexString(chArray[i]) + " , ");
            }
            System.arraycopy(newArray,0,t.termBuffer(),0,include);
            t.setTermLength(include);
        }
        return t;
        }
    @Override
    public Token next() throws IOException {
        return next(null);
    }
}
