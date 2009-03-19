package info.papyri.resolver;

import javax.naming.Name;

import org.apache.lucene.document.Document;

public class DdbXRefURLContext extends XRefURLContext {
    DdbXRefURLContext(Name name, Document doc){
        super(name,doc);
    }
    DdbXRefURLContext(String name, Document doc){
        super(name,doc);
    }

    
    @Override
    Object lookupDdb(String arg0) {
        return this.name;
    }
    @Override
    Object lookupHgv(String arg0){
        Object result = super.lookupHgv(arg0);
        if(result == null) return this.name;
        return result;
    }
}
