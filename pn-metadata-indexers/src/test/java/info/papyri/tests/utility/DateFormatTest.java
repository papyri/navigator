package info.papyri.tests.utility;

import info.papyri.metadata.OutOfRangeException;
import info.papyri.util.NumberConverter;
import junit.framework.TestCase;
import java.util.*;
import java.text.*;

import info.papyri.data.*;

public class DateFormatTest extends TestCase {
    String YEAR3 = "yyy";
    String YEAR4 = "yyyy";
    String MONTH = "MMM";
    String DAY = "d";     
    
    public void testDateIndexNegativeToNegative() throws OutOfRangeException {
        String first = NumberConverter.encode(-350);
        String second = NumberConverter.encode(-200);
        assertTrue(second + " (second) not greater than " + first + " (first)",second.compareTo(first) > 0);
    }
    public void testDateIndexPositiveToNegative() throws OutOfRangeException {
        String first = NumberConverter.encode(-350);
        String second = NumberConverter.encode(150);
        assertTrue(second + " (second) not greater than " + first + " (first)",second.compareTo(first) > 0);
        
    }
    public void testDateIndexPositiveToPositive() throws OutOfRangeException {
        String first = NumberConverter.encode(35);
        String second = NumberConverter.encode(120);
        assertTrue(second + " (second) not greater than " + first + " (first)",second.compareTo(first) > 0);
    }
    
    public void testDQ() throws OutOfRangeException {
        String s306 = NumberConverter.encodeDate(306,0,0);
        String s320 = NumberConverter.encodeDate(320,0,0);
        String s320d = NumberConverter.encodeDate(320, 12, 31);

        boolean a = s306.compareTo(s320) < 0;
        boolean b = s306.compareTo(s320d) < 0;
        boolean c = s320.compareTo(s320d) < 0;
        assertTrue(s306 + " not less than " + s320,a);
        assertTrue(s306 + " not less than " + s320d,b);
        assertTrue(s320 + " not less than " + s320d,c);
    }
    
}
