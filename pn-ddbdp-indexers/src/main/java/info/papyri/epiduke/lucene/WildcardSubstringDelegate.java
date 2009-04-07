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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class WildcardSubstringDelegate implements SubstringDelegate {
    private static final Pattern DELIMITER = Pattern.compile("[\\*\\?]");

    private Term substring;


    private final WildcardSegmentDelegate[] delegates;
    private final String [] patterns;
    private final char [] delimiters;

    /** Constructs a query for terms starting with <code>substring</code>. */
    public WildcardSubstringDelegate(Term substring) {
        this.substring = substring;
        String text = substring.text();
        text = text.replaceAll("[\\?\\*]{2,}", "*");

        patterns = text.split("[\\?\\*]");

        delimiters = new char[patterns.length - 1];
        delegates = new WildcardSegmentDelegate[patterns.length];
        int i;
        for (i=0;i<patterns.length;i++){
            delegates[i] = new WildcardSegmentDelegate(patterns[i]);
        }
        i = 0;
        Matcher matcher = DELIMITER.matcher(text);
        while (i<delimiters.length){
            matcher.find();
            delimiters[i] = matcher.group().charAt(0);
            i++;
        }
    }

    /** Returns the substring of this query. */
    public Term substringTerm() { return substring; }
    
    public boolean matches(char [] termBuf, int start, int end) throws IOException {
        BitSet[] matches = new BitSet[delegates.length];
        matches[0] = delegates[0].offsets(termBuf,start,end);
        if (matches[0].cardinality() == 0) return false;

        termParts:
            for (int i=1;i<delegates.length;i++){
                matches[i] = delegates[i].offsets(termBuf,start,end);
                if (matches[i].cardinality() == 0) {
                    return false;
                }
                int curr = -1;
                boolean checked =false;
                checkPrior:
                    while ((curr = matches[i - 1].nextSetBit(curr+1)) > -1 ){
                        if (matches[i].nextSetBit(curr) > -1){
                            checked = true;
                            break;
                        }
                    }
                if (!checked) return false;

                if (delimiters[i-1] == '?'){
                    curr = -1;
                    int prev = 0;
                    checked = false;
                    checkZeroOrOne:
                        while ((curr = matches[i].nextSetBit(curr+1)) > -1 ){
                            prev = curr - patterns[i -1].length();
                            if (prev < 0) continue;
                            if (matches[i-1].get(prev) || (prev > 0 && matches[i-1].get(prev - 1))){
                                checked = true;
                                break checkZeroOrOne;
                            }
                        }
                    if (!checked) return false;
                }
            }
        return true;
    }
    
    public boolean matches(String text) throws IOException {
        BitSet[] matches = new BitSet[delegates.length];
        matches[0] = delegates[0].offsets(text);
        if (matches[0].cardinality() == 0) return false;

        termParts:
            for (int i=1;i<delegates.length;i++){
                matches[i] = delegates[i].offsets(text);
                if (matches[i].cardinality() == 0) {
                    return false;
                }
                int curr = -1;
                boolean checked =false;
                checkPrior:
                    while ((curr = matches[i - 1].nextSetBit(curr+1)) > -1 ){
                        if (matches[i].nextSetBit(curr) > -1){
                            checked = true;
                            break;
                        }
                    }
                if (!checked) return false;

                if (delimiters[i-1] == '?'){
                    curr = -1;
                    int prev = 0;
                    checked = false;
                    checkZeroOrOne:
                        while ((curr = matches[i].nextSetBit(curr+1)) > -1 ){
                            prev = curr - patterns[i -1].length();
                            if (prev < 0) continue;
                            if (matches[i-1].get(prev) || (prev > 0 && matches[i-1].get(prev - 1))){
                                checked = true;
                                break checkZeroOrOne;
                            }
                        }
                    if (!checked) return false;
                }
            }
        return true;
        }

    public String [] matches(IndexReader reader) throws IOException {
        TermEnum enumerator = reader.terms(this.substring.createTerm(""));

        java.util.ArrayList<String> result = new java.util.ArrayList<String>();

        try {
            String substringField = substring.field();
            terms:
                do {
                    Term term = enumerator.term();
                    if (term != null &&
                            term.field().equals(substringField)) // interned comparison
                    {   
                        String text = term.text();
                        if (matches(text))result.add(text);

                    } else {
                        break terms;
                    }
                } while (enumerator.next());
        } finally {
            enumerator.close();
        }
        return result.toArray(info.papyri.util.ArrayTypes.STRING);
    }

}
