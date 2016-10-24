<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:t="http://www.tei-c.org/ns/1.0"
    xmlns:pi="http://papyri.info/ns"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:output method="html"/>
    
    <xsl:template name="serialize-dclp-metadata">
        
        <!-- New Work -->
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'ancientEdition']/t:listBibl/t:bibl"
            mode="metadata"/>
        
        <!-- Principal Edition bibliographic division (addresses all subtypes) -->
        <xsl:message>selecting principalEdition div</xsl:message>
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition']"
            mode="metadata-dclp"/>
        
        <!-- Fragments / Inv. Id-->
        <tr>
            <th class="rowheader">Fragments</th>
            <td>
                <xsl:for-each select="//t:msIdentifier/descendant::t:idno[@type='invNo']">
                    <xsl:value-of select="."/>
                    <xsl:if test="position() != last()">
                        <xsl:choose>
                            <xsl:when test="substring(., string-length(.), 1) = ';'">
                                <xsl:text> </xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>; </xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </xsl:for-each>
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

        <!-- Form and Layout -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc"
            mode="dclp-metadata-form"/>
        
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
        <!-- Now display at the top of each column - see htm-teidivedition.xsl -->
        <!--<xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:additional/t:adminInfo/t:custodialHist"
            mode="metadata"/>-->

        <!-- Physical Description -->
        <!--<xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc"
            mode="metadata"/> -->
        
        <!-- Images -->
        <xsl:apply-templates select="t:text/t:body/t:div[@type = 'figure']" mode="metadata"/>
        
        <!-- Intellectual Property and License -->
        <xsl:choose>
            <xsl:when test="//t:publicationStmt/t:availability">
                <xsl:apply-templates select="//t:publicationStmt/t:availability" mode="metadata"/>
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <th class="rowheader">Availability</th>
                    <td>The source data for this page does not contain any information concerning its copyright, license, or availability. It should be considered "all rights reserved" until proven otherwise.</td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>        
    </xsl:template>
    
    <!-- DCLP-specific handling of keyword terms -->
    <xsl:template name="dclp-keywords">
        <xsl:param name="type"/>
        <xsl:param name="label" select="concat(upper-case(substring($type, 1, 1)), substring($type, 2))"/>
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
        <xsl:if test="count($terms/t:term) &gt; 0">
            <tr>
                <th class="rowheader"><xsl:value-of select="$label"/></th>
                <td><xsl:for-each select="$terms/t:term"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each></td>
            </tr>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="t:physDesc" mode="dclp-metadata-form">
        <tr>
            <th class="rowheader">Form and Layout</th>
            <td>
				<xsl:choose>
					<xsl:when test="t:objectDesc/t:p[@type='bookForm']">
						<xsl:value-of select="t:objectDesc/t:p"/>
					</xsl:when>	
				</xsl:choose>		
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="t:div[@type = 'bibliography' and @subtype =  'principalEdition']" mode="metadata-dclp">
        <xsl:message>matched principalEdition div</xsl:message>
        <xsl:for-each select="t:listBibl/t:bibl[@type='publication' and @subtype='principal']">
            <xsl:call-template name="dclp-bibliography">
                <xsl:with-param name="heading">Principal Edition</xsl:with-param>
                <xsl:with-param name="references" select="."/>
                <xsl:with-param name="treat-as-structured">no</xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each-group select="t:listBibl/t:bibl[@type='reference']"  group-by="@subtype">
            <xsl:call-template name="dclp-bibliography">
                <xsl:with-param name="heading">
                    <xsl:choose>
                        <xsl:when test="@subtype='principal'">
                            <xsl:text>Reference Edition</xsl:text>
                        </xsl:when>
                        <xsl:when test="@subtype='partial'">
                            <xsl:text>Partial Edition</xsl:text>
                        </xsl:when>
                        <xsl:when test="@subtype='previous'">
                            <xsl:text>Previous Edition</xsl:text>
                        </xsl:when>
                        <xsl:when test="@subtype='readings'">
                            <xsl:text>Readings</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@subtype"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="references" select="current-group()"/>
                <xsl:with-param name="treat-as-structured">yes</xsl:with-param>
            </xsl:call-template>
        </xsl:for-each-group>
        <!--
        <xsl:for-each select="t:listBibl/t:bibl">
            <xsl:message>bibl!</xsl:message>
                <xsl:variable name="biblio-header">
                    <xsl:choose>
                        <xsl:when test="@type='publication' and @subtype='principal'">
                            <xsl:text>Principal Edition</xsl:text>
                        </xsl:when>
                        <xsl:when test="@type='reference'">
                            <xsl:choose>
                                <xsl:when test="@subtype='principal'">
                                    <xsl:text>Reference Edition</xsl:text>
                                </xsl:when>
                                <xsl:when test="@subtype='partial'">
                                    <xsl:text>Partial Edition</xsl:text>
                                </xsl:when>
                                <xsl:when test="@subtype='previous'">
                                    <xsl:text>Previous Edition</xsl:text>
                                </xsl:when>
                                <xsl:when test="@subtype='readings'">
                                    <xsl:text>Readings</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:message>WARNING: untrapped bibliographic subtype="<xsl:value-of select="@subtype"/>"</xsl:message>
                                    <xsl:text>WARNING: untrapped bibliographic subtype="</xsl:text>
                                    <xsl:value-of select="@subtype"/>
                                    <xsl:text>"</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:message>WARNING: untrapped bibliographic type+subtype combination: type="<xsl:value-of select="@type"/>" subtype="<xsl:value-of select="@subtype"/>"</xsl:message>
                            <xsl:text>WARNING: untrapped bibliographic type+subtype combination: type="</xsl:text>
                            <xsl:value-of select="@type"/>
                            <xsl:text>" subtype="</xsl:text>
                            <xsl:value-of select="@subtype"/>
                            <xsl:text>"</xsl:text>
                        </xsl:otherwise>            
                    </xsl:choose>
                </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="@type='publication' and @subtype = 'principal'">
                            <xsl:call-template name="dclp-bibliography">
                                <xsl:with-param name="heading" select="$biblio-header"/>
                                <xsl:with-param name="references" select="."/>
                                <xsl:with-param name="treat-as-structured">no</xsl:with-param>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="dclp-bibliography">
                                <xsl:with-param name="heading" select="$biblio-header"/>
                                <xsl:with-param name="references" select="."/>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
        </xsl:for-each> -->
    </xsl:template>
    
    <xsl:template name="dclp-bibliography">
        <xsl:param name="heading"/>
        <xsl:param name="references"/>
        <xsl:param name="treat-as-structured">yes</xsl:param>
        <xsl:message>in named template dclp-bibliography</xsl:message>
        <xsl:for-each select="$references">
            <tr>
                <th><xsl:value-of select="$heading"/></th>
                <td>
                    <xsl:choose>
                        <xsl:when test="$treat-as-structured='no'">
                            <xsl:variable name="bibl-plain">
                                <xsl:value-of select="."/>
                            </xsl:variable>
                            <xsl:value-of select="normalize-space($bibl-plain)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="t:ptr | t:ref">
                                    <xsl:message>found ptr or ref inside bibl"</xsl:message>
                                    <xsl:message>trying bibliographic file lookup...</xsl:message>
                                    <xsl:variable name="biblio-target" >
                                        <xsl:choose>
                                            <xsl:when test="t:ptr">
                                                <xsl:value-of select="concat(t:ptr[0]/@target, '/source')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="concat(t:ref[0]/@target, '/source')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:variable>
                                    <xsl:message>biblio-target is: "<xsl:value-of select="$biblio-target"/></xsl:message>
                                    <xsl:variable name="biblio-filename" select="pi:get-filename($biblio-target, 'xml')"/>
                                    <xsl:message>local filesystem biblio-filename should be "<xsl:value-of select="$biblio-filename"/>"</xsl:message>
                                    <xsl:choose>
                                        <xsl:when test="doc-available($biblio-filename)">
                                            <xsl:message>local file is available!</xsl:message>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:message>ERROR: local file "<xsl:value-of select="$biblio-filename"/>" is not available.</xsl:message>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:for-each select="pi:get-docs(concat(t:ptr/@target, '/source'), 'xml')/t:bibl">
                                        <xsl:message>... success with bibliographic lookup!</xsl:message>
                                        <xsl:message>building citation</xsl:message>
                                        <xsl:call-template name="buildCitation"/>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:message>building citation with what we have in the source file</xsl:message>
                                    <xsl:call-template name="buildCitation"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </xsl:for-each>
    </xsl:template>
    
    <!-- Bibliography within dclp div@type=commentary -->
    <xsl:template match="t:listBibl[$collection='dclp' and ancestor::t:div[@type='commentary']]">
        <xsl:for-each select="t:bibl">
            <xsl:choose>
                <xsl:when test="t:ref">
                    <xsl:apply-templates />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="buildCitation"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="./following-sibling::t:bibl">; </xsl:if>
        </xsl:for-each>
    </xsl:template>
   
        
</xsl:stylesheet>