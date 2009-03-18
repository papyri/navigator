package info.papyri.epiduke.lucene.analysis;


import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public class AncientGreekAnalyzer extends Analyzer {
    protected final Normalize options;
    String name;
    boolean NORMALIZE_UNICODE  = true;

    public AncientGreekAnalyzer(){
    	this(Normalize.NONE,true);
    }
    public AncientGreekAnalyzer(boolean ignoreAccents, boolean ignoreCase){
        short opts = 0;
        if (ignoreAccents) opts += 1;
        if (ignoreCase) opts +=2;
        switch (opts){
        case 1:
            this.options = Normalize.DIACRITICS;
            break;
        case 2:
            this.options = Normalize.CASE;
            break;
        case 3:
            this.options = Normalize.CASE_AND_DIACRITICS;
            break;
        default:
            this.options = Normalize.NONE;
            break;
        }
    }
    
    public AncientGreekAnalyzer(Normalize options){
        this.options = options;
        this.NORMALIZE_UNICODE = true;
    }

    public AncientGreekAnalyzer(Normalize options, boolean normalizeUnicode){
        this.options = options;
        this.NORMALIZE_UNICODE = normalizeUnicode;
    }

	@Override
	public TokenStream tokenStream(String arg0, Reader reader) {
        TokenStream result = (NORMALIZE_UNICODE)?new UnicodeCFilter(arg0, new InPlaceAltTokenStream(new AncientGreekTokenizer(reader))):new InPlaceAltTokenStream(new AncientGreekTokenizer(reader));
//        TokenStream result = (NORMALIZE_UNICODE)?new UnicodeCFilter(arg0, new AncientGreekTokenizer(reader)):new AncientGreekTokenizer(reader);
        return this.options.filter(result);
	}
	
	public TokenStream filter(TokenStream tokens){
		return this.options.filter(tokens);
	}
    
    public static abstract class Normalize{
        public static final Normalize NONE = new Normalize(){
            TokenStream filter(TokenStream tokens){
                return tokens;
            }
        };
        public static final Normalize CASE = new Normalize(){
            TokenStream filter(TokenStream tokens){
                return new AncientGreekLowerCaseFilter(tokens);
            }
        };
        public static final Normalize DIACRITICS = new Normalize(){
            TokenStream filter(TokenStream tokens){
                return new AncientGreekAccentFilter(tokens);
            }
        };
        public static final Normalize CASE_AND_DIACRITICS = new Normalize(){
            TokenStream filter(TokenStream tokens){
                return new AncientGreekLowerCaseFilter(new AncientGreekAccentFilter(tokens));
            }
        };
        public static final Normalize BETACODE = new Normalize(){
            TokenStream filter(TokenStream tokens){
                return new AncientGreekAccentFilter(new BetaCodeFilter(tokens));
            }
        };
        public static final Normalize CASE_AND_BETACODE = new Normalize(){
            TokenStream filter(TokenStream tokens){
                return new AncientGreekLowerCaseFilter(new AncientGreekAccentFilter(new BetaCodeFilter(tokens)));
            }
        };
        Normalize(){
            
        }
        abstract TokenStream filter(TokenStream tokens);
    };

}
