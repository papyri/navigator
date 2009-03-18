/*
 * Provides the interface for running all pn-metadata-indexer operations from
 * the command line.
 */

package info.papyri.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;

import info.papyri.digester.APISCentricMergeIndexer;
import info.papyri.digester.IndexerFactory;

/**
 *
 * @author hcayless
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0 || "-h".equals(args[0]) || "--help".equals(args[0])) {
            printHelp();
            System.exit(0);
        } else if("hgv".equals(args[0])){
            String in = "";
            String out = "";
            for (int i = 1; i < args.length; i++) {
                if ("-i".equals(args[i])) {
                    in = args[++i];
                } else if ("-o".equals(args[i])) {
                    out = args[++i];
                }

            }
            File dir0 = new File(out);
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
                IndexerFactory.indexHGV(new File(in),baseDir,true);
                newErr.flush();
                System.setErr(oldSysErr);
                newOut.flush();
                System.setOut(oldSysOut);
                newErr.close();
                newOut.close();
                System.exit(0);
            } catch(Exception e){
               e.printStackTrace();
            }
            System.exit(0);
        }
        else if("apis".equals(args[0])){
            System.out.println("starting APIS-OAI index..." + new java.util.Date());
            String in = "";
            String out = "";
            for (int i = 1; i < args.length; i++) {
                if ("-i".equals(args[i])) {
                    in = args[++i];
                } else if ("-o".equals(args[i])) {
                    out = args[++i];
                }

            }
            File dir0 = new File(out);
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
                IndexerFactory.indexAPIS_OAI(new URL(in),baseDir,true);
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
            }
            System.exit(0);
        } else if ("merge".equals(args[0])) {
            String apis = "";
            String hgv = "";
            String out = "";
            for (int i = 1; i < args.length; i++) {
                if ("-a".equals(args[i])) {
                    apis = args[++i];
                } else if ("-h".equals(args[i])) {
                    hgv = args[++i];
                } else if ("-o".equals(args[i])) {
                    out = args[++i];
                }

            }
            APISCentricMergeIndexer.run(apis, hgv, out);
            System.exit(0);
        }
        printHelp();
    }

    public static void printHelp() {
        System.out.println("Usage: java -jar pn-metadata-indexer.jar [apis|hgv|merge] [-i <input url/directory>] [-a <apis index directory>] [-h <hgv index directory>] -o <output directory>");
    }

}
