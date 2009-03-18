package  org.apache.lucene.search.highlight;

import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.TokenGroup;

public class FastHTMLFormatter implements Formatter {
    public static final FastHTMLFormatter THREADSAFE_FORMATTER = new FastHTMLFormatter();
    public static final FastHTMLFormatter NULL_FORMATTER = new FastHTMLFormatter(){
        public char[] highlightTerm(char[] arg0) {return arg0;};
        @Override
        public String highlightTerm(String arg0) {
            return arg0;
        }
        @Override
        public String highlightTerm(String arg0, TokenGroup arg1) {
            return arg0;
        }
    };
    private static final char[] PRETAG = "<B>".toCharArray();
    private static final char[] POSTTAG = "</B>".toCharArray();
    private static final int ADD = 7;
    private FastHTMLFormatter(){};
    public String highlightTerm(String arg0, TokenGroup arg1) {
        if(arg1.getTotalScore() > 0){
            int len = arg0.length();
            char [] result = new char[len + ADD];
            System.arraycopy(PRETAG,0,result,0,3);
            System.arraycopy(arg0.toCharArray(),0,result,3,len);
            System.arraycopy(POSTTAG, 0,result, len+3, 4);
            return new String(result);
        }
        return arg0;
    }
    public String highlightTerm(String arg0) {
            int len = arg0.length();
            char [] result = new char[len + ADD];
            System.arraycopy(PRETAG,0,result,0,3);
            System.arraycopy(arg0.toCharArray(),0,result,3,len);
            System.arraycopy(POSTTAG, 0,result, len+3, 4);
            return new String(result);
    }
    public char[]  highlightTerm(char[] arg0) {
        int len = arg0.length;
        char [] result = new char[len + ADD];
        System.arraycopy(PRETAG,0,result,0,3);
        System.arraycopy(arg0,0,result,3,len);
        System.arraycopy(POSTTAG, 0,result, len+3, 4);
        return result;
}

}
