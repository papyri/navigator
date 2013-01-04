<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" exclude-result-prefixes="xs tei" version="2.0">
  <xsl:output omit-xml-declaration="yes"/>
  <xsl:param name="root">/data/papyri.info/idp.data</xsl:param>
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
      <!-- Captures old-style DDbDP references -->
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
        <xsl:if test="not(doc-available($ddb-doc-uri))"><xsl:message><xsl:value-of select="$ddb-doc-uri"/> not available.</xsl:message></xsl:if>
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
      <xsl:if test="//tei:publicationStmt/tei:idno[@type = 'ddb-hybrid']">
        <xsl:variable name="ddb" select="tokenize(normalize-space(//tei:publicationStmt/tei:idno[@type='ddb-hybrid']), ';')"></xsl:variable>
        <xsl:variable name="ddb-doc-uri">
          <xsl:choose>
            <xsl:when test="string-length($ddb[2]) = 0"><xsl:value-of select="concat('file://', $DDB-root, '/', $ddb[1], '/', $ddb[1], '.', encode-for-uri($ddb[3]), '.xml')"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="concat('file://', $DDB-root, '/', $ddb[1], '/', $ddb[1], '.', $ddb[2], '/', $ddb[1], '.', $ddb[2], '.', encode-for-uri($ddb[3]), '.xml')"/></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:if test="doc-available($ddb-doc-uri)">
          <dcterms:relation>
            <rdf:Description
              rdf:about="http://papyri.info/ddbdp/{//tei:publicationStmt/tei:idno[@type = 'ddb-hybrid']/text()}/source">
              <dcterms:relation rdf:resource="{$id}"/>
            </rdf:Description>
          </dcterms:relation>
        </xsl:if>
      </xsl:if>
      <xsl:for-each select="//tei:publicationStmt/tei:idno[@type = 'HGV']">
        <xsl:for-each select="tokenize(., '\s')">
          <xsl:variable name="dir" select="ceiling(number(replace(., '[a-z]', '')) div 1000)"/>
          <xsl:if test="doc-available(concat('file://', $root, '/HGV_meta_EpiDoc/HGV', $dir, '/', ., '.xml'))">
            <dcterms:relation>
              <rdf:Description
                rdf:about="http://papyri.info/hgv/{.}/source">
                <dcterms:relation rdf:resource="{$id}"/>
              </rdf:Description>
            </dcterms:relation>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:for-each select="//tei:publicationStmt/tei:idno[@type = 'TM']">
        <xsl:for-each select="tokenize(., '\s')">
          <dcterms:relation>
            <rdf:Description
              rdf:about="http://www.trismegistos.org/text/{.}">
              <dcterms:relation rdf:resource="{$id}"/>
            </rdf:Description>
          </dcterms:relation>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:if test="//tei:facsimile">
        <dcterms:relation rdf:resource="http://papyri.info/apis/{//tei:publicationStmt/tei:idno[@type = 'apisid']/text()}/images"/>
      </xsl:if>
    </rdf:Description>
    <xsl:if test="//tei:facsimile">
      <rdf:Seq rdf:about="http://papyri.info/apis/{//tei:publicationStmt/tei:idno[@type = 'apisid']/text()}/images">
        <xsl:for-each select="//tei:facsimile//tei:graphic">
          <xsl:sort select="ancestor::tei:surfaceGrp/@n"/>
          <rdf:li>
            <rdf:Description rdf:about="{@url}">
              <xsl:if test="../@type">
                <rdfs:label><xsl:value-of select="substring(@url, 30)"/><xsl:text> </xsl:text><xsl:value-of select="../@type"/></rdfs:label>
              </xsl:if>
              <rdf:type rdf:resource="http://purl.org/ontology/bibo/Image"/>
              <foaf:depicts rdf:resource="http://papyri.info/apis/{//tei:publicationStmt/tei:idno[@type = 'apisid']/text()}/original"/>
            </rdf:Description>
          </rdf:li>
        </xsl:for-each>
      </rdf:Seq>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
