package info.papyri.tests;

import info.papyri.metadata.CoreMetadataFields;

import java.io.IOException;
import java.util.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
public class PlaceTest extends GreekTestsBase {
    public void testRec() throws IOException {
        String path = "/C:/DOCUME~1/User/MYDOCU~1/ddbdp/epidoc-fast/metadata";
        IndexReader rdr = IndexReader.open(path);
        Document doc = rdr.document(20000);
        List<Field> fields = doc.getFields();
        for(Field field:fields){
            System.out.println(field.name() + "\t\t" + field.stringValue());
//            String [] vals = doc.getValues(field.name());
//            for (String val:vals){
//            }
        }
    }
    public void testHGV() throws IOException {
        String path = "/C:/DOCUME~1/User/MYDOCU~1/ddbdp/epidoc-fast/metadata";
        IndexReader rdr = IndexReader.open(path);
        IndexSearcher srch = new IndexSearcher(rdr);
        Term t = new Term(CoreMetadataFields.APIS_COLLECTION,"columbia");
        TermQuery q = new TermQuery(t);
        Hits hits = srch.search(q);
        Document doc = hits.doc(15);
        List<Field> fields = doc.getFields();
        for(Field field:fields){
            System.out.println(field.name() + "\t\t" + field.stringValue());
//            String [] vals = doc.getValues(field.name());
//            for (String val:vals){
//            }
        }
    }
}
