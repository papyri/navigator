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
--%>
<%@page language="java" pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
<%@page import="org.apache.jetspeed.PortalReservedParameters"%>
<%@page import="org.apache.jetspeed.decoration.LayoutDecoration"%>
<%@page import="org.apache.jetspeed.decoration.Theme"%>
<%@page import="org.apache.jetspeed.layout.JetspeedPowerTool"%>
<%@page import="org.apache.jetspeed.om.page.ContentFragment"%>
<%@page import="org.apache.jetspeed.request.RequestContext"%>

<%@page import="java.util.Locale"%>


<portlet:defineObjects/>

  <%!
	  /**
	   * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
	   */
      private final static Log log = LogFactory.getLog("org.apache.jetspeed.decoration.layout.tigris_jsp");
      private int PAGE_SCOPE = PageContext.PAGE_SCOPE;
      private String getLayoutResource(LayoutDecoration _layoutDecoration,String _path)
      {
        String _resourcePath = _layoutDecoration.getResource(_path);
        return ((null == _resourcePath) ? _path : _resourcePath);
      }
  %>

<%

  int DEFAULT_SCOPE = PageContext.PAGE_SCOPE;
  int REQ_SCOPE = PageContext.REQUEST_SCOPE;
  
  //extract the jpt
  JetspeedPowerTool _jpt = (JetspeedPowerTool) renderRequest.getAttribute("jpt");

  //extract the theme
  Theme _theme = (Theme) renderRequest.getAttribute("theme");

  //rootFragment
  ContentFragment _rootFragment = _jpt.getCurrentFragment();
  pageContext.setAttribute("rootFragment", _rootFragment, DEFAULT_SCOPE);

  //layoutDecoration TODO vmmacros call decoration
  LayoutDecoration _layoutDecoration = _theme.getPageLayoutDecoration();
  pageContext.setAttribute("layoutDecoration", _layoutDecoration, DEFAULT_SCOPE);

  //Jetspeed Request Context
  RequestContext _rc = (RequestContext) renderRequest.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
  pageContext.setAttribute("rc", _rc, DEFAULT_SCOPE);

  //preferedLocale (NOTE: req scope menus need it)
  Locale _preferedLocale = _rc.getLocale();
  pageContext.setAttribute("preferedLocale", _preferedLocale, REQ_SCOPE);
  
  //PageTitle
  String _PageTitle = _jpt.getPage().getTitle(_preferedLocale);
  pageContext.setAttribute("PageTitle", _PageTitle, DEFAULT_SCOPE);
  
%>
  <%-- BEGIN GLOBAL PAGE SCOPE variables for decorators --%>


  <c:set var="jetspeed"     value="${requestScope.jpt}" scope="page" />
  <c:set var="PageBaseCSSClass" value="${layoutDecoration.baseCSSClass}" scope="page" />
  <c:set var="resourceBaseHref" value="${requestScope.cPath}${layoutDecoration.basePath}"/>
  
  
  <%-- PageActionBar --%>
  <c:set var="actions" value="${layoutDecoration.actions}" scope="page" />
  <c:set var="PageActionBar" scope="page">
  
                        <div id="portal-page-actions"> <!-- B: div portal-page-actions --> 
                          <c:forEach var="_action" items="${actions}">
                            <a href="<c:out value='${_action.action}'/>" title="<c:out value='${_action.name}'/>" class="action pageAction">
                              <img src="<c:out value='${requestScope.cPath}/${_action.link}'/>" alt="<c:out value='${_action.alt}'/>" border="0" />
                            </a>
                          </c:forEach>
                        </div> <!-- E: div portal-page-actions --> 
  
  </c:set>

  <%-- END GLOBAL PAGE SCOPE variables for decorators --%>  
