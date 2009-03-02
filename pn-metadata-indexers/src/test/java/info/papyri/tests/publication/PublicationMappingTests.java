package info.papyri.tests.publication;

import info.papyri.util.DBUtils;
import junit.framework.*;

import java.sql.SQLException;
import java.util.regex.*;
import java.util.Set;

import info.papyri.data.publication.*;

public class PublicationMappingTests extends TestCase {
    Pattern plainPub;
    Pattern embeddedPub;
    private static void oneTimeSetUp() {
        try{
            java.net.URL derbydata = PublicationMappingTests.class.getResource("/xml/ddbdp-perseus.xml");
            info.papyri.util.DBUtils.setupDerby(derbydata);
        }
        catch(java.io.IOException e){
            e.printStackTrace();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    static {
        oneTimeSetUp();
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // set up IOC data for HGV and APIS
        // feed ioc data into APISCentricAnalyzer
        // pull mapped data out

    }
    
    public void testStructPub(){
        String in = "P.Oxy. I 36";
        String expected = "series=0181;volume=1;document=36;side=*";
        Set<String> actual = StructuredPublication.getStructuredPub(in);
        if(!actual.contains(expected)){
            System.err.println("actual: " + actual);
            System.err.println("expected: " + expected);
            fail();
        }
    }
    public void testStructPubColumn(){
        String in = "P.Col. II 1 R 4, Kol. 2";
        String expected = "series=0094;volume=2;document=1;side=RECTO;subdoc=4;span=column:2";
        Set<String> actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
        in = "P.Bub. I 4 Kol. V";
        expected = "series=0086;volume=1;document=4;side=*;span=column:5";
        actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
    }
    public void testStructPubSubdocSpan(){
        String in = "P.Mich. IV 357 B - D";
        String expected = "series=0163;volume=4;document=357;side=*;subdoc=C";
        Set<String> actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
    }
    public void testStructPubNoVol(){
        String in = "O.Kellis 2";
        String expected = "series=0271;volume=*;document=2;side=*";
        Set<String> actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
        in = "O.Berl. 22";
        expected = "series=0013;volume=*;document=22;side=*";
        actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
    }
    public void testStructPubNoVolSubdoc(){
        String in = "Jur.Pap. 56 d";
        String expected = "series=x333;volume=*;document=56;side=*;subdoc=D";
        Set<String> actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
    }
    public void testStructPubSide(){
        String in = "BGU I 199 R";
        String expected = "series=0001;volume=1;document=199;side=RECTO";
        Set<String> actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
    }
    public void testStructPubLineSpan(){
        String in = "BGU IV 1151 I Z. 1 - 18";
        String expected = "series=0001;volume=4;document=1151;side=*;subdoc=I;span=line:1-18";
        Set<String> actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
    }
    public void testStructPubColumnSpan(){
        String in = "BGU IV 1151 I Kol. I - IV";
        String expected = "series=0001;volume=4;document=1151;side=*;subdoc=I;span=column:I-IV";
        Set<String> actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
    }
    public void testStructPubPageSpan(){
        String in = "P.Oxy. I 9 V S. 77";
        String expected = "series=0181;volume=1;document=9;side=VERSO;span=page:77";
        Set<String> actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
        in = "P.Haw. 13 R S. 29";
        expected = "series=x312;volume=*;document=13;side=RECTO;span=page:29";
        actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
    }
    public void testStructPubPageSpanParens(){
        String in = "P.Lond. III 1164 (i)  (S. 165)";
        String expected = "series=0154;volume=3;document=1164;side=*;subdoc=I;span=page:165";
        Set<String> actual = StructuredPublication.getStructuredPub(in);
        assertTrue(actual.contains(expected));
    }
    
}