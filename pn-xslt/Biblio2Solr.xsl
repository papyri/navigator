<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns:pi="http://papyri.info/ns"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  exclude-result-prefixes="t"
  version="2.0">
  
  <xsl:include href="pi-functions.xsl"/>
  <xsl:variable name="path">/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase">/data/papyri.info/pn/idp.html</xsl:variable>
  
  <xsl:template match="/t:bibl">
    <add>
      <doc>
        <field name="id"><xsl:value-of select="@xml:id"/></field>
      <xsl:apply-templates/>
    </doc>
    </add>
  </xsl:template>
    
  <xsl:template match="t:idno">
    <field name="identifier">urn:cts:<xsl:value-of select="@type"/>:<xsl:value-of select="pi:escape-urn(.)"/></field>
  </xsl:template>
  
  <xsl:template match="t:note">
    <field name="{@type}"><xsl:value-of select="."/></field>
  </xsl:template>
  
  <xsl:template match="t:seg">
    <field name="original"><xsl:value-of select="."/></field>
  </xsl:template>
  
  <xsl:template match="*">
    <field name="{local-name(.)}"><xsl:value-of select="normalize-space(replace(.,'\s+',' '))"/></field>
  </xsl:template>
  
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