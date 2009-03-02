<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:xalan="http://xml.apache.org/xalan"
 xmlns:mads="http://www.loc.gov/mads/"
 xmlns:mods="http://www.loc.gov/mods/v3"
>
<xsl:output method="text" />
<xsl:template match="/">
      <xsl:apply-templates select="mads:madsCollection"/>
</xsl:template>
<xsl:template match="mads:madsCollection">
    var mads = new Array();
      <xsl:apply-templates select="//mods:title[@type='abbreviated']"/>
</xsl:template>

<xsl:template match="mads:mads">
<xsl:variable name="AUTHORITY">
  <xsl:value-of select="./mads:authority/mads:titleInfo/mods:title[@type='abbreviated']"/>
</xsl:variable>
<xsl:variable name="DIM1">
  <xsl:value-of select="position() - 1"/>
</xsl:variable>

<xsl:for-each select=".//mods:title[@type='abbreviated']">
  mads[<xsl:value-of select="$DIM1" />] = ["<xsl:value-of select="text()" />", "<xsl:value-of select="$AUTHORITY" />"];
</xsl:for-each>
</xsl:template>

<xsl:template match="mods:title[@type='abbreviated']">
<xsl:variable name="AUTHORITY">
  <xsl:value-of select="ancestor::mads:mads/mads:authority/mads:titleInfo/mods:title[@type='abbreviated']"/>
</xsl:variable>
<xsl:variable name="DIM1">
  <xsl:value-of select="position() - 1"/>
</xsl:variable>
  mads[<xsl:value-of select="$DIM1" />] = ["<xsl:value-of select="text()" />", "<xsl:value-of select="$AUTHORITY" />"];
</xsl:template>
</xsl:transform>