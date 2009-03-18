package info.papyri.epiduke.lucene;
import java.io.IOException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;

public class FilteredTermEnum extends org.apache.lucene.search.FilteredTermEnum {
	final Term template;
    final String [] knownTerms;
    private int pos = 0;
    private boolean endEnum;
    private final TermEnum src;
    private Term next;
    public FilteredTermEnum(Term template, String [] terms, IndexReader reader) throws IOException{
    	java.util.Arrays.sort(terms);
    	this.knownTerms = terms;
    	this.template = template;
    	endEnum = (terms.length == 0);
    	if(!endEnum) src = reader.terms(template.createTerm(knownTerms[0]));
    	else src = null;
    	next = (next())?term():null;
    }

    public boolean next() throws IOException {
             if(!endEnum){
                 Term skip = template.createTerm("");
                 while(pos < knownTerms.length){
                	 org.apache.lucene.index.TermTextSwap.swapText(skip, knownTerms[pos++]);
                	 int c = skip.compareTo(src.term());
                     if(c > 0 && !src.skipTo(skip)){
                    	 endEnum = true;
                    	 next = null;
                    	 return false;
                     }
                     c = skip.compareTo(src.term());

                     if(c == 0){
                    	 next = src.term();
                    	 return true;
                     }
                     
                 }
                 endEnum = true;
                 next = null;
                 return false;
             }
             next = null;
             return false;
    }
    
 	
    public Term term(){
        return next;
    }
    public boolean endEnum(){
        return endEnum;
    }
    public boolean termCompare(Term c){
    	Term check = template.createTerm("");
        for(int i = pos; i < knownTerms.length;i++){
        	org.apache.lucene.index.TermTextSwap.swapText(check, knownTerms[i]);
        	int comp = check.compareTo(c);
        	if(comp == 0) return true;
        	if(comp > 0) return false;
        }
        return false;
    }
    public final float difference(){
        return 1.0f;
    }
    public void close() throws IOException {
        endEnum = true;
        if(src != null) src.close();
    }

}
