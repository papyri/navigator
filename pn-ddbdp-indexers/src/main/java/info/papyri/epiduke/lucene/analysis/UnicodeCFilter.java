package info.papyri.epiduke.lucene.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import edu.unc.epidoc.transcoder.*;
import java.util.Properties;
import java.util.TreeMap;

public class UnicodeCFilter extends TokenFilter {
    
    UnicodeBufferParser parser;
    private static final UnicodeCConverter converter = new UnicodeCConverter();
    String name;
    public UnicodeCFilter(TokenStream in){
        this(null,in);
    }
    public UnicodeCFilter(String name, TokenStream in){
        super(in);
        this.name = name;
        parser = new UnicodeBufferParser();
    }

    @Override
    public Token next(Token t) throws IOException {
        if (t==null) t = new Token();
         t = input.next(t);

        if (t == null)
            return null;
        if(t.termLength()==0)return t;
        char [] tt = t.termBuffer();
        if(tt[0] == '&' && tt[t.termLength() - 1] == ';') return t; // ignore entities

        //String orig = t.termText(); 
        parser.setChars(t.termBuffer(),0,t.termLength());
        try{
            char [] converted = converter.convertToString(parser).toCharArray();
            int len = t.termLength();
            if(converted.length > len){
                if (tt.length < converted.length) t.resizeTermBuffer(converted.length);
                System.arraycopy(converted,0,t.termBuffer(),0,converted.length);
                t.setTermLength(converted.length);
            }
            else{
                System.arraycopy(converted,0,tt,0,converted.length);
                t.setTermLength(converted.length);
            }
        }
        catch(Throwable thrown){
            if (this.name != null){ System.err.print(this.name + "\t");
            System.err.print(t.termText() + "\t");
            for (char c:t.termBuffer()){
                System.err.print(Integer.toHexString(c));
                System.err.print('\t');
            }
            System.err.println(thrown.toString());
            }
        }
        
        return t;
    }
    
    public Token next() throws IOException {
        return next(null);
    }
    private static class UnicodeBufferParser extends AbstractGreekParser {
        int start = 0;
        int length = 0;
        private Properties up;
        private Properties ga;
        private StringBuilder strb = new StringBuilder();
        private TreeMap map = new TreeMap();

        public UnicodeBufferParser(){
            up = new Properties();
            ga = new Properties();
            try {
                Class c = edu.unc.epidoc.transcoder.UnicodeParser.class;
                up.load(c.getResourceAsStream("UnicodeParser.properties"));
                ga.load(c.getResourceAsStream("GreekAccents.properties"));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace(System.out);
                }
        }
        public String next() {
            strb.delete(0,strb.length());
            if (chArray != null && hasNext()) {
                strb.append(lookup(chArray[index]));
                index++;
                if (chArray[index - 1] == '\u03C3' || chArray[index - 1] == '\u03C2') {
                    switch (chArray[index - 1]) {
                        case '\u03C3':
                            if(!hasNext() || !Character.isLetter(chArray[index]))
                                strb.append("Fixed");
                            break;
                        case '\u03C2':
                            if(hasNext() && Character.isLetter(chArray[index]))
                                strb.append("Fixed");
                    }
                } else {
                    if (hasNext() && isCombiningDiacritical(chArray[index])) {
                        map.clear();
                        while (index < chArray.length && isCombiningDiacritical(chArray[index]) ) {
                            map.put(lookupAccent(chArray[index]), lookup(chArray[index]));
                            index++;
                        }
                        while (!map.isEmpty()) {
                            strb.append("_" + (String)map.remove(map.firstKey()));
                        }
                    }
                }
            }
            return strb.toString();
        }
        
        private String lookup(char ch) {
            String key = String.valueOf(ch);
            return up.getProperty(key, key);
        }
        
        private String lookupAccent(char ch) {
            String key = String.valueOf(ch);
            int i = (int)ch;
            String result = lookup(ch);
            return ga.getProperty(lookup(ch));
        }
        
        private boolean isCombiningDiacritical(char ch) {
            switch (ch) {
                case '\u0313':
                case '\u0314':
                case '\u0301':
                case '\u0300':
                case '\u0303':
                case '\u0308':
                case '\u0342':
                case '\u0345':
                    return true;
                default:
                    return false;
            }
        }    
        public void setChars(char [] buf, int start, int len){
            chArray = buf;
            index = start;
            length = len;
        }
        public boolean hasNext(){
            return (index < start + length);
        }
    }
}
