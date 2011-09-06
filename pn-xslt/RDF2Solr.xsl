<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/terms/"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:pi="http://papyri.info/ns"
  xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:t="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs dc rdf pi tei t xd" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  version="2.0">

  <xsl:import href="global-varsandparams.xsl"/>

  <xsl:import href="txt-teiab.xsl"/>
  <xsl:import href="txt-teiapp.xsl"/>
  <xsl:import href="txt-teidiv.xsl"/>
  <xsl:import href="txt-teidivedition.xsl"/>
  <xsl:import href="txt-teigap.xsl"/>
  <xsl:import href="txt-teihead.xsl"/>
  <xsl:import href="txt-teilb.xsl"/>
  <xsl:import href="txt-teilgandl.xsl"/>
  <xsl:import href="txt-teilistanditem.xsl"/>
  <xsl:import href="txt-teilistbiblandbibl.xsl"/>
  <xsl:import href="txt-teimilestone.xsl"/>
  <xsl:import href="txt-teinote.xsl"/>
  <xsl:import href="txt-teip.xsl"/>
  <xsl:import href="txt-teispace.xsl"/>
  <xsl:import href="txt-teisupplied.xsl"/>
  <xsl:import href="txt-teiref.xsl"/>
  <xsl:import href="teiabbrandexpan.xsl"/>
  <xsl:import href="teiaddanddel.xsl"/>
  <xsl:import href="teichoice.xsl"/>
  <xsl:import href="teiheader.xsl"/>
  <xsl:import href="teihi.xsl"/>
  <xsl:import href="teimilestone.xsl"/>
  <xsl:import href="teinum.xsl"/>
  <xsl:import href="teiorig.xsl"/>
  <xsl:import href="teiorigandreg.xsl"/>
  <xsl:import href="teiq.xsl"/>
  <xsl:import href="teiseg.xsl"/>
  <xsl:import href="teisicandcorr.xsl"/>
  <xsl:import href="teispace.xsl"/>
  <xsl:import href="teisupplied.xsl"/>
  <xsl:import href="teisurplus.xsl"/>
  <xsl:import href="teiunclear.xsl"/>
  <xsl:import href="txt-tpl-apparatus.xsl"/>
  <xsl:import href="txt-tpl-linenumberingtab.xsl"/>
  <xsl:import href="tpl-reasonlost.xsl"/>
  <xsl:import href="tpl-certlow.xsl"/>
  <xsl:import href="tpl-text.xsl"/>

  <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes"/>

  <xsl:param name="collection"/>
  <xsl:param name="related"/>
  <xsl:variable name="relations" select="tokenize($related, ' ')"/>
  <xsl:variable name="path">/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase"/>
  <xsl:variable name="line-inc">5</xsl:variable>

  <xsl:include href="pi-functions.xsl"/>

  <xsl:template match="/">
    <xsl:variable name="ddbdp" select="$collection = 'ddbdp'"/>
    <xsl:variable name="hgv" select="$collection = 'hgv' or contains($related, 'hgv/')"/>
    <xsl:variable name="apis" select="$collection = 'apis' or contains($related, '/apis/')"/>
    <xsl:variable name="translation"
      select="contains($related, 'hgvtrans') or (contains($related, '/apis/') and pi:get-docs($relations[contains(., '/apis/')], 'xml')//t:div[@type = 'translation'])"/>
    <xsl:variable name="image" select="contains($related, 'info:fedora/ldpd')"/>
    
    <add>
      <doc>
        <xsl:if test="$ddbdp = true()">
          <field name="collection">ddbdp</field>
        </xsl:if>
        <xsl:if test="$hgv = true()">
          <field name="collection">hgv</field>
        </xsl:if>
        <xsl:if test="$apis = true()">
          <field name="collection">apis</field>
        </xsl:if>

        <xsl:choose>
          <xsl:when test="$collection = 'ddbdp'">
            <field name="id">http://papyri.info/ddbdp/<xsl:value-of
                select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'ddb-hybrid']"
              /></field>
            <xsl:for-each
              select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type != 'HGV']">
              <xsl:choose>
                <xsl:when test="@type='ddb-hybrid'">
                  <field name="identifier">http://papyri.info/ddbdp/<xsl:value-of select="."
                    /></field>
                </xsl:when>
                <xsl:when test="@type='apisid'">
                  <field name="identifier">http://papyri.info/apis/<xsl:value-of select="."
                    /></field>
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
            <xsl:variable name="text">
              <xsl:apply-templates select="//t:div[@type = 'edition']"/>
            </xsl:variable>
            <xsl:variable name="textnfc"
              select="normalize-space(replace(translate($text, '[]{},.-()+^?̣&lt;&gt;*&#xD;\\/〚〛ʼ', ''),'&#xA0;', ''))"/>
            <xsl:variable name="textnfd" select="normalize-unicode($textnfc, 'NFD')"/>
            <!-- transcription fields are 8-fold: plain, plain ignore diacriticals, plain ignore case, plain ignore case and diacriticals,
              ngram, ngram ignore diacriticals, ngram ignore case, ngram ignore case and diacriticals-->
            <field name="transcription">
              <xsl:value-of select="translate($textnfc, 'ς', 'σ')"/>
            </field>
            <field name="transcription_id">
              <xsl:value-of
                select="translate(replace($textnfd, '[\p{IsCombiningDiacriticalMarks}]', ''), 'ς', 'σ')"
              />
            </field>
            <field name="transcription_ic">
              <xsl:value-of select="translate(lower-case($textnfc), 'ς', 'σ')"/>
            </field>
            <field name="transcription_ia">
              <xsl:value-of
                select="translate(lower-case(replace($textnfd, '[\p{IsCombiningDiacriticalMarks}]', '')), 'ς', 'σ')"
              />
            </field>
            <!--
            <field name="transcription_ngram">
              <xsl:for-each select="tokenize($textnfc, '\s+')">
                <xsl:if test=". != ''"><xsl:text>^</xsl:text><xsl:value-of select="."/><xsl:text>^ </xsl:text></xsl:if>
              </xsl:for-each>
            </field>
            <field name="transcription_ngram_id">
              <xsl:for-each select="tokenize(replace($textnfd, '[\p{IsCombiningDiacriticalMarks}]', ''), '\s+')">
                <xsl:if test=". != ''"><xsl:text>^</xsl:text><xsl:value-of select="."/><xsl:text>^ </xsl:text></xsl:if>
              </xsl:for-each>
            </field>
            <field name="transcription_ngram_ic">
              <xsl:for-each select="tokenize(lower-case($textnfc), '\s+')">
                <xsl:if test=". != ''"><xsl:text>^</xsl:text><xsl:value-of select="."/><xsl:text>^ </xsl:text></xsl:if>
              </xsl:for-each>
            </field>
            -->
            <field name="transcription_ngram_ia">
              <xsl:for-each
                select="tokenize(translate(lower-case(replace($textnfd, '[\p{IsCombiningDiacriticalMarks}]', '')), 'ς', 'σ'), '\s+')">
                <xsl:if test="string-length(normalize-space(.)) &gt; 0">
                  <xsl:text>^</xsl:text>
                  <xsl:value-of select="normalize-space(.)"/>
                  <xsl:text>^ </xsl:text>
                </xsl:if>
              </xsl:for-each>
            </field>
            <xsl:if test="string-length($textnfd) > 0">
              <field name="has_transcription">true</field>
            </xsl:if>
            <xsl:variable name="languages"
              select="distinct-values(//t:div[@type='edition']/descendant-or-self::*/@xml:lang)"/>
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
            <xsl:call-template name="facetfields">
              <xsl:with-param name="docs" select="/"/>
              <xsl:with-param name="alterity">self</xsl:with-param>
            </xsl:call-template>
            <xsl:if test="$hgv or $apis">
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
                <xsl:with-param name="docs"
                  select="pi:get-docs($relations[contains(., 'hgv/') or contains(., '/apis/')], 'xml')"
                />
              </xsl:call-template>
              <xsl:call-template name="translation">
                <xsl:with-param name="docs"
                  select="pi:get-docs($relations[contains(., 'hgv/') or contains(., '/apis/') or contains(., '/hgvtrans/')], 'xml')"
                />
              </xsl:call-template>
            </xsl:if>
            <xsl:call-template name="images">
              <xsl:with-param name="docs"
                  select="pi:get-docs($relations[contains(., 'hgv/') or contains(., '/apis/')], 'xml')"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="$collection = 'hgv'">
            <field name="id">http://papyri.info/hgv/<xsl:value-of
                select="t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'filename']"
              /></field>
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
              <xsl:with-param name="docs"
                select="pi:get-docs($relations[contains(., '/apis/')], 'xml')"/>
            </xsl:call-template>
            <xsl:call-template name="images">
              <xsl:with-param name="docs"
                  select="pi:get-docs($relations[contains(., '/apis/')], 'xml')"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="$collection = 'apis'">
            <field name="id">http://papyri.info/apis/<xsl:value-of
                select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'apisid']"
              /></field>
            <xsl:call-template name="facetfields">
              <xsl:with-param name="docs" select="/"/>
              <xsl:with-param name="alterity">self</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="metadata">
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
            <xsl:call-template name="images"></xsl:call-template>
          </xsl:when>
        </xsl:choose>
      </doc>
    </add>
  </xsl:template>

  <xsl:template name="facetfields">
    <xsl:param name="docs"/>
    <xsl:param name="alterity"/>
    <xsl:choose>
      <xsl:when
        test="$docs[1]//t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition']">
        <!-- IFF HGV document -->
        <xsl:variable name="hgv_identifiers">
          <xsl:perform-sort select="$docs//t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']">
            <xsl:sort select="."/>
          </xsl:perform-sort>
        </xsl:variable>
        <field name="hgv_identifier">
          <xsl:choose>
            <xsl:when test="count($hgv_identifiers//*) gt 1">
              <xsl:value-of select="$hgv_identifiers//*[1]"/> - <xsl:value-of select="$hgv_identifiers//*[position() = last()]"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="$hgv_identifiers[1]"/></xsl:otherwise>
          </xsl:choose>
         </field>
        <xsl:variable name="hgv_series">
          <xsl:value-of
            select="replace(normalize-space($docs[1]//t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition']//t:bibl/t:title[@level = 's']), ' ', '_')"
          />
        </xsl:variable>
        <xsl:variable name="hgv_volume">
          <xsl:variable name="hgv_volprep"
            select="replace(normalize-space($docs[1]//t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition']//t:bibl/t:biblScope[@type = 'volume']), ' ', '_')"/>
          <xsl:choose>
            <xsl:when test="string-length($hgv_volprep) = 0">0</xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$hgv_volprep"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="hgv_numbers"
          select="replace(normalize-space($docs[1]//t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition']//t:bibl/t:biblScope[@type = 'numbers']), ' ', '_')"/>
        <xsl:variable name="hgv_lines"
          select="normalize-space($docs[1]//t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition']//t:bibl/t:biblScope[@type = 'lines'])"/>
        <xsl:variable name="hgv_item" select="replace(concat($hgv_numbers, ' ', $hgv_lines), '\s+$', '')"/>
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
      </xsl:when>
      <xsl:when
        test="$docs[1]/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'ddb-hybrid']">
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
        <field name="ddbdp_full_identifier">
          <xsl:value-of select="normalize-space($sort[3])"/>
        </field>
        <field name="ddbdp_item">
          <xsl:value-of select="$ddbdp_item"/>
        </field>
        <xsl:if test="string-length($ddbdp_item_letter) > 0">
          <field name="ddbdp_item_letter">
            <xsl:value-of select="$ddbdp_item_letter"/>
          </field>
        </xsl:if>
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
      <xsl:when test="$docs[1]//t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'apisid']">
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
            <xsl:value-of
              select="replace(., ':', ' ')"
            />
          </field>
        </xsl:for-each>
        <field name="apis_inventory">
          <xsl:value-of
            select="$docs[1]//t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno"/>
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

  <xsl:template name="metadata">
    <xsl:param name="docs"/>
    <field name="display_place">
      <xsl:value-of
        select="normalize-space(string-join($docs[.//t:origin/(t:origPlace|t:p/t:placeName[@type='ancientFindspot'])][1]/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/(t:origPlace|t:p[t:placeName/@type='ancientFindspot']), ' '))"
      />
    </field>
    <field name="display_date">
      <xsl:value-of
        select="pi:get-date-range($docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate/(@when|@notBefore|@notAfter))"
      />
    </field>
    <field name="metadata">
      <!-- Title -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:teiHeader/t:fileDesc/t:titleStmt/t:title, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Summary -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:summary, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Publications -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition'], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Inv. Id -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Physical Desc. -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:p, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Post-concordance BL Entries -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'corrections'], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Translations -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'translations'], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Provenance -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/(t:origPlace|t:p[t:placeName/@type='ancientFindspot']), ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Material -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:support/t:material, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Language -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItem/t:textLang, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Date -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Commentary -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:text/t:body/t:div[@type='commentary']/t:p, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Notes (general|lines|palaeography|recto/verso|conservation|preservation) -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItem/t:note, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Print Illustrations -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'illustrations'][.//t:bibl], ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Subjects -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:teiHeader/t:profileDesc/t:textClass/t:keywords, ' '))"/>
      <xsl:text> </xsl:text>
      <!-- Associated Names -->
      <xsl:value-of
        select="normalize-space(string-join($docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin[t:persName/@type = 'asn'], ' '))"/>
      <xsl:text> </xsl:text>
   
    </field>
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
      <!-- edited thill 2011.05.11 for multiple possible findspots -->
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
      <!-- end thill 2011.05.11 -->
    </xsl:choose>
    <!-- Dates -->
    <xsl:for-each
      select="$docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate">
      <xsl:choose>
        <xsl:when test="pi:iso-date-to-num(@notBefore) and pi:iso-date-to-num(@notAfter)">
          <field name="date_start">
            <xsl:value-of select="pi:iso-date-to-num(@notBefore)"/>
          </field>
          <field name="date_end">
            <xsl:value-of select="pi:iso-date-to-num(@notAfter)"/>
          </field>
          <field name="date_category">
            <xsl:value-of select="pi:assign-date-category(@notBefore)"/>
          </field>
          <field name="date_category">
            <xsl:value-of select="pi:assign-date-category(@notAfter)"/>
          </field>
        </xsl:when>
        <xsl:when test="pi:iso-date-to-num(@when)">
          <field name="date_start">
            <xsl:value-of select="pi:iso-date-to-num(@when)"/>
          </field>
          <field name="date_end">
            <xsl:value-of select="pi:iso-date-to-num(@when)"/>
          </field>
          <field name="date_category">
            <xsl:value-of select="pi:assign-date-category(@when)"/>
          </field>
        </xsl:when>
        <xsl:when test="pi:iso-date-to-num(@notBefore)">
          <field name="date_start">
            <xsl:value-of select="pi:iso-date-to-num(@notBefore)"/>
          </field>
          <field name="date_end">
            <xsl:value-of select="pi:iso-date-to-num(@notBefore)"/>
          </field>
          <field name="date_category">
            <xsl:value-of select="pi:assign-date-category(@notBefore)"/>
          </field>
        </xsl:when>
        <xsl:when test="pi:iso-date-to-num(@notAfter)">
          <field name="date_start">
            <xsl:value-of select="pi:iso-date-to-num(@notAfter)"/>
          </field>
          <field name="date_end">
            <xsl:value-of select="pi:iso-date-to-num(@notAfter)"/>
          </field>
          <field name="date_category">
            <xsl:value-of select="pi:assign-date-category(@notAfter)"/>
          </field>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
    <!-- unknown date -->
    <xsl:if
      test="count($docs/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate) = 0">
      <field name="unknown_date_flag">true</field>
    </xsl:if>
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
  
  <xsl:template name="images">
    <!-- note difference here - 'images' are *online* images, 'illustrations' are print-publication images -->
    <xsl:param name="docs" select="node()"></xsl:param>
    <xsl:if
      test="$docs/t:TEI/t:text/t:body/t:div[@type = 'figure'] or /t:TEI/t:text/t:body/t:div[@type = 'figure'] or contains($related, 'images/')">
      <field name="images">true</field>
      <xsl:for-each select="$docs/t:TEI/t:text/t:body/t:div[@type = 'figure']">
        <field name="image_path"><xsl:value-of select=".//t:graphic/@url"></xsl:value-of></field>
      </xsl:for-each>
      <xsl:for-each select="/t:TEI/t:text/t:body/t:div[@type = 'figure']">
        <field name="image_path"><xsl:value-of select=".//t:graphic/@url"></xsl:value-of></field>
      </xsl:for-each>
      <xsl:for-each select="$relations">
        <xsl:if test="contains(. , 'images/')">
          <field name="image_path"><xsl:value-of select="."></xsl:value-of></field>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>    
    <xsl:if test="$docs/t:TEI/t:text/t:body/t:div[@type = 'figure'] or /t:TEI/t:text/t:body/t:div[@type = 'figure']">
      <field name="images-ext">true</field>
    </xsl:if>
    <xsl:if test="contains($related, 'images/')">
      <field name="images-int">true</field>
    </xsl:if>
    <xsl:if test="$docs/t:TEI/t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'illustrations'][.//t:bibl]">
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
    match="text()[local-name(following-sibling::*[1]) = 'lb' and following-sibling::t:lb[1][@type='inWord']]">
    <xsl:value-of select="replace(., '\s+$', '')"/>
  </xsl:template>

  <xsl:template match="t:div[@type = 'textpart']" priority="1">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="t:lb[@type='inWord']"/>

  <xsl:template match="t:lb[not(@type='inWord')]">
    <xsl:text> </xsl:text>
  </xsl:template>

  <xsl:template match="t:gap">
    <xsl:text> </xsl:text>
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

        <xsl:call-template name="app-link">
          <xsl:with-param name="location" select="'apparatus'"/>
        </xsl:call-template>

        <!-- Found in tpl-apparatus.xsl -->
        <xsl:call-template name="ddbdp-app"/>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="ddbdp-app">
    <xsl:choose>
      <!-- choice -->
      <xsl:when test="local-name() = 'choice' and child::t:sic and child::t:corr">
        <xsl:apply-templates select="t:sic/node()"/>
      </xsl:when>

      <xsl:when test="local-name() = 'choice' and child::t:reg and child::t:orig">
        <xsl:apply-templates select="t:orig/node()"/>
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

  <xsl:function name="pi:iso-date-to-num">
    <xsl:param name="date"/>
    <xsl:choose>
      <xsl:when test="starts-with($date, '-')">
        <xsl:sequence select=" number(substring($date, 1, 5))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="number(substring($date, 1, 4))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="pi:assign-date-category">
    <!-- receives iso date and returns facetable date category -->
    <xsl:param name="raw-date"/>
    <xsl:variable name="era">
      <xsl:choose>
        <xsl:when test="starts-with($raw-date, '-')">
          <xsl:value-of select="'BCE'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'CE'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="year">
      <xsl:analyze-string select="$raw-date" regex="(-?\d{{4}})">
        <xsl:matching-substring>
          <xsl:value-of select="regex-group(0)"/>
        </xsl:matching-substring>
      </xsl:analyze-string>
    </xsl:variable>
    <xsl:variable name="interval" select="50"/>
    <xsl:variable name="category">
      <xsl:choose>
        <xsl:when test="(number($year) mod $interval) = 0">
          <xsl:value-of select="abs(number($year)) div $interval"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of
            select="(abs(number($year)) + ($interval - (abs(number($year)) mod $interval))) div $interval"
          />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$era = 'BCE'">
        <xsl:sequence select="concat('-', $category)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$category"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="pi:get-date-range">
    <xsl:param name="date-seq"/>
    <xsl:variable name="min"
      select="pi:get-min-date(remove($date-seq, 1), pi:iso-date-to-num($date-seq[1]))"/>
    <xsl:variable name="max"
      select="pi:get-max-date(remove($date-seq, 1), pi:iso-date-to-num($date-seq[1]))"/>
    <xsl:choose>
      <xsl:when test="$min = $max">
        <xsl:sequence select="$min"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="concat($min, ' - ', $max)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="pi:get-min-date">
    <xsl:param name="date-seq"/>
    <xsl:param name="current"/>
    <xsl:choose>
      <xsl:when test="count($date-seq) = 0">
        <xsl:choose>
          <xsl:when test="$current le 0">
            <xsl:sequence select="concat(abs($current), ' BCE')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="concat($current, ' CE')"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="pi:iso-date-to-num($date-seq[1]) lt $current">
        <xsl:sequence
          select="pi:get-min-date(remove($date-seq, 1), pi:iso-date-to-num($date-seq[1]))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="pi:get-min-date(remove($date-seq, 1), $current)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="pi:get-max-date">
    <xsl:param name="date-seq"/>
    <xsl:param name="current"/>
    <xsl:choose>
      <xsl:when test="count($date-seq) = 0">
        <xsl:choose>
          <xsl:when test="$current le 0">
            <xsl:sequence select="concat(abs($current), ' BCE')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="concat($current, ' CE')"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="pi:iso-date-to-num($date-seq[1]) gt $current">
        <xsl:sequence
          select="pi:get-max-date(remove($date-seq, 1), pi:iso-date-to-num($date-seq[1]))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="pi:get-max-date(remove($date-seq, 1), $current)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
