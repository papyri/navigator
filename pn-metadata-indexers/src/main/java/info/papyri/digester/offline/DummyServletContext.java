package info.papyri.digester.offline;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.*;

public class DummyServletContext implements ServletContext {
    
    private HashMap<String,String> atts = new HashMap<String,String>();
    
    DummyServletContext(){
        ServletContext sc = null;
        atts.put("ddbdp-src","");
        atts.put("apis-src","");
        atts.put("ddbdp-src","");
        atts.put("ddbdp-src","");
        String DDBDP = sc.getInitParameter("ddbdp-src");
        String HGV = sc.getInitParameter("hgv-src");
        String APIS = sc.getInitParameter("apis-src");
        String XREF = sc.getInitParameter("crosswalk-src");
    }

    public Object getAttribute(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getAttributeNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public ServletContext getContext(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getInitParameter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getInitParameterNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getMajorVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getMimeType(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getMinorVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    public RequestDispatcher getNamedDispatcher(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRealPath(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public URL getResource(String arg0) throws MalformedURLException {
        // TODO Auto-generated method stub
        return null;
    }

    public InputStream getResourceAsStream(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Set getResourcePaths(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getServerInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public Servlet getServlet(String arg0) throws ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getServletContextName() {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getServletNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getServlets() {
        // TODO Auto-generated method stub
        return null;
    }

    public void log(Exception arg0, String arg1) {
        // TODO Auto-generated method stub
        
    }

    public void log(String arg0, Throwable arg1) {
        // TODO Auto-generated method stub
        
    }

    public void log(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public void removeAttribute(String arg0) {
        // TODO Auto-generated method stub
        
    }

    public void setAttribute(String arg0, Object arg1) {
        // TODO Auto-generated method stub
        
    }
    
}