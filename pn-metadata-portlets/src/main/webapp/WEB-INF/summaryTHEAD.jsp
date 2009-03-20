<%@page language="java" session="false" contentType="text/html" import="org.apache.lucene.search.*,org.apache.lucene.index.*,org.apache.lucene.document.*,edu.columbia.apis.*,util.jsp.el.Functions,java.util.*,javax.portlet.*,info.papyri.navigator.portlet.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@taglib uri="tld/el-functions.tld" prefix="custom"%><portlet:defineObjects/>
<%
String query = "";
if (request.getAttribute(NavigatorPortlet.XREF_QUERY_STRING) != null){
    query = request.getAttribute(NavigatorPortlet.XREF_QUERY_STRING).toString();
}
int numResults = 0;
int pageN = 1;
String pageS = null;
if (request.getAttribute(NavigatorPortlet.XREF_PAGE) != null){
    pageS = request.getAttribute(NavigatorPortlet.XREF_PAGE).toString();
}
if (pageS != null && pageS.matches("^\\d+$")){
    pageN = Math.max(Integer.parseInt(pageS),1);
}
int numPerPage = 25;
if (request.getAttribute(MetadataSearchPortlet.PN_NUM_DOCS_PER_PAGE) != null){
    numPerPage = ((Integer)request.getAttribute(MetadataSearchPortlet.PN_NUM_DOCS_PER_PAGE)).intValue();
}

int offset = numPerPage * (pageN - 1);
int numPages = 0;
if (request.getAttribute(NavigatorPortlet.XREF_NUM_RESULTS) != null){
    numResults = (Integer)request.getAttribute(NavigatorPortlet.XREF_NUM_RESULTS);
    numPages = (numResults % numPerPage == 0)?(numResults / numPerPage):(numResults/numPerPage + 1);
}
String url = "";

if (request.getAttribute(NavigatorPortlet.XREF_REQ_URL) != null){
    url = request.getAttribute(NavigatorPortlet.XREF_REQ_URL).toString();
}
%>                  <thead>
                      <tr>
                      <td class="rowheader" align="left" colspan="8">
                        <div style="float:right">
                        <form><input type="button" class="pn-button" onclick="document.getElementById('pn-display-mode').value='search';document.getElementById('ui').submit();return false;" value="Revise Search" />
                              <input type="button" class="pn-button" onclick="clearForm(document.getElementById('ui'),true);document.getElementById('pn-display-mode').value='search';document.getElementById('ui').submit();return false;" value="New Search" />
                        </form>
                      </div>
                      </td></tr>
                      <tr>
                      <td colspan="8"><div style="float:left">
                      <% if (numResults == 0) {%>
                      No Results (0)
                      <% }else{ %>
                      Results <%=offset+1 %>-<%=Math.min(offset + numPerPage,numResults)%> (of <%=numResults %>)
                      <%} %></div>
                      <% if (renderRequest.getAttribute(MetadataSearchPortlet.PN_QUERY) != null){
    String pnQuery = renderRequest.getAttribute(MetadataSearchPortlet.PN_QUERY).toString();
    %>
<div  style="float:left" id="pn-query"><%=pnQuery %>
</div>
<%} %>
                      
                      <div style="float:right">Go to Page:&nbsp;
                      <%
                      int prevPage = ((pageN - 1) / 10) * 10;
                      
                      if (prevPage > 0){
                           %>
                          <a href="<portlet:renderURL/><%="?" + query + "&amp;page=" + prevPage%>" >&lt;&lt;<%=prevPage %></a>&nbsp;
                          <%
                      }
                      %>
                      <% for(int i=(prevPage + 1);i<=Math.min(prevPage +10, numPages);i++){
                         if (pageN == i){
                      %>
                      <span style="font-size:13px;font-weight:bold;"><%=i %></span>&nbsp;
                      <%}
                         else {
                      %>
                      <a href="<portlet:renderURL/><%="?" + query + "&amp;page=" + i%>" ><%=i %></a>&nbsp;
                      <%}
                         }
                         %>
                         <%
                         if(numPages >= prevPage+11) {%>
                          <a href="<portlet:renderURL/><%="?" + query + "&amp;page=" + (prevPage+11)%>" ><%=prevPage+11 %>&gt;&gt;</a>&nbsp;
                         <%} %>
                      </div>
                      </td>
                      </tr>
             </thead>
