<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dct="http://purl.org/dc/terms/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" exclude-result-prefixes="xs tei" version="2.0">
  <xsl:output omit-xml-declaration="yes"/>
  <xsl:param name="root"/>

  <xsl:template match="/tei:TEI">
    <xsl:variable name="ddb-seq"
      select="tokenize(normalize-space(//tei:publicationStmt/tei:idno[@type='ddb-hybrid']), ';')"/>
    <xsl:variable name="id">http://papyri.info/ddbdp/<xsl:value-of
        select="replace(normalize-unicode($ddb-seq[1], 'NFD'), '[^.a-z0-9]', '')"/>;<xsl:value-of
        select="$ddb-seq[2]"/>;<xsl:value-of select="encode-for-uri($ddb-seq[3])"
      />/source</xsl:variable>
    <xsl:variable name="page">http://papyri.info/ddbdp/<xsl:value-of
        select="replace(normalize-unicode($ddb-seq[1], 'NFD'), '[^.a-z0-9]', '')"/>;<xsl:value-of
        select="$ddb-seq[2]"/>;<xsl:value-of select="encode-for-uri($ddb-seq[3])"/></xsl:variable>
    <xsl:variable name="perseus-id"
      select="//tei:publicationStmt/tei:idno[@type = 'ddb-perseus-style']"/>
    <rdf:Description rdf:about="{$id}">
      <dct:identifier>papyri.info/ddbdp/<xsl:value-of
          select="//tei:publicationStmt/tei:idno[@type = 'ddb-hybrid']/text()"/></dct:identifier>
      <dct:source>
        <rdf:Description
          rdf:about="http://papyri.info/ddbdp/{//tei:publicationStmt/tei:idno[@type = 'ddb-hybrid']/text()}/work">
          <dct:source
            rdf:resource="http://papyri.info/ddbdp/{//tei:publicationStmt/tei:idno[@type = 'ddb-hybrid']/text()}/original"
          />
        </rdf:Description>
      </dct:source>
      <dct:identifier>
        <xsl:value-of select="//tei:publicationStmt/tei:idno[@type = 'filename']/text()"/>
      </dct:identifier>
      <xsl:for-each
        select="distinct-values(//tei:text/tei:body/tei:head[@xml:lang='en']/tei:ref[@type='reprint-in']/@n)">
        <xsl:for-each select="tokenize(., '\|')">
          <xsl:variable name="ddb-reprint-seq" select="tokenize(., ';')"/>
          <xsl:if test="matches(., '(\w|\.)+;(\d|\.)*;.+')">
            <dct:isReplacedBy
              rdf:resource="http://papyri.info/ddbdp/{$ddb-reprint-seq[1]};{$ddb-reprint-seq[2]};{encode-for-uri($ddb-reprint-seq[3])}/source"
            />
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:for-each select="//tei:body/tei:head[@xml:lang='en']/tei:ref[@type='reprint-from']">
        <xsl:for-each select="tokenize(@n, '\|')">
          <xsl:variable name="ddb-reprint-seq" select="tokenize(., ';')"/>
          <xsl:if test="matches(., '(\w|\.)+;(\d|\.)*;.+')">
            <dct:replaces
              rdf:resource="http://papyri.info/ddbdp/{$ddb-reprint-seq[1]};{$ddb-reprint-seq[2]};{encode-for-uri($ddb-reprint-seq[3])}/source"
            />
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
      <dct:isPartOf>
        <xsl:choose>
          <xsl:when test="$ddb-seq[2] = ''">
            <rdf:Description rdf:about="http://papyri.info/ddbdp/{$ddb-seq[1]}">
              <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
              <dct:isPartOf rdf:resource="http://papyri.info/ddbdp"/>
            </rdf:Description>
          </xsl:when>
          <xsl:otherwise>
            <rdf:Description rdf:about="http://papyri.info/ddbdp/{$ddb-seq[1]};{$ddb-seq[2]}">
              <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
              <dct:isPartOf>
                <rdf:Description rdf:about="http://papyri.info/ddbdp/{$ddb-seq[1]}">
                  <rdf:type rdf:resource="http://purl.org/ontology/bibo/Series"/>
                  <dct:isPartOf rdf:resource="http://papyri.info/ddbdp"/>
                </rdf:Description>
              </dct:isPartOf>
            </rdf:Description>
          </xsl:otherwise>
        </xsl:choose>
      </dct:isPartOf>
      <xsl:for-each select="//tei:idno[@type = 'HGV']">
        <xsl:for-each select="tokenize(., '\s')">
          <xsl:variable name="dir" select="ceiling(number(replace(., '[a-z]', '')) div 1000)"/>
          <xsl:if
            test="doc-available(concat('file://', $root, '/HGV_meta_EpiDoc/HGV', $dir, '/', ., '.xml'))">
            <dct:relation rdf:resource="http://papyri.info/hgv/{.}/source"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:for-each select="//tei:idno[@type = 'TM']">
        <xsl:for-each select="tokenize(., '\s')">
          <dct:relation rdf:resource="http://www.trismegistos.org/text/{.}"/>
        </xsl:for-each>
      </xsl:for-each>
      <foaf:page>
        <rdf:Description rdf:about="{$page}">
          <foaf:topic rdf:resource="{$id}"/>
        </rdf:Description>
      </foaf:page>
    </rdf:Description>
  </xsl:template>
</xsl:stylesheet>
