package edu.columbia.apis.resolver;


import javax.naming.Name;

import org.apache.lucene.document.*;

public class ApisXRefURLContext extends XRefURLContext {

    ApisXRefURLContext(Name name, Document doc){
        super(name,doc);
    }
    ApisXRefURLContext(String name, Document doc){
        super(name,doc);
    }

    
    @Override
    Object lookupApis(String arg0) {
        return this.name;
    }

}
