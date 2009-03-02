<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:jsp="http://java.sun.com/JSP/Page"
>
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
      <tbody>
	<tr>
		<th scope="col"><a href="#" onclick="return false;"
			onmouseover="">Publication</a></th>
			<th scope="col"><a href="#" onclick="return false;"
			onmouseover="">Inventory</a></th>
		<th scope="col"><a href="#" onclick="return false;"
			onmouseover="">Provenance</a></th>
		<th scope="col"><a href="#" onclick="return false;"
			onmouseover="">Date</a></th>
		<th scope="col"><a href="#" onclick="return false;"
			onmouseover="">Title</a></th>
		<th scope="col"><a href="#" onclick="return false;"
			onmouseover="">Language</a></th>
			<th scope="col">BL Post-Concordance</th>
	</tr>
      <xsl:apply-templates/>
      </tbody>
    </table>
    </xsl:element>
</xsl:template>

<xsl:template match="document">
	<tr class="searchresult">
		<td class="pub"><a href="#" onclick="return false;"><xsl:value-of select="datum[@class='pub']" /></a>
		
		<xsl:if test="datum[@class='image']" >
    <span style="font-style:italic;font-weight:bold">i</span>
    </xsl:if>
		</td>
    <td class="ids"><xsl:value-of select="datum[@class='id, inventory']" />    </td>
		<td class="provenance"><xsl:value-of select="datum[@class='provenance']" /></td>
		<td class="date"><xsl:value-of select="datum[@class='date']" /></td>
		<td class="title"><xsl:value-of select="datum[@class='title']" /></td>
		<td class="lang"><xsl:value-of select="datum[@class='lang']" /></td>
    <td class="bl"><xsl:value-of select="datum[@class='bl']" />
    </td>
	</tr>
</xsl:template>
</xsl:transform>