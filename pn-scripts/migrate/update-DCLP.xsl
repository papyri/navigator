<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs tei"
  expand-text="yes"
  version="3.0">
  
  <xsl:mode on-no-match="shallow-copy"/>
  
  <xsl:template match="processing-instruction()">
    <xsl:text>
</xsl:text><xsl:copy-of select="."/><xsl:text>
</xsl:text>
  </xsl:template>
  
  <xsl:template match="tei:div[@type='edition']//tei:div[@n][not(@xml:id)]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="xml:id">{generate-id(.)}</xsl:attribute>
      <head><xsl:if test="@subtype">{upper-case(substring(@subtype, 1, 1))}{substring(@subtype, 2)} </xsl:if>{@n}</head>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@default"/>
  <xsl:template match="@full"/>
  <xsl:template match="@part"/>
  <xsl:template match="tei:div/@org"/>
  <xsl:template match="tei:div/@sample"/>
  <xsl:template match="@instant"/>
  <xsl:template match="@status"/>
  
</xsl:stylesheet>