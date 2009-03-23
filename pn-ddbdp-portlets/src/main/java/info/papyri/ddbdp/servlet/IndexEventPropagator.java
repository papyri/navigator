package info.papyri.ddbdp.servlet;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.ParallelReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.sql.Connection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet; 
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;

import info.papyri.ddbdp.util.ArrayIterator;
import info.papyri.epiduke.lucene.BigramIndexer;
import info.papyri.epiduke.lucene.Indexer;
import info.papyri.metadata.*;

public class IndexEventPropagator implements Runnable {
    private static final Logger LOG = Logger.getLogger(IndexEventPropagator.class);
    private final static long ONE_MIN = 60000;
    private final static long TEN_MIN = 10 * ONE_MIN;
    private final static Term PLAIN = new Term(Indexer.WORD_SPAN_TERM,"");
    private final static Term DF = new Term(Indexer.WORD_SPAN_TERM_DF,"");
    private final static Term LC = new Term(Indexer.WORD_SPAN_TERM_LC,"");
    private final static Term FL = new Term(Indexer.WORD_SPAN_TERM_FL,"");
    private final HashSet<IndexEventListener> listeners = new HashSet<IndexEventListener>();
    private IndexEvent docEvent;
    private SearcherEvent bigramEvent;
    private IndexReader DOC_READER;
    private IndexSearcher SEARCHER;
    private IndexSearcher BIGRAMS;
    private IndexSearcher BIGRAMS_DF;
    private IndexSearcher BIGRAMS_FL;
    private IndexSearcher BIGRAMS_LC;
    private IndexReader METADATA;
    private Directory docs;
    private long last;
    private  File indexRoot;
    private final String indexRootPath;
    private  Connection db;
    private static String [] SERIALS = new String[0];
    private static String [] APIS_COLLECTIONS = new String[0];
    public IndexEventPropagator(String indexRootPath){
        this.indexRootPath = indexRootPath;
        refreshIndex();
    }
    public void close() throws IOException {
        if(this.SEARCHER != null) this.SEARCHER.close();
        if(this.DOC_READER != null) this.DOC_READER.close();
        if(this.BIGRAMS != null) this.BIGRAMS.close();
        if(this.BIGRAMS_DF != null) this.BIGRAMS_DF.close();
        if(this.BIGRAMS_FL != null) this.BIGRAMS_FL.close();
        if(this.BIGRAMS_LC != null) this.BIGRAMS_LC.close();
    }
    
    public void setConnection(Connection db){
        this.db = db;
    }
    
    public Connection connection(){
        return this.db;
    }
    
    private synchronized void refreshIndex(){
        try{
            this.indexRoot = new File(indexRootPath);
            File docsFile = new File(indexRoot,"docs");
            docs = FSDirectory.getDirectory(docsFile);
            FSDirectory.setDisableLocks(true);
            this.last = IndexReader.lastModified(docs);
            File plainFile = new File(indexRoot,"plain");
            Directory plain = FSDirectory.getDirectory(plainFile);
            plain = new RAMDirectory(plain);
            
            File lcFile = new File(indexRoot,"lc");
            Directory lc = FSDirectory.getDirectory(lcFile);
            lc = new RAMDirectory(lc);
            File dfFile = new File(indexRoot,"df");
            Directory df = FSDirectory.getDirectory(dfFile);
            df = new RAMDirectory(df);
            File flFile = new File(indexRoot,"fl");
            Directory fl = FSDirectory.getDirectory(flFile);
            fl = new RAMDirectory(fl);
            DOC_READER = IndexReader.open(docs);
            docEvent = new IndexEvent(DOC_READER);
            ParallelReader TOKENS = new ParallelReader();
            TOKENS.add(IndexReader.open(plain));
            TOKENS.add(IndexReader.open(lc));
            TOKENS.add(IndexReader.open(df));
            TOKENS.add(IndexReader.open(fl));
            BIGRAMS = new IndexSearcher(BigramIndexer.indexBigrams(TOKENS, PLAIN, new RAMDirectory()));
            BIGRAMS_DF = new IndexSearcher(BigramIndexer.indexBigrams(TOKENS, DF, new RAMDirectory()));
            BIGRAMS_LC = new IndexSearcher(BigramIndexer.indexBigrams(TOKENS, LC, new RAMDirectory()));
            BIGRAMS_FL = new IndexSearcher(BigramIndexer.indexBigrams(TOKENS, FL, new RAMDirectory()));
            File md = new File(indexRoot,"metadata");
            if(md.exists()){
                Directory mdir = FSDirectory.getDirectory(md);
                METADATA = IndexReader.open(mdir);
                TOKENS.add(METADATA);
                setIndexedSeries(METADATA);
                setAPISCollections(METADATA);
            }
            SEARCHER = new IndexSearcher(TOKENS);
            bigramEvent = new SearcherEvent(SEARCHER, BIGRAMS,BIGRAMS_DF,BIGRAMS_FL, BIGRAMS_LC);
            LOG.info("num greek docs: " + (SEARCHER.getIndexReader().maxDoc()) + " reader/searcher refreshed at " + new java.util.Date());   
            for(IndexEventListener listener:listeners){
                listener.replaceDocReader(docEvent);
                listener.replaceSearchers(bigramEvent);
            }
        }
        catch(Exception ioe){
            if(ioe.getMessage().equals("Stale NFS file handle")){
                LOG.error("IndexEventPropagator: " + ioe.toString());
            }
            else{
                throw new RuntimeException(ioe.toString(),ioe);
            }
        }
    }
    private static final String [] ARRAY_TYPE = new String[0];
    private static final String COLON_PLUS = new String(new char[]{(':'+1)});
    private void setIndexedSeries(IndexReader index) throws IOException {
        ArrayList<String> serials = new ArrayList<String>();
        Term template = new Term(CoreMetadataFields.INDEXED_SERIES,"");
        TermEnum terms = index.terms(template);
        Term check = terms.term();
        if(check != null && check.field().equals(template.field())){
            do{
                serials.add(check.text());
            }while(terms.next() && (check = terms.term()).field().equals(template.field()));
        }
        SERIALS = serials.toArray(ARRAY_TYPE);
    }
    private void setAPISCollections(IndexReader index) throws IOException {
        ArrayList<String> colls = new ArrayList<String>();
        Term template = new Term(CoreMetadataFields.XREFS,NamespacePrefixes.APIS);
        TermEnum terms = index.terms(template);
        Term check = terms.term();
        if(check != null && check.field().equals(template.field())){
            String next;
            do{
                String text = check.text();
                if(!text.startsWith(NamespacePrefixes.APIS)) break;
                text = text.substring(NamespacePrefixes.APIS.length());
                int ix = text.indexOf(':');
                text = text.substring(0,ix);
                colls.add(text);
                next = (NamespacePrefixes.APIS + text + COLON_PLUS);
            }while(terms.skipTo(template.createTerm(next)) && (check = terms.term()).field().equals(template.field()));
        }
        APIS_COLLECTIONS = colls.toArray(ARRAY_TYPE);
    }
    public static Iterator<String> getAPIS(){
        return new ArrayIterator<String>(APIS_COLLECTIONS);
    }
    public static Iterator<String> getSerials(){
        return new ArrayIterator<String>(SERIALS);
    }
    public synchronized void addListener(IndexEventListener listener){
        listeners.add(listener);
        listener.replaceDocReader(docEvent);
        listener.replaceSearchers(bigramEvent);
    }
    public synchronized void removeListener(IndexEventListener listener){
        listeners.remove(listener);
    }
    public synchronized void removeListeners(){
        listeners.clear();
    }
    public void run(){
        while(!Thread.interrupted()){
            try{
                if( IndexReader.lastModified(docs) > this.last || !DOC_READER.isCurrent()){
                    refreshIndex();
                }
                Thread.sleep(TEN_MIN);
            }
            catch(InterruptedException e){
                break;
            }
            catch(IOException i){
                if(i.getMessage().equals("Stale NFS file handle")){
                    LOG.error("IndexEventPropagator: " + i.toString());
                    this.indexRoot = new File(this.indexRootPath);
                }
                else{
                    i.printStackTrace();
                    break;
                }
            }
        }
    }
}
