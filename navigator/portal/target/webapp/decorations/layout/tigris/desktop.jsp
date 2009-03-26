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
<%@ page language="java" import="org.apache.jetspeed.desktop.JetspeedDesktopContext" session="true" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="org.apache.jetspeed.request.RequestContext"%>
<%@ page import="org.apache.jetspeed.Jetspeed" %>
<%@ page import="org.apache.jetspeed.PortalReservedParameters" %>
<%@ page import="org.apache.jetspeed.om.page.Fragment" %>
<%@ page import="org.apache.jetspeed.decoration.Theme" %>
<%@ page import="org.apache.jetspeed.decoration.Decoration" %>
<% 
    JetspeedDesktopContext desktop = (JetspeedDesktopContext)request.getAttribute(JetspeedDesktopContext.DESKTOP_CONTEXT_ATTRIBUTE);
    RequestContext requestContext = (RequestContext)request.getAttribute(RequestContext.REQUEST_PORTALENV);

    String layoutStyleClass = "layout-" + desktop.getLayoutDecorationName();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html> <!-- .jsp --> <!-- NOTE: do not use strict doctype - see dojo svn log for FloatingPane.js -->
<head>
<meta http-equiv="Content-type" content="text/html; charset=UTF-8" />
<meta http-equiv="Content-style-type" content="text/css" />

<%= desktop.getHeaderResource().getNamedContentForPrefix( "header.dojo" )%>

<%= desktop.getHeaderResource().getContent()%>

</head>

<body class="<%= layoutStyleClass %>">
<!-- Start Jetspeed Page -->
<div class="<%= layoutStyleClass %>" id="jetspeedPage">
<div id="banner" style="position: static">    <!-- BOZO: set to absolute in stylesheet - don't know why - no apparent reason -->
  <table>
    <tr>
      <td>
        <div class='logo'>
        <img src='<%= desktop.getLayoutBaseUrl("images/Jetspeed_blue_med.png") %>' alt="Logo" border="0"/>
        </div>
      </td>
      <td>
        <div align="right" id="login">
          &nbsp;
        </div>
      </td>
    </tr>
  </table>
</div>
<div widgetId="jetspeed-menu-pages" dojoType="jetspeed:PortalTabContainer" style="width: 100%; margin-top: 2px; margin-left: -1px"></div>
<div widgetId="jetspeed-menu-breadcrumbs" dojoType="jetspeed:PortalBreadcrumbContainer" style="width: 100%; margin-top: 2px; margin-left: -1px"></div>
<table cellpadding="0" cellspacing="0" border="0" width="100%" id="main">
<tr>
<td id="leftcol">
<div widgetId="jetspeed-menu-navigations" dojoType="jetspeed:PortalAccordionContainer" style=""></div>
</td>
<td id="jetspeedDesktopCell">
<!-- Start Jetspeed Desktop -->
<div class="<%= layoutStyleClass %>" id="jetspeedDesktop"></div>
<!-- End Jetspeed Desktop -->
</td>
</tr>
</table>
<!-- Start Taskbar -->
<!-- (when we don't want a taskbar - set windowState to "minimized", otherwise omit windowState) -->
<!-- <div dojoType="jetspeed:PortalTaskBar" id="jetspeedTaskbar" style="background-color: #666; width: 98%; bottom: 5px; height: 110px" windowState="minimized" resizable="false"></div> -->
<!-- End Taskbar -->
</div>
<!-- End Jetspeed Page -->
  <p>
     <img src='<%= desktop.getLayoutBaseUrl("images/Jetspeed_blue_sm.png") %>' alt="Jetspeed 2 Powered" border="0" />
    </p>
<!-- page level loading indicator (associated with layout decoration resource: desktop.action.loadpage) -->
<!-- js-showloading-img is controlled by desktop.loading.img* properties, but src still needs to be set in content due to IE quirks -->
<div id="js-showloading" class="js-showloading" style="display: none">
    <table cellpadding="0" cellspacing="0" border="0">
        <tbody>
            <tr>
                <td class="js-showloading-left"></td>
                <td class="js-showloading-middle">
                    <div>
                        <div class="js-showloading-content" id="js-showloading-content">Loading&#133;</div>
                        <div class="js-showloading-imgcontainer"><img id="js-showloading-img" src='<%= desktop.getLayoutBaseUrl("images/desktop/loading/loaddots.gif") %>' border="0"/></div>
                    </div>
                </td>
                <td class="js-showloading-right"></td>
            </tr>
        </tbody>
    </table>
</div>
</body>
</html>
