<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%><portlet:defineObjects/><%
String id = request.getParameter("controlName");
id=(id==null)?"":id.trim();%>
<div class="pn-hgv-data" style="height:50px;">
<table class="metadata">
<tr><td style="text-align:center;font-weight:bold;">No related metadata available for <%=id %> in other data sources.</td></tr>
</table>
</div>