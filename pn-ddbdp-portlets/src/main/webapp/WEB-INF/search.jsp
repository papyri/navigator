<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/html; charset=UTF-8" import="java.util.*,info.papyri.ddbdp.servlet.*" %><%!
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
        result.append(" within ");
        result.append(slops[0]);
    }
    if(terms.length > 2 && !"".equals(terms[2])){
        result.append(' ');
        result.append(ops[1]);
        result.append(' ');
        result.append('"');
        result.append(terms[2]);
        result.append(" within ");
        result.append(slops[1]);
    }
    return result.toString();
}
%><html>
<head>
<title>TERM SEARCH</title>
<script language="javascript" type="text/javascript">
var firstByteMark = [ 0x00,0x00,0xC0,0xE0,0xF0,0xF8,0xFC ];
var byteMask = 0xBF;
var byteMark = 0x80;
<% boolean debug = "on".equals(request.getParameter("debug")); %>
function clearForm(form){
    for(var i=0;i<form.elements.length;i++){
        if(form.elements[i].checked) form.elements[i].checked = false;
        else if(form.elements[i].type == 'text') form.elements[i].value = "";
    }
}

function help(id){
var beta = (id == 'help-beta');
var word = (id == 'help-word');
var phrase = (id == 'help-phrase');
document.getElementById('help-beta').style.visibility = (beta)?'visible':'hidden';
document.getElementById('help-word').style.visibility = (word)?'visible':'hidden';
document.getElementById('help-phrase').style.visibility = (phrase)?'visible':'hidden';
}

function UTF16toUTF8Bytes(u16){
    var bytes = new Array();
    if (u16 < 128){
        bytes.length = 1;
    } else if (u16 < 2048){
        bytes.length = 2;
    } else { // presuming max js charCode of 65535
        bytes.length = 3;
    }
    switch (bytes.length){
        case 3:
            bytes[2] = ((u16 | byteMark) & byteMask);
            u16 >>= 6;
        case 2:
            bytes[1] = ((u16 | byteMark) & byteMask);
            u16 >>= 6;
        case 1:
            bytes[0] = (u16 | firstByteMark[bytes.length]);
    }
    return bytes;
}
function encode(input){
    var output = new Array();
    var inputArray = input.split(/\s+/);
    for(var i=0;i<inputArray.length;i++){
        var term = '';
        for(var j=0;j<inputArray[i].length;j++){
            var u16 = inputArray[i].charCodeAt(j);
            if (u16 < 128){
                term += inputArray[i].charAt(j);
                continue;
            }
            var utf8bytes = UTF16toUTF8Bytes(u16);
            for(var k=0;k<utf8bytes.length;k++){
                if(utf8bytes[k] < 16){
                    term += "%0";
                } else {
                   term += "%";
                }
                term += utf8bytes[k].toString(16);
            }
        }
        output[i] = term;
    }
    return output;
}

function setQueries(){
    var debug = document.getElementById('debug') && document.getElementById('debug').checked;
    var form = document.getElementById("query-form");
    var queries = new Array();
    var rels = new Array();
    var slops = new Array(); 
    var termctr = 0;
    for(var i=0;i<form.elements.length;i++){
        if(form.elements[i].name != "queryterm") continue;
        else termctr++;

        if(form.elements[i].value == '') continue;

        var input = form.elements[i].value;
        var out = input.split(/\s+/); // encode(input);
        var query = "";
        if (out.length > 1){
            var prox = " prox/unit=word/distance<=1/ordered";
            var prefix = 'cql.keywords=';
            if (form.beta.checked) prefix += "/locale=grc.beta";
            prefix += (form.caps.checked)?"/ignoreCapitals":"/respectCapitals";
            prefix += (form.marks.checked)?"/ignoreAccents":"/respectAccents";
            
            query = "(";
            query += (prefix + ' "' + out[0].replace(/[#]/g,'^') + '"');
            for (var j = 1; j < out.length; j++){
                query += ( prox + ' ' + prefix + ' "' + out[j].replace(/[#]/g,'^') + '"'); // .replace( 
            }
            query += ")";
        }
        else {
            query = "(cql.keywords=";
            if (form.beta.checked) query += "/locale=grc.beta";
            query += (form.caps.checked)?"/ignoreCapitals":"/respectCapitals";
            query += (form.marks.checked)?"/ignoreAccents":"/respectAccents";
            query += '"' + out[0].replace(/[#]/g,"^") + '")';
        }
        /** query = query.replace(/[=]/g,"%3d");
        query = query.replace(/[/]/g,"%2f"); */
        if(debug) window.alert(query);
        
        queries[queries.length] = query;
        
        if (termctr > 1){
            var bID = "boolean-" + (termctr - 1);
            rels[rels.length] = document.getElementById(bID).value;
            var sID = "slop-" + (termctr - 1);
            slops[slops.length] = (document.getElementById(sID))?parseInt(document.getElementById(sID).value):1; 
        }
    }
    var querystrings = new Array();
    querystrings[0] = queries[0];
    var not = 0;
    var and = 0;
    var then = 0;
    if(rels.length > 0){
        if(rels[0] == 'NOT') not &=1;
        if(rels[0] == 'AND') and &=1;
        if(rels[0] == 'THEN') then &=1;
        if(rels.length > 1){
            if(rels[1] == 'NOT') not &= 2;
            if(rels[1] == 'AND') and &= 2;
            if(rels[1] == 'THEN') then &= 2;
        }
    }
    switch(then){
      case 3:
         var prox = "((" + querystrings[0] +  " prox/unit=word/distance<=" + slops[0] + " " + queries[1] + ") prox/unit=word/distance<=" + slops[1] + " " + queries[2] + ")";
         querystrings[0] = prox;
      case 2:
         var prox = "(" + querystrings[0] +  " prox/unit=word/distance<=" + slops[1] + " " + queries[2] + ")";
         querystrings[1] = prox;
         break;
      case 1:
         var prox = "(" + querystrings[0] +  " prox/unit=word/distance<=" + slops[0] + " " + queries[1] + ")";
         querystrings[0] = "(" + querystrings[0] + " NOT " + prox + ")";
      default:
    }
    switch(not){
      case 3:
         var prox = "(" + querystrings[0] +  " prox/unit=word/distance<=" + slops[0] + " " + queries[1] + ")";
         querystrings[0] = "(" + querystrings[0] + " NOT " + prox + ")";
      case 2:
         var prox = "(" + querystrings[0] +  " prox/unit=word/distance<=" + slops[1] + " " + queries[2] + ")";
         querystrings[1] = "(" + querystrings[0] + " NOT " + prox + ")";
         break;
      case 1:
         var prox = "(" + querystrings[0] +  " prox/unit=word/distance<=" + slops[0] + " " + queries[1] + ")";
         querystrings[0] = "(" + querystrings[0] + " NOT " + prox + ")";
      default:
    }
    if(queries.length > 1){ // do first subclause
    }
    
    if(queries.length > 1){
        for(var i=1;i<queries.length;i++){
            if(rels[i-1] == 'THEN'){
                for(ix = 0; ix< querystrings.length; ix++){
                    var prox = "(" + querystrings[ix] + " prox/unit=word/distance<=" + slops[i-1] +  "/ordered " + queries[i] + ")";
                    querystrings[ix] = prox;
                }
            }
            else if(rels[i-1] == 'AND'){
                var offset = querystrings.length;
                var expand = new Array(2*offset);
                for(ix = 0; ix< querystrings.length; ix++){
                    var qA = "(" + querystrings[ix] + " prox/unit=word/distance<=" + slops[i-1] + " " + queries[i] + ")";
                    var qB = "(" + queries[i] + " prox/unit=word/distance<=" + slops[i-1] + " " + querystrings[ix] + ")";
                    expand[ix] = qA;
                    expand[ix + offset] = qB;
                }
                querystrings = expand;
            }
            else if(rels[i-1] == 'NOT'){
                for(ix = 0; ix< querystrings.length; ix++){
                    var prox = "(" + querystrings[ix] +  " prox/unit=word/distance<=" + slops[i-1] + " " + queries[i] + ")";
                    querystrings[ix] = "(" + querystrings[ix] + " NOT " + prox + ")";
                }
            }
            else if(rels[i-1] == 'OR'){
               var offset = querystrings.length;
                for(ix = 0; ix< querystrings.length; ix++){
                    querystrings[ix] = "(" + querystrings[ix] + " OR " + queries[i] + ")";
                }
            }
            if (debug) window.alert(querystrings.join());
        }
    }

    var qbuf = "(" + querystrings[0];
    for(var i = 1; i < querystrings.length;i++){
        qbuf += " OR " + querystrings[i];
    }
    qbuf += ")";    
    document.getElementById('query-1').value = qbuf;
    return true;
}
</script>
<style type="text/css">
    DIV#help{
        position:absolute;
        top:5%;
        left:50%;
        width:20%;
    }
    DIV.help{
        visibility:hidden;
        background-color:#CCC;
        border:thin black solid;
        color:black;
        position:absolute;
        top:0px;
        left:0px;
        padding:15px;
    }
    DIV#pager{
    color:gray;
    }
    DIV#pager A{
    color:blue;
    text-decoration:none;
    }
    DIV#pager TD.current{
    color:black;
    font-weight:bold;
    }
    .lineNumber {
    font-size:0.8em;
    font-style:italic;
    font-weight:normal;
    }
</style>
</head>
<%
String [] terms = request.getParameterValues("queryterm");

if (terms != null){
    for(int i=0;i<terms.length;i++){
        terms[i] = Sru.getSafeUTF8(terms[i].replaceAll("[<>\\{\\}]",""));
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
    slops[i] = Sru.getSafeUTF8(slops[i].replaceAll("[<>\\{\\}]",""));
}
String numHitsS = (String)request.getAttribute(Sru.NUM_RECS_ATTR);
int numHits = (numHitsS != null)?Integer.parseInt(numHitsS):0;
int numPages = numHits / Search.PAGE_SIZE;
if(numHits % Search.PAGE_SIZE != 0) numPages++;
String [] fnames = (String[])request.getAttribute(Sru.FILENAMES_ARRAY_ATTR);
String [] fragments = (String[])request.getAttribute(Sru.FRAGMENTS_ARRAY_ATTR);
String marks = ("on".equals(request.getParameter("marks")))?"checked=\"checked\"":"";
String caps = ("on".equals(request.getParameter("caps")))?"checked=\"checked\"":"";
String beta = ("on".equals(request.getParameter("beta")))?"checked=\"checked\"":"";
String frags = ("on".equals(request.getParameter("frags")))?"checked=\"checked\"":"";
boolean submitted = request.getParameter("submitted") != null; 
boolean doFrags = "on".equals(request.getParameter("frags"));
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
<body>
<form method="GET" action="search" id="query-form" accept-charset="UTF-8" onsubmit="setQueries();">
Click links for help.<br/>
<input type="hidden" name="operation" value="searchRetrieve" /><input type="hidden" name="startRecord" id="startRecord" value="1" />
<input type="checkbox" name="beta" id="beta" <%=beta%> /> input is in <a href="" onclick="help('help-beta');return false;">Beta</a> Code
<br/>
<input type="checkbox" name="caps" id="caps" <%=caps%> /> ignore capitals
<br/>
<input type="checkbox" name="marks" id="marks" <%=marks%> /> ignore diacritics/accents
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
    within: <input name="slop" id="slop-1" type="text" value="<%=(slops.length > 0)?slops[0]:""%>"/> words [default 1]
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
    within: <input name="slop" id="slop-2" type="text" value="<%=(slops.length > 1)?slops[1]:""%>"/> words [default 1]
    </div>
</div>
<input type="hidden" name="query" id="query-1" value="" />
<input type="checkbox" name="frags" id="frags" <%=frags%> /> show highlighted fragments (slower)
<input type="hidden" name="submitted" id="submitted" value="X" />
<br/>
<input type="submit" />
<input type="button" value="Clear Form" onclick="clearForm(this.form)" />
<% if(debug){ %>
<input type="checkbox" name="debug" id="debug" checked="checked" /> debug CQL
<%} %>
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
    int startRec = 1;
    String startRecordS = request.getParameter("startRecord");
    if (startRecordS != null){
        startRecordS = startRecordS.trim();
        try{
            startRec = Integer.parseInt(startRecordS);
        }
        catch(Throwable t){}
    }
    if( fnames.length == 0){
%>
<h1>No results for <%=getQueryDesc(terms,ops,slops) %></h1>
<%} else{ %>
<h1>Hits <%=startRec%> to <%=(startRec+fnames.length - 1)%> of <%=numHitsS %> for <%=getQueryDesc(terms,ops,slops) %></h1>
<table id="pager">
<tr>
<th>Jump to page:</th>
<%
int currPage = (startRec)/Search.PAGE_SIZE;
int this10 = (startRec / (10*Search.PAGE_SIZE)) ;
if(numPages > 10){
    int last10 =  this10 - 1;
    if(last10 > -1){
        String link = "<td><a href=\"\" onclick=\"document.getElementById('startRecord').value='" + (last10 * Search.PAGE_SIZE + 1) + "';setQueries();document.getElementById('query-form').submit();return false;\">[prev 10]</a></td>";
        out.print(link);
    }
    else{
        out.print("<td>[prev 10]</td>");
    }
}

int prevPageStart  = startRec - Search.PAGE_SIZE;
if(prevPageStart > 0){
    String link = "<td><a href=\"\" onclick=\"document.getElementById('startRecord').value='" + (prevPageStart) + "';setQueries();document.getElementById('query-form').submit();return false;\">&lt;&lt;</a></td>";
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
    int pageStartRec = (Search.PAGE_SIZE*i) + 1;
    
    String link = "<td><a href=\"\" onclick=\"document.getElementById('startRecord').value='" + pageStartRec + "';setQueries();document.getElementById('query-form').submit();return false;\">" + (i+1) + "</a></td>";
    out.print(link);
}

int nextPageStart = startRec + Search.PAGE_SIZE;
if(nextPageStart > 0 && nextPageStart < numHits){
    String link = "<td><a href=\"\" onclick=\"document.getElementById('startRecord').value='" + (nextPageStart) + "';setQueries();document.getElementById('query-form').submit();return false;\">&gt;&gt;</a></td>";
    out.print(link);
}
else{
    out.print("<td>&gt;&gt;</td>");
}

if(lastPage < numPages){
    int next = (this10 + 1) * (10 * Search.PAGE_SIZE) + 1;
    String link = "<td><a href=\"\" onclick=\"document.getElementById('startRecord').value='" + next + "';setQueries();document.getElementById('query-form').submit();return false;\">[next 10]</a></td>";
    out.print(link);
}
%>
</tr>
</table>
<table>
<%
    for(int i=0;i<fnames.length;i++){
        out.println("<tr><td style=\"font-weight:bold;font-size:1.1em;\"><a href=\"doc?name=" + fnames[i] + "\" target=\"_new\">" + fnames[i] + "</a></td></tr>");
        if(doFrags){
            out.println("<tr><td>" + fragments[i] + "</td></tr>");
        }
    }
}
    }%>
    </table>
    <% 
          if (request.getAttribute(Sru.QUERY_TIMER_ATTR) != null){
              Long start = (Long)request.getAttribute(Sru.QUERY_TIMER_ATTR);
              long millis = System.currentTimeMillis() - start.longValue();
              out.println("<p>Query executed in " + millis + " ms</p>");
          }
    %>
</body>
</html>