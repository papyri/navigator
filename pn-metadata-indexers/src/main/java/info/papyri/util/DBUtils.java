package info.papyri.util;

import info.papyri.lucene.ConstantBitsetFilter;
import info.papyri.lucene.SimpleCollector;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.NodeCreateRule;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.QueryWrapperFilter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import info.papyri.data.APISIndices;
import info.papyri.data.LuceneIndex;
import info.papyri.data.XREFIndices;

import javax.xml.parsers.ParserConfigurationException;
public class DBUtils {
  
    private static String createDDBDP = "CREATE table APP.PERSEUS (" +
    "ID          INTEGER NOT NULL" + 
    "            PRIMARY KEY GENERATED ALWAYS AS IDENTITY" + 
    "            (START WITH 1, INCREMENT BY 1)," +
    "PERSEUS_ID VARCHAR(25)," +
    "PUB_COLLECTION VARCHAR(30))";
    
    private static final String dropDDBDP = "DROP table APP.PERSEUS";
    
    private static final String checkDDBDP = "SELECT COUNT(*) FROM SYS.SYSTABLES WHERE TABLENAME = 'PERSEUS'";

    private static final Pattern NUMBER = Pattern.compile("^\\d+$");
    
    private static final String DERBY_HOME = (System.getProperty("java.io.tmpdir") + java.io.File.separator + "APISData").replaceAll("[\\\\]+", "/");
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
        LuceneIndex.LOOSE_APIS_FILTER = apisOnly();
        LuceneIndex.LOOSE_HGV_FILTER = hgvOnly();
        LuceneIndex.XREF_MAPPED = xrefMapped();

    }
    
    private static ConstantBitsetFilter apisOnly(){
        TermQuery flagQuery = new TermQuery(new Term(XREFIndices.APIS_EXCLUSIVE_FLAG,XREFIndices.EXCLUSIVE_FLAG_VALUE));
        try{
        SimpleCollector sc = new SimpleCollector((IndexSearcher)LuceneIndex.SEARCH_XREF);
        LuceneIndex.SEARCH_XREF.search(flagQuery, sc);
        BitSet filterBits = getApisBitSet(sc.get());
        return new ConstantBitsetFilter(LuceneIndex.INDEX_COL,filterBits);
        }
        catch (IOException ioe){
            throw new RuntimeException("Could not read XREF index: " + ioe);
        }
    }
    
    public static BitSet getApisBitSet(BitSet XREFBitSet){
        try{
        SimpleCollector sc = new SimpleCollector((IndexSearcher)LuceneIndex.SEARCH_XREF);
        sc.get().or(XREFBitSet);
        Iterator<Document> iter = sc.iterator();
        BitSet filterBits = new BitSet(LuceneIndex.SEARCH_COL.maxDoc());
        while (iter.hasNext()){
            Document doc = iter.next();
            String apisCN = doc.get(XREFIndices.APIS_ID);
            Term controlName = new Term(APISIndices.CONTROL_NAME,apisCN);
            Hits hits = LuceneIndex.SEARCH_COL.search(new TermQuery(controlName));
            Iterator<Hit> hitsIter = hits.iterator();
            while (hitsIter.hasNext()){
                Hit hit = hitsIter.next();
                filterBits.set(hit.getId());
            }
        }
        return filterBits;
        }
        catch (IOException ioe){
            throw new RuntimeException("Could not read XREF index: " + ioe);
        }
    }
    
    public static BitSet getHGVBitSet(BitSet XREFBitSet){
        try{
        SimpleCollector sc = new SimpleCollector((IndexSearcher)LuceneIndex.SEARCH_XREF);
        sc.get().or(XREFBitSet);
        Iterator<Document> iter = sc.iterator();
        BitSet filterBits = new BitSet(LuceneIndex.SEARCH_HGV.maxDoc());
        while (iter.hasNext()){
            Document doc = iter.next();
            String hgvId = doc.get(XREFIndices.HGV_ID);
            Term controlName = new Term(APISIndices.CONTROL_NAME,hgvId);
            Hits hits = LuceneIndex.SEARCH_HGV.search(new TermQuery(controlName));
            Iterator<Hit> hitsIter = hits.iterator();
            while (hitsIter.hasNext()){
                Hit hit = hitsIter.next();
                filterBits.set(hit.getId());
            }
        }
        return filterBits;
        }
        catch (IOException ioe){
            throw new RuntimeException("Could not read XREF index: " + ioe);
        }
    }

    private static ConstantBitsetFilter hgvOnly(){
        TermQuery flagQuery = new TermQuery(
                new Term(XREFIndices.HGV_EXCLUSIVE_FLAG,XREFIndices.EXCLUSIVE_FLAG_VALUE));
        try{
        SimpleCollector sc = new SimpleCollector((IndexSearcher)LuceneIndex.SEARCH_XREF);
        LuceneIndex.SEARCH_XREF.search(flagQuery, sc);
        BitSet filterBits = getHGVBitSet(sc.get());
        return new ConstantBitsetFilter(LuceneIndex.INDEX_HGV,filterBits);
        }
        catch (IOException ioe){
            throw new RuntimeException("Could not read XREF index: " + ioe);
        }
    }
    private static ConstantBitsetFilter xrefMapped(){
        TermQuery apisOnly = new TermQuery(new Term(XREFIndices.APIS_EXCLUSIVE_FLAG,XREFIndices.EXCLUSIVE_FLAG_VALUE));
        TermQuery hgvOnly = new TermQuery(new Term(XREFIndices.HGV_EXCLUSIVE_FLAG,XREFIndices.EXCLUSIVE_FLAG_VALUE));
        BooleanQuery bool = new BooleanQuery();
        bool.add(apisOnly,BooleanClause.Occur.SHOULD);
        bool.add(hgvOnly,BooleanClause.Occur.SHOULD);
        try{
        SimpleCollector sc = new SimpleCollector((IndexSearcher)LuceneIndex.SEARCH_XREF);
        Filter qFilter = new CachingWrapperFilter(new QueryWrapperFilter(bool));
        Weight w = bool.weight(LuceneIndex.SEARCH_XREF);
        LuceneIndex.SEARCH_XREF.search(w,qFilter, sc);
        BitSet bits = sc.get();
        bits.flip(0,bits.size());
        return new ConstantBitsetFilter(LuceneIndex.INDEX_XREF,bits);
        }
        catch (IOException ioe){
            throw new RuntimeException("Could not read XREF index: " + ioe);
        }
    }
    
    public static int activeIndexers(){
        if (indexers == null) throw new IllegalStateException("Indexers Thread Group not initialized.");
        return indexers.activeCount();        
    }

    private static boolean dropDDBDPXrefTable(Connection dbConnection){
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
    
    private static boolean createDDBDPXrefTable(Connection dbConnection){
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
    
    public static boolean checkDDBDPXrefTable(){
        boolean result = false;
        Statement statement = null;
        Connection dbConnection = null;
        try{
            dbConnection = getConnection();
            statement = dbConnection.createStatement();
            statement.execute(checkDDBDP);
            statement.getResultSet().next();
            System.out.println(statement.getResultSet());
            result = statement.getResultSet().getInt(1) > 0;
        }
        catch (SQLException se){
            System.out.println(se.toString());   
        }
        catch (ClassNotFoundException se){
            System.out.println(se.toString());   
        }
        finally{
            if(dbConnection != null) returnConnection(dbConnection);
        }
        return result;
    }
    
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        if (pool.size() > 1) return pool.pop();
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");        
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
    
    
    private static boolean loadDDBDPXrefTable(Connection dbConnection, InputStream data) throws SQLException {
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
        digest.addCallParam("data/xref/perseus", 1);
        digest.addCallParam("data/xref/collection", 0);
        digest.addSetNext("results/result/text", "addText",
                Element.class.getName());
        try{
            HashMap results = (HashMap) digest.parse(data);
            Iterator keys = results.keySet().iterator();
            
            while (keys.hasNext()){
                Object key = keys.next();
                result = insertDDBDPRecord(statement,results.get(key).toString(),key.toString());
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
                System.out.println("WARNING: Insert failed for " + ins);
            }
            

        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        return result;
    }
    
    public static String query(String series, boolean useCache) throws SQLException {
        if (useCache && cache.containsKey(series)) return cache.get(series);
        boolean numeric = NUMBER.matcher(series).matches();
        Connection connection = null;
        try{
            connection = getConnection();
        }
        catch(ClassNotFoundException e){
            e.printStackTrace(System.err);
            return series;
        }
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
            returnConnection(connection);
            return series;
        }
        else{
            val = results.getString(1); 
        }
        if (useCache && !cache.containsKey(series)) cache.put(series, val);
        returnConnection(connection);
        return val;
        
    }

    public static void setupDerby(URL data) throws IOException, SQLException {
            String dbHome = getDerbyHome();
            File dbFiles = new File(dbHome);
            InputStream ddbdpData = null;
            if (!dbFiles.exists()) dbFiles.mkdirs();
            System.out.println("System.getProperty(\"java.io.tmpdir\") = " + System.getProperty("java.io.tmpdir"));
            System.out.println("System.getProperty(\"derby.system.home\") = " + System.getProperty("derby.system.home"));
            System.out.println("System.getProperty(\"user.home\") = " + System.getProperty("user.home"));
            Connection dbConnection = null;
            try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");        
            ddbdpData = data.openStream();
             dbConnection = getConnection();
            dropDDBDPXrefTable(dbConnection);
            System.out.println("dropDDBDPXrefTable");
                createDDBDPXrefTable(dbConnection);
                System.out.println("createDDBDPXrefTable");
                loadDDBDPXrefTable(dbConnection,ddbdpData);
                System.out.println("loadDDBDPXrefTable");
            }
            catch(ClassNotFoundException e){
                
            }
            finally {
                if(dbConnection != null) returnConnection(dbConnection);
                if (ddbdpData != null){
                    try{
                        ddbdpData.close();
                    }catch (Throwable t){};
                }
            }
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
