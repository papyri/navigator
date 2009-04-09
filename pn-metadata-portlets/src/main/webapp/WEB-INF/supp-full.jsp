<%@page language="java" session="false" contentType="text/html" import="util.XMLEncoder,info.papyri.index.DisplayFields,info.papyri.metadata.*,info.papyri.navigator.portlet.*,org.apache.lucene.document.*,org.apache.lucene.search.*,java.util.*,javax.portlet.*" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@taglib uri="tld/el-functions.tld" prefix="custom"%>
<%!
String parseCaps(String value) {
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

String EXT_LINK_ICON = "<img src=\"decorations/images/external.gif\" alt=\"link to external metadata\" />";
%>
<portlet:defineObjects />
<%
String id = request.getParameter("controlName");
id = (id == null)?"":id.trim();
String suppColl = "";
%>
<%
String [] fields = DisplayFields.hgvFields;
String [] labels = DisplayFields.hgvLabels;
String [] dFields = DisplayFields.hgvDebugFields;
String [] dLabels = DisplayFields.hgvDebugLabels;
if (!id.startsWith(NamespacePrefixes.APIS)){
    fields = DisplayFields.apisFields;
    labels = DisplayFields.apisLabels;
    dFields = DisplayFields.apisDebugFields;
    dLabels = DisplayFields.apisDebugLabels;
}
Document doc = (Document)renderRequest.getAttribute(SupplementalMetadataPortlet.DOC_ATTR);
String[] xrefs = doc.getValues(CoreMetadataFields.XREFS);
Set<String> tmNumbers = new HashSet<String>();
for (String xref:xrefs) {
    if (xref.contains("trismegistos")) {
        tmNumbers.add(xref.substring(xref.lastIndexOf(':')+1));
    }
}

   if (doc == null){
       doc = new Document();
   }
   String xml = "/pn-portals/xml?controlName=" + doc.get(CoreMetadataFields.DOC_ID);
   %>
<tbody>
	<tr>
		<th class="apis-portal-title" colspan="2">Metadata for <%=XREFPortlet.getDisplay(doc.get(CoreMetadataFields.DOC_ID)) %><a
			class="xml" href="<%=xml %>" target="_new">(xml)</a></th>
	</tr>

	<%
  if (doc.get(CoreMetadataFields.DOC_ID) == null){
    Enumeration docFields = doc.fields();
    while (docFields.hasMoreElements()){
        out.println(docFields.nextElement());
    }
      %>
	<tr>
		<td style="text-align:center;font-weight:bold;">No metadata
		available for <%=id %> in other collections.</td>
	</tr>
	<%
  }
  if (doc.get(CoreMetadataFields.TITLE) != null){
      %>
	<tr>
		<th class="rowheader">Title</th>
		<td><%=doc.get(CoreMetadataFields.TITLE) %></td>
	</tr>
	<%  }
  if (doc.get(CoreMetadataFields.INV) != null){
      %>
      	<tr>
      		<th class="rowheader">Inv. Id</th>
      		<td><%=doc.get(CoreMetadataFields.INV) %></td>
      	</tr>
      	<%
          }
    String [] pubs = doc.getValues(CoreMetadataFields.BIBL_PUB);
    if (pubs == null) pubs = new String[0];
    for (int i =0; i<pubs.length; i++){
        out.print("<tr>");
        if (i == 0) out.print("<th class=\"rowheader\" rowspan=\"" + pubs.length + "\">Publication Number</th>");
        out.println("<td>" + pubs[i] + "</td></tr>");
    }
    if (doc.get(CoreMetadataFields.PUB_ABOUT) != null){
 %>
	<tr>
		<th class="rowheader">Additional Publication Notes</th>
		<td><%=doc.get(CoreMetadataFields.PUB_ABOUT)%></td>
	</tr>
	<%
    }
    if(doc.get(CoreMetadataFields.BIBL_CORR) != null){
        %>
	<tr>
		<th class="rowheader">Corrections</th>
		<td><%=doc.get(CoreMetadataFields.BIBL_CORR)%></td>
	</tr>
        <%
    }
    if (doc.get(CoreMetadataFields.PROVENANCE_NOTE) != null){
%>
	<tr>
		<th class="rowheader">Provenance</th>
		<td><%=doc.get(CoreMetadataFields.PROVENANCE_NOTE) %></td>
	</tr>
	<%
    }
    if (doc.get(CoreMetadataFields.MATERIAL) != null){
%>
	<tr>
		<th class="rowheader">Material</th>
		<td><%=doc.get(CoreMetadataFields.MATERIAL) %></td>
	</tr>
	<%
    }
String [] dates = doc.getValues(CoreMetadataFields.DATE1_D);
if (dates == null) dates = new String[0];
for (int i =0; i<dates.length; i++){
    out.print("<tr>");
    if (i == 0) out.print("<th class=\"rowheader\" rowspan=\"" + dates.length + "\">Date</th>");
    out.println("<td>" + dates[i] + "</td></tr>");
 }
String [] bibTrans = doc.getValues(CoreMetadataFields.BIBL_TRANS);
if (bibTrans == null) bibTrans = new String[0];
for (int i =0; i<bibTrans.length; i++){
    out.print("<tr>");
    if (i == 0) out.print("<th class=\"rowheader\" rowspan=\"" + bibTrans.length + "\">Translations</th>");
    out.println("<td>" + bibTrans[i] + "</td></tr>");
 }
      String illustr = doc.get(CoreMetadataFields.BIBL_ILLUSTR);
      if (illustr != null){
    %>
	<tr>
		<th class="rowheader">Image Notes</th>
		<td><%=XMLEncoder.insertLinks(illustr,"link to image") %></td>
	</tr>
	<% }
      String [] imgs = doc.getValues(CoreMetadataFields.IMG_URL);
      String [] captions = doc.getValues(CoreMetadataFields.IMG_CAPTION);
      if(imgs != null){
          for (int i =0; i<imgs.length; i++){
              out.print("<tr>");
              if (i == 0) out.print("<th class=\"rowheader\" rowspan=\"" + imgs.length + "\">Images on the Web</th>");
              out.println("<td><a href=\"" + imgs[i] + "\">" + captions[i] + "</a></td></tr>");
           }
      }
      String ext = doc.get(CoreMetadataFields.EXTERNAL_RESOURCE);
      if (ext != null){
    %>
	<tr>
		<th class="rowheader">Internet Resources</th>
		<td><%=XMLEncoder.insertLinks(ext,"link to image") %></td>
	</tr>
	<%}
      for (String tm:tmNumbers) {
    %>
    <tr>
        <th class="rowheader">TM Number</th>
        <td><a href="http://www.trismegistos.org/tm/detail.php?quick=<%=tm%>"><%=tm%></a></td>
    </tr>
    <%
    }

  if ("true".equals(request.getParameter("debug"))){
    for(int i=0;i<dFields.length;i++){
    String name = dFields[i];
      String [] dValues = doc.getValues(name);
      if (dValues != null && dValues.length > 0 && !"".equals(dValues[0])){
          if (dValues[0].trim().charAt(0) == '|') dValues[0] = dValues[0].trim().substring(1);
          %>
	<tr>
		<th class="rowheader" rowspan="<%=dValues.length %>"><%=dLabels[i] %></th>
		<td style=""><%=XMLEncoder.insertLinks(dValues[0], "external resource" + EXT_LINK_ICON).replaceAll("\\|","<br/>") %>
		</td>
	</tr>
	<%
        for (int j=1;j<dValues.length;j++){
          if (dValues[j].trim().charAt(0) == '|') dValues[j] = dValues[j].trim().substring(1);
      %>
	<tr>
		<th class="rowheader">Internet Resources</th>
		<td><%=XMLEncoder.insertLinks(ext,"link to resource") %></td>
	</tr>
	<%}
      }
    }
  }
      String subjectData = doc.get(CoreMetadataFields.SUBJECT_I);
      if (subjectData != null){
      String [] subjects = subjectData.split("\\|");
      for (int i =0; i<subjects.length; i++){
        out.print("<tr>");
        if (i == 0) out.print("<th class=\"rowheader\" rowspan=\"" + subjects.length + "\">Subject</th>");
        out.println("<td>" + subjects[i] + "</td></tr>");
        
    }
    %>
	<%
      }
 %>
</tbody>
