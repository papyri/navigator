package info.papyri.epiduke.lucene.analysis;

import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;

import java.io.IOException;
public class VectorTokenFilter extends CachingTokenFilter {
	private VectorToken first;
	private VectorToken next;
	private boolean firstTime = true;
	private boolean cached = false;
	private VectorTokenFilter(){
		super(null);
	};
	public VectorTokenFilter(TokenStream input){
		super(input);
	}
	public Token next() throws IOException {
		return next(null);
	}
	public void buildCache() throws IOException {
		while(next()!= null){}
	}
	public Token next(Token t) throws IOException {

		if(firstTime){
			firstTime = false;
			if(first == null){
				if(cached) return null;
				this.first = (VectorToken) input.next(new VectorToken());
				if(this.first != null){
					first.next = (VectorToken)input.next(new VectorToken());
				}
				else return null;
			}
			this.next =  first.next;
			return this.first.getToken(t);
		}
		if(next == null){
			if(cached) return null;
			next = (VectorToken)input.next(new VectorToken());
			if(next == null){
				cached = true;
				return null;
			}
		}
		if(next.next == null && !cached){ // get next VectorToken, build vector
		    next.next = (VectorToken)input.next(new VectorToken());
		    VectorToken vt = next;
		    next = next.next;
		    if(next==null) cached = true;
		    return vt.getToken(t);
		}
		VectorToken vt = next;
		next = next.next;
		return vt.getToken(t);
	}

	public void reset() throws IOException{
		this.firstTime = true;
	}

	public void close() throws IOException {
		if(input != null) input.close();
		this.first = null;
	}

	public VectorTokenFilter clone(){
		VectorTokenFilter result = new VectorTokenFilter();
		result.cached = true;
		result.first = this.first;
		return result;
	}
}
