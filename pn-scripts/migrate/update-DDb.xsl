<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:array="http://www.w3.org/2005/xpath-functions/array"
  xmlns:map="http://www.w3.org/2005/xpath-functions/map"
  xmlns="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs tei array map"
  expand-text="yes"
  version="3.0">
  
  <xsl:mode on-no-match="shallow-copy"/>
  
  <xsl:param name="id"/>
  <xsl:param name="tmbase"/>
  <xsl:param name="hgvbase"/>
  <xsl:variable name="TM" select="tei:getTM(/)"/>
  <xsl:variable name="HGV" select="tei:getHGV(/)"/>
  
  <xsl:template match="processing-instruction()">
    <xsl:text>
</xsl:text><xsl:copy-of select="."/><xsl:text>
</xsl:text>
  </xsl:template>
  
  <xsl:template match="tei:body/tei:head">
    <head>
      <xsl:variable name="TMout">
        <xsl:for-each select="//tei:idno[@type='TM']">
          <xsl:for-each select="tokenize(normalize-space(.), ' ')">
            <xsl:variable name="TM" select="tei:getTM(.)"/>
              <xsl:if test="$TM instance of map(*) and map:contains($TM, 'publications')">
                <xsl:for-each select="array:flatten($TM('publications'))" >
                  <ref n="{format-number(.('id'), '#')}"><title>{.('title')}</title><date>{.('date')}</date></ref>
                </xsl:for-each>
              </xsl:if>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="string-length(normalize-space($TMout)) = 0">
          <xsl:for-each select="$HGV">
            <xsl:for-each select=".//tei:div[@type='bibliography'][@subtype='principalEdition']//tei:bibl">
              <ref>{normalize-space(.)}</ref>
            </xsl:for-each>
            <xsl:for-each select=".//tei:div[@type='bibliography'][@subtype='otherPublications']//tei:bibl">
              <ref>{normalize-space(.)}</ref>
            </xsl:for-each>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$TMout"/>
        </xsl:otherwise>
      </xsl:choose>
      <ref target="https://papyri.info/editions/{tei:makeURI(replace(//tei:idno[@type='ddb-hybrid'], ';+', '/'))}">{//tei:idno[@type='ddb-hybrid']}</ref>
      <xsl:for-each select="tei:ref[@type='reprint-from']/@n">
        <xsl:for-each select="tokenize(., '\|')">
           <ref target="https://papyri.info/editions/{replace(.,';+', '/')}">{.}</ref>
        </xsl:for-each>
      </xsl:for-each>
    </head>
  </xsl:template>

  <xsl:template match="tei:idno[@type='filename']">
    <idno type="filename">{$id}</idno>
  </xsl:template>
  
  <xsl:template match="tei:div[@type='edition']//tei:div[@n]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="xml:id">{@subtype}_{translate(@n, ',', '_')}</xsl:attribute>
      <head><xsl:if test="@subtype">{upper-case(substring(@subtype, 1, 1))}{substring(@subtype, 2)} </xsl:if>{@n}</head>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:function name="tei:getHGV">
    <xsl:param name="root"/>
    <xsl:for-each select="$root//tei:idno[@type='HGV']">
      <xsl:for-each select="tokenize(.)">
        <xsl:variable name="folder" select="ceiling(xs:int(replace(., '\D', '')) div 1000)"/>
        <xsl:if test="doc-available(concat('file://', $hgvbase, '/HGV_meta_EpiDoc/HGV', $folder, '/', ., '.xml'))">
          <xsl:copy-of select="doc(concat('file://', $hgvbase, '/HGV_meta_EpiDoc/HGV', $folder, '/', ., '.xml'))"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:function>

  <xsl:function name="tei:getTM">
    <xsl:param name="idno"/>
    <xsl:variable name="folder" select="floor(xs:int($idno) div 1000)"/>
    <xsl:variable name="file" select="concat('file://', $tmbase, '/', $folder, '/', $idno, '.json')"/>
    <xsl:try>
      <xsl:sequence select="json-doc($file)"/>
      <xsl:catch>
        <xsl:message>{$file} is not available</xsl:message>
        <xsl:map></xsl:map>
      </xsl:catch>
    </xsl:try>
  </xsl:function>
  
  <xsl:function name="tei:makeURI">
    <xsl:param name="filename"/>
    <xsl:for-each select="tokenize($filename, '/')">
      <xsl:text>{encode-for-uri(.)}</xsl:text><xsl:if test="position() != last()">/</xsl:if>
    </xsl:for-each>
  </xsl:function>
  
  
</xsl:stylesheet>