<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id: reg-teiabbrandexpan.xsl 1542 2012-04-15 18:09:22Z thill $ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:t="http://www.tei-c.org/ns/1.0" 
    exclude-result-prefixes="t"  
    version="2.0">
    <!-- Contains templates for expan and abbr -->
    <xsl:template match="orig-edition-wrapper//t:abbr">
         <xsl:apply-templates/>
         <!-- Found in tpl-certlow.xsl -->
         <xsl:call-template name="cert-low"/>
         <xsl:value-of select="$abbreviation-marker"></xsl:value-of>
    </xsl:template>
    
    <xsl:template match="orig-edition-wrapper//t:ex">
        <xsl:value-of select="$abbreviation-marker"></xsl:value-of>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    
</xsl:stylesheet>