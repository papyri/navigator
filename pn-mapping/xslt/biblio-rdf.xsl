<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns:pi="http://papyri.info/ns"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:cito="http://purl.org/spar/cito/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  exclude-result-prefixes="t"
  version="2.0">
  <xsl:output omit-xml-declaration="yes"/>
  
  
  <xsl:variable name="path">/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase">/data/papyri.info/pn/idp.html</xsl:variable>
  
  <xsl:template match="/t:bibl">
    <xsl:variable name="id">http://papyri.info/<xsl:value-of select="replace(@xml:id, '[a-zA-Z]', '')"/></xsl:variable>
    <rdf:Description rdf:about="{$id}">
      <dcterms:source rdf:parseType="resource">
        <dcterms:bibliographicCitation rdf:resource="{$id}"/>
      </dcterms:source>
      <xsl:apply-templates select="t:relatedItem//t:ptr"><xsl:with-param name="id" select="$id"/></xsl:apply-templates>
    </rdf:Description>
  </xsl:template>  
  
  <xsl:template match="t:relatedItem[@type='appearsIn']//t:ptr">
    <xsl:param name="id"/>
    <dcterms:isPartOf rdf:resource="{@target}">
      <dcterms:hasPart rdf:resource="{$id}"/>
    </dcterms:isPartOf>
  </xsl:template>
  
  <xsl:template match="t:relatedItem[@type='reviews']//t:ptr">
    <xsl:param name="id"/>
    <cito:reviews rdf:resource="{@target}">
      <cito:isReviewedBy rdf:resource="{$id}"/>
    </cito:reviews>
  </xsl:template>
  
  <xsl:template match="t:relatedItem[@type='mentions']//t:bibl">
    <xsl:param name="id"/>
    <dcterms:references rdf:resource="http://papyri.info/ddbdp/{t:idno[@type='ddb']}/edition">
      <dcterms:isReferencedBy rdf:resource="{$id}"/>
    </dcterms:references>
  </xsl:template>
  
</xsl:stylesheet>