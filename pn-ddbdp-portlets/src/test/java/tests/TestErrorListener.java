package tests;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class TestErrorListener implements ErrorListener {
    private  enum Level  {WARN, ERROR, FATAL};
    private static String getMsg(TransformerException e, Level level){
        String result = e.getLocationAsString() + " ; " + e.toString();
        switch(level){
        case WARN:
            return "[warning] " + result;
        case ERROR:
            return "[error] " + result;
        case FATAL:
            return "[fatal] " + result;
        }
        return "[unknown]: " + result;
    }
    public void error(TransformerException e) throws TransformerException {
        System.out.println(getMsg(e,Level.ERROR));
        e.printStackTrace();
        if(e.getCause() != null){
            e.getCause().printStackTrace();
        }
        throw e;
    }

    public void fatalError(TransformerException e)
            throws TransformerException {
        System.out.println(getMsg(e,Level.ERROR));
        e.printStackTrace();
        throw e;
    }

    public void warning(TransformerException e) throws TransformerException {
        System.out.println(getMsg(e,Level.WARN));
        e.printStackTrace();
    }

}
