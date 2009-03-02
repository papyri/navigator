package info.papyri.tests.metadata;

import java.util.HashMap;

import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.NamespacePrefixes;
import info.papyri.util.DBUtils;
import info.papyri.data.Indexer;
import info.papyri.digester.APISCentricMergeIndexer;
import info.papyri.digester.IndexerFactory;
import junit.framework.TestCase;

import org.apache.lucene.store.*;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;

import info.papyri.tests.PublicationTests;

public class NumberServerTest extends TestCase {
    static IndexSearcher getNumbers(){
        try{
        Directory dir = new RAMDirectory();
        HashMap<String, Directory> directories = new HashMap<String,Directory>();
        directories.put("out", dir);
        directories.put("apis", PublicationTests.APIS.getIndexReader().directory());
        directories.put("hgv", PublicationTests.HGV.getIndexReader().directory());
        APISCentricMergeIndexer indexer = new APISCentricMergeIndexer(true,false,directories);
        indexer.merge();
        return  new IndexSearcher(dir);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    public final static IndexSearcher NUMBERS = getNumbers();
    
    public void testSubdocMapping() throws Exception {
        TermQuery q = new TermQuery(new Term(CoreMetadataFields.DOC_ID,NamespacePrefixes.APIS + "michigan:1638"));
        Hits h = NUMBERS.search(q);
        assertTrue("unexpected result size for " + CoreMetadataFields.DOC_ID + " search: " + h.length(),h.length() == 1);
        Document test = h.doc(0);
        String [] xrefs = test.getValues(CoreMetadataFields.XREFS);
        assertTrue("No xrefs found for expected mapped document",(xrefs!=null && xrefs.length>0));
        int hgv = 0;
        for(String xref:xrefs){
            if(xref.startsWith(NamespacePrefixes.HGV))hgv++;
            if(hgv > 1) fail("expected 1 hgv match; had multiple");
        }
        
    }
    
    public void testSanity(){
        try{
            String HGVval = NamespacePrefixes.HGV + "P.Col.:7:188";
            String APISval = NamespacePrefixes.APIS + "columbia:p223";
            TermQuery q = new TermQuery(new Term(CoreMetadataFields.XREFS,HGVval));
            Hits h = NUMBERS.search(q);
            int hits = h.length();
            assertTrue("No document hits for " + CoreMetadataFields.XREFS + " = " + HGVval,hits > 0);
            if (hits > 0){
                System.out.println("INFO: " + hits + " docs associated with ["+ CoreMetadataFields.XREFS + " = " + HGVval + "]");
            }
            TermEnum terms = NUMBERS.getIndexReader().terms(new Term(CoreMetadataFields.DOC_ID,""));
            do{
                System.out.println("TEST: " + terms.term());
            }while (terms.next() && terms.term().field().equals(CoreMetadataFields.DOC_ID));
            boolean pass = false;
            for (int i=0;i<hits;i++){
                Document d = h.doc(i);
                Field f = d.getField(CoreMetadataFields.DOC_ID);
                if (f.stringValue().equals(APISval)) {
                    pass = true;
                    break;
                }
                
            }
            assertTrue("Could not match [" + CoreMetadataFields.XREFS + " = " + HGVval + "] with [" + CoreMetadataFields.DOC_ID + " = " + APISval + "]",pass);
        }
            catch (Exception e){
                fail(e.toString());
            }
    }
 
    public void testNoHGV(){ 
            String DDBval = NamespacePrefixes.DDBDP + "0055::62";
       try{
            Query q = new TermQuery(new Term(CoreMetadataFields.XREFS,DDBval));
            Hits h = NUMBERS.search(q);
            int hits = h.length();
            if (hits > 0){
                System.out.println("INFO: " + hits + " docs associated with ["+ CoreMetadataFields.XREFS + " = " + DDBval + "]");
            }            
            boolean pass = hits > 0;
            q = new TermQuery(new Term(CoreMetadataFields.DOC_ID,DDBval));
            h = NUMBERS.search(q);
            if(h.length()==0){
                System.err.println(DDBval + " not in Numbers as docId");
                h = PublicationTests.HGV.search(q);
                if(h.length()==0)System.err.println(DDBval + " not in HGV as docId");
                else System.err.println(DDBval + " IS in HGV as docId");
            }
            assertTrue("No document hits for " + CoreMetadataFields.XREFS + " = " + DDBval,pass);
        }
        catch (Exception e){
            fail(e.toString());
        }
    }
}
