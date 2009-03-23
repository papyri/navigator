package info.papyri.ddbdp.parser;

import org.apache.log4j.Logger;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

public class DefaultReporter implements ErrorReporter {
    private static final Logger LOG = Logger.getLogger(DefaultReporter.class);
    public void warning(String message, String sourceName, int line,
        String lineSource, int lineOffset){
        LOG.warn(message);
    }
    public void error(String message, String sourceName, int line,
            String lineSource, int lineOffset){
        LOG.error(message);
    }
    public EvaluatorException runtimeError(String message, String sourceName, int line,
            String lineSource, int lineOffset){
        LOG.error(message);
        return new EvaluatorException(message,sourceName,line,lineSource,lineOffset);
    }
}
