<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dc="http://purl.org/dc/terms/" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:pi="http://papyri.info/ns"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:t="http://www.tei-c.org/ns/1.0"
  xmlns:xi="http://www.w3.org/2001/XInclude"
  xmlns:sl="http://www.w3.org/2005/sparql-results#"
  version="2.0" exclude-result-prefixes="#all">
  
  <xsl:import href="metadata-dclp.xsl"/>
  <xsl:output method="html"/>
  
  <xsl:template match="t:TEI" mode="metadata">
    <xsl:variable name="md-collection"><xsl:choose>
      <xsl:when test="//t:idno[@type='apisid']">apis</xsl:when>
      <xsl:when test="//t:idno[@type='dclp']">dclp</xsl:when>
      <xsl:otherwise>hgv</xsl:otherwise>
    </xsl:choose>
    </xsl:variable>
    <div class="metadata">
      <div class="{$md-collection} data">
        <xsl:choose>
          <xsl:when test="$md-collection = 'dclp'">
            <h2>DCLP/LDAB [<a class="xml" href="https://github.com/DCLP/idp.data/blob/dclp/DCLP/{number(substring(descendant::t:idno[@type='TM'],0,3))}/{/t:TEI/t:teiHeader/t:fileDesc/t:publicationStmt/t:idno[@type='filename']}.xml"
                target="_new">xml</a>]
            </h2>
          </xsl:when>
          <xsl:when test="$md-collection = 'hgv'">
            <h2>
              HGV: <xsl:value-of select="//t:bibl[@type = 'publication' and @subtype='principal']"/> [<a href="http://aquila.papy.uni-heidelberg.de/Hauptregister/FMPro?-db=hauptregister_&amp;TM_Nr.={//t:idno[@type = 'filename']}&amp;-format=DTableVw.htm&amp;-lay=Liste&amp;-find">source</a>] [<a class="xml" href="/hgv/{//t:idno[@type='filename']}/source" target="_new">xml</a>]
            </h2>
          </xsl:when>
          <xsl:otherwise>
            <h2>
              Catalog Record: <xsl:value-of select="//t:idno[@type='apisid']"/> [<a href="/apis/{//t:idno[@type='apisid']}/source">xml</a>] 
            </h2>
          </xsl:otherwise>
        </xsl:choose>
        <table class="metadata">
          <tbody>
            <xsl:choose>
              <xsl:when test="$md-collection = 'dclp'">
                <xsl:call-template name="serialize-dclp-metadata"/>
              </xsl:when>
              <xsl:otherwise>
                <!-- Title -->
                <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:titleStmt/t:title"
                  mode="metadata"/>
                <!-- Author -->
                <xsl:apply-templates select="t:teiHeader/t:fileDesc/t:titleStmt/t:author"
                  mode="metadata"/>
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
                <!-- Inv. Id -->
                <xsl:apply-templates
                  select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msIdentifier/t:idno"
                  mode="metadata"/>
                <!-- Physical Desc. -->
                <xsl:apply-templates
                  select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:p"
                  mode="metadata"/>
                <!-- Support / Dimensions -->
                <xsl:apply-templates
                  select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:support"
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
                <!-- Provenance -->
                <xsl:apply-templates
                  select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/(t:origPlace|t:p)"
                  mode="metadata"/>
                <!-- Material -->
                <xsl:apply-templates
                  select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:physDesc/t:objectDesc/t:supportDesc/t:support/t:material"
                  mode="metadata"/>
                <!-- Language -->
                <xsl:apply-templates
                  select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItemStruct/t:textLang"
                  mode="metadata"/>
                <!-- Date -->
                <xsl:apply-templates
                  select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/t:origDate"
                  mode="metadata"/>
                <!-- Commentary -->
                <xsl:apply-templates select="t:text/t:body/t:div[@type = 'commentary']"
                  mode="metadata"/>
                <!-- Notes (general|local|related) -->
                <xsl:apply-templates
                  select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:msContents/t:msItemStruct/t:note"
                  mode="metadata"/>
                <!-- Print Illustrations -->
                <xsl:apply-templates
                  select="t:text/t:body/t:div[@type = 'bibliography' and @subtype = 'illustrations'][.//t:bibl]"
                  mode="metadata"/>
                <!-- Subjects -->
                <xsl:apply-templates select="t:teiHeader/t:profileDesc/t:textClass/t:keywords"
                  mode="metadata"/>
                <!-- Associated Names -->
                <xsl:apply-templates
                  select="t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin[t:persName/@type = 'asn']"
                  mode="metadata"/>
                <!-- Images -->
                <xsl:apply-templates select="t:text/t:body/t:div[@type = 'figure']" mode="metadata"/>
                <xsl:choose>
                  <xsl:when test="$md-collection = 'hgv'">
                    <tr>
                      <th class="rowheader">License</th>
                      <td><a rel="license" href="http://creativecommons.org/licenses/by/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/80x15.png"/></a> 
                        © Heidelberger Gesamtverzeichnis der griechischen Papyrusurkunden Ägyptens.  This 
                        work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 License</a>.</td>
                    </tr>
                  </xsl:when>
                  <xsl:otherwise>
                    <tr>
                      <th class="rowheader">License</th>
                      <td><a rel="license" href="http://creativecommons.org/licenses/by-nc/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nc/3.0/80x15.png"/></a> This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc/3.0/">Creative Commons Attribution-NonCommercial 3.0 License</a>.</td>
                    </tr>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
          </tbody>
        </table>
      </div>
    </div>
  </xsl:template>
  
  <xsl:template match="text" mode="metadata">
    <div class="metadata">
      <div class="tm data">
        <h2>Trismegistos: <xsl:value-of select="field[@n='0']"/> [<a href="http://www.trismegistos.org/text/{field[@n='0']}">source</a>]</h2>
        <table class="metadata">
          <tbody>
            <!-- Publications -->
            <tr>
              <th>Publications</th>
              <td><xsl:value-of select="field[@n='6']"/><xsl:text> </xsl:text>
                <xsl:value-of select="replace(field[@n='8'],'&lt;br&gt;',' ')"/></td>
            </tr>
            <!-- Inventory Number -->
            <tr>
              <th>Inv. no.</th>
              <td><xsl:value-of select="collref[starts-with(field[@n='15'],'1.')]/field[@n='14']"/>
                <xsl:if test="collref[not(starts-with(field[@n='15'],'1.'))]">;
                  <xsl:if test="collref[starts-with(field[@n='15'],'2.')]">other inv.: </xsl:if>
                  <xsl:for-each select="collref[starts-with(field[@n='15'],'2.')]">
                    <xsl:value-of select="field[@n='14']"/><xsl:if test="following-sibling::collref[not(starts-with(field[@n='15'],'1.'))]">; </xsl:if>
                  </xsl:for-each>
                  <xsl:if test="collref[starts-with(field[@n='15'],'3.')]">formerly: </xsl:if>
                  <xsl:for-each select="collref[starts-with(field[@n='15'],'3.')]">
                    <xsl:value-of select="field[@n='14']"/><xsl:if test="following-sibling::collref[starts-with(field[@n='15'],'3.')]">; </xsl:if>
                  </xsl:for-each></xsl:if>
              </td>
            </tr>
            <!-- Reuse -->
            <xsl:if test="string-length(field[@n='13']) gt 0">
              <tr>
                <th>Reuse Type</th>
                <td><xsl:value-of select="field[@n='13']"/><xsl:text> </xsl:text>
                <xsl:for-each select="tokenize(field[@n='14'], ', ')">
                  <a href="/trismegistos/{.}"><xsl:value-of select="."/></a><xsl:if test="position() != last()">, </xsl:if>
                </xsl:for-each>
                <xsl:value-of select="field[@n='57']"/></td>
              </tr>
            </xsl:if>
            <!-- Date -->
            <tr>
              <th>Date</th>
              <td><xsl:value-of select="replace(field[@n='89'],'&lt;br&gt;','; ')"/></td>
            </tr>
            <!-- Language -->
            <tr>
              <th>Language</th>
              <td><xsl:value-of select="field[@n='21']"/></td>
            </tr>
            <!-- Provenance -->
            <tr>
              <th>Provenance</th>
              <td><xsl:for-each select="geotex">
                <a href="http://www.trismegistos.org/place/{field[@n='2']}"><xsl:value-of select="field[@n='28']"/></a><xsl:if test="following-sibling::geotex">; </xsl:if>
              </xsl:for-each></td>
            </tr>
            <!-- Archive -->
            <xsl:if test="archref">
              <tr>
                <th>Archive</th>
                <td><a href="http://www.trismegistos.org/archive/{archref/field[@n='5']}"><xsl:value-of select="archref/field[@n='37']"/></a></td>
              </tr>
            </xsl:if>
            <!-- People -->
            <xsl:if test="personref">
              <tr>
                <th>People</th>
                <td><a href="http://www.trismegistos.org/ref/ref_list.php?tex_id={field[@n='0']}">mentioned people</a></td>
              </tr>
            </xsl:if>
            <!-- Places -->
            <xsl:if test="georef">
              <tr>
                <th>Places</th>
                <td><a href="http://www.trismegistos.org/geo/georef_list.php?tex_id={field[@n='0']}">mentioned places</a></td>
              </tr>
            </xsl:if>
          </tbody>
        </table>
      </div>
    </div>
  </xsl:template>
    
  <!-- Title -->
  <xsl:template match="t:title" mode="metadata">
    <tr>
      <th class="rowheader" rowspan="1">Title</th>
      <td><xsl:if test="not(starts-with(., 'kein'))"><xsl:attribute name="class">mdtitle</xsl:attribute></xsl:if><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Author -->
  <xsl:template match="t:author" mode="metadata">
    <tr>
      <th class="rowheader" rowspan="1">Author</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Summary -->
  <xsl:template match="t:summary" mode="metadata">
    <tr>
      <th class="rowheader">Summary</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Publications -->
  <xsl:template match="t:div[@type = 'bibliography' and @subtype='principalEdition']" mode="metadata">
    <xsl:variable name="pubcount" select="count(../t:div[@type = 'bibliography' and @subtype = 'otherPublications']//t:bibl) + 1"/>
    <tr>
      <th class="rowheader" rowspan="{$pubcount}">Publications</th>
      <td>
        <xsl:value-of select=".//t:bibl"/>
        <!-- more-like-this output -->
        <xsl:if test=".//t:title[@level='s']">
          <div class="more-like-this">
          <xsl:variable name="series-name">
            <xsl:value-of select=".//t:title[@level='s'][1]"></xsl:value-of>
          </xsl:variable>
          <a href="{concat($facet-root, $series-param, '=', $series-name)}" title="More in series {$series-name}" target="_blank" rel="nofollow">More in series <xsl:value-of select="$series-name"></xsl:value-of></a>
          <xsl:if test=".//t:biblScope[@type='volume']">
            <xsl:variable name="series-name">
            <xsl:value-of select=".//t:title[@level='s'][1]"></xsl:value-of>
          </xsl:variable>
            <xsl:variable name="volume-name">
              <xsl:value-of select=".//t:biblScope[@type='volume']"></xsl:value-of>
            </xsl:variable>
            <a href="{concat($facet-root, $series-param, '=', $series-name, '&amp;', $volume-param, '=', $volume-name)}" title="More in series {$series-name}, vol. {$volume-name}" target="_blank" rel="nofollow">More in series <xsl:value-of select="$series-name"/>, vol. <xsl:value-of select="$volume-name"/></a>
          </xsl:if>
            </div>
        </xsl:if>
      </td>
    </tr>
    <xsl:for-each select="../t:div[@type = 'bibliography' and @subtype = 'otherPublications']//t:bibl">
      <tr>
        <td><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>
  
  <!-- APIS Citations -->
  <xsl:template match="t:div[@type = 'bibliography' and @subtype='citations']" mode="metadata">
    <tr>
      <th class="rowheader" rowspan="{count(.//t:bibl)}">Citations</th>
      <td>
        <xsl:for-each select=".//t:bibl[not(preceding-sibling::t:bibl)]">
          <xsl:apply-templates/>
        </xsl:for-each>
      </td>
    </tr>
    <xsl:for-each select=".//t:bibl[preceding-sibling::t:bibl]">
      <tr>
        <td><xsl:apply-templates/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="t:note[parent::t:bibl]"><xsl:text> </xsl:text>[<xsl:apply-templates/>]</xsl:template>
  
  <!-- Commentary -->
  <xsl:template match="t:div[@type = 'commentary'][@subtype != 'mentionedDates']" mode="metadata">
    <tr>
      <th>Commentary</th>
      <td><xsl:value-of select="t:p"/></td>
    </tr>
  </xsl:template>
  
  <xsl:template match="t:div[@type = 'commentary'][@subtype = 'mentionedDates']" mode="metadata">
    <tr>
      <th>Mentioned Dates</th>
      <td><ul><xsl:for-each select="t:list/t:item">
        <li><b><xsl:value-of select="t:ref"/>:</b> <xsl:value-of select="t:date"/></li>
      </xsl:for-each></ul></td>
    </tr>
  </xsl:template>
  
  <!-- Print Illustrations -->
  <xsl:template match="t:div[@type = 'bibliography' and @subtype='illustrations']" mode="metadata">
    <tr>
      <th class="rowheader">Print Illustrations</th>
      <td><xsl:for-each select=".//t:bibl"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each></td>
    </tr>
  </xsl:template>
  
  <!-- Inv. Id -->
  <xsl:template match="t:msIdentifier/t:idno" mode="metadata">
    <tr>
      <th class="rowheader">Inv. Id</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Physical Desc. -->
  <xsl:template match="t:physDesc/t:p" mode="metadata">
    <tr>
      <th class="rowheader">Physical Desc.</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Support/Dimensions -->
  <xsl:template match="t:support" mode="metadata">
    <tr>
      <th class="rowheader">Support/Dimensions</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Condition -->
  <xsl:template match="t:ab[@type='conservation']" mode="metadata">
    <tr>
      <th class="rowheader">Condition</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Preservation -->
  <xsl:template match="t:ab[@type='preservation']" mode="metadata">
    <tr>
      <th class="rowheader">Preservation</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Lines -->
  <xsl:template match="t:ab[@type='lines']" mode="metadata">
    <tr>
      <th class="rowheader">Lines</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Recto/Verso -->
  <xsl:template match="t:ab[@type='recto-verso']" mode="metadata">
    <tr>
      <th class="rowheader">Recto/Verso</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Hands -->
  <xsl:template match="t:handDesc/t:p" mode="metadata">
    <tr>
      <th class="rowheader">Hands</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Post-Concordance BL Entries -->
  <xsl:template match="t:div[@type = 'bibliography' and @subtype='corrections']" mode="metadata">
    <tr>
      <th class="rowheader" rowspan="1">Post-Concordance BL Entries</th>
      <td>
        <xsl:for-each select=".//t:bibl"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each>
      </td>
    </tr>
  </xsl:template>
  
  <!-- Translations -->
  <xsl:template match="t:div[@type = 'bibliography' and @subtype='translations']" mode="metadata">
    <tr>
      <th class="rowheader" rowspan="1">Translations</th>
      <td>
        <xsl:for-each select=".//t:bibl"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each>
      </td>
    </tr>
  </xsl:template>
  
  <!-- Provenance -->
  <xsl:template match="t:origPlace|t:p" mode="metadata">
    <tr>
      <th class="rowheader" rowspan="1">Provenance</th>
      <td class="mdprov">
        <xsl:choose>
          <xsl:when test="local-name(.) = 'origPlace'"><xsl:apply-templates select="."/></xsl:when>
          <xsl:otherwise><xsl:if test="t:placeName[@type='ancientFindspot']"><xsl:value-of select="t:placeName[@type='ancientFindspot']"/><xsl:if test="t:geogName">, </xsl:if></xsl:if>
            <xsl:if test="t:geogName[@type='nome']"><xsl:value-of select="t:geogName[@type='nome']"/><xsl:if test="t:geogName[@type='ancientRegion']">, </xsl:if></xsl:if>
            <xsl:value-of select="t:geogName[@type='ancientRegion']"/></xsl:otherwise>
        </xsl:choose>
          <!-- more-like-this output -->        
          <xsl:if test="//t:origin/(t:origPlace|t:p/t:placeName[@type='ancientFindspot'])">
            <xsl:variable name="provenance-value">
              <xsl:value-of select="normalize-space(string-join(/t:TEI/t:teiHeader/t:fileDesc/t:sourceDesc/t:msDesc/t:history/t:origin/(t:origPlace|t:p[t:placeName/@type='ancientFindspot']), ' '))"></xsl:value-of>
            </xsl:variable>
            <div class="more-like-this"><a href="{concat($facet-root, $provenance-param, '=', $provenance-value)}" title="More from {$provenance-value}" target="_blank" rel="nofollow">More from <xsl:value-of select="$provenance-value"></xsl:value-of></a></div>
          </xsl:if>
      </td>
    </tr>
  </xsl:template>
  
  <!-- Commented out until HGV XML makes sense
  <xsl:template match="t:origPlace/t:placeName">
    <xsl:choose>
      <xsl:when test="t:placeName"> 
        <xsl:apply-templates select="t:placeName[@type"></xsl:apply-templates>
      </xsl:when> 
      <xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
    </xsl:choose>
    </xsl:template>
  -->
  
  <!-- Material -->
  <xsl:template match="t:material" mode="metadata">
    <tr>
      <th class="rowheader">Material</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Language -->
  <xsl:template match="t:textLang" mode="metadata">
    <tr>
      <th class="rowheader">Language</th>
      <td>
        <xsl:value-of select="."/>
        <!-- more-like-this output -->
        <!-- nb: impossible to test at present owing to problems with how language information is selected. See http://idp.atlantides.org/trac/idp/ticket/916 -->
        <xsl:variable name="all-langs-found" select="distinct-values(//t:div[@type='edition']/descendant-or-self::*/@xml:lang)"></xsl:variable>
        <xsl:if test="count($all-langs-found) > 0 and count(//t:langUsage/t:language) > 0">
          <div class="more-like-this">
            <xsl:for-each select="//t:langUsage/t:language">
              <xsl:variable name="ident" select="string(@ident)"/>
              <xsl:if test="index-of($all-langs-found, $ident)">
                <xsl:if test="key('lang-codes', $ident)">
                  <xsl:variable name="expansion"><xsl:value-of select="key('lang-codes', $ident)"></xsl:value-of></xsl:variable>
                  <a href="{concat($facet-root, $language-param, '=', $ident)}" title="More texts in {$expansion}" target="_blank" rel="nofollow">More texts in <xsl:value-of select="$expansion"></xsl:value-of></a>
                </xsl:if>
              </xsl:if>             
            </xsl:for-each>
          </div>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>
  
  <!-- Date -->
  <xsl:template match="t:origDate" mode="metadata">
    <tr>
      <th class="rowheader">Date</th>
      <td class="mddate">
        <xsl:value-of select="."/>
        <!-- more-like-this output -->
        <xsl:if test="./(@when|@notBefore|@notAfter)">
          <xsl:variable name="date-start">
            <xsl:choose>
              <xsl:when test="./@when">
               <xsl:value-of select="pi:trim-date-to-year(./@when)"></xsl:value-of>
              </xsl:when>
              <xsl:when test="./@notBefore">
                <xsl:value-of select="pi:trim-date-to-year(./@notBefore)"></xsl:value-of>
              </xsl:when>
              <xsl:otherwise>no start date</xsl:otherwise>
            </xsl:choose> 
          </xsl:variable>
         <xsl:variable name="temp-date-end">
           <xsl:choose>
             <xsl:when test="./@when">
                 <xsl:value-of select="pi:trim-date-to-year(./@when) + 1"></xsl:value-of>
             </xsl:when>
             <xsl:when test="./@notAfter">
               <xsl:value-of select="pi:trim-date-to-year(./@notAfter)"></xsl:value-of>
             </xsl:when>
             <xsl:otherwise>no end date</xsl:otherwise>
           </xsl:choose>
          </xsl:variable>
           <xsl:variable name="date-end">
             <xsl:choose>
             <xsl:when test="$temp-date-end eq $date-start">
               <xsl:value-of select="$temp-date-end + 1"></xsl:value-of>
             </xsl:when>
               <xsl:otherwise>
                 <xsl:value-of select="$temp-date-end"></xsl:value-of>
               </xsl:otherwise>
             </xsl:choose>
           </xsl:variable>
          <xsl:variable name="and">&amp;</xsl:variable>
           <xsl:variable name="date-start-querystring">
             <xsl:choose>
               <xsl:when test="$date-start eq 'no start date'"></xsl:when>
               <xsl:otherwise>
                 <xsl:value-of select="$and"></xsl:value-of>
                 <xsl:value-of select="$date-start-param"></xsl:value-of>
                 <xsl:text>=</xsl:text>
                 <xsl:value-of select="abs($date-start)"></xsl:value-of>
                <xsl:value-of select="$and"></xsl:value-of>
                 <xsl:value-of select="$date-start-era-param"></xsl:value-of>
                 <xsl:text>=</xsl:text>
                 <xsl:value-of select="pi:get-era($date-start)"></xsl:value-of>
               </xsl:otherwise>
             </xsl:choose>
           </xsl:variable>
           <xsl:variable name="date-end-querystring">
             <xsl:choose>
               <xsl:when test="$date-end eq 'no end date'"></xsl:when>
               <xsl:otherwise>
                 <xsl:value-of select="$and"></xsl:value-of>
                 <xsl:value-of select="$date-end-param"></xsl:value-of>
                 <xsl:text>=</xsl:text>
                 <xsl:value-of select="abs($date-end)"></xsl:value-of>
                 <xsl:value-of select="$and"></xsl:value-of>
                 <xsl:value-of select="$date-end-era-param"></xsl:value-of>
                 <xsl:text>=</xsl:text>
                 <xsl:value-of select="pi:get-era($date-end)"></xsl:value-of>
               </xsl:otherwise>
             </xsl:choose>
           </xsl:variable>
          <xsl:variable name="span-label">
            <xsl:choose>
              <xsl:when test="$date-start eq 'no start date'">
                <xsl:text> before </xsl:text>
                <xsl:value-of select="abs($date-end)"></xsl:value-of>
                <xsl:text> </xsl:text>
                <xsl:value-of select="pi:get-era($date-end)"/>
              </xsl:when>
              <xsl:when test="$date-end eq 'no end date'">
                <xsl:text> after </xsl:text>
                <xsl:value-of select='abs($date-start)'></xsl:value-of>
                <xsl:text> </xsl:text>
                <xsl:value-of select="pi:get-era($date-start)"></xsl:value-of>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text> between </xsl:text>
                <xsl:value-of select="abs($date-start)"></xsl:value-of>
                <xsl:text> </xsl:text>
                <xsl:value-of select="pi:get-era($date-start)"></xsl:value-of>
                <xsl:text> and </xsl:text>
                <xsl:value-of select="abs($date-end)"></xsl:value-of>
                <xsl:text> </xsl:text>
                <xsl:value-of select="pi:get-era($date-end)"></xsl:value-of>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="date-mode">
            <xsl:value-of select="$date-mode-param"></xsl:value-of>
            <xsl:text>=</xsl:text>
            <xsl:value-of select="$date-mode-value"></xsl:value-of>
          </xsl:variable>
          <xsl:variable name="anchor" select="concat($facet-root, $date-mode, $date-start-querystring, $date-end-querystring)"></xsl:variable>
          <div class="more-like-this"><a href="{$anchor}" title="More from this timespan" target="_blank" rel="nofollow">More from the period <xsl:value-of select="$span-label"></xsl:value-of></a></div>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>
  
  <xsl:function name="pi:trim-date-to-year">
    <xsl:param name="raw-date"/>
    <xsl:variable name="cooked-date">
    <xsl:analyze-string select="$raw-date" regex="(-?\d{{4}})(-\d{{1,2}}){{0,2}}">
      <xsl:matching-substring>
        <xsl:value-of select="regex-group(1)"></xsl:value-of>
      </xsl:matching-substring>
    </xsl:analyze-string>
    </xsl:variable>
    <xsl:variable name="trimmed-date">
      <xsl:choose>
        <xsl:when test="substring($cooked-date, 1, 1) eq '0'"><xsl:value-of select="substring($cooked-date, 2)"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$cooked-date"/></xsl:otherwise>
      </xsl:choose>    
    </xsl:variable>
    <xsl:sequence select="number($trimmed-date)"/>
  </xsl:function>
  
  <xsl:function name="pi:get-era">
    <xsl:param name="raw-year"/>
    <xsl:variable name="era">
      <xsl:choose>
      <xsl:when test="number($raw-year) lt 0">BCE</xsl:when>
      <xsl:otherwise>CE</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:sequence select="$era"/>
  </xsl:function>
  
  <!-- Custodial Events --> 
  <xsl:template match="t:custodialHist" mode="metadata">
    <tr>
      <th class="rowheader" rowspan="1">Custodial Events</th>
      <td>
        <ul>

          <xsl:for-each select="t:custEvent">
            <xsl:variable name="context-node" select="../../../../t:msIdentifier/t:idno/t:idno"/>

            <xsl:choose>
              <xsl:when test="t:ptr[@target]">
                <xsl:variable name="link" select="t:ptr/@target"/>
                <a href="{$link}">

                  <xsl:choose>
                    <xsl:when test="@corresp">
                      <xsl:choose>
                        <xsl:when test="@type">
                          <li><xsl:value-of select="@type"/> by&#xA0; <xsl:choose>
                              <xsl:when test="@from">
                                <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                  select="t:surname"/>(<xsl:value-of select="@from"/> - <xsl:value-of
                                  select="@to"/>) :&#xA0; <xsl:variable name="token"
                                  select="tokenize(@corresp,' ')"/>
                                <xsl:for-each select="$token">
                                  <xsl:variable name="frag" select="."/>
                                  <xsl:variable name="xml-id" select="substring-after($frag, '#')"/>
                                  <xsl:value-of select="$context-node[@xml:id=$xml-id]"/>&#xA0;
                                </xsl:for-each>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                  select="t:surname"/> :&#xA0; <xsl:variable name="token"
                                  select="tokenize(@corresp,' ')"/>
                                <xsl:for-each select="$token"> <xsl:variable name="frag" select="."/>
                                  <xsl:variable name="xml-id" select="substring-after($frag, '#')"/>
                                    <xsl:value-of select="$context-node[@xml:id=$xml-id]"/>
                                </xsl:for-each>
                              </xsl:otherwise>
                            </xsl:choose>
                          </li>
                        </xsl:when>
                        <xsl:otherwise>
                          <li>
                            <xsl:choose>
                              <xsl:when test="@from">
                                <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                  select="t:surname"/>(<xsl:value-of select="@from"/> - <xsl:value-of
                                  select="@to"/>) :&#xA0; <xsl:variable name="token"
                                  select="tokenize(@corresp,' ')"/>
                                <xsl:for-each select="$token">
                                  <xsl:variable name="frag" select="."/>
                                  <xsl:variable name="xml-id" select="substring-after($frag, '#')"/>
                                  <xsl:value-of select="$context-node[@xml:id=$xml-id]"/>&#xA0;
                                </xsl:for-each>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                  select="t:surname"/> :&#xA0; <xsl:variable name="token"
                                  select="tokenize(@corresp,' ')"/>
                                <xsl:for-each select="$token"> <xsl:variable name="frag" select="."/>
                                  <xsl:variable name="xml-id" select="substring-after($frag, '#')"/>
                                    <xsl:value-of select="$context-node[@xml:id=$xml-id]"/>
                                </xsl:for-each>
                              </xsl:otherwise>
                            </xsl:choose>
                          </li>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:choose>
                        <xsl:when test="@type">
                          <li><xsl:value-of select="@type"/> by&#xA0; <xsl:choose>
                              <xsl:when test="@from">
                                <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                  select="t:surname"/>(<xsl:value-of select="@from"/> - <xsl:value-of
                                  select="@to"/>) </xsl:when>
                              <xsl:otherwise>
                                <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                  select="t:surname"/>
                              </xsl:otherwise>
                            </xsl:choose>
                          </li>
                        </xsl:when>
                        <xsl:otherwise>
                          <li>
                            <xsl:choose>
                              <xsl:when test="@from">
                                <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                  select="t:surname"/>(<xsl:value-of select="@from"/> - <xsl:value-of
                                  select="@to"/>) </xsl:when>
                              <xsl:otherwise>
                                <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                  select="t:surname"/>
                              </xsl:otherwise>
                            </xsl:choose>
                          </li>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:otherwise>
                  </xsl:choose>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:choose>
                  <xsl:when test="@corresp">
                    <xsl:choose>
                      <xsl:when test="@type">
                        <li><xsl:value-of select="@type"/> by&#xA0; <xsl:choose>
                            <xsl:when test="@from">
                              <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                select="t:surname"/>(<xsl:value-of select="@from"/> - <xsl:value-of
                                select="@to"/>) :&#xA0; <xsl:variable name="token"
                                select="tokenize(@corresp,' ')"/>
                              <xsl:for-each select="$token">
                                <xsl:variable name="frag" select="."/>
                                <xsl:variable name="xml-id" select="substring-after($frag, '#')"/>
                                <xsl:value-of select="$context-node[@xml:id=$xml-id]"/>&#xA0;
                              </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                select="t:surname"/> :&#xA0; <xsl:variable name="token"
                                select="tokenize(@corresp,' ')"/>
                              <xsl:for-each select="$token"> <xsl:variable name="frag" select="."/>
                                <xsl:variable name="xml-id" select="substring-after($frag, '#')"/>
                                  <xsl:value-of select="$context-node[@xml:id=$xml-id]"/>
                              </xsl:for-each>
                            </xsl:otherwise>
                          </xsl:choose>
                        </li>
                      </xsl:when>
                      <xsl:otherwise>
                        <li>
                          <xsl:choose>
                            <xsl:when test="@from">
                              <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                select="t:surname"/>(<xsl:value-of select="@from"/> - <xsl:value-of
                                select="@to"/>) :&#xA0; <xsl:variable name="token"
                                select="tokenize(@corresp,' ')"/>
                              <xsl:for-each select="$token">
                                <xsl:variable name="frag" select="."/>
                                <xsl:variable name="xml-id" select="substring-after($frag, '#')"/>
                                <xsl:value-of select="$context-node[@xml:id=$xml-id]"/>&#xA0;
                              </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                select="t:surname"/> :&#xA0; <xsl:variable name="token"
                                select="tokenize(@corresp,' ')"/>
                              <xsl:for-each select="$token"> <xsl:variable name="frag" select="."/>
                                <xsl:variable name="xml-id" select="substring-after($frag, '#')"/>
                                  <xsl:value-of select="$context-node[@xml:id=$xml-id]"/>
                              </xsl:for-each>
                            </xsl:otherwise>
                          </xsl:choose>
                        </li>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:choose>
                      <xsl:when test="@type">
                        <li><xsl:value-of select="@type"/> by&#xA0; <xsl:choose>
                            <xsl:when test="@from">
                              <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                select="t:surname"/>(<xsl:value-of select="@from"/> - <xsl:value-of
                                select="@to"/>) </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                select="t:surname"/>
                            </xsl:otherwise>
                          </xsl:choose>
                        </li>
                      </xsl:when>
                      <xsl:otherwise>
                        <li>
                          <xsl:choose>
                            <xsl:when test="@from">
                              <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                select="t:surname"/>(<xsl:value-of select="@from"/> - <xsl:value-of
                                select="@to"/>) </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="t:forename"/>&#xA0;<xsl:value-of
                                select="t:surname"/>
                            </xsl:otherwise>
                          </xsl:choose>
                        </li>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>

        </ul>
      </td>

    </tr>
  </xsl:template>
  
  <!-- Reference Edition-->
  <xsl:template match="t:div[@type = 'bibliography' and @subtype = 'referenceEdition']" mode="metadata">
  <tr>
      <th>Reference Edition</th>
      <td><ul><xsl:for-each select="t:listBibl/t:bibl">
        <li><xsl:value-of select="t:author"/><xsl:value-of select="t:editor"/>, <xsl:value-of select="t:title"/> , <xsl:value-of select="t:date"/></li>
      </xsl:for-each></ul></td>
    </tr>
  </xsl:template>
  
  <!-- Notes -->
  <xsl:template match="t:msItemStruct/t:note" mode="metadata">
    <tr>
      <th class="rowheader">Note (<xsl:value-of select="replace(./@type, '_', '/')"/>)</th>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <!-- Subjects -->
  <xsl:template match="t:keywords" mode="metadata">
    <tr>
      <th class="rowheader">Subjects</th>
      <td><xsl:for-each select="t:term"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each></td>
    </tr>
  </xsl:template>
  
  <!-- Place Stored (Ancient) -->
  <xsl:template match="t:provenance[@type= 'stored']/t:p" mode="metadata">
    <tr>
      <th class="rowheader">Place Stored (Ancient)</th>
      <td>
        <xsl:for-each select="t:placeName">
          <xsl:choose>
            <xsl:when test="not(@subtype)">
              <xsl:choose>
                <xsl:when test="not(@ref)">
                  <xsl:value-of select="."/>,&#xA0; </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="."/>,&#xA0; </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise> </xsl:otherwise>
          </xsl:choose>

        </xsl:for-each>
        <xsl:for-each select="t:placeName">
          <xsl:choose>
            <xsl:when test="not(@subtype)"> </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="."/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>

      </td>
    </tr>
  </xsl:template>
  
  
  <!-- Associated Names -->
  <xsl:template match="t:origin" mode="metadata">
    <tr>
      <th class="rowheader">Associated Names</th>
      <td><xsl:for-each select="t:persName[@type = 'asn']"><xsl:value-of select="normalize-space(.)"/><xsl:if test="position() != last()">; </xsl:if></xsl:for-each></td>
    </tr>
  </xsl:template>
  
  <!-- Images -->
  <xsl:template match="t:div[@type = 'figure']" mode="metadata">
    <xsl:for-each select=".//t:figure">
      <tr>
        <th class="rowheader">Images</th>
      <td><a href="{t:graphic/@url}"><xsl:choose>
        <xsl:when test="t:figDesc"><xsl:value-of select="t:figDesc"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="substring(t:graphic/@url, 1, 60)"/>...</xsl:otherwise>
      </xsl:choose>
      </a></td>
    </tr>
    </xsl:for-each>
    
  </xsl:template>
  

</xsl:stylesheet>

