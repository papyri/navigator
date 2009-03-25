package info.papyri.navigator.portlet;

import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.NamespacePrefixes;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.lucene.document.Document;

public class TranslationPortlet extends NavigatorPortlet {
    public static final String TITLE_ATTR = "info.papyri.translation.title";
    public static final String FIELD_ATTR = "info.papyri.translation.field";
    void renderView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        PortletPreferences prefs = request.getPreferences();
        String lookup;
        String translationField;
        String langSuffix = " (English)";
        if(prefs == null){
            lookup = "apis";
            translationField = CoreMetadataFields.TRANSLATION_EN;
        }
        else{
            lookup = prefs.getValue("lookup", "apis");
            String lang = prefs.getValue("lang", "en");
            if(lang.equals("de")){
                translationField = CoreMetadataFields.TRANSLATION_DE;
                langSuffix = " (German)";
            }
            else if(lang.equals("fr")){
                translationField = CoreMetadataFields.TRANSLATION_FR;
                langSuffix = " (French)";
            } else{
                translationField = CoreMetadataFields.TRANSLATION_EN;
            }
        }
        request.setAttribute(FIELD_ATTR, translationField);
        String id = request.getParameter("controlName");
        if(id == null){
            PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/translation.jsp");
            rd.include(request, response);
            return;
        }
        id = new String(id.getBytes("ISO-8859-1"),"UTF-8");
        if(id.startsWith(NamespacePrefixes.ID_NS)){

            Hashtable env = new Hashtable();
            env.put(Context.URL_PKG_PREFIXES, "info.papyri");
            try{
                Context c = NamingManager.getURLContext("jndi", env);
                Name xName = XREFPortlet.getName(id);
                c = (Context)c.lookup(xName);
                if (c == null){
                    PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/translation.jsp");
                    rd.include(request, response);
                    return;
                }
                id = (String)c.lookup(lookup);
                if (id != null){
                    Document doc = getDocumentByControlName(id);
                    request.setAttribute(NavigatorPortlet.DOC_ATTR, doc);
                    if(!"apis".equals(lookup)){
                        request.setAttribute(TITLE_ATTR, "HGV Translation" + langSuffix);
                    }else request.setAttribute(TITLE_ATTR, "APIS Translation" + langSuffix);
                }
            }
            catch(NamingException ne){
                ne.printStackTrace();
                PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/translation.jsp");
                rd.include(request, response);
                return;
            }

        }
        PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/translation.jsp");
        rd.include(request, response);
    }
}
