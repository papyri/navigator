<%@page import="javax.portlet.*"  pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%>
<%!
static final String SRC = "http://iris.cul.columbia.edu:8080/erez4/fsi4/fsi.swf?images3dURL=http%3A//iris.cul.columbia.edu%3A8080/fsi-config/QQcnQQ.fsi%3Fcollection%3DAPIS%26subcollection%3DQQsubQQ&cfg=http%3A//iris.cul.columbia.edu%3A8080/erez4/erez%3Fcmd%3Dview%26vtl%3Dfsi/viewer_textbox.xml%26tmp%3Dfsi%26escape%3Dnone%26encode%3Dutf-8%26src%3D%24src&MenuAlign=TR";
%>
        <%@page import="info.papyri.navigator.portlet.XREFPortlet"%>
<%@page import="info.papyri.navigator.portlet.FSIImagePortlet"%>
<portlet:defineObjects />
<%
String src = "";
String cn = (String)renderRequest.getAttribute(FSIImagePortlet.CN_ATTR);
if (cn != null){
    String collection = XREFPortlet.getAPISCollection(cn);
    String number = cn.substring(cn.lastIndexOf(':')+1);
    src = SRC.replaceAll("QQcnQQ",collection + ".apis." + number);
    src = src.replaceAll("QQsubQQ",cn.substring(cn.length() - 1));
}
String width = "";
String height = "";
if (WindowState.MAXIMIZED.equals(renderRequest.getWindowState())){
   width="800";
   height="600";
}
else{
    width="400";
    height="400";
}

%>
<table class="t1" cellspacing="0" cellpadding="0" border="0"><tr><td align="center" valign="middle" class="t1">
<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,65,0" width="<%=width %>" height="<%=height%>">
<param name="movie" value="<%=src %>"/>
<param name="bgcolor" value="FFFFFF" />
<embed src="<%=src %>"
width="<%=width%>"
height="<%=height%>"
bgcolor="FFFFFF"
type="application/x-shockwave-flash"
pluginspage="http://www.macromedia.com/go/getflashplayer">
</embed>
</object>

</td></tr></table><!-- original wxh = 640x725 -->
