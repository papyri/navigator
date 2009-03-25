<%@page language="java" session="false" contentType="text/html" import="info.papyri.navigator.portlet.NavigatorPortlet,org.apache.lucene.search.*,org.apache.lucene.index.*,org.apache.lucene.document.*,util.jsp.el.Functions,java.util.*,javax.portlet.*,info.papyri.metadata.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@taglib uri="tld/el-functions.tld" prefix="custom"%><%@page import="info.papyri.index.LuceneIndex"%>
<portlet:defineObjects/>
<portlet:defineObjects/>
<%!
Term cnTemplate = new Term(CoreMetadataFields.DOC_ID,"");
%>
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
<div>
<form name="uimockup" action="" method="get">
<table>
	<tbody>
		<tr>  <th class="rowheader">Search All Fields:</th>
			<td colspan="2"><label for="keyword">Keyword:</label> <input id="keyword"
				name="keyword" value="<%=keyword %>" size="26" />
			<input id="chkIncludeTranslations" name="chkIncludeTranslations" value=""
				 type="checkbox" disabled="disabled" /><label for="chkIncludeTranslations">
			Include translations</label>
			</td>
		</tr>
		<tr>
		  <th class="rowheader">Provenance:</th>
		  <td colspan="2">
		    <select name="provenance" id="provenance">
			<option value="">[Select]</option>
			<%
			Iterator provenanceVals = LuceneIndex.getIndexedProvenanceValues();
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
			Iterator collections = LuceneIndex.getIndexedApisCollections();
			                while (collections.hasNext()) {
			                String next = collections.next().toString();
			                pageContext.setAttribute("institution", next);
			                //Functions.selectedOption(collection,request.getParameter("apisnum_inst"));
			%>
			${custom:selectedOption(institution,INSTITUTION)}
			<%
			}
			%>
			</select></td>
		</tr>
		<tr>
			<th class="rowheader">Limit by Date:</th>
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
			Iterator<String> seriesIter = LuceneIndex.getIndexedSeries();                
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
			collections = LuceneIndex.getIndexedApisCollections();
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
			collections = LuceneIndex.getIndexedApisCollections();
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
		<tr>
		  <th class="rowheader" rowspan="2">Image / Translation:</th>
		  <td>
		    <label for="req_img">Check to require documents with images:</label>
		    <input type="checkbox" name="req_img" id="req_img" <%=("on".equals(request.getParameter("req_img")))?"checked=\"checked\"":"" %> />
		  </td>
		</tr>
		<tr>
		  <td>
		    <label for="req_img">Check to require documents with translation:</label>
		    <input type="checkbox" name="req_trans" id="req_trans" disabled="disabled"/>
		  </td>
		</tr>
	</tbody>
</table>
  <div id="metadata-search-controls">
    <input name="btnSearch" value="Go!" onclick="return true;"
	       type="submit" /> 
	<input name="btnClear" value="&nbsp; clear &nbsp;"
	       onclick="clearForm(this.form);" type="button" />
	<input type="hidden" name="exhibit" value="metadata" />
  </div>
	
</form>
<%
int pageN = 0;
String pageS = request.getParameter("page");
if (pageS != null && pageS.matches("^\\d+$")){
    pageN = Integer.parseInt(pageS);
}
int offset = 25 * pageN;
int numResults = 0;
int numPages = 0;
if (renderRequest.getAttribute(NavigatorPortlet.XREF_NUM_RESULTS) != null){
    numResults = ((Integer)renderRequest.getAttribute(NavigatorPortlet.XREF_NUM_RESULTS)).intValue();
    numPages = (numResults % 25 == 0)?(numResults / 25):(numResults/25 + 1);
}
String url = renderRequest.getAttribute(NavigatorPortlet.XREF_REQ_URL).toString();
StringBuffer queryBuf = new StringBuffer();
Enumeration parmNames = renderRequest.getParameterNames();
while (parmNames.hasMoreElements()){
    String parm = parmNames.nextElement().toString();
    if (parm.equals("page")) continue;
    String [] vals = renderRequest.getParameterValues(parm);
    for (int i=0;i<vals.length;i++){
        if (i > 0) queryBuf.append("&amp;");
        queryBuf.append(parm);
        queryBuf.append('=');
        queryBuf.append(vals[i]);
    }
    if (parmNames.hasMoreElements()) queryBuf.append("&amp;");
}
String query = queryBuf.toString();
%>
    <table width="100%" class="metadata">
        <tr valign="top">
            <td>
                  <table>
                  <thead>
                      <caption colspan="6">
                      <% if (numResults == 0) {%>
                      Search Results (0 Total Matches)
                      <% }else{ %>
                      Search Results <%=offset+1 %>-<%=offset+Math.min(25,numResults - offset) %> (<%=numResults %> Total Matches)
                      <%} %>
                      </caption>
                      <tr>
                      <td colspan="6" align="center">Go to Page:&nbsp;
                      <% for(int i=0;i<numPages;i++){
                         if (pageN == i){
                      %>
                      <span style="font-size:13px;font-weight:bold;"><%=i %></span>&nbsp;
                      <%}
                         else {
                      %>
                      <a href="<%=url + "?" + query + "&amp;page=" + i%>" ><%=i %></a>&nbsp;
                      <%}
                         }
                         %>
                      </td>
                      </tr>
                  </thead>

            <%if (renderRequest.getAttribute(NavigatorPortlet.XREF_RESULTS) != null){
                Hits results = (Hits)renderRequest.getAttribute(NavigatorPortlet.XREF_RESULTS);
                Iterator<Hit> xrefHits = results.iterator();
                for (int i =0;i < offset; i++){
                    if (xrefHits.hasNext()) xrefHits.next();
                }
                for (int pageDocIndex = 0; pageDocIndex < 25; pageDocIndex++){
                    if (!xrefHits.hasNext()) break;
                    Hit hit = xrefHits.next();
                    org.apache.lucene.document.Document doc = hit.getDocument();
                    String apisId = hit.get(CoreMetadataFields.DOC_ID);
                    String apisDisplay = apisId;
                    String [] xrefs = doc.getValues(CoreMetadataFields.XREFS);
                    if(xrefs==null)xrefs = new String[0];
                    HashSet<String> hgvList = new HashSet<String>(xrefs.length);
                    HashSet<String> ddbList = new HashSet<String>(xrefs.length);
                    for(String xref:xrefs){
                        if(xref.startsWith(NamespacePrefixes.HGV))hgvList.add(xref);
                        if(xref.startsWith(NamespacePrefixes.DDBDP))ddbList.add(xref);
                    }
                    String [] hgvId = hgvList.toArray(hgvId);
                    String [] ddbId = ddbList.toArray(ddbId);
                    String [] publication = doc.getValues(CoreMetadataFields.BIBL_PUB);
                    if (publication == null)publication = new String[0];
                    Document apisDoc = null;
                    Document [] hgvDoc = new Document [hgvId.length];
                    String title = "";
                    String docInv = "";
                    String archive = "";
                    boolean apisImg = false;
                    boolean hgvImg = false;
                    StringBuffer docProvenance = new StringBuffer();
                    StringBuffer lang = new StringBuffer();
                    ArrayList<String> dates = new ArrayList<String>();
                    ArrayList<String> bl = new ArrayList<String>();
                    if (apisId == null || apisId.startsWith("none")){
                        apisDisplay = "none";   
                    } else {
                        apisDoc = ((Hit)LuceneIndex.SEARCH_COL.search(new TermQuery(cnTemplate.createTerm(apisId))).iterator().next()).getDocument();
                        if (apisDoc.get(CoreMetadataFields.TITLE) != null) title = apisDoc.get(CoreMetadataFields.TITLE);
                        if (apisDoc.get(CoreMetadataFields.PROVENANCE) != null) docProvenance.append(apisDoc.get(CoreMetadataFields.PROVENANCE));
                        if (apisDoc.get(CoreMetadataFields.LANG) != null) lang.append(apisDoc.get(CoreMetadataFields.LANG));
                        if (apisDoc.get(CoreMetadataFields.INV) != null) docInv = apisDoc.get(CoreMetadataFields.INV);
                        if (apisDoc.get(CoreMetadataFields.IMG_URL) != null) apisImg = true;
                        if(apisDoc.get(CoreMetadataFields.DATE1_D) != null){
                            if(apisDoc.get(CoreMetadataFields.DATE2_D) != null){
                                dates.add(apisDoc.get(CoreMetadataFields.DATE1_D) + " - " + apisDoc.get(CoreMetadataFields.DATE2_D) + " (apis)");
                            }
                            else{
                                dates.add(apisDoc.get(CoreMetadataFields.DATE1_D) + " (apis)");
                            }
                        }
                    }
                    
                    
                    for (int j=0;j<hgvId.length;j++){
                        hgvDoc[j] = ((Hit)LuceneIndex.SEARCH_HGV.search(new TermQuery(cnTemplate.createTerm(hgvId[j]))).iterator().next()).getDocument();
                        
                    }
                    if (hgvDoc.length != 0){
                        title = hgvDoc[0].get(CoreMetadataFields.TITLE);
                        for (Document hgv: hgvDoc){
                            String p = hgv.get(CoreMetadataFields.PROVENANCE);
                            String l = hgv.get(CoreMetadataFields.LANG);
                            if ( p != null && docProvenance.indexOf(p) == -1){
                              if (docProvenance.length() > 0) docProvenance.append(';');
                              docProvenance.append(hgv.get(CoreMetadataFields.PROVENANCE));
                            }
                            if (hgv.get(CoreMetadataFields.IMG_URL) != null && !hgvImg) hgvImg = true;
                            if (l != null && lang.indexOf(l) == -1){
                              if (lang.length() > 0) lang.append(';');
                              lang.append(hgv.get(CoreMetadataFields.LANG));
                            }
                            if(hgv.get(CoreMetadataFields.DATE1_D) != null){
                                if(hgv.get(CoreMetadataFields.DATE2_D) != null){
                                    dates.add(hgv.get(CoreMetadataFields.DATE1_D) + " - " + hgv.get(CoreMetadataFields.DATE2_D) + " (hgv)");
                                }
                                else{
                                    dates.add(hgv.get(CoreMetadataFields.DATE1_D) + " (hgv)");
                                }
                            }
                            if (hgv.get(CoreMetadataFields.PUB_ABOUT) != null){
                                bl.add(hgv.get(CoreMetadataFields.PUB_ABOUT) + ";");
                            }
                        }
                    }
                    
                %>
                        <tbody class="searchresult" <%=(pageDocIndex % 2 != 0)?"style=\"background-color:#f6f5e0;color:#393929;\"":"" %>>
	                    <tr>
	                      <td class="metadatalinks" rowspan="2">
	                        <b>Identifiers:</b>
	                        <ul>
	                          <li>APIS: <a href="http://wwwapp.cc.columbia.edu/ldpd/app/apis/item?mode=item&key=<%=apisId %>" target="_blank"><span><%=apisDisplay %></span></a><a href="">detail view</a></li>
	                          <li>HGV:
	                           <% for (int j =0;j<hgvId.length;j++){ %>
	                           <span><%=hgvId[j] %></span><br/>
	                           <%} %>
	                           </li>
	                          <li>DDBDP:
	                           <% for (int j =0;j<ddbId.length;j++){ %>
	                           <span><%=ddbId[j] %></span><br/>
	                           <%} %>
	                           </li>
	                        </ul>
	                      </td>
	                      <td class="publication" rowspan="2">
	                        <b>Publication:</b>
	                        <br/>
	                           <% for (int j =0;j<publication.length;j++){ %>
	                           <span><%=publication[j] %></span><br/>
	                           <%} %>
	                      </td>
	                      <td class="date"><b>Date:</b><br/>
	                      <% Iterator<String> datesIter = dates.iterator();
	                      while(datesIter.hasNext()){%><span><%=datesIter.next() %></span><br/>
	                      <%} %></td>
	                      <td class="provenance"><b>Provenance:</b><br/><span><%=docProvenance %></span></td>
	                      <td class="title"><b>Title:</b><br/><span><%=title %></span></td>
	                      <td class="translation"><span><%=(apisDoc != null && apisDoc.get(CoreMetadataFields.TRANSLATION_EN) != null)?"Translation available.":"" %></span></td>
	                    </tr>
                        <tr>
                          <td class="ids">
                          <b>Inventory:</b>
                          <br/><span><%=docInv %></span></td>
                          <td><b>Language:</b><br/><span><%=lang.toString() %></span></td>
                          <td class="image">
                            <%=(apisImg)?"Image available in APIS<br/>":"" %>
                            <%=(hgvImg)?"Image available in HGV":"" %>
                            <a ex:href-content=".image-url"><span></span></a>
                            
                          </td>
                          <td class="bl"><b>BL Post-Concordance:</b><br/>
                          <%Iterator<String> blIter = bl.iterator();
                          while(blIter.hasNext()){%><span><%=blIter.next() %></span><br/>
                          <%} %>
                          </td>
                        </tr>
                    </tbody>
                <%}
                }
                %>
                </table>
                 </div>
            </td>
        </tr>
    </table>

</td>
</tr>
</table>
</div>
