<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:t="http://www.tei-c.org/ns/1.0" 
    xmlns:ms="urn:schemas-microsoft-com:office:spreadsheet"
    exclude-result-prefixes="xs t ms" version="2.0">
    <xsl:output indent="yes"/>
  
  <xsl:variable name="base">/Users/hcayless/Development/APIS/apis_translations/</xsl:variable>
  <!-- Expects an XML-form Excel Spreadsheet with the columns filename (w/ recto/verso and .jp2 suffix stripped), inv. no., apis id, image url -->
  <!--<xsl:variable name="image-doc" select="doc('file:/Users/hcayless/Development/APIS/P-Lund.xml')"/>-->
  <xsl:variable name="id">http://papyri.info/apis/<xsl:value-of select="normalize-space(//cu001)"/>/source</xsl:variable>
  <xsl:variable name="olddoc"><xsl:choose>
    <xsl:when test="doc-available(concat('file:///Users/hac13/Development/APIS/idp.data/APIS/berkeley/xml/',
      //cu001, '.xml'))"><xsl:copy-of select="doc(concat('file:///Users/hac13/Development/APIS/idp.data/APIS/berkeley/xml/',
        //cu001, '.xml'))"></xsl:copy-of></xsl:when>
    <xsl:otherwise><xsl:sequence select="/"/></xsl:otherwise>
  </xsl:choose></xsl:variable>
    
    <xsl:template match="/">
      <xsl:processing-instruction name="xml-model">href="http://www.stoa.org/epidoc/schema/8.13/tei-epidoc.rng" type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction><xsl:text>
</xsl:text>
        <TEI
            xmlns="http://www.tei-c.org/ns/1.0">
            <teiHeader>
                <fileDesc>
                    <titleStmt>
                        <title><xsl:value-of select="//cu245ab"/></title>
                        <xsl:for-each select="//cu1004[. = 'aut']">
                            <author><xsl:value-of select="//cu100a[@n = current()/@n]"/></author>
                        </xsl:for-each>
                    </titleStmt>
                    <publicationStmt>
                        <authority>APIS</authority>
                        <idno type="apisid"><xsl:value-of select="normalize-space(//cu001)"/></idno>
                        <xsl:if test="//cu035"><idno type="controlNo"><xsl:value-of select="normalize-space(//cu035)"/></idno></xsl:if>
                        <!-- makes an attempt to create a match on the ddb-readable field in the mapping doc -->
                        <xsl:for-each select="//cu510_dd"><idno type="ddbdp"><xsl:value-of select="replace(normalize-space(replace(lower-case(.),':','.')),'\.\.','.')"/></idno></xsl:for-each>
                      <xsl:for-each select="$olddoc//t:idno[@type='ddb-perseus-style']"><xsl:copy-of
                        select="."/></xsl:for-each>
                      <xsl:for-each select="$olddoc//t:idno[@type='ddb-hybrid']"><xsl:copy-of
                        select="."/></xsl:for-each>
                      <xsl:for-each select="$olddoc//t:idno[@type='HGV']"><xsl:copy-of
                        select="."/></xsl:for-each>
                      <xsl:for-each select="$olddoc//t:idno[@type='TM']"><xsl:copy-of
                        select="."/></xsl:for-each>
                    </publicationStmt>
                    <sourceDesc>
                        <msDesc>
                            <msIdentifier>
                                <xsl:copy-of select="document('repositories.xml')//t:msIdentifier[@xml:id = substring-before(//cu001, '.')]/t:*"/>
                                <idno type="invNo"><xsl:value-of select="//cu090"/></idno>
                            </msIdentifier>
                            <msContents>
                                <xsl:if test="//cu520"><summary><xsl:value-of select="//cu520"></xsl:value-of></summary></xsl:if>
                                <xsl:if test="//*[starts-with(local-name(.), 'cu500') or //*[local-name(.) = 'cu544_n'] or //*[local-name(.) = 'cu590'] or //cuLCODE or //cu546[not(contains(lower-case(.), 'no language'))]]">
                                    <msItemStruct>
                                        <xsl:for-each select="//cu500"><note type="general"><xsl:value-of select="."/></note></xsl:for-each>
                                        <xsl:for-each select="//cu544_n"><note type="related"><xsl:value-of select="."/></note></xsl:for-each>
                                        <xsl:for-each select="//cu590"><note type="local_note"><xsl:value-of select="."/></note></xsl:for-each>
                                        <xsl:if test="//cuLCODE or //cu546[not(contains(lower-case(.), 'no language'))]">
                                            <textLang>
                                                <xsl:attribute name="mainLang">
                                                    <xsl:choose>
                                                        <xsl:when test="//cuLCODE"><xsl:call-template name="langCode"><xsl:with-param name="code" select="//cuLCODE[1]"></xsl:with-param></xsl:call-template></xsl:when>
                                                        <xsl:otherwise><xsl:call-template name="langCode"><xsl:with-param name="code" select="//cu546[1]"/></xsl:call-template></xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:attribute>
                                                <xsl:choose>
                                                    <xsl:when test="//cuLCODE[number(@n) gt 1]">
                                                        <xsl:attribute name="otherLangs">
                                                            <xsl:for-each select="//cuLCODE[number(@n) gt 1]">
                                                                <xsl:variable name="code"><xsl:call-template name="langCode"><xsl:with-param name="code" select="."/></xsl:call-template></xsl:variable>
                                                                <xsl:value-of select="$code"/><xsl:if test="position() != last()"><xsl:text> </xsl:text></xsl:if>
                                                            </xsl:for-each>
                                                        </xsl:attribute>
                                                    </xsl:when>
                                                    <xsl:when test="//cu546[2]">
                                                        <xsl:attribute name="otherLangs">
                                                            <xsl:for-each select="//cu546[position() gt 1]">
                                                                <xsl:variable name="code"><xsl:call-template name="langCode"><xsl:with-param name="code" select="."/></xsl:call-template></xsl:variable>
                                                                <xsl:value-of select="$code"/><xsl:if test="position() != last()"><xsl:text> </xsl:text></xsl:if>
                                                            </xsl:for-each>
                                                        </xsl:attribute>
                                                    </xsl:when>
                                                </xsl:choose>
                                                <xsl:for-each select="//cu546"><xsl:value-of select="."/><xsl:if test="position() != last()"><xsl:text>; </xsl:text></xsl:if></xsl:for-each>
                                            </textLang>
                                        </xsl:if>
                                    </msItemStruct>
                                </xsl:if>
                            </msContents>
                            <physDesc>
                              <objectDesc>
                                <supportDesc>
                                  <support><xsl:value-of select="//cu300"/></support>
                                  <xsl:if test="//*[starts-with(local-name(.), 'cu590_')]">
                                    <condition>
                                      <xsl:for-each select="//cu590_con"><ab type="conservation"><xsl:value-of select="."/></ab></xsl:for-each>
                                      <xsl:for-each select="//cu590_prs"><ab type="preservation"><xsl:value-of select="."/></ab></xsl:for-each>
                                    </condition>
                                  </xsl:if>
                                </supportDesc>
                                <xsl:if test="//cu500_lin or //cu500_rec">
                                  <layoutDesc>
                                    <layout>
                                      <xsl:for-each select="//cu500_lin"><ab type="lines"><xsl:value-of select="."/></ab></xsl:for-each>
                                      <xsl:for-each select="//cu500_rec"><ab type="recto-verso"><xsl:value-of select="."/></ab></xsl:for-each>
                                    </layout>
                                  </layoutDesc>
                                </xsl:if>
                              </objectDesc>
                              <xsl:if test="//cu500_pal">
                                <handDesc>
                                  <p><xsl:value-of select="//cu500_pal"/></p>
                                </handDesc>
                              </xsl:if>
                            </physDesc>
                            <history>
                                <origin>
                                    <xsl:if test="//cu245f or //cuDateValue"><origDate>
                                        <xsl:if test="//cuDateRange[. = 'b']">
                                            <xsl:attribute name="notBefore">
                                                <xsl:call-template name="formatDate">
                                                    <xsl:with-param name="date"><xsl:value-of select="//cuDateValue[@n = //cuDateRange[. = 'b']/@n]"/></xsl:with-param>
                                                    <xsl:with-param name="scheme"><xsl:value-of select="//cuDateSchema[@n = //cuDateRange[. = 'b']/@n]"></xsl:value-of></xsl:with-param>
                                                </xsl:call-template>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:if test="//cuDateRange[. = 'e']">
                                            <xsl:attribute name="notAfter">
                                                <xsl:call-template name="formatDate">
                                                    <xsl:with-param name="date"><xsl:value-of select="//cuDateValue[@n = //cuDateRange[. = 'e']/@n]"/></xsl:with-param>
                                                    <xsl:with-param name="scheme"><xsl:value-of select="//cuDateSchema[@n = //cuDateRange[. = 'e']/@n]"></xsl:value-of></xsl:with-param>
                                                </xsl:call-template>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:if test="//cuDateRange[. = 's']">
                                            <xsl:attribute name="when">
                                                <xsl:call-template name="formatDate">
                                                    <xsl:with-param name="date"><xsl:value-of select="//cuDateValue[@n = //cuDateRange[. = 's']/@n]"/></xsl:with-param>
                                                    <xsl:with-param name="scheme"><xsl:value-of select="//cuDateSchema[@n = //cuDateRange[. = 's']/@n]"></xsl:value-of></xsl:with-param>
                                                </xsl:call-template>
                                            </xsl:attribute>
                                        </xsl:if>
                                      <xsl:choose>
                                        <xsl:when test="//cu245f"><xsl:value-of select="//cu245f"/></xsl:when>
                                        <xsl:otherwise>
                                          <xsl:if test="//cuDateRange[. = 'b']">
                                            <xsl:call-template name="formatDateEra">
                                                <xsl:with-param name="date"><xsl:value-of select="//cuDateValue[@n = //cuDateRange[. = 'b']/@n]"/></xsl:with-param>
                                                <xsl:with-param name="scheme"><xsl:value-of select="//cuDateSchema[@n = //cuDateRange[. = 'b']/@n]"></xsl:value-of></xsl:with-param>
                                            </xsl:call-template>
                                            <xsl:text> – </xsl:text>
                                        </xsl:if>
                                        <xsl:if test="//cuDateRange[. = 'e']">
                                            <xsl:call-template name="formatDateEra">
                                                <xsl:with-param name="date"><xsl:value-of select="//cuDateValue[@n = //cuDateRange[. = 'e']/@n]"/></xsl:with-param>
                                                <xsl:with-param name="scheme"><xsl:value-of select="//cuDateSchema[@n = //cuDateRange[. = 'e']/@n]"></xsl:value-of></xsl:with-param>
                                            </xsl:call-template>
                                        </xsl:if>
                                        <xsl:if test="//cuDateRange[. = 's']">
                                            <xsl:call-template name="formatDateEra">
                                                <xsl:with-param name="date"><xsl:value-of select="//cuDateValue[@n = //cuDateRange[. = 's']/@n]"/></xsl:with-param>
                                                <xsl:with-param name="scheme"><xsl:value-of select="//cuDateSchema[@n = //cuDateRange[. = 's']/@n]"></xsl:value-of></xsl:with-param>
                                            </xsl:call-template>
                                        </xsl:if>
                                        </xsl:otherwise>
                                      </xsl:choose>
                                      </origDate></xsl:if>
                                    <xsl:for-each select="//cu518">
                                        <origPlace><xsl:value-of select="."></xsl:value-of></origPlace>
                                    </xsl:for-each>     
                                    <xsl:for-each select="//cu1004[. = 'asn']">
                                        <persName type="asn"><xsl:value-of select="//cu100a[@n = current()/@n]"/></persName>
                                    </xsl:for-each>
                                </origin>
                                <xsl:for-each select="//cu561">
                                    <provenance>
                                        <p><xsl:value-of select="."/></p>
                                    </provenance>
                                </xsl:for-each>
                            </history>
                        </msDesc>
                    </sourceDesc>
                </fileDesc>
              <encodingDesc>
                <classDecl>
                  <taxonomy xml:id="apis">
                    <desc>APIS keywords are controlled locally at the institution level. They are not necessarily consistent.</desc>
                  </taxonomy>
                </classDecl>
              </encodingDesc>
                <profileDesc>
                    <langUsage>
                        <language ident="en">English</language>
                        <xsl:for-each select="//cu546[not(contains(lower-case(.), 'no language'))]">
                            <xsl:variable name="lang">
                                <xsl:choose>
                                    <xsl:when test="//cuLCODE[@n = current()/@n]"><xsl:call-template name="langCode"><xsl:with-param name="code" select="//cuLCODE[@n = current()/@n]"/></xsl:call-template></xsl:when>
                                    <xsl:otherwise><xsl:call-template name="langCode"><xsl:with-param name="code" select="."/></xsl:call-template></xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <language ident="{$lang}"><xsl:value-of select="."/></language>
                        </xsl:for-each>
                    </langUsage>
                    <xsl:if test="//cu653 or //cu655">
                        <textClass>
                            <keywords scheme="#apis">
                                <xsl:for-each select="//cu653">
                                    <term><xsl:value-of select="."/></term>
                                </xsl:for-each>
                                <xsl:for-each select="//cu655">
                                    <term type="genre_form"><xsl:value-of select="."/></term>
                                </xsl:for-each>
                            </keywords>
                        </textClass>
                    </xsl:if>
                </profileDesc>
            </teiHeader>
          <!--<xsl:variable name="images" select="$image-doc//ms:Row[contains(ms:Cell[3]/ms:Data,$id)]"/>
          <xsl:if test="count($images) gt 0">
            <facsimile>
              <xsl:for-each-group select="$images" group-by="ms:Cell[1]/ms:Data">
                <surfaceGrp n="{replace(current-group()[1]/ms:Cell[1]/ms:Data,'P.Lund0*','')}">
                  <xsl:for-each select="current-group()">
                    <surface>
                      <xsl:choose>
                        <xsl:when test="ends-with(ms:Cell[4]/ms:Data, 'r')"><xsl:attribute name="type">recto</xsl:attribute></xsl:when>
                        <xsl:when test="ends-with(ms:Cell[4]/ms:Data, 'v')"><xsl:attribute name="type">verso</xsl:attribute></xsl:when>
                      </xsl:choose>
                      <graphic url="{ms:Cell[4]/ms:Data}"/>
                    </surface>
                  </xsl:for-each>
                </surfaceGrp>
              </xsl:for-each-group>
            </facsimile>
          </xsl:if>-->
            <text>
                <body>
                    <xsl:for-each select="//cu500_t">
                        <div type="translation">
                          <xsl:variable name="extern" select="contains(., 'mmfclob')"/>
                          <xsl:choose>
                            <xsl:when test="$extern and unparsed-text-available(concat($base, substring-before(substring-after(., '&lt;mmfclob&gt;/www/data/cu/libraries/inside/projects/metadata/translator/conv/'), '&lt;/mmfclob&gt;')))">
                              <ab>
                                <xsl:value-of select="unparsed-text(concat($base, substring-before(substring-after(., '&lt;mmfclob&gt;/www/data/cu/libraries/inside/projects/metadata/translator/conv/'), '&lt;/mmfclob&gt;')))"/>
                              </ab>
                            </xsl:when>
                            <xsl:otherwise>
                              <ab><xsl:value-of select="."/></ab>
                            </xsl:otherwise>
                          </xsl:choose>
                        </div>
                    </xsl:for-each>
                    <div type="bibliography" subtype="citations">
                        <xsl:choose>
                            <xsl:when test="//*[starts-with(local-name(.), 'cu510')]">
                                <xsl:if test="//cu510">
                                    <listBibl>
                                        <bibl><xsl:value-of select="//cu510"/><xsl:if test="//cu581"><xsl:text> </xsl:text><note><xsl:value-of select="//cu581"/></note></xsl:if></bibl>
                                        <xsl:for-each select="//cu510_dd">
                                            <bibl type="ddbdp"><xsl:value-of select="normalize-space(.)"/></bibl>
                                        </xsl:for-each>
                                    </listBibl>
                                </xsl:if>
                                <xsl:if test="//cu510_m">
                                    <p><ref target="{//cu510_m}">Original record</ref>.</p>
                                </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>
                                <p>No citations.</p>
                            </xsl:otherwise>
                        </xsl:choose>
                    </div>
                    <xsl:if test="//cuPresentation_url">
                        <div type="figure">
                            <xsl:for-each select="//cuPresentation_url">
                                <xsl:variable name="n1" select="number(@n)"/>
                                <xsl:variable name="n2" select="number(@m)"/>
                                <figure>
                                    <head><xsl:value-of select="//cuPart_caption[number(@n) = $n1]"/></head>
                                    <figDesc><xsl:value-of select="//cuPart_section[number(@n) = $n1]"/><xsl:text> </xsl:text><xsl:value-of select="//cuPresentation_display_res[number(@n) = $n1 and number(@m) = $n2]"/></figDesc>
                                    <graphic url="{.}"/>
                                </figure>
                            </xsl:for-each>
                        </div>
                    </xsl:if>
                </body>
            </text>
        </TEI>
    </xsl:template>
    
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template name="langCode">
        <xsl:param name="code"/>
        <xsl:choose><xsl:when test="'ara' = $code or contains(lower-case($code), 'arabic')">ar-Arab</xsl:when>
        <xsl:when test="'arc' = $code or contains(lower-case($code), 'aramaic')">arc</xsl:when>
        <xsl:when test="'cop' = $code or contains(lower-case($code), 'coptic')">egy-Copt</xsl:when>
        <xsl:when test="'dem' = $code or contains(lower-case($code), 'demotic')">egy-Egyd</xsl:when>
        <xsl:when test="'egy' = $code">egy</xsl:when>
        <xsl:when test="'fas' = $code or contains(lower-case($code), 'persian')">fas</xsl:when>
        <xsl:when test="'grc' = $code or contains(lower-case($code), 'greek')">grc</xsl:when>
        <xsl:when test="'heb' = $code or contains(lower-case($code), 'hebrew')">he-Hebr</xsl:when>
        <xsl:when test="'hir' = $code or contains(lower-case($code), 'hieratic')">egy-Egyh</xsl:when>
        <xsl:when test="'hig' = $code or contains(lower-case($code), 'hieroglyph')">egy-Egyp</xsl:when>
        <xsl:when test="'la' = $code or 'lat' = $code or contains(lower-case($code), 'latin')">la</xsl:when>
        <xsl:when test="'pal' = $code or contains(lower-case($code), 'middle persian')">pal-Phil</xsl:when>
        <xsl:when test="'ira' = $code or contains(lower-case($code), 'parthian')">xpr-Prti</xsl:when>
        <xsl:when test="'sem' = $code or contains(lower-case($code), 'semitic')">sem</xsl:when>
        <xsl:when test="'und' = $code or contains(lower-case($code), 'undetermined') or contains(lower-case($code), 'uncertain') or contains(lower-case($code), 'unknown')">und</xsl:when></xsl:choose>
    </xsl:template>
    
    <xsl:template name="formatDate">
        <xsl:param name="date"/>
        <xsl:param name="scheme"/>
        <xsl:if test="$scheme = 'b' and number($date) gt 0">-</xsl:if><xsl:choose>
            <xsl:when test="number($date) = 0">0001</xsl:when>
            <xsl:when test="number($date)"><xsl:number format="0001" value="$date"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="$date"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
  
    <xsl:template name="formatDateEra">
        <xsl:param name="date"/>
        <xsl:param name="scheme"/>
        <xsl:choose>
            <xsl:when test="number($date) = 0">1</xsl:when>
            <xsl:otherwise><xsl:value-of select="$date"/></xsl:otherwise>
        </xsl:choose>
      <xsl:text> </xsl:text>
      <xsl:choose>
        <xsl:when test="$scheme = 'b' and number($date) gt 0">BCE</xsl:when>
        <xsl:otherwise>CE</xsl:otherwise>
      </xsl:choose>
    </xsl:template>
  
</xsl:stylesheet>
