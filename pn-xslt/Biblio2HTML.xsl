<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://www.tei-c.org/ns/1.0" 
  xmlns:xi="http://www.w3.org/2001/XInclude"
  xmlns:pi="http://papyri.info/ns"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:import href="pi-global-varsandparams.xsl"/>
  <xsl:variable name="path">/srv/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase">/srv/data/papyri.info/pn/idp.html</xsl:variable>
  <xsl:variable name="tmbase">/srv/data/papyri.info/TM/files</xsl:variable>
  <xsl:variable name="resolve-uris" select="false()"/>
  <xsl:include href="htm-teibibl.xsl"/>
  <xsl:include href="pi-functions.xsl"/>
  
  <xsl:output method="html"/>
  
  <xsl:template match="/">
    <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
    <html lang="en">
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <link rel="stylesheet" href="/css/yui/reset-fonts-grids.css" type="text/css" media="screen" title="no title" charset="utf-8"/>
        <link rel="stylesheet" href="/css/master.css" type="text/css" media="screen" title="no title" charset="utf-8" />
        <title><xsl:value-of select="t:bibl/t:idno[@type='pi']"/></title>
        <link rel="stylesheet" href="/css/custom-theme/jquery-ui-1.14.1.min.css" type="text/css" media="screen" title="no title" charset="utf-8" />
        <link rel="stylesheet" href="/css/custom-theme/jquery-ui-dul-theme-shim.css" type="text/css" media="screen" title="no title" charset="utf-8" />
        <script src="/js/jquery-3.7.1.min.js" type="text/javascript" charset="utf-8"></script>
        <script src="/js/jquery-ui-1.14.1.min.js" type="text/javascript" charset="utf-8"></script>
        <script src="/js/init.js" type="text/javascript" charset="utf-8"></script>
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
      </head>
      <body onload="init()">
        <div id="d">
          <div id="hd">
            <h1>Papyri.info</h1>
            <h2 id="login"><a href="/editor/user/signin">sign in</a></h2>
          </div>
          <div id="bd">
            <xi:include href="nav.xml"/>
            <div id="main">
              <div class="content ui-corner-all">
                <xsl:apply-templates select="t:bibl"/>
                <div id="ld" class="data">
                  <xsl:variable name="id" select="/t:bibl/t:idno[@type='pi']"/>
                  <xsl:variable name="url">/biblio/<xsl:value-of select="$id"/></xsl:variable>
                  <h2>Linked Data</h2>
                  <p><a href="{$url}/rdf">RDF/XML</a> | 
                    <a href="{$url}/turtle">Turtle</a> | 
                    <a href="{$url}/n3">N-Triples</a> |
                    <a href="{$url}/json">JSON</a> | 
                    <a href="{$url}/graph">Graph Visualization</a></p>
                </div>
              </div>
            </div>
          </div>
          <xi:include href="footer.xml"/>
        </div>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>
