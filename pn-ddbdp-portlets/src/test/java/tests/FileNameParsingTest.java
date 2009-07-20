package tests;

import junit.framework.TestCase;
import info.papyri.epiduke.sax.TEILineHandler;

public class FileNameParsingTest extends TestCase {
    public void testIndexParsing(){
        String n = "0001;4;1145";
        String id = "bgu.4.1145";
        String [] parts = TEILineHandler.getNameParts(id, n);
        assertEquals("bgu", parts[TEILineHandler.COLL_IX]);
        assertEquals("0004", parts[TEILineHandler.VOL_IX]);
        assertEquals("1145", parts[TEILineHandler.DOC_IX]);
    }
}
