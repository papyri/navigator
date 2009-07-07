package info.papyri.tests;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;

import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.NamespacePrefixes;

import java.util.*;
import java.io.IOException;
public class DdbIndexTest extends GreekTestsBase {
    private static void printDoc(Document doc){
        List fields = doc.getFields();
        for(Object fo:fields){
            Field field = (Field)fo;
            System.out.print(field.name());
            System.out.print(": ");
            System.out.println(field.stringValue());
        }
    }
    public void testIdFields() throws IOException {
        Document doc = check.doc(0);
        printDoc(doc);
    }
    public void testStoredImgFlag() throws IOException {
        String mp = "C:/staging/data/index/merge";
        IndexReader rdr = IndexReader.open(mp);
        IndexSearcher srch = new IndexSearcher(rdr);
        Term t = new Term(CoreMetadataFields.XREFS,NamespacePrefixes.APIS + "columbia:p223");
        TermQuery q = new TermQuery(t);
        Document doc = srch.search(q).doc(1);
        printDoc(doc);
    }
}
