<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:tei="http://www.tei-c.org/ns/1.0" 
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:bibo="http://purl.org/ontology/bibo/"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
    exclude-result-prefixes="xs tei" 
    expand-text="yes" 
    version="3.0">
    <xsl:output omit-xml-declaration="yes"/>
    
    <xsl:template match="/tei:TEI">
        <xsl:variable name="id">https://papyri.info/translation/<xsl:value-of select="//tei:publicationStmt/tei:idno[@type = 'filename']"/>/source</xsl:variable>
        <rdf:Description rdf:about="{$id}">
          <bibo:translationOf rdf:resource="https://papyri.info/current/{substring-before(//tei:idno[@type='filename'], '-')}/source"/>
          <xsl:for-each select="//tei:publicationStmt/tei:idno[@type='ddb-hybrid']">
            <xsl:for-each select="tokenize(., ' ')">
              <xsl:variable name="ddb-seq" select="tokenize(normalize-space(.), ';')"/>
              <xsl:variable name="ddb-id">https://papyri.info/editions/<xsl:value-of
                  select="replace(normalize-unicode($ddb-seq[1], 'NFD'), '[^.a-z0-9]', '')"/><xsl:if test="string-length($ddb-seq[2]) gt 0">/<xsl:value-of
                      select="$ddb-seq[2]"/></xsl:if>/<xsl:value-of select="encode-for-uri($ddb-seq[3])"
                    />/source</xsl:variable>
              <dcterms:relation>
                <rdf:Description rdf:about="{$ddb-id}">
                  <dcterms:relation rdf:resource="{$id}"/>
                </rdf:Description>
              </dcterms:relation>
            </xsl:for-each>
          </xsl:for-each>
          <xsl:for-each select="//tei:publicationStmt/tei:idno[@type='HGV']">
            <xsl:for-each select="tokenize(., ' ')">
              <dcterms:relation>
                <rdf:Description
                  rdf:about="https://papyri.info/hgv/{.}/source">
                  <dcterms:relation rdf:resource="{$id}"/>
                </rdf:Description>
              </dcterms:relation>
            </xsl:for-each>
          </xsl:for-each>
          <xsl:for-each select="//tei:publicationStmt/tei:idno[@type='TM']">
            <xsl:for-each select="tokenize(., ' ')">
              <dcterms:relation>
                <rdf:Description rdf:about="https://www.trismegistos.org/text/{.}">
                  <dcterms:relation rdf:resource="{$id}"/>
                </rdf:Description>
              </dcterms:relation>
            </xsl:for-each>
          </xsl:for-each>
        </rdf:Description>
    </xsl:template>
</xsl:stylesheet>
