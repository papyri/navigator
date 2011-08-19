<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id: htm-teidiv.xsl 1447 2008-08-07 12:57:55Z zau $ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:t="http://www.tei-c.org/ns/1.0"
                version="1.0">

  <xsl:template match="t:div">
    <!-- div[@type = 'edition']" and div[starts-with(@type, 'textpart')] can be found in htm-teidivedition.xsl -->
        <div>
          <xsl:if test="parent::t:body and @type">
            <xsl:attribute name="id">
              <xsl:value-of select="@type"/>
            </xsl:attribute>
          </xsl:if>

          <!-- Body of the div -->
          <xsl:apply-templates/>

        </div>

  </xsl:template>

</xsl:stylesheet>