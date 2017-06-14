<!-- script for updating dclp dropdown overview (author, tm, series). needs saxon in classpath -->
<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet exclude-result-prefixes="#all" version="2.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:papy="Papyrillio"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:date="http://exslt.org/dates-and-times"
   xmlns:fm="http://www.filemaker.com/fmpxmlresult"
   xmlns:tei="http://www.tei-c.org/ns/1.0"
   xmlns:fn="http://www.xsltfunctions.com/"
   xmlns:functx="http://www.functx.com"
   xmlns="http://www.tei-c.org/ns/1.0">

<!--   <xsl:include href="xwalkHelper.xsl"/> -->
   <xsl:output method="html" media-type="text/xml" />
   <xsl:param name="collection" select="'/srv/data/papyri.info/idp.data/DCLP/?select=[0-9]+.xml;recurse=yes'"/>
   <xsl:param name="pathTarget"/>
   
   <xsl:template name="HTML">
      <html lang="en">
         <head>
            <meta charset="utf-8"/>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
            <title>DCLP</title>
            <link href="js/jquery/jquery-ui.css" rel="stylesheet"/>
            <style>
               body{
               font-family: "Trebuchet MS", sans-serif;
               margin: 50px;
               }
               .demoHeaders {
               margin-top: 2em;
               }
               #dialog-link {
               padding: .4em 1em .4em 20px;
               text-decoration: none;
               position: relative;
               }
               #dialog-link span.ui-icon {
               margin: 0 5px 0 0;
               position: absolute;
               left: .2em;
               top: 50%;
               margin-top: -8px;
               }
               #icons {
               margin: 0;
               padding: 0;
               }
               #icons li {
               margin: 2px;
               position: relative;
               padding: 4px 0;
               cursor: pointer;
               float: left;
               list-style: none;
               }
               #icons span.ui-icon {
               float: left;
               margin: 0 4px;
               }
               .fakewindowcontain .ui-widget-overlay {
               position: absolute;
               }
               select {
               width: 200px;
               }
            </style>
         </head>
         <body>
            <h1>DCLP</h1>
            <div id="browser">
               <ul>
                  <li><a href="#tm">TM</a></li>
                  <li><a href="#series">Series</a></li>
                  <li><a href="#author">Author</a></li>
               </ul>

               <xsl:result-document href="{concat($pathTarget, '/dclp_tm.html')}" method="html" html-version="5.0">
                  <xsl:call-template name="htmlFrame">
                     <xsl:with-param name="content">
                        <div id="tm" class="collection">
                           <xsl:for-each-group select="collection($collection)" group-by="ceiling(number(//tei:idno[@type='TM']) div 1000)">
                              <xsl:message select="current-grouping-key()"/>
                              <h3><xsl:value-of select="current-grouping-key()"/></h3>
                              <ul>
                                 <xsl:for-each select="current-group()">
                                    <xsl:variable name="tm" select="string(//tei:idno[@type='TM'])"></xsl:variable>
                                    <li>
                                       <a href="/dclp/{$tm}"><xsl:value-of select="$tm"/></a>
                                       <xsl:if test="//tei:div[@type='edition'] and string(normalize-space(string-join(//tei:div[@type='edition'], '')))">
                                          <span>
                                             <xsl:text> (</xsl:text>
                                             <xsl:if test="//tei:div[@type='edition']/@xml:lang">
                                                <xsl:value-of select="string-join(//tei:language[@ident=//tei:div[@type='edition'][not(string(normalize-space(.))='')]/@xml:lang], ', ')"/>
                                                <xsl:text> </xsl:text>
                                             </xsl:if>
                                             <xsl:text>text available)</xsl:text>
                                          </span>
                                       </xsl:if>
                                       <xsl:call-template name="viewIn">
                                          <xsl:with-param name="tm" select="$tm"/>
                                       </xsl:call-template>
                                    </li>
                                 </xsl:for-each>
                              </ul>
                           </xsl:for-each-group>
                        </div>
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:result-document>

               <xsl:result-document href="{concat($pathTarget, '/dclp_series.html')}" method="html" html-version="5.0">
                  <xsl:call-template name="htmlFrame">
                     <xsl:with-param name="content">
                        <div id="series" class="collection">
                           <xsl:for-each-group select="collection($collection)" group-by="substring-before(string(tei:TEI/tei:teiHeader[1]/tei:fileDesc[1]/tei:publicationStmt[1]/tei:idno[@type='dclp-hybrid'][1]), ';')">
                              <xsl:sort select="if(string(tei:TEI/tei:teiHeader[1]/tei:fileDesc[1]/tei:publicationStmt[1]/tei:idno[@type='dclp-hybrid'][1]))then(substring-before(string(tei:TEI/tei:teiHeader[1]/tei:fileDesc[1]/tei:publicationStmt[1]/tei:idno[@type='dclp-hybrid'][1]), ';')) else ('z')"/>
                              <xsl:variable name="series" select="if(string(current-grouping-key())) then current-grouping-key() else 'TM'"></xsl:variable>
                              <xsl:message select="$series"/>
                              <h3><xsl:value-of select="$series"/></h3>
                              <ul>
                                 <xsl:for-each select="current-group()">
                                    <xsl:variable name="tm" select="string(tei:TEI/tei:teiHeader[1]/tei:fileDesc[1]/tei:publicationStmt[1]/tei:idno[@type='TM'])"></xsl:variable>
                                    <xsl:variable name="dclpHybrid" select="string(tei:TEI/tei:teiHeader[1]/tei:fileDesc[1]/tei:publicationStmt[1]/tei:idno[@type='dclp-hybrid'][1])" />
                                    <li>
                                       <a href="/dclp/{$tm}"><xsl:value-of select="if(string($dclpHybrid)) then ($dclpHybrid) else ($tm)"/></a>
                                       <xsl:if test="//tei:div[@type='edition'] and string(normalize-space(string-join(//tei:div[@type='edition'], '')))">
                                          <span>
                                             <xsl:text> (</xsl:text>
                                             <xsl:if test="//tei:div[@type='edition']/@xml:lang">
                                                <xsl:value-of select="string-join(//tei:language[@ident=//tei:div[@type='edition'][string(.)]/@xml:lang], ', ')"/>
                                                <xsl:text> </xsl:text>
                                             </xsl:if>
                                             <xsl:text>text available)</xsl:text>
                                          </span>
                                       </xsl:if>
                                       <xsl:call-template name="viewIn">
                                          <xsl:with-param name="tm" select="$tm"/>
                                       </xsl:call-template>
                                    </li>
                                 </xsl:for-each>
                              </ul>
                           </xsl:for-each-group>
                        </div>
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:result-document>

               <xsl:result-document href="{concat($pathTarget, '/dclp_authors.html')}" method="html" html-version="5.0">
                  <xsl:call-template name="htmlFrame">
                     <xsl:with-param name="content">
                        <div id="author" class="collection">
                           <xsl:for-each-group select="collection($collection)" group-by="normalize-space(/tei:TEI/tei:text[1]/tei:body[1]/tei:div[@type='bibliography'][@subtype='ancientEdition'][1]/tei:listBibl[1]/tei:bibl[@type='publication'][@subtype='ancient'][1]/tei:author[1])">
                              <xsl:sort select="if(string(normalize-space(/tei:TEI/tei:text[1]/tei:body[1]/tei:div[@type='bibliography'][@subtype='ancientEdition'][1]/tei:listBibl[1]/tei:bibl[@type='publication'][@subtype='ancient'][1]/tei:author[1])))then(normalize-space(/tei:TEI/tei:text[1]/tei:body[1]/tei:div[@type='bibliography'][@subtype='ancientEdition'][1]/tei:listBibl[1]/tei:bibl[@type='publication'][@subtype='ancient'][1]/tei:author[1]))else('z')"/>
                              <xsl:variable name="author" select="if(string(current-grouping-key())) then current-grouping-key() else 'No Author'"></xsl:variable>
                              <xsl:message select="$author"/>
                              <h3><xsl:value-of select="$author"/></h3>
                              <ul>
                                 <xsl:for-each-group select="current-group()" group-by="normalize-space(/tei:TEI/tei:text[1]/tei:body[1]/tei:div[@type='bibliography'][@subtype='ancientEdition'][1]/tei:listBibl[1]/tei:bibl[@type='publication'][@subtype='ancient'][1]/tei:title[1])">
                                    <xsl:sort select="normalize-space(/tei:TEI/tei:text[1]/tei:body[1]/tei:div[@type='bibliography'][@subtype='ancientEdition'][1]/tei:listBibl[1]/tei:bibl[@type='publication'][@subtype='ancient'][1]/tei:title[1])"/>
                                    <xsl:variable name="work" select="if(string(current-grouping-key()))then(current-grouping-key())else('No Work')"/>
                                    <xsl:message select="$work"/>
                                    <li>
                                       <xsl:value-of select="$work"/>
                                       <ul>
                                          <xsl:for-each select="current-group()">
                                             <xsl:variable name="tm" select="string(tei:TEI/tei:teiHeader[1]/tei:fileDesc[1]/tei:publicationStmt[1]/tei:idno[@type='TM'])"></xsl:variable>
                                             <xsl:variable name="dclpHybrid" select="string(tei:TEI/tei:teiHeader[1]/tei:fileDesc[1]/tei:publicationStmt[1]/tei:idno[@type='dclp-hybrid'][1])"></xsl:variable>
                                             <li>
                                                <a href="/dclp/{$tm}">
                                                   <xsl:value-of select="$author"/>
                                                   <xsl:text>, </xsl:text>
                                                   <xsl:value-of select="$work"/>
                                                   <xsl:text> </xsl:text>
                                                   <xsl:value-of select="$tm"/>
                                                   <xsl:value-of select="if(string($dclpHybrid))then(concat(' (', $dclpHybrid, ')'))else()"/>
                                                </a>
                                                <xsl:if test="//tei:div[@type='edition'] and string(normalize-space(string-join(//tei:div[@type='edition'], '')))">
                                                   <span>
                                                      <xsl:text> (</xsl:text>
                                                      <xsl:if test="//tei:div[@type='edition']/@xml:lang">
                                                         <xsl:value-of select="string-join(//tei:language[@ident=//tei:div[@type='edition'][string(.)]/@xml:lang], ', ')"/>
                                                         <xsl:text> </xsl:text>
                                                      </xsl:if>
                                                      <xsl:text>text available)</xsl:text>
                                                   </span>
                                                </xsl:if>
                                                <xsl:call-template name="viewIn">
                                                   <xsl:with-param name="tm" select="$tm"/>
                                                </xsl:call-template>
                                             </li>
                                          </xsl:for-each>
                                       </ul>
                                    </li>
                                 </xsl:for-each-group>
                              </ul>
                           </xsl:for-each-group>
                        </div>
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:result-document>
               
            </div>
            
            <script src="js/jquery/external/jquery/jquery.js"></script>
            <script src="js/jquery/jquery-ui.js"></script>
            <script>
               $(function(){
               $('.collection').accordion({
               heightStyle: "content",
               collapsible: true,
               animate: false
               });
               
               $('#browser').tabs();

               });
               
            </script>
            
         </body>
      </html>
   </xsl:template>
   
   <xsl:template name="htmlFrame">
      <xsl:param name="content"/>
      <html xmlns="http://www.tei-c.org/ns/1.0" lang="en">
         <head>
            <meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
            <title>DCLP</title>
            <link rel="stylesheet" href="css/yui/reset-fonts-grids.css" type="text/css" media="screen" title="no title" charset="utf-8"></link>
            <link rel="stylesheet" href="css/master.css" type="text/css" media="screen" title="no title" charset="utf-8"></link>
            <link rel="stylesheet" href="css/custom-theme/jquery-ui-1.8.14.custom.css" type="text/css" media="screen" title="no title" charset="utf-8"></link><script src="js/jquery-1.5.1.min.js" type="text/javascript" charset="utf-8"></script><script src="js/jquery-ui-1.8.14.custom.min.js" type="text/javascript" charset="utf-8"></script><script src="js/init.js" type="text/javascript" charset="utf-8"></script><script type="text/javascript">
               
               var _gaq = _gaq || [];
               _gaq.push(['_setAccount', 'UA-19774706-1']);
               _gaq.push(['_trackPageview']);
               
               (function() {
               var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
               ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
               var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
               })();
               
            </script></head>
         <body onload="init()">
            <div id="d">
               <div id="hd">
                  <h1>DCLP</h1>
                  <h2 id="login"><a href="/editor/user/signin">sign in</a></h2>
               </div>
               <div id="bd">
                  <div class="nav" style="z-index: 1000;">
                     <form method="get" accept-charset="utf-8">
                        <ul class="nav">
                           <li>Browse: </li>
                           <li><a href="/browse/ddbdp/">DDbDP</a></li>
                           <li><a href="/browse/hgv/">HGV</a></li>
                           <li><a href="/browse/apis/">APIS</a></li>
                           <li class="dclp"><a href="/browse/dclp/">DCLP</a><ul>
                              <li><a href="/browse/dclp/series">browse by series</a></li>
                              <li><a href="/browse/dclp/tm">browse by TM number</a></li>
                              <li><a href="/browse/dclp/authors">browse by authors and works</a></li>
                           </ul>
                           </li>
                           <li class="dialog" id="tm" title="TM Number">
                              <div id="tmc">TM#: <input type="text" name="tmid" value="[enter TM#]" id="tmid" onclick="this.setAttribute('value','');this.focus();" size="10"></input><input onclick="window.location='/trismegistos/'+jQuery('#tmid').attr('value');" type="button" name="tmgo" id="tmgo" value=" &gt; "></input></div>
                           </li>
                           <li>or Search:</li>
                           <li><a href="/search">Data</a></li>
                           <li><a href="/bibliosearch">Bibliography</a></li>
                        </ul>
                     </form>
                  </div>
                  <div id="main">
                     <div class="content">
                        <div class="markdown">
                           <xsl:copy-of select="$content"/>
                        </div>
                     </div>
                  </div>
               </div>
               <div id="footer">
                  <div id="footer-left-col">
                     <div id="secondary-nav">
                        <ul class="nav">
                           <li><a href="/" title="Project Home">Home</a></li>
                           <li><a href="/docs/help" title="Site help">Help</a></li>
                           <li><a href="http://digitalpapyrology.blogspot.com/" title="Digital Papyrology">Blog</a></li>
                           <li><a href="/docs/resources" title="Papyrological tools and resources">Resources</a></li>
                           <li><a href="/feedback.html" title="Feeback">Feedback</a></li>
                           <li><a href="http://epidoc.sourceforge.net/" title="EpiDoc Home">About EpiDoc</a></li>
                        </ul>
                     </div>
                     <div class="spacer"></div>
                  </div>
                  <div id="footer-right-col">
                     <p>Produced by</p>
                     <p><a href="http://blogs.library.duke.edu/dcthree/">The Duke Collaboratory for Classics Computing</a><br></br>&amp; the <a href="http://www.nyu.edu/isaw/">Institute for the Study of the
                        Ancient World</a></p>
                     <div id="about-nav">
                        <ul class="nav">
                           <li><a href="http://www.ulb.ac.be/philo/cpeg/bp.htm" title="Bibliographie Papyrologique Home">BP</a></li>
                           <li><a href="http://www.trismegistos.org/" title="Trismegistos Home">Trismegistos</a></li>
                           <li><a href="http://www.rzuser.uni-heidelberg.de/%7Egv0/" title="Heidelberger Gesamtverzeichnis der Griechischen Papyrusurkunden Ägyptens - Home ">HGV</a></li>
                           <li><a href="/docs/ddbdp" title="Duke Databank of Documentary Papyri - Home">DDbDP</a></li>
                           <li><a href="/docs/apis">APIS</a></li>
                           <li class="last-child"><a href="/docs/about" title="DCLP">DCLP</a></li>
                        </ul>
                        <h3 id="about-hdr">About:</h3>
                     </div>
                  </div>
                  <div class="spacer"></div>
               </div>
            </div><script type="text/javascript" charset="utf-8">
               jQuery("#searchbutton").button();
               jQuery("#editorbutton").button();
            </script></body>
      </html>
   </xsl:template>
   
   <xsl:template name="viewIn">
      <xsl:param name="tm"/>
      view in
      <a href="http://dclp.github.io/dclpxsltbox/output/dclp/{ceiling(number($tm) div 1000)}/{$tm}.html">
         dclpxsltbox
      </a>
      |
      <a href="https://github.com/DCLP/idp.data/blob/dclp/DCLP/{ceiling(number($tm) div 1000)}/{$tm}.xml">
         github
      </a>
   </xsl:template>
   
</xsl:stylesheet>