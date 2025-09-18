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
  version="3.0" exclude-result-prefixes="#all">

  <xsl:import href="pi-global-varsandparams.xsl"/>
  <xsl:import href="morelikethis-varsandparams.xsl"/>
  <xsl:import href="../epidoc-xslt/functions.xsl"/>

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

  <!-- Parameters -->
  <xsl:param name="collection" required="yes"/>
  <xsl:param name="related"/>
  <xsl:param name="replaces"/>
  <xsl:param name="isReplacedBy"/>
  <xsl:param name="isPartOf"/>
  <xsl:param name="sources"/>
  <xsl:param name="sources-for"/>
  <xsl:param name="images"/>
  <xsl:param name="citationForm"/>
  <xsl:param name="selfUrl" required="yes"/>
  <xsl:param name="biblio"/>
  <xsl:param name="translations"/>
  <xsl:param name="server">papyri.info</xsl:param>
  <xsl:param name="path">/srv/data/papyri.info/idp.data</xsl:param>
  <!-- variables to assist in offline testing by controlling paths and behaviors in the output html -->
  <xsl:param name="cssbase">/css</xsl:param>
  <xsl:param name="jsbase">/js</xsl:param>
  <xsl:param name="analytics">yes</xsl:param>

  <xsl:variable name="relations" select="tokenize($related, '\s+')"/>
  <xsl:variable name="imgs" select="tokenize($images, '\s+')"/>
  <xsl:variable name="biblio-relations" select="tokenize($biblio, '\s+')"/>
  <xsl:variable name="outbase">/srv/data/papyri.info/pn/idp.html</xsl:variable>
  <xsl:variable name="tmbase">/srv/data/papyri.info/TM/files</xsl:variable>
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

  <xsl:output method="html" html-version="5"/>

  <xsl:template match="/">
    <!-- set variables to control dispatch of transformation based on context -->
    <xsl:variable name="apis" select="$collection = 'apis' or contains($related, '/apis/')"/>
    <xsl:variable name="dclp" select="contains($related, 'dclp/')"/>
    <xsl:variable name="ddbdp" select="contains($related, '/ddbdp/')"/>
    <xsl:variable name="hgv" select="$collection = 'hgv' or contains($related, 'hgv/')"/>
    <xsl:variable name="tm" select="contains($related, 'trismegistos.org/')"/>
    <xsl:variable name="current" select="$collection = 'current'"/>
    <xsl:variable name="historical" select="$collection = 'editions'"/>
    <xsl:variable name="image" select="count($imgs) gt 0"/>

    <!-- start writing the output file -->
    <html lang="en">
      <head>
        <link rel="schema.dcterms" href="http://purl.org/dc/terms/"/>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta property="dcterms.identifier" content="{$selfUrl}"/>
        <xsl:call-template name="collection-hierarchy">
          <xsl:with-param name="all-ancestors"><xsl:value-of select="$isPartOf"></xsl:value-of></xsl:with-param>
        </xsl:call-template>
        <xsl:for-each select="tokenize($sources, '\s')">
          <meta property="dcterms.source" content="{.}"/>
        </xsl:for-each>
        <xsl:for-each select="$relations">
          <meta property="dcterms.relation" content="{.}"/>
        </xsl:for-each>
        <xsl:if test="string-length($citationForm) > 0">
            <meta property="dcterms.bibliographicCitation" datatype="xsd:string" content="{replace($citationForm, '&quot;', '')}"/>
        </xsl:if>

        <!-- https://getbootstrap.com/docs/5.3/getting-started/download/#cdn-via-jsdelivr -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous"/>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>

        <!-- Google Fonts -->
        <link rel="preconnect" href="https://fonts.googleapis.com"/>
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin="anonymous"/>
        <link href="https://fonts.googleapis.com/css2?family=Noto+Naskh+Arabic:wght@400..700&amp;family=Noto+Sans+Coptic&amp;family=Noto+Sans+Symbols:wght@400..700&amp;family=Noto+Sans:ital,wght@0,400..700;1,400..700&amp;family=Noto+Serif:ital,wght@0,400..700;1,400..700&amp;display=swap" rel="stylesheet" />

        <link rel="stylesheet" href="{$cssbase}/theme-variables.css" type="text/css"/>
        <link rel="stylesheet" href="{$cssbase}/main.css" type="text/css"/>

        <xsl:if test="$image">
          <link rel="stylesheet" href="{$cssbase}/imageviewer.css" type="text/css" />
        </xsl:if>
        <link rel="bookmark" href="{$selfUrl}" title="Canonical URI"/>
        <!-- document title -->
        <title>
          <xsl:call-template name="title-references"/>
        </title>
        <!-- scripts -->
        <script src="{$jsbase}/jquery-3.7.1.min.js" type="text/javascript" charset="utf-8"></script>
        <xsl:if test="$image">
          <script src="{$jsbase}/imageviewer.js" type="text/javascript" charset="utf-8"></script>
        </xsl:if>
        <script src="{$jsbase}/init.js" type="text/javascript" charset="utf-8"></script>
        <xsl:if test="$analytics='yes'">
          <script>
            var _paq = window._paq = window._paq || [];
            _paq.push(['trackPageView']);
            _paq.push(['enableLinkTracking']);
            (function() {
              var u="//analytics.lib.duke.edu/";
              _paq.push(['setTrackerUrl', u+'matomo.php']);
              _paq.push(['setSiteId', '34']);
              var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
              g.async=true; g.src=u+'matomo.js'; s.parentNode.insertBefore(g,s);
            })();
          </script>
        </xsl:if>
      </head>

      <body onload="init()">
        <nav id="skip-links" class="visually-hidden-focusable bg-dark" data-bs-theme="dark" aria-label="Skip link navigation">
          <div class="container-xl px-4">
            <a href="#main" class="d-inline-flex p-2 m-1 text-decoration-none">Skip to main content</a>
          </div>
        </nav>

        <header id="masthead" class="bg-primary text-white position-relative">
          <nav id="masthead-nav" class="navbar flex-wrap navbar-expand-md" data-bs-theme="dark" aria-label="Masthead &amp; user account navigation">
            <div class="container-xl px-4">
              <div class="d-flex justify-content-between align-items-center w-100">
                <a href="/" class="navbar-brand">Papyri.info</a>
                <div class="d-flex align-items-center">
                  <div id="login">
                    <a href="/editor/user/signin" class="btn btn-link text-decoration-none">Sign In</a>
                  </div>
                  <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNavbar" aria-controls="mainNavbar" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                  </button>
                </div>
              </div>
            </div>
          </nav>
          <xi:include href="nav.xml"/>
        </header>

        <main id="main" class="container-fluid p-0 flex-grow-1 bg-light d-flex flex-column">
          <div class="container-xl px-4 py-4 my-0 bg-white flex-grow-1">
            <div class="content">
              <h1>
                <xsl:call-template name="get-references"/>
              </h1>
              <xsl:if test="$hgv or $apis or $dclp">
                <h2 id="titledate"></h2>
              </xsl:if>
              <div id="controls" class="d-flex flex-wrap align-items-center bg-light p-3 mb-4">
                <xsl:if test="$hgv or $apis or $dclp">
                  <div id="metadatacontrols" class="me-3">
                    <div class="dropdown">
                      <div class="form-check form-switch d-inline-block">
                        <input class="form-check-input" type="checkbox" name="metadata" id="mdt" checked="checked"/>
                        <label class="form-check-label" for="mdt">metadata</label>
                      </div>
                      <button class="btn btn-sm btn-link dropdown-toggle" type="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false" aria-label="Toggle metadata sections"></button>
                      <div class="dropdown-menu p-2">
                        <xsl:if test="$hgv">
                          <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="hgv" id="hgvm" checked="checked"/>
                            <label class="form-check-label" for="hgvm">HGV data</label>
                          </div>
                        </xsl:if>
                        <xsl:if test="$tm">
                          <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="tm" id="tmm" checked="checked"/>
                            <label class="form-check-label" for="tmm">TM data</label>
                          </div>
                        </xsl:if>
                        <xsl:if test="$apis">
                          <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="apis" id="apism" checked="checked"/>
                            <label class="form-check-label" for="apism">APIS catalog record</label>
                          </div>
                        </xsl:if>
                        <xsl:if test="$dclp">
                          <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="dclp" id="dclpm" checked="checked"/>
                            <label class="form-check-label" for="dclpm">DCLP data</label>
                          </div>
                        </xsl:if>
                      </div>
                    </div>
                  </div>
                </xsl:if>
                <xsl:if test="$ddbdp or $image or $translations or $dclp">
                  <div id="textcontrols" class="me-3">
                    <div class="dropdown">
                      <div class="form-check form-switch d-inline-block">
                        <input class="form-check-input" type="checkbox" name="text" id="txt" checked="checked"/>
                        <label class="form-check-label" for="txt">text</label>
                      </div>
                      <button class="btn btn-sm btn-link dropdown-toggle" type="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false" aria-label="Toggle text sections"></button>
                      <div class="dropdown-menu p-2">
                        <xsl:if test="$ddbdp or $dclp">
                          <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="transcription" id="tcpt" checked="checked"/>
                            <label class="form-check-label" for="tcpt">transcription</label>
                          </div>
                        </xsl:if>
                        <xsl:if test="$image">
                          <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="image" id="img" checked="checked"/>
                            <label class="form-check-label" for="img">images</label>
                          </div>
                        </xsl:if>
                        <xsl:if test="$translations">
                          <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="translation" id="tslt" checked="checked"/>
                            <label class="form-check-label" for="tslt">translation</label>
                          </div>
                        </xsl:if>
                      </div>
                    </div>
                  </div>
                </xsl:if>
                <!-- todo: add dclp handling here, similar to what's below for other collections -->
                <xsl:if test="$current">
                  <div id="editthis" class="me-3">
                    <a href="/editor/publications/create_from_identifier/papyri.info/current/{/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']}" rel="nofollow" class="btn btn-sm btn-outline-primary"><i class="bi bi-edit"></i> open in editor</a>
                  </div>
                </xsl:if>
                <xsl:if test="$historical">
                  <div id="editthis" class="me-3">
                    <a href="/editor/publications/create_from_identifier/papyri.info/historical/{/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']}" rel="nofollow" class="btn btn-sm btn-outline-primary"><i class="bi bi-edit"></i> open in editor</a>
                  </div>
                </xsl:if>
                <xsl:if test="$hgv and not($current)">
                  <div id="editthis" class="me-3">
                    <a href="/editor/publications/create_from_identifier/papyri.info/hgv/{/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']}" rel="nofollow" class="btn btn-sm btn-outline-primary"><i class="bi bi-edit"></i> open in editor</a>
                  </div>
                </xsl:if>
                <xsl:if test="$apis and not($dclp or $ddbdp or $hgv)">
                  <div id="editthis">
                    <a href="/editor/publications/create_from_identifier/papyri.info/apis/{/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='apisid']}" rel="nofollow" class="btn btn-sm btn-outline-primary"><i class="bi bi-edit"></i> open in editor</a>
                  </div>
                </xsl:if>
                <div id="canonical-uri" class="me-3">
                  <span id="canonical-uri-label">Canonical URI: </span>
                  <span id="canonical-uri-value">
                    <a href="{$selfUrl}">
                      <xsl:value-of select="$selfUrl"/>
                    </a>
                  </span>
                </div>
              </div>
              <xsl:if test="$collection = 'current'">
                <xsl:if test="$hgv or $apis or $tm or $dclp">
                  <div class="metadata">
                    <xsl:for-each select="$relations[contains(., 'hgv/')]">
                      <xsl:sort select="." order="ascending"/>
                      <xsl:choose>
                        <xsl:when test="doc-available(pi:get-filename(., 'xml'))">
                          <xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/t:TEI" mode="metadata"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:message>Error: <xsl:value-of select="."/>(<xsl:value-of select="pi:get-filename(., 'xml')"/>) not available. Error in <xsl:value-of select="$doc-id"/>.</xsl:message>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                    <xsl:for-each select="$relations[contains(.,'trismegistos.org')]">
                      <xsl:sort select="." order="ascending"/>
                      <xsl:if test="doc-available(pi:get-filename(., 'xml'))">
                        <xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/text" mode="metadata"/>
                      </xsl:if>
                    </xsl:for-each>
                    <xsl:for-each select="$relations[contains(., '/apis/')]">
                      <xsl:sort select="." order="ascending"/>
                      <xsl:choose>
                        <xsl:when test="doc-available(pi:get-filename(., 'xml'))">
                          <xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/t:TEI" mode="metadata"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:message>Error: <xsl:value-of select="."/>(<xsl:value-of select="pi:get-filename(., 'xml')"/>) not available. Error in <xsl:value-of select="$doc-id"/>.</xsl:message>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                    <xsl:for-each select="$relations[contains(., 'dclp/')]">
                      <xsl:sort select="." order="ascending"/>
                      <xsl:choose>
                        <xsl:when test="doc-available(pi:get-filename(., 'xml'))">
                          <xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/t:TEI" mode="metadata"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:message>Error: <xsl:value-of select="."/>(<xsl:value-of select="pi:get-filename(., 'xml')"/>) not available. Error in <xsl:value-of select="$doc-id"/>.</xsl:message>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                    <xsl:call-template name="biblio"/>
                  </div>
                </xsl:if>
                <div class="text row">
                  <xsl:apply-templates select="/t:TEI" mode="text">
                    <xsl:with-param name="parm-apparatus-style" select="$apparatus-style" tunnel="yes"/>
                    <xsl:with-param name="parm-internal-app-style" select="$apparatus-style" tunnel="yes"/>
                    <xsl:with-param name="parm-edn-structure" select="$edn-structure" tunnel="yes"/>
                    <xsl:with-param name="parm-edition-type" select="$edition-type" tunnel="yes"/>
                    <xsl:with-param name="parm-hgv-gloss" select="$hgv-gloss" tunnel="yes"/>
                    <xsl:with-param name="parm-leiden-style" select="$leiden-style" tunnel="yes"/>
                    <xsl:with-param name="parm-line-inc" select="$line-inc" tunnel="yes" as="xs:double"/>
                    <xsl:with-param name="parm-verse-lines" select="$verse-lines" tunnel="yes"/>
                  </xsl:apply-templates>
                  <xsl:if test="$image">
                    <xsl:call-template name="images"/>
                  </xsl:if>
                  <xsl:if test="$translations">
                    <xsl:call-template name="translations"/>
                  </xsl:if>
                </div>
              </xsl:if>
              <xsl:if test="$collection = 'editions'">
                <div class="text">
                  <xsl:apply-templates select="/t:TEI" mode="text">
                    <xsl:with-param name="parm-apparatus-style" select="$apparatus-style" tunnel="yes"/>
                    <xsl:with-param name="parm-internal-app-style" select="$apparatus-style" tunnel="yes"/>
                    <xsl:with-param name="parm-edn-structure" select="$edn-structure" tunnel="yes"/>
                    <xsl:with-param name="parm-edition-type" select="$edition-type" tunnel="yes"/>
                    <xsl:with-param name="parm-hgv-gloss" select="$hgv-gloss" tunnel="yes"/>
                    <xsl:with-param name="parm-leiden-style" select="$leiden-style" tunnel="yes"/>
                    <xsl:with-param name="parm-line-inc" select="$line-inc" tunnel="yes" as="xs:double"/>
                    <xsl:with-param name="parm-verse-lines" select="$verse-lines" tunnel="yes"/>
                  </xsl:apply-templates>
                  <xsl:if test="$image">
                    <xsl:call-template name="images"/>
                  </xsl:if>
                  <xsl:if test="$translations">
                    <xsl:call-template name="translations"/>
                  </xsl:if>
                </div>
              </xsl:if>
              <xsl:if test="$collection = 'hgv'">
                <div class="metadata">
                  <xsl:apply-templates select="/t:TEI" mode="metadata"/>
                  <xsl:for-each select="$relations[contains(.,'trismegistos.org')]">
                    <xsl:sort select="." order="ascending"/>
                    <xsl:if test="doc-available(pi:get-filename(., 'xml'))">
                      <xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/text" mode="metadata"/>
                    </xsl:if>
                  </xsl:for-each>
                  <xsl:if test="$apis">
                    <xsl:for-each select="$relations[contains(., '/apis/')]">
                      <xsl:choose>
                        <xsl:when test="doc-available(pi:get-filename(., 'xml'))">
                          <xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/t:TEI" mode="metadata"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:message>Error: <xsl:value-of select="."/>(<xsl:value-of select="pi:get-filename(., 'xml')"/>) not available. Error in <xsl:value-of select="$doc-id"/>.</xsl:message>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                  </xsl:if>
                  <xsl:call-template name="biblio"/>
                </div>
                <xsl:if test="$apis">
                  <div class="text">
                    <xsl:for-each select="$relations[contains(., '/apis/')]">
                      <xsl:choose>
                        <xsl:when test="doc-available(pi:get-filename(., 'xml'))">
                          <xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/t:TEI" mode="apistrans"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:message>Error: <xsl:value-of select="."/>(<xsl:value-of select="pi:get-filename(., 'xml')"/>) not available. Error in <xsl:value-of select="$doc-id"/>.</xsl:message>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                  </div>
                </xsl:if>
              </xsl:if>
              <xsl:if test="$collection = 'apis'">
                <xsl:for-each select="$relations[contains(.,'trismegistos.org')]">
                  <xsl:sort select="." order="ascending"/>
                  <xsl:if test="doc-available(pi:get-filename(., 'xml'))">
                    <xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/text" mode="metadata"/>
                  </xsl:if>
                </xsl:for-each>
                <div class="metadata">
                  <xsl:apply-templates select="/t:TEI" mode="metadata"/>
                  <xsl:call-template name="biblio"/>
                </div>
                <div class="text">
                  <xsl:if test="$image">
                    <xsl:call-template name="images"/>
                  </xsl:if>
                </div>
              </xsl:if>
              <div id="ld" class="data">
                <h2>Linked Data</h2>
                <p>
                  <a href="{replace($selfUrl,'http://papyri.info','')}/rdf">RDF/XML</a> |
                  <a href="{replace($selfUrl,'http://papyri.info','')}/turtle">Turtle</a> |
                  <a href="{replace($selfUrl,'http://papyri.info','')}/n3">N-Triples</a> |
                  <a href="{replace($selfUrl,'http://papyri.info','')}/json">JSON</a> |
                  <a href="{replace($selfUrl,'http://papyri.info','')}/graph">Graph Visualization</a>
                </p>
              </div>
            </div>
          </div>
        </main>

        <xi:include href="footer.xml"/>

      </body>
    </html>
  </xsl:template>

  <xsl:template name="images">
    <div id="image" class="image data">
      <h2>Image<xsl:if test="count($imgs) &gt; 1">s</xsl:if> [<a href="{$selfUrl}/images" target="_blank">open in new window</a>]</h2>
        <ul>
          <xsl:for-each select="$imgs">
            <li><a href="{.}" class="imagelink" alt="papyrus image"><xsl:value-of select="substring-after(substring-after(.,'images/'),'/')"/></a></li>
          </xsl:for-each>
        </ul>
        <p class="rights"><b>Notice</b>: Each library participating in APIS has its own policy
          concerning the use and reproduction of digital images included in APIS.  Please contact
          the <a href="http://www.columbia.edu/cu/lweb/projects/digital/apis/permissions.html">owning institution</a>
          if you wish to use any image in APIS or to publish any material from APIS.</p>
    </div>
  </xsl:template>

  <xsl:template name="translations">
    <div class="translations col-12 col-lg-6">
      <xsl:for-each select="pi:get-docs(tokenize($translations), 'xml')">
        <xsl:sort select="number(substring-after(/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename'], '-'))"/>
        <div class="translation data">
          <h2><xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'filename']"/> Translation (<xsl:value-of select="/t:TEI/t:teiHeader//t:langUsage/t:language[@ident = //t:body/t:div/@xml:lang]"/>)
            <a class="btn btn-link fw-semibold text-decoration-none" href="/translation/{/t:TEI/t:teiHeader//t:idno[@type = 'filename']}/source"><i class="bi bi-xml"></i>xml</a></h2>
          <div lang="{@xml:lang}">
            <xsl:apply-templates>
              <xsl:with-param name="parm-leiden-style" select="$leiden-style" tunnel="yes"/>
            </xsl:apply-templates>
          </div>
        </div>
      </xsl:for-each>
    </div>
  </xsl:template>

  <xsl:function name="pi:get-toc">
    <xsl:param name="parts"/>
    <xsl:choose>
      <xsl:when test="$parts[1]/@rdf:resource">
        <xsl:for-each select="$parts">
          <xsl:sort select="replace(pi:get-id(@rdf:resource), '[-a-z;/_,.]', '')" data-type="number"/>
          <xsl:sequence select="string(@rdf:resource)"/>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$parts[1]/sl:binding">
        <xsl:for-each select="$parts">
          <xsl:sort select="number(replace(pi:decode-uri(sl:binding[@name='b']/sl:uri), '[^0-9]', ''))"/>
          <xsl:sequence select="string(sl:binding[@name='a']/sl:uri)"/>
        </xsl:for-each>
      </xsl:when>
    </xsl:choose>
  </xsl:function>

  <xsl:template match="t:TEI" mode="text">
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='dclp']">DCLP</xsl:when>
        <xsl:otherwise>DDbDP</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <div class="transcription data col-12 col-lg-6">
      <xsl:choose>
        <xsl:when test="$type = 'DCLP'">
          <h2>DCLP transcription <a class="btn btn-link fw-semibold text-decoration-none" href="/dclp/{t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='dclp']}/source"><i class="bi bi-xml"></i>xml</a></h2>
        </xsl:when>
        <xsl:otherwise>
          <h2>DDbDP transcription <a class="btn btn-link fw-semibold text-decoration-none" href="/{$collection}/{t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']}/source"><i class="bi bi-xml"></i>xml</a></h2></xsl:otherwise></xsl:choose>
      <xsl:variable name="text">
        <xsl:choose>
          <xsl:when test="$type = 'DCLP'">
            <h2><xsl:apply-templates select=".//t:body/t:head"/></h2>
            <xsl:apply-templates select=".//t:div[@type='edition']"/>
          </xsl:when>
          <xsl:otherwise><xsl:apply-templates select=".//t:body"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <!-- Moded templates found in htm-tpl-sqbrackets.xsl -->
      <xsl:apply-templates select="$text" mode="sqbrackets"/>

      <h2>History</h2>
      <div id="history" class="mb-4">
        <div class="accordion mb-2">
          <div class="accordion-item">
            <h3 class="accordion-header">
              <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#editorial-history-panel" aria-expanded="false" aria-controls="editorial-history-panel">
                      Editorial History
              </button>
            </h3>
            <div id="editorial-history-panel" class="accordion-collapse collapse">
              <div class="accordion-body">
                <ul id="edit-history-list">
                  <xsl:choose>
                    <!-- this test will need to be changed if a @type attribute is added to <change>, as discussed at http://idp.atlantides.org/trac/idp/ticket/967 -->
                    <xsl:when test="count(t:teiHeader/t:revisionDesc/t:change[contains(@when, 'T')]) &gt; 0">
                      <xsl:for-each select="t:teiHeader/t:revisionDesc/t:change[contains(@when, 'T')]">
                        <li>
                          <xsl:value-of select="@when"/>
                          [<a href="{@who}">
                            <xsl:choose>
                              <xsl:when test="ends-with(@who,'about')">papyri.info</xsl:when>
                              <xsl:otherwise>
                                <xsl:value-of select="replace(@who,'.*/([^/]+)$','$1')"/>
                              </xsl:otherwise>
                            </xsl:choose>
                          </a>]:
                          <xsl:apply-templates/>
                        </li>
                      </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                      <li>No editorial history recorded.</li>
                    </xsl:otherwise>
                  </xsl:choose>
                </ul>
              </div>
            </div>
          </div>
          <div class="accordion-item">
            <h3 class="accordion-header">
              <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#all-history-panel" aria-expanded="false" aria-controls="all-history-panel">
                      All History
              </button>
            </h3>
            <div id="all-history-panel" class="accordion-collapse collapse">
              <div class="accordion-body">
                <ul id="all-history-list">
                  <xsl:choose>
                    <!-- this test will need to be changed if a @type attribute is added to <change>, as discussed at http://idp.atlantides.org/trac/idp/ticket/967 -->
                    <xsl:when test="count(t:teiHeader/t:revisionDesc/t:change[matches(@when, '^\d{4}-\d{2}-\d{2}$')])">
                      <xsl:for-each select="t:teiHeader/t:revisionDesc/t:change[matches(@when, '^\d{4}-\d{2}-\d{2}$')]">
                        <li>
                          <xsl:value-of select="@when"/>
                          [<a href="{@who}">
                            <xsl:choose>
                              <xsl:when test="ends-with(@who,'about')">papyri.info</xsl:when>
                              <xsl:otherwise>
                                <xsl:value-of select="replace(@who,'.*/([^/]+)$','$1')"/>
                              </xsl:otherwise>
                            </xsl:choose>
                          </a>]:
                          <xsl:value-of select="."/>
                        </li>
                      </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                      <li>No further history recorded.</li>
                    </xsl:otherwise>
                  </xsl:choose>
                </ul>
              </div>
            </div>
          </div>
        </div>
        <p>
          <xsl:choose>
            <xsl:when test="t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='dclp']">
              <a href="{pi:get-blame-url(t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='dclp'])}" target="_blank">Detailed history</a>
            </xsl:when>
            <xsl:otherwise>
              <a href="{pi:get-blame-url(t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='ddb-hybrid'])}" target="_blank">Detailed history</a>
            </xsl:otherwise>
          </xsl:choose>
        </p>
      </div>
      <!-- closing #history -->
      <p><a rel="license" href="https://creativecommons.org/licenses/by/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/80x15.png" /></a> © Duke Databank of Documentary Papyri.
        This work is licensed under a <a rel="license" href="https://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 License</a>.</p>
    </div>
  </xsl:template>

  <xsl:template match="t:revisionDesc" mode="history">
    <xsl:variable name="file-uri" select="ceiling(number(//t:idno[@type='TM'] div 1000))"/>
    <div id="history" class="text">
      <div id="history-headers">
        <h3><span id="edit-history">Editorial History</span>;
          <span id="all-history">All History</span>;
          (<a href="{concat('https://github.com/papyri/idp.data/blame/master/DCLP/',$file-uri,'/',/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename'],'.xml')}" target="_blank">detailed</a>)</h3>
      </div>
      <div id="history-lists">
        <ul id="edit-history-list" style="display:none;">
          <xsl:choose>
            <!-- this test will need to be changed if a @type attribute is added to <change>, as discussed at http://idp.atlantides.org/trac/idp/ticket/967 -->
            <xsl:when test="count(t:change[contains(@when, 'T')]) &gt; 0">
              <xsl:for-each select="t:change[contains(@when, 'T')]">
                <li><xsl:value-of select="@when"/> [<a href="{@who}"><xsl:choose><xsl:when test="ends-with(@who,'about')">papyri.info</xsl:when><xsl:otherwise><xsl:value-of select="replace(@who,'.*/([^/]+)$','$1')"/></xsl:otherwise></xsl:choose></a>]: <xsl:apply-templates/></li>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <li>No editorial history recorded.</li>
            </xsl:otherwise>
          </xsl:choose>
        </ul>
        <ul id="all-history-list" style="display:none">
          <xsl:choose>
            <!-- this test will need to be changed if a @type attribute is added to <change>, as discussed at http://idp.atlantides.org/trac/idp/ticket/967 -->
            <xsl:when test="count(t:change[matches(@when, '^\d{4}-\d{2}-\d{2}$')])">
              <xsl:for-each select="t:change[matches(@when, '^\d{4}-\d{2}-\d{2}$')]">
                <li><xsl:value-of select="@when"/> [<a href="{@who}"><xsl:choose><xsl:when test="ends-with(@who,'about')">papyri.info</xsl:when><xsl:otherwise><xsl:value-of select="replace(@who,'.*/([^/]+)$','$1')"/></xsl:otherwise></xsl:choose></a>]: <xsl:value-of select="."/></li>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <li>No further history recorded.</li>
            </xsl:otherwise>
          </xsl:choose>
        </ul>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="t:TEI" mode="apistrans">
    <xsl:if test=".//t:div[@type = 'translation']/t:ab">
      <div class="translation data">
        <h2>APIS Translation (English)</h2>
        <xsl:for-each select=".//t:div[@type = 'translation']/t:ab">
          <p><xsl:value-of select="."/></p>
        </xsl:for-each>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="biblio">
    <xsl:if test="$biblio-relations[1]">
      <div id="bibliography">
        <h3>Citations</h3>
        <ul>
          <xsl:for-each select="$biblio-relations">
            <li><a href="{.}" class="BP">cite</a></li>
          </xsl:for-each>
        </ul>
      </div>
    </xsl:if>
  </xsl:template>

  <!-- Commentary links -->
  <xsl:template match="t:div[@type='commentary']/t:list/t:item/t:ref">
    <a href="{parent::t:item/@corresp}"><xsl:apply-templates/></a>.
  </xsl:template>

  <!-- Generate Title -->
  <xsl:template name="title-references">
    <xsl:if test="$collection = 'hgv'">HGV </xsl:if>
    <xsl:if test="$collection = 'dclp'">DCLP/Trismegistos </xsl:if>
    <xsl:choose>
      <xsl:when test="$collection = 'dclp'">
        <xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']"></xsl:value-of>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']"></xsl:value-of></xsl:otherwise>
    </xsl:choose>
    <xsl:if test="$collection = 'dclp'"> = LDAB <xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='LDAB']"></xsl:value-of></xsl:if>
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
        = Trismegistos <xsl:value-of select="replace(pi:get-id(.), '[a-z]', '')"/>
      </xsl:if>
    </xsl:for-each-group>
    <xsl:for-each select="$relations[contains(., 'apis/')]"> = <xsl:value-of select="pi:get-id(.)"></xsl:value-of></xsl:for-each>
    <xsl:for-each select="tokenize($isReplacedBy, '\s')"> = <xsl:value-of select="pi:get-id(.)"></xsl:value-of></xsl:for-each>
    <xsl:for-each select="tokenize($replaces, '\s')"> = <xsl:value-of select="pi:get-id(.)"></xsl:value-of></xsl:for-each>
  </xsl:template>

  <!-- Generate parallel reference string -->
  <xsl:template name="get-references">
    <xsl:choose>
      <xsl:when test="$collection = 'current'">
        <xsl:apply-templates select="//t:body/t:head"/>
        <xsl:for-each select="$relations[contains(., 'apis/')]"> = <xsl:value-of select="pi:get-id(.)"></xsl:value-of></xsl:for-each>
        <xsl:if test="count($relations[contains(., 'trismegistos/')]) gt 0"> = Trismegistos </xsl:if>
        <xsl:for-each select="$relations[contains(., 'trismegistos/')]">
          <a href="{.}">{replace(., 'https://www.trismegistos.org/text/', '')}</a>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$collection = 'editions'">
        <xsl:for-each select="$sources-for">
          <xsl:if test="doc-available(pi:get-filename(., 'xml'))">
            <xsl:for-each select="doc(pi:get-filename(., 'xml'))">
              <xsl:apply-templates select=".//t:body/t:head"/>
              = <a href="/current/{.//t:fileDesc/t:publicationStmt/t:idno[@type='filename']}">Current Edition</a>
            </xsl:for-each>
          </xsl:if>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$collection = 'dclp'">
            <xsl:if test="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='dclp-hybrid' and not(starts-with(., 'tm'))]">
              <xsl:for-each select="//t:div[@type='bibliography' and @subtype='principalEdition']">
                <xsl:for-each select=".//t:bibl[@type = 'publication' and @subtype='principal']">
                  <xsl:text> </xsl:text>
                  <xsl:value-of select="normalize-space(.)"/>
                </xsl:for-each>
                <xsl:text> = </xsl:text>
              </xsl:for-each>
            </xsl:if>
            <xsl:if test="/t:TEI/t:teiHeader/t:fileDesc/t:titleStmt/t:title[matches(., '^P\.\s*Herc\.')]">
              <xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:titleStmt/t:title[matches(., '^P\.\s*Herc\.')]"/>
              <xsl:text> = </xsl:text>
            </xsl:if>
            <xsl:text>Trismegistos </xsl:text>
            <a href="https://www.trismegistos.org/text/{/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']}"><xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']"></xsl:value-of></a>
            <xsl:text> = </xsl:text>LDAB <xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='LDAB']"></xsl:value-of>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="$collection = 'hgv'">HGV </xsl:if>
            <xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']"></xsl:value-of></xsl:otherwise>
        </xsl:choose>
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
          <xsl:if test="contains(., 'hgv') and not($collection = 'dclp')">
            = Trismegistos <a href="http://www.trismegistos.org/text/{replace(pi:get-id(.), '[a-z]', '')}"><xsl:value-of select="replace(pi:get-id(.), '[a-z]', '')"/></a>
          </xsl:if></xsl:for-each-group>
        <xsl:for-each select="pi:get-docs($relations[contains(., 'dclp/')], 'xml')"> = LDAB <xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='LDAB']"></xsl:value-of></xsl:for-each>
        <xsl:for-each select="$relations[contains(., 'apis/')]"> = <xsl:value-of select="pi:get-id(.)"></xsl:value-of></xsl:for-each>
        <xsl:for-each select="tokenize($isReplacedBy, '\s')"> = <xsl:value-of select="pi:get-id(.)"></xsl:value-of></xsl:for-each>
        <xsl:for-each select="tokenize($replaces, '\s')"> = <xsl:value-of select="pi:get-id(.)"></xsl:value-of></xsl:for-each>
        <xsl:if test="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='APD']"> = APD <a href="http://www.apd.gwi.uni-muenchen.de:8080/apd/show2.jsp?papname={/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='APD']}"><xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='APD']"/></a></xsl:if>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <!-- Apparatus munging
       1. Flatten the text by resolving <supplied> (which can cross word boundaries),
          and turning <hi> and <g> into plain text <hi rend="diairesis">i</hi> -> _hi_rend="diairesis"_i_hi_ (e.g.).
       2. Tokenize the text on space, wrapping the tokens in <pi:t> tags.
       3. Restore the flattened markup.

       This allows us to process the apparatus while collecting, e.g. multiple ancient diacritics on a single word
       into a single apparatus entry. -->

  <xsl:template match="*" mode="app-flatten app-tokenize app-restore">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="t:supplied|t:unclear" mode="app-flatten">
    <xsl:apply-templates select="."/>
  </xsl:template>

  <!-- Flatten nested <hi> -->
  <xsl:template match="t:hi[t:hi]" mode="app-flatten">
    <xsl:variable name="result">
      <t:hi>
        <xsl:attribute name="rend"><xsl:value-of select="string-join(descendant-or-self::*/@rend, '🦊')"/></xsl:attribute>
        <xsl:value-of select="."/>
      </t:hi>
    </xsl:variable>
    <xsl:apply-templates select="$result" mode="app-flatten"/>
  </xsl:template>

  <xsl:template match="t:hi|t:g|t:lb[@break='no']|t:add|t:del|t:subst" mode="app-flatten">🐯<xsl:value-of select="local-name(.)"/>🐯<xsl:for-each select="@*"><xsl:value-of select="name(.)"/>="<xsl:value-of select="translate(.,',.','🦋🐌')"/>"🐯</xsl:for-each><xsl:apply-templates mode="app-flatten"/>🐹<xsl:value-of select="local-name(.)"/>🐹</xsl:template>

  <xsl:template match="text()" mode="app-tokenize">
    <xsl:analyze-string select="." regex="([ \n\r\t,.;;··])+">
      <xsl:matching-substring>
        <xsl:value-of select="."/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <t:w><xsl:value-of select="."/></t:w>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>

  <xsl:template match="text()" mode="app-restore">
    <xsl:variable name="restore" select="translate(.,'🦋🐌',',.')"/>
    <xsl:variable name="pass1">
      <xsl:analyze-string select="$restore" regex="🐯([^🐯]+)🐯(([^🐯]+=&quot;[^&quot;]+&quot;🐯)*)([^🐯]*)">
        <xsl:matching-substring>
          <xsl:element namespace="http://www.tei-c.org/ns/1.0" name="{regex-group(1)}">
            <xsl:attribute name="x">open</xsl:attribute>
            <xsl:analyze-string select="regex-group(2)" regex="([^=]+)=&quot;([^&quot;]+)&quot;🐯">
              <xsl:matching-substring>
                <xsl:attribute name="{regex-group(1)}"><xsl:value-of select="replace(regex-group(2), '🦊', ' ')"/></xsl:attribute>
              </xsl:matching-substring>
            </xsl:analyze-string>
          </xsl:element>
          <xsl:value-of select="regex-group(4)"/>
        </xsl:matching-substring>
        <xsl:non-matching-substring>
          <xsl:value-of select="."/>
        </xsl:non-matching-substring>
      </xsl:analyze-string>
    </xsl:variable>
    <xsl:variable name="pass2">
      <xsl:apply-templates select="$pass1" mode="app-restore-close"/>
    </xsl:variable>
    <!-- Re-impose hierarchy -->
    <xsl:apply-templates select="$pass2/*[1]" mode="app-hierarchy"/>
  </xsl:template>

  <xsl:template match="text()" mode="app-restore-close">
    <xsl:analyze-string select="." regex="🐹([^🐹]+)🐹">
      <xsl:matching-substring>
        <xsl:element namespace="http://www.tei-c.org/ns/1.0" name="{regex-group(1)}">
          <xsl:attribute name="x">close</xsl:attribute>
        </xsl:element>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <txt x="open" xmlns="http://www.tei-c.org/ns/1.0"/><xsl:value-of select="."/><txt x="close" xmlns="http://www.tei-c.org/ns/1.0"/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>

  <xsl:template match="*" mode="app-restore-close">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="*" mode="app-hierarchy">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:variable name="closers" select="following-sibling::*[local-name() = local-name(current())][@x='close']"/>
    <xsl:variable name="close" select="$closers[count(preceding-sibling::*[local-name() = $name][@x='close']) = (count(preceding-sibling::*[local-name() = $name][@x='open']) - 1)][1]"/>
    <xsl:element name="{local-name()}" namespace="http://www.tei-c.org/ns/1.0">
      <xsl:copy-of select="@*[not(local-name() = 'x')]"/>
      <xsl:apply-templates select="following-sibling::*[@x='open'][following-sibling::* intersect $close/preceding-sibling::*][1]" mode="app-hierarchy"/>
    </xsl:element>
    <xsl:apply-templates select="$close/following-sibling::*[@x='open'][1]" mode="app-hierarchy"/>
  </xsl:template>

  <xsl:template match="t:txt" mode="app-hierarchy">
    <xsl:value-of select="following-sibling::text()[1]"/>
    <xsl:if test="following-sibling::*[2][@x='open']">
      <xsl:apply-templates select="following-sibling::*[2]" mode="app-hierarchy"/>
    </xsl:if>
  </xsl:template>

  <!-- restore nested <hi> -->
  <xsl:template match="t:hi[contains(@rend, ' ')]" mode="app-restore">
    <xsl:variable name="rends" select="tokenize(@rend, ' ')"/>
    <t:hi rend="{$rends[1]}"><t:hi rend="{$rends[2]}"><xsl:value-of select="."/></t:hi></t:hi>
  </xsl:template>

  <!-- Override template in htm-tpl-apparatus.xsl -->
  <xsl:template name="tpl-apparatus">
    <!-- An apparatus is only created if one of the following is true -->
    <xsl:if test=".//t:choice | .//t:subst | .//t:app | .//t:g[@type=('apostrophe','high-punctus','middot','low-punctus','diastole','hypodiastole')] |
      .//t:hi[@rend = ('diaeresis','grave','acute','asper','lenis','circumflex')] |
      .//t:del[@rend='slashes' or @rend='cross-strokes'] | .//t:milestone[@rend = 'box']">

      <div id="apparatus" lang="en" class="mt-3">
        <h3>Apparatus</h3>
        <xsl:variable name="pass1">
          <xsl:apply-templates select="." mode="app-flatten"/>
        </xsl:variable>
        <xsl:variable name="pass2">
          <xsl:apply-templates select="$pass1" mode="app-tokenize"/>
        </xsl:variable>
        <xsl:variable name="pass2b">
          <xsl:apply-templates select="$pass2" mode="app-restore"/>
        </xsl:variable>
        <xsl:variable name="pass3">
          <xsl:apply-templates select="$pass2b" mode="app-restore"/>
        </xsl:variable>
        <xsl:variable name="apparatus">
          <!-- An entry is created for-each of the following instances
                  * choice, subst or app not nested in another;
                  * hi not nested in the app part of an app;
                  * del or milestone.
        -->
          <xsl:for-each select="($pass3//t:choice | $pass3//t:subst | $pass3//t:app)[not(ancestor::t:*[local-name()=('choice','subst','app')])] |
            $pass3//t:w[t:g[@type=('apostrophe','high-punctus','middot','low-punctus','diastole','hypodiastole')]] |
            $pass3//t:w[t:hi[@rend=('diaeresis','grave','acute','asper','lenis','circumflex')]][not(ancestor::t:*[local-name()=('orig','reg','sic','corr','lem','rdg')
            or self::t:del[@rend='corrected']
            or self::t:add[@place='inline']][1][local-name()=('reg','corr','rdg')
            or self::t:del[@rend='corrected']]
            or ancestor::t:hi)] |
            $pass3//t:del[@rend='slashes' or @rend='cross-strokes'] | $pass3//t:milestone[@rend = 'box']">
            <app>
              <!-- Found in tpl-apparatus.xsl -->
              <xsl:call-template name="ddbdp-app">
                <xsl:with-param name="apptype">
                  <xsl:choose>
                    <xsl:when test="self::t:choice[child::t:orig and child::t:reg]">
                      <xsl:text>origreg</xsl:text>
                    </xsl:when>
                    <xsl:when test="self::t:choice[child::t:sic and child::t:corr]">
                      <xsl:text>siccorr</xsl:text>
                    </xsl:when>
                    <xsl:when test="self::t:subst">
                      <xsl:text>subst</xsl:text>
                    </xsl:when>
                    <xsl:when test="self::t:app[@type='alternative']">
                      <xsl:text>appalt</xsl:text>
                    </xsl:when>
                    <xsl:when test="self::t:app[@type='editorial'][starts-with(t:lem/@resp,'BL ')]">
                      <xsl:text>appbl</xsl:text>
                    </xsl:when>
                    <xsl:when test="self::t:app[@type='editorial'][starts-with(t:lem/@resp,'PN ')]">
                      <xsl:text>apppn</xsl:text>
                    </xsl:when>
                    <xsl:when test="self::t:app[@type='editorial']">
                      <xsl:text>apped</xsl:text>
                    </xsl:when>
                  </xsl:choose>
                </xsl:with-param>
              </xsl:call-template>
            </app>
          </xsl:for-each>
        </xsl:variable>
        <!-- XSL for-each-group effectively suppresses any duplicate apparatus generated due to sibling triggers.   -->
        <xsl:for-each-group select="$apparatus/*:app" group-by=".">
          <xsl:copy-of select="node()"/>
        </xsl:for-each-group>
      </div>
    </xsl:if>
  </xsl:template>

  <!-- Override EpiDoc templates in htm-teihead.xsl -->
  <xsl:template match="t:div/t:head">
      <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="t:body/t:head">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Override EpiDoc template in htm-teiab.xsl -->
  <xsl:template match="t:ab">
    <xsl:param name="parm-leiden-style" tunnel="yes" required="no"></xsl:param>
    <xsl:param name="parm-edition-type" tunnel="yes" required="no"></xsl:param>
    <span class="ab">
      <xsl:if test="$parm-leiden-style='iospe'">
        <xsl:variable name="div-loc">
          <xsl:for-each select="ancestor::t:div[@type='textpart']">
            <xsl:value-of select="@n"/>
            <xsl:text>-</xsl:text>
          </xsl:for-each>
        </xsl:variable>
        <xsl:attribute name="id">
          <xsl:value-of select="concat('div',$div-loc)"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
      <!-- if next div or ab begins with lb[break=no], then add hyphen -->
      <xsl:if test="following::t:lb[1][@break='no' or @type='inWord'] and not($parm-edition-type='diplomatic')">
        <xsl:text>-</xsl:text>
      </xsl:if>
      <!-- if final lb in ab is L2R or R2L, then print arrow here -->
      <xsl:if test="not($parm-leiden-style=('ddbdp','dclp','sammelbuch'))
        and descendant::t:lb[last()][@rend='left-to-right']">
        <xsl:text>&#xa0;&#xa0;→</xsl:text>
      </xsl:if>
      <xsl:if test="not($parm-leiden-style=('ddbdp','dclp','sammelbuch'))
        and descendant::t:lb[last()][@rend='right-to-left']">
        <xsl:text>&#xa0;&#xa0;←</xsl:text>
      </xsl:if>
      <!-- in IOSPE, if followed by lg, include it here (and suppress in htm-teilgandl.xsl) -->
      <xsl:if test="$parm-leiden-style='iospe' and following-sibling::t:*[1][self::t:lg]">
        <xsl:apply-templates select="following-sibling::t:lg/*"/>
      </xsl:if>
    </span>
  </xsl:template>

  <!-- Override template in htm-teiref.xsl -->
  <xsl:template match="t:ref">
    <xsl:choose>
      <xsl:when test="@type = 'reprint-from'">
        <br/>
        <!-- Found in teiref.xsl -->
        <xsl:call-template name="reprint-text">
          <xsl:with-param name="direction" select="'from'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="@type = 'reprint-in'">
        <br/>
        <!-- Found in teiref.xsl -->
        <xsl:call-template name="reprint-text">
          <xsl:with-param name="direction" select="'in'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="@type = 'Perseus'">
        <xsl:variable name="col" select="substring-before(@href, ';')"/>
        <xsl:variable name="vol" select="substring-before(substring-after(@href,';'),';')"/>
        <xsl:variable name="no" select="substring-after(substring-after(@href,';'),';')"/>
        <a href="http://www.perseus.tufts.edu/cgi-bin/ptext?doc=Perseus:text:1999.05.{$col}:volume={$vol}:document={$no}">
          <xsl:apply-templates/>
        </a>
      </xsl:when>
      <xsl:when test="not(@target)">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <a href="{replace(@target, 'https://papyri.info', '')}">
          <xsl:apply-templates/>
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="t:bibl"><xsl:apply-templates/></xsl:template>

  <!-- Override template in teicertainty.xsl, which is shouty-->
  <xsl:template match="t:certainty">
    <xsl:param name="parm-leiden-style" tunnel="yes" required="no"></xsl:param>
    <xsl:choose>
      <xsl:when test="$parm-leiden-style=('ddbdp','sammelbuch','iospe')">
        <xsl:if test="@match='..'">
          <xsl:text>(?)</xsl:text>
        </xsl:if>
      </xsl:when>
      <xsl:when test="@match='..'">
        <xsl:text>?</xsl:text>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="rdf:Description">
    <xsl:value-of select="@rdf:about"/>
  </xsl:template>
</xsl:stylesheet>
