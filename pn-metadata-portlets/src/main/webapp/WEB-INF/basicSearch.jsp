<%@page import="edu.columbia.apis.*,edu.columbia.apis.portlet.*,java.util.*,javax.portlet.*,org.apache.lucene.document.*" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
        <portlet:defineObjects />
<%! final String SELECTED = " selected=\"selected\""; %>
    <% 
    String afterEra = (request.getParameter("afterEra") == null)?"":request.getParameter("afterEra");
    String beforeEra = (request.getParameter("beforeEra") == null)?"":request.getParameter("beforeEra");
    String afterYear = (request.getParameter("afterYear") == null)?"":request.getParameter("afterYear");
    String beforeYear = (request.getParameter("beforeYear") == null)?"":request.getParameter("beforeYear");
    String aCEs = (afterEra.equals("CE"))?SELECTED:"";
    String bCEs = (afterEra.equals("CE"))?SELECTED:"";
    String aBCEs = (afterEra.equals("BCE"))?SELECTED:"";
    String bBCEs = (afterEra.equals("BCE"))?SELECTED:"";
   %>
    <form action="<portlet:renderURL />" method="get" name="<portlet:namespace />basicSearch">
    <label title="On or after year" for="<portlet:namespace />afterYear" >On or after:</label>
    <input id="<portlet:namespace />afterYear" name="afterYear" size="5" type="text" value="<%=afterYear %>"/>
    <label title="On or after era" for="<portlet:namespace />afterEra" ></label>
    <select id="<portlet:namespace />afterEra" name="afterEra" value="<%=afterEra %>"/>
        <option value="" >[Era]</option>
        <option value="CE" <%=aCEs%>>C.E.</option>
        <option value="BCE" <%=bCEs%>>B.C.E.</option>
    </select>
    <br/>
    <label title="On or before era" for="<portlet:namespace />beforeYear">On or before:</label>
    <input id="<portlet:namespace />beforeYear" name="beforeYear" size="5" type="text" value="<%=beforeYear %>"/>
    <label title="On or before era" for="<portlet:namespace />beforeEra" ></label>
    <select id="<portlet:namespace />beforeEra" name="beforeEra" type="select" value="<%=beforeEra %>" >
        <option value="" >[Era]</option>
        <option value="CE" <%=aBCEs%>>C.E.</option>
        <option value="BCE" <%=bBCEs%>>B.C.E.</option>
    </select>
    <br/>
    <label title="Collection" for="<portlet:namespace />collection" >By Collection:</label>
                        <select id="<portlet:namespace />collection" name="collection">
                          <option value="" selected="selected">[All]</option>
                          <option value="berenike">Berenike</option>

                          <option value="berkeley">Berkeley</option>
                          <option value="chicago">Chicago</option>
                          <option value="columbia">Columbia</option>
			  <option value="sacramento">CSU Sacramento</option>
                          <option value="duke">Duke</option>
                          <option value="hermitage">Hermitage</option>

                          <option value="michigan">Michigan</option>
			  <option value="nyu">NYU</option>
			  <option value="oslo">Oslo</option>
			  <option value="petra">Petra</option>
			  <option value="princeton">Princeton</option>
			  <option value="psr">PSR, Bade Museum</option>

			  <option value="perkins">SMU Perkins</option>
			  <option value="stanford">Stanford</option>
                          <option value="toronto">Toronto</option>
                          <option value="trimithis">Trimithis (Columbia)</option>
                          <option value="upenn">UPenn</option>
                          <option value="wisconsin">Wisconsin</option>

			  <option value="pullman">WSU Pullman</option>
                          <option value="yale">Yale</option>
                        </select>
    <br/>
    <input type="submit" value="Go!" />
    </form>