package info.papyri.dispatch.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import junit.framework.TestCase;

/**
 *
 * @author thill
 */
public class DocumentBrowseRecordComparatorTest extends TestCase {
    
    public DocumentBrowseRecordComparatorTest(String testName) {
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
        
        System.out.println("compare");
     
        ArrayList<String> sorted1 = new ArrayList<String>(Arrays.asList("99", "100", "101", "102", "1000", "10001"));
        ArrayList<String> unsorted1 = new ArrayList<String>(Arrays.asList("10001", "99", "102", "100", "1000", "101"));
        
        Collections.sort(unsorted1, new DocumentBrowseRecordComparator());
        
        ArrayList<String> sorted2 = new ArrayList<String>(Arrays.asList("10C", "99", "100", "100A", "100B", "C100", "hello"));
        ArrayList<String> unsorted2 = new ArrayList<String>(Arrays.asList("100B", "10C", "99", "100A", "hello", "C100", "100"));

        Collections.sort(unsorted2, new DocumentBrowseRecordComparator());
        
        ArrayList<String> sorted3 = new ArrayList<String>(Arrays.asList("Appendix", "Bibliography", "Collection", "Document", "Epigraph"));
        ArrayList<String> unsorted3 = new ArrayList<String>(Arrays.asList("Bibliography","Epigraph", "Appendix", "Document", "Collection"));
        
        Collections.sort(unsorted3, new DocumentBrowseRecordComparator());
        
        ArrayList<String> sorted4 = new ArrayList<String>(Arrays.asList("28", "28bis", "29", "29bis", "30", "AppVindol"));
        ArrayList<String> unsorted4 = new ArrayList<String>(Arrays.asList("30", "29bis", "28bis", "29", "28", "AppVindol"));
        
        Collections.sort(unsorted4, new DocumentBrowseRecordComparator());
        
        assertEquals(sorted1, unsorted1);
        assertEquals(sorted2, unsorted2);
        assertEquals(sorted3, unsorted3);
        assertEquals(sorted4, unsorted4);
        
    }

    /**
     * Test of splitIntoNumericAndAlphabeticComponents method, of class DocumentBrowseRecordComparator.
     */
    public void testSplitIntoNumericAndAlphabeticComponents() {
        
        DocumentBrowseRecordComparator testInstance = new DocumentBrowseRecordComparator();
        
        System.out.println("splitIntoNumericAndAlphabeticComponents");

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
        
        assertEquals(testArray1, testInstance.splitIntoNumericAndAlphabeticComponents(testString1));
        assertEquals(testArray2, testInstance.splitIntoNumericAndAlphabeticComponents(testString2));
        assertEquals(testArray3, testInstance.splitIntoNumericAndAlphabeticComponents(testString3));
        assertEquals(testArray4, testInstance.splitIntoNumericAndAlphabeticComponents(testString4));
        assertEquals(testArray5, testInstance.splitIntoNumericAndAlphabeticComponents(testString5));
        assertEquals(testArray6, testInstance.splitIntoNumericAndAlphabeticComponents(testString6));

    }
}
