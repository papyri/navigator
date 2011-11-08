<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:pi="http://papyri.info/ns"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:variable name="facet-root">/search?</xsl:variable>
    <xsl:variable name="series-param">SERIES</xsl:variable>
    <xsl:variable name="volume-param">VOLUME</xsl:variable>
    <xsl:variable name="provenance-param">PLACE</xsl:variable>
    <xsl:variable name="language-param">LANG</xsl:variable>
    <xsl:variable name="date-start-param">DATE_START_TEXT</xsl:variable>
    <xsl:variable name="date-end-param">DATE_END_TEXT</xsl:variable>
    <xsl:variable name="date-start-era-param">DATE_START_ERA</xsl:variable>
    <xsl:variable name="date-end-era-param">DATE_END_ERA</xsl:variable>
    <xsl:variable name="date-mode-param">DATE_MODE</xsl:variable>
    <xsl:variable name="date-mode-value">STRICT</xsl:variable>
    <!-- Language codes to expansions as mapped in info.papyri.dispatch.LanguageCode, with modern language codes omitted -->
    <pi:lang-codes-to-expansions>
        <nd code="grc">Ancient Greek</nd>
        <nd code="egy-Copt">Egyptian\Coptic</nd>
        <nd code="la">Latin</nd>
        <nd code="grc-Latn">Ancient Greek in Latin script</nd>
        <nd code="la-Grek">Latin in Greek script</nd>
        <nd code="cop">Coptic</nd>
        <nd code="egy-Coptgrc">Greek(?) Egyptian\Coptic(?)</nd>
        <nd code="ar-Arabegy-Copt">(A) Egyptian\Coptic (B) Arabic; Egyptian\Coptic (?)</nd>
        <nd code="egy-Egyd">Egyptian - Demotic script</nd>
        <nd code="ar-Arab">Arabic - Arabic script</nd>
        <nd code="egy-Egyh">Egyptian - Hieratic script</nd>
        <nd code="egy-Egyp">Egyptian - Hieroglyphic script</nd>
        <nd code="egy-Egydgrc">Egyptian - Demotic script\Ancient Greek</nd>
        <nd code="und">Undetermined</nd>
        <nd code="ar-Arabgrc">Arabic - Arabic script\Ancient Greek</nd>
        <nd code="grcla">Ancient Greek\Latin</nd>
        <nd code="he-Hebr">Hebrew - Hebrew script</nd>
        <nd code="egy-Egydegy-Egyh">Egyptian - Demotic script\Egyptian - Hieratic script</nd>
        <nd code="egy-Egyhegy-Egyp">Egyptian - Hieratic script\Egyptian - Hieroglyphic script</nd>
        <nd code="egy-Egydegy-Egyp">Egyptian - Demotic script\Egyptian - Hieroglyphic script</nd>
        <nd code="ar-Arabegy-Coptgrc">Arabic - Arabic script\Egyptian - Coptic\Ancient Greek</nd>
        <nd code="sem">Semitic language</nd>
        <nd code="arc">Aramaic</nd>
        <nd code="faspal-Phil">Persian - Pahlavi script</nd>
        <nd code="faspal-Phli">Persian - Pahlavi script</nd>
        <nd code="grcegy-Egyhegy-Egyp">Ancient Greek\Egyptian - Hieratic script\Egyptian - Hieroglyphic script</nd>
        <nd code="ar-Arabegy-Egyd">Arabic - Arabic script\Egyptian - Demotic script</nd>
        <nd code="egy">Egyptian</nd>
        <nd code="egy-Coptegy-Egydegy-Egyh">Egyptian - Coptic\Egyptian - Demotic script\Egyptian - Hieratic script</nd>
        <nd code="egy-Coptgrcund">Egyptian - Coptic\Ancient Greek\Undetermined</nd>
        <nd code="egy-Copthe-Hebr">Egyptian - Coptic\Hebrew - Hebrew script</nd>
        <nd code="egy-Egydgrcegy-Egyh">Egyptian - Demotic script\Ancient Greek\Egyptian - Hieratic script</nd>
        <nd code="fas">Persian</nd>
        <nd code="grcegy-Egyh">Ancient Greek\Egyptian - Hieratic script</nd>
        <nd code="xpr-Prti">Parthian</nd>
    </pi:lang-codes-to-expansions>
    
   
</xsl:stylesheet>