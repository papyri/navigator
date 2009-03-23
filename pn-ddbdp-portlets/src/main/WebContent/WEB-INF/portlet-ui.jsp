<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/html; charset=UTF-8" import="java.util.*,info.papyri.ddbdp.servlet.IndexEventPropagator,info.papyri.ddbdp.portlet.SearchPortlet,info.papyri.ddbdp.servlet.ScriptSearch,javax.portlet.RenderRequest" %><%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%!
private String getQueryDesc(String [] terms, String [] ops, String [] slops){
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

%>
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
String marks = ("on".equals(request.getParameter("marks")))?"checked=\"checked\"":"";
String lemmas = ("on".equals(request.getParameter("lemmas")))?"checked=\"checked\"":"";
String caps = ("on".equals(request.getParameter("caps")))?"checked=\"checked\"":"";
String betaYes = (!"off".equals(request.getParameter("beta")))?"checked=\"checked\"":"";
String betaNo = ("off".equals(request.getParameter("beta")))?"checked=\"checked\"":"";
String nofrags = ("on".equals(request.getParameter("nofrags")))?"checked=\"checked\"":"";
String imgFirst = ("on".equals(request.getParameter("imgFirst")))?"checked=\"checked\"":"";
String transFirst = ("on".equals(request.getParameter("transFirst")))?"checked=\"checked\"":"";
String apis = request.getParameter("apis");
if(apis == null || (apis=apis.trim()).equals("")) apis = "";
String pubSeries = request.getParameter("pubSeries");
if(pubSeries == null || (pubSeries=pubSeries.trim()).equals("")) pubSeries = "";
String pubVol = request.getParameter("pubVol");
if(pubVol == null || (pubVol=pubVol.trim()).equals("")) pubVol = "";
String place = request.getParameter("place");
if(place==null)place="";
boolean submitted = request.getParameter("submitted") != null; 
boolean doFrags = "on".equals(request.getParameter("frags"));

String after = request.getParameter("after");
if (after == null) after = "";
String afterEra = request.getParameter("afterEra");
String afterCESelected = "";
String afterBCESelected = "";
if("CE".equals(afterEra)) afterCESelected = "selected=\"selected\"";
if("BCE".equals(afterEra)) afterBCESelected = "selected=\"selected\"";

String before = request.getParameter("before");
if (before == null) before = "";
String beforeEra = request.getParameter("beforeEra");
String beforeCESelected = "";
String beforeBCESelected = "";
if("CE".equals(beforeEra)) beforeCESelected = "selected=\"selected\"";
if("BCE".equals(beforeEra)) beforeBCESelected = "selected=\"selected\"";

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
<div class="error"><%=errorMsg %></div>
<div>
	<div id="helpBox" style="float:right;position:top right">
		<div id="wordOrPhrase" class="helpBox">

			<table width="100%" class="helpTitle"><tr><td class="title">Word or Phrase</td>
					<td class="close" align="right">
						<a href="javascript:disableHelp('wordOrPhrase')" class="close">[x] close</a></td></tr></table>
			<p>A <span class="impt">word</span> expresses one word or part of a word.  A <span class="impt">phrase</span> represents consecutive words or consecutive parts of words.</p>

 			<p>To search for an exact phrase, be sure to indicate <strong>word boundaries</strong> at the beginning and end of the word. </p>
<p>Example: <br />Phrase: "kai upo"<br />enter: <strong>^kai^ ^upo^</strong>.</p>
                </div>
                <div id="betaCodeDiv" class="helpBox">
                                <table width="100%" class="helpTitle"><tr><td class="title">Beta Code</td>

<td class="close" align="right">
        <a href="javascript:disableHelp('betaCodeDiv')" class="close">[x] close</a></td></tr></table>
                        <p><span class="impt">Beta code</span> allows you to search for Greek characters using a non-Greek character set.  The following Roman alphabet characters represent the Greek equivalences:</p>
                        <ul><li>eta = h</li><li>theta = q</li><li>xi = c</li><li>chi = x</li><li>psi = y</li><li>omega = w</li></ul>

                        <p><span class="impt">You cannot use beta code to search for Latin terms.</span></p>
                </div>

		<div id="highlight" class="helpBox">
<table width="100%" class="helpTitle"><tr><td class="title">
			Highlighted Parts</td>
		<td class="close" align="right"><a href="javascript:disableHelp('highlight')" class="close">[x] close</a></td></tr></table>
			<p><span class="impt">Highlighted parts</span> show your search results with attention paid to individual search matches.  This allows you to pinpoint your search results right away, but will slow down the search-and-retrieval process.</p>

		</div>
		<div id="lemmatized" class="helpBox">
<table width="100%" class="helpTitle"><tr><td class="title">
			Lemmatized Search</td>
		<td class="close" align="right"><a href="javascript:disableHelp('lemmatized')" class="close">[x] close</a></td></tr></table>
			<p><span class="impt">Lemmatized Search</span> searches for all forms of the entered dictionary headword.</p>

		</div>
		<div id="capitals" class="helpBox">
<table width="100%" class="helpTitle"><tr><td class="title">
			Lemmatized Search</td>
		<td class="close" align="right"><a href="javascript:disableHelp('capitals')" class="close">[x] close</a></td></tr></table>
			<p><span class="impt">Respect Capitalization</span> searches are sensitive to upper- and lower-case distinctions.  This means that Beta code search terms that begin with a capital letter must indicate the capital with a star ('*').</p>

		</div>
	</div> <!-- helpBox -->


<div>
<div>
<form method="GET" action="portal/ddbdp-results.psml" id="query-form" accept-charset="UTF-8" onsubmit="setQueries();">
<table class="pn-form">
<tbody>
<tr class="pn-form-row">
<td class="pn-form-section">Text Search</td>
<td class="pn-options-section">
<div id="queryContainer-1">
    <div id="clause-1">
    <strong>Search for:</strong><a id="wordLink" href="javascript:enableHelp('wordOrPhrase')">word</a> or <a id="phraseLink" href="javascript:enableHelp('wordOrPhrase')">phrase</a>: <input name="queryterm" id="term-1" type="text" value="<%=terms[0]%>"/>
    <input type="submit" class="pn-button" value="Search" />
<input type="button" class="pn-button" value="Clear Form" onclick="clearForm(this.form)" />
    
    </div>
</div>
<div id="queryContainer-2">
    <div id="clause-2">
    <select name="boolean" id="boolean-1">
        <option <%=then1 %> value="THEN">THEN</option>
        <option <%=and1 %> value="AND">AND</option>
        <option <%=not1 %> value="NOT">NOT</option>
    </select>
    word or phrase: <input name="queryterm" id="term-2" type="text" value="<%=(terms.length>1)?terms[1]:""%>"/>
    within: <input name="slop" id="slop-1" type="text" maxlength="3" value="<%=(slops.length > 0)?slops[0]:""%>"/> words
    </div>
</div>
<div id="queryContainer-3">
    <div id="clause-3">
    <select name="boolean" id="boolean-2">
        <option <%=then2 %> value="THEN">THEN</option>
        <option <%=and2 %> value="AND">AND</option>
        <option <%=not2 %> value="NOT">NOT</option>
    </select>
    word or phrase: <input name="queryterm" id="term-3" type="text" value="<%=(terms.length>2)?terms[2]:""%>"/>
    within: <input name="slop" id="slop-2" type="text" maxlength="3" value="<%=(slops.length > 1)?slops[1]:""%>"/> words
    </div>
    <em>Use ^ [SHIFT-6] to anchor a substring to a word boundary. Example: ^kai to search words beginning with kai</em>
</div>
<input type="hidden" name="offset" id="offset" value="0" />
<input type="radio" name="beta" id="betaYes" value="on" <%=betaYes%> /> input is in <a id="betaCodeLink" href="javascript:enableHelp('betaCodeDiv')" style="background:transparent;">beta code</a>
<br/>
<input type="radio" name="beta" id="betaNo" value="off" <%=betaNo%> /> input is <b>not</b> in beta code
<br/>
<input type="checkbox" name="lemmas" id="lemmas" <%=lemmas%> onchange="if(this.checked){this.form.caps.disabled=true;this.form.marks.disabled=true;this.form.caps.checked=this.checked;this.form.marks.checked=this.checked;}" /> <a id="lemmaLink" href="javascript:enableHelp('lemmatized')">lemmatized</a> search
</td>
</tr>
<tr class="pn-form-row">
<td class="pn-form-section">Limit by</td>
<td class="pn-options-section">
<table class="pn-form-subtable">
<tr><td>
<label for="place"><strong>Place name</strong></label></td><td> <input type="text" id="place" name="place" value="<%=place%>" size="15" /> 
</td></tr>
<tr><td>
<strong>Publication</strong> </td><td>
<label for="pubSeries">series:</label>
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
</select><label for="pubVol"> volume: </label>
<input type="text" size="5" id="pubVol" name="pubVol" value="<%=pubVol %>" /></td></tr>
<tr><td>
<label for="apis"><strong>APIS collection</strong></label>
</td><td>
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
</td>
</tr>
<tr><td>
			<strong>Date</strong>
			</td><td>
			<label for="after">On or after:</label>
			<input size="5"
				id="after" name="after" class="forminput10" value="<%=after %>" />
			<select	name="afterEra">
				<option value="CE" <%=afterCESelected %>>CE</option>
				<option value="BCE" <%=afterBCESelected %>>BCE</option>
			</select>
                  <label for="before">On or before:</label>
			<input size="5"
				id="before" name="before" class="forminput10" value="<%=before %>" />
				<select	name="beforeEra">
				<option value="CE" <%=beforeCESelected %>>CE</option>
				<option value="BCE" <%=beforeBCESelected %>>BCE</option>
			</select>
		  </td></tr>
		  </table>
		  </td>
</tr>
<tr class="pn-form-row">
<td class="pn-form-section">Options</td>
<td class="pn-options-section">
<table class="pn-form-subtable">
    <tr><td><input type="checkbox" name="caps"   id="caps" <%=caps%> /> respect <a id="betaCodeLink" href="javascript:enableHelp('capitals')" style="background:transparent;">capitalization</a></td><td>Show records with:</td></tr>
    <tr><td><input type="checkbox" name="marks" id="marks" <%=marks%> /> respect diacritics/accents</td><td><input type="checkbox" name="imgFirst" id="imgFirst" <%=imgFirst%> /> <b>images</b> first</td></tr>
    <tr><td><input type="checkbox" name="nofrags"  id="nofrags" <%=nofrags%> /> hide <a id="highlightLink" href="javascript:enableHelp('highlight')">highlighted fragments</a> (faster)</td><td><input type="checkbox" name="transFirst" id="transFirst" <%=transFirst%> /> <b>translations</b> first</td></tr>
    </table>
</td>
</tr>
</tbody>
</table>

<br/>

<br/>


<input type="hidden" name="query" id="query-1" value="" />


<input type="hidden" name="submitted" id="submitted" value="X" />
</form>
<%
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
    if( numHits == 0){
%>
<h1>No results for <%=getQueryDesc(terms,ops,slops) %></h1>
<%} else{
      int pageHits = Math.min(ScriptSearch.PAGE_SIZE,numHits-startRec);
}
}
 %>
</div>