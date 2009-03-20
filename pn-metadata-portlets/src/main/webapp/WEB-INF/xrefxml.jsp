<%@ page language="java"%>
<%@ page session="false" contentType="text/xml" import="info.papyri.navigator.portlet.*,org.apache.lucene.document.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<%
String apisId = request.getParameter("controlName").trim();
PortletRequest portletRequest = (PortletRequest)
request.getAttribute("javax.portlet.request");
  Document doc = (Document)portletRequest.getAttribute(NavigatorPortlet.DOC_ATTR);
if (doc != null){
%>
<document>
<%if (doc.getField("APIS:metadata:apis:controlname") != null){
    %><coverage resource="apis:metadata:apis">
    <identifier resource="<%=doc.get("APIS:metadata:apis:controlname")%>" />
    </coverage>
    <%
    }
%>
<%if (doc.getField("APIS:metadata:hgv:publikationl") != null){
    %><coverage resource="apis:metadata:hgv:publikationl">
    <identifier resource="<%=doc.get("APIS:metadata:hgv:publikationl")%>" />
    </coverage>
    <%
    }
%>
<%if (doc.getField("APIS:metadata:ddbdp:ddbfull") != null){
    %><coverage resource="apis:metadata:ddbdp">
    <identifier resource="<%=doc.get("APIS:metadata:ddbdp:ddbfull")%>" />
    </coverage>
    <%
    }
%>
<%if (doc.getField("APIS:metadata:leuven:texid") != null){
    %><coverage resource="apis:metadata:leuven:texid">
    <identifier resource="<%=doc.get("APIS:metadata:leuven:texid")%>" />
    </coverage>
    <%
    }
%>



</document>
<%
}
%>