<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns:pi="http://papyri.info/ns/"
  exclude-result-prefixes="xs math t pi"
  expand-text="yes"
  version="3.0">
  
  <xsl:mode on-no-match="shallow-copy"/>
   
  <xsl:param name="base">/srv/data/papyri.info/idp.data</xsl:param>
  <xsl:variable name="name" select="//t:idno[@type = 'filename']/text()"/>  
  
  <xsl:template match="/">
    <xsl:variable name="folder" select="floor(xs:int(replace(//t:idno[@type = 'TM'], '[a-z]+', '')) div 1000)"/>
    <xsl:variable name="context" select="/"/>
    <xsl:if test="//t:idno[@type='ddb-hybrid']">
      <xsl:variable name="filename" select="concat('file://', $base, '/DDbDP/', $folder, '/', $name, '.xml')"/>
      <xsl:if test="not(doc-available($filename))">
        <xsl:call-template name="document">
          <xsl:with-param name="id" select="//t:idno[@type='ddb-hybrid'][1]"/>
          <xsl:with-param name="type">ddb</xsl:with-param>
          <xsl:with-param name="filename" select="$filename"/>
          <xsl:with-param name="context" select="$context"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
    <xsl:if test="//t:idno[@type='dclp-hybrid']">
      <xsl:variable name="filename" select="concat('file://', $base, '/DCLP/', $folder, '/', $name, '.xml')"/>
      <xsl:if test="not(doc-available($filename))">
        <xsl:call-template name="document">
          <xsl:with-param name="id" select="//t:idno[@type='dclp-hybrid'][1]"/>
          <xsl:with-param name="type">dclp</xsl:with-param>
          <xsl:with-param name="filename" select="$filename"/>
          <xsl:with-param name="context" select="$context"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="document">
    <xsl:param name="id"/>
    <xsl:param name="type"/>
    <xsl:param name="filename"/>
    <xsl:param name="context"/>
    <xsl:result-document href="{$filename}" indent="yes">
      <xsl:processing-instruction name="xml-model">href="https://epidoc.stoa.org/schema/9.7/tei-epidoc.rng" type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>
      <TEI xml:lang="en" xmlns="http://www.tei-c.org/ns/1.0">
        <teiHeader>
          <fileDesc>
            <titleStmt>
              <title>{replace($id, ';+', '.')}</title>
            </titleStmt>
            <publicationStmt>
              <authority>Duke Collaboratory for Classics Computing (DC3)</authority>
              <idno type="filename">{$name}</idno>
              <xsl:choose>
                <xsl:when test="$type = 'ddb'"><idno type="ddb-hybrid">{$id}</idno></xsl:when>
                <xsl:otherwise><idno type="dclp-hybrid">{$id}</idno></xsl:otherwise>
              </xsl:choose>
              <idno type="HGV">{$context//t:idno[@type='filename']}</idno>
              <idno type="TM">{$context//t:idno[@type='TM']}</idno>
              <availability>
                <p>© Duke Databank of Documentary Papyri. This work is licensed under a
                  <ref type="license" target="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 License</ref>.
                </p>
              </availability>
            </publicationStmt>
            <sourceDesc>
              <p/>
            </sourceDesc>
          </fileDesc>
          <profileDesc>
            <langUsage>
              <language ident="en">English</language>
              <language ident="grc">Greek</language>
            </langUsage>
          </profileDesc>
          <revisionDesc>
            <change when="{current-dateTime()}" who="http://papyri.info/about">Generated from HGV source.</change>
          </revisionDesc>
        </teiHeader>
        <text>
          <body>
            <head xml:lang="en">
              <ref target="https://papyri.info/editions/{replace($id, ';+', '/')}">{replace($id, ';+', '.')}</ref>
            </head>
            <div xml:lang="grc" type="edition" xml:space="preserve">
              <note xml:lang="en">This text has not been added to papyri.info yet.</note>
              <ab/>
            </div>
          </body>
        </text>
      </TEI>
    </xsl:result-document>
    
  </xsl:template>
  
</xsl:stylesheet>