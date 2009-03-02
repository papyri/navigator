// $ANTLR 2.7.7 (20060930): "beta4.g" -> "BetaParser.java"$

  package  info.papyri.antlr.unicode;

import info.papyri.antlr.BetaChars;
import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

public class BetaParser extends antlr.LLkParser       implements BetaLexerTokenTypes
 {

protected BetaParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public BetaParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected BetaParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public BetaParser(TokenStream lexer) {
  this(lexer,1);
}

public BetaParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final char []  characters() throws RecognitionException, TokenStreamException {
		char [] chars = new char[0];
		
		char [] s = new char[0];
		
		try {      // for error handling
			{
			_loop51:
			do {
				if ((_tokenSet_0.member(LA(1)))) {
					s=character();
					if ( inputState.guessing==0 ) {
						
						char [] c = new char[s.length + chars.length];
						if (chars.length > 0) System.arraycopy(chars,0,c,0,chars.length);
						if (s.length > 0) System.arraycopy(s,0,c,chars.length,s.length);
						chars = c;  
						
					}
				}
				else {
					break _loop51;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
				int next = chars.length - 1;
				while (next > -1){
				int last = -1;
				for (int i = next;i > -1; i--){
				if (chars[i] >= BetaChars.A_LOWER && chars[i] <= BetaChars.W_LOWER){
				if (chars[i] == BetaChars.S1){
				}
				last = i;
				break;
				}
				}
				next = (last == -1)?last:new String(chars, 0, last).replaceAll("\\s","\u2717").lastIndexOf("\u2717");
				}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		return chars;
	}
	
	public final char []  character() throws RecognitionException, TokenStreamException {
		char [] s = new char[0];
		
		Token  w = null;
		Token  p = null;
		Token  d = null;
		Token  o = null;
		Token  h = null;
		Token  n = null;
		Token  c = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ASTERISK:
			{
				s=capital();
				break;
			}
			case SIGMA:
			{
				s=sigma();
				break;
			}
			case BASIC_LETTER:
			{
				s=letter();
				break;
			}
			case HASH:
			{
				s=symbol();
				break;
			}
			case SUBSCRIPT_DOT:
			{
				match(SUBSCRIPT_DOT);
				if ( inputState.guessing==0 ) {
					s = new char[]{'\u0323'};
				}
				break;
			}
			case WS:
			{
				w = LT(1);
				match(WS);
				if ( inputState.guessing==0 ) {
					
					s = w.getText().toCharArray();
					
				}
				break;
			}
			case PUNCTUATION:
			{
				p = LT(1);
				match(PUNCTUATION);
				if ( inputState.guessing==0 ) {
					
					if (p != null && p.getText() != null) s = p.getText().toCharArray();
					else  s = new char[]{BetaChars.map(c.getText(),false)};
					
				}
				break;
			}
			case DIGIT:
			{
				d = LT(1);
				match(DIGIT);
				if ( inputState.guessing==0 ) {
					
					if (d != null && d.getText() != null) s = d.getText().toCharArray();
					else s = new char[0];
					
				}
				break;
			}
			case COMBINING:
			{
				o = LT(1);
				match(COMBINING);
				if ( inputState.guessing==0 ) {
					
					if (o != null && o.getText() != null) s = o.getText().toCharArray();
					else s = new char[0];
					
				}
				break;
			}
			case DASH:
			{
				h = LT(1);
				match(DASH);
				if ( inputState.guessing==0 ) {
					s = new char[]{'-'};
					
				}
				break;
			}
			case NONBASE:
			{
				n = LT(1);
				match(NONBASE);
				if ( inputState.guessing==0 ) {
					
					s = n.getText().toCharArray();
					if (s[0] == '\u2ce9') s = new char[]{'#','3','2','2'};
				}
				break;
			}
			case CHAR:
			{
				c = LT(1);
				match(CHAR);
				if ( inputState.guessing==0 ) {
					
					s = new char[]{BetaChars.map(c.getText(),false)};
					
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		return s;
	}
	
	public final char[]  capital() throws RecognitionException, TokenStreamException {
		char[] c = new char[0];
		
		Token  co = null;
		Token  b = null;
		Token  s = null;
		
		try {      // for error handling
			boolean synPredMatched66 = false;
			if (((LA(1)==ASTERISK))) {
				int _m66 = mark();
				synPredMatched66 = true;
				inputState.guessing++;
				try {
					{
					match(ASTERISK);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched66 = false;
				}
				rewind(_m66);
inputState.guessing--;
			}
			if ( synPredMatched66 ) {
				match(ASTERISK);
				{
				switch ( LA(1)) {
				case COMBINING:
				{
					{
					co = LT(1);
					match(COMBINING);
					}
					break;
				}
				case BASIC_LETTER:
				case SIGMA:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case BASIC_LETTER:
				{
					b = LT(1);
					match(BASIC_LETTER);
					break;
				}
				case SIGMA:
				{
					s = LT(1);
					match(SIGMA);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
					StringBuffer buffer = new StringBuffer();
					if (b != null) buffer.append(b.getText());
					if (s != null) buffer.append(s.getText());
					if (co != null) buffer.append(co.getText());
					c = buffer.toString().toCharArray();
					c[0] = BetaChars.map(Character.toString(c[0]),true);
					for (int i=1;i<c.length; i++){
					c[i] = BetaChars.map(Character.toString(c[i]),false);
					}
					c = BetaChars.combine(c);
					
				}
			}
			else {
				boolean synPredMatched71 = false;
				if (((LA(1)==ASTERISK))) {
					int _m71 = mark();
					synPredMatched71 = true;
					inputState.guessing++;
					try {
						{
						match(ASTERISK);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched71 = false;
					}
					rewind(_m71);
inputState.guessing--;
				}
				if ( synPredMatched71 ) {
					match(ASTERISK);
					if ( inputState.guessing==0 ) {
						
						c = new char[]{'*'};
						
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_2);
				} else {
				  throw ex;
				}
			}
			return c;
		}
		
	public final char []  sigma() throws RecognitionException, TokenStreamException {
		char [] c = new char[0];
		
		Token  s1 = null;
		Token  w = null;
		Token  s2 = null;
		Token  p = null;
		Token  s = null;
		Token  co = null;
		Token  e = null;
		
		try {      // for error handling
			boolean synPredMatched74 = false;
			if (((LA(1)==SIGMA))) {
				int _m74 = mark();
				synPredMatched74 = true;
				inputState.guessing++;
				try {
					{
					match(SIGMA);
					match(WS);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched74 = false;
				}
				rewind(_m74);
inputState.guessing--;
			}
			if ( synPredMatched74 ) {
				s1 = LT(1);
				match(SIGMA);
				w = LT(1);
				match(WS);
				if ( inputState.guessing==0 ) {
					
					c = (w != null)?new char[]{BetaChars.S2,w.getText().charAt(0)}:new char[]{BetaChars.S2};
					
				}
			}
			else {
				boolean synPredMatched76 = false;
				if (((LA(1)==SIGMA))) {
					int _m76 = mark();
					synPredMatched76 = true;
					inputState.guessing++;
					try {
						{
						match(SIGMA);
						match(PUNCTUATION);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched76 = false;
					}
					rewind(_m76);
inputState.guessing--;
				}
				if ( synPredMatched76 ) {
					s2 = LT(1);
					match(SIGMA);
					p = LT(1);
					match(PUNCTUATION);
					if ( inputState.guessing==0 ) {
						
						c = (p != null)?new char[]{BetaChars.S2,p.getText().charAt(0)}:new char[]{BetaChars.S2};
						
					}
				}
				else {
					boolean synPredMatched78 = false;
					if (((LA(1)==SIGMA))) {
						int _m78 = mark();
						synPredMatched78 = true;
						inputState.guessing++;
						try {
							{
							match(SIGMA);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched78 = false;
						}
						rewind(_m78);
inputState.guessing--;
					}
					if ( synPredMatched78 ) {
						s = LT(1);
						match(SIGMA);
						{
						if ((LA(1)==COMBINING)) {
							{
							co = LT(1);
							match(COMBINING);
							}
						}
						else if ((_tokenSet_2.member(LA(1)))) {
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
						{
						if ((LA(1)==EOF)) {
							{
							e = LT(1);
							match(Token.EOF_TYPE);
							}
						}
						else if ((_tokenSet_2.member(LA(1)))) {
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
						if ( inputState.guessing==0 ) {
							
							c = (co != null)? new char[co.getText().length() + 1]:new char[1];
							if (co != null){
							String cText = co.getText();
							for (int i=0;i<cText.length();i++){
							c[i + 1] = BetaChars.map(cText.substring(i,i+1),false);
							}
							}
							c[0] = (e != null)?BetaChars.S2:BetaChars.map(s.getText(),false);
							
						}
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					}}
				}
				catch (RecognitionException ex) {
					if (inputState.guessing==0) {
						reportError(ex);
						recover(ex,_tokenSet_2);
					} else {
					  throw ex;
					}
				}
				return c;
			}
			
	public final char []  letter() throws RecognitionException, TokenStreamException {
		char [] c = new char[0];
		
		Token  b = null;
		Token  co = null;
		char [] combining = new char [0];
		
		try {      // for error handling
			{
			b = LT(1);
			match(BASIC_LETTER);
			}
			{
			if ((LA(1)==COMBINING)) {
				{
				co = LT(1);
				match(COMBINING);
				}
			}
			else if ((_tokenSet_2.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
				int numSibs = 1;
				if (co != null) numSibs += co.getText().length();
				c = new char[numSibs];
				c[0] = BetaChars.map(b.getText(),false);
				if (co != null){
				String cText = co.getText();
				for (int i=0;i<cText.length();i++){
				c[i + 1] = BetaChars.map(cText.substring(i,i+1),false);
				}
				}
				c = BetaChars.combine(c);
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		return c;
	}
	
	public final char[]  symbol() throws RecognitionException, TokenStreamException {
		char[] s = new char[0];
		
		
		try {      // for error handling
			boolean synPredMatched56 = false;
			if (((LA(1)==HASH))) {
				int _m56 = mark();
				synPredMatched56 = true;
				inputState.guessing++;
				try {
					{
					match(HASH);
					{
					match(DIGIT);
					}
					}
				}
				catch (RecognitionException pe) {
					synPredMatched56 = false;
				}
				rewind(_m56);
inputState.guessing--;
			}
			if ( synPredMatched56 ) {
				match(HASH);
				{
				int _cnt59=0;
				_loop59:
				do {
					if ((LA(1)==DIGIT)) {
						{
						match(DIGIT);
						}
					}
					else {
						if ( _cnt59>=1 ) { break _loop59; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt59++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					
					String digit="";
					int digitPos = mark();
					while (LA(1) == DIGIT){
					digit+= LT(1).getText();
					}
					
				}
			}
			else if ((LA(1)==HASH)) {
				match(HASH);
				if ( inputState.guessing==0 ) {
					
					s = new char[]{'#'};
					
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		return s;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"BASIC_LETTER",
		"SIGMA",
		"PSILI",
		"DASIA",
		"VARIA",
		"OXIA",
		"GRAMMENI",
		"PERISPOMENI",
		"DIERESIS",
		"BREATHING",
		"ACCENT",
		"IOTA_SUBSCRIPT",
		"DIACRITIC",
		"COMBINING",
		"SUBSCRIPT_DOT",
		"ASTERISK",
		"DASH",
		"PUNCTUATION",
		"DIGIT",
		"HASH",
		"WS",
		"NONBASE",
		"CHAR"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 134086704L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 134086706L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	
	}
