package info.papyri.ddbdp.xslt;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

public class Log4JTransformListener implements ErrorListener {
    private final Logger log;
    public Log4JTransformListener(Logger logger){
        this.log = logger;
    }

    public void error(TransformerException arg0) throws TransformerException {
        SourceLocator loc = arg0.getLocator();
        String msg = "error: " + loc.getSystemId() + " line " + loc.getLineNumber() + "; column " + loc.getColumnNumber() + " : " + arg0.toString(); 
        this.log.error(msg,arg0);
    }

    public void fatalError(TransformerException arg0)
            throws TransformerException {
        SourceLocator loc = arg0.getLocator();
        String msg = "error: " + loc.getSystemId() + " line " + loc.getLineNumber() + "; column " + loc.getColumnNumber() + " : " + arg0.toString(); 
        this.log.fatal(msg,arg0);
        throw arg0;
    }

    public void warning(TransformerException arg0) throws TransformerException {
        SourceLocator loc = arg0.getLocator();
        String msg = "error: " + loc.getSystemId() + " line " + loc.getLineNumber() + "; column " + loc.getColumnNumber() + " : " + arg0.toString(); 
        this.log.warn(msg,arg0);
    }

}
