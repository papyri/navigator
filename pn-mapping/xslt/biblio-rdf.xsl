<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://www.tei-c.org/ns/1.0" xmlns:pi="http://papyri.info/ns"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:bibo="http://purl.org/ontology/bibo/" xmlns:cito="http://purl.org/spar/cito/"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:gawd="http://gawd.atlantides.org/terms/"
  exclude-result-prefixes="t" version="2.0">
  <xsl:output omit-xml-declaration="yes"/>


  <xsl:variable name="path">/data/papyri.info/idp.data</xsl:variable>
  <xsl:variable name="outbase">/data/papyri.info/pn/idp.html</xsl:variable>
  <xsl:variable name="id">http://papyri.info/biblio/<xsl:value-of
      select="replace(/t:bibl/@xml:id, '[a-zA-Z]', '')"/></xsl:variable>
  <xsl:variable name="urn">urn:pi:http:papyri.info/biblio/<xsl:value-of
      select="replace(/t:bibl/@xml:id, '[a-zA-Z]', '')"/></xsl:variable>

  <xsl:template match="/t:bibl">
    <rdf:Description rdf:about="{$id}">
      <rdfs:type rdf:resource="http://gawd.atlantides.org/terms/BibliographicReference"/>
      <dcterms:references rdf:resource="{$urn}"/>
    </rdf:Description>
    <rdf:Description rdf:about="{$urn}">
      <rdfs:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
      <dcterms:bibliographicCitation rdf:resource="{$id}"/>
      <xsl:apply-templates select="t:relatedItem"/>
    </rdf:Description>
  </xsl:template>

  <xsl:template match="t:relatedItem[@type='appearsIn']">
    <dcterms:isPartOf>
      <rdf:Description rdf:about="urn:pi:{replace(t:bibl/t:ptr/@target, '//', '')}">
        <dcterms:hasPart rdf:resource="{$urn}"/>
      </rdf:Description>
    </dcterms:isPartOf>
  </xsl:template>

  <xsl:template match="t:relatedItem[@type='reviews']">
    <cito:reviews rdf:resource="{$urn}">
      <rdf:Description rdf:about="urn:pi:{replace(t:bibl/t:ptr/@target, '//', '')}">
        <cito:isReviewedBy rdf:resource="{$urn}"/>
      </rdf:Description>
    </cito:reviews>
  </xsl:template>

  <xsl:template match="t:relatedItem[@type='mentions']">
    <xsl:if test="not(contains(t:bibl/t:idno[@type='ddb'], ' '))">
      <dcterms:references>
        <rdf:Description rdf:about="urn:pi:http:papyri.info/ddbdp/{t:bibl/t:idno[@type='ddb']}">
          <rdfs:type rdf:resource="http://gawd.atlantides.org/terms/Edition"/>
          <dcterms:isReferencedBy rdf:resource="{$urn}"/>
        </rdf:Description>
      </dcterms:references>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
