<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  xmlns="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs math"
  expand-text="yes"
  xpath-default-namespace="http://www.tei-c.org/ns/1.0"
  version="3.0">
  
  <xsl:mode on-no-match="shallow-copy"/>
  
  <xsl:template match="processing-instruction()">
    <xsl:text>
</xsl:text><xsl:copy-of select="."/><xsl:text>
</xsl:text>
  </xsl:template>
  
  <xsl:template match="idno[@type='filename']">
    <xsl:variable name="file" select="substring-before(substring-after(base-uri(), 'Historical/'), '.xml')"/>
    <idno type="filename">{$file}</idno>
  </xsl:template>

  <xsl:template match="publicationStmt[not(idno[@type='filename'])]">
    <xsl:variable name="file" select="substring-before(substring-after(base-uri(), 'Historical/'), '.xml')"/>
    <xsl:copy>
      <xsl:apply-templates/>
      <idno type="filename">{$file}</idno>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="sourceDesc">
    <sourceDesc>
      <bibl>{substring-before(substring-after(base-uri(), 'Historical/'), '.xml')}</bibl>
    </sourceDesc>
  </xsl:template>
  
  <xsl:template match="div[type='edition']"></xsl:template>
  
</xsl:stylesheet>