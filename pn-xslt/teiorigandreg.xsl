<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id: teiorigandreg.xsl 1434 2011-05-31 18:23:56Z gabrielbodard $ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:t="http://www.tei-c.org/ns/1.0"
   exclude-result-prefixes="t" version="2.0">
   <!-- Contains templates for choice/orig and choice/reg and surplus -->

   <xsl:template match="t:choice/t:orig">
      <xsl:choose>
         <xsl:when test="$leiden-style = 'ddbdp'">
            <xsl:choose>
               <xsl:when test="not(../t:reg[not(@xml:lang != ancestor::t:*[@xml:lang][1]/@xml:lang)])">
                  <xsl:apply-templates/>
               </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates/>
                <xsl:call-template name="cert-low"/> 
               </xsl:otherwise>
            </xsl:choose>
         </xsl:when>
      </xsl:choose>
   </xsl:template>

   <xsl:template match="t:choice/t:reg">
      <xsl:choose>
         <xsl:when test="$leiden-style = 'ddbdp'">
            <xsl:choose>
               <xsl:when test="@xml:lang != ancestor::t:*[@xml:lang][1]/@xml:lang"/>
               <xsl:when test="preceding-sibling::t:reg[not(@xml:lang != ancestor::t:*[@xml:lang][1]/@xml:lang)]"/>
            </xsl:choose>
         </xsl:when>
         <xsl:otherwise/>
      </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
