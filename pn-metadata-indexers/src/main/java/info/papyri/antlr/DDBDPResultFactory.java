package info.papyri.antlr;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.xml.sax.Attributes;

public class DDBDPResultFactory implements ObjectCreationFactory {
    private String pattern;
    public DDBDPResultFactory(String pattern){
       this.pattern = pattern;   
    }

    public Object createObject(Attributes arg0) throws Exception {
        // TODO Auto-generated method stub
        return new DDBDPResult(this.pattern);
    }

    public Digester getDigester() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setDigester(Digester arg0) {
        // TODO Auto-generated method stub

    }

}
