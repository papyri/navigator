<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns:pi="http://papyri.info/ns"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:cito="http://purl.org/spar/cito/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:gawd="http://gawd.atlantides.org/terms/"
  exclude-result-prefixes="t"
  version="2.0">
  <xsl:output omit-xml-declaration="yes"/>
  
  
  <xsl:variable name="path">/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase">/data/papyri.info/pn/idp.html</xsl:variable>
  <xsl:variable name="id">http://papyri.info/biblio/<xsl:value-of select="replace(/t:bibl/@xml:id, '[a-zA-Z]', '')"/></xsl:variable>
  
  <xsl:template match="/t:bibl">
    <rdf:Description rdf:about="{$id}">
      <rdfs:type rdf:resource="gawd:BibliographicReference"/>
      <dcterms:relation rdf:resource="{$id}/frbr:Work"/>
    </rdf:Description>
    <rdf:Description rdf:about="{$id}/frbr:Work">
    <dcterms:bibliographicCitation rdf:resource="{$id}"/>
    <xsl:apply-templates select="t:relatedItem"/>
    </dcterms:relation>
    </rdf:Description>
  </xsl:template>  
  
  
  
  <xsl:template match="t:relatedItem[@type='appearsIn']">
    <dcterms:isPartOf rdf:resource="{t:bibl/t:ptr/@target}/frbr:Work">
      <dcterms:hasPart rdf:resource="{$id}/frbr:Work"/>
    </dcterms:isPartOf>
  </xsl:template>
  
  <xsl:template match="t:relatedItem[@type='reviews']">
    <cito:reviews rdf:resource="{t:bibl/t:ptr/@target}/frbr:Work">
      <cito:isReviewedBy rdf:resource="{$id}/frbr:Work"/>
    </cito:reviews>
  </xsl:template>
  
  <xsl:template match="t:relatedItem[@type='mentions']">
    <dcterms:references rdf:resource="http://papyri.info/ddbdp/{t:biblio/t:idno[@type='ddb']}/edition">
      <rdfs:type rdf:resource="gawd:Edition"/>
      <dcterms:isReferencedBy rdf:resource="{$id}/frbr:Work"/>
    </dcterms:references>
  </xsl:template>
  
</xsl:stylesheet>