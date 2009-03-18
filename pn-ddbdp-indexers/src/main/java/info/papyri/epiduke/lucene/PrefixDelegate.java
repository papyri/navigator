/**
 * 
 */
package info.papyri.epiduke.lucene;

import info.papyri.epiduke.lucene.PrefixTermEnum;
import info.papyri.epiduke.lucene.SubstringDelegate;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

public class PrefixDelegate implements SubstringDelegate{
    private final Term t;
    private final String text;
    private final char [] chars;
    public PrefixDelegate(Term t){
        this.t = t;
        this.text = t.text();
        this.chars = text.toCharArray();
    }
    public boolean matches(char[] text, int start, int len) throws IOException {
       if(len < chars.length) return false;
       for(int i=0;i<chars.length;i++){
           if(chars[i] != text[i+start]) return false;
       }
       return true;
    }

    public String[] matches(IndexReader reader) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        PrefixTermEnum pEnum = new PrefixTermEnum(reader,this.t);
        while(pEnum.next()){
            result.add(pEnum.term().text());
        }
        return result.toArray(info.papyri.util.ArrayTypes.STRING);
    }

    public boolean matches(String text) throws IOException {
        return text.startsWith(this.text);
    }

    public Term substringTerm() {
        return t;
    }
    
}