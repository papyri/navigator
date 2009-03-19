package info.papyri.numbers.indexer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.jdom.input.DOMBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import info.papyri.metadata.*;
import edu.columbia.apis.data.XREFIndices;
import edu.columbia.apis.digester.APISCentricAnalyzer;


public class Indexer {
    public static final String CONTROL_NAME = "";
    public static final String APIS_ID = "oai:papyri.info:identifiers:apis";
    public static final String HGV_ID = "oai:papyri.info:identifiers:hgv";
    public static final String DDB_ID = "oai:papyri.info:identifiers:ddbdp";
    public static final String TM_ID = "oai:papyri.info:identifiers:trismegistos";
    public static final String INV_ID = "oai:papyri.info:identifiers:inventory";
    
    private static Term CN_TEMPLATE = new Term(CONTROL_NAME,"");
    private static Term HGV_ID_TEMPLATE = new Term(HGV_ID,"");
    private static Term APIS_ID_TEMPLATE = new Term(APIS_ID,"");
    
    public IndexWriter writer;

    public Directory indexDir;

    public int numIndexedRecords = 0;

    public int numFailedRecords = 0;

    private static XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
    private static DOMBuilder db = new DOMBuilder();
    private HashSet<String> indexed = new HashSet<String>();
    public Indexer(Directory dir, Analyzer analyzer, boolean create) throws IOException{
        this.indexDir = dir;
        this.writer = new IndexWriter(this.indexDir, analyzer, create);
        this.writer.setMergeFactor(25);
    }

    public Indexer(Directory dir, Analyzer analyzer) throws IOException{
        this(dir,analyzer,true);
    }
    public void addRDF(org.w3c.dom.Node RDF) throws IOException{
        if (RDF == null) return;
        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
        NodeList children = ((Element)RDF).getElementsByTagName("Coverage");
        if (children.getLength() == 0){
            System.out.println("WARNING: Node had no \"Coverage\" children!");
        }
        boolean apis = false;
        boolean hgv = false;
        boolean publication = false;
        coverages:
            for (int i=0;i<children.getLength();i++){
                Element coverage =  (Element)children.item(i);
                String coverageResource = coverage.getAttribute("resource");
                if (coverageResource == null) continue coverages;
                NodeList ids = coverage.getElementsByTagName("Identifier");
                ids:
                    for (int j=0;j<ids.getLength();j++){
                        Element idEl = (Element)ids.item(j);
                        String idResource = idEl.getAttribute("resource");
                        if (idResource == null) continue ids;
                        if (XREFIndices.INVENTORY.equals(coverageResource)){
                            doc.add(new Field(coverageResource,idResource,Field.Store.YES,Field.Index.TOKENIZED));
                        }
                        else{
                            doc.add(new Field(coverageResource,idResource,Field.Store.YES,Field.Index.UN_TOKENIZED));
                        }
                        if (XREFIndices.APIS_ID.equals(coverageResource) && !idResource.startsWith("none")) {
                            apis = true;
                        }
                        else if (XREFIndices.HGV_ID.equals(coverageResource) && idResource != null){
                            hgv = true;
                        }
                        else if (XREFIndices.PUBLICATION.equals(coverageResource) && idResource != null){
                            publication = true;
                            Iterator<String> series = edu.columbia.apis.data.publication.PublicationMatcher.indexableSeries(idEl.getAttribute("resource")).iterator();
                            while (series.hasNext()){
                                doc.add(new Field(XREFIndices.PUBLICATION_SERIES,series.next(),Field.Store.YES,Field.Index.UN_TOKENIZED));
                            }
                        }
                    }

            }
        NodeList di = ((Element)RDF).getElementsByTagName(APISCentricAnalyzer.DATE_INDEX_ELEMENT);
        if (di.getLength() > 0){
            Node item = di.item(0);
            String val = item.getNodeValue();
            if (item.getChildNodes().getLength() > 0) val = item.getChildNodes().item(0).getNodeValue();
            if (val != null){
                doc.add(new Field(XREFIndices.PREFERRED_DATE_INDEX,val,Field.Store.YES,Field.Index.UN_TOKENIZED));
            }
            else {
                System.out.println("Missing " + XREFIndices.PREFERRED_DATE_INDEX + " value");
                org.jdom.Element ele = db.build((Element)RDF);
                out.output(ele, System.out);
            }
        }

        NodeList hi = ((Element)RDF).getElementsByTagName(APISCentricAnalyzer.HAS_IMAGE_ELEMENT);
        if (hi.getLength() > 0){
            Node item = hi.item(0);
            String val = item.getNodeValue();
            if (item.getChildNodes().getLength() > 0) val = item.getChildNodes().item(0).getNodeValue();
            if (val != null){
                doc.add(new Field(XREFIndices.IMAGE_FLAG,val,Field.Store.YES,Field.Index.UN_TOKENIZED));
            }
            else {
                System.out.println("Missing " + XREFIndices.IMAGE_FLAG + " value");
                org.jdom.Element ele = db.build((Element)RDF);
                out.output(ele, System.out);
            }
        }

        NodeList ti = ((Element)RDF).getElementsByTagName(APISCentricAnalyzer.HAS_TRANSLATION_ELEMENT);
        if (ti.getLength() > 0){
            Node item = ti.item(0);
            String val = item.getNodeValue();
            if (item.getChildNodes().getLength() > 0) val = item.getChildNodes().item(0).getNodeValue();
            if (val != null){
                doc.add(new Field(XREFIndices.TRANSLATION_FLAG,val,Field.Store.YES,Field.Index.UN_TOKENIZED));
            }
            else {
                System.out.println("Missing " + XREFIndices.TRANSLATION_FLAG + " value");
                org.jdom.Element ele = db.build((Element)RDF);
                out.output(ele, System.out);
            }
        }

        if (!apis && hgv){
            doc.add(new Field(XREFIndices.HGV_EXCLUSIVE_FLAG,XREFIndices.EXCLUSIVE_FLAG_VALUE,Field.Store.NO,Field.Index.UN_TOKENIZED));
        }
        if (!hgv && apis){
            doc.add(new Field(XREFIndices.APIS_EXCLUSIVE_FLAG,XREFIndices.EXCLUSIVE_FLAG_VALUE,Field.Store.NO,Field.Index.UN_TOKENIZED));
        }
        if (publication){
            doc.add(new Field(XREFIndices.PUBLICATION_FLAG,APISIndices.SORTABLE_YES_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        else{
            doc.add(new Field(XREFIndices.PUBLICATION_FLAG,APISIndices.SORTABLE_NO_VALUE,Field.Store.YES,Field.Index.UN_TOKENIZED));
        }
        writer.addDocument(doc);
    }

    
}
