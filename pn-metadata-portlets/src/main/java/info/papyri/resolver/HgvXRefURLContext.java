package info.papyri.resolver;

import javax.naming.Name;

import org.apache.lucene.document.Document;

public class HgvXRefURLContext extends XRefURLContext {
    HgvXRefURLContext(Name name, Document doc){
        super(name,doc);
    }
    HgvXRefURLContext(String name, Document doc){
        super(name,doc);
    }

    
    @Override
    Object lookupHgv(String arg0) {
        return this.name;
    }   
}
