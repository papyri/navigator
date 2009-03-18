package info.papyri.epiduke.lucene.analysis;

import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer.Normalize;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;

public class AncientGreekQueryAnalyzer extends AncientGreekAnalyzer {
    public AncientGreekQueryAnalyzer(Normalize options){
        super(options);
    }

    
    public TokenStream tokenStream(String arg0, Reader reader) {
        TokenStream result = new AncientGreekQueryTokenizer(reader);
        return new UnicodeCFilter(this.options.filter(result));
    }

}
