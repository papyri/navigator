<%@page language="java" session="false" contentType="text/html" import="org.apache.jetspeed.*,org.apache.jetspeed.request.*,util.XMLEncoder" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@taglib uri="tld/el-functions.tld" prefix="custom"%><portlet:defineObjects/>
<portlet:defineObjects/><%
            String pub = "";
            String apis = "";
            String recDisplay = request.getParameter("pn-record-display");
            if (recDisplay == null) recDisplay = "brief";
            String institution = request.getParameter("institution");
            String invnum_num = request.getParameter("invnum_num");
            pageContext.setAttribute("INSTITUTION",institution);
            if ("apis".equals(request.getParameter("xrefSchema")))
                apis = request.getParameter("xrefPattern");
            String series = (request.getParameter("pubnum_series") == null) ? ""
                    : request.getParameter("pubnum_series");
            pageContext.setAttribute("SERIES", series);
            String apisnum_inst = request.getParameter("apisnum_inst");
            pageContext.setAttribute("APISNUM_INST", apisnum_inst);
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
            String [] langs = request.getParameterValues("lang");
            if (langs == null) langs = new String[0];
            //pageContext.setAttribute("LANG",lang);
            String beginDate = request.getParameter("beginDate");
            if (beginDate == null) beginDate = "";
            String beginDateEra = request.getParameter("beginDateEra");
            pageContext.setAttribute("SUBMITTED_BEGIN_DATE_ERA",beginDateEra);
            String endDate = request.getParameter("endDate");
            if (endDate == null) endDate = "";
            String endDateEra = request.getParameter("endDateEra");
            pageContext.setAttribute("SUBMITTED_END_DATE_ERA",endDateEra);
            String req_img = request.getParameter("req_img"); 
            String req_pub = request.getParameter("req_pub"); 
            String req_trans = request.getParameter("req_trans"); 
            String chkIncludeTranslations = request.getParameter("chkIncludeTranslations");
            if (chkIncludeTranslations == null) chkIncludeTranslations = "";
            if (recDisplay == null) recDisplay = "brief";
            String numPerPage = request.getParameter("pn-page-length");
            if (numPerPage == null) numPerPage = "25";
            RequestContext ctx = (RequestContext)request.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
            String action = ctx.getPortalURL().getBasePath() + "/default-page.psml";
            String sort = request.getParameter("pn-sort");
            if (sort == null) sort = "";
            String provenanceNote = request.getParameter("provenanceNote");
            if (provenanceNote == null) provenanceNote = "";
            %>
<form name="ui" id="ui" action="<%=action %>" method="get">
<input id="keyword" type="hidden" name="keyword" value="<%=XMLEncoder.encode(keyword) %>" size="26" />
<input id="chkIncludeTranslations" name="chkIncludeTranslations" value="<%=XMLEncoder.encode(chkIncludeTranslations) %>" type="hidden" />
<input id="provenance" name="provenance" type="hidden" value="<%=XMLEncoder.encode(provenance) %>" />
<input id="provenanceNote" name="provenanceNote" type="hidden" value="<%=XMLEncoder.encode(provenanceNote) %>" />
<% if (langs != null && langs.length > 0){
  for (int i =0;i<langs.length;i++){ %>
<input id="lang<%=i %>" name="lang" type="hidden" value="<%=XMLEncoder.encode(langs[i]) %>" />
<%}
  }
  else {%>
<input id="lang" name="lang" type="hidden" value="" />
  <%} %>
<input id="institution" name="institution" type="hidden" value="<%=XMLEncoder.encode(institution) %>" />
<input id="beginDate" name="beginDate" type="hidden" value="<%=XMLEncoder.encode(beginDate) %>" />
<input id="beginDateEra" name="beginDateEra" type="hidden" value="<%=XMLEncoder.encode(beginDateEra) %>" />
<input id="endDate" name="endDate" type="hidden" value="<%=XMLEncoder.encode(endDate) %>" />
<input id="endDateEra" name="endDateEra" type="hidden" value="<%=XMLEncoder.encode(endDateEra) %>" />
<input id="pubnum_series" name="pubnum_series" type="hidden" value="<%=XMLEncoder.encode(series) %>" />
<input id="pubnum_vol" name="pubnum_vol" type="hidden" value="<%=XMLEncoder.encode(volume) %>" />
<input id="pubnum_doc" name="pubnum_doc" type="hidden" value="<%=XMLEncoder.encode(document) %>" />
<input id="invnum_num" name="invnum_num" type="hidden" value="<%=XMLEncoder.encode(invnum_num) %>" />
<input id="apisnum_inst" name="apisnum_inst" type="hidden" value="<%=XMLEncoder.encode(apisnum_inst) %>" />
<input id="apisnum_num" name="apisnum_num" type="hidden" value="<%=XMLEncoder.encode(apisnum_num) %>" />
<input id="req_img" name="req_img" type="hidden" value="<%=XMLEncoder.encode(req_img) %>" />
<input id="req_pub" name="req_pub" type="hidden" value="<%=XMLEncoder.encode(req_pub) %>" />
<input id="req_trans" name="req_trans" type="hidden" value="<%=XMLEncoder.encode(req_trans) %>" />
<input id="pn-display-mode" name="pn-display-mode" type="hidden" value="results" />
<input id="pn-page-length" name="pn-page-length" type="hidden" value="<%=XMLEncoder.encode(numPerPage) %>" />
<input id="pn-sort" name="pn-sort" type="hidden" value="<%=XMLEncoder.encode(sort) %>" />
<input type="hidden" name="pn-record-display" id="brief-record-display" value="<%=recDisplay %>" />
  <div>
  <!--  
  <input type="radio" name="pn-record-display" id="brief-record-display" value="brief" ("brief".equals(recDisplay)?"checked=\"checked\"":"")
  onchange="if(this.checked)this.form.submit();return true;"
  /><label for="brief-record-display">Show Brief Results</label>
  <input type="radio" name="pn-record-display" id="verbose-record-display" value="verbose" ("verbose".equals(recDisplay)?"checked=\"checked\"":"")
  onchange="if(this.checked)this.form.submit();return true;"
  /><label for="verbose-record-display">Show Verbose Results</label>
  -->
  </div>
</form>