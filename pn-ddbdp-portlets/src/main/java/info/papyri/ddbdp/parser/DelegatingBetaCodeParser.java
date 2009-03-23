package info.papyri.ddbdp.parser;

import java.util.TreeMap;
import java.util.TreeSet;

import edu.unc.epidoc.transcoder.BetaCodeParser;
import edu.unc.epidoc.transcoder.AbstractGreekParser;

public class DelegatingBetaCodeParser extends AbstractGreekParser {
    private final BetaCodeParser delegate;
    private StringBuffer strb = new StringBuffer();
    private TreeMap<String,String> map = new TreeMap<String,String>();
    private StringBuffer escape = new StringBuffer();
    private DelegatingBetaCodeParser(){
        delegate = null;
    }
    public DelegatingBetaCodeParser(BetaCodeParser delegate){
        this.delegate = delegate;
        encoding  = delegate.getEncoding();
    }
    /** Returns the next parsed character as a String.
     * @return The name of the parsed character.
     */  
    public String next() {
        strb.delete(0,strb.length());
        if (in != null) {
            char ch = chArray[index];
            index++;
            map.clear();
            escape.delete(0,escape.length());
            if (hasNext() && BetaCodeParser.isBetaCodePrefix(ch)) {
                if (Character.isDigit(chArray[index]) || BetaCodeParser.isBetaCodePrefix(chArray[index])) {
                    escape.append(ch);
                    if (BetaCodeParser.isBetaCodePrefix(chArray[index])) {
                        escape.append(chArray[index]);
                        index++;
                    }
                    if (hasNext()) {
                        while (hasNext() && Character.isDigit(chArray[index]) ) {
                            escape.append(chArray[index]);
                            index++;
                        }
                    }
                    String result = delegate.lookup(escape.toString());
                    // reset if this isn't a recognized Beta Code escape
                    if (result.equals(escape.toString())) { 
                        index -= (escape.length() - 1);
                        escape.delete(1, escape.length());
                    }
                    strb.append((delegate.lookup(escape.toString())));
                } else {
                    if (ch == '#' && Character.isLetter(chArray[index]))
                        strb.append(delegate.lookup(ch));
                    else {
                        while (hasNext() && BetaCodeParser.isBetaCodeDiacritical(chArray[index])) {
                            map.put(delegate.lookupAccent(chArray[index]), delegate.lookup(chArray[index]));
                            index++;
                        }
                        if (hasNext()) {
                            strb.append(delegate.lookup(delegate.lookup(ch) + String.valueOf(chArray[index])));
                            index++;

                            while (!map.isEmpty()) {
                                String str = (String)map.remove(map.firstKey());
                                strb.append("_"+str);
                            }
                        }
                    }
                }
            } else {
                if (ch == 'S' || ch == 's') {
                    if (index < chArray.length && Character.isDigit(chArray[index])) {
                        escape.append(ch);
                        escape.append(chArray[index]);
                        index++;
                        strb.append(delegate.lookup(escape.toString()));
                    } else {
                        if (!BetaCodeParser.isTerminalSigma(chArray, index)) {
                            strb.append(delegate.lookup(ch));
                        } else {
                            strb.append(delegate.lookup(String.valueOf(ch)+"2"));
                        }
                            
                    }
                } else {
                    strb.append(delegate.lookup(ch));
                    while (hasNext() && BetaCodeParser.isBetaCodeDiacritical(chArray[index])) {
                        map.put(delegate.lookupAccent(chArray[index]), delegate.lookup(chArray[index]));
                        index++;
                    }

                    while (map.size()>0) {
                        String str = (String)map.remove(map.firstKey());
                        strb.append("_"+str);
                    }
                }
            }
        }
        return strb.toString();
    }
}
