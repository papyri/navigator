<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.portlet.*,java.util.*,javax.portlet.*" %><div>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%>
<portlet:defineObjects/>
<h2 class="apis-portal-title">IF Data for <%=request.getParameter("controlName").trim() %></h2>
<table style="width:auto">
<tr><th>APIS IF Field</th><th>Value</th></tr>
<%
  Map apis = (Map)request.getAttribute(IFPortlet.IFDATA_ATTR);
  Iterator keys = apis.keySet().iterator();
  while (keys.hasNext()){
	  Object key = keys.next();
	  Object val = apis.get(key);
	  %>
	  <tr><td><%=key %></td><td><%=val %></td></tr>
	  <%
  }
%>
</table>
</div>