<%@page language="java" session="false" contentType="text/html" import="java.util.regex.Pattern,org.apache.lucene.search.*,org.apache.lucene.index.*,org.apache.lucene.document.*,edu.columbia.apis.*,util.jsp.el.Functions,java.util.*,javax.portlet.*,info.papyri.index.LuceneIndex,info.papyri.navigator.portlet.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@taglib uri="tld/el-functions.tld" prefix="custom"%><portlet:defineObjects/>
<portlet:defineObjects/><%! final Pattern DIGIT = Pattern.compile("^\\d+$"); %>
<%
            
//String jsp = ((RenderRequest)renderRequest).getPreferences().getValue("jsp","summary");
String mode = renderRequest.getPreferences().getValue("pn-record-display", "brief");
String jsp = ("verbose".equals(mode))?"summary":"table";
pageContext.setAttribute("headName","/WEB-INF/" + jsp + "THEAD.jsp");
pageContext.setAttribute("bodyName","/WEB-INF/" + jsp + "TBODY.jsp");
pageContext.setAttribute("footName","/WEB-INF/" + jsp + "TFOOT.jsp");


%>
<%
int numResults = -1;
int numPages = -1;
if (renderRequest.getAttribute(NavigatorPortlet.XREF_NUM_RESULTS) != null){
    numResults = ((Integer)renderRequest.getAttribute(NavigatorPortlet.XREF_NUM_RESULTS)).intValue();
    numPages = (numResults % 25 == 0)?(numResults / 25):(numResults/25 + 1);
}
boolean showUI = !"results".equals(request.getParameter("pn-display-mode"));
if (showUI){%>
<div>
<jsp:include flush="true" page="ui.jsp"></jsp:include>
</div>
<%    
  return;
}
else {
  if(numResults <= 0){
  String msg = "Your search yielded no results.  Please revise your search terms:";
  if (renderRequest.getAttribute(MetadataSearchPortlet.PN_MSG) != null) msg = renderRequest.getAttribute(MetadataSearchPortlet.PN_MSG).toString();
  %>
<div id="no-results-msg"><%=msg %></div>
<% if (renderRequest.getAttribute(MetadataSearchPortlet.PN_QUERY) != null){
    String pnQuery = renderRequest.getAttribute(MetadataSearchPortlet.PN_QUERY).toString();
    %>
<div id="pn-query"><%=pnQuery %>
</div>
<%} %>
<div>
<jsp:include flush="true" page="ui.jsp"></jsp:include>
</div>
  <%
  return;
  } else {%>
<jsp:include flush="true" page="no-ui.jsp"></jsp:include>
<% }

request.setAttribute(NavigatorPortlet.XREF_NUM_RESULTS,numResults);
String pageS = request.getParameter("page");
request.setAttribute(NavigatorPortlet.XREF_PAGE,request.getParameter("page"));
String url = renderRequest.getAttribute(NavigatorPortlet.XREF_REQ_URL).toString();
request.setAttribute(NavigatorPortlet.XREF_REQ_URL,url);
StringBuffer queryBuf = new StringBuffer();
Enumeration parmNames = renderRequest.getParameterNames();
while (parmNames.hasMoreElements()){
    String parm = parmNames.nextElement().toString();
    if (parm.equals("page")) continue;
    String [] vals = renderRequest.getParameterValues(parm);
    for (int i=0;i<vals.length;i++){
        if (i > 0) queryBuf.append("&amp;");
        queryBuf.append(parm);
        queryBuf.append('=');
        queryBuf.append(Functions.encode(vals[i]));
    }
    if (parmNames.hasMoreElements()) queryBuf.append("&amp;");
}
String numPerPageS = request.getParameter("pn-page-length");
if (numPerPageS == null) numPerPageS = "25";
int numPerPage = 25;
if (DIGIT.matcher(numPerPageS).matches()){
    numPerPage = Integer.parseInt(numPerPageS);
}
request.setAttribute(MetadataSearchPortlet.PN_NUM_DOCS_PER_PAGE,numPerPage);

String query = queryBuf.toString();
request.setAttribute(NavigatorPortlet.XREF_QUERY_STRING,query);
int offset = 0;
if (pageS != null && pageS.matches("^\\d+$")){
    offset = numPerPage * (Integer.parseInt(pageS) - 1);
}
%><div>

                  <table id="pn-results" rules="groups" class="metadata">
                    <jsp:include flush="true" page="${headName}"></jsp:include>
                <jsp:include flush="true" page="${footName}"></jsp:include>
            <%if (renderRequest.getAttribute(NavigatorPortlet.XREF_RESULTS) != null){
                Hits results = (Hits)renderRequest.getAttribute(NavigatorPortlet.XREF_RESULTS);
                Iterator<Hit> xrefHits = results.iterator();
                for (int i =0;i < offset; i++){
                    if (xrefHits.hasNext()) xrefHits.next();
                }
                for (int pageDocIndex = 0; pageDocIndex < numPerPage; pageDocIndex++){
                    if (!xrefHits.hasNext()) break;
                    Hit hit = xrefHits.next();
                    org.apache.lucene.document.Document doc = hit.getDocument();
                    request.setAttribute(MetadataSearchPortlet.XREF_DOC,doc);
                    request.setAttribute(MetadataSearchPortlet.XREF_PAGE_DOC_NUMBER,Integer.valueOf(pageDocIndex));
                    %>
                    <jsp:include flush="true" page="${bodyName}"></jsp:include>
                <%
                }
            }
                %>
                </table>
<%}%>
</div>
