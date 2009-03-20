<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.metadata.CoreMetadataFields,java.util.*,info.papyri.navigator.portlet.*,org.apache.lucene.document.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%><%@page import="info.papyri.metadata.NamespacePrefixes"%>
<portlet:defineObjects/><%
String apisId = request.getParameter("controlName");
apisId = (apisId == null)?"":apisId.trim();
String xml = "/pn-portals/xml?controlName=" + apisId;
PortletRequest portletRequest = (PortletRequest)
request.getAttribute("javax.portlet.request");
  Document doc = (Document)renderRequest.getAttribute(NavigatorPortlet.DOC_ATTR);
String divClass = (apisId.startsWith(NamespacePrefixes.APIS))?"pn-apis-data":"pn-hgv-data";
%>
<%
  if (doc == null || doc.get(CoreMetadataFields.DOC_ID) == null){
      %>
<div class="<%=divClass %>"><table class="metadata">
<caption>No metadata available for <%=XREFPortlet.getDisplay(apisId) %></caption>
</table>
</div>
      <%
      return;
  }
%><div class="<%=divClass %>">
<h2 class="apis-portal-title">Metadata for <%=doc.get("controlName") %><a class="xml" href="<%=xml %>" target="_new">(xml)</a></h2>
<table class="metadata">
<%
  if (doc.get(CoreMetadataFields.TITLE) != null){
%>
<tr>
  <th class="rowheader">Title</th>
  <td><%=doc.get(CoreMetadataFields.TITLE) %></td>
 </tr>
<%
  }
  if (doc.get(CoreMetadataFields.DATE1_D) != null){
      String [] dates = doc.getValues(CoreMetadataFields.DATE1_D);
%>
<tr><th class="rowheader" rowspan="<%=dates.length %>">Dates</th><td><%=dates[0] %></td></tr>
<%  for(int ix=1;ix<dates.length;ix++){
%>
<tr><td><%=dates[ix] %></td></tr>
<%
}
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
  if (doc.get(CoreMetadataFields.SUBJECT_I) != null){
      String [] subjects = doc.getValues(CoreMetadataFields.SUBJECT_I);
      %>
      <tr><th class="rowheader" rowspan="<%=subjects.length %>">Language</th><td><%=subjects[0] %></td></tr>
      <%
      for (int i=1;i<subjects.length;i++){
          %>
      <tr><td><%=subjects[i] %></td></tr>
          <%
      }
        }
%>
</table>