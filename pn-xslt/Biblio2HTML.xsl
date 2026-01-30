<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns:xi="http://www.w3.org/2001/XInclude"
  xmlns:pi="http://papyri.info/ns/"
  exclude-result-prefixes="#all"
  version="2.0">

  <xsl:import href="pi-global-varsandparams.xsl"/>
  <xsl:variable name="path">/srv/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase">/srv/data/papyri.info/pn/idp.html</xsl:variable>
  <xsl:variable name="tmbase">/srv/data/papyri.info/TM</xsl:variable>
  <xsl:variable name="resolve-uris" select="false()"/>
  <xsl:include href="htm-teibibl.xsl"/>
  <xsl:include href="pi-functions.xsl"/>

  <xsl:output method="html"/>

  <xsl:template match="/">
    <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
    <html lang="en">
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

        <title>Papyri.info - Bibliographic Record <xsl:value-of select="t:bibl/t:idno[@type='pi']"/></title>

        <!-- https://getbootstrap.com/docs/5.3/getting-started/download/#cdn-via-jsdelivr -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous"/>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>

        <!-- Google Fonts -->
        <link rel="preconnect" href="https://fonts.googleapis.com"/>
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin="anonymous"/>
        <link href="https://fonts.googleapis.com/css2?family=Noto+Naskh+Arabic:wght@400..700&amp;family=Noto+Sans+Coptic&amp;family=Noto+Sans+Symbols:wght@400..700&amp;family=Noto+Sans:ital,wght@0,400..700;1,400..700&amp;family=Noto+Serif:ital,wght@0,400..700;1,400..700&amp;display=swap" rel="stylesheet" />

        <link rel="stylesheet" href="/css/theme-variables.css" type="text/css"/>
        <link rel="stylesheet" href="/css/main.css" type="text/css"/>
        <!-- TODO: determine which styles from master.css need to be ported -->
        <!--       over to main.css (if any) -->
        <!-- <link rel="stylesheet" href="/css/master.css" type="text/css" media="screen" title="no title" charset="utf-8"> -->

        <script src="/js/jquery-3.7.1.min.js" type="text/javascript" charset="utf-8"></script>

        <script src="/js/bibliosearch.js" type="text/javascript" charset="utf-8"></script>

        <!-- TODO: determine how much of init.js can be eliminated -->
        <script src="/js/init.js" type="text/javascript" charset="utf-8"></script>

        <script>
          var _paq = window._paq = window._paq || [];
          _paq.push(['trackPageView']);
          _paq.push(['enableLinkTracking']);
          (function() {
            var u="//analytics.lib.duke.edu/";
            _paq.push(['setTrackerUrl', u+'matomo.php']);
            _paq.push(['setSiteId', '34']);
            var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0]; g.async=true; g.src=u+'matomo.js'; s.parentNode.insertBefore(g,s);
          })();
        </script>
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
              <h1>Bibliographic Record <xsl:value-of select="t:bibl/t:idno[@type='pi']"/></h1>

              <xsl:apply-templates select="t:bibl"/>

              <div id="ld" class="data">
                <xsl:variable name="id" select="/t:bibl/t:idno[@type='pi']"/>
                <xsl:variable name="url">/biblio/<xsl:value-of select="$id"/>
                </xsl:variable>
                <h2>Linked Data</h2>
                <p>
                  <a href="{$url}/rdf">RDF/XML</a> |
                  <a href="{$url}/turtle">Turtle</a> |
                  <a href="{$url}/n3">N-Triples</a> |
                  <a href="{$url}/json">JSON</a> |
                  <a href="{$url}/graph">Graph Visualization</a>
                </p>
              </div>
            </div>
          </div>
        </main>

        <xi:include href="footer.xml"/>

        </body>
    </html>
  </xsl:template>

</xsl:stylesheet>
