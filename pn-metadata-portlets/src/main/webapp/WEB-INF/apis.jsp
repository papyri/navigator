<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.metadata.*,java.util.*,edu.columbia.apis.portlet.*,org.apache.lucene.document.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%><portlet:defineObjects/><%
String apisId = request.getParameter("controlName").trim();
PortletRequest portletRequest = (PortletRequest)
request.getAttribute("javax.portlet.request");
  Document doc = (Document)renderRequest.getAttribute(NavigatorPortlet.DOC_ATTR);
%><div class="pn-apis-data">
<h2 class="apis-portal-title">Metadata for <%=apisId %></h2>
<table class="metadata">
<%
  if (doc.get(CoreMetadataFields.DOC_ID) == null){
      %>
<tr><td style="text-align:center;font-weight:bold;">No metadata available for <%=apisId %>.</td></tr>
      <%
  }
  if (doc.get(CoreMetadataFields.TITLE) != null){
%>
<tr>
  <th class="rowheader">Title</th>
  <td><%=doc.get(CoreMetadataFields.TITLE) %></td>
 </tr>
<%
  }
  if (doc.get(CoreMetadataFields.SUMMARY) != null){
%>
<tr><th class="rowheader">Summary</th><td><%=doc.get(CoreMetadataFields.SUMMARY) %></td></tr>
<%
  }
  if (doc.get(CoreMetadataFields.LANG) != null){
      String [] langs = doc.getValues(CoreMetadataFields.LANG);
      %>
      <tr><th class="rowheader" rowspan="<%=langs.length %>">Language</th><td><%=langs[0] %></td></tr>
      <%
      for (int i=1;i<langs.length;i++){
          %>
      <tr><td><%=langs[i] %></td></tr>
          <%
      }
        }
  if (doc.get(CoreMetadataFields.INV) != null){
%>
<tr><th class="rowheader">Inv. Id</th><td><%=doc.get(CoreMetadataFields.INV) %></td></tr>
<%
  }
  if (doc.get(CoreMetadataFields.PHYS_DESC) != null){
%>
<tr><th class="rowheader">Phys. Desc.</th><td><%=doc.get(CoreMetadataFields.PHYS_DESC) %></td></tr>
<%
  }
  if (doc.get(CoreMetadataFields.GEN_NOTES) != null){
%>
<tr><th class="rowheader">Notes</th><td><%=doc.get(CoreMetadataFields.GEN_NOTES) %></td></tr>
<%
  }
%>
</table>
</div>