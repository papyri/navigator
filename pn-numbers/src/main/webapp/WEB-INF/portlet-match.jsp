<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/html; charset=UTF-8" import="java.util.*,javax.portlet.RenderRequest,info.papyri.numbers.portlet.Numbers" %><%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/> 
<%
RenderRequest rReq = (RenderRequest)renderRequest;
String item = (String)rReq.getAttribute(Numbers.ATTR_ITEM);
String apisPrefix = request.getParameter("apisPrefix");
String hgvPrefix = request.getParameter("hgvPrefix");
String ddbPrefix = request.getParameter("ddbPrefix");
String tmPrefix = request.getParameter("tmPrefix");
StringBuffer parms = new StringBuffer();
parms.append("prefix="+item);
if(apisPrefix != null) parms.append("&apisPrefix="+apisPrefix);
if(hgvPrefix != null) parms.append("&hgvPrefix="+hgvPrefix);
if(ddbPrefix != null) parms.append("&ddbPrefix="+ddbPrefix);
if(tmPrefix != null) parms.append("&tmPrefix="+tmPrefix);
%>
<tr><td><a href="portal/numbers.psml?<%=parms.toString()%>"><%=item%></a></td></tr>