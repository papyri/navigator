<%@page
	import="java.util.*"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="tld/el-functions.tld" prefix="custom"%>
<%!
%>
<%@page import="util.jsp.el.Functions"%>
<%@page import="info.papyri.index.LuceneIndex"%>
<portlet:defineObjects />
<%
            String pub = "";
            String apis = "";
            if ("apis".equals(request.getParameter("xrefSchema")))
                apis = request.getParameter("xrefPattern");
            String series = (request.getParameter("pubnum_series") == null) ? ""
                    : request.getParameter("pubnum_series");
            pageContext.setAttribute("series", series);
            pageContext.setAttribute("apisInstitution", request
                    .getParameter("apisnum_inst"));
            String apisnum_num = request.getParameter("apisnum_num");
            if (apisnum_num == null) apisnum_num = "";
            String volume = (request.getParameter("pubnum_vol") == null) ? ""
                    : request.getParameter("pubnum_vol");
            String document = (request.getParameter("pubnum_doc") == null) ? ""
                    : request.getParameter("pubnum_doc");
            String selected = "selected=\"selected\"";
            if ("pub".equals(request.getParameter("xrefSchema"))) {
                pub = request.getParameter("xrefPattern");
            }

%>

<form action="<portlet:renderURL />" method="get"
	name="<portlet:namespace />xrefnew">
<table>
	<tr>
		<th class="rowheader">Publication Number:</th>

		<td colspan="2"><label for="pubnum_series"">Series:</label> <select
			id="pubnum_series" name="pubnum_series">
			<option value="">[Select]</option>
			<%
			Iterator<String> seriesIter = LuceneIndex.getIndexedSeries();                
			while (seriesIter.hasNext()) {
			    String seriesIndex = seriesIter.next();
			                pageContext.setAttribute("SERIES", seriesIndex);
			%>
			${custom:selectedOption(SERIES,series)}
			<%
			}
			%>
			<!-- 
				${custom:selectedOption("O.Berenike",series)}
				${custom:selectedOption("O.Mich.",series)}
				${custom:selectedOption("O.Oslo.",series)}
				${custom:selectedOption("P.Col.",series)}
				${custom:selectedOption("P.Corn.",series)}
				${custom:selectedOption("P.Fay.",series)}
				${custom:selectedOption("P.Hib.",series)}
				${custom:selectedOption("P.Lond.",series)}
				${custom:selectedOption("P.Mich.",series)}
				${custom:selectedOption("P.NYU",series)}
				${custom:selectedOption("P.O.I.",series)}
				${custom:selectedOption("P.Oslo.",series)}
				${custom:selectedOption("P.Oxy.",series)}
				${custom:selectedOption("P.Petra.",series)}
				${custom:selectedOption("P.Princ.",series)}
				${custom:selectedOption("P.Ross. Georg.",series)}
				${custom:selectedOption("P.Tebt.",series)}
				${custom:selectedOption("P.Wisc.",series)}
				${custom:selectedOption("P.Yale",series)}
				${custom:selectedOption("SB",series)}
				-->
		</select> <label for="pubnum_vol">Volume:</label> <input id="pubnum_vol"
			name="pubnum_vol" size="5" maxlength="25" class="forminput10"
			value="<%=volume %>" type="text" /> <label for="pubnum_doc">Document:</label>
		<input id="pubnum_doc" name="pubnum_doc" size="5" maxlength="25"
			class="forminput10" value="<%=document %>" type="text" /></td>
		<td><input type="hidden" name="xrefSchema" value="altPub" /> <input
			type="hidden" name="xrefPattern" value="placeholder" /> <input
			type="hidden" name="xrefOutput" value="json" /> <%
 if (request.getParameter("controlName") != null) {
 %> <input type="hidden" name="controlName"
			value="<%=request.getParameter("controlName").trim() %>" /> <%
 }
 %> <input type="submit" value="Go!" /></td>
	</tr>
</table>
<input type="hidden" name="exhibit" value="numbers" />
</form>
<form action="<portlet:renderURL />" method="get"
	name="<portlet:namespace />xrefnew">
<table>
	<tr>
		<th class="rowheader">APIS Number:</th>

		<td class="caption12" align="left">Institution: <select
			name="apisnum_inst">
			<option value="">[Select]</option>
			<%
			Iterator collections = LuceneIndex.getIndexedApisCollections();
			                while (collections.hasNext()) {
			                String collection = collections.next().toString();
			                pageContext.setAttribute("nextCollection", collection);
			                //Functions.selectedOption(collection,request.getParameter("apisnum_inst"));
			%>
			${custom:selectedOption(nextCollection,apisInstitution)}
			<%
			}
			%>
			<!-- 
				${custom:selectedOption("berenike",apisInstitution)}
				${custom:selectedOption("berkeley",apisInstitution)}
				${custom:selectedOption("chicago",apisInstitution)}
				${custom:selectedOption("columbia",apisInstitution)}
				${custom:selectedOption("duke",apisInstitution)}
				${custom:selectedOption("hermitage",apisInstitution)}
				${custom:selectedOption("michigan",apisInstitution)}
				${custom:selectedOption("nyu",apisInstitution)}
				${custom:selectedOption("oslo",apisInstitution)}
				${custom:selectedOption("perkins",apisInstitution)}
				${custom:selectedOption("petra",apisInstitution)}
				${custom:selectedOption("princeton",apisInstitution)}
				${custom:selectedOption("psr",apisInstitution)}
				${custom:selectedOption("pullman",apisInstitution)}
				${custom:selectedOption("sacramento",apisInstitution)}
				${custom:selectedOption("stanford",apisInstitution)}
				${custom:selectedOption("toronto",apisInstitution)}
				${custom:selectedOption("trimithis",apisInstitution)}
				${custom:selectedOption("upenn",apisInstitution)}
				${custom:selectedOption("wisconsin",apisInstitution)}
				${custom:selectedOption("yale",apisInstitution)}
				-->
		</select> <span style="font-weight: normal;">.apis.</span> <label
			for="apisnum_num">Number: <input id="apisnum_num"
			name="apisnum_num" size="5" maxlength="25" class="forminput10"
			value="<%=apisnum_num %>" type="text" /></td>
		<td><input type="hidden" name="xrefSchema" value="altApis" /> <input
			type="hidden" name="xrefPattern" value="placeholder" /> <input
			type="hidden" name="xrefOutput" value="json" />
			<input type="hidden" name="exhibit" value="numbers" />
			 <%
 if (request.getParameter("controlName") != null) {
 %> <input type="hidden" name="controlName"
			value="<%=request.getParameter("controlName").trim() %>" /> <%
 }
 %> <input type="submit" value="Go!" /></td>
	</tr>
</table>
<input type="hidden" name="exhibit" value="numbers" />
</form>
