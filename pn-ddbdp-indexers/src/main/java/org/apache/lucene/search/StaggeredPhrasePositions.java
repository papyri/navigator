package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.TermPositions;
public class StaggeredPhrasePositions {
    int doc;                      // current doc
    int position;                     // position in doc
    int count;                    // remaining pos in this doc
    int offset;                   // position in phrase
    int slop;                   // max distance from previous in phrase
    TermPositions tp;                 // stream of positions
    StaggeredPhrasePositions prev; //, next;
    // used to make lists
    boolean repeats;       // there's other pp for same term (e.g. query="1st word 2nd word"~1) 

    StaggeredPhrasePositions(TermPositions t, int o, int s) {
      tp = t;
      offset = o;
      slop = s;
    }

    final boolean next() throws IOException {     // increments to next doc
      if (!tp.next()) {
        tp.close();               // close stream
        doc = Integer.MAX_VALUE;              // sentinel value
        return false;
      }
      doc = tp.doc();
      position = 0;
      return true;
    }

    final boolean skipTo(int target) throws IOException {
      if (!tp.skipTo(target)) {
        tp.close();               // close stream
        doc = Integer.MAX_VALUE;              // sentinel value
        return false;
      }
      doc = tp.doc();
      position = 0;
      return true;
    }


    final void firstPosition() throws IOException {
      count = tp.freq();                  // read first pos
      nextPosition();
    }

    /**
     * Go to next location of this term current document, and set 
     * <code>position</code> as <code>location - offset</code>, so that a 
     * matching exact phrase is easily identified when all PhrasePositions 
     * have exactly the same <code>position</code>.
     */
    final boolean nextPosition() throws IOException {
      if (count-- > 0) {                  // read subsequent pos's
        position = tp.nextPosition() - offset;
        return true;
      } else
        return false;
    }
  }