<%@ page language="java"%>
<%@ page session="false" contentType="text/html" %>
<html>
<head>
<title></title>
</head>
<body>
<%
String cn = (String)request.getAttribute("controlName");
String field = (String)request.getAttribute("field");
String content = (String)request.getAttribute(MoreInfoServlet.CONTENT_ATTR);
%>
<pre>More information about <%=field %> in <%=cn %>
<%=content %></pre>
</body>
</html>
