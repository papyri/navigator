package info.papyri.tests;

import info.papyri.tests.metadata.APISDataTest;
import info.papyri.tests.metadata.EpiDocMetadataParserTests;
import info.papyri.tests.metadata.LuceneIndexTests;
import info.papyri.tests.metadata.NumberServerTest;
import info.papyri.tests.metadata.ProvenanceTests;
import info.papyri.tests.utility.DateFormatTests;
import junit.framework.Test;
import junit.framework.TestSuite;

public class MetadataTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for tests");
        //$JUnit-BEGIN$
        suite.addTestSuite(APISDataTest.class);
        suite.addTestSuite(EpiDocMetadataParserTests.class);
        suite.addTestSuite(LuceneIndexTests.class);
        suite.addTestSuite(NumberServerTest.class);
        suite.addTestSuite(ProvenanceTests.class);
        //$JUnit-END$
        return suite;
    }

}
