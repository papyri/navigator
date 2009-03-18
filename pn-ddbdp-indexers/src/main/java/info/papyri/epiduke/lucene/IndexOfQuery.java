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

    public class IndexOfQuery extends Query {
      private Term substring;
      private final char [] textChars;

      /** Constructs a query for terms starting with <code>substring</code>. */
      public IndexOfQuery(Term substring) {
        this.substring = substring;
        textChars = substring.text().toCharArray();
      }

      /** Returns the substring of this query. */
      public Term getSubstring() { return substring; }

      public Query rewrite(IndexReader reader) throws IOException {
        BooleanQuery query = new BooleanQuery(true);
        query.setMinimumNumberShouldMatch(1);
        TermEnum enumerator = reader.terms(this.substring.createTerm(""));

        try {
          String substringField = substring.field();
          String substring = this.substring.text();
          do {
            Term term = enumerator.term();
            if (term != null &&
                term.field() == substringField) // interned comparison 
            {   
                if (term.text().indexOf(substring) == -1) continue;
                //if (!term.text().contains(substring.text())) continue;
              TermQuery tq = new TermQuery(term);
              tq.setBoost(getBoost());
              query.add(tq, BooleanClause.Occur.SHOULD);
            } else {
              break;
            }
          } while (enumerator.next());
        } finally {
          enumerator.close();
        }
        return query;
      }

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
        if (!(o instanceof IndexOfQuery))
          return false;
        IndexOfQuery other = (IndexOfQuery)o;
        return (this.getBoost() == other.getBoost())
          && this.substring.equals(other.substring);
      }

      /** Returns a hash code value for this object.*/
      public int hashCode() {
        return Float.floatToIntBits(getBoost()) ^ substring.hashCode() ^ 0x6634D93C;
      }

            
    }
