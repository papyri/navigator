<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:bibo="http://purl.org/ontology/bibo/"
    xmlns:gawd="http://gawd.atlantides.org/terms/"
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
            <dcterms:identifier>papyri.info/hgv/<xsl:value-of select="//tei:publicationStmt/tei:idno[@type='filename']"/></dcterms:identifier>
            <xsl:for-each select="//tei:publicationStmt/tei:idno[@type='TM']">
                <dcterms:identifier>tm:<xsl:value-of select="."/></dcterms:identifier>
            </xsl:for-each>
            <dcterms:identifier>
                <rdf:Description>
                    <xsl:attribute name="rdf:about"><xsl:value-of select="$cite_uri"/></xsl:attribute>
                    <dcterms:identifier rdf:resource="{$id}"/>
                </rdf:Description>
            </dcterms:identifier>
          <!--
            <dcterms:references>
                <xsl:attribute name="rdf:resource">http://papyri.info/navigator/full/hgv_<xsl:value-of select="encode-for-uri($bibl/tei:title[@level='s'])"/>_<xsl:value-of select="$bibl//tei:biblScope[@type='volume'][not(matches(., '\s+'))]"/>:<xsl:value-of select="encode-for-uri($bibl//tei:biblScope[@type='numbers'])"/><xsl:for-each select="$bibl//tei:biblScope[@type='parts']">:<xsl:value-of select="replace(encode-for-uri(.), '%', '%25')"/></xsl:for-each></xsl:attribute>
            </dcterms:references>
          -->
            <dcterms:isPartOf>
                <xsl:choose>
                  <xsl:when test="$bibl//tei:biblScope[@type='volume'][not(matches(., '\s+'))]">
                        <rdf:Description rdf:about="http://papyri.info/hgv/{$title}_{normalize-space($bibl//tei:biblScope[@type='volume'])}">
                          <dcterms:bibliographicCitation><xsl:value-of select="$bibl/tei:title[@level='s']"/><xsl:text> </xsl:text><xsl:value-of select="$bibl//tei:biblScope[@type='volume']"/></dcterms:bibliographicCitation>
                          <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
                            <dcterms:isPartOf>
                                <rdf:Description rdf:about="http://papyri.info/hgv/{$title}">
                                  <rdf:type rdf:resource="http://purl.org/ontology/bibo/Series"/>
                                  <dcterms:bibliographicCitation><xsl:value-of select="$bibl/tei:title[@level='s']"/></dcterms:bibliographicCitation>
                                   <dcterms:isPartOf rdf:resource="http://papyri.info/hgv"/>
                                </rdf:Description>
                            </dcterms:isPartOf>
                        </rdf:Description>
                    </xsl:when>
                    <xsl:otherwise>
                        <rdf:Description rdf:about="http://papyri.info/hgv/{$title}">
                          <dcterms:bibliographicCitation><xsl:value-of select="$bibl/tei:title[@level='s']"/></dcterms:bibliographicCitation>
                          <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
                          <dcterms:isPartOf rdf:resource="http://papyri.info/hgv"/>
                        </rdf:Description>
                    </xsl:otherwise>
                </xsl:choose>
            </dcterms:isPartOf>
            <xsl:if test="(//tei:publicationStmt/tei:idno[@type='ddb-hybrid']) and doc-available($ddb-doc-uri)">
                <dcterms:relation>
                    <rdf:Description rdf:about="http://papyri.info/ddbdp/{replace(normalize-unicode($ddb[1], 'NFD'), '[^.a-z0-9]', '')};{$ddb[2]};{encode-for-uri($ddb[3])}/source">
                        <dcterms:relation rdf:resource="{$id}"/>
                    </rdf:Description>
                </dcterms:relation>
            </xsl:if>
          <xsl:for-each select="//tei:idno[@type = 'TM']">
            <dcterms:relation rdf:resource="http://www.trismegistos.org/text/{.}"/>
            <dcterms:relation>
              <rdf:Description rdf:about="http://papyri.info/trismegistos/{.}">
                <dcterms:relation rdf:resource="{$id}"/>
              </rdf:Description>
            </dcterms:relation>
          </xsl:for-each>
            <xsl:for-each select="//tei:text/tei:body/tei:div[@type='figure']//tei:graphic[contains(@url, 'columbia.edu') and contains(@url, 'key')]">
                <dcterms:relation>
                    <rdf:Description rdf:about="http://papyri.info/apis/{normalize-space(substring-after(@url, 'key='))}/source">
                        <dcterms:relation rdf:resource="{$id}"/>
                      <xsl:if test="//tei:publicationStmt/tei:idno[@type='ddb-hybrid'] and doc-available($ddb-doc-uri)">
                            <dcterms:relation>
                                <rdf:Description rdf:about="http://papyri.info/ddbdp/{replace(normalize-unicode($ddb[1], 'NFD'), '[^.a-z0-9]', '')};{$ddb[2]};{encode-for-uri($ddb[3])}/source">
                                    <dcterms:relation rdf:resource="http://papyri.info/apis/{normalize-space(substring-after(@url, 'key='))}/source"/>
                                </rdf:Description>
                            </dcterms:relation>
                        </xsl:if>
                    </rdf:Description>
                </dcterms:relation>
            </xsl:for-each>
            <dcterms:source>
                <rdf:Description rdf:about="http://papyri.info/hgv/{//tei:publicationStmt/tei:idno[@type='filename']}/frbr:Work">
                  <dcterms:bibliographicCitation><xsl:value-of select="$bibl/tei:title[@level='s']"/><xsl:if test="$bibl//tei:biblScope[@type='volume']"><xsl:text> </xsl:text><xsl:value-of select="$bibl//tei:biblScope[@type='volume']"/></xsl:if><xsl:if test="$bibl//tei:biblScope[@type='numbers']">, <xsl:value-of select="$bibl//tei:biblScope[@type='numbers']"/></xsl:if><xsl:for-each select="$bibl//tei:biblScope[@type!='volume' and @type!='numbers']"><xsl:text> </xsl:text><xsl:value-of select="."/></xsl:for-each></dcterms:bibliographicCitation>
                <xsl:for-each select="//tei:text/tei:body//tei:bibl[@type='publication'][@subtype='other']">
                    <dcterms:relation><xsl:value-of select="."/></dcterms:relation>
                </xsl:for-each>
                </rdf:Description>
            </dcterms:source>
            <rdfs:label><xsl:value-of select="$unicode-title"></xsl:value-of></rdfs:label>
        </rdf:Description>
      <xsl:if test="(//tei:publicationStmt/tei:idno[@type='ddb-hybrid']) and doc-available($ddb-doc-uri)">
        <xsl:for-each select="//tei:msDesc/tei:history/tei:provenance[@type='located']/tei:p/tei:placeName[contains(@ref,'http://pleiades')]">
          <rdf:Description rdf:about="http://papyri.info/ddbdp/{replace(normalize-unicode($ddb[1], 'NFD'), '[^.a-z0-9]', '')};{$ddb[2]};{encode-for-uri($ddb[3])}/original">
            <xsl:variable name="label" select="string(.)"/>
            <xsl:for-each select="tokenize(@ref,'\s')">
              <xsl:if test="starts-with(.,'http://pleiades')">
                <gawd:findspot>
                  <rdf:Description rdf:about="{.}#this">
                    <rdfs:label><xsl:value-of select="$label"/></rdfs:label>
                  </rdf:Description>
                </gawd:findspot>
              </xsl:if>
            </xsl:for-each>
          </rdf:Description>
        </xsl:for-each>
      </xsl:if>
    </xsl:template>
    <xsl:template match="TEI.2"/>
</xsl:stylesheet>
