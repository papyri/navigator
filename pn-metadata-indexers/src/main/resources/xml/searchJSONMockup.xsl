<xsl:transform version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:jsp="http://java.sun.com/JSP/Page"
>
<xsl:template match="/"><xsl:call-template name="series" /></xsl:template>
<xsl:template match="Type" />
<xsl:template name="series">var APIS_NS_RESULTS = {
  items:[
      <xsl:apply-templates match="document"/>
      ]
      }
</xsl:template>

<xsl:template match="document">
<xsl:if test="position() > 2">,</xsl:if>
	{
	    label:"APIS",
	    id:<xsl:value-of select="position()" />,
		identifier:[<xsl:apply-templates select="data"/>],
		publication:"<xsl:value-of select="datum[@class='pub']" />",
		<xsl:if test="datum[@class='image']" >imageAvailable:"Y",</xsl:if>
        inventory:"<xsl:value-of select="datum[@class='inventory']" />",
		provenance:"<xsl:value-of select="datum[@class='provenance']" />",
		date:"<xsl:value-of select="datum[@class='date']" />",
		title:"<xsl:value-of select="datum[@class='title']" />",
        bl:"<xsl:value-of select="datum[@class='bl']" />",
        lang:[<xsl:apply-templates select="datum[@class='lang']"/>]
    }
</xsl:template>
<xsl:template match="datum[@class='lang']"><xsl:if test="position() > 1">,</xsl:if>"<xsl:value-of select="." />"</xsl:template>
<xsl:template match="data"><xsl:apply-templates select="id" /></xsl:template>
<xsl:template match="id"><xsl:if test="position() > 1">,</xsl:if>"<xsl:value-of select="." />"</xsl:template>
</xsl:transform>