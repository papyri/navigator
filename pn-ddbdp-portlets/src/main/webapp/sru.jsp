<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/html; charset=UTF-8" import="java.util.*,info.papyri.ddbdp.servlet.*" %><%!
String getQueryDesc(String [] terms, String op){
    StringBuffer result = new StringBuffer();
    if(terms.length > 0 && !"".equals(terms[0])){
        result.append('"');
        result.append(terms[0]);
        result.append('"');
    }
    if(terms.length > 0 && !"".equals(terms[0])){
        result.append(' ');
        result.append(op);
        result.append(' ');
        result.append('"');
        result.append(terms[1]);
        result.append('"');
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
<% boolean debug = request.getParameter("debug") != null; %>
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
    var form = document.getElementById("query-form");
    var querystring = "("; 
    var termctr = 0;
    for(var i=0;i<form.elements.length;i++){
        if(form.elements[i].name != "queryterm" || form.elements[i].value == '') continue;
        else termctr++;
        var input = form.elements[i].value;
        var out = input.split(/\s+/); // encode(input);
        var query = "";
        if (out.length > 1){

            var slop = form.slop[termctr - 1].value;
            if (slop == "" || slop == null) {
                slop = 1;
            } else {
                slop = parseInt(slop);
            }
            var prox = " prox/unit=word/distance<=" + slop;
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
        <%if (debug){ %>
        window.alert(query);
         <%}%>
        if (termctr > 1){
            var bID = "boolean-" + (termctr - 1);
            querystring += ' ' + document.getElementById(bID).value + ' '; 
        }
        querystring+=query;
    }
    querystring += ')';
    document.getElementById('query-1').value = querystring;
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
    terms = new String[]{"",""};
}
String [] slops = request.getParameterValues("slop");

if (slops != null){
    for(int i=0;i<slops.length;i++){
        slops[i] = Sru.getSafeUTF8(slops[i].replaceAll("[<>\\{\\}]",""));
    }
}
else {
    slops = new String[]{"",""};
}

String [] fnames = (String[])request.getAttribute(Sru.FILENAMES_ARRAY_ATTR);
String [] fragments = (String[])request.getAttribute(Sru.FRAGMENTS_ARRAY_ATTR);
String marks = ("on".equals(request.getParameter("marks")))?"checked=\"checked\"":"";
String caps = ("on".equals(request.getParameter("caps")))?"checked=\"checked\"":"";
String beta = ("on".equals(request.getParameter("beta")))?"checked=\"checked\"":"";
String frags = ("on".equals(request.getParameter("frags")))?"checked=\"checked\"":"";
boolean submitted = request.getParameter("submitted") != null; 
boolean doFrags = "on".equals(request.getParameter("frags"));
String bool = request.getParameter("boolean-1");
String and = "AND".equals(bool)?"selected=\"selected\"":"";
String or = "OR".equals(bool)?"selected=\"selected\"":"";
String not = "NOT".equals(bool)?"selected=\"selected\"":"";
%>
<body>
<form method="GET" action="sru" id="submission" target="_new" accept-charset="UTF-8">
<input type="hidden" name="version" value="1.2" />
<input type="hidden" name="operation" value="searchRetrieve" />
<input type="hidden" name="query" id="query-1" value="" />
<input type="text" name="startRecord" value="1" size="4" />
<input type="hidden" name="maximumRecords" value="25" />
</form>
<form method="GET" action="sru" id="query-form" target="_new" accept-charset="UTF-8" onsubmit="setQueries();document.getElementById('submission').submit();return false;">
Click links for help.<br/>
<input type="checkbox" name="beta" id="beta" <%=beta%> /> input is in <a href="" onclick="help('help-beta');return false;">Beta</a> Code
<br/>
<input type="checkbox" name="caps" id="caps" <%=caps%> /> ignore capitals
<br/>
<input type="checkbox" name="marks" id="marks" <%=marks%> /> ignore diacritics/accents
<br/>
<div id="queryContainer-1">
    <div id="clause-1">
    <a href="" onclick="help('help-word');return false;">word</a> or <a href="" onclick="help('help-phrase');return false;">phrase</a>: <input name="queryterm" id="term-1" type="text" value="<%=terms[0]%>"/>
    <br/>
    separated by up to: <input name="slop" id="slop-1" type="text" value="<%=slops[0]%>"/> words [default 1 for exact phrase]
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
    <select name="boolean-1" id="boolean-1">
        <option <%=and %> value="AND">AND</option>
        <option <%=or %> value="OR">OR</option>
        <option <%=not %> value="NOT">NOT</option>
    </select>
    word or phrase: <input name="queryterm" id="term-1" type="text" value="<%=terms[1]%>"/>
    <br/>
    separated by up to: <input name="slop" id="slop-1" type="text" value="<%=slops[1]%>"/> words [default 1 for exact phrase]
    </div>
    <div id="subclause-2-1" class="subclause">
    </div>
    <div id="subclause-2-2" class="subclause">
    </div>
    <div id="subclause-2-3" class="subclause">
    </div>
    <div id="subclause-2-4" class="subclause">
    </div>
</div>

<input type="checkbox" name="frags" id="frags" <%=frags%> /> show highlighted fragments (slower)
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
    if( fnames.length == 0){
%>
<h1>No results for <%=getQueryDesc(terms,bool) %></h1>
<%} else{ %>
<h1><%=fnames.length %> Results for <%=getQueryDesc(terms,bool) %></h1>
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