package tests;

import junit.framework.TestCase;
import edu.unc.epidoc.transcoder.*;
import info.papyri.ddbdp.parser.*;
public class TranscoderTests extends TestCase {
    static final BetaCodeParser delegate = new BetaCodeParser();
    static final UnicodeCConverter converter = new UnicodeCConverter();
    public void testDelegateParsing() throws Exception {
        DelegatingBetaCodeParser parser = new DelegatingBetaCodeParser(delegate);
        String beta = "*(/GILS)";
        BetaCodeParser control = new BetaCodeParser();
        control.setString(beta);
        parser.setString(beta);
        while(control.hasNext()){
            String cNext= control.next();
            String pNext = parser.next();
            assertEquals(cNext,pNext);
        }
        assertTrue(!parser.hasNext());
        String expected = converter.convertToString(control);
        String actual = converter.convertToString(parser);
        assertEquals(expected,actual);
    }
}
