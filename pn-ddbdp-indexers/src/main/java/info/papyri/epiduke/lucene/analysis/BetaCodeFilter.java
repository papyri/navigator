package info.papyri.epiduke.lucene.analysis;

import java.io.IOException;
import java.util.regex.*;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import edu.unc.epidoc.transcoder.UnicodeCConverter;
import edu.unc.epidoc.transcoder.BetaCodeParser;

public class BetaCodeFilter extends TokenFilter {
    BetaCodeParser parser;
    private static final UnicodeCConverter converter = new UnicodeCConverter();
    private static final Pattern WC_CHAR = Pattern.compile("[*?]");
    
    public BetaCodeFilter(TokenStream in){
        super(in);
        parser = new BetaCodeParser();
    }

    @Override
    public Token next() throws IOException {
        return next(null);
    }
    
    @Override
    public Token next(Token token) throws IOException {
        if(token == null) token = new Token();
         token = input.next(token);

        if (token == null)
            return null;
        if(token.termLength()==0)return token;
        short anchors = 0;
        String orig = token.termText();
        char [] chArray = token.termBuffer();
        if(chArray[0] == '&' && chArray[token.termLength() - 1] == ';') return token; // ignore entities

        if (orig.startsWith(AnchoredTokenStream.ANCHOR_STR)){
            anchors += 1;
            orig = orig.substring(1);
        }    
        if (orig.endsWith(AnchoredTokenStream.ANCHOR_STR)){
            anchors += 2;
            orig = orig.substring(0,orig.length() -1);
        }

        String converted = showWildcards(getConverted(hideWildcards(orig,1)));
        switch (anchors){
        case 1:
            token.setTermText(AnchoredTokenStream.ANCHOR_STR + converted);
            break;
        case 2:
            token.setTermText(converted + AnchoredTokenStream.ANCHOR_STR);
            break;
        case 3:
            token.setTermText(AnchoredTokenStream.ANCHOR_STR + converted + AnchoredTokenStream.ANCHOR_STR);
            break;
        default:
            token.setTermText(converted);
        }

        return token;
    }

    private static final char A = '*';
    private static final char Ah = '`';
    private static final char Q = '?';
    private static final char Qh = '~';
    
    public static String hideWildcards(String in, int offset){
        return in.substring(0,offset) + in.replace(A, Ah).replace(Q, Qh).substring(offset);
    }
    public static String showWildcards(String in){
        return in.replace(Ah, A).replace(Qh, Q);
    }
    
    private String getConverted(String orig) throws IOException {
//        System.out.println("String to convert: \"" + orig + "\"");
        StringBuffer result  = new StringBuffer();
        Matcher matcher = WC_CHAR.matcher(orig);
        int last = 0;
        String converted;
        while(matcher.find(last + 1)){
            String temp = orig.substring(last,matcher.start());
//            System.out.println("converting \"" + temp + "\"");
            parser.setString(temp);
            converted = converter.convertToString(parser);
            converted = converted.replace('\u03C2', '\u03C3');
            result.append(converted);
            result.append(orig.charAt(matcher.start()));
            last = matcher.start()+1;
        }
//        System.out.println("converting \"" + orig.substring(last) + "\"");
        parser.setString(orig.substring(last));
        converted = converter.convertToString(parser);
        converted = converted.replace('\u03C2', '\u03C3');
        result.append(converted);
        return result.toString();
        
    }

}
