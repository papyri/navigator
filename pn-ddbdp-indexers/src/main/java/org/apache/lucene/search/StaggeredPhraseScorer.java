package org.apache.lucene.search;

import java.io.IOException;
import org.apache.lucene.index.TermPositions;

import org.apache.lucene.util.PriorityQueue;

/**
 * Work-in-progress phrase scorer with term-specific slop.  the generic slop is zero.
 * @author Ben
 *
 */
public class StaggeredPhraseScorer extends Scorer {
    private Weight weight;
    protected byte[] norms;
    protected float value;

    private boolean firstTime = true;
    private boolean more = true;
    protected StaggeredPhrasePositions last, first;
    private final int minSpan;
    private final int maxSpan;
    private float freq;

    private PositionsQueue oq;
    
    public StaggeredPhraseScorer(Weight weight, TermPositions [] tps, int[] offsets, int[] slops, Similarity similarity, byte [] norms) throws IOException {
        super(similarity);
        this.norms = norms;
        this.weight = weight;
        this.value = weight.getValue();
        this.oq = new PositionsQueue(tps.length);
        this.minSpan = tps.length;
        int tSlop = 0;
        for (int i = tps.length - 1; i >= 0; i--) {
          StaggeredPhrasePositions pp = new StaggeredPhrasePositions(tps[i], offsets[i],slops[i]);
          oq.put(pp);
          tSlop += slops[i];
        }
        maxSpan = tSlop;
        oqToList();
    }

    private void oqToList(){
        last = first = null;
        while (oq.top() != null) {
            StaggeredPhrasePositions pp = (StaggeredPhrasePositions) oq.pop();
          if (last != null) {             // add next to end of list
              pp.prev = last;
              last = pp;
          } else{
              pp.prev = null;
        	  last = pp;
              first = pp;
          }
        }
    }
    
    protected final float phraseFreq() throws IOException {
        for(StaggeredPhrasePositions pp = last; pp != null; pp = pp.prev) pp.firstPosition();
        do{ // shortcut  end term positions to first possible phrase match
            while(last.position < first.position){
                if(!last.nextPosition()){
                    return 0;
                }
            }
        }while(last.position > (first.position + maxSpan) && first.nextPosition());

        // we should only have to sort once, at init

        int pFreq = 0;
        StaggeredPhrasePositions check;

        do{
            check = last;
            do{
                if(check.prev == null){
                    pFreq++;
                    break;
                }
                    while(check.position > check.prev.position + check.slop){
                        if(!check.prev.nextPosition()) return (float)pFreq;
                    }
            }while(check.position >= check.prev.position && (check = check.prev) != null);

        }while(last.nextPosition());
        //if (freq < 1)         System.out.println("went through process for a dud");                
        return (float)pFreq;
    }
    
    public int doc() { return first.doc; }

    public boolean next() throws IOException {
      if (firstTime) {
        init();
        firstTime = false;
      } else if (more) {
        more = last.next();                         // trigger further scanning
      }
      return doNext();
    }
    
    // next without initial increment
    private boolean doNext() throws IOException {
        while (more ) {      // find doc w/ all the terms
            int doc = first.doc;
            boolean changed = true;
            while (more && changed) {
                StaggeredPhrasePositions pp;
                changed = false;
                for( pp = last; more && pp != null; pp = pp.prev){
                    if(pp.doc< doc){
                        if(!pp.skipTo(doc)) return (more = false);
                        changed = true;
                    }
                    if( pp.doc > doc){
                        doc = pp.doc;
                        if((last.doc < doc)){
                            if(!last.skipTo(doc)) return (more = false);
                            changed = true;
                        }
                        if(last.doc > doc) doc = last.doc;
                    }
                }
            }

            if (more) {
                // found a doc with all of the terms
                freq = phraseFreq();                      // check for phrase
                if (freq == 0.0f){                         // no match
                    more = last.next();                     // trigger further scanning
                } else
                    return true;                            // found a match
            }
        }
        return false;                                 // no more matches
    }

    public float score() throws IOException {
      //System.out.println("scoring " + first.doc);
      float raw = getSimilarity().tf(freq) * value; // raw score
      return raw * Similarity.decodeNorm(norms[first.doc]); // normalize
    }

    public boolean skipTo(int target) throws IOException {
      firstTime = false;
      for (StaggeredPhrasePositions pp = last; more && pp != null; pp = pp.prev) {
        if(!pp.skipTo(target)) return (more = false);
      }
      if (more && false)
        sort(false);                                     // re-sort
      return doNext();
    }
    
    private void init() throws IOException {
        for (StaggeredPhrasePositions pp = last; more && pp != null; pp = pp.prev) {
            more = more && pp.next();
        }
        if(more && false)
          sort(false);
      }
      
      private void sort(boolean advance) throws IOException {
       oq.clear();
        for (StaggeredPhrasePositions pp = last; pp != null; pp = pp.prev){
            if(advance) pp.firstPosition();
            oq.put(pp);
        }
        oqToList();
      }

        public Explanation explain(final int doc) throws IOException {
          Explanation tfExplanation = new Explanation();

          while (doc() < doc && next()) {}

          float phraseFreq = (doc() == doc) ? freq : 0.0f;
          tfExplanation.setValue(getSimilarity().tf(phraseFreq));
          tfExplanation.setDescription("tf(phraseFreq=" + phraseFreq + ")");

          return tfExplanation;
        }

        public String toString() { return "scorer(" + weight + ")"; }
        
    static class PositionsQueue extends PriorityQueue {
        PositionsQueue(int max){
            initialize(max);
        }
        @Override
        protected boolean lessThan(Object arg0, Object arg1) {
            StaggeredPhrasePositions pp1 = ((StaggeredPhrasePositions)arg0);
            StaggeredPhrasePositions pp2 = ((StaggeredPhrasePositions)arg1);

            if (pp1.offset == pp2.offset) 
              if (pp1.position == pp2.position)
                return pp1.doc < pp2.doc;
              else
                return pp1.position < pp2.position; // should be sorted in offset order!
            else
              return pp1.offset < pp2.offset;
          }
        }


}