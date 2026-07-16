<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs math t"
  version="3.0">
  
  <xsl:output omit-xml-declaration="no" expand-text="yes"/>
  <xsl:mode on-no-match="shallow-copy"/>
  
  <xsl:template match="/">
    <xsl:apply-templates select="*|node()"/>
  </xsl:template>
  
  <xsl:template match="@default|@part|@org|@sample|@status"></xsl:template>
  
  <xsl:template match="t:teiHeader/@type"></xsl:template>
  
  <xsl:template match="processing-instruction()"><xsl:text>
</xsl:text><xsl:processing-instruction name="{local-name()}"><xsl:value-of select="."/></xsl:processing-instruction><xsl:text>
</xsl:text></xsl:template>
    
  <xsl:template match="t:div[@type = 'edition']">
    <xsl:copy>
      <xsl:copy-of select="attribute()"/><xsl:text>
           </xsl:text>
      <note xml:lang="en">This text has not been added to papyri.info yet.</note><xsl:text>
           </xsl:text>
      <ab/><xsl:text>
         </xsl:text>
    </xsl:copy>
  </xsl:template>
    
</xsl:stylesheet>