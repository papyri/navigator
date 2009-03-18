package info.papyri.epiduke.lucene.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;

public class AnchoredPerFieldAnalyzerWrapper extends PerFieldAnalyzerWrapper {
    public AnchoredPerFieldAnalyzerWrapper(Analyzer def){
        super(def);
    }

    @Override
    public TokenStream reusableTokenStream(String arg0, Reader arg1) throws IOException {
        return new AnchoredTokenStream(super.reusableTokenStream(arg0, arg1));
    }

    @Override
    public TokenStream tokenStream(String arg0, Reader arg1) {
        return new AnchoredTokenStream(super.tokenStream(arg0, arg1));
    }
    
}
