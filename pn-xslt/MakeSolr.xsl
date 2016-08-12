<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/terms/"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:pi="http://papyri.info/ns"
  xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:t="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs dc rdf pi tei t xd" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  version="2.0">

  <xsl:import href="pi-global-varsandparams.xsl"/>

  <xsl:import href="../epidoc-xslt/txt-teiab.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teiapp.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teidiv.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teidivedition.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teigap.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teihead.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teilgandl.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teilistanditem.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teilistbiblandbibl.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teimilestone.xsl"/> 
  <xsl:import href="../epidoc-xslt/txt-teinote.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teip.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teispace.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teisupplied.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-teiref.xsl"/>
  <xsl:import href="../epidoc-xslt/teiabbrandexpan.xsl"/>
  <xsl:import href="../epidoc-xslt/teiaddanddel.xsl"/>
  <xsl:import href="../epidoc-xslt/teichoice.xsl"/>
  <xsl:import href="../epidoc-xslt/teiheader.xsl"/>
  <xsl:import href="../epidoc-xslt/teihi.xsl"/>
  <xsl:import href="../epidoc-xslt/teimilestone.xsl"/>
  <xsl:import href="../epidoc-xslt/teinum.xsl"/>
  <xsl:import href="../epidoc-xslt/teiorig.xsl"/>
  <xsl:import href="../epidoc-xslt/teiorigandreg.xsl"/>
  <xsl:import href="../epidoc-xslt/teiq.xsl"/>
  <xsl:import href="../epidoc-xslt/teiseg.xsl"/>
  <xsl:import href="../epidoc-xslt/teisicandcorr.xsl"/>
  <xsl:import href="../epidoc-xslt/teispace.xsl"/>
  <xsl:import href="../epidoc-xslt/teisupplied.xsl"/>
  <xsl:import href="../epidoc-xslt/teiunclear.xsl"/>
  <xsl:import href="../epidoc-xslt/tpl-apparatus.xsl"/>
  <xsl:import href="../epidoc-xslt/txt-tpl-linenumberingtab.xsl"/>
  <xsl:import href="../epidoc-xslt/tpl-reasonlost.xsl"/>
  <xsl:import href="../epidoc-xslt/tpl-certlow.xsl"/>
  <xsl:import href="../epidoc-xslt/tpl-text.xsl"/>
  <xsl:import href="../epidoc-xslt/teisurplus.xsl"/>
  
  <xsl:include href="pi-txt-teilb.xsl"/>
  <xsl:include href="orig-supplied.xsl"/>
  <xsl:include href="reg-surplus.xsl"/>
  <xsl:include href="orig-teiabbrandexpan.xsl"/>
  <xsl:include href="reg-teiabbrandexpan.xsl"/>

  <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes"/>

  <xsl:param name="collection"/>
  <xsl:param name="related"/>
  <xsl:param name="images"/>
  <xsl:variable name="relations" select="tokenize($related, ' ')"/>
  <xsl:variable name="path">/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase"/>
  <xsl:variable name="tmbase">/srv/data/papyri.info/TM/files</xsl:variable>
  <xsl:variable name="line-inc">5</xsl:variable>
  <xsl:variable name="resolve-uris" select="false()"/>
  <xsl:variable name="ddbdp" select="$collection = 'ddbdp'"/>
  <xsl:variable name="hgv" select="$collection = 'hgv' or contains($related, 'hgv/')"/>
  <xsl:variable name="apis" select="$collection = 'apis' or contains($related, '/apis/')"/>
  <xsl:variable name="dclp" select="$collection = 'dclp'"/>
  <xsl:include href="pi-functions.xsl"/>

  <xsl:template match="/">
    <xsl:variable name="translation"
      select="contains($related, 'hgvtrans') or (contains($related, '/apis/') and pi:get-docs($relations[contains(., '/apis/')], 'xml')//t:div[@type = 'translation'])"/>
    <add>
      <doc>
        <field name="project">IDP</field>
        <xsl:if test="$ddbdp = true()">
          <field name="collection">ddbdp</field>
        </xsl:if>
        <xsl:if test="$hgv = true()">
          <field name="collection">hgv</field>
        </xsl:if>
        <xsl:if test="$apis = true()">
          <field name="collection">apis</field>
        </xsl:if>
        <xsl:if test="$dclp = true()">
          <field name="collection">dclp</field>
        </xsl:if>
        <xsl:variable name="id"><xsl:value-of select="pi:get-identifier($collection, /t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt)"></xsl:value-of></xsl:variable>
        <xsl:choose>

          <!-- D C L P -->

          <xsl:when test="$collection = 'dclp'">
            <field name="id"><xsl:value-of select="$id"/></field>
            <xsl:call-template name="idnos">
              <xsl:with-param name="idnos" select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[not(@type = ('dclp', 'filename', 'herc-fr'))]"/>
            </xsl:call-template>
            <xsl:call-template name="text"/>
            <xsl:call-template name="languages"/>
            <xsl:call-template name="facetfields">
              <xsl:with-param name="docs" select="."/>
              <xsl:with-param name="alterity">self</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="revision-history">
              <xsl:with-param name="docs" select="."/>
            </xsl:call-template>
            <xsl:call-template name="metadata-dclp"/>
            <xsl:call-template name="images"/>
          </xsl:when>

          <xsl:when test="$collection = 'ddbdp'">
            <field name="id"><xsl:value-of select="$id"/></field>
            <xsl:call-template name="idnos">
              <xsl:with-param name="idnos" select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type != 'HGV']"/>
            </xsl:call-template>
            <xsl:for-each select="$relations">
              <field name="identifier">
                <xsl:value-of select="."/>
              </field>
            </xsl:for-each>
            <xsl:for-each
              select="t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'HGV']">
              <xsl:for-each select="tokenize(., ' ')">
                <field name="identifier">http://papyri.info/hgv/<xsl:value-of select="."/></field>
              </xsl:for-each>
            </xsl:for-each>
            <xsl:call-template name="text"/>
            <xsl:call-template name="languages"/>
            <xsl:call-template name="facetfields">
              <xsl:with-param name="docs" select="/"/>
              <xsl:with-param name="alterity">self</xsl:with-param>
            </xsl:call-template>
            <xsl:choose>
              <xsl:when test="$hgv or $apis">
                <xsl:call-template name="facetfields">
                  <xsl:with-param name="docs"
                    select="pi:get-docs($relations[contains(., 'hgv/')], 'xml')"/>
                  <xsl:with-param name="alterity">other</xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="facetfields">
                  <xsl:with-param name="docs"
                    select="pi:get-docs($relations[contains(., '/apis/')][1], 'xml')"/>
                  <xsl:with-param name="alterity">other</xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="metadata">
                  <xsl:with-param name="hgv-docs"
                    select="pi:get-docs($relations[contains(., 'hgv/')], 'xml')"/>
                  <xsl:with-param name="apis-docs"
                    select="pi:get-docs($relations[contains(., '/apis/')], 'xml')"/>
                  <xsl:with-param name="tm-docs" 
                    select="pi:get-docs($relations[contains(.,'trismegistos.org')], 'xml')"/>
                  <xsl:with-param name="docs"
                    select="pi:get-docs($relations[contains(., 'hgv/') or contains(., '/apis/')], 'xml')"
                  />
                </xsl:call-template>
                <xsl:call-template name="translation">
                  <xsl:with-param name="docs"
                    select="pi:get-docs($relations[contains(., 'hgv/') or contains(., '/apis/') or contains(., '/hgvtrans/')], 'xml')"
                  />
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <field name="unknown_date_flag">true</field>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="images">
              <xsl:with-param name="docs"
                select="pi:get-docs($relations[contains(., 'hgv/') or contains(., '/apis/')], 'xml')"
              />
            </xsl:call-template>
            <xsl:call-template name="revision-history">
                <xsl:with-param name="docs" select="pi:get-docs($relations[contains(., 'hgv/') or contains(., '/apis/')], 'xml') union /"></xsl:with-param>        
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="$collection = 'hgv'">
            <field name="id"><xsl:value-of select="$id"/></field>
            <xsl:for-each
              select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'TM']">
              <field name="identifier">
                <xsl:value-of select="."/>
              </field>
            </xsl:for-each>
            <xsl:call-template name="facetfields">
              <xsl:with-param name="docs" select="/"/>
              <xsl:with-param name="alterity">self</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="facetfields">
              <xsl:with-param name="docs"
                select="pi:get-docs($relations[contains(., '/apis/')][1], 'xml')"/>
              <xsl:with-param name="alterity">other</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="metadata">
              <xsl:with-param name="hgv-docs" select="/">
              </xsl:with-param>
              <xsl:with-param name="apis-docs"
                select="pi:get-docs($relations[contains(., '/apis/')], 'xml')"/>
              <xsl:with-param name="tm-docs" 
                select="pi:get-docs($relations[contains(.,'trismegistos.org')], 'xml')"/>
              <xsl:with-param name="docs"
                select="pi:get-docs($relations[contains(., '/apis/')], 'xml')"
              />
            </xsl:call-template>
            <xsl:call-template name="images">
              <xsl:with-param name="docs"
                select="pi:get-docs($relations[contains(., '/apis/')], 'xml')"/>
            </xsl:call-template>
            <xsl:call-template name="revision-history">
              <xsl:with-param name="docs" select="pi:get-docs($relations[contains(., 'hgv/') or contains(., '/apis/')], 'xml'), /"></xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="$collection = 'apis'">
            <field name="id"><xsl:value-of select="$id"/></field>
            <xsl:call-template name="facetfields">
              <xsl:with-param name="docs" select="/"/>
              <xsl:with-param name="alterity">self</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="metadata">
              <xsl:with-param name="hgv-docs" select="/"/>
              <xsl:with-param name="apis-docs" select="/"/>
              <xsl:with-param name="tm-docs" 
                select="pi:get-docs($relations[contains(.,'trismegistos.org')], 'xml')"/>
              <xsl:with-param name="docs" select="/"/>
            </xsl:call-template>
            <xsl:call-template name="translation">
              <xsl:with-param name="docs" select="/"/>
            </xsl:call-template>
            <xsl:for-each
              select="/t:TEI/t:teiHeader/t:profileDesc/t:langUsage/t:language[@ident != 'en']">
              <field name="language">
                <xsl:value-of select="normalize-space(.)"/>
              </field>
              <field name="facet_language">
                <xsl:value-of select="@ident"/>
              </field>
            </xsl:for-each>
            <xsl:call-template name="images"/>
            <xsl:call-template name="revision-history">
              <xsl:with-param name="docs" select="pi:get-docs($relations[contains(., 'hgv/') or contains(., '/apis/')], 'xml') union /"></xsl:with-param>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
      </doc>
    </add>
  </xsl:template>

  <xsl:template name="languages">
    <xsl:variable name="languages" select="distinct-values(//t:div[@type='edition']/descendant-or-self::*/@xml:lang)"/>
    <xsl:for-each select="//t:langUsage/t:language">
      <xsl:if test="index-of($languages, string(@ident))">
        <field name="language">
          <xsl:value-of select="."/>
        </field>
        <field name="facet_language">
          <xsl:value-of select="string(@ident)"/>
        </field>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="text">
    <xsl:variable name="text">
      <xsl:variable name="temp-reg-edition">
        <xsl:element name="reg-edition-wrapper">
          <xsl:copy-of select="//t:div[@type='edition']"></xsl:copy-of>
        </xsl:element>
      </xsl:variable>
      <xsl:call-template name="reg-text-processing">
        <xsl:with-param name="temp-node" select="$temp-reg-edition"></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="temp-orig-edition">
      <xsl:element name="orig-edition-wrapper">
        <xsl:copy-of select="//t:div[@type = 'edition']"></xsl:copy-of>
      </xsl:element>
    </xsl:variable>
    <xsl:variable name="orig-text">
      <xsl:call-template name="orig-text-processing">
        <xsl:with-param name="temp-node" select="$temp-orig-edition"></xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="textnfc"
      select="normalize-space(replace(replace(translate($text, '·[]{},.-()+^?̣&lt;&gt;*&#xD;\\/〚〛ʼ', ''),'&#xA0;', ''), 'ς', 'σ'))"/>
    <xsl:variable name="textnfd" select="normalize-unicode($textnfc, 'NFD')"/>
    <xsl:variable name="orignfc" select="normalize-space(replace(replace(translate($orig-text, '·[]{},.-()+^?̣&lt;&gt;*&#xD;\\/〚〛ʼ', ''),'&#xA0;', ''), 'ς', 'σ'))"></xsl:variable>
    <xsl:variable name="orignfd" select="normalize-unicode($orignfc, 'NFD')"></xsl:variable>
    <field name="transcription">
      <xsl:value-of select="replace($textnfc, $abbreviation-marker, '')"/>
    </field>
    <field name="transcription">
      <xsl:value-of select="$orignfc"></xsl:value-of>
    </field>
    <field name="transcription_id">
      <xsl:value-of
        select="replace(replace($textnfd, '[\p{IsCombiningDiacriticalMarks}]', ''), $abbreviation-marker, '')"
      />
    </field>
    <field name="transcription_id">
      <!--      <xsl:value-of select="translate($orignfd, '[\p{IsCombiningDiacriticalMarks}]', '')"></xsl:value-of> -->
      <xsl:value-of select="replace($orignfd, '[\p{IsCombiningDiacriticalMarks}]', '')"></xsl:value-of>
    </field>
    <field name="transcription_ic">
      <xsl:value-of select="replace(lower-case($textnfc), $abbreviation-marker, '')"/>
    </field>
    <field name="transcription_ic">
      <xsl:value-of select="lower-case($orignfc)"></xsl:value-of>
    </field>
    <xsl:variable name="transcription_ia" select="replace(lower-case(replace($textnfd, '[\p{IsCombiningDiacriticalMarks}]', '')), $abbreviation-marker, '')"></xsl:variable>
    <field name="transcription_ia">
      <xsl:value-of select="$transcription_ia"/>
    </field>
    <field name="transcription_ia">
      <xsl:value-of select="lower-case(replace($orignfd, '[\p{IsCombiningDiacriticalMarks}]', ''))"></xsl:value-of>
    </field>
    <field name="transcription_ngram">
      <xsl:for-each select="tokenize($textnfc, '\s+')">
        <xsl:variable name="word-start-boundary" select="pi:get-word-start-boundary-char(.)"></xsl:variable>
        <xsl:variable name="word-end-boundary"   select="pi:get-word-end-boundary-char(.)"></xsl:variable>
        <xsl:if test=". != ''"><xsl:value-of select="$word-start-boundary"></xsl:value-of><xsl:value-of select="replace(., $abbreviation-marker, '')"/><xsl:value-of select="$word-end-boundary"></xsl:value-of><xsl:text> </xsl:text></xsl:if>
      </xsl:for-each>
    </field>
    <field name="transcription_ngram">
      <xsl:for-each select="tokenize($orignfc, '\s+')">
        <xsl:if test=". != ''"><xsl:text>#</xsl:text><xsl:value-of select="."/><xsl:text># </xsl:text></xsl:if>
      </xsl:for-each>
    </field>
    <field name="transcription_ngram_id">
      <xsl:for-each select="tokenize(replace($textnfd, '[\p{IsCombiningDiacriticalMarks}]', ''), '\s+')">
        <xsl:variable name="word-start-boundary" select="pi:get-word-start-boundary-char(.)"></xsl:variable>
        <xsl:variable name="word-end-boundary"   select="pi:get-word-end-boundary-char(.)"></xsl:variable>
        <xsl:if test=". != ''"><xsl:value-of select="$word-start-boundary"></xsl:value-of><xsl:value-of select="replace(., $abbreviation-marker, '')"/><xsl:value-of select="$word-end-boundary"></xsl:value-of><xsl:text> </xsl:text></xsl:if>
      </xsl:for-each>
    </field>
    <field name="transcription_ngram_id">
      <xsl:for-each select="tokenize(replace($orignfd, '[\p{IsCombiningDiacriticalMarks}]', ''), '\s+')">
        <xsl:if test=". != ''"><xsl:text>#</xsl:text><xsl:value-of select="."/><xsl:text># </xsl:text></xsl:if>
      </xsl:for-each>
    </field>
    <field name="transcription_ngram_ic">
      <xsl:for-each select="tokenize(lower-case($textnfc), '\s+')">
        <xsl:variable name="word-start-boundary" select="pi:get-word-start-boundary-char(.)"></xsl:variable>
        <xsl:variable name="word-end-boundary"   select="pi:get-word-end-boundary-char(.)"></xsl:variable>
        <xsl:if test=". != ''"><xsl:value-of select="$word-start-boundary"></xsl:value-of><xsl:value-of select="replace(., $abbreviation-marker, '')"/><xsl:value-of select="$word-end-boundary"></xsl:value-of><xsl:text> </xsl:text></xsl:if>
      </xsl:for-each>
    </field>
    <field name="transcription_ngram_ic">
      <xsl:for-each select="tokenize(lower-case($orignfc), '\s+')">
        <xsl:if test=". != ''"><xsl:text>#</xsl:text><xsl:value-of select="."/><xsl:text># </xsl:text></xsl:if>
      </xsl:for-each>
    </field>
    <field name="transcription_ngram_ia">
      <xsl:for-each
        select="tokenize(lower-case(replace($textnfd, '[\p{IsCombiningDiacriticalMarks}]', '')), '\s+')">
        <xsl:if test="string-length(normalize-space(.)) &gt; 0">
          <xsl:variable name="word-start-boundary" select="pi:get-word-start-boundary-char(.)"></xsl:variable>
          <xsl:variable name="word-end-boundary"   select="pi:get-word-end-boundary-char(.)"></xsl:variable>
          <xsl:value-of select="$word-start-boundary"></xsl:value-of>
          <xsl:value-of select="replace(., $abbreviation-marker, '')"/>
          <xsl:value-of select="$word-end-boundary"></xsl:value-of><xsl:text> </xsl:text>
        </xsl:if>
      </xsl:for-each>
    </field>
    <field name="transcription_ngram_ia">
      <xsl:for-each
        select="tokenize(lower-case(replace($orignfd, '[\p{IsCombiningDiacriticalMarks}]', '')), '\s+')">
        <xsl:if test="string-length(normalize-space(.)) &gt; 0">
          <xsl:text>#</xsl:text>
          <xsl:value-of select="normalize-space(.)"/>
          <xsl:text># </xsl:text>
        </xsl:if>
      </xsl:for-each>
    </field>
    <xsl:if test="string-length($textnfd) > 0">
      <field name="has_transcription">true</field>
    </xsl:if>
  </xsl:template>

  <xsl:template name="idnos">
    <xsl:param name="idnos"/>
    <xsl:for-each select="$idnos">
      <xsl:choose>
        <xsl:when test="@type='ddb-hybrid'">
          <field name="identifier">http://papyri.info/ddbdp/<xsl:value-of select="." /></field>
        </xsl:when>
        <xsl:when test="@type='TM'">
          <field name="identifier">http://www.trismegistos.org/tm/detail.php?quick=<xsl:value-of select="." /></field>
        </xsl:when>
        <xsl:when test="@type='LDAB'">
          <field name="identifier">http://www.trismegistos.org/ldab/text.php?quick=<xsl:value-of select="." /></field>
        </xsl:when>
        <xsl:when test="@type='dclp-hybrid'">
          <field name="identifier">http://papyri.info/dclp/<xsl:value-of select="." /></field>
        </xsl:when>
        <xsl:when test="@type='apisid'">
          <field name="identifier">http://papyri.info/apis/<xsl:value-of select="." /></field>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="contains(., ' ')">
              <xsl:for-each select="tokenize(., ' ')">
                <field name="identifier">
                  <xsl:value-of select="."/>
                </field>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <field name="identifier">
                <xsl:value-of select="."/>
              </field>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="facetfields">
    <xsl:param name="docs"/>
    <xsl:param name="alterity"/>
    <xsl:choose>
      <!-- cl: mehrere dclp-hybrids bzw. principal editions -->
      <xsl:when test="$collection = 'dclp'">
        <xsl:variable name="triforce" select="tokenize(if($docs//t:idno[@type='dclp-hybrid'])then($docs//t:idno[@type='dclp-hybrid'][1])else(concat('na;;', $docs//t:idno[@type='TM'])), ';')"/>
        <field name="hgv_identifier">
          <xsl:value-of select="string($docs//t:idno[@type='TM'])"/>
        </field>
        <field name="hgv_metadata">
          <xsl:value-of select="normalize-space(replace($docs, '&#xa;', ' '))"/>
        </field>
        <field name="hgv_series">
          <xsl:value-of select="$triforce[1]"/>
        </field>
        <field name="hgv_volume">
          <xsl:value-of select="$triforce[2]"/>
        </field>
        <field name="hgv_full_identifier">
          <xsl:value-of select="$triforce[3]"/>
        </field>
        <field name="dclp_identifier">
          <xsl:value-of select="string($docs//t:idno[@type='TM'])"/>
        </field>
        <field name="dclp_metadata">
          <xsl:value-of select="normalize-space(replace($docs, '\n', ' '))"/>
        </field>
        <field name="dclp_series">
          <xsl:value-of select="$triforce[1]"/>
        </field>
        <field name="dclp_volume">
          <xsl:value-of select="$triforce[2]"/>
        </field>
        <field name="dclp_full_identifier">
          <xsl:value-of select="$triforce[3]"/>
        </field>
        <field name="series">
          <xsl:value-of select="$triforce[1]"/>
        </field>
        <field name="volume">
          <xsl:value-of select="if(string($triforce[2]))then(replace($triforce[2], '\D', ''))else(0)"/>
        </field>
        <field name="item">
          <xsl:value-of select="replace($triforce[3], '\D', '')"/>
        </field>
      </xsl:when>
      <xsl:when
        test="$docs[1]//t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition']//t:bibl[@type = 'publication'][@subtype = 'principal']">
        <!-- IFF HGV document -->
        <xsl:variable name="hgv_identifiers">
          <xsl:perform-sort
            select="$docs//t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']">
            <xsl:sort select="."/>
          </xsl:perform-sort>
        </xsl:variable>
        <field name="hgv_identifier">
          <xsl:choose>
            <xsl:when test="count($hgv_identifiers//*) gt 1">
              <xsl:value-of select="$hgv_identifiers//*[1]"/> - <xsl:value-of
                select="$hgv_identifiers//*[position() = last()]"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$hgv_identifiers[1]"/>
            </xsl:otherwise>
          </xsl:choose>
        </field>
        <xsl:for-each select="$docs[1]//t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition']//t:bibl[@type = 'publication'][@subtype = 'principal']">
         <xsl:variable name="hgv_series">
           <xsl:value-of select="replace(normalize-space(t:title[@level = 's']), ' ', '_')" />
         </xsl:variable>
         <xsl:variable name="hgv_volume">
           <xsl:variable name="hgv_volprep" select="replace(normalize-space(t:biblScope[@type = 'volume']), ' ', '_')"/>
           <xsl:choose>
             <xsl:when test="string-length($hgv_volprep) = 0">0</xsl:when>
             <xsl:otherwise>
               <xsl:value-of select="$hgv_volprep"/>
             </xsl:otherwise>
           </xsl:choose>
         </xsl:variable>
         <xsl:variable name="hgv_numbers" select="replace(normalize-space(t:biblScope[@type = 'numbers']), ' ', '_')"/>
         <xsl:variable name="hgv_lines"
           select="normalize-space(t:biblScope[@type = 'lines'])"/>
         <xsl:variable name="hgv_item"
           select="normalize-space(concat($hgv_numbers, ' ', $hgv_lines))"/>
         <xsl:variable name="hgv_item_letter">
           <xsl:value-of select="replace($hgv_item, '\d', '')"/>
         </xsl:variable>
         <field name="hgv_series">
           <xsl:value-of select="$hgv_series"/>
         </field>
         <field name="hgv_volume">
           <xsl:value-of select="$hgv_volume"/>
         </field>
         <field name="hgv_full_identifier">
           <xsl:value-of select="$hgv_item"/>
         </field>
         <field name="series_led_path">
           <xsl:value-of select="string-join(($hgv_series, $hgv_volume, $hgv_item, 'hgv'), ';')"/>
         </field>
         <field name="volume_led_path">
           <xsl:value-of select="string-join(($hgv_volume, $hgv_item, $hgv_series, 'hgv'), ';')"/>
         </field>
         <field name="idno_led_path">
           <xsl:value-of select="string-join(($hgv_item, $hgv_series, $hgv_volume, 'hgv'), ';')"/>
         </field>
         <field name="hgv_item">
           <xsl:choose>
             <xsl:when test="string-length(replace($hgv_numbers, '\D', '')) > 0">
               <xsl:value-of select="replace($hgv_numbers, '\D', '')"/>
             </xsl:when>
             <xsl:otherwise>0</xsl:otherwise>
           </xsl:choose>
         </field>
         <xsl:if test="string-length($hgv_item_letter) > 0">
           <field name="hgv_item_letter">
             <xsl:value-of select="$hgv_item_letter"/>
           </field>
         </xsl:if>
         <xsl:if test="$alterity = 'self'">
           <field name="series">
             <xsl:value-of select="lower-case($hgv_series)"/>
           </field>
           <xsl:variable name="volume" select="replace($hgv_volume, '\D', '')"/>
           <field name="volume">
             <xsl:choose>
               <xsl:when test="string-length($volume) &gt; 0">
                 <xsl:value-of select="$volume"/>
               </xsl:when>
               <xsl:otherwise>0</xsl:otherwise>
             </xsl:choose>
           </field>
           <xsl:variable name="item" select="replace($hgv_item, '\D', '')"/>
           <field name="item">
             <xsl:choose>
               <xsl:when test="string-length($item) &gt; 0">
                 <xsl:value-of select="$item"/>
               </xsl:when>
               <xsl:otherwise>0</xsl:otherwise>
             </xsl:choose>
           </field>
         </xsl:if>
        </xsl:for-each>
      </xsl:when>
      <xsl:when
        test="$docs[1]/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'ddb-hybrid'] and $alterity = 'self'">
        <!-- DDBDP document -->
        <xsl:variable name="sort"
          select="tokenize($docs[1]/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'ddb-hybrid'], ';')"/>
        <xsl:variable name="ddbdp_volume">
          <xsl:choose>
            <xsl:when test="string-length($sort[2]) = 0">0</xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="normalize-space($sort[2])"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="ddbdp_series">
          <xsl:value-of select="normalize-space($sort[1])"/>
        </xsl:variable>
        <xsl:variable name="ddbdp_item">
          <xsl:choose>
            <xsl:when test="string-length(replace(normalize-space($sort[3]), '\D', '')) = 0"
              >0</xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="replace(normalize-space($sort[3]), '\D', '')"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="ddbdp_item_letter">
          <xsl:value-of select="replace(normalize-space($sort[3]), '\d', '')"/>
        </xsl:variable>
        <field name="ddbdp_series">
          <xsl:value-of select="$ddbdp_series"/>
        </field>
        <field name="ddbdp_volume">
          <xsl:value-of select="$ddbdp_volume"/>
        </field>
        <xsl:variable name="ddbdp_full_identifier" select="normalize-space($sort[3])"/>
        <field name="ddbdp_full_identifier">
          <xsl:value-of select="$ddbdp_full_identifier"/>
        </field>
        <field name="ddbdp_item">
          <xsl:value-of select="$ddbdp_item"/>
        </field>
        <xsl:if test="string-length($ddbdp_item_letter) > 0">
          <field name="ddbdp_item_letter">
            <xsl:value-of select="$ddbdp_item_letter"/>
          </field>
        </xsl:if>
        <field name="series_led_path">
          <xsl:value-of select="string-join(($ddbdp_series, $ddbdp_volume, $ddbdp_full_identifier, 'ddbdp'), ';')"/>
        </field>
        <field name="volume_led_path">
          <xsl:value-of select="string-join(($ddbdp_volume, $ddbdp_full_identifier, $ddbdp_series, 'ddbdp'), ';')"/>
        </field>
        <field name="idno_led_path">
          <xsl:value-of select="string-join(($ddbdp_full_identifier, $ddbdp_series, $ddbdp_volume, 'ddbdp'), ';')"/>
        </field>
        <xsl:if test="$alterity = 'self'">
          <field name="series">
            <xsl:value-of select="$ddbdp_series"/>
          </field>
          <field name="volume">
            <xsl:choose>
              <xsl:when test="string-length(replace($ddbdp_volume, '\D', '')) = 0">0</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="replace($ddbdp_volume, '\D', '')"/>
              </xsl:otherwise>
            </xsl:choose>
          </field>
          <field name="item">
            <xsl:choose>
              <xsl:when test="string-length(replace($ddbdp_item, '\D', '')) &gt; 0">
                <xsl:value-of select="replace($ddbdp_item, '\D', '')"/>
              </xsl:when>
              <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>
          </field>
        </xsl:if>
      </xsl:when>
      <xsl:when
        test="$docs[1]//t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'apisid']">
        <!-- APIS document -->
        <xsl:variable name="apis_series">
          <xsl:value-of
            select="normalize-space(substring-before($docs[1]//t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'apisid'], '.'))"
          />
        </xsl:variable>
        <xsl:variable name="apis_item">
          <xsl:value-of
            select="normalize-space(substring-after($docs[1]//t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'apisid'], 'apis.'))"
          />
        </xsl:variable>
        <xsl:variable name="apis_item_letter">
          <xsl:value-of select="replace($apis_item, '\d', '')"/>
        </xsl:variable>
        <field name="apis_series">
          <xsl:value-of select="$apis_series"/>
        </field>
        <field name="apis_full_identifier">
          <xsl:value-of select="$apis_item"/>
        </field>
        <field name="apis_item">
          <xsl:choose>
            <xsl:when test="string-length(replace($apis_item, '\D', '')) > 0">
              <xsl:value-of select="replace($apis_item, '\D', '')"/>
            </xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
          </xsl:choose>
        </field>
        <xsl:if test="string-length($apis_item_letter) > 0">
          <field name="apis_item_letter">
            <xsl:value-of select="$apis_item_letter"/>
          </field>
        </xsl:if>
        <xsl:for-each
          select="$docs[1]//t:TEI/t:text/t:body/t:div[@type='bibliography' and @subtype = 'citations']/t:listBibl/t:bibl[@type='ddbdp'][1]">
          <field name="apis_publication_id">
            <xsl:value-of select="replace(., ':', ' ')"/>
          </field>
          <xsl:variable name="apis-pubid" select="."></xsl:variable>
          <field name="series_led_path">
            <xsl:value-of select="string-join(($apis_series, '0', $apis-pubid, 'apis'), ';')"/>
          </field>
          <field name="volume_led_path">
            <xsl:value-of select="string-join(('0', $apis-pubid, $apis_series, 'apis'), ';')"/>
          </field>
          <field name="idno_led_path">
            <xsl:value-of select="string-join(($apis-pubid, $apis_series, '0', 'apis'), ';')"/>
          </field>
        </xsl:for-each>
        <field name="series_led_path">
          <xsl:value-of select="string-join(($apis_series, '0', $apis_item, 'apis'), ';')"/>
        </field>
        <field name="volume_led_path">
          <xsl:value-of select="string-join(('0', $apis_item, $apis_series, 'apis'), ';')"/>
        </field>
        <field name="idno_led_path">
          <xsl:value-of select="string-join(($apis_item, $apis_series, '0', 'apis'), ';')"/>
        </field>
        <xsl:variable name="apis-inventory" select="$docs[1]//t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno"></xsl:variable>
        <field name="apis_inventory">
          <xsl:value-of select="$apis-inventory"/>
        </field>
        <field name="series_led_path">
          <xsl:value-of select="string-join(($apis_series, '0', $apis-inventory, 'apis'), ';')"/>
        </field>
        <field name="volume_led_path">
          <xsl:value-of select="string-join(('0', $apis-inventory, $apis_series, 'apis'), ';')"/>
        </field>
        <field name="idno_led_path">
          <xsl:value-of select="string-join(($apis-inventory, $apis_series, '0', 'apis'), ';')"/>
        </field>
        <xsl:if test="$alterity = 'self'">
          <field name="series">
            <xsl:value-of select="$apis_series"/>
          </field>
          <field name="volume">0</field>
          <field name="item">
            <xsl:choose>
              <xsl:when test="string-length(replace($apis_item, '\D', '')) &gt; 0">
                <xsl:value-of select="replace($apis_item, '\D', '')"/>
              </xsl:when>
              <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>
          </field>
        </xsl:if>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="metadata-dclp">
    <xsl:call-template name="title">
      <xsl:with-param name="hgv-docs" select="."/>
      <xsl:with-param name="apis-docs" select="."/>
      <xsl:with-param name="docs" select="."/>
    </xsl:call-template>
    <xsl:call-template name="display-place">
      <xsl:with-param name="docs" select="."/>
    </xsl:call-template>
    <xsl:call-template name="date">
      <xsl:with-param name="hgv-docs" select="."/>
      <xsl:with-param name="docs" select="."/>
    </xsl:call-template>
    <field name="dclp_metadata">
      <xsl:value-of select="normalize-space(/)"/>
    </field>
    <xsl:call-template name="place">
      <xsl:with-param name="docs" select="."/>
    </xsl:call-template>
    <xsl:call-template name="inventory-number"/>
  </xsl:template>

  <xsl:template name="metadata">
    <xsl:param name="hgv-docs"/>
    <xsl:param name="apis-docs"/>
    <xsl:param name="tm-docs"/>
    <xsl:param name="docs"/>
    <xsl:call-template name="title">
      <xsl:with-param name="hgv-docs"><xsl:copy-of select="$hgv-docs"></xsl:copy-of></xsl:with-param>
      <xsl:with-param name="apis-docs"><xsl:copy-of select="$apis-docs"></xsl:copy-of></xsl:with-param>
      <xsl:with-param name="docs"><xsl:copy-of select="$docs"></xsl:copy-of></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="display-place">
      <xsl:with-param name="docs"><xsl:copy-of select="$hgv-docs"></xsl:copy-of></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="date">
      <xsl:with-param name="hgv-docs"><xsl:copy-of select="$hgv-docs"></xsl:copy-of></xsl:with-param>
      <xsl:with-param name="docs"><xsl:copy-of select="$docs"></xsl:copy-of></xsl:with-param>
    </xsl:call-template>

    <field name="hgv_metadata">
      <!-- Title -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:titleStmt/t:title, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Summary -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:summary, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Publications -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition'], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Inv. Id -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Physical Desc. -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:p, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Post-concordance BL Entries -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'corrections'], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Translations -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'translations'], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Provenance -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/(t:origPlace|t:p[t:placeName/@type='ancientFindspot']), ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Material -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:support/t:material, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Language -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItem/t:textLang, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Date -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Commentary -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:text/t:body/t:div[@type='commentary']/t:p, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Notes (general|lines|palaeography|recto/verso|conservation|preservation) -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItem/t:note, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Print Illustrations -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'illustrations'][.//t:bibl], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Subjects -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:profileDesc/t:textClass/t:keywords, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Associated Names -->
      <xsl:value-of
        select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin[t:persName/@type = 'asn'], ' '))"/>
      <xsl:text> </xsl:text>

    </field>

    <field name="apis_metadata">
      <!-- Title -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:titleStmt/t:title, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Summary -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:summary, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Publications -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition'], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Inv. Id -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Physical Desc. -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:p, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Post-concordance BL Entries -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'corrections'], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Translations -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'translations'], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Provenance -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/(t:origPlace|t:p[t:placeName/@type='ancientFindspot']), ' '))"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:provenance/t:p, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Material -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:support, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Language -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItem/t:textLang, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Date -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Commentary -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:text/t:body/t:div[@type='commentary']/t:p, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Notes (general|lines|palaeography|recto/verso|conservation|preservation) -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItem/t:note, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Print Illustrations -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'illustrations'][.//t:bibl], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Subjects -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:profileDesc/t:textClass/t:keywords, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Associated Names -->
      <xsl:value-of
        select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin[t:persName/@type = 'asn'], ' '))"/>
      <xsl:text> </xsl:text>
    </field>
    
    <field name="tm_metadata">
      <xsl:for-each select="$tm-docs/text">
        <xsl:value-of select="field[@n='6']"/><xsl:text> </xsl:text>
        <xsl:value-of select="replace(field[@n='8'],'&lt;br&gt;',' ')"/><xsl:text> </xsl:text>
        <!-- Inventory Number -->
        <xsl:value-of select="collref[starts-with(field[@n='15'],'1.')]/field[@n='14']"/><xsl:text> </xsl:text>
        <xsl:for-each select="collref[starts-with(field[@n='15'],'2.')]">
          <xsl:value-of select="field[@n='14']"/><xsl:if test="following-sibling::collref[not(starts-with(field[@n='15'],'1.'))]"><xsl:text> </xsl:text></xsl:if>
        </xsl:for-each>
        <xsl:for-each select="collref[starts-with(field[@n='15'],'3.')]">
          <xsl:value-of select="field[@n='14']"/><xsl:if test="following-sibling::collref[starts-with(field[@n='15'],'3.')]"><xsl:text> </xsl:text></xsl:if>
        </xsl:for-each>
        <!-- Reuse -->
        <xsl:if test="string-length(field[@n='13']) gt 0">
          <xsl:value-of select="field[@n='13']"/><xsl:text> </xsl:text>
          <xsl:for-each select="tokenize(field[@n='14'], ', ')"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>
          <xsl:value-of select="field[@n='57']"/>
        </xsl:if>
        <!-- Date -->
        <xsl:value-of select="replace(field[@n='89'],'&lt;br&gt;','; ')"/><xsl:text> </xsl:text>
        <!-- Language -->
        <xsl:value-of select="field[@n='21']"/><xsl:text> </xsl:text>
        <!-- Provenance -->
        <xsl:for-each select="geotex"><xsl:value-of select="field[@n='28']"/><xsl:text> </xsl:text></xsl:for-each>
        <!-- Archive -->
        <xsl:if test="archref">
          <xsl:value-of select="archref/field[@n='37']"/><xsl:text> </xsl:text>
        </xsl:if>
      </xsl:for-each>
    </field>
    
    <xsl:call-template name="place">
      <xsl:with-param name="docs" select="$docs"/>
    </xsl:call-template>
    <xsl:call-template name="inventory-number"/>
  </xsl:template>

  <xsl:template name="images">
    <!-- note difference here - 'images' are *online* images, 'illustrations' are print-publication images -->
    <xsl:param name="docs" select="node()"/>
    <xsl:if
      test="$docs/t:TEI/t:text/t:body/t:div[@type = 'figure'] or /t:TEI/t:text/t:body/t:div[@type = 'figure'] or /t:TEI/t:facsimile or not(empty($images))">
      <field name="images">true</field>
      <xsl:for-each select="$docs/t:TEI/t:text/t:body/t:div[@type = 'figure']">
        <field name="image_path">
          <xsl:value-of select=".//t:graphic[1]/@url"/>
        </field>
      </xsl:for-each>
      <xsl:for-each select="/t:TEI/t:text/t:body/t:div[@type = 'figure']">
        <field name="image_path">
          <xsl:value-of select=".//t:graphic[1]/@url"/>
        </field>
      </xsl:for-each>
      <xsl:for-each select="/t:TEI/t:facsimile">
        <field name="image_path">
          <xsl:value-of select=".//t:graphic[1]/@url"/>
        </field>
      </xsl:for-each>
      <xsl:for-each select="$images">
        <field name="image_path">
          <xsl:value-of select="tokenize($images,' ')[1]"/>
        </field>
      </xsl:for-each>
    </xsl:if>
    <xsl:if
      test="$docs/t:TEI/t:text/t:body/t:div[@type = 'figure'] or /t:TEI/t:text/t:body/t:div[@type = 'figure']">
      <field name="images-ext">true</field>
    </xsl:if>
    <xsl:if test="/t:TEI/t:facsimile or contains($images, 'papyri.info')">
      <field name="images-int">true</field>
    </xsl:if>
    <xsl:if
      test="$docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'illustrations'][.//t:bibl]">
      <field name="illustrations">true</field>
    </xsl:if>
  </xsl:template>

  <xsl:template name="translation">
    <xsl:param name="docs"/>
    <xsl:for-each select="$docs//t:div[@type='translation']">
      <field name="translation">
        <xsl:value-of select="."/>
      </field>
      <xsl:variable name="trans_lang">
        <!-- defaults to 'en' -->
        <xsl:choose>
          <xsl:when test="@xml:lang">
            <xsl:value-of select="@xml:lang"/>
          </xsl:when>
          <xsl:otherwise>en</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <field name="translation_language">
        <xsl:value-of select="$trans_lang"/>
      </field>
    </xsl:for-each>
    <xsl:if test="$docs//t:div[@type='translation']">
      <field name="has_translation">true</field>
    </xsl:if>
  </xsl:template>

  <xsl:template
    match="text()[local-name(following-sibling::*[1]) = 'lb' and 
    (following-sibling::t:lb[1][@type='inWord'] or following-sibling::t:lb[1][@break='no'])]">
    <xsl:value-of select="replace(., '\s+$', '')"/>
  </xsl:template>

  <xsl:template match="t:div[@type = 'textpart']" priority="1">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="t:lb[@break='no']"/>

  <xsl:template match="t:lb[not(@break='no')]">
    <xsl:text> </xsl:text>
  </xsl:template>

  <xsl:template match="t:gap">
    <xsl:text> </xsl:text>
  </xsl:template>
  
  <xsl:template match="t:g">
    <xsl:value-of select="@type"/>
  </xsl:template>

  <xsl:template name="tpl-apparatus">
    <!-- An apparatus is only created if one of the following is true -->
    <xsl:if
      test=".//t:choice[(child::t:sic and child::t:corr) or (child::t:orig and child::t:reg)] | .//t:subst | .//t:app |        
      .//t:hi[@rend = 'diaeresis' or @rend = 'varia' or @rend = 'oxia' or @rend = 'dasia' or @rend = 'psili' or @rend = 'perispomeni'] |
      .//t:del[@rend='slashes' or @rend='cross-strokes'] | .//t:milestone[@rend = 'box']">

      <!-- An entry is created for-each of the following instances -->
      <xsl:for-each
        select=".//t:choice[(child::t:sic and child::t:corr) or (child::t:orig and child::t:reg)] | .//t:subst | .//t:app |
        .//t:hi[@rend = 'diaeresis' or @rend = 'varia' or @rend = 'oxia' or @rend = 'dasia' or @rend = 'psili' or @rend = 'perispomeni'] |
        .//t:del[@rend='slashes' or @rend='cross-strokes'] | .//t:milestone[@rend = 'box']">

        <!-- Found in tpl-apparatus.xsl -->
        <xsl:call-template name="ddbdp-app"/><xsl:text> </xsl:text>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="title">
    <xsl:param name="hgv-docs"/>
    <xsl:param name="apis-docs"/>
    <xsl:param name="docs"/>
    <xsl:if test="$docs/t:TEI/t:teiHeader/t:fileDesc/t:titleStmt/t:title">
      <field name="title">
    <xsl:choose>
      <xsl:when test="$hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:titleStmt/t:title">
        <xsl:value-of select="normalize-space(string-join($hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:titleStmt/t:title, '; '))"></xsl:value-of>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="normalize-space(string-join($apis-docs/t:TEI/t:teiHeader/t:fileDesc/t:titleStmt/t:title, '; '))"></xsl:value-of>
      </xsl:otherwise>
    </xsl:choose>
        </field>
    </xsl:if>
  </xsl:template>

  <xsl:template name="display-place">
    <xsl:param name="docs"/>
    <field name="display_place">
      <xsl:value-of
        select="normalize-space(string-join($docs[.//t:origin/(t:origPlace|t:p/t:placeName[@type='ancientFindspot'])][1]/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/(t:origPlace|t:p[t:placeName/@type='ancientFindspot']), ' '))"
      />
    </field>
  </xsl:template>

  <xsl:template name="place">
    <xsl:param name="docs"/>
    <!-- Place -->
    <xsl:choose>
      <xsl:when
        test="$docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:p">
        <xsl:variable name="doc"
          select="$docs[/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:p][1]"/>
        <field name="place">
          <xsl:value-of
            select="normalize-space($doc/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:p[1])"
          />
        </field>
      </xsl:when>
      <xsl:when
        test="$docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origPlace">
        <xsl:variable name="doc"
          select="$docs[/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origPlace][1]"/>
        <xsl:for-each
          select="$doc/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origPlace">
          <field name="place">
            <xsl:value-of select="normalize-space(.)"/>
          </field>
        </xsl:for-each>
      </xsl:when>
    </xsl:choose>
    <xsl:for-each select="$docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:provenance/t:p/t:placeName[@subtype='nome']">
      <field name="nome">
        <xsl:value-of select="normalize-space(.)"/>
      </field>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="date">
    <xsl:param name="hgv-docs"/>
    <xsl:param name="docs"/>
    <xsl:variable name="relevant-date-nodeset">
      <xsl:choose>
        <xsl:when
          test="$hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate/(@when|@notBefore|@notAfter)">
          <xsl:value-of
            select="$hgv-docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate/(@when|@notBefore|@notAfter)"
          />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of
            select="$docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate/(@when|@notBefore|@notAfter)"
          />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <field name="display_date">
      <xsl:choose>
        <xsl:when test="count(tokenize($relevant-date-nodeset, ' ')) gt 0">
          <xsl:value-of select="pi:get-date-range(tokenize($relevant-date-nodeset, ' '))"/>
        </xsl:when>
        <xsl:otherwise>Unknown</xsl:otherwise>
      </xsl:choose>
    </field>
    <xsl:choose>
      <xsl:when test="count(tokenize($relevant-date-nodeset, ' ')) gt 0">
        <field name="earliest_date">
          <xsl:value-of
            select="pi:get-min-date(remove(tokenize($relevant-date-nodeset, ' '), 1), pi:iso-date-to-num(tokenize($relevant-date-nodeset, ' ')[1]), false())"
          />
        </field>
        <field name="latest_date">
          <xsl:value-of
            select="pi:get-max-date(remove(tokenize($relevant-date-nodeset, ' '), 1), pi:iso-date-to-num(tokenize($relevant-date-nodeset, ' ')[1]), false())"
          />
        </field>
      </xsl:when>
      <!-- unknown date -->
      <xsl:otherwise>
        <field name="unknown_date_flag">true</field>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="inventory-number">
    <!-- InvNum -->
    <xsl:if
      test="/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno[@type='invno']">
      <field name="invnum">
        <xsl:value-of
          select="/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno[@type='invno']"
        />
      </field>
    </xsl:if>
  </xsl:template>

<xsl:template name="revision-history">
      <xsl:param name="docs"></xsl:param>
      
      <xsl:variable name="all-edits" select="$docs/t:TEI/t:teiHeader/t:revisionDesc/t:change[@when]"></xsl:variable> <!-- cl: only edits that have a date -->
      <xsl:variable name="app-edits" select="$docs/t:TEI/t:teiHeader/t:revisionDesc/t:change[matches(text(), 'appchange', 'i')]"></xsl:variable>
      <xsl:variable name="date-edits" select="$docs/t:TEI/t:teiHeader/t:revisionDesc/t:change[matches(text(), 'datechange', 'i')]"></xsl:variable>
      <xsl:variable name="place-edits" select="$docs/t:TEI/t:teiHeader/t:revisionDesc/t:change[matches(text(), 'placechange', 'i')]"></xsl:variable>
      <xsl:if test="count($all-edits) &gt; 0">
        <xsl:variable name="first-revised"><xsl:value-of select="pi:regularise-datestring(pi:get-earliest-date-element(remove($all-edits, 1), $all-edits[1])/@when)"></xsl:value-of></xsl:variable>
        <xsl:variable name="last-revised"><xsl:value-of select="pi:regularise-datestring(pi:get-latest-date-element(remove($all-edits, 1), $all-edits[1])/@when)"></xsl:value-of></xsl:variable>
        <xsl:variable name="all-edit-author"><xsl:value-of select="pi:get-latest-date-element(remove($all-edits, 1), $all-edits[1])/@who"></xsl:value-of></xsl:variable>
        <field name="published"><xsl:value-of select="$first-revised"></xsl:value-of></field>
        <field name="edit_date"><xsl:value-of select="$last-revised"></xsl:value-of></field>
        <field name="last_editor"><xsl:value-of select="$all-edit-author"></xsl:value-of></field>
      </xsl:if>
      <xsl:if test="count($app-edits) > 0">
        <xsl:variable name="latest-app-edit"><xsl:value-of select="pi:regularise-datestring(pi:get-latest-date-element(remove($app-edits, 1), $app-edits[1])/@when)"></xsl:value-of></xsl:variable>
        <xsl:variable name="app-edit-author"><xsl:value-of select="pi:get-latest-date-element(remove($app-edits, 1), $app-edits[1])/@who"></xsl:value-of></xsl:variable> 
        <field name="app_edit_date"><xsl:value-of select="$latest-app-edit"></xsl:value-of></field>
        <field name="app_editor"><xsl:value-of select="$app-edit-author"></xsl:value-of></field>
      </xsl:if>
      <xsl:if test="count($date-edits) > 0">
        <xsl:variable name="latest-date-edit"><xsl:value-of select="pi:regularise-datestring(pi:get-latest-date-element(remove($date-edits, 1), $date-edits[1])/@when)"></xsl:value-of></xsl:variable>
        <xsl:variable name="date-edit-author"><xsl:value-of select="pi:get-latest-date-element(remove($date-edits, 1), $date-edits[1])/@who"></xsl:value-of></xsl:variable>         
        <field name="date_edit_date"><xsl:value-of select="$latest-date-edit"></xsl:value-of></field>
        <field name="date_editor"><xsl:value-of select="$date-edit-author"></xsl:value-of></field>
      </xsl:if>
      <xsl:if test="count($place-edits) > 0">
        <xsl:variable name="latest-place-edit"><xsl:value-of select="pi:regularise-datestring(pi:get-latest-date-element(remove($place-edits, 1), $place-edits[1])/@when)"></xsl:value-of></xsl:variable>
        <xsl:variable name="place-edit-author"><xsl:value-of select="pi:get-latest-date-element(remove($place-edits, 1), $place-edits[1])/@who"></xsl:value-of></xsl:variable>        
        <field name="place_edit_date"><xsl:value-of select="$latest-place-edit"></xsl:value-of></field>
        <field name="place_editor"><xsl:value-of select="$place-edit-author"></xsl:value-of></field>
      </xsl:if>
      
  <!--   <xsl:variable name="last-revised"><xsl:value-of select="pi:get-latest-date($docs/t:TEI/t:teiHeader/t:revisionDesc/t:change/@when, $docs/t:TEI/t:teiHeader/t:revisionDesc/t:change/@when)[1]"></xsl:value-of></xsl:variable>   
 <xsl:variable name="app-whens" select="$docs/t:TEI/t:teiHeader/t:revisionDesc/t:change[contains(text(), 'appchange')]/@when"></xsl:variable> -->
  
  
  
  <!--  <field name="edit_date"><xsl:value-of select="$last-revised"></xsl:value-of></field>
  <field name="last_editor"><xsl:value-of select="pi:get-latest-editor($docs/t:TEI/t:teiHeader/t:revisionDesc/t:change/@when, data($docs/t:TEI/t:teiHeader/t:revisionDesc/t:change/@when)[1])"></xsl:value-of></field>
  
  <xsl:if test="count($app-whens) > 0">
  <xsl:variable name="first-app-revised"><xsl:value-of select="concat(pi:get-latest-date($app-whens, $app-whens[1]), $date-suffix)"></xsl:value-of></xsl:variable>
  <xsl:if test="matches($first-app-revised, '\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z')">
    <field name="app_edit_date"><xsl:value-of select="$first-app-revised"></xsl:value-of></field>
  </xsl:if>
  
  </xsl:if>-->
</xsl:template>
 
 <xsl:template name="reg-text-processing">
   <xsl:param name="temp-node"></xsl:param>
   <xsl:apply-templates select="$temp-node//t:div[@type='edition']">
     <xsl:with-param name="parm-apparatus-style" select="$apparatus-style" tunnel="yes"/>
     <xsl:with-param name="parm-leiden-style" select="$leiden-style" tunnel="yes"/>
   </xsl:apply-templates>
 </xsl:template>
 
  <xsl:template name="orig-text-processing">
    <xsl:param name="temp-node"></xsl:param>
    <xsl:apply-templates select="$temp-node//t:div[@type='edition']">
      <xsl:with-param name="parm-apparatus-style" select="$apparatus-style" tunnel="yes"/>
      <xsl:with-param name="parm-leiden-style" select="$leiden-style" tunnel="yes"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template name="app-link">
    <xsl:param name="location"/>
  </xsl:template>
  
  <xsl:template name="lbrk-app">
    <xsl:text>
&#xD;</xsl:text>
  </xsl:template>
  
  <xsl:template name="ddbdp-app">
    <xsl:choose>
      <!-- choice -->
      <xsl:when test="local-name() = 'choice' and child::t:sic and child::t:corr">
        <xsl:apply-templates select="t:sic/node()"/>
      </xsl:when>

      <xsl:when test="local-name() = 'choice' and child::t:reg and child::t:orig">
        <xsl:apply-templates select="t:reg/node()"/>
      </xsl:when>

      <!-- subst -->
      <xsl:when test="local-name() = 'subst'">
        <xsl:apply-templates select="t:del/node()"/>
      </xsl:when>

      <!-- app -->
      <xsl:when test="local-name() = 'app'">
        <xsl:apply-templates select="t:rdg/node()"/>
      </xsl:when>

    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
