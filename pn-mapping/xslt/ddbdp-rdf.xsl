<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:tei="http://www.tei-c.org/ns/1.0" 
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    exclude-result-prefixes="xs tei" version="2.0">
    <xsl:output omit-xml-declaration="yes"/>
  <xsl:param name="root"/>
    
    <xsl:template match="/tei:TEI">
        <xsl:variable name="ddb-seq" select="tokenize(normalize-space(//tei:publicationStmt/tei:idno[@type='ddb-hybrid']), ';')"/>
        <xsl:variable name="id">http://papyri.info/ddbdp/<xsl:value-of select="replace(normalize-unicode($ddb-seq[1], 'NFD'), '[^.a-z0-9]', '')"/>;<xsl:value-of select="$ddb-seq[2]"/>;<xsl:value-of select="encode-for-uri($ddb-seq[3])"/>/source</xsl:variable>
        <xsl:variable name="perseus-id" select="//tei:publicationStmt/tei:idno[@type = 'ddb-perseus-style']"/>
        <rdf:Description rdf:about="{$id}">
            <dcterms:identifier>papyri.info/ddbdp/<xsl:value-of select="//tei:publicationStmt/tei:idno[@type = 'ddb-hybrid']/text()"/></dcterms:identifier>
          <dcterms:source rdf:resource="http://papyri.info/ddbdp/{//tei:publicationStmt/tei:idno[@type = 'ddb-hybrid']/text()}/edition"/>
            <dcterms:identifier><xsl:value-of select="//tei:publicationStmt/tei:idno[@type = 'filename']/text()"/></dcterms:identifier>
            <xsl:for-each select="distinct-values(//tei:text/tei:body/tei:head[@xml:lang='en']/tei:ref[@type='reprint-in']/@n)">
                <xsl:for-each select="tokenize(., '\|')">
                <xsl:variable name="ddb-reprint-seq" select="tokenize(., ';')"/>
                <xsl:if test="matches(., '(\w|\.)+;(\d|\.)*;.+')">
                    <dcterms:isReplacedBy rdf:resource="http://papyri.info/ddbdp/{$ddb-reprint-seq[1]};{$ddb-reprint-seq[2]};{encode-for-uri($ddb-reprint-seq[3])}/source"/>
                </xsl:if>
            </xsl:for-each>
            </xsl:for-each>
          <xsl:for-each select="//tei:body/tei:head[@xml:lang='en']/tei:ref[@type='reprint-from']">
            <xsl:for-each select="tokenize(@n, '\|')">
                <xsl:variable name="ddb-reprint-seq" select="tokenize(., ';')"/>
                <xsl:if test="matches(., '(\w|\.)+;(\d|\.)*;.+')">    
                    <dcterms:replaces rdf:resource="http://papyri.info/ddbdp/{$ddb-reprint-seq[1]};{$ddb-reprint-seq[2]};{encode-for-uri($ddb-reprint-seq[3])}/source"/>
                </xsl:if>
            </xsl:for-each>
          </xsl:for-each>
            <dcterms:isPartOf>
                <xsl:choose>
                    <xsl:when test="$ddb-seq[2] = ''">
                        <rdf:Description rdf:about="http://papyri.info/ddbdp/{$ddb-seq[1]}">
                            <dcterms:isPartOf rdf:resource="http://papyri.info/ddbdp"/>
                        </rdf:Description>
                    </xsl:when>
                    <xsl:otherwise>
                        <rdf:Description rdf:about="http://papyri.info/ddbdp/{$ddb-seq[1]};{$ddb-seq[2]}">
                            <dcterms:isPartOf>
                                <rdf:Description rdf:about="http://papyri.info/ddbdp/{$ddb-seq[1]}">
                                    <dcterms:isPartOf rdf:resource="http://papyri.info/ddbdp"/>
                                </rdf:Description>
                            </dcterms:isPartOf>
                        </rdf:Description>
                    </xsl:otherwise>
                </xsl:choose>
            </dcterms:isPartOf>
          <xsl:for-each select="//tei:idno[@type = 'HGV']">
            <xsl:for-each select="tokenize(., '\s')">
              <xsl:variable name="dir" select="ceiling(number(replace(., '[a-z]', '')) div 1000)"/>
              <xsl:if test="doc-available(concat('file://', $root, '/HGV_meta_EpiDoc/HGV', $dir, '/', ., '.xml'))">
                <dcterms:relation rdf:resource="http://papyri.info/hgv/{.}/source"/>
              </xsl:if>
            </xsl:for-each>
          </xsl:for-each>
          <xsl:for-each select="//tei:idno[@type = 'TM']">
            <xsl:for-each select="tokenize(., '\s')">
              <dcterms:relation rdf:resource="http://www.trismegistos.org/text/{.}"/>
            </xsl:for-each>
          </xsl:for-each>
        </rdf:Description>
    </xsl:template>
</xsl:stylesheet>
