<%@page import="info.papyri.portlet.*,java.util.*,javax.portlet.*,org.apache.lucene.document.*" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%!

%>
        <portlet:defineObjects />
    <% String pub = "";
    String apis = "";
    if ("apis".equals(request.getParameter("xrefSchema"))) apis = request.getParameter("xrefPattern");
    String series = (request.getParameter("xrefSeries") == null)?"":request.getParameter("xrefSeries");
    String volume = (request.getParameter("xrefVolume") == null)?"":request.getParameter("xrefVolume");
    String document = (request.getParameter("xrefDocument") == null)?"":request.getParameter("xrefDocument");
    if ("pub".equals(request.getParameter("xrefSchema"))){
        pub = request.getParameter("xrefPattern");
    }
    %>
    <form action="<portlet:renderURL />" method="get" name="<portlet:namespace />xrefpub">
    <label title="id pattern" for="<portlet:namespace />xrefPub" >Enter publication pattern:</label>
    <input id="<portlet:namespace />xrefSeries" name="xrefSeries" type="text" value="<%=pub %>"/>
    <%
    if (request.getParameter("controlName") != null){
    %>
    <input type="hidden" name="controlName" value="<%=request.getParameter("controlName").trim() %>" />
    <%
    }%>
    <input type="hidden" name="xrefSchema" value="pub" />
    <input type="submit" value="Go!" />
    </form>
    <form action="<portlet:renderURL />" method="get" name="<portlet:namespace />xrefapis">
    <label title="id pattern" for="<portlet:namespace />xrefApis" >Enter apis number pattern:</label>
    <input id="<portlet:namespace />xrefApis" name="xrefPattern" type="text" value="<%=apis %>"/>
    <%
    if (request.getParameter("controlName") != null){
    %>
    <input type="hidden" name="controlName" value="<%=request.getParameter("controlName").trim() %>" />
    <%
    }%>
    <input type="hidden" name="xrefSchema" value="apis" />
    <input type="submit" value="Go!" />
    </form>   
    <form action="<portlet:renderURL />" method="get" name="<portlet:namespace />xrefnew">
    <table>
		<tr>
			<th class="rowheader">Publication Number:</th>

			<td colspan="2"><label
				for="pubnum_coll"">Collection:</label> <select id="pubnum_coll"
				name="pubnum_coll">
				<option value="">[Select]</option>
				<option value="O.Berenike">O.Berenike</option>
				<option value="O.Mich.">O.Mich.</option>
				<option value="O.Oslo">O.Oslo</option>

				<option value="P.Col." selected="selected">P.Col.</option>
				<option value="P.Fay.">P.Fay.</option>
				<option value="P.Hib.">P.Hib.</option>
				<option value="P.Lond.">P.Lond.</option>
				<option value="P.Mich.">P.Mich.</option>
				<option value="P.NYU.">P.NYU.</option>

				<option value="P.O.I.">P.O.I.</option>
				<option value="P.Oslo">P.Oslo</option>
				<option value="P.Oxy.">P.Oxy.</option>
				<option value="P.Petra">P.Petra</option>
				<option value="P.Princ.">P.Princ.</option>
				<option value="P.Ross.Georg.">P.Ross. Georg.</option>

				<option value="P.Tebt.">P.Tebt.</option>
				<option value="P.Wisc.">P.Wisc.</option>
				<option value="P.Yale">P.Yale</option>
				<option value="SB">SB</option>
			</select>

                       <label for="pubnum_vol">Volume:</label> <input id="pubnum_vol"
				name="pubnum_vol" size="5" maxlength="25" class="forminput10"
				value="III" type="text" />

                  <label for="pubnum_page">Document:</label>
			<input id="pubnum_page" name="pubnum_page" size="5" maxlength="25" class="forminput10"
				value="*" type="text" /></td>
		</tr>    
		<tr>
			<th class="rowheader">APIS Number:</th>

			<td class="caption12" align="left">Institution:
			<select name="apisnum_inst">
				<option value="" selected="selected">[Select]</option>
				<option value="berenike">Berenike</option>
				<option value="berkeley">Berkeley</option>
				<option value="chicago">Chicago</option>

				<option value="columbia">Columbia</option>
				<option value="duke">Duke</option>
				<option value="hermitage">Hermitage</option>
				<option value="michigan">Michigan</option>
				<option value="nyu">NYU</option>
				<option value="oslo">Oslo</option>

				<option value="perkins">Perkins</option>
				<option value="petra">Petra</option>
				<option value="princeton">Princeton</option>
				<option value="psr">PSR</option>
				<option value="pullman">Pullman</option>
				<option value="sacramento">Sacramento</option>

				<option value="stanford">Stanford</option>
				<option value="toronto">Toronto</option>
				<option value="trimithis">Trimithis</option>
				<option value="upenn">UPenn</option>
				<option value="wisconsin">Wisconsin</option>
				<option value="yale">Yale</option>

			</select> <span style="font-weight: normal;">.apis.</span>
                  <label for="apisnum_num">Number:
			<input id="apisnum_num" name="apisnum_num" size="5" maxlength="25" class="forminput10"
				value="" type="text" /> <span class="comment"
				style="font-weight: normal;">e.g."p163"</span></td>
		</tr>    </table>
    <input type="hidden" name="xrefSchema" value="alt" />
    <input type="submit" value="Go!" />
    </form>
    </form>