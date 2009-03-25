package tests;

import junit.framework.TestCase;
import info.papyri.ddbdp.servlet.*;
import info.papyri.epiduke.sax.TEILineHandler;

public class FileNameParsingTest extends TestCase {
    public void testNoVolume(){
        String fName = "p.oxy.14.xml";
        String actual = Document.parseName(fName);
        String expected = "p.oxy/p.oxy.14.xml";
        assertEquals("E: " + expected + " ; A: " + actual, expected,actual);
    }
    public void testLongColl(){
        String fName = "p.cair.maspeth.2.67138.xml";
        String actual = Document.parseName(fName);
        String expected = "p.cair.maspeth/p.cair.maspeth.2/p.cair.maspeth.2.67138.xml";
        assertEquals("E: " + expected + " ; A: " + actual, expected,actual);
    }
    public void testCollNoDot(){
        String fName = "bgu.1.85.xml";
        String actual = Document.parseName(fName);
        String expected = "bgu/bgu.1/bgu.1.85.xml";
        assertEquals("E: " + expected + " ; A: " + actual, expected,actual);
    }
    public void testRomMilRec(){
        String fName = "rom.mil.rec.1.1.xml";
        String actual = Document.parseName(fName);
        String expected = "rom.mil.rec/rom.mil.rec.1/rom.mil.rec.1.1.xml";
        assertEquals("E: " + expected + " ; A: " + actual, expected,actual);
    }
    public void testIndexParsing(){
        String n = "0001;4;1145";
        String id = "bgu.4.1145";
        String [] parts = TEILineHandler.getNameParts(id, n);
        assertEquals("bgu", parts[TEILineHandler.COLL_IX]);
        assertEquals("0004", parts[TEILineHandler.VOL_IX]);
        assertEquals("1145", parts[TEILineHandler.DOC_IX]);
    }
}
