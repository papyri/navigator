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
  <xsl:variable name="id">http://papyri.info/biblio/<xsl:value-of select="replace(/t:bibl/@xml:id, '[a-zA-Z]', '')"/></xsl:variable>
  
  <xsl:template match="/t:bibl">
    <rdf:Description rdf:about="{$id}">
      <dcterms:source rdf:parseType="resource">
        <dcterms:bibliographicCitation rdf:resource="{$id}"/>
      </dcterms:source>
      <xsl:apply-templates select="t:relatedItem"/>
    </rdf:Description>
  </xsl:template>  
  
  <xsl:template match="t:relatedItem[@type='appearsIn']">
    <dcterms:isPartOf rdf:resource="{t:bibl/t:ptr/@target}">
      <dcterms:hasPart rdf:resource="{$id}"/>
    </dcterms:isPartOf>
  </xsl:template>
  
  <xsl:template match="t:relatedItem[@type='reviews']">
    <cito:reviews rdf:resource="{t:bibl/t:ptr/@target}">
      <cito:isReviewedBy rdf:resource="{$id}"/>
    </cito:reviews>
  </xsl:template>
  
  <xsl:template match="t:relatedItem[@type='mentions']">
    <dcterms:references rdf:resource="http://papyri.info/ddbdp/{t:biblio/t:idno[@type='ddb']}/edition">
      <dcterms:isReferencedBy rdf:resource="{$id}"/>
    </dcterms:references>
  </xsl:template>
  
</xsl:stylesheet>