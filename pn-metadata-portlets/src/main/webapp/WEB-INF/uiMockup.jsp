<%@page import="edu.columbia.apis.*,edu.columbia.apis.jsp.el.Functions,java.util.*,javax.portlet.*,edu.columbia.apis.servlet.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="tld/el-functions.tld" prefix="custom"%>
<portlet:defineObjects/>
<%
            String pub = "";
            String apis = "";
            String invnum_coll = request.getParameter("invnum_coll");
            pageContext.setAttribute("INVNUM_COLL",invnum_coll);
            String institution = request.getParameter("institution");
            pageContext.setAttribute("INSTITUTION",institution);
            if ("apis".equals(request.getParameter("xrefSchema")))
                apis = request.getParameter("xrefPattern");
            String series = (request.getParameter("pubnum_series") == null) ? ""
                    : request.getParameter("pubnum_series");
            pageContext.setAttribute("SERIES", series);
            pageContext.setAttribute("APISNUM_INST", request
                    .getParameter("apisnum_inst"));
            String volume = (request.getParameter("pubnum_vol") == null) ? ""
                    : request.getParameter("pubnum_vol");
            String document = (request.getParameter("pubnum_doc") == null) ? ""
                    : request.getParameter("pubnum_doc");
            String selected = "selected=\"selected\"";
            if ("pub".equals(request.getParameter("xrefSchema"))) {
                pub = request.getParameter("xrefPattern");
            }
            String provenance = request.getParameter("provenance");
            pageContext.setAttribute("PROVENANCE",provenance);
            String beginDate = request.getParameter("beginDate");
            String beginDateEra = request.getParameter("beginDateEra");
            String endDate = request.getParameter("endDate");
            String endDateEra = request.getParameter("endDateEra");

%>

<form name="uimockup" action="" method="get">
<table>
	<tbody>
		<tr>  <th class="rowheader">Search All Fields:</th>
			<td colspan="2"><label for="keyword">Keyword:</label> <input id="keyword"
				name="keyword" value="" size="26" />
			<input id="chkIncludeTranslations" name="chkIncludeTranslations" value=""
				 type="checkbox" /><label for="chkIncludeTranslations">
			Include translations</label>
			</td>
			<td rowspan="7">
            <%if (request.getParameter("exhibit") != null){ %>
                <div id="exhibit-browse-panel" ex:facets=".lang, .provenance, .apis-collection"></div>
                <%} %>
			</td>
		</tr>
		<tr>
		  <th class="rowheader">Provenance:</th>
		  <td colspan="2">
		    <select name="provenance" id="provenance">
			<option value="">[Select]</option>
			<%
			Iterator provenanceVals = JSONServlet.getIndexedProvenanceValues();
			                while (provenanceVals.hasNext()) {
			                String next = provenanceVals.next().toString();
			                pageContext.setAttribute("provenance", next);
			%>
			${custom:selectedOption(provenance,PROVENANCE)}
			<%
			}
			%>
		    </select>
		  </td>
		</tr>
		<tr> <th class="rowheader">Limit by Collection:</th>
			<td colspan="2"><label for="institution">Collection:</label>
                  <select name="institution" id="institution">
			<option value="">[Select]</option>
			<%
			Iterator collections = JSONServlet.getIndexedApisCollections();
			                while (collections.hasNext()) {
			                String next = collections.next().toString();
			                pageContext.setAttribute("institution", next);
			                //Functions.selectedOption(collection,request.getParameter("apisnum_inst"));
			%>
			${custom:selectedOption(institution,INSTITUION)}
			<%
			}
			%>
			</select></td>
		</tr>
		<tr>
			<th class="rowheader">Limit by
			Date:</th>
			<td colspan="2">
			<label for="beginDate">On or after:</label>
			<input size="5"
				id="beginDate" name="beginDate" class="forminput10" value="" /> <select
				name="beginDateEra">
				<option value="CE" selected="selected">C.E.</option>

				<option value="BCE">B.C.E.</option>
			</select>
                  <label for="endDate">On or before:</label>
			<input size="5"
				id="endDate" name="endDate" class="forminput10" value="" />
				<select
				name="endDateEra">
				<option value="CE" selected="selected">C.E.</option>
				<option value="BCE">B.C.E.</option>
			</select>
            </td>
		</tr>
		<tr>
		<th class="rowheader">Publication Number:</th>

		<td colspan="2"><label for="pubnum_series"">Series:</label> <select
			id="pubnum_series" name="pubnum_series">
			<option value="">[Select]</option>
			<%
			Iterator<String> seriesIter = JSONServlet.getIndexedSeries();                
			while (seriesIter.hasNext()) {
			    String seriesIndex = seriesIter.next();
			                pageContext.setAttribute("series", seriesIndex);
			%>
			${custom:selectedOption(series,SERIES)}
			<%
			}
			%>
		</select> <label for="pubnum_vol">Volume:</label> <input id="pubnum_vol"
			name="pubnum_vol" size="5" maxlength="25" class="forminput10"
			value="<%=volume %>" type="text" /> <label for="pubnum_doc">Document:</label>
		<input id="pubnum_doc" name="pubnum_doc" size="5" maxlength="25"
			class="forminput10" value="<%=document %>" type="text" />
                       </td>
		</tr>
		<tr>
			<th class="rowheader">Inventory Number:</th>

			<td colspan="2">Collection:
			<select name="invnum_coll">
			<option value="">[Select]</option>
			<%
			collections = JSONServlet.getIndexedApisCollections();
			                while (collections.hasNext()) {
			                String next = collections.next().toString();
			                pageContext.setAttribute("invnum_coll", next);
			                //Functions.selectedOption(collection,request.getParameter("apisnum_inst"));
			%>
			${custom:selectedOption(invnum_coll,INVNUM_COLL)}
			<%
			}
			%>

			</select>
                  <label for="invnum_num" >Number:</label>
			<input id="invnum_num" name="invnum_num" size="5" maxlength="25"
				value="" type="text" /> <span class="comment"
				style="font-weight: normal;">e.g."186a"</span></td>
		</tr>
		<tr>
			<th class="rowheader">APIS Number:</th>

			<td class="caption12" align="left">Institution:
			<select name="apisnum_inst">
			<option value="">[Select]</option>
			<%
			collections = JSONServlet.getIndexedApisCollections();
			                while (collections.hasNext()) {
			                String next = collections.next().toString();
			                pageContext.setAttribute("apisnum_inst", next);
			                //Functions.selectedOption(collection,request.getParameter("apisnum_inst"));
			%>
			${custom:selectedOption(apisnum_inst,APISNUM_INST)}
			<%
			}
			%>
			</select> <span style="font-weight: normal;">.apis.</span>
                  <label for="apisnum_num">Number:
			<input id="apisnum_num" name="apisnum_num" size="5" maxlength="25" class="forminput10"
				value="" type="text" /> <span class="comment"
				style="font-weight: normal;">e.g."p163"</span></td>
		</tr>
	</tbody>
</table>
<div><input name="btnSearch" value="Go!" onclick="return true;"
	type="submit" /> <input name="btnClear" value="&nbsp; clear &nbsp;"
	onclick="return false;" type="button" /></div>
	<input type="hidden" name="exhibit" value="metadata" />
</form>

<c:set var="includeName" value="resultMockup" />
<c:set var="attName" value="javax.portlet.request" />
<%
String pageName = renderRequest.getAttribute("resultMockup").toString();
System.out.println("pageName=\"" + pageName + "\"");
pageContext.setAttribute("pageName",pageName);
%>
<jsp:include page="${pageName}"/>
wth: <c:out value="${pageName}"></c:out>
