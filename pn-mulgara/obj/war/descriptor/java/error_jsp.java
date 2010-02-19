import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.apache.jasper.runtime.*;

public class error_jsp extends HttpJspBase {


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

      out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n\n");
      out.write("<html>\n");
      out.write("<head>\n  ");
      out.write("<title>Mulgara Descriptor Error");
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
      out.write("<li>descriptor error");
      out.write("</li>\n      ");
      out.write("</ul>  ]\n    ");
      out.write("</div>  \n    ");
      out.write("<h1>Mulgara Descriptor Error");
      out.write("</h1>\n  ");
      out.write("</div>\n");
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
