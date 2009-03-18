package info.papyri.epiduke.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.*;

public class AncientGreekTokenizer extends WhitespaceTokenizer {

    public AncientGreekTokenizer(Reader reader){
    	super(reader);
    }
    
	@Override
	protected boolean isTokenChar(char c) {

		switch (c){
		case ',':
		case '"':
		case '\'':
		case '?':
        case '.':
        case '\u0323':
		case '\u037E':
		case '\u0387':
            return false;
	    default:
	    	return super.isTokenChar(c);
		}
	}
	@Override
	protected char normalize(char c) {
		switch(c){
		case AncientGreekCharsets.ALPHA_tonos:
			return '\u1FBB';
		case AncientGreekCharsets.EPSILON_tonos:
			return '\u1FC9';
		case AncientGreekCharsets.ETA_tonos:
			return '\u1FCB';
		case AncientGreekCharsets.IOTA_tonos:
			return '\u1FDB';
		case AncientGreekCharsets.OMICRON_tonos:
			return '\u1FF9';
		case AncientGreekCharsets.UPSILON_tonos:
			return '\u1FEB';
		case AncientGreekCharsets.OMEGA_tonos:
			return '\u1FFB';
		case AncientGreekCharsets.iota_dialytika_tonos:
			return '\u1FD3';
		case AncientGreekCharsets.alpha_tonos:
			return '\u1F71';
		case AncientGreekCharsets.epsilon_tonos:
			return '\u1F73';
		case AncientGreekCharsets.eta_tonos:
			return '\u1F75';
		case AncientGreekCharsets.iota_tonos:
			return '\u1F77';
		case AncientGreekCharsets.sigma_final:
			return AncientGreekCharsets.sigma_medial;
		case AncientGreekCharsets.omicron_tonos:
			return '\u1F79';
		case AncientGreekCharsets.upsilon_tonos:
			return '\u1F7B';
		case AncientGreekCharsets.omega_tonos:
		    return '\u1F7D';
		default:
			return super.normalize(c);
		}

	}
}
