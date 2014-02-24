<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="xs t"
  version="2.0">
  <xsl:output indent="yes"/>
  <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
    <desc>Convert an EpiDoc XML document using the parallel segmentation
          apparatus method into one using standoff apparatus.</desc>
  </doc>
  
  <xsl:template match="/">
    <xsl:processing-instruction name="xml-stylesheet">type="text/xsl" href="/annotator/xslt/teibp.xsl"</xsl:processing-instruction>
    <xsl:choose>
      <xsl:when test="//t:div[@type='apparatus']">
        <xsl:apply-templates select="node()|comment()|processing-instruction()" mode="unmake-app"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="node()|comment()|processing-instruction()" mode="make-app"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="node()|@*|comment()|processing-instruction()" mode="make-app">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*|comment()|processing-instruction()" mode="make-app"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="node()|@*|comment()|processing-instruction()" mode="unmake-app">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*|comment()|processing-instruction()" mode="unmake-app"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- (MakeApp) Remove inline apparatus -->
  
  <!-- (MakeApp) Turn an app, choice, or subst into a ref to that structure in the new
        standoff app -->
  <xsl:template match="t:app|t:choice|t:subst" mode="make-app">
    <xsl:variable name="id" select="generate-id(.)"/>
    <ref target="#{$id}" xml:id="ref-{$id}"><xsl:apply-templates select="t:lem|t:orig|t:sic|t:add"/></ref>
  </xsl:template>
  
  <xsl:template match="t:lem|t:orig|t:sic|t:add" mode="make-app">
    <xsl:apply-templates mode="make-app"/>
  </xsl:template>
  
  <xsl:template match="t:hi[@rend = 'diaeresis']" mode="make-app">
    <xsl:variable name="id" select="generate-id(.)"/>
    <ref target="#{$id}" xml:id="ref-{$id}"><xsl:apply-templates/></ref>
  </xsl:template>
  
  <xsl:template match="t:div[@type='edition']" mode="make-app">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*|comment()|processing-instruction()" mode="make-app"/>
    </xsl:copy>
    <xsl:if test=".//t:app or .//t:choice">
      <div type="apparatus">
        <listApp>
          <xsl:for-each select=".//(t:app|t:choice|t:subst|t:hi[@rend='diaeresis'])">
            <xsl:apply-templates select="." mode="apparatus"/>
          </xsl:for-each>
        </listApp>
      </div>
    </xsl:if>
  </xsl:template>
  
  <!-- (UnMakeApp) Put standoff apparatus back inline -->
  
  <xsl:template match="t:div[@type='apparatus']" mode="unmake-app"/>
  
  <!-- (UnMakeApp) Squash the ref -->
  <xsl:template match="t:ref[ancestor::t:text]" mode="unmake-app">
    <xsl:apply-templates select="//t:app[@xml:id = substring-after(current()/@target,'#')]" mode="unmake-app"/>
  </xsl:template>
  
  <!-- (UnMakeApp) Fetch the content from the standoff app and put it back -->
  <xsl:template match="t:app" mode="unmake-app">
    <xsl:choose>
      <!-- was a choice/orig|reg -->
      <xsl:when test="t:lem/t:orig"><choice><xsl:apply-templates select="t:rdg/t:reg" mode="unmake-app"/><xsl:apply-templates select="t:lem/t:orig" mode="unmake-app"/></choice></xsl:when>
      <!-- was a subst/add|del -->
      <xsl:when test="t:lem/t:add"><subst><xsl:apply-templates select="t:lem/t:add|t:rdg/t:del" mode="unmake-app"/></subst></xsl:when>
      <!-- was a hi -->
      <xsl:when test="t:rdg/t:hi"><hi rend="{t:rdg/t:hi/@rend}"><xsl:value-of select="//t:ref[@xml:id = substring-after(current()/@from, '#')]"/></hi></xsl:when>
      <!-- was an app/lem|rdg -->
      <xsl:otherwise><app><xsl:apply-templates select="@type" mode="unmake-app"/><xsl:apply-templates select="*" mode="unmake-app"/></app></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="t:lem|t:rdg" mode="unmake-app"><xsl:element name="{local-name(.)}"><xsl:apply-templates select="@resp" mode="unmake-app"/><xsl:apply-templates mode="unmake-app"/></xsl:element></xsl:template>
  
  <xsl:template match="t:wit" mode="unmake-app"/>
  
  <xsl:template match="t:div[@type='edition']" mode="unmake-app">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*|comment()|processing-instruction()" mode="unmake-app"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- Build the standoff apparatus -->
    
  <xsl:template match="t:lem|t:rdg" mode="apparatus">
    <xsl:copy>
      <xsl:attribute name="xml:space">preserve</xsl:attribute>
      <xsl:apply-templates select="node()|@*" mode="make-app"/><xsl:if test="@resp">
        <wit><xsl:text> </xsl:text><xsl:value-of select="@resp"/></wit>
      </xsl:if>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="t:orig|t:sic|t:add" mode="apparatus">
    <lem xml:space="preserve"><xsl:copy><xsl:apply-templates select="node()|@*" mode="make-app"/></xsl:copy></lem>
  </xsl:template>
  
  <xsl:template match="t:reg|t:corr|t:del" mode="apparatus">
    <rdg xml:space="preserve"><xsl:copy><xsl:apply-templates select="node()|@*" mode="make-app"/></xsl:copy></rdg>
  </xsl:template>
  
  <!-- Apparatus mode -->
  <xsl:template match="t:app|t:choice|t:subst" mode="apparatus">
    <xsl:variable name="id" select="generate-id(.)"/>
    <xsl:variable name="n"><xsl:choose>
      <xsl:when test=".//t:lb"><xsl:value-of select="preceding::t:lb[1]/@n"/>-<xsl:value-of select="distinct-values(.//t:lb[position()=last()]/@n)"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="preceding::t:lb[1]/@n"/></xsl:otherwise>
    </xsl:choose></xsl:variable>
    <app xml:id="{$id}" from="#ref-{$id}" n="{$n}">
      <xsl:apply-templates select="@*" mode="make-app"/>
      <xsl:apply-templates select="t:lem|t:orig|t:sic|t:add" mode="apparatus"/>
      <xsl:apply-templates select="t:rdg|t:reg|t:corr|t:del" mode="apparatus"/>
    </app>
  </xsl:template>
  
  <xsl:template match="t:hi[@rend='diaeresis']" mode="apparatus">
    <xsl:variable name="id" select="generate-id(.)"/>
    <xsl:variable name="n"><xsl:choose>
      <xsl:when test=".//t:lb"><xsl:value-of select="preceding::t:lb[1]/@n"/>-<xsl:value-of select="distinct-values(.//t:lb[position()=last()]/@n)"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="preceding::t:lb[1]/@n"/></xsl:otherwise>
    </xsl:choose></xsl:variable>
    <app xml:id="{$id}" from="#ref-{$id}" n="{$n}">
      <lem xml:space="preserve"><xsl:value-of select="."/><xsl:apply-templates select="following-sibling::node()[1]" mode="next-word"/></lem>
      <rdg xml:space="preserve"><hi rend="diaeresis"><xsl:value-of select="replace(normalize-unicode(.,'NFD'),'[\p{IsCombiningDiacriticalMarks}]','')"></xsl:value-of></hi><xsl:apply-templates select="following-sibling::node()[1]" mode="next-word"/></rdg>
    </app>
  </xsl:template>
  
  <xsl:template match="*|@*" mode="next-word">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*" mode="next-word"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="text()" mode="next-word">
    <xsl:choose>
      <xsl:when test="contains(.,' ')"><xsl:value-of select="substring-before(., ' ')"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
    
</xsl:stylesheet>
