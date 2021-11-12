<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://www.tei-c.org/ns/1.0" 
  xmlns:xi="http://www.w3.org/2001/XInclude"
  xmlns:pi="http://papyri.info/ns"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  exclude-result-prefixes="#all"
  version="2.0">
    
  <xsl:variable name="values-issues" select="('issues', 'issue', 'volume', 'v', 'vols', 'vol', 'vv')"/>
  <xsl:variable name="values-pages" select="('pp', 'pages', 'page')"/>
  <xsl:variable name="values-numbers" select="('no', 'nos', 'num', 'number', 'numbers', 'n', 'nn')"/>
  
  
  <xsl:template match="t:bibl">
    <xsl:if test="t:seg[@type='original' and @resp='#BP']">
      <div class="biblio"> <!-- class="bp-cite" -->
        <h4>Original BP record</h4>
        <p><xsl:apply-templates select="t:seg[@type='original' and @subtype='index']"/>
          <xsl:apply-templates select="t:seg[@type='original' and @subtype='indexBis']"/>
          <xsl:apply-templates select="t:seg[@type='original' and @subtype='titre']"/>
          <xsl:apply-templates select="t:seg[@type='original' and @subtype='publication']"/>
          <xsl:apply-templates select="t:note[@resp='#BP']"/>
          <xsl:apply-templates select="t:seg[@type='original' and @subtype='sbSeg']"/>
          <xsl:apply-templates select="t:seg[@type='original' and @subtype='cr']"/>
          <xsl:call-template name="bpId" /></p>
      </div>
    </xsl:if>
    <div class="biblio">
      <h4>Provisional papyri.info output</h4>
      <p><xsl:call-template name="buildCitation"/><xsl:text> [</xsl:text><a href="{t:idno[@type='pi']}/source">xml</a><xsl:text>] [</xsl:text><a class="button" id="editbibl" href="/editor/publications/create_from_identifier/papyri.info/biblio/{t:idno[@type='pi']}">edit</a> <xsl:text>]</xsl:text></p>
    </div>
    <xsl:if test="count(t:relatedItem[@type='mentions']/t:bibl) &gt; 0">
      <div class="biblio">
        <h4>Mentioned Texts</h4>
        <p>
          <xsl:call-template name="relatedArticle">
            <xsl:with-param name="relatedArticle" select="t:relatedItem[@type='mentions']/t:bibl" />
          </xsl:call-template>
        </p>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="buildCitation">
    <xsl:param name="biblType"/>
    <xsl:variable name="mainWork" select="pi:get-docs(concat(t:relatedItem[@type='appearsIn' and t:bibl/t:ptr/@target != 'https://papyri.info/biblio/0'][1]//t:ptr/@target, '/source'), 'xml')"/>
    <xsl:variable name="author"><xsl:call-template name="author"/></xsl:variable>
    <xsl:variable name="editor"><xsl:call-template name="editor"/></xsl:variable>
    <xsl:variable name="edFirst" select="string-length($author) = 0 and string-length($editor) > 0"/>
    <xsl:variable name="articleTitle"><xsl:call-template name="articleTitle"/></xsl:variable>
    <xsl:variable name="mainTitle"><xsl:choose>
      <xsl:when test="(@type='article' or @type='review') and $mainWork//*"><xsl:apply-templates select="$mainWork/t:bibl" mode="mainTitle"/></xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$biblType = 'principalEdition'">
            <xsl:choose>
              <xsl:when test="descendant::t:title[@type='short-Checklist']"><xsl:apply-templates select="descendant::t:title[@type='short-Checklist'][1]"/><xsl:text> </xsl:text></xsl:when>
              <xsl:otherwise><xsl:apply-templates select="t:title[1]"/><xsl:text> </xsl:text></xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="t:title[@type='main']">
            <i><xsl:value-of select="t:title[@type='main']"/></i><xsl:if test="t:title[@type='short']"> (<i><xsl:value-of select="t:title[@type='short']"/></i>)</xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <i><xsl:value-of select="t:title[1]"/></i>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose></xsl:variable>
    <xsl:variable name="pubInfo"><xsl:call-template name="pubInfo"><xsl:with-param name="main" select="$mainWork"/></xsl:call-template></xsl:variable>
    <xsl:choose>
      <xsl:when test="$biblType = 'title'">
        <xsl:choose>
          <xsl:when test="descendant::t:title[@type='short-Checklist']"><xsl:apply-templates select="descendant::t:title[@type='short-Checklist'][1]"/><xsl:text> </xsl:text></xsl:when>
          <xsl:otherwise><xsl:apply-templates select="t:title[1]"/><xsl:text> </xsl:text></xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
    <xsl:if test="t:idno[@type='pi']"><b><xsl:value-of select="t:idno[@type='pi']"/>. </b> </xsl:if><xsl:if test="string-length($author) > 0"><xsl:value-of select="$author"/>, </xsl:if>
    <xsl:if test="$edFirst"><xsl:value-of select="normalize-space($editor)"/>, </xsl:if>
    <xsl:if test="t:relatedItem[@type='appearsIn']"><xsl:if test="t:title">"</xsl:if><xsl:copy-of select="$articleTitle"/><xsl:if test="@subtype='journal'">,</xsl:if><xsl:if test="t:title">"</xsl:if><xsl:text> </xsl:text></xsl:if>
    <xsl:copy-of select="$mainTitle"/><xsl:if test="string-length($pubInfo) > 0">, </xsl:if><xsl:value-of select="$pubInfo"/>. </xsl:otherwise></xsl:choose></xsl:template>

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
      <xsl:when test="@type='review'">Review of <xsl:for-each select="t:relatedItem[@type='reviews']"><xsl:for-each select="pi:get-docs(concat(.//t:ptr/@target, '/source'), 'xml')/t:bibl"><a href="/biblio/{t:idno[@type='pi']}/"><xsl:call-template name="buildCitation"/></a></xsl:for-each></xsl:for-each><xsl:text>, </xsl:text></xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template  match="t:bibl" mode="mainTitle">
    <xsl:if test="t:title[@level='m']"><xsl:text> in </xsl:text><xsl:call-template name="editor"/></xsl:if><i><a href="/biblio/{t:idno[@type='pi']}/"><xsl:choose>
      <xsl:when test="t:title[@level='m']"><xsl:value-of select="t:title[@level='m']"/></xsl:when>
      <xsl:when test="t:title[@level='j']">
        <xsl:choose>
          <xsl:when test="t:title[@level='j'][@type='short-BP']"><i><xsl:value-of select="t:title[@level='j'][@type='short-BP'][1]"/></i></xsl:when>
          <xsl:when test="t:title[@level='j'][@type='short']"><i><xsl:value-of select="t:title[@level='j'][@type='short'][1]"/></i></xsl:when>
          <xsl:when test="t:title[@level='j'][@type='main']"><i><xsl:value-of select="t:title[@level='j'][@type='main'][1]"/></i></xsl:when>
          <xsl:when test="t:title[@level='j' and @type='short']"><xsl:value-of select="t:title[@level='j' and @type='short']"/></xsl:when>
          <xsl:otherwise><i><xsl:value-of select="t:title[@level='j'][1]"/></i></xsl:otherwise>
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
        <!-- article in journal: issue number -->
        <xsl:variable name="issue">
          <xsl:value-of select="normalize-space(t:biblScope[@type=$values-issues or @unit=$values-issues])"/>
        </xsl:variable>
        <xsl:if test="$issue != ''">
          <xsl:value-of select="$issue"/>
        </xsl:if>
        <!-- article in journal: date -->
        <xsl:if test="t:date and t:date[. != $issue]">
          <xsl:text> (</xsl:text><xsl:value-of select="t:date"/><xsl:text>)</xsl:text>
        </xsl:if>
        <!-- article in journal: pages -->
        <xsl:if test="t:biblScope[@type=$values-pages or @unit=$values-pages]">
            <xsl:text>, pp. </xsl:text><xsl:call-template name="pages"/>
        </xsl:if>
      </xsl:when>
      <!-- article in book -->
      <xsl:when test="$main//t:title[@level='m']">
        <xsl:if test="t:series">
          <xsl:value-of select="t:series/t:title[@level='s']"/>
          <xsl:if test="t:series/t:biblScope[@type=$values-issues or @unit=$values-issues]">
            <xsl:text> vol. </xsl:text>
            <xsl:value-of select="t:series/t:biblScope[@type=$values-issues or @unit=$values-issues]"/>
          </xsl:if>
        </xsl:if>
        <xsl:if test="t:pubPlace or t:date">
          <xsl:text>(</xsl:text>
          <xsl:if test="$main//t:pubPlace">
            <xsl:value-of select="$main//t:pubPlace"/>
            <xsl:text> </xsl:text>
          </xsl:if>
          <xsl:value-of select="$main//t:date"/>
          <xsl:text>)</xsl:text>
        </xsl:if>
        <xsl:text> </xsl:text>
        <xsl:if test="t:biblScope[@type=$values-pages or @unit=$values-pages]">
          <xsl:call-template name="pages"/>
        </xsl:if>
      </xsl:when>
      <!-- journal -->
      <xsl:when test="t:title[@level='j']"><xsl:if test="t:publisher or t:date">(<xsl:if test="t:publisher"><xsl:value-of select="t:publisher"/><xsl:text> </xsl:text></xsl:if><xsl:value-of select="t:date"/>)</xsl:if></xsl:when>
      <!-- book -->
      <xsl:when test="t:title[@level='m']">(<xsl:if test="t:pubPlace"><xsl:value-of select="t:pubPlace"/><xsl:text> </xsl:text></xsl:if><xsl:value-of select="t:date"/>)</xsl:when>
      <xsl:when test="t:pubPlace or t:date">(<xsl:if test="t:pubPlace"><xsl:value-of select="t:pubPlace"/><xsl:text> </xsl:text></xsl:if><xsl:value-of select="t:date"/>)</xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="pages">
    <xsl:value-of select="t:biblScope[@type=$values-pages or @unit=$values-pages]"/>
  </xsl:template>
  
  <xsl:template match="t:seg[@type='original']">
    <xsl:choose>
      <xsl:when test="@subtype='index'">Index: <xsl:value-of select="."/><br/></xsl:when>
      <xsl:when test="@subtype='indexBis'">Index bis: <xsl:value-of select="."/><br/></xsl:when>
      <xsl:when test="@subtype='titre'">Titre: <xsl:value-of select="."/><br/></xsl:when>
      <xsl:when test="@subtype='publication'">Publication: <xsl:value-of select="."/><br/></xsl:when>
      <xsl:when test="@subtype='sbSeg'">S.B. &amp; S.E.G.: <xsl:value-of select="."/><br/></xsl:when>
      <xsl:when test="@subtype='cr'">C.R.: <xsl:value-of select="."/><br/></xsl:when>
      <xsl:when test="@subtype='nom'"/>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="t:note[@resp='#BP']">
    Résumé: <xsl:value-of select="."/><br/>
  </xsl:template>

  <xsl:template name="bpId">
    <xsl:choose>
      <xsl:when test="string(//t:idno[@type='bp'])">
        No: <xsl:value-of select="//t:idno[@type='bp']"/><br/>
      </xsl:when>
      <xsl:when test="string(//t:idno[@type='bp_old'])">
        Ancien No: <xsl:value-of select="//t:idno[@type='bp_old']"/><br/>
      </xsl:when>
      <xsl:otherwise>
        There is no bp id available
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- related articles (TEI:relatedItem of @type »mentions«) -->
  
  <xsl:template name="relatedArticle">
    <xsl:param name="relatedArticle" />

    <xsl:if test="count($relatedArticle) &gt; 0">
      <xsl:choose>
        <xsl:when test="count($relatedArticle) &gt; 1">
          <ul>
            <xsl:for-each select="$relatedArticle">
              <li>
                <xsl:call-template name="relatedArticleRecord">
                  <xsl:with-param name="series" select="./t:title[@level='s'][@type='short']" />
                  <xsl:with-param name="volume" select="./t:biblScope[@type=$values-issues or @unit=$values-issues]" />
                  <xsl:with-param name="number" select="./t:biblScope[@type=$values-numbers or @unit=$values-numbers]" />
                  <xsl:with-param name="ddbId" select="./t:idno[@type='ddb']" />
                  <xsl:with-param name="inventory" select="./t:idno[@type='invNo']" />
                </xsl:call-template>
              </li>
            </xsl:for-each>
          </ul>
        </xsl:when>
        <xsl:otherwise>
          <p>
            <xsl:call-template name="relatedArticleRecord">
              <xsl:with-param name="series" select="normalize-space($relatedArticle/t:title[@level='s'][@type='short'])" />
              <xsl:with-param name="volume" select="normalize-space($relatedArticle/t:biblScope[@type=$values-issues or @unit=$values-issues])" />
              <xsl:with-param name="number" select="normalize-space($relatedArticle/t:biblScope[@type=$values-numbers or @unit=$values-numbers])" />
              <xsl:with-param name="ddbId" select="normalize-space($relatedArticle/t:idno[@type='ddb'])" />
              <xsl:with-param name="inventory" select="normalize-space($relatedArticle/t:idno[@type='invNo'])" />
            </xsl:call-template>
          </p>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    
  </xsl:template>
  
  <xsl:template name="relatedArticleRecord">
    <xsl:param name="series" />
    <xsl:param name="volume" />
    <xsl:param name="number" />
    <xsl:param name="ddbId" />
    <xsl:param name="inventory" />

    <xsl:variable name="link" select="pi:checkFile($ddbId)"/>
    <xsl:variable name="related">
      <xsl:choose>
        <xsl:when test="string($inventory)">
          <xsl:value-of select="$inventory" />
        </xsl:when>
        <xsl:when test="string($series) or string($volume) or string($number)">
          <xsl:value-of select="$series" />
          <xsl:text> </xsl:text>
          <xsl:value-of select="$volume" />
          <xsl:text> </xsl:text>
          <xsl:value-of select="$number" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="string($link)">
        <a href="{$link}" title="View in PN"><xsl:value-of select="$related" /></a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$related" />
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>
  
  <xsl:function name="pi:checkFile">
    <xsl:param name="ddb" />
    <xsl:variable name="link" select="concat('http://papyri.info/ddbdp/', $ddb)" />
    <xsl:variable name="test" select="doc-available(pi:get-filename(concat($link, '/source'), 'xml'))"/>
    <xsl:if test="$test">
      <xsl:value-of select="$link" />
    </xsl:if>
  </xsl:function>
  
</xsl:stylesheet>
