<%@ page language="java"%><%@ page session="false" pageEncoding="UTF-8" contentType="text/xml; charset=UTF-8" import="info.papyri.numbers.servlet.*" %><?xml version="1.0" encoding="UTF-8"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:ore="http://www.openarchives.org/ore/terms/"
         xmlns:dcterms="http://purl.org/dc/terms/"
         xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
                  <rdf:Description rdf:about="<%=request.getAttribute(Numbers.ATTR_BASE_URI) %>#aggregation">
             <ore:describes rdf:resource="<%=request.getAttribute(Numbers.ATTR_BASE_URI) %>" />
             <dcterms:title><%=request.getAttribute(Numbers.ATTR_TITLE) %></dcterms:title>
         