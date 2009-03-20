<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.index.LuceneIndex,info.papyri.metadata.CoreMetadataFields,info.papyri.navigator.portlet.*,org.apache.lucene.document.*,org.apache.lucene.index.*,org.apache.lucene.search.*,java.util.*,javax.portlet.*" %>
<%
String title = "Lucene Indexing Errors";
%>
<html>
<head>
<title><%=title%>
</head>
<body>
<table>
<thead>
<caption><%=title%>(HGV)
</caption>
</thead>
<%
    IndexSearcher s = LuceneIndex.SEARCH_HGV;
    Hits hits = s.search(new TermQuery(new Term(CoreMetadataFields.HAS_ERROR,CoreMetadataFields.SORTABLE_YES_VALUE)));
    Iterator<Hit> hitsIter = hits.iterator();
    while (hitsIter.hasNext()){
        Document doc = hitsIter.next().getDocument();
%>
<tbody>
<tr><th colspan="2"><%=doc.get(CoreMetadataFields.DOC_ID)%></th></tr>
<%
String [] errors = doc.getValues(CoreMetadataFields.ERROR);
for (int i = 1; i <= errors.length; i++){
%>
<tr><td><%=i%></td><td><%=errors[i-1]%></td></tr>
<%
}
%>
</tbody>
<%
}
%>
</table>
<table>
<thead>
<caption><%=title%>(APIS)
</caption>
</thead>
<%
    s = LuceneIndex.SEARCH_COL;
    hits = s.search(new TermQuery(new Term(CoreMetadataFields.HAS_ERROR,CoreMetadataFields.SORTABLE_YES_VALUE)));
    hitsIter = hits.iterator();
    while (hitsIter.hasNext()){
        Document doc = hitsIter.next().getDocument();
%>
<tbody>
<tr><th colspan="2"><%=doc.get(CoreMetadataFields.DOC_ID) %></th></tr>
<% String [] errors = doc.getValues(CoreMetadataFields.ERROR);
for (int i = 1; i <= errors.length; i++){%>
<tr><td><%=i %></td><td><%=errors[i-1] %></td></tr>
<%} %>
</tbody>
<% } %>
</table>
</body>
</html>
