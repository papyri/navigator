package info.papyri.epiduke.lucene.analysis;

import java.io.IOException;
import java.util.*;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;

import info.papyri.epiduke.lucene.analysis.*;

public class LinePositionTokenStream extends TokenStream {
    final Iterator<String> lines;
    private TokenStream tokens;
    private boolean newLine = false;
    private int offset = 0;
    private final Analyzer analyzer;
	public LinePositionTokenStream(Iterator<String> lines,Analyzer analyzer){
		this.lines = lines;
        this.analyzer = analyzer;
	}
    public LinePositionTokenStream(String text,Analyzer analyzer){
        String [] lines = text.split("\n");
        this.lines = java.util.Arrays.asList(lines).iterator();
        this.analyzer = analyzer;
    }

	public Token next(Token next) throws IOException {
        if(next == null) next = new Token();
        next  = (tokens==null)?null:tokens.next(next);
        if (next == null){
        	if(lines.hasNext()){
        		tokens = analyzer.tokenStream(null,new StringReader(lines.next()));
        		offset = 1;
        		newLine = true;
        		return next();
        	}
        	else {
        		return null;
        	}
        }
        
		next.setPositionIncrement(offset);

        if (newLine){
			newLine = false;
			offset = 0;
		}
		return next;
	}
    public Token next() throws IOException {
        return next(null);
    }
}
