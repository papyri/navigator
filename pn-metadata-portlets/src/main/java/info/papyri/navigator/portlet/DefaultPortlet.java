package info.papyri.navigator.portlet;

import java.io.IOException;

import javax.portlet.*;

public class DefaultPortlet extends GenericPortlet {
    protected void doView(RenderRequest arg0, RenderResponse arg1) throws PortletException, IOException {
        // TODO Auto-generated method stub
        PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/index.jsp");
        rd.include(arg0, arg1);
    }

}