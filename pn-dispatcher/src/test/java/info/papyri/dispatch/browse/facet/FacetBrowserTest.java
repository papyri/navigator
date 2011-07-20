package info.papyri.dispatch.browse.facet;

import org.apache.solr.client.solrj.SolrQuery;
import java.util.HashMap;
import java.util.EnumMap;
import javax.servlet.http.HttpServletRequest;
import junit.framework.TestCase;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 *
 * @author thill
 */
public class FacetBrowserTest extends TestCase {
    
    static FacetBrowser facetBrowser;
    static HttpServletRequest mockRequest;
    EnumMap<FacetParam, Facet> paramsToFacets;
    
    public FacetBrowserTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        facetBrowser = new FacetBrowser();
        mockRequest = createStrictMock(HttpServletRequest.class);

        
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testParseRequestToFacets(){
        
        HashMap<String, String[]> paramMap = new HashMap<String, String[]>();
        paramMap.put("TRANSL", new String[]{"true"});
        paramMap.put("LANG", new String[]{"grc"});
        expect(mockRequest.getParameterMap()).andReturn(paramMap);

    }
    
    
}
