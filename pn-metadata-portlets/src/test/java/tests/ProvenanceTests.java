package tests;

import info.papyri.metadata.provenance.*;
import junit.framework.TestCase;

public class ProvenanceTests extends TestCase {
    public void testProvenanceLD() {
        String source = "Euhermeria";
        String actual = ProvenanceControl.match(source);
        String expected = "Euhemeria";
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
        source = "Herakleoplites";
        expected = "Herakleopolite";
        actual = ProvenanceControl.match(source); 
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
        source = "Cynopolis";
        expected = "Kynopolis";
        actual = ProvenanceControl.match(source); 
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
        source="Herakleopolitenome";
        expected="Herakleopolite";
        actual = ProvenanceControl.match(source); 
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
    }
}
