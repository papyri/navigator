package info.papyri.epiduke.lucene;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.XmlRpcException;
import org.xml.sax.SAXException;
import edu.unc.epidoc.transcoder.*;

public class LemmaIndexer {

    private static int morphs = 0;
    private static int nomatches = 0;
    private static PreparedStatement stmt;

    private static PreparedStatement getStatement(Connection db) throws SQLException {
        PreparedStatement statement = db.prepareStatement(
                "INSERT INTO APP.LEMMA " +
                "   (MORPH, LEMMA) " +
                "VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        return statement;
    }

    static void index(String in, String out) throws IOException, SQLException, XmlRpcException {
        Connection dbConn = getConnection(out);
        if (!checkLemma(dbConn)) {
            createLemma(dbConn);
        }

        IndexReader lucene = IndexReader.open(FSDirectory.getDirectory(in));
        TermEnum tenum = lucene.terms(new Term(Indexer.WORD_SPAN_TERM, ""));
        UnicodeParser ucp = new UnicodeParser();
        UnicodeCConverter uc = new UnicodeCConverter();
        BetaCodeParser bcp = new BetaCodeParser();
        BetaCodeConverter bcc = new BetaCodeConverter();
        HashMap<String, String> betaToUni = new HashMap<String, String>();
        PreparedStatement stmt = getStatement(dbConn);
        while (tenum.next()) {
            Term next = tenum.term();
            if (next.field().equals(Indexer.WORD_SPAN_TERM)) {
                //exclude terms with numbers and other non-alphabetic characters
                String ucText = next.text();
                if (ucText.matches(".*[–—=+/\\!@#$%^&*():;,.{}'0-9]+.*")) {
                    continue;
                }
                if (ucText.contains("-")) {
                    continue;
                }
                //Only Greek on this pass
                if (ucText.matches("[a-zA-ZüöäëïáéíóúàèìòùßÄÖÜÁÉÍÓÚÀÈÌÒÙâêîôûÂÊÎÔÛç´`ˆ]+")) {
                    continue;
                }
                ucText = next.text().replaceAll("[\\^]", "").replaceAll("[\\[\\]\\{\\}<>]\\d?", "");
                ucp.setString(ucText);
                String beta = bcc.convertToString(ucp).replaceAll("S\\d", "S").toLowerCase();
                if (! beta.matches("[a-z()\\\\/=|+*]+")) {
                    continue;
                }
                if (beta.matches("^[^a-z]+$")) {
                    continue;
                }
                if (beta.charAt(0) == '%') {
                    continue;
                }
                if (beta.charAt(0) == '&') {
                    continue;
                }
                if (beta.charAt(0) == '#') {
                    continue;
                }
                betaToUni.put(beta, ucText);
                if (betaToUni.size() > 49) {
                    doInserts(betaToUni, stmt, uc, bcp);
                    betaToUni.clear();
                }
            }
        }
        if (betaToUni.size() > 0) {
            doInserts(betaToUni, stmt, uc, bcp);
            betaToUni.clear();
        }
        dbConn.close();
    }

    private static void doInserts(HashMap<String, String> betaToUni, PreparedStatement stmt, UnicodeCConverter uc, BetaCodeParser bcp) throws SQLException, IOException, XmlRpcException {
        String[] betaMorphs = betaToUni.keySet().toArray(info.papyri.util.ArrayTypes.STRING);
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("http://archimedes.mpiwg-berlin.mpg.de:8098/RPC2"));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        Object[] params = new Object[]{"-GRC", betaMorphs};
        try {
            Object result = client.execute("lemma", params);
            HashMap resultMap = (HashMap) result; // resultMap <String,Object[]>, object array is headwords as strings
            for (String betaMorph : betaMorphs) {
                Object[] betaHeadwords = (Object[]) resultMap.get(betaMorph);
                String unicodeMorph = betaToUni.get(betaMorph);
                if (betaHeadwords != null) {
                    for (Object hwObj : betaHeadwords) {
                        String betaHeadword = hwObj.toString();
                        betaHeadword = betaHeadword.replace('\u1fbd', ')');
                        betaHeadword = betaHeadword.replace('\u1fbe', '|');
                        betaHeadword = betaHeadword.replace('\u1fbf', ')');
                        betaHeadword = betaHeadword.replace('\u1fc0', '=');
                        betaHeadword = betaHeadword.replace('\u1ffd', '/');
                        betaHeadword = betaHeadword.replace('\u1ffe', '(');
                        betaHeadword = betaHeadword.replace('\u1fef', '\\');
                        betaHeadword = betaHeadword.replaceAll("\u27e6", "");
                        betaHeadword = betaHeadword.replaceAll("\u27e7", "");
                        betaHeadword = betaHeadword.replaceAll("\u03f2", "S3");
                        betaHeadword = betaHeadword.replaceAll("\u03f9", "*S3");
                        betaHeadword = betaHeadword.replaceAll("\u03df", "#1");
                        betaHeadword = betaHeadword.replaceAll("\u03de", "*#1");
                        betaHeadword = betaHeadword.replaceAll("\u03d9", "#3");
                        betaHeadword = betaHeadword.replaceAll("\u03d8", "*#3");
                        betaHeadword = betaHeadword.replaceAll("\u03c9", "w"); // for some reason some omegas don't get fixed
                        betaHeadword = betaHeadword.replaceAll("\ucc82", "%22");
                        boolean acute = betaHeadword.indexOf('\u00fa') != -1;
                        acute |= (betaHeadword.indexOf('\u00f3') != -1);
                        acute |= (betaHeadword.indexOf('\u00e1') != -1);
                        if ((Character.isUpperCase(betaHeadword.charAt(0)) && !betaHeadword.startsWith("S3") || acute)) { // Latin map
                            continue;
                            //insertLemma(unicodeMorph, betaHeadword, stmt);
                        } else {
                            bcp.setString(betaHeadword);
                            String unicodeHeadword = uc.convertToString(bcp);
                            insertLemma(unicodeMorph, unicodeHeadword, stmt);
                            if (betaHeadword.matches(".*\\d$")) {
                                insertLemma(unicodeMorph, unicodeHeadword.substring(0, unicodeHeadword.length() - 1), stmt);
                            }
                        }
                        System.out.println(betaMorph + " -> " + betaHeadword);
                        morphs++;
                    }
                } else {
                    System.err.println(betaMorph + " -> " + betaMorph + " (non-op)");
                    //insertLemma(unicodeMorph, unicodeMorph, stmt);
                    System.err.println("No rpc response for " + betaMorph);
                    nomatches++;
                }
            }
        } catch (Throwable t) {
            System.err.println(t.toString());
        }


    }

    private static boolean insertLemma(String morph, String lemma, PreparedStatement statement) {
        try {
            statement.clearParameters();
            statement.setString(1, morph);
            statement.setString(2, lemma);
            return statement.execute();
        } catch (SQLException sqle) {
            System.err.println(sqle.toString());
            return false;
        }
    }

    public static void main(String[] args) throws IOException, SAXException, SQLException, XmlRpcException {
        org.apache.derby.jdbc.EmbeddedDriver ed = new org.apache.derby.jdbc.EmbeddedDriver();
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            printHelp();
            System.exit(0);
        }
        
        HashMap opts = getOpts(args,
                new String[]{"-i", "-o"},
                new String[]{"--in", "--out"});
        String in = opts.get("-i").toString();
        String out = opts.get("-o").toString();
        Object recur = opts.get("-r");
        
        //String in = "/usr/local/pn/indices/ddbdp/plain";
        //String out = "/usr/local/pn/db/lemmas-new";
        morphs = 0;
        long start = System.currentTimeMillis();
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        System.out.println("Start index seeding: " + new java.util.Date(start));
        File outLog = new File("lemma.out.txt");
        outLog.delete();
        PrintStream newOut = new PrintStream(outLog);
        System.setOut(newOut);
        File errLog = new File("lemma.err.txt");
        errLog.delete();
        PrintStream newErr = new PrintStream(errLog);
        System.setErr(newErr);
        index(in, out);
        long end = System.currentTimeMillis();
        System.out.println("End index seeding: " + new java.util.Date(end));
        System.out.println("mapped " + morphs + " morphs to headwords");
        System.out.println("unmapped " + nomatches + " words");
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("-i/--in\t<STRING>\t: top-level index directory for lucene index");
        System.out.println("-o/--out\t<STRING>\t: directory for derby lemma index");
    }

    private static HashMap getOpts(String[] args, String[] sFlags, String[] lFlags) {
        HashMap result = new HashMap();
        if (args.length == 0) {
            return result;
        }
        args:
        for (int i = 0; i < args.length; i++) {
            flags:
            for (int j = 0; j < sFlags.length; j++) {
                if (args[i].equals(sFlags[j]) || args[i].equals(lFlags[j])) {
                    if (i + 1 >= args.length) {
                        Object in = result.get(sFlags[j]);
                        if (in == null) {
                            result.put(sFlags[j], Integer.valueOf(1));
                            break flags;
                        }
                        if (in instanceof Integer) {
                            result.put(sFlags[j], Integer.valueOf(((Integer) in).intValue() + 1));
                            break flags;
                        }
                        System.err.println("Missing value for flag " + sFlags[j]);
                        System.exit(1);
                    } else {
                        if (args[i + 1].charAt(0) == '-') {
                            Object in = result.get(sFlags[j]);
                            if (in == null) {
                                result.put(sFlags[j], Integer.valueOf(1));
                                break flags;
                            }
                            if (in instanceof Integer) {
                                result.put(sFlags[j], Integer.valueOf(((Integer) in).intValue() + 1));
                                break flags;
                            }
                            System.err.println("Missing value for flag " + sFlags[j]);
                            System.exit(1);
                        } else {
                            result.put(sFlags[j], args[i + 1]);
                            i++;
                        }
                    }
                }
            }
        }
        return result;
    }
    private static Stack<Connection> pool = new Stack<Connection>();
    private static String createLEMMA = "CREATE table APP.LEMMA (" +
            "ID          INTEGER NOT NULL" +
            "            PRIMARY KEY GENERATED ALWAYS AS IDENTITY" +
            "            (START WITH 1, INCREMENT BY 1)," +
            "MORPH VARCHAR(32)," +
            "LEMMA VARCHAR(32))";
    private static final String dropLEMMA = "DROP table APP.LEMMA";
    private static final String checkLEMMA = "SELECT COUNT(*) FROM SYS.SYSTABLES WHERE TABLENAME = 'LEMMA'";

    public static Connection getConnection(String home) throws SQLException {
        if (pool.size() > 1) {
            return pool.pop();
        }
        Connection dbConnection = null;

        File logHome = new File(home, "logs");
        logHome.mkdirs();
        String strUrl =
                "jdbc:derby:" + home + "/db;create=true;logDevice=" + home + "/logs/derby.log";
        dbConnection = DriverManager.getConnection(strUrl);
        return dbConnection;
    }

    public static boolean createLemma(Connection dbConnection) {
        boolean result = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(createLEMMA);
            result = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static boolean checkLemma(Connection dbConnection) {
        boolean result = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(checkLEMMA);
            statement.getResultSet().next();
            System.out.println(statement.getResultSet());
            result = statement.getResultSet().getInt(1) > 0;
        } catch (SQLException se) {
            System.out.println(se.toString());
        }
        return result;
    }

    public static Connection getSeedData(File lemmaDir) throws SQLException, IOException {
        if (!lemmaDir.exists() || (lemmaDir.listFiles().length == 0)) {
            lemmaDir.mkdirs();
            URL seedURL = LemmaIndexer.class.getResource("/lemma-seed.zip");
            ZipInputStream zis = new ZipInputStream(seedURL.openStream());
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                File file = new File(lemmaDir, name);
                if (entry.isDirectory()) {
                    file.mkdir();
                } else {
                    FileOutputStream fos = new FileOutputStream(file);
                    int len = -1;
                    byte[] buf = new byte[512];
                    while ((len = zis.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    fos.close();
                }
                zis.closeEntry();
            }
        }
        /** TODO: get service.properties, replace
         * derby.storage.logDeviceWhenBackedUp=${path}/logs/derby.log
         * logDevice=${path}/logs/derby.log
         * write out to file
         */
        String path = lemmaDir.getAbsolutePath();
        File propFile = new File(lemmaDir, "db/service.properties");
        Properties props = new Properties();
        FileInputStream propsIn = new FileInputStream(propFile);
        props.load(propsIn);
        propsIn.close();
        String logPath = path + File.separator + "logs" + File.separator + "derby.log";
        props.setProperty("derby.storage.logDeviceWhenBackedUp", logPath);
        props.setProperty("logDevice", logPath);
        FileOutputStream propsOut = new FileOutputStream(propFile);
        props.store(propsOut, "Lemma seed index for DDbDP Indexing");
        propsOut.flush();
        propsOut.close();
        try {
            return getConnection(path);
        } catch (SQLException t) {
            t.printStackTrace();
            if (t.getCause() != null) {
                t.getCause().printStackTrace();
            }
            throw t;
        }
    }
}
