package org.apache.lucene.search;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;

import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.MultipleTermPositions;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.highlight.BracketEncoder;
import org.apache.lucene.search.highlight.FastHTMLFormatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.util.BitVector;
import org.apache.lucene.util.ToStringUtils;

import info.papyri.epiduke.lucene.*;
import info.papyri.epiduke.lucene.substring.SubstringTermTokenFilter;

public class SubstringPhraseQuery extends PhraseQuery {
    private static final byte EXACT = 0x01;
    private static final byte MULTIPOS = 0x02;
    private byte scorer = 0;
    private String field;
    private Vector slops = new Vector();
    private boolean hasSlop;
    private boolean slopVaries;
    private int lastSlop;
//    private Vector terms = new Vector();
//    private Vector positions = new Vector();
//    private int slop = 0;

    /** Constructs an empty phrase query. */
    public SubstringPhraseQuery() {
    	super();
    }
    
    public void useExact(boolean flag){
        if(flag) scorer |= EXACT;
        else if((scorer & EXACT) == scorer) scorer -= EXACT;
    }
    public void useMultiPos(boolean flag){
        if(flag) scorer |= MULTIPOS;
        else if((scorer & MULTIPOS) == scorer) scorer -= MULTIPOS;
    }

    @Override
    public Similarity getSimilarity(Searcher arg0) {
        return new SubstringSimilarity();
    }

    public Similarity getSimilarity(Searcher arg0,SubstringTermEnum reuse) {
        return new SubstringSimilarity(reuse);
    }


    @Override
    public void add(Term arg0, int arg1) {
        this.add(arg0,arg1,0);
    }
    
    public void add(Term arg0, int arg1, int slop) {
        super.add(arg0,arg1);
        if(slop != 0){
            hasSlop = true;
        }
        if(slop != lastSlop) slopVaries = true;
        lastSlop = slop;
        slops.addElement(Integer.valueOf(slop));
        if(this.field==null) this.field = arg0.field();
        else if (arg0.field() != field)
            throw new IllegalArgumentException("All phrase terms must be in the same field: " + arg0); 
    }
    
    public int[] getSlops(){
        int[] result = new int[this.slops.size()];
        for(int i = 0; i < this.slops.size(); i++)
            result[i] = ((Integer) this.slops.elementAt(i)).intValue();
        return result;
    }
    
    public boolean hasSlop(){
        return this.hasSlop;
    }
    
    String field(){
        return this.field;
    }

    static abstract class PhraseWeight implements Weight {
      protected Similarity similarity;
      private float value;
      private float idf;
      private float queryNorm;
      private float queryWeight;
      private SubstringTermEnum reuse;
      protected final Term[] terms;
      protected final Term[] [] matches;
      protected final SubstringPhraseQuery query;
      public PhraseWeight(Searcher searcher,SubstringPhraseQuery query)
        throws IOException {
        this.query = query;
        this.terms = this.query.getTerms();
        this.matches = new Term[this.terms.length][];
        this.similarity = this.query.getSimilarity(searcher,reuse);
        // computing idf locally using default logic, so that we can avoid re-computing the matching terms
        final BitVector or = new BitVector(searcher.maxDoc());
        for(int i = 0; i < terms.length; i++){
            for(int k = 0; k < i;k++) if(terms[k].equals(terms[i])) this.matches[i] = this.matches[k];
            if(this.matches[i] != null) continue;
            Term [] matches = getMatchingTerms(((IndexSearcher)searcher).getIndexReader(), terms[i]);
            this.matches[i] = matches;
            for(int j = 0;j < or.size(); j++) or.clear(i);
            for(Term t:matches){
                searcher.search(new TermQuery(t),new HitCollector(){
                    @Override
                    public void collect(int arg0, float arg1) {
                       or.set(arg0);
                    }
                });
            }
            this.idf += similarity.idf(or.count(), searcher.maxDoc());
        }
        //this.idf = similarity.idf(java.util.Arrays.asList(terms), searcher);
        // by default, the sum of the individual idf's
      }
      private Term [] getMatchingTerms(IndexReader reader, Term term) {
          try{
              FilteredTermEnum tEnum = getMatchingTermEnum(reader, term);
              HashSet<Term> terms = new HashSet<Term>();
              if(!tEnum.endEnum()){
                  do{
                      if(tEnum.term() != null) terms.add(tEnum.term());
                  }while(tEnum.next());
              }
              return terms.toArray(info.papyri.util.ArrayTypes.TERM);
          }
          catch (IOException ioe){
              System.err.println(ioe.toString()); // do some logging
              return info.papyri.util.ArrayTypes.TERM;
          }
      }
      
      private FilteredTermEnum getMatchingTermEnum(IndexReader reader, Term term) throws IOException {
          String text = term.text();
          boolean wc = (text.indexOf('?') != -1 || text.indexOf('*') != -1);
              if(!wc){
                  if(text.charAt(0) == '^'){
                      return new PrefixTermEnum(reader,term);
                  }
                  else{
                      if(reuse != null){
                          reuse.resetToTerm(reader, term);
                          return reuse;
                      }else{
                          reuse =  new SubstringTermEnum(reader,term);
                      }
                      return reuse;
                  }
              }
              else{
//                  if(text.charAt(0) == '^'){
                      return new WildcardTermEnum(reader,term);
//                  }
//                  else{
//                      return new WildcardSubstringTermEnum(reader,term);
//                  }
              }
      }


      public String toString() { return "weight(" + this.query + ")"; }

      public Query getQuery() { return this.query; }
      public float getValue() { return value; }

      public float sumOfSquaredWeights() {
        queryWeight = idf * this.query.getBoost();             // compute query weight
        return queryWeight * queryWeight;           // square it
      }

      public void normalize(float queryNorm) {
        this.queryNorm = queryNorm;
        queryWeight *= queryNorm;                   // normalize query weight
        value = queryWeight * idf;                  // idf for document 
      }
      

      public Explanation explain(IndexReader reader, int doc)
        throws IOException {

        Explanation result = new Explanation();
        result.setDescription("weight("+getQuery()+" in "+doc+"), product of:");

        StringBuffer docFreqs = new StringBuffer();
        StringBuffer query = new StringBuffer();
        query.append('\"');
        for (int i = 0; i < terms.length; i++) {
          if (i != 0) {
            docFreqs.append(" ");
            query.append(" ");
          }

          Term term = (Term)terms[i];

          docFreqs.append(term.text());
          docFreqs.append("=");
          docFreqs.append(reader.docFreq(term));

          query.append(term.text());
        }
        query.append('\"');

        Explanation idfExpl =
          new Explanation(idf, "idf(" + this.query.field() + ": " + docFreqs + ")");

        // explain query weight
        Explanation queryExpl = new Explanation();
        queryExpl.setDescription("queryWeight(" + getQuery() + "), product of:");

        Explanation boostExpl = new Explanation(this.query.getBoost(), "boost");
        if (this.query.getBoost() != 1.0f)
          queryExpl.addDetail(boostExpl);
        queryExpl.addDetail(idfExpl);

        Explanation queryNormExpl = new Explanation(queryNorm,"queryNorm");
        queryExpl.addDetail(queryNormExpl);

        queryExpl.setValue(boostExpl.getValue() *
                           idfExpl.getValue() *
                           queryNormExpl.getValue());

        result.addDetail(queryExpl);

        // explain field weight
        Explanation fieldExpl = new Explanation();
        fieldExpl.setDescription("fieldWeight("+this.query.field() +":"+query+" in "+doc+
                                 "), product of:");

        Explanation tfExpl = scorer(reader).explain(doc);
        fieldExpl.addDetail(tfExpl);
        fieldExpl.addDetail(idfExpl);

        Explanation fieldNormExpl = new Explanation();
        byte[] fieldNorms = reader.norms(this.query.field());
        float fieldNorm =
          fieldNorms!=null ? Similarity.decodeNorm(fieldNorms[doc]) : 0.0f;
        fieldNormExpl.setValue(fieldNorm);
        fieldNormExpl.setDescription("fieldNorm(field="+this.query.field() +", doc="+doc+")");
        fieldExpl.addDetail(fieldNormExpl);

        fieldExpl.setValue(tfExpl.getValue() *
                           idfExpl.getValue() *
                           fieldNormExpl.getValue());

        result.addDetail(fieldExpl);

        // combine them
        result.setValue(queryExpl.getValue() * fieldExpl.getValue());

        if (queryExpl.getValue() == 1.0f)
          return fieldExpl;

        return result;
      }
    }
    
    private static class StaggeredMergedWeight extends PhraseWeight {
        public StaggeredMergedWeight(Searcher searcher,SubstringPhraseQuery query) throws IOException {
            super(searcher,query);
        }
        public Scorer scorer(IndexReader reader) throws IOException {
            Term[] terms = this.query.getTerms();
            if (terms.length == 0)            // optimize zero-term case
              return null;

            TermPositions[] tps = new TermPositions[terms.length];
            for (int i = 0; i < terms.length; i++) {
               Term[] matches = this.matches[i];
                
              if (matches == null || matches.length == 0) return null;
              TermPositions p = (matches.length > 1)?new MultipleTermPositions(reader,matches):reader.termPositions(matches[0]);
              tps[i] = p;
            }
            Scorer result = new StaggeredPhraseScorer(this,tps,query.getPositions(),this.query.getSlops(),similarity,reader.norms(this.query.field()));
            return result;
        }
    }
    private static class StaggeredMultiWeight extends PhraseWeight {
        public StaggeredMultiWeight(Searcher searcher,SubstringPhraseQuery query) throws IOException {
            super(searcher,query);
        }
        public Scorer scorer(IndexReader reader) throws IOException {
            Term[] terms = this.query.getTerms();
            if (terms.length == 0)            // optimize zero-term case
              return null;

            TermPositions[] tps = new TermPositions[terms.length];
            for (int i = 0; i < terms.length; i++) {
                Term[] matches = this.matches[i];
                
              if (matches == null || matches.length == 0) return null;
              TermPositions p = (matches.length > 1)?new MultipleTermPositions(reader,matches):reader.termPositions(matches[0]);
              tps[i] = p;
            }
//          @TODO write a scorer that accounts for character positions, to allow for uncertain word boundaries?
            Scorer result = new StaggeredPhraseScorer(this,tps,query.getPositions(),this.query.getSlops(),similarity,reader.norms(this.query.field()));
            return result;
        }
    }
    private static class ExactMultiWeight extends PhraseWeight {
        public ExactMultiWeight(Searcher searcher,SubstringPhraseQuery query) throws IOException {
            super(searcher,query);
        }
        public Scorer scorer(IndexReader reader) throws IOException {
            Term[] terms = this.query.getTerms();
            if (terms.length == 0)            // optimize zero-term case
              return null;

            TermPositions[] tps = new TermPositions[terms.length];
            for (int i = 0; i < terms.length; i++) {
                Term[] matches = this.matches[i];
                
              if (matches == null || matches.length == 0) return null;
              TermPositions p = (matches.length > 1)?new MultipleTermPositions(reader,matches):reader.termPositions(matches[0]);
              tps[i] = p;
            }
//          @TODO write a scorer that accounts for character positions, to allow for uncertain word boundaries?
            Scorer result;
            if(query.hasSlop){
                if(query.slopVaries){
                    result = new StaggeredPhraseScorer(this,tps,query.getPositions(),this.query.getSlops(),similarity,reader.norms(this.query.field()));
                }
                else{
                    result = new SloppyPhraseScorer(this,tps,query.getPositions(),similarity,this.query.getSlops()[0],reader.norms(this.query.field()));
                }
            }
            else{
                result = new ExactPhraseScorer(this,tps,query.getPositions(),similarity,reader.norms(this.query.field()));
            }
            return result;
        }
    }
    private static class ExactMergedWeight extends PhraseWeight {
        public ExactMergedWeight(Searcher searcher,SubstringPhraseQuery query) throws IOException {
            super(searcher,query);
        }
        public Scorer scorer(IndexReader reader) throws IOException {
            Term[] terms = this.query.getTerms();
            if (terms.length == 0)            // optimize zero-term case
              return null;

            TermPositions[] tps = new TermPositions[terms.length];
            for (int i = 0; i < terms.length; i++) {
                Term[] matches = this.matches[i];
                
              if (matches == null || matches.length == 0) return null;
              TermPositions p = (matches.length > 1)?new MultipleTermPositions(reader,matches):reader.termPositions(matches[0]);
              tps[i] = p;
            }
//          @TODO write a scorer that accounts for character positions, to allow for uncertain word boundaries?
            Scorer result;
            if(query.hasSlop){
                if(query.slopVaries){
                    result = new StaggeredPhraseScorer(this,tps,query.getPositions(),this.query.getSlops(),similarity,reader.norms(this.query.field()));
                }
                else{
                    result = new SloppyPhraseScorer(this,tps,query.getPositions(),similarity,this.query.getSlops()[0],reader.norms(this.query.field()));
                }
            }
            else{
                result = new ExactPhraseScorer(this,tps,query.getPositions(),similarity,reader.norms(this.query.field()));
            }
            return result;
        }
    }

    protected Weight createWeight(Searcher searcher) throws IOException {
      Term[] terms = getTerms();
        if (terms.length == 1) {            // optimize one-term case
        Term term =terms[0];
        Query termQuery = new SubstringQuery(term);
        termQuery.setBoost(getBoost());
        return termQuery.weight(searcher);
      }
        switch (this.scorer){
        case EXACT:
            return new ExactMergedWeight(searcher,this);
        case MULTIPOS:
            return new StaggeredMultiWeight(searcher,this);
        case (EXACT | MULTIPOS):
            return new ExactMultiWeight(searcher,this);
        default:
            return new StaggeredMergedWeight(searcher,this);
        }
    }

    /**
     * @see org.apache.lucene.search.Query#extractTerms(java.util.Set)
     */
    public void extractTerms(Set queryTerms) {
        for(Term t:getTerms()){
            queryTerms.add(t);
        }
    }

    /** Prints a user-readable version of this query. */
    public String toString(String f) {
        Term[] terms = getTerms();
        StringBuffer buffer = new StringBuffer();
        if (!field.equals(f)) {
            buffer.append(field);
            buffer.append(":");
        }

        buffer.append("\"");
        for (int i = 0; i < terms.length; i++) {
            int j = this.getPositions()[i];
            buffer.append(terms[i].text() + " (" + j + ")");
            if (i != terms.length-1)
                buffer.append(" ");
        }
        buffer.append("\"");

        if (getSlop() != 0) {
            buffer.append("~");
            buffer.append(getSlop());
        }

        buffer.append(ToStringUtils.boost(getBoost()));

        return buffer.toString();
    }

    /** Returns true iff <code>o</code> is equal to this. */
    public boolean equals(Object o) {
      if (!(o instanceof SubstringPhraseQuery))
        return false;
      SubstringPhraseQuery other = (SubstringPhraseQuery)o;
      return (this.getBoost() == other.getBoost())
        && (this.getSlop() == other.getSlop())
        &&  this.getTerms().equals(other.getTerms())
        && this.getPositions().equals(other.getPositions());
    }

    /** Returns a hash code value for this object.*/
    public int hashCode() {
      return Float.floatToIntBits(getBoost())
        ^ getSlop()
        ^ getTerms().hashCode()
        ^ getPositions().hashCode();
    }

    public static Highlighter getHighlighter(SubstringPhraseQuery query, String field, CachingTokenFilter tokens) throws IOException{
        Highlighter highlight = new Highlighter(FastHTMLFormatter.THREADSAFE_FORMATTER,BracketEncoder.THREADSAFE_ENCODER,new org.apache.lucene.search.highlight.PhraseScorer(query));
        return highlight;
    }

    public static CachingTokenFilter getCachedTokens(SubstringPhraseQuery query, TokenStream tokens){
        SubstringDelegate[] dels = getDelegates(query);
        return new CachingTokenFilter(new SubstringTermTokenFilter(tokens,dels));
    }

    public static SubstringDelegate[] getDelegates(SubstringPhraseQuery query){
        Term [] terms = query.getTerms();
        SubstringDelegate [] delegates = new SubstringDelegate[terms.length];
    
        for (int i=0;i<terms.length; i++){
          delegates[i] = 
              (terms[i].text().indexOf('?') == -1 && terms[i].text().indexOf('*') == -1)?
                      new SubstringTermDelegate(terms[i]):new WildcardSubstringDelegate(terms[i]);
        }
        return delegates;
    }

  }