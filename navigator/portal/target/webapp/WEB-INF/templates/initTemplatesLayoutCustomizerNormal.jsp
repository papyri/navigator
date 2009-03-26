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
<%@ include file="initTemplatesLayoutNormal.jsp" %>
<%@page import="org.apache.jetspeed.request.RequestContext"%>
<%@page import="org.apache.jetspeed.decoration.DecorationFactory"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.Locale"%>
<%@page import="javax.servlet.jsp.jstl.core.Config"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>

  <%-- BEGIN customizer page scoped variables and declarations --%>
    <%
      /**
      * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
      */
        //layoutType
        String _layoutType = portletConfig.getInitParameter("layoutType");
        pageContext.setAttribute("layoutType", _layoutType, PAGE_SCOPE);
        
        
        Set pageDecorations = _decorationFactory.getPageDecorations(_rc);
        pageContext.setAttribute("pageDecorations", pageDecorations, PAGE_SCOPE);
        
        
        //messages
        Locale _tmpLocale = (Locale) renderRequest.getLocale();
        ResourceBundle _messages = (ResourceBundle) portletConfig.getResourceBundle(renderRequest.getLocale());
        pageContext.setAttribute("messages", _messages, PAGE_SCOPE);
        //Now set the JSTL default format bundle ** FORMAT BUNDLE **
        javax.servlet.jsp.jstl.core.Config.set(request,Config.FMT_LOCALIZATION_CONTEXT,
                                               new LocalizationContext(_messages, _tmpLocale));
        

        //capture all the allowed property
        boolean isRoot = false;
        if (_myPage.getRootFragment() == _myFragment)
          isRoot = true;
        
        if (isRoot)
        {
          pageContext.setAttribute("allowChangePageTheme",
                getBoolProperty(_rootDecorator,"allow.change.page.theme",true), PAGE_SCOPE);
          pageContext.setAttribute("allowAddPage",
                  getBoolProperty(_rootDecorator,"allow.add.page",true), PAGE_SCOPE);
          pageContext.setAttribute("allowChangePageName",
                  getBoolProperty(_rootDecorator,"allow.change.page.name",true), PAGE_SCOPE);
          pageContext.setAttribute("allowNavigatePage",
                  getBoolProperty(_rootDecorator,"allow.navigate.page",true), PAGE_SCOPE);
          pageContext.setAttribute("allowDeletePage",
                  getBoolProperty(_rootDecorator,"allow.delete.page",true), PAGE_SCOPE);
          pageContext.setAttribute("allowChangeFolderTheme",
                  getBoolProperty(_rootDecorator,"allow.change.folder.theme",true), PAGE_SCOPE);
          pageContext.setAttribute("allowAddFolder",
                  getBoolProperty(_rootDecorator,"allow.add.folder",true), PAGE_SCOPE);
          pageContext.setAttribute("allowChangeFolderName",
                  getBoolProperty(_rootDecorator,"allow.change.folder.name",true), PAGE_SCOPE);
          pageContext.setAttribute("allowNavigateFolder",
                  getBoolProperty(_rootDecorator,"allow.navigate.folder",true), PAGE_SCOPE);
          pageContext.setAttribute("allowDeleteFolder",
                  getBoolProperty(_rootDecorator,"allow.delete.folder",true), PAGE_SCOPE);

        }
        else
        {
            pageContext.setAttribute("allowChangePageTheme",
                    Boolean.FALSE, PAGE_SCOPE);
              pageContext.setAttribute("allowAddPage",
            		  Boolean.FALSE, PAGE_SCOPE);
              pageContext.setAttribute("allowChangePageName",
            		  Boolean.FALSE, PAGE_SCOPE);
              pageContext.setAttribute("allowNavigatePage",
            		  Boolean.FALSE, PAGE_SCOPE);
              pageContext.setAttribute("allowDeletePage",
            		  Boolean.FALSE, PAGE_SCOPE);
              pageContext.setAttribute("allowChangeFolderTheme",
            		  Boolean.FALSE, PAGE_SCOPE);
              pageContext.setAttribute("allowAddFolder",
            		  Boolean.FALSE, PAGE_SCOPE);
              pageContext.setAttribute("allowChangeFolderName",
            		  Boolean.FALSE, PAGE_SCOPE);
              pageContext.setAttribute("allowNavigateFolder",
            		  Boolean.FALSE, PAGE_SCOPE);
              pageContext.setAttribute("allowDeleteFolder",
            		  Boolean.FALSE, PAGE_SCOPE);
        }

        pageContext.setAttribute("allowChangeLayout",
                getBoolProperty(_rootDecorator,"allow.change.layout",true), PAGE_SCOPE);
        pageContext.setAttribute("allowAddPortlet",
                getBoolProperty(_rootDecorator,"allow.add.portlet",true), PAGE_SCOPE);
        
        pageContext.setAttribute("allowChangePortletDecorator",
                getBoolProperty(_rootDecorator,"allow.change.portlet.decorator",true), PAGE_SCOPE);

        //maxLayoutNesting TODO: reminder to ask who sets fragmentNestingLevel
        Integer _maxLayoutNesting = getIntProperty(_rootDecorator, "max.layout.nesting", 2);
        Integer fragNestingLevel = (Integer) renderRequest.getAttribute("fragmentNestingLevel");
        
        if (fragNestingLevel.intValue() < _maxLayoutNesting.intValue() ) 
        {
            pageContext.setAttribute("allowAddLayout", Boolean.TRUE, PAGE_SCOPE);
        }
        else
        {
            pageContext.setAttribute("allowAddLayout", Boolean.FALSE, PAGE_SCOPE);
          
        }
    %>

<%@page import="java.util.Set"%>
<c:set var="profiledPage" value="${psrc.page.path}" scope="page"/>
    

  <%-- END customizer page scoped variables and declarations --%>  
  
  <%-- BEGIN customizer content --%>

  
  <%-- END customizer content --%>
