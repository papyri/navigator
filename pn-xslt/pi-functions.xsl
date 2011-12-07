<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:dc="http://purl.org/dc/terms/" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:pi="http://papyri.info/ns"
  exclude-result-prefixes="xs"
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" version="2.0">
  
  <xsl:function name="pi:get-docs">
    <xsl:param name="urls"/>
    <xsl:param name="format"/>
    <xsl:for-each select="$urls">
      <xsl:message><xsl:value-of select="."/></xsl:message>
      <xsl:choose>
        <xsl:when test="doc-available(pi:get-filename(., $format))">
          <xsl:copy-of select="doc(pi:get-filename(., $format))"/>
        </xsl:when>
        <xsl:when test="doc-available(.)">
          <xsl:copy-of select="doc(.)"/>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:function>
  
  <!-- Given an identifier URL, get the filename -->
  <xsl:function name="pi:get-filename" as="xs:string">
    <xsl:param name="url"/>
    <xsl:param name="format"/>
    <xsl:variable name="base"><xsl:choose>
      <xsl:when test="$format = 'xml'"><xsl:value-of select="$path"/></xsl:when>
      <xsl:when test="$format = 'html'"><xsl:value-of select="$outbase"/></xsl:when>
    </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="contains($url, 'ddbdp')">
        <xsl:choose>
          <xsl:when test="$url = 'http://papyri.info/ddbdp'"><xsl:sequence select="concat($base, '/DDB_EpiDoc_XML/index.html')"/></xsl:when>
          <xsl:when test="ends-with($url, '/source')">
            <xsl:variable name="id" select="tokenize(substring-before(substring-after($url, 'http://papyri.info/ddbdp/'), '/'), ';')"/>
            <xsl:choose>
              <!-- like http://papyri.info/ddbdp/c.etiq.mom;;165/source -->
              <xsl:when test="$id[2] = ''"><xsl:sequence select="concat($base, '/DDB_EpiDoc_XML/', $id[1], '/', $id[1], '.', replace(replace($id[3], '%2C', '-'), '%2F', '_'), '.', $format)"/></xsl:when>
              <!-- like http://papyri.info/ddbdp/bgu;1;1/source -->
              <xsl:otherwise><xsl:sequence select="concat($base, '/DDB_EpiDoc_XML/', $id[1], '/', $id[1], '.', $id[2], '/', $id[1], '.', $id[2], '.', replace(replace($id[3], '%2C', '-'), '%2F', '_'), '.', $format)"/></xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <!-- like http://papyri.info/ddbdp/bgu;1 -->
              <xsl:when test="contains($url, ';')">
                <xsl:variable name="id" select="tokenize(substring-after($url, 'http://papyri.info/ddbdp/'), ';')"/>
                <xsl:sequence select="concat($base, '/DDB_EpiDoc_XML/', $id[1], '/', $id[1], '.', $id[2], '/index.html')"/>
              </xsl:when>
              <!-- like http://papyri.info/ddbdp/bgu -->
              <xsl:otherwise><xsl:sequence select="concat($base, '/DDB_EpiDoc_XML/', substring-after($url, 'http://papyri.info/ddbdp/'), '/index.html')"/></xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="contains($url, 'hgv/')">
        <xsl:variable name="dir">
          <xsl:choose>
            <xsl:when test="ends-with($url, '/source')"><xsl:value-of select="ceiling(number(replace(substring-before(substring-after($url, 'http://papyri.info/hgv/'), '/'), '[a-z]', '')) div 1000)"></xsl:value-of></xsl:when>
            <xsl:otherwise><xsl:value-of select="substring-after($url, 'http://papyri.info/hgv/')"/></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:sequence select="concat($base, '/HGV_meta_EpiDoc/HGV', $dir, '/', replace(substring-after($url, 'http://papyri.info/hgv/'), '/source', ''), '.', $format)"/>
      </xsl:when>
      <xsl:when test="contains($url, 'hgvtrans')">
        <xsl:sequence select="concat($base, '/HGV_trans_EpiDoc/', substring-before(substring-after($url, 'http://papyri.info/hgvtrans/'), '/'), '.', $format)"/>
      </xsl:when>
      <xsl:when test="contains($url, 'apis')">
        <xsl:variable name="id" select="tokenize(replace(substring-after($url, 'http://papyri.info/apis/'), '/source', ''), '\.')"/>
        <xsl:choose>
          <xsl:when test="contains($url, '.')">
            <xsl:choose>
              <xsl:when test="count($id) = 2"><xsl:sequence select="concat($base, '/APIS/', $id[1], '/', $format, '/', $id[1], '.', $id[2], '.', $format)"/></xsl:when>
              <xsl:otherwise><xsl:sequence select="concat($base, '/APIS/', $id[1], '/', $format, '/', $id[1], '.', $id[2], '.', $id[3], '.', $format)"/></xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise><xsl:sequence select="concat($base, '/APIS/', substring-after($url, 'http://papyri.info/apis/'), 'index.html')"/></xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <!-- Like http://papyri.info/biblio/54953/source -->
      <xsl:when test="contains($url, 'biblio/')">
        <xsl:choose>
          <xsl:when test="contains($url, '/source')">
            <xsl:variable name="dir" select="ceiling(number(substring-before(substring-after($url, 'http://papyri.info/biblio/'), '/source')) div 1000)"/>
            <xsl:sequence select="concat($base, '/Biblio/', $dir, '/', substring-before(substring-after($url, 'http://papyri.info/biblio/'), '/source'), '.xml')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="dir" select="ceiling(number(substring-after($url, 'http://papyri.info/biblio/')) div 1000)"/>
            <xsl:sequence select="concat($base, '/Biblio/', $dir, '/', substring-after($url, 'http://papyri.info/biblio/'), '.xml')"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise><xsl:sequence select="string('null')"/></xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="pi:get-blame-url" as="xs:string">
    <xsl:param name="identifier"/>
    <xsl:variable name="base">https://github.com/papyri/idp.data/blame/master/</xsl:variable>
    <xsl:variable name="id" select="tokenize($identifier, ';')"/>
    <xsl:choose>
      <!-- like http://papyri.info/ddbdp/c.etiq.mom;;165/source -->
      <xsl:when test="$id[2] = ''"><xsl:sequence select="concat($base, '/DDB_EpiDoc_XML/', $id[1], '/', $id[1], '.', replace(replace($id[3], '%2C', '-'), '%2F', '_'), '.xml')"/></xsl:when>
      <!-- like http://papyri.info/ddbdp/bgu;1;1/source -->
      <xsl:otherwise><xsl:sequence select="concat($base, '/DDB_EpiDoc_XML/', $id[1], '/', $id[1], '.', $id[2], '/', $id[1], '.', $id[2], '.', replace(replace($id[3], '%2C', '-'), '%2F', '_'), '.xml')"/></xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <!-- Given an identifier URL, get the bare id -->
  <xsl:function name="pi:get-id" as="xs:string">
    <xsl:param name="url"/>
    
    <xsl:choose>
      <xsl:when test="matches($url, '^http://papyri\.info/(ddbdp|hgv|apis)$')">
        <xsl:sequence select="pi:decode-uri(upper-case(replace($url, 'http://papyri\.info/', '')))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="pi:decode-uri(replace(replace(replace(replace($url, 'http://papyri\.info/[^/]+/', ''), '/source$', ''), ';;', '.'), ';', '.'))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="pi:dec-to-bin">
    <xsl:param name="dec" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="$dec gt 1"><xsl:value-of select="concat(pi:dec-to-bin($dec idiv 2), $dec mod 2)"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$dec"/></xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="pi:bin-to-dec" as="xs:integer">
    <xsl:param name="bin"/>
    <xsl:sequence
      select="if (string-length($bin) eq 1)
      then xs:integer($bin)
      else if (starts-with($bin, '1')) 
        then pi:exp(2, string-length($bin) - 1) + pi:bin-to-dec(substring($bin, 2))
        else pi:bin-to-dec(substring($bin, 2)) "/>
  </xsl:function>
  
  <xsl:function name="pi:hex-to-dec">
    <xsl:param name="hex" as="xs:string"/>
    <xsl:variable name="length" select="string-length($hex)" as="xs:integer"/>
    <xsl:value-of select="
      if ($length &gt; 0) then
      if ($length &lt; 2) then
      string-length(substring-before('0 1 2 3 4 5 6 7 8 9 AaBbCcDdEeFf',$hex)) idiv 2
      else
      pi:hex-to-dec(substring($hex,1,$length - 1))*16+pi:hex-to-dec(substring($hex,$length))
      else
      0
      "/>
  </xsl:function>
  
  <xsl:function name="pi:dec-to-hex" as="xs:string">
    <xsl:param name="in" as="xs:integer"/>
    <xsl:value-of select="
      if ($in eq 0) then '0' 
      else 
        concat(
          if ($in gt 16) then 
          pi:dec-to-hex($in idiv 16) 
          else '',
          substring('0123456789ABCDEF', ($in mod 16) + 1, 1))"/>
  </xsl:function>
  
  <xsl:function name="pi:decode-uri-segment">
    <xsl:param name="seg"/>
    <xsl:value-of select="pi:hex-to-unicode(subsequence(tokenize($seg, '%'), 2))"/>
  </xsl:function>
  
  <xsl:function name="pi:hex-to-unicode">
    <xsl:param name="binseq"/>
      <xsl:if test="not(empty($binseq))">
        <xsl:choose>
          <xsl:when test="not(matches($binseq[1], '^[C-E].*'))">
            <xsl:value-of select="concat(codepoints-to-string(pi:bin-to-dec(replace(pi:dec-to-bin(pi:hex-to-dec($binseq[1])), '^0+', ''))), pi:hex-to-unicode(subsequence($binseq, 2)))"/>
          </xsl:when>
          <xsl:when test="matches($binseq[1], '^[CD].*')">
            <xsl:value-of select="concat(codepoints-to-string(pi:bin-to-dec(replace(concat(substring-after(pi:dec-to-bin(pi:hex-to-dec($binseq[1])), '110'), substring-after(pi:dec-to-bin(pi:hex-to-dec($binseq[2])), '10')), '^0+', ''))), pi:hex-to-unicode(subsequence($binseq, 3)))"/>
          </xsl:when>
          <xsl:when test="matches($binseq[1], '^E.*')">
            <xsl:value-of select="concat(codepoints-to-string(pi:bin-to-dec(replace(concat(substring-after(pi:dec-to-bin(pi:hex-to-dec($binseq[1])), '1110'), substring-after(pi:dec-to-bin(pi:hex-to-dec($binseq[2])), '10'), substring-after(pi:dec-to-bin(pi:hex-to-dec($binseq[3])), '10')), '^0+', ''))), pi:hex-to-unicode(subsequence($binseq, 4)))"/>
          </xsl:when>
        </xsl:choose>
      </xsl:if>
  </xsl:function>
  
  <xsl:function name="pi:exp" as="xs:integer">
    <xsl:param name="base" as="xs:integer"/>
    <xsl:param name="exponent" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="$exponent = 1"><xsl:value-of select="$base"></xsl:value-of></xsl:when>
      <xsl:otherwise><xsl:value-of select="$base * pi:exp($base, $exponent - 1)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="pi:decode-uri">
    <xsl:param name="uri"/>
    <xsl:variable name="decoded-component" as="xs:string*">
      <xsl:analyze-string select="$uri" regex="((%[0-9a-fA-F][0-9a-fA-F])+)">
        <xsl:matching-substring>
          <xsl:value-of select="pi:decode-uri-segment(regex-group(1))"/>
        </xsl:matching-substring>
        <xsl:non-matching-substring>
          <xsl:value-of select="."/>
        </xsl:non-matching-substring>
      </xsl:analyze-string>
    </xsl:variable>
    <xsl:sequence select="string-join($decoded-component,'')"/>
  </xsl:function>
  
 <xsl:function name="pi:iso-date-to-num">
    <xsl:param name="date"/>
    <xsl:choose>
      <xsl:when test="starts-with($date, '-')">
        <xsl:sequence select=" number(substring($date, 1, 5))"/>
      </xsl:when>
      <xsl:when test="not(number(substring($date, 1, 4)))"/>
      <xsl:otherwise>
        <xsl:sequence select="number(substring($date, 1, 4))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="pi:get-date-range">
    <xsl:param name="date-seq"/>
    <xsl:variable name="min"
      select="pi:get-min-date(remove($date-seq, 1), pi:iso-date-to-num($date-seq[1]), true())"/>
    <xsl:variable name="max"
      select="pi:get-max-date(remove($date-seq, 1), pi:iso-date-to-num($date-seq[1]), true())"/>
    <xsl:choose>
      <xsl:when test="$min = $max">
        <xsl:sequence select="$min"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="concat($min, ' - ', $max)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <!-- ** DATE-COMPARISON FUNCTIONS
       
       Note the difference between the two sets of functions below (roughly, between
       get-min-date, get-max-date and associated functions and get-earliest-date,
       get-latest-date. The former are used primarily for handling ancient dates, and
       are expecting dates in the format (-)?\d{4}, where the negative sign indicates
       a date BCE. The latter are used primarily for handling modern dates, and 
       expect input of the form \d{4}-\d{2}-\d{2} - i.e. YYYY-MM-DD
       
       *-->

  <xsl:function name="pi:get-min-date">
    <xsl:param name="date-seq"/>
    <xsl:param name="current"/>
    <xsl:param name="for-display"/>
    <xsl:choose>
      <xsl:when test="count($date-seq) = 0">
        <xsl:choose>
          <xsl:when test="$for-display">
            <xsl:sequence select="pi:add-era($current)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="number($current)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="pi:iso-date-to-num($date-seq[1]) lt $current">
        <xsl:sequence
          select="pi:get-min-date(remove($date-seq, 1), pi:iso-date-to-num($date-seq[1]), $for-display)"
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="pi:get-min-date(remove($date-seq, 1), $current, $for-display)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="pi:get-max-date">
    <xsl:param name="date-seq"/>
    <xsl:param name="current"/>
    <xsl:param name="for-display"/>
    <xsl:choose>
      <xsl:when test="count($date-seq) = 0">
        <xsl:choose>
          <xsl:when test="$for-display">
            <xsl:sequence select="pi:add-era($current)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="number($current)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="pi:iso-date-to-num($date-seq[1]) gt $current">
        <xsl:sequence
          select="pi:get-max-date(remove($date-seq, 1), pi:iso-date-to-num($date-seq[1]), $for-display)"
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="pi:get-max-date(remove($date-seq, 1), $current, $for-display)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="pi:add-era">
    <xsl:param name="raw-date"/>
    <xsl:choose>
      <xsl:when test="$raw-date le 0">
        <xsl:sequence select="concat(abs($raw-date), ' BCE')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="concat($raw-date, ' CE')"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
   
  <xsl:function name="pi:get-earliest-date">
    <xsl:param name="date-seq"></xsl:param>
    <xsl:param name="current"></xsl:param>
    <xsl:choose>
    <xsl:when test="count($date-seq) = 0">
      <xsl:sequence select="$current"></xsl:sequence>
    </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="current-as-num" select="number(replace($current, '-', ''))"></xsl:variable>
        <xsl:variable name="comp-as-num" select="number(replace($date-seq[1], '-', ''))"></xsl:variable>
        <xsl:choose>
          <xsl:when test="$current-as-num lt $comp-as-num">
            <xsl:sequence select="pi:get-earliest-date(remove($date-seq, 1), $current)"></xsl:sequence>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="pi:get-earliest-date(remove($date-seq, 1), $date-seq[1])"></xsl:sequence>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
      </xsl:choose>
  </xsl:function>
  
  <xsl:function name="pi:get-latest-date">
    <xsl:param name="date-seq"></xsl:param>
    <xsl:param name="current"></xsl:param>
    <xsl:choose>
    <xsl:when test="count($date-seq) = 0">
      <xsl:sequence select="$current"></xsl:sequence>
    </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="current-as-num" select="number(replace($current, '-', ''))"></xsl:variable>
        <xsl:variable name="comp-as-num" select="number(replace($date-seq[1], '-', ''))"></xsl:variable>
        <xsl:choose>
          <xsl:when test="$current-as-num gt $comp-as-num">
            <xsl:sequence select="pi:get-latest-date(remove($date-seq, 1), $current)"></xsl:sequence>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="pi:get-latest-date(remove($date-seq, 1), $date-seq[1])"></xsl:sequence>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
      </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
