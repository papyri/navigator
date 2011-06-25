package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.DocumentBrowseRecord;
import info.papyri.dispatch.browse.SolrField;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

/**
 * Enables facetted browsing of pn collections
 * 
 * General architectural notes: what needs to happen
 * 
 * Every time server is hit:
 * (0) Form needs to be displayed to user
 * (1) Incoming query needs to be parsed
 *      (a) if no restrictions, display message about restrictions
 *      (b) if restrictions, retrieve all records matching
 * (2) Records matching query (if any) retrieved and displayed
 * (3) Facet selectors need to be populated with facet values matching query
 * (4) Restrictions in place need to be displayed to user
 * 
 * Steps 2 and 3 can be done with a single query - records are results returned
 * values are the facet values with numbers 
 * 
 * General architecture:
 * 
 * The core of the thing will be the FacetController class, mediating between
 * the Facet and FacetWidget classes (M and V respectively)
 * 
 * The servlet has a List of Facet.Classes it needs to instantiate. At startup it
 * instantiates FacetControllers, passing each this class
 *  
 * 
 * 
 * @author thill
 */
@WebServlet(name = "FacetBrowser", urlPatterns = {"/FacetBrowser"})
public class FacetBrowser extends HttpServlet {
    
    static String SOLR_URL = "http://localhost:8082/solr/";
    static String PN_SEARCH = "pn-search/";
    
    public enum FacetMapping{
        
        IMG(new HasImagesFacet()),
        TRANS(new HasTranslationFacet()),
        LANG(new LanguageFacet());
        
        private Facet facet;

        FacetMapping(Facet f){
            
            this.facet = f;
            
        }
        
        public Facet facet(){ return facet; }
        
        
    }
    
    ArrayList<FacetMapping> facets = new ArrayList<FacetMapping>(Arrays.asList(FacetMapping.IMG, FacetMapping.TRANS, FacetMapping.LANG));

    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet FacetBrowser</title>");  
            out.println("</head>");
            out.println("<body>");
            parseRequestToFacets(request);
            out.println("<h1>Servlet FacetBrowser at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
             
        } finally {            
            out.close();
        }
    }

    private void parseRequestToFacets(HttpServletRequest request){
        
        Map<String, String[]> params = request.getParameterMap();
        
        Iterator<FacetMapping> fmit = facets.iterator();
        
        while(fmit.hasNext()){
            
            FacetMapping facetMapping = fmit.next();
            
            if(params.containsKey(facetMapping.name())){
                
                String[] paramValues = params.get(facetMapping.name());
                Facet facet = facetMapping.facet();
                
                for(int i = 0; i < paramValues.length; i++){
                    
                    facet.addConstraint(paramValues[i]);
                    
                }
                
            }
            
        }
        
        // might need to add other functionality here
        
        
    }
    
    private QueryResponse runFacetQuery() throws MalformedURLException, SolrServerException{
        
        SolrServer solrServer = new CommonsHttpSolrServer(SOLR_URL + PN_SEARCH);
        SolrQuery sq = new SolrQuery();
        
        // iterate through facets, adding to solr query
        
        sq.setQuery("*:*");
        
        QueryResponse queryResponse = solrServer.query(sq);
        
        return queryResponse;
        
        
    }
    
    private void populateFacets(QueryResponse queryResponse){
        
        
        
    }
    
    private ArrayList<DocumentBrowseRecord> retrieveRecords(QueryResponse queryResponse){
        
        ArrayList<DocumentBrowseRecord> records = new ArrayList<DocumentBrowseRecord>();
        
        for(SolrDocument doc : queryResponse.getResults()){
            
            
            // parse results into records
            
            
        }
        
        return records;
        
    }
    
    private String assembleHTML(){
        
        StringBuffer html = new StringBuffer();
        
        
        return html.toString();
        
    }
    
    
    
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
