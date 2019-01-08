<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:tei="http://www.tei-c.org/ns/1.0" 
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
    exclude-result-prefixes="xs tei" version="2.0">
    <xsl:output omit-xml-declaration="yes"/>
    
    <xsl:template match="/tei:TEI">
        <xsl:variable name="id">http://papyri.info/hgvtrans/<xsl:value-of select="//tei:publicationStmt/tei:idno[@type = 'filename']"/>/source</xsl:variable>
        <rdf:Description rdf:about="{$id}">
            <dcterms:identifier>papyri.info/hgvtrans/<xsl:value-of select="//tei:publicationStmt/tei:idno[@type = 'filename']"/></dcterms:identifier>
            <xsl:for-each select="//tei:publicationStmt/tei:idno[@type='ddb-hybrid']">
              <xsl:for-each select="tokenize(., ' ')">
                <dcterms:relation>
                  <rdf:Description rdf:about="http://papyri.info/ddbdp/{.}/source">
                    <dcterms:relation rdf:resource="{$id}"/>
                  </rdf:Description>
                </dcterms:relation>
              </xsl:for-each>
            </xsl:for-each>
          <xsl:for-each select="//tei:publicationStmt/tei:idno[@type='HGV']">
            <xsl:for-each select="tokenize(., ' ')">
              <dcterms:relation>
                <rdf:Description
                  rdf:about="http://papyri.info/hgv/{.}/source">
                  <dcterms:relation rdf:resource="{$id}"/>
                </rdf:Description>
              </dcterms:relation>
            </xsl:for-each>
          </xsl:for-each>
        </rdf:Description>
    </xsl:template>
</xsl:stylesheet>
