package org.apache.lucene.search.highlight;

import java.io.IOException;
import org.apache.lucene.analysis.Token;
import info.papyri.epiduke.lucene.*;
public class SubstringTermScorer extends TermScorer {
    SubstringDelegate [] dels;
    public SubstringTermScorer(SubstringQuery query){
        dels = SubstringQuery.getDelegates(query);
    }
    @Override
    public boolean matches(Token token) {
        try{
            for(SubstringDelegate del:dels){
                if (!del.matches(token.termBuffer(), 0, token.termLength())) return false;
            }
            return true;
        }catch (IOException ioe){
            return false;
        }
    }

}
