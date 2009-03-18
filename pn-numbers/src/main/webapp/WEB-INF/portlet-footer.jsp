<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/html; charset=UTF-8" import="java.util.*,javax.portlet.RenderRequest,info.papyri.numbers.portlet.Numbers" %><%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/></table>
<%
RenderRequest rReq = (RenderRequest)renderRequest;
String prefix = (String)rReq.getAttribute(Numbers.ATTR_PREFIX);
String from = (String)rReq.getAttribute(Numbers.ATTR_FROM);

if(from != null){
StringBuffer parms = new StringBuffer();
parms.append("prefix=");
parms.append(prefix.replaceAll("\\s+","%20"));
String apisPrefix = request.getParameter("apisPrefix");
if(apisPrefix != null && !(apisPrefix = apisPrefix.trim()).equals("")){
parms.append("&apisPrefix=");
parms.append(apisPrefix);
}
String hgvPrefix = request.getParameter("hgvPrefix");
if(hgvPrefix != null && !(hgvPrefix = hgvPrefix.trim()).equals("")){
parms.append("&hgvPrefix=");
parms.append(hgvPrefix);
}
String ddbPrefix = request.getParameter("ddbPrefix");
if(ddbPrefix != null && !(ddbPrefix = ddbPrefix.trim()).equals("")){
parms.append("&ddbPrefix=");
parms.append(ddbPrefix);
}
 %>
<a href="portal/numbers.psml?<%=parms.toString() %>&from=<%=from %>">More</a>
<% } %>
</div>