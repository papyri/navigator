package tests;

import util.NumberConverter;
import junit.framework.TestCase;

public class RomanNumeralsTests extends TestCase {
    public void testDecimalStringtoRoman(){
        String roman = NumberConverter.getRoman("1998");
        assertEquals("Bad Roman numeral parse","MCMXCVIII",roman);
        assertEquals("Bad roman numeral unparse",1998,NumberConverter.getInt(roman));
        roman = NumberConverter.getRoman("2751");
        assertEquals("Bad Roman numeral parse","MMDCCLI",roman);
        assertEquals("Bad roman numeral unparse",2751,NumberConverter.getInt(roman));
        roman = NumberConverter.getRoman("14");
        assertEquals("Bad Roman numeral parse","XIV",roman);
        assertEquals("Bad roman numeral unparse",14,NumberConverter.getInt(roman));
    }

}
