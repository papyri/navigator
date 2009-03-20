<%@page language="java" session="false" contentType="text/html"  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@taglib uri="tld/el-functions.tld" prefix="custom"%><portlet:defineObjects/>
<portlet:defineObjects/><%
            String pub = "";
            String apis = "";
            String invnum_coll = request.getParameter("invnum_coll");
            pageContext.setAttribute("INVNUM_COLL",invnum_coll);
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
            String lang = request.getParameter("lang");
            pageContext.setAttribute("LANG",lang);
            String beginDate = request.getParameter("beginDate");
            if (beginDate == null) beginDate = "";
            String beginDateEra = request.getParameter("beginDateEra");
            pageContext.setAttribute("SUBMITTED_BEGIN_DATE_ERA",beginDateEra);
            String endDate = request.getParameter("endDate");
            if (endDate == null) endDate = "";
            String endDateEra = request.getParameter("endDateEra");
            pageContext.setAttribute("SUBMITTED_END_DATE_ERA",endDateEra);
            String req_img = request.getParameter("req_img"); 
            String req_trans = request.getParameter("req_trans"); 
            String recDisplay = request.getParameter("pn-record-display");
            String chkIncludeTranslations = request.getParameter("chkIncludeTranslations");
            if (chkIncludeTranslations == null) chkIncludeTranslations = "";
            if (recDisplay == null) recDisplay = "brief";
            String numPerPage = request.getParameter("pn-page-length");
            if (numPerPage == null) numPerPage = "25";
            %>
  <div>
  </div>