package info.papyri.tests;

import junit.framework.TestSuite;
import junit.framework.Test;

public class IntegrityTests extends TestSuite {
    public static Test suite(){
        IntegrityTests suite = new IntegrityTests();
        suite.addTestSuite(AnalyzerTest.class);
        suite.addTestSuite(BetaToGreekLineSpanTest.class);
        suite.addTestSuite(GreekFullTermTest.class);
        suite.addTestSuite(GreekSubstringTermTest.class);
        suite.addTestSuite(HighlighterTest.class);
        return suite;
    }
}
