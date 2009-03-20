<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.metadata.CoreMetadataFields,info.papyri.navigator.portlet.*,org.apache.lucene.document.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%>
<portlet:defineObjects/>
<%
String id = request.getParameter("controlName").trim();
RenderRequest portletRequest = (RenderRequest)renderRequest;
Document doc = (Document)portletRequest.getAttribute(NavigatorPortlet.DOC_ATTR);
String docMsg = (doc==null)?" (no doc)":"";
String field = (String)portletRequest.getAttribute(TranslationPortlet.FIELD_ATTR);
docMsg = (doc != null &&field==null)?"(no field)":docMsg;
String title = (String)portletRequest.getAttribute(TranslationPortlet.TITLE_ATTR);
if(title == null) title = "Translation";
%>
<div>
<%
if (doc != null && field != null && doc.get(field) != null){ %>
<h2 class="apis-portal-title"><%=XREFPortlet.getDisplay(doc.get(CoreMetadataFields.DOC_ID)) %> <%=title %></h2>
<p><%=doc.get(field) %></p>
<%
}
else {
%>
<table class="metadata">
<tr><td style="text-align:center;font-weight:bold;"><%=title %> not available for <%=XREFPortlet.getDisplay(id) %><%=docMsg %>.</td></tr>
</table>
<%} %>
</div>