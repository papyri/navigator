<%@page import="java.util.*,javax.portlet.*" pageEncoding="UTF-8" %>
<h2 class="apis-portal-title">There was an error!</h2>
<p><% PortletRequest portletRequest = (PortletRequest)
        request.getAttribute("javax.portlet.request");
Exception e = (Exception)portletRequest.getAttribute("apis:exception");
%>
<%= e %>
</p>