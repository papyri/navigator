package info.papyri.dispatch.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import junit.framework.TestCase;

/**
 *
 * @author thill
 */
public class IdComparatorTest extends TestCase {
    
    public IdComparatorTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of compare method, of class DocumentBrowseRecordComparator.
     */
    public void testCompare() {
             
        IdComparator testInstance = new IdComparator();

        ArrayList<String> sorted1 = new ArrayList<String>(Arrays.asList("99", "100", "101", "102", "1000", "10001"));
        ArrayList<String> unsorted1 = new ArrayList<String>(Arrays.asList("10001", "99", "102", "100", "1000", "101"));
        
        Collections.sort(unsorted1, testInstance);
        
        ArrayList<String> sorted2 = new ArrayList<String>(Arrays.asList("10C", "99", "100", "100A", "100B", "C100", "hello"));
        ArrayList<String> unsorted2 = new ArrayList<String>(Arrays.asList("100B", "10C", "99", "100A", "hello", "C100", "100"));

        Collections.sort(unsorted2, testInstance);
        
        ArrayList<String> sorted3 = new ArrayList<String>(Arrays.asList("Appendix", "Bibliography", "Collection", "Document", "Epigraph"));
        ArrayList<String> unsorted3 = new ArrayList<String>(Arrays.asList("Bibliography","Epigraph", "Appendix", "Document", "Collection"));
        
        Collections.sort(unsorted3, testInstance);
        
        ArrayList<String> sorted4 = new ArrayList<String>(Arrays.asList("28", "28bis", "29", "29bis", "30", "AppVindol"));
        ArrayList<String> unsorted4 = new ArrayList<String>(Arrays.asList("30", "29bis", "28bis", "29", "28", "AppVindol"));
        
        Collections.sort(unsorted4, testInstance);
        
        ArrayList<String> sorted5 = new ArrayList<String>(Arrays.asList("bgu 2 392", "bgu 2 574", "o. bodl 2 478"));
        ArrayList<String> unsorted5 = new ArrayList<String>(Arrays.asList("o. bodl 2 478", "bgu 2 574",  "bgu 2 392"));
        
        Collections.sort(unsorted5, testInstance);
        
        assertEquals(sorted1, unsorted1);
        assertEquals(sorted2, unsorted2);
        assertEquals(sorted3, unsorted3);
        assertEquals(sorted4, unsorted4);
        assertEquals(sorted5, unsorted5);
        
    }

    /**
     * Test of splitIntoNumericAndAlphabeticComponents method, of class DocumentBrowseRecordComparator.
     */
    public void testSplitIntoNumericAndAlphabeticComponents() {
        
        IdComparator testInstance = new IdComparator();
        
        String testString1 = "12345";
        ArrayList<String> testArray1 = new ArrayList<String>(Arrays.asList("12345"));
        
        String testString2 = "1A345";
        ArrayList<String> testArray2 = new ArrayList<String>(Arrays.asList("1", "A", "345"));
        
        String testString3 = "1A3B5";
        ArrayList<String> testArray3 = new ArrayList<String>(Arrays.asList("1", "A", "3", "B", "5"));
        
        String testString4 = "12ZZ5";
        ArrayList<String> testArray4 = new ArrayList<String>(Arrays.asList("12", "ZZ", "5"));
        
        String testString5 = "AllAlpha";
        ArrayList<String> testArray5 = new ArrayList<String>(Arrays.asList("AllAlpha"));
        
        String testString6 = "A2B3C";
        ArrayList<String> testArray6 = new ArrayList<String>(Arrays.asList("A", "2", "B", "3", "C"));
        
        String testString7 = "Pro. Teb. 36A";
        ArrayList<String> testArray7 = new ArrayList<String>(Arrays.asList("Pro. Teb. ", "36", "A"));
        
        String testString8 = "C.Ep.Lat.AppVindol";
        ArrayList<String> testArray8 = new ArrayList<String>(Arrays.asList("C.Ep.Lat.AppVindol"));
        
        
        assertEquals(testArray1, testInstance.splitIntoNumericAndAlphabeticComponents(testString1));
        assertEquals(testArray2, testInstance.splitIntoNumericAndAlphabeticComponents(testString2));
        assertEquals(testArray3, testInstance.splitIntoNumericAndAlphabeticComponents(testString3));
        assertEquals(testArray4, testInstance.splitIntoNumericAndAlphabeticComponents(testString4));
        assertEquals(testArray5, testInstance.splitIntoNumericAndAlphabeticComponents(testString5));
        assertEquals(testArray6, testInstance.splitIntoNumericAndAlphabeticComponents(testString6));
        assertEquals(testArray7, testInstance.splitIntoNumericAndAlphabeticComponents(testString7));
        assertEquals(testArray8, testInstance.splitIntoNumericAndAlphabeticComponents(testString8));

    }
}
