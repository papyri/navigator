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
String [] fnames = (String[])rReq.getAttribute(ScriptSearch.FILENAMES_ARRAY_ATTR);
String [] fragments = (String[])rReq.getAttribute(ScriptSearch.FRAGMENTS_ARRAY_ATTR);
String [] ids = (String[])rReq.getAttribute(ScriptSearch.DDB_ID_ARRAY_ATTR);
String marks = ("on".equals(request.getParameter("marks")))?"checked=\"checked\"":"";
String lemmas = ("on".equals(request.getParameter("lemmas")))?"checked=\"checked\"":"";
String caps = ("on".equals(request.getParameter("caps")))?"checked=\"checked\"":"";
String betaYes = (!"off".equals(request.getParameter("beta")))?"checked=\"checked\"":"";
String betaNo = ("off".equals(request.getParameter("beta")))?"checked=\"checked\"":"";
String frags = ("on".equals(request.getParameter("frags")))?"checked=\"checked\"":"";
String imgFirst = ("on".equals(request.getParameter("imgFirst")))?"checked=\"checked\"":"";
String transFirst = ("on".equals(request.getParameter("transFirst")))?"checked=\"checked\"":"";
String apis = request.getParameter("apis");
if(apis == null || (apis=apis.trim()).equals("")) apis = "";
String pubSeries = request.getParameter("pubSeries");
if(pubSeries == null || (pubSeries=pubSeries.trim()).equals("")) pubSeries = "";
String pubVol = request.getParameter("pubVol");
if(pubVol == null || (pubVol=pubVol.trim()).equals("")) pubVol = "";
boolean submitted = request.getParameter("submitted") != null; 
boolean doFrags = "on".equals(request.getParameter("frags"));

String beginDate = request.getParameter("beginDate");
if (beginDate == null) beginDate = "";
String beginDateEra = request.getParameter("beginDateEra");
String beginCESelected = "";
String beginBCESelected = "";
if("CE".equals(beginDateEra)) beginCESelected = "selected=\"selected\"";
if("BCE".equals(beginDateEra)) beginBCESelected = "selected=\"selected\"";

String endDate = request.getParameter("endDate");
if (endDate == null) endDate = "";
String endDateEra = request.getParameter("endDateEra");
String endCESelected = "";
String endBCESelected = "";
if("CE".equals(endDateEra)) endCESelected = "selected=\"selected\"";
if("BCE".equals(endDateEra)) endBCESelected = "selected=\"selected\"";


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
%>
<form method="GET" action="" id="query-form" accept-charset="UTF-8" onsubmit="setQueries();">
Click links for help.<br/>
<input type="hidden" name="offset" id="offset" value="0" />
<input type="radio" name="beta" id="betaYes" <%=betaYes%> /> input is in <a href="" onclick="help('help-beta');return false;">Beta</a> Code
<br/>
<input type="radio" name="beta" id="betaNo" <%=betaNo%> /> input is <b>not</b> in <a href="" onclick="help('help-beta');return false;">Beta</a> Code
<br/>
<input type="checkbox" name="caps" id="caps" <%=caps%> /> respect capitalization
<br/>
<input type="checkbox" name="marks" id="marks" <%=marks%> /> respect diacritics/accents
<input type="checkbox" name="lemmas" id="lemmas" <%=lemmas%> onchange="if(this.checked){this.form.caps.disabled=true;this.form.marks.disabled=true;this.form.caps.checked=true;this.form.marks.checked=true;}" /> search lemmatized forms
<br/>
<div id="queryContainer-1">
    <div id="clause-1">
    <a href="" onclick="help('help-word');return false;">word</a> or <a href="" onclick="help('help-phrase');return false;">phrase</a>: <input name="queryterm" id="term-1" type="text" value="<%=terms[0]%>"/>
    </div>
    <div id="subclause-1-1" class="subclause">
    </div>
    <div id="subclause-1-2" class="subclause">
    </div>
    <div id="subclause-1-3" class="subclause">
    </div>
    <div id="subclause-1-4" class="subclause">
    </div>
</div>
<div id="queryContainer-2">
    <div id="clause-2">
    <select name="boolean" id="boolean-1">
        <option <%=then1 %> value="THEN">THEN</option>
        <option <%=and1 %> value="AND">AND</option>
        <option <%=or1 %> value="OR">OR</option>
        <option <%=not1 %> value="NOT">NOT</option>
    </select>
    word or phrase: <input name="queryterm" id="term-2" type="text" value="<%=(terms.length>1)?terms[1]:""%>"/>
    <br/>
    within: <input name="slop" id="slop-1" type="text" value="<%=(slops.length > 0)?slops[0]:""%>"/> words [default 10]
    </div>
</div>
<div id="queryContainer-3">
    <div id="clause-3">
    <select name="boolean" id="boolean-2">
        <option <%=then2 %> value="THEN">THEN</option>
        <option <%=and2 %> value="AND">AND</option>
        <option <%=or2 %> value="OR">OR</option>
        <option <%=not2 %> value="NOT">NOT</option>
    </select>
    word or phrase: <input name="queryterm" id="term-3" type="text" value="<%=(terms.length>2)?terms[2]:""%>"/>
    <br/>
    within: <input name="slop" id="slop-2" type="text" value="<%=(slops.length > 1)?slops[1]:""%>"/> words [default 10]
    </div>
</div>
<div>
<label for="apis">APIS Collection:</label>
<select name="apis" id="apis">
<option value="">[Select APIS Collection]</option>
<%
Iterator<String> collections = IndexEventPropagator.getAPIS();
while(collections.hasNext()){
    String collection = collections.next();
    String selected = collection.equals(apis)?"selected=\"selected\"":"";
%>
<option value="<%=collection %>" <%=selected %>><%=collection %></option>
<%} %>
</select>
<br/>
<label for="pubSeries">Publication series:</label>
<select name="pubSeries" id="pubSeries">
<option value="">[Select Publication Series]</option>
<%
Iterator<String> serials = IndexEventPropagator.getSerials();
while(serials.hasNext()){
    String series = serials.next();
    String selected = series.equals(pubSeries)?"selected=\"selected\"":"";
%>
<option value="<%=series %>" <%=selected %>><%=series %></option>
<%} %>
</select>
<input type="text" size="5" id="pubVol" name="pubVol" value="<%=pubVol %>" />
</div>
<div>
			<strong>Date</strong>
			<label for="beginDate">On or after:</label>
			<input size="5"
				id="beginDate" name="beginDate" class="forminput10" value="<%=beginDate %>" />
			<select	name="beginDateEra">
				<option value="CE" <%=beginCESelected %>>CE</option>
				<option value="BCE" <%=beginBCESelected %>>BCE</option>
			</select>
                  <label for="endDate">On or before:</label>
			<input size="5"
				id="endDate" name="endDate" class="forminput10" value="<%=endDate %>" />
				<select	name="endDateEra">
				<option value="CE" <%=endCESelected %>>CE</option>
				<option value="BCE" <%=endBCESelected %>>BCE</option>
			</select>
		  </div>
<input type="hidden" name="query" id="query-1" value="" />
<input type="checkbox" name="frags" id="frags" <%=frags%> /> show highlighted fragments (slower)<br/>
<input type="checkbox" name="imgFirst" id="imgFirst" <%=imgFirst%> /> Show documents with images first<br/>
<input type="checkbox" name="transFirst" id="transFirst" <%=transFirst%> /> Show documents with translations first<br/>
<input type="hidden" name="submitted" id="submitted" value="X" />
<br/>
<input type="submit" />
<input type="button" value="Clear Form" onclick="clearForm(this.form)" />
</form>
<div id="help">
    <div id="help-beta" class="help"> eta = h; theta = q; xi = c; psi = y; omega = w
    </div>
    <div id="help-word" class="help">word boundaries should be indicated with a carat ('^')
    </div>
    <div id="help-phrase" class="help">a phrase is simply a space-delimited set of word or substring terms
    </div>
</div>
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
    if( fnames.length == 0){
%>
<h1>No results for <%=getQueryDesc(terms,ops,slops) %></h1>
<%} else{ %>
<h1>Hits <%=offset + 1%> to <%=Math.min(offset + ScriptSearch.PAGE_SIZE,numHits)%> of <%=numHits %> for <%=getQueryDesc(terms,ops,slops) %></h1>
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
%>
</tr>
</table>
<table>
<%
    for(int i=0;i<fnames.length;i++){
        out.println("<tr><td style=\"font-weight:bold;font-size:1.1em;\"><a href=\"portal/text.psml?controlName=" + ids[i] + "\" >" + fnames[i] + "</a></td></tr>");
        if(doFrags){
            out.println("<tr><td>" + fragments[i] + "</td></tr>");
        }
    }
}
    }%>
    </table>
    <% 
          if (rReq.getAttribute(ScriptSearch.QUERY_TIMER_ATTR) != null){
              Long start = (Long)rReq.getAttribute(ScriptSearch.QUERY_TIMER_ATTR);
              long millis = System.currentTimeMillis() - start.longValue();
              out.println("<p>Query executed in " + millis + " ms</p>");
          }
    %>
</div>