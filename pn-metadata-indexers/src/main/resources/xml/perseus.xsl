<?xml version="1.0" encoding="utf-8"?>
<xsl:transform version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:xalan="http://xml.apache.org/xalan"
 xmlns:apis="xalan://edu.columbia.apis.BetaCodeParser"
 extension-element-prefixes="apis"
>
<xalan:component prefix="apis" functions="parseToString">
  <xalan:script lang="javaclass" src="xalan://edu.columbia.apis.BetaCodeParser"/>
</xalan:component>
<xsl:template match="/">
  <xsl:call-template name="TEI.2" />
</xsl:template>
<xsl:template match="Type" />
<xsl:template name="TEI.2">
  <html>
  <head>
    <style type="text/css">
      .greek {
    font-family: "Arial Unicode MS","New Athena Unicode", Gentium, "Palatino Linotype", "Lucida Grande", Galilee,Helvetica,  "Arial Unicode MS", sans-serif;
    font-size: 150%;
    line-height:250%;
}
.super {
}
    </style>
  </head>
  <body>
  <div class="greek">
      <xsl:apply-templates/>
    </div>
    </body>
    </html>
</xsl:template>

<xsl:template match="*[starts-with(name(), 'div')][@type='document']">
      <xsl:apply-templates/>
</xsl:template>
<xsl:template match="p">
      <xsl:apply-templates/>
</xsl:template>
<xsl:template match="p/text()">
  <xsl:value-of select="apis:parseToString(string())" />
</xsl:template>
<xsl:template match="expan">    <xsl:text> (</xsl:text><xsl:value-of select="apis:parseToString(text())" /><xsl:text>)</xsl:text>
</xsl:template>
<xsl:template match="lb">
  <xsl:element name="br"/>
  <xsl:if test="@n">
    <xsl:element name="span"><xsl:attribute name="class"><xsl:text>lineNumber</xsl:text></xsl:attribute><xsl:value-of select="@n" />&#160;&#160;&#160;</xsl:element>
  </xsl:if>
  <xsl:if test="not(@n)">
    <xsl:element name="span"><xsl:attribute name="class"><xsl:text>lineNumber</xsl:text></xsl:attribute><xsl:value-of select="text()" />&#160;&#160;&#160;</xsl:element>
  </xsl:if>
</xsl:template>
<xsl:template match="num">
    <xsl:text> </xsl:text>
  <xsl:if test="@value">
    <xsl:element name="span"><xsl:attribute name="class"><xsl:text>number</xsl:text></xsl:attribute><xsl:value-of select="@value" /></xsl:element>
  </xsl:if>
  <xsl:if test="not(@value)">
    <xsl:element name="span"><xsl:attribute name="class"><xsl:text>number</xsl:text></xsl:attribute><xsl:value-of select="text()" /></xsl:element>
  </xsl:if>
</xsl:template>
<xsl:template match="app/rdg" />
<xsl:template match="app/wit" />
<xsl:template match="hi">
  <xsl:element name="sup"><xsl:value-of select="apis:parseToString(text())" /></xsl:element>
</xsl:template>
<xsl:template match="app/lem/text()">
  <xsl:value-of select="apis:parseToString(string())" />
</xsl:template>
<xsl:template match="gap">
  <xsl:variable name="gap" select="number(@extent)" />
     <xsl:call-template name="gap">
        <xsl:with-param name="index" select="$gap" />
     </xsl:call-template>
</xsl:template>
<xsl:template name="gap">
  <xsl:param name="index" select="number('0')" />
  <xsl:variable name="next" select="$index - 1" />
  <xsl:if test="$index &gt; 0">
    <xsl:text>.</xsl:text>
     <xsl:call-template name="gap">
        <xsl:with-param name="index" select="$next" />
     </xsl:call-template>
  </xsl:if>
</xsl:template>
<xsl:template match="milestone[@unit='4']">
    <xsl:element name="h2">
      <xsl:if test="number(@n) = 'NaN'">
        <xsl:if test="@n = 'r'">
          <xsl:text>Recto</xsl:text>
        </xsl:if>
        <xsl:if test="@n = 'v'">
          <xsl:text>Verso</xsl:text>
        </xsl:if>
      </xsl:if>
      <xsl:if test="number(@n)">
      <xsl:text>Column </xsl:text>
      <xsl:number value="number(@n)" format="I" />
      </xsl:if>
    </xsl:element>
</xsl:template>

</xsl:transform>