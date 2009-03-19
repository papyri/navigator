package info.papyri.portlet;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.portlet.*;

public class IFPortlet extends GenericPortlet {
	final static String APIS_ID = "cu001";

	final static String APIS_INV = "cu090";

	final static String APIS_PUB = "cu510_dd";

	final static String APIS_TITLE = "cu245ab";

	final static String APIS_SUMMARY = "cu520";

	final static String APIS_LANG = "cu546";

	final static String APIS_DESC = "cu300";

	final static String APIS_NOTES = "cu500";

	final static String APIS_CUST = "cu561";

	final static String APIS_TRANS = "cu500_t";

	public static final String IFDATA_ATTR = "APIS_IF";

	public void doView(javax.portlet.RenderRequest request,
			javax.portlet.RenderResponse response)
			throws javax.portlet.PortletException, java.io.IOException {
		String apisId = request.getParameter("controlName").trim();
		if (apisId == null) {
            apisId = "columbia.apis.p53";
		}
		if ((request.getAttribute(IFDATA_ATTR) == null)) {
			Map<String, String> apisFields = new HashMap<String, String>();
			try {
				URL apisUrl = new URL(
						"http://www.columbia.edu/cu/libraries/inside/projects/apis/columbia/intake_files/"
								+ apisId + ".if");
				InputStream is = apisUrl.openStream();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				String line = null;
				while ((line = br.readLine()) != null) {
					String[] subs = line.split("\\s\\|\\s");
					if (subs.length < 3)
						continue;
					String val = apisFields.get(subs[0]);
					if (val == null)
						val = "";
					val += subs[2];
					apisFields.put(subs[0], val);
				}
				String trans = (String) apisFields.get(APIS_TRANS);
				if (trans != null) {
					trans = trans.replace("<mmfclob>/www/data",
							"http://www.columbia.edu");
					trans = trans.substring(0, trans.length()
							- "</mmfclob>".length());
					request.setAttribute(APIS_TRANS, trans);
					/*    			URL transURL = new URL(trans);
					 InputStream transIS = transURL.openStream();
					 StringBuffer sb = new StringBuffer(256);
					 byte [] buf = new byte[256];
					 int read = 0;
					 while ((read = transIS.read(buf))!=-1){
					 sb.append(new String(buf,0,read));
					 }
					 apisFields.put(APIS_TRANS,sb.toString());
					 */
				}

			} catch (IOException e) {

			}
			request.setAttribute(IFDATA_ATTR, apisFields);
		}
		response.createRenderURL().setParameter("apisId",apisId);
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(
				"/WEB-INF/if.jsp");
		rd.include(request, response);
	}
}