<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id: reg-teiabbrandexpan.xsl 1542 2012-04-15 18:09:22Z thill $ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:t="http://www.tei-c.org/ns/1.0" 
    exclude-result-prefixes="t"  
    version="2.0">
    <!-- Contains templates for expan and abbr -->
    <xsl:template match="reg-edition-wrapper//t:abbr">
        <xsl:variable name="full-abbr-string">
        <xsl:if test="substring(., 1, 1) ne upper-case(substring(., 1, 1))"><xsl:value-of select="$abbreviation-marker"></xsl:value-of></xsl:if>
        <xsl:apply-templates/>
        <!-- Found in tpl-certlow.xsl -->
        <xsl:call-template name="cert-low"/>
            <xsl:value-of select="$abbreviation-marker"></xsl:value-of>
        </xsl:variable>
        <xsl:value-of select="replace($full-abbr-string, concat('([\s])*', $abbreviation-marker, '([\s])*'), $abbreviation-marker)"></xsl:value-of>
    </xsl:template>
</xsl:stylesheet>