<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/html; charset=UTF-8" import="java.util.*,info.papyri.ddbdp.servlet.IndexEventPropagator,info.papyri.ddbdp.servlet.ScriptSearch,javax.portlet.RenderRequest" %><%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%!
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

%><%@page import="info.papyri.ddbdp.servlet.IndexEventPropagator"%>
<%@page import="info.papyri.ddbdp.portlet.SearchPortlet"%>
<portlet:defineObjects/><script type="text/javascript" src="/ddbdp/ddbdp.js"></script><div>
<%
RenderRequest rReq = (RenderRequest)renderRequest;
String [] terms = request.getParameterValues("queryterm");

if (terms != null){
    for(int i=0;i<terms.length;i++){
        terms[i] = terms[i].replaceAll("[<>\\{\\}]","");
    }
}
else {
    terms = new String[]{""};
}
String [] slops = new String[]{"10","10"};
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
String [] fnames = (String[])rReq.getAttribute(ScriptSearch.FILENAMES_ARRAY_ATTR);
String [] fragments = (String[])rReq.getAttribute(ScriptSearch.FRAGMENTS_ARRAY_ATTR);
String [] ids = (String[])rReq.getAttribute(ScriptSearch.DDB_ID_ARRAY_ATTR);
String marks = request.getParameter("marks");
if(marks==null)marks="off";
String lemmas = request.getParameter("lemmas");
if(lemmas==null)lemmas="off";
String caps = request.getParameter("caps");
if(caps==null)caps="off";
String beta = request.getParameter("beta");
if(beta==null)beta="on";
String nofrags = request.getParameter("nofrags");
if(nofrags==null)nofrags="off";
String imgFirst = request.getParameter("imgFirst");
if(imgFirst==null)imgFirst="off";
String transFirst = request.getParameter("transFirst");
if(transFirst==null)transFirst="off";
String apis = request.getParameter("apis");
if(apis == null || (apis=apis.trim()).equals("")) apis = "";
String pubSeries = request.getParameter("pubSeries");
if(pubSeries == null || (pubSeries=pubSeries.trim()).equals("")) pubSeries = "";
String pubVol = request.getParameter("pubVol");
if(pubVol == null || (pubVol=pubVol.trim()).equals("")) pubVol = "";
String place = request.getParameter("place");
if(place==null)place="";
boolean submitted = request.getParameter("submitted") != null; 

String after = request.getParameter("after");
if (after == null) after = "";
String afterEra = request.getParameter("afterEra");
if(afterEra==null)afterEra="CE";

String before = request.getParameter("before");
if (before == null) before = "";
String beforeEra = request.getParameter("beforeEra");
if(beforeEra==null)beforeEra="CE";

String [] ops = new String[]{"",""};
if(request.getParameterValues("boolean") != null){
    System.arraycopy(request.getParameterValues("boolean"),0, ops,0,request.getParameterValues("boolean").length);
}
if(request.getParameterValues("boolean") != null){
    System.arraycopy(request.getParameterValues("boolean"),0, ops,0,request.getParameterValues("boolean").length);
}

String then1 = "THEN".equals(ops[0])?"selected=\"selected\"":"";
String and1 = "AND".equals(ops[0])?"selected=\"selected\"":"";
String or1 = "OR".equals(ops[0])?"selected=\"selected\"":"";
String not1 = "NOT".equals(ops[0])?"selected=\"selected\"":"";

String then2 = "THEN".equals(ops[1])?"selected=\"selected\"":"";
String and2 = "AND".equals(ops[1])?"selected=\"selected\"":"";
String or2 = "OR".equals(ops[1])?"selected=\"selected\"":"";
String not2 = "NOT".equals(ops[1])?"selected=\"selected\"":"";
String errorMsg = "";
if(rReq.getAttribute(SearchPortlet.ERROR_ATTR) != null){
    errorMsg = ((Throwable)rReq.getAttribute(SearchPortlet.ERROR_ATTR)).getMessage();
}
%>
<div class="error"><%=errorMsg %></div><form method="GET" action="portal/ddbdp-search.psml" id="revise-form" accept-charset="UTF-8">
<input name="queryterm" id="term-1" type="hidden" value="<%=terms[0]%>"/>
<input name="boolean" type="hidden"  value="<%=ops[0]%>"/>
<input name="queryterm" id="term-2" type="hidden" value="<%=(terms.length>1)?terms[1]:""%>"/>
<input type="hidden" name="slop" id="slop-1" value="<%=(slops.length > 0)?slops[0]:""%>"/>
<input type="hidden" name="boolean"  value="<%=ops[1]%>"/>
<input type="hidden" name="queryterm" id="term-3" value="<%=(terms.length>2)?terms[2]:""%>"/>
<input  type="hidden"name="slop" id="slop-2" value="<%=(slops.length > 1)?slops[1]:""%>"/> 
<input type="hidden" name="beta" value="<%=beta%>" />
<input type="hidden" name="caps" value="<%=caps%>" />
<input type="hidden" name="marks" value="<%=marks%>" />
<input type="hidden" name="lemmas" value="<%=lemmas%>" />
<input type="hidden" name="place" value="<%=place%>"/> 
<input type="hidden" name="pubSeries" value="<%=pubSeries%>"/> 
<input type="hidden" name="pubSeries" value="<%=pubSeries%>"/> 
<input type="hidden" name="pubVol" value="<%=pubVol %>" />
<input type="hidden" name="apis" value="<%=apis %>" />
<input type="hidden" name="after" value="<%=after %>" />
<input type="hidden" name="before" value="<%=before %>" />
<input type="hidden" name="afterEra" value="<%=afterEra %>" />
<input type="hidden" name="beforeEra" value="<%=beforeEra %>" />
<input type="hidden" name="nofrags" value="<%=nofrags %>" />
<input type="hidden" name="imgFirst" value="<%=imgFirst %>" />
<input type="hidden" name="transFirst" value="<%=transFirst %>" />
<input type="submit" class="pn-button" value="Revise Search" />
</form>
<form method="GET" action="portal/ddbdp-results.psml" id="query-form" accept-charset="UTF-8" onsubmit="setQueries();">
<input type="hidden" name="queryterm" id="term-1" value="<%=terms[0]%>"/>
<input type="hidden" name="boolean"  id="boolean-1" value="<%=ops[0]%>"/>
<input type="hidden" name="queryterm" id="term-2" type="hidden" value="<%=(terms.length>1)?terms[1]:""%>"/>
<input type="hidden" name="slop" id="slop-1" value="<%=(slops.length > 0)?slops[0]:""%>"/>
<input type="hidden" name="boolean"  id="boolean-2" value="<%=ops[1]%>"/>
<input type="hidden" name="queryterm" id="term-3" value="<%=(terms.length>2)?terms[2]:""%>"/>
<input  type="hidden"name="slop" id="slop-2" value="<%=(slops.length > 1)?slops[1]:""%>"/> 
<input type="hidden" name="beta" id="betaYes" value="<%=beta%>" />
<input type="hidden" name="caps"  id="caps" value="<%=caps%>" />
<input type="hidden" name="marks"  id="marks" value="<%=marks%>" />
<input type="hidden" name="lemmas"  id="lemmas" value="<%=lemmas%>" />
<input type="hidden" id="place" name="place" value="<%=place%>"/> 
<input type="hidden" id="pubSeries" name="pubSeries" value="<%=pubSeries%>"/> 
<input type="hidden" id="pubSeries" name="pubSeries" value="<%=pubSeries%>"/> 
<input type="hidden" id="pubVol" name="pubVol" value="<%=pubVol %>" />
<input type="hidden" id="apis" name="apis" value="<%=apis %>" />
<input type="hidden" id="after" name="after" value="<%=after %>" />
<input type="hidden" id="before" name="before" value="<%=before %>" />
<input type="hidden" id="afterEra" name="afterEra" value="<%=afterEra %>" />
<input type="hidden" id="beforeEra" name="beforeEra" value="<%=beforeEra %>" />
<input type="hidden" name="query" id="query-1" value="" />
<input type="hidden" name="offset" id="offset" value="0" />
<input type="hidden" id="nofrags" name="nofrags" value="<%=nofrags %>" />
<input type="hidden" id="imgFirst" name="imgFirst" value="<%=imgFirst %>" />
<input type="hidden" id="transFirst" name="transFirst" value="<%=transFirst %>" />
<input type="hidden" name="submitted" id="submitted" value="X" />
</form>
<%
if (submitted){ 
    int offset = 0;
    String startRecordS = request.getParameter("offset");
    if (startRecordS != null){
        startRecordS = startRecordS.trim();
        try{
            offset = Integer.parseInt(startRecordS);
        }
        catch(Throwable t){}
    }
    if( numHits == 0){
%>
<h1>No results for <%=getQueryDesc(terms,ops,slops) %></h1>
<%} else{
      int pageHits = Math.min(ScriptSearch.PAGE_SIZE,numHits-offset);
 %>
<h1>Hits <%=offset+1%> to <%=Math.min(offset +ScriptSearch.PAGE_SIZE,numHits) %> of <%=numHits %> for <%=getQueryDesc(terms,ops,slops) %></h1>
<table id="pager">
<tr>
<th>Jump to page:</th>
<%
int currPage = (offset)/ScriptSearch.PAGE_SIZE + 1;
int this10 = (offset / (10*ScriptSearch.PAGE_SIZE)) ;
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

int prevPageStart  = offset - ScriptSearch.PAGE_SIZE;
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

int nextPageStart = offset + ScriptSearch.PAGE_SIZE;
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
} %>
</tr>
</table>
<%
}
%>
<table id="pn-results" class="metadata" rules="groups">
<thead>
<tr>
<th class="rowheader">&nbsp;</th>
<th class="rowheader">&nbsp;</th>
<th class="rowheader">Publication</th>
<th class="rowheader">Title</th>
<th class="rowheader">Date</th>
<th class="rowheader">Provenance</th>
</tr>
</thead>
<tbody>