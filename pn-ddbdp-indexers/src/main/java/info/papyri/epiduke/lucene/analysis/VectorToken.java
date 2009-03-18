/**
 * 
 */
package info.papyri.epiduke.lucene.analysis;

import org.apache.lucene.analysis.*;

class VectorToken extends Token{
    VectorToken next;
    VectorToken(){
    }
    public Token getToken(Token t){
    	if(t==null) t = new Token();
    	t.setTermBuffer(this.termBuffer(), 0, this.termLength());
    	t.setTermLength(this.termLength());
    	t.setPositionIncrement(this.getPositionIncrement());
    	t.setStartOffset(this.startOffset());
    	t.setEndOffset(this.endOffset());
    	return t;
    }
    public TokenStream getTokenStream(final VectorToken last){
        final VectorToken first = this;
        return new TokenStream(){
            VectorToken next = first;
            VectorToken stop = last; 
            public Token next(){
                if(this.next != null){
                    Token result = this.next;
                    this.next = (!result.equals(stop))?this.next.next:null;
                    return result;
                }
                return null;
            }
        };
    }
}