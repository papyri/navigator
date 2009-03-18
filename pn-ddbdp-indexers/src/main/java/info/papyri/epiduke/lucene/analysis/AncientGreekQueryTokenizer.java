package info.papyri.epiduke.lucene.analysis;

import java.io.Reader;

public class AncientGreekQueryTokenizer extends AncientGreekTokenizer {
    public AncientGreekQueryTokenizer(Reader reader){
        super(reader);
    }
    @Override
    protected boolean isTokenChar(char c) {

        if (c=='?' || c=='*'){
            return true;
        }
        else {
            return super.isTokenChar(c);
        }
    }

}
