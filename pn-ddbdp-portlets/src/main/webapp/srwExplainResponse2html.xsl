<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:srw="http://www.loc.gov/zing/srw/" xmlns:zr="http://explain.z3950.org/dtd/2.0/" xmlns:diag="http://www.loc.gov/zing/srw/diagnostic/" xmlns:refb="http://refbase.net/">
<!--        Author:  Rob Sanderson (azaroth@liv.ac.uk)
           Version:  0.6
      Last Updated:  27/11/2003
           Licence:  GPL
       Modified by:  Matthias Steffens (refbase@extracts.de) -->
	<xsl:output method="html" />
	<xsl:template match="/srw:explainResponse">
	<html>
		<head>
			<title>
				<xsl:value-of select="srw:record/srw:recordData/zr:explain/zr:databaseInfo/zr:title" />
			</title>
			<meta http-equiv="Content-Style-Type" content="text/css" />
			<link rel="stylesheet" href="css/style.css" type="text/css" title="CSS Definition" />
			<style type="text/css">
/*				H2 {font-family: sans-serif; color: #990000; background-color: #CCCCCC; padding-top: 10px; padding-bottom: 10px; border: 1px solid #CCCCCC }*/
/*				H3 {font-family: sans-serif; color: #990000; text-indent: 5px; border-left: solid 1px #CCCCCC; border-top: solid 1px #CCCCCC; padding-top: 5px }*/
/*				.paramTable { vertical-align: top; border: 1px solid; padding: 3px; border-collapse: collapse }*/
/*				.paramTable TD {border: 1px solid}*/
/*				TH {border: 1px solid; background-color: #eeeeff}*/
			</style>
			<script language="JavaScript" type="text/javascript">
				<xsl:text>
					function mungeForm() {
						inform = document.CQLForm;
						outform = document.SRUForm;
						max = inform.maxIndex.value;
						cql = "";
						prevIdx = 0;
						// Step through elements in form to create CQL
						for (var idx = 1; idx &lt;= max; idx++) {
							term = inform["term"+idx].value;
							if (term) {
								if (prevIdx) {
									cql += " " + inform["bool" + prevIdx].value + " ";
								}
								if (term.indexOf(' ')) {
									term = '"' + term + '"';
								}
								cql += inform["index" + idx].value + " " + inform["relat" + idx].value + " " + term;
								prevIdx = idx;
							}
						}
						if (!cql) {
							alert("At least one term is required to search.");
							return false;
						}
						outform.query.value = cql;
						outform.submit();
						return false;
					}

					function mungeScanForm() {
						inform = document.ScanIndexes;
						outform = document.ScanSubmit;
						index = inform.scanIndex.value;
						term = inform.term.value;
						relat = inform.relat.value;
						outform.scanClause.value = index + " " + relat +" \"" + term + "\""
						outform.submit();
						return false;
					}
				</xsl:text>
			</script>
		</head>
	<body bgcolor="#FFFFFF">
		<!-- page header: -->
		<table align="center" border="0" cellpadding="0" cellspacing="10" width="95%" summary="This holds the title logo and info">
			<tr>
				<td valign="bottom" rowspan="2" align="left" width="120">
				<a>
					<xsl:attribute name="href">
						<!-- note that the logo should point to the URL given in '$hostInstitutionURL' but this information is currently not available in the SRU explain response -->
						<xsl:value-of select='srw:record/srw:recordData/zr:explain/zr:databaseInfo/zr:links/zr:link[@type="www"]' />
					</xsl:attribute>
					<img src="img/logo.png" alt="" border="0" />
				</a>
				</td>
				<td>
					<h2><xsl:value-of select="srw:record/srw:recordData/zr:explain/zr:databaseInfo/zr:title" /></h2>
					<span class="smallup">
						<a href="index.php" title="go to main page">Home</a><xsl:text> | </xsl:text>
						<a href="show.php?records=all" title="show all records in the database">Show All</a><xsl:text> | </xsl:text>
						<a href="simple_search.php" title="search the main fields of the database">Simple Search</a><xsl:text> | </xsl:text>
						<a href="advanced_search.php" title="search all fields of the database">Advanced Search</a>
					</span>
				</td>
				<td class="small" align="right" valign="middle"><br /></td>
			</tr>
			<tr>
				<td>Search the SRU web service:</td>
				<td class="small" align="right" valign="middle"><a href="user_login.php" title="login to the database">Login</a></td>
			</tr>
		</table>
		<hr align="center" width="95%" />
		<!-- search forms: -->
		<xsl:apply-templates select="srw:diagnostics" />
		<table align="center" border="0" cellpadding="2" cellspacing="5" width="95%" summary="This table holds the search form">
			<tr>
				<td></td>
				<td>
					<form name="CQLForm" onsubmit="return mungeForm();">
						<table align="center" border="0" cellpadding="2" cellspacing="5" class="paramTable">
							<tr>
								<th>Index</th>
								<th>Relation</th>
								<th>Term</th>
								<th>Boolean</th>
							</tr>
							<input type="hidden" name="maxIndex">
								<xsl:attribute name="value">
									<xsl:value-of select="count(srw:record/srw:recordData/zr:explain/zr:indexInfo/zr:index)" />
								</xsl:attribute>
							</input>
							<xsl:for-each select="srw:record/srw:recordData/zr:explain/zr:indexInfo/zr:index">
								<!-- <xsl:sort select="." /> -->
								<tr>
									<td align="right">
										<b><xsl:value-of select="zr:map[1]/zr:name/@set" />.<xsl:value-of select="zr:map[1]/zr:name" /></b>
										<input type="hidden">
											<xsl:attribute name="name">index<xsl:value-of select="position()" /></xsl:attribute>
											<xsl:attribute name="value"><xsl:value-of select="zr:map[1]/zr:name/@set" />.<xsl:value-of select="zr:map[1]/zr:name" /></xsl:attribute>
										</input>
									</td>
									<td>
										<select>
											<xsl:attribute name="name">relat<xsl:value-of select="position()" /></xsl:attribute>
											<option value="=">=</option>
											<option value="exact">exact</option>
											<option value="any">any</option>
											<option value="all">all</option>
											<option value="&lt;">&lt;</option>
											<option value="&gt;">&gt;</option>
											<option value="&lt;=">&lt;=</option>
											<option value="&gt;=">&gt;=</option>
											<option value="&lt;&gt;">not</option>
										</select>
									</td>
									<td>
										<input type="text" value="">
											<xsl:attribute name="name">term<xsl:value-of select="position()" /></xsl:attribute>
											<xsl:attribute name="title"><xsl:value-of select="zr:title" /> (refbase: <xsl:value-of select="substring-after(@refb:index,'-')" />)</xsl:attribute>
										</input>
									</td>
									<td>
										<select>
											<xsl:attribute name="name">bool<xsl:value-of select="position()" /></xsl:attribute>
											<option value="and">and</option>
											<option value="or">or</option>
											<option value="not">not</option>
										</select>
									</td>
								</tr>
							</xsl:for-each>
						</table>
					</form>
					<form method="GET" name="SRUForm" onsubmit="return mungeForm();">
						<input type="hidden" name="query" value="" />
						<input type="hidden" name="version" value="1.1" />
						<input type="hidden" name="operation" value="searchRetrieve" />
						<table align="center" border="0" cellpadding="2" cellspacing="5">
							<tr>
								<td>
									<b>Record Schema:</b>
								</td>
								<td>
									<select name="recordSchema">
										<xsl:for-each select="srw:record/srw:recordData/zr:explain/zr:schemaInfo/zr:schema">
											<option>
												<xsl:attribute name="value">
													<xsl:value-of select="@identifier" />
												</xsl:attribute>
												<xsl:value-of select="zr:title" />
											</option>
										</xsl:for-each>
									</select>
								</td>
							</tr>
							<tr>
								<td>
									<b>Record Packing:</b>
								</td>
								<td>
									<select name="recordPacking">
										<xsl:choose>
											<xsl:when test='srw:record/srw:recordData/zr:explain/zr:configInfo/zr:setting[@type="recordPacking"]'>
												<option>
													<xsl:attribute name="value">
														<xsl:value-of select='srw:record/srw:recordData/zr:explain/zr:configInfo/zr:setting[@type="recordPacking"]' />
													</xsl:attribute>
													<xsl:value-of select='srw:record/srw:recordData/zr:explain/zr:configInfo/zr:setting[@type="recordPacking"]' />
												</option>
											</xsl:when>
											<xsl:otherwise>
												<option value="xml">xml</option>
												<option value="string">string</option>
											</xsl:otherwise>
										</xsl:choose>
									</select>
								</td>
							</tr>
							<tr>
								<td>
									<b>Number of Records:</b>
								</td>
								<td>
									<input type="text" name="maximumRecords">
										<xsl:attribute name="value">
											<xsl:choose>
												<xsl:when test='srw:record/srw:recordData/zr:explain/zr:configInfo/zr:default[@type="numberOfRecords"]'>
													<xsl:value-of select='srw:record/srw:recordData/zr:explain/zr:configInfo/zr:default[@type="numberOfRecords"]' />
												</xsl:when>
												<xsl:otherwise>
													<xsl:text>1</xsl:text>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:attribute>
									</input>
								</td>
							</tr>
							<tr>
								<td>
									<b>Record Position:</b>
								</td>
								<td>
									<input type="text" name="startRecord" value="1" />
								</td>
							</tr>
							<xsl:if test='srw:record/srw:recordData/zr:explain/zr:configInfo/zr:supports[@type="resultSets"] != "false"'>
								<tr>
									<td>
										<b>Result Set TTL:</b>
									</td>
									<td>
										<input type="text" name="resultSetTTL" value="0" />
									</td>
								</tr>
							</xsl:if>
							<xsl:if test='srw:record/srw:recordData/zr:explain/zr:configInfo/zr:supports[@type="recordXPath"] != "false"'>
								<tr>
									<td>
										<b>Record XPath:</b>
									</td>
									<td>
										<input type="text" name="recordXPath" value="" />
									</td>
								</tr>
							</xsl:if>
							<xsl:if test='srw:record/srw:recordData/zr:explain/zr:configInfo/zr:supports[@type="sort"] != "false"'>
								<tr>
									<td>
										<b>Sort Keys:</b>
									</td>
									<td>
										<input type="text" name="sortKeys" value="" />
									</td>
								</tr>
							</xsl:if>
							<tr>
								<td colspan="2" align="right">
									<input type="submit" value="Search" onclick="return mungeForm();" />
								</td>
							</tr>
						</table>
					</form>
				</td>
<!-- 
				<td valign="top">
					<h3>Browse</h3>
<!~~ Some browsers won't display when forms inside tables :( ~~>
					<form name="ScanIndexes" onsubmit="return mungeScanForm();">
						<table>
							<tr>
								<th>Index</th>
								<th>Relation</th>
								<th>Term</th>
								<th>Boolean</th>
							</tr>
							<tr>
								<td>
									<select name="scanIndex">
										<xsl:for-each select="srw:record/srw:recordData/zr:explain/zr:indexInfo/zr:index">
											<xsl:sort select="." />
											<option>
												<xsl:attribute name="value"><xsl:value-of select="zr:map[1]/zr:name/@set" />.<xsl:value-of select="zr:map[1]/zr:name" /></xsl:attribute>
												<xsl:value-of select="zr:title" />
											</option>
										</xsl:for-each>
									</select>
								</td>
								<td>
									<select name="relat">
										<option value="=">=</option>
										<option value="exact">exact</option>
										<option value="any">any</option>
										<option value="all">all</option>
										<option value="&lt;">&lt;</option>
										<option value="&gt;">&gt;</option>
										<option value="&lt;=">&lt;=</option>
										<option value="&gt;=">&gt;=</option>
										<option value="&lt;&gt;">not</option>
									</select>
								</td>
								<td>
									<input name="term" type="text" value="" />
								</td>
							</tr>
						</table>
					</form>
					<form name="ScanSubmit" method="GET">
						<xsl:attribute name="action">http://<xsl:value-of select="srw:record/srw:recordData/zr:explain/zr:serverInfo/zr:host" />:<xsl:value-of select="srw:record/srw:recordData/zr:explain/zr:serverInfo/zr:port" />/<xsl:value-of select="srw:record/srw:recordData/zr:explain/zr:serverInfo/zr:database" /></xsl:attribute>
						<table>
							<tr>
								<td>
									<b>Response Position:</b>
								</td>
								<td>
									<input type="text" name="responsePosition" value="1" size="5" />
								</td>
							</tr>
							<tr>
								<td>
									<b>Maximum Terms:</b>
								</td>
								<td>
									<input type="text" name="maximumTerms" value="20" size="5" />
								</td>
							</tr>
							<tr>
								<td colspan="2">
									<input type="submit" value="Browse" onclick="return mungeScanForm();" />
								</td>
							</tr>
						</table>
						<input type="hidden" name="operation" value="scan" />
						<input type="hidden" name="scanClause" value="" />
						<input type="hidden" name="version" value="1.1" />
					</form>
				</td>
 -->
			</tr>
			<tr>
				<td valign="top">
					<b>Help:</b>
				</td>
				<td valign="top">
					<xsl:text>This form gets dynamically created from the </xsl:text><a href="http://www.loc.gov/standards/sru/" target="top">SRU</a><xsl:text> explain response, it exposes indexes and capabilities offered by the refbase SRU server.</xsl:text>
					<xsl:text> Queries must be specified as </xsl:text><a href="http://www.loc.gov/standards/sru/cql/index.html" target="top">CQL (Common Query Language)</a><xsl:text>, an introduction to CQL is given </xsl:text><a href="http://zing.z3950.org/cql/intro.html" target="top">here</a><xsl:text>.</xsl:text>
					<xsl:text> Please see the </xsl:text><a href="http://sru.refbase.net/" target="top">refbase online documentation</a>
					<xsl:text> for more information about the SRU web service.</xsl:text>
				</td>
			</tr>
		</table>
		<!-- page footer: -->
		<hr align="center" width="95%" />
		<table align="center" border="0" cellpadding="0" cellspacing="10" width="95%" summary="This table holds the footer">
			<tr>
				<td class="small" width="105"><a href="index.php" title="go to main page">Home</a></td>
				<td class="small" align="center">
					<a href="show.php?records=all" title="show all records in the database">Show All</a><xsl:text> | </xsl:text>
					<a href="simple_search.php" title="search the main fields of the database">Simple Search</a><xsl:text> | </xsl:text>
					<a href="advanced_search.php" title="search all fields of the database">Advanced Search</a><xsl:text> | </xsl:text>
					<a href="library_search.php">
						<xsl:attribute name="title">
							<xsl:text>search the library of the </xsl:text><xsl:value-of select="srw:record/srw:recordData/zr:explain/zr:databaseInfo/zr:author" />
						</xsl:attribute>
						<xsl:text>Library Search</xsl:text>
					</a>
				</td>
				<td class="small" align="right" width="105"><!-- <xsl:value-of select="date:date()" /> --></td>
			</tr>
			<tr>
				<td class="small" width="105"><a href="http://wiki.refbase.net/" title="display help">Help</a></td>
				<td class="small" align="center">
					<a href="show.php" title="display details for a particular record by entering its database serial number">Show Record</a><xsl:text> | </xsl:text>
					<a href="extract.php" title="extract citations from a text and build an appropriate reference list">Extract Citations</a>
				</td>
				<td class="small" align="right" width="105"><!-- <xsl:value-of select="date:time()" /> --></td>
			</tr>
		</table>
		</body>
		</html>
	</xsl:template>
	<xsl:template match="srw:diagnostics">
		<h3>Diagnostics</h3>
		<xsl:apply-templates />
	</xsl:template>
	<xsl:template match="diag:diagnostic">
		<table>
			<xsl:apply-templates />
		</table>
	</xsl:template>
	<xsl:template match="diag:code">
		<tr>
			<td>
				<b>Code:</b>
			</td>
			<td>
				<xsl:value-of select="." />
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="diag:message">
		<tr>
			<td>
				<b>Message:</b>
			</td>
			<td>
				<xsl:value-of select="." />
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="diag:details">
		<tr>
			<td>
				<b>Details:</b>
			</td>
			<td>
				<xsl:value-of select="." />
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
