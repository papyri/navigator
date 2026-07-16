<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:pi="http://papyri.info/ns"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:dct="http://purl.org/dc/terms/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
  exclude-result-prefixes="xs tei" 
  expand-text="yes"
  xpath-default-namespace="http://www.tei-c.org/ns/1.0"
  version="3.0">

  <xsl:output omit-xml-declaration="yes" indent="yes" />
  <xsl:param name="root">/data/papyri.info/idp.data</xsl:param> <!-- use idp.data root directory (the one in which you will find DDB_EpiDoc_XML, HGV_meta_EpiDoc etc.) -->
  <xsl:param name="domain" select="'papyri.info'"/> <!-- for the DCLP project overwrite with 'dclp.atlantides.org' -->

  <xsl:template match="/tei:TEI">
    <xsl:variable name="tm" select="normalize-space(//tei:publicationStmt/tei:idno[lower-case(@type)='tm'])"/>
    <xsl:variable name="ldab" select="normalize-space(//tei:publicationStmt/tei:idno[lower-case(@type)='ldab'])"/>
    <xsl:variable name="dclp-hybrid"
      select="//tei:publicationStmt/tei:idno[lower-case(@type)='dclp-hybrid'][1]"/>
    <xsl:variable name="dclp">
      <xsl:choose>
        <xsl:when test="contains($dclp-hybrid, ';')"><xsl:value-of select="$dclp-hybrid"/></xsl:when>
        <xsl:otherwise>tm;;<xsl:value-of select="$tm"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="id">https://{$domain}/current/{$tm}/source</xsl:variable>

    <rdf:Description rdf:about="{$id}">
      <dct:identifier>
        <xsl:value-of select="$domain"/>
        <xsl:text>/current/</xsl:text>
        <xsl:value-of select="$tm"/>
      </dct:identifier>
      <xsl:if test="string($ldab)">
        <dct:identifier>
          <xsl:value-of select="$domain"/>
          <xsl:text>/ldab/</xsl:text>
          <xsl:value-of select="$ldab"/>
        </dct:identifier>
      </xsl:if>
     
      <dct:isPartOf>
        <rdf:Description rdf:about="https://papyri.info/current/group/{floor(number($tm) div 1000)}">
          <dct:isPartOf rdf:resource="https://papyri.info/current"/>
        </rdf:Description>
      </dct:isPartOf>
      
      <xsl:for-each select="$dclp">
        <xsl:variable name="dclpId" select="pi:makeURI(replace(., ';+', '/'))"/>
        <dct:source rdf:resource="https://{$domain}/editions/{$dclpId}/source"/>     
        <dct:relation rdf:resource="https://{$domain}/dclp/{$tm}/source"/>
      </xsl:for-each>
      
      <dct:identifier>
        <xsl:value-of select="//tei:publicationStmt/tei:idno[@type = 'filename']/text()"/>
      </dct:identifier>
      
      <xsl:for-each select="//tei:idno[lower-case(@type)='ldab']">
        <xsl:for-each select="tokenize(., '\s')">
          <dct:relation rdf:resource="http://www.trismegistos.org/ldab/text.php?quick={.}"/>
        </xsl:for-each>
      </xsl:for-each>
      
      <xsl:for-each select="$tm">
        <xsl:variable name="page">https://{$domain}/current/{$tm}</xsl:variable>
        <foaf:page>
          <rdf:Description rdf:about="{$page}">
            <foaf:topic rdf:resource="{$id}"/>
          </rdf:Description>
        </foaf:page>
      </xsl:for-each>

    </rdf:Description>
  </xsl:template>
  
  <xsl:function name="pi:makeUnicodeSafeUri">
    <xsl:param name="in"/>
    <xsl:value-of select="replace(normalize-unicode($in, 'NFD'), '[^;.,a-zA-Z0-9]', '')"/>
  </xsl:function>
  
  <xsl:function name="pi:makeURI">
    <xsl:param name="filename"/>
    <xsl:for-each select="tokenize($filename, '/')">
      <xsl:text>{encode-for-uri(.)}</xsl:text><xsl:if test="position() != last()">/</xsl:if>
    </xsl:for-each>
  </xsl:function>
</xsl:stylesheet>
