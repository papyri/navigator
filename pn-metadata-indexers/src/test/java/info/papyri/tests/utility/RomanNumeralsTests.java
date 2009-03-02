package info.papyri.tests.utility;

import info.papyri.util.NumberConverter;
import junit.framework.TestCase;
import info.papyri.util.VolumeUtil;

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
    
    public void testVolumeUtil(){
        for(int i=1;i< VolumeUtil.ROMAN.length;i++){
            assertEquals("error on " + i, NumberConverter.getRoman(Integer.toString(i)),VolumeUtil.ROMAN[i]);
        }
    }

}
