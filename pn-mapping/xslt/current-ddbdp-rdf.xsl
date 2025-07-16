<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dct="http://purl.org/dc/terms/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
  xmlns:pi="http://papyri.info/ns"
  exclude-result-prefixes="xs tei" 
  expand-text="yes"
  version="3.0">
  
  <xsl:output omit-xml-declaration="yes"/>
  <xsl:mode on-no-match="shallow-skip"/>
  <xsl:param name="root"/>

  <xsl:template match="/tei:TEI">
    
    <xsl:variable name="id">https://papyri.info/current/{//tei:publicationStmt/tei:idno[@type='filename']}/source</xsl:variable>
    <xsl:variable name="page">https://papyri.info/current/{//tei:publicationStmt/tei:idno[@type='filename']}</xsl:variable>
    
    <rdf:Description rdf:about="{$id}">
      <dct:identifier>papyri.info/current/{//tei:publicationStmt/tei:idno[@type = 'filename']}</dct:identifier>
      <xsl:for-each select="//tei:body/tei:head/tei:ref[@target]">
        <dct:source rdf:resource="{@target}/source"/>
      </xsl:for-each>
      <dct:identifier>
        <xsl:value-of select="//tei:publicationStmt/tei:idno[@type = 'filename']/text()"/>
      </dct:identifier>
      <dct:relation rdf:resource="https://papyri.info/ddbdp/{pi:makeURI//tei:publicationStmt/tei:idno[@type='ddb-hybrid']}/source"/>
      <xsl:for-each select="//tei:idno[@type = 'HGV']">
        <xsl:for-each select="tokenize(., '\s')">
          <xsl:variable name="dir" select="ceiling(number(replace(., '[a-z]', '')) div 1000)"/>
          <xsl:if
            test="doc-available(concat('file://', $root, '/HGV_meta_EpiDoc/HGV', $dir, '/', ., '.xml'))">
            <dct:relation rdf:resource="https://papyri.info/hgv/{.}/source"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:for-each select="//tei:idno[@type = 'TM']">
        <xsl:for-each select="tokenize(., '\s')">
          <dct:relation rdf:resource="https://www.trismegistos.org/text/{.}"/>          
        </xsl:for-each>
      </xsl:for-each>
      <foaf:page>
        <rdf:Description rdf:about="{$page}">
          <foaf:topic rdf:resource="{$id}"/>
        </rdf:Description>
      </foaf:page>
    </rdf:Description>
  </xsl:template>
  
  <xsl:function name="pi:makeUnicodeSafeUri">
    <xsl:param name="in"/>
    <xsl:value-of select="replace(normalize-unicode($in, 'NFD'), '[^;.,a-zA-Z0-9/]', '')"/>
  </xsl:function>
  
  <xsl:function name="pi:makeURI">
    <xsl:param name="filename"/>
    <xsl:for-each select="tokenize($filename, ';')">
      <xsl:text>{encode-for-uri(.)}</xsl:text><xsl:if test="position() != last()">;</xsl:if>
    </xsl:for-each>
  </xsl:function>
</xsl:stylesheet>
