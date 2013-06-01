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
  
  <xsl:import href="global-varsandparams.xsl"/>
  <xsl:import href="morelikethis-varsandparams.xsl"/>
  
  <!-- html related stylesheets, these may import tei{element} stylesheets if relevant eg. htm-teigap and teigap -->
  <xsl:import href="htm-teiab.xsl"/>
  <xsl:import href="htm-teiaddanddel.xsl"/>
  <xsl:import href="htm-teiapp.xsl"/> 
  <xsl:import href="htm-teidiv.xsl"/>
  <xsl:import href="htm-teidivedition.xsl"/>
  <xsl:import href="htm-teiemph.xsl"/>
  <xsl:import href="htm-teiforeign.xsl"/>
  <xsl:import href="htm-teifigure.xsl"/>
  <xsl:import href="htm-teig.xsl"/>
  <xsl:import href="htm-teigap.xsl"/>
  <xsl:import href="htm-teihead.xsl"/>
  <xsl:import href="htm-teihi.xsl"/>
  <xsl:import href="htm-teilb.xsl"/>
  <xsl:import href="htm-teilgandl.xsl"/>
  <xsl:import href="htm-teilistanditem.xsl"/>
  <xsl:import href="htm-teilistbiblandbibl.xsl"/>
  <xsl:import href="htm-teimilestone.xsl"/>
  <xsl:import href="htm-teinote.xsl"/>
  <xsl:import href="htm-teinum.xsl"/>
  <xsl:import href="htm-teip.xsl"/>
  <xsl:import href="htm-teiseg.xsl"/>
  <xsl:import href="htm-teispace.xsl"/>
  <xsl:import href="htm-teisupplied.xsl"/>
  <xsl:import href="htm-teiterm.xsl"/>
  <xsl:import href="htm-teiref.xsl"/>
  
  <!-- tei stylesheets that are also used by start-txt -->
  <xsl:import href="teiabbrandexpan.xsl"/>
  <xsl:import href="teicertainty.xsl"/>
  <xsl:import href="teichoice.xsl"/>
  <xsl:import href="teihandshift.xsl"/>
  <xsl:import href="teiheader.xsl"/>
  <xsl:import href="teimilestone.xsl"/>
  <xsl:import href="teiorig.xsl"/>
  <xsl:import href="teiorigandreg.xsl"/>
  <xsl:import href="teiq.xsl"/>
  <xsl:import href="teisicandcorr.xsl"/>
  <xsl:import href="teispace.xsl"/>
  <xsl:import href="teisupplied.xsl"/>
  <xsl:import href="teisurplus.xsl"/>
  <xsl:import href="teiunclear.xsl"/>
  
  <!-- html related stylesheets for named templates -->
  <xsl:import href="htm-tpl-cssandscripts.xsl"/>
  <xsl:import href="htm-tpl-apparatus.xsl"/>
  <xsl:import href="htm-tpl-lang.xsl"/>
  <xsl:import href="htm-tpl-metadata.xsl"/>
  <xsl:import href="htm-tpl-license.xsl"/>
  <!-- global named templates with no html, also used by start-txt -->
  <xsl:import href="tpl-reasonlost.xsl"/>
  <xsl:import href="tpl-certlow.xsl"/>
  <xsl:import href="tpl-text.xsl"/>
  <xsl:include href="htm-tpl-sqbrackets.xsl"/>
  <xsl:include href="htm-tpl-structure.xsl"/>
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
  <xsl:variable name="path">/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase">/data/papyri.info/pn/idp.html</xsl:variable>
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
  
  <xsl:template match="/">
    <xsl:variable name="ddbdp" select="$collection = 'ddbdp'"/>
    <xsl:variable name="hgv" select="$collection = 'hgv' or contains($related, 'hgv/')"/>
    <xsl:variable name="apis" select="$collection = 'apis' or contains($related, '/apis/')"/>
    <xsl:variable name="translation" select="contains($related, 'hgvtrans') or (contains($related, 'apis') and pi:get-docs($relations[contains(., 'apis')], 'xml')//t:div[@type = 'translation']) or //t:div[@type = 'translation']"/>
    <xsl:variable name="image" select="count($imgs) gt 0"/>
    <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;
 </xsl:text>
   
    <html lang="en" version="HTML+RDFa 1.1"
      prefix="dc: http://purl.org/dc/terms/">
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta property="dc:identifier" content="{$selfUrl}"/>
        <xsl:call-template name="collection-hierarchy">
          <xsl:with-param name="all-ancestors"><xsl:value-of select="$isPartOf"></xsl:value-of></xsl:with-param>
        </xsl:call-template>
        <xsl:for-each select="tokenize($sources, '\s')">
          <meta property="dc:source" content="{.}"/>
        </xsl:for-each>
        <xsl:for-each select="$relations">
          <meta property="dc:relation" content="{.}"/>
        </xsl:for-each>
        <xsl:for-each select="tokenize($replaces, ' ')">
          <meta property="dc:replaces" content="{.}"/>
        </xsl:for-each>
        <xsl:for-each select="tokenize($isReplacedBy, ' ')">
          <meta property="dc:isReplacedBy" content="{.}"/>
        </xsl:for-each>
       <xsl:if test="string-length($citationForm) > 0">
          <meta property="dc:bibliographicCitation" datatype="xsd:string" content="{replace($citationForm, '&quot;', '')}"/>
       </xsl:if>
        <link rel="stylesheet" href="/css/yui/reset-fonts-grids.css" type="text/css" media="screen" title="no title" charset="utf-8"/>
        <link rel="stylesheet" href="/css/master.css" type="text/css" media="screen" title="no title" charset="utf-8" />
        <link rel="bookmark" href="{$selfUrl}" title="Canonical URI"/>
        <xsl:comment><![CDATA[[if IE]><link rel="stylesheet" href="/css/ie.css" type="text/css" media="screen" charset="utf-8" /><![endif]]]></xsl:comment>
        <xsl:comment><![CDATA[[if IE 7]><link rel="stylesheet" href="/css/ie7.css" type="text/css" media="screen" charset="utf-8" /><![endif]]]></xsl:comment>
        <xsl:comment><![CDATA[[if IE 8]><link rel="stylesheet" href="/css/ie8.css" type="text/css" media="screen" charset="utf-8" /><![endif]]]></xsl:comment>
        <xsl:comment><![CDATA[[if IE 9]><link rel="stylesheet" href="/css/ie9.css" type="text/css" media="screen" charset="utf-8" /><![endif]]]></xsl:comment>        
        <title>
          <xsl:call-template name="get-references"/>
        </title>
        <script src="/js/jquery-1.5.1.min.js" type="text/javascript" charset="utf-8"></script>
        <script src="/js/jquery-ui-1.8.14.custom.min.js" type="text/javascript" charset="utf-8"></script>
        <script src="/js/jquery.bubblepopup.v2.1.5.min.js" type="text/javascript" charset="utf-8"></script>
        <xsl:if test="$image">
          <script src="/js/OpenLayers.js" type="text/javascript" charset="utf-8"></script>
          <script src="/js/imageviewer.js" type="text/javascript" charset="utf-8"></script>
        </xsl:if>            
        <script src="/js/init.js" type="text/javascript" charset="utf-8"></script>
        <script src="/js/titledate.js" type="text/javascript" charset="utf-8"></script>
        <script type="text/javascript">
        
          var _gaq = _gaq || [];
          _gaq.push(['_setAccount', 'UA-19774706-1']);
          _gaq.push(['_trackPageview']);
        
          (function() {
            var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
            ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
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
                <h3 style="text-align:center"><xsl:call-template name="get-references"></xsl:call-template></h3>
                <xsl:if test="$hgv or $apis">
                  <h4 style="text-align:center" id="titledate"></h4>
                </xsl:if>
                <div id="controls" class="ui-widget">
                  <xsl:if test="$hgv or $apis">
                    <div id="metadatacontrols" class="ui-widget-content ui-corner-all">
                      <label for="mdt">metadata</label><input type="checkbox" name="metadata" id="mdt" checked="checked"/><br/>
                      <xsl:if test="$hgv">
                        <label for="hgvm">HGV data</label><input type="checkbox" name="hgv" id="hgvm" checked="checked"/>
                      </xsl:if>
                      <xsl:if test="$apis">
                        <label for="apism">APIS catalog record</label><input type="checkbox" name="apis" id="apism" checked="checked"/>
                      </xsl:if>
                    </div>
                  </xsl:if>
                  <xsl:if test="$ddbdp or $image or $translation">
                    <div id="textcontrols" class="ui-widget-content ui-corner-all">
                      <label for="txt">text</label><input type="checkbox" name="text" id="txt" checked="checked"/><br/>
                      <xsl:if test="$ddbdp">
                        <label for="tcpt">transcription</label><input type="checkbox" name="transcription" id="tcpt" checked="checked"/>
                      </xsl:if>
                      <xsl:if test="$image">
                        <label for="img">images</label><input type="checkbox" name="image" id="img" checked="checked"/>
                      </xsl:if>
                      <xsl:if test="$translation">
                        <label for="tslt">translation</label><input type="checkbox" name="translation" id="tslt" checked="checked"/>
                      </xsl:if>
                    </div>
                  </xsl:if>
                  <xsl:if test="$ddbdp">
                    <div id="editthis" class="ui-widget-content ui-corner-all">
                      <a href="/editor/publications/create_from_identifier/papyri.info/ddbdp/{/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='ddb-hybrid']}" rel="nofollow">open in editor</a>
                    </div>
                  </xsl:if>
                  <xsl:if test="$hgv and not($ddbdp)">
                    <div id="editthis" class="ui-widget-content ui-corner-all">
                      <a href="/editor/publications/create_from_identifier/papyri.info/hgv/{/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']}" rel="nofollow">open in editor</a>
                    </div>
                  </xsl:if>
                  <xsl:if test="$apis and not($ddbdp or $hgv)">
                    <div id="editthis" class="ui-widget-content ui-corner-all">
                      <a href="/editor/publications/create_from_identifier/papyri.info/apis/{/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='apisid']}" rel="nofollow">open in editor</a>
                    </div>
                  </xsl:if>
                  <div id="canonical-uri" class="ui-widget-content ui-corner-all">
                    <span id="canonical-uri-label">Canonical URI: </span>
                    <span id="canonical-uri-value"><a href="{$selfUrl}"><xsl:value-of select="$selfUrl"/></a></span>
                  </div>
                </div>
                <xsl:if test="$collection = 'ddbdp'">
                  <xsl:if test="$hgv or $apis">
                    <div class="metadata">
                      <xsl:for-each select="$relations[contains(., 'hgv/')]">
                        <xsl:sort select="." order="ascending"/>
                        <xsl:choose>
                          <xsl:when test="doc-available(pi:get-filename(., 'xml'))">
                            <xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/t:TEI" mode="metadata"/>
                          </xsl:when>
                          <xsl:otherwise><xsl:message>Error: <xsl:value-of select="pi:get-filename(., 'xml')"/> not available. Error in <xsl:value-of select="$doc-id"/>.</xsl:message></xsl:otherwise>
                        </xsl:choose>
                      </xsl:for-each>
                      <xsl:for-each select="$relations[contains(., '/apis/')]">
                        <xsl:sort select="." order="ascending"/>
                        <xsl:choose>
                          <xsl:when test="doc-available(pi:get-filename(., 'xml'))">
                            <xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/t:TEI" mode="metadata"/>
                          </xsl:when>
                          <xsl:otherwise><xsl:message>Error: <xsl:value-of select="pi:get-filename(., 'xml')"/> not available. Error in <xsl:value-of select="$doc-id"/>.</xsl:message></xsl:otherwise>
                        </xsl:choose>
                      </xsl:for-each>
                      <xsl:call-template name="biblio"/>
                    </div>
                  </xsl:if>
                  <div class="text">
                    <xsl:apply-templates select="/t:TEI" mode="text"/>
                    <xsl:for-each select="pi:get-docs($relations[contains(., '/ddbdp/') and not(. = $replaces)], 'xml')/t:TEI"> 
                      <xsl:apply-templates select="." mode="text"/>
                    </xsl:for-each>
                    <xsl:if test="$image">
                      <xsl:call-template name="images"/>
                    </xsl:if>
                    <xsl:if test="$translation">
                      <xsl:for-each select="pi:get-docs($relations[contains(., 'hgvtrans')], 'xml')/t:TEI//t:div[@type = 'translation']">
                        <xsl:sort select="number(ancestor::t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename'])"/>
                        <div class="translation data">
                          <h2>HGV <xsl:value-of select="ancestor::t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'filename']"/> Translation (<xsl:value-of select="ancestor::t:TEI/t:teiHeader//t:langUsage/t:language[@ident = current()/@xml:lang]"/>) 
                            [<a href="/hgvtrans/{ancestor::t:TEI/t:teiHeader//t:idno[@type = 'filename']}/source">xml</a>]</h2>
                          <xsl:apply-templates />
                        </div>
                      </xsl:for-each>
                      <xsl:for-each select="$relations[contains(., '/apis/')]">
                        <xsl:choose>
                          <xsl:when test="doc-available(pi:get-filename(., 'xml'))"><xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/t:TEI" mode="apistrans"/></xsl:when>
                          <xsl:otherwise><xsl:message>Error: <xsl:value-of select="pi:get-filename(., 'xml')"/> not available. Error in <xsl:value-of select="$doc-id"/>.</xsl:message></xsl:otherwise>
                        </xsl:choose>
                      </xsl:for-each>
                    </xsl:if>
                  </div>
                </xsl:if>
                <xsl:if test="$collection = 'hgv'">
                  <div class="metadata">
                    <xsl:apply-templates select="/t:TEI" mode="metadata"/>
                    <xsl:if test="$apis">
                      <xsl:for-each select="$relations[contains(., '/apis/')]">
                        <xsl:choose>
                          <xsl:when test="doc-available(pi:get-filename(., 'xml'))">
                            <xsl:apply-templates select="doc(pi:get-filename(., 'xml'))/t:TEI" mode="metadata"/>
                          </xsl:when>
                          <xsl:otherwise><xsl:message>Error: <xsl:value-of select="pi:get-filename(., 'xml')"/> not available. Error in <xsl:value-of select="$doc-id"/>.</xsl:message></xsl:otherwise>
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
                          <xsl:otherwise><xsl:message>Error: <xsl:value-of select="pi:get-filename(., 'xml')"/> not available. Error in <xsl:value-of select="$doc-id"/>.</xsl:message></xsl:otherwise>
                        </xsl:choose>
                      </xsl:for-each>
                    </div>
                  </xsl:if>
                </xsl:if>
                <xsl:if test="$collection = 'apis'">
                  <div class="metadata">
                    <xsl:apply-templates select="/t:TEI" mode="metadata"/>
                    <xsl:call-template name="biblio"/>
                  </div>
                  <div class="text">
                    <xsl:if test="$image">
                      <xsl:call-template name="images"/>
                    </xsl:if>
                    <xsl:if test="$translation">
                      <xsl:apply-templates select="/t:TEI" mode="apistrans"/>
                    </xsl:if>
                  </div>
                </xsl:if>
              </div>
            </div>
          </div>
          <xi:include href="footer.xml"/>
        </div>
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
    <div class="transcription data">
      <h2>DDbDP transcription: <xsl:value-of select="t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']"/> [<a href="/ddbdp/{t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='ddb-hybrid']}/source">xml</a>]</h2>
      <xsl:apply-templates select="."/>
      <div id="history">
        <div id="history-headers">
          <h3><span id="edit-history">Editorial History</span>; <span id="all-history">All History</span>; (<a href="{pi:get-blame-url(t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='ddb-hybrid'])}" target="_blank">detailed</a>)</h3>
        </div>
        <!-- closing #history-headers -->
        <div id="history-lists">
          <ul id="edit-history-list" style="display:none;">
            <xsl:choose>
              <!-- this test will need to be changed if a @type attribute is added to <change>, as discussed at http://idp.atlantides.org/trac/idp/ticket/967 -->
              <xsl:when test="count(t:teiHeader/t:revisionDesc/t:change[contains(@when, 'T')]) &gt; 0">
                <xsl:for-each select="t:teiHeader/t:revisionDesc/t:change[contains(@when, 'T')]">
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
              <xsl:when test="count(t:teiHeader/t:revisionDesc/t:change[matches(@when, '^\d{4}-\d{2}-\d{2}$')])">
                <xsl:for-each select="t:teiHeader/t:revisionDesc/t:change[matches(@when, '^\d{4}-\d{2}-\d{2}$')]">
                  <li><xsl:value-of select="@when"/> [<a href="{@who}"><xsl:choose><xsl:when test="ends-with(@who,'about')">papyri.info</xsl:when><xsl:otherwise><xsl:value-of select="replace(@who,'.*/([^/]+)$','$1')"/></xsl:otherwise></xsl:choose></a>]: <xsl:value-of select="."/></li>
                </xsl:for-each>                                                    
              </xsl:when>
              <xsl:otherwise>
                <li>No further history recorded.</li>
              </xsl:otherwise>
            </xsl:choose>
          </ul>
        </div>
        <!-- closing #history-lists -->
      </div>
      <!-- closing #history -->
      <p><a rel="license" href="http://creativecommons.org/licenses/by/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/80x15.png" /></a> © Duke Databank of Documentary Papyri.  
        This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 License</a>.</p>
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
  
  <xsl:template match="rdf:Description">
    <xsl:value-of select="@rdf:about"/>
  </xsl:template>
</xsl:stylesheet>

