package org.apache.lucene.search.highlight;

import org.apache.lucene.search.highlight.Encoder;

public class BracketEncoder implements Encoder {
    public static final BracketEncoder THREADSAFE_ENCODER = new BracketEncoder();
    public static final BracketEncoder NULL_ENCODER = new BracketEncoder(){
        public char[] encodeText(char[] chars, int start, int end) {
            char [] temp = new char[end-start];
            System.arraycopy(chars,start,temp,0,temp.length);
            return temp;
        };
        public String encodeText(String arg0) {return arg0;};
    };
    private static final char [] GT = new char[]{'&','g','t',';'};
    private static final char [] LT = new char[]{'&','l','t',';'};
    private BracketEncoder(){
        
    }
    
    public static boolean test(char [] chars, int start, int end){
        for(int i=start; i<end;i++){
            if(chars[i]=='<'||chars[i]=='>') return true;
        }
        return false;
    }
    
    public char[]  encodeText(char [] chars, int start, int end) {
        int diff = 0;
        char [] temp = new char[end-start];
        System.arraycopy(chars,start,temp,0,temp.length);
        for(int i=0; i<temp.length;i++){
            if(temp[i]=='<'||temp[i]=='>') diff +=3;
        }
        if (diff == 0) return temp;
        char [] result = new char[temp.length + diff];
        for(int i=temp.length-1;i>=0;i--){
            if(temp[i]=='<'){
                diff-=3;
                System.arraycopy(LT, 0, result, i+diff, 4);
//                result[i+diff--] = ';';
//                result[i+diff--] = 't';
//                result[i+diff--] = 'l';
//                result[i+diff] = '&';
            }
            else if(temp[i]=='>'){
                diff-=3;
                System.arraycopy(GT, 0, result, i+diff, 4);
//                result[i+diff--] = ';';
//                result[i+diff--] = 't';
//                result[i+diff--] = 'g';
//                result[i+diff] = '&';
            }
            else{
                result[i+diff] = temp[i];
            }
        }
        return  result;
    }
    public String encodeText(String arg0) {
        char [] chars = arg0.toCharArray();
        int diff = 0;
        for(char c:chars){
            if(c=='<'||c=='>') diff +=3;
        }
        if (diff == 0) return arg0;
        char [] result = new char[chars.length + diff];
        for(int i=chars.length-1;i>=0;i--){
            if(chars[i]=='<'){
                diff-=3;
                System.arraycopy(LT, 0, result, i+diff, 4);
//                result[i+diff--] = ';';
//                result[i+diff--] = 't';
//                result[i+diff--] = 'l';
//                result[i+diff] = '&';
            }
            else if(chars[i]=='>'){
                diff-=3;
                System.arraycopy(GT, 0, result, i+diff, 4);
//                result[i+diff--] = ';';
//                result[i+diff--] = 't';
//                result[i+diff--] = 'g';
//                result[i+diff] = '&';
            }
            else{
                result[i+diff] = chars[i];
            }
        }
        return new String(result);
    }
    

}
