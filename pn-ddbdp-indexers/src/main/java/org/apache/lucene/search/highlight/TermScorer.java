package org.apache.lucene.search.highlight;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.PrefixQuery;

import info.papyri.epiduke.lucene.SubstringQuery;

public abstract class TermScorer implements Scorer {
    private float score = 0;

    public float getFragmentScore() {
        return score;
    }

    public float getTokenScore(Token arg0) {
        if (matches(arg0)){
            score++;
            return 1;
        }
        return 0;
    }

    public void startFragment(TextFragment arg0) {
        score = 0;
    }
    
   abstract public boolean matches(Token token);
   
   public static TermScorer getTermScorer(Query query){
       if (query instanceof SubstringQuery) return new SubstringTermScorer((SubstringQuery)query);
       else  if (query instanceof PrefixQuery) return new PrefixScorer((PrefixQuery)query);
       else return new PrefixScorer((TermQuery)query);
   }
}
