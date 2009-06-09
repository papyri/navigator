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
        in = "http://localhost/navigator/portal/_ns:YWFwaXMtZGF0YS1hcGlzfGQx/apisfull.psml?controlName=oai:papyri.info:identifiers:apis:toronto:17";
        expResult = "http://localhost/navigator/full/apis_toronto_17/_ns:YWFwaXMtZGF0YS1hcGlzfGQx";
        result = instance.rewriteUrl(in);
        assertEquals(expResult, result);
        in = "portal/numbers.psml?prefix=oai:papyri.info:identifiers:apis:berkeley:1038&apisPrefix=berkeley";
        expResult = "/navigator/numbers/apis_berkeley_1038?apisPrefix=berkeley";
        result = instance.rewriteUrl(in);
        assertEquals(expResult, result);
    }

    public void testRewriteId() {
        String in = "oai:papyri.info:identifiers:hgv:P.Oxy.:4:744";
        JetspeedUrlRewriter instance = new JetspeedUrlRewriter();
        String expResult = "hgv_P.Oxy._4:744";
        String result = instance.rewriteId(in);
        assertEquals(expResult, result);
        in = "oai:papyri.info:identifiers:trismegistos:69865";
        result = instance.rewriteId(in);
        expResult = "trismegistos_69865";
        assertEquals(expResult, result);
    }

    public void testGetStaticDir() {
        String filename = "o.deiss.30a";
        String id = "ddbdp_0021_:30a";
        JetspeedUrlRewriter instance = new JetspeedUrlRewriter();
        String expResult = "/o.deiss/o.deiss.30a";
        String result = instance.getStaticDir(filename, id);
        assertEquals(expResult, result);
        filename = "o.bodl.2.2304";
        id = "ddbdp_0014_2:2304";
        expResult = "/o.bodl/o.bodl.2/o.bodl.2.2304";
        result = instance.getStaticDir(filename, id);
        assertEquals(expResult, result);
        filename = "p.gen.2.1.1";
        id = "ddbdp_0266_1:1";
        expResult = "/p.gen.2/p.gen.2.1/p.gen.2.1.1";
        result = instance.getStaticDir(filename, id);
        assertEquals(expResult, result);
        filename = "p.fuad.i.univ.AppII-139";
        id = "ddbdp_0116_:AppII,139";
        expResult = "/p.fuad.i.univ/p.fuad.i.univ.AppII-139";
        result = instance.getStaticDir(filename, id);
        assertEquals(expResult, result);
    }

}
