<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.metadata.NamespacePrefixes,info.papyri.metadata.CoreMetadataFields,info.papyri.index.LuceneIndex,org.apache.lucene.index.*,org.apache.lucene.search.*,info.papyri.navigator.portlet.*,org.apache.lucene.document.*,java.util.*,javax.portlet.*" %><div>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%>
<portlet:defineObjects/>
<%
String apisId = (String)renderRequest.getAttribute(FSIImagePortlet.CN_ATTR);
apisId = (apisId == null)?"":apisId.trim();
IndexSearcher search = null;
if (apisId.startsWith(NamespacePrefixes.APIS)) search = LuceneIndex.SEARCH_COL;
if (apisId.startsWith(NamespacePrefixes.HGV)) search = LuceneIndex.SEARCH_HGV;

//PortletRequest portletRequest = (PortletRequest)request.getAttribute("javax.portlet.request");
  Document doc = (Document)renderRequest.getAttribute(NavigatorPortlet.DOC_ATTR);
  if(doc==null)doc=new Document();
%>
<h2 class="apis-portal-title">Images for <%=XREFPortlet.getDisplay(apisId) %></h2>
<%
  if (doc.get(CoreMetadataFields.DOC_ID) == null){
      %>
<table class="metadata">
<tr><td style="text-align:center;font-weight:bold;">No metadata available for <%=XREFPortlet.getDisplay(apisId) %>.</td></tr>
</table>
</div>
      <%
  return;
  }
%>
<table class="metadata">
<%
if (doc.get(CoreMetadataFields.IMG_URL) != null){
String thumb = null;
    String [] images = doc.getValues(CoreMetadataFields.IMG_URL);
    String [] captions = doc.getValues(CoreMetadataFields.IMG_CAPTION);
%>
  
<%
    for (int i=1;i<captions.length; i++){
      if (captions[i].toLowerCase().indexOf("thumb") > -1){
          thumb = images[i];
          break;
      }
    }
    for (int i=0;i<images.length; i++){
        if (!images[i].startsWith("http")){
            %><tr><td class="imageLinks"><%=images[i] %></td></tr>
            <%
        }
        else {
%>
    <tr><td class="imageLinks"><a href="<%=images[i] %>"><%=captions[i] %></a></td>
    <% if (i == 0){ %>
    <td rowspan="<%=images.length %>" ><%if (thumb != null){ %><img src="<%=thumb %>" /><%}; %></td>
<%  }
    %>
    </tr>
    <% }
    }
} else {%>
<tr><td style="text-align:center;font-weight:bold;">No image metadata available for <%=XREFPortlet.getDisplay(doc.get(CoreMetadataFields.DOC_ID)) %>.</td></tr>
<%} %>
</table></div>