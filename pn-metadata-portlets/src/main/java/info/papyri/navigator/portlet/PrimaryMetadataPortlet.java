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


public class PrimaryMetadataPortlet extends NavigatorPortlet {
   
    void renderView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        PortletPreferences prefs = request.getPreferences();
        if (prefs == null){
            PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/primary.jsp");
            rd.include(request, response);
            return;
        }
        
        String jsp = prefs.getValue("apisDisplay", "primary.jsp");
        PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/" + jsp);
        rd.include(request, response);
    }
    
  
}