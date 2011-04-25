<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" exclude-result-prefixes="xs tei" version="2.0">
  <xsl:output omit-xml-declaration="yes"/>
  <xsl:param name="DDB-root">/data/papyri.info/idp.data/DDB_EpiDoc_XML</xsl:param>

  <xsl:template match="/tei:TEI">
    <xsl:variable name="id">http://papyri.info/apis/<xsl:value-of
        select="//tei:publicationStmt/tei:idno[@type = 'apisid']/text()"/>/source</xsl:variable>
    <rdf:Description rdf:about="{$id}">
      <dcterms:identifier>papyri.info/apis/<xsl:value-of
          select="//tei:publicationStmt/tei:idno[@type = 'apisid']/text()"/></dcterms:identifier>
      <dcterms:identifier>
        <xsl:value-of select="//tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:idno"/>
      </dcterms:identifier>
      <dcterms:isPartOf>
        <rdf:Description
          rdf:about="http://papyri.info/apis/{substring-before(//tei:publicationStmt/tei:idno[@type = 'apisid'], '.')}">
          <dcterms:isPartOf rdf:resource="http://papyri.info/apis"/>
        </rdf:Description>
      </dcterms:isPartOf>
      <xsl:for-each select="//tei:bibl[@type = 'ddbdp']">
        <xsl:variable name="ddb-seq" select=" tokenize(., ':')"/>
        <xsl:variable name="col" select="replace(lower-case($ddb-seq[1]),'\.$','')"/>
        <xsl:variable name="ddb-doc-uri">
          <xsl:choose>
            <xsl:when test="count($ddb-seq) = 2">
              <xsl:value-of
                select="concat($DDB-root, '/', $col, '/', $col, '.', $ddb-seq[2], '.xml')"/>
            </xsl:when>
            <xsl:when test="count($ddb-seq) = 3">
              <xsl:value-of
                select="concat($DDB-root, '/', $col, '/', $col, '.', $ddb-seq[2], '/', $col, '.', $ddb-seq[2], '.', $ddb-seq[3], '.xml')"
              />
            </xsl:when>
            <xsl:otherwise>/</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:if test="doc-available($ddb-doc-uri)">
          <xsl:variable name="ddb-doc" select="doc($ddb-doc-uri)"/>
          <dcterms:relation>
            <rdf:Description
              rdf:about="http://papyri.info/ddbdp/{$ddb-doc//tei:publicationStmt/tei:idno[@type = 'ddb-hybrid']/text()}/source">
              <dcterms:relation rdf:resource="{$id}"/>
            </rdf:Description>
          </dcterms:relation>
          <xsl:for-each select="tokenize($ddb-doc//tei:titleStmt/tei:title/@n, '\s')">
            <dcterms:relation>
              <rdf:Description rdf:about="http://papyri.info/hgv/{.}/source">
                <dcterms:relation rdf:resource="{$id}"/>
              </rdf:Description>
            </dcterms:relation>
          </xsl:for-each>
        </xsl:if>
      </xsl:for-each>
    </rdf:Description>
  </xsl:template>

</xsl:stylesheet>
