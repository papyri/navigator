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
<%@ include file="../../../initTemplatesLayoutNormal.jsp" %>

<portlet:defineObjects/>

  <%

    /**
    * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
    */
    // objects from initTemplatesLayoutNormal
    //  _jpt,_myPage

      ContentFragment maxFragment = (ContentFragment) renderRequest.getAttribute(PortalReservedParameters.MAXIMIZED_FRAGMENT_ATTRIBUTE);
      pageContext.setAttribute("frag", maxFragment, PAGE_SCOPE);
      
      //title
      String title = _jpt.getTitle(_jpt.getCurrentPortletEntity(),maxFragment);
      if (null == title)
        title = "";
      pageContext.setAttribute("title",title, PAGE_SCOPE);

      //pageDescription
      String pageDescription = _jpt.getPage().getTitle(_rc.getLocale());
      pageContext.setAttribute("pageDescription",pageDescription, PAGE_SCOPE);

      //baseCSSClass
      String baseCSSClass =  _theme.getDecoration(_jpt.getCurrentFragment()).getBaseCSSClass();
      pageContext.setAttribute("baseCSSClass",baseCSSClass, PAGE_SCOPE);
               
  %>

<html>
  <head>
  <base href="<c:out value='${baseHRef}'/>"/>
    <meta http-equiv="Content-type" content="<c:out value='${ContentType}'/>" />
    <meta http-equiv="Content-style-type" content="text/css" />   
    <meta http-equiv="Content-Script-Type" content="text/javascript" />

    <c:out value="${includeJavaScriptForHead}" escapeXml="false"/>
    <c:out value="${includeStyleSheets}" escapeXml="false"/>

    <!--  include any headerResource -->
    <c:if test="${not empty requestScope.headerResource}">
      <c:out escapeXml="false" value="${requestScope.headerResource.content}"/><br />
    </c:if>

    <title><c:out value="${title}"/></title>

    <meta name="version" content="<c:out value='${SiteVersionTag}'/>"/>
    <meta name="keywords" content="" />
    <meta name="description" content="<c:out value='${pageDescription}'/>" />
    
  </head>
  <body class="<c:out value='${baseCSSClass}'/>">
  
    <%-- Get the portlet content --%>
    <div id="<portlet:namespace/>" class="portal-layout-solo"> <!-- B: portlet content -->
      <%
        //inform JPT that we are current.
        _jpt.setCurrentFragment(maxFragment);
      
      %>
      <c:choose>
        <c:when test="${frag.type eq 'portlet'}">

          <%
              // TODO hack alert, we need to do this because PortletDecorationImpl has
              // decorator.vm hard coded sigh!     
              String _tempPath = maxFragment.getDecoration().getBasePath();
              String _portletJSP = _tempPath.substring(0,_tempPath.lastIndexOf('/')) +
                                   "/decorator.jsp";
              pageContext.setAttribute("fragTemplateUrl", _portletJSP, PageContext.PAGE_SCOPE);
          %>
          
          <c:import url="${fragTemplateUrl}"></c:import>
        </c:when>
        <c:otherwise>
          <c:out value="${frag.renderedContent}" />
        </c:otherwise>
      </c:choose>
    </div> <!-- E: portlet content -->

  </body>
</html>
