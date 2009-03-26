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


  <%--
    /**
    * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
    */
  --%>

  <%
   

    //get the messages resourcebundle
    Locale _tmpLocale = (Locale) renderRequest.getLocale();
    ResourceBundle _messages = (ResourceBundle) portletConfig.getResourceBundle(renderRequest.getLocale());
    pageContext.setAttribute("messages", _messages, PAGE_SCOPE);
    //Now set the JSTL default format bundle ** FORMAT BUNDLE **
    javax.servlet.jsp.jstl.core.Config.set(request,Config.FMT_LOCALIZATION_CONTEXT,
                                           new LocalizationContext(_messages, _tmpLocale));
    
    //head
    LayoutDecoration pageDecoration = _theme.getPageLayoutDecoration();
      // get the layout decoration header file
      String headerJSP = pageDecoration.getHeader();
      if (!(headerJSP.startsWith("/")))
      {
        headerJSP = "/" + headerJSP;
      }
      pageContext.setAttribute("layoutHeaderJSP", headerJSP, PAGE_SCOPE);
      
      // get the layout decoration footer file
      String footerJSP = pageDecoration.getFooter();
      if (!(footerJSP.startsWith("/")))
      {
        footerJSP = "/" + footerJSP;
      }
      pageContext.setAttribute("layoutFooterJSP", footerJSP, PAGE_SCOPE);
    
  %>

  <%-- ***** BEGIN header.jsp ***** --%>
    <c:import url="${layoutHeaderJSP}"></c:import>
  <%-- ***** END header.jsp ***** --%>

  <%-- ***** BEGIN help contents ***** --%>
  <div id="portlet-help" class="portlet-Jetspeed" >
    <table border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
        <td class="PContent">
          <h2><fmt:message key="portal.help.title"/></h2>
          <h3><fmt:message key="portal.page.help.title"/></h3>
          <p>
            <img src="decorations/images/view.gif" alt="view" border="0" /> 
            <fmt:message key="portal.page.help.view"/>
          </p>
          <p>
            <img src="decorations/images/help.gif" alt="help" border="0" /> 
              <fmt:message key="portal.page.help.help"/>
          </p>
          <p>
            <img src="decorations/images/edit.gif" alt="edit" border="0" /> 
            <fmt:message key="portal.page.help.edit"/>
          </p>
          <p>
            <img src="decorations/layout/images/select.gif" alt="select" border="0" />
            <fmt:message key="portal.portlet.help.title"/>
          </p>
          <h3><fmt:message key="portal.help.title"/></h3>
          <p>
            <img src="decorations/images/minimized.gif" alt="minimize" border="0" />
            <fmt:message key="portal.portlet.help.minimize"/>
          </p>
          <p>
            <img src="decorations/images/maximized.gif" alt="maximize" border="0" />
            <fmt:message key="portal.portlet.help.maximize"/>
          </p>
          <p>
            <img src="decorations/images/restore.gif" alt="restore" border="0" />
            <fmt:message key="portal.portlet.help.restore"/>
          </p>
          <p>
             <img src="decorations/images/edit.gif" alt="edit" border="0" />
             <fmt:message key="portal.portlet.help.edit"/>
          </p>
          <p>
            <img src="decorations/images/print.gif" alt="print" border="0" />
            <fmt:message key="portal.portlet.help.print"/>
          </p>
          <h4><fmt:message key="portal.portlet.help.edit.title"/></h4>
          <p>
            <img src="decorations/images/close.gif" alt="close" border="0" />
            <fmt:message key="portal.portlet.help.close"/>
          </p>
          <p>
            <img src="decorations/layout/images/movePortletUp.gif" alt="move up" border="0" />
            <fmt:message key="portal.portlet.help.movePortletUp"/>
          </p>
          <p>
            <img src="decorations/layout/images/movePortletDown.gif" alt="move down" border="0" />
            <fmt:message key="portal.portlet.help.movePortletDown"/>
          </p>
          <p>
            <img src="decorations/layout/images/movePortletLeft.gif" alt="move left" border="0" />
            <fmt:message key="portal.portlet.help.movePortletLeft"/>
          </p>
          <p>
            <img src="decorations/layout/images/movePortletRight.gif" alt="move right" border="0" />
            <fmt:message key="portal.portlet.help.movePortletRight"/>
          </p>
        </td>
      </tr>
    </table>
  </div>

  <%-- ***** END help contents ***** --%>

  <%-- ***** BEGIN footer.jsp ***** --%>
    <c:import url="${layoutFooterJSP}"></c:import>
  <%-- ***** END footer.jsp ***** --%>
  