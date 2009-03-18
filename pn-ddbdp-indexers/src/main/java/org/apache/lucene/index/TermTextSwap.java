package org.apache.lucene.index;

public abstract class TermTextSwap {
    public static Term swapText(Term t, String newText){
        t.text = newText;
        return t;
    }
}
