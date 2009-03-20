<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.navigator.portlet.*,org.apache.lucene.document.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%!

%>
<portlet:defineObjects/>
<%
  String apisId = request.getParameter("controlName");
  if (apisId != null){
      apisId = apisId.trim();
  }
  else {
      apisId = "";
  }
PortletRequest portletRequest = (PortletRequest)
request.getAttribute("javax.portlet.request");
  Document doc = (Document)portletRequest.getAttribute(NavigatorPortlet.DOC_ATTR);
if (doc != null){
%>
<tr class="searchresult">
<%if (doc.getField("APIS:metadata:apis:controlname") != null){
    %><td><a href="<%=XREFPortlet.getAPISlink(doc.getField("APIS:metadata:apis:controlname").stringValue()) %>"><%=doc.get("APIS:metadata:apis:controlname")%></a>
    </td><td> <%
String aMD = "portal/apisfull.psml?controlName=" + doc.getField("APIS:metadata:apis:controlname").stringValue();
%>
    <a href="<%=aMD%>">(go to document)</a>
</td>
    <%
    }
    else{ %>
    <td>&nbsp;</td><td>&nbsp;</td>
    <%} %>
<td><%if (doc.getField("APIS:metadata:hgv:publikationl") != null){
    %><a href="<%=XREFPortlet.getHGVlink(doc.getField("APIS:metadata:hgv:publikationl").stringValue()) %>"><%=doc.get("APIS:metadata:hgv:publikationl")%></a>
    <%
    }
    else{ %>
    &nbsp;
    <%} %></td>
<td><%if (doc.getField("APIS:metadata:ddbdp:ddbfull") != null){
    %><a href="<%=XREFPortlet.getDDBDPlink(doc.getField("APIS:metadata:ddbdp:ddbfull").stringValue()) %>"><%=doc.get("APIS:metadata:ddbdp:ddbfull")%></a>
    <%
    }
    else{ %>
    &nbsp;
    <%} %></td>
<td><%if (doc.getField("APIS:metadata:leuven:texid") != null){
    %><span> <%= ("<!--" + doc.get("APIS:metadata:leuven:texid") + "-->")%>  </span>
    <%
    }
    else { %>
    &nbsp;
    <% } %></td></tr>
<%
}
else {
%>
null document
<%
}
%>