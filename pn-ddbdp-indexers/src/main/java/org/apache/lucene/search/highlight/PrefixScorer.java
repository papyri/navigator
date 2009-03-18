package org.apache.lucene.search.highlight;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.TermScorer;
public class PrefixScorer extends TermScorer {
    char [] query;
    public PrefixScorer(TermQuery query){
        this.query = query.getTerm().text().toCharArray();
    }
    public PrefixScorer(PrefixQuery query){
        this.query = query.getPrefix().text().toCharArray();
    }
    @Override
    public boolean matches(Token token) {
        int len = token.termLength();
        if (len < query.length) return false;
        char [] tt = token.termBuffer();
        for(int i=0;i<query.length;i++){
            if(query[i] != tt[i]) return false;
        }
        return true;
    }

}
