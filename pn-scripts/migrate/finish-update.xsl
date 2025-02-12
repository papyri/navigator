<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:ns0="http://www.tei-c.org/ns/1.0"
  xmlns="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs tei ns0"
  expand-text="yes"
  version="3.0">
  
  <xsl:mode on-no-match="shallow-copy"/>
  
  <xsl:template match="/">
    <xsl:copy>
      <xsl:processing-instruction name="xml-model">href="https://epidoc.stoa.org/schema/8.16/tei-epidoc.rng" type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:element name="{local-name(.)}" namespace="http://www.tei-c.org/ns/1.0">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="//tei:body/tei:head">
    <xsl:element name="{local-name(.)}" namespace="http://www.tei-c.org/ns/1.0">
      <xsl:copy-of select="@*"/>
      <xsl:for-each select="tei:ref">
        <xsl:if test="position() gt 1"> = </xsl:if>
        <xsl:element name="ref" namespace="http://www.tei-c.org/ns/1.0">
          <xsl:copy-of select="@*"/>
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>