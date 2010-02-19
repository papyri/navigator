import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.apache.jasper.runtime.*;
import java.net.*;
import org.mulgara.server.HttpServices;

public class index_jsp extends HttpJspBase {


  private static java.util.Vector _jspx_includes;

  public java.util.List getIncludes() {
    return _jspx_includes;
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    JspFactory _jspxFactory = null;
    javax.servlet.jsp.PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;


    try {
      _jspxFactory = JspFactory.getDefaultFactory();
      response.setContentType("text/html;charset=ISO-8859-1");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
      out.write("\n");


String hostname = null;
String currentHostUrl = null;
try {

  if (hostname == null) {
    hostname = (String)getServletContext().getAttribute(HttpServices.BOUND_HOST_NAME_KEY);
    
    // determine the requesting URL
    currentHostUrl = HttpUtils.getRequestURL(request).toString();

    // check it to see if the hostname is in it
    if (!currentHostUrl.startsWith("http://" + hostname + ":")) {
      String configuredHostUrl = currentHostUrl.replaceFirst("http://[a-zA-Z0-9.]*:", 
          "http://" + hostname + ":");

      // do redirect
      response.sendRedirect(configuredHostUrl);
    }
  }

} catch (Exception e) {
  System.out.println(e.getMessage());
}

String URL2Here = currentHostUrl.substring(0, currentHostUrl.length() - "index.jsp".length());
String descriptorModel = (String)getServletContext().getAttribute(HttpServices.SERVER_MODEL_URI_KEY) + "#descriptors";
  

      out.write("\n\n");
      out.write("<html>\n");
      out.write("<head>\n  ");
      out.write("<title>Mulgara Descriptor");
      out.write("</title>\n");
      out.write("<link media=\"all\" href=\"/all.css\" type=\"text/css\" title=\"Default\" rel=\"stylesheet\">\n");
      out.write("<link media=\"screen\" href=\"/default.css\" type=\"text/css\" title=\"Default\" rel=\"stylesheet\">\n");
      out.write("<link media=\"print\" href=\"/print.css\" type=\"text/css\" rel=\"stylesheet\">\n");
      out.write("<link href=\"/burnt.css\" title=\"Burnt\" media=\"screen\" type=\"text/css\" rel=\"alternate stylesheet\">\n");
      out.write("<link rel=\"icon\" type=\"text/png\" href=\"/images/icons/siteicon.png\">\n");
      out.write("<link rel=\"shortcut icon\" type=\"text/png\" href=\"/images/icons/siteicon.png\">\n");
      out.write("</head>\n\n");
      out.write("<body>\n");
      out.write("<div id=\"container\">\n\n  ");
      out.write("<!-- Banner -->\n  ");
      out.write("<div id=\"banner\">\n    ");
      out.write("<h1>mulgara.sourceforge.net");
      out.write("</h1>\n  ");
      out.write("</div>\n\n  ");
      out.write("<div id=\"content\">\n    ");
      out.write("<div id=\"breadcrumb\">\n      [ Location:\n      ");
      out.write("<ul>\n        ");
      out.write("<li>descriptors");
      out.write("</li>\n      ");
      out.write("</ul>  ]\n  ");
      out.write("</div>  \n  ");
      out.write("<h1>Mulgara Descriptors");
      out.write("</h1>\n  ");
      out.write("<ul>\n    ");
      out.write("<li>");
      out.write("<a href=\"#overview\">Overview");
      out.write("</a>\n    ");
      out.write("<li>");
      out.write("<a href=\"#tasks\">Tasks");
      out.write("</a>\n    ");
      out.write("<li>");
      out.write("<a href=\"doc/\">Documentation");
      out.write("</a> \n    ");
      out.write("<li>");
      out.write("<a href=\"examples/\">Examples");
      out.write("</a>\n    ");
      out.write("<li>");
      out.write("<a href=\"tutorial/\">Tutorials");
      out.write("</a>\n  ");
      out.write("</ul>  \n\n");
      out.write("<p/>\n  ");
      out.write("<a name=\"overview\"/>\n  ");
      out.write("<h1>Overview");
      out.write("</h1>");
      out.write("</a>\n  Descriptors:\n  ");
      out.write("<ul>\n    ");
      out.write("<li>Allow complex or frequently used queries to be named and invoked by a client. \n    ");
      out.write("<li>Can perform an iTQL command, including inserting data into Mulgara.\n    ");
      out.write("<li>May return XML, HTML, text or anything XSL is capable of producing.\n    ");
      out.write("<li>Output can be deserialized into java objects.\n    ");
      out.write("<li>Expose their interfaces using RDF stored on a Mulgara server.  \n    ");
      out.write("<li>Are accessible as SOAP services for 3rd party integration.\n    ");
      out.write("<li>May be changed on the fly even if interfaces change.\n    ");
      out.write("<li>Are stored somewhere accessible as a URL such as on a webserver.  \n    ");
      out.write("<li>May call other descriptors in order to perform a sub task. \n  ");
      out.write("</ul>\n  ");
      out.write("</p>\n\n  \n  ");
      out.write("<a name=\"tasks\"/>\n  ");
      out.write("<h1>Descriptor Tasks");
      out.write("</h1>");
      out.write("</a>\n");
      out.write("<p>\n");
      out.write("<p/>\n");
      out.write("<b>NOTE");
      out.write("</b> Buttons labeled 'Invoke' invoke descriptors, therefore some may not work until the descriptors are deployed, which is a task available below. e.g. The List Descriptors avilable task wil not work until the list descriptors descriptor has been deployed.\n");
      out.write("<p/>\n\n\n");
      out.write("<!-- invoke wizard -->\n");
      out.write("<p>\n");
      out.write("<form action=\"execute\">\n  ");
      out.write("<input type=\"submit\" value=\"Invoke\"/>\n  Create Descriptor using Wizard&nbsp;&nbsp; \n  ");
      out.write("<input type=\"hidden\" name=\"_self\" value=\"");
      out.print( URL2Here );
      out.write("descriptors/default/descriptorWizard.xsl\"/>\n");
      out.write("</form>\n");
      out.write("</p>\n\n");
      out.write("<hr/>\n\n");
      out.write("<!-- redeploy descriptors -->\n");
      out.write("<p>\n");
      out.write("<form action=\"deploy\">\n  ");
      out.write("<input type=\"submit\" value=\"ReDeploy\"/>\n  Redeploy bundled Descriptors - drops existing Descriptors from Mulgara, reloads built-in descriptors, clears descriptors from cache&nbsp;&nbsp; \n  ");
      out.write("<input type=\"hidden\" name=\"deployLocalDescriptors\" value=\"true\"/>\n  ");
      out.write("<input type=\"hidden\" name=\"clearLocalDescriptors\" value=\"true\"/>\n");
      out.write("</form>\n");
      out.write("</p>\n\n");
      out.write("<hr/>\n");
      out.write("<!-- deploy descriptors -->\n");
      out.write("<p>\n");
      out.write("<form action=\"deploy\">\n  ");
      out.write("<input type=\"submit\" value=\"Deploy\"/>\n  Deploy bundled Descriptors - preserves any existing Descriptors in Mulgara, reloads built-in descriptors, clears descriptors from cache&nbsp;&nbsp; \n  ");
      out.write("<input type=\"hidden\" name=\"deployLocalDescriptors\" value=\"true\"/>\n");
      out.write("</form>\n");
      out.write("</p>\n\n");
      out.write("<hr/>\n");
      out.write("<!-- list on this host -->\n\n");
      out.write("<p>\n");
      out.write("<form action=\"execute\">\n  ");
      out.write("<input type=\"submit\" value=\"Invoke\"/>\n  See List of Descriptors available on this host&nbsp;&nbsp; \n  ");
      out.write("<input type=\"hidden\" name=\"_self\" value=\"");
      out.print( URL2Here );
      out.write("descriptors/default/descriptorListHTML.xsl\"/>\n  ");
      out.write("<input type=\"hidden\" name=\"descriptorBase\" value=\"");
      out.print( URL2Here );
      out.write("descriptors/\"/>\n  ");
      out.write("<input type=\"hidden\" name=\"model\" value=\"");
      out.print( descriptorModel );
      out.write("\"/>\n");
      out.write("</form>\n");
      out.write("</p>\n\n");
      out.write("<hr/>\n\n");
      out.write("<!-- list on other host -->\n\n");
      out.write("<p>\n");
      out.write("<form action=\"execute\">\n  ");
      out.write("<input type=\"submit\" value=\"Invoke\"/>\n  See list of Descriptors available from Mulgara model&nbsp;&nbsp;\n  ");
      out.write("<input type=\"hidden\" name=\"_self\" value=\"");
      out.print( URL2Here );
      out.write("descriptors/default/descriptorListHTML.xsl\"/>\n  ");
      out.write("<input type=\"hidden\" name=\"descriptorBase\" value=\"");
      out.print( URL2Here );
      out.write("descriptors/\"/>\n  ");
      out.write("<input type=\"text\" name=\"model\" size=\"40\"/>\n");
      out.write("</form>\n");
      out.write("</p>\n\n");
      out.write("<hr/>\n\n");
      out.write("<!-- purge from other host -->\n\n");
      out.write("<p>\n");
      out.write("<form action=\"execute\">\n  ");
      out.write("<input type=\"submit\" value=\"Invoke\"/>\nPurge all cached Descriptors available on this host, list of descriptors available will be shown.\n  ");
      out.write("<input type=\"hidden\" name=\"_self\" value=\"");
      out.print( URL2Here );
      out.write("descriptors/default/descriptorListHTML.xsl\"/>\n  ");
      out.write("<input type=\"hidden\" name=\"descriptorBase\" value=\"");
      out.print( URL2Here );
      out.write("descriptors/\"/>\n  ");
      out.write("<input type=\"hidden\" name=\"_clearCache\" value=\"true\"/>\n  ");
      out.write("<input type=\"hidden\" name=\"model\" value=\"");
      out.print( descriptorModel );
      out.write("\"/>\n");
      out.write("</form>\n");
      out.write("</p>\n\n");
      out.write("<hr/>\n\n");
      out.write("<!-- purge from other host -->\n\n");
      out.write("<p>\n");
      out.write("<form action=\"execute\">\n  ");
      out.write("<input type=\"submit\" value=\"Invoke\"/>\nPurge Descriptors from this Mulgara model, list of descriptors available will be shown.&nbsp;&nbsp;\n  ");
      out.write("<input type=\"hidden\" name=\"_self\" value=\"");
      out.print( URL2Here );
      out.write("descriptors/default/descriptorListHTML.xsl\"/>\n  ");
      out.write("<input type=\"hidden\" name=\"descriptorBase\" value=\"");
      out.print( URL2Here );
      out.write("descriptors/\"/>\n  ");
      out.write("<input type=\"hidden\" name=\"_clearCache\" value=\"true\"/>\n  ");
      out.write("<input type=\"text\" name=\"model\" size=\"40\"/>\n");
      out.write("</form>\n");
      out.write("</p>\n\n\n");
      out.write("</div>");
      out.write("</div>\n");
      out.write("</body>\n");
      out.write("</html>\n");
    } catch (Throwable t) {
      out = _jspx_out;
      if (out != null && out.getBufferSize() != 0)
        out.clearBuffer();
      if (pageContext != null) pageContext.handlePageException(t);
    } finally {
      if (_jspxFactory != null) _jspxFactory.releasePageContext(pageContext);
    }
  }
}
