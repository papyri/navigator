<%@ page language="java"%>
<%@ page session="false"
         contentType="text/html"
         import="info.papyri.metadata.*,info.papyri.navigator.portlet.*,org.apache.lucene.document.*,org.apache.lucene.search.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%@page import="util.XMLEncoder"%>
<%!
String EXT_LINK_ICON = "<img src=\"decorations/images/external.gif\" alt=\"link to external metadata\" />";
String [] fields = new String[]{
        CoreMetadataFields.TITLE, CoreMetadataFields.PUBLICATION_NOTE, CoreMetadataFields.SUMMARY,
        CoreMetadataFields.INV, CoreMetadataFields.PHYS_DESC, CoreMetadataFields.PROVENANCE_NOTE,
        CoreMetadataFields.LANG, CoreMetadataFields.MATERIAL,
        CoreMetadataFields.GEN_NOTES, CoreMetadataFields.SUBJECT_D, CoreMetadataFields.NAME_ASSOC
};
String [] labels = new String[]{
        "Title", "Publication", "Summary",
        "Inv. Id", "Physical Desc.", "Provenance",
        "Language", "Material",
        "Notes", "Subject(s)", "Associated Name(s)"
};
String [] dFields = new String[]{
        CoreMetadataFields.DATE1_I,
        CoreMetadataFields.DATE2_I,
        CoreMetadataFields.SORT_HAS_IMG,
        CoreMetadataFields.SORT_HAS_TRANS,
        CoreMetadataFields.INDEXED_SERIES,
        CoreMetadataFields.PROVENANCE,
        CoreMetadataFields.XREFS

};
String [] dLabels = new String[]{
        "Date 1 Index",
        "Date 2 Index",
        "Image Flag",
        "Translation Flag",
        "Indexed Series",
        "Indexed Provenance",
        "Cross-References"
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
%><portlet:defineObjects/><%String apisId = request.getParameter("controlName").trim();%>
<%
Document doc = (Document)renderRequest.getAttribute(HGVPortlet.DOC_ATTR);

   if (doc == null){
       doc = new Document();
   }
   %><tbody>
<tr><th class="apis-portal-title" colspan="2">Metadata for <%=doc.get(CoreMetadataFields.DOC_ID) %></th></tr>

<%
  if (doc.get(CoreMetadataFields.DOC_ID) == null){
    Enumeration fields = doc.fields();
    while (fields.hasMoreElements()){
        out.println(fields.nextElement());
    }
      %>
<tr><td style="text-align:center;font-weight:bold;">No metadata available for this document in other collections.</td></tr>
      <%
  }
  if (doc.get(CoreMetadataFields.TITLE) != null){
      %>
<tr><th class="rowheader">Title</th><td><%=doc.get(CoreMetadataFields.TITLE) %></td></tr>
<%  }
    String [] pubs = doc.getValues(CoreMetadataFields.BIBL_PUB);
    if (pubs == null) pubs = new String[0];
    for (int i =0; i<pubs.length; i++){
        out.print("<tr>");
        if (i == 0) out.print("<th class=\"rowheader\" rowspan=\"" + pubs.length + "\">Publication Number</th>");
        out.println("<td>" + pubs[i] + "</td></tr>");
        
    }
    if (doc.get(CoreMetadataFields.PUB_ABOUT) != null){
%>
<tr><th class="rowheader">Post-Concordance BL Entries</th><td><%=doc.get(CoreMetadataFields.PUB_ABOUT)%></td></tr>
<%
    }
    if (doc.get(CoreMetadataFields.PROVENANCE_NOTE) != null){
%>
<tr><th class="rowheader">Provenance</th><td><%=doc.get(CoreMetadataFields.PROVENANCE_NOTE) %></td></tr>
<%
    }
    if (doc.get(CoreMetadataFields.MATERIAL) != null){
%>
<tr><th class="rowheader">Material</th><td><%=doc.get(CoreMetadataFields.MATERIAL) %></td></tr>
<%
String [] dates = doc.getValues(CoreMetadataFields.DATE1_D);
String [] date2s = doc.getValues(CoreMetadataFields.DATE2_D);

if (dates == null) dates = new String[0];
for (int i =0; i<dates.length; i++){
    out.print("<tr>");
    if (i == 0) out.print("<th class=\"rowheader\" rowspan=\"" + dates.length + "\">Date</th>");
    if (!date2s[i].equals(dates[i])){
        out.println("<td>" + dates[i] + " - " + date2s[i] + "</td></tr>");
    }
    else{
        out.println("<td>" + dates[i] + "</td></tr>");
    }
    
}
%>

<%
 }
      String img = doc.get(CoreMetadataFields.IMG_URL);
      if (img != null){
    %>
<tr><th class="rowheader">Image Notes</th><td><%=XMLEncoder.insertLinks(img,"link to image") %></td></tr>
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
  } %>    </tbody>