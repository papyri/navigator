package info.papyri.tests.hgv_fmp;
import java.io.*;
import org.apache.lucene.search.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.*;

import info.papyri.data.XREFIndices;

import java.util.*;

import javax.xml.transform.stream.StreamSource;
public class HGVMappingTest implements org.xml.sax.ContentHandler {
    StringBuffer publ;
    StringBuffer apis;
    boolean append = false;
    int td = -1;
    public HashMap<String,ArrayList<String>> hgvMap = new HashMap<String,ArrayList<String>>();
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        // TODO Auto-generated method stub
        if(append){
            switch(td){
            case 0:
                break;
            case 1:
                break;
            case 2:
                publ.append(arg0,arg1,arg2);
                break;
            case 3:
                break;
            case 4:
                apis.append(arg0,arg1,arg2);
                break;
            case 5:
                apis.append(arg0,arg1,arg2);
                break;
            }
        }
    }

    public void endDocument() throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public void endElement(String uri, String localName, String qname){
        if("TR".equals(qname)){
            append = false;
            td = -1;
            String apisCN = apis.toString().trim();
            apisCN = apisCN.replaceAll("\\s", "");
            String hgv = publ.toString().trim();
            if(!hgvMap.containsKey(publ)){
                hgvMap.put(hgv,new ArrayList<String>());
            }
            hgvMap.get(hgv).add(apisCN);
        }
        
    }

    public void endPrefixMapping(String arg0) throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public void processingInstruction(String arg0, String arg1) throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public void setDocumentLocator(Locator arg0) {
        // TODO Auto-generated method stub
        
    }

    public void skippedEntity(String arg0) throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public void startDocument() throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public void startElement(String uri, String localName, String qname,
            Attributes atts) throws SAXException {
        if("TR".equals(qname)){
            publ = new StringBuffer();
            apis = new StringBuffer();
            append = true;
            td = -1;
        }
        if("TD".equals(qname)){
            td++;
            if(td == 5) apis.append(".apis.");
        }
    }

    public void startPrefixMapping(String arg0, String arg1) throws SAXException {
        // TODO Auto-generated method stub
        
    }

    public static void main(String[] args) throws Exception {
        XMLReader reader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
        HGVMappingTest main = new HGVMappingTest();
        reader.setContentHandler(main);
        reader.parse(new InputSource(HGVMappingTest.class.getResourceAsStream("mapping.htm")));
        PrintStream report = new PrintStream("report.txt");
        File dir0 = new File("C:\\staging\\data\\index\\crosswalk");
        Directory baseDir = FSDirectory.getDirectory( dir0 , false);
        IndexSearcher search = new IndexSearcher(baseDir);
        for(int i=0;i<search.getIndexReader().maxDoc();i++){
            if(search.getIndexReader().isDeleted(i))continue;
            Document doc = search.doc(i);
            
            String apis = doc.get(XREFIndices.APIS_ID);
            if(apis == null || apis.startsWith("none.apis.")) {
//                String hgv = doc.get(XREFIndices.HGV_ID);
//                if(hgv==null) continue;
//
//                ArrayList<String> apisIDs = main.hgvMap.get(hgv);
//                if(apisIDs == null) continue;
//                for(String apisID:apisIDs){
//                    String msg = "JamesSays\tHand mapping of " + hgv + " to " + apisID + " was not found among auto [ns: hgv, no apis]";
//                    System.out.println(msg);
//                    report.println(msg);
//                }
                continue;
            }
            String [] hgv = doc.getValues(XREFIndices.HGV_ID);
            if (hgv == null){
                for(String hgvId :main.hgvMap.keySet()){
                    ArrayList<String> apisIDs = main.hgvMap.get(hgvId);
                    if(apisIDs.contains(apis)){
                        String msg = "JamesSays\tHand mapping of " + hgvId + " to " + apis + " was not found among auto [ns: apis, no hgv]";
                        System.out.println(msg);
                        report.println(msg);
                    }
                }
            }
            else{
                for(String hgvId:hgv){
                    hgvId = hgvId.trim();
                    ArrayList<String> apisIDs = main.hgvMap.get(hgvId);
                    if(apisIDs == null){
                        String msg = "RobotSays\tAuto mapping of " + hgvId + " to " + apis + " was not found among hand (hgvId not in manual mapping)";
                        System.out.println(msg);
                        report.println(msg);
                    }
                    else{
                        if(!apisIDs.contains(apis)){
                            String msg = "RobotSays\tAuto mapping of " + hgvId + " to " + apis + " was not found among hand (first among others was : " + apisIDs.get(0) + ")";
                            System.out.println(msg);
                            report.println(msg);
                        }
                    }
                }
            }
        }
        Term HGV = new Term(XREFIndices.APIS_ID,"");
        for(String hgv:main.hgvMap.keySet()){
            ArrayList<String> apisIDs = main.hgvMap.get(hgv);
            for(String apis:apisIDs){
                TermQuery q = new TermQuery(HGV.createTerm(apis));
                Hits hits = search.search(q);
                Iterator<Hit> iter = hits.iterator();
                while(iter.hasNext()){
                    Hit hit = iter.next();
                    String [] hgvs = hit.getDocument().getValues(XREFIndices.HGV_ID);
                    if(hgvs == null || !java.util.Arrays.asList(hgvs).contains(hgv)){
                        String msg = (hgvs != null)?( "JamesSays\tHand mapping of " + hgv + " to " + apis + " was not found among auto [ " + hgvs.length + " ] maps were]"):
                            ( "JamesSays\tHand mapping of " + hgv + " to " + apis + " was not found among auto [ 0 ] maps were");
                        if (hgvs != null){
                            msg += " starting with [ " + hgvs[0] + " ]";
                        }
                        System.out.println(msg);
                        report.println(msg);
                    }
                }
            }

        }
        report.flush();
        report.close();
    }

}
