<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/html; charset=UTF-8" import="java.util.*,info.papyri.ddbdp.servlet.IndexEventPropagator,info.papyri.ddbdp.servlet.ScriptSearch,javax.portlet.RenderRequest" %><%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%!
String getQueryDesc(String [] terms, String [] ops, String [] slops){
    StringBuffer result = new StringBuffer();
    if(terms.length > 0 && !"".equals(terms[0])){
        result.append('"');
        result.append(terms[0]);
        result.append('"');
    }
    if(terms.length > 1 && !"".equals(terms[1])){
        result.append(' ');
        result.append(ops[0]);
        result.append(' ');
        result.append('"');
        result.append(terms[1]);
        result.append("\" within ");
        result.append(slops[0]);
    }
    if(terms.length > 2 && !"".equals(terms[2])){
        result.append(' ');
        result.append(ops[1]);
        result.append(' ');
        result.append('"');
        result.append(terms[2]);
        result.append("\" within ");
        result.append(slops[1]);
    }
    return result.toString();
}
%><%@page import="info.papyri.ddbdp.servlet.IndexEventPropagator;"%>
<portlet:defineObjects/></tbody></table>
<%
RenderRequest rReq = (RenderRequest)renderRequest;
String [] terms = request.getParameterValues("queryterm");
boolean submitted = request.getParameter("submitted") != null; 

if (terms != null){
    for(int i=0;i<terms.length;i++){
        terms[i] = ScriptSearch.getSafeUTF8(terms[i].replaceAll("[<>\\{\\}]",""));
    }
}
else {
    terms = new String[]{""};
}
String [] slops = new String[]{"1","1"};
if(request.getParameterValues("slop") != null){
    System.arraycopy(request.getParameterValues("slop"),0, slops,0,request.getParameterValues("slop").length);
}

for(int i=0;i<slops.length;i++){
    slops[i] = ScriptSearch.getSafeUTF8(slops[i].replaceAll("[<>\\{\\}]",""));
}
String numHitsS = (String)rReq.getAttribute(ScriptSearch.NUM_RECS_ATTR);
int numHits = (numHitsS != null)?Integer.parseInt(numHitsS):0;
int numPages = numHits / ScriptSearch.PAGE_SIZE;
if(numHits % ScriptSearch.PAGE_SIZE != 0) numPages++;
if (submitted){ 
    int startRec = 1;
    String startRecordS = request.getParameter("offset");
    if (startRecordS != null){
        startRecordS = startRecordS.trim();
        try{
            startRec += Integer.parseInt(startRecordS);
        }
        catch(Throwable t){}
    }

%>
<table id="pager-foot">
<tr>
<th>Jump to page:</th>
<%
int currPage = (startRec)/ScriptSearch.PAGE_SIZE;
int this10 = (startRec / (10*ScriptSearch.PAGE_SIZE)) ;
if(numPages > 10){
    int last10 =  this10 - 1;
    if(last10 > -1){
        String link = "<td><a href=\"\" onclick=\"document.getElementById('offset').value='" + (last10 * ScriptSearch.PAGE_SIZE) + "';setQueries();document.getElementById('query-form').submit();return false;\">[prev 10]</a></td>";
        out.print(link);
    }
    else{
        out.print("<td>[prev 10]</td>");
    }
}

int prevPageStart  = startRec - ScriptSearch.PAGE_SIZE;
if(prevPageStart > -1){
    String link = "<td><a href=\"\" onclick=\"document.getElementById('offset').value='" + (prevPageStart) + "';setQueries();document.getElementById('query-form').submit();return false;\">&lt;&lt;</a></td>";
    out.print(link);
}
else{
    out.print("<td>&lt;&lt;</td>");
}

int lastPage = Math.min((this10 + 1)*10,numPages);
for(int i=(this10*10); i<lastPage;i++){
    if(i == currPage){
        out.print("<td class=\"current\">" + (i + 1) + "</td>" );
        continue;
    }
    int pageStartRec = (ScriptSearch.PAGE_SIZE*i) + 1;
    
    String link = "<td><a href=\"\" onclick=\"document.getElementById('offset').value='" + (ScriptSearch.PAGE_SIZE*i) + "';setQueries();document.getElementById('query-form').submit();return false;\">" + (i+1) + "</a></td>";
    out.print(link);
}

int nextPageStart = startRec + ScriptSearch.PAGE_SIZE;
if(nextPageStart > 0 && nextPageStart < numHits){
    String link = "<td><a href=\"\" onclick=\"document.getElementById('offset').value='" + (nextPageStart) + "';setQueries();document.getElementById('query-form').submit();return false;\">&gt;&gt;</a></td>";
    out.print(link);
}
else{
    out.print("<td>&gt;&gt;</td>");
}

if(lastPage < numPages){
    int next = (this10 + 1) * (10 * ScriptSearch.PAGE_SIZE);
    String link = "<td><a href=\"\" onclick=\"document.getElementById('offset').value='" + next + "';setQueries();document.getElementById('query-form').submit();return false;\">[next 10]</a></td>";
    out.print(link);
}
}
%>
</tr>
</table>
    <% 
          if (rReq.getAttribute(ScriptSearch.QUERY_TIMER_ATTR) != null){
              Long start = (Long)rReq.getAttribute(ScriptSearch.QUERY_TIMER_ATTR);
              long millis = System.currentTimeMillis() - start.longValue();
              out.println("<p>Query executed in " + millis + " ms</p>");
          }
    %>
</div>