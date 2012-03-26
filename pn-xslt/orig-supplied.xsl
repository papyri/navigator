<?xml version="1.0" encoding="UTF-8"?>
<!-- omits supplied[@reason='omitted'] output for indexing of text in original form -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:t="http://www.tei-c.org/ns/1.0" xmlns:EDF="http://epidoc.sourceforge.net/ns/functions"
    exclude-result-prefixes="t EDF" version="2.0">
    <xsl:template match="orig-edition-wrapper//t:supplied[@reason='omitted']"></xsl:template>
    
</xsl:stylesheet>