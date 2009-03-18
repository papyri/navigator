package info.papyri.epiduke.lucene;

import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.ToStringUtils;
import java.util.Arrays;

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

    public class WildcardSegmentDelegate {
      private String substring;
      private final char [] termChars;

      private final short[] baseShifts;
      private final short[] extShifts;
      private final short[] latinShifts;
      private final short[] gs;
      private final short def;
      private final short last;
      /** Constructs a query for terms starting with <code>substring</code>. */
      public WildcardSegmentDelegate(String substring) {
        this.substring = substring;
        termChars = substring.toCharArray();
        def = (short)(termChars.length);
        last = (short)(termChars.length - 1);
        gs = SubstringQuery.getGSShiftArray(termChars);
        baseShifts = new short[256];
        extShifts = new short[256];
        latinShifts = new short[256];
        Arrays.fill(baseShifts,def);
        Arrays.fill(extShifts,def);
        Arrays.fill(latinShifts,def);
        SubstringQuery.initBCShiftMaps(termChars,baseShifts,extShifts,latinShifts);
      }

      /** Returns the substring of this query. */
      public String getSubstring() { return substring; }
/**
 * open question:  changing the maximal shift from Shanghai to pattern length (from length + 1)
 * correct the bug whereby the entire pattern may be skipped, but does it create the condition
 * below, in which patterns must be at least 3 characters long?
 */
      public BitSet offsets(String text) throws IOException {
          char[]textChars = text.toCharArray();

          BitSet matches = null;
          switch (termChars.length){
          case 0:
              return new BitSet(textChars.length);
          case 1:
              matches = new BitSet(textChars.length);
              for (int i=0;i<textChars.length;i++){
                  if(textChars[i]==termChars[0]) matches.set(i);
              }
              return matches;
          case 2:
              matches = new BitSet(textChars.length);
              for (int i=0;i<textChars.length-1;i++){
                  if(textChars[i]==termChars[0] && textChars[i+1]==termChars[1]) matches.set(i);
              }
              return matches;
          default:
              return matchPositions(baseShifts, extShifts, latinShifts, gs, termChars,text.toCharArray(), gs[0], last);
          }
      }

      public BitSet offsets(char[] text, int start, int end) throws IOException {

          BitSet matches = null;
          switch (termChars.length){
          case 0:
              return new BitSet(end - start);
          case 1:
              matches = new BitSet(end - start);
              for (int i=start;i<end;i++){
                  if(text[i]==termChars[0]) matches.set(i);
              }
              return matches;
          case 2:
              matches = new BitSet(end - start);
              for (int i=start;i<(end - -1);i++){
                  if(text[i]==termChars[0] && text[i+1]==termChars[1]) matches.set(i);
              }
              return matches;
          default:
              return matchPositions(baseShifts, extShifts, latinShifts, gs, termChars,text, gs[0], last);
          }
      }


      /**
       * http://portal.acm.org/citation.cfm?id=355231&dl=GUIDE&coll=GUIDE
       * corrected for max increment = pattern length, to prevent stupid bug
       */
      private static BitSet matchPositions(short[] baseShifts, short[] extShifts, short[] latinShifts,short [] gs,char [] P, char [] T, final short def, final short last){
          BitSet result = new BitSet(T.length);
          final short diff = (short)(T.length - P.length);
          short i = 0;
          short m = (short)P.length;
          short shift = 0;
          short offset = 0;
          short delta;
          short j;

          scan:
          while (i <= diff){
              compare:
              for (j = last; (j >= 0); j--){
                  if (P[j]!=T[i+j]){
                      break compare;
                  }
                  if (j == 0){
                      result.set(i);
                      i+=gs[0];
                      continue scan;
                  }
              }

          delta = (short)(i+m);
          shift = (delta < T.length)?SubstringQuery.shift(baseShifts,extShifts,latinShifts,delta,T,def):def;
          offset = shift;
          while(shift == def && (delta+offset)<=diff){
              shift = SubstringQuery.shift(baseShifts,extShifts,latinShifts,delta+offset,T,def);
              offset += shift;
          }

          if (gs[j+1] > offset){
              offset = gs[j+1];
          }
          i += offset;
          }
          
          return result;
      }
      
    }
