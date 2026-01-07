<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:pi="http://papyri.info/ns"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:dct="http://purl.org/dc/terms/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
  expand-text="yes"
  exclude-result-prefixes="xs tei" version="3.0">

  <xsl:output omit-xml-declaration="yes" indent="yes" />
  <xsl:param name="root">/srv/data/papyri.info/idp.data</xsl:param> <!-- use idp.data root directory (the one in which you will find DDB_EpiDoc_XML, HGV_meta_EpiDoc etc.) -->
  <xsl:param name="domain" select="'papyri.info'"/> <!-- for the DCLP project overwrite with 'dclp.atlantides.org' -->

  <xsl:template match="/tei:TEI">
    <xsl:variable name="filename" select="normalize-space(//tei:publicationStmt/tei:idno[lower-case(@type)='filename'])"/>
    <xsl:variable name="ldab" select="normalize-space(//tei:publicationStmt/tei:idno[lower-case(@type)='ldab'])"/>
    <xsl:variable name="dclp-hybrid"
      select="//tei:publicationStmt/tei:idno[lower-case(@type)='dclp-hybrid'][1]"/>
    <xsl:variable name="ddbdp-hybrid" select="//tei:publicationStmt/tei:idno[@type = 'ddb-hybrid']"/>
    <xsl:variable name="id">https://{$domain}/editions/{pi:makeURI($filename)}/source</xsl:variable>
    <xsl:variable name="page">https://{$domain}/editions/{pi:makeURI($filename)}</xsl:variable>

    <rdf:Description rdf:about="{$id}">
      <dct:identifier>
        <xsl:value-of select="$domain"/>
        <xsl:text>/editions/</xsl:text>
        <xsl:value-of select="$filename"/>
      </dct:identifier>
      <xsl:for-each select="distinct-values(//tei:body/tei:head/tei:ref[@type='reprint-in']/@n)">
        <xsl:for-each select="tokenize(., '\|')">
          <dct:hasVersion rdf:resource="https://{$domain}/editions/{replace(., ';', '/')}/source"/>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:for-each select="//tei:body/tei:head[@xml:lang='en']/tei:ref[@type='reprint-from']">
        <xsl:for-each select="tokenize(@n, '\|')">
          <xsl:if test="matches(., '^\d+$')">
            <dct:versionOf rdf:resource="https://{$domain}/editions/{replace(., ';', '/')}/source"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
      
      <xsl:variable name="path-seq" select="tokenize($filename, '/')"/>
      <dct:isPartOf>
        <xsl:choose>
          <xsl:when test="count($path-seq) = 2">
            <rdf:Description rdf:about="https://{$domain}/editions/{$path-seq[1]}">
              <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
              <dct:isPartOf rdf:resource="https://{$domain}/editions"/>
            </rdf:Description>
          </xsl:when>
          <xsl:otherwise>
            <rdf:Description rdf:about="https://{$domain}/editions/{$path-seq[1]}/{$path-seq[2]}">
              <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
              <dct:isPartOf>
                <rdf:Description rdf:about="https://{$domain}/editions/{$path-seq[1]}">
                  <rdf:type rdf:resource="http://purl.org/ontology/bibo/Series"/>
                  <dct:isPartOf rdf:resource="https://{$domain}/editions"/>
                </rdf:Description>
              </dct:isPartOf>
            </rdf:Description>
          </xsl:otherwise>
        </xsl:choose>
      </dct:isPartOf>
      
      <xsl:choose>
        <xsl:when test="//tei:idno[@type = 'HGV'][not(contains(., ' '))]">
          <xsl:for-each select="//tei:idno[@type = 'HGV']">
            <pi:source-for>
              <rdf:Description rdf:about="https://{$domain}/current/{.}/source">
                <dct:source rdf:resource="{$id}"/>
              </rdf:Description>
            </pi:source-for>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="//tei:idno[lower-case(@type) = 'tm']">
            <xsl:choose>
              <xsl:when test="contains(., ' ')">
                <pi:source-for>
                  <rdf:Description rdf:about="https://{$domain}/current/{tokenize(., '\s')[1]}/source">
                    <dct:source rdf:resource="{$id}"/>
                  </rdf:Description>
                </pi:source-for>
              </xsl:when>
              <xsl:otherwise>
                <pi:source-for>
                  <rdf:Description rdf:about="https://{$domain}/current/{.}/source">
                    <dct:source rdf:resource="{$id}"/>
                  </rdf:Description>
                </pi:source-for>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
      
      <xsl:for-each select="//tei:idno[lower-case(@type)='tm']">
        <xsl:for-each select="tokenize(., '\s')">
          <dct:relation rdf:resource="https://www.trismegistos.org/text/{.}"/>
        </xsl:for-each>
      </xsl:for-each>
      
      <xsl:for-each select="//tei:idno[lower-case(@type)='ldab']">
        <xsl:for-each select="tokenize(., '\s')">
          <dct:relation rdf:resource="https://www.trismegistos.org/ldab/text.php?quick={.}"/>
        </xsl:for-each>
      </xsl:for-each>
      
      <xsl:for-each select="$dclp-hybrid">
        <dct:relation rdf:resource="https://{$domain}/dclp/{pi:makeUnicodeSafeUri(.)}/source"/>
      </xsl:for-each>
      
      <xsl:for-each select="$ddbdp-hybrid">
        <dct:relation rdf:resource="https://{$domain}/ddbdp/{pi:makeUnicodeSafeUri(.)}/source"/>
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
    <xsl:value-of select="replace(normalize-unicode($in, 'NFD'), '[^;.,a-zA-Z0-9]', '')"/>
  </xsl:function>
  
  <xsl:function name="pi:makeURI">
    <xsl:param name="filename"/>
    <xsl:for-each select="tokenize($filename, '/')">
      <xsl:text>{encode-for-uri(.)}</xsl:text><xsl:if test="position() != last()">/</xsl:if>
    </xsl:for-each>
  </xsl:function>
  
</xsl:stylesheet>
