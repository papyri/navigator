<%@page import="info.papyri.antlr.*,info.papyri.portlet.*,java.util.*" pageEncoding="UTF-8" %>
<h2>Query for <%=request.getAttribute(DDBDPSearchPortlet.QUERY_TERM_ATTR) %></h2>
<table>
<thead><tr><th>Perseus ID</th><th>&nbsp;</th><th>&nbsp;</th><th>Matching Text</th></tr></thead>
<%
  DDBDPResultSet ddbdpSet = (DDBDPResultSet)request.getAttribute(DDBDPSearchPortlet.RESULTS_ATTR);
  Map<String,DDBDPResult> results = ddbdpSet.results;
  Iterator iter = results.keySet().iterator();
  while (iter.hasNext()){
	  String key = (String)iter.next();
	  DDBDPResult result = results.get(key);
	  %>
	  <tr>
	  <td class="greek"><a href="<%=DDBDPSearchPortlet.TEXT_QUERY.replaceAll("QQdocQQ",key) %>"><%= key %> at Perseus</a></td>
	  
	  <% if (!"".equals(result.getControlName()) && result.getControlName() != null){  %>
	  <td><a href="/serapis/portal/apismetadata.psml?controlName=<%=result.getControlName() %>">Metadata Portlets</a></td>
	  <td><a href="/serapis/portal/apistext.psml?controlName=<%=result.getControlName() %>">Text Portlets</a></td>
	  <%}
	  else{
	  %><td>Metadata Portlet Unavailable</td><td>Text Portlet Unavailable</td>
	  <%} %>
	  
	  <td class="greek"><%out.write(results.get(key).getText()); %></td>
	  </tr>
	  <%
  }
%>
</table>