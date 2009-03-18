package info.papyri.tests;

import info.papyri.tests.metadata.APISDataTest;
import info.papyri.tests.metadata.EpiDocMetadataParserTest;
import info.papyri.tests.metadata.LuceneIndexTest;
import info.papyri.tests.metadata.NumberServerTest;
import info.papyri.tests.metadata.ProvenanceTest;
import info.papyri.tests.utility.DateFormatTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class MetadataTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for tests");
        //$JUnit-BEGIN$
        suite.addTestSuite(APISDataTest.class);
        suite.addTestSuite(EpiDocMetadataParserTest.class);
        suite.addTestSuite(LuceneIndexTest.class);
        suite.addTestSuite(NumberServerTest.class);
        suite.addTestSuite(ProvenanceTest.class);
        //$JUnit-END$
        return suite;
    }

}
