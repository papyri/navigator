<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dct="http://purl.org/dc/terms/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:olo="http://purl.org/ontology/olo/core#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" exclude-result-prefixes="xs tei" version="2.0">
  <xsl:output omit-xml-declaration="yes"/>
  <xsl:param name="root">/srv/data/papyri.info/idp.data</xsl:param>
  <xsl:param name="DDB-root">/srv/data/papyri.info/idp.data/Historical</xsl:param>

  <xsl:template match="/tei:TEI">
    <xsl:if test="string-length(//tei:publicationStmt/tei:idno[@type = 'apisid']) gt 0">
      <xsl:variable name="id">https://papyri.info/apis/<xsl:value-of
          select="//tei:publicationStmt/tei:idno[@type = 'apisid']/text()"/>/source</xsl:variable>
      <rdf:Description rdf:about="{$id}">
        <dct:identifier>papyri.info/apis/<xsl:value-of
            select="//tei:publicationStmt/tei:idno[@type = 'apisid']/text()"/></dct:identifier>
        <dct:identifier>
          <xsl:value-of select="//tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:idno"/>
        </dct:identifier>
        <dct:isPartOf>
          <rdf:Description
            rdf:about="https://papyri.info/apis/{substring-before(//tei:publicationStmt/tei:idno[@type = 'apisid'], '.')}">
            <dct:isPartOf rdf:resource="https://papyri.info/apis"/>
          </rdf:Description>
        </dct:isPartOf>        
        <xsl:for-each select="//tei:publicationStmt/tei:idno[@type = 'HGV']">
          <xsl:for-each select="tokenize(., '\s')">
            <xsl:variable name="dir" select="ceiling(number(replace(., '[a-z]', '')) div 1000)"/>
            <xsl:if
              test="doc-available(concat('file://', $root, '/HGV_meta_EpiDoc/HGV', $dir, '/', ., '.xml'))">
              <dct:relation>
                <rdf:Description rdf:about="https://papyri.info/hgv/{.}/source">
                  <dct:relation rdf:resource="{$id}"/>
                </rdf:Description>
              </dct:relation>
              <dct:relation>
                <rdf:Description rdf:about="https://papyri.info/current/{.}/source">
                  <dct:relation rdf:resource="{$id}"/>
                </rdf:Description>
              </dct:relation>
            </xsl:if>
          </xsl:for-each>
        </xsl:for-each>
        <xsl:for-each select="//tei:publicationStmt/tei:idno[@type = 'TM']">
          <xsl:variable name="HGV" select="exists(//tei:publicationStmt/tei:idno[@type = 'HGV'])"/>
          <xsl:for-each select="tokenize(., '\s')">
            <dct:relation>
              <rdf:Description rdf:about="http://www.trismegistos.org/text/{.}">
                <dct:relation rdf:resource="{$id}"/>
              </rdf:Description>
            </dct:relation>
            <xsl:if test="not($HGV)">
              <dct:relation>
                <rdf:Description rdf:about="https://papyri.info/current/{.}/source">
                  <dct:relation rdf:resource="{$id}"/>
                </rdf:Description>
              </dct:relation>
            </xsl:if>
          </xsl:for-each>
        </xsl:for-each>
        
        <xsl:if test="//tei:facsimile">
          <dct:relation
            rdf:resource="https://papyri.info/apis/{//tei:publicationStmt/tei:idno[@type = 'apisid']/text()}/images"
          />
        </xsl:if>
        <foaf:page>
          <rdf:Description rdf:about="{substring-before($id, '/source')}">
            <foaf:topic rdf:resource="{$id}"/>
          </rdf:Description>
        </foaf:page>
      </rdf:Description>
      <xsl:if test="//tei:facsimile">
        <rdf:Description
          rdf:about="https://papyri.info/apis/{//tei:publicationStmt/tei:idno[@type = 'apisid']/text()}/images">
          <rdf:type rdf:resource="http://purl.org/ontology/olo/core#OrderedList"/>
          <olo:length rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">
            <xsl:value-of select="count(//tei:facsimile//tei:graphic)"/>
          </olo:length>
          <xsl:for-each select="//tei:facsimile//tei:graphic">
            <olo:slot>
              <rdf:Description
                rdf:about="https://papyri.info/apis/{//tei:publicationStmt/tei:idno[@type = 'apisid']/text()}/images/{position()}">
                <olo:index rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">
                  <xsl:value-of select="position()"/>
                </olo:index>
                <olo:item>
                  <rdf:Description rdf:about="{@url}">
                    <rdfs:label>
                      <xsl:choose>
                        <xsl:when test="//tei:msIdentifier/tei:idno[@type = 'invNo']">
                          <xsl:value-of select="//tei:msIdentifier/tei:idno[@type = 'invNo']"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="//tei:idno[@type = 'apisid']"/>
                        </xsl:otherwise>
                      </xsl:choose>
                      <xsl:if test="ancestor::tei:surfaceGrp/@n and not(../tei:desc)">
                        <xsl:text>,
                  </xsl:text>
                        <xsl:value-of select="ancestor::tei:surfaceGrp/@n"/>
                      </xsl:if>
                      <xsl:choose>
                        <xsl:when test="../tei:desc">
                          <xsl:text> </xsl:text>
                          <xsl:value-of select="../tei:desc"/>
                        </xsl:when>
                        <xsl:when test="../@type">
                          <xsl:text> </xsl:text>
                          <xsl:value-of select="../@type"/>
                        </xsl:when>
                      </xsl:choose>
                    </rdfs:label>
                    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Image"/>
                    <foaf:depicts
                      rdf:resource="https://papyri.info/apis/{//tei:publicationStmt/tei:idno[@type = 'apisid']/text()}/original"
                    />
                  </rdf:Description>
                </olo:item>
              </rdf:Description>
            </olo:slot>
          </xsl:for-each>
        </rdf:Description>
        <rdf:Description
          rdf:about="https://papyri.info/apis/{//tei:publicationStmt/tei:idno[@type = 'apisid']/text()}/source">
          <dct:source
            rdf:resource="https://papyri.info/apis/{//tei:publicationStmt/tei:idno[@type = 'apisid']/text()}/original"
          />
        </rdf:Description>
      </xsl:if>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
