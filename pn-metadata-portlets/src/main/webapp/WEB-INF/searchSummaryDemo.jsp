<%@ page language="java"%>
<%@page import="edu.columbia.apis.*,util.jsp.el.Functions,java.util.*,javax.portlet.*,edu.columbia.apis.servlet.*" pageEncoding="UTF-8"%>
<%@ page session="false" contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
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
            String provenance = request.getParameter("provenance");
            String keyword = request.getParameter("keyword");
            if (keyword == null) keyword = "";
            pageContext.setAttribute("PROVENANCE",provenance);
            String beginDate = request.getParameter("beginDate");
            if (beginDate == null) beginDate = "";
            String beginDateEra = request.getParameter("beginDateEra");
            pageContext.setAttribute("SUBMITTED_BEGIN_DATE_ERA",beginDateEra);
            String endDate = request.getParameter("endDate");
            if (endDate == null) endDate = "";
            String endDateEra = request.getParameter("endDateEra");
            pageContext.setAttribute("SUBMITTED_END_DATE_ERA",endDateEra);

%>

<table width="100%"><tr><td width="75%">
<form name="uimockup" action="" method="get">
<table>
	<tbody>
		<tr>  <th class="rowheader">Search All Fields:</th>
			<td colspan="2"><label for="keyword">Keyword:</label> <input id="keyword"
				name="keyword" value="<%=keyword %>" size="26" />
			<input id="chkIncludeTranslations" name="chkIncludeTranslations" value=""
				 type="checkbox" /><label for="chkIncludeTranslations">
			Include translations</label>
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
				id="beginDate" name="beginDate" class="forminput10" value="<%=beginDate %>" /> <select
				name="beginDateEra">
			${custom:selectedOption("CE",SUBMITTED_BEGIN_DATE_ERA)}
			${custom:selectedOption("BCE",SUBMITTED_BEGIN_DATE_ERA)}
			</select>
                  <label for="endDate">On or before:</label>
			<input size="5"
				id="endDate" name="endDate" class="forminput10" value="<%=endDate %>" />
				<select
				name="endDateEra">
			${custom:selectedOption("CE",SUBMITTED_END_DATE_ERA)}
			${custom:selectedOption("BCE",SUBMITTED_END_DATE_ERA)}
			</select>
            </td>
		</tr>
		<tr>
		<th class="rowheader">Publication Number:</th>

		<td colspan="2"><label for="pubnum_series">Series:</label> <select
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
                  <label for="apisnum_num">Number:</label>
			<input id="apisnum_num" name="apisnum_num" size="5" maxlength="25" class="forminput10"
				value="<%=apisnum_num %>" type="text" /> <span class="comment"
				style="font-weight: normal;">e.g."p163"</span></td>
		</tr>
	</tbody>
</table>
  <div id="metadata-search-controls">
    <input name="btnSearch" value="Go!" onclick="return true;"
	       type="submit" /> 
	<input name="btnClear" value="&nbsp; clear &nbsp;"
	       onclick="return false;" type="button" />
	<input type="hidden" name="exhibit" value="metadata" />
  </div>
	
</form>

            <%if (request.getParameter("exhibit") != null){ %>
    <table width="100%" class="summaryTable">
        <tr valign="top">
            <td>
                <div id="exhibit-control-panel"></div>
                <div id="exhibit-view-panel">

                <div class="item" ex:role="exhibit-lens" style="display:none">
                  <table>
                        <tbody>
	                    <tr>
	                      <td class="metadatalinks" rowspan="2">
	                        <b>Identifiers:</b>
	                        <ul>
	                          <li>APIS: <a ex:if-exists=".apis"  ex:href-subcontent="http://wwwapp.cc.columbia.edu/ldpd/app/apis/item?mode=item&key={{.apis}}" target="_blank"><span ex:content=".apis"></span></a></li>
	                          <li>HGV: <span ex:content=".hgv"></span></li>
	                          <li>DDBDP: <span ex:content=".ddbdp"></span></li>
	                        </ul>
	                      </td>
	                      <td class="publication">
	                        <b>Publication:</b>
	                        <br/>
	                        <span ex:content=".publication"></span>
	                      </td>
	                      <td class="date"><b>Date:</b><br/><span ex:content=".date"></span></td>
	                      <td class="provenance"><b>Provenance:</b><br/><span ex:content=".provenance"></span></td>
	                      <td class="title"><b>Title:</b><br/><span ex:content=".title"></span></td>
	                      <td class="translation"><a href="#">translation (EN)</a></td>
	                    </tr>
                        <tr>
                          <td class="author-archive"><b>Author/Archive:</b><br/></td>
                          <td class="ids">
                          <b>Inventory:</b>
                          <br/><span ex:content=".inventory"></span></td>
                          <td><b>Language:</b><br/><span  ex:content=".lang"></span></td>
                          <td class="image">
                            
                            <a ex:href-content=".image-url"><span ex:content=".image-url-label"></span></a>
                            
                          </td>
                          <td class="bl"><b>BL Post-Concordance:</b><br/>
                          <span ex:content=".BL"></span>
                          </td>
                        </tr>
                    </tbody>
                </table>
            </div>


                        <div ex:role="exhibit-view"
                        ex:viewClass="Exhibit.TileView"
                        ex:label="Summary"
                        ex:orders=".apis"
                        ex:possibleOrders=".apis, .publication, .inventory, .has-image, .date, .title, .lang, .bl, .has-translation"
                        ex:grouped="false"
                        >
                        
                     </div>
                     <div ex:role="exhibit-view"
                          ex:viewClass="Exhibit.ThumbnailView"
                          ex:Label="APIS Data"
                          ex:orders=".apis"
                          
                      >
                       <div ex:role="exhibit-lens" ex:template="apisTemplate"></div>
                     </div>
                     <div ex:role="exhibit-view"
                          ex:viewClass="Exhibit.ThumbnailView"
                          ex:Label="HGV Data"
                          ex:orders=".hgv"
                          
                      >
                       <div ex:role="exhibit-lens" ex:template="hgvTemplate"></div>
                     </div>
                 </div>
            </td>
        </tr>
    </table>
                <%} %>

</td>
<td width="20%">
            <%if (request.getParameter("exhibit") != null){ %>
                <div id="exhibit-browse-panel" ex:facets=".lang, .provenance, .apis-collection"></div>
                <%} %>
</td></tr>
</table>
<div class="item" id="apisTemplate" xmlns:ex="http://simile.mit.edu/2006/11/exhibit#" style="display:none;">
	<table class="singleSourceData">
		<tbody>
			<tr>
				<th class="metadatalinks" colspan="3">
					<span ex:if-not-exists=".apis" ex:content="'None'"></span>
					<span ex:content=".apis"></span>
				</th>
			</tr>
			<tr>
	        	<td class="publication">
	            	<b>Publication:</b>
	                <br/>
	                <span ex:content=".publication-apis"></span>
	            </td>
	            <td class="date"><b>Date:</b><br/><span ex:content=".date-apis"></span></td>
	            <td class="title"><b>Title:</b><br/><span ex:content=".title-apis"></span></td>
	        </tr>
            <tr>
            	<td class="ids">
                	<b>Inventory:</b>
                    <br/><span ex:content=".inventory"></span>
                </td>
	            <td class="provenance">
	            	<b>Provenance:</b>
	            	<br/>
	            	<span ex:content=".provenance-apis"></span>
	            </td>
                <td>
                	<b>Language:</b>
                	<br/>
                	<span  ex:content=".lang"></span>
               	</td>
            </tr>
            <tr>
            	<td colspan="3">
            			<span ex:content=".all-apis" ex:highlight="<%=keyword %>"></span>
           		</td>
            </tr>
        </tbody>
    </table>
</div>
<div id="hgvTemplate" style="display:none;" class="item" xmlns:ex="http://simile.mit.edu/2006/11/exhibit#">
  <table class="singleSourceData">
    <tbody>
      <tr>
        <th class="metadatalinks" colspan="2">
          <span ex:content=".hgv"></span>
	      <span ex:if-not-exist=".hgv">None</span>
	      (apis: <span ex:content=".apis"></span>)
	   </th>
      </tr>
      <tr>
	    <td class="publication">
	      <b>Publication:</b>
	      <br/>
	      <span ex:content=".publication-hgv"></span>
	    </td>
	    <td class="title">
	      <b>Title:</b>
	      <br/>
	      <span ex:content=".title-hgv"></span>
	    </td>
	  </tr>
      <tr>
	    <td class="date">
	      <b>Date:</b>
	      <br/>
	      <span ex:content=".date-hgv"></span>
	    </td>
	    <td class="provenance">
	      <b>Provenance:</b>
	      <br/>
	      <span ex:content=".provenance-hgv"></span>
	    </td>
      </tr>
      <tr>
        <td colspan="2">
          <span ex:content=".all-hgv" ex:highlight="<%=keyword %>"></span>
        </td>
      </tr>
    </tbody>
  </table>
  </div>