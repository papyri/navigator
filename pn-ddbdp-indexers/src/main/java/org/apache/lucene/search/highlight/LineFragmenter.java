package org.apache.lucene.search.highlight;

import org.apache.lucene.analysis.Token;

public class LineFragmenter implements Fragmenter {

    public boolean isNewFragment(Token arg0) {
        char [] chars = arg0.termBuffer();
        // ^&LINE-\\d+;^
        if(arg0.termLength() < 8) return false;
        int offset = (chars[0] == '^')?1:0;
        if(chars[0+offset] != '&') return false;
        if(chars[1+offset] != 'L' && chars[1+offset] != 'l' ) return false;
        if(chars[2+offset] != 'I' && chars[2+offset] != 'i') return false;
        if(chars[3+offset] != 'N' && chars[3+offset] != 'n') return false;
        if(chars[4+offset] != 'E' && chars[4+offset] != 'e') return false;
        if(chars[arg0.termLength() - (1+offset)] != ';'){
            return false;
        }
        return true;
    }

    public void start(String arg0) {
        // TODO Auto-generated method stub
        
    }

}
