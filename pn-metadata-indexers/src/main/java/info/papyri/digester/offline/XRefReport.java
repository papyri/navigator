package info.papyri.digester.offline;

import java.io.*;
import java.util.*;
//import java.net.*;
import org.apache.commons.digester.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;

public class XRefReport implements ObjectCreationFactory {
    
    static final String APIS = "APIS:metadata:apis:controlname";
    Digester d;
    ArrayList<String> apis = new ArrayList<String>();
    public XRefReport(){
    }
    
    public void addCoverage(org.w3c.dom.Node node ){
        String apisRe = node.getAttributes().getNamedItem("resource").getNodeValue();
        if (!APIS.equals(apisRe)){
            return;
        }
        Node id = node.getFirstChild();
        this.apis.add(id.getAttributes().getNamedItem("resource").getNodeValue());
        
    }
    
    public static void main(String [] args) throws Exception {
        File report = new File("xref-report.txt");
        PrintStream sysout = System.out;
        PrintStream newout = new java.io.PrintStream(new java.io.FileOutputStream(report,false));
        System.setOut(newout);
        XRefReport orig = new XRefReport();
        Digester d = new Digester();
        //d.push(orig);
        try{
            //d.addFactoryCreate("DocumentIdentifiers", orig);
            //d.addFactoryCreate("DocumentIdentifiers/RDF", orig);
            d.addFactoryCreate("DocumentIdentifiers/RDF/Coverage", orig);
            d.addRule("DocumentIdentifiers/RDF/Coverage",new NodeCreateRule());
            d.addSetNext("DocumentIdentifiers/RDF/Coverage", "addCoverage", "org.w3c.dom.Node");
            d.parse(new File("C:\\staging\\data\\compile2.xml"));
        }
        catch (ParserConfigurationException pce){
            
        }
        catch (IOException ioe){
            
        }
        catch (SAXException se){
            
        }
        d = new Digester();
        XRefReport update = new XRefReport();
        
        try{
            d.addFactoryCreate("DocumentIdentifiers/RDF/Coverage", update);
            d.addRule("DocumentIdentifiers/RDF/Coverage",new NodeCreateRule());
            d.addSetNext("DocumentIdentifiers/RDF/Coverage", "addCoverage", "org.w3c.dom.Node");
            d.parse(new File("compile.xml"));
        }
        catch (ParserConfigurationException pce){
            
        }
        catch (IOException ioe){
            
        }
        catch (SAXException se){
            
        }
        String [] origApis = new String[orig.apis.size()]; 
        orig.apis.toArray(origApis);
        String [] updateApis = new String[update.apis.size()];
        update.apis.toArray(updateApis);
        Arrays.sort(origApis);
        Arrays.sort(updateApis);
        int found = 0;
        for (int i = 0; i < updateApis.length; i ++){
            int result = Arrays.binarySearch(origApis, updateApis[i]);
            if (result < 0) {
                System.out.println("+ " + updateApis[i]);
            }
        }
        
            found = 0;
            for (int i = 0; i < origApis.length; i ++){
                int result = Arrays.binarySearch(updateApis, origApis[i]);
                if (result < 0) {
                    System.out.println("- " + origApis[i]);
                }
            }
        System.out.flush();
        System.setOut(sysout);
        newout.flush();
        newout.close();
        System.out.println("Original map: " + origApis.length + " items; updated: " + updateApis.length);
    }

    public Object createObject(Attributes arg0) throws Exception {
        return this;
    }

    public Digester getDigester() {
        return d;
    }

    public void setDigester(Digester arg0) {
        d=arg0;
    }
    

}
