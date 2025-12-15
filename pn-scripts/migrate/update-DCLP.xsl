<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:array="http://www.w3.org/2005/xpath-functions/array"
  xmlns:map="http://www.w3.org/2005/xpath-functions/map"
  xmlns="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs tei array map"
  expand-text="yes"
  version="3.0">
  
  <xsl:mode on-no-match="shallow-copy"/>
  
  <xsl:param name="tmbase"/>
  <xsl:variable name="TM" select="tei:getTM(/)"/>
  
  <xsl:template match="processing-instruction()">
    <xsl:text>
</xsl:text><xsl:copy-of select="."/><xsl:text>
</xsl:text>
  </xsl:template>
  
  <xsl:template match="tei:body">
    <xsl:copy>
      <head>
        <xsl:if test="$TM instance of map(*) and map:contains($TM, 'publications')">
          <xsl:for-each select="array:flatten($TM('publications'))" >
            <ref n="{format-number(.('id'), '#')}"><title>{.('title')}</title><date>{.('date')}</date></ref>
          </xsl:for-each>
        </xsl:if>
        <xsl:for-each select="//tei:idno[@type='dclp-hybrid']">
          <ref target="https://papyri.info/editions/{tei:makeURI(replace(., ';+', '/'))}">{.}</ref>
        </xsl:for-each>
      </head>
      <xsl:apply-templates/>
    </xsl:copy>
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
  
  <xsl:function name="tei:getTM">
    <xsl:param name="root"/>
    <xsl:for-each select="$root//tei:idno[@type='TM']">
      <xsl:for-each select="tokenize(.)">
        <xsl:variable name="folder" select="floor(xs:int(.) div 1000)"/>
        <xsl:variable name="file" select="concat('file://', $tmbase, '/', $folder, '/', ., '.json')"/>
        <xsl:try>
          <xsl:copy-of select="json-doc($file)"/>
          <xsl:catch>
            <xsl:message>{$file} is not available</xsl:message>
            <xsl:map></xsl:map>
          </xsl:catch>
        </xsl:try>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:function>
  
  <xsl:function name="tei:makeURI">
    <xsl:param name="filename"/>
    <xsl:for-each select="tokenize($filename, '/')">
      <xsl:text>{encode-for-uri(.)}</xsl:text><xsl:if test="position() != last()">/</xsl:if>
    </xsl:for-each>
  </xsl:function>
  
</xsl:stylesheet>