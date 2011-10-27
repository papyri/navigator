<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://www.tei-c.org/ns/1.0" 
  xmlns:xi="http://www.w3.org/2001/XInclude"
  xmlns:pi="http://papyri.info/ns"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:variable name="path">/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase">/data/papyri.info/pn/idp.html</xsl:variable>
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
        <script src="/js/jquery-1.5.1.min.js" type="text/javascript" charset="utf-8"></script>
        <script src="/js/jquery-ui-1.8.14.custom.min.js" type="text/javascript" charset="utf-8"></script>
        <script src="/js/jquery.bubblepopup.v2.1.5.min.js" type="text/javascript" charset="utf-8"></script>
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
                <xsl:apply-templates select="t:bibl"/>
              </div>
            </div>
          </div>
          <xi:include href="footer.xml"/>
        </div>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="t:bibl">
    <p><xsl:call-template name="buildCitation"/> <a href="source">xml</a> <a class="button" id="editbibl" href="/editor/publications/create_from_identifier/papyri.info/biblio/{t:idno[@type='pi']}">edit</a></p>
    <xsl:if test="t:seg[@type='original' and @resp='#BP']">
      <div class="bp-cite">
        <h4>original BP record</h4>
        <p><xsl:for-each select="t:seg[@type='original']">
          <xsl:value-of select="@subtype"/>: <xsl:value-of select="."/><xsl:if test="position() != last()"><br/></xsl:if>
        </xsl:for-each></p>
      </div>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="buildCitation">
    <xsl:variable name="mainWork" select="pi:get-docs(t:relatedItem[@type='appearsIn']//t:ptr/@target, 'xml')"/>
    <xsl:variable name="author"><xsl:call-template name="author"/></xsl:variable>
    <xsl:variable name="editor"><xsl:call-template name="editor"/></xsl:variable>
    <xsl:variable name="edFirst" select="string-length($author) = 0 and string-length($editor) > 0"/>
    <xsl:variable name="articleTitle"><xsl:call-template name="articleTitle"/></xsl:variable>
    <xsl:variable name="mainTitle"><xsl:choose>
      <xsl:when test="(@type='article' or @type='review') and $mainWork//*"><xsl:apply-templates select="$mainWork/t:bibl" mode="mainTitle"/></xsl:when>
      <xsl:otherwise><i><xsl:value-of select="t:title[@type='main']"/></i><xsl:if test="t:title[@type='short']"> (<i><xsl:value-of select="t:title[@type='short']"/></i>)</xsl:if></xsl:otherwise>
    </xsl:choose></xsl:variable>
    <xsl:variable name="pubInfo"><xsl:call-template name="pubInfo"><xsl:with-param name="main" select="$mainWork"/></xsl:call-template></xsl:variable>
    <b><xsl:value-of select="t:idno[@type='pi']"/>. </b> <xsl:if test="string-length($author) > 0"><xsl:value-of select="$author"/>, </xsl:if>
      <xsl:if test="$edFirst"><xsl:value-of select="normalize-space($editor)"/>, </xsl:if>
    <xsl:if test="t:relatedItem[@type='appearsIn']"><xsl:if test="t:title">"</xsl:if><xsl:copy-of select="$articleTitle"/><xsl:if test="@subtype='journal'">,</xsl:if><xsl:if test="t:title">"</xsl:if> </xsl:if>
      <xsl:copy-of select="$mainTitle"/><xsl:if test="string-length($pubInfo) > 0">, </xsl:if><xsl:value-of select="$pubInfo"/>. </xsl:template>
  
  <xsl:template name="author">
    <xsl:for-each select="t:author"><xsl:if test="count(../t:author) > 1 and position() = last()"><xsl:text> and </xsl:text></xsl:if>
      <xsl:choose>
        <xsl:when test="t:forename|t:surname"><xsl:value-of select="t:forename"/><xsl:text> </xsl:text><xsl:value-of select="t:surname"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
      </xsl:choose><xsl:if test="(position() != last()) and (count(../t:author) > 2)"><xsl:text>, </xsl:text></xsl:if></xsl:for-each>
  </xsl:template>
  
  <xsl:template name="articleTitle">
    <xsl:choose>
      <xsl:when test="t:title[@level='a']"><xsl:value-of select="t:title[@level='a']"/></xsl:when>
      <xsl:when test="@type='review'">Review of <xsl:for-each select="pi:get-docs(t:relatedItem[@type='reviews']//t:ptr/@target, 'xml')/t:bibl"><a href="/biblio/{t:idno[@type='pi']}/"><xsl:call-template name="buildCitation"/></a></xsl:for-each><xsl:text>, </xsl:text></xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="mainTitle" match="t:bibl" mode="mainTitle">
    <xsl:if test="t:title[@level='m']"><xsl:text> in </xsl:text><xsl:call-template name="editor"/></xsl:if><i><a href="/biblio/{t:idno[@type='pi']}/"><xsl:choose>
      <xsl:when test="t:title[@level='m']"><xsl:value-of select="t:title[@level='m']"/></xsl:when>
      <xsl:when test="t:title[@level='j']">
        <xsl:choose>
          <xsl:when test="t:title[@level='j' and @type='short']"><xsl:value-of select="t:title[@level='j' and @type='short']"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="t:title[@level='j' and @type='main']"/></xsl:otherwise>
        </xsl:choose>
      </xsl:when>
    </xsl:choose></a></i>
  </xsl:template>
  
  <xsl:template name="editor">
    <xsl:for-each select="t:editor">
      <xsl:sort select="number(@n)"/>
      <xsl:if test="position() > 1 and position() = last()"><xsl:text> and </xsl:text></xsl:if><xsl:value-of select="."/><xsl:if test="count(../t:editor) > 2 and position() != last()">, </xsl:if>
    </xsl:for-each><xsl:if test="t:editor"> ed<xsl:if test="count(t:editor) > 1">s</xsl:if>.</xsl:if>
  </xsl:template>
  
  <xsl:template name="pubInfo">
    <xsl:param name="main"/>
    <xsl:choose>
      <!-- article in journal -->
      <xsl:when test="$main//t:title[@level='j']">
        <!-- TODO: get additional biblScope values -->
        <xsl:value-of select="t:biblScope[@type='issue']"/> <xsl:if test="t:book">(<xsl:value-of select="$main//t:date"/>)</xsl:if>
      </xsl:when>
      <!-- article in book -->
      <xsl:when test="$main//t:title[@level='m']">
        <xsl:if test="t:series"><xsl:value-of select="t:series/t:title[@level='s']"/><xsl:if test="t:series/t:biblScope[@type='volume']"> vol. <xsl:value-of select="t:series/t:biblScope[@type='volume']"/></xsl:if></xsl:if>
        <xsl:if test="t:publisher or t:date">(<xsl:if test="$main//t:publisher"><xsl:value-of select="$main/t:publisher"/><xsl:text> </xsl:text></xsl:if><xsl:value-of select="$main//t:date"/>)</xsl:if> <xsl:if test="t:biblScope[@type='pp']"><xsl:call-template name="pages"/></xsl:if>
      </xsl:when>
      <!-- journal -->
      <xsl:when test="t:title[@level='j']"><xsl:if test="t:publisher">(<xsl:value-of select="t:publisher"/>)</xsl:if></xsl:when>
      <!-- book -->
      <xsl:when test="t:title[@level='m']">(<xsl:if test="t:publisher"><xsl:value-of select="t:publisher"/><xsl:text> </xsl:text></xsl:if><xsl:value-of select="t:date"/>)</xsl:when>
      <xsl:when test="t:publisher or t:date">(<xsl:if test="t:publisher"><xsl:value-of select="t:publisher"/><xsl:text> </xsl:text></xsl:if><xsl:value-of select="t:date"/>)</xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="pages">
    <xsl:value-of select="t:biblScope[@type='pp']"/>
  </xsl:template>
  
</xsl:stylesheet>