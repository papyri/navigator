/**
 * 
 */
package org.apache.lucene.search;

import java.io.IOException;
import org.apache.lucene.index.TermPositions;


class PPTuple{
    final  PhrasePositions pp;
    int slop;
    int position;
    PPTuple prev;
    PPTuple(PhrasePositions pp){
        this.pp = pp;
        this.position = pp.position;
    }
    int doc(){
    	return pp.doc;
    }
    void firstPosition() throws IOException{
        pp.firstPosition();
        this.position = pp.position;
    }
    boolean nextPosition() throws IOException{
        boolean result =  pp.nextPosition();
        position = pp.position;
        return result;
    }
    TermPositions termPositions(){
        return pp.tp;
    }
    PhrasePositions phrasePositions(){
        return pp;
    }
    
}