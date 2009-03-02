<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
<xsl:output method="xml" indent="yes" />

<xsl:template match="/">
  <xsl:comment>ROOT</xsl:comment>
  <xsl:apply-templates select="fm" />
</xsl:template>

<xsl:template match="fm">
  <xsl:comment>template:fm</xsl:comment>
  <xsl:element name="DocumentIdentifiers">
      <xsl:apply-templates select="ROW" />
  </xsl:element>
</xsl:template>

<xsl:template match="ERRORCODE"><xsl:comment>template:ERRORCODE</xsl:comment></xsl:template>

<xsl:template match="DATABASE"><xsl:comment>template:DATABASE</xsl:comment></xsl:template>

<xsl:template match="LAYOUT"><xsl:comment>template:LAYOUT</xsl:comment></xsl:template>

<xsl:template match="ROW">
  <xsl:element name="RDF">
    <xsl:element name="Type"><xsl:text>Dataset</xsl:text></xsl:element>
    <xsl:apply-templates select="PublikationL"/>
    <xsl:apply-templates select="ddbFull"/>
    <xsl:apply-templates select="TMnumber"/>
  </xsl:element>
</xsl:template>


<xsl:template match="PublikationL">
    <xsl:element name="hgv"><xsl:attribute name="id"><xsl:value-of select="text()"/></xsl:attribute><xsl:attribute name="resource">apis:metadata:hgv:publikationl</xsl:attribute></xsl:element>
</xsl:template>

<xsl:template match="ddbFull">
    <xsl:element name="ddbdp"><xsl:attribute name="id"><xsl:value-of select="text()"/></xsl:attribute><xsl:attribute name="resource">apis:metadata:ddbdp:ddbfull</xsl:attribute></xsl:element>
</xsl:template>

<xsl:template match="TMnumber">
    <xsl:element name="tm"><xsl:attribute name="id"><xsl:value-of select="text()"/></xsl:attribute><xsl:attribute name="resource">apis:metadata:tm:texid</xsl:attribute></xsl:element>
</xsl:template>

</xsl:stylesheet>