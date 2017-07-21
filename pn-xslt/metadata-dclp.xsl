<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:t="http://www.tei-c.org/ns/1.0"
    xmlns:pi="http://papyri.info/ns"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <xsl:output method="html"/>
    
    <xsl:template name="serialize-dclp-metadata">
        <!-- Title  -->
        <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:titleStmt" mode="metadata"/>
        
        <!-- New Work -->
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'ancientEdition']/t:listBibl"
            mode="metadata"/>
        
        <!-- Content overview -->
        <xsl:apply-templates
            select="//t:profileDesc/t:textClass/t:keywords/t:term[@type='overview']" 
            mode="metadata"/>
        
        <!-- Principal Edition bibliographic division (addresses all subtypes) -->
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'principalEdition']"
            mode="metadata-dclp"/>
        
        <!-- Catalog(s)/MP3 number -->
        <xsl:if test="t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='MP3']">
            <tr>
                <th class="rowheader">Catalog(s)</th>
                <td>
                    <xsl:for-each select="t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='MP3']">
                        <xsl:value-of select="concat('MP3 ',.)"/>
                        <xsl:if test="position() != last()"><xsl:text>; </xsl:text></xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>

        <!-- Archive <collection type="ancient"> -->
        <xsl:if test="//t:msIdentifier/t:collection[@type='ancient']">
            <tr>
                <th class="rowheader">Archive</th>
                <td>
                    <xsl:for-each select="//t:msIdentifier/t:collection[@type='ancient']">
                        <xsl:choose>
                            <xsl:when test="@ref">
                                <a href="{@ref}"><xsl:value-of select="."/></a>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
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
        </xsl:if>

        <!-- Fragments / Inv. Id-->
        <tr>
            <th class="rowheader">Fragments</th>
            <td>
                <!-- Show first 10 fragments -->
                <xsl:for-each select="//t:msIdentifier/descendant::t:idno[@type='invNo'][position() &lt; 10]">
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
                <!-- If greater then 10 fragments use jquery toggle function to show/hide fragments above 10 -->
                <xsl:if test="count(//t:msIdentifier/descendant::t:idno[@type='invNo']) &gt; 10">
                    <span class="fragmentsMetadata" style="display:none;">
                        <xsl:for-each select="//t:msIdentifier/descendant::t:idno[@type='invNo'][position() &gt; 9]">
                            <xsl:if test="position() = 1"><xsl:text>; </xsl:text></xsl:if>
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
                    </span>
                    <span class="fragmentsMetadata">...</span>
                    <input type="button" class="toggleFragments" 
                        style="margin-left:.5em; 
                        background-color: transparent; 
                        text-decoration: underline; 
                        border: none; font-size: 
                        small; color: #162A5C; 
                        cursor: pointer;" value="[Show All Fragments]"/>
                    <!-- Javascript to toggle fragment view -->
                    <script type="text/javascript">
                        $('.toggleFragments').click(function(){
                            $(this).siblings('span.fragmentsMetadata').toggle();
                            $(this).val( $(this).val() == '[Fewer Fragments]' ? '[Show All Fragments]' : '[Fewer Fragments]' );    
                         });   
                    </script>  
              </xsl:if> 
            </td>
        </tr>

        <!-- Support:  Dimensions -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:support"
            mode="dclp-metadata-form"/>
        
        <!-- Date -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate"
            mode="metadata"/>
        
        <!-- Provenance -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/(t:origPlace|t:p)"
            mode="metadata"/>
        <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:provenance[not(@type = 'stored')]" mode="metadata"/>
        
        <!-- Place Stored (Ancient) -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:provenance[@type = 'stored']/t:p"
            mode="metadata"/>

        <!-- Form and Layout -->
        <xsl:apply-templates
            select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc"
            mode="dclp-metadata-form"/>
        
        <!-- Script Type -->
        <xsl:if test="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:handDesc">
            <tr>
                <th class="rowheader">Script Type</th>
                <td>
                    <xsl:for-each select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:handDesc">
                        <xsl:apply-templates select="t:p/node()"/>
                        <xsl:if test="position() != last()"><xsl:text>; </xsl:text></xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
        
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
        
        <!-- General Notes -->
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'commentary' and @subtype = 'general']"
            mode="metadata-dclp"/>
        
        <!-- Externally Published Illustrations -->
        <xsl:apply-templates
            select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'illustrations']"
            mode="metadata-dclp"/>
                
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
                <xsl:when test="$type='overview'"/> <!-- suppress overview because we handle in "content" row -->
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
    
    <!-- Template for title -->
    <!-- t:teiHeader/t:fileDesc/t:titleStmt/t:title -->
    <xsl:template match="t:titleStmt" mode="metadata-dclp">
        <tr>
            <th class="rowheader">Title</th>
            <td><xsl:apply-templates select="t:title"/></td>
        </tr>
    </xsl:template>
    
    <xsl:template match="t:physDesc" mode="dclp-metadata-form">
        <tr>
            <th class="rowheader">Form and Layout</th>
            <td>
                <xsl:for-each select="t:objectDesc">
                    <xsl:variable name="form" select="@form"/>
                    <xsl:variable name="material" select="t:supportDesc/t:support/t:material" />
                    <xsl:variable name="layout" select="t:layoutDesc/t:layout/t:p" />
                    <xsl:if test="$material != '' and not(contains($layout, $material))">
                        <xsl:value-of select="$material"/>
                        <xsl:text> </xsl:text>
                    </xsl:if>
                    <xsl:if test="$form != '' and not(contains($layout, $form))">
                        <xsl:value-of select="$form"/>
                        <xsl:text>: </xsl:text>
                    </xsl:if>
                    <xsl:for-each select="$layout">
                        <xsl:apply-templates/>
                    </xsl:for-each>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
    
    <!-- handle principal edition bibliography -->
    <xsl:template match="t:div[@type = 'bibliography' and @subtype =  'principalEdition']" mode="metadata-dclp">
        <xsl:for-each-group select="t:listBibl/t:bibl[@type='publication' and @subtype='principal']" group-by="@subtype">
            <tr>
                <th>Principal Edition</th>
                <td>
                    <xsl:choose>
                        <xsl:when test="count(current-group()) &gt; 1">
                            <ul class="biblList">
                                <xsl:for-each select="current-group()">
                                    <xsl:sort select="t:date"/>
                                    <li><xsl:text> - </xsl:text>
                                        <xsl:call-template name="dclp-bibliography">
                                            <xsl:with-param name="references" select="."/>    
                                        </xsl:call-template>    
                                    </li>
                                </xsl:for-each>                            
                            </ul>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:for-each select="current-group()">
                                <xsl:sort select="t:date"/>
                                <xsl:call-template name="dclp-bibliography">
                                    <xsl:with-param name="references" select="."/>    
                                </xsl:call-template>        
                            </xsl:for-each>                            
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </xsl:for-each-group>
        <!-- Sort and group t:bibl/@type reference. use pi:bibl-type-order() function to force correct sort order. See pi-functions.xsl for order. -->
        <xsl:for-each-group select="t:listBibl/t:bibl[@type='reference']"  group-by="@subtype">
            <xsl:sort select="pi:bibl-type-order(current-grouping-key())" order="ascending"/>
            <tr>
                <th>
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
                </th>
                <td>
                    <xsl:choose>
                        <xsl:when test="count(current-group()) &gt; 1">
                            <ul class="biblList">
                                <xsl:for-each select="current-group()">
                                    <xsl:sort select="t:date"/>
                                    <li><xsl:text> - </xsl:text> 
                                        <xsl:call-template name="dclp-bibliography">
                                            <xsl:with-param name="references" select="."/>    
                                        </xsl:call-template>    
                                    </li>
                                </xsl:for-each>                            
                            </ul>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:for-each select="current-group()">
                                <xsl:sort select="t:date"/>
                                <xsl:call-template name="dclp-bibliography">
                                    <xsl:with-param name="references" select="."/>    
                                </xsl:call-template>        
                            </xsl:for-each>                            
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </xsl:for-each-group>
    </xsl:template>    
    <xsl:template name="dclp-get-biblio-passthrough">
        <xsl:param name="references"/>
        <xsl:choose>
            <xsl:when test="child::comment()[contains(.,'ignore')]">
                <xsl:for-each select="child::*[not(self::t:title) and not(self::t:ptr) and not(self::t:ref)][following-sibling::comment()[contains(.,'ignore - start')] or preceding-sibling::comment()[contains(.,'ignore - stop')]]">
                    <xsl:apply-templates/>
                    <xsl:if test="position() != last()">
                        <xsl:text> </xsl:text>
                    </xsl:if>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="$references/child::*[not(self::t:title) and not(self::t:ptr) and not(self::t:ref)]">
                    <xsl:apply-templates/>
                    <xsl:if test="position() != last()">
                        <xsl:text> </xsl:text>
                    </xsl:if>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="dclp-biblio-principal-dereference">
        <xsl:param name="passThrough"/>
        <xsl:param name="type"/>
        <xsl:choose>
            <xsl:when test="t:ptr | t:ref">
                <xsl:variable name="biblio-target" >
                    <xsl:choose>
                        <xsl:when test="t:ptr">
                            <xsl:value-of select="concat(t:ptr[1]/@target, '/source')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat(t:ref[1]/@target, '/source')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="biblio-filename" select="pi:get-filename($biblio-target, 'xml')"/>
                <xsl:choose>
                    <xsl:when test="doc-available($biblio-filename)">
                        <xsl:variable name="biblio-doc" select="pi:get-docs($biblio-target, 'xml')"/>
                        <xsl:for-each select="$biblio-doc/t:bibl">
                            <xsl:call-template name="buildCitation"><xsl:with-param name="biblType" select="$type"/></xsl:call-template>
                        </xsl:for-each>
                        <xsl:value-of select="$passThrough"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:message>ERROR (<xsl:value-of select="//t:idno[@type='filename']"/>): local file "<xsl:value-of select="$biblio-filename"/>" is not available.</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="buildCitation"><xsl:with-param name="biblType" select="$type"/></xsl:call-template>
                <xsl:value-of select="$passThrough"/>
            </xsl:otherwise>
        </xsl:choose>    </xsl:template>
    <xsl:template name="dclp-bibliography">
        <xsl:param name="heading"/>
        <xsl:param name="references"/>
        <xsl:for-each select="$references">
            <xsl:variable name="type">
                <xsl:choose>
                    <xsl:when test="@type='publication' and @subtype='principal'">principalEdition</xsl:when>
                    <xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="passThrough">
                <xsl:call-template name="dclp-get-biblio-passthrough">
                    <xsl:with-param name="references" select="$references"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test=".[@subtype='principal'] and ancestor::t:div[@type='bibliography'][@subtype='principalEdition']">
                    <xsl:call-template name="dclp-biblio-principal-dereference">
                        <xsl:with-param name="passThrough" select="$passThrough"/>
                        <xsl:with-param name="type" select="$type"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="buildCitation"><xsl:with-param name="biblType" select="$type"/></xsl:call-template>
                    <xsl:value-of select="$passThrough"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <!-- Handle General notes   -->
    <xsl:template match="t:div[@type = 'commentary' and @subtype='general']" mode="metadata-dclp">
        <tr>
            <th class="rowheader">General Notes</th>
            <td><xsl:apply-templates select="t:p/node()"/></td>
        </tr>
    </xsl:template>
    
    <!-- handle external illustrations bibliography and web links -->
    <xsl:template match="t:div[@type = 'bibliography' and @subtype='illustrations']" mode="metadata-dclp">
        <!-- images -->
        <xsl:if test=".//t:bibl[@type='online']">
            <tr>
                <th class="rowheader">Images</th>
                <td>
                    <xsl:for-each select=".//t:bibl[@type = 'online']">
                        <xsl:choose>
                            <xsl:when test="@type='online' and t:ptr and starts-with(t:ptr/@target, 'http')">
                                <xsl:variable name="url-chunks" select="tokenize(substring-after(t:ptr/@target, '://'), '/')"/>
                                <xsl:variable name="text-from-nodes">
                                    <xsl:for-each select="./text()">
                                        <xsl:value-of select="."/>
                                    </xsl:for-each>
                                </xsl:variable>
                                <xsl:variable name="link-text">
                                    <xsl:choose>
                                        
                                        <!-- if bibl has a title (apparently not common in this context) -->
                                        <xsl:when test="t:title">
                                            <xsl:value-of select="normalize-space(t:title[1])"/>
                                        </xsl:when>
                                        
                                        <!-- if there's free text inside the bibl (also apparently not common in this context) -->
                                        <xsl:when test="normalize-space($text-from-nodes) != ''">
                                            <xsl:value-of select="normalize-space($text-from-nodes)"/>
                                        </xsl:when>
                                        
                                        <!-- otherwise try to make the URL into something that doesn't eat up too much space as a text node for the link -->
                                        <xsl:otherwise>
                                            
                                            <!-- try to isolate domain from url for first part of link -->
                                            <xsl:choose>
                                                <xsl:when test="contains($url-chunks[1], ':') and starts-with($url-chunks[1], 'www.')">
                                                    <xsl:value-of select="substring-after(substring-before($url-chunks[1], ':'), 'www.')"/>
                                                </xsl:when>
                                                <xsl:when test="contains($url-chunks[1], ':')">
                                                    <xsl:value-of select="substring-before($url-chunks[1], ':')"/>
                                                </xsl:when>
                                                <xsl:when test="starts-with($url-chunks[1], 'www.')">
                                                    <xsl:value-of select="substring-after($url-chunks[1], 'www.')"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$url-chunks[1]"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            
                                            <!-- delimiter, using ellipsis if necessary -->
                                            <xsl:choose>
                                                <xsl:when test="count($url-chunks) &gt; 2">
                                                    <xsl:text>/.../</xsl:text>
                                                </xsl:when>
                                                <xsl:otherwise>/</xsl:otherwise>
                                            </xsl:choose>
                                            
                                            <!-- try to clean up whatever the last bit is -->
                                            <xsl:variable name="last-chunk">
                                                <xsl:choose>
                                                    <xsl:when test="$url-chunks[last()] = ''">
                                                        <xsl:value-of select="$url-chunks[last()-1]"/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="$url-chunks[last()]"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:variable>
                                            <xsl:variable name="last-chunk-identity">
                                                <xsl:choose>
                                                    <xsl:when test="contains($last-chunk, '#')">
                                                        <xsl:value-of select="substring-after($last-chunk, '#')"/>
                                                    </xsl:when>
                                                    <xsl:when test="contains($last-chunk, 'id=')">
                                                        <xsl:variable name="id-raw" select="substring-after($last-chunk, 'id=')"/>
                                                        <xsl:choose>
                                                            <xsl:when test="contains($id-raw, '&amp;')">
                                                                <xsl:value-of select="substring-after($id-raw, '&amp;')"/>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <xsl:value-of select="$id-raw"/>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </xsl:when>
                                                    <xsl:when test="contains($last-chunk, 'Inv.%20Nr.=')">
                                                        <xsl:variable name="id-raw" select="substring-after($last-chunk, 'Inv.%20Nr.=')"/>
                                                        <xsl:choose>
                                                            <xsl:when test="contains($id-raw, '&amp;')">
                                                                <xsl:value-of select="substring-after($id-raw, '&amp;')"/>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <xsl:value-of select="$id-raw"/>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </xsl:when>
                                                    <xsl:when test="contains($last-chunk, '=')">
                                                        <xsl:value-of select="tokenize($last-chunk, '=')[last()]"/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:choose>
                                                            <xsl:when test="contains($last-chunk, '&amp;')">
                                                                <xsl:value-of select="substring-before($last-chunk, '&amp;')"/>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <xsl:value-of select="$last-chunk"/>
                                                            </xsl:otherwise>
                                                        </xsl:choose>                                                        
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:variable>
                                            <xsl:variable name="last-chunk-complete">
                                                <xsl:choose>
                                                    <xsl:when test="contains($last-chunk-identity, '?')">
                                                        <xsl:value-of select="substring-after($last-chunk-identity, '?')"/>
                                                    </xsl:when>
                                                    <xsl:when test="contains($last-chunk-identity, ';')">
                                                        <xsl:value-of select="substring-before($last-chunk-identity, ';')"/>
                                                    </xsl:when>
                                                    <xsl:when test="contains($last-chunk-identity, '.htm')">
                                                        <xsl:value-of select="substring-before($last-chunk-identity, '.htm')"/>
                                                    </xsl:when>
                                                    <xsl:when test="contains($last-chunk-identity, '.tif')">
                                                        <xsl:value-of select="substring-before($last-chunk-identity, '.tif')"/>
                                                    </xsl:when>
                                                    <xsl:when test="contains($last-chunk-identity, '.TIF')">
                                                        <xsl:value-of select="substring-before($last-chunk-identity, '.TIF')"/>
                                                    </xsl:when>
                                                    <xsl:when test="contains($last-chunk-identity, '.jpg')">
                                                        <xsl:value-of select="substring-before($last-chunk-identity, '.jpg')"/>
                                                    </xsl:when>
                                                    <xsl:when test="contains($last-chunk-identity, '.JPG')">
                                                        <xsl:value-of select="substring-before($last-chunk-identity, '.JPG')"/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="$last-chunk-identity"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:variable>
                                            <xsl:value-of select="$last-chunk-complete"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:variable>
                                <a href="{t:ptr/@target}">
                                    <xsl:value-of select="$link-text"/>
                                </a>
                            </xsl:when>
                            <xsl:when test="@type='online' and t:ptr and not(starts-with(t:ptr/@target, 'http'))">
                                <xsl:message>ERROR  (<xsl:value-of select="//t:idno[@type='filename']"/>): invalid ptr target: URL has no protocol prefix: <xsl:value-of select="t:ptr/@target"/></xsl:message>
                                <xsl:value-of select="t:ptr/@target"/>
                            </xsl:when>
                        </xsl:choose>
                        <xsl:if test="./following-sibling::t:bibl[@type = 'online']">; </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>            
        </xsl:if>

        <!-- print illustrations -->
        <xsl:if test=".//t:bibl[@type != 'online']">
            <tr>
                <th class="rowheader">Print Illustrations</th>
                <td>
                    <xsl:for-each select=".//t:bibl[@type != 'online']">
                        <xsl:value-of select="."/>
                        <xsl:if test="./following-sibling::t:bibl[@type != 'online']">; </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
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
    
    <!-- DCLP handling of support material and dimensions -->
    <xsl:template match="t:support" mode="dclp-metadata-form">
        <xsl:apply-templates mode="dclp-metadata-form"/>
    </xsl:template>
    
    <xsl:template match="t:material" mode="dclp-metadata-form">
        <tr>
            <th class="rowheader">Support Material</th>
            <td><xsl:value-of select="."/></td>
        </tr>            
    </xsl:template>
    
    <xsl:template match="t:dimensions[parent::t:support]" mode="dclp-metadata-form">
        <tr>
            <th class="rowheader">Support Dimensions</th>
            <td>
                <xsl:for-each select="t:*">
                    <xsl:variable name="extent">
                        <xsl:choose>
                            <xsl:when test="@extent">
                                <xsl:value-of select="@extent"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:value-of select="$extent"/>
                    <xsl:if test="@unit">
                        <xsl:if test="not(contains($extent, @unit))">
                            <xsl:value-of select="@unit"/>
                        </xsl:if>
                    </xsl:if>
                    <xsl:variable name="dim" select="local-name()"/>
                    <xsl:text> </xsl:text>
                    <xsl:choose>
                        <xsl:when test="$dim = 'width'">wide</xsl:when>
                        <xsl:when test="$dim = 'height'">high</xsl:when>
                        <xsl:when test="$dim = 'depth'">deep</xsl:when>
                        <xsl:when test="$dim = 'dim'">
                            <xsl:value-of select="@type"/>
                        </xsl:when>
                    </xsl:choose>
                    <xsl:if test="following-sibling::t:*">
                        <xsl:text> x </xsl:text>
                    </xsl:if>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
    
    
        
</xsl:stylesheet>
