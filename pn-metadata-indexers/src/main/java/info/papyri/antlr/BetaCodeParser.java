package info.papyri.antlr;
import java.util.*;

import antlr.ASTNULLType;
import antlr.collections.AST;
import antlr.Token;
import java.nio.*;
import java.io.*;
import java.nio.charset.*;

public class BetaCodeParser {
    final static Map<CharSequence, char[]> beta = loadBeta();
    final static Map<CharSequence, char[]> betaPunc = loadBetaPunc();
    static Map<CharSequence, char[]> loadBeta() {
        Map<CharSequence, char[]> beta = new HashMap<CharSequence, char[]>(62);
        beta.put("*A", new char[] {'\u0391'});
        beta.put("A", new char[] {'\u03B1'});
        beta.put("*B", new char[] {'\u0392'});
        beta.put("B", new char[] {'\u03B2'});
        beta.put("*C", new char[] {'\u039E'});
        beta.put("C", new char[] {'\u03BE'});
        beta.put("*D", new char[] {'\u0394'});
        beta.put("D", new char[] {'\u03B4'});
        beta.put("*E", new char[] {'\u0395'});
        beta.put("E", new char[] {'\u03B5'});
        beta.put("*F", new char[] {'\u03A6'});
        beta.put("F", new char[] {'\u03C6'});
        beta.put("*G", new char[] {'\u0393'});
        beta.put("G", new char[] {'\u03B3'});
        beta.put("*H", new char[] {'\u0397'});
        beta.put("H", new char[] {'\u03B7'});
        beta.put("*I", new char[] {'\u0399'});
        beta.put("I", new char[] {'\u03B9'});
        beta.put("*K", new char[] {'\u039A'});
        beta.put("K", new char[] {'\u03BA'});
        beta.put("*L", new char[] {'\u039B'});
        beta.put("L", new char[] {'\u03BB'});
        beta.put("*M", new char[] {'\u039C'});
        beta.put("M", new char[] {'\u03BC'});
        beta.put("*N", new char[] {'\u039D'});
        beta.put("N", new char[] {'\u03BD'});
        beta.put("*O", new char[] {'\u039F'});
        beta.put("O", new char[] {'\u03BF'});
        beta.put("*P", new char[] {'\u03A0'});
        beta.put("P", new char[] {'\u03C0'});
        beta.put("*Q", new char[] {'\u0398'});
        beta.put("Q", new char[] {'\u03B8'});
        beta.put("*R", new char[] {'\u03A1'});
        beta.put("R", new char[] {'\u03C1'});
        beta.put("*S", new char[] {'\u03A3'});
        beta.put("S", new char[] {'\u03C3'}); // 03C2 FOR FINAL SIGMA
        beta.put("S1", new char[] {'\u03C3'});
        beta.put("S2", new char[] {'\u03C2'});
        beta.put("*S3", new char[] {'\u03F9'});
        beta.put("S3", new char[] {'\u03F2'});
        beta.put("*T", new char[] {'\u03A4'});
        beta.put("T", new char[] {'\u03C4'});
        beta.put("*U", new char[] {'\u03A5'});
        beta.put("U", new char[] {'\u03C5'});
        beta.put("*V", new char[] {'\u03DC'});
        beta.put("V", new char[] {'\u03DD'});
        beta.put("*W", new char[] {'\u03A9'});
        beta.put("W", new char[] {'\u03C9'});
        beta.put("*X", new char[] {'\u03A7'});
        beta.put("X", new char[] {'\u03C7'});
        beta.put("*Y", new char[] {'\u03A8'});
        beta.put("Y", new char[] {'\u03C8'});
        beta.put("*Z", new char[] {'\u0396'});
        beta.put("Z", new char[] {'\u03B6'});
        beta.put(")", new char[] {'\u0313'});
        beta.put("(", new char[] {'\u0314'});
        beta.put("/", new char[] {'\u0301'});
        beta.put("=", new char[] {'\u0342'});
        beta.put("\\", new char[] {'\u0300'});
        beta.put("+", new char[] {'\u0308'});
        beta.put("|", new char[] {'\u0345'});
        beta.put("?", new char[] {'\u0323'});
        beta.put(".", new char[] {'\u002E'});
        beta.put(":", new char[] {'\u00B7'});
        beta.put(";", new char[] {'\u003B'});
        beta.put("'", new char[] {'\u2019'});
        beta.put("-", new char[] {'\u2010'});
        beta.put("—", new char[] {'\u2014'});
        beta.put("\u00A0", new char[] {'\u00A0'});
        beta.put(" ", new char[] {' '});
        return Collections.unmodifiableMap(beta);
    }
    
    static Map<CharSequence, char[]> loadBetaPunc() {
        Map<CharSequence, char[]> beta = new HashMap<CharSequence, char[]>(62);
        beta.put(")", new char[] {'\u0313'});
        beta.put("(", new char[] {'\u0314'});
        beta.put("/", new char[] {'\u0301'});
        beta.put("=", new char[] {'\u0342'});
        beta.put("\\", new char[] {'\u0300'});
        beta.put("+", new char[] {'\u0308'});
        beta.put("|", new char[] {'\u0345'});
        beta.put("?", new char[] {'\u0323'});
        return Collections.unmodifiableMap(beta);
    }


    
    
    public CharBuffer parse(String betaCode){
        String b = "\n\b\t\r ";
        char [] diacriticals = new char[]{'(',')','/','=','\\','+','|','?'};
        char [] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        int len = betaCode.length();
        CharBuffer result = CharBuffer.allocate(len);
        boolean capital = false;
        boolean diacritical = false;
        iterate:
        for (int i = 0; i < len; i++) {
            char c = betaCode.charAt(i);
            diacritical = (Arrays.binarySearch(diacriticals,c) > -1);
            if ( c == '*') { // possible uppercase token
                capMode:
                for (int x=i+1;x<len;x++){
                    if (Arrays.binarySearch(diacriticals, betaCode.charAt(x)) > -1){
                        continue capMode;
                    }
                    if (Arrays.binarySearch(letters, betaCode.charAt(x)) > -1){
                        //System.out.println("capital mode");
                        capital = true;
                    }
                    break capMode;

                }
                if (!capital) result.append(c);
                continue iterate;
            }
            else if (c == '&') { // possible xml ent
                int x = betaCode.indexOf(';', i);
                if (x > i){
                    result.append(betaCode.substring(i,x+1));
                    i = x;
                }
                else {
                    result.append(c);
                }
                continue iterate;
            }
            else { // possible lowercase token
                if (c == 'S'){
                    if(i + 1 < len) {
                    sigma:
                    switch (betaCode.charAt(i + 1)) {
                    case '1':
                        if (capital) {
                            result.append(beta.get("*S")[0]);
                            result.append('1');
                        } else
                            result.append(beta.get("S1")[0]);
                        i++;
                        break sigma;
                    case '2':
                        if (capital) {
                            result.append(beta.get("*S")[0]);
                            result.append('2');
                        } else
                            result.append(beta.get("S2")[0]);
                        i++;
                        break sigma;
                    case '3':
                        if (capital)
                            result.append(beta.get("*S3")[0]);
                        else
                            result.append(beta.get("S3")[0]);
                        i++;
                        break sigma;
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '0':
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                    case '.':
                    case '/':
                    case '=':
                    case '\\':
                    case '+':
                    case '|':
                    case '?':
                        if (capital)
                            result.append(beta.get("*S")[0]);
                        else
                            result.append(beta.get("S2")[0]);
                        
                            break sigma;
                    case ')':
                        if (capital)
                            result.append(beta.get("*S")[0]);
                        else{
                            if (betaCode.length() > i+2 && b.indexOf(betaCode.charAt(i+2)) > -1){
                                result.append(beta.get("S2")[0]);
                            }
                            else {
                                result.append(beta.get("S")[0]);
                            }
                        }
                        break sigma;
                    default:
                        if (capital){
                            result.append(beta.get("*S")[0]);
                        }
                        else {
                                result.append(beta.get("S")[0]);
                        }
                        break sigma;
                    }
                    }
                    else { // end of buffer, terminal sigma
                        System.out.println("terminal");
                        result.append(beta.get("S2")[0]);
                        
                    }
                }
                else{
                    String key = (capital && !diacritical)?new String(new char[]{'*',c}):new String(new char[]{c});
                    if (beta.get(key) != null){
                        result.append(beta.get(key)[0]);
                    }
                    else {
                        result.append(c);
                    }
                }
                if (!diacritical){
                    capital = false;
                }
            }

        }
        result.flip();
        return result.asReadOnlyBuffer();
    }
    
    public CharBuffer parseANTLR(String betaCode){
        //ByteArrayInputStream bis = new ByteArrayInputStream(betaCode.getBytes());
        StringReader reader = new StringReader(betaCode);
        info.papyri.antlr.unicode.BetaLexer bl = new info.papyri.antlr.unicode.BetaLexer(reader);
        info.papyri.antlr.unicode.BetaParser bp = new info.papyri.antlr.unicode.BetaParser(bl);
        StringBuffer result = new StringBuffer();

        try{
            result.append(bp.characters()); // last character

        }
        catch (Throwable e){
            System.err.println("parseANTLR: " + e.toString());
            e.printStackTrace(System.err);
            return CharBuffer.allocate(0);
        }
        char [] buf = result.toString().toCharArray();

        return CharBuffer.wrap(buf);
    }
    
    public String parseToString(String betaCode){
        return parseANTLR(betaCode).toString();
    }
    public String parseToHex(String betaCode){
        StringBuffer result = new StringBuffer();
        char[] chars = betaCode.toCharArray();
        for (char c: chars){
            result.append("0x");
            result.append(Integer.toHexString(c));
            result.append(" ");
        }
        return result.toString();
    }
}
