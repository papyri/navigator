package info.papyri.tests.publication;

import junit.framework.TestCase;
import java.util.*;
import java.util.regex.*;
import info.papyri.data.publication.*;

public class PubTests extends TestCase {
	private static final String[] CANONICAL_FORMS = new String[] {
		"P.Col.",
		"P.Col.Zen.",
		"P.Yale",
		"P.Yale Copt."
	};
    
    public void testGroupCount(){
        String data = "foo(foo)foo(foo(bar))foo";
        int count = PublicationMatcher.countTopLevelGroups(data);
        assertTrue("Expected 2 top level, counted " + count, count == 2);
    }
    
	public void testRegEx(){
		String pCol = "P.Col.Zen.";
		
		Pattern p = PublicationMatcher.getPattern(pCol, false);
		Matcher case1 = p.matcher("freeform -- freeform P.Col.Zen. freeform".toUpperCase());
		Matcher case2 = p.matcher("freeform -- freeform PColZen freeform".toUpperCase());
		Matcher case3 = p.matcher("freeform P.Col. Zen. freeform".toUpperCase());
		assertTrue("Exact match embedded did not match: " + p.pattern(),case1.find());
		assertTrue("Compressed no dots embedded: " + p.pattern(),case2.find());
		assertTrue("HGV spacing convention did  match",case3.find());
	}
	
	public void testBestMatch(){
		String data = "freeform -- freeform P.Col. Zen X,3a freeform";
		Collection<String> result = PublicationMatcher.findMatches(data);
		String expected = "P.Col.Zen. X 3 A";
		assertTrue("No match returned, 1 expected", result.size() > 0);
        if(result.size() > 1){
            System.err.println("Multiple matches when one was expected: " + result.size() + " matches");
            for(String r:result){
                System.err.println("\t" + r);
            }
        }
        String actual = result.iterator().next();
        actual = actual.replaceAll("\\$", "").replaceAll("\\s+", " ");
		assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
	}
	
	public void testBestMatches(){
		String data = "freeform -- freeform P.Yale Copt. V 2 freeform P.Yale Zen. X 3";
		Collection<String> result = PublicationMatcher.findMatches(data);
		assertTrue("No match returned, 1 expected", result.size() > 0);
        assertTrue("Expected 2 matches: " + result.size() + " matches", result.size() == 1);
	}
    
    public void testPCOl(){
        String data = "Z. 1-8: P.Col. IV 114 d";
        Collection<String> result = PublicationMatcher.findMatches(data);
        assertTrue("No match returned, 1 expected", result.size() > 0);
        String expected = "P.Col. IV 114 D";
        String actual = result.iterator().next();
        actual = actual.replaceAll("\\$", "").replaceAll("\\s+", " ");
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
        
    }
    
    public void testSakaon(){
        String data = "P.Sakaon 72";
        Collection<String> result = PublicationMatcher.findMatches(data);
        assertTrue("No match returned, 1 expected", result.size() > 0);
        String expected = "P.Sakaon 72";
        String actual = result.iterator().next();
        actual = actual.replaceAll("\\$", "").replaceAll("\\s+", " ");
        assertEquals("e: \"" + expected + "\", a: \"" + actual + "\"",expected,actual);
    }

	
}
