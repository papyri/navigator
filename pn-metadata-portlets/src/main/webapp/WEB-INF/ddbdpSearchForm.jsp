<%@page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%>
        <portlet:defineObjects />
    <form action="<portlet:renderURL />" method="get" name="<portlet:namespace />ddbdp">
    <label title="beta code pattern" for="<portlet:namespace />pattern" >Enter beta code search string:</label>
    <input id="<portlet:namespace />pattern" name="ddbdpPattern" type="text" />
    <% String cn = request.getParameter("");
       if (cn != null){
           cn = cn.trim();
       }
       else {
           cn = "";
       }
       
    %>
    <input type="hidden" name="controlName" value="<%=cn %>" />
    <input type="submit" value="Go!" />
    <p>Enter query in beta code.  Example: <b>*)abouqi/w|</b> for <em>Abouthios</em></p>
        </form>    