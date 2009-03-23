package info.papyri.ddbdp.servlet;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.ParallelReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import info.papyri.epiduke.lucene.*;

public class IndexInitializer implements ServletContextListener {
    private Thread directoryMonitor;
    private IndexEventPropagator events;
    private Connection db;
    private static final Logger LOG = Logger.getLogger(IndexInitializer.class);
    static FieldSelector getTokenOnlySelector(){
        final ArrayList<String> wordFields = new ArrayList<String>();
        wordFields.add(Indexer.WORD_SPAN_TERM);
        wordFields.add(Indexer.WORD_SPAN_TERM_DF);
        wordFields.add(Indexer.WORD_SPAN_TERM_FL);
        wordFields.add(Indexer.WORD_SPAN_TERM_LC);
        return new FieldSelector(){
            public FieldSelectorResult accept(String name){
                if(wordFields.contains(name)) return FieldSelectorResult.LOAD;
                return FieldSelectorResult.NO_LOAD;
            }
        };
    }
    
    public void contextDestroyed(ServletContextEvent arg0) {
        try{
            if(this.directoryMonitor != null) this.directoryMonitor.interrupt();
            if(this.events != null) this.events.close();
            if(this.db != null) this.db.close();
        }
        catch (IOException ioe){
            LOG.error(ioe.toString(),ioe);
        }
        catch(SQLException e){
            LOG.error(e.toString(),e);
        }
    }

    public void contextInitialized(ServletContextEvent arg0) {
        String indexroot = arg0.getServletContext().getInitParameter("indexroot");
        this.events = new IndexEventPropagator(indexroot);
        this.directoryMonitor = new Thread(this.events);
        directoryMonitor.start();
        System.out.println("Started directory monitor");
        arg0.getServletContext().setAttribute("EVENTS", this.events);
        System.out.println("Set event monitor");
        String lemmaPath = arg0.getServletContext().getInitParameter("lemmaRoot");
        if(lemmaPath==null) lemmaPath = System.getProperty("java.io.tmpdir") + File.separator + "lemmas";
        File lemmaRoot = new File(lemmaPath);
        try{
            db = LemmaIndexer.getSeedData(lemmaRoot);
            System.out.println("Got seed data: " + lemmaPath);
            this.events.setConnection(db);
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }

    }
    
}
