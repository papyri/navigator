package info.papyri.tests.perseus;

import info.papyri.antlr.BetaChars;
import info.papyri.antlr.BetaCodeParser;
import junit.framework.TestCase;
import info.papyri.*;
public class BCPTests extends TestCase {
    BetaCodeParser bcp = new BetaCodeParser();
    public void testCombinations(){
        if (true) return;
        String in = "*XARO/PPAS *ZH/NWNI";
        char [] exp = new char[]{BetaChars.X_UPPER,BetaChars.A_LOWER,BetaChars.R_LOWER,
                '\u1F79',
                BetaChars.P_LOWER,BetaChars.P_LOWER,BetaChars.A_LOWER,BetaChars.S2,' ',
                BetaChars.Z_UPPER,'\u1F75',
                BetaChars.N_LOWER,BetaChars.W_LOWER,BetaChars.N_LOWER,BetaChars.I_LOWER
                };
        String actual = bcp.parseToString(in);
        String expected = new String(exp);
        String message = "expected = \"" + expected + "\" length "  + expected.length() + ", actual = \"" + actual + "\" length "  + actual.length();
        TestCase.assertEquals(message,expected, actual);
    }
    public void testCombinationsOfMultipleDiacritics(){
        String in = "*(ILARI/WN *)/ALITI A)PO/DOS";
        char [] exp = new char[]{
                '\u1F39', BetaChars.L_LOWER, BetaChars.A_LOWER,
                BetaChars.R_LOWER,'\u1F77',BetaChars.W_LOWER, BetaChars.N_LOWER,
                ' ',
                '\u1F0C',BetaChars.L_LOWER, BetaChars.I_LOWER,BetaChars.T_LOWER,BetaChars.I_LOWER,
                ' ',
                '\u1F00',BetaChars.P_LOWER,'\u1F79',BetaChars.D_LOWER,BetaChars.O_LOWER,BetaChars.S2
                };
        String actual = bcp.parseToString(in);
        String expected = new String(exp);
        String message = "expected = \"" + expected + "\" length "  + expected.length() + ", actual = \"" + actual + "\" length "  + actual.length();
        TestCase.assertEquals(message,expected, actual);
    }

    public void testCombinationsWithSubscriptDot(){
        String in = "*TRAIA?N?H=?S?";
        char [] exp = new char[]{
                BetaChars.T_UPPER, BetaChars.R_LOWER,BetaChars.A_LOWER,
                BetaChars.I_LOWER, BetaChars.A_LOWER, '\u0323',
                BetaChars.N_LOWER,'\u0323','\u1FC6',
                '\u0323',BetaChars.S2,'\u0323'
                };
        String actual = bcp.parseToString(in);
        String expected = new String(exp);
        String message = "expected = \"" + expected + "\" length "  + expected.length() + ", actual = \"" + actual + "\" length "  + actual.length();
        TestCase.assertEquals(message,expected, actual);
    }
    public void testCombinationsWithUpsilon(){
        String in = "NU=N";
        char [] exp = new char[]{
                BetaChars.N_LOWER, '\u1FE6',BetaChars.N_LOWER,
                };
        String actual = bcp.parseToString(in);
        String expected = new String(exp);
        String message = "expected = \"" + expected + "\" length "  + expected.length() + ", actual = \"" + actual + "\" length "  + actual.length();
        TestCase.assertEquals(message,expected, actual);
    }
}
