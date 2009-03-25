<%@page language="java" session="false" contentType="text/html" import="org.apache.jetspeed.request.*,org.apache.jetspeed.PortalReservedParameters,util.XMLEncoder,org.apache.lucene.search.*,org.apache.lucene.index.*,org.apache.lucene.document.*,util.jsp.el.Functions,java.util.*,javax.portlet.*,info.papyri.index.LuceneIndex" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@taglib uri="tld/el-functions.tld" prefix="custom"%><portlet:defineObjects/>
<portlet:defineObjects/>
<%
  String BRIEF = "brief.psml";
  String VERBOSE = "verbose.psml";
            RequestContext ctx = (RequestContext)request.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
            BRIEF = ctx.getPortalURL().getBasePath() + "/" + BRIEF; 
            VERBOSE = ctx.getPortalURL().getBasePath() + "/" + VERBOSE; 
            String pub = "";
            String apis = "";
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
            String inv = request.getParameter("invnum_num");
            if (inv == null) inv = "";
            String provenance = request.getParameter("provenance");
            String provenanceNote = request.getParameter("provenanceNote");
            if (provenanceNote == null){
                provenanceNote = "";
            }
            else{
                provenanceNote = XMLEncoder.encode(provenanceNote);
            }
            String keyword = request.getParameter("keyword");
            if (keyword == null) keyword = "";
            pageContext.setAttribute("PROVENANCE",provenance);
            String [] lang = request.getParameterValues("lang");
            if (lang == null) lang= new String[0];
            pageContext.setAttribute("LANG",lang);
            String beginDate = request.getParameter("beginDate");
            if (beginDate == null) beginDate = "";
            String beginDateEra = request.getParameter("beginDateEra");
            pageContext.setAttribute("SUBMITTED_BEGIN_DATE_ERA",beginDateEra);
            String endDate = request.getParameter("endDate");
            if (endDate == null) endDate = "";
            String endDateEra = request.getParameter("endDateEra");
            pageContext.setAttribute("SUBMITTED_END_DATE_ERA",endDateEra);
            String recDisplay = request.getParameter("pn-record-display");
            if (!"brief".equals(recDisplay) && !"verbose".equals(recDisplay)) recDisplay = "brief";
            boolean brief = "brief".equals(recDisplay);
            String numPerPage = request.getParameter("pn-page-length");
            if (numPerPage == null) numPerPage = "25";
            pageContext.setAttribute("PAGE_LENGTH",numPerPage);
            String sort = request.getParameter("pn-sort");
            if (sort == null) sort = "";
            %>
<div>
<form name="ui" id="ui" action="<%=(brief)?BRIEF:VERBOSE %>" method="get">
<table class="pn-form">
    	<tbody>
    <tr class="pn-form-row">
      <td class="pn-form-section">
        <label for="keyword">Search by Keyword</label>
      </td>
      <td class="pn-options-section">
          <div>
              <input id="keyword"
			         name="keyword" value="<%=XMLEncoder.encode(keyword) %>" size="26" />
			  <input id="chkIncludeTranslations" name="chkIncludeTranslations"
				     type="checkbox" <%=("on".equals(request.getParameter("chkIncludeTranslations")))?"checked=\"checked\"":"" %>  />
		      <label for="chkIncludeTranslations">
			    Include translations
			  </label>
			  <input name="btnSearch" value="Search"  class="pn-button" onclick="return true;" type="submit" />
	          <input name="btnClear" value="Clear" class="pn-button"
	                 onclick="clearForm(this.form);" type="button" />
	          <input type="hidden" name="pn-display-mode" id="pn-display-mode" value="results" />	
	        </div>
	    </td>
	</tr>
    <tr class="pn-form-row">
      <td class="pn-form-section">Search by Number</td>

		<td class="pn-options-section">
		  <div>
		    <strong>Publication Number</strong>
		<label for="pubnum_series">Series:</label> <select
			id="pubnum_series" name="pubnum_series">
			<option value="">[Select]</option>
			<%
			Iterator<String> seriesIter = LuceneIndex.getIndexedSeries();                
			while (seriesIter.hasNext()) {
			    String seriesIndex = seriesIter.next();
			                pageContext.setAttribute("series", seriesIndex);
			%>
			${custom:selectedOption(series,series,SERIES)}
			<%
			}
			%>
		</select> <label for="pubnum_vol">Volume:</label> <input id="pubnum_vol"
			name="pubnum_vol" size="5" maxlength="25" class="forminput10"
			value="<%=XMLEncoder.encode(volume) %>" type="text" /> <label for="pubnum_doc">Document:</label>
		<input id="pubnum_doc" name="pubnum_doc" size="5" maxlength="25"
			class="forminput10" value="<%=XMLEncoder.encode(document) %>" type="text" />

		  </div>
		  <div>
            <label for="invnum_num" ><strong>Inventory Number <span class="pn-note">(apis only)</span></strong></label>
                  
			<input id="invnum_num" name="invnum_num" size="5" maxlength="25"
				value="<%=inv %>" type="text" /> <span class="comment"
				style="font-weight: normal;">e.g."186a"</span>
		  </div><div>
				<strong>APIS Number</strong> Institution:
			<select name="apisnum_inst">
			<option value="">[Select]</option>
			<%
			Iterator<String> collections = LuceneIndex.getIndexedApisCollections();
			                while (collections.hasNext()) {
			                String next = collections.next().toString();
			                pageContext.setAttribute("apisnum_inst", next);
			                //Functions.selectedOption(collection,request.getParameter("apisnum_inst"));
			%>
			${custom:selectedOption(apisnum_inst,apisnum_inst,APISNUM_INST)}
			<%
			}
			%>
			</select> <span style="font-weight: normal;">.apis.</span>
                  <label for="apisnum_num">Number:</label>
			<input id="apisnum_num" name="apisnum_num" size="5" maxlength="25" class="forminput10"
				value="<%=XMLEncoder.encode(apisnum_num) %>" type="text" /> <span class="comment"
				style="font-weight: normal;">e.g."p163"</span></div></td>
	</tr>
    <tr class="pn-form-row">
		 <td class="pn-form-section">Search or Limit By</td>
			<td class="pn-options-section">
			<div>
			<strong>Provenance</strong>
		    <input type="text" id="provenanceNote" name="provenanceNote" value="<%=XMLEncoder.encode(provenanceNote) %>" />
			</div>
			<div style="float:left">
			<strong id="lang-label">Language <span class="pn-note">(apis only)</span></strong>
		    <select name="lang" id="lang" multiple="multiple" size="3">
			<option value="">[Select]</option>
			<%
			Iterator langVals = LuceneIndex.getIndexedLanguageValues();
			TreeMap<String,String> labelMap = new TreeMap<String,String>();
			                while (langVals.hasNext()) {
			                String val = langVals.next().toString();
			                String key = LuceneIndex.getLanguageLabel(val);
			                labelMap.put(key,val);
			                }
			                Iterator<String> labels = labelMap.keySet().iterator();
			                while(labels.hasNext()){
			                    String label = labels.next();
			                    String option = labelMap.get(label);
			                pageContext.setAttribute("lang", option);
			                pageContext.setAttribute("label",label);
			%>
			${custom:selectedOptionMultiple(lang,label,LANG)}
			<%
			}
			%>
		    </select></div>
		    <div class="instruction">CTRL-click (Mac users command-click)<br />to select multiple languages
			</div>
			<div style="clear:left"><label for="institution"><strong>APIS Collection</strong> </label>
                  <select name="institution" id="institution">
			<option value="">[Select]</option>
			<%
			Iterator institutions = LuceneIndex.getIndexedApisCollections();
			                while (institutions.hasNext()) {
			                String next = institutions.next().toString();
			                pageContext.setAttribute("institution", next);
			                //Functions.selectedOption(collection,request.getParameter("apisnum_inst"));
			%>
			${custom:selectedOption(institution,institution,INSTITUTION)}
			<%
			}
			%>
			</select>
		  </div><div>
			<strong>Date</strong>
			<label for="beginDate">On or after:</label>
			<input size="5"
				id="beginDate" name="beginDate" class="forminput10" value="<%=beginDate %>" />
			<select	name="beginDateEra">
			${custom:selectedOption("CE","CE",SUBMITTED_BEGIN_DATE_ERA)}
			${custom:selectedOption("BCE","BCE",SUBMITTED_BEGIN_DATE_ERA)}
			</select>
                  <label for="endDate">On or before:</label>
			<input size="5"
				id="endDate" name="endDate" class="forminput10" value="<%=endDate %>" />
				<select	name="endDateEra">
			${custom:selectedOption("CE","CE",SUBMITTED_END_DATE_ERA)}
			${custom:selectedOption("BCE","BCE",SUBMITTED_END_DATE_ERA)}
			</select>
		  </div>
		  </td>
		</tr>
    <tr class="pn-form-row">
      <td class="pn-form-section">Options</td>
  <td class="pn-options-section">
      <table id="pn-options-table">
          <tr>
              <td>
              <div>Show records with:
                  <br/>
		          <input type="checkbox" name="req_img" id="req_img" <%=("on".equals(request.getParameter("req_img")))?"checked=\"checked\"":"" %> />
		          <label for="req_img"><strong>images</strong> first</label>
		          <br/>
		          <input type="checkbox" name="req_pub" id="req_pub" <%=("on".equals(request.getParameter("req_pub")))?"checked=\"checked\"":"" %> />
                  <label for="req_pub"><strong>publications</strong> first</label>
		          <br/>
		          <input type="checkbox" name="req_trans" id="req_trans" <%=("on".equals(request.getParameter("req_trans")))?"checked=\"checked\"":"" %> />
		          <label for="req_trans"><strong>translations</strong> first</label>
		      </div>
		      </td>
		      <td>
		          <div>Show results:
                      <input type="radio" name="pn-record-display" id="brief-record-display" 
                             onchange="if(this.checked)this.form.action='<%=BRIEF %>';return true;"
                             value="brief" <%=("brief".equals(recDisplay)?"checked=\"checked\"":"") %>/>
                      <label for="brief-record-display">in brief</label>
                      <input type="radio" name="pn-record-display" id="verbose-record-display" 
                             onchange="if(this.checked)this.form.action='<%=VERBOSE %>';return true;"
                             value="verbose" <%=("verbose".equals(recDisplay)?"checked=\"checked\"":"") %>/>
                      <label for="verbose-record-display">in detail</label>
                  </div>
                  <div>Display 
                      <select id="pn-page-length" name="pn-page-length">
    ${custom:selectedOption('10','10',PAGE_LENGTH)}
    ${custom:selectedOption('25','25',PAGE_LENGTH)}
    ${custom:selectedOption('50','50',PAGE_LENGTH)}
    ${custom:selectedOption('100','100',PAGE_LENGTH)}
                      </select>
                      hits per page
                  </div>
                  <div>Sort by 
                      <select id="pn-sort" name="pn-sort">
                          <option value="">APIS Control Name</option>
                          <option value="PUBLICATION" <%=("PUBLICATION".equals(sort))?"selected=\"selected\"":"" %>>Publication</option>
                          <option value="DATE" <%=("DATE".equals(sort))?"selected=\"selected\"":"" %>>Date</option>
                      </select>
                  </div>
              </td>
          </tr>
      </table>
 </td>
  </tr>
	</tbody>
</table>
</form>
</div>