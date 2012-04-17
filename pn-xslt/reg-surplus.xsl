<?xml version="1.0" encoding="UTF-8"?>
<!-- omits output of <surplus> tags for indexing of text in regularised form -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:t="http://www.tei-c.org/ns/1.0" xmlns:EDF="http://epidoc.sourceforge.net/ns/functions"
    exclude-result-prefixes="t EDF" version="2.0">
    <xsl:template match="reg-edition-wrapper//t:surplus"></xsl:template>
    
</xsl:stylesheet>