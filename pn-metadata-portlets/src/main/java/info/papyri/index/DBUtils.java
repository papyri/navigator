package info.papyri.index;


import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.NamespacePrefixes;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.NodeCreateRule;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

import org.apache.lucene.search.TermQuery;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.lucene.ConstantBitsetFilter;
import util.lucene.SimpleCollector;

import javax.xml.parsers.ParserConfigurationException;
public class DBUtils {
    private static final Logger LOG = Logger.getLogger(DBUtils.class);
    private static String createDDBDP = "CREATE table APP.PERSEUS (" +
    "ID          INTEGER NOT NULL" + 
    "            PRIMARY KEY GENERATED ALWAYS AS IDENTITY" + 
    "            (START WITH 1, INCREMENT BY 1)," +
    "PERSEUS_ID VARCHAR(25)," +
    "PUB_COLLECTION VARCHAR(30))";
    
    private static final String dropDDBDP = "DROP table APP.PERSEUS";
    
    private static final String checkDDBDP = "SELECT COUNT(*) FROM SYS.SYSTABLES WHERE TABLENAME = 'APP.PERSEUS'";

    private static final Pattern NUMBER = Pattern.compile("^\\d+$");
    
    private static final String DERBY_HOME = System.getProperty("java.io.tmpdir") + "/APISData";
    public static String getDerbyHome(){
        if (System.getProperty("derby.system.home") == null){
            System.setProperty("derby.system.home", DERBY_HOME);
        }
        return System.getProperty("derby.system.home");
    }
    private static ThreadGroup indexers = null;
    private static Stack<Connection> pool = new Stack<Connection>();
    private static HashMap<String,String> cache = new HashMap<String,String>();
    public static void setIndexersGroup(ThreadGroup tg){
        indexers = tg;
    }
    
    public static final String PERSEUS_PREFIX = "Perseus:text:1999.05.";
    
    public static void waitOnIndexers(){
        if (indexers == null) throw new IllegalStateException("Indexers Thread Group not initialized.");
        
        Thread[] active = new Thread[1];
        while (indexers.activeCount() > 0){
            indexers.enumerate(active);
            try{
                active[0].join();
            }
            catch (InterruptedException ie){
                
            }
        }

        BitSet xrefMapped = new BitSet(LuceneIndex.INDEX_XREF.maxDoc());
        BitSet apisOnly = new BitSet(LuceneIndex.INDEX_COL.maxDoc());
        BitSet hgvOnly = new BitSet(LuceneIndex.INDEX_HGV.maxDoc());
        try{
            SimpleCollector apisCollector = new SimpleCollector(LuceneIndex.SEARCH_COL,apisOnly);
            SimpleCollector hgvCollector = new SimpleCollector(LuceneIndex.SEARCH_HGV,hgvOnly);
            for(int i = 0;i<LuceneIndex.INDEX_XREF.maxDoc();i++){
                Document doc = LuceneIndex.INDEX_XREF.document(i);
                String [ ] xrefs = doc.getValues(CoreMetadataFields.XREFS);
                byte mask = 0;
                byte apis = 1;
                byte hgv = 2;
                byte mapped = 3;
                for(String xref:xrefs){
                    if(xref.startsWith(NamespacePrefixes.APIS)){
                        mask |= apis;
                        continue;
                    }
                    if(xref.startsWith(NamespacePrefixes.HGV)){
                        mask |= hgv;
                        continue;
                    }
                    if ((mapped & mask) == mapped){
                        xrefMapped.set(i);
                        break;
                    }
                }
                Term docId = new Term(CoreMetadataFields.DOC_ID,"");
                if((mask & apis) == apis){
                    String apisId = doc.get(CoreMetadataFields.DOC_ID);
                    LuceneIndex.SEARCH_COL.search(new TermQuery(docId.createTerm(apisId)),apisCollector);
                }
                if((mask & hgv) == hgv){
                    String hgvId = doc.get(CoreMetadataFields.DOC_ID);
                    LuceneIndex.SEARCH_HGV.search(new TermQuery(docId.createTerm(hgvId)),hgvCollector);
                }
            }
        }
        catch(IOException e){
            
        }

        LuceneIndex.LOOSE_APIS_FILTER = new ConstantBitsetFilter(LuceneIndex.INDEX_COL,apisOnly);
        LuceneIndex.LOOSE_HGV_FILTER = new ConstantBitsetFilter(LuceneIndex.INDEX_HGV,hgvOnly);
        LuceneIndex.XREF_MAPPED = new ConstantBitsetFilter(LuceneIndex.INDEX_XREF,xrefMapped);
    }
        
    public static int activeIndexers(){
        if (indexers == null) throw new IllegalStateException("Indexers Thread Group not initialized.");
        return indexers.activeCount();        
    }

    public static boolean dropDDBDPXrefTable(Connection dbConnection){
        boolean result = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(dropDDBDP);
            result = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return result;
    }
    
    public static boolean createDDBDPXrefTable(Connection dbConnection){
        boolean result = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(createDDBDP);
            result = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return result;
    }
    
    public static boolean checkDDBDPXrefTable(Connection dbConnection){
        boolean result = false;
        Statement statement = null;
        try{
            statement = dbConnection.createStatement();
            statement.execute(checkDDBDP);
            statement.getResultSet().next();
            if(LOG.isDebugEnabled())LOG.debug(statement.getResultSet());
            result = statement.getResultSet().getInt(1) > 0;
        }
        catch (SQLException se){
            LOG.error(se.toString(),se);   
        }
        return result;
    }
    
    public static Connection getConnection() throws SQLException{
        if (pool.size() > 1) return pool.pop();
        Connection dbConnection = null;
        String home = getDerbyHome();
        File logHome = new File(home,"logs");
        logHome.mkdirs();
        String strUrl =
            "jdbc:derby:"
            + home
            + "/db;create=true;logDevice="
            + home
            + "/logs/derby.log";
        dbConnection = DriverManager.getConnection(strUrl);
        return dbConnection;
    }
    
    public static void returnConnection(Connection conn){
        pool.push(conn);
    }
    
    
    public static boolean loadDDBDPXrefTable(Connection dbConnection, InputStream data) throws SQLException {
        boolean result = false;
        PreparedStatement statement = dbConnection.prepareStatement(
                "INSERT INTO APP.PERSEUS " +
                "   (PERSEUS_ID, PUB_COLLECTION) " +
                "VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        Digester digest = new Digester();
        try{
            NodeCreateRule ncd = new NodeCreateRule(Node.ELEMENT_NODE);
            digest.addRule("results/result/text", ncd);
        }
        catch(ParserConfigurationException pce){
            pce.printStackTrace();
            return false;
        }
        digest.addObjectCreate("data", java.util.HashMap.class);
        //digest.addSetNext("results/result", "addResult",
         //       DDBDPResult.class.getName());
        digest.addCallMethod("data/xref", "put", 2);
        digest.addCallParam("data/xref/perseus", 0);
        digest.addCallParam("data/xref/collection", 1);
        digest.addSetNext("results/result/text", "addText",
                Element.class.getName());
        try{
            HashMap results = (HashMap) digest.parse(data);
            Iterator keys = results.keySet().iterator();
            
            while (keys.hasNext()){
                Object key = keys.next();
                result = insertDDBDPRecord(statement,key.toString(),results.get(key).toString());
            }
            
        }
        catch (Exception ioe){
            ioe.printStackTrace();
            return false;
        }
        
      
        return result;
    }
    private static boolean insertDDBDPRecord(PreparedStatement stmt,String perseus, String collection) {
        boolean result = false;
        String ins = "( " + perseus + " , " + collection + " )";
        try {
            stmt.clearParameters();
            stmt.setString(1, perseus);
            stmt.setString(2, collection);
            stmt.execute();
            result = (stmt.getUpdateCount() == 1);
            if ( !result){
                LOG.warn("Insert failed for " + ins);
            }
            

        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        return result;
    }
    
    public static String query(String series, Connection connection, boolean useCache) throws SQLException {
        if (useCache && cache.containsKey(series)) return cache.get(series);
        boolean numeric = NUMBER.matcher(series).matches();
        PreparedStatement stmt = (numeric)
                ?connection.prepareStatement("SELECT PUB_COLLECTION FROM APP.PERSEUS WHERE PERSEUS_ID = ?")
                :connection.prepareStatement("SELECT PERSEUS_ID FROM APP.PERSEUS WHERE PUB_COLLECTION = ?")
                ;
                    stmt.clearParameters();
        String searchSeries = null;
        if (numeric){
            searchSeries =  PERSEUS_PREFIX + series.trim();
        }
        else {
            searchSeries = series.trim();
        }
        stmt.setString(1, searchSeries);
        stmt.execute();
        ResultSet results = stmt.getResultSet();
        boolean success = results.next();
        String val = "";
        if ( !success){
            //System.out.println("WARNING: Select failed for " + series);
            return series;
        }
        else{
            val = results.getString(1); 
        }
        if (useCache && !cache.containsKey(series)) cache.put(series, val);
        return val;
        
    }
    
    
/*
       private static boolean insertHGV(Connection connection){
        
        String insertStatement = "INSERT INTO APP.HGV  " +
        "(PUB_COLL, PUB_VOL, PUB_DOC, RECTO_VERSO, LOCATION," +
        " PUB_DISPLAY, DATE1_YEAR, DATE1_MONTH, DATE1_DAY, DATE1_DISPLAY1," +
        " DATE1_DISPLAY2, IMAGE, ORIGIN, TITLE_EN, CEN1_ALT," +
        " PUB1_ALT, NT1, CONNECTION, DATE2_YEAR, DATE2_MONTH," +
        " DATE2_DAY, CEN2_ALT, TITLE_DE, MATERIAL, DATE1_PREF," +
        " DATE2_PREF, DATE_LINES, DATE3_YEAR, DATE3_MONTH, DATE3_DAY," +
        " NT2, NT3, NT4, NT5, NT6," +
        " NT7, NT8, NT9, NT10, APIS_URL," + 
        " APIS_ID, APIS_SORT, NOTE) " +
        "VALUES (" +
        " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
        " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
        " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
        " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
        " ?, ?, ?)";

        return false;
    }
*/

}
