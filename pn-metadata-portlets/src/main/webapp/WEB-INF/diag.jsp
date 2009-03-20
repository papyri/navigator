<%@page import="edu.columbia.apis.*,java.util.*" pageEncoding="UTF-8" %>
<html>
<head><title>Diagnostics</title></head>
<body>
<h2 class="apis-portal-title">Diagnostics</h2>
<table>
<caption>Library Version Information</caption>
<tr><th>library</th><th>version</th><th>Loaded From</th></tr>
<tr><td>Xerces</td><td><%= org.apache.xerces.impl.Version.getVersion()
%></td><td>
<%= org.apache.xerces.impl.Version.class.getResource("Version.class").getFile()
%></td></tr>
<tr><td>Xalan</td><td><%= org.apache.xalan.Version.getVersion()
%></td><td>
<%= org.apache.xalan.Version.class.getResource("Version.class").getFile()
%></td></tr>
<tr><td>Java</td><td><%= System.getProperty("java.specification.version")
%></td></tr></table>

<%
 String tf = System.getProperty("javax.xml.transform.TransformerFactory");
 if (tf != null && tf.indexOf('.') != -1){
    String tfClass = tf.substring(tf.lastIndexOf('.') + 1);
    java.net.URL tfURL = Thread.currentThread().getContextClassLoader().loadClass(tf).getResource(tfClass + ".class");
    String tfPath = (tfURL == null)?"not found [ " + tfClass + " ]":tfURL.getFile();
%>
<table><caption><%= Package.getPackage("javax.xml.transform").getImplementationTitle() %></caption>
<tr><td><%=tf %></td><td><%= Package.getPackage("javax.xml.transform").getImplementationVersion()
%></td><td><%=tfPath %></td></tr>
</table>
<%} %>
</body>
</html>