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
        <xsl:apply-templates select="t:teiHeader/t:profileDesc/t:textClass/t:keywords"
            mode="metadata"/>
        
        <!-- Culture -->
        <xsl:apply-templates
            select="t:teiHeader/t:profileDesc/t:textClass/t:keywords/t:term[@type = 'culture']"
            mode="metadata"/>
        
        <!-- Religion -->
        <xsl:apply-templates
            select="t:teiHeader/t:profileDesc/t:textClass/t:keywords/t:term[@type = 'religion']"
            mode="metadata"/>
        
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
</xsl:stylesheet>