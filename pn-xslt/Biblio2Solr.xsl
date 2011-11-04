<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns:pi="http://papyri.info/ns"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  exclude-result-prefixes="t"
  version="2.0">
  
  <xsl:import href="Biblio2HTML.xsl"/>
  <xsl:include href="pi-functions.xsl"/>
  <xsl:variable name="path">/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase">/data/papyri.info/pn/idp.html</xsl:variable>
  
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="/t:bibl">
    <xsl:variable name="citation"><xsl:call-template name="buildCitation"/></xsl:variable>
    <add>
      <doc>
        <field name="id"><xsl:value-of select="t:idno[@type='pi']"/></field>
        <field name="sort"><xsl:call-template name="sort"/></field>
        <field name="date"><xsl:for-each select="//t:date">
          <xsl:if test="position() = 1">
            <xsl:choose>
          <xsl:when test="string-length(.) > 0 and number(.) = number(.)"><xsl:value-of select="."/></xsl:when>
          <xsl:otherwise>99999</xsl:otherwise>
        </xsl:choose>
          </xsl:if>
          
        </xsl:for-each></field>
        <field name="display"><xsl:call-template name="escapeTags"><xsl:with-param name="markup" select="$citation"/></xsl:call-template></field>
      <xsl:apply-templates/>
    </doc>
    </add>
  </xsl:template>
    
  <xsl:template match="t:idno">
    <field name="identifier">urn:cts:<xsl:value-of select="@type"/>:<xsl:value-of select="pi:escape-urn(.)"/></field>
  </xsl:template>
  
  <xsl:template match="t:date"/>
    
  
  
  <xsl:template name="sort">
    <xsl:choose>
      <xsl:when test="t:author[@n]">
        <xsl:value-of select="t:author[@n=1]/t:surname"/>, <xsl:value-of select="t:author[@n=1]/t:forename"/>, 
      </xsl:when>
      <xsl:when test="t:author/t:surname">
        <xsl:value-of select="t:author[1]/t:surname"/>, <xsl:value-of select="t:author[1]/t:forename"/>,
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="t:author"/>, 
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="t:title[@type='main']"><xsl:value-of select="t:title[@type='main'][1]"/></xsl:when>
      <xsl:when test="@type='review'">Review</xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="t:note">
    <field name="note"><xsl:value-of select="."/></field>
  </xsl:template>
  
  <xsl:template match="t:seg">
    <field name="original"><xsl:value-of select="."/></field>
  </xsl:template>
  
  <xsl:template match="t:seg[@subtype='index']">
    <field name="index"><xsl:value-of select="."/></field>
  </xsl:template>
  
  <xsl:template match="*">
    <field name="{local-name(.)}"><xsl:value-of select="normalize-space(replace(.,'\s+',' '))"/></field>
  </xsl:template>
  
  <xsl:template name="escapeTags">
    <xsl:param name="markup"/>
    <xsl:apply-templates select="$markup" mode="escape"/>
  </xsl:template>
  
  <xsl:template match="*" mode="escape">&lt;<xsl:value-of select="local-name(.)"/><xsl:for-each select="@*"><xsl:text> </xsl:text><xsl:value-of select="local-name(.)"/>="<xsl:value-of select="."/>"</xsl:for-each>&gt;<xsl:apply-templates mode="escape"/>&lt;/<xsl:value-of select="local-name(.)"/>&gt;</xsl:template>
    
  <xsl:function name="pi:escape-urn">
    <xsl:param name="in"/>
    <xsl:sequence select="string-join(for $c in pi:split($in) 
      return 
      if (matches($c, '([/#?\\&amp;&quot;&lt;&gt;\[\]^`{|}~])')) then
      concat('%', pi:dec-to-hex(string-to-codepoints($c)[1]))
      else $c, '')"/>
  </xsl:function>
  
  <xsl:function name="pi:split">
    <xsl:param name="str" as="xs:string"/>
    <xsl:analyze-string select="$str" regex=".">
      <xsl:matching-substring>
        <xsl:sequence select="."/>
      </xsl:matching-substring>
    </xsl:analyze-string>
  </xsl:function>
</xsl:stylesheet>