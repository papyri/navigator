<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:t="http://www.tei-c.org/ns/1.0"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:output method="html"/>
    
    <xsl:template name="serialize-dclp-metadata">
        
        <!-- Title -->
        <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:titleStmt/t:title" mode="metadata"/>
        
        <!-- New Work -->
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'ancientEdition']/t:listBibl/t:bibl"
            mode="metadata"/>
        
        <!-- Reference Edition -->
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'referenceEdition']"
            mode="metadata"/>
        
        <!-- Fragments / Inv. Id-->
        <tr>
            <th class="rowheader">Fragments</th>
            <td>
                <xsl:value-of
                    select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno"
                />
            </td>
        </tr>

        <!-- Support / Dimensions -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:support"
            mode="metadata"/>
        
        <!-- Date -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate"
            mode="metadata"/>
        
        <!-- Provenance -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/(t:origPlace|t:p)"
            mode="metadata"/>
        
        <!-- Place Stored (Ancient) -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:provenance[@type = 'stored']/t:p"
            mode="metadata"/>
        
        <!-- Material -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:support/t:material"
            mode="metadata"/>

        <!-- Genre -->
        <xsl:call-template name="dclp-keywords">
            <xsl:with-param name="label">Genre</xsl:with-param>
        </xsl:call-template>
        
        <!-- Culture -->
        <xsl:call-template name="dclp-keywords">
            <xsl:with-param name="type">culture</xsl:with-param>
        </xsl:call-template>
        
        <!-- Religion -->
        <xsl:call-template name="dclp-keywords">
            <xsl:with-param name="type">religion</xsl:with-param>
        </xsl:call-template>
        
        <!-- Print Illustrations -->
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'illustrations'][.//t:bibl]"
            mode="metadata"/>
        
        <!-- Custodial Events -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:additional/t:adminInfo/t:custodialHist"
            mode="metadata"/>
        
        <!-- Author -->
        <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:titleStmt/t:author" mode="metadata"/>
        
        <!-- Summary -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:summary"
            mode="metadata"/>
        
        <!-- Publications -->
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition']"
            mode="metadata"/>
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'citations']"
            mode="metadata"/>
        
        <!-- Physical Desc. -->
        <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:p"
            mode="metadata"/>
        
        <!-- Condition (conservation|preservation)-->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:condition/t:ab"
            mode="metadata"/>
        
        <!-- Layout (lines|recto/verso) -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:layoutDesc/t:layout/t:ab"
            mode="metadata"/>
        
        <!-- Hands -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:handDesc/t:p"
            mode="metadata"/>
        
        <!-- Post-concordance BL Entries -->
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'corrections']"
            mode="metadata"/>
        
        <!-- Translations -->
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'translations']"
            mode="metadata"/>
        
        <!-- Language -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItemStruct/t:textLang"
            mode="metadata"/>
        
        <!-- Commentary -->
        <xsl:apply-templates select="t:text/t:body/t:div[@type = 'commentary']" mode="metadata"/>
        
        <!-- Notes (general|local|related) -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItemStruct/t:note"
            mode="metadata"/>
        
        <!-- Associated Names -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin[t:persName/@type = 'asn']"
            mode="metadata"/>
        
        <!-- Images -->
        <xsl:apply-templates select="t:text/t:body/t:div[@type = 'figure']" mode="metadata"/>
        
        <!-- Copyright and license -->
        <tr>
            <th class="rowheader">License</th>
            <td><a rel="license" href="http://creativecommons.org/licenses/by-nc/3.0/"><img
                        alt="Creative Commons License" style="border-width:0"
                        src="http://i.creativecommons.org/l/by-nc/3.0/80x15.png"/></a> This
                work is licensed under a <a rel="license"
                    href="http://creativecommons.org/licenses/by-nc/3.0/">Creative Commons
                    Attribution-NonCommercial 3.0 License</a>.</td>
        </tr>
        
    </xsl:template>
    
    <!-- DCLP-specific handling of keyword terms -->
    <xsl:template name="dclp-keywords">
        <xsl:param name="type"/>
        <xsl:param name="label" select="concat(upper-case(substring($type, 1, 1)), substring($type, 2))"/>
        <xsl:message>template dclp-keywords(type=<xsl:value-of select="$type"/>, label=<xsl:value-of select="$label"/>)</xsl:message>
        <xsl:variable name="terms">
            <xsl:choose>
                <xsl:when test="$type=''">
                    <xsl:sequence select="t:teiHeader/t:profileDesc/t:textClass/t:keywords/t:term[not(@type)]"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="t:teiHeader/t:profileDesc/t:textClass/t:keywords/t:term[@type=$type]"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:message>$terms: <xsl:value-of select="$terms"/></xsl:message>
        <xsl:message>count($terms/t:term): <xsl:value-of select="count($terms/t:term)"/></xsl:message>
        <xsl:if test="count($terms/t:term) &gt; 0">
            <tr>
                <th class="rowheader"><xsl:value-of select="$label"/></th>
                <td><xsl:for-each select="$terms/t:term"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each></td>
            </tr>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>