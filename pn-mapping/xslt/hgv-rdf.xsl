<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dct="http://purl.org/dc/terms/"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:bibo="http://purl.org/ontology/bibo/"
    xmlns:lawd="http://lawd.info/ontology/"
    xmlns:foaf="http://xmlns.com/foaf/0.1/"
    exclude-result-prefixes="xs tei" version="2.0">
    <xsl:output omit-xml-declaration="yes"/>
    <xsl:param name="DDB-root"/>
    
    <xsl:template match="/tei:TEI">
        <xsl:variable name="id">http://papyri.info/hgv/<xsl:value-of select="//tei:publicationStmt/tei:idno[@type='filename']"/>/source</xsl:variable>
        <xsl:variable name="bibl" select="//tei:div[@type='bibliography']//tei:bibl[@type='publication' and @subtype='principal']"/>
        <xsl:variable name="title" select="replace(normalize-unicode(replace($bibl/tei:title[@level='s'],'\s','_'), 'NFD'), '[^._a-zA-Z]', '')"/>
        <xsl:variable name="unicode-title" select="replace(replace($bibl/tei:title[@level='s'],'\s','_'), '[^._\p{L}]', '')"></xsl:variable>
        <xsl:variable name="cite_uri">http://papyri.info/hgv/<xsl:value-of select="$title"/><xsl:if test="$bibl//tei:biblScope[@type='volume']">_<xsl:value-of select="normalize-space($bibl//tei:biblScope[@type='volume'])"/></xsl:if><xsl:for-each select="$bibl//tei:biblScope[not(@type='volume')]">_<xsl:value-of select="encode-for-uri(normalize-space(.))"/></xsl:for-each></xsl:variable>
        <xsl:variable name="ddb" select="tokenize(normalize-space(//tei:publicationStmt/tei:idno[@type='ddb-hybrid']), ';')"></xsl:variable>
        <xsl:variable name="ddb-doc-uri">
            <xsl:choose>
                <xsl:when test="string-length($ddb[2]) = 0"><xsl:value-of select="concat('file://', $DDB-root, '/', $ddb[1], '/', $ddb[1], '.', encode-for-uri($ddb[3]), '.xml')"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="concat('file://', $DDB-root, '/', $ddb[1], '/', $ddb[1], '.', $ddb[2], '/', $ddb[1], '.', $ddb[2], '.', encode-for-uri($ddb[3]), '.xml')"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <rdf:Description rdf:about="{$id}">
            <dct:identifier>papyri.info/hgv/<xsl:value-of select="//tei:publicationStmt/tei:idno[@type='filename']"/></dct:identifier>
            <xsl:for-each select="//tei:publicationStmt/tei:idno[@type='TM']">
                <dct:identifier>tm:<xsl:value-of select="."/></dct:identifier>
            </xsl:for-each>
            <dct:identifier>
                <rdf:Description>
                    <xsl:attribute name="rdf:about"><xsl:value-of select="$cite_uri"/></xsl:attribute>
                    <dct:identifier rdf:resource="{$id}"/>
                </rdf:Description>
            </dct:identifier>
          <!--
            <dct:references>
                <xsl:attribute name="rdf:resource">http://papyri.info/navigator/full/hgv_<xsl:value-of select="encode-for-uri($bibl/tei:title[@level='s'])"/>_<xsl:value-of select="$bibl//tei:biblScope[@type='volume'][not(matches(., '\s+'))]"/>:<xsl:value-of select="encode-for-uri($bibl//tei:biblScope[@type='numbers'])"/><xsl:for-each select="$bibl//tei:biblScope[@type='parts']">:<xsl:value-of select="replace(encode-for-uri(.), '%', '%25')"/></xsl:for-each></xsl:attribute>
            </dct:references>
          -->
            <dct:isPartOf>
                <xsl:choose>
                  <xsl:when test="$bibl//tei:biblScope[@type='volume'][not(matches(., '\s+'))]">
                        <rdf:Description rdf:about="http://papyri.info/hgv/{$title}_{normalize-space($bibl//tei:biblScope[@type='volume'])}">
                          <dct:bibliographicCitation><xsl:value-of select="$bibl/tei:title[@level='s']"/><xsl:text> </xsl:text><xsl:value-of select="$bibl//tei:biblScope[@type='volume']"/></dct:bibliographicCitation>
                          <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
                            <dct:isPartOf>
                                <rdf:Description rdf:about="http://papyri.info/hgv/{$title}">
                                  <rdf:type rdf:resource="http://purl.org/ontology/bibo/Series"/>
                                  <dct:bibliographicCitation><xsl:value-of select="$bibl/tei:title[@level='s']"/></dct:bibliographicCitation>
                                   <dct:isPartOf rdf:resource="http://papyri.info/hgv"/>
                                </rdf:Description>
                            </dct:isPartOf>
                        </rdf:Description>
                    </xsl:when>
                    <xsl:otherwise>
                        <rdf:Description rdf:about="http://papyri.info/hgv/{$title}">
                          <dct:bibliographicCitation><xsl:value-of select="$bibl/tei:title[@level='s']"/></dct:bibliographicCitation>
                          <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
                          <dct:isPartOf rdf:resource="http://papyri.info/hgv"/>
                        </rdf:Description>
                    </xsl:otherwise>
                </xsl:choose>
            </dct:isPartOf>
            <xsl:if test="(//tei:publicationStmt/tei:idno[@type='ddb-hybrid']) and doc-available($ddb-doc-uri)">
                <dct:relation>
                    <rdf:Description>
                    	<xsl:attribute name="rdf:about">http://papyri.info/ddbdp/<xsl:value-of select="replace(normalize-unicode($ddb[1], 'NFD'), '[^.a-z0-9]', '')"/>;<xsl:value-of select="$ddb[2]"/>;<xsl:value-of select="encode-for-uri($ddb[3])"/>/source</xsl:attribute>
                        <dct:relation rdf:resource="{$id}"/>
                    </rdf:Description>
                </dct:relation>
            </xsl:if>
          <xsl:for-each select="//tei:idno[@type = 'TM']">
            <dct:relation rdf:resource="http://www.trismegistos.org/text/{.}"/>
            <dct:relation>
              <rdf:Description rdf:about="http://papyri.info/trismegistos/{.}">
                <dct:relation rdf:resource="{$id}"/>
              </rdf:Description>
            </dct:relation>
          </xsl:for-each>
            <xsl:for-each select="//tei:text/tei:body/tei:div[@type='figure']//tei:graphic[contains(@url, 'columbia.edu') and contains(@url, 'key')]">
                <dct:relation>
                    <rdf:Description rdf:about="http://papyri.info/apis/{normalize-space(substring-after(@url, 'key='))}/source">
                        <dct:relation rdf:resource="{$id}"/>
                      <xsl:if test="//tei:publicationStmt/tei:idno[@type='ddb-hybrid'] and doc-available($ddb-doc-uri)">
                            <dct:relation>
                                <rdf:Description> 
                                    <xsl:attribute name="rdf:about">http://papyri.info/ddbdp/<xsl:value-of select="replace(normalize-unicode($ddb[1], 'NFD'), '[^.a-z0-9]', '')"/>;<xsl:value-of select="$ddb[2]"/>;<xsl:value-of select="encode-for-uri($ddb[3])"/>/source</xsl:attribute>
                                    <dct:relation rdf:resource="http://papyri.info/apis/{normalize-space(substring-after(@url, 'key='))}/source"/>
                                </rdf:Description>
                            </dct:relation>
                        </xsl:if>
                    </rdf:Description>
                </dct:relation>
            </xsl:for-each>
            <dct:source>
                <rdf:Description rdf:about="http://papyri.info/hgv/{//tei:publicationStmt/tei:idno[@type='filename']}/work">
                  <dct:bibliographicCitation><xsl:value-of select="$bibl/tei:title[@level='s']"/><xsl:if test="$bibl//tei:biblScope[@type='volume']"><xsl:text> </xsl:text><xsl:value-of select="$bibl//tei:biblScope[@type='volume']"/></xsl:if><xsl:if test="$bibl//tei:biblScope[@type='numbers']">, <xsl:value-of select="$bibl//tei:biblScope[@type='numbers']"/></xsl:if><xsl:for-each select="$bibl//tei:biblScope[@type!='volume' and @type!='numbers']"><xsl:text> </xsl:text><xsl:value-of select="."/></xsl:for-each></dct:bibliographicCitation>
                <xsl:for-each select="//tei:text/tei:body//tei:bibl[@type='publication'][@subtype='other']">
                    <dct:relation><xsl:value-of select="."/></dct:relation>
                </xsl:for-each>
                </rdf:Description>
            </dct:source>
            <rdfs:label><xsl:value-of select="$unicode-title"></xsl:value-of></rdfs:label>
          <foaf:page>
            <xsl:variable name="page">
              <xsl:choose>
                <xsl:when test="//tei:publicationStmt/tei:idno[@type='ddb-hybrid'] and doc-available($ddb-doc-uri)">http://papyri.info/ddbdp/<xsl:value-of select="replace(normalize-unicode($ddb[1], 'NFD'), '[^.a-z0-9]', '')"/>;<xsl:value-of select="$ddb[2]"/>;<xsl:value-of select="encode-for-uri($ddb[3])"/></xsl:when>
                <xsl:otherwise>http://papyri.info/hgv/<xsl:value-of select="//tei:publicationStmt/tei:idno[@type='filename']"/></xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <rdf:Description rdf:about="{$page}">
              <foaf:topic rdf:resource="{$id}"/>
            </rdf:Description>
          </foaf:page>
        </rdf:Description>
      <xsl:if test="(//tei:publicationStmt/tei:idno[@type='ddb-hybrid']) and doc-available($ddb-doc-uri)">
        <xsl:for-each select="//tei:msDesc/tei:history/tei:provenance[@type='located']/tei:p/tei:placeName[contains(@ref,'http://pleiades')]">
          <rdf:Description>
            <xsl:attribute name="rdf:about">http://papyri.info/ddbdp/<xsl:value-of select="replace(normalize-unicode($ddb[1], 'NFD'), '[^.a-z0-9]', '')"/>;<xsl:value-of select="$ddb[2]"/>;<xsl:value-of select="encode-for-uri($ddb[3])"/>/original</xsl:attribute>
            <xsl:variable name="label" select="string(.)"/>
            <xsl:for-each select="tokenize(@ref,'\s')">
              <xsl:if test="starts-with(.,'http://pleiades')">
                <lawd:foundAt>
                  <rdf:Description rdf:about="{.}#this">
                    <rdfs:label><xsl:value-of select="$label"/></rdfs:label>
                  </rdf:Description>
                </lawd:foundAt>
              </xsl:if>
            </xsl:for-each>
          </rdf:Description>
        </xsl:for-each>
      </xsl:if>
    </xsl:template>
    <xsl:template match="TEI.2"/>
</xsl:stylesheet>
