<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  exclude-result-prefixes="xd"
  version="1.0">
  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p><xd:b>Created on:</xd:b> Aug 19, 2011</xd:p>
      <xd:p><xd:b>Author:</xd:b> hcayless</xd:p>
      <xd:p></xd:p>
    </xd:desc>
  </xd:doc>
  
  <xsl:template match="t:emph">
    <xsl:choose>
      <xsl:when test="@rend = 'bold'"><b><xsl:apply-templates/></b></xsl:when>
      <xsl:when test="starts-with(@rend, 'italic')"><i><xsl:apply-templates/></i></xsl:when>
      <xsl:otherwise><span class="{@rend}"><xsl:apply-templates/></span></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>