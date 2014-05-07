<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id: txt-teilb.xsl 1447 2008-08-07 12:57:55Z zau $ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:EDF="http://epidoc.sourceforge.net/ns/functions"
   xmlns:t="http://www.tei-c.org/ns/1.0" version="1.0">
   <!-- Actual display and increment calculation found in teilb.xsl -->
   <xsl:import href="../epidoc-xslt/teilb.xsl"/>

   <xsl:template match="t:lb">
      <xsl:choose>
         <xsl:when test="ancestor::t:lg and $verse-lines = 'yes'">
            <xsl:apply-imports/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:variable name="div-loc">
               <xsl:for-each select="ancestor::t:div[@type='textpart']">
                  <xsl:value-of select="@n"/>
                  <xsl:text>-</xsl:text>
               </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="line">
               <xsl:if test="@n">
                  <xsl:value-of select="@n"/>
               </xsl:if>
            </xsl:variable>
           <xsl:if test="(@break='no' or @type='inWord')">
             <!-- print hyphen if break=no  -->
             <xsl:choose>
               <!--    *unless* diplomatic edition  -->
               <xsl:when test="$edition-type='diplomatic'"/>
               <!--    *or unless* the lb is first in its ancestor div  -->
               <xsl:when test="generate-id(self::t:lb) = generate-id(ancestor::t:div[1]/t:*[child::t:lb][1]/t:lb[1])"/>
               <!--   *or unless* the second part of an app in ddbdp  -->
               <xsl:when test="($leiden-style = 'ddbdp' or $leiden-style = 'sammelbuch') and
                 (ancestor::t:corr or ancestor::t:reg or ancestor::t:rdg or ancestor::t:del[parent::t:subst])"/>
               <!--  *unless* previous line ends with space / g / supplied[reason=lost]  -->
               <xsl:when test="preceding-sibling::node()[1][local-name() = 'space' or
                 local-name() = 'g' or (local-name()='supplied' and @reason='lost') or
                 (normalize-space(.)='' 
                 and preceding-sibling::node()[1][local-name() = 'space' or
                 local-name() = 'g' or (local-name()='supplied' and @reason='lost')])]"/>
               <xsl:otherwise>
                 <xsl:text>-</xsl:text>
               </xsl:otherwise>
             </xsl:choose>
           </xsl:if>
            <xsl:choose>
               <xsl:when test="starts-with($leiden-style, 'edh')">
                  <xsl:variable name="cur_anc"
                     select="generate-id(ancestor::node()[local-name()='lg' or local-name()='ab'])"/>
                  <xsl:if
                     test="preceding::t:lb[1][generate-id(ancestor::node()[local-name()='lg' or local-name()='ab'])=$cur_anc]">
                     <xsl:choose>
                        <xsl:when
                           test="ancestor::t:w | ancestor::t:name | ancestor::t:placeName | ancestor::t:geogName">
                           <xsl:text>/</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:text> / </xsl:text>
                        </xsl:otherwise>
                     </xsl:choose>
                  </xsl:if>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:text>
</xsl:text>
               </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
               <xsl:when test="not(number(@n)) and ($leiden-style = 'ddbdp' or $leiden-style = 'sammelbuch')">
                  <xsl:call-template name="margin-num"/>
               </xsl:when>
               <xsl:when test="@n mod $line-inc = 0 and not(@n = 0)">
                  <xsl:choose>
                     <xsl:when test="starts-with($leiden-style, 'edh')"/>
                     <xsl:otherwise>
                        <xsl:call-template name="margin-num"/>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:when>
               <xsl:when test="preceding-sibling::t:*[1][local-name() = 'gap'][@unit = 'line']">
                  <xsl:call-template name="margin-num"/>
               </xsl:when>
               <xsl:otherwise>
                  <!-- template »line-numbering-tab« found in txt-tpl-linenumberingtab.xsl respectively odf-tpl-linenumberingtab.xsl -->
                  <xsl:call-template name="line-numbering-tab" />
               </xsl:otherwise>
            </xsl:choose>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template name="margin-num">
     <!-- template »line-numbering-tab« found in txt-tpl-linenumberingtab.xsl respectively odf-tpl-linenumberingtab.xsl -->
      <xsl:value-of select="@n"/>.<xsl:call-template name="line-numbering-tab" /> 
   </xsl:template>
  
  <xsl:function name="EDF:f-wwrap">
    <!-- called by teisupplied.xsl, teig.xsl and teispace.xsl -->
    <xsl:param name="ww-context"/>
    <xsl:choose>
      <xsl:when test="$ww-context/following-sibling::node()[1][(local-name()='lb' and (@break='no' or @type='inWord'))
        or normalize-space(.)='' and following-sibling::node()[1][local-name()='lb' and (@break='no' or @type='inWord')]]">
        <xsl:value-of select="true()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="false()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
