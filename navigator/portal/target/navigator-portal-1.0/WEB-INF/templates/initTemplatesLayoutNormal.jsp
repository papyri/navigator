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

<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>

<%@page import="org.apache.jetspeed.PortalReservedParameters"%>
<%@page import="org.apache.jetspeed.decoration.Theme"%>
<%@page import="org.apache.jetspeed.layout.JetspeedPowerTool"%>
<%@page import="org.apache.jetspeed.om.page.Page"%>
<%@page import="org.apache.jetspeed.om.page.ContentFragment"%>
<%@page import="org.apache.jetspeed.decoration.LayoutDecoration"%>
<%@page import="org.apache.jetspeed.portlets.layout.ColumnLayout"%>
<%@page import="org.apache.jetspeed.portalsite.PortalSiteRequestContext"%>
<%@page import="org.apache.jetspeed.headerresource.HeaderResourceFactory"%>
<%@page import="org.apache.jetspeed.headerresource.HeaderResource"%>
<%@page import="org.apache.jetspeed.request.RequestContext"%>
<%@page import="org.apache.jetspeed.portlets.layout.LayoutCoordinate"%>
<%@page import="org.apache.jetspeed.decoration.LayoutDecoration"%>
<%@page import="org.apache.jetspeed.decoration.DecorationFactory"%>
<%@page import="org.apache.jetspeed.om.page.ContentFragment"%>
<%@page import="org.apache.jetspeed.util.Path"%>

<%@taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>


<portlet:defineObjects/>

  <%
    /**
    * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
    */
   %>


  <%!  
      private static final Log log = LogFactory.getLog("org.apache.jetspeed.portlets.layout.jsp_templates");
      private static final int REQ_SCOPE = PageContext.REQUEST_SCOPE;
      private static final int PAGE_SCOPE = PageContext.PAGE_SCOPE;
      
      private String getLayoutResource(LayoutDecoration _layoutDecoration,String _path)
      {
        String _resourcePath = _layoutDecoration.getResource(_path);
        return ((null == _resourcePath) ? _path : _resourcePath);
      }
      private Boolean getBoolProperty(LayoutDecoration _decorator, String propKey, boolean _default)
      {
          Boolean value = new Boolean(_default);
          String _value = _decorator.getProperty(propKey);
          if ( (_value != null) && !(_value.equalsIgnoreCase("")) )
          {
             value = new Boolean(_value);
          }
          return value;
      }

      private Integer getIntProperty(LayoutDecoration _decorator, String propKey, int _default)
      {
          Integer value = new Integer(_default);
          String _value = _decorator.getProperty(propKey);
          if ( (_value != null) && !(_value.equalsIgnoreCase("")) )
          {
              try 
              {
                  value = new Integer(_value);
              }
              catch (NumberFormatException e)
              {
                log.warn("wrong format for property:<" + propKey + "> " + "value given:<" + _value + ">");
              }
             
          }
          return value;
      }
      
      
  %>

  <%
      // Since we will try to *avoid* using the rt, let's stuff everything in the pageContext
      //contextPath
      String _cPath = (String) request.getContextPath();
  
      if (_cPath.equalsIgnoreCase(""))
      {
          _cPath = "/"; 
      }
      else if (_cPath.endsWith("/"))
      {
          _cPath = _cPath.substring(0,_cPath.length()-1);
      }
      pageContext.setAttribute("cPath",_cPath,REQ_SCOPE);
    
      //baseHRef  
      String _baseHRef = request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort()+ _cPath + "/";
      pageContext.setAttribute("baseHRef", _baseHRef, REQ_SCOPE);
    
      //Jetspeed Power Tool
      JetspeedPowerTool _jpt = (JetspeedPowerTool) renderRequest.getAttribute(PortalReservedParameters.JETSPEED_POWER_TOOL_REQ_ATTRIBUTE);
      pageContext.setAttribute("jpt", _jpt, REQ_SCOPE);
    
      //Jetspeed Request Context
      RequestContext _rc = (RequestContext) renderRequest.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);

      //PortalSiteRequestContext
      PortalSiteRequestContext _psrc = (PortalSiteRequestContext) renderRequest.getAttribute("org.apache.jetspeed.portalsite.PortalSiteRequestContext");
      pageContext.setAttribute("psrc", _psrc, REQ_SCOPE);

      //Get the resourceHeaderFactory
      HeaderResourceFactory headerResourceFactory = (HeaderResourceFactory) _jpt.getComponent("org.apache.jetspeed.headerresource.HeaderResourceFactory");
      HeaderResource headerResource = null;
      if (null != headerResourceFactory)
      {
          headerResource = headerResourceFactory.getHeaderResouce(_rc);
      }
      else
      {
          log.warn("JSPTemplate: Could not acquire HeaderResourceFactory during template initialization"); 
      }
      pageContext.setAttribute("headerResource", headerResource, REQ_SCOPE);
      
  %>

  <%
  
      //Theme
      Theme _theme = (Theme) renderRequest.getAttribute(PortalReservedParameters.PAGE_THEME_ATTRIBUTE);
      pageContext.setAttribute("theme",_theme,REQ_SCOPE);
    
      //myPage
      Page _myPage = _jpt.getPage();
      pageContext.setAttribute("myPage", _myPage, REQ_SCOPE);
    
      
      //numberOfColumns
      Integer _numberOfColumns = (Integer)renderRequest.getAttribute("numberOfColumns");
      pageContext.setAttribute("numberOfColumns", _numberOfColumns, REQ_SCOPE);
    
      //editing mode?
      Boolean _editing = (Boolean) renderRequest.getAttribute("editing");
      pageContext.setAttribute("editing", _editing, REQ_SCOPE);
    
      pageContext.setAttribute("layoutType", portletConfig.getInitParameter("layoutType"), REQ_SCOPE);
    
      //columnLayout, no type info here because layout jars not available
      // TODO got to move ColumnLayout from layout-portlets out! Jetspeed webapp cannot see it!
      ColumnLayout _columnLayout = (ColumnLayout) renderRequest.getAttribute("columnLayout");
      pageContext.setAttribute("columnLayout", _columnLayout, REQ_SCOPE);
    
      //myFragment
      ContentFragment _myFragment = _jpt.getCurrentFragment();
      pageContext.setAttribute("myFragment", _myFragment, PAGE_SCOPE);
      
      //defaultDecorator
      String _defaultDecorator = _myPage.getDefaultDecorator(_myFragment.getType());
      pageContext.setAttribute("defaultDecorator", _defaultDecorator, PAGE_SCOPE);
  
      //decorationFactory
      DecorationFactory _decorationFactory = (DecorationFactory) renderRequest.getAttribute("decorationFactory");
      pageContext.setAttribute("decorationFactory", _decorationFactory, PAGE_SCOPE);

      if ((_editing != null) && (_editing.booleanValue()))
      {
	      //LayoutDecorations list
	      pageContext.setAttribute("layoutDecorationsList",
	                                _decorationFactory.getLayouts(_rc),REQ_SCOPE);
	      //LayoutDecorations list
	      pageContext.setAttribute("portletDecorationsSet",
	                                _decorationFactory.getPortletDecorations(_rc),REQ_SCOPE);
      }
      
      //rootDecorator
      LayoutDecoration _rootDecorator = (LayoutDecoration)((ContentFragment)_myPage.getRootFragment()).getDecoration();
      pageContext.setAttribute("rootDecorator",_rootDecorator, PAGE_SCOPE);
  %>


  <%-- BEGIN Page and Request Scoped Variables --%>

    <%-- decorator --%>
    <c:set var="decorator" value="${myFragment.decorator}" scope="page"/>  
    <c:if test="${empty decorator}">
      <c:set var="decorator" value="${defaultDecorator}" scope="page"/>
    </c:if>
  
    <%-- root --%>
    <c:if test="${myFragment eq myPage.rootFragment}" var="isRoot" scope="page"/>

    <%-- ContentType --%>
    <c:set var="ContentType" value="text/html" scope="request" />
    <c:if test="${not empty renderResponse.characterEncoding}">
      <c:set var="ContentType" value="${requestScope.ContentType};${renderResponse.characterEncoding}" scope="request" />
    </c:if>
  
    <%-- SiteVersionTag --%>
    <c:set var="SiteVersionTag" value="$Id$" scope="request" />
  
  
    <%-- includeJavaScriptforHead --%>
    <c:set var="includeJavaScriptForHead" scope="request">
     <script type="text/javascript">
      <!--//--><![CDATA[//><!--
        function openWindow(pipeline)
        {    
          var vWinUsers = window.open(pipeline, 'PortletSelector', 'status=no,resizable=yes,width=500,height=600,scrollbars=yes');
          vWinUsers.opener = self;
          vWinUsers.focus();
        }
      //--><!]]>
     </script>
    </c:set>
  
    <c:set var="includeStyleSheets" scope="request">
      <c:forEach var="style" items="${theme.styleSheets}">
        <link rel="stylesheet" type="text/css" media="screen, projection" href="<c:out value='${cPath}/${style}' escapeXml='false'/>" />
      </c:forEach>
    </c:set>


  <%-- END Request and Scoped Variables --%>


