package util;

import info.papyri.util.JetspeedUrlRewriter;
import junit.framework.TestCase;

/**
 *
 * @author hcayless
 */
public class JetspeedUrlRewriterTest extends TestCase {
    
    public JetspeedUrlRewriterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of rewriteUrl method, of class JetspeedUrlRewriter.
     */
    public void testRewriteUrl() {
        String in = "http://localhost:80/navigator/portal/apisfull.psml?controlName=oai:papyri.info:identifiers:hgv:P.Oxy.:4:744";
        JetspeedUrlRewriter instance = new JetspeedUrlRewriter();
        String expResult = "http://localhost/navigator/full/hgv_P.Oxy._4:744";
        String result = instance.rewriteUrl(in);
        assertEquals(expResult, result);
        in = "http://localhost:80/navigator/portal/default-page.psml";
        expResult = "http://localhost/navigator/search";
        result = instance.rewriteUrl(in);
        assertEquals(expResult, result);
    }

    public void testRewriteId() {
        String in = "oai:papyri.info:identifiers:hgv:P.Oxy.:4:744";
        JetspeedUrlRewriter instance = new JetspeedUrlRewriter();
        String expResult = "hgv_P.Oxy._4:744";
        String result = instance.rewriteId(in);
        assertEquals(expResult, result);
    }

}
