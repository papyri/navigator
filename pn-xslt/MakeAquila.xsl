<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dc="http://purl.org/dc/terms/" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:pi="http://papyri.info/ns"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns:xi="http://www.w3.org/2001/XInclude"
  xmlns:sl="http://www.w3.org/2005/sparql-results#"
  version="2.0" exclude-result-prefixes="#all">
  
  <!--
    java net.sf.saxon.Transform -o:TT.html -it:GENERATE-HTML-SNIPPETS -xsl:MakeAquila.xsl collection=ddbdp line-inc=5 >> TT 2>&1
  -->
  
  <xsl:import href="pi-global-varsandparams.xsl"/>
  <xsl:import href="morelikethis-varsandparams.xsl"/>
  
  <!-- html related stylesheets, these may import tei{element} stylesheets if relevant eg. htm-teigap and teigap -->
  <xsl:import href="../epidoc-xslt/htm-teiab.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teiaddanddel.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teiapp.xsl"/> 
  <xsl:import href="../epidoc-xslt/htm-teidiv.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teidivedition.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teiforeign.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teifigure.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teig.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teigap.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teihead.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teihi.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teilb.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teilgandl.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teilistanditem.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teilistbiblandbibl.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teimilestone.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teinote.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teinum.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teip.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teiseg.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teispace.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teisupplied.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teiterm.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-teiref.xsl"/>
  
  <!-- tei stylesheets that are also used by start-txt -->
  <xsl:import href="../epidoc-xslt/teiabbrandexpan.xsl"/>
  <xsl:import href="../epidoc-xslt/teicertainty.xsl"/>
  <xsl:import href="../epidoc-xslt/teichoice.xsl"/>
  <xsl:import href="../epidoc-xslt/teihandshift.xsl"/>
  <xsl:import href="../epidoc-xslt/teiheader.xsl"/>
  <xsl:import href="../epidoc-xslt/teimilestone.xsl"/>
  <xsl:import href="../epidoc-xslt/teiorig.xsl"/>
  <xsl:import href="../epidoc-xslt/teiorigandreg.xsl"/>
  <xsl:import href="../epidoc-xslt/teiq.xsl"/>
  <xsl:import href="../epidoc-xslt/teisicandcorr.xsl"/>
  <xsl:import href="../epidoc-xslt/teispace.xsl"/>
  <xsl:import href="../epidoc-xslt/teisupplied.xsl"/>
  <xsl:import href="../epidoc-xslt/teisurplus.xsl"/>
  <xsl:import href="../epidoc-xslt/teiunclear.xsl"/>
  
  <!-- html related stylesheets for named templates -->
  <xsl:import href="../epidoc-xslt/htm-tpl-cssandscripts.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-tpl-apparatus.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-tpl-lang.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-tpl-metadata.xsl"/>
  <xsl:import href="../epidoc-xslt/htm-tpl-license.xsl"/>
  <!-- global named templates with no html, also used by start-txt -->
  <xsl:import href="../epidoc-xslt/tpl-reasonlost.xsl"/>
  <xsl:import href="../epidoc-xslt/tpl-certlow.xsl"/>
  <xsl:import href="../epidoc-xslt/tpl-text.xsl"/>
  
  <xsl:include href="htm-teiemph.xsl"/>
  <xsl:include href="../epidoc-xslt/htm-tpl-sqbrackets.xsl"/>
  <xsl:include href="../epidoc-xslt/htm-tpl-structure.xsl"/>
  <xsl:include href="metadata.xsl"/>
  <xsl:key name="lang-codes" match="//pi:lang-codes-to-expansions" use="@code"></xsl:key>
  <xsl:param name="collection"/>
  <xsl:param name="related"/>
  <xsl:param name="replaces"/>
  <xsl:param name="isReplacedBy"/>
  <xsl:param name="isPartOf"/>
  <xsl:param name="sources"/>
  <xsl:param name="images"/>
  <xsl:param name="citationForm"/>
  <xsl:param name="selfUrl"/>
  <xsl:param name="biblio"/>
  <xsl:param name="server">papyri.info</xsl:param>
  <xsl:variable name="relations" select="tokenize($related, '\s+')"/>
  <xsl:variable name="imgs" select="tokenize($images, '\s+')"/>
  <xsl:variable name="biblio-relations" select="tokenize($biblio, '\s+')"/>
  <xsl:variable name="path">/srv/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase">/srv/data/papyri.info/pn/idp.html</xsl:variable>
  <xsl:variable name="doc-id">
    <xsl:choose>
      <xsl:when test="//t:idno[@type='apisid']"><xsl:value-of select="//t:idno[@type='apisid']"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="//t:idno[@type='filename']"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="line-inc">5</xsl:variable>
  <xsl:variable name="resolve-uris" select="false()"/>
  
  <xsl:template name="collection-hierarchy">
    <xsl:param name="all-ancestors"></xsl:param>
    <xsl:param name="last-ancestor"></xsl:param>
    <xsl:variable name="ancestors"><xsl:value-of select="concat(normalize-space($all-ancestors), ' ')"></xsl:value-of></xsl:variable>
    <xsl:variable name="current-ancestor">
      <xsl:value-of select="substring-before($ancestors, ' ')"></xsl:value-of>
    </xsl:variable>
    <xsl:variable name="remaining">
      <xsl:value-of select="concat(normalize-space(substring-after($ancestors, $current-ancestor)), ' ')"></xsl:value-of>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$last-ancestor">
        <meta about="{$last-ancestor}" property="dc:isPartOf" content="{$current-ancestor}" />
      </xsl:when>
      <xsl:otherwise>
        <meta property="dc:isPartOf" content="{$current-ancestor}"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="$remaining ne ' '">
      <xsl:call-template name="collection-hierarchy">
        <xsl:with-param name="all-ancestors"><xsl:value-of select="$remaining"></xsl:value-of></xsl:with-param>
        <xsl:with-param name="last-ancestor"><xsl:value-of select="$current-ancestor"></xsl:value-of></xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <xsl:include href="pi-functions.xsl"/>
  
  <xsl:output method="html"/>
  
  <!-- cl: generate html snippets for aquila website -->
  <xsl:template name="GENERATE-HTML-SNIPPETS">
    <xsl:for-each select="collection('/Users/Admin/idp.data/aquila/DDB_EpiDoc_XML/?select=*.xml;recurse=yes')">
      <xsl:if test="not(tei:TEI/tei:text[1]/tei:body[1]/tei:head[1]/tei:ref[@type='reprint-in'])">
        <xsl:variable name="resultDocument" select="replace(replace(document-uri(.), 'DDB_EpiDoc_XML', 'DDB_EpiDoc_HTML'), '\.xml', '.html')"/>
        <xsl:message select="$resultDocument"/>
        <xsl:result-document href="{$resultDocument}" method="html">
          <xsl:apply-templates select="." />
        </xsl:result-document>
      </xsl:if>
    </xsl:for-each>

    <xsl:for-each select="collection('/Users/Admin/idp.data/aquila/HGV_trans_EpiDoc/?select=*.xml;recurse=yes')">
      <xsl:variable name="resultDocument" select="replace(replace(document-uri(.), 'HGV_trans_EpiDoc', 'HGV_trans_EpiDoc_HTML'), '\.xml', '.html')"/>
      <xsl:message select="$resultDocument"/>
      <xsl:result-document href="{$resultDocument}" method="html">
        <xsl:apply-templates select="." />
      </xsl:result-document>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="/">
    <xsl:variable name="ddbdp" select="$collection = 'ddbdp'"/>
    <xsl:variable name="hgv" select="$collection = 'hgv' or contains($related, 'hgv/')"/>
    <xsl:variable name="apis" select="$collection = 'apis' or contains($related, '/apis/')"/>
    <xsl:variable name="translation" select="contains($related, 'hgvtrans') or (contains($related, 'apis') and pi:get-docs($relations[contains(., 'apis')], 'xml')//t:div[@type = 'translation']) or //t:div[@type = 'translation']"/>
    <xsl:variable name="image" select="count($imgs) gt 0"/>
    <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;
 </xsl:text>

                <xsl:if test="$collection = 'ddbdp'">


                    <xsl:apply-templates select="/t:TEI" mode="text">
                      <xsl:with-param name="parm-apparatus-style" select="$apparatus-style" tunnel="yes"/>
                      <xsl:with-param name="parm-edn-structure" select="$edn-structure" tunnel="yes"/>
                      <xsl:with-param name="parm-edition-type" select="$edition-type" tunnel="yes"/>
                      <xsl:with-param name="parm-hgv-gloss" select="$hgv-gloss" tunnel="yes"/>
                      <xsl:with-param name="parm-leiden-style" select="$leiden-style" tunnel="yes"/>
                      <xsl:with-param name="parm-line-inc" select="$line-inc" tunnel="yes" as="xs:double"/>
                      <xsl:with-param name="parm-verse-lines" select="$verse-lines" tunnel="yes"/>
                    </xsl:apply-templates>
                    <!--<xsl:for-each select="pi:get-docs($relations[contains(., '/ddbdp/') and not(contains($replaces,.))], 'xml')/t:TEI">
                      <xsl:apply-templates select="." mode="text">
                        <xsl:with-param name="parm-apparatus-style" select="$apparatus-style" tunnel="yes"/>
                        <xsl:with-param name="parm-edn-structure" select="$edn-structure" tunnel="yes"/>
                        <xsl:with-param name="parm-edition-type" select="$edition-type" tunnel="yes"/>
                        <xsl:with-param name="parm-hgv-gloss" select="$hgv-gloss" tunnel="yes"/>
                        <xsl:with-param name="parm-leiden-style" select="$leiden-style" tunnel="yes"/>
                        <xsl:with-param name="parm-line-inc" select="$line-inc" tunnel="yes" as="xs:double"/>
                        <xsl:with-param name="parm-verse-lines" select="$verse-lines" tunnel="yes"/>
                      </xsl:apply-templates>
                    </xsl:for-each>-->
                  
                </xsl:if>

  </xsl:template>

  <xsl:template match="t:TEI" mode="text">

      <xsl:variable name="text">
        <xsl:apply-templates select="."/>
      </xsl:variable>
      <!-- Moded templates found in htm-tpl-sqbrackets.xsl -->
      <xsl:apply-templates select="$text" mode="sqbrackets"/>
     
  </xsl:template>
  
  <!-- add rule for t:body to make some preparations for translation files -->
  <xsl:variable name="languages" as="node()">
    <langUsage>
      <language ident="fr">Französisch</language>
      <language ident="en">Englisch</language>
      <language ident="de">Deutsch</language>
      <language ident="it">Italienisch</language>
      <language ident="es">Spanisch</language>
      <language ident="la">Latein</language>
      <language ident="el">Griechisch</language>
    </langUsage>
  </xsl:variable>
  <xsl:template match="t:body">
    <xsl:if test="t:div[@type='translation'] and count(t:div[@type='translation']) = count(t:div)">
      <xsl:message select="string($languages/language[@ident='fr'])"/>
      <select id="dashboardTranslationPicker">
        <xsl:for-each select="t:div">
          <option name="{concat(@type, '_', @xml:lang)}">
            <xsl:value-of select="$languages/language[@ident = current()/@xml:lang]"/>
          </option>
        </xsl:for-each>
      </select>
    </xsl:if>
    <xsl:apply-templates/>
  </xsl:template>
  

  <!-- Commentary links -->
  <xsl:template match="t:div[@type='commentary']/t:list/t:item/t:ref">
    <a href="{parent::t:item/@corresp}"><xsl:apply-templates/></a>.
  </xsl:template>
  
  <!-- Generate parallel reference string -->
  <xsl:template name="get-references">
    <xsl:if test="$collection = 'hgv'">HGV </xsl:if>
    <xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']"></xsl:value-of>
    <xsl:if test="count($relations[contains(., 'hgv/')]) gt 0"> = HGV </xsl:if>
    <xsl:for-each select="$relations[contains(., 'hgv/')]">
      <xsl:if test="doc-available(pi:get-filename(., 'xml'))">
        <xsl:for-each select="normalize-space(doc(pi:get-filename(., 'xml'))//t:bibl[@type = 'publication' and @subtype='principal'])"> 
          <xsl:text> </xsl:text>
          <xsl:value-of select="."/>       
        </xsl:for-each>
        <xsl:if test="contains($relations[position() + 1], 'hgv/')">; </xsl:if>
        <xsl:if test="position() != last()"> = </xsl:if>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each-group select="$relations[contains(., 'hgv/')]" group-by="replace(., '[a-z]', '')">
      <xsl:if test="contains(., 'hgv')">
        = Trismegistos <a href="http://www.trismegistos.org/text/{replace(pi:get-id(.), '[a-z]', '')}"><xsl:value-of select="replace(pi:get-id(.), '[a-z]', '')"/></a>
    </xsl:if></xsl:for-each-group>
    <xsl:for-each select="$relations[contains(., 'apis/')]"> = <xsl:value-of select="pi:get-id(.)"></xsl:value-of></xsl:for-each>
    <xsl:for-each select="tokenize($isReplacedBy, '\s')"> = <xsl:value-of select="pi:get-id(.)"></xsl:value-of></xsl:for-each>
    <xsl:for-each select="tokenize($replaces, '\s')"> = <xsl:value-of select="pi:get-id(.)"></xsl:value-of></xsl:for-each>
  </xsl:template>
  
  <!-- Override EpiDoc template in htm-teihead.xsl -->
  <xsl:template match="t:head"/>

  <!-- Override template in htm-teiref.xsl -->
  <xsl:template match="t:ref"/>

  <xsl:template match="rdf:Description">
    <xsl:value-of select="@rdf:about"/>
  </xsl:template>
</xsl:stylesheet>
