package info.papyri.tests;

import info.papyri.tests.utility.DateFormatTest;
import info.papyri.tests.utility.IntQueueTest;
import info.papyri.tests.utility.JndiTest;
import info.papyri.tests.utility.RomanNumeralsTest;
import info.papyri.tests.utility.IntToByteArrayTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class UtilityTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for tests");
        //$JUnit-BEGIN$
        suite.addTestSuite(DateFormatTest.class);
        suite.addTestSuite(IntQueueTest.class);
        suite.addTestSuite(JndiTest.class);
        suite.addTestSuite(RomanNumeralsTest.class);
        suite.addTestSuite(IntToByteArrayTest.class);
        //$JUnit-END$
        return suite;
    }

}
