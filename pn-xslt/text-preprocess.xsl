<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/terms/"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:pi="http://papyri.info/ns"
  xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:t="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs dc rdf pi tei t xd" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  version="2.0">

  <xsl:import href="global-varsandparams.xsl"/>

  <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes"/>

  <xsl:param name="collection"/>
  <xsl:param name="related"/>
  <xsl:param name="images"/>
  <xsl:variable name="relations" select="tokenize($related, ' ')"/>
  <xsl:variable name="path">/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase"/>
  <xsl:variable name="line-inc">5</xsl:variable>
  <xsl:variable name="resolve-uris" select="false()"/>

  <xsl:include href="pi-functions.xsl"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="node()[not(ancestor::t:body)]|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="t:div[@type='edition']">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="t:head">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template match="t:ab">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="t:num">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
    
  <!-- REGULARIZATION -->
  <!-- Choose regularized or expanded choices -->
  <xsl:template match="t:choice">
    <xsl:apply-templates select="t:corr|t:reg|t:expan"/>
  </xsl:template>
  
  <!-- Suppress abbreviation markers -->
  <xsl:template match="t:am"/>
  
  <!-- Suppress alternate readings in apparatus -->
  <xsl:template match="t:app">
    <xsl:apply-templates select="t:lem"/>
  </xsl:template>
  
  <!-- Suppress deletions with substituting additions -->
  <xsl:template match="t:subst">
    <xsl:apply-templates select="t:add"/>
  </xsl:template>
  
  <!-- Line breaks. Erase nonbreaking line breaks -->
  <xsl:template
    match="text()[local-name(following-sibling::*[1]) = 'lb' and 
    (following-sibling::t:lb[1][@type='inWord'] or following-sibling::t:lb[1][@break='no'])]">
    <xsl:value-of select="replace(., '\s+$', '')"/>
  </xsl:template>
  
  <xsl:template match="t:lb[@break='no']"/>

  <xsl:template match="t:lb[not(@break='no')]">
    <xsl:text> </xsl:text>
  </xsl:template>
  
  <xsl:template match="t:div[@type = 'textpart']" priority="1">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Turn gaps into ellipsis -->
  <xsl:template match="t:gap">
    <xsl:text>…</xsl:text>
  </xsl:template>
  
  <!-- Turn symbols into their interpretation (in parentheses) -->
  <xsl:template match="t:g">
    (<xsl:value-of select="@type"/>)
  </xsl:template>

</xsl:stylesheet>
