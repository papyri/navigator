<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs tei"
  expand-text="yes"
  version="3.0">
  
  <xsl:mode on-no-match="shallow-copy"/>
  
  <xsl:variable name="base">file:///Users/hac13/Development/APIS/idp.data</xsl:variable>
  <xsl:variable name="HGV" select="tei:getHGV(/)"/>
  
  <xsl:template match="processing-instruction()">
    <xsl:text>
</xsl:text><xsl:copy-of select="."/><xsl:text>
</xsl:text>
  </xsl:template>
  
  <xsl:template match="tei:body/tei:head">
    <head>
      <xsl:for-each select="$HGV">
        <xsl:for-each select=".//tei:div[@type='bibliography'][@subtype='principalEdition']//tei:bibl">
          <ref>{normalize-space(.)}</ref>
        </xsl:for-each>
        <xsl:for-each select=".//tei:div[@type='bibliography'][@subtype='otherPublications']//tei:bibl">
          <ref>{normalize-space(.)}</ref>
        </xsl:for-each>
      </xsl:for-each>
          <ref target="https://papyri.info/pub/{replace(//tei:idno[@type='ddb-hybrid'], ';+', '/')}">{//tei:idno[@type='ddb-hybrid']}</ref>
      <xsl:for-each select="tei:ref[@type='reprint-from']/@n">
        <xsl:for-each select="tokenize(., '\|')">
           <ref target="https://papyri.info/pub/{replace(.,';+', '/')}">{.}</ref>
        </xsl:for-each>
      </xsl:for-each>
    </head>
  </xsl:template>
  
  <xsl:template match="tei:div[@type='edition']//tei:div[@n]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="xml:id">{@subtype}_{translate(@n, ',', '_')}</xsl:attribute>
      <head><xsl:if test="@subtype">{upper-case(substring(@subtype, 1, 1))}{substring(@subtype, 2)} </xsl:if>{@n}</head>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:function name="tei:getHGV">
    <xsl:param name="root"/>
    <xsl:for-each select="$root//tei:idno[@type='HGV']">
      <xsl:for-each select="tokenize(.)">
        <xsl:variable name="folder" select="ceiling(xs:int(replace(., '\D', '')) div 1000)"/>
        <xsl:if test="doc-available(concat($base, '/HGV_meta_EpiDoc/HGV', $folder, '/', ., '.xml'))">
          <xsl:copy-of select="doc(concat($base, '/HGV_meta_EpiDoc/HGV', $folder, '/', ., '.xml'))"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:function>
  
  
</xsl:stylesheet>