<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  xmlns="http://www.tei-c.org/ns/1.0"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs math tei"
  expand-text="yes"
  xpath-default-namespace="http://www.tei-c.org/ns/1.0"
  version="3.0">
  
  <xsl:param name="id"/>
  <xsl:output indent="yes"/>
  <xsl:mode on-no-match="shallow-copy"/>
  
  <xsl:template match="teiHeader">
    <xsl:copy>
      <xsl:apply-templates/>
      <profileDesc>
        <langUsage>
          <language ident="en">English</language>
        </langUsage>
      </profileDesc>
      <revisionDesc>
        <change when="{current-dateTime()}" who="https://papyri.info/editor/users/hcayless">Pull translation out of APIS record.</change>
      </revisionDesc>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="publicationStmt">
    <xsl:copy>
      <xsl:apply-templates/>
      <idno type="filename">{$id}</idno>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="msContents"/>
  <xsl:template match="physDesc"/>
  <xsl:template match="history"/>
  <xsl:template match="encodingDesc"/>
  <xsl:template match="profileDesc"/>
  <xsl:template match="facsimile"/>
  
  <xsl:template match="p"/>
  <xsl:template match="bibl[@type]"/>
  
  <xsl:template match="@default|@part|@sample|@org"/>
  
  
  <xsl:template match="body/div[@type='translation']">
    <xsl:copy>
      <xsl:attribute name="xml:lang">en</xsl:attribute>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="body/div[@type='bibliography']">
    <div type="bibliography">
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  
  <xsl:template match="div"/>
  
</xsl:stylesheet>