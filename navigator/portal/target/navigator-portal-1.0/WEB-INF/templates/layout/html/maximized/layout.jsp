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
<%@ include file="../../../initTemplatesLayoutNormal.jsp" %>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.Locale"%>  
<%@page import="javax.servlet.jsp.jstl.core.Config"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>

  <%

    /**
    * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
    */
      // initTemplatesLayoutNormal
      //   _myPage, _jpt
      ContentFragment maxFragment = (ContentFragment) renderRequest.getAttribute(PortalReservedParameters.MAXIMIZED_FRAGMENT_ATTRIBUTE);
      //get the messages resourcebundle
      Locale _tmpLocale = (Locale) renderRequest.getLocale();
      ResourceBundle _messages = (ResourceBundle) portletConfig.getResourceBundle(renderRequest.getLocale());
      pageContext.setAttribute("messages", _messages, PAGE_SCOPE);
      //Now set the JSTL default format bundle ** FORMAT BUNDLE **
      javax.servlet.jsp.jstl.core.Config.set(request,Config.FMT_LOCALIZATION_CONTEXT,
                                             new LocalizationContext(_messages, _tmpLocale));
      // get the headers and footer based on the
      // rootFragment
      ContentFragment rootFragment = (ContentFragment) _myPage.getRootFragment();
          
      LayoutDecoration layoutDecoration = (LayoutDecoration) rootFragment.getDecoration();
      // get the layout decoration header file
      String headerJSP = layoutDecoration.getHeader();
      if ( (headerJSP != null) && !(headerJSP.startsWith("/")) )
      {
        headerJSP = "/" + headerJSP;
      }
      pageContext.setAttribute("layoutHeaderJSP", headerJSP, PAGE_SCOPE);
      
      // get the layout decoration footer file
      String footerJSP = layoutDecoration.getFooter();
      if ( (footerJSP != null) && !(footerJSP.startsWith("/")) )
      {
        footerJSP = "/" + footerJSP;
      }
      pageContext.setAttribute("layoutFooterJSP", footerJSP, PAGE_SCOPE);
  %>
  
  <!-- header.jsp -->
  <c:if test="${not empty layoutHeaderJSP}">
    <c:import url="${layoutHeaderJSP}"></c:import>
  </c:if>

  <!-- content -->
  <div id="portal-layout-$htmlUtil.getSafeElementId($myPage.id)" class="portal-layout-maxed">
    <%
      //inform JPT that we are current.
      _jpt.setCurrentFragment(maxFragment);
      // TODO hack alert, we need to do this because PortletDecorationImpl has
      // decorator.vm hard coded sigh!     
      String _tempPath = maxFragment.getDecoration().getBasePath();
      String _portletJSP = _tempPath.substring(0,_tempPath.lastIndexOf('/')) +
                           "/decorator.jsp";
      pageContext.setAttribute("fragTemplateUrl", _portletJSP, PageContext.PAGE_SCOPE);
    %>
    <c:import url="${fragTemplateUrl}"></c:import>
  </div>

  
  <!-- footer.jsp -->
  <c:if test="${not empty layoutFooterJSP}">
    <c:import url="${layoutFooterJSP}"></c:import>
  </c:if>
  