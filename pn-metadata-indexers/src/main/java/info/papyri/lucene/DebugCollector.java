package info.papyri.lucene;
import org.apache.lucene.search.*;
import org.apache.lucene.document.*;

import info.papyri.metadata.CoreMetadataFields;

import java.util.*;
import java.io.*;

public class DebugCollector extends SimpleCollector {
    private final Weight w;
    private final PrintWriter out;
    public DebugCollector(PrintWriter out,IndexSearcher s, Weight w) throws IOException {
        super(s);
        this.w = w;
        this.out = out;
        
    }
    @Override
    public void collect(int arg0, float arg1) {
        // TODO Auto-generated method stub
        super.collect(arg0, arg1);
        try{
            out.println("doc: " + this.searcher.doc(arg0).get(CoreMetadataFields.DOC_ID));
            out.println(this.searcher.explain(w, arg0));
            out.flush();
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}
