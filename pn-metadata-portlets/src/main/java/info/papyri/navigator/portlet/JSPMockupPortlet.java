package info.papyri.navigator.portlet;

import info.papyri.index.DBUtils;
import info.papyri.index.LuceneIndex;

import java.io.*;
import java.util.*;

import javax.portlet.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;


public class JSPMockupPortlet extends GenericPortlet {
   
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        PortletPreferences prefs = request.getPreferences();
        if (prefs == null){
            PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/unspecified.jsp");
            rd.include(request, response);
            return;
        }
        debug(request);
        String jsp = request.getPreferences().getValue("jsp", "unspecified.jsp");
        request.setAttribute("resultMockup", jsp);
        PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/" + jsp);
        rd.include(request, response);
    }
    
    private static void debug(RenderRequest req){
        Enumeration en = req.getAttributeNames();
        while(en.hasMoreElements()){
            String name = en.nextElement().toString();
            System.out.println(name + ": " + req.getAttribute(name));
        }
    }
    
  
}