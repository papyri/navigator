<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/html; charset=UTF-8" import="java.util.*,javax.portlet.RenderRequest,info.papyri.numbers.portlet.Numbers" %><%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<%
String apisPrefix = request.getParameter("apisPrefix");
if(apisPrefix==null) apisPrefix = "*";
String ddbPrefix = request.getParameter("ddbPrefix");
if(ddbPrefix==null) ddbPrefix = "*";
String hgvPrefix = request.getParameter("hgvPrefix");
if(hgvPrefix==null) hgvPrefix = "*";
String tmPrefix = request.getParameter("tmPrefix");
if(tmPrefix==null) tmPrefix = "*";
RenderRequest rReq = (RenderRequest)renderRequest;
String prefix = (String)rReq.getAttribute(Numbers.ATTR_PREFIX);
if(prefix==null)prefix="";
String from = (String)rReq.getAttribute(Numbers.ATTR_FROM);
Numbers portlet = (Numbers)rReq.getAttribute(Numbers.ATTR_PORTLET);
Iterator<String> apis = portlet.getApisCollections();
Iterator<String> hgv = portlet.getHGVCollections();
Iterator<String> ddb = portlet.getDDbCollections();
%>
<script type="text/javascript">
function getAPIS(collection, doc){
if(collection == null || collection.match(/^\s*$/)) collection = '*';
if(doc == null || doc.match(/^\s*$/)) doc = '*';
return 'oai:papyri.info:identifiers:apis:' + collection + ':' + doc;
}
function getHGV(collection, volume, doc){
if(collection == null || collection.match(/^\s*$/)) collection = '*';
if(volume == null || volume.match(/^\s*$/)) volume = '*';
if(doc == null || doc.match(/^\s*$/)) doc = '*';
return 'oai:papyri.info:identifiers:hgv:' + collection + ':' + volume + ':' + doc;
}
function getDDB(collection,volume, doc){
if(collection == null || collection.match(/^\s*$/)) collection = '*';
if(volume == null || volume.match(/^\s*$/)) volume = '*';
if(doc == null || doc.match(/^\s*$/)) doc = '*';
return 'oai:papyri.info:identifiers:ddbdp:' + collection + ':' + volume + ':' + doc;
}
</script>
<div>
<form id="apisNumber" action="">
<input type="hidden"  name="prefix" value="oai:papyri.info:identifiers:apis:" />
APIS Collection: <select  name="apisPrefix" value="<%=apisPrefix %>" >
<option value="">[Select]</option>
<%while(apis.hasNext()){
    String next = apis.next();
    String selected = (apisPrefix.equals(next))?"selected=\"selected\"":"";
    %>
    <option value="<%=next %>" <%=selected %>><%=next %></option>
<% } %>
</select>
<input type="submit" value="Go" onclick="this.form.prefix.value =getAPIS(this.form.apisPrefix.value,'*')" />
</form>
<form id="hgvNumber" action="">
<input type="hidden"  name="prefix" value="oai:papyri.info:identifiers:apis:" />
HGV Series: <select  name="hgvPrefix" value="<%=hgvPrefix %>" >
<option value="">[Select]</option>
<%while(hgv.hasNext()){
    String next = hgv.next();
    String selected = (hgvPrefix.equals(next))?"selected=\"selected\"":"";
    %>
    <option value="<%=next %>" <%=selected %>><%=next %></option>
<% } %>
</select>
<input type="submit" value="Go" onclick="this.form.prefix.value =getHGV(this.form.hgvPrefix.value,'*','*')" />
</form>
<form id="ddbNumber" action="">
<input type="hidden"  name="prefix" value="oai:papyri.info:identifiers:apis:ddbdp:*" />
DDbDP Series: <select  name="ddbPrefix" value="<%=ddbPrefix %>" >
<option value="">[Select]</option>
<%while(ddb.hasNext()){
    String next = ddb.next();
    String selected = (ddbPrefix.equals(next))?"selected=\"selected\"":"";
    %>
    <option value="<%=next %>" <%=selected %>><%=next %></option>
<% } %>
</select>
<input type="submit" value="Go" onclick="this.form.prefix.value =getDDB(this.form.ddbPrefix.value,'*','*')" />
</form>
<form id="tmNumber" action="">
<input type="hidden"  name="prefix" value="oai:papyri.info:identifiers:trismegistos:" />
TM number: <input type="text"  name="tmPrefix" value="<%=tmPrefix %>" />
<input type="submit" value="Go" onclick="if(this.form.tmPrefix.value > ' ')this.form.prefix.value=this.form.prefix.value+this.form.tmPrefix.value; else this.form.prefix.value=this.form.prefix.value+'*'" />
</form>

<h2 class="apis-portal-title"><%=prefix %></h2>
<table class="metadata">