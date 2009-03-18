package info.papyri.epiduke.lucene.analysis;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

public class AnchoredTokenStream extends TokenStream {
    private TokenStream src;
    private String name;
    private boolean debug = false;
    public static final char ANCHOR = '^';
    public static final String ANCHOR_STR = "^";

    public AnchoredTokenStream(TokenStream src){
        this(null,src);
    }
    public AnchoredTokenStream(TokenStream src, boolean debug){
        this(null,src);
        this.debug = debug;
    }
    public AnchoredTokenStream(String name, TokenStream src){
        this.src = src;
        this.name = name;
    }
    
    @Override
    public Token next() throws IOException {
        return next(null);
    }
    @Override
    public Token next(Token arg0) throws IOException {
        if (arg0 == null) arg0 = new Token();
       arg0 = src.next(arg0);
        if (arg0 != null){
            int len = arg0.termLength();
            if(len == 0) return arg0;
            
            char [] tt = arg0.termBuffer();
            if (len > 1 && tt[0] == '~'){
                if (len+1 > tt.length){
                    arg0.resizeTermBuffer(len + 1);
                    tt = arg0.termBuffer();
                }
                arg0.setPositionIncrement(0); // if this was the first term of a line, we will have a problem
                tt[len] = '^';
                tt[0] = '^';
                arg0.setTermLength(len+1);
            }
            else{
                if (len+2 > tt.length) {
                    arg0.resizeTermBuffer(len+2);
                    tt = arg0.termBuffer();
                }
                
                System.arraycopy(tt,0,tt,1,len);
                
                tt[len+1] = '^';
                tt[0] = '^';
                arg0.setTermLength(len+2);
            }
        }
        return arg0;
    }

}
