<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="util.*,info.papyri.index.LuceneIndex,info.papyri.index.DisplayFields,info.papyri.metadata.*,info.papyri.navigator.portlet.*,org.apache.lucene.document.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%>
<%!
String EXT_LINK_ICON = "<img src=\"decorations/images/external.gif\" alt=\"link to external metadata\" />";

  static String parseCaps(String value){
    String result = "";
    int len = value.length();
    int prev = 0;
    for (int i=1;i<len;i++){
        if (Character.isUpperCase(value.charAt(i))){
            result = result.concat(value.substring(prev,i));
            result = result.concat(" ");
            prev = i;
        }
     }
 if (prev > 0) result= result.concat(value.substring(prev));
 if (prev == 0) result = value;
     char [] chars = result.toCharArray();
     if (chars.length > 0) chars[0] = Character.toUpperCase(chars[0]);
     return new String(chars).trim();
 }
%><portlet:defineObjects/><%
String [] fields = DisplayFields.apisFields;
String [] labels = DisplayFields.apisLabels;
String [] dFields = DisplayFields.apisDebugFields;
String [] dLabels = DisplayFields.apisDebugLabels;
String apisId = request.getParameter("controlName");
apisId = (apisId == null)?"":apisId.trim();
if (apisId.startsWith(NamespacePrefixes.HGV)){
    fields = DisplayFields.hgvFields;
    labels = DisplayFields.hgvLabels;
    dFields = DisplayFields.hgvDebugFields;
    dLabels = DisplayFields.hgvDebugLabels;
}

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
<caption>No metadata available for <%=apisId %></caption>
</table>
</div>
      <%
      return;
  } %>
  <div class="<%=divClass %>">
  <h2 class="apis-portal-title">Metadata for <%=XREFPortlet.getDisplay(doc.get(CoreMetadataFields.DOC_ID)) %><a class="xml" href="<%=xml %>" target="_new">(xml)</a></h2>
<table class="metadata">
  <%
for(int i=0;i<fields.length;i++){
    String name = fields[i];
      String [] values = doc.getValues(name);
      if (values != null && values.length > 0 && !"".equals(values[0])){
          if (values[0].trim().charAt(0) == '|') values[0] = values[0].trim().substring(1);
          %>
          <tr><th class="rowheader" rowspan="<%=values.length %>"><%=labels[i] %></th>
      <td style="">
     <%=XMLEncoder.insertLinks(values[0], "external resource" + EXT_LINK_ICON).replaceAll("\\|","<br/>") %>
</td></tr>
          <%
        for (int j=1;j<values.length;j++){
          if (values[j].trim().charAt(0) == '|') values[j] = values[j].trim().substring(1);
      %>
      <tr><td>
      <%=XMLEncoder.insertLinks(values[j], "external resource" + EXT_LINK_ICON).replaceAll("\\|","<br/>") %>
      </td></tr>
      <%}
      }
  }
  if (doc.get(CoreMetadataFields.DATE1_D) != null){
      String date = doc.get(CoreMetadataFields.DATE1_D);
      String date2 = doc.get(CoreMetadataFields.DATE2_D);
      if ( date2 != null && !date.equals(date2)) date += " - " + date2;
%>
          <tr><th class="rowheader">Date</th><td><%=date %></td></tr>
<%}
  if ("true".equals(request.getParameter("debug"))){
    for(int i=0;i<dFields.length;i++){
    String name = dFields[i];
      String [] dValues = doc.getValues(name);
      if (dValues != null && dValues.length > 0 && !"".equals(dValues[0])){
          if (dValues[0].trim().charAt(0) == '|') dValues[0] = dValues[0].trim().substring(1);
          %>
          <tr><th class="rowheader" rowspan="<%=dValues.length %>"><%=dLabels[i] %></th>
      <td style="">
     <%=XMLEncoder.insertLinks(dValues[0], "external resource" + EXT_LINK_ICON).replaceAll("\\|","<br/>") %>
</td></tr>
          <%
        for (int j=1;j<dValues.length;j++){
          if (dValues[j].trim().charAt(0) == '|') dValues[j] = dValues[j].trim().substring(1);
      %>
      <tr><td>
      <%=XMLEncoder.insertLinks(dValues[j], "external resource" + EXT_LINK_ICON).replaceAll("\\|","<br/>") %>
      </td></tr>
      <%}
      }
  }
  } %>
</table>
</div>