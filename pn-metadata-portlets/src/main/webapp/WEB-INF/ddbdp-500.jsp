<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="edu.columbia.apis.data.APISIndices,edu.columbia.apis.portlet.*,org.apache.lucene.document.*,org.apache.lucene.search.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%><portlet:defineObjects/><%
String apisId = (String)renderRequest.getAttribute(DDBDPTextPortlet.APIS_ID);
String ddbId = (String)renderRequest.getAttribute(DDBDPTextPortlet.DDB_ID);
%>
<div class="pn-ddbdp-data" style="height:50px;">
<table class="metadata">
<tr><td style="text-align:center;font-weight:bold;"><%=apisId %> was mapped to <%=ddbId %>, which does not appear to be available as a Duke Databank transcription.</td></tr>
</table>
</div>