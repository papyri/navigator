package info.papyri.epiduke.lucene;

import java.io.IOException;
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

    public class SubstringTermDelegate implements SubstringDelegate {
      private Term substring;
      private char [] termChars;

      private final short[] baseShifts;
      private final short[] extShifts;
      private final short[] latinShifts;
      private short[] gs;
      private short def;
      private short last;
      

      /** Constructs a query for terms starting with <code>substring</code>. */
      public SubstringTermDelegate() {
          baseShifts = new short[256];
          extShifts = new short[256];
          latinShifts = new short[256];
      }
      public SubstringTermDelegate(Term substring) {
          this();
          setTerm(substring);
      }
      
      public void setTerm(Term substring){
          this.substring = substring;
          termChars = substring.text().toCharArray();
          def = (short)(termChars.length);
          last = (short)(termChars.length - 1);
          Arrays.fill(baseShifts,def);
          Arrays.fill(extShifts, def);
          Arrays.fill(latinShifts, def);
          SubstringQuery.initBCShiftMaps(termChars,baseShifts,extShifts,latinShifts);
          gs = SubstringQuery.getGSShiftArray(termChars);
      }

      /** Returns the substring of this query. */
      public Term substringTerm() { return substring; }
      
      public boolean matches(char [] buf, int start, int end){
          return SubstringQuery.matchShanghai(baseShifts, extShifts, latinShifts,gs, termChars,buf, start, end, last, def);
      }
      
      public boolean matches(String text) throws IOException {
          return SubstringQuery.matchShanghai(baseShifts, extShifts, latinShifts,gs, termChars,text.toCharArray(), last, def);
      }
      public String [] matches(IndexReader reader) throws IOException {
          TermEnum enumerator = reader.terms(this.substring.createTerm(""));

          java.util.ArrayList<String> result = new java.util.ArrayList<String>();
          try {
              String substringField = substring.field();
              do {
                Term term = enumerator.term();
                if (term != null &&
                    term.field() == substringField) // interned comparison 
                {   
                    if (!matches(term.text())) continue;
                  result.add(term.text());
                } else {
                  break;
                }
              } while (enumerator.next());
            } finally {
              enumerator.close();
            }
        return result.toArray(info.papyri.util.ArrayTypes.STRING);
      }
            
    }
