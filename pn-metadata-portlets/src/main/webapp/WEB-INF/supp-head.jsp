<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.metadata.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%><portlet:defineObjects/><%
String id = request.getParameter("controlName");
id = (id==null)?"":id.trim();
String divClass = (id.startsWith(NamespacePrefixes.APIS))?"pn-hgv-data":"pn-apis-data";
%><div class="<%=divClass %>">
<table class="metadata">