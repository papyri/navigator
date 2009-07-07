package info.papyri.tests;

import junit.framework.TestCase;
import info.papyri.epiduke.sax.TEILineHandler;


public class DdbIdParsingTest extends TestCase {
    public void testNoVolume(){
        String id = "c.etiq.mom.1657";
        String n = "0003;;1657";
        String [] expected = new String[]{"c.etiq.mom","0000","1657"};
        String [] actual = TEILineHandler.getNameParts(id, n);
        for(int i=0;i<3;i++){
            assertEquals(expected[i],actual[i]);
        }
    }
    
    public void testStandard(){
        String id = "p.prag.varcl.NS.2";
        String n = "0205;NS;2";
        String [] expected = new String[]{"p.prag.varcl","NS","2"};
        String [] actual = TEILineHandler.getNameParts(id, n);
        for(int i=0;i<3;i++){
            assertEquals(expected[i],actual[i]);
        }
    }
    public void testNonNumeric(){
        String id = "p.stras.1.2";
        String n = "0205;1;2";
        String [] expected = new String[]{"p.stras","0001","2"};
        String [] actual = TEILineHandler.getNameParts(id, n);
        for(int i=0;i<3;i++){
            assertEquals(expected[i],actual[i]);
        }
    }
    public void testPunctuatedVolume(){
        String id = "c.pap.gr.2.1.10";
        String n = "0006;2.1;10";
        String [] expected = new String[]{"c.pap.gr","0002.1","10"};
        String [] actual = TEILineHandler.getNameParts(id, n);
        for(int i=0;i<3;i++){
            assertEquals(expected[i],actual[i]);
        }
    }
}
