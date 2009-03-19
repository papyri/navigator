package info.papyri.numbers.indexer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.NodeCreateRule;
import org.apache.commons.digester.ObjectCreationFactory;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import edu.columbia.apis.data.Indexer;
import edu.columbia.apis.digester.IndexerFactory;

public class Main  implements ObjectCreationFactory {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
    
    public Object createObject(Attributes arg0) throws Exception {
        //System.out.println("Pushing Indexer object onto digester stack...");
        WhitespaceAnalyzer ws = new WhitespaceAnalyzer();
        return new Indexer(indexDir,ws,this.create);
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
            indexer.writer.optimize();
            indexer.writer.close();
        } catch (SAXParseException spe) {
            System.out.println("SAXParseException: " + spe);
        }
        finally{
            
        }
        return indexer;
    }

}
