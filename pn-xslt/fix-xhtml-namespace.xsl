<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  exclude-result-prefixes="xd"
  version="1.0">
  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p><xd:b>Created on:</xd:b> Dec 16, 2011</xd:p>
      <xd:p><xd:b>Author:</xd:b> hcayless</xd:p>
      <xd:p></xd:p>
    </xd:desc>
  </xd:doc>
  
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="xsl:stylesheet"><xsl:text>
</xsl:text>
    <xsl:element name="stylesheet" namespace="http://www.w3.org/1999/XSL/Transform">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
    
  </xsl:template>
  
  <xsl:template match="node()|@*|comment()">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*|comment()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>