package info.papyri.tests.metadata;

import junit.framework.TestCase;
import info.papyri.data.provenance.*;

public class ProvenanceTest extends TestCase {
    public void testProvenanceLD() {
        String source = "Euhermeria";
        String actual = ProvenanceControl.match(source);
        String expected = "euhemeria";
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
        source = "Herakleoplites";
        expected = "herakleopolite";
        actual = ProvenanceControl.match(source); 
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
        source = "Cynopolis";
        expected = "kynopolis";
        actual = ProvenanceControl.match(source); 
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
        source="Herakleopolitenome";
        expected="herakleopolite";
        actual = ProvenanceControl.match(source); 
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
    }
    public void testProvenanceShingled() {
        String source = "Euhermeria";
        String actual = ProvenanceControl.matchShingled(source);
        String expected = "euhemeria";
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
        source = "Herakleoplites";
        expected = "herakleopolite";
        actual = ProvenanceControl.matchShingled(source); 
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
        source = "Cynopolis";
        expected = "kynopolis";
        actual = ProvenanceControl.matchShingled(source); 
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
        source="Herakleopolitenome";
        expected="herakleopolite";
        actual = ProvenanceControl.matchShingled(source); 
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
    }
}
