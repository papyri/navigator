<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.metadata.CoreMetadataFields,info.papyri.navigator.portlet.*,org.apache.lucene.document.*,org.apache.lucene.search.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%><%@page import="util.XMLEncoder"%>
<portlet:defineObjects/><%String apisId = request.getParameter("controlName").trim();%>
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
<tr><td style="text-align:center;font-weight:bold;">No HGV metadata available for this document.</td></tr>
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
    if (doc.get(CoreMetadataFields.PROVENANCE) != null){
%>
<tr><th class="rowheader">Provenance</th><td><%=doc.get(CoreMetadataFields.PROVENANCE_NOTE) %></td></tr>
<%
    }
    if (doc.get(CoreMetadataFields.MATERIAL) != null){
%>
<tr><th class="rowheader">Material</th><td><%=doc.get(CoreMetadataFields.MATERIAL) %></td></tr>
<%
    }
      String date1 = doc.get(CoreMetadataFields.DATE1_D);
      if (date1 != null){
%>
<tr><th class="rowheader">Date</th><td><%=date1 %>
<%
            String date2 = doc.get(CoreMetadataFields.DATE2_D);
            if (date2 != null && !date2.equals(date1)) out.print(" - " + date2);
%>
</td></tr>
<%
 }
      String img = doc.get(CoreMetadataFields.IMG_URL);
      if (img != null){
    %>
<tr><th class="rowheader">Image Notes</th><td><%=XMLEncoder.insertLinks(img,"link to image") %></td></tr>
    <%} %>
    </tbody>