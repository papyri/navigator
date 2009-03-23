package info.papyri.ddbdp.parser;

import info.papyri.epiduke.lucene.spans.*;
import info.papyri.ddbdp.servlet.DDBDPServlet;
import info.papyri.ddbdp.servlet.Sru;
import info.papyri.epiduke.lucene.Indexer;
import info.papyri.epiduke.lucene.SubstringQuery;
import info.papyri.epiduke.lucene.WildcardSubstringQuery;
import info.papyri.epiduke.lucene.analysis.AnchoredTokenStream;
import info.papyri.epiduke.lucene.analysis.AncientGreekAnalyzer;
import info.papyri.epiduke.lucene.analysis.AncientGreekQueryAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SubstringPhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.TermScorer;
import org.apache.lucene.search.spans.*;
import org.z3950.zing.cql.CQLAndNode;
import org.z3950.zing.cql.CQLBooleanNode;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLNotNode;
import org.z3950.zing.cql.CQLOrNode;
import org.z3950.zing.cql.CQLProxNode;
import org.z3950.zing.cql.CQLRelation;
import org.z3950.zing.cql.CQLSortNode;
import org.z3950.zing.cql.CQLTermNode;
import org.z3950.zing.cql.Modifier;

public abstract class CQLSpanQueryParser {
    private static final List<String> SR_PARMS = srParms();
    private static final List<String> SCAN_PARMS = scanParms();
    private static final SpanQuery [] TEMPLATE = new SpanQuery[0];
    public static final String CQL_ATTR = "info.papyri.CQL";
    public static final String FILENAMES_ARRAY_ATTR = "info.papyri.FNAMES";
    public static final String FRAGMENTS_ARRAY_ATTR = "info.papyri.FRAGMENTS";
    public static final String NEXT_RECORD_POS_ATTR = "info.papyri.NEXT";
    public static final String NUM_RECS_ATTR = "info.papyri.NUMRECS";
    public static final String QUERY_TIMER_ATTR = "info.papyri.STARTTIME";
    public static final String XQL_ATTR = "info.papyri.XQL";
    public static final String DIAGNOSTIC_URI_ATTR = "info.papyri.DIAGNOSTIC.URI";
    public static final String DIAGNOSTIC_DETAIL_ATTR = "info.papyri.DIAGNOSTIC.DETAIL";
    public static final String DIAGNOSTIC_MESSAGE_ATTR = "info.papyri.DIAGNOSTIC.MESSAGE";
     static final String TEXT_FIELD = "text".intern();
     static final String FNAME_FIELD = "fileName".intern();
     static final int MODE_NONE = 0;
     static final int MODE_FILTER_CAPITALS = 1;
     static final int MODE_FILTER_DIACRITIC = 2;
     static final int MODE_FILTER_CAPITALS_AND_DIACRITICS = MODE_FILTER_CAPITALS + MODE_FILTER_DIACRITIC;
     static final int MODE_BETA = 4;
     static final int MODE_BETA_FILTER_CAPITALS = MODE_BETA + MODE_FILTER_CAPITALS;
     static final int MODE_BETA_FILTER_DIACRITICS = MODE_BETA + MODE_FILTER_DIACRITIC; 
     static final int MODE_BETA_FILTER_ALL = MODE_BETA + MODE_FILTER_CAPITALS + MODE_FILTER_DIACRITIC;

      public static String getSafeUTF8(String iso){
         try{
             byte [] bytes = iso.getBytes("ISO-8859-1");
             String result =  new String(bytes,"UTF-8");
             return result.trim();
         }
         catch (Throwable t){
             return "";
         }
     }

     static String getField(int mode){
         switch (mode){
         case MODE_FILTER_CAPITALS:
             return Indexer.WORD_SPAN_TERM_LC.intern();
         case MODE_FILTER_DIACRITIC:
             return Indexer.WORD_SPAN_TERM_DF.intern();
         case MODE_FILTER_CAPITALS_AND_DIACRITICS:
             return Indexer.WORD_SPAN_TERM_FL.intern();
         case MODE_BETA:
             return Indexer.WORD_SPAN_TERM_DF.intern();
         case MODE_BETA_FILTER_CAPITALS:
             return Indexer.WORD_SPAN_TERM_FL.intern();
         case MODE_BETA_FILTER_DIACRITICS:
             return Indexer.WORD_SPAN_TERM_DF.intern();
         case MODE_BETA_FILTER_ALL:
             return Indexer.WORD_SPAN_TERM_FL.intern();
         default:
             return Indexer.WORD_SPAN_TERM.intern();
         }

     }

     static SpanQuery getSpanQuery(CQLTermNode tNode, IndexSearcher[] bigrams) throws IOException {
         String term = Sru.getTerm(tNode);

         CQLRelation relation = tNode.getRelation();
         int mode = Sru.getMode(relation); 
         String fieldName = getField(mode);
         term = DDBDPServlet.QUERY_ANALYZERS[mode].tokenStream(term, new StringReader(term)).next().termText();
         Term searchTerm = new Term(fieldName,term);
         if (term.indexOf('*') != -1 || term.indexOf('?') != -1){
             return new SubstringSpanTermQuery(searchTerm,bigrams[mode&MODE_FILTER_CAPITALS_AND_DIACRITICS]);
         }
         else{
             if (term.charAt(0) == AnchoredTokenStream.ANCHOR){
                 if(term.charAt(term.length()-1) ==  AnchoredTokenStream.ANCHOR){
                     return new SpanTermQuery(searchTerm);
                 }
                 else{
                     return new SubstringSpanTermQuery(searchTerm,bigrams[mode&MODE_FILTER_CAPITALS_AND_DIACRITICS]);
                 }
             }
             else{
                 return new SubstringSpanTermQuery(searchTerm,bigrams[mode&MODE_FILTER_CAPITALS_AND_DIACRITICS]);
             }
         }
     }

     
     static SpanQuery getSpanQuery(CQLProxNode pNode, IndexSearcher[] bigrams) throws IOException {

         int slop = Sru.getSlop(pNode);
         boolean ordered = Sru.getOrdered(pNode);

         java.util.Vector<SpanQuery> queries = new Vector<SpanQuery>();
         if(pNode.left instanceof CQLTermNode){
             queries.add( getSpanQuery((CQLTermNode)pNode.left,bigrams));
         }
         else if(pNode.left instanceof CQLProxNode){
             CQLProxNode left = (CQLProxNode)pNode.left;
             int lSlop = Sru.getSlop(left);
             SpanQuery lQuery = getSpanQuery(left,bigrams);
             boolean lOrdered = Sru.getOrdered(left);
             if(lQuery instanceof SpanNearQuery && slop == lSlop && lOrdered == ordered){
                 SpanNearQuery snq = (SpanNearQuery)lQuery;
                 for(SpanQuery clause:snq.getClauses()){
                     queries.add(clause);
                 }
             }
             else {
                 queries.add(lQuery);
             }
         }
         else if (pNode.left instanceof CQLSortNode){
             SpanQuery q = getSpanQuery((CQLSortNode)pNode.left,bigrams);
             if(q instanceof SpanNearQuery){
                 SpanNearQuery snq = (SpanNearQuery)q;
                 if(snq.getSlop() == slop && snq.isInOrder() == ordered){
                     for(SpanQuery clause:snq.getClauses()){
                         queries.add(clause);
                     }
                 }
                 else{
                     queries.add(q);
                 }
             }
             else queries.add(q);
         }
         else {
             throw new IllegalArgumentException("Unexpected left node: " +pNode.left.getClass().getName());
         }
         
         if(pNode.right instanceof CQLTermNode){
             queries.add( getSpanQuery((CQLTermNode)pNode.left,bigrams));
         }
         else if(pNode.right instanceof CQLProxNode){
             CQLProxNode right = (CQLProxNode)pNode.right;
             int rSlop = Sru.getSlop(right);
             boolean rOrdered = Sru.getOrdered(right);
             if(slop ==rSlop && rOrdered == ordered){
                 for(SpanQuery clause:((SpanNearQuery)getSpanQuery(right,bigrams)).getClauses()){
                     queries.add(clause);
                 }
             }
             else {
                 queries.add(getSpanQuery(right,bigrams));
             }
         }
         else if (pNode.right instanceof CQLSortNode){
             SpanQuery q = getSpanQuery((CQLSortNode)pNode.right,bigrams);
             if(q instanceof SpanNearQuery){
                 SpanNearQuery snq = (SpanNearQuery)q;
                 if(snq.getSlop() == slop && snq.isInOrder() == ordered){
                     for(SpanQuery clause:snq.getClauses()){
                         queries.add(clause);
                     }
                 }
                 else{
                     queries.add(q);
                 }
             }
             else queries.add(q);
         }
         else {
             throw new IllegalArgumentException("Unexpected right node: " +pNode.right.getClass().getName());
         }
         
         SpanNearQuery result = new SpanNearQuery(queries.toArray(TEMPLATE),slop,ordered);
         return result;
     }
     
     static SpanQuery getSpanQuery(CQLSortNode sNode, IndexSearcher[] bigrams) throws IOException {
         CQLNode subtree = sNode.subtree;
         if(subtree instanceof CQLProxNode){
             return getSpanQuery((CQLProxNode)subtree,bigrams);
         }
         else if(subtree instanceof CQLTermNode){
             return getSpanQuery((CQLTermNode)subtree,bigrams);
         }
         else if(subtree instanceof CQLSortNode){
             return getSpanQuery((CQLSortNode)subtree,bigrams);
         }
         else {
             throw new IllegalArgumentException("Unexpected sort subtree node: " +subtree.getClass().getName());
         }
     }
     
     static Object[] processNode(CQLNode bNode, IndexSearcher reader, IndexSearcher [] bigrams) throws IOException {
         Object [] result = new Object[3]; // {query, scorer,sort}
         if (bNode instanceof CQLAndNode){
             CQLAndNode qNode = (CQLAndNode)bNode;
             BooleanQuery query = new BooleanQuery();
//             BooleanScorer scorer = new AndScorer();
             Object [] left = processNode(qNode.left,reader,bigrams);
             Object [] right = processNode(qNode.right,reader,bigrams);
//             scorer.setLeft((Scorer)left[1]);
//             scorer.setRight((Scorer)right[1]);
             query.add((Query)left[0], BooleanClause.Occur.MUST);
             query.add((Query)right[0], BooleanClause.Occur.MUST);
             result[0] = query;
             result[1] = new QueryScorer(query);
             return result;
         }
         if (bNode instanceof CQLOrNode){
             CQLOrNode qNode = (CQLOrNode)bNode;
             BooleanQuery query = new BooleanQuery();
//             BooleanScorer scorer = new OrScorer();
             Object [] left = processNode(qNode.left,reader,bigrams);
             Object [] right = processNode(qNode.right,reader,bigrams);
//             scorer.setLeft((Scorer)left[1]);
//             scorer.setRight((Scorer)right[1]);
             query.add((Query)left[0], BooleanClause.Occur.SHOULD);
             query.add((Query)right[0], BooleanClause.Occur.SHOULD);
             result[0] = query;
             result[1] = new QueryScorer(query);
             return result;
         }
         if (bNode instanceof CQLNotNode){
             CQLNotNode qNode = (CQLNotNode)bNode;
             BooleanQuery query = new BooleanQuery();
//             BooleanScorer scorer = new NotScorer();
             Object [] left = processNode(qNode.left,reader,bigrams);
             Object [] right = processNode(qNode.right,reader,bigrams);
//             scorer.setLeft((Scorer)left[1]);
//             scorer.setRight((Scorer)right[1]);
             query.add((Query)left[0], BooleanClause.Occur.MUST);
             query.add((Query)right[0], BooleanClause.Occur.MUST_NOT);
             result[0] = query;
             result[1] = new QueryScorer(query);
             return result;
         }
         if (bNode instanceof CQLProxNode){
             CQLProxNode qNode = (CQLProxNode)bNode;
             SpanQuery query = getSpanQuery(qNode,bigrams);
             result[0] = query;
             result[1] = new QueryScorer(query);
             return result;
         }
         if (bNode instanceof CQLSortNode){
             CQLSortNode qNode = (CQLSortNode)bNode;
             Sort sort = Sru.getSort(qNode);
             Object [] qinfo = processNode(qNode.subtree,reader,bigrams);
             result[2] = sort;
             result[0] = qinfo[0];
             result[1] = new QueryScorer((Query)qinfo[0]);
         }

             CQLTermNode qNode = (CQLTermNode)bNode;
             Query query = getSpanQuery(qNode,bigrams);
             result[0] = query;
             result[1] = TermScorer.getTermScorer(query);
             return result;
     }




     
     static List<String> srParms(){
         String [] names = new String[]{"operation","version","query","startRecord","maximumRecords","recordPacking","recordSchema","recordXPath","resultSetTTL","sortKeys","stylesheet","extraRequestData"};
         return java.util.Arrays.asList(names);
     }
     static List<String> scanParms(){
         String [] names = new String[]{"operation","version","scanClause","responsePosition","maximumTerms","stylesheet","extraRequestData"};
         return java.util.Arrays.asList(names);
     }
     static class ProxTuple{
         int offset;
         String term;
     }
     static class SetOp {
         public static SetOp AND = new SetOp("AND");
         public static SetOp OR = new SetOp("OR");
         public static SetOp NOT = new SetOp("NOT");
         private final String label;
         private SetOp(){
             this.label = "NONE";
         };
         private SetOp(String label){
             this.label = label;
         }
         public String toString(){
             return this.label;
         }
     }
}
