package info.papyri.epiduke.lucene;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.util.ToStringUtils;

import info.papyri.epiduke.lucene.bigrams.*;

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

    public class WildcardSubstringQuery extends MultiTermQuery {
    	private final info.papyri.epiduke.lucene.bigrams.WildcardSubstringDelegate delegate;
    	private final String [] knownTerms;
        private final Term substring;
      /** Constructs a query for terms starting with <code>substring</code>. */
      public WildcardSubstringQuery(Term substring, IndexSearcher bigrams) throws IOException {
          super(substring);
          this.substring = substring;
          this.delegate = new info.papyri.epiduke.lucene.bigrams.WildcardSubstringDelegate(bigrams,substring);
          this.knownTerms = this.delegate.matches();
      }

      protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
          return new info.papyri.epiduke.lucene.FilteredTermEnum(this.substring,this.knownTerms,reader);
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
        if (!(o instanceof WildcardSubstringQuery))
          return false;
        WildcardSubstringQuery other = (WildcardSubstringQuery)o;
        return (this.getBoost() == other.getBoost())
          && this.substring.equals(other.substring);
      }

      /** Returns a hash code value for this object.*/
      public int hashCode() {
        return Float.floatToIntBits(getBoost()) ^ substring.hashCode() ^ 0x6634D93C;
      }
                 
    }
