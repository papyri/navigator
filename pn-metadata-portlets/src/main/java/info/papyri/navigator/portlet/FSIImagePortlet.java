package info.papyri.navigator.portlet;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletRequestDispatcher;

import javax.servlet.RequestDispatcher;

import org.apache.lucene.document.Document;

import info.papyri.metadata.*;

public class FSIImagePortlet extends NavigatorPortlet {
   private static final String NOT_HOSTED = "NOTHOSTED";
   public static final String CN_ATTR = "info.papyri.navigator.imageCN";
    @Override
    protected void renderView(RenderRequest arg0, RenderResponse arg1) throws PortletException, IOException {
        String hosted = arg0.getPreferences().getValue("hostedImages","");
        String controlName = arg0.getParameter("controlName");
        boolean cuHosted = false;
        boolean apis = (controlName.startsWith(NamespacePrefixes.APIS));
        if(!apis){
            Document doc = XREFPortlet.getDocumentByControlName(controlName);
            String [] xrefs = (doc != null)?doc.getValues(CoreMetadataFields.XREFS):new String[0];
            if(xrefs != null && xrefs.length != 0){
                for(String xref:xrefs){
                    if(!xref.startsWith(NamespacePrefixes.APIS)) continue;
                    String apisColl = XREFPortlet.getAPISCollection(xref);
                    if(hosted.indexOf(apisColl)!=-1){
                        controlName = xref;
                        cuHosted = true;
                        break;
                    }
                }
            }
        }
        else{
            String apisColl = (apis)?XREFPortlet.getAPISCollection(controlName):NOT_HOSTED;
            if(apisColl == null) apisColl = NOT_HOSTED;
            cuHosted = (apis && hosted.indexOf(apisColl)!=-1);

        }
        arg0.setAttribute(CN_ATTR, controlName);
        String page = (cuHosted)?"/WEB-INF/fsi.jsp":"/WEB-INF/imgLinks.jsp";
        PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(page);
        rd.include(arg0, arg1);
    }
    
}
