<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.metadata.CoreMetadataFields,info.papyri.navigator.portlet.*,org.apache.lucene.document.*,org.apache.lucene.search.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%><%@page import="util.XMLEncoder"%>
<%@page import="info.papyri.metadata.NamespacePrefixes"%>
<portlet:defineObjects/><%
String apisId = request.getParameter("controlName");
apisId = (apisId == null)?"":apisId.trim();

%>
<%
Document doc = (Document)renderRequest.getAttribute(SupplementalMetadataPortlet.DOC_ATTR);

   if (doc == null){
       doc = new Document();
   }

    String[] xrefs = doc.getValues(CoreMetadataFields.XREFS);
    Set<String> tmNumbers = new HashSet<String>();
    for (String xref:xrefs) {
        if (xref.contains("trismegistos")) {
            tmNumbers.add(xref.substring(xref.lastIndexOf(':')+1));
        }
    }
   String xml = "/pn-portals/xml?controlName=" + doc.get(CoreMetadataFields.DOC_ID);
   %><tbody>
<tr><th class="apis-portal-title" colspan="2">Metadata for <%=XREFPortlet.getDisplay(doc.get(CoreMetadataFields.DOC_ID)) %><a class="xml" href="<%=xml %>" target="_new">(xml)</a></th></tr>

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
    if (doc.get(CoreMetadataFields.BIBL_CORR) != null){
        String pubAboutLabel = (apisId.startsWith(NamespacePrefixes.APIS))?"Post-Concordance BL Entries":"Additional Publication Notes";
%>
<tr><th class="rowheader"><%=pubAboutLabel %></th><td><%=doc.get(CoreMetadataFields.BIBL_CORR)%></td></tr>
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
      String [] imgs = doc.getValues(CoreMetadataFields.IMG_URL);
      String [] captions = doc.getValues(CoreMetadataFields.IMG_CAPTION);
      String illustration = doc.get(CoreMetadataFields.BIBL_ILLUSTR);
      if(illustration != null){%>
      <tr><th class="rowheader">Print Illustrations</th><td><%=illustration %></td></tr>
      <%
      }
      if (imgs != null){
    %>
<tr><th class="rowheader">Images on the Web</th><td><%for(int i=0;i<imgs.length;i++)out.print(XMLEncoder.insertLinks(imgs[i],captions[i])+"<br/>"); %></td></tr>
<%}   
      for (String tm:tmNumbers) {
%>
<tr>
    <th class="rowheader">TM Number</th>
    <td><a href="http://www.trismegistos.org/tm/detail.php?quick=<%=tm%>"><%=tm%></a></td>
</tr>
    <%
      }
      String ext = doc.get(CoreMetadataFields.EXTERNAL_RESOURCE);
      if (ext != null){
    %>
<tr><th class="rowheader">Internet Resources</th><td><%=XMLEncoder.insertLinks(ext,"link to resource") %></td></tr>
<%}
%>

    </tbody>