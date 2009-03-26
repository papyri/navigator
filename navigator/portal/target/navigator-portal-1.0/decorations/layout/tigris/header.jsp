<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="../initLayoutDecorators.jsp" %>
<%@ include file="tigrisNavigations.jsp" %>

  <%-- img variables --%>
  <%
    /**
    * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
    */
      String _cPath = (String) renderRequest.getAttribute("cPath");
  
      pageContext.setAttribute("imgBanner", _cPath + "/" +
                getLayoutResource(_layoutDecoration,"images/Jetspeed_blue_med.png"), PAGE_SCOPE);
      //site
      PortalSiteRequestContext _site = (PortalSiteRequestContext) renderRequest.getAttribute("psrc");

      // pages or tabs navigation
      Menu pageTabNavigations = (Menu) _site.getMenu("pages");
      String pageTabNavigationHTML = "";
      if (null != pageTabNavigations)
      {
          pageTabNavigationHTML = getTabsNavigationContent(renderRequest,pageTabNavigations,
                                          _preferedLocale,LEFT_TO_RIGHT);
      }
      pageContext.setAttribute("pageTabNavigationHTML", pageTabNavigationHTML, PAGE_SCOPE);
      
      // breadcrumbs menu
      Menu breadCrumbs = (Menu) _site.getMenu("breadcrumbs");
      String breadCrumbsHTML = "&nbsp;";
      if (null != breadCrumbs)
      {
          breadCrumbsHTML = getLinksNavigation(renderRequest,breadCrumbs,_preferedLocale,
                                  LEFT_TO_RIGHT,TITLE_ORDER_NONE,BREADCRUMBS_STYLE,"");
      }
      pageContext.setAttribute("breadCrumbs", breadCrumbsHTML, PAGE_SCOPE);

      // navigations menu
      Menu navigations = (Menu) _site.getMenu("navigations");
      Boolean haveNavigationLinks = Boolean.FALSE;
      
      String navigationsHTML = "&nbsp;";
      if ((null != navigations) && !(navigations.isEmpty()))
      {
          navigationsHTML = getLinksWithIconNavigation(renderRequest,
                  navigations, _preferedLocale, TOP_TO_BOTTOM);
          haveNavigationLinks = Boolean.TRUE;
          pageContext.setAttribute("haveNavigationLinks", haveNavigationLinks, PAGE_SCOPE);
      }
      
      pageContext.setAttribute("navigationsHTML", navigationsHTML, PAGE_SCOPE);
      
  %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>

  <head>

    <title><c:out value="${PageTitle}" /></title>

    <!--  include any headerResource -->
    <c:if test="${not empty requestScope.headerResource}">
      <c:out escapeXml="false" value="${requestScope.headerResource.content}"/><br />
    </c:if>
    
    <meta http-equiv="Content-type" content="<c:out value='${ContentType}'/>"/>
    <meta http-equiv="Content-style-type" content="text/css"/>   
    <meta name="version" content="<c:out value='${SiteVersionTag}'/>"/>
    <meta name="keywords" content="" />
    <meta name="description" content="<c:out value="${PageTitle}"/>"/>

    <c:out value="${includeJavaScriptForHead}" escapeXml="false"/>
    <c:out value="${includeStyleSheets}" escapeXml="false"/>


    
  </head>

  <body class="<c:out value='${PageBaseCSSClass}'/>">

    <div class="<c:out value='${PageBaseCSSClass}'/>"> <!-- BEGIN: body div  wrapper -->

      <div id="banner"> <!-- B: banner div -->
        <table border="0" cellspacing="0" cellpadding="8" width="100%">
          <tr>
            <td>
              <h1><img src="<c:out escapeXml='false' value='${imgBanner}'/>" alt="Jetspeed 2 Logo" border="0"/></h1>
            </td>
            <td>
              <div align="right" id="login">
                &nbsp;
              </div>
            </td>
          </tr>
        </table>
      </div> <!-- E: banner div -->

      <!-- TABS navidation -->
      <div class="tabs">
        <c:out escapeXml="false" value="${pageTabNavigationHTML}"/>
      </div>

      <!-- BREADCRUMBS Navigation -->
      <div id="breadcrumbs"> <!-- B: breadcrumbs -->
        <c:out escapeXml="false" value="${breadCrumbs}"/>
        <c:if test="${not empty renderRequest.userPrincipal}">
          <span style="position:absolute;right:5em"><a href="<c:out value='${requestScope.baseHRef}login/logout'/>">Logout</a></span>
        </c:if>
        <c:out escapeXml="false" value="${PageActionBar}"/>
      </div> <!-- E: breadcrumbs -->
      
      <!-- CONTENT in a Table -->
      <table cellpadding="0" cellspacing="0" border="0" width="100%" id="main"> <!--  S: ALL CONTENT TABLE -->
        <tr> <!--  S: Main row -->
            <!--  S: Left Navigation -->
            <c:if test="${haveNavigationLinks}">
                <td valign="top" id="leftcol" >
                  <div id="navcolumn">
                    <table cellpadding="0" cellspacing="4" border="0" width="100%">
                      <c:out escapeXml="false" value="${navigationsHTML}" />
                    </table>
                  </div>
                </td>
            </c:if>
            <!--  START ALL PORTLETS CONTENT -->
            <td nowrap="nowrap" valign="top"> <!-- S: all portlet content -->