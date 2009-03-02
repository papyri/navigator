<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:jsp="http://java.sun.com/JSP/Page"
>
<!--
  <img src="http://ldpddev.cul.columbia.edu:8080/erez4/erez?src=APIS/columbia.apis.1447.f.0.600.tif&tmp=Thumbnail&format=png">
-->
<xsl:template match="/">
  <xsl:call-template name="series" />
</xsl:template>
<xsl:template match="Type" />
<xsl:template name="series">
  <xsl:element name="jsp:root">
  <xsl:attribute name="version">2.0</xsl:attribute>
  <!-- xsl:attribute name="xmlns:jsp">http://java.sun.com/JSP/Page</xsl:attribute-->
  <xsl:element name="jsp:directive.page">
     <xsl:attribute name="import">edu.columbia.apis.*,java.util.*</xsl:attribute>
     <xsl:attribute name="pageEncoding">UTF-8</xsl:attribute>
  </xsl:element>
  <table>
  <caption>Search Results</caption>
      <xsl:apply-templates/>
    </table>
    </xsl:element>
</xsl:template>

<xsl:template match="document">
<xsl:element name="tbody"> 
<xsl:if test="position() mod 2 = 0">
       <xsl:attribute name="class">greenline</xsl:attribute>
    </xsl:if>
    <xsl:attribute name="class">searchresult</xsl:attribute>

  <tr>
    <td rowspan="2" class="metadatalinks">
    <b>Identifiers:</b>
    <ul>
    <li>APIS: <xsl:value-of select="datum[@class='id, apis']" /></li>
    <li>HGV: <xsl:value-of select="datum[@class='id, hgv']" /></li>
    <li>DDBDP: <xsl:value-of select="datum[@class='id, ddbdp']" /></li>
    </ul>
    </td>
    <td class="publication"><b>Publication:</b><br/>
    <xsl:value-of select="datum[@class='pub']" />
    </td>
    <td class="date"><b>Date:</b><br/>
    <xsl:value-of select="datum[@class='date']" /> 
    </td>
    <td class="provenance"><b>Provenance:</b><br/>
    <xsl:value-of select="datum[@class='provenance']" />
    </td>

    <td class="title"><b>Title:</b><br/>
    <xsl:value-of select="datum[@class='title']" />
    </td>
    <td class="translation">
    <xsl:element name="a">
      <xsl:attribute name="href">
        <xsl:value-of select="datum[@class='translation'][@lang='EN']" />
      </xsl:attribute>translation (EN)
      </xsl:element>
    
    </td>
  </tr>
  <tr>
    <td class="author-archive"><b>Author/Archive:</b><br/>
    <xsl:value-of select="datum[@class='author-archive']" />
    </td>

    <td class="ids"><b>Inventory:</b><br/>
<xsl:value-of select="datum[@class='id, inventory']" />    </td>
    <td class="lang">
    <xsl:value-of select="datum[@class='lang']" />
    </td>
    <td class="image">
    <xsl:if test="datum[@class='image']" >
    <xsl:element name="a">
    <xsl:attribute name="href"><!-- drawn from Abbildung in HGV -->
     <xsl:choose>
      <xsl:when test="datum[@class='image']/image[@uri]">
        <xsl:text>#</xsl:text>
      </xsl:when>
      <xsl:otherwise>#</xsl:otherwise>
     </xsl:choose>
    </xsl:attribute>
      Image at APIS
    </xsl:element>
    <span style="font-style:italic;font-weight:bold">i</span>
    </xsl:if>
    </td>
    <td class="bl"><b>BL Post-Concordance:</b><br/>
    <xsl:value-of select="datum[@class='bl']" />
    </td>

  </tr>
</xsl:element>
</xsl:template>
</xsl:transform>