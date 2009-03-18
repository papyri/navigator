/**
 * 
 */
package org.apache.lucene.search.highlight;
import org.apache.lucene.analysis.Token;
public class BuilderTextFragment extends TextFragment{
    Token first = null;
    Token last = null;
    @Override
    public void merge(TextFragment frag2) {
        textEndPos = frag2.textEndPos;
        score = Math.max(score, frag2.score);
        this.first = null; // start acting like a generic TextFragment
        this.last = null;
    }
    public void merge(BuilderTextFragment frag2, float score) {
        textEndPos = frag2.textEndPos;
        boolean merged = false;
        if(this.last != null){
            if(frag2.first != null){
                //this.last.next = frag2.first;
                this.last = frag2.last;
                this.score = score;
                merged = true;
            }
        }
        if(!merged){
            merge(frag2); // no vectors, merge like a generic TextFragment
        }
    }
    StringBuilder builder;
    public BuilderTextFragment(StringBuilder builder, int startPos, int fragNum){
        super(null,startPos,fragNum);
        this.builder = builder;
    }
    public String toString(){
        return builder.substring(textStartPos, textEndPos);
    }
}