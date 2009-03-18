package info.papyri.epiduke.lucene;

import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.highlight.BracketEncoder;
import org.apache.lucene.search.highlight.FastHTMLFormatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.SubstringScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.util.ToStringUtils;

    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */


    /** A Query that matches documents containing terms with a specified prefix. A PrefixQuery
     * is built by QueryParser for input like <code>app*</code>. */

    public class SubstringQuery extends MultiTermQuery {
      protected Term substring;

      /** Constructs a query for terms starting with <code>substring</code>. */
      public SubstringQuery(Term substring) {
          super(substring);
        this.substring = substring;
        if (substring.text().length() < 2) throw new IllegalArgumentException("Substring pattern must be at least 2 characters: \"" + substring.text() + "\"");
      }
      
      

      @Override
    protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
        return new SubstringTermEnum(reader,this.substring);
    }



    /** Returns the substring of this query. */
      public Term getSubstring() { return substring; }

      /** Prints a user-readable version of this query. */
      public String toString(String field) {
        StringBuffer buffer = new StringBuffer();
        if (!substring.field().equals(field)) {
          buffer.append(substring.field());
          buffer.append(":");
        }
        buffer.append(substring.text());
        buffer.append('*');
        buffer.append(ToStringUtils.boost(getBoost()));
        return buffer.toString();
      }

      /** Returns true iff <code>o</code> is equal to this. */
      public boolean equals(Object o) {
        if (!(o instanceof SubstringQuery))
          return false;
        SubstringQuery other = (SubstringQuery)o;
        return (this.getBoost() == other.getBoost())
          && this.substring.equals(other.substring);
      }

      /** Returns a hash code value for this object.*/
      public int hashCode() {
        return Float.floatToIntBits(getBoost()) ^ substring.hashCode() ^ 0x6634D93C;
      }
      public static Highlighter getHighlighter(SubstringQuery query, IndexReader reader) throws IOException{
        Scorer scorer = new SubstringScorer(query);
        Highlighter highlight = new Highlighter(FastHTMLFormatter.THREADSAFE_FORMATTER,BracketEncoder.THREADSAFE_ENCODER,scorer);
        return highlight;
    }



    public static CachingTokenFilter getCachedTokens(SubstringQuery query, TokenStream tokens){
        return new CachingTokenFilter(tokens);
    }



    public static SubstringDelegate[] getDelegates(SubstringQuery query){
        Term term = query.getSubstring();
        if (term.text().indexOf('?') == -1 || term.text().indexOf('*') == -1){
           return new WildcardSubstringDelegate[]{ new WildcardSubstringDelegate(term)};
        }
        return new SubstringTermDelegate[]{new SubstringTermDelegate(term)};
    }



    /**
       * This match method is described in
       * 1990. Sunday, Daniel M. 'A very fast substring search algorithm'. "Communications of the ACM."
       * http://portal.acm.org/citation.cfm?id=79184
       * @param p
       * @param t
       * @return
       */
      private static boolean matchQuickSearch(char[] p, char[] t){

          final int diff = t.length - p.length;
          final int k1init = p.length - 1;
          int k = 0; // text pos
          int i;
          int k1;
          scan:
          while (k < diff){
              for (i = 0; (i < p.length && p[i] == t[k+i] ); i++){}
              if (i == p.length) return true;
              offset:
              for(k1 = k1init; k1 >=0; k1--){
                  if (p[k1]==t[k+ p.length]){
                      k += (p.length - k1);
                      continue scan;
                  }
              }
              k += (p.length);
          }
          return false;
      }
      
      private static boolean matchQuickSearchPrecompute(char[] p, char[] t, HashMap<Character,Integer> shifts, final int def){

          final int diff = t.length - p.length;
          int k = 0; // text pos
          int i;
          Integer k1;

          while (k < diff){
              for (i = 0; (i < p.length && p[i] == t[k+i] ); i++){}
              if (i == p.length) return true;
              k1 = shifts.get(Character.valueOf(t[k+ p.length]));    
              if (k1 != null) k += k1.intValue();
              else k += def;
          }
          return false;
      }
      /**
       * http://doi.acm.org/10.1145/355214.355231
       */
      public static boolean matchShanghai(short[] baseShifts, short[] extShifts, short[] latinShifts,short [] gs,char [] P, char [] T, final short last, final short def){

          final short diff = (short)(T.length - P.length);
          short i = 0;
          short m = (short)(P.length);
          short lastPos = (short)(m - 1);
          short shift = 0;
          short offset = 0;
          short delta;
          short j;

          scan:
          while (i <= diff){
              compare:
              for (j = lastPos; (j >= 0); j--){
                  if (P[j]!=T[i+j]){
                      break compare;
                  }
                  if (j == 0){
                      return true;
                  }
              }

              delta = (short)(i+m);
              shift = (delta < T.length)?shift(baseShifts,extShifts,latinShifts,delta,T,def):def;
              offset = shift;
              while(shift == def && (delta+offset)<=diff){
                  shift = shift(baseShifts,extShifts,latinShifts,delta+offset,T,def);
                  offset += shift;
              }
              

              if (gs[j+1] > offset){
                  offset = gs[j+1];
              }
              i += offset;
          }
          
          return false;
      }
      public static boolean matchShanghai(short[] baseShifts, short[] extShifts, short[] latinShifts,short [] gs,char [] P, char [] T, int start, int end, final short last, final short def){

          final short diff = (short)(end - P.length);
          short i = (short)start;
          short m = (short)(P.length);
          short lastPos = (short)(m - 1);
          short shift = 0;
          short offset = 0;
          short delta;
          short j;

          scan:
          while (i <= diff){
              compare:
              for (j = lastPos; (j >= 0); j--){
                  if (P[j]!=T[i+j]){
                      break compare;
                  }
                  if (j == 0){
                      return true;
                  }
              }

              delta = (short)(i+m);
              shift = (delta < T.length)?shift(baseShifts,extShifts,latinShifts,delta,T,def):def;
              offset = shift;
              while(shift == def && (delta+offset)<=diff){
                  shift = shift(baseShifts,extShifts,latinShifts,delta+offset,T,def);
                  offset += shift;
              }
              

              if (gs[j+1] > offset){
                  offset = gs[j+1];
              }
              i += offset;
          }
          
          return false;
      }
      
      static short shift(short[] baseShifts,short[]extShifts,short[]latinShifts,int pos, char[] T, short def){
          short result = 0;
          if (pos < T.length){
              if (T[pos] > '\u1EEE'){
                  result = extShifts[T[pos] & '\u00FF'];
              }
              else if (T[pos] > '\u036F'){
                  result = baseShifts[T[pos] & '\u00FF'];
              }
              else {
                  result = latinShifts[T[pos] & '\u00FF'];
              }
          }
          else result = def;
          return result;
      }
      
      public static void initBCShiftMaps(char[]pattern, short[] baseShifts, short [] extShifts,boolean debug){
          for(int i = 0; i < pattern.length; i++){
              int base = (pattern[i] & '\u00FF');
              if(pattern[i] > '\u1EEE'){
                  extShifts[base] = (short)(pattern.length - i);
              }
              else if(pattern[i] > '\u036F'){
                  baseShifts[base] = (short)(pattern.length - i);
              }
          }
      }

      public static void initBCShiftMaps(char[]pattern, short[] baseShifts, short [] extShifts, short[] latinShifts){
          for(int i = 0; i < pattern.length; i++){
              int base = (pattern[i] & '\u00FF');
              if(pattern[i] > '\u1EEE'){
                  extShifts[base] = (short)(pattern.length - i);
              }
              else if(pattern[i] > '\u036F'){
                  baseShifts[base] = (short)(pattern.length - i);
              }
              else {
                  latinShifts[base] = (short)(pattern.length - i);
              }
          }
      }
      
      public static short [] getGSShiftArray(char[]p){
          short [] shifts = new short[p.length + 1];
          shifts[0] = (short)p.length;
          for (int jPlus = 1;jPlus < shifts.length; jPlus++){
              short min = (short)p.length;
              int j = jPlus - 1;
              for (short s = 0;s<p.length;s++){
                  if (cond1(j,s,p) && cond2(j,s,p) && s < min ) min = s;
              }
              shifts[jPlus] = min;
          }
          return shifts;
      }
      private static boolean cond1(int j, int s, char[]P){
          for (int k = j+1;k<P.length;k++){
              if (!(k <= s || P[k - s] == P[k])){
                  return false;
              }
          }
          return true;
      }
      private static boolean cond2(int j, int s, char[]P){
          if (s < j && P[j-s] == P[j]) return false;
          return true;
      }
            
    }
