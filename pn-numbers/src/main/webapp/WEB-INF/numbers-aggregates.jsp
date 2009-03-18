<%@ page language="java"%><%@ page session="false" pageEncoding="UTF-8" contentType="text/xml; charset=UTF-8" import="info.papyri.numbers.servlet.*" %>
<ore:aggregates>
<%
java.util.Map<String,String> uriToUrl = (java.util.Map<String,String>)request.getAttribute(Numbers.ATTR_AGGREGATES);
for(String uri:uriToUrl.keySet()){
%>
<rdf:Description rdf:about="<%=uri %>#source">
    <ore:describes rdf:resource="<%=uri %>" />
    <dcterms:source><%=uriToUrl.get(uri)%></dcterms:source>
 </rdf:Description>

<%
}
%>
</ore:aggregates> 
         