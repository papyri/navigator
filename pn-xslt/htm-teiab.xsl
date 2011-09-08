<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id: htm-teiab.xsl 1530 2011-08-02 11:24:51Z gabrielbodard $ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:t="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="t" 
                version="2.0">
  
  <xsl:template match="t:ab">
      <div class="textpart">
         <xsl:apply-templates/>
         <!-- if next div or ab begins with lb[inWord], then add hyphen -->
         <xsl:if test="following::t:lb[1][@type='inWord'] and not($edition-type='diplomatic')">
            <xsl:text>-</xsl:text>
         </xsl:if>
      </div>
  </xsl:template>

</xsl:stylesheet>
