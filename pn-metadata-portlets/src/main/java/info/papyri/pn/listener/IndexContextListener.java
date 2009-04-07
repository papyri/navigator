package info.papyri.pn.listener;

import info.papyri.index.DBUtils;
import info.papyri.index.LuceneIndex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import info.papyri.jndi.jndiURLContext;
import info.papyri.resolver.XRefResolver;

public class IndexContextListener implements ServletContextListener {
    
    private ArrayList<ServletContextListener>delegates = new ArrayList<ServletContextListener>();

    public void contextDestroyed(ServletContextEvent arg0) {
        Iterator<ServletContextListener> iter = delegates.iterator();
        while(iter.hasNext()){
            iter.next().contextDestroyed(arg0);
        }
        try{
            LuceneIndex.INDEX_COL.close();
            LuceneIndex.INDEX_HGV.close();
            LuceneIndex.INDEX_XREF.close();
        }
        catch(Throwable t){
            t.printStackTrace();
        }
    }
    
    public void addDelegate(ServletContextListener listener){
        delegates.add(listener);
    }

    public void contextInitialized(ServletContextEvent arg0) {
        try {
            ServletContext sc = arg0.getServletContext();
            String DDBDP = sc.getInitParameter("ddbdp-src");
            String HGV =  sc.getInitParameter("hgv-src");
            String APIS = sc.getInitParameter("apis-src");
            String XREF =  sc.getInitParameter("crosswalk-src");

            File hgvData = new File(HGV);
            File apisData = new File(APIS); 
            File ddbdpData = new File(DDBDP);
            File xrefData = new File(XREF);
            LuceneIndex.INDEX_COL = IndexReader.open(apisData);
            LuceneIndex.INDEX_HGV = IndexReader.open(hgvData);
            LuceneIndex.INDEX_XREF = IndexReader.open(xrefData);
            LuceneIndex.SEARCH_COL = new IndexSearcher(LuceneIndex.INDEX_COL);
            LuceneIndex.SEARCH_HGV = new IndexSearcher(LuceneIndex.INDEX_HGV);
            LuceneIndex.SEARCH_XREF = new IndexSearcher(LuceneIndex.INDEX_XREF);
            LuceneIndex.getIndexedValues();
            jndiURLContext.setResolver(new XRefResolver());
            initDerby(ddbdpData.toURL());
        }
        catch (Throwable t){
            t.printStackTrace();    
        }
    }

    private void initDerby(URL data){
        InputStream ddbdpData = null;
        try {
            String dbHome = DBUtils.getDerbyHome();
            File dbFiles = new File(dbHome);
            if (!dbFiles.exists()) dbFiles.mkdirs();
            System.out.println("System.getProperty(\"java.io.tmpdir\") = " + System.getProperty("java.io.tmpdir"));
            System.out.println("System.getProperty(\"derby.system.home\") = " + System.getProperty("derby.system.home"));
            System.out.println("System.getProperty(\"user.home\") = " + System.getProperty("user.home"));

            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            ddbdpData = data.openStream();
            Connection dbConnection = DBUtils.getConnection();
            DBUtils.dropDDBDPXrefTable(dbConnection);
            System.out.println("dropDDBDPXrefTable");
            DBUtils.createDDBDPXrefTable(dbConnection);
            System.out.println("createDDBDPXrefTable");
            DBUtils.loadDDBDPXrefTable(dbConnection,ddbdpData);
            System.out.println("loadDDBDPXrefTable");
        }
        catch (IOException ie){
            ie.printStackTrace();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        catch (ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }
        finally{
            if (ddbdpData != null){
                try{
                    ddbdpData.close();
                }catch (Throwable t){};
            }
        }
    }

}


