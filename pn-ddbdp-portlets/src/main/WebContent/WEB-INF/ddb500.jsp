<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.ddbdp.portlet.*,org.apache.lucene.document.*,org.apache.lucene.search.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%><portlet:defineObjects/><%
String ddbId = (String)renderRequest.getAttribute(DocumentPortlet.DDB_ID);
%>
<div class="pn-ddbdp-data" style="height:50px;">
<table class="metadata">
<tr><td style="text-align:center;font-weight:bold;">There was an error trying to get the transcription of<%=ddbId %>: </td></tr>
</table>
</div>