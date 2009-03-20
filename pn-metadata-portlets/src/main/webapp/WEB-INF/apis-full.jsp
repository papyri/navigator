<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="util.*,edu.columbia.apis.portlet.*,org.apache.lucene.document.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%>
<%!
String EXT_LINK_ICON = "<img src=\"decorations/images/external.gif\" alt=\"link to external metadata\" />";
String [] fields = new String[]{
        APISIndices.TITLE, APISIndices.PUBLICATION_NOTE, APISIndices.SUMMARY,
        APISIndices.INV, APISIndices.PHYS_DESC, APISIndices.PROVENANCE_NOTE,
        APISIndices.LANG, APISIndices.MATERIAL, APISIndices.CUST_HIST_D,
        APISIndices.GEN_NOTES, APISIndices.SUBJECT_D, APISIndices.ASSOC_NAME
};
String [] labels = new String[]{
        "Title", "Publication", "Summary",
        "Inv. Id", "Physical Desc.", "Provenance",
        "Language", "Material", "Custodial History",
        "Notes", "Subject(s)", "Associated Name(s)"
};
String [] dFields = new String[]{
        APISIndices.DATE1_I,
        APISIndices.DATE2_I,
        APISIndices.HAS_IMGS,
        APISIndices.HAS_TRANS,
        APISIndices.INDEXED_SERIES,
        APISIndices.PROVENANCE,
        APISIndices.DDBDP_ALL,
        APISIndices.DDBDP_FIRST
};
String [] dLabels = new String[]{
        "Date 1 Index",
        "Date 2 Index",
        "Image Flag",
        "Translation Flag",
        "Indexed Series",
        "Indexed Provenance",
        "DDbDP ALL",
        "DDbDP FIRST"
        
};

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
String apisId = request.getParameter("controlName").trim();
PortletRequest portletRequest = (PortletRequest)
request.getAttribute("javax.portlet.request");
  Document doc = (Document)renderRequest.getAttribute(NavigatorPortlet.DOC_ATTR);
%>
<div class="pn-apis-data">
<h2 class="apis-portal-title">Metadata for <%=apisId %></h2>
<table class="metadata">
<%
  if (doc.get(APISIndices.CONTROL_NAME) == null){
      %>
<caption>No metadata available for <%=apisId %></caption>
</table>
</div>
      <%
      return;
  }
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
  if (doc.get(APISIndices.DATE1_D) != null){
      String date = doc.get(APISIndices.DATE1_D);
      String date2 = doc.get(APISIndices.DATE2_D);
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