package info.papyri.tests;

import info.papyri.tests.utility.DateFormatTests;
import info.papyri.tests.utility.IntQueueTests;
import info.papyri.tests.utility.JndiTests;
import info.papyri.tests.utility.RomanNumeralsTests;
import info.papyri.tests.utility.TestIntToByteArray;
import junit.framework.Test;
import junit.framework.TestSuite;

public class UtilityTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for tests");
        //$JUnit-BEGIN$
        suite.addTestSuite(DateFormatTests.class);
        suite.addTestSuite(IntQueueTests.class);
        suite.addTestSuite(JndiTests.class);
        suite.addTestSuite(RomanNumeralsTests.class);
        suite.addTestSuite(TestIntToByteArray.class);
        //$JUnit-END$
        return suite;
    }

}
