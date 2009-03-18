package org.apache.lucene.search.highlight;

import java.util.BitSet;
import java.io.IOException;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.search.SubstringPhraseQuery;
import info.papyri.epiduke.lucene.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public class SubstringScorer implements Scorer {
    private final SubstringDelegate [] delegates;
    private int score = 0;
    public SubstringScorer(SubstringQuery query){
        this.delegates =SubstringQuery.getDelegates(query);
    }

    public float getFragmentScore() {
               return score;
            }
    
    public float getTokenScore(Token arg0) {
        int ctr = 0;
        try{for(int i=0;i<delegates.length;i++){
            if(delegates[i].matches(arg0.termBuffer(),0,arg0.termLength())){
                ctr++;
            }
        }
        }
        catch (IOException ioe){
            System.err.print(ioe.toString());
        };
        score += ctr;
        return ctr;
    }

    public void startFragment(TextFragment arg0) {
        this.score = 0; // since positions are relative, we should be able to start at 0
    }

}
