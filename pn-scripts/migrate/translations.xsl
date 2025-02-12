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
  
  <xsl:mode on-no-match="shallow-copy" name="out"/>
  
  <xsl:template match="/">
    <xsl:variable name="root" select="."/>
    <xsl:for-each select="//body/div[@type='translation']">
      <xsl:variable name="position" select="count(preceding-sibling::div[@type='translation'])" as="xs:integer"/>
      <xsl:result-document href="{$root//idno[@type='filename']}-{$position + 1}.xml">
        <xsl:apply-templates select="/" mode="out">
          <xsl:with-param name="position" select="$position" tunnel="yes"/>
        </xsl:apply-templates>
      </xsl:result-document>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="body/div[@type='translation']" mode="out">
    <xsl:param name="position" as="xs:integer" tunnel="yes"/>
    <xsl:if test="count(preceding-sibling::div[@type='translation']) = $position">
      <xsl:copy>
        <xsl:apply-templates select="@*" mode="out"/>
        <xsl:apply-templates mode="out"/>
      </xsl:copy>
      <div type="bibliography">
        <listBibl>
          <xsl:choose>
            <xsl:when test=".//note[starts-with(., 'Translation:')]">
              <xsl:for-each select="distinct-values(.//note[starts-with(., 'Translation:')])">
                <bibl>{substring-after(., 'Translation: ')}</bibl>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise><bibl/></xsl:otherwise>
          </xsl:choose>
        </listBibl>
      </div>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>