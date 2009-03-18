<%@ page language="java"%><%@ page session="false" contentType="text/html" import="info.papyri.metadata.*,org.apache.lucene.document.*,org.apache.lucene.search.*,java.util.*" %><%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%><%@page import="info.papyri.numbers.servlet.Numbers"%>
<portlet:defineObjects/><?xml version="1.0" encoding="utf-8"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:dc="http://purl.org/dc/elements/1.1/" >
         <rdf:Container  rdf:ID="http://papyri.info/numbers">
  <rdf:Description rdf:about="http://papyri.info/numbers"
		   dc:title="Digital Papyrological Identifiers">
		   <dc:Source><%=request.getAttribute(Numbers.ATTR_BASE) %></dc:Source>
  </rdf:Description>
		   
  <id>http://papyri.info/numbers/rem#aggregation</id>
  <link href="http://appdev.cul.columbia.edu:8082/numbers/rem" rel="self" type="application/atom+xml"/>
  <generator uri="http://papyri.info/">papyri.info Digital Papyrology Services</generator> 
  <updated>2008-05-12T18:30:02Z</updated> 
  <category scheme="http://www.openarchives.org/ore/terms/" 
  term="http://www.openarchives.org/ore/terms/Aggregation" label="Aggregation" />
  <entry>
    <id>http://papyri.info/numbers/rem/apis#aggregation</id>
    <link href="http://appdev.cul.columbia.edu:8082/numbers/rem/apis" rel="alternate" type="application/atom+xml" />
    <updated>2008-05-12T18:30:02Z</updated>
    <title>APIS: Advanced Papyrological Information System</title>
    <category scheme="http://www.openarchives.org/ore/terms/" 
    term="http://www.openarchives.org/ore/terms/Aggregation" label="Aggregation" />
  </entry>
  <entry>
    <id>http://papyri.info/numbers/rem/hgv#aggregation</id>
    <link href="http://appdev.cul.columbia.edu:8082/numbers/rem/hgv" rel="alternate" type="application/atom+xml" />
    <updated>2008-05-12T18:30:02Z</updated>
    <title>HGV: Heidelberger Gesamtverzeichnis Der Griechischen Papyrusurkunden Ägyptens</title>
    <category scheme="http://www.openarchives.org/ore/terms/" 
    term="http://www.openarchives.org/ore/terms/Aggregation" label="Aggregation" />
  </entry>
  <entry>
    <id>http://papyri.info/numbers/rem/ddbdp#aggregation</id>
    <link href="http://appdev.cul.columbia.edu:8082/numbers/rem/ddbdp" rel="alternate" type="application/atom+xml" />
    <updated>2008-05-12T18:30:02Z</updated>
    <title>DDbDP: Duke Databank of Documentary Papyri</title>
    <category scheme="http://www.openarchives.org/ore/terms/" 
    term="http://www.openarchives.org/ore/terms/Aggregation" label="Aggregation" />
  </entry>
  <entry>
    <id>http://papyri.info/numbers/rem/tm#aggregation</id>
    <link href="http://appdev.cul.columbia.edu:8082/numbers/rem/tm" rel="alternate" type="application/atom+xml" />
    <updated>2008-05-12T18:30:02Z</updated>
    <title>TM: Trismegistos Texts Database</title>
    <category scheme="http://www.openarchives.org/ore/terms/" 
    term="http://www.openarchives.org/ore/terms/Aggregation" label="Aggregation" />
  </entry>
</rdf:RDF>

