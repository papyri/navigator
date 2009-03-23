package info.papyri.ddbdp.portlet;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

public class UIPortlet extends GenericPortlet {

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/portlet-ui.jsp");
        rd.include(request,response);
        response.flushBuffer();
        return;
    }

}
