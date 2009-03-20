<?xml version="1.0" encoding="utf-8"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
  <xsl:call-template name="RDF" />
</xsl:template>
<xsl:template match="Type" />
<xsl:template name="RDF" match="RDF">
  <html>
    <head><title>Query Building Example</title></head>
    <body>
      <ul>
      <xsl:apply-templates/>
      </ul>
    </body>
    </html>
</xsl:template>

<xsl:template match="Coverage[@resource='APIS:metadata:ddbdp:ddbfull']//Identifier">
  <xsl:variable name="series" select="substring-before(@resource,';')" />
  <xsl:variable name="afterSeries" select="substring-after(@resource,';')" />
  <xsl:variable name="volume" select="substring-before($afterSeries,';')" />
  <xsl:variable name="document" select="substring-after($afterSeries,';')"/>
  <li>
  <xsl:element name="a">
    <xsl:attribute name="href">
      <xsl:text>http://www.perseus.tufts.edu/hopper/text.jsp?doc=Perseus%3Atext%3A1999.05.</xsl:text><xsl:value-of select="$series"/><xsl:text>%3Avolume%3D</xsl:text><xsl:value-of select="$volume"/><xsl:text>%3Adocument%3D</xsl:text><xsl:value-of select="$document" />
    </xsl:attribute>
    <xsl:text>Link to Perseus doc id </xsl:text>
    <xsl:value-of select="@resource"/>
  </xsl:element>
  </li>
</xsl:template>
<xsl:template match="Coverage[@resource='APIS:metadata:hgv:publikationl']//Identifier">
<xsl:variable name="resource" select="@resource" />
<xsl:call-template name="replace">
  <xsl:with-param name="outputString" select="$resource" />
  <xsl:with-param name="target" select=" " />
  <xsl:with-param name="replacement" select="%20" /> 
</xsl:call-template>
  <li>
  <xsl:element name="a">
    <xsl:attribute name="href">
      <xsl:text>http://aquila.papy.uni-heidelberg.de/Hauptregister/FMPro?-db=hauptregister%5f&amp;PublikationL=</xsl:text><xsl:value-of select="$resource"/><xsl:text>&amp;-format=DFormVw.htm&amp;-lay=Einzel&amp;-max=1&amp;-skip=0&amp;-token=25&amp;-find</xsl:text>
    </xsl:attribute>
    <xsl:text>Link to HGV doc id </xsl:text>
    <xsl:value-of select="@resource"/>
  </xsl:element>
  </li>
</xsl:template> 
<xsl:template match="Coverage[@resource='APIS:metadata:apis:controlname']//Identifier">
  <xsl:variable name="institution" select="substring-before(@resource,'.')" />
  <xsl:variable name="apis" select="substring-after(@resource,'.')" />
  <xsl:variable name="number" select="substring-after($apis,'.')"/>
  <li>
  <xsl:element name="a">
    <xsl:attribute name="href">
      <xsl:text>http://wwwapp.cc.columbia.edu/ldpd/app/apis/search?mode=search&amp;apisnum_inst=</xsl:text><xsl:value-of select="$institution"/><xsl:text>&amp;apisnum_num=</xsl:text><xsl:value-of select="$number" /><xsl:text>&amp;sort=date&amp;resPerPage=25&amp;action=search&amp;p=1</xsl:text>
    </xsl:attribute>
    <xsl:text>Link to APIS id </xsl:text>
    <xsl:value-of select="@resource"/>
  </xsl:element>
  </li>
</xsl:template> 
<xsl:template match="Coverage[@resource='APIS:metadata:leuven:texid']//Identifier">
  <li>
  <xsl:element name="a">
    <xsl:attribute name="href">
      <xsl:text>http://ldab.arts.kuleuven.be/ldab_text_detail.php?tm=</xsl:text><xsl:value-of select="@resource"/><xsl:text>&amp;i=1</xsl:text>
    </xsl:attribute>
    <xsl:text>Link to Leuven LDAB texID</xsl:text>
    <xsl:value-of select="@resource"/>
  </xsl:element>
  </li>
</xsl:template> 


<xsl:template name="replace">
  <xsl:param name="outputString"/>
  <xsl:param name="target"/>
  <xsl:param name="replacement"/>
  <xsl:choose>
    <xsl:when test="contains($outputString,$target)">
   
      <xsl:value-of select=
        "concat(substring-before($outputString,$target),
               $replacement)"/>
      <xsl:call-template name="globalReplace">
        <xsl:with-param name="outputString" 
             select="substring-after($outputString,$target)"/>
        <xsl:with-param name="target" select="$target"/>
        <xsl:with-param name="replacement" 
             select="$replacement"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$outputString"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
</xsl:transform>