package info.papyri.tests.utility;

import info.papyri.util.IntUtil;
import junit.framework.TestCase;

public class IntToByteArrayTest extends TestCase {
    public void testIntToByteArray(){
        int i = 50621;
        byte [] b = IntUtil.toBytes(i);
        int t = IntUtil.fromBytes(b);
        assertEquals(i,t);
        i = -1;
        b = IntUtil.toBytes(i);
        t = IntUtil.fromBytes(b);
        assertEquals(i,t);
    }
}
