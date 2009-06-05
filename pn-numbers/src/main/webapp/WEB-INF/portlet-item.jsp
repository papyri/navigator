<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/html; charset=UTF-8" import="java.util.*,javax.portlet.RenderRequest,info.papyri.numbers.portlet.Numbers" %><%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/><script type="text/javascript" src="/ddbdp/ddbdp.js"></script>
<div>
<%
RenderRequest rReq = (RenderRequest)renderRequest;
Collection<String> pubs = (Collection)rReq.getAttribute(Numbers.ATTR_PUBS);
Collection<String> apis = (Collection)rReq.getAttribute(Numbers.ATTR_APIS);
Collection<String> hgv = (Collection)rReq.getAttribute(Numbers.ATTR_HGV);
Collection<String> tm = (Collection)rReq.getAttribute(Numbers.ATTR_TM);
Collection<String> ddb = (Collection)rReq.getAttribute(Numbers.ATTR_DDB);
String prefix = (String)rReq.getAttribute(Numbers.ATTR_PREFIX);
%>
<tr><th class="rowheader">Associated Publications</th></tr> 
<% for(String pub:pubs){ %>
<tr><td><%=pub %></td></tr>
<%} %>
<tr><th class="rowheader">Associated APIS ID's</th></tr>
<% for(String pub:apis){ %>
<tr><td><%=pub %> <a href="portal/apisfull.psml?controlName=<%=pub.replaceAll("\\s+","%20") %>">[metadata]</a></td></tr>
<%} %>
<tr><th class="rowheader">Associated HGV ID's</th></tr>
<% for(String pub:hgv){ %>
<tr><td><%=pub %> <a href="portal/apisfull.psml?controlName=<%=pub.replaceAll("\\s+","%20") %>">[metadata]</a></td></tr>
<%} %>
<tr><th class="rowheader">Associated DDbDP ID's</th></tr>
<% for(String pub:ddb){ %>
<tr><td><%=pub %> <a href="portal/text.psml?controlName=<%=pub.replaceAll("\\s+","%20") %>">[transcription]</a></td></tr>
<%} %>
<tr><th class="rowheader">Associated Trismegistos (TM) ID's</th></tr>
<% for(String pub:tm){ %>
<tr><td><%=pub %> <a href="portal/apisfull.psml?controlName=<%=pub.replaceAll("\\s+","%20") %>">[metadata]</a></td></tr>
<%} %>
</div>
