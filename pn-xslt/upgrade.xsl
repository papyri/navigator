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
  
  <xsl:param name="id" select="//tei:idno[@type='filename']/text()"/>
  <xsl:param name="tmbase"/>
  <xsl:param name="hgvbase"/>
  <xsl:param name="tmCSV"/>
  <xsl:variable name="tmTable" select="tei:getCSV($tmCSV)"/>
  <xsl:variable name="TM" select="tei:getTM(/)"/>
  <xsl:variable name="HGV" select="tei:getHGV(/)"/>
  
  <xsl:template match="processing-instruction()">
    <xsl:text>
</xsl:text><xsl:processing-instruction name="xml-model">href="https://papyri.info/schemas/papyri.info.rng" type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction><xsl:text>
</xsl:text><xsl:processing-instruction name="xml-model">href="https://papyri.info/schemas/papyri.info.rng" type="application/xml" schematypens="http://purl.oclc.org/dsdl/schematron"</xsl:processing-instruction><xsl:text>
</xsl:text>
  </xsl:template>
  
  <!-- Squash default attributes -->
  <xsl:template match="@default"/>
  <xsl:template match="@full"/>
  <xsl:template match="@instant"/>
  <xsl:template match="@org"/>
  <xsl:template match="@part"/>
  <xsl:template match="@sample"/>
  <xsl:template match="@status"/>
  <xsl:template match="tei:teiHeader/@type"/>
  
  <xsl:template match="tei:body">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <head>
        <xsl:variable name="hybrid">
          <xsl:choose>
            <xsl:when test="//tei:idno[@type='ddb-hybrid']">{//tei:idno[@type='ddb-hybrid']}</xsl:when>
            <xsl:when test="//tei:idno[@type='dclp-hybrid']">{//tei:idno[@type='dclp-hybrid']}</xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="targets">
          <xsl:sequence select="tei:head/tei:ref[@target]/xs:string(@target)"/>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="contains(document-uri(/), 'DDbDP')">
            <xsl:variable name="TMout">
              <xsl:for-each select="//tei:idno[@type='TM']">
                <xsl:for-each select="tokenize(normalize-space(.), ' ')">
                  <xsl:variable name="TM" select="tei:getTM(.)"/>
                  <xsl:if test="$TM instance of map(*) and map:contains($TM, 'publications')">
                    <xsl:variable name="count" select="count(array:flatten($TM('publications')))"/>
                    <xsl:for-each select="array:flatten($TM('publications'))" >
                      <xsl:variable name="rows" select="tei:matchTM(.('title'))"/>
                      <xsl:if test="count($rows//tei:cell) gt 0">
                        <ref n="{format-number(.('id'), '#')}">
                          <xsl:choose>
                            <xsl:when test="starts-with(substring-before($hybrid, ';'), $rows/tei:row[1]/tei:cell[@name='PN']) and contains(substring-after(.('title'), $rows/tei:row[1]/tei:cell[@name='TM']), substring-after(substring-after($hybrid, ';') , ';'))"><xsl:attribute name="target">https://papyri.info/editions/{tei:makeURI(replace($hybrid, ';+', '/'))}</xsl:attribute></xsl:when>
                            <xsl:otherwise>
                              <xsl:variable name="title" select=".('title')"/>
                              <xsl:for-each select="tokenize($targets)">
                                <xsl:variable name="path" select="tokenize(substring-after(., 'https://papyri.info/editions/'), '/')"/>
                                <xsl:if test="not(empty($rows/tei:row[1]/tei:cell[@name='PN'])) and starts-with($path[1], $rows/tei:row[1]/tei:cell[@name='PN']) and contains(replace($title, '\W', ''), substring-before(replace($path[last()], '\W', ''), ' '))"><xsl:attribute name="target" select="."/></xsl:if>
                              </xsl:for-each>
                            </xsl:otherwise>
                          </xsl:choose>
                          <title>{replace(.('title'), $rows/tei:row[1]/tei:cell[@name='TM'], $rows/tei:row[1]/tei:cell[@name='Checklist'], 'q')}</title><date>{.('date')}</date></ref>
                      </xsl:if>
                    </xsl:for-each>
                  </xsl:if>
                </xsl:for-each>
              </xsl:for-each>
            </xsl:variable>
            <!-- If there's nothing in TM, use the HGV biblio instead. -->
            <xsl:choose>
              <xsl:when test="count($TMout//tei:ref) = 0">
                <xsl:for-each select="$HGV">
                  <xsl:for-each select=".//tei:div[@type='bibliography'][@subtype='principalEdition']//tei:bibl">
                    <ref><title>{normalize-space(.)}</title></ref><xsl:if test="position() != last()"><xsl:text>; </xsl:text></xsl:if>
                  </xsl:for-each>
                  <xsl:for-each select=".//tei:div[@type='bibliography'][@subtype='otherPublications']//tei:bibl">
                    <ref><title>{normalize-space(.)}</title></ref><xsl:if test="position() != last()"><xsl:text>; </xsl:text></xsl:if>
                  </xsl:for-each>
                </xsl:for-each>
              </xsl:when>
              <xsl:otherwise>
                <xsl:for-each select="$TMout/tei:ref">
                  <xsl:sort select="tei:date/text()" order="ascending"/>
                  <xsl:sort select="tei:title/text()" order="ascending"/>
                  <xsl:copy-of select="."/><xsl:if test="position() != last()"><xsl:text>; </xsl:text></xsl:if>
                </xsl:for-each>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <!-- DCLP might have a TM title match, but it's less likely -->
            <xsl:variable name="TMout">
              <xsl:for-each select="//tei:idno[@type='TM']">
                <xsl:for-each select="tokenize(normalize-space(.), ' ')">
                  <xsl:variable name="TM" select="tei:getTM(.)"/>
                  <xsl:if test="$TM instance of map(*) and map:contains($TM, 'publications')">
                    <xsl:variable name="count" select="count(array:flatten($TM('publications')))"/>
                    <xsl:for-each select="array:flatten($TM('publications'))" >
                      <xsl:variable name="rows" select="tei:matchTM(.('title'))"/>
                      <xsl:choose>
                        <xsl:when test="count($rows//tei:cell) gt 0">
                          <ref n="{format-number(.('id'), '#')}">
                            <xsl:choose>
                              <xsl:when test="starts-with(substring-before($hybrid, ';'), $rows/tei:row[1]/tei:cell[@name='PN']) and contains(substring-after(.('title'), $rows/tei:row[1]/tei:cell[@name='TM']), substring-after(substring-after($hybrid, ';') , ';'))"><xsl:attribute name="target">https://papyri.info/editions/{tei:makeURI(replace($hybrid, ';+', '/'))}</xsl:attribute></xsl:when>
                              <xsl:otherwise>
                                <xsl:variable name="title" select=".('title')"/>
                                <xsl:for-each select="tokenize($targets)">
                                  <xsl:variable name="path" select="tokenize(substring-after(., 'https://papyri.info/editions/'), '/')"/>
                                  <xsl:if test="not(empty($rows/tei:row[1]/tei:cell[@name='PN'])) and starts-with($path[1], $rows/tei:row[1]/tei:cell[@name='PN']) and contains(replace($title, '\W', ''), substring-before(replace($path[last()], '\W', ''), ' '))"><xsl:attribute name="target" select="."/></xsl:if>
                                </xsl:for-each>
                              </xsl:otherwise>
                            </xsl:choose>
                            <title>{replace(.('title'), $rows/tei:row[1]/tei:cell[@name='TM'], $rows/tei:row[1]/tei:cell[@name='Checklist'], 'q')}</title><date>{.('date')}</date></ref>
                        </xsl:when>
                        <xsl:otherwise><ref n="{format-number(.('id'), '#')}"><title>{.('title')}</title><date>{.('date')}</date></ref></xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                  </xsl:if>
                </xsl:for-each>
              </xsl:for-each>
            </xsl:variable>
            <xsl:for-each select="$TMout/tei:ref">
              <xsl:sort select="tei:date/text()" order="ascending"/>
              <xsl:sort select="tei:title/text()" order="ascending"/>
              <xsl:copy-of select="."/><xsl:if test="position() != last()"><xsl:text>; </xsl:text></xsl:if>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>        
      </head>
      <xsl:apply-templates select="*[not(self::tei:head)]"/>
    </xsl:copy>    
  </xsl:template>

  <xsl:template match="tei:idno[@type='filename']">
    <idno type="filename">{$id}</idno>
  </xsl:template>
  
  <xsl:template match="tei:div[@type='edition']//tei:div[@n]">
    <xsl:copy>
      <xsl:copy-of select="@*[not(local-name() = ('n', 'part', 'org', 'sample'))]"/>
      <xsl:attribute name="n">{replace(@n, '\W', '')}</xsl:attribute>
      <xsl:if test="not(@xml:id)">
        <xsl:attribute name="xml:id">{generate-id()}</xsl:attribute>
      </xsl:if>
      <head><xsl:if test="@subtype">{upper-case(substring(@subtype, 1, 1))}{substring(@subtype, 2)} </xsl:if>{@n}</head>
      <xsl:apply-templates select="*[not(self::tei:head)]"/>
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
  
  <xsl:function name="tei:getTokens" as="xs:string+">
    <xsl:param name="str" as="xs:string"/>
    <xsl:analyze-string select="concat($str, ',')" regex='(("[^"]*")+|[^,]*),'>
      <xsl:matching-substring>
        <xsl:sequence select='replace(regex-group(1), "^""|""$|("")""", "$1")'/>
      </xsl:matching-substring>
    </xsl:analyze-string>
  </xsl:function>
  
  <xsl:function name="tei:getCSV">
    <xsl:param name="path" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="unparsed-text-available($path)">
        <xsl:variable name="csv" select="unparsed-text($path)"/>
        <xsl:variable name="lines" select="tokenize($csv, '(&#xa;)|(&#xd;&#xa;)')" as="xs:string+"/>
        <xsl:variable name="columns" select="('PN', 'Checklist', 'TM', 'replace', 'regex')"/>
        <xsl:variable name="rows">
          <rows>
          <xsl:for-each select="$lines[position() > 1][. != '']">
            <xsl:variable name="lineParts" select="tei:getTokens(.)" as="xs:string+"/>
            <row n="{string-length($lineParts[3])}">
              <xsl:for-each select="$columns">
                <xsl:variable name="pos" select="position()"/>
                <cell name="{.}">{$lineParts[$pos]}</cell>
              </xsl:for-each>
            </row>
          </xsl:for-each>
        </rows>
        </xsl:variable>
        <rows>
          <xsl:for-each select="$rows//tei:row">
            <xsl:sort select="@n" order="descending"/>
            <xsl:copy-of select="."/>
          </xsl:for-each>
        </rows>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">Can't find: {$path}</xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="tei:matchTM">
    <xsl:param name="TM"/>
    <rows>
      <xsl:choose>
        <!-- TM uses AÉ for some stuff. It's not in the Checklist, so we fake it. -->
        <xsl:when test="starts-with($TM, 'Année épigraphique')">
          <row><cell name="PN">ae</cell><cell name="Checklist">Année épigraphique</cell><cell name="TM">Année épigraphique</cell></row>
        </xsl:when>
        <!-- If we don't have a title, return nothing -->
        <xsl:when test="empty($TM)"></xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="$tmTable//tei:row[string-length(tei:cell[@name='TM']) gt 0]">
            <xsl:if test="starts-with($TM, tei:cell[@name='TM'])">
              <xsl:copy-of select="."/>
            </xsl:if>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </rows>
  </xsl:function>
  
</xsl:stylesheet>