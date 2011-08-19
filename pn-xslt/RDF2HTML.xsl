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
  <xsl:import href="htm-tpl-nav.xsl"/>
  <xsl:import href="htm-tpl-license.xsl"/>
  
  <!-- global named templates with no html, also used by start-txt -->
  <xsl:import href="tpl-reasonlost.xsl"/>
  <xsl:import href="tpl-certlow.xsl"/>
  <xsl:import href="tpl-text.xsl"/>
  
  <xsl:param name="collection"/>
  <xsl:param name="related"/>
  <xsl:param name="replaces"/>
  <xsl:param name="isReplacedBy"/>
  <xsl:param name="server">papyri.info</xsl:param>
  <xsl:variable name="relations" select="tokenize($related, '\s+')"/>
  <xsl:variable name="path">/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase">/data/papyri.info/pn/idp.html</xsl:variable>
  <xsl:variable name="doc-id">
    <xsl:choose>
      <xsl:when test="//t:idno[@type='apisid']"><xsl:value-of select="//t:idno[@type='apisid']"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="//t:idno[@type='filename']"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="line-inc">5</xsl:variable>
  
  <xsl:include href="pi-functions.xsl"/>
  
  <xsl:output method="html"/>
  
  <xsl:template match="/">
    <xsl:variable name="ddbdp" select="$collection = 'ddbdp'"/>
    <xsl:variable name="hgv" select="$collection = 'hgv' or contains($related, 'hgv/')"/>
    <xsl:variable name="apis" select="$collection = 'apis' or contains($related, '/apis/')"/>
    <xsl:variable name="translation" select="contains($related, 'hgvtrans') or (contains($related, 'apis') and pi:get-docs($relations[contains(., 'apis')], 'xml')//t:div[@type = 'translation']) or //t:div[@type = 'translation']"/>
    <xsl:variable name="image" select="contains($related, 'http://papyri.info/images')"/>
    <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
    <html lang="en">
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <link rel="stylesheet" href="/css/master.css" type="text/css" media="screen" title="no title" charset="utf-8" />
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
            <h2 class="mode">Navigator | <a href="/editor">Editor</a></h2>
          </div>
          <div id="bd">
            <xi:include href="nav.xml"/>
            <div id="main">
              <div class="content ui-corner-all">
                <h3 style="text-align:center"><xsl:call-template name="get-references"><xsl:with-param name="links">yes</xsl:with-param></xsl:call-template></h3>
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
                    </div>
                  </xsl:if>
                  <div class="text">
                    <div class="transcription data">
                      <h2>DDbDP transcription: <xsl:value-of select="//t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']"/> [<a href="/ddbdp/{//t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='ddb-hybrid']}/source">xml</a>]</h2>
                      <xsl:apply-templates select="/t:TEI"/>
                      <div id="history">
                        <h3>History</h3>
                        <ul style="display:none">
                          <xsl:for-each select="/t:TEI/t:teiHeader/t:revisionDesc/t:change">
                            <li><xsl:value-of select="@when"/> [<xsl:value-of select="@who"/>]: <xsl:value-of select="."/></li>
                          </xsl:for-each>
                        </ul>
                      </div>
                      <p><a rel="license" href="http://creativecommons.org/licenses/by/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/80x15.png" /></a> © Duke Databank of Documentary Papyri.  
                        This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 License</a>.</p>
                    </div>
                    <xsl:if test="$image">
                      <div id="image" class="image data"> 
                        <h2>Image<xsl:if test="count($relations[contains(., 'images/')]) &gt; 1">s</xsl:if></h2>
                        <ul>
                          <xsl:for-each select="$relations[contains(., 'images/')]">
                            <xsl:sort order="descending"/>
                            <li><img src="{.}" alt="papyrus image"/></li>
                          </xsl:for-each>
                        </ul>
                        <p class="rights"><b>Notice</b>: Each library participating in APIS has its own policy 
                          concerning the use and reproduction of digital images included in APIS.  Please contact 
                          the <a href="http://www.columbia.edu/cu/lweb/projects/digital/apis/permissions.html">owning institution</a> 
                          if you wish to use any image in APIS.</p>
                      </div>
                    </xsl:if>
                    <xsl:if test="$translation">
                      <xsl:for-each select="pi:get-docs($relations[contains(., 'hgvtrans')], 'xml')/t:TEI//t:div[@type = 'translation']">
                        <xsl:sort select="number(ancestor::t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename'])"/>
                        <div class="translation data">
                          <h2>HGV <xsl:value-of select="ancestor::t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type = 'filename']"/> Translation (<xsl:value-of select="ancestor::t:TEI/t:teiHeader//t:langUsage/t:language[@ident = current()/@xml:lang]"/>) 
                            [<a href="/hgvtrans/{ancestor::t:TEI/t:teiHeader//t:idno[@type = 'filename']}/source">xml</a>]</h2>
                          <xsl:apply-templates select="t:p"/>
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
                  </div>
                  <div class="text">
                    <xsl:if test="$image">
                    <div id="image" class="image data"> 
                      <h2>Image<xsl:if test="count($relations[contains(., 'images/')]) &gt; 1">s</xsl:if></h2>
                      <ul>
                        <xsl:for-each select="$relations[contains(., 'images/')]">
                          <xsl:sort order="descending"/>
                          <li><img src="{.}" alt="papyrus image"/></li>
                        </xsl:for-each>
                      </ul>
                      <p class="rights"><b>Notice</b>: Each library participating in APIS has its own policy 
                        concerning the use and reproduction of digital images included in APIS.  Please contact 
                        the <a href="http://www.columbia.edu/cu/lweb/projects/digital/apis/permissions.html">owning institution</a> 
                        if you wish to use any image in APIS or to publish any material from APIS.</p>
                    </div>
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
        <script type="text/javascript" charset="utf-8">
          $("#controls input").click(
            function() {
              if (this.checked) {
                $("."+this.name).show();
                if (this.name == "transcription") {
                  $(".image").css('width','50%');
                  $(".translation").css('width','50%');
                }
              } else {
                $("."+this.name).hide();
                if (this.name == "transcription") {
                  $(".image").css('width','100%');
                  $(".translation").css('width','100%');
                }
              }
            }
          );
          $("#titledate").append(function() {
            var result = "";
            result += $(".mdtitle:first").text();
            if (result != "") {
              result += " - ";
            }
            if ($("div.hgv .mddate").length > 0) {
              result += $("div.hgv .mddate").map(function (i) {
                return $(this).text();
              }).get().join("; ");
            } else {
              result += $(".mddate:first").text();
            }
            if ($(".mdprov").length > 0) {
              result += " - ";
              result += $(".mdprov:first").text();
            }
            return result;
          });
          $("#history").click( function() {
            $("#history>ul").toggle("blind");
          });
        </script>
      </body>
    </html>
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
    
  <xsl:template match="t:TEI" mode="metadata">
    <xsl:variable name="md-collection"><xsl:choose>
      <xsl:when test="//t:idno[@type='apisid']">apis</xsl:when>
      <xsl:otherwise>hgv</xsl:otherwise>
    </xsl:choose>
    </xsl:variable>
    <div class="metadata">
      <div class="{$md-collection} data">
        <xsl:choose>
          <xsl:when test="$md-collection = 'hgv'">
            <h2>
              HGV Data for <xsl:value-of select="//t:bibl[@type = 'publication' and @subtype='principal']"/> [<a href="http://aquila.papy.uni-heidelberg.de/Hauptregister/FMPro?-db=hauptregister_&amp;TM_Nr.={//t:idno[@type = 'filename']}&amp;-format=DTableVw.htm&amp;-lay=Liste&amp;-find">source</a>] [<a class="xml" href="/hgv/{//t:idno[@type='filename']}/source" target="_new">xml</a>]
            </h2>
          </xsl:when>
          <xsl:otherwise>
            <h2>
              APIS Catalog Record for <xsl:value-of select="//t:idno[@type='apisid']"/> [<a href="/apis/{//t:idno[@type='apisid']}/source">xml</a>] 
            </h2>
          </xsl:otherwise>
        </xsl:choose>
        <table class="metadata">
          <tbody>
            <!-- Title -->
            <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:titleStmt/t:title" mode="metadata"/>
            <!-- Summary -->
            <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:summary" mode="metadata"/>
            <!-- Publications -->
            <xsl:apply-templates select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition']" mode="metadata"/>
            <!-- Inv. Id -->
            <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno" mode="metadata"/>
            <!-- Physical Desc. -->
            <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:p" mode="metadata"/>
            <!-- Post-concordance BL Entries -->
            <xsl:apply-templates select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'corrections']" mode="metadata"/>
            <!-- Translations -->
            <xsl:apply-templates select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'translations']" mode="metadata"/>
            <!-- Provenance -->
            <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/(t:origPlace|t:p)" mode="metadata"/>
            <!-- Material -->
            <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:support/t:material" mode="metadata"/>
            <!-- Language -->
            <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItem/t:textLang" mode="metadata"/>
            <!-- Date -->
            <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate" mode="metadata"/>
            <!-- Commentary -->
            <xsl:apply-templates select="t:text/t:body/t:div[@type = 'commentary']" mode="metadata"/>
            <!-- Notes (general|lines|palaeography|recto/verso|conservation|preservation) -->
            <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItem/t:note" mode="metadata"/>
            <!-- Print Illustrations -->
            <xsl:apply-templates select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'illustrations'][.//t:bibl]" mode="metadata"/>
            <!-- Subjects -->
            <xsl:apply-templates select="t:teiHeader/t:profileDesc/t:textClass/t:keywords" mode="metadata"/>
            <!-- Associated Names -->
            <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin[t:persName/@type = 'asn']" mode="metadata"/>
            <!-- Images -->
            <xsl:apply-templates select="t:text/t:body/t:div[@type = 'figure']" mode="metadata"/>
            <xsl:choose>
              <xsl:when test="$md-collection = 'hgv'">
                <tr>
                  <th class="rowheader">License</th>
                  <td><a rel="license" href="http://creativecommons.org/licenses/by/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/80x15.png" /></a>
                    © Heidelberger Gesamtverzeichnis der griechischen Papyrusurkunden Ägyptens.  This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 License</a>.</td>
                </tr>
              </xsl:when>
              <xsl:otherwise>
                <tr>
                  <th class="rowheader">License</th>
                  <td><a rel="license" href="http://creativecommons.org/licenses/by-nc/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nc/3.0/80x15.png" /></a> This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc/3.0/">Creative Commons Attribution-NonCommercial 3.0 License</a>.</td>
                </tr>                  
              </xsl:otherwise>
            </xsl:choose>
            
          </tbody>
        </table>
      </div>
      
    </div>
  </xsl:template>
  
  <xsl:template match="t:TEI" mode="apistrans">
    <div class="translation data">
      <h2>APIS Translation (English)</h2>
      <p><xsl:value-of select=".//t:div[@type = 'translation']/t:ab"/></p>
    </div>
  </xsl:template>
  
  <!-- Title -->
  <xsl:template match="t:title" mode="metadata">
    <tr>
      <th class="rowheader" rowspan="1">Title</th>
      <td><xsl:if test="not(starts-with(., 'kein'))"><xsl:attribute name="class">mdtitle</xsl:attribute></xsl:if><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Summary -->
  <xsl:template match="t:summary" mode="metadata">
    <tr>
      <th class="rowheader">Summary</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Publications -->
  <xsl:template match="t:div[@type = 'bibliography' and @subtype='principalEdition']" mode="metadata">
    <xsl:variable name="pubcount" select="count(../t:div[@type = 'bibliography' and @subtype = 'otherPublications']//t:bibl) + 1"/>
    <tr>
      <th class="rowheader" rowspan="{$pubcount}">Publications</th>
      <td><xsl:value-of select=".//t:bibl"/></td>
    </tr>
    <xsl:for-each select="../t:div[@type = 'bibliography' and @subtype = 'otherPublications']//t:bibl">
      <tr>
        <td><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>
  
  <!-- Commentary -->
  <xsl:template match="t:div[@type = 'commentary']" mode="metadata">
    <tr>
      <th>Commentary</th>
      <td><xsl:value-of select="t:p"/></td>
    </tr>
  </xsl:template>
  
  <!-- Print Illustrations -->
  <xsl:template match="t:div[@type = 'bibliography' and @subtype='illustrations']" mode="metadata">
    <tr>
      <th class="rowheader">Print Illustrations</th>
      <td><xsl:for-each select=".//t:bibl"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each></td>
    </tr>
  </xsl:template>
  
  <!-- Inv. Id -->
  <xsl:template match="t:msIdentifier/t:idno" mode="metadata">
    <tr>
      <th class="rowheader">Inv. Id</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Physical Desc. -->
  <xsl:template match="t:physDesc/t:p" mode="metadata">
    <tr>
      <th class="rowheader">Physical Desc.</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Post-Concordance BL Entries -->
  <xsl:template match="t:div[@type = 'bibliography' and @subtype='corrections']" mode="metadata">
    <tr>
      <th class="rowheader" rowspan="1">Post-Concordance BL Entries</th>
      <td>
        <xsl:for-each select=".//t:bibl"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each>
      </td>
    </tr>
  </xsl:template>
  
  <!-- Translations -->
  <xsl:template match="t:div[@type = 'bibliography' and @subtype='translations']" mode="metadata">
    <tr>
      <th class="rowheader" rowspan="1">Translations</th>
      <td>
        <xsl:for-each select=".//t:bibl"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each>
      </td>
    </tr>
  </xsl:template>
  
  <!-- Provenance -->
  <xsl:template match="t:origPlace|t:p" mode="metadata">
    <tr>
      <th class="rowheader" rowspan="1">Provenance</th>
      <td class="mdprov">
        <xsl:choose>
          <xsl:when test="local-name(.) = 'origPlace'"><xsl:value-of select="."/></xsl:when>
          <xsl:otherwise><xsl:if test="t:placeName[@type='ancientFindspot']"><xsl:value-of select="t:placeName[@type='ancientFindspot']"/><xsl:if test="t:geogName">, </xsl:if></xsl:if>
            <xsl:if test="t:geogName[@type='nome']"><xsl:value-of select="t:geogName[@type='nome']"/><xsl:if test="t:geogName[@type='ancientRegion']">, </xsl:if></xsl:if>
            <xsl:value-of select="t:geogName[@type='ancientRegion']"/></xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>
  
  <!-- Material -->
  <xsl:template match="t:material" mode="metadata">
    <tr>
      <th class="rowheader">Material</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Language -->
  <xsl:template match="t:textLang" mode="metadata">
    <tr>
      <th class="rowheader">Language</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Date -->
  <xsl:template match="t:origDate" mode="metadata">
    <tr>
      <th class="rowheader">Date</th>
      <td class="mddate"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Notes -->
  <xsl:template match="t:msItem/t:note" mode="metadata">
    <tr>
      <th class="rowheader">Note (<xsl:value-of select="replace(./@type, '_', '/')"/>)</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Subjects -->
  <xsl:template match="t:keywords" mode="metadata">
    <tr>
      <th class="rowheader">Subjects</th>
      <td><xsl:for-each select="t:term"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each></td>
    </tr>
  </xsl:template>
  
  <!-- Associated Names -->
  <xsl:template match="t:origin" mode="metadata">
    <tr>
      <th class="rowheader">Associated Names</th>
      <td><xsl:for-each select="t:persName[@type = 'asn']"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each></td>
    </tr>
  </xsl:template>
  
  <!-- Images -->
  <xsl:template match="t:div[@type = 'figure']" mode="metadata">
    <xsl:for-each select=".//t:figure">
      <tr>
        <th class="rowheader">Images</th>
      <td><a href="{t:graphic/@url}"><xsl:choose>
        <xsl:when test="t:figDesc"><xsl:value-of select="t:figDesc"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="substring(t:graphic/@url, 1, 60)"/>...</xsl:otherwise>
      </xsl:choose>
      </a></td>
    </tr>
    </xsl:for-each>
    
  </xsl:template>
  
  <!-- Commentary links -->
  <xsl:template match="t:div[@type='commentary']/t:list/t:item/t:ref">
    <a href="{parent::t:item/@corresp}"><xsl:apply-templates/></a>.
  </xsl:template>
  
  <!-- Generate parallel reference string -->
  <xsl:template name="get-references">
    <xsl:param name="links"/>
    <xsl:if test="$collection = 'hgv'">HGV </xsl:if><xsl:value-of select="/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']"></xsl:value-of>
    <xsl:if test="count($relations[contains(., 'hgv/')]) gt 0"> = HGV </xsl:if>
    <xsl:for-each select="$relations[contains(., 'hgv/')]">
      <xsl:if test="doc-available(pi:get-filename(., 'xml'))">
        <xsl:for-each select="normalize-space(doc(pi:get-filename(., 'xml'))//t:bibl[@type = 'publication' and @subtype='principal'])"> 
          <xsl:text> </xsl:text><xsl:value-of select="."/></xsl:for-each><xsl:if test="contains($relations[position() + 1], 'hgv/')">; </xsl:if>
        <xsl:if test="position() != last()"> = </xsl:if>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each-group select="$relations[contains(., 'hgv/')]" group-by="replace(., '[a-z]', '')"><xsl:if test="contains(., 'hgv')">
      = <xsl:choose>
        <xsl:when test="$links = 'yes'"><a href="http://www.trismegistos.org/tm/detail.php?quick={replace(pi:get-id(.), '[a-z]', '')}">Trismegistos <xsl:value-of select="replace(pi:get-id(.), '[a-z]', '')"/></a></xsl:when>
        <xsl:otherwise>Trismegistos <xsl:value-of select="replace(pi:get-id(.), '[a-z]', '')"/></xsl:otherwise>
      </xsl:choose>
    </xsl:if></xsl:for-each-group>
    <xsl:for-each select="$relations[contains(., 'apis/')]"> = <xsl:value-of select="pi:get-id(.)"/></xsl:for-each>
    <xsl:for-each select="tokenize($isReplacedBy, '\s')"> = <xsl:value-of select="pi:get-id(.)"/></xsl:for-each>
    <xsl:for-each select="tokenize($replaces, '\s')"> = <xsl:value-of select="pi:get-id(.)"/></xsl:for-each>
  </xsl:template>
  
  <xsl:template match="rdf:Description">
    <xsl:value-of select="@rdf:about"/>
  </xsl:template>
</xsl:stylesheet>
