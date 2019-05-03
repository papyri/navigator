<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:pi="http://papyri.info/ns"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:dct="http://purl.org/dc/terms/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
  exclude-result-prefixes="xs tei" version="2.0">

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
        <xsl:when test="contains($dclp-hybrid, ';')"><xsl:value-of select="pi:makeUnicodeSafeUri($dclp-hybrid)"/></xsl:when>
        <xsl:otherwise>na;;<xsl:value-of select="$tm"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="id">
      <xsl:text>http://</xsl:text>
      <xsl:value-of select="$domain"/>
      <xsl:text>/dclp/</xsl:text>
      <xsl:value-of select="$tm"/> <!-- since the dclp-hybrid number cannot be relied upon, use DCLP number resp. TM number like HGV does -->
      <xsl:text>/source</xsl:text>
    </xsl:variable>

    <rdf:Description rdf:about="{$id}">
      <dct:identifier>
        <xsl:value-of select="$domain"/>
        <xsl:text>/dclp/</xsl:text>
        <xsl:value-of select="$tm"/>
      </dct:identifier>
      <xsl:if test="string($ldab)">
        <dct:identifier>
          <xsl:value-of select="$domain"/>
          <xsl:text>/ldab/</xsl:text>
          <xsl:value-of select="$ldab"/>
        </dct:identifier>
      </xsl:if>
     
      <xsl:for-each select="$dclp">
        <xsl:variable name="dclpId" select="pi:makeUnicodeSafeUri(.)"/>
        <dct:identifier>
          <xsl:value-of select="$domain"/>
          <xsl:text>/dclp/</xsl:text>
          <xsl:value-of select="$dclpId"/>
        </dct:identifier>
        <dct:source>
          <rdf:Description rdf:about="http://{$domain}/dclp/{$dclpId}/work">
            <dct:source rdf:resource="http://{$domain}/dclp/{$dclpId}/original" />
          </rdf:Description>
        </dct:source>
      </xsl:for-each>
      <dct:identifier>
        <xsl:value-of select="//tei:publicationStmt/tei:idno[@type = 'filename']/text()"/>
      </dct:identifier>

      <xsl:for-each select="distinct-values(//tei:text/tei:body/tei:head[@xml:lang='en']/tei:ref[@type='reprint-in']/@n)">
        <xsl:for-each select="tokenize(., '\|')">
          <xsl:if test="matches(., '^\d+$')">
            <dct:isReplacedBy rdf:resource="http://{$domain}/dclp/{.}/source"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:for-each select="//tei:body/tei:head[@xml:lang='en']/tei:ref[@type='reprint-from']">
        <xsl:for-each select="tokenize(@n, '\|')">
          <xsl:if test="matches(., '^\d+$')">
            <dct:replaces rdf:resource="http://{$domain}/dclp/{.}/source"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:choose>
        <xsl:when test="$dclp">
          <xsl:for-each select="$dclp">
            <xsl:variable name="ddbId" select="pi:makeUnicodeSafeUri(.)"/>
            <xsl:variable name="ddb-seq" select="tokenize(normalize-space($ddbId), ';')"/> <!-- bgu;7;1510 => ['bgu','7','1510'] -->
            <dct:isPartOf>
              <xsl:choose>
                <xsl:when test="$ddb-seq[2] = ''">
                  <rdf:Description rdf:about="http://{$domain}/dclp/{$ddb-seq[1]}">
                    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
                    <dct:isPartOf rdf:resource="http://{$domain}/dclp"/>
                  </rdf:Description>
                </xsl:when>
                <xsl:otherwise>
                  <rdf:Description rdf:about="http://{$domain}/dclp/{$ddb-seq[1]};{$ddb-seq[2]}">
                    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
                    <dct:isPartOf>
                      <rdf:Description rdf:about="http://{$domain}/dclp/{$ddb-seq[1]}">
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Series"/>
                        <dct:isPartOf rdf:resource="http://{$domain}/dclp"/>
                      </rdf:Description>
                    </dct:isPartOf>
                  </rdf:Description>
                </xsl:otherwise>
              </xsl:choose>
            </dct:isPartOf>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <dct:isPartOf>
            <rdf:Description rdf:about="http://{$domain}/dclp/na">
              <dct:isPartOf rdf:resource="http://{$domain}/dclp"/>
            </rdf:Description>
          </dct:isPartOf>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:for-each select="//tei:idno[lower-case(@type)='tm']">
        <xsl:for-each select="tokenize(., '\s')">
          <dct:relation rdf:resource="http://www.trismegistos.org/text/{.}"/>
          <dct:relation>
            <rdf:Description rdf:about="http://papyri.info/trismegistos/{.}">
              <dct:relation rdf:resource="{$id}"/>
            </rdf:Description>
          </dct:relation>
          <!-- not all HGV trans files have TM no., e.g. HGV_trans_EpiDoc/9363a.xml -->
          <!-- some HGV trans files have more than one TM no., e.g. HGV_trans_EpiDoc/5285\ 44179.xml -->
          <!-- relation DCLP to HGV trans 1:n (because of the HGV number [TM no. + letter code]) -->
          <!-- but the system can handle only 1:1 -->
          <!-- so we take into cosideration only translations that have a distinct TM no. -->
          <xsl:if test="doc-available(concat($root, '/HGV_trans_EpiDoc/', ., '.xml'))">
            <dct:relation>
              <rdf:Description rdf:about="http://papyri.info/hgvtrans/{.}/source">
                <dct:relation rdf:resource="{$id}"/>
              </rdf:Description>
            </dct:relation>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
      
      <xsl:for-each select="//tei:idno[lower-case(@type)='ldab']">
        <xsl:for-each select="tokenize(., '\s')">
          <dct:relation rdf:resource="http://www.trismegistos.org/ldab/text.php?quick={.}"/>
        </xsl:for-each>
      </xsl:for-each>
      
      <xsl:for-each select="$dclp">
        <xsl:variable name="ddbId" select="pi:makeUnicodeSafeUri(.)"/>
        <xsl:variable name="ddb-seq" select="tokenize(normalize-space($ddbId), ';')"/> <!-- bgu;7;1510 => ['bgu','7','1510'] -->
        <xsl:variable name="page">
            <xsl:text>http://</xsl:text>
            <xsl:value-of select="$domain"/>
            <xsl:text>/dclp/</xsl:text>
            <xsl:value-of select="pi:makeUnicodeSafeUri($ddb-seq[1])"/>
            <xsl:text>;</xsl:text>
            <xsl:value-of select="$ddb-seq[2]"/>;<xsl:value-of select="encode-for-uri($ddb-seq[3])"/>
        </xsl:variable>
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
</xsl:stylesheet>
