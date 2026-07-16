<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs tei"
  expand-text="yes"
  xpath-default-namespace="http://www.tei-c.org/ns/1.0"
  version="3.0">
  
  <xsl:mode on-no-match="shallow-copy"/>
  <xsl:param name="filename" as="xs:string"/>
  <xsl:variable name="dclp">https://papyri.info/current/{//idno[@type='dclp']}</xsl:variable>
    
  <xsl:template match="processing-instruction()">
    <xsl:text>
</xsl:text><xsl:copy-of select="."/><xsl:text>
</xsl:text>
  </xsl:template>
  
  <xsl:template match="idno[@type='filename']">
    <idno type="filename">{$filename}</idno>
  </xsl:template>
    
  <xsl:template match="sourceDesc">
    <sourceDesc>
      <bibl>{$filename}</bibl>
    </sourceDesc>
  </xsl:template>
  
  <xsl:template match="div[@type=('commentary','introduction','edition')]"/>
  
  <xsl:template match="@corresp">
    <xsl:attribute name="corresp">{$dclp}{.}</xsl:attribute>
  </xsl:template>
  
  <xsl:template match="@default"/>
  <xsl:template match="@full"/>
  <xsl:template match="@part"/>
  <xsl:template match="tei:div/@org"/>
  <xsl:template match="tei:div/@sample"/>
  <xsl:template match="@instant"/>
  <xsl:template match="@status"/>
  
</xsl:stylesheet>