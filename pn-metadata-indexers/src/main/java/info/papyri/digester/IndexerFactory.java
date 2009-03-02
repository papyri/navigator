package info.papyri.digester;

import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.metadata.apis.OAIContentHandler;
import info.papyri.metadata.hgv.EpiDocHandler;
import info.papyri.util.DBUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.naming.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import info.papyri.data.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

public class IndexerFactory implements ObjectCreationFactory {
    public static String indexRoot = System.getProperty("java.io.tmpdir") + File.separator + "apis" + File.separator; // can be overridden by command

    private Digester digester;
    private Analyzer analyzer;
    private boolean create;
    Directory indexDir;
    private Indexer indexer;
    
    
    public static Indexer reopen(Directory indexSubdir) throws IOException {
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer());
        WhitespaceAnalyzer ws = new WhitespaceAnalyzer();
        analyzer.addAnalyzer("ddbdp_all", ws);
        analyzer.addAnalyzer("ddbdp_first_only", ws);
        if (IndexReader.isLocked(indexSubdir)) IndexReader.unlock(indexSubdir);
        return new Indexer(indexSubdir,analyzer,false);
    }
    
    private static String getLatestUTC(Directory indexDir) throws IOException {
        String [] list = indexDir.list();
        if(list.length == 0) return null;
        IndexReader rdr = IndexReader.open(indexDir);
        TermEnum terms = rdr.terms(new Term(CoreMetadataFields.UTC,""));
        String val = "";
        do{
            Term term = terms.term();
            if(term == null || !CoreMetadataFields.UTC.equals(term.field())) break;
            val = term.text();
        }while(terms.next() && terms.term() != null);
        return val;
    }
    
    static final String LIST = "verb=ListRecords";
    
    public static Indexer indexAPIS_OAI(URL baseUrl, Directory indexDir, boolean create) throws IOException, SAXException {
        try{
            if(!DBUtils.checkDDBDPXrefTable()){
                DBUtils.setupDerby(IndexerFactory.class.getResource("/info/papyri/util/ddbdp.xml"));
            }
        }
        catch(SQLException e){
            try{
                DBUtils.setupDerby(IndexerFactory.class.getResource("/info/papyri/util/ddbdp.xml"));
            }
            catch(SQLException se){
                throw new IOException(se.toString());
            }
          }
        

        String latest = getLatestUTC(indexDir);
        if (baseUrl == null){
            return null;
        }
        
        IndexerFactory factory = new IndexerFactory(indexDir, (latest==null || create));
        Indexer indexer = factory.createObject(null);
        OAIContentHandler handler = new OAIContentHandler(indexer);
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(handler);
        String base = baseUrl.toString();
        if(!base.endsWith("?")){
            base += "?";
        }
        String rtoken = handler.getResumptionToken();
        do{
            URL data = null;
            String dataUrl =base;
            if(baseUrl.getProtocol().equals("http")){
            if(rtoken == null){
                if(latest != null){
                    dataUrl += (LIST + "&metadataPrefix=pi_dc&from=" + latest);
                } else dataUrl += (LIST + "&metadataPrefix=pi_dc");
            }
            else{
                dataUrl += (LIST + "&resumptionToken=" + rtoken);
            }
            data = new URL(dataUrl);
            }
            else data = baseUrl;
            System.out.println(data.toString());
            InputSource src = new InputSource(data.openStream());
            reader.parse(src);
        } while((rtoken = handler.getResumptionToken()) != null);
        //indexer.
        indexer.writer.flush();
        indexer.writer.optimize();
        indexer.writer.close();
        return indexer;
    }

    
    public IndexerFactory() {
        this(new RAMDirectory(),true);

    }
    
    public IndexerFactory(String indexDir, boolean create) throws IOException {
        this(getFSDirectory(indexDir),create);    
    }
    
    public IndexerFactory(Directory dir, boolean create) {
        this.indexDir = dir;
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer());
        WhitespaceAnalyzer ws = new WhitespaceAnalyzer();
        analyzer.addAnalyzer(APISIndices.DDBDP_ALL, ws);
        analyzer.addAnalyzer(APISIndices.DDBDP_FIRST, ws);
        this.analyzer = analyzer;
        this.create = create;
    }
    
    public IndexerFactory(Directory dir, Analyzer analyzer, boolean create){
        this.indexDir = dir;
        this.analyzer = analyzer;
        this.create = create;
    }
    
    public Indexer createObject(Attributes arg0) throws IOException {
        //System.out.println("Pushing Indexer object onto digester stack...");
        if(this.indexer==null){
            this.indexer = new Indexer(indexDir,analyzer,this.create); 
        }
        return this.indexer;
    }

    public Digester getDigester() {
        return digester;
    }

    public void setDigester(Digester arg0) {
        digester = arg0;
        
    }
    public static Indexer index(URL xmlData, String indexSubDirPath,boolean create) throws IOException, SAXException {
        File indexSubDir = new File(indexSubDirPath);
        Directory dir = getFSDirectory(indexSubDirPath);
        return indexAPIS_OAI(xmlData,dir,create);
        
    }

    public static void indexHGVEpidDoc(URL epiDoc, Indexer indexer, XMLReader reader) throws IOException, SAXException, SAXParseException {
        reader.parse(new InputSource(epiDoc.openStream()));
        EpiDocHandler handler = (EpiDocHandler)reader.getContentHandler();
        java.util.Iterator<CoreMetadataRecord> recs = handler.getRecords();
        while(recs.hasNext()){
            CoreMetadataRecord rec = recs.next();
            indexer.addCoreMetadataRecord(rec);
        }
    }
    
    public static Indexer indexHGV(File xmlRoot, Directory indexDir, boolean create) throws IOException, SAXException, SAXParseException {

        IndexerFactory iFactory = new IndexerFactory(indexDir,create);
        Indexer indexer = iFactory.createObject(null);
    
        if (xmlRoot == null){
            return null;
        }
        long start = System.currentTimeMillis();
        try {
            XMLReader xmlRdr = XMLReaderFactory.createXMLReader();
            EpiDocHandler handler = new EpiDocHandler();
            xmlRdr.setContentHandler(handler);
            xmlRdr.setEntityResolver(Indexer.getEpiDocResolver(xmlRdr.getEntityResolver()));

                try{
                    indexHgvXmlRoot(xmlRoot, indexer, xmlRdr);
                }
                catch(Throwable t){
                    System.err.println("error: " + xmlRoot.getPath());
                    System.err.println("\t" + t.toString());
                    if(!(t instanceof SAXException))t.printStackTrace(System.err);
                }
            
            if (indexer.numFailedRecords > 0) {
                System.out.println(indexer.numFailedRecords
                        + " records were not indexed due to errors.");
            }
            indexer.writer.flush();
            indexer.writer.optimize();
            indexer.writer.close();
        }finally{
            System.out.println("Started index: " + new java.util.Date(start));
            System.out.println("Ended index: " + new java.util.Date());
        }
        return indexer;
    }
    
    private static void indexHgvXmlRoot(File directory, Indexer indexer, XMLReader xmlRdr) throws IOException, SAXException {
        String [] names = directory.list();
        for(String name:names){
            if(name.charAt(0)=='.')continue;
            File file = new File(directory,name);
            if(name.endsWith(".xml")){
                indexHGVEpidDoc(file.toURI().toURL(),indexer,xmlRdr);
                continue;
            }
            else if(file.isDirectory() && !name.equals(".svn")){
                indexHgvXmlRoot(file,indexer,xmlRdr);
            }
        }
    }
    
    private static void findXML(File directory, Set<URI> fNames) throws IOException {
        String [] names = directory.list();
        for(String name:names){
            if(name.charAt(0)=='.')continue;
            File file = new File(directory,name);
            if(name.endsWith(".xml")){
                fNames.add(file.toURI());
                continue;
            }
            else if(file.isDirectory() && !name.equals(".svn")){
                findXML(file,fNames);
            }
        }
    }

    public static Indexer indexNServer(URL xmlData, Directory indexDir, boolean create) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
    
        try{
            digester.addFactoryCreate("DocumentIdentifiers", new IndexerFactory(indexDir,new StandardAnalyzer(),create));
            digester.addRule("DocumentIdentifiers/RDF", new NodeCreateRule(){
                public void end() throws Exception{
                    digester.pop(); // check into when this change was rolled back in commons, but node isn't popping by default
                }
            });
            digester.addSetNext("DocumentIdentifiers/RDF", "addRDF"); // addRDF?
        }
        catch (ParserConfigurationException e){
            System.out.println(e.toString());
        }
    
    
        // now that rules and actions are configured, start the parsing process
    
        if (xmlData == null){
            return null;
        }
        Indexer indexer = null;
        try {
            indexer = 
                (Indexer) digester.parse(
                        new InputStreamReader(xmlData.openStream(),"UTF-8"));
    
            if (indexer.numFailedRecords > 0) {
                System.out.println(indexer.numFailedRecords
                        + " records were not indexed due to errors.");
            }
            indexer.writer.flush();
            indexer.writer.optimize();
            indexer.writer.close();
        } catch (SAXParseException spe) {
            System.out.println("SAXParseException: " + spe);
        }
        finally{
            
        }
        return indexer;
    }

    public static Directory getFSDirectory(String subdir) throws IOException{
        File fsDir = new File(IndexerFactory.indexRoot + subdir);
        return FSDirectory.getDirectory(fsDir,true);
    }
    
    public static void main(String [] args){
        if(args.length != 3){
            printUsage();
            System.exit(1);
        }
        if("hgv".equals(args[0])){
            File dir0 = new File(args[2]);
            try{
                File syserr = new File("hgv.system.err");
                PrintStream oldSysErr = System.err;
                File sysout = new File("hgv.system.out");
                PrintStream oldSysOut = System.out;
                PrintStream newErr = new PrintStream(new FileOutputStream(syserr,false)); 
                PrintStream newOut = new PrintStream(new FileOutputStream(sysout,false)); 
                System.err.flush();
                System.setErr(newErr);
                System.out.flush();
                System.setOut(newOut);
                System.out.println(dir0.getPath());
                System.out.println(dir0.getCanonicalPath());
                dir0.delete();
                Directory baseDir = FSDirectory.getDirectory( dir0 , true);
                baseDir.setLockFactory(NoLockFactory.getNoLockFactory());
                IndexerFactory.indexHGV(new File(args[1]),baseDir,true);
                newErr.flush();
                System.setErr(oldSysErr);
                newOut.flush();
                System.setOut(oldSysOut);
                newErr.close();
                newOut.close();
                System.exit(0);
            } catch(Exception e){
               e.printStackTrace();
                return;
            }
        }
        else if("apis".equals(args[0])){
            System.out.println("starting APIS-OAI index..." + new java.util.Date());
            
            File dir0 = new File(args[2]);
            try{
                File syserr = new File("apis.system.err");
                PrintStream oldSysErr = System.err;
                File sysout = new File("apis.system.out");
                PrintStream oldSysOut = System.out;
                PrintStream newErr = new PrintStream(new FileOutputStream(syserr,false)); 
                PrintStream newOut = new PrintStream(new FileOutputStream(sysout,false)); 
                System.err.flush();
                System.setErr(newErr);
                System.out.flush();
                System.setOut(newOut);
                System.out.println(dir0.getPath());
                System.out.println(dir0.getCanonicalPath());
                dir0.delete();
                Directory baseDir = FSDirectory.getDirectory( dir0 , true);
                IndexerFactory.indexAPIS_OAI(new URL(args[1]),baseDir,true);
                newErr.flush();
                System.setErr(oldSysErr);
                newOut.flush();
                System.setOut(oldSysOut);
                newErr.close();
                newOut.close();
                System.out.println("finishing APIS-OAI index..." + new java.util.Date());
                System.exit(0);
            } catch(Exception e){
               e.printStackTrace();
                return;
            }
        }
        else if ("crosswalk".equals(args[0])){
            File dir0 = new File(args[2]);
            try{
                System.out.println(dir0.getPath());
                System.out.println(dir0.getCanonicalPath());
                dir0.delete();
                Directory baseDir = FSDirectory.getDirectory( dir0 , true);
                IndexerFactory.indexNServer(new File(args[1]).toURL(),baseDir,true);
                System.exit(0);
            } catch(Exception e){
               e.printStackTrace();
                return;
            }
        }
        printUsage();
    }
    
    private static void printUsage(){
        System.out.println("usage: java -jar <jarname> [apis | hgv | xref] <src filepath> <index dirpath>");
    }

}
