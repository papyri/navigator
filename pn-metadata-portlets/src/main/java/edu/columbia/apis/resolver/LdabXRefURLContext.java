package edu.columbia.apis.resolver;

import javax.naming.Name;

import org.apache.lucene.document.Document;

public class LdabXRefURLContext extends XRefURLContext {
    LdabXRefURLContext(Name name, Document doc){
        super(name,doc);
    }
    LdabXRefURLContext(String name, Document doc){
        super(name,doc);
    }

    
    @Override
    Object lookupTm(String arg0) {
        return this.name;
    }   
}
