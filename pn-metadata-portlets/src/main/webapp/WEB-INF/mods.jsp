<%@ page language="java"%><%@ page session="false" contentType="text/html" import="util.*,info.papyri.navigator.portlet.*,org.apache.lucene.document.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%><%@page import="info.papyri.metadata.CoreMetadataFields"%>
<portlet:defineObjects/><%
String apisId = request.getParameter("controlName").trim();
PortletRequest portletRequest = (PortletRequest)
request.getAttribute("javax.portlet.request");
  Document doc = (Document)renderRequest.getAttribute(NavigatorPortlet.DOC_ATTR);
%><mods ID=<%=doc.get(CoreMetadataFields.DOC_ID) %>>
<location>
<url><%=XREFPortlet.getHGVlink(doc.get(CoreMetadataFields.DOC_ID)) %></url>
</location>
<%
String [] pubs = doc.getValues(CoreMetadataFields.BIBL_PUB);
if (pubs != null){
    %><relatedItem type="isReferencedBy">
	<typeOfResource>text</typeOfResource>
<%
    for (int i=0;i<pubs.length;i++){
%>
<identifier type="publication"><%=pubs[i] %></identifier><% } %> 
</relatedItem>
<%
} %>
</mods>