<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:xalan="http://xml.apache.org/xalan"
 xmlns:mads="http://www.loc.gov/mads/"
 xmlns:mods="http://www.loc.gov/mods/v3"
>
<xsl:output method="text" />
<xsl:template match="/">
      <xsl:apply-templates select="mods:modsCollection"/>
</xsl:template>
<xsl:template match="mods:modsCollection">
      <xsl:apply-templates select="//mods:mods"/>
</xsl:template>
<xsl:template match="mods:mods">
<xsl:variable name="AUTHORITY">
  <xsl:value-of select="@ID"/>
</xsl:variable>
<xsl:for-each select=".//mods:titleInfo[@type='abbreviated']"><xsl:value-of select="mods:title/text()" /> = <xsl:value-of select="$AUTHORITY" />
<xsl:text>
</xsl:text>
</xsl:for-each>
</xsl:template>
</xsl:transform>