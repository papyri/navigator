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
<html> <!-- .jsp --> <!-- NOTE: do not use strict doctype - see dojo svn log for FloatingPane.js -->
<head>
<meta http-equiv="Content-type" content="text/html; charset=UTF-8" />
<meta http-equiv="Content-style-type" content="text/css" />

<%= desktop.getHeaderResource().getNamedContentForPrefix( "header.dojo" )%>

<%= desktop.getHeaderResource().getContent()%>

<script language="JavaScript" type="text/javascript">
    function notifyRetrieveAllMenusFinished()
    {
        dojo.debug( "window.notifyRetrieveAllMenusFinished" );
    }
    dojo.event.connect( jetspeed, "notifyRetrieveAllMenusFinished", "notifyRetrieveAllMenusFinished" );
</script>
</head>

<body class="<%= layoutStyleClass %>">
<!-- Start Jetspeed Page -->
<div class="<%= layoutStyleClass %>" id="jetspeedPage">
<table cellpadding="0" cellspacing="0" border="0" width="100%" id="main">
<tr>
<td id="jetspeedDesktopCell">
<!-- Start Jetspeed Desktop -->
<div class="<%= layoutStyleClass %>" id="jetspeedDesktop"></div>
<!-- End Jetspeed Desktop -->
</td>
</tr>
</table>
</div>
<!-- End Jetspeed Page -->
</body>
</html>
