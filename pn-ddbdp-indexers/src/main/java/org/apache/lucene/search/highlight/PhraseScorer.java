package org.apache.lucene.search.highlight;

import java.util.BitSet;
import java.io.IOException;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.search.SubstringPhraseQuery;
import info.papyri.epiduke.lucene.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public class PhraseScorer implements Scorer {
    private final SubstringDelegate [] delegates;
    private final int slop;
    private final BitSet [] hits;
    private int offset = 0;
    private final float maxScore;
    public PhraseScorer(SubstringPhraseQuery query){
        this.delegates = SubstringPhraseQuery.getDelegates(query);
        maxScore = BigInteger.valueOf(2).pow(delegates.length).subtract(BigInteger.ONE).floatValue();
        this.slop = Math.max(query.getSlop(),1);
        this.hits = new BitSet[this.delegates.length];
        for(int i =0;i<hits.length;i++){
            hits[i] = new BitSet(64);
        }
    }

    public float getFragmentScore() {
                int curr = -1;
               int score = 0;

                int first = -1;
                for(int i =0;i<hits.length;i++){
                    if(hits[i].cardinality() != 0){
                        first = i;
                        break;
                    }
                }
                if(first == -1) return 0;
                score = score | (int)Math.pow(2,first);

                for(int i =first+1;i<hits.length;i++){
                    if(hits[i].cardinality() == 0) continue;

                    int prev = 0;
                    previous:
                    while ((curr = hits[i].nextSetBit(curr+1)) > -1 ){
                        prev = curr - slop;
                        if(prev < 0) continue previous;
                        if (hits[i-1].nextSetBit(prev) < curr){
                            score = score | (int)Math.pow(2,i);
                            //score.setBit(i);
                        }
                        else{
                            hits[i].set(curr,false); // to prevent false matches in subsequent patterns
                        }
                    }
                }
            return (score == maxScore)?(2*maxScore - 1):score;
            }
    
    public float maxScore(){
        return maxScore;
    }

    public float getTokenScore(Token arg0) {
        this.offset+= arg0.getPositionIncrement();
        boolean hit = false;
        try{for(int i=0;i<delegates.length;i++){
            if(delegates[i].matches(arg0.termBuffer(),0,arg0.termLength())){
                hit = true;
                if (offset >= hits[i].size()){
                    BitSet temp = new BitSet(hits[i].size() * 2);
                    temp.or(hits[i]);
                    hits[i] = temp;
                }
                hits[i].set(offset);
            }
        }
        }
        catch (IOException ioe){
            hit = false;
            System.err.print(ioe.toString());
        };
        if(hit){
            float result = BigDecimal.ONE.divide(BigDecimal.valueOf(this.delegates.length),10,BigDecimal.ROUND_UP).floatValue();
            //System.out.println(arg0.termText() + " (" + result + ")");
            return result;
        }
        else{
           // System.out.println(arg0.termText() + " (0)");
            return 0;
        }
    }

    public void startFragment(TextFragment arg0) {
        this.offset = 0; // since positions are relative, we should be able to start at 0
        for(int i =0;i<hits.length;i++){
            hits[i] = new BitSet(64);
        }
    }

}
